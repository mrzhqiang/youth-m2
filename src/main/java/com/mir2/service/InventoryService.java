package com.mir2.service;

import com.mir2.core.model.Item;
import com.mir2.core.model.Player;
import com.mir2.entity.Inventory;
import com.mir2.entity.Inventory.AddItemResult;
import com.mir2.entity.Inventory.RemoveItemResult;
import com.mir2.entity.Inventory.InventorySlot;
import com.mir2.entity.Inventory.InventorySummary;
import com.mir2.entity.InventoryEntity;
import com.mir2.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 背包服务类
 * 
 * @author Mir2 Team
 */
@Service
@Slf4j
public class InventoryService {
    
    /** 玩家背包缓存 */
    private final Map<String, Inventory> playerInventories = new ConcurrentHashMap<>();
    
    /** 背包扩展价格配置 */
    private final Map<Integer, Integer> expansionPrices = new HashMap<>() {{
        put(40, 50000);    // 40格扩展到50格需要5万金币
        put(50, 100000);   // 50格扩展到60格需要10万金币
        put(60, 200000);   // 60格扩展到70格需要20万金币
        put(70, 400000);   // 70格扩展到80格需要40万金币
        put(80, 800000);   // 80格扩展到90格需要80万金币
        put(90, 1600000);  // 90格扩展到100格需要160万金币
    }};
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    /**
     * 获取玩家背包
     * 
     * @param playerName 玩家名称
     * @return 背包对象
     */
    public Inventory getPlayerInventory(String playerName) {
        return playerInventories.computeIfAbsent(playerName, name -> {
            Inventory inventory = loadInventoryFromDatabase(name);
            if (inventory == null) {
                inventory = new Inventory(name, 40); // 默认40格背包
                saveInventoryToDatabase(inventory);
            }
            return inventory;
        });
    }
    
    /**
     * 添加物品到背包
     * 
     * @param playerName 玩家名称
     * @param item 物品
     * @param quantity 数量
     * @return 添加结果
     */
    @Transactional
    public AddItemResult addItemToInventory(String playerName, Item item, int quantity) {
        try {
            Inventory inventory = getPlayerInventory(playerName);
            AddItemResult result = inventory.addItem(item, quantity);
            
            if (result.isSuccess()) {
                saveInventoryToDatabase(inventory);
                log.debug("玩家 {} 添加物品 {} x{} 成功", playerName, item.getName(), quantity);
            } else {
                log.warn("玩家 {} 添加物品 {} x{} 失败: {}", playerName, item.getName(), quantity, result.getMessage());
            }
            
            return result;
        } catch (Exception e) {
            log.error("添加物品到背包失败", e);
            return new AddItemResult(false, 0, "系统错误");
        }
    }
    
    /**
     * 从背包移除物品
     * 
     * @param playerName 玩家名称
     * @param slotIndex 槽位索引
     * @param quantity 数量
     * @return 移除结果
     */
    @Transactional
    public RemoveItemResult removeItemFromInventory(String playerName, int slotIndex, int quantity) {
        try {
            Inventory inventory = getPlayerInventory(playerName);
            RemoveItemResult result = inventory.removeItem(slotIndex, quantity);
            
            if (result.isSuccess()) {
                saveInventoryToDatabase(inventory);
                log.debug("玩家 {} 从槽位 {} 移除物品 {} x{} 成功", 
                        playerName, slotIndex, result.getRemovedItem().getName(), quantity);
            } else {
                log.warn("玩家 {} 从槽位 {} 移除物品失败: {}", playerName, slotIndex, result.getMessage());
            }
            
            return result;
        } catch (Exception e) {
            log.error("从背包移除物品失败", e);
            return new RemoveItemResult(false, null, 0, "系统错误");
        }
    }
    
