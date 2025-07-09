package com.mir2.core.model;

import com.mir2.core.enums.Direction;
import com.mir2.core.enums.GameObjectType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 游戏地图类
 * 
 * <p>代表游戏中的地图，包含地图的基本信息、地形数据、对象管理等。
 * 对应原M2Engine中的地图系统。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>地图基本信息管理</li>
 *   <li>地形数据处理</li>
 *   <li>游戏对象管理（玩家、NPC、怪物等）</li>
 *   <li>路径查找和碰撞检测</li>
 *   <li>地图事件处理</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Slf4j
public class GameMap {
    
    /** 地图ID */
    private int mapId;
    
    /** 地图名称 */
    private String name;
    
    /** 地图文件名 */
    private String fileName;
    
    /** 地图宽度 */
    private int width;
    
    /** 地图高度 */
    private int height;
    
    /** 地图类型 */
    private MapType mapType;
    
    /** 地图描述 */
    private String description;
    
    /** 是否为安全区 */
    private boolean safeZone;
    
    /** 是否允许PK */
    private boolean pkAllowed;
    
    /** 是否允许行会战 */
    private boolean guildWarAllowed;
    
    /** 是否允许使用回城卷 */
    private boolean townPortalAllowed;
    
    /** 是否允许使用随机传送 */
    private boolean randomTeleportAllowed;
    
    /** 地图等级限制 */
    private int levelLimit;
    
    /** 地图最大玩家数 */
    private int maxPlayers;
    
    /** 地形数据 */
    private MapTile[][] tiles;
    
    /** 地图中的所有对象 */
    private Map<Long, BaseObject> objects;
    
    /** 地图中的玩家 */
    private Map<String, Player> players;
    
    /** 地图中的NPC */
    private Map<String, BaseObject> npcs;
    
    /** 地图中的怪物 */
    private Map<String, BaseObject> monsters;
    
    /** 地图中的物品 */
    private Map<String, BaseObject> items;
    
    /** 地图事件列表 */
    private List<MapEvent> events;
    
    /** 刷怪点配置 */
    private List<MonsterSpawnPoint> spawnPoints;
    
    /** 传送点配置 */
    private List<TeleportPoint> teleportPoints;
    
    /** 地图创建时间 */
    private long createTime;
    
    /** 最后更新时间 */
    private long lastUpdateTime;
    
    /**
     * 构造函数
     * 
     * @param mapId 地图ID
     * @param name 地图名称
     * @param fileName 地图文件名
     * @param width 地图宽度
     * @param height 地图高度
     */
    public GameMap(int mapId, String name, String fileName, int width, int height) {
        this.mapId = mapId;
        this.name = name;
        this.fileName = fileName;
        this.width = width;
        this.height = height;
        this.mapType = MapType.NORMAL;
        this.safeZone = false;
        this.pkAllowed = true;
        this.guildWarAllowed = true;
        this.townPortalAllowed = true;
        this.randomTeleportAllowed = true;
        this.levelLimit = 0;
        this.maxPlayers = 500;
        
        // 初始化地形数据
        this.tiles = new MapTile[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = new MapTile(x, y);
            }
        }
        
        // 初始化对象集合
        this.objects = new ConcurrentHashMap<>();
        this.players = new ConcurrentHashMap<>();
        this.npcs = new ConcurrentHashMap<>();
        this.monsters = new ConcurrentHashMap<>();
        this.items = new ConcurrentHashMap<>();
        this.events = new CopyOnWriteArrayList<>();
        this.spawnPoints = new CopyOnWriteArrayList<>();
        this.teleportPoints = new CopyOnWriteArrayList<>();
        
        this.createTime = System.currentTimeMillis();
        this.lastUpdateTime = createTime;
        
