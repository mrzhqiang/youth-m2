package com.mir2.entity;

import com.mir2.core.model.Item;
import com.mir2.core.model.ItemType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 背包系统实体类
 * 
 * @author Mir2 Team
 */
@Data
@Slf4j
public class Inventory {
    
    /** 背包唯一ID */
    private String inventoryId;
    
    /** 玩家名称 */
    private String playerName;
    
    /** 背包容量 */
    private int capacity;
    
    /** 背包物品列表 */
    private List<InventorySlot> slots;
    
    /** 金币数量 */
    private int gold;
    
    /** 绑定金币数量 */
    private int boundGold;
    
    /** 背包类型 */
    private InventoryType type;
    
    /** 最后修改时间 */
    private long lastModified;
    
    /**
     * 背包类型枚举
     */
    public enum InventoryType {
        MAIN,      // 主背包
        TEMP,      // 临时背包
        QUEST,     // 任务背包
        TRADE      // 交易背包
    }
    
    /**
     * 背包槽位
     */
    @Data
    public static class InventorySlot {
        /** 槽位索引 */
        private int slotIndex;
        
        /** 物品 */
        private Item item;
        
        /** 数量 */
        private int quantity;
        
        /** 是否锁定 */
        private boolean locked;
        
        /** 过期时间 */
        private long expireTime;
        
        public InventorySlot(int slotIndex) {
            this.slotIndex = slotIndex;
            this.quantity = 0;
            this.locked = false;
            this.expireTime = 0;
        }
        
        public boolean isEmpty() {
            return item == null || quantity <= 0;
        }
        
        public boolean canStack(Item otherItem) {
            if (isEmpty() || otherItem == null) {
                return false;
            }
            
            // 检查是否为同一物品
            if (item.getItemId() != otherItem.getItemId()) {
                return false;
            }
            
            // 检查是否可堆叠
            if (!item.isStackable()) {
                return false;
            }
            
            // 检查属性是否相同
            return item.isSameItem(otherItem);
        }
        
        public int getAvailableStack() {
            if (isEmpty()) {
                return item != null ? item.getMaxStack() : 0;
            }
            return Math.max(0, item.getMaxStack() - quantity);
        }
    }
    
    /**
     * 默认构造函数
     */
    public Inventory() {
        this.capacity = 40; // 默认40格
        this.slots = new ArrayList<>();
        this.gold = 0;
        this.boundGold = 0;
        this.type = InventoryType.MAIN;
        this.lastModified = System.currentTimeMillis();
        initializeSlots();
    }
    
    /**
     * 构造函数
     */
    public Inventory(String playerName, int capacity) {
        this.inventoryId = UUID.randomUUID().toString();
        this.playerName = playerName;
        this.capacity = capacity;
        this.slots = new ArrayList<>();
        this.gold = 0;
        this.boundGold = 0;
        this.type = InventoryType.MAIN;
        this.lastModified = System.currentTimeMillis();
        initializeSlots();
    }
    
    /**
     * 初始化背包槽位
     */
    private void initializeSlots() {
        slots.clear();
        for (int i = 0; i < capacity; i++) {
            slots.add(new InventorySlot(i));
        }
    }
    
    /**
     * 获取空闲槽位数量
     */
    public int getFreeSlots() {
        return (int) slots.stream().filter(InventorySlot::isEmpty).count();
    }
    
    /**
     * 获取已使用槽位数量
     */
    public int getUsedSlots() {
        return capacity - getFreeSlots();
    }
    
    /**
     * 检查是否有足够空间
     */
    public boolean hasSpace(Item item, int quantity) {
        if (item == null || quantity <= 0) {
            return false;
        }
        
        int remainingQuantity = quantity;
        
        // 检查现有可堆叠槽位
        for (InventorySlot slot : slots) {
            if (slot.canStack(item)) {
                int availableStack = slot.getAvailableStack();
                remainingQuantity -= availableStack;
                if (remainingQuantity <= 0) {
                    return true;
                }
            }
        }
        
        // 检查空槽位
        int emptySlots = getFreeSlots();
        int maxPerSlot = item.getMaxStack();
        int slotsNeeded = (remainingQuantity + maxPerSlot - 1) / maxPerSlot;
        
        return slotsNeeded <= emptySlots;
    }
    
