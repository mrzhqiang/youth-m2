package com.mir2.service;

import com.mir2.core.model.Guild;
import com.mir2.core.model.Player;
import com.mir2.entity.RankingEntry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.mir2.entity.Item;
import com.mir2.entity.Inventory;
import com.mir2.service.InventoryService;
import com.mir2.service.GuildService;
import com.mir2.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RankingService {
    
    private static final Logger log = LoggerFactory.getLogger(RankingService.class);
    
    private final Map<String, List<RankingEntry>> rankingCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastUpdateTime = new ConcurrentHashMap<>();
    
    private final InventoryService inventoryService;
    private final GuildService guildService;
    private final PlayerService playerService;
    
    // 排行榜类型常量
    public static final String LEVEL_RANKING = "LEVEL";
    public static final String POWER_RANKING = "POWER";
    public static final String WEALTH_RANKING = "WEALTH";
    public static final String PK_RANKING = "PK";
    public static final String GUILD_RANKING = "GUILD";
    public static final String REPUTATION_RANKING = "REPUTATION";
    public static final String ACHIEVEMENT_RANKING = "ACHIEVEMENT";
    public static final String EQUIPMENT_RANKING = "EQUIPMENT";
    
    // 配置参数
    private static final int RANKING_SIZE = 100;        // 排行榜显示数量
    private static final long UPDATE_INTERVAL = 600000; // 更新间隔10分钟
    
    public RankingService(InventoryService inventoryService, GuildService guildService, PlayerService playerService) {
        this.inventoryService = inventoryService;
        this.guildService = guildService;
        this.playerService = playerService;
    }
    
    /**
     * 获取等级排行榜
     */
    public List<RankingEntry> getLevelRanking(int limit) {
        return getCachedRanking(LEVEL_RANKING, limit, () -> calculateLevelRanking());
    }
    
    /**
     * 获取战力排行榜
     */
    public List<RankingEntry> getPowerRanking(int limit) {
        return getCachedRanking(POWER_RANKING, limit, () -> calculatePowerRanking());
    }
    
    /**
     * 获取财富排行榜
     */
    public List<RankingEntry> getWealthRanking(int limit) {
        return getCachedRanking(WEALTH_RANKING, limit, () -> calculateWealthRanking());
    }
    
    /**
     * 获取PK排行榜
     */
    public List<RankingEntry> getPKRanking(int limit) {
        return getCachedRanking(PK_RANKING, limit, () -> calculatePKRanking());
    }
    
    /**
     * 获取行会排行榜
     */
    public List<RankingEntry> getGuildRanking(int limit) {
        return getCachedRanking(GUILD_RANKING, limit, () -> calculateGuildRanking());
    }
    
    /**
     * 获取声望排行榜
     */
    public List<RankingEntry> getReputationRanking(int limit) {
        return getCachedRanking(REPUTATION_RANKING, limit, () -> calculateReputationRanking());
    }
    
    /**
     * 获取成就排行榜
     */
    public List<RankingEntry> getAchievementRanking(int limit) {
        return getCachedRanking(ACHIEVEMENT_RANKING, limit, () -> calculateAchievementRanking());
    }
    
    /**
     * 获取装备排行榜
     */
    public List<RankingEntry> getEquipmentRanking(int limit) {
        return getCachedRanking(EQUIPMENT_RANKING, limit, () -> calculateEquipmentRanking());
    }
    
    /**
     * 获取玩家在排行榜中的位置
     */
    public RankingPosition getPlayerRankingPosition(String playerName) {
        RankingPosition position = new RankingPosition();
        
        // 获取各种排行榜中的位置
        position.setLevelRank(getPlayerRank(LEVEL_RANKING, playerName));
        position.setPowerRank(getPlayerRank(POWER_RANKING, playerName));
        position.setWealthRank(getPlayerRank(WEALTH_RANKING, playerName));
        position.setPkRank(getPlayerRank(PK_RANKING, playerName));
        position.setReputationRank(getPlayerRank(REPUTATION_RANKING, playerName));
        position.setAchievementRank(getPlayerRank(ACHIEVEMENT_RANKING, playerName));
        position.setEquipmentRank(getPlayerRank(EQUIPMENT_RANKING, playerName));
        
        return position;
    }
    
    /**
     * 强制更新排行榜
     */
    @Transactional
    public void refreshAllRankings() {
        String[] rankingTypes = {
            LEVEL_RANKING, POWER_RANKING, WEALTH_RANKING, PK_RANKING,
            GUILD_RANKING, REPUTATION_RANKING, ACHIEVEMENT_RANKING, EQUIPMENT_RANKING
        };
        
        for (String type : rankingTypes) {
            lastUpdateTime.put(type, LocalDateTime.now().minusHours(1)); // 强制更新
            getCachedRanking(type, RANKING_SIZE, getRankingCalculator(type));
        }
    }
    
    /**
     * 获取缓存的排行榜
     */
    private List<RankingEntry> getCachedRanking(String type, int limit, RankingCalculator calculator) {
        LocalDateTime lastUpdate = lastUpdateTime.get(type);
        LocalDateTime now = LocalDateTime.now();
        
        // 检查是否需要更新
        if (lastUpdate == null || 
            now.minusMinutes(UPDATE_INTERVAL / 60000).isAfter(lastUpdate)) {
            
            List<RankingEntry> ranking = calculator.calculate();
            rankingCache.put(type, ranking);
            lastUpdateTime.put(type, now);
        }
        
        List<RankingEntry> ranking = rankingCache.get(type);
        if (ranking == null) {
            return new ArrayList<>();
        }
        
        return ranking.stream()
                .limit(Math.min(limit, RANKING_SIZE))
                .collect(Collectors.toList());
    }
    
    /**
     * 计算等级排行榜
     */
    private List<RankingEntry> calculateLevelRanking() {
        List<Player> players = getAllPlayers();
        
        return players.stream()
                .sorted((a, b) -> {
                    int levelCompare = Integer.compare(b.getLevel(), a.getLevel());
                    if (levelCompare != 0) {
                        return levelCompare;
                    }
                    // 等级相同时按经验排序
                    return Long.compare(b.getExperience(), a.getExperience());
                })
                .limit(RANKING_SIZE)
                .map(player -> {
                    RankingEntry entry = new RankingEntry();
                    entry.setPlayerName(player.getUsername());
                    entry.setLevel(player.getLevel());
                    entry.setValue(player.getExperience());
                    entry.setJob(player.getJob());
                    entry.setGuildName(player.getGuildName());
                    entry.setLastUpdateTime(LocalDateTime.now());
                    return entry;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 计算战力排行榜
     */
    private List<RankingEntry> calculatePowerRanking() {
        List<Player> players = getAllPlayers();
        
        return players.stream()
                .sorted((a, b) -> Long.compare(calculatePowerValue(b), calculatePowerValue(a)))
                .limit(RANKING_SIZE)
                .map(player -> {
                    RankingEntry entry = new RankingEntry();
                    entry.setPlayerName(player.getUsername());
                    entry.setLevel(player.getLevel());
                    entry.setValue(calculatePowerValue(player));
                    entry.setJob(player.getJob());
                    entry.setGuildName(player.getGuildName());
                    entry.setLastUpdateTime(LocalDateTime.now());
                    return entry;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 计算财富排行榜
     */
    private List<RankingEntry> calculateWealthRanking() {
        List<Player> players = getAllPlayers();
        
        return players.stream()
                .sorted((a, b) -> Long.compare(calculateWealthValue(b), calculateWealthValue(a)))
                .limit(RANKING_SIZE)
                .map(player -> {
                    RankingEntry entry = new RankingEntry();
                    entry.setPlayerName(player.getUsername());
                    entry.setLevel(player.getLevel());
                    entry.setValue(calculateWealthValue(player));
                    entry.setJob(player.getJob());
                    entry.setGuildName(player.getGuildName());
                    entry.setLastUpdateTime(LocalDateTime.now());
                    return entry;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 计算PK排行榜
     */
    private List<RankingEntry> calculatePKRanking() {
        List<Player> players = getAllPlayers();
        
        return players.stream()
                .filter(player -> player.getPKValue() > 0)
                .sorted((a, b) -> Integer.compare(b.getPKValue(), a.getPKValue()))
                .limit(RANKING_SIZE)
                .map(player -> {
                    RankingEntry entry = new RankingEntry();
                    entry.setPlayerName(player.getUsername());
                    entry.setLevel(player.getLevel());
                    entry.setValue(player.getPKValue());
                    entry.setJob(player.getJob());
                    entry.setGuildName(player.getGuildName());
                    entry.setLastUpdateTime(LocalDateTime.now());
                    return entry;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 计算行会排行榜
     */
    private List<RankingEntry> calculateGuildRanking() {
        List<Guild> guilds = getAllGuilds();
        
        return guilds.stream()
                .sorted((a, b) -> {
                    int levelCompare = Integer.compare(b.getLevel(), a.getLevel());
                    if (levelCompare != 0) {
                        return levelCompare;
                    }
                    return Integer.compare(b.getMemberCount(), a.getMemberCount());
                })
                .limit(RANKING_SIZE)
                .map(guild -> {
                    RankingEntry entry = new RankingEntry();
                    entry.setPlayerName(guild.getName());
                    entry.setLevel(guild.getLevel());
                    entry.setValue(guild.getMemberCount());
                    entry.setJob("行会");
                    entry.setGuildName(guild.getName());
                    entry.setLastUpdateTime(LocalDateTime.now());
                    return entry;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 计算声望排行榜
     */
    private List<RankingEntry> calculateReputationRanking() {
        List<Player> players = getAllPlayers();
        
        return players.stream()
                .filter(player -> player.getCreditPoint() > 0)
                .sorted((a, b) -> Integer.compare(b.getCreditPoint(), a.getCreditPoint()))
                .limit(RANKING_SIZE)
                .map(player -> {
                    RankingEntry entry = new RankingEntry();
                    entry.setPlayerName(player.getUsername());
                    entry.setLevel(player.getLevel());
                    entry.setValue(player.getCreditPoint());
                    entry.setJob(player.getJob());
                    entry.setGuildName(player.getGuildName());
                    entry.setLastUpdateTime(LocalDateTime.now());
                    return entry;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 计算成就排行榜
     */
    private List<RankingEntry> calculateAchievementRanking() {
        List<Player> players = getAllPlayers();
        
        return players.stream()
                .sorted((a, b) -> Long.compare(calculateAchievementScore(b), calculateAchievementScore(a)))
                .limit(RANKING_SIZE)
                .map(player -> {
                    RankingEntry entry = new RankingEntry();
                    entry.setPlayerName(player.getUsername());
                    entry.setLevel(player.getLevel());
                    entry.setValue(calculateAchievementScore(player));
                    entry.setJob(player.getJob());
                    entry.setGuildName(player.getGuildName());
                    entry.setLastUpdateTime(LocalDateTime.now());
                    return entry;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 计算装备排行榜
     */
    private List<RankingEntry> calculateEquipmentRanking() {
        List<Player> players = getAllPlayers();
        
        return players.stream()
                .sorted((a, b) -> Long.compare(calculateEquipmentScore(b), calculateEquipmentScore(a)))
                .limit(RANKING_SIZE)
                .map(player -> {
                    RankingEntry entry = new RankingEntry();
                    entry.setPlayerName(player.getUsername());
                    entry.setLevel(player.getLevel());
                    entry.setValue(calculateEquipmentScore(player));
                    entry.setJob(player.getJob());
                    entry.setGuildName(player.getGuildName());
                    entry.setLastUpdateTime(LocalDateTime.now());
                    return entry;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 计算战力值
     */
    private long calculatePowerValue(Player player) {
        long power = 0;
        
        // 等级贡献
        power += player.getLevel() * 100;
        
        // 属性贡献
        power += player.getAttackPower() * 10;
        power += player.getDefensePower() * 8;
        power += player.getMagicPower() * 10;
        power += player.getMagicDefense() * 8;
        power += player.getHp() * 2;
        power += player.getMp() * 1;
        
        // 装备贡献
        power += calculateEquipmentScore(player);
        
        // 技能贡献
        power += player.getSkillPoints() * 5;
        
        return power;
    }
    
    /**
     * 计算财富值
     */
    private long calculateWealthValue(Player player) {
        long wealth = 0;
        
        // 金币
        wealth += player.getGold();
        
        // 装备价值
        wealth += calculateEquipmentWorth(player);
        
        // 道具价值
        wealth += calculateItemWorth(player);
        
        return wealth;
    }
    
    /**
     * 计算成就分数
     */
    private long calculateAchievementScore(Player player) {
        long score = 0;
        
        // 等级成就
        score += player.getLevel() * 10;
        
        // 声望成就
        score += player.getCreditPoint() * 2;
        
        // PK成就
        if (player.getPKValue() > 0) {
            score += player.getPKValue() * 5;
        }
        
        // 在线时长成就
        score += player.getTotalOnlineTime() / 3600; // 按小时计算
        
        // 添加更多成就类型
        // 装备成就
        score += calculateEquipmentScore(player) / 100;
        
        // 财富成就
        score += calculateWealthValue(player) / 10000;
        
        // 行会成就
        if (player.getGuildName() != null && !player.getGuildName().isEmpty()) {
            score += 1000; // 加入行会奖励
            score += player.getGuildRank() * 100; // 行会职位奖励
        }
        
        // 结婚成就
        if (player.getSpouseName() != null && !player.getSpouseName().isEmpty()) {
            score += 500; // 结婚奖励
        }
        
        // 师父成就
        if (player.getMasterName() != null && !player.getMasterName().isEmpty()) {
            score += 200; // 拜师奖励
        }
        
        // 徒弟成就
        score += player.getApprenticeCount() * 300; // 每个徒弟300分
        
        return score;
    }
    
    /**
     * 计算装备评分
     */
    private long calculateEquipmentScore(Player player) {
        long score = 0;
        
        try {
            // 获取玩家装备
            List<Item> equippedItems = player.getEquippedItems();
            
            for (Item item : equippedItems) {
                // 基础装备分数
                score += item.getValue() / 100;
                
                // 强化等级分数
                int enhanceLevel = item.getEnhanceLevel();
                if (enhanceLevel > 0) {
                    score += enhanceLevel * enhanceLevel * 1000; // 强化等级的平方 * 1000
                }
                
                // 装备品质分数
                score += calculateItemQualityScore(item);
                
                // 装备套装分数
                score += calculateSetEquipmentScore(item);
                
                // 稀有属性分数
                score += calculateRareAttributeScore(item);
            }
            
            // 装备总价值奖励
            long totalWorth = calculateEquipmentWorth(player);
            score += totalWorth / 10000;
            
        } catch (Exception e) {
            log.error("计算装备评分失败", e);
        }
        
        return score;
    }
    
    /**
     * 计算物品品质分数
     */
    private long calculateItemQualityScore(Item item) {
        // 根据物品品质计算分数
        // 假设有品质等级：普通=1, 优秀=2, 稀有=3, 史诗=4, 传说=5
        int qualityLevel = item.getQualityLevel();
        return qualityLevel * qualityLevel * 500;
    }
    
    /**
     * 计算套装装备分数
     */
    private long calculateSetEquipmentScore(Item item) {
        // 套装装备额外分数
        if (item.getSetId() > 0) {
            return 2000; // 套装装备额外2000分
        }
        return 0;
    }
    
    /**
     * 计算稀有属性分数
     */
    private long calculateRareAttributeScore(Item item) {
        long score = 0;
        
        // 计算特殊属性分数
        Map<String, Integer> specialAttrs = item.getSpecialAttributes();
        if (specialAttrs != null) {
            for (Map.Entry<String, Integer> entry : specialAttrs.entrySet()) {
                String attrName = entry.getKey();
                int attrValue = entry.getValue();
                
                // 根据属性类型给予不同分数
                switch (attrName) {
                    case "幸运":
                        score += attrValue * 1000;
                        break;
                    case "诅咒":
                        score += attrValue * 500;
                        break;
                    case "吸血":
                        score += attrValue * 300;
                        break;
                    case "麻痹":
                        score += attrValue * 800;
                        break;
                    default:
                        score += attrValue * 100;
                        break;
                }
            }
        }
        
        return score;
    }
    
    /**
     * 计算装备价值
     */
    private long calculateEquipmentWorth(Player player) {
        long totalWorth = 0;
        
        try {
            List<Item> equippedItems = player.getEquippedItems();
            
            for (Item item : equippedItems) {
                long itemWorth = item.getValue();
                
                // 强化装备价值翻倍
                int enhanceLevel = item.getEnhanceLevel();
                if (enhanceLevel > 0) {
                    itemWorth *= (1 + enhanceLevel * 0.5); // 每级强化增加50%价值
                }
                
                // 套装装备价值翻倍
                if (item.getSetId() > 0) {
                    itemWorth *= 2;
                }
                
                // 稀有属性增加价值
                if (item.getSpecialAttributes() != null && !item.getSpecialAttributes().isEmpty()) {
                    itemWorth *= 1.5;
                }
                
                totalWorth += itemWorth;
            }
            
        } catch (Exception e) {
            log.error("计算装备价值失败", e);
        }
        
        return totalWorth;
    }
    
    /**
     * 计算道具价值
     */
    private long calculateItemWorth(Player player) {
        long totalWorth = 0;
        
        try {
            // 获取背包物品
            List<Inventory.InventorySlot> items = inventoryService.getInventoryItems(player.getName());
            
            for (Inventory.InventorySlot slot : items) {
                if (!slot.isEmpty()) {
                    Item item = slot.getItem();
                    int quantity = slot.getQuantity();
                    
                    // 计算物品价值
                    long itemValue = item.getValue() * quantity;
                    
                    // 稀有物品价值翻倍
                    if (item.getType().name().contains("RARE")) {
                        itemValue *= 2;
                    }
                    
                    // 绑定物品价值减半
                    if (item.isBound()) {
                        itemValue /= 2;
                    }
                    
                    totalWorth += itemValue;
                }
            }
            
        } catch (Exception e) {
            log.error("计算背包道具价值失败", e);
        }
        
        return totalWorth;
    }
    
    /**
     * 获取玩家在排行榜中的位置
     */
    private int getPlayerRank(String rankingType, String playerName) {
        List<RankingEntry> ranking = getCachedRanking(rankingType, RANKING_SIZE, getRankingCalculator(rankingType));
        
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).getPlayerName().equals(playerName)) {
                return i + 1; // 排名从1开始
            }
        }
        
        return -1; // 未上榜
    }
    
    /**
     * 获取排行榜计算器
     */
    private RankingCalculator getRankingCalculator(String type) {
        switch (type) {
            case LEVEL_RANKING:
                return () -> calculateLevelRanking();
            case POWER_RANKING:
                return () -> calculatePowerRanking();
            case WEALTH_RANKING:
                return () -> calculateWealthRanking();
            case PK_RANKING:
                return () -> calculatePKRanking();
            case GUILD_RANKING:
                return () -> calculateGuildRanking();
            case REPUTATION_RANKING:
                return () -> calculateReputationRanking();
            case ACHIEVEMENT_RANKING:
                return () -> calculateAchievementRanking();
            case EQUIPMENT_RANKING:
                return () -> calculateEquipmentRanking();
            default:
                return () -> calculateLevelRanking();
        }
    }
    
    /**
     * 获取所有玩家
     */
    private List<Player> getAllPlayers() {
        try {
            return playerService.getAllPlayers();
        } catch (Exception e) {
            log.error("获取所有玩家失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取所有行会
     */
    private List<Guild> getAllGuilds() {
        try {
            return guildService.getAllGuilds();
        } catch (Exception e) {
            log.error("获取所有行会失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取排行榜摘要
     */
    public RankingSummary getRankingSummary() {
        RankingSummary summary = new RankingSummary();
        
        // 获取各排行榜的前3名
        summary.setTopLevel(getLevelRanking(3));
        summary.setTopPower(getPowerRanking(3));
        summary.setTopWealth(getWealthRanking(3));
        summary.setTopPK(getPKRanking(3));
        summary.setTopGuild(getGuildRanking(3));
        summary.setTopReputation(getReputationRanking(3));
        
        summary.setLastUpdateTime(LocalDateTime.now());
        
        return summary;
    }
    
    /**
     * 排行榜计算器接口
     */
    @FunctionalInterface
    private interface RankingCalculator {
        List<RankingEntry> calculate();
    }
    
    /**
     * 排行榜位置类
     */
    public static class RankingPosition {
        private int levelRank = -1;
        private int powerRank = -1;
        private int wealthRank = -1;
        private int pkRank = -1;
        private int reputationRank = -1;
        private int achievementRank = -1;
        private int equipmentRank = -1;
        
        // Getters and Setters
        public int getLevelRank() { return levelRank; }
        public void setLevelRank(int levelRank) { this.levelRank = levelRank; }
        
        public int getPowerRank() { return powerRank; }
        public void setPowerRank(int powerRank) { this.powerRank = powerRank; }
        
        public int getWealthRank() { return wealthRank; }
        public void setWealthRank(int wealthRank) { this.wealthRank = wealthRank; }
        
        public int getPkRank() { return pkRank; }
        public void setPkRank(int pkRank) { this.pkRank = pkRank; }
        
        public int getReputationRank() { return reputationRank; }
        public void setReputationRank(int reputationRank) { this.reputationRank = reputationRank; }
        
        public int getAchievementRank() { return achievementRank; }
        public void setAchievementRank(int achievementRank) { this.achievementRank = achievementRank; }
        
        public int getEquipmentRank() { return equipmentRank; }
        public void setEquipmentRank(int equipmentRank) { this.equipmentRank = equipmentRank; }
    }
    
    /**
     * 排行榜摘要类
     */
    public static class RankingSummary {
        private List<RankingEntry> topLevel;
        private List<RankingEntry> topPower;
        private List<RankingEntry> topWealth;
        private List<RankingEntry> topPK;
        private List<RankingEntry> topGuild;
        private List<RankingEntry> topReputation;
        private LocalDateTime lastUpdateTime;
        
        // Getters and Setters
        public List<RankingEntry> getTopLevel() { return topLevel; }
        public void setTopLevel(List<RankingEntry> topLevel) { this.topLevel = topLevel; }
        
        public List<RankingEntry> getTopPower() { return topPower; }
        public void setTopPower(List<RankingEntry> topPower) { this.topPower = topPower; }
        
        public List<RankingEntry> getTopWealth() { return topWealth; }
        public void setTopWealth(List<RankingEntry> topWealth) { this.topWealth = topWealth; }
        
        public List<RankingEntry> getTopPK() { return topPK; }
        public void setTopPK(List<RankingEntry> topPK) { this.topPK = topPK; }
        
        public List<RankingEntry> getTopGuild() { return topGuild; }
        public void setTopGuild(List<RankingEntry> topGuild) { this.topGuild = topGuild; }
        
        public List<RankingEntry> getTopReputation() { return topReputation; }
        public void setTopReputation(List<RankingEntry> topReputation) { this.topReputation = topReputation; }
        
        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
    }
} 