package com.mir2.core.model;

import com.mir2.core.enums.Direction;
import com.mir2.core.enums.GameObjectType;
import com.mir2.core.enums.ObjectState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 游戏对象基类
 * 
 * <p>所有游戏对象的基类，包含基本的属性和方法。
 * 对应原M2Engine中的BaseObject。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>提供统一的对象标识</li>
 *   <li>位置和方向管理</li>
 *   <li>对象状态管理</li>
 *   <li>基础属性定义</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Slf4j
public abstract class BaseObject {
    
    /** 对象ID生成器 */
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);
    
    /** 对象唯一ID */
    private final Long id;
    
    /** 对象名称 */
    private String name;
    
    /** 对象类型 */
    private GameObjectType objectType;
    
    /** X坐标 */
    private int x;
    
    /** Y坐标 */
    private int y;
    
    /** 面向方向 */
    private int direction;
    
    /** 所在地图名称 */
    private String mapName;
    
    /** 是否可见 */
    private boolean visible;
    
    /** 是否激活 */
    private boolean active;
    
    /** 创建时间 */
    private long createTime;
    
    /** 最后更新时间 */
    private long lastUpdateTime;
    
    /** 当前生命值 */
    private volatile int hp;
    
    /** 最大生命值 */
    private volatile int maxHp;
    
    /** 当前魔法值 */
    private volatile int mp;
    
    /** 最大魔法值 */
    private volatile int maxMp;
    
    /** 当前状态集合 */
    private final Set<ObjectState> states;
    
    /** 是否存活 */
    private volatile boolean alive;
    
    /** 最后移动时间 */
    private volatile long lastMoveTime;
    
    /** 最后攻击时间 */
    private volatile long lastAttackTime;
    
    /** 移动速度（毫秒） */
    private volatile int moveSpeed;
    
    /** 攻击速度（毫秒） */
    private volatile int attackSpeed;
    
    /**
     * 构造函数
     * 
     * @param name 对象名称
     * @param objectType 对象类型
     * @param x X坐标
     * @param y Y坐标
     * @param mapName 地图名称
     */
    protected BaseObject(String name, GameObjectType objectType, int x, int y, String mapName) {
        this.id = ID_GENERATOR.getAndIncrement();
        this.name = name;
        this.objectType = objectType;
        this.x = x;
        this.y = y;
        this.direction = 0;
        this.mapName = mapName;
        this.visible = true;
        this.active = true;
        this.createTime = System.currentTimeMillis();
        this.lastUpdateTime = createTime;
        this.alive = true;
        this.states = EnumSet.noneOf(ObjectState.class);
        this.moveSpeed = 600; // 默认移动速度
        this.attackSpeed = 1000; // 默认攻击速度
        
        // 初始化基础属性
        this.hp = 100;
        this.maxHp = 100;
        this.mp = 100;
        this.maxMp = 100;
        
        log.debug("创建游戏对象: {} [ID: {}, Type: {}, 位置: ({}, {}), 地图: {}]", 
                 name, id, objectType.getName(), x, y, mapName);
    }
    
    /**
     * 设置位置
     * 
     * @param x X坐标
     * @param y Y坐标
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * 设置位置和方向
     * 
     * @param x X坐标
     * @param y Y坐标
     * @param direction 方向
     */
    public void setPositionAndDirection(int x, int y, int direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * 计算与另一个对象的距离
     * 
     * @param other 另一个对象
     * @return 距离
     */
    public double distanceTo(BaseObject other) {
        if (other == null || !this.mapName.equals(other.mapName)) {
            return Double.MAX_VALUE;
        }
        
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * 计算与指定坐标的距离
     * 
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     * @return 距离
     */
    public double distanceTo(int targetX, int targetY) {
        int dx = this.x - targetX;
        int dy = this.y - targetY;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * 检查是否在指定范围内
     * 
     * @param centerX 中心X坐标
     * @param centerY 中心Y坐标
     * @param range 范围
     * @return 是否在范围内
     */
    public boolean isInRange(int centerX, int centerY, int range) {
        return distanceTo(centerX, centerY) <= range;
    }
    
    /**
     * 检查是否与另一个对象在同一地图
     * 
     * @param other 另一个对象
     * @return 是否在同一地图
     */
    public boolean isInSameMap(BaseObject other) {
        return other != null && this.mapName.equals(other.mapName);
    }
    
    /**
     * 更新对象状态
     * 
     * <p>子类可以重写此方法来实现特定的更新逻辑</p>
     */
    public void update() {
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * 销毁对象
     * 
     * <p>子类可以重写此方法来实现特定的清理逻辑</p>
     */
    public void destroy() {
        this.active = false;
        this.visible = false;
        log.debug("对象销毁: {} [ID: {}]", name, id);
    }
    
    /**
     * 获取对象信息
     * 
     * @return 对象信息字符串
     */
    public String getObjectInfo() {
        return String.format("%s [ID: %d, 类型: %s, 位置: (%d, %d), 地图: %s]", 
                name, id, objectType.getName(), x, y, mapName);
    }
    
    /**
     * 检查对象是否有效
     * 
     * @return 是否有效
     */
    public boolean isValid() {
        return active && name != null && !name.isEmpty() && mapName != null && !mapName.isEmpty();
    }
    
    /**
     * 复制基本属性到另一个对象
     * 
     * @param target 目标对象
     */
    protected void copyBasicPropertiesTo(BaseObject target) {
        if (target != null) {
            target.name = this.name;
            target.objectType = this.objectType;
            target.x = this.x;
            target.y = this.y;
            target.direction = this.direction;
            target.mapName = this.mapName;
            target.visible = this.visible;
            target.active = this.active;
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BaseObject other = (BaseObject) obj;
        return id.equals(other.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return getObjectInfo();
    }
} 