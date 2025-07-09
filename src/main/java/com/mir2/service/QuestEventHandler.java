package com.mir2.service;

import com.mir2.entity.Quest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务事件处理器
 * 
 * @author Mir2 Team
 */
@Component
@Slf4j
public class QuestEventHandler {
    
    @Autowired
    private QuestService questService;
    
    /**
     * 处理怪物击杀事件
     */
    public void handleMonsterKill(String playerName, int monsterId, String mapName) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("monsterId", monsterId);
            parameters.put("mapName", mapName);
            
            questService.updateQuestProgress(playerName, Quest.QuestObjective.ObjectiveType.KILL_MONSTER, parameters);
            
            log.debug("处理怪物击杀事件: {} 在 {} 击杀了怪物 {}", playerName, mapName, monsterId);
        } catch (Exception e) {
            log.error("处理怪物击杀事件失败", e);
        }
    }
    
    /**
     * 处理物品收集事件
     */
    public void handleItemCollect(String playerName, int itemId, int quantity) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("itemId", itemId);
            parameters.put("quantity", quantity);
            
            // 更新多次以匹配数量
            for (int i = 0; i < quantity; i++) {
                questService.updateQuestProgress(playerName, Quest.QuestObjective.ObjectiveType.COLLECT_ITEM, parameters);
            }
            
            log.debug("处理物品收集事件: {} 收集了 {} 个物品 {}", playerName, quantity, itemId);
        } catch (Exception e) {
            log.error("处理物品收集事件失败", e);
        }
    }
    
    /**
     * 处理玩家升级事件
     */
    public void handlePlayerLevelUp(String playerName, int newLevel) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("level", newLevel);
            
            questService.updateQuestProgress(playerName, Quest.QuestObjective.ObjectiveType.LEVEL_UP, parameters);
            
            log.debug("处理玩家升级事件: {} 升级到 {}", playerName, newLevel);
        } catch (Exception e) {
            log.error("处理玩家升级事件失败", e);
        }
    }
    
    /**
     * 处理NPC对话事件
     */
    public void handleNpcTalk(String playerName, int npcId, String mapName) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("npcId", npcId);
            parameters.put("mapName", mapName);
            
            questService.updateQuestProgress(playerName, Quest.QuestObjective.ObjectiveType.TALK_TO_NPC, parameters);
            
            log.debug("处理NPC对话事件: {} 与NPC {} 对话", playerName, npcId);
        } catch (Exception e) {
            log.error("处理NPC对话事件失败", e);
        }
    }
    
    /**
     * 处理物品使用事件
     */
    public void handleItemUse(String playerName, int itemId) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("itemId", itemId);
            
            questService.updateQuestProgress(playerName, Quest.QuestObjective.ObjectiveType.USE_ITEM, parameters);
            
            log.debug("处理物品使用事件: {} 使用了物品 {}", playerName, itemId);
        } catch (Exception e) {
            log.error("处理物品使用事件失败", e);
        }
    }
    
    /**
     * 处理装备穿戴事件
     */
    public void handleEquipItem(String playerName, int itemId) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("itemId", itemId);
            
            questService.updateQuestProgress(playerName, Quest.QuestObjective.ObjectiveType.EQUIP_ITEM, parameters);
            
            log.debug("处理装备穿戴事件: {} 装备了物品 {}", playerName, itemId);
        } catch (Exception e) {
            log.error("处理装备穿戴事件失败", e);
        }
    }
    
    /**
     * 处理技能学习事件
     */
    public void handleSkillLearn(String playerName, int skillId) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("skillId", skillId);
            
            questService.updateQuestProgress(playerName, Quest.QuestObjective.ObjectiveType.LEARN_SKILL, parameters);
            
            log.debug("处理技能学习事件: {} 学习了技能 {}", playerName, skillId);
        } catch (Exception e) {
            log.error("处理技能学习事件失败", e);
        }
    }
    
    /**
     * 处理经验获得事件
     */
    public void handleExperienceGain(String playerName, long experience) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("experience", experience);
            
            questService.updateQuestProgress(playerName, Quest.QuestObjective.ObjectiveType.GAIN_EXPERIENCE, parameters);
            
            log.debug("处理经验获得事件: {} 获得了 {} 经验", playerName, experience);
        } catch (Exception e) {
            log.error("处理经验获得事件失败", e);
        }
    }
    
    /**
     * 处理金币消费事件
     */
    public void handleGoldSpend(String playerName, int amount) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("amount", amount);
            
            questService.updateQuestProgress(playerName, Quest.QuestObjective.ObjectiveType.SPEND_GOLD, parameters);
            
            log.debug("处理金币消费事件: {} 消费了 {} 金币", playerName, amount);
        } catch (Exception e) {
            log.error("处理金币消费事件失败", e);
        }
    }
    
    /**
     * 处理行会加入事件
     */
    public void handleGuildJoin(String playerName, String guildName) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("guildName", guildName);
            
            questService.updateQuestProgress(playerName, Quest.QuestObjective.ObjectiveType.JOIN_GUILD, parameters);
            
            log.debug("处理行会加入事件: {} 加入了行会 {}", playerName, guildName);
        } catch (Exception e) {
            log.error("处理行会加入事件失败", e);
        }
    }
    
    /**
     * 处理PK事件
     */
    public void handlePKEvent(String killerName, String victimName) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("victimName", victimName);
            
            questService.updateQuestProgress(killerName, Quest.QuestObjective.ObjectiveType.PK_PLAYER, parameters);
            
            log.debug("处理PK事件: {} PK了 {}", killerName, victimName);
        } catch (Exception e) {
            log.error("处理PK事件失败", e);
        }
    }
    
    /**
     * 处理地图探索事件
     */
    public void handleMapExplore(String playerName, String mapName) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("mapName", mapName);
            
            questService.updateQuestProgress(playerName, Quest.QuestObjective.ObjectiveType.EXPLORE_MAP, parameters);
            
            log.debug("处理地图探索事件: {} 探索了地图 {}", playerName, mapName);
        } catch (Exception e) {
            log.error("处理地图探索事件失败", e);
        }
    }
    
    /**
     * 处理物品制作事件
     */
    public void handleItemCraft(String playerName, int itemId, int quantity) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("itemId", itemId);
            parameters.put("quantity", quantity);
            
            for (int i = 0; i < quantity; i++) {
                questService.updateQuestProgress(playerName, Quest.QuestObjective.ObjectiveType.CRAFT_ITEM, parameters);
            }
            
            log.debug("处理物品制作事件: {} 制作了 {} 个物品 {}", playerName, quantity, itemId);
        } catch (Exception e) {
            log.error("处理物品制作事件失败", e);
        }
    }
    
    /**
     * 处理装备强化事件
     */
    public void handleEquipmentUpgrade(String playerName, int itemId, int level) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("itemId", itemId);
            parameters.put("level", level);
            
            questService.updateQuestProgress(playerName, Quest.QuestObjective.ObjectiveType.UPGRADE_EQUIPMENT, parameters);
            
            log.debug("处理装备强化事件: {} 强化了装备 {} 到 {} 级", playerName, itemId, level);
        } catch (Exception e) {
            log.error("处理装备强化事件失败", e);
        }
    }
    
    /**
     * 处理玩家交易事件
     */
    public void handlePlayerTrade(String playerName, String targetPlayer, int itemId, int quantity) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("targetPlayer", targetPlayer);
            parameters.put("itemId", itemId);
            parameters.put("quantity", quantity);
            
            questService.updateQuestProgress(playerName, Quest.QuestObjective.ObjectiveType.TRADE_WITH_PLAYER, parameters);
            
            log.debug("处理玩家交易事件: {} 与 {} 交易了 {} 个物品 {}", playerName, targetPlayer, quantity, itemId);
        } catch (Exception e) {
            log.error("处理玩家交易事件失败", e);
        }
    }
    
    /**
     * 处理任务完成事件
     */
    public void handleQuestComplete(String playerName, int questId) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("questId", questId);
            
            questService.updateQuestProgress(playerName, Quest.QuestObjective.ObjectiveType.COMPLETE_QUEST, parameters);
            
            log.debug("处理任务完成事件: {} 完成了任务 {}", playerName, questId);
        } catch (Exception e) {
            log.error("处理任务完成事件失败", e);
        }
    }
} 