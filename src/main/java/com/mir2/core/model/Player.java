package com.mir2.core.model;

import com.mir2.core.enums.Direction;
import com.mir2.core.enums.GameObjectType;
import com.mir2.core.enums.Job;
import com.mir2.core.enums.ObjectState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 玩家对象类
 * 
 * <p>代表游戏中的玩家角色，继承自BaseObject，
 * 包含了玩家特有的属性和行为。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>角色属性管理（等级、经验、属性点等）</li>
 *   <li>装备系统管理</li>
 *   <li>背包物品管理</li>
 *   <li>技能系统管理</li>
 *   <li>社交系统（好友、师徒、结婚等）</li>
 *   <li>游戏货币管理</li>
 *   <li>PK和声望系统</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class Player extends BaseObject {
    
    /** 账号名 */
    private String accountName;
    
    /** 角色职业 */
    private Job job;
    
    /** 性别 (0: 男, 1: 女) */
    private int gender;
    
    /** 发型 */
    private int hair;
    
    /** 等级 */
    private int level;
    
    /** 当前经验值 */
    private long experience;
    
    /** 下级所需经验值 */
    private long nextLevelExp;
    
    /** 基础属性 */
    private PlayerAbility ability;
    
    /** 属性点 */
    private int statPoints;
    
    /** 技能点 */
    private int skillPoints;
    
    /** 游戏金币 */
    private long gold;
    
    /** 游戏点数 */
    private long gamePoint;
    
    /** 游戏钻石 */
    private long gameDiamond;
    
    /** PK值 */
    private int pkValue;
    
    /** 声望值 */
    private int reputation;
    
    /** 负重 */
    private int weight;
    
    /** 最大负重 */
    private int maxWeight;
    
    /** 背包物品列表 */
    private Map<Integer, Item> inventory;
    
    /** 装备物品映射 */
    private Map<Integer, Item> equipment;
    
    /** 技能列表 */
    private Map<Integer, PlayerSkill> skills;
    
    /** 好友列表 */
    private List<String> friends;
    
    /** 师傅名称 */
    private String masterName;
    
    /** 徒弟列表 */
    private List<String> disciples;
    
    /** 配偶名称 */
    private String spouseName;
    
    /** 行会名称 */
    private String guildName;
    
    /** 行会职位 */
    private String guildRank;
    
    /** 在线时长（分钟） */
    private int onlineTime;
    
    /** 登录时间 */
    private long loginTime;
    
    /** 最后保存时间 */
    private long lastSaveTime;
    
    /** 自定义变量 */
    private Map<String, Object> customVariables;
    
    /** 在线状态 */
    private boolean online;
    
    /** 最后活动时间 */
    private long lastActivityTime;
    
    /**
     * 构造函数
     * 
     * @param accountName 账号名
     * @param name 角色名
     * @param job 职业
     * @param gender 性别
     * @param x 初始X坐标
     * @param y 初始Y坐标
     * @param mapName 初始地图名称
     */
    public Player(String accountName, String name, Job job, int gender, int x, int y, String mapName) {
        super(GameObjectType.PLAYER, name, x, y, mapName);
        
        this.accountName = accountName;
        this.job = job;
        this.gender = gender;
        this.hair = 0;
        this.level = 1;
        this.experience = 0;
        this.nextLevelExp = calculateNextLevelExp(1);
        this.statPoints = 0;
        this.skillPoints = 0;
        this.gold = 0;
        this.gamePoint = 0;
        this.gameDiamond = 0;
        this.pkValue = 0;
        this.reputation = 0;
        this.weight = 0;
        this.maxWeight = 100;
        
        // 初始化集合
        this.inventory = new ConcurrentHashMap<>();
        this.equipment = new ConcurrentHashMap<>();
        this.skills = new ConcurrentHashMap<>();
        this.friends = new ArrayList<>();
        this.disciples = new ArrayList<>();
        this.customVariables = new HashMap<>();
        
        // 根据职业初始化属性
        this.ability = new PlayerAbility(job);
        
        // 初始化生命值和魔法值
        updateMaxHpMp();
        setHp(getMaxHp());
        setMp(getMaxMp());
        
        this.loginTime = System.currentTimeMillis();
        this.lastSaveTime = loginTime;
        this.online = false;
        
        log.info("创建玩家角色: {} [账号: {}, 职业: {}, 性别: {}, 位置: ({}, {}), 地图: {}]",
                name, accountName, job.getName(), gender == 0 ? "男" : "女", x, y, mapName);
    }
    
    /**
     * 增加经验值
     * 
     * @param exp 经验值
     * @return 是否升级
     */
    public boolean addExperience(long exp) {
        if (exp <= 0) {
            return false;
        }
        
        long oldExp = this.experience;
        this.experience += exp;
        
        log.debug("玩家 {} 获得经验: {} (总经验: {})", getName(), exp, this.experience);
        
        // 检查是否升级
        boolean levelUp = false;
        while (this.experience >= this.nextLevelExp && this.level < 65535) {
            levelUp();
            levelUp = true;
        }
        
        return levelUp;
    }
    
    /**
     * 升级处理
     */
    private void levelUp() {
        int oldLevel = this.level;
        this.level++;
        this.nextLevelExp = calculateNextLevelExp(this.level);
        
        // 根据职业分配属性点
        int attributePoints = 5; // 每级5点属性
        this.statPoints += attributePoints;
        
        // 获得技能点
        if (this.level % 2 == 0) { // 偶数级获得技能点
            this.skillPoints++;
        }
        
        // 更新最大生命值和魔法值
        updateMaxHpMp();
        
        // 恢复满状态
        setHp(getMaxHp());
        setMp(getMaxMp());
        
        log.info("玩家 {} 升级: {} -> {} (属性点: +{}, 技能点: +{})", 
                getName(), oldLevel, this.level, attributePoints, this.level % 2 == 0 ? 1 : 0);
        
        // 触发升级事件
        onLevelUp(oldLevel, this.level);
    }
    
    /**
     * 计算下级所需经验
     * 
     * @param level 等级
     * @return 下级所需经验
     */
    private long calculateNextLevelExp(int level) {
        // 经验计算公式（可根据游戏平衡性调整）
        return (long) (Math.pow(level, 2.5) * 100);
    }
    
    /**
     * 更新最大生命值和魔法值
     */
    private void updateMaxHpMp() {
        // 根据职业和属性计算最大HP/MP
        int baseHp = 100;
        int baseMp = 100;
        
        // 职业基础值
        switch (job) {
            case WARRIOR:
                baseHp = 150;
                baseMp = 50;
                break;
            case WIZARD:
                baseHp = 80;
                baseMp = 150;
                break;
            case TAOIST:
                baseHp = 120;
                baseMp = 120;
                break;
        }
        
        // 等级加成
        int levelHp = (level - 1) * 20;
        int levelMp = (level - 1) * 15;
        
        // 属性加成
        int attributeHp = ability.getConstitution() * 10;
        int attributeMp = ability.getIntelligence() * 8;
        
        setMaxHp(baseHp + levelHp + attributeHp);
        setMaxMp(baseMp + levelMp + attributeMp);
    }
    
    /**
     * 分配属性点
     * 
     * @param strength 力量
     * @param agility 敏捷
     * @param constitution 体力
     * @param intelligence 智力
     * @return 分配是否成功
     */
    public boolean allocateStatPoints(int strength, int agility, int constitution, int intelligence) {
        int totalPoints = strength + agility + constitution + intelligence;
        
        if (totalPoints > this.statPoints || totalPoints <= 0) {
            log.warn("玩家 {} 属性点分配失败: 需要 {} 点，可用 {} 点", getName(), totalPoints, this.statPoints);
            return false;
        }
        
        // 分配属性点
        this.ability.addStrength(strength);
        this.ability.addAgility(agility);
        this.ability.addConstitution(constitution);
        this.ability.addIntelligence(intelligence);
        
        this.statPoints -= totalPoints;
        
        // 更新最大HP/MP
        updateMaxHpMp();
        
        log.info("玩家 {} 分配属性点: 力量+{}, 敏捷+{}, 体力+{}, 智力+{}, 剩余: {}",
                getName(), strength, agility, constitution, intelligence, this.statPoints);
        
        return true;
    }
    
    /**
     * 添加物品到背包
     * 
     * @param item 物品
     * @return 添加是否成功
     */
    public boolean addItem(Item item) {
        if (item == null) {
            return false;
        }
        
        // 找到空闲的背包槽位
        for (int i = 0; i < 40; i++) { // 背包40个格子
            if (!inventory.containsKey(i)) {
                inventory.put(i, item);
                log.debug("玩家 {} 获得物品: {} 数量: {}", getName(), item.getName(), item.getQuantity());
                return true;
            }
        }
        
        log.warn("玩家 {} 背包已满，无法添加物品: {}", getName(), item.getName());
        return false;
    }
    
    /**
     * 从背包移除物品
     * 
     * @param slot 背包槽位
     * @return 移除的物品
     */
    public Item removeItem(int slot) {
        Item item = inventory.remove(slot);
        if (item != null) {
            log.debug("玩家 {} 移除物品: {} 数量: {}", getName(), item.getName(), item.getQuantity());
        }
        return item;
    }
    
    /**
     * 装备物品
     * 
     * @param item 物品
     * @param slot 装备位置
     * @return 装备是否成功
     */
    public boolean equipItem(Item item, int slot) {
        if (item == null || slot < 0 || slot >= 12) {
            return false;
        }
        
        // 检查是否可以装备
        if (!canEquipItem(item, slot)) {
            log.warn("玩家 {} 无法装备物品: {} 到位置: {}", getName(), item.getName(), slot);
            return false;
        }
        
        // 卸下原有装备
        Item oldItem = equipment.get(slot);
        if (oldItem != null) {
            unequipItem(slot);
        }
        
        // 装备新物品
        equipment.put(slot, item);
        removeItem(slot);
        
        log.info("玩家 {} 装备物品: {} 到位置: {}", getName(), item.getName(), slot);
        
        // 触发装备事件
        onEquipItem(item, slot);
        
        return true;
    }
    
    /**
     * 卸下装备
     * 
     * @param slot 装备位置
     * @return 卸下的物品
     */
    public Item unequipItem(int slot) {
        if (slot < 0 || slot >= 12) {
            return null;
        }
        
        Item item = equipment.remove(slot);
        if (item != null) {
            addItem(item);
            log.info("玩家 {} 卸下装备: {} 从位置: {}", getName(), item.getName(), slot);
            
            // 触发卸装事件
            onUnequipItem(item, slot);
        }
        
        return item;
    }
    
    /**
     * 检查是否可以装备物品
     * 
     * @param item 物品
     * @param slot 装备位置
     * @return 是否可以装备
     */
    private boolean canEquipItem(Item item, int slot) {
        // 检查职业限制
        if (!item.canUseByJob(job)) {
            return false;
        }
        
        // 检查等级限制
        if (item.getRequiredLevel() > level) {
            return false;
        }
        
        // 检查装备位置匹配
        return item.getEquipmentSlot() == slot;
    }
    
    /**
     * 增加金币
     * 
     * @param amount 金币数量
     * @return 是否成功
     */
    public boolean addGold(long amount) {
        if (amount <= 0) {
            return false;
        }
        
        this.gold += amount;
        log.debug("玩家 {} 获得金币: {}, 当前金币: {}", getName(), amount, this.gold);
        return true;
    }

    /**
     * 消费金币
     * 
     * @param amount 金币数量
     * @return 是否成功
     */
    public boolean consumeGold(long amount) {
        if (amount <= 0 || this.gold < amount) {
            return false;
        }
        
        this.gold -= amount;
        log.debug("玩家 {} 消费金币: {}, 剩余金币: {}", getName(), amount, this.gold);
        return true;
    }
    
    /**
     * 获取装备物品列表
     * 
     * @return 装备物品列表
     */
    public List<Item> getEquippedItems() {
        List<Item> equippedItems = new ArrayList<>();
        if (equipment != null) {
            equippedItems.addAll(equipment.values());
        }
        return equippedItems;
    }
    
    /**
     * 获取徒弟数量
     * 
     * @return 徒弟数量
     */
    public int getApprenticeCount() {
        return disciples != null ? disciples.size() : 0;
    }
    
    /**
     * 获取行会等级（职位对应的等级）
     * 
     * @return 行会等级
     */
    public int getGuildRankLevel() {
        if (guildRank == null) {
            return 0;
        }
        
        switch (guildRank) {
            case "会长":
                return 1;
            case "副会长":
                return 2;
            case "长老":
                return 3;
            case "精英":
                return 4;
            case "成员":
                return 5;
            default:
                return 6;
        }
    }
    
    /**
     * 获取总在线时间（小时）
     * 
     * @return 总在线时间
     */
    public long getTotalOnlineTime() {
        long currentSessionTime = 0;
        if (online && loginTime > 0) {
            currentSessionTime = (System.currentTimeMillis() - loginTime) / 1000 / 60; // 转换为分钟
        }
        return onlineTime + currentSessionTime;
    }
    
    /**
     * 移除装备
     * 
     * @param itemId 物品ID
     * @return 是否成功
     */
    public boolean removeEquipment(int itemId) {
        if (equipment == null) {
            return false;
        }
        
        for (Map.Entry<Integer, Item> entry : equipment.entrySet()) {
            if (entry.getValue().getItemId() == itemId) {
                equipment.remove(entry.getKey());
                log.debug("玩家 {} 移除装备: {}", getName(), entry.getValue().getName());
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取名字颜色
     * 
     * @return 名字颜色
     */
    public String getNameColor() {
        // 根据PK值返回名字颜色
        if (pkValue >= 500) {
            return "PINK";
        } else if (pkValue >= 300) {
            return "RED";
        } else if (pkValue >= 100) {
            return "YELLOW";
        } else {
            return "WHITE";
        }
    }
    
    /**
     * 设置名字颜色
     * 
     * @param color 颜色
     */
    public void setNameColor(String color) {
        // 这个方法可以用于其他系统设置特殊颜色
        // 暂时只记录日志
        log.debug("设置玩家 {} 名字颜色为: {}", getName(), color);
    }
    
    /**
     * 获取最后PK值衰减时间
     * 
     * @return 最后PK值衰减时间
     */
    public long getLastPKDecayTime() {
        Object lastDecayTime = getCustomVariable("lastPKDecayTime");
        return lastDecayTime != null ? (Long) lastDecayTime : 0;
    }
    
    /**
     * 设置最后PK值衰减时间
     * 
     * @param time 时间
     */
    public void setLastPKDecayTime(long time) {
        setCustomVariable("lastPKDecayTime", time);
    }
    
    /**
     * 获取自定义变量
     * 
     * @param key 键
     * @return 值
     */
    public Object getCustomVariable(String key) {
        if (customVariables == null) {
            return null;
        }
        return customVariables.get(key);
    }
    
    /**
     * 设置自定义变量
     * 
     * @param key 键
     * @param value 值
     */
    public void setCustomVariable(String key, Object value) {
        if (customVariables == null) {
            customVariables = new HashMap<>();
        }
        customVariables.put(key, value);
    }
    
    /**
     * 获取队伍ID
     * 
     * @return 队伍ID
     */
    public String getTeamId() {
        Object teamId = getCustomVariable("teamId");
        return teamId != null ? (String) teamId : null;
    }
    
    /**
     * 设置队伍ID
     * 
     * @param teamId 队伍ID
     */
    public void setTeamId(String teamId) {
        setCustomVariable("teamId", teamId);
    }
    
    /**
     * 获取声望值（兼容方法）
     * 
     * @return 声望值
     */
    public int getCreditPoint() {
        return reputation;
    }
    
    /**
     * 设置声望值（兼容方法）
     * 
     * @param creditPoint 声望值
     */
    public void setCreditPoint(int creditPoint) {
        this.reputation = creditPoint;
    }
    
    /**
     * 学习技能
     * 
     * @param skill 技能
     * @param level 学习等级
     * @return 是否学习成功
     */
    public boolean learnSkill(PlayerSkill skill, int level) {
        if (skill == null) {
            return false;
        }
        
        // 检查技能点
        if (skillPoints <= 0) {
            log.warn("玩家 {} 技能点不足，无法学习技能: {}", getName(), skill.getName());
            return false;
        }
        
        // 检查职业限制
        if (!skill.canLearnByJob(job)) {
            log.warn("玩家 {} 职业不符，无法学习技能: {}", getName(), skill.getName());
            return false;
        }
        
        // 检查等级限制
        if (skill.getRequiredLevel() > level) {
            log.warn("玩家 {} 等级不足，无法学习技能: {} (需要等级: {})", 
                    getName(), skill.getName(), skill.getRequiredLevel());
            return false;
        }
        
        skills.put(skill.getId(), skill);
        skillPoints--;
        
        log.info("玩家 {} 学习技能: {} [等级: {}] (剩余技能点: {})", getName(), skill.getName(), level, skillPoints);
        
        return true;
    }
    
    /**
     * 使用技能
     * 
     * @param skillId 技能ID
     * @return 是否使用成功
     */
    public boolean useSkill(int skillId) {
        PlayerSkill playerSkill = skills.get(skillId);
        if (playerSkill == null) {
            log.warn("玩家 {} 没有学会技能 ID: {}", getName(), skillId);
            return false;
        }
        
        // 检查魔法值
        Skill.SkillAttributes attributes = playerSkill.getCurrentAttributes();
        if (this.mp < attributes.getMpCost()) {
            log.debug("玩家 {} 魔法值不足，无法使用技能: {}", getName(), playerSkill.getSkill().getName());
            return false;
        }
        
        // 使用技能
        if (playerSkill.use()) {
            this.mp -= attributes.getMpCost();
            log.info("玩家 {} 使用技能: {}", getName(), playerSkill.getSkill().getName());
            return true;
        }
        
        return false;
    }
    
    @Override
    public void update() {
        // 更新在线时长
        long currentTime = System.currentTimeMillis();
        if (currentTime - getLastUpdateTime() >= 60000) { // 每分钟更新一次
            onlineTime++;
            setLastUpdateTime(currentTime);
        }
        
        // 定期保存玩家数据
        if (currentTime - lastSaveTime >= 300000) { // 每5分钟保存一次
            savePlayerData();
        }
    }
    
    /**
     * 保存玩家数据
     */
    private void savePlayerData() {
        this.lastSaveTime = System.currentTimeMillis();
        log.debug("保存玩家数据: {}", getName());
        
        // 实现数据持久化
        try {
            // 创建玩家数据快照
            PlayerDataSnapshot snapshot = createDataSnapshot();
            
            // 这里可以保存到数据库
            // playerRepository.save(snapshot);
            
            // 或者保存到文件
            // saveToFile(snapshot);
            
            log.debug("玩家数据保存成功: {}", getName());
        } catch (Exception e) {
            log.error("保存玩家数据失败: {}", getName(), e);
        }
    }
    
    /**
     * 创建玩家数据快照
     */
    private PlayerDataSnapshot createDataSnapshot() {
        PlayerDataSnapshot snapshot = new PlayerDataSnapshot();
        snapshot.setName(getName());
        snapshot.setAccountName(accountName);
        snapshot.setJob(job);
        snapshot.setGender(gender);
        snapshot.setHair(hair);
        snapshot.setLevel(level);
        snapshot.setExperience(experience);
        snapshot.setHp(getHp());
        snapshot.setMp(getMp());
        snapshot.setMaxHp(getMaxHp());
        snapshot.setMaxMp(getMaxMp());
        snapshot.setX(getX());
        snapshot.setY(getY());
        snapshot.setMapName(getMapName());
        snapshot.setGold(gold);
        snapshot.setGamePoint(gamePoint);
        snapshot.setGameDiamond(gameDiamond);
        snapshot.setPkValue(pkValue);
        snapshot.setReputation(reputation);
        snapshot.setWeight(weight);
        snapshot.setMaxWeight(maxWeight);
        snapshot.setStatPoints(statPoints);
        snapshot.setSkillPoints(skillPoints);
        snapshot.setOnlineTime(onlineTime);
        snapshot.setLoginTime(loginTime);
        snapshot.setLastSaveTime(lastSaveTime);
        snapshot.setGuildName(guildName);
        snapshot.setGuildRank(guildRank);
        snapshot.setMasterName(masterName);
        snapshot.setSpouseName(spouseName);
        
        // 保存能力值
        if (ability != null) {
            snapshot.setStrength(ability.getStrength());
            snapshot.setAgility(ability.getAgility());
            snapshot.setConstitution(ability.getConstitution());
            snapshot.setIntelligence(ability.getIntelligence());
        }
        
        // 保存物品信息
        if (inventory != null) {
            snapshot.setInventoryJson(serializeInventory(inventory));
        }
        
        // 保存装备信息
        if (equipment != null) {
            snapshot.setEquipmentJson(serializeEquipment(equipment));
        }
        
        // 保存技能信息
        if (skills != null) {
            snapshot.setSkillsJson(serializeSkills(skills));
        }
        
        // 保存好友列表
        if (friends != null) {
            snapshot.setFriendsJson(serializeFriends(friends));
        }
        
        // 保存自定义变量
        if (customVariables != null) {
            snapshot.setCustomVariablesJson(serializeCustomVariables(customVariables));
        }
        
        return snapshot;
    }
    
    /**
     * 序列化物品栏
     */
    private String serializeInventory(Map<Integer, Item> inventory) {
        // 这里可以使用JSON序列化
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<Integer, Item> entry : inventory.entrySet()) {
            if (sb.length() > 1) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            sb.append(serializeItem(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * 序列化装备
     */
    private String serializeEquipment(Map<Integer, Item> equipment) {
        return serializeInventory(equipment); // 与物品栏序列化相同
    }
    
    /**
     * 序列化技能
     */
    private String serializeSkills(Map<Integer, PlayerSkill> skills) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<Integer, PlayerSkill> entry : skills.entrySet()) {
            if (sb.length() > 1) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            sb.append(serializeSkill(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * 序列化好友列表
     */
    private String serializeFriends(List<String> friends) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < friends.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(friends.get(i)).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * 序列化自定义变量
     */
    private String serializeCustomVariables(Map<String, Object> customVariables) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<String, Object> entry : customVariables.entrySet()) {
            if (sb.length() > 1) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            sb.append("\"").append(entry.getValue().toString()).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * 序列化物品
     */
    private String serializeItem(Item item) {
        return "{" +
                "\"name\":\"" + item.getName() + "\"," +
                "\"weight\":" + item.getWeight() + "," +
                "\"requiredLevel\":" + item.getRequiredLevel() + "," +
                "\"requiredJob\":\"" + (item.getRequiredJob() != null ? item.getRequiredJob().name() : "null") + "\"," +
                "\"equipmentSlot\":" + item.getEquipmentSlot() +
                "}";
    }
    
    /**
     * 序列化技能
     */
    private String serializeSkill(PlayerSkill skill) {
        return "{" +
                "\"id\":" + skill.getId() + "," +
                "\"level\":" + skill.getLevel() + "," +
                "\"requiredLevel\":" + skill.getRequiredLevel() + "," +
                "\"requiredJob\":\"" + (skill.getRequiredJob() != null ? skill.getRequiredJob().name() : "null") + "\"" +
                "}";
    }
    
    /**
     * 玩家数据快照类
     */
    @Data
    public static class PlayerDataSnapshot {
        private String name;
        private String accountName;
        private Job job;
        private int gender;
        private int hair;
        private int level;
        private long experience;
        private int hp;
        private int mp;
        private int maxHp;
        private int maxMp;
        private int x;
        private int y;
        private String mapName;
        private long gold;
        private long gamePoint;
        private long gameDiamond;
        private int pkValue;
        private int reputation;
        private int weight;
        private int maxWeight;
        private int statPoints;
        private int skillPoints;
        private int onlineTime;
        private long loginTime;
        private long lastSaveTime;
        private String guildName;
        private String guildRank;
        private String masterName;
        private String spouseName;
        private int strength;
        private int agility;
        private int constitution;
        private int intelligence;
        private String inventoryJson;
        private String equipmentJson;
        private String skillsJson;
        private String friendsJson;
        private String customVariablesJson;
    }
    
    // 事件处理方法
    
    /**
     * 升级事件处理
     */
    protected void onLevelUp(int oldLevel, int newLevel) {
        // 可被子类重写
    }
    
    /**
     * 装备物品事件处理
     */
    protected void onEquipItem(Item item, int slot) {
        // 可被子类重写
    }
    
    /**
     * 卸下物品事件处理
     */
    protected void onUnequipItem(Item item, int slot) {
        // 可被子类重写
    }
    
    // 内部类定义
    
    /**
     * 玩家属性类
     */
    @Data
    public static class PlayerAbility {
        private int strength;     // 力量
        private int agility;      // 敏捷
        private int constitution; // 体力
        private int intelligence; // 智力
        
        public PlayerAbility(Job job) {
            // 根据职业设置初始属性
            switch (job) {
                case WARRIOR:
                    this.strength = 15;
                    this.agility = 10;
                    this.constitution = 15;
                    this.intelligence = 5;
                    break;
                case WIZARD:
                    this.strength = 5;
                    this.agility = 10;
                    this.constitution = 10;
                    this.intelligence = 20;
                    break;
                case TAOIST:
                    this.strength = 8;
                    this.agility = 12;
                    this.constitution = 12;
                    this.intelligence = 13;
                    break;
                default:
                    this.strength = 10;
                    this.agility = 10;
                    this.constitution = 10;
                    this.intelligence = 10;
                    break;
            }
        }
        
        public void addStrength(int amount) { this.strength += amount; }
        public void addAgility(int amount) { this.agility += amount; }
        public void addConstitution(int amount) { this.constitution += amount; }
        public void addIntelligence(int amount) { this.intelligence += amount; }
    }
    
    /**
     * 游戏物品类（简化版）
     */
    @Data
    public static class Item {
        private String name;
        private int weight;
        private int requiredLevel;
        private Job requiredJob;
        private int equipmentSlot;
        
        public boolean canUseByJob(Job job) {
            return requiredJob == null || requiredJob == job;
        }
    }
    
    /**
     * 玩家技能类（简化版）
     */
    @Data
    public static class PlayerSkill {
        private int id;
        private Skill skill;
        private int level;
        private int requiredLevel;
        private Job requiredJob;
        
        public PlayerSkill(Skill skill, int level) {
            this.id = skill.getId();
            this.skill = skill;
            this.level = level;
            this.requiredLevel = skill.getRequiredLevel();
            this.requiredJob = skill.getRequiredJob();
        }
        
        public Skill.SkillAttributes getCurrentAttributes() {
            return skill.getAttributes(level);
        }
        
        public boolean use() {
            return skill.use();
        }
        
        public boolean canLearnByJob(Job job) {
            return requiredJob == null || requiredJob == job;
        }
    }
} 