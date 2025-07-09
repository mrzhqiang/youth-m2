package com.mir2.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "black_list")
public class BlackList {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "owner_name", nullable = false)
    private String ownerName;
    
    @Column(name = "target_name", nullable = false)
    private String targetName;
    
    @Column(name = "add_time")
    private LocalDateTime addTime;
    
    @Column(name = "reason")
    private String reason;
    
    // Constructors
    public BlackList() {}
    
    public BlackList(String ownerName, String targetName, String reason) {
        this.ownerName = ownerName;
        this.targetName = targetName;
        this.reason = reason;
        this.addTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    
    public LocalDateTime getAddTime() { return addTime; }
    public void setAddTime(LocalDateTime addTime) { this.addTime = addTime; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
} 