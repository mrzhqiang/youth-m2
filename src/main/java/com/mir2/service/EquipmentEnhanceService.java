package com.mir2.service;

import com.mir2.core.model.Player;
import com.mir2.core.model.Item;
import com.mir2.entity.EnhancementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;

@Service
@Slf4j
public class EquipmentEnhanceService {
    
    private final Random random = new Random();
    private final Map<Integer, EnhancementInfo> enhancementData = new ConcurrentHashMap<>();
    
    @Autowired
    private InventoryService inventoryService;
    
    // 强化成功率配置 (0-19级)
    private final int[] successRates = {
        100, 100, 90, 80, 70, 60, 50, 40, 30, 25,  // 0-9级
        20, 15, 12, 10, 8, 6, 4, 3, 2, 1          // 10-19级
    };
    
    // 强化材料消耗配置
    private final int[] materialCosts = {
        1, 1, 2, 2, 3, 3, 4, 4, 5, 5,           // 0-9级
        6, 7, 8, 9, 10, 12, 14, 16, 18, 20      // 10-19级
    };
    
    // 金币消耗配置
    private final int[] goldCosts = {
        10000, 20000, 40000, 80000, 150000,      // 0-4级
        300000, 500000, 800000, 1200000, 1800000, // 5-9级
        2500000, 3500000, 5000000, 7000000, 10000000, // 10-14级
        15000000, 20000000, 30000000, 45000000, 60000000 // 15-19级
    };
    
    /**
     * 强化装备
     */
    @Transactional
    public EnhanceResult enhanceEquipment(Player player, Item equipment, Item material, boolean useProtection) {
        // 检查装备是否可强化
        if (!canEnhance(equipment)) {
            return new EnhanceResult(false, "该装备无法强化", equipment.getEnhanceLevel());
        }
        
        int currentLevel = equipment.getEnhanceLevel();
        
        // 检查是否达到最大强化等级
        if (currentLevel >= 20) {
            return new EnhanceResult(false, "装备已达到最大强化等级", currentLevel);
        }
        
        // 检查材料是否足够
        if (!checkMaterials(player, currentLevel, material)) {
            return new EnhanceResult(false, "强化材料不足", currentLevel);
        }
        
        // 检查金币是否足够
        int goldCost = goldCosts[currentLevel];
        if (player.getGold() < goldCost) {
            return new EnhanceResult(false, "金币不足", currentLevel);
        }
        
        // 扣除材料和金币
        consumeMaterials(player, currentLevel, material);
        player.setGold(player.getGold() - goldCost);
        
        // 计算强化成功率
        int successRate = calculateSuccessRate(currentLevel, equipment, useProtection);
        boolean success = random.nextInt(100) < successRate;
        
        if (success) {
            // 强化成功
            equipment.setEnhanceLevel(currentLevel + 1);
            applyEnhancementBonus(equipment);
            
            // 记录强化信息
            recordEnhancement(player.getUsername(), equipment, true);
            
            return new EnhanceResult(true, "强化成功！", currentLevel + 1);
        } else {
            // 强化失败
            if (useProtection) {
                // 使用保护符，装备不降级
                return new EnhanceResult(false, "强化失败，但装备受到保护", currentLevel);
            } else {
                // 强化失败处理
                return handleEnhanceFailure(equipment, currentLevel);
            }
        }
    }
    
    /**
     * 检查装备是否可以强化
     */
    private boolean canEnhance(Item equipment) {
        // 检查装备类型
        String itemType = equipment.getType();
        return itemType.equals("WEAPON") || itemType.equals("ARMOR") || 
               itemType.equals("HELMET") || itemType.equals("NECKLACE") ||
               itemType.equals("RING") || itemType.equals("BRACELET");
    }
    
    /**
     * 检查强化材料
     */
    private boolean checkMaterials(Player player, int level, Item material) {
        int requiredAmount = materialCosts[level];
        
        // 检查材料类型和数量
        if (material == null || !isValidEnhanceMaterial(material)) {
            return false;
        }
        
        return material.getQuantity() >= requiredAmount;
    }
    
    /**
     * 消耗强化材料
     */
    private void consumeMaterials(Player player, int level, Item material) {
        int requiredAmount = materialCosts[level];
        material.setQuantity(material.getQuantity() - requiredAmount);
        
        // 如果材料用完，从背包移除
        if (material.getQuantity() <= 0) {
            try {
                // 从玩家背包移除材料
                inventoryService.removeItem(player.getName(), material.getItemId(), requiredAmount);
                log.debug("从玩家 {} 背包移除强化材料: {} x{}", player.getName(), material.getName(), requiredAmount);
            } catch (Exception e) {
                log.error("移除强化材料失败", e);
            }
        }
    }
    
