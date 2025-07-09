package com.mir2.service;

import com.mir2.entity.Player;
import com.mir2.entity.MasterApprenticeInfo;
import com.mir2.core.model.Position;
import com.mir2.core.model.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MasterApprenticeService {
    
    private final Map<String, MasterApprenticeInfo> masterApprenticeMap = new ConcurrentHashMap<>();
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private InventoryService inventoryService;
    
    /**
     * 申请拜师
     */
    @Transactional
    public boolean requestMaster(Player apprentice, Player master) {
        // 检查师父是否已有师父
        if (hasMaster(master.getUsername())) {
            return false;
        }
        
        // 检查等级要求
        if (master.getLevel() < 35 || apprentice.getLevel() >= master.getLevel()) {
            return false;
        }
        
        // 检查师父徒弟数量限制
        if (getApprenticeCount(master.getUsername()) >= 3) {
            return false;
        }
        
        // 检查徒弟是否已有师父
        if (hasMaster(apprentice.getUsername())) {
            return false;
        }
        
        // 检查距离要求
        if (calculateDistance(apprentice.getPosition(), master.getPosition()) > 2) {
            return false;
        }
        
        // 检查声望要求
        if (master.getCreditPoint() < 100) {
            return false;
        }
        
        // 创建师徒关系
        MasterApprenticeInfo relationship = new MasterApprenticeInfo();
        relationship.setMasterName(master.getUsername());
        relationship.setApprenticeName(apprentice.getUsername());
        relationship.setStartDate(LocalDateTime.now());
        relationship.setExpBonus(0);
        relationship.setLevel(1);
        
        // 保存关系信息
        masterApprenticeMap.put(apprentice.getUsername(), relationship);
        
        // 师父获得荣誉点
        master.setCreditPoint(master.getCreditPoint() + 10);
        
        return true;
    }
    
    /**
     * 解除师徒关系
     */
    @Transactional
    public boolean dismissApprentice(Player master, String apprenticeName) {
        MasterApprenticeInfo relationship = masterApprenticeMap.get(apprenticeName);
        if (relationship == null || !relationship.getMasterName().equals(master.getUsername())) {
            return false;
        }
        
        // 移除师徒关系
        masterApprenticeMap.remove(apprenticeName);
        
        // 师父失去荣誉点
        master.setCreditPoint(Math.max(0, master.getCreditPoint() - 5));
        
        return true;
    }
    
    /**
     * 师父召唤徒弟
     */
    public boolean masterRecallApprentice(Player master, Player apprentice) {
        MasterApprenticeInfo relationship = masterApprenticeMap.get(apprentice.getUsername());
        if (relationship == null || !relationship.getMasterName().equals(master.getUsername())) {
            return false;
        }
        
        // 检查召唤条件
        if (relationship.getLevel() < 2) {
            return false;
        }
        
        // 检查召唤冷却
        if (relationship.getLastRecallTime() != null) {
            long cooldown = 300000; // 5分钟冷却
            if (System.currentTimeMillis() - relationship.getLastRecallTime() < cooldown) {
                return false;
            }
        }
        
        // 执行召唤
        apprentice.setPosition(master.getPosition());
        relationship.setLastRecallTime(System.currentTimeMillis());
        
        return true;
    }
    
    /**
     * 徒弟出师
     */
    @Transactional
    public boolean graduateApprentice(Player apprentice) {
        MasterApprenticeInfo relationship = masterApprenticeMap.get(apprentice.getUsername());
        if (relationship == null) {
            return false;
        }
        
        // 检查出师条件
        if (apprentice.getLevel() < 35) {
            return false;
        }
        
        // 移除师徒关系
        masterApprenticeMap.remove(apprentice.getUsername());
        
        // 师父获得奖励
        try {
            giveMasterGraduationReward(relationship.getMasterName(), apprentice);
        } catch (Exception e) {
            log.error("发放师父出师奖励失败", e);
        }
        
        return true;
    }
    
    /**
     * 获取经验加成
     */
    public int getExpBonus(String apprenticeName) {
        MasterApprenticeInfo relationship = masterApprenticeMap.get(apprenticeName);
        if (relationship == null) {
            return 0;
        }
        
        // 根据师徒关系等级计算经验加成
        int baseBonus = 10; // 基础10%加成
        int levelBonus = relationship.getLevel() * 5; // 每级额外5%
        
        return Math.min(baseBonus + levelBonus, 50); // 最大50%加成
    }
    
    /**
     * 获取师父名称
     */
    public String getMasterName(String apprenticeName) {
        MasterApprenticeInfo relationship = masterApprenticeMap.get(apprenticeName);
        return relationship != null ? relationship.getMasterName() : null;
    }
    
    /**
     * 获取徒弟列表
     */
    public List<String> getApprenticeList(String masterName) {
        return masterApprenticeMap.values().stream()
                .filter(r -> r.getMasterName().equals(masterName))
                .map(MasterApprenticeInfo::getApprenticeName)
                .collect(Collectors.toList());
    }
    
    /**
     * 检查是否有师父
     */
    public boolean hasMaster(String playerName) {
        return masterApprenticeMap.containsKey(playerName);
    }
    
    /**
     * 检查是否是师父
     */
    public boolean isMaster(String playerName) {
        return masterApprenticeMap.values().stream()
                .anyMatch(r -> r.getMasterName().equals(playerName));
    }
    
    /**
     * 获取徒弟数量
     */
    public int getApprenticeCount(String masterName) {
        return (int) masterApprenticeMap.values().stream()
                .filter(r -> r.getMasterName().equals(masterName))
                .count();
    }
    
    /**
     * 获取师徒关系信息
     */
    public MasterApprenticeInfo getRelationship(String apprenticeName) {
        return masterApprenticeMap.get(apprenticeName);
    }
    
    /**
     * 更新师徒关系等级
     */
    public void updateRelationshipLevel(String apprenticeName, int newLevel) {
        MasterApprenticeInfo relationship = masterApprenticeMap.get(apprenticeName);
        if (relationship != null) {
            relationship.setLevel(Math.min(newLevel, 10)); // 最大等级10
        }
    }
    
    /**
     * 师徒共享经验
     */
    public void shareExperience(Player apprentice, int exp) {
        MasterApprenticeInfo relationship = masterApprenticeMap.get(apprentice.getUsername());
        if (relationship == null) {
            return;
        }
        
        // 计算师父获得的经验
        int masterExp = exp / 10; // 师父获得徒弟经验的10%
        
        try {
            // 给师父发放经验
            giveMasterExperience(relationship.getMasterName(), masterExp);
            
            // 更新师徒关系经验
            relationship.setExpBonus(relationship.getExpBonus() + masterExp);
            
        } catch (Exception e) {
            log.error("师徒共享经验失败", e);
        }
    }
    
    /**
     * 计算两个位置之间的距离
     */
    private int calculateDistance(Position pos1, Position pos2) {
        int dx = pos1.getX() - pos2.getX();
        int dy = pos1.getY() - pos2.getY();
        return Math.max(Math.abs(dx), Math.abs(dy));
    }
    
    /**
     * 给师父发放出师奖励
     */
    private void giveMasterGraduationReward(String masterName, Player apprentice) {
        try {
            // 尝试获取在线的师父
            Player master = playerService.getOnlinePlayer(masterName);
            
            if (master != null) {
                // 发放金币奖励
                long goldReward = 100000 + (apprentice.getLevel() - 35) * 10000;
                master.addGold(goldReward);
                
                // 发放经验奖励
                long expReward = apprentice.getLevel() * 5000;
                master.addExperience(expReward);
                
                // 发放荣誉点奖励
                int creditReward = 50;
                master.setCreditPoint(master.getCreditPoint() + creditReward);
                
                // 发放出师令牌
                Item graduationToken = createGraduationToken(apprentice);
                inventoryService.addItem(masterName, graduationToken);
                
                log.info("师父 {} 获得出师奖励: 金币 {}, 经验 {}, 荣誉点 {}", 
                    masterName, goldReward, expReward, creditReward);
                
                // 发送系统消息
                sendSystemMessage(master, 
                    String.format("恭喜您的徒弟 %s 出师了！获得金币 %d，经验 %d，荣誉点 %d", 
                        apprentice.getName(), goldReward, expReward, creditReward));
                
            } else {
                // 师父不在线，通过邮件或其他方式发放
                log.info("师父 {} 不在线，出师奖励将通过邮件发放", masterName);
                sendRewardByMail(masterName, apprentice);
            }
            
        } catch (Exception e) {
            log.error("发放师父出师奖励失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 给师父发放经验
     */
    private void giveMasterExperience(String masterName, int exp) {
        if (exp <= 0) {
            return;
        }
        
        try {
            // 尝试获取在线的师父
            Player master = playerService.getOnlinePlayer(masterName);
            
            if (master != null) {
                // 直接给师父加经验
                master.addExperience(exp);
                
                log.debug("师父 {} 通过师徒关系获得经验: {}", masterName, exp);
                
                // 发送经验获得提示
                sendSystemMessage(master, 
                    String.format("您的徒弟为您带来了 %d 经验", exp));
                
            } else {
                // 师父不在线，记录离线经验
                recordOfflineExperience(masterName, exp);
            }
            
        } catch (Exception e) {
            log.error("给师父发放经验失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 创建出师令牌
     */
    private Item createGraduationToken(Player apprentice) {
        Item token = new Item();
        token.setItemId(30001); // 出师令牌的物品ID
        token.setName("出师令牌");
        token.setType(Item.ItemType.MISC_SPECIAL);
        token.setDescription(String.format("徒弟 %s 的出师纪念，见证了师徒情深", apprentice.getName()));
        token.setWeight(1);
        token.setPrice(0); // 无法买卖
        token.setBound(true); // 绑定物品
        token.setStackable(false);
        token.setMaxStack(1);
        
        // 添加特殊属性
        token.addExtraAttribute("graduationToken", 1);
        token.addExtraAttribute("apprenticeName", apprentice.getName().hashCode());
        token.addExtraAttribute("graduationTime", (int) (System.currentTimeMillis() / 1000));
        
        return token;
    }
    
    /**
     * 通过邮件发放奖励
     */
    private void sendRewardByMail(String masterName, Player apprentice) {
        try {
            // 这里应该调用邮件系统发送奖励
            // 暂时记录日志
            log.info("通过邮件为师父 {} 发放出师奖励", masterName);
            
            // 记录离线奖励
            recordOfflineReward(masterName, apprentice);
            
        } catch (Exception e) {
            log.error("通过邮件发放奖励失败", e);
        }
    }
    
    /**
     * 记录离线经验
     */
    private void recordOfflineExperience(String masterName, int exp) {
        try {
            // 这里应该记录到数据库，等师父上线时发放
            // 暂时记录日志
            log.info("记录师父 {} 的离线经验: {}", masterName, exp);
            
        } catch (Exception e) {
            log.error("记录离线经验失败", e);
        }
    }
    
    /**
     * 记录离线奖励
     */
    private void recordOfflineReward(String masterName, Player apprentice) {
        try {
            // 这里应该记录到数据库，等师父上线时发放
            // 暂时记录日志
            log.info("记录师父 {} 的离线出师奖励", masterName);
            
        } catch (Exception e) {
            log.error("记录离线奖励失败", e);
        }
    }
    
    /**
     * 发送系统消息
     */
    private void sendSystemMessage(Player player, String message) {
        try {
            // 这里应该调用消息系统发送消息
            // 暂时记录日志
            log.info("向玩家 {} 发送系统消息: {}", player.getName(), message);
            
        } catch (Exception e) {
            log.error("发送系统消息失败", e);
        }
    }
    
    /**
     * 检查师父是否在线
     */
    public boolean isMasterOnline(String apprenticeName) {
        String masterName = getMasterName(apprenticeName);
        if (masterName == null) {
            return false;
        }
        
        try {
            Player master = playerService.getOnlinePlayer(masterName);
            return master != null;
        } catch (Exception e) {
            log.error("检查师父在线状态失败", e);
            return false;
        }
    }
    
    /**
     * 获取师父等级
     */
    public int getMasterLevel(String apprenticeName) {
        String masterName = getMasterName(apprenticeName);
        if (masterName == null) {
            return 0;
        }
        
        try {
            Player master = playerService.getOnlinePlayer(masterName);
            return master != null ? master.getLevel() : 0;
        } catch (Exception e) {
            log.error("获取师父等级失败", e);
            return 0;
        }
    }
} 