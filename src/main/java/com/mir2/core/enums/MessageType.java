package com.mir2.core.enums;

/**
 * 消息类型枚举
 * 
 * <p>定义游戏中各种消息类型，包括聊天、系统、通知等。
 * 对应原M2Engine中的消息系统。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public enum MessageType {
    /** 普通聊天 */
    NORMAL("普通聊天"),
    
    /** 私聊 */
    PRIVATE("私聊"),
    
    /** 系统消息 */
    SYSTEM("系统消息"),
    
    /** 行会消息 */
    GUILD("行会消息"),
    
    /** 队伍消息 */
    TEAM("队伍消息"),
    
    /** 喊话 */
    SHOUT("喊话"),
    
    /** 公告 */
    ANNOUNCEMENT("公告"),
    
    /** 通知 */
    NOTIFICATION("通知"),
    
    /** 错误消息 */
    ERROR("错误消息"),
    
    /** 调试消息 */
    DEBUG("调试消息"),
    
    /** 提示消息 */
    HINT("提示消息"),
    
    /** 警告消息 */
    WARNING("警告消息"),
    
    /** 信息消息 */
    INFO("信息消息"),
    
    /** 成功消息 */
    SUCCESS("成功消息"),
    
    /** 失败消息 */
    FAIL("失败消息"),
    
    /** 交易消息 */
    TRADE("交易消息"),
    
    /** 邮件消息 */
    MAIL("邮件消息"),
    
    /** 好友消息 */
    FRIEND("好友消息"),
    
    /** 敌人消息 */
    ENEMY("敌人消息"),
    
    /** 中立消息 */
    NEUTRAL("中立消息"),
    
    /** 地图消息 */
    MAP("地图消息"),
    
    /** 区域消息 */
    AREA("区域消息"),
    
    /** 全服消息 */
    GLOBAL("全服消息"),
    
    /** 管理员消息 */
    ADMIN("管理员消息"),
    
    /** GM消息 */
    GM("GM消息"),
    
    /** 自定义消息 */
    CUSTOM("自定义消息");
    
    private final String description;
    
    MessageType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据描述获取消息类型
     * 
     * @param description 描述
     * @return 消息类型
     */
    public static MessageType fromDescription(String description) {
        for (MessageType type : values()) {
            if (type.description.equals(description)) {
                return type;
            }
        }
        return NORMAL;
    }
} 