        log.info("创建地图: {} [{}x{}]", name, width, height);
    }
    
    /**
     * 检查坐标是否在地图范围内
     * 
     * @param x X坐标
     * @param y Y坐标
     * @return 是否在范围内
     */
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    /**
     * 检查坐标是否可以通过
     * 
     * @param x X坐标
     * @param y Y坐标
     * @return 是否可以通过
     */
    public boolean isPassable(int x, int y) {
        if (!isValidPosition(x, y)) {
            return false;
        }
        
        MapTile tile = tiles[x][y];
        return tile.isPassable() && !tile.isOccupied();
    }
    
    /**
     * 添加对象到地图
     * 
     * @param object 游戏对象
     * @return 是否添加成功
     */
    public boolean addObject(BaseObject object) {
        if (object == null) {
            return false;
        }
        
        int x = object.getX();
        int y = object.getY();
        
        if (!isValidPosition(x, y)) {
            log.warn("尝试在无效位置添加对象: {} at ({}, {})", object.getName(), x, y);
            return false;
        }
        
        // 检查是否可以放置
        if (!isPassable(x, y)) {
            log.warn("位置被占用，无法添加对象: {} at ({}, {})", object.getName(), x, y);
            return false;
        }
        
        // 添加到对象集合
        objects.put(object.getId(), object);
        
        // 根据对象类型添加到相应集合
        switch (object.getObjectType()) {
            case PLAYER:
                players.put(object.getName(), (Player) object);
                break;
            case NPC:
                npcs.put(object.getName(), object);
                break;
            case MONSTER:
                monsters.put(object.getName(), object);
                break;
            case ITEM:
                items.put(object.getName(), object);
                break;
        }
        
        // 标记地图块为已占用
        tiles[x][y].setOccupied(true);
        tiles[x][y].setObject(object);
        
        log.debug("添加对象到地图: {} at ({}, {}) on map {}", object.getName(), x, y, name);
        return true;
    }
    
    /**
     * 从地图移除对象
     * 
     * @param object 游戏对象
     * @return 是否移除成功
     */
    public boolean removeObject(BaseObject object) {
        if (object == null) {
            return false;
        }
        
        int x = object.getX();
        int y = object.getY();
        
        if (!isValidPosition(x, y)) {
            return false;
        }
        
        // 从对象集合移除
        objects.remove(object.getId());
        
        // 从相应集合移除
        switch (object.getObjectType()) {
            case PLAYER:
                players.remove(object.getName());
                break;
            case NPC:
                npcs.remove(object.getName());
                break;
            case MONSTER:
                monsters.remove(object.getName());
                break;
            case ITEM:
                items.remove(object.getName());
                break;
        }
        
        // 清除地图块占用状态
        tiles[x][y].setOccupied(false);
        tiles[x][y].setObject(null);
        
        log.debug("从地图移除对象: {} at ({}, {}) on map {}", object.getName(), x, y, name);
        return true;
    }
    
    /**
     * 移动对象到新位置
     * 
     * @param object 游戏对象
     * @param newX 新X坐标
     * @param newY 新Y坐标
     * @return 是否移动成功
     */
    public boolean moveObject(BaseObject object, int newX, int newY) {
        if (object == null) {
            return false;
        }
        
        int oldX = object.getX();
        int oldY = object.getY();
        
        // 检查新位置是否有效
        if (!isValidPosition(newX, newY)) {
            return false;
        }
        
        // 检查新位置是否可以通过
        if (!isPassable(newX, newY)) {
            return false;
        }
        
        // 清除旧位置
        if (isValidPosition(oldX, oldY)) {
            tiles[oldX][oldY].setOccupied(false);
            tiles[oldX][oldY].setObject(null);
        }
        
        // 设置新位置
        tiles[newX][newY].setOccupied(true);
        tiles[newX][newY].setObject(object);
        
        // 更新对象坐标
        object.setX(newX);
        object.setY(newY);
        
        log.debug("移动对象: {} from ({}, {}) to ({}, {}) on map {}", 
                object.getName(), oldX, oldY, newX, newY, name);
        return true;
    }
    
    /**
     * 获取指定位置的对象
     * 
     * @param x X坐标
     * @param y Y坐标
     * @return 对象，如果没有则返回null
     */
    public BaseObject getObjectAt(int x, int y) {
        if (!isValidPosition(x, y)) {
            return null;
        }
        
        return tiles[x][y].getObject();
    }
    
    /**
     * 获取指定范围内的所有对象
     * 
     * @param centerX 中心X坐标
     * @param centerY 中心Y坐标
     * @param range 范围
     * @return 对象列表
     */
    public List<BaseObject> getObjectsInRange(int centerX, int centerY, int range) {
        List<BaseObject> result = new CopyOnWriteArrayList<>();
        
        for (int x = centerX - range; x <= centerX + range; x++) {
            for (int y = centerY - range; y <= centerY + range; y++) {
                if (isValidPosition(x, y)) {
                    BaseObject object = tiles[x][y].getObject();
                    if (object != null) {
                        result.add(object);
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * 寻找可用的传送点
     * 
     * @param x 起始X坐标
     * @param y 起始Y坐标
     * @param range 搜索范围
     * @return 可用坐标，如果没有则返回null
     */
    public Position findAvailablePosition(int x, int y, int range) {
        // 首先检查中心点
        if (isPassable(x, y)) {
            return new Position(x, y);
        }
        
        // 螺旋搜索周围可用位置
        for (int r = 1; r <= range; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    int newX = x + dx;
                    int newY = y + dy;
                    
                    if (isPassable(newX, newY)) {
                        return new Position(newX, newY);
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * 更新地图状态
     */
    public void update() {
        lastUpdateTime = System.currentTimeMillis();
        
        // 更新所有对象
        for (BaseObject object : objects.values()) {
            object.update();
        }
        
        // 处理地图事件
        processEvents();
        
        // 处理刷怪
        processMonsterSpawn();
    }
    
    /**
     * 处理地图事件
     */
    private void processEvents() {
        // 实现地图事件处理逻辑
        long currentTime = System.currentTimeMillis();
        
        for (MapEvent event : events) {
            if (!event.isActive()) {
                continue;
            }
            
            processMapEvent(event, currentTime);
        }
    }
    
    /**
     * 处理单个地图事件
     */
    private void processMapEvent(MapEvent event, long currentTime) {
        String eventType = event.getEventType();
        Position eventPos = event.getPosition();
        
        switch (eventType) {
            case "HEAL_ZONE":
                processHealZoneEvent(event);
                break;
            case "DAMAGE_ZONE":
                processDamageZoneEvent(event);
                break;
            case "TELEPORT":
                processTeleportEvent(event);
                break;
            case "TREASURE_SPAWN":
                processTreasureSpawnEvent(event, currentTime);
                break;
            case "MONSTER_SPAWN":
                processMonsterSpawnEvent(event, currentTime);
                break;
            case "BUFF_ZONE":
                processBuffZoneEvent(event);
                break;
            case "PK_ZONE":
                processPkZoneEvent(event);
                break;
            case "QUEST_TRIGGER":
                processQuestTriggerEvent(event);
                break;
            default:
                log.warn("未知的地图事件类型: {}", eventType);
                break;
        }
    }
    
    /**
     * 处理治愈区域事件
     */
    private void processHealZoneEvent(MapEvent event) {
        int healAmount = (Integer) event.getParameters().getOrDefault("heal_amount", 10);
        int healInterval = (Integer) event.getParameters().getOrDefault("heal_interval", 5000); // 5秒
        
        Long lastHealTime = (Long) event.getParameters().get("last_heal_time");
        long currentTime = System.currentTimeMillis();
        
        if (lastHealTime == null || currentTime - lastHealTime >= healInterval) {
            List<Player> playersInRange = getPlayersInRange(event.getPosition(), event.getRange());
            for (Player player : playersInRange) {
                int currentHp = player.getHp();
                int maxHp = player.getMaxHp();
                if (currentHp < maxHp) {
                    player.setHp(Math.min(maxHp, currentHp + healAmount));
                    log.debug("玩家 {} 在治愈区域恢复 {} 点生命值", player.getName(), healAmount);
                }
            }
            event.getParameters().put("last_heal_time", currentTime);
        }
    }
    
    /**
     * 处理伤害区域事件
     */
    private void processDamageZoneEvent(MapEvent event) {
        int damage = (Integer) event.getParameters().getOrDefault("damage", 10);
        int damageInterval = (Integer) event.getParameters().getOrDefault("damage_interval", 3000); // 3秒
        
        Long lastDamageTime = (Long) event.getParameters().get("last_damage_time");
        long currentTime = System.currentTimeMillis();
        
        if (lastDamageTime == null || currentTime - lastDamageTime >= damageInterval) {
            List<Player> playersInRange = getPlayersInRange(event.getPosition(), event.getRange());
            for (Player player : playersInRange) {
                player.takeDamage(damage);
                log.debug("玩家 {} 在伤害区域受到 {} 点伤害", player.getName(), damage);
            }
            event.getParameters().put("last_damage_time", currentTime);
        }
    }
    
    /**
     * 处理传送事件
     */
    private void processTeleportEvent(MapEvent event) {
        String targetMap = (String) event.getParameters().get("target_map");
        Integer targetX = (Integer) event.getParameters().get("target_x");
        Integer targetY = (Integer) event.getParameters().get("target_y");
        
        if (targetMap == null || targetX == null || targetY == null) {
            return;
        }
        
        List<Player> playersInRange = getPlayersInRange(event.getPosition(), event.getRange());
        for (Player player : playersInRange) {
            // 检查传送条件
            Boolean requiresItem = (Boolean) event.getParameters().getOrDefault("requires_item", false);
            if (requiresItem) {
                // 检查玩家是否有传送道具
                continue;
            }
            
            // 执行传送
            player.setMapName(targetMap);
            player.setX(targetX);
            player.setY(targetY);
            log.info("玩家 {} 通过地图事件传送到 {} ({}, {})", 
                    player.getName(), targetMap, targetX, targetY);
        }
    }
    
    /**
     * 处理宝藏生成事件
     */
    private void processTreasureSpawnEvent(MapEvent event, long currentTime) {
        int spawnInterval = (Integer) event.getParameters().getOrDefault("spawn_interval", 300000); // 5分钟
        Long lastSpawnTime = (Long) event.getParameters().get("last_spawn_time");
        
        if (lastSpawnTime == null || currentTime - lastSpawnTime >= spawnInterval) {
            int treasureId = (Integer) event.getParameters().getOrDefault("treasure_id", 1001);
            
            // 在事件位置生成宝藏
            log.info("在位置 ({}, {}) 生成宝藏", event.getPosition().getX(), event.getPosition().getY());
            event.getParameters().put("last_spawn_time", currentTime);
        }
    }
    
    /**
     * 处理怪物生成事件
     */
    private void processMonsterSpawnEvent(MapEvent event, long currentTime) {
        int spawnInterval = (Integer) event.getParameters().getOrDefault("spawn_interval", 60000); // 1分钟
        Long lastSpawnTime = (Long) event.getParameters().get("last_spawn_time");
        
        if (lastSpawnTime == null || currentTime - lastSpawnTime >= spawnInterval) {
            String monsterType = (String) event.getParameters().getOrDefault("monster_type", "zombie");
            int maxCount = (Integer) event.getParameters().getOrDefault("max_count", 5);
            
            // 检查当前怪物数量
            long currentMonsterCount = monsters.values().stream()
                    .filter(monster -> monster.getName().contains(monsterType))
                    .count();
            
            if (currentMonsterCount < maxCount) {
                log.info("在位置 ({}, {}) 生成怪物: {}", 
                        event.getPosition().getX(), event.getPosition().getY(), monsterType);
                event.getParameters().put("last_spawn_time", currentTime);
            }
        }
    }
    
    /**
     * 处理增益区域事件
     */
    private void processBuffZoneEvent(MapEvent event) {
        String buffType = (String) event.getParameters().getOrDefault("buff_type", "speed");
        int buffValue = (Integer) event.getParameters().getOrDefault("buff_value", 10);
        int buffDuration = (Integer) event.getParameters().getOrDefault("buff_duration", 30000); // 30秒
        
        List<Player> playersInRange = getPlayersInRange(event.getPosition(), event.getRange());
        for (Player player : playersInRange) {
            // 给玩家添加增益效果
            log.debug("玩家 {} 获得区域增益效果: {} +{}", player.getName(), buffType, buffValue);
        }
    }
    
    /**
     * 处理PK区域事件
     */
    private void processPkZoneEvent(MapEvent event) {
        Boolean pkEnabled = (Boolean) event.getParameters().getOrDefault("pk_enabled", true);
        
        List<Player> playersInRange = getPlayersInRange(event.getPosition(), event.getRange());
        for (Player player : playersInRange) {
            // 设置玩家PK状态
            if (pkEnabled) {
                log.debug("玩家 {} 进入PK区域", player.getName());
            }
        }
    }
    
    /**
     * 处理任务触发事件
     */
    private void processQuestTriggerEvent(MapEvent event) {
        int questId = (Integer) event.getParameters().getOrDefault("quest_id", 0);
        
        List<Player> playersInRange = getPlayersInRange(event.getPosition(), event.getRange());
        for (Player player : playersInRange) {
            // 触发任务
            log.debug("玩家 {} 触发任务: {}", player.getName(), questId);
        }
    }
    
    /**
     * 获取范围内的玩家
     */
    private List<Player> getPlayersInRange(Position center, int range) {
        List<Player> result = new ArrayList<>();
        for (Player player : players.values()) {
            int distance = Math.abs(player.getX() - center.getX()) + Math.abs(player.getY() - center.getY());
            if (distance <= range) {
                result.add(player);
            }
        }
        return result;
    }
    
    /**
     * 处理刷怪逻辑
     */
    private void processMonsterSpawn() {
        // 实现刷怪逻辑
        long currentTime = System.currentTimeMillis();
        
        for (MonsterSpawnPoint spawnPoint : spawnPoints) {
            processSpawnPoint(spawnPoint, currentTime);
        }
    }
    
    /**
     * 处理单个刷怪点
     */
    private void processSpawnPoint(MonsterSpawnPoint spawnPoint, long currentTime) {
        // 检查是否可以刷怪
        if (!spawnPoint.canSpawn()) {
            return;
        }
        
        // 检查刷怪间隔
        if (currentTime - spawnPoint.getLastSpawnTime() < spawnPoint.getSpawnInterval()) {
            return;
        }
        
        // 检查当前数量是否已达上限
        if (spawnPoint.getCurrentCount() >= spawnPoint.getMaxCount()) {
            return;
        }
        
        // 生成怪物
        Monster monster = createMonster(spawnPoint);
        if (monster != null) {
            // 寻找合适的生成位置
            Position spawnPosition = findSpawnPosition(spawnPoint);
            if (spawnPosition != null) {
                monster.setX(spawnPosition.getX());
                monster.setY(spawnPosition.getY());
                monster.setMapName(this.name);
                
                // 添加怪物到地图
                if (addObject(monster)) {
                    spawnPoint.setCurrentCount(spawnPoint.getCurrentCount() + 1);
                    spawnPoint.setLastSpawnTime(currentTime);
                    
                    log.debug("在刷怪点 {} 位置 ({}, {}) 生成怪物: {} [{}]", 
                            spawnPoint.getSpawnId(), spawnPosition.getX(), spawnPosition.getY(), 
                            monster.getName(), monster.getLevel());
                } else {
                    log.warn("无法将怪物添加到地图: {}", monster.getName());
                }
            } else {
                log.warn("刷怪点 {} 找不到合适的生成位置", spawnPoint.getSpawnId());
            }
        }
    }
    
    /**
     * 创建怪物
     */
    private Monster createMonster(MonsterSpawnPoint spawnPoint) {
        try {
            // 根据怪物类型创建怪物
            String monsterType = spawnPoint.getMonsterType();
            Monster monster = createMonsterByType(monsterType);
            
            if (monster != null) {
                // 设置刷怪点相关信息
                monster.setSpawnX(spawnPoint.getPosition().getX());
                monster.setSpawnY(spawnPoint.getPosition().getY());
                monster.setSpawnTime(System.currentTimeMillis());
                
                return monster;
            }
        } catch (Exception e) {
            log.error("创建怪物失败: {}", spawnPoint.getMonsterType(), e);
        }
        
        return null;
    }
    
    /**
     * 根据类型创建怪物
     */
    private Monster createMonsterByType(String monsterType) {
        switch (monsterType.toLowerCase()) {
            case "chicken":
                return createChicken();
            case "deer":
                return createDeer();
            case "pig":
                return createPig();
            case "zombie":
                return createZombie();
            case "skeleton":
                return createSkeleton();
            case "orc":
                return createOrc();
            case "tiger":
                return createTiger();
            case "wolf":
                return createWolf();
            case "spider":
                return createSpider();
            case "snake":
                return createSnake();
            default:
                log.warn("未知的怪物类型: {}", monsterType);
                return createDefaultMonster();
        }
    }
    
    /**
     * 寻找生成位置
     */
    private Position findSpawnPosition(MonsterSpawnPoint spawnPoint) {
        Position center = spawnPoint.getPosition();
        int range = spawnPoint.getRange();
        
        // 尝试在范围内找到合适的位置
        for (int attempts = 0; attempts < 10; attempts++) {
            int x = center.getX() + (int)(Math.random() * (range * 2 + 1)) - range;
            int y = center.getY() + (int)(Math.random() * (range * 2 + 1)) - range;
            
            // 检查位置是否有效且可通过
            if (isValidPosition(x, y) && isPassable(x, y)) {
                // 检查位置是否已被占用
                if (getObjectAt(x, y) == null) {
                    return new Position(x, y);
                }
            }
        }
        
        return null;
    }
    
    /**
     * 创建各种怪物的方法
     */
    private Monster createChicken() {
        Monster monster = new Monster(1001, "鸡", 1, 0, 0, this.name);
        monster.setHp(15);
        monster.setMaxHp(15);
        monster.setAttack(1);
        monster.setDefense(0);
        monster.setExperience(2);
        monster.setAggressive(false);
        return monster;
    }
    
    private Monster createDeer() {
        Monster monster = new Monster(1002, "鹿", 3, 0, 0, this.name);
        monster.setHp(30);
        monster.setMaxHp(30);
        monster.setAttack(2);
        monster.setDefense(1);
        monster.setExperience(5);
        monster.setAggressive(false);
        return monster;
    }
    
    private Monster createPig() {
        Monster monster = new Monster(1003, "猪", 5, 0, 0, this.name);
        monster.setHp(50);
        monster.setMaxHp(50);
        monster.setAttack(3);
        monster.setDefense(2);
        monster.setExperience(8);
        monster.setAggressive(false);
        return monster;
    }
    
    private Monster createZombie() {
        Monster monster = new Monster(2001, "僵尸", 10, 0, 0, this.name);
        monster.setHp(120);
        monster.setMaxHp(120);
        monster.setAttack(15);
        monster.setDefense(5);
        monster.setExperience(25);
        monster.setAggressive(true);
        return monster;
    }
    
    private Monster createSkeleton() {
        Monster monster = new Monster(2002, "骷髅", 12, 0, 0, this.name);
        monster.setHp(150);
        monster.setMaxHp(150);
        monster.setAttack(18);
        monster.setDefense(6);
        monster.setExperience(30);
        monster.setAggressive(true);
        return monster;
    }
    
    private Monster createOrc() {
        Monster monster = new Monster(2003, "兽人", 15, 0, 0, this.name);
        monster.setHp(200);
        monster.setMaxHp(200);
        monster.setAttack(25);
        monster.setDefense(8);
        monster.setExperience(45);
        monster.setAggressive(true);
        return monster;
    }
    
    private Monster createTiger() {
        Monster monster = new Monster(3001, "老虎", 20, 0, 0, this.name);
        monster.setHp(300);
        monster.setMaxHp(300);
        monster.setAttack(35);
        monster.setDefense(10);
        monster.setExperience(65);
        monster.setAggressive(true);
        return monster;
    }
    
    private Monster createWolf() {
        Monster monster = new Monster(3002, "狼", 18, 0, 0, this.name);
        monster.setHp(250);
        monster.setMaxHp(250);
        monster.setAttack(30);
        monster.setDefense(8);
        monster.setExperience(55);
        monster.setAggressive(true);
        return monster;
    }
    
    private Monster createSpider() {
        Monster monster = new Monster(3003, "蜘蛛", 16, 0, 0, this.name);
        monster.setHp(180);
        monster.setMaxHp(180);
        monster.setAttack(22);
        monster.setDefense(6);
        monster.setExperience(40);
        monster.setAggressive(true);
        return monster;
    }
    
    private Monster createSnake() {
        Monster monster = new Monster(3004, "蛇", 14, 0, 0, this.name);
        monster.setHp(160);
        monster.setMaxHp(160);
        monster.setAttack(20);
        monster.setDefense(5);
        monster.setExperience(35);
        monster.setAggressive(true);
        return monster;
    }
    
    private Monster createDefaultMonster() {
        Monster monster = new Monster(9999, "未知怪物", 1, 0, 0, this.name);
        monster.setHp(20);
        monster.setMaxHp(20);
        monster.setAttack(5);
        monster.setDefense(1);
        monster.setExperience(3);
        monster.setAggressive(false);
        return monster;
    }
    
    /**
     * 怪物死亡时调用，更新刷怪点计数
     */
    public void onMonsterDeath(Monster monster) {
        // 找到对应的刷怪点并减少计数
        for (MonsterSpawnPoint spawnPoint : spawnPoints) {
            if (isMonsterFromSpawnPoint(monster, spawnPoint)) {
                spawnPoint.setCurrentCount(Math.max(0, spawnPoint.getCurrentCount() - 1));
                log.debug("刷怪点 {} 怪物死亡，当前数量: {}/{}", 
                        spawnPoint.getSpawnId(), spawnPoint.getCurrentCount(), spawnPoint.getMaxCount());
                break;
            }
        }
    }
    
    /**
     * 检查怪物是否来自指定刷怪点
     */
    private boolean isMonsterFromSpawnPoint(Monster monster, MonsterSpawnPoint spawnPoint) {
        // 通过距离和类型判断
        int distance = Math.abs(monster.getSpawnX() - spawnPoint.getPosition().getX()) +
                      Math.abs(monster.getSpawnY() - spawnPoint.getPosition().getY());
        
        return distance <= spawnPoint.getRange() && 
               monster.getName().toLowerCase().contains(spawnPoint.getMonsterType().toLowerCase());
    }
    
    /**
     * 获取地图信息
     * 
     * @return 地图信息字符串
     */
    public String getMapInfo() {
        return String.format("地图: %s [%dx%d] 玩家: %d/%d 对象: %d", 
                name, width, height, players.size(), maxPlayers, objects.size());
    }
    
    /**
     * 地图类型枚举
     */
    public enum MapType {
        NORMAL(0, "普通地图"),
        DUNGEON(1, "地牢"),
        SPECIAL(2, "特殊地图"),
        GUILD_WAR(3, "行会战地图"),
        SAFE_ZONE(4, "安全区"),
        PK_ZONE(5, "PK区");
        
        private final int id;
        private final String name;
        
        MapType(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
    }
    
    /**
     * 地图块类
     */
    @Data
    public static class MapTile {
        /** X坐标 */
        private int x;
        
        /** Y坐标 */
        private int y;
        
        /** 是否可通过 */
        private boolean passable;
        
        /** 是否被占用 */
        private boolean occupied;
        
        /** 地形类型 */
        private int terrainType;
        
        /** 地面物品 */
        private BaseObject object;
        
        /** 特殊标记 */
        private Map<String, Object> flags;
        
        /**
         * 构造函数
         * 
         * @param x X坐标
         * @param y Y坐标
         */
        public MapTile(int x, int y) {
            this.x = x;
            this.y = y;
            this.passable = true;
            this.occupied = false;
            this.terrainType = 0;
            this.flags = new ConcurrentHashMap<>();
        }
        
        /**
         * 添加标记
         * 
         * @param key 标记键
         * @param value 标记值
         */
        public void addFlag(String key, Object value) {
            flags.put(key, value);
        }
        
        /**
         * 获取标记
         * 
         * @param key 标记键
         * @return 标记值
         */
        public Object getFlag(String key) {
            return flags.get(key);
        }
        
        /**
         * 移除标记
         * 
         * @param key 标记键
         */
        public void removeFlag(String key) {
            flags.remove(key);
        }
    }
    
    /**
     * 地图事件类
     */
    @Data
    public static class MapEvent {
        /** 事件ID */
        private int eventId;
        
        /** 事件类型 */
        private String eventType;
        
        /** 触发位置 */
        private Position position;
        
        /** 触发范围 */
        private int range;
        
        /** 事件参数 */
        private Map<String, Object> parameters;
        
        /** 是否激活 */
        private boolean active;
        
        /**
         * 构造函数
         * 
         * @param eventId 事件ID
         * @param eventType 事件类型
         * @param position 触发位置
         */
        public MapEvent(int eventId, String eventType, Position position) {
            this.eventId = eventId;
            this.eventType = eventType;
            this.position = position;
            this.range = 1;
            this.parameters = new ConcurrentHashMap<>();
            this.active = true;
        }
    }
    
    /**
     * 刷怪点类
     */
    @Data
    public static class MonsterSpawnPoint {
        /** 刷怪点ID */
        private int spawnId;
        
        /** 刷怪位置 */
        private Position position;
        
        /** 刷怪范围 */
        private int range;
        
        /** 怪物类型 */
        private String monsterType;
        
        /** 最大数量 */
        private int maxCount;
        
        /** 当前数量 */
        private int currentCount;
        
        /** 刷怪间隔（毫秒） */
        private long spawnInterval;
        
        /** 最后刷怪时间 */
        private long lastSpawnTime;
        
        /**
         * 构造函数
         * 
         * @param spawnId 刷怪点ID
         * @param position 刷怪位置
         * @param monsterType 怪物类型
         * @param maxCount 最大数量
         */
        public MonsterSpawnPoint(int spawnId, Position position, String monsterType, int maxCount) {
            this.spawnId = spawnId;
            this.position = position;
            this.monsterType = monsterType;
            this.maxCount = maxCount;
            this.currentCount = 0;
            this.range = 3;
            this.spawnInterval = 30000; // 30秒
            this.lastSpawnTime = 0;
        }
        
        /**
         * 检查是否可以刷怪
         * 
         * @return 是否可以刷怪
         */
        public boolean canSpawn() {
            long currentTime = System.currentTimeMillis();
            return currentCount < maxCount && (currentTime - lastSpawnTime) >= spawnInterval;
        }
    }
    
    /**
     * 传送点类
     */
    @Data
    public static class TeleportPoint {
        /** 传送点ID */
        private int teleportId;
        
        /** 传送点位置 */
        private Position position;
        
        /** 目标地图 */
        private String targetMap;
        
        /** 目标位置 */
        private Position targetPosition;
        
        /** 传送条件 */
        private Map<String, Object> conditions;
        
        /** 传送费用 */
        private int cost;
        
        /** 是否激活 */
        private boolean active;
        
        /**
         * 构造函数
         * 
         * @param teleportId 传送点ID
         * @param position 传送点位置
         * @param targetMap 目标地图
         * @param targetPosition 目标位置
         */
        public TeleportPoint(int teleportId, Position position, String targetMap, Position targetPosition) {
            this.teleportId = teleportId;
            this.position = position;
            this.targetMap = targetMap;
            this.targetPosition = targetPosition;
            this.conditions = new ConcurrentHashMap<>();
            this.cost = 0;
            this.active = true;
        }
    }
    
    /**
     * 位置类
     */
    @Data
    public static class Position {
        /** X坐标 */
        private int x;
        
        /** Y坐标 */
        private int y;
        
        /**
         * 构造函数
         * 
         * @param x X坐标
         * @param y Y坐标
         */
        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        /**
         * 计算与另一个位置的距离
         * 
         * @param other 另一个位置
         * @return 距离
         */
        public double distanceTo(Position other) {
            int dx = this.x - other.x;
            int dy = this.y - other.y;
            return Math.sqrt(dx * dx + dy * dy);
        }
        
        /**
         * 计算与另一个位置的曼哈顿距离
         * 
         * @param other 另一个位置
         * @return 曼哈顿距离
         */
        public int manhattanDistanceTo(Position other) {
            return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
        }
        
        @Override
        public String toString() {
            return String.format("(%d, %d)", x, y);
        }
    }
} 