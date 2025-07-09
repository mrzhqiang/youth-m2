package com.mir2.service;

import com.mir2.core.model.Player;
import com.mir2.core.model.Item;
import com.mir2.core.model.Position;
import com.mir2.entity.Quest;
import com.mir2.entity.PlayerQuest;
import com.mir2.entity.Inventory;
import com.mir2.entity.Mail;
import com.mir2.entity.MailAttachment;
import com.mir2.entity.PlayerQuestEntity;
import com.mir2.repository.PlayerQuestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 任务服务类
 * 
 * @author Mir2 Team
 */
@Service
@Slf4j
public class QuestService {
    
    /** 任务模板缓存 */
    private final Map<Integer, Quest> questTemplates = new ConcurrentHashMap<>();
    
    /** 玩家任务缓存 */
    private final Map<String, Map<Integer, PlayerQuest>> playerQuests = new ConcurrentHashMap<>();
    
    /** 日常任务重置时间 */
    private final Map<String, Long> dailyQuestResetTime = new ConcurrentHashMap<>();
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private SkillService skillService;
    
    @Autowired
    private MailService mailService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private PlayerQuestRepository playerQuestRepository;
    
    /**
     * 初始化任务模板
     */
    public void initializeQuestTemplates() {
        // 创建一些示例任务
        createSampleQuests();
    }
    
    /**
     * 创建示例任务
     */
    private void createSampleQuests() {
        // 新手任务1：升级到10级
        Quest newbieQuest1 = createLevelUpQuest(1001, "新手成长", "达到10级", 1, 10);
        questTemplates.put(1001, newbieQuest1);
        
        // 新手任务2：杀怪任务
        Quest newbieQuest2 = createKillMonsterQuest(1002, "清理鸡群", "杀死20只鸡", 10, 1001, 20);
        questTemplates.put(1002, newbieQuest2);
        
        // 收集任务：收集木材
        Quest collectQuest = createCollectItemQuest(1003, "收集木材", "收集10个木材", 5, null, 1001, 10);
        questTemplates.put(1003, collectQuest);
        
        // 日常任务：杀怪日常
        Quest dailyQuest = createDailyKillQuest(2001, "日常清理", "每日杀死50只怪物", 20, 2001, 50);
        questTemplates.put(2001, dailyQuest);
        
        log.info("已初始化 {} 个任务模板", questTemplates.size());
    }
    
    /**
     * 创建升级任务
     */
    private Quest createLevelUpQuest(int questId, String name, String description, int minLevel, int targetLevel) {
        Quest quest = new Quest();
        quest.setQuestId(questId);
        quest.setName(name);
        quest.setDescription(description);
        quest.setType(Quest.QuestType.MAIN);
        quest.setStatus(Quest.QuestStatus.AVAILABLE);
        quest.setMinLevel(minLevel);
        quest.setMaxLevel(999);
        quest.setRepeatable(false);
        quest.setCreatedTime(new Date());
        
        // 创建升级目标
        Quest.QuestObjective objective = new Quest.QuestObjective();
        objective.setObjectiveId(1);
        objective.setType(Quest.QuestObjective.ObjectiveType.LEVEL_UP);
        objective.setDescription("达到" + targetLevel + "级");
        objective.setRequiredAmount(targetLevel);
        objective.setCurrentProgress(0);
        objective.setCompleted(false);
        objective.setOptional(false);
        
        quest.setObjectives(Arrays.asList(objective));
        
        // 创建奖励
        Quest.QuestReward reward = new Quest.QuestReward();
        reward.setExperience(targetLevel * 1000L);
        reward.setGold(targetLevel * 100);
        reward.setReputation(50);
        quest.setReward(reward);
        
        return quest;
    }
    