    /**
     * 根据物品ID移除物品
     * 
     * @param playerName 玩家名称
     * @param itemId 物品ID
     * @param quantity 数量
     * @return 移除结果
     */
    @Transactional
    public RemoveItemResult removeItemById(String playerName, int itemId, int quantity) {
        try {
            Inventory inventory = getPlayerInventory(playerName);
            RemoveItemResult result = inventory.removeItemById(itemId, quantity);
            
            if (result.isSuccess()) {
                saveInventoryToDatabase(inventory);
                log.debug("玩家 {} 移除物品ID {} x{} 成功", playerName, itemId, quantity);
            } else {
                log.warn("玩家 {} 移除物品ID {} x{} 失败: {}", playerName, itemId, quantity, result.getMessage());
            }
            
            return result;
        } catch (Exception e) {
            log.error("从背包移除物品失败", e);
            return new RemoveItemResult(false, null, 0, "系统错误");
        }
    }
    
    /**
     * 检查背包是否有足够空间
     * 
     * @param playerName 玩家名称
     * @param item 物品
     * @param quantity 数量
     * @return 是否有足够空间
     */
    public boolean hasInventorySpace(String playerName, Item item, int quantity) {
        try {
            Inventory inventory = getPlayerInventory(playerName);
            return inventory.hasSpace(item, quantity);
        } catch (Exception e) {
            log.error("检查背包空间失败", e);
            return false;
        }
    }
    
    /**
     * 获取背包中物品数量
     * 
     * @param playerName 玩家名称
     * @param itemId 物品ID
     * @return 物品数量
     */
    public int getItemCount(String playerName, int itemId) {
        try {
            Inventory inventory = getPlayerInventory(playerName);
            return inventory.getItemCount(itemId);
        } catch (Exception e) {
            log.error("获取物品数量失败", e);
            return 0;
        }
    }
    
    /**
     * 整理背包
     * 
     * @param playerName 玩家名称
     * @return 是否成功
     */
    @Transactional
    public boolean organizeInventory(String playerName) {
        try {
            Inventory inventory = getPlayerInventory(playerName);
            inventory.organizeInventory();
            saveInventoryToDatabase(inventory);
            log.debug("玩家 {} 整理背包成功", playerName);
            return true;
        } catch (Exception e) {
            log.error("整理背包失败", e);
            return false;
        }
    }
    
