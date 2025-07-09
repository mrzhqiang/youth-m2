package com.mir2.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 邮件附件JPA实体类
 * 
 * <p>用于数据库持久化的邮件附件实体，包括：</p>
 * <ul>
 *   <li>附件基本信息</li>
 *   <li>附件内容</li>
 *   <li>附件状态</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "mail_attachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailAttachmentEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "attachment_id", unique = true, nullable = false, length = 100)
    private String attachmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_id", nullable = false)
    private MailEntity mail;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private MailAttachment.AttachmentType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MailAttachment.AttachmentStatus status;
    
    @Column(name = "name", length = 200)
    private String name;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "item_id")
    private Integer itemId;
    
    @Column(name = "equipment_id")
    private Integer equipmentId;
    
    @Column(name = "gold_amount")
    private Long goldAmount;
    
    @Column(name = "experience_amount")
    private Long experienceAmount;
    
    @Column(name = "skill_id")
    private Integer skillId;
    
    @Column(name = "buff_id")
    private Integer buffId;
    
    @Column(name = "item_data", columnDefinition = "TEXT")
    private String itemData; // JSON格式存储物品数据
    
    @Column(name = "custom_data", columnDefinition = "TEXT")
    private String customData; // JSON格式存储自定义数据
    
    @Column(name = "quantity", nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer quantity;
    
    @Column(name = "min_level")
    private Integer minLevel;
    
    @Column(name = "max_level")
    private Integer maxLevel;
    
    @Column(name = "required_job")
    private Integer requiredJob;
    
    @Column(name = "expire_time")
    private LocalDateTime expireTime;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
    
    @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean deleted;
    
    /**
     * 构造函数
     * 
     * @param attachmentId 附件ID
     * @param mail 邮件实体
     * @param type 附件类型
     */
    public MailAttachmentEntity(String attachmentId, MailEntity mail, MailAttachment.AttachmentType type) {
        this.attachmentId = attachmentId;
        this.mail = mail;
        this.type = type;
        this.status = MailAttachment.AttachmentStatus.PENDING;
        this.quantity = 1;
        this.deleted = false;
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }
    
    /**
     * 从MailAttachment转换为Entity
     * 
     * @param attachment 附件对象
     * @param mailEntity 邮件实体
     * @return MailAttachmentEntity
     */
    public static MailAttachmentEntity fromMailAttachment(MailAttachment attachment, MailEntity mailEntity) {
        MailAttachmentEntity entity = new MailAttachmentEntity();
        entity.setAttachmentId(attachment.getId());
        entity.setMail(mailEntity);
        entity.setType(attachment.getType());
        entity.setStatus(attachment.getStatus());
        entity.setName(attachment.getName());
        entity.setDescription(attachment.getDescription());
        entity.setItemId(attachment.getItemId());
        entity.setEquipmentId(attachment.getEquipmentId());
        entity.setGoldAmount(attachment.getGoldAmount());
        entity.setExperienceAmount(attachment.getExperienceAmount());
        entity.setSkillId(attachment.getSkillId());
        entity.setBuffId(attachment.getBuffId());
        entity.setQuantity(attachment.getQuantity());
        entity.setMinLevel(attachment.getMinLevel());
        entity.setMaxLevel(attachment.getMaxLevel());
        entity.setRequiredJob(attachment.getRequiredJob());
        entity.setExpireTime(convertToLocalDateTime(attachment.getExpireTime()));
        entity.setDeleted(false);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        
        // 转换数据为JSON
        entity.setItemData(convertObjectToJson(attachment.getItemData()));
        entity.setCustomData(convertObjectToJson(attachment.getCustomData()));
        
        return entity;
    }
    
    /**
     * 转换为MailAttachment对象
     * 
     * @return MailAttachment对象
     */
    public MailAttachment toMailAttachment() {
        MailAttachment attachment = new MailAttachment();
        attachment.setId(this.attachmentId);
        attachment.setType(this.type);
        attachment.setStatus(this.status);
        attachment.setName(this.name);
        attachment.setDescription(this.description);
        attachment.setItemId(this.itemId);
        attachment.setEquipmentId(this.equipmentId);
        attachment.setGoldAmount(this.goldAmount);
        attachment.setExperienceAmount(this.experienceAmount);
        attachment.setSkillId(this.skillId);
        attachment.setBuffId(this.buffId);
        attachment.setQuantity(this.quantity != null ? this.quantity : 1);
        attachment.setMinLevel(this.minLevel);
        attachment.setMaxLevel(this.maxLevel);
        attachment.setRequiredJob(this.requiredJob);
        attachment.setExpireTime(convertToDate(this.expireTime));
        
        // 转换JSON数据
        attachment.setItemData(convertJsonToObject(this.itemData));
        attachment.setCustomData(convertJsonToObject(this.customData));
        
        return attachment;
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
        if (this.status == null) {
            this.status = MailAttachment.AttachmentStatus.PENDING;
        }
        if (this.quantity == null) {
            this.quantity = 1;
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
    
    private static String convertObjectToJson(Object obj) {
        if (obj == null) return null;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }
    
    private static Object convertJsonToObject(String json) {
        if (json == null || json.trim().isEmpty()) return null;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, Object.class);
        } catch (Exception e) {
            return null;
        }
    }
} 