    /**
     * 创建杀怪任务
     */
    private Quest createKillMonsterQuest(int questId, String name, String description, int minLevel, Integer previousQuest, int killCount) {
        Quest quest = new Quest();
        quest.setQuestId(questId);
        quest.setName(name);
        quest.setDescription(description);
        quest.setType(Quest.QuestType.MAIN);
        quest.setStatus(Quest.QuestStatus.AVAILABLE);
        quest.setMinLevel(minLevel);
        quest.setMaxLevel(999);
        quest.setPreviousQuestId(previousQuest);
        quest.setRepeatable(false);
        quest.setCreatedTime(new Date());
        
        // 创建杀怪目标
        Quest.QuestObjective objective = new Quest.QuestObjective();
        objective.setObjectiveId(1);
        objective.setType(Quest.QuestObjective.ObjectiveType.KILL_MONSTER);
        objective.setDescription("杀死" + killCount + "只怪物");
        objective.setRequiredAmount(killCount);
        objective.setCurrentProgress(0);
        objective.setCompleted(false);
        objective.setOptional(false);
        
        // 设置目标参数
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("monsterId", 1001); // 鸡的ID
        parameters.put("mapName", "新手村");
        objective.setParameters(parameters);
        
        quest.setObjectives(Arrays.asList(objective));
        
        // 创建奖励
        Quest.QuestReward reward = new Quest.QuestReward();
        reward.setExperience(killCount * 100L);
        reward.setGold(killCount * 10);
        reward.setReputation(10);
        quest.setReward(reward);
        
        return quest;
    }
    
    /**
     * 创建收集物品任务
     */
    private Quest createCollectItemQuest(int questId, String name, String description, int minLevel, Integer previousQuest, int itemId, int quantity) {
        Quest quest = new Quest();
        quest.setQuestId(questId);
        quest.setName(name);
        quest.setDescription(description);
        quest.setType(Quest.QuestType.SIDE);
        quest.setStatus(Quest.QuestStatus.AVAILABLE);
        quest.setMinLevel(minLevel);
        quest.setMaxLevel(999);
        quest.setPreviousQuestId(previousQuest);
        quest.setRepeatable(false);
        quest.setCreatedTime(new Date());
        
        // 创建收集目标
        Quest.QuestObjective objective = new Quest.QuestObjective();
        objective.setObjectiveId(1);
        objective.setType(Quest.QuestObjective.ObjectiveType.COLLECT_ITEM);
        objective.setDescription("收集" + quantity + "个物品");
        objective.setRequiredAmount(quantity);
        objective.setCurrentProgress(0);
        objective.setCompleted(false);
        objective.setOptional(false);
        
        // 设置目标参数
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("itemId", itemId);
        objective.setParameters(parameters);
        
        quest.setObjectives(Arrays.asList(objective));
        
        // 创建奖励
        Quest.QuestReward reward = new Quest.QuestReward();
        reward.setExperience(quantity * 200L);
        reward.setGold(quantity * 50);
        reward.setReputation(20);
        quest.setReward(reward);
        
        return quest;
    }
    
    /**
     * 创建日常杀怪任务
     */
    private Quest createDailyKillQuest(int questId, String name, String description, int minLevel, int monsterId, int killCount) {
        Quest quest = new Quest();
        quest.setQuestId(questId);
        quest.setName(name);
        quest.setDescription(description);
        quest.setType(Quest.QuestType.DAILY);
        quest.setStatus(Quest.QuestStatus.AVAILABLE);
        quest.setMinLevel(minLevel);
        quest.setMaxLevel(999);
        quest.setRepeatable(true);
        quest.setRepeatInterval(24 * 60 * 60 * 1000L); // 24小时
        quest.setCreatedTime(new Date());
        
        // 创建杀怪目标
        Quest.QuestObjective objective = new Quest.QuestObjective();
        objective.setObjectiveId(1);
        objective.setType(Quest.QuestObjective.ObjectiveType.KILL_MONSTER);
        objective.setDescription("杀死" + killCount + "只怪物");
        objective.setRequiredAmount(killCount);
        objective.setCurrentProgress(0);
        objective.setCompleted(false);
        objective.setOptional(false);
        
        // 设置目标参数
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("monsterId", monsterId);
        objective.setParameters(parameters);
        
        quest.setObjectives(Arrays.asList(objective));
        
        // 创建奖励
        Quest.QuestReward reward = new Quest.QuestReward();
        reward.setExperience(killCount * 50L);
        reward.setGold(killCount * 20);
        reward.setReputation(100);
        quest.setReward(reward);
        
        return quest;
    }
    