    /**
     * 添加物品到背包
     */
    public AddItemResult addItem(Item item, int quantity) {
        if (item == null || quantity <= 0) {
            return new AddItemResult(false, 0, "无效的物品或数量");
        }
        
        if (!hasSpace(item, quantity)) {
            return new AddItemResult(false, 0, "背包空间不足");
        }
        
        int remainingQuantity = quantity;
        int addedQuantity = 0;
        
        // 优先填充现有可堆叠槽位
        for (InventorySlot slot : slots) {
            if (remainingQuantity <= 0) break;
            
            if (slot.canStack(item)) {
                int availableStack = slot.getAvailableStack();
                int toAdd = Math.min(remainingQuantity, availableStack);
                
                slot.setQuantity(slot.getQuantity() + toAdd);
                remainingQuantity -= toAdd;
                addedQuantity += toAdd;
            }
        }
        
        // 使用空槽位
        for (InventorySlot slot : slots) {
            if (remainingQuantity <= 0) break;
            
            if (slot.isEmpty()) {
                int toAdd = Math.min(remainingQuantity, item.getMaxStack());
                
                slot.setItem(item.clone());
                slot.setQuantity(toAdd);
                remainingQuantity -= toAdd;
                addedQuantity += toAdd;
            }
        }
        
        updateLastModified();
        return new AddItemResult(true, addedQuantity, "成功添加物品");
    }
    
    /**
     * 从背包移除物品
     */
    public RemoveItemResult removeItem(int slotIndex, int quantity) {
        if (slotIndex < 0 || slotIndex >= capacity) {
            return new RemoveItemResult(false, null, 0, "无效的槽位索引");
        }
        
        InventorySlot slot = slots.get(slotIndex);
        if (slot.isEmpty()) {
            return new RemoveItemResult(false, null, 0, "槽位为空");
        }
        
        if (slot.isLocked()) {
            return new RemoveItemResult(false, null, 0, "物品已锁定");
        }
        
        if (quantity <= 0 || quantity > slot.getQuantity()) {
            return new RemoveItemResult(false, null, 0, "数量无效");
        }
        
        Item removedItem = slot.getItem().clone();
        int removedQuantity = quantity;
        
        slot.setQuantity(slot.getQuantity() - quantity);
        
        if (slot.getQuantity() <= 0) {
            slot.setItem(null);
            slot.setQuantity(0);
        }
        
        updateLastModified();
        return new RemoveItemResult(true, removedItem, removedQuantity, "成功移除物品");
    }
    
    /**
     * 根据物品ID移除物品
     */
    public RemoveItemResult removeItemById(int itemId, int quantity) {
        if (quantity <= 0) {
            return new RemoveItemResult(false, null, 0, "数量无效");
        }
        
        int remainingQuantity = quantity;
        Item removedItem = null;
        int totalRemoved = 0;
        
        for (InventorySlot slot : slots) {
            if (remainingQuantity <= 0) break;
            
            if (!slot.isEmpty() && slot.getItem().getItemId() == itemId && !slot.isLocked()) {
                int toRemove = Math.min(remainingQuantity, slot.getQuantity());
                
                if (removedItem == null) {
                    removedItem = slot.getItem().clone();
                }
                
                slot.setQuantity(slot.getQuantity() - toRemove);
                remainingQuantity -= toRemove;
                totalRemoved += toRemove;
                
                if (slot.getQuantity() <= 0) {
                    slot.setItem(null);
                    slot.setQuantity(0);
                }
            }
        }
        
        if (totalRemoved > 0) {
            updateLastModified();
            return new RemoveItemResult(true, removedItem, totalRemoved, "成功移除物品");
        }
        
        return new RemoveItemResult(false, null, 0, "未找到指定物品");
    }
    
    /**
     * 获取物品数量
     */
    public int getItemCount(int itemId) {
        return slots.stream()
                .filter(slot -> !slot.isEmpty() && slot.getItem().getItemId() == itemId)
                .mapToInt(InventorySlot::getQuantity)
                .sum();
    }
    
