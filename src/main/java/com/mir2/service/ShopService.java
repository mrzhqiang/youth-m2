package com.mir2.service;

import com.mir2.core.model.Item;
import com.mir2.core.model.Player;
import com.mir2.core.enums.ItemType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 商店系统服务
 * 
 * <p>负责处理NPC商店、买卖物品、修理装备等功能。
 * 对应原M2Engine中的Shop模块。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class ShopService {
    
    /** 商店数据 */
    private final Map<String, Shop> shops = new ConcurrentHashMap<>();
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private PlayerService playerService;
    
    /**
     * 初始化商店系统
     */
    public void initializeShops() {
        log.info("开始初始化商店系统...");
        
        // 创建基础商店
        Shop weaponShop = new Shop("武器店", "NPC_武器商人");
        weaponShop.addItem(new ShopItem(1001, 500, 10)); // 木剑
        weaponShop.addItem(new ShopItem(1002, 1000, 8)); // 短剑
        weaponShop.addItem(new ShopItem(1003, 2000, 6)); // 长剑
        shops.put(weaponShop.getId(), weaponShop);
        
        Shop armorShop = new Shop("防具店", "NPC_防具商人");
        armorShop.addItem(new ShopItem(2001, 800, 8)); // 布衣
        armorShop.addItem(new ShopItem(2002, 1500, 6)); // 皮甲
        armorShop.addItem(new ShopItem(2003, 3000, 5)); // 铁甲
        shops.put(armorShop.getId(), armorShop);
        
        Shop potionShop = new Shop("药品店", "NPC_药剂师");
        potionShop.addItem(new ShopItem(4001, 50, 50)); // 金疮药
        potionShop.addItem(new ShopItem(4002, 100, 30)); // 强效金疮药
        potionShop.addItem(new ShopItem(4003, 200, 20)); // 超级金疮药
        shops.put(potionShop.getId(), potionShop);
        
        log.info("商店系统初始化完成，共创建 {} 个商店", shops.size());
    }
    
    /**
     * 购买物品
     * 
     * @param player 玩家
     * @param shopId 商店ID
     * @param itemId 物品ID
     * @param quantity 数量
     * @return 购买结果
     */
    public ShopResult buyItem(Player player, String shopId, int itemId, int quantity) {
        if (player == null) {
            return new ShopResult(false, "玩家不存在");
        }
        
        Shop shop = shops.get(shopId);
        if (shop == null) {
            return new ShopResult(false, "商店不存在");
        }
        
        ShopItem shopItem = shop.getItem(itemId);
        if (shopItem == null) {
            return new ShopResult(false, "商品不存在");
        }
        
        // 检查库存
        if (shopItem.getStock() < quantity) {
            return new ShopResult(false, "库存不足");
        }
        
        // 计算总价
        int totalPrice = shopItem.getPrice() * quantity;
        
        // 检查玩家金币
        if (player.getGold() < totalPrice) {
            return new ShopResult(false, "金币不足");
        }
        
        // 扣除金币
        player.setGold(player.getGold() - totalPrice);
        
        // 添加物品
        player.addItem(itemId, quantity);
        
        // 扣除库存
        shopItem.setStock(shopItem.getStock() - quantity);
        
        log.info("玩家 {} 在 {} 购买了 {} 个 {}，花费 {} 金币", 
                player.getName(), shop.getName(), quantity, itemId, totalPrice);
        
        return new ShopResult(true, "购买成功", totalPrice);
    }
    
    /**
     * 出售物品
     * 
     * @param player 玩家
     * @param shopId 商店ID
     * @param itemId 物品ID
     * @param quantity 数量
     * @return 出售结果
     */
    public ShopResult sellItem(Player player, String shopId, int itemId, int quantity) {
        if (player == null) {
            return new ShopResult(false, "玩家不存在");
        }
        
        Shop shop = shops.get(shopId);
        if (shop == null) {
            return new ShopResult(false, "商店不存在");
        }
        
        // 检查玩家是否有该物品
        Item item = player.getInventory().get(itemId);
        if (item == null || item.getQuantity() < quantity) {
            return new ShopResult(false, "物品数量不足");
        }
        
        // 计算出售价格
        int sellPrice = calculateSellPrice(itemId, quantity);
        
        // 移除物品
        player.removeItem(itemId, quantity);
        
        // 添加金币
        player.setGold(player.getGold() + sellPrice);
        
        log.info("玩家 {} 在 {} 出售了 {} 个 {}，获得 {} 金币", 
                player.getName(), shop.getName(), quantity, itemId, sellPrice);
        
        return new ShopResult(true, "出售成功", sellPrice);
    }
    
    /**
     * 修理装备
     * 
     * @param player 玩家
     * @param shopId 商店ID
     * @param itemId 物品ID
     * @return 修理结果
     */
    public ShopResult repairItem(Player player, String shopId, int itemId) {
        if (player == null) {
            return new ShopResult(false, "玩家不存在");
        }
        
        Shop shop = shops.get(shopId);
        if (shop == null) {
            return new ShopResult(false, "商店不存在");
        }
        
        // 检查玩家是否有该物品
        Item item = player.getInventory().get(itemId);
        if (item == null) {
            return new ShopResult(false, "物品不存在");
        }
        
        // 检查是否需要修理
        if (item.getDurability() >= item.getMaxDurability()) {
            return new ShopResult(false, "物品不需要修理");
        }
        
        // 计算修理费用
        int repairCost = calculateRepairCost(item);
        
        // 检查玩家金币
        if (player.getGold() < repairCost) {
            return new ShopResult(false, "金币不足");
        }
        
        // 扣除金币
        player.setGold(player.getGold() - repairCost);
        
        // 修理物品
        item.setDurability(item.getMaxDurability());
        
        log.info("玩家 {} 在 {} 修理了 {}，花费 {} 金币", 
                player.getName(), shop.getName(), itemId, repairCost);
        
        return new ShopResult(true, "修理成功", repairCost);
    }
    
    /**
     * 计算出售价格
     */
    private int calculateSellPrice(int itemId, int quantity) {
        // 获取物品模板
        Item itemTemplate = itemService.getItemTemplate(itemId);
        if (itemTemplate == null) {
            return 0;
        }
        
        // 出售价格为购买价格的50%
        int basePrice = itemTemplate.getPrice() / 2;
        
        return basePrice * quantity;
    }
    
    /**
     * 计算修理费用
     */
    private int calculateRepairCost(Item item) {
        // 获取物品模板
        Item itemTemplate = itemService.getItemTemplate(item.getItemId());
        if (itemTemplate == null) {
            return 0;
        }
        
        // 修理费用基于物品价格和损坏程度
        int basePrice = itemTemplate.getPrice();
        double damagePct = (double) (item.getMaxDurability() - item.getDurability()) / item.getMaxDurability();
        
        return (int) (basePrice * damagePct * 0.3); // 修理费用为物品价格的30%
    }
    
    /**
     * 获取商店
     * 
     * @param shopId 商店ID
     * @return 商店
     */
    public Shop getShop(String shopId) {
        return shops.get(shopId);
    }
    
    /**
     * 商店类
     */
    @Data
    public static class Shop {
        private String id;
        private String name;
        private String npcId;
        private List<ShopItem> items;
        
        public Shop(String name, String npcId) {
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.npcId = npcId;
            this.items = new ArrayList<>();
        }
        
        public void addItem(ShopItem item) {
            items.add(item);
        }
        
        public ShopItem getItem(int itemId) {
            return items.stream()
                    .filter(item -> item.getItemId() == itemId)
                    .findFirst()
                    .orElse(null);
        }
    }
    
    /**
     * 商店物品类
     */
    @Data
    public static class ShopItem {
        private int itemId;
        private int price;
        private int stock;
        private int maxStock;
        
        public ShopItem(int itemId, int price, int stock) {
            this.itemId = itemId;
            this.price = price;
            this.stock = stock;
            this.maxStock = stock;
        }
    }
    
    /**
     * 商店结果类
     */
    @Data
    public static class ShopResult {
        private boolean success;
        private String message;
        private int amount;
        
        public ShopResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.amount = 0;
        }
        
        public ShopResult(boolean success, String message, int amount) {
            this.success = success;
            this.message = message;
            this.amount = amount;
        }
    }
} 