package com.mir2.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 邮箱管理类
 * 
 * <p>用于管理玩家的邮件收发和存储，包括：</p>
 * <ul>
 *   <li>邮件存储管理</li>
 *   <li>邮件分类筛选</li>
 *   <li>邮件容量管理</li>
 *   <li>邮件自动清理</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
public class MailBox {
    
    /** 邮箱所有者 */
    private String ownerName;
    
    /** 收件箱 */
    private List<Mail> inboxMails;
    
    /** 发件箱 */
    private List<Mail> sentMails;
    
    /** 草稿箱 */
    private List<Mail> draftMails;
    
    /** 已删除邮件 */
    private List<Mail> deletedMails;
    
    /** 邮箱容量限制 */
    private int maxCapacity;
    
    /** 当前邮件数量 */
    private int currentCount;
    
    /** 未读邮件数量 */
    private int unreadCount;
    
    /** 邮箱设置 */
    private MailBoxSettings settings;
    
    /** 邮箱创建时间 */
    private LocalDateTime createTime;
    
    /** 最后访问时间 */
    private LocalDateTime lastAccessTime;
    
    /** 最后清理时间 */
    private LocalDateTime lastCleanTime;
    
    /** 邮件缓存 */
    private Map<String, Mail> mailCache;
    
    /** 邮件索引 */
    private Map<String, Set<String>> mailIndex;
    
    /**
     * 构造函数
     * 
     * @param ownerName 邮箱所有者
     */
    public MailBox(String ownerName) {
        this.ownerName = ownerName;
        this.inboxMails = new ArrayList<>();
        this.sentMails = new ArrayList<>();
        this.draftMails = new ArrayList<>();
        this.deletedMails = new ArrayList<>();
        this.maxCapacity = 100; // 默认邮箱容量
        this.currentCount = 0;
        this.unreadCount = 0;
        this.settings = new MailBoxSettings();
        this.createTime = LocalDateTime.now();
        this.lastAccessTime = LocalDateTime.now();
        this.lastCleanTime = LocalDateTime.now();
        this.mailCache = new HashMap<>();
        this.mailIndex = new HashMap<>();
        
        // 初始化邮件索引
        initializeMailIndex();
    }
    
    /**
     * 初始化邮件索引
     */
    private void initializeMailIndex() {
        mailIndex.put("sender", new HashSet<>());
        mailIndex.put("subject", new HashSet<>());
        mailIndex.put("type", new HashSet<>());
        mailIndex.put("tag", new HashSet<>());
    }
    
    /**
     * 接收邮件
     * 
     * @param mail 邮件
     * @return 是否成功接收
     */
    public boolean receiveMail(Mail mail) {
        if (mail == null || !mail.getReceiverName().equals(this.ownerName)) {
            return false;
        }
        
        // 检查邮箱容量
        if (currentCount >= maxCapacity) {
            // 自动清理过期邮件
            cleanExpiredMails();
            
            // 如果还是满的，拒绝接收
            if (currentCount >= maxCapacity) {
                return false;
            }
        }
        
        // 添加到收件箱
        inboxMails.add(mail);
        mailCache.put(mail.getMailId(), mail);
        
        // 更新统计
        currentCount++;
        if (!mail.isRead()) {
            unreadCount++;
        }
        
        // 更新索引
        updateMailIndex(mail);
        
        // 更新访问时间
        lastAccessTime = LocalDateTime.now();
        
        return true;
    }
    
    /**
     * 发送邮件
     * 
     * @param mail 邮件
     * @return 是否成功发送
     */
    public boolean sendMail(Mail mail) {
        if (mail == null || !mail.getSenderName().equals(this.ownerName)) {
            return false;
        }
        
        // 添加到发件箱
        sentMails.add(mail);
        mailCache.put(mail.getMailId(), mail);
        
        // 更新统计
        currentCount++;
        
        // 更新索引
        updateMailIndex(mail);
        
        // 更新访问时间
        lastAccessTime = LocalDateTime.now();
        
        return true;
    }
    
    /**
     * 保存草稿
     * 
     * @param mail 邮件
     * @return 是否成功保存
     */
    public boolean saveDraft(Mail mail) {
        if (mail == null) {
            return false;
        }
        
        mail.setStatus(Mail.MailStatus.DRAFT);
        
        // 添加到草稿箱
        draftMails.add(mail);
        mailCache.put(mail.getMailId(), mail);
        
        // 更新统计
        currentCount++;
        
        // 更新索引
        updateMailIndex(mail);
        
        // 更新访问时间
        lastAccessTime = LocalDateTime.now();
        
        return true;
    }
    
