package com.mir2.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 邮件附件实体类
 * 
 * <p>用于存储和管理邮件附件，包括：</p>
 * <ul>
 *   <li>物品附件</li>
 *   <li>装备附件</li>
 *   <li>金币附件</li>
 *   <li>附件状态管理</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailAttachment {
    
    /** 附件ID */
    private String attachmentId;
    
    /** 邮件ID */
    private String mailId;
    
    /** 附件类型 */
    private AttachmentType type;
    
    /** 物品ID */
    private Integer itemId;
    
    /** 物品名称 */
    private String itemName;
    
    /** 物品数量 */
    private int quantity;
    
    /** 物品数据（序列化的装备属性等） */
    private String itemData;
    
    /** 是否绑定 */
    private boolean bound;
    
    /** 附件描述 */
    private String description;
    
    /** 附件状态 */
    private AttachmentStatus status;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 领取时间 */
    private LocalDateTime claimTime;
    
    /** 过期时间 */
    private LocalDateTime expireTime;
    
    /** 是否已过期 */
    private boolean expired;
    
    /** 附件价值评估 */
    private int estimatedValue;
    
    /**
     * 附件类型枚举
     */
    public enum AttachmentType {
        ITEM("物品"),
        EQUIPMENT("装备"),
        GOLD("金币"),
        EXPERIENCE("经验"),
        SKILL_BOOK("技能书"),
        POTION("药水"),
        MATERIAL("材料"),
        SPECIAL("特殊物品"),
        CURRENCY("货币"),
        TICKET("票券");
        
        private final String description;
        
        AttachmentType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 附件状态枚举
     */
    public enum AttachmentStatus {
        PENDING("待领取"),
        CLAIMED("已领取"),
        EXPIRED("已过期"),
        FAILED("领取失败"),
        RETURNED("已退回");
        
        private final String description;
        
        AttachmentStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 构造函数 - 物品附件
     * 
     * @param mailId 邮件ID
     * @param itemId 物品ID
     * @param itemName 物品名称
     * @param quantity 数量
     * @param bound 是否绑定
     */
    public MailAttachment(String mailId, Integer itemId, String itemName, int quantity, boolean bound) {
        this.attachmentId = generateAttachmentId();
        this.mailId = mailId;
        this.type = AttachmentType.ITEM;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.bound = bound;
        this.status = AttachmentStatus.PENDING;
        this.createTime = LocalDateTime.now();
        this.expired = false;
        this.estimatedValue = 0;
        
        // 设置过期时间（默认30天）
        this.expireTime = LocalDateTime.now().plusDays(30);
    }
    
    /**
     * 构造函数 - 装备附件
     * 
     * @param mailId 邮件ID
     * @param itemId 装备ID
     * @param itemName 装备名称
     * @param itemData 装备数据
     * @param bound 是否绑定
     */
    public MailAttachment(String mailId, Integer itemId, String itemName, String itemData, boolean bound) {
        this.attachmentId = generateAttachmentId();
        this.mailId = mailId;
        this.type = AttachmentType.EQUIPMENT;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = 1;
        this.itemData = itemData;
        this.bound = bound;
        this.status = AttachmentStatus.PENDING;
        this.createTime = LocalDateTime.now();
        this.expired = false;
        this.estimatedValue = 0;
        
        // 设置过期时间（默认30天）
        this.expireTime = LocalDateTime.now().plusDays(30);
    }
    
    /**
     * 生成附件ID
     * 
     * @return 附件ID
     */
    private String generateAttachmentId() {
        return "ATT_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
    
    /**
     * 检查附件是否过期
     * 
     * @return 是否过期
     */
    public boolean isExpired() {
        if (this.expireTime == null) {
            return false;
        }
        boolean expired = LocalDateTime.now().isAfter(this.expireTime);
        if (expired && !this.expired) {
            this.expired = true;
            this.status = AttachmentStatus.EXPIRED;
        }
        return expired;
    }
    
    /**
     * 领取附件
     * 
     * @return 是否成功领取
     */
    public boolean claim() {
        if (this.status != AttachmentStatus.PENDING) {
            return false;
        }
        
        if (isExpired()) {
            return false;
        }
        
        this.status = AttachmentStatus.CLAIMED;
        this.claimTime = LocalDateTime.now();
        return true;
    }
    
    /**
     * 退回附件
     * 
     * @return 是否成功退回
     */
    public boolean returnAttachment() {
        if (this.status == AttachmentStatus.CLAIMED) {
            return false;
        }
        
        this.status = AttachmentStatus.RETURNED;
        return true;
    }
    
    /**
     * 检查是否可以领取
     * 
     * @return 是否可以领取
     */
    public boolean canClaim() {
        return this.status == AttachmentStatus.PENDING && !isExpired();
    }
    
    /**
     * 获取附件显示名称
     * 
     * @return 显示名称
     */
    public String getDisplayName() {
        if (this.itemName != null) {
            if (this.quantity > 1) {
                return String.format("%s x%d", this.itemName, this.quantity);
            } else {
                return this.itemName;
            }
        }
        return this.type.getDescription();
    }
    
    /**
     * 获取附件详细信息
     * 
     * @return 详细信息
     */
    public String getDetailInfo() {
        StringBuilder info = new StringBuilder();
        info.append("类型: ").append(this.type.getDescription()).append("\n");
        
        if (this.itemName != null) {
            info.append("名称: ").append(this.itemName).append("\n");
        }
        
        if (this.quantity > 1) {
            info.append("数量: ").append(this.quantity).append("\n");
        }
        
        info.append("状态: ").append(this.status.getDescription()).append("\n");
        
        if (this.bound) {
            info.append("绑定: 是\n");
        }
        
        if (this.estimatedValue > 0) {
            info.append("估值: ").append(this.estimatedValue).append(" 金币\n");
        }
        
        if (this.expireTime != null) {
            info.append("过期时间: ").append(this.expireTime.toString()).append("\n");
        }
        
        return info.toString();
    }
    
    /**
     * 设置附件价值
     * 
     * @param value 价值
     */
    public void setEstimatedValue(int value) {
        this.estimatedValue = Math.max(0, value);
    }
    
    /**
     * 延长过期时间
     * 
     * @param days 延长天数
     */
    public void extendExpireTime(int days) {
        if (this.expireTime != null) {
            this.expireTime = this.expireTime.plusDays(days);
        }
    }
    
    /**
     * 创建物品附件
     * 
     * @param mailId 邮件ID
     * @param itemId 物品ID
     * @param itemName 物品名称
     * @param quantity 数量
     * @param bound 是否绑定
     * @return 物品附件
     */
    public static MailAttachment createItemAttachment(String mailId, Integer itemId, String itemName, int quantity, boolean bound) {
        return new MailAttachment(mailId, itemId, itemName, quantity, bound);
    }
    
    /**
     * 创建装备附件
     * 
     * @param mailId 邮件ID
     * @param itemId 装备ID
     * @param itemName 装备名称
     * @param itemData 装备数据
     * @param bound 是否绑定
     * @return 装备附件
     */
    public static MailAttachment createEquipmentAttachment(String mailId, Integer itemId, String itemName, String itemData, boolean bound) {
        return new MailAttachment(mailId, itemId, itemName, itemData, bound);
    }
    
    /**
     * 创建金币附件
     * 
     * @param mailId 邮件ID
     * @param amount 金币数量
     * @return 金币附件
     */
    public static MailAttachment createGoldAttachment(String mailId, int amount) {
        MailAttachment attachment = new MailAttachment();
        attachment.setAttachmentId(attachment.generateAttachmentId());
        attachment.setMailId(mailId);
        attachment.setType(AttachmentType.GOLD);
        attachment.setItemName("金币");
        attachment.setQuantity(amount);
        attachment.setBound(false);
        attachment.setStatus(AttachmentStatus.PENDING);
        attachment.setCreateTime(LocalDateTime.now());
        attachment.setExpired(false);
        attachment.setEstimatedValue(amount);
        attachment.setExpireTime(LocalDateTime.now().plusDays(30));
        return attachment;
    }
    
    /**
     * 创建经验附件
     * 
     * @param mailId 邮件ID
     * @param amount 经验数量
     * @return 经验附件
     */
    public static MailAttachment createExperienceAttachment(String mailId, long amount) {
        MailAttachment attachment = new MailAttachment();
        attachment.setAttachmentId(attachment.generateAttachmentId());
        attachment.setMailId(mailId);
        attachment.setType(AttachmentType.EXPERIENCE);
        attachment.setItemName("经验");
        attachment.setQuantity((int)amount);
        attachment.setBound(false);
        attachment.setStatus(AttachmentStatus.PENDING);
        attachment.setCreateTime(LocalDateTime.now());
        attachment.setExpired(false);
        attachment.setEstimatedValue((int)(amount / 100)); // 经验价值估算
        attachment.setExpireTime(LocalDateTime.now().plusDays(30));
        return attachment;
    }
    
    @Override
    public String toString() {
        return String.format("MailAttachment[id=%s, mailId=%s, type=%s, item=%s, quantity=%d, status=%s]",
                attachmentId, mailId, type, itemName, quantity, status);
    }
} 