    /**
     * 获取玩家可接受的任务列表
     */
    public List<Quest> getAvailableQuests(String playerName) {
        Player player = playerService.getPlayer(playerName);
        if (player == null) {
            return new ArrayList<>();
        }
        
        Map<Integer, PlayerQuest> playerQuestMap = playerQuests.getOrDefault(playerName, new HashMap<>());
        
        return questTemplates.values().stream()
                .filter(quest -> canAcceptQuest(player, quest, playerQuestMap))
                .collect(Collectors.toList());
    }
    
    /**
     * 检查玩家是否可以接受任务
     */
    private boolean canAcceptQuest(Player player, Quest quest, Map<Integer, PlayerQuest> playerQuestMap) {
        // 检查等级要求
        if (player.getLevel() < quest.getMinLevel() || player.getLevel() > quest.getMaxLevel()) {
            return false;
        }
        
        // 检查职业限制
        if (quest.getJobRestriction() != null && !quest.getJobRestriction().isEmpty()) {
            if (!quest.getJobRestriction().equals(player.getJob().name())) {
                return false;
            }
        }
        
        // 检查前置任务
        if (quest.getPreviousQuestId() != null) {
            PlayerQuest previousQuest = playerQuestMap.get(quest.getPreviousQuestId());
            if (previousQuest == null || previousQuest.getStatus() != Quest.QuestStatus.TURNED_IN) {
                return false;
            }
        }
        
        // 检查是否已接受
        PlayerQuest playerQuest = playerQuestMap.get(quest.getQuestId());
        if (playerQuest != null) {
            // 检查是否可以重复
            if (quest.isRepeatable() && playerQuest.canRepeat(quest)) {
                return true;
            }
            return false;
        }
        
        return true;
    }
    
    /**
     * 接受任务
     */
    @Transactional
    public QuestResult acceptQuest(String playerName, int questId) {
        try {
            Player player = playerService.getPlayer(playerName);
            if (player == null) {
                return new QuestResult(false, "玩家不存在");
            }
            
            Quest questTemplate = questTemplates.get(questId);
            if (questTemplate == null) {
                return new QuestResult(false, "任务不存在");
            }
            
            Map<Integer, PlayerQuest> playerQuestMap = playerQuests.computeIfAbsent(playerName, k -> new ConcurrentHashMap<>());
            
            if (!canAcceptQuest(player, questTemplate, playerQuestMap)) {
                return new QuestResult(false, "无法接受此任务");
            }
            
            // 创建玩家任务
            PlayerQuest playerQuest = createPlayerQuest(player, questTemplate);
            playerQuestMap.put(questId, playerQuest);
            
            // 保存到数据库
            savePlayerQuest(playerQuest);
            
            log.info("玩家 {} 接受任务: {}", playerName, questTemplate.getName());
            return new QuestResult(true, "任务接受成功");
            
        } catch (Exception e) {
            log.error("接受任务失败", e);
            return new QuestResult(false, "系统错误");
        }
    }
    