    /**
     * 删除邮件
     * 
     * @param mailId 邮件ID
     * @return 是否成功删除
     */
    public boolean deleteMail(String mailId) {
        Mail mail = mailCache.get(mailId);
        if (mail == null) {
            return false;
        }
        
        // 检查是否可以删除
        if (!mail.canDelete()) {
            return false;
        }
        
        // 从相应的箱子中移除
        boolean removed = false;
        if (inboxMails.remove(mail)) {
            removed = true;
            if (!mail.isRead()) {
                unreadCount--;
            }
        }
        
        if (!removed) {
            removed = sentMails.remove(mail) || draftMails.remove(mail);
        }
        
        if (removed) {
            // 添加到已删除邮件（软删除）
            mail.setStatus(Mail.MailStatus.DELETED);
            deletedMails.add(mail);
            
            // 更新统计
            currentCount--;
            
            // 更新访问时间
            lastAccessTime = LocalDateTime.now();
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 永久删除邮件
     * 
     * @param mailId 邮件ID
     * @return 是否成功删除
     */
    public boolean permanentlyDeleteMail(String mailId) {
        Mail mail = mailCache.get(mailId);
        if (mail == null) {
            return false;
        }
        
        // 从已删除邮件中移除
        boolean removed = deletedMails.remove(mail);
        
        if (removed) {
            // 从缓存中移除
            mailCache.remove(mailId);
            
            // 从索引中移除
            removeFromMailIndex(mail);
            
            // 更新访问时间
            lastAccessTime = LocalDateTime.now();
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 标记邮件为已读
     * 
     * @param mailId 邮件ID
     * @return 是否成功标记
     */
    public boolean markAsRead(String mailId) {
        Mail mail = mailCache.get(mailId);
        if (mail == null) {
            return false;
        }
        
        if (!mail.isRead()) {
            mail.markAsRead();
            unreadCount--;
            
            // 更新访问时间
            lastAccessTime = LocalDateTime.now();
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 领取邮件附件
     * 
     * @param mailId 邮件ID
     * @return 邮件附件列表
     */
    public List<MailAttachment> claimAttachments(String mailId) {
        Mail mail = mailCache.get(mailId);
        if (mail == null || !mail.isHasAttachment()) {
            return new ArrayList<>();
        }
        
        if (mail.claimAttachments()) {
            // 更新访问时间
            lastAccessTime = LocalDateTime.now();
            
            return new ArrayList<>(mail.getAttachments());
        }
        
        return new ArrayList<>();
    }
    
    /**
     * 获取收件箱邮件
     * 
     * @return 收件箱邮件列表
     */
    public List<Mail> getInboxMails() {
        return getInboxMails(0, Integer.MAX_VALUE);
    }
    
    /**
     * 获取收件箱邮件（分页）
     * 
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 收件箱邮件列表
     */
    public List<Mail> getInboxMails(int offset, int limit) {
        return inboxMails.stream()
                .sorted((m1, m2) -> m2.getSendTime().compareTo(m1.getSendTime()))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取发件箱邮件
     * 
     * @return 发件箱邮件列表
     */
    public List<Mail> getSentMails() {
        return getSentMails(0, Integer.MAX_VALUE);
    }
    
    /**
     * 获取发件箱邮件（分页）
     * 
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 发件箱邮件列表
     */
    public List<Mail> getSentMails(int offset, int limit) {
        return sentMails.stream()
                .sorted((m1, m2) -> m2.getSendTime().compareTo(m1.getSendTime()))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取草稿箱邮件
     * 
     * @return 草稿箱邮件列表
     */
    public List<Mail> getDraftMails() {
        return new ArrayList<>(draftMails);
    }
    
    /**
     * 获取未读邮件
     * 
     * @return 未读邮件列表
     */
    public List<Mail> getUnreadMails() {
        return inboxMails.stream()
                .filter(mail -> !mail.isRead())
                .sorted((m1, m2) -> m2.getSendTime().compareTo(m1.getSendTime()))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取有附件的邮件
     * 
     * @return 有附件的邮件列表
     */
    public List<Mail> getMailsWithAttachments() {
        return inboxMails.stream()
                .filter(mail -> mail.isHasAttachment() && !mail.isAttachmentClaimed())
                .sorted((m1, m2) -> m2.getSendTime().compareTo(m1.getSendTime()))
                .collect(Collectors.toList());
    }
    
    /**
     * 按类型筛选邮件
     * 
     * @param type 邮件类型
     * @return 邮件列表
     */
    public List<Mail> getMailsByType(Mail.MailType type) {
        return inboxMails.stream()
                .filter(mail -> mail.getType() == type)
                .sorted((m1, m2) -> m2.getSendTime().compareTo(m1.getSendTime()))
                .collect(Collectors.toList());
    }
    
    /**
     * 搜索邮件
     * 
     * @param keyword 关键词
     * @return 邮件列表
     */
    public List<Mail> searchMails(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerKeyword = keyword.toLowerCase();
        
        return inboxMails.stream()
                .filter(mail -> 
                    mail.getSenderName().toLowerCase().contains(lowerKeyword) ||
                    mail.getSubject().toLowerCase().contains(lowerKeyword) ||
                    mail.getContent().toLowerCase().contains(lowerKeyword)
                )
                .sorted((m1, m2) -> m2.getSendTime().compareTo(m1.getSendTime()))
                .collect(Collectors.toList());
    }
    
    /**
     * 清理过期邮件
     */
    public void cleanExpiredMails() {
        LocalDateTime now = LocalDateTime.now();
        
        // 清理收件箱过期邮件
        Iterator<Mail> inboxIterator = inboxMails.iterator();
        while (inboxIterator.hasNext()) {
            Mail mail = inboxIterator.next();
            if (mail.isExpired() && mail.canDelete()) {
                inboxIterator.remove();
                mailCache.remove(mail.getMailId());
                removeFromMailIndex(mail);
                currentCount--;
                
                if (!mail.isRead()) {
                    unreadCount--;
                }
            }
        }
        
        // 清理发件箱过期邮件
        Iterator<Mail> sentIterator = sentMails.iterator();
        while (sentIterator.hasNext()) {
            Mail mail = sentIterator.next();
            if (mail.isExpired() && mail.canDelete()) {
                sentIterator.remove();
                mailCache.remove(mail.getMailId());
                removeFromMailIndex(mail);
                currentCount--;
            }
        }
        
        // 清理已删除邮件（超过30天）
        LocalDateTime deleteThreshold = now.minusDays(30);
        Iterator<Mail> deletedIterator = deletedMails.iterator();
        while (deletedIterator.hasNext()) {
            Mail mail = deletedIterator.next();
            if (mail.getUpdateTime().isBefore(deleteThreshold)) {
                deletedIterator.remove();
                mailCache.remove(mail.getMailId());
                removeFromMailIndex(mail);
            }
        }
        
        lastCleanTime = now;
    }
    
    /**
     * 更新邮件索引
     * 
     * @param mail 邮件
     */
    private void updateMailIndex(Mail mail) {
        mailIndex.get("sender").add(mail.getSenderName());
        mailIndex.get("subject").add(mail.getSubject());
        mailIndex.get("type").add(mail.getType().name());
        
        if (mail.getTags() != null) {
            mailIndex.get("tag").addAll(mail.getTags());
        }
    }
    
    /**
     * 从邮件索引中移除
     * 
     * @param mail 邮件
     */
    private void removeFromMailIndex(Mail mail) {
        // 这里简化处理，实际应该检查是否还有其他邮件包含相同的索引项
        // 由于复杂性，这里暂时不实现完整的移除逻辑
    }
    
    /**
     * 获取邮箱统计信息
     * 
     * @return 邮箱统计信息
     */
    public MailBoxStats getStats() {
        return new MailBoxStats(
            currentCount,
            unreadCount,
            inboxMails.size(),
            sentMails.size(),
            draftMails.size(),
            deletedMails.size(),
            maxCapacity,
            getMailsWithAttachments().size()
        );
    }
    
    /**
     * 扩展邮箱容量
     * 
     * @param additionalCapacity 额外容量
     */
    public void expandCapacity(int additionalCapacity) {
        this.maxCapacity += additionalCapacity;
    }
    
    /**
     * 邮箱设置类
     */
    @Data
    public static class MailBoxSettings {
        /** 自动删除过期邮件 */
        private boolean autoDeleteExpired = true;
        
        /** 邮件过期天数 */
        private int mailExpireDays = 30;
        
        /** 自动标记已读 */
        private boolean autoMarkRead = false;
        
        /** 邮件排序方式 */
        private String sortOrder = "TIME_DESC";
        
        /** 显示邮件数量 */
        private int displayCount = 20;
        
        /** 邮件通知 */
        private boolean enableNotification = true;
    }
    
    /**
     * 邮箱统计信息类
     */
    @Data
    public static class MailBoxStats {
        private final int totalMails;
        private final int unreadMails;
        private final int inboxMails;
        private final int sentMails;
        private final int draftMails;
        private final int deletedMails;
        private final int maxCapacity;
        private final int mailsWithAttachments;
        
        public MailBoxStats(int totalMails, int unreadMails, int inboxMails, 
                           int sentMails, int draftMails, int deletedMails, 
                           int maxCapacity, int mailsWithAttachments) {
            this.totalMails = totalMails;
            this.unreadMails = unreadMails;
            this.inboxMails = inboxMails;
            this.sentMails = sentMails;
            this.draftMails = draftMails;
            this.deletedMails = deletedMails;
            this.maxCapacity = maxCapacity;
            this.mailsWithAttachments = mailsWithAttachments;
        }
        
        public double getUsagePercentage() {
            return maxCapacity > 0 ? (double) totalMails / maxCapacity * 100 : 0;
        }
    }
} 