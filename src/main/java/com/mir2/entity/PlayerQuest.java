package com.mir2.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 玩家任务实体类
 * 
 * @author Mir2 Team
 */
@Data
public class PlayerQuest {
    
    /** 玩家任务ID */
    private String playerQuestId;
    
    /** 玩家名称 */
    private String playerName;
    
    /** 任务ID */
    private int questId;
    
    /** 任务状态 */
    private Quest.QuestStatus status;
    
    /** 任务进度 */
    private List<QuestProgress> progress;
    
    /** 任务变量 */
    private Map<String, Object> variables;
    
    /** 任务标志 */
    private Map<String, Integer> flags;
    
    /** 接受任务时间 */
    private Date acceptTime;
    
    /** 完成任务时间 */
    private Date completeTime;
    
    /** 交付任务时间 */
    private Date turnInTime;
    
    /** 任务过期时间 */
    private Date expireTime;
    
    /** 最后更新时间 */
    private Date lastUpdateTime;
    
    /** 任务重复计数 */
    private int repeatCount;
    
    /** 上次完成时间（用于重复任务） */
    private Date lastCompletedTime;
    
    /** 任务来源NPC ID */
    private Integer sourceNpcId;
    
    /** 任务奖励选择 */
    private Integer rewardChoice;
    
    /** 是否已获得奖励 */
    private boolean rewardReceived;
    
    /**
     * 任务进度记录
     */
    @Data
    public static class QuestProgress {
        
        /** 目标ID */
        private int objectiveId;
        
        /** 目标类型 */
        private Quest.QuestObjective.ObjectiveType type;
        
        /** 当前进度 */
        private int currentProgress;
        
        /** 需要完成的数量 */
        private int requiredAmount;
        
        /** 是否已完成 */
        private boolean completed;
        
        /** 最后更新时间 */
        private Date lastUpdateTime;
        
        /** 进度参数 */
        private Map<String, Object> parameters;
        
        /**
         * 更新进度
         */
        public void updateProgress(int progress) {
            this.currentProgress = Math.min(progress, requiredAmount);
            this.completed = this.currentProgress >= requiredAmount;
            this.lastUpdateTime = new Date();
        }
        
        /**
         * 增加进度
         */
        public void addProgress(int amount) {
            updateProgress(currentProgress + amount);
        }
        
        /**
         * 获取进度百分比
         */
        public double getProgressPercentage() {
            if (requiredAmount == 0) {
                return 0.0;
            }
            return Math.min(100.0, (double) currentProgress / requiredAmount * 100);
        }
    }
    
    /**
     * 检查任务是否过期
     */
    public boolean isExpired() {
        if (expireTime == null) {
            return false;
        }
        return new Date().after(expireTime);
    }
    
    /**
     * 检查任务是否完成
     */
    public boolean isCompleted() {
        if (status != Quest.QuestStatus.IN_PROGRESS) {
            return false;
        }
        
        // 检查所有必需目标是否完成
        return progress.stream()
                .allMatch(QuestProgress::isCompleted);
    }
    
    /**
     * 获取任务完成百分比
     */
    public double getCompletionPercentage() {
        if (progress == null || progress.isEmpty()) {
            return 0.0;
        }
        
        double totalProgress = progress.stream()
                .mapToDouble(QuestProgress::getProgressPercentage)
                .sum();
        
        return totalProgress / progress.size();
    }
    
    /**
     * 获取指定目标的进度
     */
    public QuestProgress getObjectiveProgress(int objectiveId) {
        return progress.stream()
                .filter(p -> p.getObjectiveId() == objectiveId)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 更新目标进度
     */
    public void updateObjectiveProgress(int objectiveId, int progress) {
        QuestProgress questProgress = getObjectiveProgress(objectiveId);
        if (questProgress != null) {
            questProgress.updateProgress(progress);
            this.lastUpdateTime = new Date();
        }
    }
    
    /**
     * 增加目标进度
     */
    public void addObjectiveProgress(int objectiveId, int amount) {
        QuestProgress questProgress = getObjectiveProgress(objectiveId);
        if (questProgress != null) {
            questProgress.addProgress(amount);
            this.lastUpdateTime = new Date();
        }
    }
    
    /**
     * 设置任务变量
     */
    public void setVariable(String key, Object value) {
        if (variables == null) {
            variables = new java.util.HashMap<>();
        }
        variables.put(key, value);
        this.lastUpdateTime = new Date();
    }
    
    /**
     * 获取任务变量
     */
    public Object getVariable(String key) {
        if (variables == null) {
            return null;
        }
        return variables.get(key);
    }
    
    /**
     * 设置任务标志
     */
    public void setFlag(String key, int value) {
        if (flags == null) {
            flags = new java.util.HashMap<>();
        }
        flags.put(key, value);
        this.lastUpdateTime = new Date();
    }
    
    /**
     * 获取任务标志
     */
    public int getFlag(String key) {
        if (flags == null) {
            return 0;
        }
        return flags.getOrDefault(key, 0);
    }
    
    /**
     * 检查是否可以重复任务
     */
    public boolean canRepeat(Quest quest) {
        if (!quest.isRepeatable()) {
            return false;
        }
        
        if (lastCompletedTime == null) {
            return true;
        }
        
        if (quest.getRepeatInterval() == null) {
            return true;
        }
        
        long elapsed = System.currentTimeMillis() - lastCompletedTime.getTime();
        return elapsed >= quest.getRepeatInterval();
    }
    
    /**
     * 重置任务进度（用于重复任务）
     */
    public void resetForRepeat() {
        this.status = Quest.QuestStatus.AVAILABLE;
        this.completeTime = null;
        this.turnInTime = null;
        this.rewardReceived = false;
        this.rewardChoice = null;
        
        // 重置所有目标进度
        if (progress != null) {
            for (QuestProgress prog : progress) {
                prog.setCurrentProgress(0);
                prog.setCompleted(false);
                prog.setLastUpdateTime(new Date());
            }
        }
        
        this.lastUpdateTime = new Date();
    }
    
    /**
     * 获取任务剩余时间
     */
    public long getRemainingTime() {
        if (expireTime == null) {
            return -1; // 无时间限制
        }
        
        long remaining = expireTime.getTime() - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    /**
     * 检查任务是否可以交付
     */
    public boolean canTurnIn() {
        return status == Quest.QuestStatus.COMPLETED && !rewardReceived;
    }
} 