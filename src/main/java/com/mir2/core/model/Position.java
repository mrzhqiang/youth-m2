package com.mir2.core.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 位置坐标类
 * 
 * <p>表示游戏中的二维坐标位置，包含X、Y坐标和所在地图信息。
 * 对应原M2Engine中的坐标系统。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Position {
    
    /** X坐标 */
    private int x;
    
    /** Y坐标 */
    private int y;
    
    /** 地图名称 */
    private String mapName;
    
    /**
     * 构造函数（不包含地图名称）
     * 
     * @param x X坐标
     * @param y Y坐标
     */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * 计算与另一个位置的距离
     * 
     * @param other 另一个位置
     * @return 距离
     */
    public double distanceTo(Position other) {
        if (other == null) {
            return Double.MAX_VALUE;
        }
        
        // 如果不在同一地图，认为距离无穷大
        if (!isSameMap(other)) {
            return Double.MAX_VALUE;
        }
        
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * 计算与另一个位置的曼哈顿距离
     * 
     * @param other 另一个位置
     * @return 曼哈顿距离
     */
    public int manhattanDistanceTo(Position other) {
        if (other == null || !isSameMap(other)) {
            return Integer.MAX_VALUE;
        }
        
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }
    
    /**
     * 检查是否在同一地图
     * 
     * @param other 另一个位置
     * @return 是否在同一地图
     */
    public boolean isSameMap(Position other) {
        if (other == null) {
            return false;
        }
        
        if (this.mapName == null && other.mapName == null) {
            return true;
        }
        
        return this.mapName != null && this.mapName.equals(other.mapName);
    }
    
    /**
     * 检查是否在指定范围内
     * 
     * @param other 目标位置
     * @param range 范围
     * @return 是否在范围内
     */
    public boolean isWithinRange(Position other, int range) {
        if (other == null || !isSameMap(other)) {
            return false;
        }
        
        return manhattanDistanceTo(other) <= range;
    }
    
    /**
     * 获取相对于另一个位置的方向
     * 
     * @param other 另一个位置
     * @return 方向（0-7，8个方向）
     */
    public int getDirectionTo(Position other) {
        if (other == null || !isSameMap(other)) {
            return 0;
        }
        
        int dx = other.x - this.x;
        int dy = other.y - this.y;
        
        if (dx == 0 && dy == 0) {
            return 0; // 同一位置
        }
        
        // 8个方向：0=上, 1=右上, 2=右, 3=右下, 4=下, 5=左下, 6=左, 7=左上
        if (dx > 0) {
            if (dy > 0) return 3; // 右下
            else if (dy < 0) return 1; // 右上
            else return 2; // 右
        } else if (dx < 0) {
            if (dy > 0) return 5; // 左下
            else if (dy < 0) return 7; // 左上
            else return 6; // 左
        } else {
            if (dy > 0) return 4; // 下
            else return 0; // 上
        }
    }
    
    /**
     * 获取指定方向的相邻位置
     * 
     * @param direction 方向（0-7）
     * @return 相邻位置
     */
    public Position getAdjacentPosition(int direction) {
        int newX = this.x;
        int newY = this.y;
        
        switch (direction) {
            case 0: newY--; break; // 上
            case 1: newX++; newY--; break; // 右上
            case 2: newX++; break; // 右
            case 3: newX++; newY++; break; // 右下
            case 4: newY++; break; // 下
            case 5: newX--; newY++; break; // 左下
            case 6: newX--; break; // 左
            case 7: newX--; newY--; break; // 左上
        }
        
        return new Position(newX, newY, this.mapName);
    }
    
    /**
     * 复制位置
     * 
     * @return 位置副本
     */
    public Position copy() {
        return new Position(this.x, this.y, this.mapName);
    }
    
    /**
     * 设置坐标
     * 
     * @param x X坐标
     * @param y Y坐标
     */
    public void setCoordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * 偏移坐标
     * 
     * @param deltaX X偏移量
     * @param deltaY Y偏移量
     */
    public void offset(int deltaX, int deltaY) {
        this.x += deltaX;
        this.y += deltaY;
    }
    
    /**
     * 检查坐标是否有效
     * 
     * @return 是否有效
     */
    public boolean isValid() {
        return x >= 0 && y >= 0 && mapName != null && !mapName.isEmpty();
    }
    
    /**
     * 获取周围的位置列表
     * 
     * @param radius 半径
     * @return 周围位置列表
     */
    public java.util.List<Position> getSurroundingPositions(int radius) {
        java.util.List<Position> positions = new java.util.ArrayList<>();
        
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                if (i == 0 && j == 0) continue; // 跳过自己
                
                int newX = this.x + i;
                int newY = this.y + j;
                
                if (newX >= 0 && newY >= 0) {
                    positions.add(new Position(newX, newY, this.mapName));
                }
            }
        }
        
        return positions;
    }
    
    /**
     * 获取格式化的位置字符串
     * 
     * @return 格式化字符串
     */
    public String getFormattedString() {
        if (mapName != null && !mapName.isEmpty()) {
            return String.format("(%d, %d) @ %s", x, y, mapName);
        } else {
            return String.format("(%d, %d)", x, y);
        }
    }
    
    @Override
    public String toString() {
        return getFormattedString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Position position = (Position) obj;
        return x == position.x && 
               y == position.y && 
               java.util.Objects.equals(mapName, position.mapName);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y, mapName);
    }
} 