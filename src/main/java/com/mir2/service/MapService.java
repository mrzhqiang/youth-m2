package com.mir2.service;

import com.mir2.core.model.GameMap;
import com.mir2.core.model.Player;
import com.mir2.core.model.BaseObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 地图服务类
 * 
 * <p>提供地图相关的业务逻辑处理。
 * 对应原M2Engine中的地图管理系统。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class MapService {
    
    /** 地图缓存 */
    private final Map<String, GameMap> mapCache = new ConcurrentHashMap<>();
    
    /** 地图索引 */
    private final Map<String, GameMap> mapIndex = new ConcurrentHashMap<>();
    
    /**
     * 初始化地图
     */
    public void initializeMaps() {
        log.info("开始初始化地图...");
        
        // 创建新手村
        createBeginnerMaps();
        
        // 创建主要城市
        createMainCities();
        
        // 创建练级地图
        createLevelingMaps();
        
        // 创建副本地图
        createDungeonMaps();
        
        log.info("地图初始化完成，共加载 {} 个地图", mapCache.size());
    }
    
    /**
     * 根据地图名称获取地图
     * 
     * @param mapName 地图名称
     * @return 地图对象
     */
    public GameMap getMap(String mapName) {
        return mapCache.get(mapName);
    }
    
    /**
     * 获取所有地图名称
     * 
     * @return 地图名称列表
     */
    public List<String> getAllMapNames() {
        return mapCache.keySet().stream().collect(Collectors.toList());
    }
    
    /**
     * 玩家进入地图
     * 
     * @param player 玩家
     * @param mapName 地图名称
     * @param x X坐标
     * @param y Y坐标
     * @return 是否成功进入
     */
    public boolean enterMap(Player player, String mapName, int x, int y) {
        GameMap map = getMap(mapName);
        if (map == null) {
            log.warn("地图不存在: {}", mapName);
            return false;
        }
        
        // 检查坐标是否可用
        if (!map.isValidPosition(x, y)) {
            log.warn("玩家 {} 尝试进入无效位置: {} ({}, {})", player.getName(), mapName, x, y);
            return false;
        }
        
        // 从原地图移除玩家
        String oldMapName = player.getMapName();
        if (oldMapName != null && !oldMapName.equals(mapName)) {
            GameMap oldMap = getMap(oldMapName);
            if (oldMap != null) {
                oldMap.removeObject(player);
                log.debug("玩家 {} 离开地图: {}", player.getName(), oldMapName);
            }
        }
        
        // 添加玩家到新地图
        player.setPositionAndDirection(x, y, 0);
        player.setMapName(mapName);
        map.addObject(player);
        
        log.info("玩家 {} 进入地图: {} 位置: ({}, {})", player.getName(), mapName, x, y);
        return true;
    }
    
    /**
     * 玩家离开地图
     * 
     * @param player 玩家
     * @return 是否成功离开
     */
    public boolean leaveMap(Player player) {
        String mapName = player.getMapName();
        if (mapName == null) {
            return false;
        }
        
        GameMap map = getMap(mapName);
        if (map != null) {
            map.removeObject(player);
            log.info("玩家 {} 离开地图: {}", player.getName(), mapName);
            return true;
        }
        
        return false;
    }
    
    /**
     * 玩家移动
     * 
     * @param player 玩家
     * @param newX 新的X坐标
     * @param newY 新的Y坐标
     * @param direction 方向
     * @return 是否移动成功
     */
    public boolean movePlayer(Player player, int newX, int newY, int direction) {
        String mapName = player.getMapName();
        if (mapName == null) {
            return false;
        }
        
        GameMap map = getMap(mapName);
        if (map == null) {
            return false;
        }
        
        // 检查新位置是否可用
        if (!map.isValidPosition(newX, newY)) {
            log.debug("玩家 {} 尝试移动到无效位置: ({}, {})", player.getName(), newX, newY);
            return false;
        }
        
        // 更新玩家位置
        int oldX = player.getX();
        int oldY = player.getY();
        player.setPositionAndDirection(newX, newY, direction);
        
        // 更新地图中的对象位置
        map.updateObjectPosition(player, oldX, oldY, newX, newY);
        
        log.debug("玩家 {} 移动: ({}, {}) -> ({}, {})", player.getName(), oldX, oldY, newX, newY);
        return true;
    }
    
    /**
     * 传送玩家
     * 
     * @param player 玩家
     * @param targetMapName 目标地图
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     * @return 是否传送成功
     */
    public boolean teleportPlayer(Player player, String targetMapName, int targetX, int targetY) {
        return enterMap(player, targetMapName, targetX, targetY);
    }
    
    /**
     * 获取地图中的所有玩家
     * 
     * @param mapName 地图名称
     * @return 玩家列表
     */
    public List<Player> getPlayersInMap(String mapName) {
        GameMap map = getMap(mapName);
        if (map == null) {
            return List.of();
        }
        
        return map.getObjects().stream()
                .filter(obj -> obj instanceof Player)
                .map(obj -> (Player) obj)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取地图中指定范围内的玩家
     * 
     * @param mapName 地图名称
     * @param centerX 中心X坐标
     * @param centerY 中心Y坐标
     * @param range 范围
     * @return 玩家列表
     */
    public List<Player> getPlayersInRange(String mapName, int centerX, int centerY, int range) {
        GameMap map = getMap(mapName);
        if (map == null) {
            return List.of();
        }
        
        return map.getObjects().stream()
                .filter(obj -> obj instanceof Player)
                .map(obj -> (Player) obj)
                .filter(player -> player.isInRange(centerX, centerY, range))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取地图中的所有对象
     * 
     * @param mapName 地图名称
     * @return 对象列表
     */
    public List<BaseObject> getObjectsInMap(String mapName) {
        GameMap map = getMap(mapName);
        if (map == null) {
            return List.of();
        }
        
        return map.getObjects();
    }
    
    /**
     * 获取地图信息
     * 
     * @param mapName 地图名称
     * @return 地图信息
     */
    public String getMapInfo(String mapName) {
        GameMap map = getMap(mapName);
        if (map == null) {
            return "地图不存在";
        }
        
        int playerCount = getPlayersInMap(mapName).size();
        return String.format("地图: %s [宽度: %d, 高度: %d, 玩家数: %d]",
                map.getName(), map.getWidth(), map.getHeight(), playerCount);
    }
    
    /**
     * 查找安全位置
     * 
     * @param mapName 地图名称
     * @param preferredX 首选X坐标
     * @param preferredY 首选Y坐标
     * @return 安全位置坐标数组 [x, y]
     */
    public int[] findSafePosition(String mapName, int preferredX, int preferredY) {
        GameMap map = getMap(mapName);
        if (map == null) {
            return new int[]{0, 0};
        }
        
        return map.findSafePosition(preferredX, preferredY);
    }
    
    /**
     * 检查位置是否有效
     * 
     * @param mapName 地图名称
     * @param x X坐标
     * @param y Y坐标
     * @return 是否有效
     */
    public boolean isValidPosition(String mapName, int x, int y) {
        GameMap map = getMap(mapName);
        if (map == null) {
            return false;
        }
        
        return map.isValidPosition(x, y);
    }
    
    /**
     * 创建新手村地图
     */
    private void createBeginnerMaps() {
        // 新手村
        GameMap beginnerVillage = new GameMap("新手村", 100, 100, GameMap.MapType.SAFE);
        beginnerVillage.setDescription("新手玩家的出生地");
        beginnerVillage.setMinLevel(1);
        beginnerVillage.setMaxLevel(10);
        beginnerVillage.setPkAllowed(false);
        
        // 添加传送点
        beginnerVillage.addTeleportPoint(new GameMap.TeleportPoint(50, 50, "比奇城", 333, 333));
        
        // 添加刷怪点
        beginnerVillage.addMonsterSpawnPoint(new GameMap.MonsterSpawnPoint(1001, 30, 30, 5, 10000));
        
        addMap(beginnerVillage);
        
        log.debug("创建新手村地图完成");
    }
    
    /**
     * 创建主要城市地图
     */
    private void createMainCities() {
        // 比奇城
        GameMap biqi = new GameMap("比奇城", 200, 200, GameMap.MapType.SAFE);
        biqi.setDescription("传奇世界的主要城市");
        biqi.setMinLevel(1);
        biqi.setMaxLevel(99);
        biqi.setPkAllowed(false);
        
        // 添加传送点
        biqi.addTeleportPoint(new GameMap.TeleportPoint(100, 100, "比奇矿区", 50, 50));
        biqi.addTeleportPoint(new GameMap.TeleportPoint(150, 150, "沙漠", 100, 100));
        
        addMap(biqi);
        
        // 盟重城
        GameMap mengzhong = new GameMap("盟重城", 180, 180, GameMap.MapType.SAFE);
        mengzhong.setDescription("盟重省的首府");
        mengzhong.setMinLevel(1);
        mengzhong.setMaxLevel(99);
        mengzhong.setPkAllowed(false);
        
        addMap(mengzhong);
        
        log.debug("创建主要城市地图完成");
    }
    
    /**
     * 创建练级地图
     */
    private void createLevelingMaps() {
        // 比奇矿区
        GameMap biqiMine = new GameMap("比奇矿区", 120, 120, GameMap.MapType.NORMAL);
        biqiMine.setDescription("比奇城附近的矿区");
        biqiMine.setMinLevel(1);
        biqiMine.setMaxLevel(15);
        biqiMine.setPkAllowed(true);
        
        // 添加刷怪点
        biqiMine.addMonsterSpawnPoint(new GameMap.MonsterSpawnPoint(1002, 60, 60, 10, 30000));
        biqiMine.addMonsterSpawnPoint(new GameMap.MonsterSpawnPoint(1003, 80, 80, 8, 45000));
        
        addMap(biqiMine);
        
        // 沙漠
        GameMap desert = new GameMap("沙漠", 300, 300, GameMap.MapType.NORMAL);
        desert.setDescription("炎热的沙漠地带");
        desert.setMinLevel(15);
        desert.setMaxLevel(35);
        desert.setPkAllowed(true);
        
        // 添加刷怪点
        desert.addMonsterSpawnPoint(new GameMap.MonsterSpawnPoint(2001, 150, 150, 15, 60000));
        desert.addMonsterSpawnPoint(new GameMap.MonsterSpawnPoint(2002, 200, 200, 12, 90000));
        
        addMap(desert);
        
        log.debug("创建练级地图完成");
    }
    
    /**
     * 创建副本地图
     */
    private void createDungeonMaps() {
        // 蜈蚣洞
        GameMap centipedeCave = new GameMap("蜈蚣洞", 80, 80, GameMap.MapType.DUNGEON);
        centipedeCave.setDescription("蜈蚣怪物的巢穴");
        centipedeCave.setMinLevel(20);
        centipedeCave.setMaxLevel(40);
        centipedeCave.setPkAllowed(true);
        
        // 添加刷怪点
        centipedeCave.addMonsterSpawnPoint(new GameMap.MonsterSpawnPoint(3001, 40, 40, 20, 120000));
        
        addMap(centipedeCave);
        
        // 祖玛寺庙
        GameMap zumaTemple = new GameMap("祖玛寺庙", 150, 150, GameMap.MapType.DUNGEON);
        zumaTemple.setDescription("古老的祖玛寺庙");
        zumaTemple.setMinLevel(35);
        zumaTemple.setMaxLevel(55);
        zumaTemple.setPkAllowed(true);
        
        // 添加刷怪点
        zumaTemple.addMonsterSpawnPoint(new GameMap.MonsterSpawnPoint(4001, 75, 75, 25, 180000));
        
        addMap(zumaTemple);
        
        log.debug("创建副本地图完成");
    }
    
    /**
     * 添加地图
     * 
     * @param map 地图对象
     */
    private void addMap(GameMap map) {
        mapCache.put(map.getName(), map);
        mapIndex.put(map.getName(), map);
    }
} 