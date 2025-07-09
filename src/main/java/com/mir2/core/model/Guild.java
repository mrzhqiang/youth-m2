package com.mir2.core.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 行会类
 * 
 * <p>代表游戏中的行会组织，包含行会的基本信息、成员管理、等级系统等。
 * 对应原M2Engine中的行会系统。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Slf4j
public class Guild {
    
    /** 行会ID */
    private Long guildId;
    
    /** 行会名称 */
    private String name;
    
    /** 行会会长 */
    private String masterName;
    
    /** 行会等级 */
    private int level;
    
    /** 行会经验 */
    private long experience;
    
    /** 行会声望 */
    private int reputation;
    
    /** 行会资金 */
    private long fund;
    
    /** 行会宣言 */
    private String declaration;
    
    /** 行会公告 */
    private String notice;
    
    /** 最大成员数 */
    private int maxMembers;
    
    /** 当前成员数 */
    private int currentMembers;
    
    /** 行会状态 */
    private GuildStatus status;
    
    /** 行会成员列表 */
    private List<GuildMember> members;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 最后更新时间 */
    private LocalDateTime lastUpdateTime;
    
    /**
     * 构造函数
     * 
     * @param guildId 行会ID
     * @param name 行会名称
     * @param masterName 会长名称
     */
    public Guild(Long guildId, String name, String masterName) {
        this.guildId = guildId;
        this.name = name;
        this.masterName = masterName;
        this.level = 1;
        this.experience = 0;
        this.reputation = 0;
        this.fund = 0;
        this.declaration = "";
        this.notice = "";
        this.maxMembers = 50;
        this.currentMembers = 1;
        this.status = GuildStatus.ACTIVE;
        
        // 初始化集合
        this.members = new CopyOnWriteArrayList<>();
        
        this.createTime = LocalDateTime.now();
        this.lastUpdateTime = createTime;
        
        // 添加会长作为首个成员
        addMember(masterName, GuildRank.MASTER);
        
        log.info("创建行会: {} [会长: {}]", name, masterName);
    }
    
    /**
     * 添加成员
     * 
     * @param playerName 玩家名称
     * @param rank 职位
     * @return 是否添加成功
     */
    public boolean addMember(String playerName, GuildRank rank) {
        if (currentMembers >= maxMembers) {
            log.warn("行会 {} 成员已满，无法添加新成员: {}", name, playerName);
            return false;
        }
        
        // 检查是否已经是成员
        if (findMember(playerName) != null) {
            log.warn("玩家 {} 已经是行会 {} 的成员", playerName, name);
            return false;
        }
        
        GuildMember member = new GuildMember(playerName, rank);
        members.add(member);
        currentMembers++;
        
        log.info("行会 {} 添加成员: {} [职位: {}]", name, playerName, rank.getName());
        return true;
    }
    
    /**
     * 移除成员
     * 
     * @param playerName 玩家名称
     * @return 是否移除成功
     */
    public boolean removeMember(String playerName) {
        GuildMember member = findMember(playerName);
        if (member == null) {
            log.warn("玩家 {} 不是行会 {} 的成员", playerName, name);
            return false;
        }
        
        // 会长不能被移除
        if (member.getRank() == GuildRank.MASTER) {
            log.warn("无法移除行会 {} 的会长: {}", name, playerName);
            return false;
        }
        
        members.remove(member);
        currentMembers--;
        
        log.info("行会 {} 移除成员: {} [职位: {}]", name, playerName, member.getRank().getName());
        return true;
    }
    