    /**
     * 扩展背包容量
     * 
     * @param playerName 玩家名称
     * @param player 玩家对象
     * @return 是否成功
     */
    @Transactional
    public boolean expandInventory(String playerName, Player player) {
        try {
            Inventory inventory = getPlayerInventory(playerName);
            int currentCapacity = inventory.getCapacity();
            
            // 检查扩展价格
            Integer price = expansionPrices.get(currentCapacity);
            if (price == null) {
                log.warn("背包容量 {} 无法扩展", currentCapacity);
                return false;
            }
            
            // 检查金币是否足够
            if (player.getGold() < price) {
                log.warn("玩家 {} 金币不足，无法扩展背包", playerName);
                return false;
            }
            
            // 扩展背包
            if (inventory.expandCapacity(10)) {
                player.setGold(player.getGold() - price);
                saveInventoryToDatabase(inventory);
                log.info("玩家 {} 扩展背包成功，容量: {} -> {}，消耗金币: {}", 
                        playerName, currentCapacity, inventory.getCapacity(), price);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("扩展背包失败", e);
            return false;
        }
    }
    
    /**
     * 获取背包摘要信息
     * 
     * @param playerName 玩家名称
     * @return 背包摘要
     */
    public InventorySummary getInventorySummary(String playerName) {
        try {
            Inventory inventory = getPlayerInventory(playerName);
            return inventory.getSummary();
        } catch (Exception e) {
            log.error("获取背包摘要失败", e);
            return null;
        }
    }
    
    /**
     * 获取背包物品列表
     * 
     * @param playerName 玩家名称
     * @return 物品列表
     */
    public List<InventorySlot> getInventoryItems(String playerName) {
        try {
            Inventory inventory = getPlayerInventory(playerName);
            return new ArrayList<>(inventory.getSlots());
        } catch (Exception e) {
            log.error("获取背包物品列表失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 查找指定物品
     * 
     * @param playerName 玩家名称
     * @param itemId 物品ID
     * @return 物品槽位列表
     */
    public List<InventorySlot> findItems(String playerName, int itemId) {
        try {
            Inventory inventory = getPlayerInventory(playerName);
            return inventory.findItems(itemId);
        } catch (Exception e) {
            log.error("查找物品失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 锁定/解锁物品
     * 
     * @param playerName 玩家名称
     * @param slotIndex 槽位索引
     * @param locked 是否锁定
     * @return 是否成功
     */
    @Transactional
    public boolean lockItem(String playerName, int slotIndex, boolean locked) {
        try {
            Inventory inventory = getPlayerInventory(playerName);
            List<InventorySlot> slots = inventory.getSlots();
            
            if (slotIndex < 0 || slotIndex >= slots.size()) {
                log.warn("无效的槽位索引: {}", slotIndex);
                return false;
            }
            
            InventorySlot slot = slots.get(slotIndex);
            if (slot.isEmpty()) {
                log.warn("槽位 {} 为空，无法锁定", slotIndex);
                return false;
            }
            
            slot.setLocked(locked);
            saveInventoryToDatabase(inventory);
            log.debug("玩家 {} 槽位 {} 锁定状态更新为: {}", playerName, slotIndex, locked);
            return true;
        } catch (Exception e) {
            log.error("锁定/解锁物品失败", e);
            return false;
        }
    }
    
    /**
     * 获取背包重量统计
     * 
     * @param playerName 玩家名称
     * @return 重量统计信息
     */
    public WeightInfo getWeightInfo(String playerName) {
        try {
            Inventory inventory = getPlayerInventory(playerName);
            int totalWeight = 0;
            int itemCount = 0;
            
            for (InventorySlot slot : inventory.getSlots()) {
                if (!slot.isEmpty()) {
                    totalWeight += slot.getItem().getWeight() * slot.getQuantity();
                    itemCount += slot.getQuantity();
                }
            }
            
            return new WeightInfo(totalWeight, itemCount, inventory.getCapacity());
        } catch (Exception e) {
            log.error("获取背包重量统计失败", e);
            return new WeightInfo(0, 0, 40);
        }
    }
    
    /**
     * 清空背包
     * 
     * @param playerName 玩家名称
     * @return 是否成功
     */
    @Transactional
    public boolean clearInventory(String playerName) {
        try {
            Inventory inventory = getPlayerInventory(playerName);
            for (InventorySlot slot : inventory.getSlots()) {
                slot.setItem(null);
                slot.setQuantity(0);
                slot.setLocked(false);
            }
            saveInventoryToDatabase(inventory);
            log.info("清空玩家 {} 的背包", playerName);
            return true;
        } catch (Exception e) {
            log.error("清空背包失败", e);
            return false;
        }
    }
    
    /**
     * 保存背包到数据库
     * 
     * @param inventory 背包对象
     */
    private void saveInventoryToDatabase(Inventory inventory) {
        try {
            // 转换为Entity并保存到数据库
            InventoryEntity entity = InventoryEntity.fromInventory(inventory);
            
            // 检查是否已存在
            Optional<InventoryEntity> existingEntity = inventoryRepository.findByPlayerNameAndDeletedFalse(inventory.getPlayerName());
            if (existingEntity.isPresent()) {
                // 更新现有实体
                InventoryEntity existing = existingEntity.get();
                existing.setCapacity(entity.getCapacity());
                existing.setSlotsData(entity.getSlotsData());
                existing.setExpansionLevel(entity.getExpansionLevel());
                existing.setLocked(entity.getLocked());
                existing.setLastCleanupTime(entity.getLastCleanupTime());
                
                inventoryRepository.save(existing);
                log.debug("更新玩家 {} 的背包数据，容量: {}", inventory.getPlayerName(), inventory.getCapacity());
            } else {
                // 新增实体
                inventoryRepository.save(entity);
                log.debug("新增玩家 {} 的背包数据，容量: {}", inventory.getPlayerName(), inventory.getCapacity());
            }
            
        } catch (Exception e) {
            log.error("保存背包数据失败: player={}", inventory.getPlayerName(), e);
        }
    }
    
    /**
     * 从数据库加载背包
     * 
     * @param playerName 玩家名称
     * @return 背包对象
     */
    private Inventory loadInventoryFromDatabase(String playerName) {
        try {
            // 从数据库查询背包数据
            Optional<InventoryEntity> entityOpt = inventoryRepository.findByPlayerNameAndDeletedFalse(playerName);
            
            if (entityOpt.isPresent()) {
                // 转换为Inventory对象
                InventoryEntity entity = entityOpt.get();
                Inventory inventory = entity.toInventory();
                
                log.debug("从数据库加载玩家 {} 的背包数据成功，容量: {}", playerName, inventory.getCapacity());
                return inventory;
            } else {
                // 创建新的背包实例
                log.debug("玩家 {} 的背包数据不存在，创建新背包", playerName);
                return new Inventory(playerName);
            }
            
        } catch (Exception e) {
            log.error("加载背包数据失败: player={}", playerName, e);
            return new Inventory(playerName);
        }
    }
    
    /**
     * 删除玩家背包数据
     * 
     * @param playerName 玩家名称
     * @return 是否成功
     */
    @Transactional
    public boolean deletePlayerInventory(String playerName) {
        try {
            // 从缓存中移除
            playerInventories.remove(playerName);
            
            // 从数据库中逻辑删除
            inventoryRepository.deleteByPlayerName(playerName);
            
            log.info("删除玩家 {} 的背包数据", playerName);
            return true;
            
        } catch (Exception e) {
            log.error("删除背包数据失败: player={}", playerName, e);
            return false;
        }
    }
    
    /**
     * 批量保存背包数据
     * 
     * @param inventories 背包列表
     */
    @Transactional
    public void batchSaveInventories(List<Inventory> inventories) {
        try {
            List<InventoryEntity> entities = new ArrayList<>();
            
            for (Inventory inventory : inventories) {
                InventoryEntity entity = InventoryEntity.fromInventory(inventory);
                entities.add(entity);
            }
            
            if (!entities.isEmpty()) {
                inventoryRepository.saveAll(entities);
                log.info("批量保存背包数据成功，数量: {}", entities.size());
            }
            
        } catch (Exception e) {
            log.error("批量保存背包数据失败", e);
        }
    }
    
    /**
     * 获取背包扩展价格
     * 
     * @param currentCapacity 当前容量
     * @return 扩展价格，如果无法扩展返回-1
     */
    public int getExpansionPrice(int currentCapacity) {
        return expansionPrices.getOrDefault(currentCapacity, -1);
    }
    
    /**
     * 检查背包是否已满
     * 
     * @param playerName 玩家名称
     * @return 是否已满
     */
    public boolean isInventoryFull(String playerName) {
        try {
            Inventory inventory = getPlayerInventory(playerName);
            return inventory.isFull();
        } catch (Exception e) {
            log.error("检查背包是否已满失败", e);
            return true; // 出错时返回true，避免继续操作
        }
    }
    
    /**
     * 获取背包空闲槽位数量
     * 
     * @param playerName 玩家名称
     * @return 空闲槽位数量
     */
    public int getEmptySlotCount(String playerName) {
        try {
            Inventory inventory = getPlayerInventory(playerName);
            return inventory.getEmptySlotCount();
        } catch (Exception e) {
            log.error("获取背包空闲槽位数量失败", e);
            return 0;
        }
    }
    
    /**
     * 检查背包是否有足够空间
     * 
     * @param playerName 玩家名称
     * @param requiredSlots 需要的槽位数量
     * @return 是否有足够空间
     */
    public boolean hasSpace(String playerName, int requiredSlots) {
        try {
            return getEmptySlotCount(playerName) >= requiredSlots;
        } catch (Exception e) {
            log.error("检查背包空间失败", e);
            return false;
        }
    }
    
    /**
     * 重量信息
     */
    public static class WeightInfo {
        public final int totalWeight;
        public final int itemCount;
        public final int capacity;
        
        public WeightInfo(int totalWeight, int itemCount, int capacity) {
            this.totalWeight = totalWeight;
            this.itemCount = itemCount;
            this.capacity = capacity;
        }
    }
} 