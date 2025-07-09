package com.mir2.entity;

import javax.persistence.*;

@Entity
@Table(name = "enhancement_info")
public class EnhancementInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "player_name")
    private String playerName;
    
    @Column(name = "item_name")
    private String itemName;
    
    @Column(name = "item_id")
    private Long itemId;
    
    @Column(name = "enhance_level")
    private int enhanceLevel;
    
    @Column(name = "success")
    private boolean success;
    
    @Column(name = "timestamp")
    private Long timestamp;
    
    // Constructors
    public EnhancementInfo() {}
    
    public EnhancementInfo(String playerName, String itemName, Long itemId, int enhanceLevel, boolean success) {
        this.playerName = playerName;
        this.itemName = itemName;
        this.itemId = itemId;
        this.enhanceLevel = enhanceLevel;
        this.success = success;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    
    public int getEnhanceLevel() { return enhanceLevel; }
    public void setEnhanceLevel(int enhanceLevel) { this.enhanceLevel = enhanceLevel; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
} 