    /**
     * 查找成员
     * 
     * @param playerName 玩家名称
     * @return 成员对象，如果不存在则返回null
     */
    public GuildMember findMember(String playerName) {
        return members.stream()
                .filter(member -> member.getPlayerName().equals(playerName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 增加行会经验
     * 
     * @param exp 经验值
     * @return 是否升级
     */
    public boolean addExperience(long exp) {
        if (exp <= 0) {
            return false;
        }
        
        long oldExp = this.experience;
        this.experience += exp;
        
        // 检查是否升级
        long requiredExp = calculateRequiredExp(level);
        if (this.experience >= requiredExp) {
            this.experience -= requiredExp;
            this.level++;
            
            // 升级后增加最大成员数
            this.maxMembers = calculateMaxMembers(level);
            
            log.info("行会 {} 升级到 {} 级！最大成员数: {}", name, level, maxMembers);
            return true;
        }
        
        log.debug("行会 {} 获得经验: {} (总经验: {})", name, exp, this.experience);
        return false;
    }
    
    /**
     * 计算升级所需经验
     * 
     * @param level 当前等级
     * @return 升级所需经验
     */
    private long calculateRequiredExp(int level) {
        if (level >= 20) { // 最高20级
            return Long.MAX_VALUE;
        }
        
        // 基础经验 * 等级系数
        long baseExp = 10000;
        double levelMultiplier = 1.8;
        
        return (long) (baseExp * Math.pow(levelMultiplier, level - 1));
    }
    
    /**
     * 计算最大成员数
     * 
     * @param level 行会等级
     * @return 最大成员数
     */
    private int calculateMaxMembers(int level) {
        return 50 + (level - 1) * 10; // 每级增加10人
    }
    
    /**
     * 获取行会信息
     * 
     * @return 行会信息字符串
     */
    public String getGuildInfo() {
        return String.format("行会: %s [等级: %d] [会长: %s] [成员: %d/%d] [资金: %d] [声望: %d]",
                name, level, masterName, currentMembers, maxMembers, fund, reputation);
    }
    
    /**
     * 行会状态枚举
     */
    public enum GuildStatus {
        ACTIVE(0, "正常"),
        DISBANDED(1, "解散"),
        SUSPENDED(2, "暂停");
        
        private final int code;
        private final String name;
        
        GuildStatus(int code, String name) {
            this.code = code;
            this.name = name;
        }
        
        public int getCode() { return code; }
        public String getName() { return name; }
    }
    
    /**
     * 行会职位枚举
     */
    public enum GuildRank {
        MASTER(0, "会长"),
        VICE_MASTER(1, "副会长"),
        ELDER(2, "长老"),
        ELITE(3, "精英"),
        MEMBER(4, "会员");
        
        private final int code;
        private final String name;
        
        GuildRank(int code, String name) {
            this.code = code;
            this.name = name;
        }
        
        public int getCode() { return code; }
        public String getName() { return name; }
        
        /**
         * 是否有管理权限
         * 
         * @return 是否有管理权限
         */
        public boolean hasManagePermission() {
            return this == MASTER || this == VICE_MASTER;
        }
        
        /**
         * 是否可以邀请成员
         * 
         * @return 是否可以邀请成员
         */
        public boolean canInviteMembers() {
            return this == MASTER || this == VICE_MASTER || this == ELDER;
        }
    }
    
    /**
     * 行会成员类
     */
    @Data
    public static class GuildMember {
        /** 玩家名称 */
        private String playerName;
        
        /** 职位 */
        private GuildRank rank;
        
        /** 加入时间 */
        private LocalDateTime joinTime;
        
        /** 贡献度 */
        private int contribution;
        
        /** 在线状态 */
        private boolean online;
        
        /** 最后在线时间 */
        private LocalDateTime lastOnlineTime;
        
        /**
         * 构造函数
         * 
         * @param playerName 玩家名称
         * @param rank 职位
         */
        public GuildMember(String playerName, GuildRank rank) {
            this.playerName = playerName;
            this.rank = rank;
            this.joinTime = LocalDateTime.now();
            this.contribution = 0;
            this.online = false;
            this.lastOnlineTime = LocalDateTime.now();
        }
        
        /**
         * 增加贡献度
         * 
         * @param amount 贡献值
         */
        public void addContribution(int amount) {
            this.contribution += amount;
        }
        
        /**
         * 设置在线状态
         * 
         * @param online 是否在线
         */
        public void setOnline(boolean online) {
            this.online = online;
            if (online) {
                this.lastOnlineTime = LocalDateTime.now();
            }
        }
    }
} 