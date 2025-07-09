package com.mir2.core.model;

import com.mir2.core.enums.Job;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 游戏物品类
 * 
 * <p>代表游戏中的所有物品，包括装备、药品、杂物等。
 * 对应原M2Engine中的物品系统。</p>
 * 
 * <p>物品类型：</p>
 * <ul>
 *   <li>武器：刀剑、法杖、弓箭等</li>
 *   <li>防具：头盔、盔甲、靴子等</li>
 *   <li>首饰：项链、戒指、手镯等</li>
 *   <li>药品：红药、蓝药、毒药等</li>
 *   <li>杂物：材料、书籍、特殊物品等</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Slf4j
public class Item {
    
    /** 物品ID */
    private int itemId;
    
    /** 物品名称 */
    private String name;
    
    /** 物品类型 */
    private ItemType type;
    
    /** 物品形状（在背包中的图标） */
    private int shape;
    
    /** 物品重量 */
    private int weight;
    
    /** 物品价格 */
    private int price;
    
    /** 物品描述 */
    private String description;
    
    /** 需要等级 */
    private int requiredLevel;
    
    /** 需要职业 */
    private Job requiredJob;
    
    /** 需要声望 */
    private int requiredReputation;
    
    /** 性别限制 (0: 无限制, 1: 男性, 2: 女性) */
    private int genderLimit;
    
    /** 物品属性 */
    private ItemAttributes attributes;
    
    /** 是否可堆叠 */
    private boolean stackable;
    
    /** 最大堆叠数量 */
    private int maxStack;
    
    /** 耐久度 */
    private int durability;
    
    /** 最大耐久度 */
    private int maxDurability;
    
    /** 是否绑定 */
    private boolean bound;
    
    /** 特殊属性 */
    private Map<String, Integer> specialAttributes;
    
    /**
     * 构造函数
     */
    public Item() {
        this.specialAttributes = new HashMap<>();
        this.attributes = new ItemAttributes();
    }
    
    /**
     * 构造函数
     * 
     * @param itemId 物品ID
     * @param name 物品名称
     * @param type 物品类型
     */
    public Item(int itemId, String name, ItemType type) {
        this();
        this.itemId = itemId;
        this.name = name;
        this.type = type;
    }
    
