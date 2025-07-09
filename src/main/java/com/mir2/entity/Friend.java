package com.mir2.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "friend")
public class Friend {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "owner_name", nullable = false)
    private String ownerName;
    
    @Column(name = "friend_name", nullable = false)
    private String friendName;
    
    @Column(name = "add_time")
    private LocalDateTime addTime;
    
    @Column(name = "remark")
    private String remark;
    
    @Column(name = "group_name")
    private String group;
    
    // Constructors
    public Friend() {}
    
    public Friend(String ownerName, String friendName) {
        this.ownerName = ownerName;
        this.friendName = friendName;
        this.addTime = LocalDateTime.now();
        this.remark = "";
        this.group = "默认分组";
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    
    public String getFriendName() { return friendName; }
    public void setFriendName(String friendName) { this.friendName = friendName; }
    
    public LocalDateTime getAddTime() { return addTime; }
    public void setAddTime(LocalDateTime addTime) { this.addTime = addTime; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    
    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }
} 