    /**
     * 计算强化成功率
     */
    private int calculateSuccessRate(int level, Item equipment, boolean useProtection) {
        int baseRate = successRates[level];
        
        // 装备幸运值影响成功率
        int luckBonus = equipment.getLuck() * 2;
        
        // 保护符提供额外成功率
        int protectionBonus = useProtection ? 10 : 0;
        
        return Math.min(95, baseRate + luckBonus + protectionBonus);
    }
    
    /**
     * 应用强化属性加成
     */
    private void applyEnhancementBonus(Item equipment) {
        int level = equipment.getEnhanceLevel();
        String itemType = equipment.getType();
        
        // 根据装备类型和强化等级计算属性加成
        switch (itemType) {
            case "WEAPON":
                applyWeaponEnhancement(equipment, level);
                break;
            case "ARMOR":
                applyArmorEnhancement(equipment, level);
                break;
            case "HELMET":
                applyHelmetEnhancement(equipment, level);
                break;
            case "NECKLACE":
                applyNecklaceEnhancement(equipment, level);
                break;
            case "RING":
                applyRingEnhancement(equipment, level);
                break;
            case "BRACELET":
                applyBraceletEnhancement(equipment, level);
                break;
        }
    }
    
    /**
     * 武器强化加成
     */
    private void applyWeaponEnhancement(Item weapon, int level) {
        // 每级增加5-8点攻击力
        int attackBonus = level * (5 + random.nextInt(4));
        weapon.setAttackPower(weapon.getAttackPower() + attackBonus);
        
        // 高级强化有额外属性
        if (level >= 10) {
            weapon.setHitRate(weapon.getHitRate() + level - 9); // 准确+1
            weapon.setCriticalRate(weapon.getCriticalRate() + (level - 9) * 2); // 暴击率+2%
        }
        
        if (level >= 15) {
            weapon.setIgnoreDefense(weapon.getIgnoreDefense() + (level - 14) * 5); // 无视防御
        }
    }
    
    /**
     * 防具强化加成
     */
    private void applyArmorEnhancement(Item armor, int level) {
        // 每级增加3-5点防御力
        int defenseBonus = level * (3 + random.nextInt(3));
        armor.setDefensePower(armor.getDefensePower() + defenseBonus);
        
        // 高级强化有额外属性
        if (level >= 10) {
            armor.setMagicDefense(armor.getMagicDefense() + (level - 9) * 2); // 魔防+2
            armor.setHpBonus(armor.getHpBonus() + (level - 9) * 50); // 生命+50
        }
        
        if (level >= 15) {
            armor.setDamageReduction(armor.getDamageReduction() + (level - 14) * 3); // 伤害减免
        }
    }
    
    /**
     * 头盔强化加成
     */
    private void applyHelmetEnhancement(Item helmet, int level) {
        int defenseBonus = level * (2 + random.nextInt(2));
        helmet.setDefensePower(helmet.getDefensePower() + defenseBonus);
        helmet.setMagicDefense(helmet.getMagicDefense() + defenseBonus);
        
        if (level >= 10) {
            helmet.setHpBonus(helmet.getHpBonus() + (level - 9) * 30);
            helmet.setMpBonus(helmet.getMpBonus() + (level - 9) * 20);
        }
    }
    
    /**
     * 项链强化加成
     */
    private void applyNecklaceEnhancement(Item necklace, int level) {
        if (level >= 5) {
            necklace.setMagicPower(necklace.getMagicPower() + (level - 4) * 2);
            necklace.setMpBonus(necklace.getMpBonus() + (level - 4) * 40);
        }
        
        if (level >= 10) {
            necklace.setSpellPower(necklace.getSpellPower() + (level - 9) * 3);
        }
    }
    
    /**
     * 戒指强化加成
     */
    private void applyRingEnhancement(Item ring, int level) {
        if (level >= 3) {
            ring.setAttackPower(ring.getAttackPower() + (level - 2) * 2);
            ring.setMagicPower(ring.getMagicPower() + (level - 2) * 2);
        }
        
        if (level >= 8) {
            ring.setLuck(ring.getLuck() + (level - 7));
        }
    }
    
    /**
     * 手镯强化加成
     */
    private void applyBraceletEnhancement(Item bracelet, int level) {
        if (level >= 3) {
            bracelet.setDefensePower(bracelet.getDefensePower() + (level - 2) * 2);
            bracelet.setMagicDefense(bracelet.getMagicDefense() + (level - 2) * 2);
        }
        
        if (level >= 8) {
            bracelet.setHpBonus(bracelet.getHpBonus() + (level - 7) * 25);
        }
    }
    