    /**
     * 查找物品
     */
    public List<InventorySlot> findItems(int itemId) {
        return slots.stream()
                .filter(slot -> !slot.isEmpty() && slot.getItem().getItemId() == itemId)
                .collect(Collectors.toList());
    }
    
    /**
     * 整理背包
     */
    public void organizeInventory() {
        // 收集所有物品
        Map<Integer, List<InventorySlot>> itemGroups = new HashMap<>();
        
        for (InventorySlot slot : slots) {
            if (!slot.isEmpty()) {
                int itemId = slot.getItem().getItemId();
                itemGroups.computeIfAbsent(itemId, k -> new ArrayList<>()).add(slot);
            }
        }
        
        // 清空背包
        for (InventorySlot slot : slots) {
            slot.setItem(null);
            slot.setQuantity(0);
        }
        
        // 重新排列物品
        int currentSlot = 0;
        
        for (List<InventorySlot> itemSlots : itemGroups.values()) {
            if (itemSlots.isEmpty()) continue;
            
            // 合并相同物品
            Item item = itemSlots.get(0).getItem();
            int totalQuantity = itemSlots.stream().mapToInt(InventorySlot::getQuantity).sum();
            
            // 分配到槽位
            while (totalQuantity > 0 && currentSlot < capacity) {
                InventorySlot slot = slots.get(currentSlot);
                int toAdd = Math.min(totalQuantity, item.getMaxStack());
                
                slot.setItem(item.clone());
                slot.setQuantity(toAdd);
                totalQuantity -= toAdd;
                currentSlot++;
            }
        }
        
        updateLastModified();
    }
    
    /**
     * 扩展背包容量
     */
    public boolean expandCapacity(int additionalSlots) {
        if (additionalSlots <= 0) {
            return false;
        }
        
        int newCapacity = capacity + additionalSlots;
        if (newCapacity > 200) { // 最大容量限制
            return false;
        }
        
        // 添加新槽位
        for (int i = capacity; i < newCapacity; i++) {
            slots.add(new InventorySlot(i));
        }
        
        capacity = newCapacity;
        updateLastModified();
        return true;
    }
    
    /**
     * 获取背包摘要信息
     */
    public InventorySummary getSummary() {
        int totalItems = 0;
        int totalValue = 0;
        Map<ItemType, Integer> typeCount = new HashMap<>();
        
        for (InventorySlot slot : slots) {
            if (!slot.isEmpty()) {
                totalItems += slot.getQuantity();
                totalValue += slot.getItem().getValue() * slot.getQuantity();
                
                ItemType type = slot.getItem().getType();
                typeCount.put(type, typeCount.getOrDefault(type, 0) + slot.getQuantity());
            }
        }
        
        return new InventorySummary(capacity, getUsedSlots(), totalItems, totalValue, typeCount);
    }
    
    /**
     * 更新最后修改时间
     */
    private void updateLastModified() {
        this.lastModified = System.currentTimeMillis();
    }
    
    /**
     * 添加物品结果
     */
    @Data
    public static class AddItemResult {
        private boolean success;
        private int addedQuantity;
        private String message;
        
        public AddItemResult(boolean success, int addedQuantity, String message) {
            this.success = success;
            this.addedQuantity = addedQuantity;
            this.message = message;
        }
    }
    
    /**
     * 移除物品结果
     */
    @Data
    public static class RemoveItemResult {
        private boolean success;
        private Item removedItem;
        private int removedQuantity;
        private String message;
        
        public RemoveItemResult(boolean success, Item removedItem, int removedQuantity, String message) {
            this.success = success;
            this.removedItem = removedItem;
            this.removedQuantity = removedQuantity;
            this.message = message;
        }
    }
    
    /**
     * 背包摘要信息
     */
    @Data
    public static class InventorySummary {
        private int capacity;
        private int usedSlots;
        private int totalItems;
        private int totalValue;
        private Map<ItemType, Integer> typeCount;
        
        public InventorySummary(int capacity, int usedSlots, int totalItems, int totalValue, Map<ItemType, Integer> typeCount) {
            this.capacity = capacity;
            this.usedSlots = usedSlots;
            this.totalItems = totalItems;
            this.totalValue = totalValue;
            this.typeCount = typeCount;
        }
    }
} 