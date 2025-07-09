package com.mir2.entity;

import javax.persistence.*;

@Entity
@Table(name = "castle_guard")
public class CastleGuard {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "type", nullable = false)
    private String type; // ARCHER, GUARD
    
    @Column(name = "castle_name", nullable = false)
    private String castleName;
    
    @Column(name = "x")
    private int x;
    
    @Column(name = "y")
    private int y;
    
    @Column(name = "max_hp", nullable = false)
    private int maxHp;
    
    @Column(name = "current_hp", nullable = false)
    private int currentHp;
    
    @Column(name = "attack_power", nullable = false)
    private int attackPower;
    
    @Column(name = "defense", nullable = false)
    private int defense;
    
    @Column(name = "attack_range")
    private int attackRange = 8;
    
    @Column(name = "is_alive")
    private boolean isAlive = true;
    
    @Column(name = "respawn_time")
    private long respawnTime = 0;
    
    // 构造函数
    public CastleGuard() {}
    
    public CastleGuard(String name, String type, String castleName) {
        this.name = name;
        this.type = type;
        this.castleName = castleName;
        this.isAlive = true;
        
        // 根据类型设置默认属性
        if ("ARCHER".equals(type)) {
            this.maxHp = 5000;
            this.currentHp = 5000;
            this.attackPower = 200;
            this.defense = 100;
            this.attackRange = 8;
        } else if ("GUARD".equals(type)) {
            this.maxHp = 8000;
            this.currentHp = 8000;
            this.attackPower = 300;
            this.defense = 150;
            this.attackRange = 1;
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getCastleName() {
        return castleName;
    }
    
    public void setCastleName(String castleName) {
        this.castleName = castleName;
    }
    
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public int getMaxHp() {
        return maxHp;
    }
    
    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }
    
    public int getCurrentHp() {
        return currentHp;
    }
    
    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }
    
    public int getAttackPower() {
        return attackPower;
    }
    
    public void setAttackPower(int attackPower) {
        this.attackPower = attackPower;
    }
    
    public int getDefense() {
        return defense;
    }
    
    public void setDefense(int defense) {
        this.defense = defense;
    }
    
    public int getAttackRange() {
        return attackRange;
    }
    
    public void setAttackRange(int attackRange) {
        this.attackRange = attackRange;
    }
    
    public boolean isAlive() {
        return isAlive;
    }
    
    public void setAlive(boolean alive) {
        isAlive = alive;
    }
    
    public long getRespawnTime() {
        return respawnTime;
    }
    
    public void setRespawnTime(long respawnTime) {
        this.respawnTime = respawnTime;
    }
    
    /**
     * 受到伤害
     */
    public boolean takeDamage(int damage) {
        if (!isAlive) {
            return false;
        }
        
        int actualDamage = Math.max(1, damage - defense);
        currentHp = Math.max(0, currentHp - actualDamage);
        
        if (currentHp <= 0) {
            isAlive = false;
            respawnTime = System.currentTimeMillis() + 300000; // 5分钟后重生
            return true; // 死亡
        }
        
        return false;
    }
    
    /**
     * 检查是否可以重生
     */
    public boolean canRespawn() {
        return !isAlive && System.currentTimeMillis() >= respawnTime;
    }
    
    /**
     * 重生
     */
    public void respawn() {
        if (canRespawn()) {
            isAlive = true;
            currentHp = maxHp;
            respawnTime = 0;
        }
    }
    
    /**
     * 治疗
     */
    public void heal(int amount) {
        if (isAlive) {
            currentHp = Math.min(maxHp, currentHp + amount);
        }
    }
    
    @Override
    public String toString() {
        return "CastleGuard{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", castleName='" + castleName + '\'' +
                ", currentHp=" + currentHp +
                ", maxHp=" + maxHp +
                ", attackPower=" + attackPower +
                ", defense=" + defense +
                ", isAlive=" + isAlive +
                '}';
    }
} 