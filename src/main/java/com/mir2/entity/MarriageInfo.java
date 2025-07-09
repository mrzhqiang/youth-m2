package com.mir2.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "marriage_info")
public class MarriageInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "husband", nullable = false)
    private String husband;
    
    @Column(name = "wife", nullable = false)
    private String wife;
    
    @Column(name = "marriage_date", nullable = false)
    private LocalDateTime marriageDate;
    
    @Column(name = "marriage_level", nullable = false)
    private int marriageLevel;
    
    @Column(name = "last_teleport_time")
    private Long lastTeleportTime;
    
    @Column(name = "total_love_points")
    private int totalLovePoints;
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    // 构造函数
    public MarriageInfo() {}
    
    public MarriageInfo(String husband, String wife) {
        this.husband = husband;
        this.wife = wife;
        this.marriageDate = LocalDateTime.now();
        this.marriageLevel = 1;
        this.totalLovePoints = 0;
        this.isActive = true;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getHusband() {
        return husband;
    }
    
    public void setHusband(String husband) {
        this.husband = husband;
    }
    
    public String getWife() {
        return wife;
    }
    
    public void setWife(String wife) {
        this.wife = wife;
    }
    
    public LocalDateTime getMarriageDate() {
        return marriageDate;
    }
    
    public void setMarriageDate(LocalDateTime marriageDate) {
        this.marriageDate = marriageDate;
    }
    
    public int getMarriageLevel() {
        return marriageLevel;
    }
    
    public void setMarriageLevel(int marriageLevel) {
        this.marriageLevel = marriageLevel;
    }
    
    public Long getLastTeleportTime() {
        return lastTeleportTime;
    }
    
    public void setLastTeleportTime(Long lastTeleportTime) {
        this.lastTeleportTime = lastTeleportTime;
    }
    
    public int getTotalLovePoints() {
        return totalLovePoints;
    }
    
    public void setTotalLovePoints(int totalLovePoints) {
        this.totalLovePoints = totalLovePoints;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    @Override
    public String toString() {
        return "MarriageInfo{" +
                "id=" + id +
                ", husband='" + husband + '\'' +
                ", wife='" + wife + '\'' +
                ", marriageDate=" + marriageDate +
                ", marriageLevel=" + marriageLevel +
                ", totalLovePoints=" + totalLovePoints +
                ", isActive=" + isActive +
                '}';
    }
} 