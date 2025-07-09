package com.mir2.core.enums;

/**
 * 游戏对象类型枚举
 * 
 * <p>定义游戏中所有对象的类型。
 * 对应原M2Engine中的对象类型定义。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public enum GameObjectType {
    /** 玩家 */
    PLAYER(0, "玩家"),
    
    /** NPC */
    NPC(1, "NPC"),
    
    /** 怪物 */
    MONSTER(2, "怪物"),
    
    /** 物品 */
    ITEM(3, "物品"),
    
    /** 陷阱 */
    TRAP(4, "陷阱"),
    
    /** 传送门 */
    PORTAL(5, "传送门"),
    
    /** 火墙 */
    FIREWALL(6, "火墙"),
    
    /** 魔法屏障 */
    MAGIC_SHIELD(7, "魔法屏障"),
    
    /** 光柱 */
    LIGHT_BEAM(8, "光柱"),
    
    /** 爆炸 */
    EXPLOSION(9, "爆炸"),
    
    /** 召唤物 */
    SUMMONED(10, "召唤物"),
    
    /** 宠物 */
    PET(11, "宠物"),
    
    /** 镖车 */
    CART(12, "镖车"),
    
    /** 攻城器械 */
    SIEGE_ENGINE(13, "攻城器械"),
    
    /** 建筑物 */
    BUILDING(14, "建筑物"),
    
    /** 装饰物 */
    DECORATION(15, "装饰物"),
    
    /** 特效 */
    EFFECT(16, "特效"),
    
    /** 触发器 */
    TRIGGER(17, "触发器"),
    
    /** 采集物 */
    GATHERABLE(18, "采集物"),
    
    /** 机关 */
    MECHANISM(19, "机关");
    
    private final int id;
    private final String name;
    
    GameObjectType(int id, String name) {
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
     * 根据ID获取对象类型
     * 
     * @param id 类型ID
     * @return 对象类型
     */
    public static GameObjectType fromId(int id) {
        for (GameObjectType type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return PLAYER; // 默认返回玩家类型
    }
    
    /**
     * 根据名称获取对象类型
     * 
     * @param name 类型名称
     * @return 对象类型
     */
    public static GameObjectType fromName(String name) {
        for (GameObjectType type : values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return PLAYER; // 默认返回玩家类型
    }
    
    /**
     * 检查是否为可移动对象
     * 
     * @return 是否可移动
     */
    public boolean isMovable() {
        return this == PLAYER || this == MONSTER || this == NPC || 
               this == SUMMONED || this == PET || this == CART;
    }
    
    /**
     * 检查是否为生物
     * 
     * @return 是否为生物
     */
    public boolean isLiving() {
        return this == PLAYER || this == MONSTER || this == NPC || 
               this == SUMMONED || this == PET;
    }
    
    /**
     * 检查是否为可攻击对象
     * 
     * @return 是否可攻击
     */
    public boolean isAttackable() {
        return this == PLAYER || this == MONSTER || this == SUMMONED || 
               this == PET || this == BUILDING || this == SIEGE_ENGINE;
    }
    
    /**
     * 检查是否为临时对象
     * 
     * @return 是否为临时对象
     */
    public boolean isTemporary() {
        return this == FIREWALL || this == MAGIC_SHIELD || this == LIGHT_BEAM || 
               this == EXPLOSION || this == EFFECT;
    }
    
    /**
     * 检查是否为静态对象
     * 
     * @return 是否为静态对象
     */
    public boolean isStatic() {
        return this == ITEM || this == TRAP || this == PORTAL || 
               this == BUILDING || this == DECORATION || this == GATHERABLE || 
               this == MECHANISM;
    }
} 