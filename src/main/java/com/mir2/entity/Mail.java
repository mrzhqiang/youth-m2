package com.mir2.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * 邮件实体类
 * 
 * <p>用于存储和管理游戏内邮件系统，包括：</p>
 * <ul>
 *   <li>玩家邮件收发</li>
 *   <li>系统邮件发送</li>
 *   <li>邮件附件管理</li>
 *   <li>邮件状态跟踪</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mail {
    
    /** 邮件ID */
    private String mailId;
    
    /** 发送者名称 */
    private String senderName;
    
    /** 接收者名称 */
    private String receiverName;
    
    /** 邮件主题 */
    private String subject;
    
    /** 邮件内容 */
    private String content;
    
    /** 邮件类型 */
    private MailType type;
    
    /** 邮件状态 */
    private MailStatus status;
    
    /** 优先级 */
    private MailPriority priority;
    
    /** 发送时间 */
    private LocalDateTime sendTime;
    
    /** 读取时间 */
    private LocalDateTime readTime;
    
    /** 过期时间 */
    private LocalDateTime expireTime;
    
    /** 是否已读 */
    private boolean isRead;
    
    /** 是否有附件 */
    private boolean hasAttachment;
    
    /** 附件列表 */
    private List<MailAttachment> attachments;
    
    /** 金币附件 */
    private int goldAttachment;
    
    /** 是否已领取附件 */
    private boolean attachmentClaimed;
    
    /** 邮件标签 */
    private List<String> tags;
    
    /** 回复邮件ID */
    private String replyToMailId;
    
    /** 转发原邮件ID */
    private String forwardFromMailId;
    
    /** 是否自动删除 */
    private boolean autoDelete;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 最后更新时间 */
    private LocalDateTime updateTime;
    
    /**
     * 邮件类型枚举
     */
    public enum MailType {
        PLAYER_TO_PLAYER("玩家邮件"),
        SYSTEM("系统邮件"),
        GUILD("行会邮件"),
        AUCTION("拍卖行邮件"),
        TRADE("交易邮件"),
        QUEST_REWARD("任务奖励"),
        EVENT_REWARD("活动奖励"),
        COMPENSATION("补偿邮件"),
        NOTIFICATION("通知邮件"),
        ADMIN("管理员邮件");
        
        private final String description;
        
        MailType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 邮件状态枚举
     */
    public enum MailStatus {
        DRAFT("草稿"),
        SENT("已发送"),
        DELIVERED("已送达"),
        READ("已读"),
        REPLIED("已回复"),
        FORWARDED("已转发"),
        ARCHIVED("已归档"),
        DELETED("已删除"),
        EXPIRED("已过期"),
        FAILED("发送失败");
        
        private final String description;
        
        MailStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 邮件优先级枚举
     */
    public enum MailPriority {
        LOW("低"),
        NORMAL("普通"),
        HIGH("高"),
        URGENT("紧急"),
        CRITICAL("严重");
        
        private final String description;
        
        MailPriority(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 构造函数 - 玩家邮件
     * 
     * @param senderName 发送者名称
     * @param receiverName 接收者名称
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    public Mail(String senderName, String receiverName, String subject, String content) {
        this.mailId = generateMailId();
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.subject = subject;
        this.content = content;
        this.type = MailType.PLAYER_TO_PLAYER;
        this.status = MailStatus.SENT;
        this.priority = MailPriority.NORMAL;
        this.sendTime = LocalDateTime.now();
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.isRead = false;
        this.hasAttachment = false;
        this.attachmentClaimed = false;
        this.autoDelete = true;
        this.attachments = new ArrayList<>();
        this.tags = new ArrayList<>();
        
        // 设置过期时间（默认30天）
        this.expireTime = LocalDateTime.now().plusDays(30);
    }
    
    /**
     * 生成邮件ID
     * 
     * @return 邮件ID
     */
    private String generateMailId() {
        return "MAIL_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
    
    /**
     * 添加附件
     * 
     * @param attachment 附件
     */
    public void addAttachment(MailAttachment attachment) {
        if (this.attachments == null) {
            this.attachments = new ArrayList<>();
        }
        this.attachments.add(attachment);
        this.hasAttachment = true;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 移除附件
     * 
     * @param attachmentId 附件ID
     */
    public void removeAttachment(String attachmentId) {
        if (this.attachments != null) {
            this.attachments.removeIf(attachment -> attachment.getAttachmentId().equals(attachmentId));
            this.hasAttachment = !this.attachments.isEmpty() || this.goldAttachment > 0;
            this.updateTime = LocalDateTime.now();
        }
    }
    
    /**
     * 添加金币附件
     * 
     * @param amount 金币数量
     */
    public void addGoldAttachment(int amount) {
        this.goldAttachment += amount;
        this.hasAttachment = true;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 标记为已读
     */
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readTime = LocalDateTime.now();
            this.status = MailStatus.READ;
            this.updateTime = LocalDateTime.now();
        }
    }
    
    /**
     * 领取附件
     * 
     * @return 是否成功领取
     */
    public boolean claimAttachments() {
        if (!this.hasAttachment || this.attachmentClaimed) {
            return false;
        }
        
        this.attachmentClaimed = true;
        this.updateTime = LocalDateTime.now();
        return true;
    }
    
    /**
     * 检查邮件是否过期
     * 
     * @return 是否过期
     */
    public boolean isExpired() {
        if (this.expireTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.expireTime);
    }
    
    /**
     * 检查邮件是否可以删除
     * 
     * @return 是否可以删除
     */
    public boolean canDelete() {
        // 未读邮件不能删除
        if (!this.isRead) {
            return false;
        }
        
        // 有未领取附件的邮件不能删除
        if (this.hasAttachment && !this.attachmentClaimed) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 添加标签
     * 
     * @param tag 标签
     */
    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
            this.updateTime = LocalDateTime.now();
        }
    }
    
    /**
     * 移除标签
     * 
     * @param tag 标签
     */
    public void removeTag(String tag) {
        if (this.tags != null) {
            this.tags.remove(tag);
            this.updateTime = LocalDateTime.now();
        }
    }
    
    /**
     * 检查是否有特定标签
     * 
     * @param tag 标签
     * @return 是否有标签
     */
    public boolean hasTag(String tag) {
        return this.tags != null && this.tags.contains(tag);
    }
    
    /**
     * 获取邮件大小（字节）
     * 
     * @return 邮件大小
     */
    public int getMailSize() {
        int size = 0;
        
        // 基础邮件内容大小
        if (subject != null) size += subject.length() * 2; // Unicode字符
        if (content != null) size += content.length() * 2;
        
        // 附件大小
        if (attachments != null) {
            size += attachments.size() * 100; // 每个附件估算100字节
        }
        
        return size;
    }
    
    /**
     * 创建系统邮件
     * 
     * @param receiverName 接收者
     * @param subject 主题
     * @param content 内容
     * @param type 邮件类型
     * @return 系统邮件
     */
    public static Mail createSystemMail(String receiverName, String subject, String content, MailType type) {
        Mail mail = new Mail();
        mail.setMailId(mail.generateMailId());
        mail.setSenderName("系统");
        mail.setReceiverName(receiverName);
        mail.setSubject(subject);
        mail.setContent(content);
        mail.setType(type);
        mail.setStatus(MailStatus.SENT);
        mail.setPriority(MailPriority.NORMAL);
        mail.setSendTime(LocalDateTime.now());
        mail.setCreateTime(LocalDateTime.now());
        mail.setUpdateTime(LocalDateTime.now());
        mail.setRead(false);
        mail.setHasAttachment(false);
        mail.setAttachmentClaimed(false);
        mail.setAutoDelete(true);
        mail.setAttachments(new ArrayList<>());
        mail.setTags(new ArrayList<>());
        mail.setExpireTime(LocalDateTime.now().plusDays(30));
        
        return mail;
    }
    
    /**
     * 创建带附件的系统邮件
     * 
     * @param receiverName 接收者
     * @param subject 主题
     * @param content 内容
     * @param type 邮件类型
     * @param attachments 附件列表
     * @param goldAmount 金币数量
     * @return 系统邮件
     */
    public static Mail createSystemMailWithAttachments(String receiverName, String subject, String content, 
                                                      MailType type, List<MailAttachment> attachments, int goldAmount) {
        Mail mail = createSystemMail(receiverName, subject, content, type);
        
        if (attachments != null && !attachments.isEmpty()) {
            mail.setAttachments(new ArrayList<>(attachments));
            mail.setHasAttachment(true);
        }
        
        if (goldAmount > 0) {
            mail.setGoldAttachment(goldAmount);
            mail.setHasAttachment(true);
        }
        
        return mail;
    }
    
    @Override
    public String toString() {
        return String.format("Mail[id=%s, from=%s, to=%s, subject=%s, type=%s, status=%s, read=%s]",
                mailId, senderName, receiverName, subject, type, status, isRead);
    }
} 