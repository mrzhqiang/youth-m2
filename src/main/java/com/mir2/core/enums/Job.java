package com.mir2.core.enums;

/**
 * 职业枚举
 * 
 * <p>定义了传奇游戏中的三大职业，每个职业都有其特色和技能。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public enum Job {
    
    /** 战士 */
    WARRIOR(0, "战士", "物理攻击职业，拥有强大的近战能力和防御力"),
    
    /** 法师 */
    WIZARD(1, "法师", "魔法攻击职业，拥有强大的远程法术攻击能力"),
    
    /** 道士 */
    TAOIST(2, "道士", "辅助职业，拥有治疗、召唤和毒术等多样化技能");
    
    /** 职业ID */
    private final int id;
    
    /** 职业名称 */
    private final String name;
    
    /** 职业描述 */
    private final String description;
    
    /**
     * 构造函数
     * 
     * @param id 职业ID
     * @param name 职业名称
     * @param description 职业描述
     */
    Job(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    /**
     * 获取职业ID
     * 
     * @return 职业ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * 获取职业名称
     * 
     * @return 职业名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取职业描述
     * 
     * @return 职业描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据ID获取职业
     * 
     * @param id 职业ID
     * @return 职业，如果找不到返回null
     */
    public static Job fromId(int id) {
        for (Job job : values()) {
            if (job.id == id) {
                return job;
            }
        }
        return null;
    }
    
    /**
     * 根据名称获取职业
     * 
     * @param name 职业名称
     * @return 职业，如果找不到返回null
     */
    public static Job fromName(String name) {
        for (Job job : values()) {
            if (job.name.equals(name)) {
                return job;
            }
        }
        return null;
    }
    
    /**
     * 获取职业的主要属性
     * 
     * @return 主要属性描述
     */
    public String getPrimaryAttribute() {
        switch (this) {
            case WARRIOR:
                return "力量和体力";
            case WIZARD:
                return "智力和魔法";
            case TAOIST:
                return "精神和道术";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取职业的武器类型
     * 
     * @return 武器类型描述
     */
    public String getWeaponType() {
        switch (this) {
            case WARRIOR:
                return "刀剑类武器";
            case WIZARD:
                return "法杖类武器";
            case TAOIST:
                return "法杖、剑类武器";
            default:
                return "未知";
        }
    }
} 