    /**
     * 处理强化失败
     */
    private EnhanceResult handleEnhanceFailure(Item equipment, int currentLevel) {
        if (currentLevel >= 10) {
            // 10级以上失败会降级
            equipment.setEnhanceLevel(Math.max(0, currentLevel - 1));
            removeEnhancementBonus(equipment, currentLevel);
            
            recordEnhancement(null, equipment, false);
            return new EnhanceResult(false, "强化失败，装备降级", currentLevel - 1);
        } else if (currentLevel >= 5) {
            // 5-9级失败有概率降级
            if (random.nextInt(100) < 50) {
                equipment.setEnhanceLevel(Math.max(0, currentLevel - 1));
                removeEnhancementBonus(equipment, currentLevel);
                return new EnhanceResult(false, "强化失败，装备降级", currentLevel - 1);
            } else {
                return new EnhanceResult(false, "强化失败，装备等级不变", currentLevel);
            }
        } else {
            // 5级以下失败不降级
            return new EnhanceResult(false, "强化失败，装备等级不变", currentLevel);
        }
    }
    
    /**
     * 移除强化加成
     */
    private void removeEnhancementBonus(Item equipment, int fromLevel) {
        try {
            // 获取装备原始属性
            Map<String, Integer> originalAttributes = getOriginalAttributes(equipment);
            
            // 根据装备类型重置属性
            String itemType = equipment.getType().name();
            
            switch (itemType) {
                case "WEAPON_SWORD":
                case "WEAPON_STAFF":
                case "WEAPON_BOW":
                    removeWeaponEnhancementBonus(equipment, fromLevel, originalAttributes);
                    break;
                case "ARMOR_HELMET":
                case "ARMOR_DRESS":
                case "ARMOR_BOOTS":
                    removeArmorEnhancementBonus(equipment, fromLevel, originalAttributes);
                    break;
                case "JEWELRY_NECKLACE":
                case "JEWELRY_RING":
                case "JEWELRY_BRACELET":
                    removeJewelryEnhancementBonus(equipment, fromLevel, originalAttributes);
                    break;
                default:
                    log.warn("未知的装备类型: {}", itemType);
                    break;
            }
            
            log.debug("移除装备 {} 的强化加成，从等级 {} 降级", equipment.getName(), fromLevel);
            
        } catch (Exception e) {
            log.error("移除强化加成失败", e);
        }
    }
    
    /**
     * 获取装备原始属性
     */
    private Map<String, Integer> getOriginalAttributes(Item equipment) {
        Map<String, Integer> originalAttributes = new HashMap<>();
        
        // 从装备的特殊属性中获取原始值
        // 如果没有存储原始值，则使用当前值减去强化加成
        int currentLevel = equipment.getEnhanceLevel();
        
        // 计算原始攻击力
        int originalAttack = equipment.getExtraAttribute("originalAttack");
        if (originalAttack == 0) {
            originalAttack = equipment.getAttackPower() - calculateAttackBonus(currentLevel);
        }
        originalAttributes.put("attack", originalAttack);
        
        // 计算原始防御力
        int originalDefense = equipment.getExtraAttribute("originalDefense");
        if (originalDefense == 0) {
            originalDefense = equipment.getDefensePower() - calculateDefenseBonus(currentLevel);
        }
        originalAttributes.put("defense", originalDefense);
        
        // 计算原始魔防
        int originalMagicDefense = equipment.getExtraAttribute("originalMagicDefense");
        if (originalMagicDefense == 0) {
            originalMagicDefense = equipment.getMagicDefense() - calculateMagicDefenseBonus(currentLevel);
        }
        originalAttributes.put("magicDefense", originalMagicDefense);
        
        return originalAttributes;
    }
    
    /**
     * 移除武器强化加成
     */
    private void removeWeaponEnhancementBonus(Item weapon, int fromLevel, Map<String, Integer> originalAttributes) {
        // 重置攻击力到原始值
        weapon.setAttackPower(originalAttributes.get("attack"));
        
        // 移除高级强化属性
        if (fromLevel >= 10) {
            weapon.setHitRate(weapon.getHitRate() - (fromLevel - 9));
            weapon.setCriticalRate(weapon.getCriticalRate() - (fromLevel - 9) * 2);
        }
        
        if (fromLevel >= 15) {
            weapon.setIgnoreDefense(weapon.getIgnoreDefense() - (fromLevel - 14) * 5);
        }
    }
    