    /**
     * 检查玩家是否可以使用此物品
     * 
     * @param player 玩家对象
     * @return 是否可以使用
     */
    public boolean canUseBy(Player player) {
        // 检查等级需求
        if (player.getLevel() < requiredLevel) {
            return false;
        }
        
        // 检查职业需求
        if (requiredJob != null && requiredJob != Job.ALL && player.getJob() != requiredJob) {
            return false;
        }
        
        // 检查声望需求
        if (player.getReputation() < requiredReputation) {
            return false;
        }
        
        // 检查性别限制
        if (genderLimit > 0 && player.getGender() != (genderLimit - 1)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取物品的完整属性信息
     * 
     * @return 属性信息字符串
     */
    public String getAttributeInfo() {
        StringBuilder info = new StringBuilder();
        info.append(name).append("\n");
        
        if (attributes.getAttack() > 0) {
            info.append("攻击力: ").append(attributes.getAttack()).append("\n");
        }
        if (attributes.getMagicAttack() > 0) {
            info.append("魔法攻击: ").append(attributes.getMagicAttack()).append("\n");
        }
        if (attributes.getTaoAttack() > 0) {
            info.append("道术攻击: ").append(attributes.getTaoAttack()).append("\n");
        }
        if (attributes.getDefense() > 0) {
            info.append("防御力: ").append(attributes.getDefense()).append("\n");
        }
        if (attributes.getMagicDefense() > 0) {
            info.append("魔法防御: ").append(attributes.getMagicDefense()).append("\n");
        }
        if (attributes.getAccuracy() > 0) {
            info.append("准确: ").append(attributes.getAccuracy()).append("\n");
        }
        if (attributes.getAgility() > 0) {
            info.append("敏捷: ").append(attributes.getAgility()).append("\n");
        }
        
        if (requiredLevel > 0) {
            info.append("需要等级: ").append(requiredLevel).append("\n");
        }
        if (requiredJob != null && requiredJob != Job.ALL) {
            info.append("需要职业: ").append(requiredJob.getName()).append("\n");
        }
        if (weight > 0) {
            info.append("重量: ").append(weight).append("\n");
        }
        
        return info.toString();
    }
    
    /**
     * 获取物品价值
     * 
     * @return 物品价值
     */
    public int getValue() {
        return price;
    }
    
    /**
     * 克隆物品
     * 
     * @return 物品副本
     */
    public Item clone() {
        Item clonedItem = new Item();
        clonedItem.itemId = this.itemId;
        clonedItem.name = this.name;
        clonedItem.type = this.type;
        clonedItem.shape = this.shape;
        clonedItem.weight = this.weight;
        clonedItem.price = this.price;
        clonedItem.description = this.description;
        clonedItem.requiredLevel = this.requiredLevel;
        clonedItem.requiredJob = this.requiredJob;
        clonedItem.requiredReputation = this.requiredReputation;
        clonedItem.genderLimit = this.genderLimit;
        clonedItem.attributes = this.attributes.copy();
        clonedItem.stackable = this.stackable;
        clonedItem.maxStack = this.maxStack;
        clonedItem.durability = this.durability;
        clonedItem.maxDurability = this.maxDurability;
        clonedItem.bound = this.bound;
        clonedItem.specialAttributes = new HashMap<>(this.specialAttributes);
        return clonedItem;
    }
    
    /**
     * 检查两个物品是否相同（可堆叠）
     * 
     * @param otherItem 其他物品
     * @return 是否相同
     */
    public boolean isSameItem(Item otherItem) {
        if (otherItem == null) {
            return false;
        }
        
        // 检查基本属性
        if (this.itemId != otherItem.itemId) {
            return false;
        }
        
        // 检查绑定状态
        if (this.bound != otherItem.bound) {
            return false;
        }
        
        // 检查特殊属性
        if (!this.specialAttributes.equals(otherItem.specialAttributes)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取最大堆叠数量
     * 
     * @return 最大堆叠数量
     */
    public int getMaxStack() {
        return stackable ? maxStack : 1;
    }
    
    /**
     * 是否可以堆叠
     * 
     * @return 是否可以堆叠
     */
    public boolean isStackable() {
        return stackable && maxStack > 1;
    }
    
    /**
     * 获取强化等级
     * 
     * @return 强化等级
     */
    public int getEnhanceLevel() {
        return getExtraAttribute("enhanceLevel");
    }
    
    /**
     * 设置强化等级
     * 
     * @param level 强化等级
     */
    public void setEnhanceLevel(int level) {
        addExtraAttribute("enhanceLevel", level);
    }
    
    /**
     * 获取品质等级
     * 
     * @return 品质等级
     */
    public int getQualityLevel() {
        return getExtraAttribute("qualityLevel");
    }
    
    /**
     * 设置品质等级
     * 
     * @param level 品质等级
     */
    public void setQualityLevel(int level) {
        addExtraAttribute("qualityLevel", level);
    }
    
    /**
     * 获取套装ID
     * 
     * @return 套装ID
     */
    public int getSetId() {
        return getExtraAttribute("setId");
    }
    
    /**
     * 设置套装ID
     * 
     * @param setId 套装ID
     */
    public void setSetId(int setId) {
        addExtraAttribute("setId", setId);
    }
    
    /**
     * 添加额外属性
     * 
     * @param name 属性名
     * @param value 属性值
     */
    public void addExtraAttribute(String name, int value) {
        if (specialAttributes == null) {
            specialAttributes = new HashMap<>();
        }
        specialAttributes.put(name, value);
    }
    
    /**
     * 获取额外属性
     * 
     * @param name 属性名
     * @return 属性值
     */
    public int getExtraAttribute(String name) {
        if (specialAttributes == null) {
            return 0;
        }
        return specialAttributes.getOrDefault(name, 0);
    }
    
    /**
     * 物品类型枚举
     */
    public enum ItemType {
        WEAPON_SWORD(5, "刀剑"),           // 武器-刀剑
        WEAPON_STAFF(6, "法杖"),           // 武器-法杖
        WEAPON_BOW(7, "弓箭"),             // 武器-弓箭
        ARMOR_HELMET(10, "头盔"),          // 防具-头盔
        ARMOR_DRESS(11, "盔甲"),           // 防具-盔甲
        ARMOR_BOOTS(12, "靴子"),           // 防具-靴子
        JEWELRY_NECKLACE(19, "项链"),      // 首饰-项链
        JEWELRY_RING(20, "戒指"),          // 首饰-戒指
        JEWELRY_BRACELET(21, "手镯"),      // 首饰-手镯
        JEWELRY_BELT(22, "腰带"),          // 首饰-腰带
        POTION_HEAL(23, "治疗药水"),       // 药品-治疗
        POTION_MANA(24, "魔法药水"),       // 药品-魔法
        POTION_SPECIAL(25, "特殊药水"),    // 药品-特殊
        SCROLL_TOWN(26, "回城卷"),         // 卷轴-回城
        SCROLL_DUNGEON(27, "地牢卷"),      // 卷轴-地牢
        BOOK_SKILL(28, "技能书"),          // 书籍-技能
        BOOK_MAGIC(29, "魔法书"),          // 书籍-魔法
        MATERIAL_ORE(30, "矿石"),          // 材料-矿石
        MATERIAL_HERB(31, "草药"),         // 材料-草药
        MATERIAL_SPECIAL(32, "特殊材料"),  // 材料-特殊
        MISC_KEY(40, "钥匙"),              // 杂物-钥匙
        MISC_QUEST(41, "任务物品"),        // 杂物-任务
        MISC_SPECIAL(42, "特殊物品");      // 杂物-特殊
        
        private final int id;
        private final String name;
        
        ItemType(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public int getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        /**
         * 根据ID获取物品类型
         * 
         * @param id 类型ID
         * @return 物品类型
         */
        public static ItemType fromId(int id) {
            for (ItemType type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return MISC_SPECIAL;
        }
        
        /**
         * 是否为武器类型
         * 
         * @return 是否为武器
         */
        public boolean isWeapon() {
            return id >= 5 && id <= 7;
        }
        
        /**
         * 是否为防具类型
         * 
         * @return 是否为防具
         */
        public boolean isArmor() {
            return id >= 10 && id <= 12;
        }
        
        /**
         * 是否为首饰类型
         * 
         * @return 是否为首饰
         */
        public boolean isJewelry() {
            return id >= 19 && id <= 22;
        }
        
        /**
         * 是否为药品类型
         * 
         * @return 是否为药品
         */
        public boolean isPotion() {
            return id >= 23 && id <= 25;
        }
    }
    
    /**
     * 物品属性类
     */
    @Data
    public static class ItemAttributes {
        /** 攻击力 (物理攻击) */
        private int attack;
        
        /** 魔法攻击 */
        private int magicAttack;
        
        /** 道术攻击 */
        private int taoAttack;
        
        /** 物理防御 */
        private int defense;
        
        /** 魔法防御 */
        private int magicDefense;
        
        /** 准确度 */
        private int accuracy;
        
        /** 敏捷度 */
        private int agility;
        
        /** 生命值加成 */
        private int healthBonus;
        
        /** 魔法值加成 */
        private int manaBonus;
        
        /** 幸运值 */
        private int luck;
        
        /** 暴击率 */
        private int criticalRate;
        
        /** 暴击伤害 */
        private int criticalDamage;
        
        /** 吸血率 */
        private int lifeSteal;
        
        /** 魔法吸取 */
        private int manaSteal;
        
        /** 攻击速度 */
        private int attackSpeed;
        
        /** 移动速度 */
        private int moveSpeed;
        
        /** 经验加成 */
        private int expBonus;
        
        /** 额外属性 */
        private Map<String, Integer> extraAttributes = new HashMap<>();
        
        /**
         * 添加额外属性
         * 
         * @param name 属性名
         * @param value 属性值
         */
        public void addExtraAttribute(String name, int value) {
            extraAttributes.put(name, value);
        }
        
        /**
         * 获取额外属性
         * 
         * @param name 属性名
         * @return 属性值，如果不存在返回0
         */
        public int getExtraAttribute(String name) {
            return extraAttributes.getOrDefault(name, 0);
        }
        
        /**
         * 复制属性
         * 
         * @return 属性副本
         */
        public ItemAttributes copy() {
            ItemAttributes copy = new ItemAttributes();
            copy.attack = this.attack;
            copy.magicAttack = this.magicAttack;
            copy.taoAttack = this.taoAttack;
            copy.defense = this.defense;
            copy.magicDefense = this.magicDefense;
            copy.accuracy = this.accuracy;
            copy.agility = this.agility;
            copy.healthBonus = this.healthBonus;
            copy.manaBonus = this.manaBonus;
            copy.luck = this.luck;
            copy.criticalRate = this.criticalRate;
            copy.criticalDamage = this.criticalDamage;
            copy.lifeSteal = this.lifeSteal;
            copy.manaSteal = this.manaSteal;
            copy.attackSpeed = this.attackSpeed;
            copy.moveSpeed = this.moveSpeed;
            copy.expBonus = this.expBonus;
            copy.extraAttributes = new HashMap<>(this.extraAttributes);
            return copy;
        }
    }
} 