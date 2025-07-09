package com.mir2.core.model;

import com.mir2.core.enums.Job;
import com.mir2.core.enums.GameObjectType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 怪物类
 * 
 * <p>代表游戏中的怪物，继承自BaseObject。
 * 对应原M2Engine中的Monster对象。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class Monster extends BaseObject {
    
    /** 怪物模板ID */
    private int templateId;
    
    /** 怪物等级 */
    private int level;
    
    /** 当前生命值 */
    private int hp;
    
    /** 最大生命值 */
    private int maxHp;
    
    /** 当前魔法值 */
    private int mp;
    
    /** 最大魔法值 */
    private int maxMp;
    
    /** 攻击力 */
    private int attack;
    
    /** 防御力 */
    private int defense;
    
    /** 魔法攻击力 */
    private int magicAttack;
    
    /** 魔法防御力 */
    private int magicDefense;
    
    /** 准确度 */
    private int accuracy;
    
    /** 敏捷度 */
    private int agility;
    
    /** 移动速度 */
    private int moveSpeed;
    
    /** 攻击速度 */
    private int attackSpeed;
    
    /** 视野范围 */
    private int viewRange;
    
    /** 攻击范围 */
    private int attackRange;
    
    /** 追击范围 */
    private int chaseRange;
    
    /** 经验值 */
    private int experience;
    
    /** 怪物类型 */
    private MonsterType monsterType;
    
    /** 怪物种族 */
    private MonsterRace race;
    
    /** 怪物AI类型 */
    private AIType aiType;
    
    /** 刷新点X坐标 */
    private int spawnX;
    
    /** 刷新点Y坐标 */
    private int spawnY;
    
    /** 刷新时间 */
    private long spawnTime;
    
    /** 死亡时间 */
    private long deathTime;
    
    /** 复活时间间隔 */
    private long respawnInterval;
    
    /** 是否BOSS */
    private boolean boss;
    
    /** 是否主动攻击 */
    private boolean aggressive;
    
    /** 当前目标 */
    private BaseObject target;
    
    /** 仇恨列表 */
    private Map<String, Integer> hatredList;
    
    /** 巡逻路径 */
    private List<Position> patrolPath;
    
    /** 当前巡逻点索引 */
    private int currentPatrolIndex;
    
    /** 掉落物品列表 */
    private List<DropItem> dropItems;
    
    /** 技能列表 */
    private List<MonsterSkill> skills;
    
    /** 状态效果 */
    private Map<String, StatusEffect> statusEffects;
    
    /** 最后攻击时间 */
    private long lastAttackTime;
    
    /** 最后移动时间 */
    private long lastMoveTime;
    
    /** 最后受伤时间 */
    private long lastDamageTime;
    
    /** AI更新时间 */
    private long lastAIUpdateTime;
    
    /**
     * 构造函数
     * 
     * @param templateId 怪物模板ID
     * @param name 怪物名称
     * @param level 怪物等级
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param mapName 地图名称
     */
    public Monster(int templateId, String name, int level, int x, int y, String mapName) {
        super(name, GameObjectType.MONSTER, x, y, mapName);
        this.templateId = templateId;
        this.level = level;
        this.spawnX = x;
        this.spawnY = y;
        this.spawnTime = System.currentTimeMillis();
        this.respawnInterval = 30000; // 默认30秒复活
        this.boss = false;
        this.aggressive = true;
        this.hatredList = new ConcurrentHashMap<>();
        this.patrolPath = new CopyOnWriteArrayList<>();
        this.currentPatrolIndex = 0;
        this.dropItems = new CopyOnWriteArrayList<>();
        this.skills = new CopyOnWriteArrayList<>();
        this.statusEffects = new ConcurrentHashMap<>();
        this.lastAIUpdateTime = System.currentTimeMillis();
        
        // 初始化属性
        initializeAttributes();
    }
    
    /**
     * 初始化怪物属性
     */
    private void initializeAttributes() {
        // 根据等级计算基础属性
        this.maxHp = 100 + level * 50;
        this.maxMp = 50 + level * 20;
        this.attack = 10 + level * 5;
        this.defense = 5 + level * 3;
        this.magicAttack = 8 + level * 4;
        this.magicDefense = 6 + level * 3;
        this.accuracy = level * 2;
        this.agility = level;
        this.moveSpeed = 800; // 移动间隔毫秒
        this.attackSpeed = 1500; // 攻击间隔毫秒
        this.viewRange = 6;
        this.attackRange = 1;
        this.chaseRange = 10;
        this.experience = level * 10;
        
        // 设置当前生命值和魔法值
        this.hp = this.maxHp;
        this.mp = this.maxMp;
        
        log.debug("初始化怪物属性: {} [等级: {}, HP: {}, 攻击: {}]", 
                getName(), level, maxHp, attack);
    }
    
    /**
     * 受到伤害
     * 
     * @param damage 伤害值
     * @param attacker 攻击者
     * @return 实际伤害值
     */
    public int takeDamage(int damage, BaseObject attacker) {
        if (isDead()) {
            return 0;
        }
        
        // 计算实际伤害
        int actualDamage = Math.max(1, damage - this.defense);
        this.hp = Math.max(0, this.hp - actualDamage);
        this.lastDamageTime = System.currentTimeMillis();
        
        // 添加仇恨值
        if (attacker != null) {
            addHatred(attacker.getName(), actualDamage);
        }
        
        log.debug("怪物 {} 受到伤害: {} -> HP: {}/{}", getName(), actualDamage, hp, maxHp);
        
        // 检查是否死亡
        if (isDead()) {
            die(attacker);
        }
        
        return actualDamage;
    }
    
    /**
     * 攻击目标
     * 
     * @param target 目标
     * @return 是否攻击成功
     */
    public boolean attackTarget(BaseObject target) {
        if (target == null || isDead()) {
            return false;
        }
        
        // 检查攻击冷却
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime < attackSpeed) {
            return false;
        }
        
        // 检查攻击距离
        if (distanceTo(target) > attackRange) {
            return false;
        }
        
        this.lastAttackTime = currentTime;
        
        // 计算伤害
        int damage = calculateDamage(target);
        
        // 应用伤害
        if (target instanceof Player) {
            ((Player) target).takeDamage(damage);
        } else if (target instanceof Monster) {
            ((Monster) target).takeDamage(damage, this);
        }
        
        log.debug("怪物 {} 攻击 {} 造成 {} 点伤害", getName(), target.getName(), damage);
        return true;
    }
    
    /**
     * 计算对目标的伤害
     * 
     * @param target 目标
     * @return 伤害值
     */
    private int calculateDamage(BaseObject target) {
        // 基础攻击力 + 随机值
        int baseDamage = this.attack + (int)(Math.random() * this.attack * 0.3);
        
        // 根据目标防御力、等级差异等因素调整伤害
        if (target instanceof Player) {
            Player player = (Player) target;
            
            // 计算等级差异影响
            int levelDiff = this.level - player.getLevel();
            double levelModifier = 1.0 + (levelDiff * 0.05); // 每级差5%伤害修正
            baseDamage = (int)(baseDamage * levelModifier);
            
            // 计算防御力减免
            int defense = player.getAbility().getDefense();
            double defenseReduction = defense / (defense + 100.0); // 防御公式
            int finalDamage = (int)(baseDamage * (1 - defenseReduction));
            
            // 检查幸运值影响
            int luck = player.getAbility().getLucky();
            if (Math.random() * 100 < luck) {
                finalDamage = (int)(finalDamage * 0.7); // 幸运减伤30%
            }
            
            log.debug("怪物 {} 对玩家 {} 造成伤害: {} (基础: {}, 等级修正: {}, 防御减免后: {}, 幸运减免: {})",
                    getName(), player.getName(), Math.max(1, finalDamage), baseDamage,
                    (int)(baseDamage * levelModifier), (int)(baseDamage * (1 - defenseReduction)), finalDamage);
            
            return Math.max(1, finalDamage);
        } else if (target instanceof Monster) {
            Monster monster = (Monster) target;
            
            // 计算等级差异影响
            int levelDiff = this.level - monster.level;
            double levelModifier = 1.0 + (levelDiff * 0.05);
            baseDamage = (int)(baseDamage * levelModifier);
            
            // 计算防御力减免
            int defense = monster.defense;
            double defenseReduction = defense / (defense + 100.0);
            int finalDamage = (int)(baseDamage * (1 - defenseReduction));
            
            return Math.max(1, finalDamage);
        }
        
        return Math.max(1, baseDamage);
    }
    
    /**
     * 添加仇恨值
     * 
     * @param targetName 目标名称
     * @param hatred 仇恨值
     */
    public void addHatred(String targetName, int hatred) {
        if (targetName != null) {
            hatredList.merge(targetName, hatred, Integer::sum);
            log.debug("怪物 {} 对 {} 增加仇恨值: {}", getName(), targetName, hatred);
        }
    }
    
    /**
     * 获取最高仇恨目标
     * 
     * @return 目标名称
     */
    public String getTopHatredTarget() {
        return hatredList.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    /**
     * 清除仇恨值
     * 
     * @param targetName 目标名称
     */
    public void clearHatred(String targetName) {
        hatredList.remove(targetName);
    }
    
    /**
     * 清除所有仇恨值
     */
    public void clearAllHatred() {
        hatredList.clear();
        this.target = null;
    }
    
    /**
     * 移动到指定位置
     * 
     * @param newX 新X坐标
     * @param newY 新Y坐标
     * @return 是否移动成功
     */
    public boolean moveTo(int newX, int newY) {
        if (isDead()) {
            return false;
        }
        
        // 检查移动冷却
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMoveTime < moveSpeed) {
            return false;
        }
        
        this.lastMoveTime = currentTime;
        setPosition(newX, newY);
        
        log.debug("怪物 {} 移动到: ({}, {})", getName(), newX, newY);
        return true;
    }
    
    /**
     * 返回刷新点
     * 
     * @return 是否返回成功
     */
    public boolean returnToSpawn() {
        if (getX() == spawnX && getY() == spawnY) {
            return true;
        }
        
        return moveTo(spawnX, spawnY);
    }
    
    /**
     * 检查是否死亡
     * 
     * @return 是否死亡
     */
    public boolean isDead() {
        return hp <= 0;
    }
    
    /**
     * 死亡处理
     * 
     * @param killer 杀死者
     */
    public void die(BaseObject killer) {
        if (isDead() && deathTime == 0) {
            this.deathTime = System.currentTimeMillis();
            clearAllHatred();
            
            log.info("怪物 {} 死亡，杀死者: {}", getName(), killer != null ? killer.getName() : "未知");
            
            // 掉落物品
            dropItems();
            
            // 给予经验
            if (killer instanceof Player) {
                ((Player) killer).addExperience(this.experience);
            }
        }
    }
    
    /**
     * 复活处理
     */
    public void respawn() {
        this.hp = this.maxHp;
        this.mp = this.maxMp;
        this.deathTime = 0;
        this.spawnTime = System.currentTimeMillis();
        setPosition(spawnX, spawnY);
        clearAllHatred();
        
        log.info("怪物 {} 复活", getName());
    }
    
    /**
     * 检查是否可以复活
     * 
     * @return 是否可以复活
     */
    public boolean canRespawn() {
        return isDead() && deathTime > 0 && 
               (System.currentTimeMillis() - deathTime) >= respawnInterval;
    }
    
    /**
     * 掉落物品
     */
    private void dropItems() {
        for (DropItem dropItem : dropItems) {
            if (Math.random() * 100 < dropItem.getDropRate()) {
                // 在地图上创建掉落物品
                createDroppedItem(dropItem);
                log.debug("怪物 {} 掉落物品: {} 数量: {}", 
                        getName(), dropItem.getItemId(), dropItem.getQuantity());
            }
        }
    }
    
    /**
     * 创建掉落物品
     */
    private void createDroppedItem(DropItem dropItem) {
        // 创建掉落物品对象
        DroppedItem droppedItem = new DroppedItem();
        droppedItem.setItemId(dropItem.getItemId());
        droppedItem.setQuantity(dropItem.getQuantity());
        droppedItem.setX(getX());
        droppedItem.setY(getY());
        droppedItem.setMapName(getMapName());
        droppedItem.setDropTime(System.currentTimeMillis());
        droppedItem.setExpireTime(droppedItem.getDropTime() + 300000); // 5分钟后消失
        droppedItem.setOwner(null); // 公开掉落
        
        // 如果死亡时有攻击者，设置归属
        if (target instanceof Player) {
            droppedItem.setOwner(target.getName());
            droppedItem.setProtectTime(droppedItem.getDropTime() + 30000); // 30秒保护时间
        }
        
        // 寻找合适的掉落位置
        Position dropPosition = findDropPosition();
        droppedItem.setX(dropPosition.getX());
        droppedItem.setY(dropPosition.getY());
        
        // 添加到地图管理器
        // mapManager.addDroppedItem(getMapName(), droppedItem);
        
        log.info("在位置 ({}, {}) 创建掉落物品: {} x{}", 
                dropPosition.getX(), dropPosition.getY(), dropItem.getItemId(), dropItem.getQuantity());
    }
    
    /**
     * 寻找掉落位置
     */
    private Position findDropPosition() {
        // 在死亡位置周围寻找合适的掉落位置
        int attempts = 10;
        while (attempts > 0) {
            int dropX = getX() + (int)(Math.random() * 6) - 3; // ±3范围
            int dropY = getY() + (int)(Math.random() * 6) - 3;
            
            // 检查位置是否有效（这里简化处理）
            if (dropX >= 0 && dropY >= 0) {
                return new Position(dropX, dropY);
            }
            attempts--;
        }
        
        // 如果找不到合适位置，就在死亡位置掉落
        return new Position(getX(), getY());
    }
    
    /**
     * 掉落物品类
     */
    @Data
    public static class DroppedItem {
        private int itemId;
        private int quantity;
        private int x;
        private int y;
        private String mapName;
        private long dropTime;
        private long expireTime;
        private long protectTime;
        private String owner; // 归属玩家
        
        /**
         * 检查是否已过期
         */
        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
        
        /**
         * 检查是否在保护时间内
         */
        public boolean isProtected() {
            return protectTime > 0 && System.currentTimeMillis() < protectTime;
        }
        
        /**
         * 检查玩家是否可以拾取
         */
        public boolean canPickup(String playerName) {
            if (isExpired()) {
                return false;
            }
            
            // 如果有归属且在保护时间内，只有归属玩家可以拾取
            if (isProtected() && owner != null && !owner.equals(playerName)) {
                return false;
            }
            
            return true;
        }
    }
    
    /**
     * 添加掉落物品
     * 
     * @param itemId 物品ID
     * @param quantity 数量
     * @param dropRate 掉落率
     */
    public void addDropItem(int itemId, int quantity, double dropRate) {
        dropItems.add(new DropItem(itemId, quantity, dropRate));
    }
    
    /**
     * 怪物类型枚举
     */
    public enum MonsterType {
        NORMAL(0, "普通"),
        ELITE(1, "精英"),
        BOSS(2, "BOSS"),
        GUARD(3, "守卫"),
        PET(4, "宠物"),
        SUMMON(5, "召唤兽");
        
        private final int id;
        private final String name;
        
        MonsterType(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
    }
    
    /**
     * 怪物种族枚举
     */
    public enum MonsterRace {
        HUMAN(0, "人形"),
        BEAST(1, "野兽"),
        UNDEAD(2, "不死族"),
        DEMON(3, "恶魔"),
        ELEMENTAL(4, "元素"),
        DRAGON(5, "龙族");
        
        private final int id;
        private final String name;
        
        MonsterRace(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
    }
    
    /**
     * AI类型枚举
     */
    public enum AIType {
        PASSIVE(0, "被动"),
        AGGRESSIVE(1, "主动"),
        PATROL(2, "巡逻"),
        GUARD(3, "守卫"),
        SMART(4, "智能");
        
        private final int id;
        private final String name;
        
        AIType(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
    }
    
    /**
     * 掉落物品类
     */
    @Data
    public static class DropItem {
        private int itemId;
        private int quantity;
        private double dropRate;
        
        public DropItem(int itemId, int quantity, double dropRate) {
            this.itemId = itemId;
            this.quantity = quantity;
            this.dropRate = dropRate;
        }
    }
    
    /**
     * 怪物技能类
     */
    @Data
    public static class MonsterSkill {
        private int skillId;
        private int level;
        private int cooldown;
        private long lastUseTime;
        private double useRate;
        
        public MonsterSkill(int skillId, int level, int cooldown, double useRate) {
            this.skillId = skillId;
            this.level = level;
            this.cooldown = cooldown;
            this.useRate = useRate;
            this.lastUseTime = 0;
        }
        
        public boolean canUse() {
            return System.currentTimeMillis() - lastUseTime >= cooldown;
        }
        
        public void use() {
            this.lastUseTime = System.currentTimeMillis();
        }
    }
    
    /**
     * 状态效果类
     */
    @Data
    public static class StatusEffect {
        private String name;
        private int duration;
        private long startTime;
        private Map<String, Object> properties;
        
        public StatusEffect(String name, int duration) {
            this.name = name;
            this.duration = duration;
            this.startTime = System.currentTimeMillis();
            this.properties = new ConcurrentHashMap<>();
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - startTime >= duration;
        }
    }
    
    /**
     * 位置类
     */
    @Data
    public static class Position {
        private int x;
        private int y;
        
        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    
    @Override
    public String toString() {
        return String.format("怪物: %s [等级: %d] [HP: %d/%d] [位置: (%d, %d)]",
                getName(), level, hp, maxHp, getX(), getY());
    }
} 