    /**
     * 移除防具强化加成
     */
    private void removeArmorEnhancementBonus(Item armor, int fromLevel, Map<String, Integer> originalAttributes) {
        // 重置防御力到原始值
        armor.setDefensePower(originalAttributes.get("defense"));
        armor.setMagicDefense(originalAttributes.get("magicDefense"));
        
        // 移除高级强化属性
        if (fromLevel >= 10) {
            armor.setHpBonus(armor.getHpBonus() - (fromLevel - 9) * 50);
        }
        
        if (fromLevel >= 15) {
            armor.setDamageReduction(armor.getDamageReduction() - (fromLevel - 14) * 3);
        }
    }
    
    /**
     * 移除首饰强化加成
     */
    private void removeJewelryEnhancementBonus(Item jewelry, int fromLevel, Map<String, Integer> originalAttributes) {
        String itemType = jewelry.getType().name();
        
        switch (itemType) {
            case "JEWELRY_NECKLACE":
                if (fromLevel >= 5) {
                    jewelry.setMagicPower(jewelry.getMagicPower() - (fromLevel - 4) * 2);
                    jewelry.setMpBonus(jewelry.getMpBonus() - (fromLevel - 4) * 40);
                }
                if (fromLevel >= 10) {
                    jewelry.setSpellPower(jewelry.getSpellPower() - (fromLevel - 9) * 3);
                }
                break;
            case "JEWELRY_RING":
                if (fromLevel >= 3) {
                    jewelry.setAttackPower(jewelry.getAttackPower() - (fromLevel - 2) * 2);
                    jewelry.setMagicPower(jewelry.getMagicPower() - (fromLevel - 2) * 2);
                }
                if (fromLevel >= 8) {
                    jewelry.setLuck(jewelry.getLuck() - (fromLevel - 7));
                }
                break;
            case "JEWELRY_BRACELET":
                if (fromLevel >= 3) {
                    jewelry.setDefensePower(jewelry.getDefensePower() - (fromLevel - 2) * 2);
                    jewelry.setMagicDefense(jewelry.getMagicDefense() - (fromLevel - 2) * 2);
                }
                if (fromLevel >= 8) {
                    jewelry.setHpBonus(jewelry.getHpBonus() - (fromLevel - 7) * 25);
                }
                break;
        }
    }
    
    /**
     * 计算攻击力加成
     */
    private int calculateAttackBonus(int level) {
        int total = 0;
        for (int i = 1; i <= level; i++) {
            total += i * (5 + random.nextInt(4));
        }
        return total;
    }
    
    /**
     * 计算防御力加成
     */
    private int calculateDefenseBonus(int level) {
        int total = 0;
        for (int i = 1; i <= level; i++) {
            total += i * (3 + random.nextInt(3));
        }
        return total;
    }
    
    /**
     * 计算魔防加成
     */
    private int calculateMagicDefenseBonus(int level) {
        int total = 0;
        for (int i = 10; i <= level; i++) {
            total += (i - 9) * 2;
        }
        return total;
    }
    
    /**
     * 检查是否为有效的强化材料
     */
    private boolean isValidEnhanceMaterial(Item material) {
        // 检查材料类型
        return material.getName().contains("强化石") || 
               material.getName().contains("祝福油") ||
               material.getName().contains("灵魂宝石");
    }
    
    /**
     * 记录强化信息
     */
    private void recordEnhancement(String playerName, Item equipment, boolean success) {
        EnhancementInfo info = new EnhancementInfo();
        info.setPlayerName(playerName);
        info.setItemName(equipment.getName());
        info.setItemId(equipment.getId());
        info.setEnhanceLevel(equipment.getEnhanceLevel());
        info.setSuccess(success);
        info.setTimestamp(System.currentTimeMillis());
        
        enhancementData.put(info.hashCode(), info);
    }
    
    /**
     * 获取装备强化信息
     */
    public String getEnhancementInfo(Item equipment) {
        int level = equipment.getEnhanceLevel();
        if (level == 0) {
            return equipment.getName();
        }
        
        String prefix = getEnhancementPrefix(level);
        return prefix + equipment.getName() + " (+" + level + ")";
    }
    
    /**
     * 获取强化前缀
     */
    private String getEnhancementPrefix(int level) {
        if (level >= 15) return "神圣的 ";
        if (level >= 12) return "传说的 ";
        if (level >= 10) return "史诗的 ";
        if (level >= 8) return "稀有的 ";
        if (level >= 5) return "精良的 ";
        if (level >= 3) return "优秀的 ";
        return "";
    }
    
    /**
     * 强化结果类
     */
    public static class EnhanceResult {
        private final boolean success;
        private final String message;
        private final int newLevel;
        
        public EnhanceResult(boolean success, String message, int newLevel) {
            this.success = success;
            this.message = message;
            this.newLevel = newLevel;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getNewLevel() { return newLevel; }
    }
} 