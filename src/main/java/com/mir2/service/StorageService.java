package com.mir2.service;

import com.mir2.core.model.Player;
import com.mir2.core.model.Item;
import com.mir2.core.model.Guild;
import com.mir2.entity.Storage;
import com.mir2.entity.Inventory;
import com.mir2.entity.Inventory.AddItemResult;
import com.mir2.entity.Inventory.RemoveItemResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class StorageService {
    
    private final Map<String, Storage> personalStorages = new ConcurrentHashMap<>();
    private final Map<String, Storage> guildStorages = new ConcurrentHashMap<>();
    
    @Autowired
    private InventoryService inventoryService;
    
    // 仓库容量配置
    private static final int BASIC_STORAGE_SIZE = 40;        // 基础仓库40格
    private static final int EXPANDED_STORAGE_SIZE = 80;     // 扩展仓库80格
    private static final int GUILD_STORAGE_SIZE = 200;       // 行会仓库200格
    
    // 仓库扩展费用
    private static final int STORAGE_EXPANSION_COST = 5000000; // 500万金币
    
    /**
     * 获取个人仓库
     */
    public Storage getPersonalStorage(String playerName) {
        return personalStorages.computeIfAbsent(playerName, k -> {
            Storage storage = new Storage();
            storage.setOwnerName(playerName);
            storage.setType("PERSONAL");
            storage.setMaxSize(BASIC_STORAGE_SIZE);
            storage.setItems(new ArrayList<>());
            storage.setPassword("");
            storage.setExpanded(false);
            return storage;
        });
    }
    
    /**
     * 获取行会仓库
     */
    public Storage getGuildStorage(String guildName) {
        return guildStorages.computeIfAbsent(guildName, k -> {
            Storage storage = new Storage();
            storage.setOwnerName(guildName);
            storage.setType("GUILD");
            storage.setMaxSize(GUILD_STORAGE_SIZE);
            storage.setItems(new ArrayList<>());
            storage.setPassword("");
            storage.setExpanded(true);
            return storage;
        });
    }
    
    /**
     * 存储物品到个人仓库
     */
    @Transactional
    public StorageResult depositItem(Player player, Item item, int quantity) {
        Storage storage = getPersonalStorage(player.getUsername());
        
        // 检查仓库是否有足够空间
        if (!hasSpace(storage, item, quantity)) {
            return new StorageResult(false, "仓库空间不足");
        }
        
        // 检查物品是否可以存储
        if (!canStoreItem(item)) {
            return new StorageResult(false, "该物品无法存储到仓库");
        }
        
        // 存储物品
        boolean success = addItemToStorage(storage, item, quantity);
        if (success) {
            // 从玩家背包移除物品
            removeItemFromInventory(player, item, quantity);
            
            // 记录存储日志
            recordStorageOperation(player.getUsername(), item, quantity, "DEPOSIT");
            
            return new StorageResult(true, "物品已存储到仓库");
        } else {
            return new StorageResult(false, "存储失败");
        }
    }
    
    /**
     * 从个人仓库取出物品
     */
    @Transactional
    public StorageResult withdrawItem(Player player, String itemId, int quantity) {
        Storage storage = getPersonalStorage(player.getUsername());
        
        // 查找物品
        Item item = findItemInStorage(storage, itemId);
        if (item == null) {
            return new StorageResult(false, "仓库中未找到该物品");
        }
        
        // 检查数量
        if (item.getQuantity() < quantity) {
            return new StorageResult(false, "仓库中该物品数量不足");
        }
        
        // 检查背包空间
        if (!hasInventorySpace(player, item, quantity)) {
            return new StorageResult(false, "背包空间不足");
        }
        
        // 取出物品
        boolean success = removeItemFromStorage(storage, item, quantity);
        if (success) {
            // 添加到玩家背包
            addItemToInventory(player, item, quantity);
            
            // 记录取出日志
            recordStorageOperation(player.getUsername(), item, quantity, "WITHDRAW");
            
            return new StorageResult(true, "物品已取出到背包");
        } else {
            return new StorageResult(false, "取出失败");
        }
    }
    
    /**
     * 存储物品到行会仓库
     */
    @Transactional
    public StorageResult depositToGuildStorage(Player player, Guild guild, Item item, int quantity) {
        // 检查权限
        if (!canAccessGuildStorage(player, guild)) {
            return new StorageResult(false, "没有权限访问行会仓库");
        }
        
        Storage storage = getGuildStorage(guild.getName());
        
        // 检查仓库空间
        if (!hasSpace(storage, item, quantity)) {
            return new StorageResult(false, "行会仓库空间不足");
        }
        
        // 检查物品是否可以存储
        if (!canStoreItem(item)) {
            return new StorageResult(false, "该物品无法存储到行会仓库");
        }
        
        // 存储物品
        boolean success = addItemToStorage(storage, item, quantity);
        if (success) {
            removeItemFromInventory(player, item, quantity);
            recordStorageOperation(player.getUsername(), item, quantity, "GUILD_DEPOSIT");
            
            return new StorageResult(true, "物品已存储到行会仓库");
        } else {
            return new StorageResult(false, "存储失败");
        }
    }
    
    /**
     * 从行会仓库取出物品
     */
    @Transactional
    public StorageResult withdrawFromGuildStorage(Player player, Guild guild, String itemId, int quantity) {
        // 检查权限
        if (!canAccessGuildStorage(player, guild)) {
            return new StorageResult(false, "没有权限访问行会仓库");
        }
        
        Storage storage = getGuildStorage(guild.getName());
        Item item = findItemInStorage(storage, itemId);
        
        if (item == null) {
            return new StorageResult(false, "行会仓库中未找到该物品");
        }
        
        if (item.getQuantity() < quantity) {
            return new StorageResult(false, "行会仓库中该物品数量不足");
        }
        
        if (!hasInventorySpace(player, item, quantity)) {
            return new StorageResult(false, "背包空间不足");
        }
        
        boolean success = removeItemFromStorage(storage, item, quantity);
        if (success) {
            addItemToInventory(player, item, quantity);
            recordStorageOperation(player.getUsername(), item, quantity, "GUILD_WITHDRAW");
            
            return new StorageResult(true, "物品已从行会仓库取出");
        } else {
            return new StorageResult(false, "取出失败");
        }
    }
    
    /**
     * 扩展个人仓库
     */
    @Transactional
    public StorageResult expandPersonalStorage(Player player) {
        Storage storage = getPersonalStorage(player.getUsername());
        
        if (storage.isExpanded()) {
            return new StorageResult(false, "仓库已经扩展过了");
        }
        
        if (player.getGold() < STORAGE_EXPANSION_COST) {
            return new StorageResult(false, "金币不足，需要" + STORAGE_EXPANSION_COST + "金币");
        }
        
        // 扣除金币并扩展仓库
        player.setGold(player.getGold() - STORAGE_EXPANSION_COST);
        storage.setMaxSize(EXPANDED_STORAGE_SIZE);
        storage.setExpanded(true);
        
        return new StorageResult(true, "仓库扩展成功，容量增加到" + EXPANDED_STORAGE_SIZE + "格");
    }
    
    /**
     * 设置仓库密码
     */
    @Transactional
    public StorageResult setStoragePassword(Player player, String password) {
        Storage storage = getPersonalStorage(player.getUsername());
        
        if (password == null || password.length() < 4 || password.length() > 10) {
            return new StorageResult(false, "密码长度必须在4-10位之间");
        }
        
        storage.setPassword(password);
        return new StorageResult(true, "仓库密码设置成功");
    }
    
    /**
     * 验证仓库密码
     */
    public boolean verifyStoragePassword(String playerName, String password) {
        Storage storage = getPersonalStorage(playerName);
        String storedPassword = storage.getPassword();
        
        if (storedPassword == null || storedPassword.isEmpty()) {
            return true; // 没有设置密码
        }
        
        return storedPassword.equals(password);
    }
    
    /**
     * 获取仓库物品列表
     */
    public List<Item> getStorageItems(String playerName) {
        Storage storage = getPersonalStorage(playerName);
        return new ArrayList<>(storage.getItems());
    }
    
    /**
     * 获取行会仓库物品列表
     */
    public List<Item> getGuildStorageItems(String guildName) {
        Storage storage = getGuildStorage(guildName);
        return new ArrayList<>(storage.getItems());
    }
    
    /**
     * 整理仓库
     */
    @Transactional
    public StorageResult organizeStorage(String playerName) {
        Storage storage = getPersonalStorage(playerName);
        
        // 合并相同物品
        mergeItems(storage);
        
        // 按类型排序
        sortItems(storage);
        
        return new StorageResult(true, "仓库整理完成");
    }
    
    /**
     * 检查是否有足够空间
     */
    private boolean hasSpace(Storage storage, Item item, int quantity) {
        int usedSlots = storage.getItems().size();
        int maxSlots = storage.getMaxSize();
        
        // 检查是否有相同物品可以堆叠
        Item existingItem = findStackableItem(storage, item);
        if (existingItem != null) {
            return true; // 可以堆叠
        }
        
        return usedSlots < maxSlots;
    }
    
    /**
     * 检查物品是否可以存储
     */
    private boolean canStoreItem(Item item) {
        // 检查物品属性
        if (item.isNoStore()) {
            return false; // 无法存储的物品
        }
        
        if (item.isTemporary()) {
            return false; // 临时物品不能存储
        }
        
        return true;
    }
    
    /**
     * 添加物品到仓库
     */
    private boolean addItemToStorage(Storage storage, Item item, int quantity) {
        // 尝试找到可堆叠的物品
        Item existingItem = findStackableItem(storage, item);
        
        if (existingItem != null) {
            // 堆叠到现有物品
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            return true;
        } else {
            // 创建新的物品槽
            Item newItem = item.clone();
            newItem.setQuantity(quantity);
            storage.getItems().add(newItem);
            return true;
        }
    }
    
    /**
     * 从仓库移除物品
     */
    private boolean removeItemFromStorage(Storage storage, Item item, int quantity) {
        if (item.getQuantity() > quantity) {
            // 部分移除
            item.setQuantity(item.getQuantity() - quantity);
            return true;
        } else if (item.getQuantity() == quantity) {
            // 完全移除
            storage.getItems().remove(item);
            return true;
        }
        
        return false; // 数量不足
    }
    
    /**
     * 查找仓库中的物品
     */
    private Item findItemInStorage(Storage storage, String itemId) {
        return storage.getItems().stream()
                .filter(item -> item.getId().toString().equals(itemId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 查找可堆叠的物品
     */
    private Item findStackableItem(Storage storage, Item targetItem) {
        return storage.getItems().stream()
                .filter(item -> item.canStackWith(targetItem))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 检查是否可以访问行会仓库
     */
    private boolean canAccessGuildStorage(Player player, Guild guild) {
        // 检查是否为行会成员
        if (!guild.getName().equals(player.getGuildName())) {
            return false;
        }
        
        // 检查权限等级（可能需要特定权限才能访问行会仓库）
        return player.getGuildRank() <= 3; // 假设权限等级3以下可以访问
    }
    
    /**
     * 检查背包空间
     */
    private boolean hasInventorySpace(Player player, Item item, int quantity) {
        return inventoryService.hasInventorySpace(player.getName(), item, quantity);
    }
    
    /**
     * 从玩家背包移除物品
     */
    private void removeItemFromInventory(Player player, Item item, int quantity) {
        RemoveItemResult result = inventoryService.removeItemById(player.getName(), item.getItemId(), quantity);
        if (!result.isSuccess()) {
            throw new RuntimeException("移除背包物品失败: " + result.getMessage());
        }
    }
    
    /**
     * 添加物品到玩家背包
     */
    private void addItemToInventory(Player player, Item item, int quantity) {
        AddItemResult result = inventoryService.addItemToInventory(player.getName(), item, quantity);
        if (!result.isSuccess()) {
            throw new RuntimeException("添加背包物品失败: " + result.getMessage());
        }
    }
    
    /**
     * 记录仓库操作日志
     */
    private void recordStorageOperation(String playerName, Item item, int quantity, String operation) {
        // 记录操作日志到数据库或日志文件
        String logEntry = String.format("[%s] %s %s %s x%d", 
                System.currentTimeMillis(), playerName, operation, item.getName(), quantity);
        
        // 这里可以写入到数据库或日志文件
        System.out.println(logEntry);
    }
    
    /**
     * 合并相同物品
     */
    private void mergeItems(Storage storage) {
        Map<String, Item> itemMap = new ConcurrentHashMap<>();
        List<Item> newItems = new ArrayList<>();
        
        for (Item item : storage.getItems()) {
            String key = item.getName() + "_" + item.getType();
            
            if (itemMap.containsKey(key)) {
                Item existing = itemMap.get(key);
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
            } else {
                itemMap.put(key, item);
                newItems.add(item);
            }
        }
        
        storage.setItems(newItems);
    }
    
    /**
     * 物品排序
     */
    private void sortItems(Storage storage) {
        List<Item> sortedItems = storage.getItems().stream()
                .sorted((a, b) -> {
                    // 按类型排序，然后按名称排序
                    int typeCompare = a.getType().compareTo(b.getType());
                    if (typeCompare != 0) {
                        return typeCompare;
                    }
                    return a.getName().compareTo(b.getName());
                })
                .collect(Collectors.toList());
        
        storage.setItems(sortedItems);
    }
    
    /**
     * 仓库操作结果类
     */
    public static class StorageResult {
        private final boolean success;
        private final String message;
        
        public StorageResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
} 