package com.mir2.service;

import com.mir2.core.model.Guild;
import com.mir2.core.model.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 行会服务类
 * 
 * <p>提供行会相关的业务逻辑处理。
 * 对应原M2Engine中的行会管理系统。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class GuildService {
    
    @Autowired
    private PlayerService playerService;
    
    /** 行会ID生成器 */
    private final AtomicLong guildIdGenerator = new AtomicLong(1);
    
    /** 行会缓存 */
    private final Map<Long, Guild> guildCache = new ConcurrentHashMap<>();
    
    /** 行会名称索引 */
    private final Map<String, Guild> guildNameIndex = new ConcurrentHashMap<>();
    
    /**
     * 创建行会
     * 
     * @param guildName 行会名称
     * @param masterPlayer 会长
     * @return 创建的行会，如果失败返回null
     */
    public Guild createGuild(String guildName, Player masterPlayer) {
        // 检查行会名称是否已存在
        if (guildNameIndex.containsKey(guildName)) {
            log.warn("行会名称已存在: {}", guildName);
            return null;
        }
        
        // 检查玩家是否已加入其他行会
        if (masterPlayer.getGuildId() != null) {
            log.warn("玩家 {} 已加入其他行会", masterPlayer.getName());
            return null;
        }
        
        // 创建行会
        long guildId = guildIdGenerator.getAndIncrement();
        Guild guild = new Guild(guildId, guildName, masterPlayer.getName());
        
        // 添加会长到行会
        Guild.GuildMember master = new Guild.GuildMember(masterPlayer.getName(), 
                Guild.GuildRank.MASTER, System.currentTimeMillis());
        guild.addMember(master);
        
        // 更新玩家信息
        masterPlayer.setGuildId(guildId);
        masterPlayer.setGuildName(guildName);
        masterPlayer.setGuildRank(Guild.GuildRank.MASTER.getName());
        
        // 添加到缓存
        guildCache.put(guildId, guild);
        guildNameIndex.put(guildName, guild);
        
        log.info("创建行会: {} 会长: {}", guildName, masterPlayer.getName());
        return guild;
    }
    
    /**
     * 解散行会
     * 
     * @param guildId 行会ID
     * @param masterPlayer 会长
     * @return 是否解散成功
     */
    public boolean disbandGuild(long guildId, Player masterPlayer) {
        Guild guild = guildCache.get(guildId);
        if (guild == null) {
            log.warn("行会不存在: {}", guildId);
            return false;
        }
        
        // 检查是否为会长
        if (!guild.getMasterName().equals(masterPlayer.getName())) {
            log.warn("玩家 {} 不是行会 {} 的会长", masterPlayer.getName(), guild.getName());
            return false;
        }
        
        // 清除所有成员的行会信息
        for (Guild.GuildMember member : guild.getMembers()) {
            // 更新在线玩家的行会信息
            Player onlinePlayer = playerService.getOnlinePlayer(member.getName());
            if (onlinePlayer != null) {
                onlinePlayer.setGuildId(null);
                onlinePlayer.setGuildName(null);
                onlinePlayer.setGuildRank(null);
                
                // 发送行会解散通知
                sendGuildMessage(onlinePlayer, "行会 " + guild.getName() + " 已被解散", 
                        MessageType.GUILD_DISBAND);
            }
        }
        
        // 从缓存中移除
        guildCache.remove(guildId);
        guildNameIndex.remove(guild.getName());
        
        log.info("解散行会: {} 会长: {}", guild.getName(), masterPlayer.getName());
        return true;
    }
    
    /**
     * 邀请玩家加入行会
     * 
     * @param guildId 行会ID
     * @param inviterName 邀请者名称
     * @param targetPlayer 目标玩家
     * @return 是否邀请成功
     */
    public boolean invitePlayer(long guildId, String inviterName, Player targetPlayer) {
        Guild guild = guildCache.get(guildId);
        if (guild == null) {
            log.warn("行会不存在: {}", guildId);
            return false;
        }
        
        // 检查邀请者是否有权限
        if (!guild.canInvite(inviterName)) {
            log.warn("玩家 {} 没有邀请权限", inviterName);
            return false;
        }
        
        // 检查目标玩家是否已加入其他行会
        if (targetPlayer.getGuildId() != null) {
            log.warn("玩家 {} 已加入其他行会", targetPlayer.getName());
            return false;
        }
        
        // 检查行会是否已满
        if (guild.isFull()) {
            log.warn("行会 {} 成员已满", guild.getName());
            return false;
        }
        
        // 添加成员到行会
        Guild.GuildMember member = new Guild.GuildMember(targetPlayer.getName(), 
                Guild.GuildRank.MEMBER, System.currentTimeMillis());
        guild.addMember(member);
        
        // 更新玩家信息
        targetPlayer.setGuildId(guildId);
        targetPlayer.setGuildName(guild.getName());
        targetPlayer.setGuildRank(Guild.GuildRank.MEMBER.getName());
        
        log.info("玩家 {} 邀请 {} 加入行会: {}", inviterName, targetPlayer.getName(), guild.getName());
        return true;
    }
    
    /**
     * 踢出行会成员
     * 
     * @param guildId 行会ID
     * @param kickerName 踢出者名称
     * @param targetName 目标玩家名称
     * @return 是否踢出成功
     */
    public boolean kickMember(long guildId, String kickerName, String targetName) {
        Guild guild = guildCache.get(guildId);
        if (guild == null) {
            log.warn("行会不存在: {}", guildId);
            return false;
        }
        
        // 检查踢出者是否有权限
        if (!guild.canKick(kickerName, targetName)) {
            log.warn("玩家 {} 没有踢出权限", kickerName);
            return false;
        }
        
        // 从行会中移除成员
        if (guild.removeMember(targetName)) {
            // 更新在线玩家的行会信息
            Player onlinePlayer = playerService.getOnlinePlayer(targetName);
            if (onlinePlayer != null) {
                onlinePlayer.setGuildId(null);
                onlinePlayer.setGuildName(null);
                onlinePlayer.setGuildRank(null);
                
                // 发送踢出通知
                sendGuildMessage(onlinePlayer, "你已被踢出行会 " + guild.getName(), 
                        MessageType.GUILD_KICK);
            }
            
            log.info("玩家 {} 被踢出行会: {}", targetName, guild.getName());
            return true;
        }
        
        return false;
    }
    
    /**
     * 玩家离开行会
     * 
     * @param player 玩家
     * @return 是否离开成功
     */
    public boolean leaveGuild(Player player) {
        Long guildId = player.getGuildId();
        if (guildId == null) {
            log.warn("玩家 {} 未加入任何行会", player.getName());
            return false;
        }
        
        Guild guild = guildCache.get(guildId);
        if (guild == null) {
            log.warn("行会不存在: {}", guildId);
            return false;
        }
        
        // 检查是否为会长
        if (guild.getMasterName().equals(player.getName())) {
            log.warn("会长不能离开行会，请先转让会长或解散行会");
            return false;
        }
        
        // 从行会中移除成员
        if (guild.removeMember(player.getName())) {
            // 清除玩家行会信息
            player.setGuildId(null);
            player.setGuildName(null);
            player.setGuildRank(null);
            
            log.info("玩家 {} 离开行会: {}", player.getName(), guild.getName());
            return true;
        }
        
        return false;
    }
    
    /**
     * 设置成员职位
     * 
     * @param guildId 行会ID
     * @param setterName 设置者名称
     * @param targetName 目标玩家名称
     * @param rank 新职位
     * @return 是否设置成功
     */
    public boolean setMemberRank(long guildId, String setterName, String targetName, Guild.GuildRank rank) {
        Guild guild = guildCache.get(guildId);
        if (guild == null) {
            log.warn("行会不存在: {}", guildId);
            return false;
        }
        
        // 检查设置者是否有权限
        if (!guild.canSetRank(setterName, targetName, rank)) {
            log.warn("玩家 {} 没有设置职位权限", setterName);
            return false;
        }
        
        // 设置成员职位
        Guild.GuildMember member = guild.getMember(targetName);
        if (member != null) {
            member.setRank(rank);
            
            // 更新在线玩家的行会信息
            Player onlinePlayer = playerService.getOnlinePlayer(targetName);
            if (onlinePlayer != null) {
                onlinePlayer.setGuildRank(rank.getName());
                
                // 发送职位变更通知
                sendGuildMessage(onlinePlayer, "你的行会职位已变更为: " + rank.getName(), 
                        MessageType.GUILD_RANK_CHANGE);
            }
            
            log.info("玩家 {} 职位设置为: {}", targetName, rank.getName());
            return true;
        }
        
        return false;
    }
    
    /**
     * 转让会长
     * 
     * @param guildId 行会ID
     * @param currentMaster 当前会长
     * @param newMasterName 新会长名称
     * @return 是否转让成功
     */
    public boolean transferMaster(long guildId, Player currentMaster, String newMasterName) {
        Guild guild = guildCache.get(guildId);
        if (guild == null) {
            log.warn("行会不存在: {}", guildId);
            return false;
        }
        
        // 检查是否为会长
        if (!guild.getMasterName().equals(currentMaster.getName())) {
            log.warn("玩家 {} 不是会长", currentMaster.getName());
            return false;
        }
        
        // 检查新会长是否为成员
        Guild.GuildMember newMember = guild.getMember(newMasterName);
        if (newMember == null) {
            log.warn("玩家 {} 不是行会成员", newMasterName);
            return false;
        }
        
        // 转让会长
        Guild.GuildMember oldMaster = guild.getMember(currentMaster.getName());
        if (oldMaster != null) {
            oldMaster.setRank(Guild.GuildRank.MEMBER);
        }
        
        newMember.setRank(Guild.GuildRank.MASTER);
        guild.setMasterName(newMasterName);
        
        // 更新玩家信息
        currentMaster.setGuildRank(Guild.GuildRank.MEMBER.getName());
        
        log.info("行会 {} 会长转让: {} -> {}", guild.getName(), currentMaster.getName(), newMasterName);
        return true;
    }
    
    /**
     * 获取行会信息
     * 
     * @param guildId 行会ID
     * @return 行会信息
     */
    public Guild getGuild(long guildId) {
        return guildCache.get(guildId);
    }
    
    /**
     * 根据名称获取行会
     * 
     * @param guildName 行会名称
     * @return 行会信息
     */
    public Guild getGuildByName(String guildName) {
        return guildNameIndex.get(guildName);
    }
    
    /**
     * 获取所有行会
     * 
     * @return 行会列表
     */
    public List<Guild> getAllGuilds() {
        return guildCache.values().stream().collect(Collectors.toList());
    }
    
    /**
     * 搜索行会
     * 
     * @param keyword 关键词
     * @return 匹配的行会列表
     */
    public List<Guild> searchGuilds(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        
        String lowerKeyword = keyword.toLowerCase();
        return guildCache.values().stream()
                .filter(guild -> guild.getName().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取行会排行榜
     * 
     * @param limit 限制数量
     * @return 排行榜列表
     */
    public List<Guild> getGuildRanking(int limit) {
        return guildCache.values().stream()
                .sorted((g1, g2) -> Integer.compare(g2.getLevel(), g1.getLevel()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * 增加行会资金
     * 
     * @param guildId 行会ID
     * @param amount 金额
     * @return 是否增加成功
     */
    public boolean addGuildFunds(long guildId, long amount) {
        Guild guild = guildCache.get(guildId);
        if (guild == null) {
            return false;
        }
        
        guild.setFunds(guild.getFunds() + amount);
        log.debug("行会 {} 资金增加: {}", guild.getName(), amount);
        return true;
    }
    
    /**
     * 扣除行会资金
     * 
     * @param guildId 行会ID
     * @param amount 金额
     * @return 是否扣除成功
     */
    public boolean deductGuildFunds(long guildId, long amount) {
        Guild guild = guildCache.get(guildId);
        if (guild == null) {
            return false;
        }
        
        if (guild.getFunds() < amount) {
            log.warn("行会 {} 资金不足", guild.getName());
            return false;
        }
        
        guild.setFunds(guild.getFunds() - amount);
        log.debug("行会 {} 资金扣除: {}", guild.getName(), amount);
        return true;
    }
    
    /**
     * 设置行会公告
     * 
     * @param guildId 行会ID
     * @param setterName 设置者名称
     * @param notice 公告内容
     * @return 是否设置成功
     */
    public boolean setGuildNotice(long guildId, String setterName, String notice) {
        Guild guild = guildCache.get(guildId);
        if (guild == null) {
            return false;
        }
        
        // 检查权限
        if (!guild.canSetNotice(setterName)) {
            log.warn("玩家 {} 没有设置公告权限", setterName);
            return false;
        }
        
        guild.setNotice(notice);
        log.info("行会 {} 公告更新: {}", guild.getName(), notice);
        return true;
    }
    
    /**
     * 获取行会统计信息
     * 
     * @return 统计信息
     */
    public String getGuildStatistics() {
        int totalGuilds = guildCache.size();
        int totalMembers = guildCache.values().stream()
                .mapToInt(guild -> guild.getMembers().size())
                .sum();
        
        return String.format("行会总数: %d, 成员总数: %d", totalGuilds, totalMembers);
    }
    
    /**
     * 检查行会名称是否可用
     * 
     * @param guildName 行会名称
     * @return 是否可用
     */
    public boolean isGuildNameAvailable(String guildName) {
        return !guildNameIndex.containsKey(guildName);
    }
    
    /**
     * 发送行会消息
     */
    private void sendGuildMessage(Player player, String message, MessageType type) {
        // 根据消息类型设置不同的通知方式
        switch (type) {
            case GUILD_DISBAND:
                log.info("向玩家 {} 发送行会解散通知: {}", player.getName(), message);
                break;
            case GUILD_KICK:
                log.info("向玩家 {} 发送踢出通知: {}", player.getName(), message);
                break;
            case GUILD_RANK_CHANGE:
                log.info("向玩家 {} 发送职位变更通知: {}", player.getName(), message);
                break;
            default:
                log.info("向玩家 {} 发送行会消息: {}", player.getName(), message);
                break;
        }
        
        // 这里可以通过网络发送消息给玩家
        // networkService.sendMessage(player, message);
    }
    
    /**
     * 消息类型枚举
     */
    public enum MessageType {
        GUILD_DISBAND,      // 行会解散
        GUILD_KICK,         // 被踢出行会
        GUILD_RANK_CHANGE,  // 职位变更
        GUILD_NOTICE        // 行会公告
    }
} 