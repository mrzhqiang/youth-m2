package com.mir2.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私聊消息实体类
 * 
 * <p>用于存储和管理玩家之间的私聊消息，包括：</p>
 * <ul>
 *   <li>消息发送者和接收者</li>
 *   <li>消息内容</li>
 *   <li>发送时间</li>
 *   <li>消息状态</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class PrivateMessage {
    
    /** 消息ID */
    private String messageId;
    
    /** 发送者名称 */
    private String fromPlayer;
    
    /** 接收者名称 */
    private String toPlayer;
    
    /** 消息内容 */
    private String message;
    
    /** 发送时间 */
    private LocalDateTime sendTime;
    
    /** 是否已读 */
    private boolean read;
    
    /** 消息类型 */
    private MessageType type;
    
    /** 消息状态 */
    private MessageStatus status;
    
    /** 消息优先级 */
    private MessagePriority priority;
    
    /** 过期时间 */
    private LocalDateTime expireTime;
    
    /** 附件信息 */
    private String attachment;
    
    /** 消息标签 */
    private String tags;
    
    /** 回复的消息ID */
    private String replyToMessageId;
    
    /** 消息长度 */
    private int messageLength;
    
    /** 是否为系统消息 */
    private boolean systemMessage;
    
    /**
     * 消息类型枚举
     */
    public enum MessageType {
        NORMAL("普通消息"),
        SYSTEM("系统消息"),
        NOTIFICATION("通知消息"),
        GIFT("礼物消息"),
        INVITE("邀请消息"),
        TRADE("交易消息"),
        GUILD("行会消息"),
        TEAM("组队消息");
        
        private final String description;
        
        MessageType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 消息状态枚举
     */
    public enum MessageStatus {
        SENDING("发送中"),
        SENT("已发送"),
        DELIVERED("已送达"),
        READ("已读"),
        FAILED("发送失败"),
        EXPIRED("已过期"),
        DELETED("已删除");
        
        private final String description;
        
        MessageStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 消息优先级枚举
     */
    public enum MessagePriority {
        LOW("低"),
        NORMAL("普通"),
        HIGH("高"),
        URGENT("紧急");
        
        private final String description;
        
        MessagePriority(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 默认构造函数
     */
    public PrivateMessage() {
        this.messageId = generateMessageId();
        this.sendTime = LocalDateTime.now();
        this.read = false;
        this.type = MessageType.NORMAL;
        this.status = MessageStatus.SENDING;
        this.priority = MessagePriority.NORMAL;
        this.systemMessage = false;
        this.messageLength = 0;
    }
    
    /**
     * 构造函数
     * 
     * @param fromPlayer 发送者
     * @param toPlayer 接收者
     * @param message 消息内容
     */
    public PrivateMessage(String fromPlayer, String toPlayer, String message) {
        this();
        this.fromPlayer = fromPlayer;
        this.toPlayer = toPlayer;
        this.message = message;
        this.messageLength = message != null ? message.length() : 0;
    }
    
    /**
     * 构造函数（包含消息类型）
     * 
     * @param fromPlayer 发送者
     * @param toPlayer 接收者
     * @param message 消息内容
     * @param type 消息类型
     */
    public PrivateMessage(String fromPlayer, String toPlayer, String message, MessageType type) {
        this(fromPlayer, toPlayer, message);
        this.type = type;
        this.systemMessage = (type == MessageType.SYSTEM);
    }
    
    /**
     * 生成唯一消息ID
     * 
     * @return 消息ID
     */
    private String generateMessageId() {
        return "MSG_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
    
    /**
     * 标记消息为已读
     */
    public void markAsRead() {
        this.read = true;
        this.status = MessageStatus.READ;
    }
    
    /**
     * 标记消息为已送达
     */
    public void markAsDelivered() {
        if (this.status == MessageStatus.SENT) {
            this.status = MessageStatus.DELIVERED;
        }
    }
    
    /**
     * 标记消息发送成功
     */
    public void markAsSent() {
        this.status = MessageStatus.SENT;
    }
    
    /**
     * 标记消息发送失败
     */
    public void markAsFailed() {
        this.status = MessageStatus.FAILED;
    }
    
    /**
     * 检查消息是否已过期
     * 
     * @return 是否已过期
     */
    public boolean isExpired() {
        if (expireTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expireTime);
    }
    
    /**
     * 设置消息过期时间
     * 
     * @param hours 小时数
     */
    public void setExpireAfterHours(int hours) {
        this.expireTime = sendTime.plusHours(hours);
    }
    
    /**
     * 获取消息摘要
     * 
     * @param maxLength 最大长度
     * @return 消息摘要
     */
    public String getMessageSummary(int maxLength) {
        if (message == null) {
            return "";
        }
        
        if (message.length() <= maxLength) {
            return message;
        }
        
        return message.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * 获取格式化的发送时间
     * 
     * @return 格式化的时间字符串
     */
    public String getFormattedSendTime() {
        if (sendTime == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutesDiff = java.time.Duration.between(sendTime, now).toMinutes();
        
        if (minutesDiff < 1) {
            return "刚刚";
        } else if (minutesDiff < 60) {
            return minutesDiff + "分钟前";
        } else if (minutesDiff < 24 * 60) {
            return (minutesDiff / 60) + "小时前";
        } else {
            return sendTime.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm"));
        }
    }
    
    /**
     * 检查是否为长消息
     * 
     * @return 是否为长消息
     */
    public boolean isLongMessage() {
        return messageLength > 100; // 超过100字符认为是长消息
    }
    
    /**
     * 检查是否包含附件
     * 
     * @return 是否包含附件
     */
    public boolean hasAttachment() {
        return attachment != null && !attachment.trim().isEmpty();
    }
    
    /**
     * 检查是否为回复消息
     * 
     * @return 是否为回复消息
     */
    public boolean isReply() {
        return replyToMessageId != null && !replyToMessageId.trim().isEmpty();
    }
    
    /**
     * 检查是否为紧急消息
     * 
     * @return 是否为紧急消息
     */
    public boolean isUrgent() {
        return priority == MessagePriority.URGENT;
    }
    
    /**
     * 添加标签
     * 
     * @param tag 标签
     */
    public void addTag(String tag) {
        if (tags == null) {
            tags = tag;
        } else {
            tags += "," + tag;
        }
    }
    
    /**
     * 检查是否包含指定标签
     * 
     * @param tag 标签
     * @return 是否包含
     */
    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }
    
    /**
     * 获取消息完整信息
     * 
     * @return 完整信息描述
     */
    public String getFullInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("消息ID: ").append(messageId).append("\n");
        sb.append("发送者: ").append(fromPlayer).append("\n");
        sb.append("接收者: ").append(toPlayer).append("\n");
        sb.append("类型: ").append(type.getDescription()).append("\n");
        sb.append("状态: ").append(status.getDescription()).append("\n");
        sb.append("优先级: ").append(priority.getDescription()).append("\n");
        sb.append("发送时间: ").append(getFormattedSendTime()).append("\n");
        sb.append("是否已读: ").append(read ? "是" : "否").append("\n");
        
        if (hasAttachment()) {
            sb.append("附件: ").append(attachment).append("\n");
        }
        
        if (isReply()) {
            sb.append("回复消息: ").append(replyToMessageId).append("\n");
        }
        
        sb.append("内容: ").append(message);
        
        return sb.toString();
    }
    
    /**
     * 克隆消息（用于转发）
     * 
     * @return 克隆的消息
     */
    public PrivateMessage clone() {
        PrivateMessage cloned = new PrivateMessage();
        cloned.fromPlayer = this.fromPlayer;
        cloned.toPlayer = this.toPlayer;
        cloned.message = this.message;
        cloned.type = this.type;
        cloned.priority = this.priority;
        cloned.attachment = this.attachment;
        cloned.tags = this.tags;
        cloned.messageLength = this.messageLength;
        cloned.systemMessage = this.systemMessage;
        
        return cloned;
    }
} 