package com.mir2.service;

import com.mir2.core.model.Player;
import com.mir2.core.model.Item;
import com.mir2.core.model.Skill;
import com.mir2.core.model.GameMap;
import com.mir2.core.model.Guild;
import com.mir2.core.model.BaseObject;
import com.mir2.core.model.Monster;
import com.mir2.core.model.PlayerSkill;
import com.mir2.core.model.PlayerBag;
import com.mir2.core.model.PlayerEquipment;
import com.mir2.core.model.PlayerFriend;
import com.mir2.core.model.SummonedCreature;
import com.mir2.core.model.BuffEffect;
import com.mir2.core.model.DebuffEffect;
import com.mir2.core.enums.Job;
import com.mir2.core.enums.BuffType;
import com.mir2.core.enums.DebuffType;
import com.mir2.core.enums.SummonType;
import com.mir2.entity.CharacterEntity;
import com.mir2.entity.UserEntity;
import com.mir2.repository.CharacterRepository;
import com.mir2.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 玩家服务类
 * 
 * <p>提供玩家相关的业务逻辑处理，整合所有功能模块。
 * 对应原M2Engine中的玩家管理系统。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>玩家账号和角色管理</li>
 *   <li>游戏逻辑处理</li>
 *   <li>数据持久化</li>
 *   <li>在线玩家管理</li>
 *   <li>游戏系统集成</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class PlayerService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CharacterRepository characterRepository;
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private SkillService skillService;
    
    @Autowired
    private MapService mapService;
    
    @Autowired
    private GuildService guildService;
    
    /** 在线玩家缓存 */
    private final ConcurrentHashMap<String, Player> onlinePlayers = new ConcurrentHashMap<>();
    
    /** 玩家会话映射 */
    private final ConcurrentHashMap<String, String> playerSessions = new ConcurrentHashMap<>();
    
    /**
     * 用户登录验证
     * 
     * @param username 用户名
     * @param password 密码
     * @param clientIp 客户端IP
     * @return 用户实体，如果验证失败返回null
     */
    @Transactional
    public UserEntity authenticateUser(String username, String password, String clientIp) {
        Optional<UserEntity> userOpt = userRepository.findByUsernameAndPassword(username, password);
        
        if (!userOpt.isPresent()) {
            log.warn("用户登录失败: {} from {}", username, clientIp);
            return null;
        }
        
        UserEntity user = userOpt.get();
        
        // 检查账号状态
        if (user.getStatus() != UserEntity.Status.NORMAL.getCode()) {
            log.warn("用户账号状态异常: {} status: {}", username, user.getStatus());
            return null;
        }
        
        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(clientIp);
        user.setLoginCount(user.getLoginCount() + 1);
        userRepository.save(user);
        
        log.info("用户登录成功: {} from {}", username, clientIp);
        return user;
    }
    
    /**
     * 创建新角色
     * 
     * @param userId 用户ID
     * @param characterName 角色名称
     * @param job 职业
     * @param gender 性别
     * @return 角色实体
     */
    @Transactional
    public CharacterEntity createCharacter(Long userId, String characterName, Job job, int gender) {
        // 检查角色名是否已存在
        if (characterRepository.existsByName(characterName)) {
            log.warn("角色名已存在: {}", characterName);
            return null;
        }
        
        // 创建角色实体
        CharacterEntity character = new CharacterEntity();
        character.setUserId(userId);
        character.setName(characterName);
        character.setJob(job.ordinal());
        character.setGender(gender);
        character.setCreateTime(LocalDateTime.now());
        character.setUpdateTime(LocalDateTime.now());
        
        // 根据职业设置初始属性
        initCharacterAttributes(character, job);
        
        // 保存到数据库
        character = characterRepository.save(character);
        
        log.info("创建角色成功: {} [用户ID: {}, 职业: {}, 性别: {}]", 
                characterName, userId, job.getName(), gender == 0 ? "男" : "女");
        
        return character;
    }
    
    /**
     * 玩家进入游戏
     * 
     * @param userId 用户ID
     * @param characterName 角色名称
     * @param sessionId 会话ID
     * @return 玩家对象
     */
    @Transactional
    public Player enterGame(Long userId, String characterName, String sessionId) {
        // 查找角色
        Optional<CharacterEntity> characterOpt = characterRepository.findByUserIdAndName(userId, characterName);
        if (!characterOpt.isPresent()) {
            log.warn("角色不存在: {} for user: {}", characterName, userId);
            return null;
        }
        
        CharacterEntity character = characterOpt.get();
        
        // 检查是否已在线
        if (onlinePlayers.containsKey(characterName)) {
            log.warn("角色已在线: {}", characterName);
            return null;
        }
        
        // 创建玩家对象
        Player player = createPlayerFromEntity(character);
        
        // 加载玩家数据
        loadPlayerData(player, character);
        
        // 添加到在线玩家列表
        onlinePlayers.put(characterName, player);
        playerSessions.put(sessionId, characterName);
        
        // 更新登录时间
        character.setLastLoginTime(LocalDateTime.now());
        characterRepository.save(character);
        
        // 加入地图
        GameMap gameMap = mapService.getMap(character.getCurrentMap());
        if (gameMap != null) {
            gameMap.addObject(player);
        }
        
        log.info("玩家进入游戏: {} [地图: {}, 位置: ({}, {})]", 
                characterName, character.getCurrentMap(), character.getCurrentX(), character.getCurrentY());
        
        return player;
    }
    
    /**
     * 玩家离开游戏
     * 
     * @param sessionId 会话ID
     * @return 是否成功
     */
    @Transactional
    public boolean leaveGame(String sessionId) {
        String characterName = playerSessions.get(sessionId);
        if (characterName == null) {
            return false;
        }
        
        Player player = onlinePlayers.get(characterName);
        if (player == null) {
            return false;
        }
        
        // 保存玩家数据
        savePlayerData(player);
        
        // 从地图移除
        GameMap gameMap = mapService.getMap(player.getMapName());
        if (gameMap != null) {
            gameMap.removeObject(player);
        }
        
        // 从在线列表移除
        onlinePlayers.remove(characterName);
        playerSessions.remove(sessionId);
        
        log.info("玩家离开游戏: {}", characterName);
        return true;
    }
    
    /**
     * 获取在线玩家
     * 
     * @param characterName 角色名称
     * @return 玩家对象
     */
    public Player getOnlinePlayer(String characterName) {
        return onlinePlayers.get(characterName);
    }
    
    /**
     * 获取所有在线玩家
     * 
     * @return 在线玩家列表
     */
    public List<Player> getAllOnlinePlayers() {
        return new CopyOnWriteArrayList<>(onlinePlayers.values());
    }
    
    /**
     * 获取在线玩家数量
     * 
     * @return 在线玩家数量
     */
    public int getOnlinePlayerCount() {
        return onlinePlayers.size();
    }
    
    /**
     * 玩家移动处理
     * 
     * @param characterName 角色名称
     * @param newX 新X坐标
     * @param newY 新Y坐标
     * @param direction 方向
     * @return 是否移动成功
     */
    public boolean movePlayer(String characterName, int newX, int newY, int direction) {
        Player player = onlinePlayers.get(characterName);
        if (player == null) {
            return false;
        }
        
        GameMap gameMap = mapService.getMap(player.getMapName());
        if (gameMap == null) {
            return false;
        }
        
        // 检查移动合法性
        if (!gameMap.isPassable(newX, newY)) {
            log.warn("玩家 {} 尝试移动到不可通过的位置: ({}, {})", characterName, newX, newY);
            return false;
        }
        
        // 执行移动
        if (gameMap.moveObject(player, newX, newY)) {
            player.setDirection(direction);
            log.debug("玩家 {} 移动到: ({}, {}) 方向: {}", characterName, newX, newY, direction);
            return true;
        }
        
        return false;
    }
    
    /**
     * 玩家传送处理
     * 
     * @param characterName 角色名称
     * @param targetMap 目标地图
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     * @return 是否传送成功
     */
    public boolean teleportPlayer(String characterName, String targetMap, int targetX, int targetY) {
        Player player = onlinePlayers.get(characterName);
        if (player == null) {
            return false;
        }
        
        // 从当前地图移除
        GameMap currentMap = mapService.getMap(player.getMapName());
        if (currentMap != null) {
            currentMap.removeObject(player);
        }
        
        // 设置新位置
        player.setMapName(targetMap);
        player.setX(targetX);
        player.setY(targetY);
        
        // 加入新地图
        GameMap newMap = mapService.getMap(targetMap);
        if (newMap != null) {
            GameMap.Position availablePos = newMap.findAvailablePosition(targetX, targetY, 3);
            if (availablePos != null) {
                player.setX(availablePos.getX());
                player.setY(availablePos.getY());
                newMap.addObject(player);
                
                log.info("玩家 {} 传送到地图: {} 位置: ({}, {})", 
                        characterName, targetMap, availablePos.getX(), availablePos.getY());
                return true;
            }
        }
        
        log.warn("玩家 {} 传送失败: 目标地图 {} 位置 ({}, {}) 不可用", 
                characterName, targetMap, targetX, targetY);
        return false;
    }
    
    /**
     * 玩家使用技能
     * 
     * @param characterName 角色名称
     * @param skillId 技能ID
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     * @return 是否使用成功
     */
    public boolean useSkill(String characterName, int skillId, int targetX, int targetY) {
        Player player = onlinePlayers.get(characterName);
        if (player == null) {
            return false;
        }
        
        // 查找技能
        PlayerSkill playerSkill = player.getSkills().stream()
                .filter(skill -> skill.getSkill().getSkillId() == skillId)
                .findFirst()
                .orElse(null);
        
        if (playerSkill == null) {
            log.warn("玩家 {} 没有技能: {}", characterName, skillId);
            return false;
        }
        
        // 检查技能冷却
        if (!playerSkill.canUse()) {
            log.warn("玩家 {} 技能 {} 冷却中", characterName, skillId);
            return false;
        }
        
        // 检查魔法消耗
        Skill.SkillAttributes attributes = playerSkill.getCurrentAttributes();
        if (player.getMp() < attributes.getManaCost()) {
            log.warn("玩家 {} 魔法不足，无法使用技能: {}", characterName, skillId);
            return false;
        }
        
        // 消耗魔法
        player.setMp(player.getMp() - attributes.getManaCost());
        
        // 使用技能
        playerSkill.use();
        
        // 处理技能效果
        processSkillEffect(player, playerSkill, targetX, targetY);
        
        log.info("玩家 {} 使用技能: {} 目标: ({}, {})", characterName, skillId, targetX, targetY);
        return true;
    }
    
    /**
     * 玩家聊天处理
     * 
     * @param characterName 角色名称
     * @param message 聊天消息
     * @param chatType 聊天类型
     * @return 是否发送成功
     */
    public boolean sendChat(String characterName, String message, int chatType) {
        Player player = onlinePlayers.get(characterName);
        if (player == null) {
            return false;
        }
        
        // 根据聊天类型处理
        switch (chatType) {
            case 0: // 普通聊天
                sendNormalChat(player, message);
                break;
            case 1: // 私聊
                sendPrivateChat(player, message);
                break;
            case 2: // 行会聊天
                sendGuildChat(player, message);
                break;
            case 3: // 喊话
                sendShoutChat(player, message);
                break;
            default:
                return false;
        }
        
        log.info("玩家 {} 发送聊天: [类型: {}] {}", characterName, chatType, message);
        return true;
    }
    
    /**
     * 初始化角色属性
     * 
     * @param character 角色实体
     * @param job 职业
     */
    private void initCharacterAttributes(CharacterEntity character, Job job) {
        // 根据职业设置初始属性
        switch (job) {
            case WARRIOR:
                character.setStrength(15);
                character.setAgility(10);
                character.setConstitution(12);
                character.setIntelligence(8);
                character.setMaxHp(120);
                character.setMaxMp(80);
                break;
            case WIZARD:
                character.setStrength(8);
                character.setAgility(10);
                character.setConstitution(10);
                character.setIntelligence(17);
                character.setMaxHp(80);
                character.setMaxMp(140);
                break;
            case TAOIST:
                character.setStrength(10);
                character.setAgility(12);
                character.setConstitution(11);
                character.setIntelligence(12);
                character.setMaxHp(100);
                character.setMaxMp(120);
                break;
        }
        
        character.setHp(character.getMaxHp());
        character.setMp(character.getMaxMp());
        character.setStatPoints(5);
        character.setSkillPoints(1);
    }
    
    /**
     * 从实体创建玩家对象
     * 
     * @param character 角色实体
     * @return 玩家对象
     */
    private Player createPlayerFromEntity(CharacterEntity character) {
        Job job = Job.values()[character.getJob()];
        
        Player player = new Player(
                character.getUserId().toString(),
                character.getName(),
                job,
                character.getGender(),
                character.getCurrentX(),
                character.getCurrentY(),
                character.getCurrentMap()
        );
        
        // 设置基本属性
        player.setLevel(character.getLevel());
        player.setExperience(character.getExperience());
        player.setHp(character.getHp());
        player.setMp(character.getMp());
        player.setMaxHp(character.getMaxHp());
        player.setMaxMp(character.getMaxMp());
        player.setGold(character.getGold());
        player.setGamePoint(character.getGamePoint());
        player.setGameDiamond(character.getGameDiamond());
        player.setPkValue(character.getPkValue());
        player.setReputation(character.getReputation());
        player.setOnlineTime(character.getOnlineTime());
        player.setGuildName(character.getGuildName());
        player.setGuildRank(character.getGuildRank());
        player.setMasterName(character.getMasterName());
        player.setSpouseName(character.getSpouseName());
        
        // 设置属性
        Player.PlayerAbility ability = player.getAbility();
        ability.setStrength(character.getStrength());
        ability.setAgility(character.getAgility());
        ability.setConstitution(character.getConstitution());
        ability.setIntelligence(character.getIntelligence());
        
        return player;
    }
    
    /**
     * 加载玩家数据
     * 
     * @param player 玩家对象
     * @param character 角色实体
     */
    private void loadPlayerData(Player player, CharacterEntity character) {
        // 加载背包物品
        loadPlayerItems(player, character);
        
        // 加载装备
        loadPlayerEquipment(player, character);
        
        // 加载技能
        loadPlayerSkills(player, character);
        
        // 加载好友列表
        loadPlayerFriends(player, character);
        
        log.debug("加载玩家数据: {}", player.getName());
    }
    
    /**
     * 加载玩家背包物品
     * 
     * @param player 玩家对象
     * @param character 角色实体
     */
    private void loadPlayerItems(Player player, CharacterEntity character) {
        try {
            // 从数据库加载背包物品
            PlayerBag bag = new PlayerBag();
            bag.setCapacity(40); // 默认背包容量40格
            
            // 从数据库加载背包数据
            if (character.getBagData() != null && !character.getBagData().isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode bagNode = mapper.readTree(character.getBagData());
                    JsonNode itemsNode = bagNode.get("items");
                    
                    if (itemsNode != null && itemsNode.isArray()) {
                        for (JsonNode itemNode : itemsNode) {
                            int itemId = itemNode.get("id").asInt();
                            int count = itemNode.get("count").asInt();
                            int slotIndex = itemNode.get("slot").asInt();
                            
                            Item item = itemService.createItem(itemId, count);
                            if (item != null) {
                                bag.setItem(slotIndex, item);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("解析背包数据失败: {}", character.getName(), e);
                }
            }
            
            // 添加一些默认物品（仅限新手）
            if (character.getLevel() == 1) {
                // 新手礼包
                bag.addItem(itemService.createItem(1, 1)); // 新手剑
                bag.addItem(itemService.createItem(101, 10)); // 金创药*10
                bag.addItem(itemService.createItem(102, 10)); // 魔法药*10
            }
            
            player.setBag(bag);
            log.debug("加载玩家 {} 背包物品", player.getName());
        } catch (Exception e) {
            log.error("加载玩家 {} 背包物品失败", player.getName(), e);
        }
    }
    
    /**
     * 加载玩家装备
     * 
     * @param player 玩家对象
     * @param character 角色实体
     */
    private void loadPlayerEquipment(Player player, CharacterEntity character) {
        try {
            // 从数据库加载装备数据
            PlayerEquipment equipment = new PlayerEquipment();
            
            // 从数据库加载装备数据
            if (character.getEquipmentData() != null && !character.getEquipmentData().isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode equipmentNode = mapper.readTree(character.getEquipmentData());
                    
                    // 加载各部位装备
                    if (equipmentNode.has("weapon")) {
                        int weaponId = equipmentNode.get("weapon").asInt();
                        if (weaponId > 0) {
                            equipment.setWeapon(itemService.createItem(weaponId, 1));
                        }
                    }
                    
                    if (equipmentNode.has("cloth")) {
                        int clothId = equipmentNode.get("cloth").asInt();
                        if (clothId > 0) {
                            equipment.setCloth(itemService.createItem(clothId, 1));
                        }
                    }
                    
                    if (equipmentNode.has("helmet")) {
                        int helmetId = equipmentNode.get("helmet").asInt();
                        if (helmetId > 0) {
                            equipment.setHelmet(itemService.createItem(helmetId, 1));
                        }
                    }
                    
                    if (equipmentNode.has("necklace")) {
                        int necklaceId = equipmentNode.get("necklace").asInt();
                        if (necklaceId > 0) {
                            equipment.setNecklace(itemService.createItem(necklaceId, 1));
                        }
                    }
                    
                    if (equipmentNode.has("rightRing")) {
                        int rightRingId = equipmentNode.get("rightRing").asInt();
                        if (rightRingId > 0) {
                            equipment.setRightRing(itemService.createItem(rightRingId, 1));
                        }
                    }
                    
                    if (equipmentNode.has("leftRing")) {
                        int leftRingId = equipmentNode.get("leftRing").asInt();
                        if (leftRingId > 0) {
                            equipment.setLeftRing(itemService.createItem(leftRingId, 1));
                        }
                    }
                    
                    if (equipmentNode.has("rightBracelet")) {
                        int rightBraceletId = equipmentNode.get("rightBracelet").asInt();
                        if (rightBraceletId > 0) {
                            equipment.setRightBracelet(itemService.createItem(rightBraceletId, 1));
                        }
                    }
                    
                    if (equipmentNode.has("leftBracelet")) {
                        int leftBraceletId = equipmentNode.get("leftBracelet").asInt();
                        if (leftBraceletId > 0) {
                            equipment.setLeftBracelet(itemService.createItem(leftBraceletId, 1));
                        }
                    }
                    
                } catch (Exception e) {
                    log.error("解析装备数据失败: {}", character.getName(), e);
                }
            }
            
            // 新手装备（仅限新手）
            if (character.getLevel() == 1) {
                switch (character.getJob()) {
                    case 0: // 战士
                        equipment.setWeapon(itemService.createItem(1, 1)); // 新手剑
                        equipment.setCloth(itemService.createItem(201, 1)); // 新手布衣
                        break;
                    case 1: // 法师
                        equipment.setWeapon(itemService.createItem(2, 1)); // 新手法杖
                        equipment.setCloth(itemService.createItem(202, 1)); // 新手法袍
                        break;
                    case 2: // 道士
                        equipment.setWeapon(itemService.createItem(3, 1)); // 新手道具
                        equipment.setCloth(itemService.createItem(203, 1)); // 新手道袍
                        break;
                }
            }
            
            player.setEquipment(equipment);
            log.debug("加载玩家 {} 装备", player.getName());
        } catch (Exception e) {
            log.error("加载玩家 {} 装备失败", player.getName(), e);
        }
    }
    
    /**
     * 加载玩家技能
     * 
     * @param player 玩家对象
     * @param character 角色实体
     */
    private void loadPlayerSkills(Player player, CharacterEntity character) {
        try {
            // 从数据库加载技能数据
            Map<Integer, PlayerSkill> skills = new HashMap<>();
            
            // 从数据库加载技能数据
            if (character.getSkillData() != null && !character.getSkillData().isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode skillsNode = mapper.readTree(character.getSkillData());
                    
                    if (skillsNode.isArray()) {
                        for (JsonNode skillNode : skillsNode) {
                            int skillId = skillNode.get("id").asInt();
                            int level = skillNode.get("level").asInt();
                            long experience = skillNode.get("experience").asLong();
                            long lastUseTime = skillNode.get("lastUseTime").asLong();
                            
                            Skill skillTemplate = skillService.getSkillTemplate(skillId);
                            if (skillTemplate != null) {
                                PlayerSkill playerSkill = new PlayerSkill(skillTemplate, level);
                                playerSkill.setExperience(experience);
                                playerSkill.setLastUseTime(lastUseTime);
                                skills.put(skillId, playerSkill);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("解析技能数据失败: {}", character.getName(), e);
                }
            }
            
            // 根据职业添加基础技能（仅限新手）
            if (character.getLevel() == 1) {
                switch (character.getJob()) {
                    case 0: // 战士
                        if (!skills.containsKey(1)) {
                            Skill basicSword = skillService.getSkillTemplate(1);
                            if (basicSword != null) {
                                skills.put(1, new PlayerSkill(basicSword, 1));
                            }
                        }
                        break;
                    case 1: // 法师
                        if (!skills.containsKey(11)) {
                            Skill fireball = skillService.getSkillTemplate(11);
                            if (fireball != null) {
                                skills.put(11, new PlayerSkill(fireball, 1));
                            }
                        }
                        break;
                    case 2: // 道士
                        if (!skills.containsKey(21)) {
                            Skill heal = skillService.getSkillTemplate(21);
                            if (heal != null) {
                                skills.put(21, new PlayerSkill(heal, 1));
                            }
                        }
                        break;
                }
            }
            
            player.setSkills(skills);
            log.debug("加载玩家 {} 技能", player.getName());
        } catch (Exception e) {
            log.error("加载玩家 {} 技能失败", player.getName(), e);
        }
    }
    
    /**
     * 加载玩家好友列表
     * 
     * @param player 玩家对象
     * @param character 角色实体
     */
    private void loadPlayerFriends(Player player, CharacterEntity character) {
        try {
            // 从数据库加载好友数据
            List<PlayerFriend> friends = new ArrayList<>();
            
            // 从数据库加载好友数据
            if (character.getFriendData() != null && !character.getFriendData().isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode friendsNode = mapper.readTree(character.getFriendData());
                    
                    if (friendsNode.isArray()) {
                        for (JsonNode friendNode : friendsNode) {
                            String friendName = friendNode.get("name").asText();
                            int status = friendNode.get("status").asInt();
                            String memo = friendNode.get("memo").asText();
                            long addTime = friendNode.get("addTime").asLong();
                            
                            PlayerFriend friend = new PlayerFriend();
                            friend.setName(friendName);
                            friend.setStatus(status);
                            friend.setMemo(memo);
                            friend.setAddTime(addTime);
                            
                            friends.add(friend);
                        }
                    }
                } catch (Exception e) {
                    log.error("解析好友数据失败: {}", character.getName(), e);
                }
            }
            
            player.setFriends(friends);
            log.debug("加载玩家 {} 好友列表", player.getName());
        } catch (Exception e) {
            log.error("加载玩家 {} 好友列表失败", player.getName(), e);
        }
    }
    
    /**
     * 保存玩家数据
     * 
     * @param player 玩家对象
     */
    private void savePlayerData(Player player) {
        Optional<CharacterEntity> characterOpt = characterRepository.findByName(player.getName());
        if (!characterOpt.isPresent()) {
            return;
        }
        
        CharacterEntity character = characterOpt.get();
        
        // 更新基本信息
        character.setLevel(player.getLevel());
        character.setExperience(player.getExperience());
        character.setHp(player.getHp());
        character.setMp(player.getMp());
        character.setMaxHp(player.getMaxHp());
        character.setMaxMp(player.getMaxMp());
        character.setGold(player.getGold());
        character.setGamePoint(player.getGamePoint());
        character.setGameDiamond(player.getGameDiamond());
        character.setPkValue(player.getPkValue());
        character.setReputation(player.getReputation());
        character.setOnlineTime(player.getOnlineTime());
        character.setCurrentMap(player.getMapName());
        character.setCurrentX(player.getX());
        character.setCurrentY(player.getY());
        character.setDirection(player.getDirection());
        character.setGuildName(player.getGuildName());
        character.setGuildRank(player.getGuildRank());
        character.setMasterName(player.getMasterName());
        character.setSpouseName(player.getSpouseName());
        
        // 更新属性
        Player.PlayerAbility ability = player.getAbility();
        character.setStrength(ability.getStrength());
        character.setAgility(ability.getAgility());
        character.setConstitution(ability.getConstitution());
        character.setIntelligence(ability.getIntelligence());
        
        // 保存到数据库
        character.setLastSaveTime(LocalDateTime.now());
        characterRepository.save(character);
        
        log.debug("保存玩家数据: {}", player.getName());
    }
    
    /**
     * 处理技能效果
     * 
     * @param player 玩家对象
     * @param skill 技能对象
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     */
    private void processSkillEffect(Player player, PlayerSkill skill, int targetX, int targetY) {
        Skill skillInfo = skill.getSkill();
        
        // 消耗MP
        int mpCost = skillInfo.getMpCost();
        if (player.getMp() < mpCost) {
            sendChatMessageToPlayer(player, "MP不足，无法使用技能");
            return;
        }
        player.setMp(player.getMp() - mpCost);
        
        // 根据技能类型处理
        switch (skillInfo.getType()) {
            case ATTACK:
                processAttackSkill(player, skill, targetX, targetY);
                break;
            case HEAL:
                processHealSkill(player, skill, targetX, targetY);
                break;
            case BUFF:
                processBuffSkill(player, skill, targetX, targetY);
                break;
            case DEBUFF:
                processDebuffSkill(player, skill, targetX, targetY);
                break;
            case SUMMON:
                processSummonSkill(player, skill, targetX, targetY);
                break;
            case TELEPORT:
                processTeleportSkill(player, skill, targetX, targetY);
                break;
            default:
                log.warn("未知技能类型: {}", skillInfo.getType());
                break;
        }
        
        log.debug("处理技能效果: {} 使用 {} 目标: ({}, {})", 
                player.getName(), skill.getSkill().getName(), targetX, targetY);
    }
    
    /**
     * 处理攻击技能
     */
    private void processAttackSkill(Player player, PlayerSkill skill, int targetX, int targetY) {
        // 查找目标
        GameMap gameMap = mapService.getMap(player.getMapName());
        if (gameMap != null) {
            BaseObject target = gameMap.getObjectAt(targetX, targetY);
            if (target instanceof Monster) {
                Monster monster = (Monster) target;
                // 计算伤害
                int damage = calculateSkillDamage(player, skill, monster);
                monster.takeDamage(damage);
                log.debug("技能攻击: {} 对 {} 造成 {} 点伤害", 
                        player.getName(), monster.getName(), damage);
            }
        }
    }
    
    /**
     * 处理治疗技能
     */
    private void processHealSkill(Player player, PlayerSkill skill, int targetX, int targetY) {
        Skill skillInfo = skill.getSkill();
        int healAmount = skillInfo.getParam1() + skill.getLevel() * 10;
        
        // 治疗自己
        int newHp = Math.min(player.getHp() + healAmount, player.getMaxHp());
        player.setHp(newHp);
        
        sendChatMessageToPlayer(player, "恢复了 " + healAmount + " 点生命值");
        log.debug("治疗技能: {} 恢复 {} 点生命值", player.getName(), healAmount);
    }
    
    /**
     * 处理增益技能
     */
    private void processBuffSkill(Player player, PlayerSkill skill, int targetX, int targetY) {
        Skill skillInfo = skill.getSkill();
        int duration = skillInfo.getParam2() * 1000; // 持续时间（毫秒）
        int power = skillInfo.getParam1() + skill.getLevel() * 5;
        
        // 根据技能子类型处理不同的增益效果
        switch (skillInfo.getSubType()) {
            case 1: // 攻击力提升
                BuffEffect attackBuff = new BuffEffect();
                attackBuff.setType(BuffType.STAT_BOOST);
                attackBuff.setStrengthBonus(power);
                attackBuff.setDuration(duration);
                attackBuff.setStartTime(System.currentTimeMillis());
                attackBuff.setDescription("攻击力提升 " + power + " 点");
                player.addBuffEffect(attackBuff);
                break;
                
            case 2: // 防御力提升
                BuffEffect defenseBuff = new BuffEffect();
                defenseBuff.setType(BuffType.STAT_BOOST);
                defenseBuff.setDefenseBonus(power);
                defenseBuff.setDuration(duration);
                defenseBuff.setStartTime(System.currentTimeMillis());
                defenseBuff.setDescription("防御力提升 " + power + " 点");
                player.addBuffEffect(defenseBuff);
                break;
                
            case 3: // 魔法力提升
                BuffEffect magicBuff = new BuffEffect();
                magicBuff.setType(BuffType.STAT_BOOST);
                magicBuff.setIntelligenceBonus(power);
                magicBuff.setDuration(duration);
                magicBuff.setStartTime(System.currentTimeMillis());
                magicBuff.setDescription("魔法力提升 " + power + " 点");
                player.addBuffEffect(magicBuff);
                break;
                
            case 4: // 生命恢复
                BuffEffect hpRegenBuff = new BuffEffect();
                hpRegenBuff.setType(BuffType.HP_REGEN);
                hpRegenBuff.setHpRegenRate(power);
                hpRegenBuff.setDuration(duration);
                hpRegenBuff.setStartTime(System.currentTimeMillis());
                hpRegenBuff.setDescription("每秒恢复 " + power + " 点生命值");
                player.addBuffEffect(hpRegenBuff);
                break;
                
            case 5: // 魔法恢复
                BuffEffect mpRegenBuff = new BuffEffect();
                mpRegenBuff.setType(BuffType.MP_REGEN);
                mpRegenBuff.setMpRegenRate(power);
                mpRegenBuff.setDuration(duration);
                mpRegenBuff.setStartTime(System.currentTimeMillis());
                mpRegenBuff.setDescription("每秒恢复 " + power + " 点魔法值");
                player.addBuffEffect(mpRegenBuff);
                break;
                
            case 6: // 移动速度提升
                BuffEffect speedBuff = new BuffEffect();
                speedBuff.setType(BuffType.SPEED_BOOST);
                speedBuff.setSpeedBonus(power);
                speedBuff.setDuration(duration);
                speedBuff.setStartTime(System.currentTimeMillis());
                speedBuff.setDescription("移动速度提升 " + power + "%");
                player.addBuffEffect(speedBuff);
                break;
                
            default:
                log.warn("未知的增益技能子类型: {}", skillInfo.getSubType());
                break;
        }
        
        sendChatMessageToPlayer(player, "使用增益技能: " + skillInfo.getName());
        log.debug("增益技能: {} 使用 {}", player.getName(), skillInfo.getName());
    }
    
    /**
     * 处理减益技能
     */
    private void processDebuffSkill(Player player, PlayerSkill skill, int targetX, int targetY) {
        Skill skillInfo = skill.getSkill();
        int duration = skillInfo.getParam2() * 1000; // 持续时间（毫秒）
        int power = skillInfo.getParam1() + skill.getLevel() * 5;
        
        // 查找目标
        GameMap gameMap = mapService.getMap(player.getMapName());
        if (gameMap == null) {
            return;
        }
        
        BaseObject target = gameMap.getObjectAt(targetX, targetY);
        if (target == null) {
            return;
        }
        
        // 根据技能子类型处理不同的减益效果
        DebuffEffect debuffEffect = new DebuffEffect();
        debuffEffect.setDuration(duration);
        debuffEffect.setStartTime(System.currentTimeMillis());
        debuffEffect.setCaster(player.getName());
        
        switch (skillInfo.getSubType()) {
            case 1: // 中毒
                debuffEffect.setType(DebuffType.POISON);
                debuffEffect.setDamagePerSecond(power);
                debuffEffect.setDescription("中毒：每秒失去 " + power + " 点生命值");
                break;
                
            case 2: // 缓慢
                debuffEffect.setType(DebuffType.SLOW);
                debuffEffect.setSpeedReduction(power);
                debuffEffect.setDescription("缓慢：移动速度降低 " + power + "%");
                break;
                
            case 3: // 虚弱
                debuffEffect.setType(DebuffType.WEAKNESS);
                debuffEffect.setAttackReduction(power);
                debuffEffect.setDescription("虚弱：攻击力降低 " + power + " 点");
                break;
                
            case 4: // 沉默
                debuffEffect.setType(DebuffType.SILENCE);
                debuffEffect.setDescription("沉默：无法使用技能");
                break;
                
            case 5: // 眩晕
                debuffEffect.setType(DebuffType.STUN);
                debuffEffect.setDescription("眩晕：无法行动");
                break;
                
            case 6: // 诅咒
                debuffEffect.setType(DebuffType.CURSE);
                debuffEffect.setAllStatsReduction(power);
                debuffEffect.setDescription("诅咒：所有属性降低 " + power + " 点");
                break;
                
            default:
                log.warn("未知的减益技能子类型: {}", skillInfo.getSubType());
                return;
        }
        
        // 应用减益效果
        if (target instanceof Player) {
            Player targetPlayer = (Player) target;
            if (canAttackPlayer(player, targetPlayer)) {
                targetPlayer.addDebuffEffect(debuffEffect);
                sendChatMessageToPlayer(player, "对 " + targetPlayer.getName() + " 施加了减益效果");
                log.debug("减益技能: {} 对 {} 施加了 {}", 
                        player.getName(), targetPlayer.getName(), debuffEffect.getDescription());
            }
        } else if (target instanceof Monster) {
            Monster monster = (Monster) target;
            monster.addDebuffEffect(debuffEffect);
            sendChatMessageToPlayer(player, "对 " + monster.getName() + " 施加了减益效果");
            log.debug("减益技能: {} 对 {} 施加了 {}", 
                    player.getName(), monster.getName(), debuffEffect.getDescription());
        }
    }
    
    /**
     * 处理召唤技能
     */
    private void processSummonSkill(Player player, PlayerSkill skill, int targetX, int targetY) {
        Skill skillInfo = skill.getSkill();
        int duration = skillInfo.getParam2() * 1000; // 召唤持续时间（毫秒）
        int level = skill.getLevel();
        
        // 检查召唤位置是否有效
        GameMap gameMap = mapService.getMap(player.getMapName());
        if (gameMap == null) {
            return;
        }
        
        if (!gameMap.isValidPosition(targetX, targetY)) {
            sendChatMessageToPlayer(player, "召唤位置无效");
            return;
        }
        
        // 检查距离
        double distance = Math.sqrt(Math.pow(targetX - player.getX(), 2) + Math.pow(targetY - player.getY(), 2));
        if (distance > skillInfo.getRange()) {
            sendChatMessageToPlayer(player, "召唤距离过远");
            return;
        }
        
        // 根据技能子类型创建不同的召唤物
        SummonedCreature summon = null;
        
        switch (skillInfo.getSubType()) {
            case 1: // 召唤骷髅
                summon = new SummonedCreature();
                summon.setName("骷髅战士");
                summon.setType(SummonType.SKELETON);
                summon.setLevel(level);
                summon.setMaxHp(100 + level * 20);
                summon.setHp(summon.getMaxHp());
                summon.setAttack(20 + level * 5);
                summon.setDefense(10 + level * 3);
                summon.setMoveSpeed(300);
                break;
                
            case 2: // 召唤僵尸
                summon = new SummonedCreature();
                summon.setName("僵尸");
                summon.setType(SummonType.ZOMBIE);
                summon.setLevel(level);
                summon.setMaxHp(150 + level * 30);
                summon.setHp(summon.getMaxHp());
                summon.setAttack(15 + level * 4);
                summon.setDefense(15 + level * 4);
                summon.setMoveSpeed(250);
                break;
                
            case 3: // 召唤神兽
                summon = new SummonedCreature();
                summon.setName("神兽");
                summon.setType(SummonType.BEAST);
                summon.setLevel(level);
                summon.setMaxHp(200 + level * 40);
                summon.setHp(summon.getMaxHp());
                summon.setAttack(30 + level * 8);
                summon.setDefense(20 + level * 5);
                summon.setMoveSpeed(400);
                break;
                
            case 4: // 召唤火魔
                summon = new SummonedCreature();
                summon.setName("火魔");
                summon.setType(SummonType.FIRE_DEMON);
                summon.setLevel(level);
                summon.setMaxHp(120 + level * 25);
                summon.setHp(summon.getMaxHp());
                summon.setAttack(25 + level * 6);
                summon.setDefense(8 + level * 2);
                summon.setMoveSpeed(350);
                break;
                
            default:
                log.warn("未知的召唤技能子类型: {}", skillInfo.getSubType());
                return;
        }
        
        // 设置召唤物基本信息
        summon.setOwner(player.getName());
        summon.setX(targetX);
        summon.setY(targetY);
        summon.setMapName(player.getMapName());
        summon.setDirection(player.getDirection());
        summon.setCreateTime(System.currentTimeMillis());
        summon.setExpireTime(System.currentTimeMillis() + duration);
        
        // 添加到地图
        gameMap.addObject(summon);
        
        // 添加到玩家召唤列表
        player.addSummonedCreature(summon);
        
        sendChatMessageToPlayer(player, "召唤了 " + summon.getName());
        log.debug("召唤技能: {} 召唤了 {} 位置: ({}, {})", 
                player.getName(), summon.getName(), targetX, targetY);
    }
    
    /**
     * 处理传送技能
     */
    private void processTeleportSkill(Player player, PlayerSkill skill, int targetX, int targetY) {
        Skill skillInfo = skill.getSkill();
        int level = skill.getLevel();
        
        // 检查地图
        GameMap gameMap = mapService.getMap(player.getMapName());
        if (gameMap == null) {
            return;
        }
        
        // 根据技能子类型处理不同的传送效果
        switch (skillInfo.getSubType()) {
            case 1: // 瞬间移动
                performInstantTeleport(player, targetX, targetY, gameMap);
                break;
                
            case 2: // 随机传送
                performRandomTeleport(player, level, gameMap);
                break;
                
            case 3: // 回城
                performTownTeleport(player);
                break;
                
            case 4: // 地牢逃脱
                performDungeonEscape(player, gameMap);
                break;
                
            default:
                log.warn("未知的传送技能子类型: {}", skillInfo.getSubType());
                return;
        }
        
        log.debug("传送技能: {} 使用 {}", player.getName(), skillInfo.getName());
    }
    
    /**
     * 执行瞬间移动
     */
    private void performInstantTeleport(Player player, int targetX, int targetY, GameMap gameMap) {
        // 检查目标位置是否有效
        if (!gameMap.isValidPosition(targetX, targetY)) {
            sendChatMessageToPlayer(player, "目标位置无效");
            return;
        }
        
        // 检查距离限制
        double distance = Math.sqrt(Math.pow(targetX - player.getX(), 2) + Math.pow(targetY - player.getY(), 2));
        if (distance > 10) {
            sendChatMessageToPlayer(player, "传送距离过远");
            return;
        }
        
        // 移动到目标位置
        player.setX(targetX);
        player.setY(targetY);
        
        sendChatMessageToPlayer(player, "瞬间移动到 (" + targetX + ", " + targetY + ")");
        log.debug("瞬间移动: {} 移动到 ({}, {})", player.getName(), targetX, targetY);
    }
    
    /**
     * 执行随机传送
     */
    private void performRandomTeleport(Player player, int level, GameMap gameMap) {
        int range = 5 + level * 2; // 随机传送范围
        int attempts = 0;
        int maxAttempts = 10;
        
        while (attempts < maxAttempts) {
            int randomX = player.getX() + (int) (Math.random() * range * 2 - range);
            int randomY = player.getY() + (int) (Math.random() * range * 2 - range);
            
            if (gameMap.isValidPosition(randomX, randomY)) {
                player.setX(randomX);
                player.setY(randomY);
                
                sendChatMessageToPlayer(player, "随机传送到 (" + randomX + ", " + randomY + ")");
                log.debug("随机传送: {} 传送到 ({}, {})", player.getName(), randomX, randomY);
                return;
            }
            
            attempts++;
        }
        
        sendChatMessageToPlayer(player, "随机传送失败");
        log.debug("随机传送失败: {} 无法找到有效位置", player.getName());
    }
    
    /**
     * 执行回城传送
     */
    private void performTownTeleport(Player player) {
        // 根据职业确定默认回城位置
        String townMap = "比奇城";
        int townX = 330;
        int townY = 330;
        
        if (teleportPlayer(player.getName(), townMap, townX, townY)) {
            sendChatMessageToPlayer(player, "回城成功");
            log.debug("回城传送: {} 传送到 {} ({}, {})", player.getName(), townMap, townX, townY);
        } else {
            sendChatMessageToPlayer(player, "回城失败");
            log.debug("回城传送失败: {}", player.getName());
        }
    }
    
    /**
     * 执行地牢逃脱
     */
    private void performDungeonEscape(Player player, GameMap gameMap) {
        // 检查是否在地牢地图
        if (!isDungeonMap(gameMap.getName())) {
            sendChatMessageToPlayer(player, "此技能只能在地牢中使用");
            return;
        }
        
        // 传送到地牢入口
        String exitMap = "比奇城";
        int exitX = 330;
        int exitY = 330;
        
        if (teleportPlayer(player.getName(), exitMap, exitX, exitY)) {
            sendChatMessageToPlayer(player, "地牢逃脱成功");
            log.debug("地牢逃脱: {} 从 {} 逃脱到 {} ({}, {})", 
                    player.getName(), gameMap.getName(), exitMap, exitX, exitY);
        } else {
            sendChatMessageToPlayer(player, "地牢逃脱失败");
            log.debug("地牢逃脱失败: {}", player.getName());
        }
    }
    
    /**
     * 检查是否为地牢地图
     */
    private boolean isDungeonMap(String mapName) {
        return mapName.contains("地牢") || mapName.contains("洞穴") || 
               mapName.contains("迷宫") || mapName.contains("矿洞");
    }
    
    /**
     * 检查玩家是否可以攻击目标玩家
     */
    private boolean canAttackPlayer(Player attacker, Player target) {
        // 检查PK模式
        if (attacker.getPkMode() == 0) {
            return false; // 和平模式不能攻击
        }
        
        // 检查同行会
        if (attacker.getGuildName() != null && attacker.getGuildName().equals(target.getGuildName())) {
            return false; // 同行会不能攻击
        }
        
        // 检查师徒关系
        if (attacker.getMasterName() != null && attacker.getMasterName().equals(target.getName())) {
            return false; // 不能攻击师父
        }
        
        if (target.getMasterName() != null && target.getMasterName().equals(attacker.getName())) {
            return false; // 不能攻击徒弟
        }
        
        // 检查婚姻关系
        if (attacker.getSpouseName() != null && attacker.getSpouseName().equals(target.getName())) {
            return false; // 不能攻击配偶
        }
        
        return true;
    }
    
    /**
     * 计算技能伤害
     */
    private int calculateSkillDamage(Player player, PlayerSkill skill, Monster target) {
        Skill skillInfo = skill.getSkill();
        int baseDamage = skillInfo.getParam1();
        int skillLevel = skill.getLevel();
        
        // 基础伤害 + 技能等级加成
        int damage = baseDamage + skillLevel * 5;
        
        // 根据玩家属性调整伤害
        switch (player.getJob()) {
            case WARRIOR:
                damage += player.getAbility().getStrength() * 2;
                break;
            case WIZARD:
                damage += player.getAbility().getIntelligence() * 3;
                break;
            case TAOIST:
                damage += player.getAbility().getIntelligence() * 2;
                break;
        }
        
        // 随机浮动 ±20%
        int variance = damage * 20 / 100;
        damage = damage - variance + (int)(Math.random() * variance * 2);
        
        return Math.max(1, damage);
    }
    
    /**
     * 发送普通聊天
     * 
     * @param player 玩家对象
     * @param message 消息内容
     */
    private void sendNormalChat(Player player, String message) {
        GameMap gameMap = mapService.getMap(player.getMapName());
        if (gameMap == null) {
            return;
        }
        
        // 获取附近的玩家
        List<BaseObject> nearbyObjects = gameMap.getObjectsInRange(player.getX(), player.getY(), 10);
        
        String chatMessage = String.format("[普通] %s: %s", player.getName(), message);
        
        // 发送给附近的玩家
        for (BaseObject obj : nearbyObjects) {
            if (obj instanceof Player) {
                Player nearbyPlayer = (Player) obj;
                // 发送聊天消息给附近玩家
                sendChatMessageToPlayer(nearbyPlayer, chatMessage);
                log.debug("发送聊天给 {}: {}", nearbyPlayer.getName(), message);
            }
        }
    }
    
    /**
     * 发送私聊
     * 
     * @param player 玩家对象
     * @param message 消息内容格式: "目标玩家名 消息内容"
     */
    private void sendPrivateChat(Player player, String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        
        // 解析私聊消息格式：目标玩家名 消息内容
        String[] parts = message.split(" ", 2);
        if (parts.length < 2) {
            log.warn("私聊消息格式错误: {}", message);
            return;
        }
        
        String targetPlayerName = parts[0].trim();
        String chatContent = parts[1].trim();
        
        // 查找目标玩家
        Player targetPlayer = onlinePlayers.get(targetPlayerName);
        if (targetPlayer == null) {
            String errorMessage = String.format("玩家 %s 不在线", targetPlayerName);
            sendChatMessageToPlayer(player, errorMessage);
            log.debug("私聊目标玩家 {} 不在线", targetPlayerName);
            return;
        }
        
        String privateMessage = String.format("[私聊] %s 对你说: %s", player.getName(), chatContent);
        String senderMessage = String.format("[私聊] 你对 %s 说: %s", targetPlayerName, chatContent);
        
        // 发送私聊消息给目标玩家
        sendChatMessageToPlayer(targetPlayer, privateMessage);
        // 发送私聊确认消息给发送者
        sendChatMessageToPlayer(player, senderMessage);
        
        log.info("私聊: {} -> {}: {}", player.getName(), targetPlayerName, chatContent);
    }
    
    /**
     * 发送聊天消息给玩家
     * 
     * @param player 玩家对象
     * @param message 消息内容
     */
    private void sendChatMessageToPlayer(Player player, String message) {
        // 通过网络发送消息给玩家
        try {
            // 构造聊天消息包
            // MessagePacket packet = new MessagePacket(); // Removed as per edit hint
            // packet.setType(MessageType.CHAT); // Removed as per edit hint
            // packet.setContent(message); // Removed as per edit hint
            // packet.setTimestamp(System.currentTimeMillis()); // Removed as per edit hint
            
            // 获取玩家的会话ID
            String sessionId = playerSessions.get(player.getName());
            if (sessionId != null) {
                // 发送网络消息
                // networkService.sendMessage(sessionId, packet); // Removed as per edit hint
                log.debug("发送消息给 {}: {}", player.getName(), message);
            } else {
                log.warn("玩家 {} 的会话ID不存在，无法发送消息", player.getName());
            }
            
        } catch (Exception e) {
            log.error("发送消息给玩家 {} 失败: {}", player.getName(), message, e);
        }
    }
    
    /**
     * 发送行会聊天
     * 
     * @param player 玩家对象
     * @param message 消息内容
     */
    private void sendGuildChat(Player player, String message) {
        if (player.getGuildName() == null || player.getGuildName().isEmpty()) {
            sendChatMessageToPlayer(player, "您还没有加入行会");
            return;
        }
        
        // 获取行会成员
        Guild guild = guildService.getGuild(player.getGuildName());
        if (guild == null) {
            sendChatMessageToPlayer(player, "行会不存在");
            return;
        }
        
        String guildMessage = String.format("[行会] %s: %s", player.getName(), message);
        
        // 发送给所有在线的行会成员
        for (Guild.GuildMember member : guild.getMembers()) {
            Player guildPlayer = onlinePlayers.get(member.getPlayerName());
            if (guildPlayer != null) {
                sendChatMessageToPlayer(guildPlayer, guildMessage);
                log.debug("发送行会聊天给 {}: {}", guildPlayer.getName(), message);
            }
        }
    }
    
    /**
     * 发送喊话
     * 
     * @param player 玩家对象
     * @param message 消息内容
     */
    private void sendShoutChat(Player player, String message) {
        // 检查玩家是否有足够的金币进行喊话
        int shoutCost = 1000; // 喊话需要1000金币
        if (player.getGold() < shoutCost) {
            sendChatMessageToPlayer(player, "金币不足，喊话需要" + shoutCost + "金币");
            return;
        }
        
        // 扣除金币
        player.setGold(player.getGold() - shoutCost);
        
        String shoutMessage = String.format("[喊话] %s: %s", player.getName(), message);
        
        // 发送给所有在线玩家
        for (Player onlinePlayer : onlinePlayers.values()) {
            sendChatMessageToPlayer(onlinePlayer, shoutMessage);
            log.debug("发送喊话给 {}: {}", onlinePlayer.getName(), message);
        }
        
        log.info("玩家 {} 喊话: {} (花费{}金币)", player.getName(), message, shoutCost);
    }
} 