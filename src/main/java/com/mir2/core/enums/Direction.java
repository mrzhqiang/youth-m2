package com.mir2.core.enums;

/**
 * 方向枚举
 * 
 * <p>定义了游戏中的8个方向，用于角色移动和朝向。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public enum Direction {
    
    /** 上 */
    UP(0, "上", 0, -1),
    
    /** 右上 */
    UP_RIGHT(1, "右上", 1, -1),
    
    /** 右 */
    RIGHT(2, "右", 1, 0),
    
    /** 右下 */
    DOWN_RIGHT(3, "右下", 1, 1),
    
    /** 下 */
    DOWN(4, "下", 0, 1),
    
    /** 左下 */
    DOWN_LEFT(5, "左下", -1, 1),
    
    /** 左 */
    LEFT(6, "左", -1, 0),
    
    /** 左上 */
    UP_LEFT(7, "左上", -1, -1);
    
    /** 方向ID */
    private final int id;
    
    /** 方向名称 */
    private final String name;
    
    /** X轴偏移量 */
    private final int offsetX;
    
    /** Y轴偏移量 */
    private final int offsetY;
    
    /**
     * 构造函数
     * 
     * @param id 方向ID
     * @param name 方向名称
     * @param offsetX X轴偏移量
     * @param offsetY Y轴偏移量
     */
    Direction(int id, String name, int offsetX, int offsetY) {
        this.id = id;
        this.name = name;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
    
    /**
     * 获取方向ID
     * 
     * @return 方向ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * 获取方向名称
     * 
     * @return 方向名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取X轴偏移量
     * 
     * @return X轴偏移量
     */
    public int getOffsetX() {
        return offsetX;
    }
    
    /**
     * 获取Y轴偏移量
     * 
     * @return Y轴偏移量
     */
    public int getOffsetY() {
        return offsetY;
    }
    
    /**
     * 根据ID获取方向
     * 
     * @param id 方向ID
     * @return 方向，如果找不到返回null
     */
    public static Direction fromId(int id) {
        for (Direction dir : values()) {
            if (dir.id == id) {
                return dir;
            }
        }
        return null;
    }
    
    /**
     * 根据坐标偏移量获取方向
     * 
     * @param deltaX X轴偏移量
     * @param deltaY Y轴偏移量
     * @return 方向，如果找不到返回null
     */
    public static Direction fromOffset(int deltaX, int deltaY) {
        for (Direction dir : values()) {
            if (dir.offsetX == deltaX && dir.offsetY == deltaY) {
                return dir;
            }
        }
        return null;
    }
    
    /**
     * 获取相反方向
     * 
     * @return 相反方向
     */
    public Direction getOpposite() {
        return fromId((id + 4) % 8);
    }
    
    /**
     * 计算两个坐标之间的方向
     * 
     * @param fromX 起始X坐标
     * @param fromY 起始Y坐标
     * @param toX 目标X坐标
     * @param toY 目标Y坐标
     * @return 方向
     */
    public static Direction calculateDirection(int fromX, int fromY, int toX, int toY) {
        int deltaX = toX - fromX;
        int deltaY = toY - fromY;
        
        if (deltaX == 0 && deltaY == 0) {
            return DOWN; // 默认向下
        }
        
        // 标准化偏移量
        int normalizedX = Integer.compare(deltaX, 0);
        int normalizedY = Integer.compare(deltaY, 0);
        
        return fromOffset(normalizedX, normalizedY);
    }
} 