    /**
     * 创建玩家任务
     */
    private PlayerQuest createPlayerQuest(Player player, Quest questTemplate) {
        PlayerQuest playerQuest = new PlayerQuest();
        playerQuest.setPlayerQuestId(UUID.randomUUID().toString());
        playerQuest.setPlayerName(player.getName());
        playerQuest.setQuestId(questTemplate.getQuestId());
        playerQuest.setStatus(Quest.QuestStatus.IN_PROGRESS);
        playerQuest.setAcceptTime(new Date());
        playerQuest.setLastUpdateTime(new Date());
        playerQuest.setRepeatCount(0);
        playerQuest.setRewardReceived(false);
        
        // 设置过期时间
        if (questTemplate.getTimeLimit() != null) {
            Date expireTime = new Date(System.currentTimeMillis() + questTemplate.getTimeLimit());
            playerQuest.setExpireTime(expireTime);
        }
        
        // 初始化任务进度
        List<PlayerQuest.QuestProgress> progressList = new ArrayList<>();
        for (Quest.QuestObjective objective : questTemplate.getObjectives()) {
            PlayerQuest.QuestProgress progress = new PlayerQuest.QuestProgress();
            progress.setObjectiveId(objective.getObjectiveId());
            progress.setType(objective.getType());
            progress.setCurrentProgress(0);
            progress.setRequiredAmount(objective.getRequiredAmount());
            progress.setCompleted(false);
            progress.setLastUpdateTime(new Date());
            progress.setParameters(objective.getParameters());
            progressList.add(progress);
        }
        playerQuest.setProgress(progressList);
        
        return playerQuest;
    }
    
    /**
     * 更新任务进度
     */
    @Transactional
    public void updateQuestProgress(String playerName, Quest.QuestObjective.ObjectiveType type, Map<String, Object> parameters) {
        Map<Integer, PlayerQuest> playerQuestMap = playerQuests.get(playerName);
        if (playerQuestMap == null) {
            return;
        }
        
        for (PlayerQuest playerQuest : playerQuestMap.values()) {
            if (playerQuest.getStatus() != Quest.QuestStatus.IN_PROGRESS) {
                continue;
            }
            
            // 检查是否过期
            if (playerQuest.isExpired()) {
                playerQuest.setStatus(Quest.QuestStatus.EXPIRED);
                continue;
            }
            
            // 更新匹配的目标进度
            boolean updated = false;
            for (PlayerQuest.QuestProgress progress : playerQuest.getProgress()) {
                if (progress.getType() == type && !progress.isCompleted()) {
                    if (isObjectiveMatching(progress, parameters)) {
                        progress.addProgress(1);
                        updated = true;
                        log.debug("更新任务进度: {} - {} ({}/{})", 
                                playerQuest.getQuestId(), type, progress.getCurrentProgress(), progress.getRequiredAmount());
                    }
                }
            }
            
            if (updated) {
                playerQuest.setLastUpdateTime(new Date());
                
                // 检查任务是否完成
                if (playerQuest.isCompleted()) {
                    playerQuest.setStatus(Quest.QuestStatus.COMPLETED);
                    playerQuest.setCompleteTime(new Date());
                    log.info("玩家 {} 完成任务: {}", playerName, playerQuest.getQuestId());
                }
                
                // 保存到数据库
                savePlayerQuest(playerQuest);
            }
        }
    }
    
    /**
     * 检查目标是否匹配
     */
    private boolean isObjectiveMatching(PlayerQuest.QuestProgress progress, Map<String, Object> parameters) {
        if (progress.getParameters() == null || parameters == null) {
            return true;
        }
        
        // 检查怪物ID匹配
        if (progress.getParameters().containsKey("monsterId") && parameters.containsKey("monsterId")) {
            return progress.getParameters().get("monsterId").equals(parameters.get("monsterId"));
        }
        
        // 检查物品ID匹配
        if (progress.getParameters().containsKey("itemId") && parameters.containsKey("itemId")) {
            return progress.getParameters().get("itemId").equals(parameters.get("itemId"));
        }
        
        // 检查地图匹配
        if (progress.getParameters().containsKey("mapName") && parameters.containsKey("mapName")) {
            return progress.getParameters().get("mapName").equals(parameters.get("mapName"));
        }
        
        return true;
    }
    
