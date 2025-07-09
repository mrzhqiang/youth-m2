package com.mir2.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 玩家任务JPA实体类
 * 
 * <p>用于数据库持久化的玩家任务实体，包括：</p>
 * <ul>
 *   <li>任务基本信息</li>
 *   <li>任务进度数据</li>
 *   <li>任务状态管理</li>
 *   <li>任务变量存储</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "player_quest")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerQuestEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "player_quest_id", unique = true, nullable = false, length = 100)
    private String playerQuestId;
    
    @Column(name = "player_name", nullable = false, length = 50)
    private String playerName;
    
    @Column(name = "quest_id", nullable = false)
    private Integer questId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Quest.QuestStatus status;
    
    @Column(name = "progress_data", columnDefinition = "TEXT")
    private String progressData; // JSON格式存储进度数据
    
    @Column(name = "variables_data", columnDefinition = "TEXT")
    private String variablesData; // JSON格式存储变量数据
    
    @Column(name = "flags_data", columnDefinition = "TEXT")
    private String flagsData; // JSON格式存储标志数据
    
    @Column(name = "accept_time")
    private LocalDateTime acceptTime;
    
    @Column(name = "complete_time")
    private LocalDateTime completeTime;
    
    @Column(name = "turn_in_time")
    private LocalDateTime turnInTime;
    
    @Column(name = "expire_time")
    private LocalDateTime expireTime;
    
    @Column(name = "last_update_time")
    private LocalDateTime lastUpdateTime;
    
    @Column(name = "repeat_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer repeatCount;
    
    @Column(name = "last_completed_time")
    private LocalDateTime lastCompletedTime;
    
    @Column(name = "source_npc_id")
    private Integer sourceNpcId;
    
    @Column(name = "reward_choice")
    private Integer rewardChoice;
    
    @Column(name = "reward_received", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean rewardReceived;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
    
    @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deleted;
    
    /**
     * 构造函数
     * 
     * @param playerQuestId 玩家任务ID
     * @param playerName 玩家名称
     * @param questId 任务ID
     * @param status 任务状态
     */
    public PlayerQuestEntity(String playerQuestId, String playerName, Integer questId, Quest.QuestStatus status) {
        this.playerQuestId = playerQuestId;
        this.playerName = playerName;
        this.questId = questId;
        this.status = status;
        this.repeatCount = 0;
        this.rewardReceived = false;
        this.deleted = false;
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
        this.lastUpdateTime = LocalDateTime.now();
    }
    
    /**
     * 从PlayerQuest转换为Entity
     * 
     * @param playerQuest 玩家任务对象
     * @return PlayerQuestEntity
     */
    public static PlayerQuestEntity fromPlayerQuest(PlayerQuest playerQuest) {
        PlayerQuestEntity entity = new PlayerQuestEntity();
        entity.setPlayerQuestId(playerQuest.getPlayerQuestId());
        entity.setPlayerName(playerQuest.getPlayerName());
        entity.setQuestId(playerQuest.getQuestId());
        entity.setStatus(playerQuest.getStatus());
        entity.setAcceptTime(convertToLocalDateTime(playerQuest.getAcceptTime()));
        entity.setCompleteTime(convertToLocalDateTime(playerQuest.getCompleteTime()));
        entity.setTurnInTime(convertToLocalDateTime(playerQuest.getTurnInTime()));
        entity.setExpireTime(convertToLocalDateTime(playerQuest.getExpireTime()));
        entity.setLastUpdateTime(convertToLocalDateTime(playerQuest.getLastUpdateTime()));
        entity.setRepeatCount(playerQuest.getRepeatCount());
        entity.setLastCompletedTime(convertToLocalDateTime(playerQuest.getLastCompletedTime()));
        entity.setSourceNpcId(playerQuest.getSourceNpcId());
        entity.setRewardChoice(playerQuest.getRewardChoice());
        entity.setRewardReceived(playerQuest.isRewardReceived());
        entity.setDeleted(false);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        
        // 转换进度数据为JSON
        entity.setProgressData(convertProgressToJson(playerQuest.getProgress()));
        entity.setVariablesData(convertMapToJson(playerQuest.getVariables()));
        entity.setFlagsData(convertMapToJson(playerQuest.getFlags()));
        
        return entity;
    }
    
    /**
     * 转换为PlayerQuest对象
     * 
     * @return PlayerQuest对象
     */
    public PlayerQuest toPlayerQuest() {
        PlayerQuest playerQuest = new PlayerQuest();
        playerQuest.setPlayerQuestId(this.playerQuestId);
        playerQuest.setPlayerName(this.playerName);
        playerQuest.setQuestId(this.questId);
        playerQuest.setStatus(this.status);
        playerQuest.setAcceptTime(convertToDate(this.acceptTime));
        playerQuest.setCompleteTime(convertToDate(this.completeTime));
        playerQuest.setTurnInTime(convertToDate(this.turnInTime));
        playerQuest.setExpireTime(convertToDate(this.expireTime));
        playerQuest.setLastUpdateTime(convertToDate(this.lastUpdateTime));
        playerQuest.setRepeatCount(this.repeatCount != null ? this.repeatCount : 0);
        playerQuest.setLastCompletedTime(convertToDate(this.lastCompletedTime));
        playerQuest.setSourceNpcId(this.sourceNpcId);
        playerQuest.setRewardChoice(this.rewardChoice);
        playerQuest.setRewardReceived(this.rewardReceived != null ? this.rewardReceived : false);
        
        // 转换JSON数据
        playerQuest.setProgress(convertJsonToProgress(this.progressData));
        playerQuest.setVariables(convertJsonToMap(this.variablesData));
        playerQuest.setFlags(convertJsonToIntegerMap(this.flagsData));
        
        return playerQuest;
    }
    
    /**
     * 更新时间戳
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedTime = LocalDateTime.now();
        this.lastUpdateTime = LocalDateTime.now();
    }
    
    @PrePersist
    public void prePersist() {
        if (this.createdTime == null) {
            this.createdTime = LocalDateTime.now();
        }
        if (this.updatedTime == null) {
            this.updatedTime = LocalDateTime.now();
        }
        if (this.lastUpdateTime == null) {
            this.lastUpdateTime = LocalDateTime.now();
        }
        if (this.repeatCount == null) {
            this.repeatCount = 0;
        }
        if (this.rewardReceived == null) {
            this.rewardReceived = false;
        }
        if (this.deleted == null) {
            this.deleted = false;
        }
    }
    
    // 辅助方法
    private static LocalDateTime convertToLocalDateTime(java.util.Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
    }
    
    private static java.util.Date convertToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return java.util.Date.from(localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant());
    }
    
    private static String convertProgressToJson(List<PlayerQuest.QuestProgress> progress) {
        if (progress == null || progress.isEmpty()) return "[]";
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(progress);
        } catch (Exception e) {
            return "[]";
        }
    }
    
    private static String convertMapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) return "{}";
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    @SuppressWarnings("unchecked")
    private static List<PlayerQuest.QuestProgress> convertJsonToProgress(String json) {
        if (json == null || json.trim().isEmpty() || "[]".equals(json.trim())) {
            return new java.util.ArrayList<>();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.core.type.TypeReference<List<PlayerQuest.QuestProgress>> typeRef = 
                new com.fasterxml.jackson.core.type.TypeReference<List<PlayerQuest.QuestProgress>>() {};
            return mapper.readValue(json, typeRef);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, Object> convertJsonToMap(String json) {
        if (json == null || json.trim().isEmpty() || "{}".equals(json.trim())) {
            return new java.util.HashMap<>();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>> typeRef = 
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {};
            return mapper.readValue(json, typeRef);
        } catch (Exception e) {
            return new java.util.HashMap<>();
        }
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, Integer> convertJsonToIntegerMap(String json) {
        if (json == null || json.trim().isEmpty() || "{}".equals(json.trim())) {
            return new java.util.HashMap<>();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.core.type.TypeReference<Map<String, Integer>> typeRef = 
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Integer>>() {};
            return mapper.readValue(json, typeRef);
        } catch (Exception e) {
            return new java.util.HashMap<>();
        }
    }
} 