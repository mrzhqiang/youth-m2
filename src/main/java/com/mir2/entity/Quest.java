package com.mir2.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 任务实体类
 * 
 * @author Mir2 Team
 */
@Data
public class Quest {
    
    /** 任务ID */
    private int questId;
    
    /** 任务名称 */
    private String name;
    
    /** 任务描述 */
    private String description;
    
    /** 任务类型 */
    private QuestType type;
    
    /** 任务状态 */
    private QuestStatus status;
    
    /** 最低等级要求 */
    private int minLevel;
    
    /** 最高等级限制 */
    private int maxLevel;
    
    /** 职业限制 */
    private String jobRestriction;
    
    /** 性别限制 */
    private String genderRestriction;
    
    /** 前置任务ID */
    private Integer previousQuestId;
    
    /** 任务目标列表 */
    private List<QuestObjective> objectives;
    
    /** 任务奖励 */
    private QuestReward reward;
    
    /** 任务开始时间 */
    private Date startTime;
    
    /** 任务结束时间 */
    private Date endTime;
    
    /** 任务超时时间（毫秒） */
    private Long timeLimit;
    
    /** 是否可重复 */
    private boolean repeatable;
    
    /** 重复间隔（毫秒） */
    private Long repeatInterval;
    
    /** 任务标志 */
    private Map<String, Integer> flags;
    
    /** 任务变量 */
    private Map<String, Object> variables;
    
    /** 任务NPC ID */
    private Integer npcId;
    
    /** 创建时间 */
    private Date createdTime;
    
    /** 最后更新时间 */
    private Date lastUpdateTime;
    
    /**
     * 任务类型枚举
     */
    public enum QuestType {
        MAIN,           // 主线任务
        SIDE,           // 支线任务
        DAILY,          // 日常任务
        WEEKLY,         // 周常任务
        MONTHLY,        // 月常任务
        GUILD,          // 行会任务
        SYSTEM,         // 系统任务
        EVENT,          // 活动任务
        REPEATABLE      // 可重复任务
    }
    
    /**
     * 任务状态枚举
     */
    public enum QuestStatus {
        AVAILABLE,      // 可接受
        ACCEPTED,       // 已接受
        IN_PROGRESS,    // 进行中
        COMPLETED,      // 已完成
        FAILED,         // 失败
        EXPIRED,        // 过期
        CANCELLED,      // 取消
        TURNED_IN       // 已交付
    }
    
    /**
     * 任务目标
     */
    @Data
    public static class QuestObjective {
        
        /** 目标ID */
        private int objectiveId;
        
        /** 目标类型 */
        private ObjectiveType type;
        
        /** 目标描述 */
        private String description;
        
        /** 目标参数 */
        private Map<String, Object> parameters;
        
        /** 当前进度 */
        private int currentProgress;
        
        /** 需要完成的数量 */
        private int requiredAmount;
        
        /** 是否已完成 */
        private boolean completed;
        
        /** 是否可选（可选目标不影响任务完成） */
        private boolean optional;
        
        /**
         * 目标类型枚举
         */
        public enum ObjectiveType {
            KILL_MONSTER,       // 杀怪
            COLLECT_ITEM,       // 收集物品
            TALK_TO_NPC,        // 与NPC对话
            DELIVER_ITEM,       // 送物品
            REACH_LOCATION,     // 到达地点
            USE_ITEM,           // 使用物品
            EQUIP_ITEM,         // 装备物品
            LEVEL_UP,           // 升级
            LEARN_SKILL,        // 学习技能
            GAIN_EXPERIENCE,    // 获得经验
            SPEND_GOLD,         // 花费金币
            TRADE_WITH_PLAYER,  // 与玩家交易
            JOIN_GUILD,         // 加入行会
            COMPLETE_QUEST,     // 完成任务
            SURVIVE_TIME,       // 存活时间
            EXPLORE_MAP,        // 探索地图
            CRAFT_ITEM,         // 制作物品
            UPGRADE_EQUIPMENT,  // 升级装备
            PK_PLAYER,          // PK玩家
            PARTICIPATE_EVENT   // 参与活动
        }
        
        /**
         * 检查目标是否完成
         */
        public boolean isCompleted() {
            return currentProgress >= requiredAmount;
        }
        
        /**
         * 更新进度
         */
        public void updateProgress(int progress) {
            this.currentProgress = Math.min(progress, requiredAmount);
            this.completed = isCompleted();
        }
        
        /**
         * 增加进度
         */
        public void addProgress(int amount) {
            updateProgress(currentProgress + amount);
        }
    }
    
    /**
     * 任务奖励
     */
    @Data
    public static class QuestReward {
        
        /** 经验奖励 */
        private long experience;
        
        /** 金币奖励 */
        private int gold;
        
        /** 声望奖励 */
        private int reputation;
        
        /** 物品奖励 */
        private List<ItemReward> items;
        
        /** 技能奖励 */
        private List<SkillReward> skills;
        
        /** 选择奖励（玩家可选择其中一个） */
        private List<ItemReward> choiceItems;
        
        /** 称号奖励 */
        private String title;
        
        /** 特殊奖励 */
        private Map<String, Object> specialRewards;
        
        /**
         * 物品奖励
         */
        @Data
        public static class ItemReward {
            private int itemId;
            private String itemName;
            private int quantity;
            private boolean bound;
        }
        
        /**
         * 技能奖励
         */
        @Data
        public static class SkillReward {
            private int skillId;
            private String skillName;
            private int level;
        }
    }
    
    /**
     * 检查任务是否过期
     */
    public boolean isExpired() {
        if (timeLimit == null || startTime == null) {
            return false;
        }
        return System.currentTimeMillis() - startTime.getTime() > timeLimit;
    }
    
    /**
     * 检查任务是否可以接受
     */
    public boolean canAccept() {
        return status == QuestStatus.AVAILABLE;
    }
    
    /**
     * 检查任务是否已完成
     */
    public boolean isCompleted() {
        if (status != QuestStatus.IN_PROGRESS) {
            return false;
        }
        
        // 检查所有必需目标是否完成
        return objectives.stream()
                .filter(obj -> !obj.isOptional())
                .allMatch(QuestObjective::isCompleted);
    }
    
    /**
     * 获取任务完成百分比
     */
    public double getCompletionPercentage() {
        if (objectives == null || objectives.isEmpty()) {
            return 0.0;
        }
        
        int totalObjectives = objectives.size();
        int completedObjectives = (int) objectives.stream()
                .filter(QuestObjective::isCompleted)
                .count();
        
        return (double) completedObjectives / totalObjectives * 100;
    }
    
    /**
     * 获取任务剩余时间
     */
    public long getRemainingTime() {
        if (timeLimit == null || startTime == null) {
            return -1; // 无时间限制
        }
        
        long elapsed = System.currentTimeMillis() - startTime.getTime();
        return Math.max(0, timeLimit - elapsed);
    }
} 