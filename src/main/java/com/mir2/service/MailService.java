package com.mir2.service;

import com.mir2.core.model.Player;
import com.mir2.core.model.Item;
import com.mir2.entity.Mail;
import com.mir2.entity.MailAttachment;
import com.mir2.entity.MailBox;
import com.mir2.entity.MailEntity;
import com.mir2.entity.MailAttachmentEntity;
import com.mir2.repository.MailRepository;
import com.mir2.repository.MailAttachmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 邮件服务类
 * 
 * <p>负责游戏内邮件系统的管理，包括：</p>
 * <ul>
 *   <li>邮件发送与接收</li>
 *   <li>邮件附件管理</li>
 *   <li>邮箱容量管理</li>
 *   <li>邮件过期清理</li>
 *   <li>系统邮件发送</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class MailService {
    
    /** 玩家邮箱缓存 */
    private final Map<String, MailBox> playerMailBoxes = new ConcurrentHashMap<>();
    
    /** 邮件发送历史 */
    private final Map<String, List<String>> sendHistory = new ConcurrentHashMap<>();
    
    /** 邮件发送冷却时间（毫秒） */
    private static final long SEND_COOLDOWN = 5000; // 5秒
    
    /** 每日发送限制 */
    private static final int DAILY_SEND_LIMIT = 100;
    
    /** 邮件内容最大长度 */
    private static final int MAX_CONTENT_LENGTH = 1000;
    
    /** 邮件主题最大长度 */
    private static final int MAX_SUBJECT_LENGTH = 100;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private MailRepository mailRepository;
    
    @Autowired
    private MailAttachmentRepository mailAttachmentRepository;
    
    /**
     * 发送玩家邮件
     * 
     * @param senderName 发送者名称
     * @param receiverName 接收者名称
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 发送结果
     */
    @Transactional
    public MailResult sendPlayerMail(String senderName, String receiverName, String subject, String content) {
        try {
            // 验证参数
            if (senderName == null || receiverName == null || subject == null || content == null) {
                return new MailResult(false, "邮件参数不能为空");
            }
            
            // 检查发送者是否存在
            Player sender = playerService.getPlayerByName(senderName);
            if (sender == null) {
                return new MailResult(false, "发送者不存在");
            }
            
            // 检查接收者是否存在
            Player receiver = playerService.getPlayerByName(receiverName);
            if (receiver == null) {
                return new MailResult(false, "接收者不存在");
            }
            
            // 检查是否给自己发送邮件
            if (senderName.equals(receiverName)) {
                return new MailResult(false, "不能给自己发送邮件");
            }
            
            // 验证邮件内容长度
            if (subject.length() > MAX_SUBJECT_LENGTH) {
                return new MailResult(false, "邮件主题过长");
            }
            
            if (content.length() > MAX_CONTENT_LENGTH) {
                return new MailResult(false, "邮件内容过长");
            }
            
            // 检查发送冷却
            if (!checkSendCooldown(senderName)) {
                return new MailResult(false, "发送邮件过于频繁，请稍后再试");
            }
            
            // 检查日发送限制
            if (!checkDailySendLimit(senderName)) {
                return new MailResult(false, "今日发送邮件数量已达上限");
            }
            
            // 创建邮件
            Mail mail = new Mail(senderName, receiverName, subject, content);
            
            // 发送邮件
            return sendMail(mail);
            
        } catch (Exception e) {
            log.error("发送玩家邮件失败: from={}, to={}, subject={}", senderName, receiverName, subject, e);
            return new MailResult(false, "发送邮件失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送带附件的邮件
     * 
     * @param senderName 发送者名称
     * @param receiverName 接收者名称
     * @param subject 邮件主题
     * @param content 邮件内容
     * @param attachments 附件列表
     * @param goldAmount 金币数量
     * @return 发送结果
     */
    @Transactional
    public MailResult sendMailWithAttachments(String senderName, String receiverName, String subject, 
                                            String content, List<MailAttachment> attachments, int goldAmount) {
        try {
            // 基础验证
            MailResult basicResult = sendPlayerMail(senderName, receiverName, subject, content);
            if (!basicResult.isSuccess()) {
                return basicResult;
            }
            
            // 验证附件
            if (attachments != null && attachments.size() > 10) {
                return new MailResult(false, "邮件附件数量不能超过10个");
            }
            
            // 验证金币数量
            if (goldAmount > 0) {
                Player sender = playerService.getPlayerByName(senderName);
                if (sender.getGold() < goldAmount) {
                    return new MailResult(false, "金币不足");
                }
            }
            
            // 验证物品附件
            if (attachments != null) {
                for (MailAttachment attachment : attachments) {
                    if (attachment.getItemId() != null) {
                        if (!inventoryService.hasItem(senderName, attachment.getItemId(), attachment.getQuantity())) {
                            return new MailResult(false, "物品不足: " + attachment.getItemName());
                        }
                    }
                }
            }
            
            // 创建带附件的邮件
            Mail mail = new Mail(senderName, receiverName, subject, content);
            
            // 添加附件
            if (attachments != null) {
                for (MailAttachment attachment : attachments) {
                    attachment.setMailId(mail.getMailId());
                    mail.addAttachment(attachment);
                }
            }
            
            // 添加金币附件
            if (goldAmount > 0) {
                mail.addGoldAttachment(goldAmount);
            }
            
            // 扣除发送者的物品和金币
            if (goldAmount > 0) {
                playerService.deductGold(senderName, goldAmount);
            }
            
            if (attachments != null) {
                for (MailAttachment attachment : attachments) {
                    if (attachment.getItemId() != null) {
                        inventoryService.removeItem(senderName, attachment.getItemId(), attachment.getQuantity());
                    }
                }
            }
            
            // 发送邮件
            return sendMail(mail);
            
        } catch (Exception e) {
            log.error("发送带附件邮件失败: from={}, to={}, subject={}", senderName, receiverName, subject, e);
            return new MailResult(false, "发送邮件失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送系统邮件
     * 
     * @param receiverName 接收者名称
     * @param subject 邮件主题
     * @param content 邮件内容
     * @param type 邮件类型
     * @return 发送结果
     */
    @Transactional
    public MailResult sendSystemMail(String receiverName, String subject, String content, Mail.MailType type) {
        try {
            // 检查接收者是否存在
            Player receiver = playerService.getPlayerByName(receiverName);
            if (receiver == null) {
                return new MailResult(false, "接收者不存在");
            }
            
            // 创建系统邮件
            Mail mail = Mail.createSystemMail(receiverName, subject, content, type);
            
            // 发送邮件
            return sendMail(mail);
            
        } catch (Exception e) {
            log.error("发送系统邮件失败: to={}, subject={}", receiverName, subject, e);
            return new MailResult(false, "发送系统邮件失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送系统奖励邮件
     * 
     * @param receiverName 接收者名称
     * @param subject 邮件主题
     * @param content 邮件内容
     * @param type 邮件类型
     * @param attachments 附件列表
     * @param goldAmount 金币数量
     * @return 发送结果
     */
    @Transactional
    public MailResult sendSystemRewardMail(String receiverName, String subject, String content, 
                                         Mail.MailType type, List<MailAttachment> attachments, int goldAmount) {
        try {
            // 检查接收者是否存在
            Player receiver = playerService.getPlayerByName(receiverName);
            if (receiver == null) {
                return new MailResult(false, "接收者不存在");
            }
            
            // 创建系统奖励邮件
            Mail mail = Mail.createSystemMailWithAttachments(receiverName, subject, content, type, attachments, goldAmount);
            
            // 发送邮件
            return sendMail(mail);
            
        } catch (Exception e) {
            log.error("发送系统奖励邮件失败: to={}, subject={}", receiverName, subject, e);
            return new MailResult(false, "发送系统奖励邮件失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取玩家邮箱
     * 
     * @param playerName 玩家名称
     * @return 邮箱
     */
    public MailBox getPlayerMailBox(String playerName) {
        return playerMailBoxes.computeIfAbsent(playerName, name -> {
            MailBox mailBox = new MailBox(name);
            // 这里可以从数据库加载邮箱数据
            loadMailBoxFromDatabase(mailBox);
            return mailBox;
        });
    }
    
    /**
     * 获取收件箱邮件
     * 
     * @param playerName 玩家名称
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 邮件列表
     */
    public List<Mail> getInboxMails(String playerName, int offset, int limit) {
        MailBox mailBox = getPlayerMailBox(playerName);
        return mailBox.getInboxMails(offset, limit);
    }
    
    /**
     * 获取发件箱邮件
     * 
     * @param playerName 玩家名称
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 邮件列表
     */
    public List<Mail> getSentMails(String playerName, int offset, int limit) {
        MailBox mailBox = getPlayerMailBox(playerName);
        return mailBox.getSentMails(offset, limit);
    }
    
    /**
     * 获取未读邮件
     * 
     * @param playerName 玩家名称
     * @return 未读邮件列表
     */
    public List<Mail> getUnreadMails(String playerName) {
        MailBox mailBox = getPlayerMailBox(playerName);
        return mailBox.getUnreadMails();
    }
    
    /**
     * 获取有附件的邮件
     * 
     * @param playerName 玩家名称
     * @return 有附件的邮件列表
     */
    public List<Mail> getMailsWithAttachments(String playerName) {
        MailBox mailBox = getPlayerMailBox(playerName);
        return mailBox.getMailsWithAttachments();
    }
    
    /**
     * 标记邮件为已读
     * 
     * @param playerName 玩家名称
     * @param mailId 邮件ID
     * @return 操作结果
     */
    @Transactional
    public MailResult markMailAsRead(String playerName, String mailId) {
        try {
            MailBox mailBox = getPlayerMailBox(playerName);
            
            if (mailBox.markAsRead(mailId)) {
                // 保存到数据库
                saveMailBoxToDatabase(mailBox);
                return new MailResult(true, "标记已读成功");
            } else {
                return new MailResult(false, "标记已读失败");
            }
            
        } catch (Exception e) {
            log.error("标记邮件已读失败: player={}, mailId={}", playerName, mailId, e);
            return new MailResult(false, "标记已读失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除邮件
     * 
     * @param playerName 玩家名称
     * @param mailId 邮件ID
     * @return 操作结果
     */
    @Transactional
    public MailResult deleteMail(String playerName, String mailId) {
        try {
            MailBox mailBox = getPlayerMailBox(playerName);
            
            if (mailBox.deleteMail(mailId)) {
                // 保存到数据库
                saveMailBoxToDatabase(mailBox);
                return new MailResult(true, "删除邮件成功");
            } else {
                return new MailResult(false, "删除邮件失败");
            }
            
        } catch (Exception e) {
            log.error("删除邮件失败: player={}, mailId={}", playerName, mailId, e);
            return new MailResult(false, "删除邮件失败: " + e.getMessage());
        }
    }
    
    /**
     * 领取邮件附件
     * 
     * @param playerName 玩家名称
     * @param mailId 邮件ID
     * @return 操作结果
     */
    @Transactional
    public MailResult claimAttachments(String playerName, String mailId) {
        try {
            MailBox mailBox = getPlayerMailBox(playerName);
            List<MailAttachment> attachments = mailBox.claimAttachments(mailId);
            
            if (attachments.isEmpty()) {
                return new MailResult(false, "没有可领取的附件");
            }
            
            // 检查背包空间
            int requiredSlots = attachments.size();
            if (!inventoryService.hasSpace(playerName, requiredSlots)) {
                return new MailResult(false, "背包空间不足");
            }
            
            // 发放附件
            for (MailAttachment attachment : attachments) {
                if (attachment.getType() == MailAttachment.AttachmentType.GOLD) {
                    // 发放金币
                    playerService.addGold(playerName, attachment.getQuantity());
                } else if (attachment.getType() == MailAttachment.AttachmentType.EXPERIENCE) {
                    // 发放经验
                    playerService.addExperience(playerName, attachment.getQuantity());
                } else if (attachment.getItemId() != null) {
                    // 发放物品
                    Item item = itemService.getItemById(attachment.getItemId());
                    if (item != null) {
                        inventoryService.addItem(playerName, item, attachment.getQuantity());
                    }
                }
                
                // 标记附件为已领取
                attachment.claim();
            }
            
            // 保存到数据库
            saveMailBoxToDatabase(mailBox);
            
            return new MailResult(true, "领取附件成功");
            
        } catch (Exception e) {
            log.error("领取邮件附件失败: player={}, mailId={}", playerName, mailId, e);
            return new MailResult(false, "领取附件失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索邮件
     * 
     * @param playerName 玩家名称
     * @param keyword 关键词
     * @return 邮件列表
     */
    public List<Mail> searchMails(String playerName, String keyword) {
        MailBox mailBox = getPlayerMailBox(playerName);
        return mailBox.searchMails(keyword);
    }
    
    /**
     * 获取邮箱统计信息
     * 
     * @param playerName 玩家名称
     * @return 邮箱统计信息
     */
    public MailBox.MailBoxStats getMailBoxStats(String playerName) {
        MailBox mailBox = getPlayerMailBox(playerName);
        return mailBox.getStats();
    }
    
    /**
     * 清理过期邮件
     * 
     * @param playerName 玩家名称
     */
    @Transactional
    public void cleanExpiredMails(String playerName) {
        try {
            MailBox mailBox = getPlayerMailBox(playerName);
            mailBox.cleanExpiredMails();
            
            // 保存到数据库
            saveMailBoxToDatabase(mailBox);
            
            log.info("清理过期邮件完成: player={}", playerName);
            
        } catch (Exception e) {
            log.error("清理过期邮件失败: player={}", playerName, e);
        }
    }
    
    /**
     * 全服邮件清理任务
     */
    @Transactional
    public void globalMailCleanup() {
        try {
            log.info("开始全服邮件清理任务");
            
            for (String playerName : playerMailBoxes.keySet()) {
                cleanExpiredMails(playerName);
            }
            
            log.info("全服邮件清理任务完成");
            
        } catch (Exception e) {
            log.error("全服邮件清理任务失败", e);
        }
    }
    
    /**
     * 发送邮件的内部方法
     * 
     * @param mail 邮件
     * @return 发送结果
     */
    private MailResult sendMail(Mail mail) {
        try {
            // 获取发送者和接收者邮箱
            MailBox senderMailBox = getPlayerMailBox(mail.getSenderName());
            MailBox receiverMailBox = getPlayerMailBox(mail.getReceiverName());
            
            // 发送邮件
            if (!senderMailBox.sendMail(mail)) {
                return new MailResult(false, "发送邮件失败");
            }
            
            // 接收邮件
            if (!receiverMailBox.receiveMail(mail)) {
                return new MailResult(false, "接收邮件失败");
            }
            
            // 记录发送历史
            recordSendHistory(mail.getSenderName());
            
            // 保存到数据库
            saveMailBoxToDatabase(senderMailBox);
            saveMailBoxToDatabase(receiverMailBox);
            
            // 发送通知
            if (playerService.isPlayerOnline(mail.getReceiverName())) {
                notificationService.sendMailNotification(mail.getReceiverName(), mail.getSubject());
            }
            
            log.info("邮件发送成功: from={}, to={}, subject={}", 
                    mail.getSenderName(), mail.getReceiverName(), mail.getSubject());
            
            return new MailResult(true, "邮件发送成功");
            
        } catch (Exception e) {
            log.error("发送邮件失败: mail={}", mail, e);
            return new MailResult(false, "发送邮件失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查发送冷却
     * 
     * @param playerName 玩家名称
     * @return 是否可以发送
     */
    private boolean checkSendCooldown(String playerName) {
        // 这里简化实现，实际应该记录最后发送时间
        return true;
    }
    
    /**
     * 检查日发送限制
     * 
     * @param playerName 玩家名称
     * @return 是否可以发送
     */
    private boolean checkDailySendLimit(String playerName) {
        List<String> todaySends = sendHistory.get(playerName + "_" + LocalDateTime.now().toLocalDate().toString());
        return todaySends == null || todaySends.size() < DAILY_SEND_LIMIT;
    }
    
    /**
     * 记录发送历史
     * 
     * @param playerName 玩家名称
     */
    private void recordSendHistory(String playerName) {
        String key = playerName + "_" + LocalDateTime.now().toLocalDate().toString();
        sendHistory.computeIfAbsent(key, k -> new ArrayList<>()).add(LocalDateTime.now().toString());
    }
    
    /**
     * 从数据库加载邮箱数据
     * 
     * @param mailBox 邮箱
     */
    private void loadMailBoxFromDatabase(MailBox mailBox) {
        try {
            String playerName = mailBox.getOwnerName();
            
            // 从数据库加载邮件
            List<MailEntity> mailEntities = mailRepository.findByReceiverAndDeletedFalse(playerName);
            
            for (MailEntity mailEntity : mailEntities) {
                // 转换为Mail对象
                Mail mail = mailEntity.toMail();
                
                // 根据邮件类型添加到对应的邮箱
                if (mail.getSender().equals(playerName)) {
                    mailBox.getSentMails().add(mail);
                } else {
                    mailBox.getInboxMails().add(mail);
                }
            }
            
            // 加载发送的邮件
            List<MailEntity> sentMailEntities = mailRepository.findBySenderAndDeletedFalse(playerName);
            for (MailEntity mailEntity : sentMailEntities) {
                if (!mailEntity.getReceiver().equals(playerName)) {
                    Mail mail = mailEntity.toMail();
                    mailBox.getSentMails().add(mail);
                }
            }
            
            // 更新邮箱状态
            mailBox.updateStats();
            
            log.debug("从数据库加载邮箱数据成功: player={}, inboxCount={}, sentCount={}", 
                    playerName, mailBox.getInboxMails().size(), mailBox.getSentMails().size());
            
        } catch (Exception e) {
            log.error("从数据库加载邮箱数据失败: player={}", mailBox.getOwnerName(), e);
        }
    }
    
    /**
     * 保存邮箱数据到数据库
     * 
     * @param mailBox 邮箱
     */
    private void saveMailBoxToDatabase(MailBox mailBox) {
        try {
            String playerName = mailBox.getOwnerName();
            
            // 保存收件箱邮件
            for (Mail mail : mailBox.getInboxMails()) {
                saveMailToDatabase(mail);
            }
            
            // 保存发件箱邮件
            for (Mail mail : mailBox.getSentMails()) {
                saveMailToDatabase(mail);
            }
            
            log.debug("保存邮箱数据到数据库成功: player={}", playerName);
            
        } catch (Exception e) {
            log.error("保存邮箱数据到数据库失败: player={}", mailBox.getOwnerName(), e);
        }
    }
    
    /**
     * 保存单个邮件到数据库
     * 
     * @param mail 邮件
     */
    private void saveMailToDatabase(Mail mail) {
        try {
            // 检查邮件是否已存在
            Optional<MailEntity> existingEntity = mailRepository.findByMailId(mail.getId());
            
            MailEntity mailEntity;
            if (existingEntity.isPresent()) {
                // 更新现有邮件
                mailEntity = existingEntity.get();
                mailEntity.setStatus(mail.getStatus());
                mailEntity.setReadTime(MailEntity.convertToLocalDateTime(mail.getReadTime()));
                mailEntity.setReplyCount(mail.getReplyCount());
                mailEntity.setAttachmentRetrieved(mail.isAttachmentRetrieved());
                
                // 更新标签
                if (mail.getTags() != null && !mail.getTags().isEmpty()) {
                    mailEntity.setTags(String.join(",", mail.getTags()));
                }
                
            } else {
                // 创建新邮件
                mailEntity = MailEntity.fromMail(mail);
            }
            
            // 保存邮件
            mailEntity = mailRepository.save(mailEntity);
            
            // 保存附件
            if (mail.getAttachments() != null && !mail.getAttachments().isEmpty()) {
                for (MailAttachment attachment : mail.getAttachments()) {
                    saveMailAttachmentToDatabase(attachment, mailEntity);
                }
            }
            
            log.debug("保存邮件到数据库成功: mailId={}", mail.getId());
            
        } catch (Exception e) {
            log.error("保存邮件到数据库失败: mailId={}", mail.getId(), e);
        }
    }
    
    /**
     * 保存邮件附件到数据库
     * 
     * @param attachment 附件
     * @param mailEntity 邮件实体
     */
    private void saveMailAttachmentToDatabase(MailAttachment attachment, MailEntity mailEntity) {
        try {
            // 检查附件是否已存在
            Optional<MailAttachmentEntity> existingEntity = mailAttachmentRepository.findByAttachmentId(attachment.getId());
            
            MailAttachmentEntity attachmentEntity;
            if (existingEntity.isPresent()) {
                // 更新现有附件
                attachmentEntity = existingEntity.get();
                attachmentEntity.setStatus(attachment.getStatus());
                attachmentEntity.setExpireTime(MailAttachmentEntity.convertToLocalDateTime(attachment.getExpireTime()));
            } else {
                // 创建新附件
                attachmentEntity = MailAttachmentEntity.fromMailAttachment(attachment, mailEntity);
            }
            
            // 保存附件
            mailAttachmentRepository.save(attachmentEntity);
            
            log.debug("保存邮件附件到数据库成功: attachmentId={}", attachment.getId());
            
        } catch (Exception e) {
            log.error("保存邮件附件到数据库失败: attachmentId={}", attachment.getId(), e);
        }
    }
    
    /**
     * 初始化玩家邮箱（从数据库加载）
     * 
     * @param playerName 玩家名称
     * @return 邮箱对象
     */
    private MailBox initializePlayerMailBox(String playerName) {
        MailBox mailBox = new MailBox(playerName);
        
        // 从数据库加载邮箱数据
        loadMailBoxFromDatabase(mailBox);
        
        return mailBox;
    }
    
    /**
     * 批量删除过期邮件
     */
    @Transactional
    public void batchDeleteExpiredMails() {
        try {
            LocalDateTime currentTime = LocalDateTime.now();
            
            // 删除过期邮件
            List<MailEntity> expiredMails = mailRepository.findExpiredMails(currentTime);
            if (!expiredMails.isEmpty()) {
                List<String> mailIds = new ArrayList<>();
                for (MailEntity mailEntity : expiredMails) {
                    mailIds.add(mailEntity.getMailId());
                }
                
                // 批量逻辑删除邮件和附件
                mailRepository.deleteByMailIds(mailIds);
                for (String mailId : mailIds) {
                    mailAttachmentRepository.deleteByMailId(mailId);
                }
                
                log.info("批量删除过期邮件完成: count={}", expiredMails.size());
            }
            
            // 删除过期附件
            List<MailAttachmentEntity> expiredAttachments = mailAttachmentRepository.findExpiredAttachments(currentTime);
            if (!expiredAttachments.isEmpty()) {
                List<String> attachmentIds = new ArrayList<>();
                for (MailAttachmentEntity attachmentEntity : expiredAttachments) {
                    attachmentIds.add(attachmentEntity.getAttachmentId());
                }
                
                mailAttachmentRepository.deleteByAttachmentIds(attachmentIds);
                
                log.info("批量删除过期附件完成: count={}", expiredAttachments.size());
            }
            
        } catch (Exception e) {
            log.error("批量删除过期邮件失败", e);
        }
    }
    
    /**
     * 邮件操作结果类
     */
    public static class MailResult {
        private final boolean success;
        private final String message;
        
        public MailResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
} 