package com.mir2.core.model;

import com.mir2.core.enums.Job;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 游戏技能类
 * 
 * <p>代表游戏中的技能系统，包括战士、法师、道士的所有技能。
 * 对应原M2Engine中的魔法系统。</p>
 * 
 * <p>技能分类：</p>
 * <ul>
 *   <li>战士技能：基础剑术、攻杀剑术、半月弯刀、野蛮冲撞等</li>
 *   <li>法师技能：小火球、大火球、雷电术、疾光电影等</li>
 *   <li>道士技能：治愈术、精神力战法、召唤骷髅、群体治疗等</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Slf4j
public class Skill {
    
    /** 技能ID */
    private int skillId;
    
    /** 技能名称 */
    private String name;
    
    /** 技能类型 */
    private SkillType type;
    
    /** 适用职业 */
    private Job job;
    
    /** 学习等级 */
    private int learnLevel;
    
    /** 最大等级 */
    private int maxLevel;
    
    /** 技能描述 */
    private String description;
    
    /** 技能图标 */
    private int icon;
    
    /** 基础属性 */
    private SkillAttributes baseAttributes;
    
    /** 每级属性增长 */
    private SkillAttributes levelAttributes;
    
    /** 施法时间（毫秒） */
    private int castTime;
    
    /** 冷却时间（毫秒） */
    private int cooldown;
    
    /** 施法距离 */
    private int range;
    
    /** 影响范围 */
    private int areaRange;
    
    /** 持续时间（毫秒） */
    private int duration;
    
    /** 技能效果 */
    private Map<String, Integer> effects;
    
    /** 前置技能要求 */
    private Map<Integer, Integer> prerequisites;
    
    /**
     * 构造函数
     */
    public Skill() {
        this.effects = new HashMap<>();
        this.prerequisites = new HashMap<>();
        this.baseAttributes = new SkillAttributes();
        this.levelAttributes = new SkillAttributes();
    }
    
    /**
     * 构造函数
     * 
     * @param skillId 技能ID
     * @param name 技能名称
     * @param type 技能类型
     * @param job 适用职业
     */
    public Skill(int skillId, String name, SkillType type, Job job) {
        this();
        this.skillId = skillId;
        this.name = name;
        this.type = type;
        this.job = job;
    }
    
    /**
     * 计算指定等级的技能属性
     * 
     * @param level 技能等级
     * @return 技能属性
     */
    public SkillAttributes calculateAttributes(int level) {
        SkillAttributes attributes = new SkillAttributes();
        
        // 基础属性 + 等级属性 * (等级 - 1)
        attributes.setDamage(baseAttributes.getDamage() + levelAttributes.getDamage() * (level - 1));
        attributes.setManaCost(baseAttributes.getManaCost() + levelAttributes.getManaCost() * (level - 1));
        attributes.setHealAmount(baseAttributes.getHealAmount() + levelAttributes.getHealAmount() * (level - 1));
        attributes.setBuffValue(baseAttributes.getBuffValue() + levelAttributes.getBuffValue() * (level - 1));
        attributes.setSuccessRate(baseAttributes.getSuccessRate() + levelAttributes.getSuccessRate() * (level - 1));
        
        // 确保成功率不超过100%
        if (attributes.getSuccessRate() > 100) {
            attributes.setSuccessRate(100);
        }
        
        return attributes;
    }
    
    /**
     * 计算技能升级所需经验
     * 
     * @param level 当前等级
     * @return 升级所需经验
     */
    public int calculateRequiredExp(int level) {
        if (level >= maxLevel) {
            return Integer.MAX_VALUE;
        }
        
        // 基础经验需求
        int baseExp = 100;
        
        // 根据技能类型调整经验需求
        double typeMultiplier = switch (type) {
            case BASIC -> 1.0;
            case ADVANCED -> 1.5;
            case MASTER -> 2.0;
            case LEGENDARY -> 3.0;
            default -> 1.0;
        };
        
        // 等级系数
        double levelMultiplier = 1.0 + (level - 1) * 0.3;
        
        return (int) (baseExp * typeMultiplier * levelMultiplier);
    }
    
    /**
     * 检查是否满足前置技能要求
     * 
     * @param playerSkills 玩家技能列表
     * @return 是否满足要求
     */
    public boolean checkPrerequisites(Map<Integer, PlayerSkill> playerSkills) {
        for (Map.Entry<Integer, Integer> entry : prerequisites.entrySet()) {
            int requiredSkillId = entry.getKey();
            int requiredLevel = entry.getValue();
            
            PlayerSkill playerSkill = playerSkills.get(requiredSkillId);
            if (playerSkill == null || playerSkill.getLevel() < requiredLevel) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 获取指定等级的技能属性
     * 
     * @param level 技能等级
     * @return 技能属性
     */
    public SkillAttributes getAttributesAtLevel(int level) {
        return calculateAttributes(level);
    }
    
    /**
     * 获取技能类型枚举
     */
    public enum SkillType {
        BASIC("基础技能"),
        ADVANCED("高级技能"),
        MASTER("大师技能"),
        LEGENDARY("传奇技能");
        
        private final String name;
        
        SkillType(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
    }
    
    /**
     * 技能属性类
     */
    @Data
    public static class SkillAttributes {
        /** 伤害值 */
        private int damage;
        
        /** 魔法消耗 */
        private int manaCost;
        
        /** 治疗量 */
        private int healAmount;
        
        /** 增益效果数值 */
        private int buffValue;
        
        /** 成功率 (0-100) */
        private int successRate;
        
        /** 暴击率 (0-100) */
        private int criticalRate;
        
        /** 施法时间 */
        private int castTime;
        
        /** 冷却时间 */
        private int cooldown;
        
        /** 威力 */
        private int power;
        
        /** 魔法消耗 */
        private int mpCost;
        
        /** 额外效果 */
        private Map<String, Integer> extraEffects = new HashMap<>();
        
        /**
         * 添加额外效果
         * 
         * @param name 效果名称
         * @param value 效果数值
         */
        public void addExtraEffect(String name, int value) {
            extraEffects.put(name, value);
        }
        
        /**
         * 获取额外效果
         * 
         * @param name 效果名称
         * @return 效果数值
         */
        public int getExtraEffect(String name) {
            return extraEffects.getOrDefault(name, 0);
        }
        
        /**
         * 设置威力（兼容方法）
         * 
         * @param power 威力值
         */
        public void setPower(int power) {
            this.power = power;
            this.damage = power; // 同步设置伤害值
        }
        
        /**
         * 获取威力（兼容方法）
         * 
         * @return 威力值
         */
        public int getPower() {
            return Math.max(power, damage);
        }
        
        /**
         * 设置魔法消耗（兼容方法）
         * 
         * @param mpCost 魔法消耗
         */
        public void setMpCost(int mpCost) {
            this.mpCost = mpCost;
            this.manaCost = mpCost; // 同步设置魔法消耗
        }
        
        /**
         * 获取魔法消耗（兼容方法）
         * 
         * @return 魔法消耗
         */
        public int getMpCost() {
            return Math.max(mpCost, manaCost);
        }
    }
} 