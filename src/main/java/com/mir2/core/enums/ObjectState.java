package com.mir2.core.enums;

/**
 * 游戏对象状态枚举
 * 
 * <p>定义了游戏对象的各种状态，用于控制对象的行为和显示。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public enum ObjectState {
    
    /** 正常状态 */
    NORMAL(0, "正常", "对象处于正常状态"),
    
    /** 死亡状态 */
    DEAD(1, "死亡", "对象已死亡"),
    
    /** 隐身状态 */
    INVISIBLE(2, "隐身", "对象处于隐身状态"),
    
    /** 中毒状态 */
    POISONED(3, "中毒", "对象中毒，持续损失生命值"),
    
    /** 石化状态 */
    STONE(4, "石化", "对象被石化，无法移动和攻击"),
    
    /** 麻痹状态 */
    PARALYZED(5, "麻痹", "对象被麻痹，无法移动但可以攻击"),
    
    /** 魔法盾状态 */
    MAGIC_SHIELD(6, "魔法盾", "对象受到魔法盾保护"),
    
    /** 隐藏状态 */
    HIDDEN(7, "隐藏", "对象处于隐藏状态"),
    
    /** 无敌状态 */
    INVINCIBLE(8, "无敌", "对象处于无敌状态"),
    
    /** 冰冻状态 */
    FROZEN(9, "冰冻", "对象被冰冻，无法行动"),
    
    /** 燃烧状态 */
    BURNING(10, "燃烧", "对象正在燃烧"),
    
    /** 治疗状态 */
    HEALING(11, "治疗", "对象正在被治疗"),
    
    /** 加速状态 */
    HASTE(12, "加速", "对象移动速度提升"),
    
    /** 减速状态 */
    SLOW(13, "减速", "对象移动速度降低"),
    
    /** 混乱状态 */
    CONFUSED(14, "混乱", "对象行动混乱"),
    
    /** 沉默状态 */
    SILENCED(15, "沉默", "对象无法使用魔法"),
    
    /** 虚弱状态 */
    WEAKENED(16, "虚弱", "对象攻击力降低"),
    
    /** 强化状态 */
    STRENGTHENED(17, "强化", "对象攻击力提升"),
    
    /** 防御状态 */
    DEFENDED(18, "防御", "对象防御力提升"),
    
    /** 脆弱状态 */
    VULNERABLE(19, "脆弱", "对象防御力降低");
    
    /** 状态ID */
    private final int id;
    
    /** 状态名称 */
    private final String name;
    
    /** 状态描述 */
    private final String description;
    
    /**
     * 构造函数
     * 
     * @param id 状态ID
     * @param name 状态名称
     * @param description 状态描述
     */
    ObjectState(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    /**
     * 获取状态ID
     * 
     * @return 状态ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * 获取状态名称
     * 
     * @return 状态名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取状态描述
     * 
     * @return 状态描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据ID获取状态
     * 
     * @param id 状态ID
     * @return 状态，如果找不到返回null
     */
    public static ObjectState fromId(int id) {
        for (ObjectState state : values()) {
            if (state.id == id) {
                return state;
            }
        }
        return null;
    }
    
    /**
     * 检查是否为负面状态
     * 
     * @return 如果是负面状态返回true，否则返回false
     */
    public boolean isNegativeState() {
        return this == DEAD || this == POISONED || this == STONE || 
               this == PARALYZED || this == FROZEN || this == BURNING ||
               this == SLOW || this == CONFUSED || this == SILENCED ||
               this == WEAKENED || this == VULNERABLE;
    }
    
    /**
     * 检查是否为正面状态
     * 
     * @return 如果是正面状态返回true，否则返回false
     */
    public boolean isPositiveState() {
        return this == MAGIC_SHIELD || this == INVINCIBLE || this == HEALING ||
               this == HASTE || this == STRENGTHENED || this == DEFENDED;
    }
    
    /**
     * 检查是否为特殊状态
     * 
     * @return 如果是特殊状态返回true，否则返回false
     */
    public boolean isSpecialState() {
        return this == INVISIBLE || this == HIDDEN;
    }
    
    /**
     * 检查是否影响移动
     * 
     * @return 如果影响移动返回true，否则返回false
     */
    public boolean affectsMovement() {
        return this == STONE || this == PARALYZED || this == FROZEN || 
               this == SLOW || this == HASTE;
    }
    
    /**
     * 检查是否影响攻击
     * 
     * @return 如果影响攻击返回true，否则返回false
     */
    public boolean affectsAttack() {
        return this == STONE || this == FROZEN || this == CONFUSED ||
               this == SILENCED || this == WEAKENED || this == STRENGTHENED;
    }
} 