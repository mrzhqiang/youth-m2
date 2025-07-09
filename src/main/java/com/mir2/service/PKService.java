package com.mir2.service;

import com.mir2.core.model.Player;
import com.mir2.core.model.Position;
import com.mir2.core.model.GameMap;
import com.mir2.core.model.Item;
import com.mir2.core.enums.NameColor;
import com.mir2.entity.PKRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PKService {
    
    private final Map<String, PKRecord> pkRecords = new ConcurrentHashMap<>();
    private final Map<String, List<String>> redNameList = new ConcurrentHashMap<>();
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private MarriageService marriageService;
    
    @Autowired
    private MasterApprenticeService masterApprenticeService;
    
    @Autowired
    private MapService mapService;
    
    @Autowired
    private ItemService itemService;
    
    // PK值阈值配置
    private static final int WHITE_NAME_THRESHOLD = 0;      // 白名
    private static final int YELLOW_NAME_THRESHOLD = 100;   // 黄名
    private static final int RED_NAME_THRESHOLD = 300;      // 红名
    private static final int PINK_NAME_THRESHOLD = 500;     // 粉名
    
    // PK保护配置
    private static final int PK_PROTECTION_LEVEL = 20;     // 20级以下受保护
    private static final int RED_PK_PROTECTION_LEVEL = 35; // 红名PK保护等级
    
    // PK值衰减配置
    private static final int PK_DECAY_RATE = 1;            // 每小时减少1点PK值
    private static final long PK_DECAY_INTERVAL = 3600000; // 1小时
    
    // 装备掉落配置
    private static final float YELLOW_DROP_RATE = 0.1f;    // 黄名掉落率10%
    private static final float RED_DROP_RATE = 0.3f;       // 红名掉落率30%
    private static final float PINK_DROP_RATE = 0.5f;      // 粉名掉落率50%
    
    /**
     * 处理PK事件
     */
    @Transactional
    public PKResult processPK(Player killer, Player victim) {
        // 检查PK条件
        PKCheckResult checkResult = checkPKConditions(killer, victim);
        if (!checkResult.canPK) {
            return new PKResult(false, checkResult.reason, 0, 0);
        }
        
        // 计算PK值变化
        int killerPKChange = calculateKillerPKChange(killer, victim);
        int victimPKChange = calculateVictimPKChange(killer, victim);
        
        // 应用PK值变化
        killer.setPKValue(Math.max(0, killer.getPKValue() + killerPKChange));
        victim.setPKValue(Math.max(0, victim.getPKValue() + victimPKChange));
        
        // 更新名字颜色
        updateNameColor(killer);
        updateNameColor(victim);
        
        // 处理PK惩罚
        processPKPenalty(killer, victim);
        
        // 处理PK奖励
        processPKReward(killer, victim);
        
        // 记录PK事件
        recordPKEvent(killer, victim, killerPKChange, victimPKChange);
        
        return new PKResult(true, "PK处理成功", killerPKChange, victimPKChange);
    }
    
    /**
     * 检查PK条件
     */
    private PKCheckResult checkPKConditions(Player killer, Player victim) {
        // 检查等级保护
        if (victim.getLevel() < PK_PROTECTION_LEVEL) {
            return new PKCheckResult(false, "目标等级过低，受到PK保护");
        }
        
        // 检查红名保护
        if (isRedName(killer) && victim.getLevel() < RED_PK_PROTECTION_LEVEL) {
            return new PKCheckResult(false, "红名玩家不能PK等级过低的玩家");
        }
        
        // 检查是否在安全区
        if (isInSafeZone(victim.getPosition())) {
            return new PKCheckResult(false, "目标在安全区内，无法PK");
        }
        
        // 检查是否为行会成员
        if (isGuildMember(killer, victim)) {
            return new PKCheckResult(false, "不能PK同行会成员");
        }
        
        // 检查是否为师徒关系
        if (isMasterApprentice(killer, victim)) {
            return new PKCheckResult(false, "不能PK师父/徒弟");
        }
        
        // 检查是否为夫妻关系
        if (isMarried(killer, victim)) {
            return new PKCheckResult(false, "不能PK配偶");
        }
        
        // 检查是否为队友
        if (isTeamMember(killer, victim)) {
            return new PKCheckResult(false, "不能PK队友");
        }
        
        return new PKCheckResult(true, "可以PK");
    }
    
    /**
     * 计算杀手PK值变化
     */
    private int calculateKillerPKChange(Player killer, Player victim) {
        int pkChange = 100; // 基础PK值增长
        
        // 根据被杀者PK值调整
        if (isRedName(victim)) {
            pkChange = -50; // 杀红名减少PK值
        } else if (isYellowName(victim)) {
            pkChange = 50; // 杀黄名增加50PK值
        }
        
        // 根据等级差调整
        int levelDiff = killer.getLevel() - victim.getLevel();
        if (levelDiff > 10) {
            pkChange += 50; // 等级差过大增加PK值
        } else if (levelDiff < -10) {
            pkChange -= 30; // 杀高等级玩家减少PK值
        }
        
        return pkChange;
    }
    
    /**
     * 计算受害者PK值变化
     */
    private int calculateVictimPKChange(Player killer, Player victim) {
        // 被杀者一般不增加PK值
        int pkChange = 0;
        
        // 如果被杀者是红名，死亡时减少PK值
        if (isRedName(victim)) {
            pkChange = -20;
        }
        
        return pkChange;
    }
    
    /**
     * 更新名字颜色
     */
    private void updateNameColor(Player player) {
        int pkValue = player.getPKValue();
        
        if (pkValue >= PINK_NAME_THRESHOLD) {
            player.setNameColor(NameColor.PINK);
        } else if (pkValue >= RED_NAME_THRESHOLD) {
            player.setNameColor(NameColor.RED);
        } else if (pkValue >= YELLOW_NAME_THRESHOLD) {
            player.setNameColor(NameColor.YELLOW);
        } else {
            player.setNameColor(NameColor.WHITE);
        }
    }
    
    /**
     * 处理PK惩罚
     */
    private void processPKPenalty(Player killer, Player victim) {
        // 对受害者的处理
        if (isRedName(victim)) {
            // 红名死亡掉落装备
            dropEquipmentOnDeath(victim, RED_DROP_RATE);
        } else if (isYellowName(victim)) {
            // 黄名死亡可能掉落装备
            dropEquipmentOnDeath(victim, YELLOW_DROP_RATE);
        }
        
        // 经验损失
        float expLossRate = isRedName(victim) ? 0.05f : 0.02f;
        loseExperienceOnDeath(victim, expLossRate);
    }
    
    /**
     * 处理PK奖励
     */
    private void processPKReward(Player killer, Player victim) {
        // 杀死红名的奖励
        if (isRedName(victim)) {
            // 给予经验奖励
            long expReward = victim.getLevel() * 1000L;
            killer.setExperience(killer.getExperience() + expReward);
            
            // 可能获得声望奖励
            killer.setReputation(killer.getReputation() + 100);
        }
    }
    
    /**
     * 记录PK事件
     */
    private void recordPKEvent(Player killer, Player victim, int killerPKChange, int victimPKChange) {
        PKRecord record = new PKRecord();
        record.setKillerName(killer.getName());
        record.setVictimName(victim.getName());
        record.setKillerLevel(killer.getLevel());
        record.setVictimLevel(victim.getLevel());
        record.setKillerPKBefore(killer.getPKValue() - killerPKChange);
        record.setKillerPKAfter(killer.getPKValue());
        record.setVictimPKBefore(victim.getPKValue() - victimPKChange);
        record.setVictimPKAfter(victim.getPKValue());
        record.setMapName(victim.getMapName());
        record.setTimestamp(new Date());
        
        pkRecords.put(UUID.randomUUID().toString(), record);
    }
    
    /**
     * PK值自动衰减
     */
    public void processPKDecay() {
        long currentTime = System.currentTimeMillis();
        
        // 获取所有在线玩家
        List<Player> onlinePlayers = playerService.getAllOnlinePlayers();
        
        for (Player player : onlinePlayers) {
            if (currentTime - player.getLastPKDecayTime() >= PK_DECAY_INTERVAL) {
                if (player.getPKValue() > 0) {
                    player.setPKValue(Math.max(0, player.getPKValue() - PK_DECAY_RATE));
                    updateNameColor(player);
                    log.debug("玩家 {} PK值衰减，当前PK值: {}", player.getName(), player.getPKValue());
                }
                player.setLastPKDecayTime(currentTime);
            }
        }
    }
    
    /**
     * 获取PK排行榜
     */
    public List<PKRecord> getPKRanking(int limit) {
        return pkRecords.values().stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取玩家PK记录
     */
    public List<PKRecord> getPlayerPKRecords(String playerName, int limit) {
        return pkRecords.values().stream()
                .filter(record -> record.getKillerName().equals(playerName) || 
                                record.getVictimName().equals(playerName))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * 检查是否为红名
     */
    public boolean isRedName(Player player) {
        return player.getPKValue() >= RED_NAME_THRESHOLD;
    }
    
    /**
     * 检查是否为黄名
     */
    public boolean isYellowName(Player player) {
        return player.getPKValue() >= YELLOW_NAME_THRESHOLD && 
               player.getPKValue() < RED_NAME_THRESHOLD;
    }
    
    /**
     * 检查是否在安全区
     */
    private boolean isInSafeZone(Position position) {
        try {
            GameMap gameMap = mapService.getMap(position.getMapName());
            if (gameMap != null) {
                return gameMap.isSafeZone(position.getX(), position.getY());
            }
        } catch (Exception e) {
            log.error("检查安全区失败", e);
        }
        return false;
    }
    
    /**
     * 检查是否为行会成员
     */
    private boolean isGuildMember(Player player1, Player player2) {
        return player1.getGuildName() != null && 
               player1.getGuildName().equals(player2.getGuildName());
    }
    
    /**
     * 检查是否为师徒关系
     */
    private boolean isMasterApprentice(Player player1, Player player2) {
        try {
            return masterApprenticeService.isMasterApprentice(player1.getName(), player2.getName());
        } catch (Exception e) {
            log.error("检查师徒关系失败", e);
            return false;
        }
    }
    
    /**
     * 检查是否为夫妻关系
     */
    private boolean isMarried(Player player1, Player player2) {
        try {
            return marriageService.isMarried(player1.getName(), player2.getName());
        } catch (Exception e) {
            log.error("检查夫妻关系失败", e);
            return false;
        }
    }
    
    /**
     * 检查是否为队友
     */
    private boolean isTeamMember(Player player1, Player player2) {
        return player1.getTeamId() != null && 
               player1.getTeamId().equals(player2.getTeamId());
    }
    
    /**
     * 死亡时掉落装备
     */
    private void dropEquipmentOnDeath(Player player, float dropRate) {
        try {
            List<Item> equipments = player.getEquippedItems();
            for (Item equipment : equipments) {
                if (Math.random() < dropRate) {
                    // 掉落装备到地面
                    itemService.dropItemToGround(equipment, player.getPosition());
                    player.removeEquipment(equipment.getItemId());
                    log.info("玩家 {} 死亡掉落装备: {}", player.getName(), equipment.getName());
                }
            }
        } catch (Exception e) {
            log.error("处理装备掉落失败", e);
        }
    }
    
    /**
     * 死亡时损失经验
     */
    private void loseExperienceOnDeath(Player player, float lossRate) {
        long currentExp = player.getExperience();
        long expLoss = (long) (currentExp * lossRate);
        player.setExperience(Math.max(0, currentExp - expLoss));
    }
    
    /**
     * 掉落随机装备
     */
    private void dropRandomEquipment(Player player) {
        try {
            List<Item> equipments = player.getEquippedItems();
            if (!equipments.isEmpty()) {
                Item randomEquipment = equipments.get(new Random().nextInt(equipments.size()));
                itemService.dropItemToGround(randomEquipment, player.getPosition());
                player.removeEquipment(randomEquipment.getItemId());
                log.info("玩家 {} 随机掉落装备: {}", player.getName(), randomEquipment.getName());
            }
        } catch (Exception e) {
            log.error("处理随机装备掉落失败", e);
        }
    }
    
    /**
     * 清除PK值
     */
    @Transactional
    public void clearPKValue(Player player) {
        player.setPKValue(0);
        updateNameColor(player);
    }
    
    /**
     * 添加PK值
     */
    @Transactional
    public void addPKValue(Player player, int amount) {
        player.setPKValue(player.getPKValue() + amount);
        updateNameColor(player);
    }
    
    /**
     * 获取PK统计信息
     */
    public PKStatistics getPKStatistics(String playerName) {
        List<PKRecord> playerRecords = getPlayerPKRecords(playerName, Integer.MAX_VALUE);
        
        long killCount = playerRecords.stream()
                .filter(record -> record.getKillerName().equals(playerName))
                .count();
        
        long deathCount = playerRecords.stream()
                .filter(record -> record.getVictimName().equals(playerName))
                .count();
        
        return new PKStatistics(killCount, deathCount, killCount - deathCount);
    }
    
    /**
     * PK检查结果类
     */
    private static class PKCheckResult {
        final boolean canPK;
        final String reason;
        
        PKCheckResult(boolean canPK, String reason) {
            this.canPK = canPK;
            this.reason = reason;
        }
    }
    
    /**
     * PK结果类
     */
    public static class PKResult {
        private final boolean success;
        private final String message;
        private final int killerPKChange;
        private final int victimPKChange;
        
        public PKResult(boolean success, String message, int killerPKChange, int victimPKChange) {
            this.success = success;
            this.message = message;
            this.killerPKChange = killerPKChange;
            this.victimPKChange = victimPKChange;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getKillerPKChange() { return killerPKChange; }
        public int getVictimPKChange() { return victimPKChange; }
    }
    
    /**
     * PK统计信息类
     */
    public static class PKStatistics {
        private final long killCount;
        private final long deathCount;
        private final long kdRatio;
        
        public PKStatistics(long killCount, long deathCount, long kdRatio) {
            this.killCount = killCount;
            this.deathCount = deathCount;
            this.kdRatio = kdRatio;
        }
        
        public long getKillCount() { return killCount; }
        public long getDeathCount() { return deathCount; }
        public long getKdRatio() { return kdRatio; }
    }
} 