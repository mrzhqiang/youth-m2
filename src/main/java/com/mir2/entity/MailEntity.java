package com.mir2.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 邮件JPA实体类
 * 
 * <p>用于数据库持久化的邮件实体，包括：</p>
 * <ul>
 *   <li>邮件基本信息</li>
 *   <li>邮件内容</li>
 *   <li>附件信息</li>
 *   <li>邮件状态</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "mail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "mail_id", unique = true, nullable = false, length = 100)
    private String mailId;
    
    @Column(name = "sender", nullable = false, length = 50)
    private String sender;
    
    @Column(name = "receiver", nullable = false, length = 50)
    private String receiver;
    
    @Column(name = "subject", nullable = false, length = 200)
    private String subject;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private Mail.MailType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Mail.MailStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private Mail.MailPriority priority;
    
    @Column(name = "send_time", nullable = false)
    private LocalDateTime sendTime;
    
    @Column(name = "read_time")
    private LocalDateTime readTime;
    
    @Column(name = "expire_time")
    private LocalDateTime expireTime;
    
    @Column(name = "auto_delete", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean autoDelete;
    
    @Column(name = "has_attachment", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean hasAttachment;
    
    @Column(name = "attachment_retrieved", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean attachmentRetrieved;
    
    @Column(name = "tags", length = 500)
    private String tags; // 用逗号分隔的标签
    
    @Column(name = "reply_to")
    private String replyTo;
    
    @Column(name = "reply_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer replyCount;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
    
    @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deleted;
    
    @OneToMany(mappedBy = "mail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MailAttachmentEntity> attachments;
    
    /**
     * 构造函数
     * 
     * @param mailId 邮件ID
     * @param sender 发送者
     * @param receiver 接收者
     * @param subject 主题
     * @param content 内容
     * @param type 邮件类型
     */
    public MailEntity(String mailId, String sender, String receiver, String subject, 
                     String content, Mail.MailType type) {
        this.mailId = mailId;
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.content = content;
        this.type = type;
        this.status = Mail.MailStatus.UNREAD;
        this.priority = Mail.MailPriority.NORMAL;
        this.sendTime = LocalDateTime.now();
        this.autoDelete = false;
        this.hasAttachment = false;
        this.attachmentRetrieved = false;
        this.replyCount = 0;
        this.deleted = false;
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }
    
    /**
     * 从Mail转换为Entity
     * 
     * @param mail 邮件对象
     * @return MailEntity
     */
    public static MailEntity fromMail(Mail mail) {
        MailEntity entity = new MailEntity();
        entity.setMailId(mail.getId());
        entity.setSender(mail.getSender());
        entity.setReceiver(mail.getReceiver());
        entity.setSubject(mail.getSubject());
        entity.setContent(mail.getContent());
        entity.setType(mail.getType());
        entity.setStatus(mail.getStatus());
        entity.setPriority(mail.getPriority());
        entity.setSendTime(convertToLocalDateTime(mail.getSendTime()));
        entity.setReadTime(convertToLocalDateTime(mail.getReadTime()));
        entity.setExpireTime(convertToLocalDateTime(mail.getExpireTime()));
        entity.setAutoDelete(mail.isAutoDelete());
        entity.setHasAttachment(mail.hasAttachment());
        entity.setAttachmentRetrieved(mail.isAttachmentRetrieved());
        entity.setTags(String.join(",", mail.getTags()));
        entity.setReplyTo(mail.getReplyTo());
        entity.setReplyCount(mail.getReplyCount());
        entity.setDeleted(false);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        return entity;
    }
    
    /**
     * 转换为Mail对象
     * 
     * @return Mail对象
     */
    public Mail toMail() {
        Mail mail = new Mail();
        mail.setId(this.mailId);
        mail.setSender(this.sender);
        mail.setReceiver(this.receiver);
        mail.setSubject(this.subject);
        mail.setContent(this.content);
        mail.setType(this.type);
        mail.setStatus(this.status);
        mail.setPriority(this.priority);
        mail.setSendTime(convertToDate(this.sendTime));
        mail.setReadTime(convertToDate(this.readTime));
        mail.setExpireTime(convertToDate(this.expireTime));
        mail.setAutoDelete(this.autoDelete != null ? this.autoDelete : false);
        mail.setHasAttachment(this.hasAttachment != null ? this.hasAttachment : false);
        mail.setAttachmentRetrieved(this.attachmentRetrieved != null ? this.attachmentRetrieved : false);
        mail.setReplyTo(this.replyTo);
        mail.setReplyCount(this.replyCount != null ? this.replyCount : 0);
        
        // 转换标签
        if (this.tags != null && !this.tags.trim().isEmpty()) {
            String[] tagArray = this.tags.split(",");
            for (String tag : tagArray) {
                if (!tag.trim().isEmpty()) {
                    mail.addTag(tag.trim());
                }
            }
        }
        
        // 转换附件
        if (this.attachments != null) {
            for (MailAttachmentEntity attachmentEntity : this.attachments) {
                mail.addAttachment(attachmentEntity.toMailAttachment());
            }
        }
        
        return mail;
    }
    
    /**
     * 更新时间戳
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedTime = LocalDateTime.now();
    }
    
    @PrePersist
    public void prePersist() {
        if (this.createdTime == null) {
            this.createdTime = LocalDateTime.now();
        }
        if (this.updatedTime == null) {
            this.updatedTime = LocalDateTime.now();
        }
        if (this.sendTime == null) {
            this.sendTime = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = Mail.MailStatus.UNREAD;
        }
        if (this.priority == null) {
            this.priority = Mail.MailPriority.NORMAL;
        }
        if (this.autoDelete == null) {
            this.autoDelete = false;
        }
        if (this.hasAttachment == null) {
            this.hasAttachment = false;
        }
        if (this.attachmentRetrieved == null) {
            this.attachmentRetrieved = false;
        }
        if (this.replyCount == null) {
            this.replyCount = 0;
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
} 