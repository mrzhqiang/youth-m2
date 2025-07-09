package com.mir2.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 背包JPA实体类
 * 
 * <p>用于数据库持久化的背包实体，包括：</p>
 * <ul>
 *   <li>背包基本信息</li>
 *   <li>背包容量</li>
 *   <li>背包物品槽位</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "player_name", unique = true, nullable = false, length = 50)
    private String playerName;
    
    @Column(name = "capacity", nullable = false, columnDefinition = "INT DEFAULT 40")
    private Integer capacity;
    
    @Column(name = "slots_data", columnDefinition = "TEXT")
    private String slotsData; // JSON格式存储槽位数据
    
    @Column(name = "last_cleanup_time")
    private LocalDateTime lastCleanupTime;
    
    @Column(name = "expansion_level", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer expansionLevel;
    
    @Column(name = "locked", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean locked;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
    
    @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deleted;
    
    /**
     * 构造函数
     * 
     * @param playerName 玩家名称
     * @param capacity 背包容量
     */
    public InventoryEntity(String playerName, Integer capacity) {
        this.playerName = playerName;
        this.capacity = capacity;
        this.expansionLevel = 0;
        this.locked = false;
        this.deleted = false;
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
        this.lastCleanupTime = LocalDateTime.now();
    }
    
    /**
     * 从Inventory转换为Entity
     * 
     * @param inventory 背包对象
     * @return InventoryEntity
     */
    public static InventoryEntity fromInventory(Inventory inventory) {
        InventoryEntity entity = new InventoryEntity();
        entity.setPlayerName(inventory.getPlayerName());
        entity.setCapacity(inventory.getCapacity());
        entity.setExpansionLevel(inventory.getExpansionLevel());
        entity.setLocked(inventory.isLocked());
        entity.setDeleted(false);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        entity.setLastCleanupTime(LocalDateTime.now());
        
        // 转换槽位数据为JSON
        entity.setSlotsData(convertSlotsToJson(inventory.getSlots()));
        
        return entity;
    }
    
    /**
     * 转换为Inventory对象
     * 
     * @return Inventory对象
     */
    public Inventory toInventory() {
        Inventory inventory = new Inventory(this.playerName, this.capacity);
        inventory.setExpansionLevel(this.expansionLevel != null ? this.expansionLevel : 0);
        inventory.setLocked(this.locked != null ? this.locked : false);
        
        // 转换JSON数据为槽位
        List<Inventory.InventorySlot> slots = convertJsonToSlots(this.slotsData);
        inventory.setSlots(slots);
        
        return inventory;
    }
    
    /**
     * 更新时间戳
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedTime = LocalDateTime.now();
    }
    
    @PrePersist
    public void prePersist() {
        if (this.createdTime == null) {
            this.createdTime = LocalDateTime.now();
        }
        if (this.updatedTime == null) {
            this.updatedTime = LocalDateTime.now();
        }
        if (this.lastCleanupTime == null) {
            this.lastCleanupTime = LocalDateTime.now();
        }
        if (this.capacity == null) {
            this.capacity = 40;
        }
        if (this.expansionLevel == null) {
            this.expansionLevel = 0;
        }
        if (this.locked == null) {
            this.locked = false;
        }
        if (this.deleted == null) {
            this.deleted = false;
        }
    }
    
    // 辅助方法
    private static String convertSlotsToJson(List<Inventory.InventorySlot> slots) {
        if (slots == null || slots.isEmpty()) return "[]";
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(slots);
        } catch (Exception e) {
            return "[]";
        }
    }
    
    @SuppressWarnings("unchecked")
    private static List<Inventory.InventorySlot> convertJsonToSlots(String json) {
        if (json == null || json.trim().isEmpty() || "[]".equals(json.trim())) {
            return new java.util.ArrayList<>();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.core.type.TypeReference<List<Inventory.InventorySlot>> typeRef = 
                new com.fasterxml.jackson.core.type.TypeReference<List<Inventory.InventorySlot>>() {};
            return mapper.readValue(json, typeRef);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
} 