    /**
     * 交付任务
     */
    @Transactional
    public QuestResult turnInQuest(String playerName, int questId, Integer rewardChoice) {
        try {
            Map<Integer, PlayerQuest> playerQuestMap = playerQuests.get(playerName);
            if (playerQuestMap == null) {
                return new QuestResult(false, "没有找到玩家任务");
            }
            
            PlayerQuest playerQuest = playerQuestMap.get(questId);
            if (playerQuest == null) {
                return new QuestResult(false, "没有找到指定任务");
            }
            
            // 检查任务是否完成
            if (!playerQuest.isCompleted()) {
                return new QuestResult(false, "任务尚未完成");
            }
            
            // 检查是否可以交付
            if (!playerQuest.canTurnIn()) {
                return new QuestResult(false, "任务无法交付");
            }
            
            Quest questTemplate = questTemplates.get(questId);
            if (questTemplate == null) {
                return new QuestResult(false, "任务模板不存在");
            }
            
            // 发放奖励
            QuestResult rewardResult = giveQuestReward(playerName, questTemplate, rewardChoice);
            if (!rewardResult.isSuccess()) {
                return rewardResult;
            }
            
            // 更新任务状态
            playerQuest.setStatus(Quest.QuestStatus.TURNED_IN);
            playerQuest.setTurnInTime(new Date());
            
            // 移除任务（如果不是可重复任务）
            if (!questTemplate.isRepeatable()) {
                playerQuestMap.remove(questId);
            } else {
                // 重置可重复任务
                playerQuest.resetForRepeat();
            }
            
            // 保存任务数据
            savePlayerQuests(playerName, new ArrayList<>(playerQuestMap.values()));
            
            // 发送任务完成通知
            notificationService.sendQuestNotification(playerName, questTemplate.getName(), "任务完成！");
            
            log.info("玩家 {} 完成任务: {}", playerName, questTemplate.getName());
            
            return new QuestResult(true, "任务交付成功");
            
        } catch (Exception e) {
            log.error("交付任务失败: player={}, questId={}", playerName, questId, e);
            return new QuestResult(false, "任务交付失败: " + e.getMessage());
        }
    }
    
    /**
     * 发放任务奖励
     */
    private QuestResult giveQuestReward(String playerName, Quest quest, Integer rewardChoice) {
        try {
            Quest.QuestReward reward = quest.getReward();
            if (reward == null) {
                return new QuestResult(true, "该任务没有奖励");
            }
            
            Player player = playerService.getPlayerByName(playerName);
            if (player == null) {
                return new QuestResult(false, "玩家不存在");
            }
            
            // 检查背包空间
            int requiredSlots = 0;
            if (reward.getItems() != null) {
                requiredSlots += reward.getItems().size();
            }
            if (reward.getChoiceItems() != null && rewardChoice != null) {
                requiredSlots += 1;
            }
            
            // 如果背包空间不足，通过邮件发送奖励
            if (requiredSlots > 0 && !inventoryService.hasSpace(playerName, requiredSlots)) {
                return sendQuestRewardByMail(playerName, quest, rewardChoice);
            }
            
            // 发放基础奖励
            if (reward.getExperience() > 0) {
                playerService.addExperience(playerName, reward.getExperience());
            }
            
            if (reward.getGold() > 0) {
                playerService.addGold(playerName, reward.getGold());
            }
            
            if (reward.getReputation() > 0) {
                playerService.addReputation(playerName, reward.getReputation());
            }
            
            // 发放物品奖励
            if (reward.getItems() != null) {
                for (Quest.QuestReward.ItemReward itemReward : reward.getItems()) {
                    Item item = itemService.getItemById(itemReward.getItemId());
                    if (item != null) {
                        inventoryService.addItem(playerName, item, itemReward.getQuantity());
                    }
                }
            }
            
            // 发放可选择奖励
            if (reward.getChoiceItems() != null && rewardChoice != null) {
                if (rewardChoice >= 0 && rewardChoice < reward.getChoiceItems().size()) {
                    Quest.QuestReward.ItemReward choiceReward = reward.getChoiceItems().get(rewardChoice);
                    Item item = itemService.getItemById(choiceReward.getItemId());
                    if (item != null) {
                        inventoryService.addItem(playerName, item, choiceReward.getQuantity());
                    }
                }
            }
            
            // 发放技能奖励
            if (reward.getSkills() != null) {
                for (Quest.QuestReward.SkillReward skillReward : reward.getSkills()) {
                    skillService.learnSkill(playerName, skillReward.getSkillId(), skillReward.getLevel());
                }
            }
            
            // 发放特殊奖励
            if (reward.getSpecialRewards() != null) {
                for (Map.Entry<String, Object> entry : reward.getSpecialRewards().entrySet()) {
                    handleSpecialReward(playerName, entry.getKey(), entry.getValue());
                }
            }
            
            return new QuestResult(true, "奖励发放成功");
            
        } catch (Exception e) {
            log.error("发放任务奖励失败: player={}, quest={}", playerName, quest.getName(), e);
            return new QuestResult(false, "奖励发放失败: " + e.getMessage());
        }
    }
    
