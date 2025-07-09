package com.mir2.entity;

import com.mir2.core.enums.Job;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友信息实体类
 * 
 * <p>用于存储和管理好友的详细信息，包括：</p>
 * <ul>
 *   <li>基本信息（姓名、等级、职业）</li>
 *   <li>在线状态</li>
 *   <li>位置信息</li>
 *   <li>社交信息（备注、分组、亲密度）</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class FriendInfo {
    
    /** 好友名称 */
    private String friendName;
    
    /** 好友等级 */
    private int level;
    
    /** 好友职业 */
    private Job job;
    
    /** 是否在线 */
    private boolean online;
    
    /** 当前地图名称 */
    private String mapName;
    
    /** 最后在线时间 */
    private long lastOnlineTime;
    
    /** 好友备注 */
    private String remark;
    
    /** 好友分组 */
    private String group;
    
    /** 亲密度（0-100） */
    private int intimacy;
    
    /** 添加好友时间 */
    private LocalDateTime addTime;
    
    /** 最后互动时间 */
    private LocalDateTime lastInteractionTime;
    
    /** 互动次数 */
    private int interactionCount;
    
    /** 好友状态（在线、离线、忙碌等） */
    private String status;
    
    /** 好友头像ID */
    private int avatarId;
    
    /** 好友性别 */
    private int gender;
    
    /** 好友PK值 */
    private int pkValue;
    
    /** 好友行会名称 */
    private String guildName;
    
    /** 好友配偶名称 */
    private String spouseName;
    
    /** 好友师父名称 */
    private String masterName;
    
    /** 好友徒弟数量 */
    private int apprenticeCount;
    
    /** 是否互为好友 */
    private boolean mutual;
    
    /** 好友VIP等级 */
    private int vipLevel;
    
    /** 好友战斗力 */
    private long combatPower;
    
    /**
     * 默认构造函数
     */
    public FriendInfo() {
        this.online = false;
        this.intimacy = 0;
        this.group = "默认分组";
        this.remark = "";
        this.status = "离线";
        this.interactionCount = 0;
        this.mutual = false;
        this.vipLevel = 0;
        this.combatPower = 0;
    }
    
    /**
     * 构造函数
     * 
     * @param friendName 好友名称
     * @param level 等级
     * @param job 职业
     * @param online 是否在线
     */
    public FriendInfo(String friendName, int level, Job job, boolean online) {
        this();
        this.friendName = friendName;
        this.level = level;
        this.job = job;
        this.online = online;
        this.addTime = LocalDateTime.now();
    }
    
    /**
     * 更新在线状态
     * 
     * @param online 是否在线
     */
    public void updateOnlineStatus(boolean online) {
        this.online = online;
        if (!online) {
            this.lastOnlineTime = System.currentTimeMillis();
            this.status = "离线";
        } else {
            this.status = "在线";
        }
    }
    
    /**
     * 更新位置信息
     * 
     * @param mapName 地图名称
     */
    public void updateLocation(String mapName) {
        this.mapName = mapName;
    }
    
    /**
     * 增加互动次数
     */
    public void incrementInteraction() {
        this.interactionCount++;
        this.lastInteractionTime = LocalDateTime.now();
    }
    
    /**
     * 更新亲密度
     * 
     * @param intimacy 亲密度值
     */
    public void updateIntimacy(int intimacy) {
        this.intimacy = Math.max(0, Math.min(100, intimacy));
    }
    
    /**
     * 增加亲密度
     * 
     * @param amount 增加的亲密度
     */
    public void addIntimacy(int amount) {
        updateIntimacy(this.intimacy + amount);
    }
    
    /**
     * 获取亲密度等级
     * 
     * @return 亲密度等级描述
     */
    public String getIntimacyLevel() {
        if (intimacy >= 80) {
            return "挚友";
        } else if (intimacy >= 60) {
            return "好友";
        } else if (intimacy >= 40) {
            return "朋友";
        } else if (intimacy >= 20) {
            return "熟人";
        } else {
            return "陌生";
        }
    }
    
    /**
     * 获取在线时长描述
     * 
     * @return 在线时长描述
     */
    public String getOnlineTimeDescription() {
        if (online) {
            return "在线";
        } else if (lastOnlineTime <= 0) {
            return "从未上线";
        } else {
            long offlineTime = System.currentTimeMillis() - lastOnlineTime;
            long minutes = offlineTime / (1000 * 60);
            long hours = minutes / 60;
            long days = hours / 24;
            
            if (days > 0) {
                return days + "天前";
            } else if (hours > 0) {
                return hours + "小时前";
            } else if (minutes > 0) {
                return minutes + "分钟前";
            } else {
                return "刚刚离线";
            }
        }
    }
    
    /**
     * 检查是否为新手好友
     * 
     * @return 是否为新手
     */
    public boolean isNewbie() {
        return level < 10;
    }
    
    /**
     * 检查是否为高级玩家
     * 
     * @return 是否为高级玩家
     */
    public boolean isVeteran() {
        return level >= 50;
    }
    
    /**
     * 获取显示名称
     * 
     * @return 显示名称（包含备注）
     */
    public String getDisplayName() {
        if (remark != null && !remark.trim().isEmpty()) {
            return String.format("%s(%s)", friendName, remark);
        }
        return friendName;
    }
    
    /**
     * 获取职业描述
     * 
     * @return 职业描述
     */
    public String getJobDescription() {
        return job != null ? job.getName() : "未知";
    }
    
    /**
     * 获取性别描述
     * 
     * @return 性别描述
     */
    public String getGenderDescription() {
        return gender == 0 ? "男" : "女";
    }
    
    /**
     * 检查是否可以私聊
     * 
     * @return 是否可以私聊
     */
    public boolean canSendPrivateMessage() {
        return online; // 只有在线才能私聊
    }
    
    /**
     * 检查是否可以组队
     * 
     * @return 是否可以组队
     */
    public boolean canInviteToTeam() {
        return online && level >= 10; // 在线且10级以上才能组队
    }
    
    /**
     * 获取完整信息描述
     * 
     * @return 完整信息描述
     */
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("姓名: ").append(getDisplayName()).append("\n");
        sb.append("等级: ").append(level).append("\n");
        sb.append("职业: ").append(getJobDescription()).append("\n");
        sb.append("性别: ").append(getGenderDescription()).append("\n");
        sb.append("状态: ").append(getOnlineTimeDescription()).append("\n");
        sb.append("亲密度: ").append(getIntimacyLevel()).append("(").append(intimacy).append(")\n");
        
        if (guildName != null && !guildName.isEmpty()) {
            sb.append("行会: ").append(guildName).append("\n");
        }
        
        if (spouseName != null && !spouseName.isEmpty()) {
            sb.append("配偶: ").append(spouseName).append("\n");
        }
        
        if (online && mapName != null) {
            sb.append("位置: ").append(mapName).append("\n");
        }
        
        return sb.toString();
    }
} 