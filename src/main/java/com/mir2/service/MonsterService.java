package com.mir2.service;

import com.mir2.core.model.Monster;
import com.mir2.core.model.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 怪物管理服务
 * 
 * <p>负责怪物的创建、管理、刷新和AI调度等功能。
 * 对应原M2Engine中的MonsterManager模块。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class MonsterService {
    
    /** 怪物模板数据 */
    private final Map<Integer, MonsterTemplate> monsterTemplates = new ConcurrentHashMap<>();
    
    /** 所有怪物实例 */
    private final Map<String, Monster> monsters = new ConcurrentHashMap<>();
    
    /** 按地图分组的怪物 */
    private final Map<String, List<Monster>> monstersByMap = new ConcurrentHashMap<>();
    
    /** 怪物刷新点配置 */
    private final Map<String, List<MonsterSpawn>> monsterSpawns = new ConcurrentHashMap<>();
    
    @Autowired
    private MonsterAIService monsterAIService;
    
    @Autowired
    private MapService mapService;
    
    /**
     * 初始化怪物模板
     */
    public void initializeMonsterTemplates() {
        log.info("开始初始化怪物模板...");
        
        // 新手区怪物
        addMonsterTemplate(1, "鸡", 1, 50, 20, 10, 5, 8, 3, 
                Monster.MonsterType.NORMAL, Monster.MonsterRace.BEAST, Monster.AIType.PASSIVE);
        
        addMonsterTemplate(2, "鹿", 2, 80, 30, 15, 8, 12, 5, 
                Monster.MonsterType.NORMAL, Monster.MonsterRace.BEAST, Monster.AIType.PASSIVE);
        
        addMonsterTemplate(3, "稻草人", 3, 120, 40, 20, 10, 15, 8, 
                Monster.MonsterType.NORMAL, Monster.MonsterRace.HUMAN, Monster.AIType.AGGRESSIVE);
        
        // 比奇城周边怪物
        addMonsterTemplate(10, "半兽人", 5, 200, 60, 30, 15, 25, 12, 
                Monster.MonsterType.NORMAL, Monster.MonsterRace.HUMAN, Monster.AIType.AGGRESSIVE);
        
        addMonsterTemplate(11, "半兽战士", 8, 350, 80, 45, 20, 35, 18, 
                Monster.MonsterType.NORMAL, Monster.MonsterRace.HUMAN, Monster.AIType.AGGRESSIVE);
        
        addMonsterTemplate(12, "森林雪人", 10, 450, 100, 55, 25, 40, 22, 
                Monster.MonsterType.NORMAL, Monster.MonsterRace.ELEMENTAL, Monster.AIType.AGGRESSIVE);
        
        // 毒蛇山谷怪物
        addMonsterTemplate(20, "蛇", 6, 280, 70, 35, 18, 30, 15, 
                Monster.MonsterType.NORMAL, Monster.MonsterRace.BEAST, Monster.AIType.AGGRESSIVE);
        
        addMonsterTemplate(21, "蝎子", 7, 320, 80, 40, 20, 32, 16, 
                Monster.MonsterType.NORMAL, Monster.MonsterRace.BEAST, Monster.AIType.AGGRESSIVE);
        
        addMonsterTemplate(22, "蜘蛛", 9, 400, 90, 50, 22, 38, 20, 
                Monster.MonsterType.NORMAL, Monster.MonsterRace.BEAST, Monster.AIType.AGGRESSIVE);
        
        // 骷髅洞穴怪物
        addMonsterTemplate(30, "骷髅", 12, 600, 120, 70, 30, 50, 25, 
                Monster.MonsterType.NORMAL, Monster.MonsterRace.UNDEAD, Monster.AIType.AGGRESSIVE);
        
        addMonsterTemplate(31, "骷髅战士", 15, 800, 150, 85, 35, 60, 30, 
                Monster.MonsterType.NORMAL, Monster.MonsterRace.UNDEAD, Monster.AIType.AGGRESSIVE);
        
        addMonsterTemplate(32, "骷髅弓箭手", 14, 700, 140, 75, 32, 55, 28, 
                Monster.MonsterType.NORMAL, Monster.MonsterRace.UNDEAD, Monster.AIType.AGGRESSIVE);
        
        // 沃玛寺庙怪物
        addMonsterTemplate(40, "沃玛战士", 20, 1200, 200, 120, 50, 80, 40, 
                Monster.MonsterType.ELITE, Monster.MonsterRace.DEMON, Monster.AIType.AGGRESSIVE);
        
        addMonsterTemplate(41, "沃玛战将", 25, 1800, 250, 150, 60, 100, 50, 
                Monster.MonsterType.ELITE, Monster.MonsterRace.DEMON, Monster.AIType.SMART);
        
        addMonsterTemplate(42, "沃玛教主", 35, 5000, 500, 300, 120, 200, 100, 
                Monster.MonsterType.BOSS, Monster.MonsterRace.DEMON, Monster.AIType.SMART);
        
        // 祖玛寺庙怪物
        addMonsterTemplate(50, "祖玛卫士", 30, 2500, 300, 200, 80, 150, 75, 
                Monster.MonsterType.ELITE, Monster.MonsterRace.DEMON, Monster.AIType.AGGRESSIVE);
        
        addMonsterTemplate(51, "祖玛雕像", 32, 2800, 350, 220, 90, 160, 80, 
                Monster.MonsterType.ELITE, Monster.MonsterRace.DEMON, Monster.AIType.GUARD);
        
        addMonsterTemplate(52, "祖玛教主", 40, 8000, 800, 500, 200, 400, 200, 
                Monster.MonsterType.BOSS, Monster.MonsterRace.DEMON, Monster.AIType.SMART);
        
        // 赤月峡谷怪物
        addMonsterTemplate(60, "双头血魔", 45, 12000, 1000, 700, 300, 500, 250, 
                Monster.MonsterType.BOSS, Monster.MonsterRace.DEMON, Monster.AIType.SMART);
        
        addMonsterTemplate(61, "双头金刚", 50, 15000, 1200, 800, 350, 600, 300, 
                Monster.MonsterType.BOSS, Monster.MonsterRace.DEMON, Monster.AIType.SMART);
        
        addMonsterTemplate(62, "赤月恶魔", 60, 30000, 2000, 1500, 500, 1000, 500, 
                Monster.MonsterType.BOSS, Monster.MonsterRace.DEMON, Monster.AIType.SMART);
        
        log.info("怪物模板初始化完成，共加载 {} 个模板", monsterTemplates.size());
    }
    
    /**
     * 添加怪物模板
     */
    private void addMonsterTemplate(int templateId, String name, int level, int hp, int mp, 
                                   int attack, int defense, int magicAttack, int magicDefense,
                                   Monster.MonsterType type, Monster.MonsterRace race, Monster.AIType aiType) {
        MonsterTemplate template = new MonsterTemplate();
        template.setId(templateId);
        template.setName(name);
        template.setLevel(level);
        template.setHp(hp);
        template.setMp(mp);
        template.setAttack(attack);
        template.setDefense(defense);
        template.setMagicAttack(magicAttack);
        template.setMagicDefense(magicDefense);
        template.setType(type);
        template.setRace(race);
        template.setAiType(aiType);
        template.setExperience(level * 10);
        template.setViewRange(6);
        template.setAttackRange(type == Monster.MonsterType.BOSS ? 3 : 1);
        template.setChaseRange(type == Monster.MonsterType.BOSS ? 15 : 10);
        template.setMoveSpeed(type == Monster.MonsterType.BOSS ? 600 : 800);
        template.setAttackSpeed(type == Monster.MonsterType.BOSS ? 1200 : 1500);
        template.setRespawnInterval(type == Monster.MonsterType.BOSS ? 300000 : 30000); // BOSS 5分钟，普通怪30秒
        
        monsterTemplates.put(templateId, template);
    }
    
    /**
     * 初始化怪物刷新点
     */
    public void initializeMonsterSpawns() {
        log.info("开始初始化怪物刷新点...");
        
        // 新手村刷新点
        addMonsterSpawn("新手村", 1, 330, 330, 1, 60000); // 鸡
        addMonsterSpawn("新手村", 2, 335, 335, 2, 90000); // 鹿
        addMonsterSpawn("新手村", 3, 340, 340, 1, 120000); // 稻草人
        
        // 比奇城野外刷新点
        addMonsterSpawn("比奇城野外", 10, 100, 100, 5, 45000); // 半兽人
        addMonsterSpawn("比奇城野外", 11, 200, 200, 3, 60000); // 半兽战士
        addMonsterSpawn("比奇城野外", 12, 300, 300, 2, 90000); // 森林雪人
        
        // 毒蛇山谷刷新点
        addMonsterSpawn("毒蛇山谷", 20, 150, 150, 8, 40000); // 蛇
        addMonsterSpawn("毒蛇山谷", 21, 250, 250, 6, 50000); // 蝎子
        addMonsterSpawn("毒蛇山谷", 22, 350, 350, 4, 70000); // 蜘蛛
        
        // 骷髅洞穴刷新点
        addMonsterSpawn("骷髅洞穴", 30, 180, 180, 10, 35000); // 骷髅
        addMonsterSpawn("骷髅洞穴", 31, 280, 280, 8, 45000); // 骷髅战士
        addMonsterSpawn("骷髅洞穴", 32, 380, 380, 6, 55000); // 骷髅弓箭手
        
        // 沃玛寺庙刷新点
        addMonsterSpawn("沃玛寺庙", 40, 200, 200, 5, 120000); // 沃玛战士
        addMonsterSpawn("沃玛寺庙", 41, 300, 300, 3, 180000); // 沃玛战将
        addMonsterSpawn("沃玛寺庙", 42, 400, 400, 1, 900000); // 沃玛教主 (15分钟)
        
        // 祖玛寺庙刷新点
        addMonsterSpawn("祖玛寺庙", 50, 250, 250, 4, 180000); // 祖玛卫士
        addMonsterSpawn("祖玛寺庙", 51, 350, 350, 2, 240000); // 祖玛雕像
        addMonsterSpawn("祖玛寺庙", 52, 450, 450, 1, 1800000); // 祖玛教主 (30分钟)
        
        log.info("怪物刷新点初始化完成");
    }
    
    /**
     * 添加怪物刷新点
     */
    private void addMonsterSpawn(String mapName, int templateId, int x, int y, int count, long interval) {
        MonsterSpawn spawn = new MonsterSpawn();
        spawn.setMapName(mapName);
        spawn.setTemplateId(templateId);
        spawn.setX(x);
        spawn.setY(y);
        spawn.setCount(count);
        spawn.setInterval(interval);
        spawn.setRange(5); // 刷新范围
        
        monsterSpawns.computeIfAbsent(mapName, k -> new CopyOnWriteArrayList<>()).add(spawn);
    }
    
    /**
     * 创建怪物
     * 
     * @param templateId 模板ID
     * @param x X坐标
     * @param y Y坐标
     * @param mapName 地图名称
     * @return 怪物实例
     */
    public Monster createMonster(int templateId, int x, int y, String mapName) {
        MonsterTemplate template = monsterTemplates.get(templateId);
        if (template == null) {
            log.error("找不到怪物模板: {}", templateId);
            return null;
        }
        
        String monsterId = UUID.randomUUID().toString();
        Monster monster = new Monster(templateId, template.getName(), template.getLevel(), x, y, mapName);
        
        // 应用模板属性
        applyTemplate(monster, template);
        
        // 添加到管理器
        monsters.put(monsterId, monster);
        monstersByMap.computeIfAbsent(mapName, k -> new CopyOnWriteArrayList<>()).add(monster);
        
        // 添加掉落物品
        addMonsterDrops(monster, template);
        
        log.debug("创建怪物: {} [{}] 位置: ({}, {}) 地图: {}", 
                monster.getName(), monsterId, x, y, mapName);
        
        return monster;
    }
    
    /**
     * 应用模板属性到怪物
     */
    private void applyTemplate(Monster monster, MonsterTemplate template) {
        monster.setMaxHp(template.getHp());
        monster.setHp(template.getHp());
        monster.setMaxMp(template.getMp());
        monster.setMp(template.getMp());
        monster.setAttack(template.getAttack());
        monster.setDefense(template.getDefense());
        monster.setMagicAttack(template.getMagicAttack());
        monster.setMagicDefense(template.getMagicDefense());
        monster.setExperience(template.getExperience());
        monster.setViewRange(template.getViewRange());
        monster.setAttackRange(template.getAttackRange());
        monster.setChaseRange(template.getChaseRange());
        monster.setMoveSpeed(template.getMoveSpeed());
        monster.setAttackSpeed(template.getAttackSpeed());
        monster.setRespawnInterval(template.getRespawnInterval());
        monster.setMonsterType(template.getType());
        monster.setRace(template.getRace());
        monster.setAiType(template.getAiType());
        monster.setBoss(template.getType() == Monster.MonsterType.BOSS);
        monster.setAggressive(template.getAiType() != Monster.AIType.PASSIVE);
    }
    
    /**
     * 添加怪物掉落物品
     */
    private void addMonsterDrops(Monster monster, MonsterTemplate template) {
        // 根据怪物等级和类型添加掉落物品
        int level = template.getLevel();
        
        // 金币掉落
        monster.addDropItem(1, level * 10 + ThreadLocalRandom.current().nextInt(level * 5), 80.0);
        
        // 药品掉落
        if (level <= 10) {
            monster.addDropItem(100, 1, 30.0); // 金疮药
        } else if (level <= 20) {
            monster.addDropItem(101, 1, 25.0); // 强效金疮药
        } else {
            monster.addDropItem(102, 1, 20.0); // 超级金疮药
        }
        
        // 装备掉落
        if (template.getType() == Monster.MonsterType.BOSS) {
            // BOSS掉落高级装备
            monster.addDropItem(1000 + level, 1, 10.0); // 武器
            monster.addDropItem(2000 + level, 1, 15.0); // 防具
            monster.addDropItem(3000 + level, 1, 20.0); // 首饰
        } else if (template.getType() == Monster.MonsterType.ELITE) {
            // 精英怪掉落中级装备
            monster.addDropItem(1000 + level, 1, 5.0); // 武器
            monster.addDropItem(2000 + level, 1, 8.0); // 防具
        } else if (level >= 10) {
            // 普通怪掉落低级装备
            monster.addDropItem(1000 + level, 1, 2.0); // 武器
            monster.addDropItem(2000 + level, 1, 3.0); // 防具
        }
    }
    
    /**
     * 获取怪物
     * 
     * @param monsterId 怪物ID
     * @return 怪物实例
     */
    public Monster getMonster(String monsterId) {
        return monsters.get(monsterId);
    }
    
    /**
     * 获取地图中的怪物
     * 
     * @param mapName 地图名称
     * @return 怪物列表
     */
    public List<Monster> getMonstersInMap(String mapName) {
        return monstersByMap.getOrDefault(mapName, new CopyOnWriteArrayList<>());
    }
    
    /**
     * 获取玩家附近的怪物
     * 
     * @param player 玩家
     * @param range 范围
     * @return 怪物列表
     */
    public List<Monster> getMonstersNearPlayer(Player player, int range) {
        List<Monster> nearbyMonsters = new ArrayList<>();
        List<Monster> mapMonsters = getMonstersInMap(player.getMapName());
        
        for (Monster monster : mapMonsters) {
            if (!monster.isDead() && monster.distanceTo(player) <= range) {
                nearbyMonsters.add(monster);
            }
        }
        
        return nearbyMonsters;
    }
    
    /**
     * 移除怪物
     * 
     * @param monsterId 怪物ID
     */
    public void removeMonster(String monsterId) {
        Monster monster = monsters.remove(monsterId);
        if (monster != null) {
            List<Monster> mapMonsters = monstersByMap.get(monster.getMapName());
            if (mapMonsters != null) {
                mapMonsters.remove(monster);
            }
            log.debug("移除怪物: {} [{}]", monster.getName(), monsterId);
        }
    }
    
    /**
     * 定时刷新怪物
     */
    @Scheduled(fixedDelay = 5000) // 每5秒检查一次
    public void refreshMonsters() {
        for (Map.Entry<String, List<MonsterSpawn>> entry : monsterSpawns.entrySet()) {
            String mapName = entry.getKey();
            List<MonsterSpawn> spawns = entry.getValue();
            
            for (MonsterSpawn spawn : spawns) {
                refreshMonsterSpawn(mapName, spawn);
            }
        }
    }
    
    /**
     * 刷新单个怪物刷新点
     */
    private void refreshMonsterSpawn(String mapName, MonsterSpawn spawn) {
        // 检查当前怪物数量
        List<Monster> mapMonsters = getMonstersInMap(mapName);
        long currentCount = mapMonsters.stream()
                .filter(m -> m.getTemplateId() == spawn.getTemplateId() && !m.isDead())
                .count();
        
        if (currentCount < spawn.getCount()) {
            // 检查刷新间隔
            long currentTime = System.currentTimeMillis();
            if (currentTime - spawn.getLastSpawnTime() >= spawn.getInterval()) {
                // 计算刷新位置
                int x = spawn.getX() + ThreadLocalRandom.current().nextInt(-spawn.getRange(), spawn.getRange() + 1);
                int y = spawn.getY() + ThreadLocalRandom.current().nextInt(-spawn.getRange(), spawn.getRange() + 1);
                
                // 检查位置是否可用
                if (mapService.canMove(mapName, x, y)) {
                    createMonster(spawn.getTemplateId(), x, y, mapName);
                    spawn.setLastSpawnTime(currentTime);
                }
            }
        }
    }
    
    /**
     * 定时处理怪物AI
     */
    @Scheduled(fixedDelay = 1000) // 每秒更新一次
    public void updateMonsterAI() {
        for (Monster monster : monsters.values()) {
            if (!monster.isDead()) {
                monsterAIService.processMonsterAI(monster);
            }
        }
    }
    
    /**
     * 定时处理怪物复活
     */
    @Scheduled(fixedDelay = 10000) // 每10秒检查一次
    public void handleMonsterRespawn() {
        for (Monster monster : monsters.values()) {
            if (monster.canRespawn()) {
                monster.respawn();
            }
        }
    }
    
    /**
     * 清理死亡怪物
     */
    @Scheduled(fixedDelay = 300000) // 每5分钟清理一次
    public void cleanupDeadMonsters() {
        Iterator<Map.Entry<String, Monster>> iterator = monsters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Monster> entry = iterator.next();
            Monster monster = entry.getValue();
            
            // 清理长时间死亡的怪物
            if (monster.isDead() && monster.getDeathTime() > 0 && 
                (System.currentTimeMillis() - monster.getDeathTime()) > 600000) { // 10分钟
                iterator.remove();
                List<Monster> mapMonsters = monstersByMap.get(monster.getMapName());
                if (mapMonsters != null) {
                    mapMonsters.remove(monster);
                }
                log.debug("清理死亡怪物: {}", monster.getName());
            }
        }
    }
    
    /**
     * 获取怪物统计信息
     * 
     * @return 统计信息
     */
    public Map<String, Object> getMonsterStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("总怪物数", monsters.size());
        stats.put("存活怪物数", monsters.values().stream().mapToLong(m -> m.isDead() ? 0 : 1).sum());
        stats.put("死亡怪物数", monsters.values().stream().mapToLong(m -> m.isDead() ? 1 : 0).sum());
        stats.put("怪物模板数", monsterTemplates.size());
        stats.put("刷新点数", monsterSpawns.values().stream().mapToInt(List::size).sum());
        
        return stats;
    }
    
    /**
     * 怪物模板类
     */
    public static class MonsterTemplate {
        private int id;
        private String name;
        private int level;
        private int hp;
        private int mp;
        private int attack;
        private int defense;
        private int magicAttack;
        private int magicDefense;
        private int experience;
        private int viewRange;
        private int attackRange;
        private int chaseRange;
        private int moveSpeed;
        private int attackSpeed;
        private long respawnInterval;
        private Monster.MonsterType type;
        private Monster.MonsterRace race;
        private Monster.AIType aiType;
        
        // getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public int getHp() { return hp; }
        public void setHp(int hp) { this.hp = hp; }
        public int getMp() { return mp; }
        public void setMp(int mp) { this.mp = mp; }
        public int getAttack() { return attack; }
        public void setAttack(int attack) { this.attack = attack; }
        public int getDefense() { return defense; }
        public void setDefense(int defense) { this.defense = defense; }
        public int getMagicAttack() { return magicAttack; }
        public void setMagicAttack(int magicAttack) { this.magicAttack = magicAttack; }
        public int getMagicDefense() { return magicDefense; }
        public void setMagicDefense(int magicDefense) { this.magicDefense = magicDefense; }
        public int getExperience() { return experience; }
        public void setExperience(int experience) { this.experience = experience; }
        public int getViewRange() { return viewRange; }
        public void setViewRange(int viewRange) { this.viewRange = viewRange; }
        public int getAttackRange() { return attackRange; }
        public void setAttackRange(int attackRange) { this.attackRange = attackRange; }
        public int getChaseRange() { return chaseRange; }
        public void setChaseRange(int chaseRange) { this.chaseRange = chaseRange; }
        public int getMoveSpeed() { return moveSpeed; }
        public void setMoveSpeed(int moveSpeed) { this.moveSpeed = moveSpeed; }
        public int getAttackSpeed() { return attackSpeed; }
        public void setAttackSpeed(int attackSpeed) { this.attackSpeed = attackSpeed; }
        public long getRespawnInterval() { return respawnInterval; }
        public void setRespawnInterval(long respawnInterval) { this.respawnInterval = respawnInterval; }
        public Monster.MonsterType getType() { return type; }
        public void setType(Monster.MonsterType type) { this.type = type; }
        public Monster.MonsterRace getRace() { return race; }
        public void setRace(Monster.MonsterRace race) { this.race = race; }
        public Monster.AIType getAiType() { return aiType; }
        public void setAiType(Monster.AIType aiType) { this.aiType = aiType; }
    }
    
    /**
     * 怪物刷新点类
     */
    public static class MonsterSpawn {
        private String mapName;
        private int templateId;
        private int x;
        private int y;
        private int count;
        private long interval;
        private int range;
        private long lastSpawnTime;
        
        // getters and setters
        public String getMapName() { return mapName; }
        public void setMapName(String mapName) { this.mapName = mapName; }
        public int getTemplateId() { return templateId; }
        public void setTemplateId(int templateId) { this.templateId = templateId; }
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public long getInterval() { return interval; }
        public void setInterval(long interval) { this.interval = interval; }
        public int getRange() { return range; }
        public void setRange(int range) { this.range = range; }
        public long getLastSpawnTime() { return lastSpawnTime; }
        public void setLastSpawnTime(long lastSpawnTime) { this.lastSpawnTime = lastSpawnTime; }
    }
} 