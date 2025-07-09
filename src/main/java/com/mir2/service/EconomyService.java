package com.mir2.service;

import com.mir2.core.model.Player;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 经济系统服务
 * 
 * <p>负责管理游戏内的经济系统，包括物价调节、通胀控制、财富统计等。
 * 对应原M2Engine中的Economy模块。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class EconomyService {
    
    /** 经济数据 */
    private final Map<String, EconomyData> economyData = new ConcurrentHashMap<>();
    
    /** 物价数据 */
    private final Map<Integer, PriceData> priceData = new ConcurrentHashMap<>();
    
    /** 财富统计 */
    private final Map<String, WealthStatistics> wealthStats = new ConcurrentHashMap<>();
    
    /** 基础通胀率 */
    private double baseInflationRate = 0.001; // 0.1%
    
    /** 最大通胀率 */
    private double maxInflationRate = 0.01; // 1%
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private ItemService itemService;
    
    /**
     * 初始化经济系统
     */
    public void initializeEconomy() {
        log.info("开始初始化经济系统...");
        
        // 初始化全局经济数据
        EconomyData globalData = new EconomyData();
        globalData.setRegionName("全球");
        globalData.setInflationRate(baseInflationRate);
        globalData.setTotalGold(1000000); // 初始金币总量
        globalData.setLastUpdateTime(System.currentTimeMillis());
        economyData.put("global", globalData);
        
        // 初始化各地区经济数据
        initializeRegionalEconomy();
        
        // 初始化物价数据
        initializePriceData();
        
        log.info("经济系统初始化完成");
    }
    
    /**
     * 初始化地区经济数据
     */
    private void initializeRegionalEconomy() {
        String[] regions = {"比奇城", "盟重城", "土城", "银杏村", "苍月岛"};
        
        for (String region : regions) {
            EconomyData regionData = new EconomyData();
            regionData.setRegionName(region);
            regionData.setInflationRate(baseInflationRate);
            regionData.setTotalGold(100000); // 地区初始金币量
            regionData.setLastUpdateTime(System.currentTimeMillis());
            economyData.put(region, regionData);
        }
    }
    
    /**
     * 初始化物价数据
     */
    private void initializePriceData() {
        // 基础物品价格
        addPriceData(1, 50, 0.8, 1.5); // 金疮药
        addPriceData(2, 100, 0.7, 1.8); // 强效金疮药
        addPriceData(3, 200, 0.6, 2.0); // 超级金疮药
        
        // 装备价格
        addPriceData(1001, 500, 0.5, 3.0); // 木剑
        addPriceData(1002, 1000, 0.4, 3.5); // 短剑
        addPriceData(1003, 2000, 0.3, 4.0); // 长剑
        
        // 材料价格
        addPriceData(5001, 100, 0.9, 1.2); // 铁矿
        addPriceData(5002, 200, 0.8, 1.5); // 银矿
        addPriceData(5003, 500, 0.7, 2.0); // 黑铁矿
    }
    
    /**
     * 添加物价数据
     */
    private void addPriceData(int itemId, int basePrice, double minRate, double maxRate) {
        PriceData priceData = new PriceData();
        priceData.setItemId(itemId);
        priceData.setBasePrice(basePrice);
        priceData.setCurrentPrice(basePrice);
        priceData.setMinPriceRate(minRate);
        priceData.setMaxPriceRate(maxRate);
        priceData.setLastUpdateTime(System.currentTimeMillis());
        this.priceData.put(itemId, priceData);
    }
    
    /**
     * 记录交易
     * 
     * @param player 玩家
     * @param itemId 物品ID
     * @param quantity 数量
     * @param price 价格
     * @param isBuy 是否购买
     */
    public void recordTransaction(Player player, int itemId, int quantity, int price, boolean isBuy) {
        // 更新物价数据
        updatePriceData(itemId, price, quantity, isBuy);
        
        // 更新财富统计
        updateWealthStatistics(player, price, isBuy);
        
        // 更新经济数据
        updateEconomyData(player.getMapName(), price, isBuy);
        
        log.debug("记录交易: {} {} {} 个 {} 价格 {}", 
                player.getName(), isBuy ? "购买" : "出售", quantity, itemId, price);
    }
    
    /**
     * 更新物价数据
     */
    private void updatePriceData(int itemId, int transactionPrice, int quantity, boolean isBuy) {
        PriceData priceData = this.priceData.get(itemId);
        if (priceData == null) {
            return;
        }
        
        // 简单的供需调价模型
        double priceChange = 0;
        if (isBuy) {
            // 购买增加需求，价格上涨
            priceChange = quantity * 0.01;
        } else {
            // 出售增加供给，价格下跌
            priceChange = -quantity * 0.01;
        }
        
        // 计算新价格
        double newPrice = priceData.getCurrentPrice() * (1 + priceChange);
        
        // 限制价格范围
        int minPrice = (int) (priceData.getBasePrice() * priceData.getMinPriceRate());
        int maxPrice = (int) (priceData.getBasePrice() * priceData.getMaxPriceRate());
        
        newPrice = Math.max(minPrice, Math.min(maxPrice, newPrice));
        
        priceData.setCurrentPrice((int) newPrice);
        priceData.setLastUpdateTime(System.currentTimeMillis());
        
        // 记录价格历史
        priceData.addPriceHistory((int) newPrice);
    }
    
    /**
     * 更新财富统计
     */
    private void updateWealthStatistics(Player player, int amount, boolean isSpend) {
        WealthStatistics stats = wealthStats.computeIfAbsent(player.getName(), 
                k -> new WealthStatistics(player.getName()));
        
        if (isSpend) {
            stats.setTotalSpent(stats.getTotalSpent() + amount);
        } else {
            stats.setTotalEarned(stats.getTotalEarned() + amount);
        }
        
        stats.setLastTransactionTime(System.currentTimeMillis());
        stats.setCurrentGold(player.getGold());
    }
    
    /**
     * 更新经济数据
     */
    private void updateEconomyData(String region, int amount, boolean isSpend) {
        EconomyData data = economyData.get(region);
        if (data == null) {
            data = economyData.get("global");
        }
        
        if (isSpend) {
            data.setTotalSpent(data.getTotalSpent() + amount);
        } else {
            data.setTotalEarned(data.getTotalEarned() + amount);
        }
        
        // 更新金币流通量
        data.setTotalGold(data.getTotalGold() + (isSpend ? -amount : amount));
        data.setLastUpdateTime(System.currentTimeMillis());
    }
    
    /**
     * 获取物品当前价格
     * 
     * @param itemId 物品ID
     * @return 当前价格
     */
    public int getCurrentPrice(int itemId) {
        PriceData priceData = this.priceData.get(itemId);
        if (priceData == null) {
            // 如果没有价格数据，返回基础价格
            return 100;
        }
        
        return priceData.getCurrentPrice();
    }
    
    /**
     * 获取物品价格历史
     * 
     * @param itemId 物品ID
     * @return 价格历史
     */
    public List<Integer> getPriceHistory(int itemId) {
        PriceData priceData = this.priceData.get(itemId);
        if (priceData == null) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(priceData.getPriceHistory());
    }
    
    /**
     * 获取玩家财富统计
     * 
     * @param playerName 玩家名称
     * @return 财富统计
     */
    public WealthStatistics getWealthStatistics(String playerName) {
        return wealthStats.get(playerName);
    }
    
    /**
     * 获取经济数据
     * 
     * @param region 地区名称
     * @return 经济数据
     */
    public EconomyData getEconomyData(String region) {
        return economyData.get(region);
    }
    
    /**
     * 获取通胀率
     * 
     * @param region 地区名称
     * @return 通胀率
     */
    public double getInflationRate(String region) {
        EconomyData data = economyData.get(region);
        if (data == null) {
            return baseInflationRate;
        }
        
        return data.getInflationRate();
    }
    
    /**
     * 计算通胀调整后的价格
     * 
     * @param basePrice 基础价格
     * @param region 地区
     * @return 调整后价格
     */
    public int calculateInflationAdjustedPrice(int basePrice, String region) {
        double inflationRate = getInflationRate(region);
        return (int) (basePrice * (1 + inflationRate));
    }
    
    /**
     * 定时更新经济数据
     */
    @Scheduled(fixedDelay = 60000) // 每分钟更新一次
    public void updateEconomyMetrics() {
        updateInflationRates();
        updatePriceVolatility();
        cleanupOldData();
    }
    
    /**
     * 更新通胀率
     */
    private void updateInflationRates() {
        for (EconomyData data : economyData.values()) {
            // 根据金币流通量调整通胀率
            double goldRatio = (double) data.getTotalGold() / 1000000; // 相对于初始金币量
            double newInflationRate = baseInflationRate * goldRatio;
            
            // 限制通胀率范围
            newInflationRate = Math.max(0, Math.min(maxInflationRate, newInflationRate));
            
            data.setInflationRate(newInflationRate);
        }
    }
    
    /**
     * 更新价格波动
     */
    private void updatePriceVolatility() {
        for (PriceData data : priceData.values()) {
            // 添加小幅随机波动
            double volatility = (Math.random() - 0.5) * 0.02; // ±1%
            int newPrice = (int) (data.getCurrentPrice() * (1 + volatility));
            
            // 限制价格范围
            int minPrice = (int) (data.getBasePrice() * data.getMinPriceRate());
            int maxPrice = (int) (data.getBasePrice() * data.getMaxPriceRate());
            
            newPrice = Math.max(minPrice, Math.min(maxPrice, newPrice));
            
            data.setCurrentPrice(newPrice);
            data.addPriceHistory(newPrice);
        }
    }
    
    /**
     * 清理旧数据
     */
    private void cleanupOldData() {
        long currentTime = System.currentTimeMillis();
        long oneWeekAgo = currentTime - 7 * 24 * 60 * 60 * 1000L; // 一周前
        
        // 清理价格历史
        for (PriceData data : priceData.values()) {
            data.cleanupOldHistory(oneWeekAgo);
        }
        
        // 清理不活跃玩家的财富统计
        wealthStats.entrySet().removeIf(entry -> 
                entry.getValue().getLastTransactionTime() < oneWeekAgo);
    }
    
    /**
     * 获取经济报告
     * 
     * @return 经济报告
     */
    public Map<String, Object> getEconomyReport() {
        Map<String, Object> report = new HashMap<>();
        
        // 全球经济数据
        EconomyData globalData = economyData.get("global");
        report.put("全球数据", globalData);
        
        // 地区经济数据
        Map<String, EconomyData> regionalData = new HashMap<>();
        for (Map.Entry<String, EconomyData> entry : economyData.entrySet()) {
            if (!"global".equals(entry.getKey())) {
                regionalData.put(entry.getKey(), entry.getValue());
            }
        }
        report.put("地区数据", regionalData);
        
        // 物价数据
        report.put("物价数据", priceData);
        
        // 财富统计
        report.put("财富统计", wealthStats);
        
        return report;
    }
    
    /**
     * 经济数据类
     */
    @Data
    public static class EconomyData {
        private String regionName;
        private double inflationRate;
        private long totalGold;
        private long totalSpent;
        private long totalEarned;
        private long lastUpdateTime;
        
        public EconomyData() {
            this.totalSpent = 0;
            this.totalEarned = 0;
        }
    }
    
    /**
     * 物价数据类
     */
    @Data
    public static class PriceData {
        private int itemId;
        private int basePrice;
        private int currentPrice;
        private double minPriceRate;
        private double maxPriceRate;
        private long lastUpdateTime;
        private List<Integer> priceHistory;
        
        public PriceData() {
            this.priceHistory = new ArrayList<>();
        }
        
        public void addPriceHistory(int price) {
            priceHistory.add(price);
            // 限制历史记录数量
            if (priceHistory.size() > 100) {
                priceHistory.remove(0);
            }
        }
        
        public void cleanupOldHistory(long cutoffTime) {
            // 简化实现，保留最近100条记录
            if (priceHistory.size() > 100) {
                priceHistory = priceHistory.subList(priceHistory.size() - 100, priceHistory.size());
            }
        }
    }
    
    /**
     * 财富统计类
     */
    @Data
    public static class WealthStatistics {
        private String playerName;
        private long totalSpent;
        private long totalEarned;
        private int currentGold;
        private long lastTransactionTime;
        
        public WealthStatistics(String playerName) {
            this.playerName = playerName;
            this.totalSpent = 0;
            this.totalEarned = 0;
            this.currentGold = 0;
            this.lastTransactionTime = System.currentTimeMillis();
        }
        
        public long getNetWorth() {
            return totalEarned - totalSpent + currentGold;
        }
    }
} 