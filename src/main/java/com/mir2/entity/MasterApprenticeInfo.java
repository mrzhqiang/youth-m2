package com.mir2.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "master_apprentice_info")
public class MasterApprenticeInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "master_name", nullable = false)
    private String masterName;
    
    @Column(name = "apprentice_name", nullable = false, unique = true)
    private String apprenticeName;
    
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "level", nullable = false)
    private int level;
    
    @Column(name = "exp_bonus")
    private int expBonus;
    
    @Column(name = "last_recall_time")
    private Long lastRecallTime;
    
    @Column(name = "total_shared_exp")
    private long totalSharedExp;
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    // 构造函数
    public MasterApprenticeInfo() {}
    
    public MasterApprenticeInfo(String masterName, String apprenticeName) {
        this.masterName = masterName;
        this.apprenticeName = apprenticeName;
        this.startDate = LocalDateTime.now();
        this.level = 1;
        this.expBonus = 0;
        this.totalSharedExp = 0;
        this.isActive = true;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getMasterName() {
        return masterName;
    }
    
    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }
    
    public String getApprenticeName() {
        return apprenticeName;
    }
    
    public void setApprenticeName(String apprenticeName) {
        this.apprenticeName = apprenticeName;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public int getExpBonus() {
        return expBonus;
    }
    
    public void setExpBonus(int expBonus) {
        this.expBonus = expBonus;
    }
    
    public Long getLastRecallTime() {
        return lastRecallTime;
    }
    
    public void setLastRecallTime(Long lastRecallTime) {
        this.lastRecallTime = lastRecallTime;
    }
    
    public long getTotalSharedExp() {
        return totalSharedExp;
    }
    
    public void setTotalSharedExp(long totalSharedExp) {
        this.totalSharedExp = totalSharedExp;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    @Override
    public String toString() {
        return "MasterApprenticeInfo{" +
                "id=" + id +
                ", masterName='" + masterName + '\'' +
                ", apprenticeName='" + apprenticeName + '\'' +
                ", startDate=" + startDate +
                ", level=" + level +
                ", expBonus=" + expBonus +
                ", totalSharedExp=" + totalSharedExp +
                ", isActive=" + isActive +
                '}';
    }
} 