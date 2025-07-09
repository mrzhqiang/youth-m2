package com.mir2.service;

import com.mir2.core.model.Player;
import com.mir2.core.model.Guild;
import com.mir2.core.model.GameMap;
import com.mir2.entity.Castle;
import com.mir2.entity.CastleGuard;
import com.mir2.core.model.Position;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CastleWarService {
    
    private final Map<String, Castle> castleMap = new ConcurrentHashMap<>();
    private final Map<String, CastleGuard> guardMap = new ConcurrentHashMap<>();
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private GuildService guildService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private MapService mapService;
    
    /**
     * 初始化城堡
     */
    public void initializeCastle(String castleName, String mapName, int x, int y) {
        Castle castle = new Castle();
        castle.setName(castleName);
        castle.setMapName(mapName);
        castle.setX(x);
        castle.setY(y);
        castle.setOwnerGuild(null);
        castle.setWarInProgress(false);
        castle.setMainDoorHp(10000);
        castle.setLeftWallHp(8000);
        castle.setCenterWallHp(8000);
        castle.setRightWallHp(8000);
        castle.setTaxRate(0.1f); // 10%税率
        castle.setTotalGold(0);
        castle.setTodayIncome(0);
        castle.setTechLevel(1);
        castle.setPower(100);
        
        castleMap.put(castleName, castle);
        
        // 初始化守卫
        initializeCastleGuards(castleName);
    }
    
    /**
     * 开始攻城战
     */
    @Transactional
    public boolean startCastleWar(String castleName, Guild attackingGuild) {
        Castle castle = castleMap.get(castleName);
        if (castle == null || castle.isWarInProgress()) {
            return false;
        }
        
        // 检查攻城时间
        LocalDateTime now = LocalDateTime.now();
        if (now.getHour() < 20 || now.getHour() > 22) {
            return false; // 只能在晚上8-10点攻城
        }
        
        // 检查行会等级
        if (attackingGuild.getLevel() < 3) {
            return false;
        }
        
        // 检查攻城费用
        if (attackingGuild.getMoney() < 5000000) {
            return false;
        }
        
        // 扣除攻城费用
        attackingGuild.setMoney(attackingGuild.getMoney() - 5000000);
        
        // 设置攻城状态
        castle.setWarInProgress(true);
        castle.setWarStartTime(now);
        castle.setWarEndTime(now.plusHours(2)); // 攻城持续2小时
        castle.getAttackingGuilds().add(attackingGuild.getName());
        
        // 重置城防设施血量
        resetCastleDefenses(castleName);
        
        return true;
    }
    
    /**
     * 结束攻城战
     */
    @Transactional
    public void endCastleWar(String castleName) {
        Castle castle = castleMap.get(castleName);
        if (castle == null || !castle.isWarInProgress()) {
            return;
        }
        
        // 检查是否有行会占领皇宫
        Guild newOwner = checkPalaceOccupation(castleName);
        if (newOwner != null) {
            changeCastleOwner(castleName, newOwner);
        }
        
        // 清除攻城状态
        castle.setWarInProgress(false);
        castle.setWarStartTime(null);
        castle.setWarEndTime(null);
        castle.getAttackingGuilds().clear();
    }
    
    /**
     * 更换城主
     */
    @Transactional
    public void changeCastleOwner(String castleName, Guild newOwner) {
        Castle castle = castleMap.get(castleName);
        if (castle == null) {
            return;
        }
        
        Guild oldOwner = castle.getOwnerGuild();
        
        // 更换城主
        castle.setOwnerGuild(newOwner);
        castle.setChangeDate(LocalDateTime.now());
        
        // 重置城防设施
        resetCastleDefenses(castleName);
        
        // 给新城主发放奖励
        if (newOwner != null) {
            newOwner.setMoney(newOwner.getMoney() + 1000000);
            newOwner.setCreditPoint(newOwner.getCreditPoint() + 500);
        }
        
        // 通知所有玩家
        broadcastCastleOwnerChange(castleName, oldOwner, newOwner);
    }
    
    /**
     * 攻击城防设施
     */
    public boolean attackCastleDefense(String castleName, String targetType, int damage) {
        Castle castle = castleMap.get(castleName);
        if (castle == null || !castle.isWarInProgress()) {
            return false;
        }
        
        boolean destroyed = false;
        
        switch (targetType.toLowerCase()) {
            case "maindoor":
                castle.setMainDoorHp(Math.max(0, castle.getMainDoorHp() - damage));
                destroyed = castle.getMainDoorHp() <= 0;
                break;
            case "leftwall":
                castle.setLeftWallHp(Math.max(0, castle.getLeftWallHp() - damage));
                destroyed = castle.getLeftWallHp() <= 0;
                break;
            case "centerwall":
                castle.setCenterWallHp(Math.max(0, castle.getCenterWallHp() - damage));
                destroyed = castle.getCenterWallHp() <= 0;
                break;
            case "rightwall":
                castle.setRightWallHp(Math.max(0, castle.getRightWallHp() - damage));
                destroyed = castle.getRightWallHp() <= 0;
                break;
        }
        
        return destroyed;
    }
    
    /**
     * 检查是否在攻城范围内
     */
    public boolean inCastleWarArea(String castleName, Position position) {
        Castle castle = castleMap.get(castleName);
        if (castle == null) {
            return false;
        }
        
        int range = 50; // 攻城范围50格
        int dx = Math.abs(position.getX() - castle.getX());
        int dy = Math.abs(position.getY() - castle.getY());
        
        return dx <= range && dy <= range;
    }
    
    /**
     * 检查是否为城主成员
     */
    public boolean isCastleMember(String castleName, Player player) {
        Castle castle = castleMap.get(castleName);
        if (castle == null || castle.getOwnerGuild() == null) {
            return false;
        }
        
        return castle.getOwnerGuild().getName().equals(player.getGuildName());
    }
    
    /**
     * 修理城门
     */
    @Transactional
    public boolean repairMainDoor(String castleName, Player player) {
        Castle castle = castleMap.get(castleName);
        if (castle == null || !isCastleMember(castleName, player)) {
            return false;
        }
        
        if (castle.getMainDoorHp() >= 10000) {
            return false; // 城门血量已满
        }
        
        // 检查修理费用
        int repairCost = 100000;
        if (player.getGold() < repairCost) {
            return false;
        }
        
        // 扣除费用并修理
        player.setGold(player.getGold() - repairCost);
        castle.setMainDoorHp(10000);
        
        return true;
    }
    
    /**
     * 修理城墙
     */
    @Transactional
    public boolean repairWall(String castleName, String wallType, Player player) {
        Castle castle = castleMap.get(castleName);
        if (castle == null || !isCastleMember(castleName, player)) {
            return false;
        }
        
        int repairCost = 80000;
        if (player.getGold() < repairCost) {
            return false;
        }
        
        player.setGold(player.getGold() - repairCost);
        
        switch (wallType.toLowerCase()) {
            case "left":
                castle.setLeftWallHp(8000);
                break;
            case "center":
                castle.setCenterWallHp(8000);
                break;
            case "right":
                castle.setRightWallHp(8000);
                break;
            default:
                return false;
        }
        
        return true;
    }
    
    /**
     * 收取城池税收
     */
    @Transactional
    public int collectTax(String castleName, Player player) {
        Castle castle = castleMap.get(castleName);
        if (castle == null || !isCastleMember(castleName, player)) {
            return 0;
        }
        
        int totalGold = castle.getTotalGold();
        if (totalGold <= 0) {
            return 0;
        }
        
        // 清空城池金库
        castle.setTotalGold(0);
        
        // 给玩家发放金币
        player.setGold(player.getGold() + totalGold);
        
        return totalGold;
    }
    
    /**
     * 升级城池科技
     */
    @Transactional
    public boolean upgradeTechLevel(String castleName, Player player) {
        Castle castle = castleMap.get(castleName);
        if (castle == null || !isCastleMember(castleName, player)) {
            return false;
        }
        
        if (castle.getTechLevel() >= 10) {
            return false; // 已达到最大等级
        }
        
        int upgradeCost = castle.getTechLevel() * 1000000;
        if (player.getGold() < upgradeCost) {
            return false;
        }
        
        player.setGold(player.getGold() - upgradeCost);
        castle.setTechLevel(castle.getTechLevel() + 1);
        castle.setPower(castle.getPower() + 50);
        
        return true;
    }
    
    /**
     * 获取城池信息
     */
    public Castle getCastleInfo(String castleName) {
        return castleMap.get(castleName);
    }
    
    /**
     * 获取所有城池列表
     */
    public List<Castle> getAllCastles() {
        return castleMap.values().stream().collect(Collectors.toList());
    }
    
    /**
     * 重置城防设施
     */
    private void resetCastleDefenses(String castleName) {
        Castle castle = castleMap.get(castleName);
        if (castle == null) {
            return;
        }
        
        castle.setMainDoorHp(10000);
        castle.setLeftWallHp(8000);
        castle.setCenterWallHp(8000);
        castle.setRightWallHp(8000);
    }
    
    /**
     * 初始化城堡守卫
     */
    private void initializeCastleGuards(String castleName) {
        // 创建弓箭手守卫
        for (int i = 0; i < 12; i++) {
            CastleGuard archer = new CastleGuard();
            archer.setName("弓箭手" + (i + 1));
            archer.setType("ARCHER");
            archer.setCastleName(castleName);
            archer.setMaxHp(5000);
            archer.setCurrentHp(5000);
            archer.setAttackPower(200);
            archer.setDefense(100);
            archer.setAlive(true);
            
            guardMap.put(castleName + "_archer_" + i, archer);
        }
        
        // 创建守卫
        for (int i = 0; i < 4; i++) {
            CastleGuard guard = new CastleGuard();
            guard.setName("守卫" + (i + 1));
            guard.setType("GUARD");
            guard.setCastleName(castleName);
            guard.setMaxHp(8000);
            guard.setCurrentHp(8000);
            guard.setAttackPower(300);
            guard.setDefense(150);
            guard.setAlive(true);
            
            guardMap.put(castleName + "_guard_" + i, guard);
        }
    }
    
    /**
     * 检查皇宫占领情况
     */
    private Guild checkPalaceOccupation(String castleName) {
        try {
            // 获取城堡信息
            Castle castle = castleMap.get(castleName);
            if (castle == null) {
                return null;
            }
            
            // 定义皇宫区域坐标
            Position palaceStart = new Position(castle.getX() - 10, castle.getY() - 10);
            Position palaceEnd = new Position(castle.getX() + 10, castle.getY() + 10);
            
            // 获取皇宫内的所有玩家
            List<Player> playersInPalace = getPlayersInArea(castle.getMapName(), palaceStart, palaceEnd);
            
            if (playersInPalace.isEmpty()) {
                return null;
            }
            
            // 统计各个行会在皇宫内的人数
            Map<String, Integer> guildCounts = new ConcurrentHashMap<>();
            
            for (Player player : playersInPalace) {
                String guildName = player.getGuildName();
                if (guildName != null && !guildName.isEmpty()) {
                    guildCounts.put(guildName, guildCounts.getOrDefault(guildName, 0) + 1);
                }
            }
            
            // 找出人数最多的行会
            String dominantGuildName = null;
            int maxCount = 0;
            
            for (Map.Entry<String, Integer> entry : guildCounts.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    dominantGuildName = entry.getKey();
                }
            }
            
            // 至少需要3个人才能占领皇宫
            if (maxCount >= 3) {
                Guild dominantGuild = guildService.getGuildByName(dominantGuildName);
                if (dominantGuild != null) {
                    log.info("行会 {} 在皇宫内有 {} 人，正在占领城池 {}", 
                        dominantGuildName, maxCount, castleName);
                    return dominantGuild;
                }
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("检查皇宫占领情况失败", e);
            return null;
        }
    }
    
    /**
     * 广播城主变更消息
     */
    private void broadcastCastleOwnerChange(String castleName, Guild oldOwner, Guild newOwner) {
        try {
            String message;
            
            if (oldOwner == null && newOwner != null) {
                message = String.format("【系统公告】城池 %s 被行会 %s 占领！", 
                    castleName, newOwner.getName());
            } else if (oldOwner != null && newOwner == null) {
                message = String.format("【系统公告】城池 %s 失去了城主，现在无人占领！", 
                    castleName);
            } else if (oldOwner != null && newOwner != null) {
                message = String.format("【系统公告】城池 %s 的城主从 %s 变更为 %s！", 
                    castleName, oldOwner.getName(), newOwner.getName());
            } else {
                return; // 没有变化
            }
            
            // 发送给所有在线玩家
            List<Player> onlinePlayers = playerService.getOnlinePlayers();
            for (Player player : onlinePlayers) {
                notificationService.sendSystemMessage(player.getName(), message);
            }
            
            // 发送给相关行会成员特殊消息
            if (newOwner != null) {
                String guildMessage = String.format("恭喜！您的行会已成功占领城池 %s，成为新的城主！", 
                    castleName);
                List<Player> guildMembers = guildService.getOnlineGuildMembers(newOwner.getName());
                for (Player member : guildMembers) {
                    notificationService.sendGuildMessage(member.getName(), guildMessage);
                }
            }
            
            if (oldOwner != null && newOwner != null) {
                String lostMessage = String.format("很遗憾！您的行会失去了城池 %s 的控制权。", 
                    castleName);
                List<Player> oldGuildMembers = guildService.getOnlineGuildMembers(oldOwner.getName());
                for (Player member : oldGuildMembers) {
                    notificationService.sendGuildMessage(member.getName(), lostMessage);
                }
            }
            
            log.info("城主变更广播: {}", message);
            
        } catch (Exception e) {
            log.error("广播城主变更消息失败", e);
        }
    }
    
    /**
     * 获取指定区域内的玩家
     */
    private List<Player> getPlayersInArea(String mapName, Position start, Position end) {
        try {
            // 获取地图上的所有玩家
            List<Player> playersOnMap = mapService.getPlayersOnMap(mapName);
            
            // 过滤出指定区域内的玩家
            return playersOnMap.stream()
                .filter(player -> {
                    Position pos = player.getPosition();
                    return pos.getX() >= start.getX() && pos.getX() <= end.getX() &&
                           pos.getY() >= start.getY() && pos.getY() <= end.getY();
                })
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("获取区域内玩家失败", e);
            return List.of();
        }
    }
    
    /**
     * 检查城战是否结束
     */
    public boolean isCastleWarActive(String castleName) {
        Castle castle = castleMap.get(castleName);
        if (castle == null) {
            return false;
        }
        
        return castle.isInWar();
    }
    
    /**
     * 获取城战剩余时间
     */
    public long getCastleWarRemainingTime(String castleName) {
        Castle castle = castleMap.get(castleName);
        if (castle == null || !castle.isInWar()) {
            return 0;
        }
        
        LocalDateTime warEndTime = castle.getWarStartTime().plusHours(2); // 攻城战持续2小时
        return warEndTime.compareTo(LocalDateTime.now()) > 0 ? 
               java.time.Duration.between(LocalDateTime.now(), warEndTime).toSeconds() : 0;
    }
    
    /**
     * 获取城池守卫状态
     */
    public List<CastleGuard> getCastleGuards(String castleName) {
        return guardMap.values().stream()
            .filter(guard -> guard.getCastleName().equals(castleName))
            .collect(Collectors.toList());
    }
    
    /**
     * 攻击城池守卫
     */
    public boolean attackCastleGuard(String castleName, String guardType, int index, int damage) {
        String guardKey = castleName + "_" + guardType.toLowerCase() + "_" + index;
        CastleGuard guard = guardMap.get(guardKey);
        
        if (guard == null || !guard.isAlive()) {
            return false;
        }
        
        // 扣除血量
        guard.setCurrentHp(Math.max(0, guard.getCurrentHp() - damage));
        
        // 检查是否死亡
        if (guard.getCurrentHp() <= 0) {
            guard.setAlive(false);
            log.info("城池守卫 {} 已被击杀", guard.getName());
        }
        
        return true;
    }
    
    /**
     * 复活城池守卫
     */
    public boolean reviveCastleGuard(String castleName, String guardType, int index, Player player) {
        if (!isCastleMember(castleName, player)) {
            return false;
        }
        
        String guardKey = castleName + "_" + guardType.toLowerCase() + "_" + index;
        CastleGuard guard = guardMap.get(guardKey);
        
        if (guard == null || guard.isAlive()) {
            return false;
        }
        
        // 检查复活费用
        int reviveCost = 50000;
        if (player.getGold() < reviveCost) {
            return false;
        }
        
        // 扣除费用并复活
        player.setGold(player.getGold() - reviveCost);
        guard.setCurrentHp(guard.getMaxHp());
        guard.setAlive(true);
        
        log.info("玩家 {} 复活了城池守卫 {}", player.getName(), guard.getName());
        return true;
    }
    
    /**
     * 获取城池税收统计
     */
    public Map<String, Integer> getCastleTaxStatistics(String castleName) {
        Castle castle = castleMap.get(castleName);
        if (castle == null) {
            return Map.of();
        }
        
        Map<String, Integer> stats = new ConcurrentHashMap<>();
        stats.put("totalGold", castle.getTotalGold());
        stats.put("dailyIncome", castle.getDailyIncome());
        stats.put("taxRate", castle.getTaxRate());
        
        return stats;
    }
    
    /**
     * 设置城池税率
     */
    public boolean setCastleTaxRate(String castleName, int taxRate, Player player) {
        if (!isCastleMember(castleName, player)) {
            return false;
        }
        
        Castle castle = castleMap.get(castleName);
        if (castle == null) {
            return false;
        }
        
        // 税率限制在0-20%之间
        if (taxRate < 0 || taxRate > 20) {
            return false;
        }
        
        castle.setTaxRate(taxRate);
        log.info("城池 {} 的税率设置为 {}%", castleName, taxRate);
        
        return true;
    }
    
    /**
     * 城池日常维护
     */
    public void dailyCastleMaintenance() {
        try {
            for (Castle castle : castleMap.values()) {
                // 维护费用
                int maintenanceCost = castle.getTechLevel() * 10000;
                
                if (castle.getTotalGold() >= maintenanceCost) {
                    castle.setTotalGold(castle.getTotalGold() - maintenanceCost);
                } else {
                    // 金币不足，降低城池科技等级
                    castle.setTechLevel(Math.max(1, castle.getTechLevel() - 1));
                    castle.setPower(Math.max(100, castle.getPower() - 50));
                }
                
                // 重置日收入
                castle.setDailyIncome(0);
            }
            
            log.info("完成城池日常维护");
            
        } catch (Exception e) {
            log.error("城池日常维护失败", e);
        }
    }
} 