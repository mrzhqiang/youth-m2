package com.mir2.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "castle")
public class Castle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Column(name = "map_name", nullable = false)
    private String mapName;
    
    @Column(name = "x", nullable = false)
    private int x;
    
    @Column(name = "y", nullable = false)
    private int y;
    
    @ManyToOne
    @JoinColumn(name = "owner_guild_id")
    private Guild ownerGuild;
    
    @Column(name = "war_in_progress")
    private boolean warInProgress = false;
    
    @Column(name = "war_start_time")
    private LocalDateTime warStartTime;
    
    @Column(name = "war_end_time")
    private LocalDateTime warEndTime;
    
    @Column(name = "change_date")
    private LocalDateTime changeDate;
    
    @Column(name = "main_door_hp")
    private int mainDoorHp = 10000;
    
    @Column(name = "left_wall_hp")
    private int leftWallHp = 8000;
    
    @Column(name = "center_wall_hp")
    private int centerWallHp = 8000;
    
    @Column(name = "right_wall_hp")
    private int rightWallHp = 8000;
    
    @Column(name = "tax_rate")
    private float taxRate = 0.1f;
    
    @Column(name = "total_gold")
    private int totalGold = 0;
    
    @Column(name = "today_income")
    private int todayIncome = 0;
    
    @Column(name = "tech_level")
    private int techLevel = 1;
    
    @Column(name = "power")
    private int power = 100;
    
    @ElementCollection
    @CollectionTable(name = "castle_attacking_guilds", 
                     joinColumns = @JoinColumn(name = "castle_id"))
    @Column(name = "guild_name")
    private List<String> attackingGuilds = new ArrayList<>();
    
    // 构造函数
    public Castle() {}
    
    public Castle(String name, String mapName, int x, int y) {
        this.name = name;
        this.mapName = mapName;
        this.x = x;
        this.y = y;
        this.warInProgress = false;
        this.taxRate = 0.1f;
        this.totalGold = 0;
        this.todayIncome = 0;
        this.techLevel = 1;
        this.power = 100;
        this.attackingGuilds = new ArrayList<>();
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
    
    public String getMapName() {
        return mapName;
    }
    
    public void setMapName(String mapName) {
        this.mapName = mapName;
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
    
    public Guild getOwnerGuild() {
        return ownerGuild;
    }
    
    public void setOwnerGuild(Guild ownerGuild) {
        this.ownerGuild = ownerGuild;
    }
    
    public boolean isWarInProgress() {
        return warInProgress;
    }
    
    public void setWarInProgress(boolean warInProgress) {
        this.warInProgress = warInProgress;
    }
    
    public LocalDateTime getWarStartTime() {
        return warStartTime;
    }
    
    public void setWarStartTime(LocalDateTime warStartTime) {
        this.warStartTime = warStartTime;
    }
    
    public LocalDateTime getWarEndTime() {
        return warEndTime;
    }
    
    public void setWarEndTime(LocalDateTime warEndTime) {
        this.warEndTime = warEndTime;
    }
    
    public LocalDateTime getChangeDate() {
        return changeDate;
    }
    
    public void setChangeDate(LocalDateTime changeDate) {
        this.changeDate = changeDate;
    }
    
    public int getMainDoorHp() {
        return mainDoorHp;
    }
    
    public void setMainDoorHp(int mainDoorHp) {
        this.mainDoorHp = mainDoorHp;
    }
    
    public int getLeftWallHp() {
        return leftWallHp;
    }
    
    public void setLeftWallHp(int leftWallHp) {
        this.leftWallHp = leftWallHp;
    }
    
    public int getCenterWallHp() {
        return centerWallHp;
    }
    
    public void setCenterWallHp(int centerWallHp) {
        this.centerWallHp = centerWallHp;
    }
    
    public int getRightWallHp() {
        return rightWallHp;
    }
    
    public void setRightWallHp(int rightWallHp) {
        this.rightWallHp = rightWallHp;
    }
    
    public float getTaxRate() {
        return taxRate;
    }
    
    public void setTaxRate(float taxRate) {
        this.taxRate = taxRate;
    }
    
    public int getTotalGold() {
        return totalGold;
    }
    
    public void setTotalGold(int totalGold) {
        this.totalGold = totalGold;
    }
    
    public int getTodayIncome() {
        return todayIncome;
    }
    
    public void setTodayIncome(int todayIncome) {
        this.todayIncome = todayIncome;
    }
    
    public int getTechLevel() {
        return techLevel;
    }
    
    public void setTechLevel(int techLevel) {
        this.techLevel = techLevel;
    }
    
    public int getPower() {
        return power;
    }
    
    public void setPower(int power) {
        this.power = power;
    }
    
    public List<String> getAttackingGuilds() {
        return attackingGuilds;
    }
    
    public void setAttackingGuilds(List<String> attackingGuilds) {
        this.attackingGuilds = attackingGuilds;
    }
    
    @Override
    public String toString() {
        return "Castle{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mapName='" + mapName + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", ownerGuild=" + (ownerGuild != null ? ownerGuild.getName() : "无") +
                ", warInProgress=" + warInProgress +
                ", techLevel=" + techLevel +
                ", power=" + power +
                '}';
    }
} 