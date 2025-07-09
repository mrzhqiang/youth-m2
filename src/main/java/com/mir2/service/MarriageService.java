package com.mir2.service;

import com.mir2.entity.Player;
import com.mir2.entity.MarriageInfo;
import com.mir2.core.model.Position;
import com.mir2.core.model.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MarriageService {
    
    private final Map<String, MarriageInfo> marriageMap = new ConcurrentHashMap<>();
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private InventoryService inventoryService;
    
    /**
     * 申请结婚
     */
    @Transactional
    public boolean proposeMarriage(Player proposer, Player target) {
        // 检查双方是否已婚
        if (isMarried(proposer.getUsername()) || isMarried(target.getUsername())) {
            return false;
        }
        
        // 检查等级要求
        if (proposer.getLevel() < 22 || target.getLevel() < 22) {
            return false;
        }
        
        // 检查距离要求
        if (calculateDistance(proposer.getPosition(), target.getPosition()) > 2) {
            return false;
        }
        
        // 检查金币要求
        if (proposer.getGold() < 1000000) {
            return false;
        }
        
        // 创建结婚信息
        MarriageInfo marriageInfo = new MarriageInfo();
        marriageInfo.setHusband(proposer.getUsername());
        marriageInfo.setWife(target.getUsername());
        marriageInfo.setMarriageDate(LocalDateTime.now());
        marriageInfo.setMarriageLevel(1);
        
        // 保存结婚信息
        marriageMap.put(proposer.getUsername(), marriageInfo);
        marriageMap.put(target.getUsername(), marriageInfo);
        
        // 扣除金币
        proposer.setGold(proposer.getGold() - 1000000);
        
        // 添加结婚戒指
        addWeddingRing(proposer);
        addWeddingRing(target);
        
        return true;
    }
    
    /**
     * 离婚
     */
    @Transactional
    public boolean divorce(Player player) {
        MarriageInfo marriage = marriageMap.get(player.getUsername());
        if (marriage == null) {
            return false;
        }
        
        String spouse = getSpouseName(player.getUsername());
        
        // 移除结婚信息
        marriageMap.remove(player.getUsername());
        marriageMap.remove(spouse);
        
        // 移除结婚戒指
        removeWeddingRing(player);
        
        return true;
    }
    
    /**
     * 夫妻传送
     */
    public boolean spouseTeleport(Player player, Player target) {
        if (!isMarriedTo(player.getUsername(), target.getUsername())) {
            return false;
        }
        
        MarriageInfo marriage = marriageMap.get(player.getUsername());
        if (marriage == null) {
            return false;
        }
        
        // 检查传送条件
        if (marriage.getMarriageLevel() < 2) {
            return false;
        }
        
        // 检查传送冷却
        if (marriage.getLastTeleportTime() != null) {
            long cooldown = 600000; // 10分钟冷却
            if (System.currentTimeMillis() - marriage.getLastTeleportTime() < cooldown) {
                return false;
            }
        }
        
        // 执行传送
        player.setPosition(target.getPosition());
        marriage.setLastTeleportTime(System.currentTimeMillis());
        
        return true;
    }
    
    /**
     * 获取配偶名称
     */
    public String getSpouseName(String playerName) {
        MarriageInfo marriage = marriageMap.get(playerName);
        if (marriage == null) {
            return null;
        }
        
        if (marriage.getHusband().equals(playerName)) {
            return marriage.getWife();
        } else {
            return marriage.getHusband();
        }
    }
    
    /**
     * 检查是否已婚
     */
    public boolean isMarried(String playerName) {
        return marriageMap.containsKey(playerName);
    }
    
    /**
     * 检查是否与指定玩家结婚
     */
    public boolean isMarriedTo(String playerName, String spouseName) {
        String spouse = getSpouseName(playerName);
        return spouse != null && spouse.equals(spouseName);
    }
    
    /**
     * 获取结婚信息
     */
    public MarriageInfo getMarriageInfo(String playerName) {
        return marriageMap.get(playerName);
    }
    
    /**
     * 添加结婚戒指
     */
    private void addWeddingRing(Player player) {
        try {
            // 创建结婚戒指
            Item weddingRing = createWeddingRing(player);
            
            // 尝试添加到玩家背包
            boolean success = inventoryService.addItem(player.getName(), weddingRing);
            
            if (success) {
                log.info("为玩家 {} 添加结婚戒指", player.getName());
                
                // 发送系统消息
                sendSystemMessage(player, "恭喜您获得了结婚戒指！");
                
                // 记录结婚日志
                recordMarriageLog(player, "获得结婚戒指");
                
            } else {
                log.warn("玩家 {} 背包空间不足，无法添加结婚戒指", player.getName());
                // 如果背包满了，可以考虑通过邮件发送
                sendRingByMail(player, weddingRing);
            }
            
        } catch (Exception e) {
            log.error("为玩家 {} 添加结婚戒指失败", player.getName(), e);
        }
    }
    
    /**
     * 移除结婚戒指
     */
    private void removeWeddingRing(Player player) {
        try {
            // 查找并移除结婚戒指
            boolean removed = inventoryService.removeItemByName(player.getName(), "结婚戒指");
            
            if (removed) {
                log.info("从玩家 {} 背包移除结婚戒指", player.getName());
                
                // 发送系统消息
                sendSystemMessage(player, "结婚戒指已被移除");
                
                // 记录离婚日志
                recordMarriageLog(player, "移除结婚戒指");
                
            } else {
                log.warn("玩家 {} 背包中没有找到结婚戒指", player.getName());
            }
            
        } catch (Exception e) {
            log.error("从玩家 {} 移除结婚戒指失败", player.getName(), e);
        }
    }
    
    /**
     * 创建结婚戒指
     */
    private Item createWeddingRing(Player player) {
        // 创建结婚戒指物品
        Item weddingRing = new Item();
        weddingRing.setItemId(20001); // 结婚戒指的物品ID
        weddingRing.setName("结婚戒指");
        weddingRing.setType(Item.ItemType.JEWELRY_RING);
        weddingRing.setDescription("见证爱情的神圣戒指，佩戴后可获得特殊效果");
        weddingRing.setWeight(1);
        weddingRing.setPrice(0); // 无法买卖
        weddingRing.setBound(true); // 绑定物品
        weddingRing.setStackable(false);
        weddingRing.setMaxStack(1);
        weddingRing.setRequiredLevel(22);
        weddingRing.setDurability(100);
        weddingRing.setMaxDurability(100);
        
        // 设置戒指属性
        Item.ItemAttributes attributes = new Item.ItemAttributes();
        attributes.setAttack(5);
        attributes.setMagicAttack(5);
        attributes.setDefense(5);
        attributes.setMagicDefense(5);
        attributes.setLuck(3);
        attributes.setHealthBonus(100);
        attributes.setManaBonus(100);
        weddingRing.setAttributes(attributes);
        
        // 添加特殊属性
        weddingRing.addExtraAttribute("marriageRing", 1);
        weddingRing.addExtraAttribute("ownerName", player.getName().hashCode());
        
        return weddingRing;
    }
    
    /**
     * 通过邮件发送戒指
     */
    private void sendRingByMail(Player player, Item weddingRing) {
        try {
            // 这里应该调用邮件系统发送戒指
            // 暂时记录日志
            log.info("通过邮件为玩家 {} 发送结婚戒指", player.getName());
            
            // 发送系统消息
            sendSystemMessage(player, "背包空间不足，结婚戒指已通过邮件发送");
            
        } catch (Exception e) {
            log.error("通过邮件发送结婚戒指失败", e);
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
     * 记录结婚日志
     */
    private void recordMarriageLog(Player player, String action) {
        try {
            // 这里应该记录到数据库
            // 暂时记录日志
            log.info("结婚日志 - 玩家: {}, 动作: {}, 时间: {}", 
                player.getName(), action, LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("记录结婚日志失败", e);
        }
    }
    
    /**
     * 检查玩家是否拥有结婚戒指
     */
    public boolean hasWeddingRing(Player player) {
        try {
            return inventoryService.hasItem(player.getName(), "结婚戒指");
        } catch (Exception e) {
            log.error("检查结婚戒指失败", e);
            return false;
        }
    }
    
    /**
     * 获取结婚戒指属性加成
     */
    public Item.ItemAttributes getWeddingRingBonus(Player player) {
        if (!hasWeddingRing(player)) {
            return null;
        }
        
        MarriageInfo marriage = getMarriageInfo(player.getName());
        if (marriage == null) {
            return null;
        }
        
        // 根据结婚等级提供不同的属性加成
        Item.ItemAttributes bonus = new Item.ItemAttributes();
        int level = marriage.getMarriageLevel();
        
        bonus.setAttack(level * 2);
        bonus.setMagicAttack(level * 2);
        bonus.setDefense(level * 2);
        bonus.setMagicDefense(level * 2);
        bonus.setLuck(level);
        bonus.setHealthBonus(level * 50);
        bonus.setManaBonus(level * 50);
        
        return bonus;
    }
    
    /**
     * 计算两个位置之间的距离
     */
    private int calculateDistance(Position pos1, Position pos2) {
        int dx = pos1.getX() - pos2.getX();
        int dy = pos1.getY() - pos2.getY();
        return Math.max(Math.abs(dx), Math.abs(dy));
    }
} 