    /**
     * 通过邮件发送任务奖励
     */
    private QuestResult sendQuestRewardByMail(String playerName, Quest quest, Integer rewardChoice) {
        try {
            Quest.QuestReward reward = quest.getReward();
            if (reward == null) {
                return new QuestResult(true, "该任务没有奖励");
            }
            
            String subject = "任务奖励: " + quest.getName();
            String content = "恭喜您完成了任务【" + quest.getName() + "】！\n\n由于您的背包空间不足，奖励已通过邮件发送。请及时领取！";
            
            List<MailAttachment> attachments = new ArrayList<>();
            int goldAmount = 0;
            
            // 准备物品附件
            if (reward.getItems() != null) {
                for (Quest.QuestReward.ItemReward itemReward : reward.getItems()) {
                    Item item = itemService.getItemById(itemReward.getItemId());
                    if (item != null) {
                        MailAttachment attachment = MailAttachment.createItemAttachment(
                            "", // mailId会在邮件创建时设置
                            itemReward.getItemId(),
                            item.getName(),
                            itemReward.getQuantity(),
                            itemReward.isBound()
                        );
                        attachments.add(attachment);
                    }
                }
            }
            
            // 准备可选择奖励
            if (reward.getChoiceItems() != null && rewardChoice != null) {
                if (rewardChoice >= 0 && rewardChoice < reward.getChoiceItems().size()) {
                    Quest.QuestReward.ItemReward choiceReward = reward.getChoiceItems().get(rewardChoice);
                    Item item = itemService.getItemById(choiceReward.getItemId());
                    if (item != null) {
                        MailAttachment attachment = MailAttachment.createItemAttachment(
                            "", // mailId会在邮件创建时设置
                            choiceReward.getItemId(),
                            item.getName(),
                            choiceReward.getQuantity(),
                            choiceReward.isBound()
                        );
                        attachments.add(attachment);
                    }
                }
            }
            
            // 准备金币奖励
            if (reward.getGold() > 0) {
                goldAmount = reward.getGold();
            }
            
            // 准备经验奖励
            if (reward.getExperience() > 0) {
                MailAttachment expAttachment = MailAttachment.createExperienceAttachment(
                    "", // mailId会在邮件创建时设置
                    reward.getExperience()
                );
                attachments.add(expAttachment);
            }
            
            // 发送邮件
            MailService.MailResult mailResult = mailService.sendSystemRewardMail(
                playerName, subject, content, Mail.MailType.QUEST_REWARD, attachments, goldAmount
            );
            
            if (mailResult.isSuccess()) {
                // 发放非物品奖励
                if (reward.getReputation() > 0) {
                    playerService.addReputation(playerName, reward.getReputation());
                }
                
                // 发放技能奖励
                if (reward.getSkills() != null) {
                    for (Quest.QuestReward.SkillReward skillReward : reward.getSkills()) {
                        skillService.learnSkill(playerName, skillReward.getSkillId(), skillReward.getLevel());
                    }
                }
                
                return new QuestResult(true, "奖励已通过邮件发送");
            } else {
                return new QuestResult(false, "发送奖励邮件失败: " + mailResult.getMessage());
            }
            
        } catch (Exception e) {
            log.error("通过邮件发送任务奖励失败: player={}, quest={}", playerName, quest.getName(), e);
            return new QuestResult(false, "发送奖励邮件失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理特殊奖励
     */
    private void handleSpecialReward(String playerName, String rewardType, Object rewardValue) {
        try {
            switch (rewardType.toUpperCase()) {
                case "TITLE":
                    // 给予称号
                    playerService.addTitle(playerName, (String) rewardValue);
                    break;
                case "BUFF":
                    // 给予BUFF
                    playerService.addBuff(playerName, (String) rewardValue);
                    break;
                case "GUILD_CONTRIBUTION":
                    // 增加行会贡献
                    playerService.addGuildContribution(playerName, (Integer) rewardValue);
                    break;
                case "INVENTORY_EXPANSION":
                    // 扩展背包
                    inventoryService.expandInventory(playerName, (Integer) rewardValue);
                    break;
                default:
                    log.warn("未知的特殊奖励类型: {}", rewardType);
                    break;
            }
        } catch (Exception e) {
            log.error("处理特殊奖励失败: player={}, type={}, value={}", playerName, rewardType, rewardValue, e);
        }
    }
    
    /**
     * 发送任务奖励邮件（通用方法）
     */
    public void sendQuestRewardMail(String playerName, String questName, String description, 
                                   List<MailAttachment> attachments, int goldAmount) {
        try {
            String subject = "任务奖励: " + questName;
            String content = description != null ? description : "恭喜您完成了任务！";
            
            MailService.MailResult result = mailService.sendSystemRewardMail(
                playerName, subject, content, Mail.MailType.QUEST_REWARD, attachments, goldAmount
            );
            
            if (result.isSuccess()) {
                log.info("任务奖励邮件发送成功: player={}, quest={}", playerName, questName);
                notificationService.sendQuestNotification(playerName, questName, "任务奖励已通过邮件发送");
            } else {
                log.error("任务奖励邮件发送失败: player={}, quest={}, error={}", playerName, questName, result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("发送任务奖励邮件失败: player={}, quest={}", playerName, questName, e);
        }
    }
    
    /**
     * 获取玩家任务列表
     */
    public List<PlayerQuest> getPlayerQuests(String playerName) {
        Map<Integer, PlayerQuest> playerQuestMap = playerQuests.get(playerName);
        if (playerQuestMap == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(playerQuestMap.values());
    }
    
    /**
     * 放弃任务
     */
    @Transactional
    public QuestResult abandonQuest(String playerName, int questId) {
        try {
            Map<Integer, PlayerQuest> playerQuestMap = playerQuests.get(playerName);
            if (playerQuestMap == null) {
                return new QuestResult(false, "没有找到任务");
            }
            
            PlayerQuest playerQuest = playerQuestMap.get(questId);
            if (playerQuest == null) {
                return new QuestResult(false, "任务不存在");
            }
            
            if (playerQuest.getStatus() == Quest.QuestStatus.TURNED_IN) {
                return new QuestResult(false, "已完成的任务无法放弃");
            }
            
            // 更新任务状态
            playerQuest.setStatus(Quest.QuestStatus.CANCELLED);
            playerQuest.setLastUpdateTime(new Date());
            
            // 保存到数据库
            savePlayerQuest(playerQuest);
            
            log.info("玩家 {} 放弃任务: {}", playerName, questId);
            return new QuestResult(true, "任务已放弃");
            
        } catch (Exception e) {
            log.error("放弃任务失败", e);
            return new QuestResult(false, "系统错误");
        }
    }
    
    /**
     * 处理日常任务重置
     */
    public void processDailyQuestReset() {
        long currentTime = System.currentTimeMillis();
        
        for (Map.Entry<String, Map<Integer, PlayerQuest>> entry : playerQuests.entrySet()) {
            String playerName = entry.getKey();
            Map<Integer, PlayerQuest> questMap = entry.getValue();
            
            Long lastResetTime = dailyQuestResetTime.get(playerName);
            if (lastResetTime == null || currentTime - lastResetTime >= 24 * 60 * 60 * 1000) {
                // 重置日常任务
                for (PlayerQuest playerQuest : questMap.values()) {
                    Quest questTemplate = questTemplates.get(playerQuest.getQuestId());
                    if (questTemplate != null && questTemplate.getType() == Quest.QuestType.DAILY) {
                        if (playerQuest.getStatus() == Quest.QuestStatus.TURNED_IN) {
                            playerQuest.resetForRepeat();
                            savePlayerQuest(playerQuest);
                        }
                    }
                }
                
                dailyQuestResetTime.put(playerName, currentTime);
                log.debug("重置玩家 {} 的日常任务", playerName);
            }
        }
    }
    
    /**
     * 保存玩家任务到数据库
     */
    private void savePlayerQuest(PlayerQuest playerQuest) {
        try {
            // 转换为Entity并保存到数据库
            PlayerQuestEntity entity = PlayerQuestEntity.fromPlayerQuest(playerQuest);
            
            // 检查是否已存在，如果存在则更新，否则新增
            Optional<PlayerQuestEntity> existingEntity = playerQuestRepository.findByPlayerQuestId(playerQuest.getPlayerQuestId());
            if (existingEntity.isPresent()) {
                // 更新现有实体
                PlayerQuestEntity existing = existingEntity.get();
                existing.setStatus(entity.getStatus());
                existing.setProgressData(entity.getProgressData());
                existing.setVariablesData(entity.getVariablesData());
                existing.setFlagsData(entity.getFlagsData());
                existing.setCompleteTime(entity.getCompleteTime());
                existing.setTurnInTime(entity.getTurnInTime());
                existing.setExpireTime(entity.getExpireTime());
                existing.setLastUpdateTime(entity.getLastUpdateTime());
                existing.setRepeatCount(entity.getRepeatCount());
                existing.setLastCompletedTime(entity.getLastCompletedTime());
                existing.setRewardChoice(entity.getRewardChoice());
                existing.setRewardReceived(entity.getRewardReceived());
                existing.setUpdatedTime(LocalDateTime.now());
                
                playerQuestRepository.save(existing);
            } else {
                // 新增实体
                playerQuestRepository.save(entity);
            }
            
            log.debug("保存玩家任务成功: {} - {}", playerQuest.getPlayerName(), playerQuest.getQuestId());
            
        } catch (Exception e) {
            log.error("保存玩家任务失败: {} - {}", playerQuest.getPlayerName(), playerQuest.getQuestId(), e);
        }
    }
    
    /**
     * 保存玩家任务数据
     */
    private void savePlayerQuests(String playerName, List<PlayerQuest> quests) {
        try {
            // 批量保存到数据库
            List<PlayerQuestEntity> entities = new ArrayList<>();
            for (PlayerQuest quest : quests) {
                PlayerQuestEntity entity = PlayerQuestEntity.fromPlayerQuest(quest);
                entities.add(entity);
            }
            
            if (!entities.isEmpty()) {
                playerQuestRepository.saveAll(entities);
                log.debug("保存玩家任务数据成功: player={}, questCount={}", playerName, quests.size());
            }
            
        } catch (Exception e) {
            log.error("保存玩家任务数据失败: player={}", playerName, e);
        }
    }
    
    /**
     * 加载玩家任务数据
     */
    private List<PlayerQuest> loadPlayerQuests(String playerName) {
        try {
            // 从数据库加载玩家任务
            List<PlayerQuestEntity> entities = playerQuestRepository.findByPlayerNameAndDeletedFalse(playerName);
            List<PlayerQuest> quests = new ArrayList<>();
            
            for (PlayerQuestEntity entity : entities) {
                PlayerQuest quest = entity.toPlayerQuest();
                quests.add(quest);
            }
            
            log.debug("加载玩家任务数据成功: player={}, questCount={}", playerName, quests.size());
            return quests;
            
        } catch (Exception e) {
            log.error("加载玩家任务数据失败: player={}", playerName, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 任务结果类
     */
    public static class QuestResult {
        private final boolean success;
        private final String message;
        
        public QuestResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
} 