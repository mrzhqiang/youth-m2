package com.mir2.repository;

import com.mir2.entity.MailEntity;
import com.mir2.entity.Mail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 邮件数据访问接口
 * 
 * <p>提供邮件相关的数据库操作方法。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface MailRepository extends JpaRepository<MailEntity, Long>, JpaSpecificationExecutor<MailEntity> {
    
    /**
     * 根据邮件ID查询邮件
     * 
     * @param mailId 邮件ID
     * @return 邮件实体
     */
    Optional<MailEntity> findByMailId(String mailId);
    
    /**
     * 根据接收者查询邮件列表
     * 
     * @param receiver 接收者
     * @return 邮件列表
     */
    List<MailEntity> findByReceiverAndDeletedFalse(String receiver);
    
    /**
     * 根据发送者查询邮件列表
     * 
     * @param sender 发送者
     * @return 邮件列表
     */
    List<MailEntity> findBySenderAndDeletedFalse(String sender);
    
    /**
     * 根据接收者和状态查询邮件列表
     * 
     * @param receiver 接收者
     * @param status 邮件状态
     * @return 邮件列表
     */
    List<MailEntity> findByReceiverAndStatusAndDeletedFalse(String receiver, Mail.MailStatus status);
    
    /**
     * 根据接收者和多个状态查询邮件列表
     * 
     * @param receiver 接收者
     * @param statuses 邮件状态列表
     * @return 邮件列表
     */
    List<MailEntity> findByReceiverAndStatusInAndDeletedFalse(String receiver, List<Mail.MailStatus> statuses);
    
    /**
     * 根据接收者和邮件类型查询邮件列表
     * 
     * @param receiver 接收者
     * @param type 邮件类型
     * @return 邮件列表
     */
    List<MailEntity> findByReceiverAndTypeAndDeletedFalse(String receiver, Mail.MailType type);
    
    /**
     * 根据接收者和优先级查询邮件列表
     * 
     * @param receiver 接收者
     * @param priority 优先级
     * @return 邮件列表
     */
    List<MailEntity> findByReceiverAndPriorityAndDeletedFalse(String receiver, Mail.MailPriority priority);
    
    /**
     * 查询已过期的邮件
     * 
     * @param currentTime 当前时间
     * @return 过期邮件列表
     */
    @Query("SELECT m FROM MailEntity m WHERE m.expireTime < :currentTime AND m.deleted = false")
    List<MailEntity> findExpiredMails(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 查询带附件的邮件
     * 
     * @param receiver 接收者
     * @return 带附件邮件列表
     */
    List<MailEntity> findByReceiverAndHasAttachmentTrueAndDeletedFalse(String receiver);
    
    /**
     * 查询未读邮件数量
     * 
     * @param receiver 接收者
     * @return 未读邮件数量
     */
    @Query("SELECT COUNT(m) FROM MailEntity m WHERE m.receiver = :receiver AND m.status = 'UNREAD' AND m.deleted = false")
    long countUnreadMails(@Param("receiver") String receiver);
    
    /**
     * 查询带附件未读邮件数量
     * 
     * @param receiver 接收者
     * @return 带附件未读邮件数量
     */
    @Query("SELECT COUNT(m) FROM MailEntity m WHERE m.receiver = :receiver AND m.status = 'UNREAD' AND m.hasAttachment = true AND m.deleted = false")
    long countUnreadMailsWithAttachment(@Param("receiver") String receiver);
    
    /**
     * 根据主题模糊查询邮件
     * 
     * @param receiver 接收者
     * @param subject 主题关键字
     * @return 邮件列表
     */
    List<MailEntity> findByReceiverAndSubjectContainingAndDeletedFalse(String receiver, String subject);
    
    /**
     * 根据发送者模糊查询邮件
     * 
     * @param receiver 接收者
     * @param sender 发送者关键字
     * @return 邮件列表
     */
    List<MailEntity> findByReceiverAndSenderContainingAndDeletedFalse(String receiver, String sender);
    
    /**
     * 根据内容模糊查询邮件
     * 
     * @param receiver 接收者
     * @param content 内容关键字
     * @return 邮件列表
     */
    List<MailEntity> findByReceiverAndContentContainingAndDeletedFalse(String receiver, String content);
    
    /**
     * 根据时间范围查询邮件
     * 
     * @param receiver 接收者
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 邮件列表
     */
    List<MailEntity> findByReceiverAndSendTimeBetweenAndDeletedFalse(String receiver, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查询系统邮件
     * 
     * @param receiver 接收者
     * @return 系统邮件列表
     */
    @Query("SELECT m FROM MailEntity m WHERE m.receiver = :receiver AND m.type = 'SYSTEM' AND m.deleted = false ORDER BY m.sendTime DESC")
    List<MailEntity> findSystemMails(@Param("receiver") String receiver);
    
    /**
     * 查询玩家邮件
     * 
     * @param receiver 接收者
     * @return 玩家邮件列表
     */
    @Query("SELECT m FROM MailEntity m WHERE m.receiver = :receiver AND m.type = 'PLAYER' AND m.deleted = false ORDER BY m.sendTime DESC")
    List<MailEntity> findPlayerMails(@Param("receiver") String receiver);
    
    /**
     * 查询奖励邮件
     * 
     * @param receiver 接收者
     * @return 奖励邮件列表
     */
    @Query("SELECT m FROM MailEntity m WHERE m.receiver = :receiver AND m.type = 'REWARD' AND m.deleted = false ORDER BY m.sendTime DESC")
    List<MailEntity> findRewardMails(@Param("receiver") String receiver);
    
    /**
     * 查询公告邮件
     * 
     * @param receiver 接收者
     * @return 公告邮件列表
     */
    @Query("SELECT m FROM MailEntity m WHERE m.receiver = :receiver AND m.type = 'ANNOUNCEMENT' AND m.deleted = false ORDER BY m.sendTime DESC")
    List<MailEntity> findAnnouncementMails(@Param("receiver") String receiver);
    
    /**
     * 查询需要自动删除的邮件
     * 
     * @param currentTime 当前时间
     * @return 需要自动删除的邮件列表
     */
    @Query("SELECT m FROM MailEntity m WHERE m.autoDelete = true AND m.readTime IS NOT NULL AND m.readTime < :currentTime AND m.deleted = false")
    List<MailEntity> findAutoDeleteMails(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 查询最近的邮件
     * 
     * @param receiver 接收者
     * @param limit 限制数量
     * @return 最近邮件列表
     */
    @Query("SELECT m FROM MailEntity m WHERE m.receiver = :receiver AND m.deleted = false ORDER BY m.sendTime DESC")
    List<MailEntity> findRecentMails(@Param("receiver") String receiver, @Param("limit") int limit);
    
    /**
     * 根据标签查询邮件
     * 
     * @param receiver 接收者
     * @param tag 标签
     * @return 邮件列表
     */
    @Query("SELECT m FROM MailEntity m WHERE m.receiver = :receiver AND m.tags LIKE %:tag% AND m.deleted = false")
    List<MailEntity> findByReceiverAndTag(@Param("receiver") String receiver, @Param("tag") String tag);
    
    /**
     * 批量删除邮件（逻辑删除）
     * 
     * @param mailIds 邮件ID列表
     */
    @Query("UPDATE MailEntity m SET m.deleted = true WHERE m.mailId IN :mailIds")
    void deleteByMailIds(@Param("mailIds") List<String> mailIds);
    
    /**
     * 批量更新邮件状态
     * 
     * @param mailIds 邮件ID列表
     * @param status 邮件状态
     */
    @Query("UPDATE MailEntity m SET m.status = :status WHERE m.mailId IN :mailIds")
    void updateStatusByMailIds(@Param("mailIds") List<String> mailIds, @Param("status") Mail.MailStatus status);
    
    /**
     * 清理过期邮件（逻辑删除）
     * 
     * @param expireTime 过期时间
     */
    @Query("UPDATE MailEntity m SET m.deleted = true WHERE m.expireTime < :expireTime")
    void deleteExpiredMails(@Param("expireTime") LocalDateTime expireTime);
    
    /**
     * 统计发送者发送的邮件数量
     * 
     * @param sender 发送者
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 邮件数量
     */
    @Query("SELECT COUNT(m) FROM MailEntity m WHERE m.sender = :sender AND m.sendTime BETWEEN :startTime AND :endTime AND m.deleted = false")
    long countMailsBySenderAndTimeRange(@Param("sender") String sender, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 检查邮件是否存在
     * 
     * @param mailId 邮件ID
     * @return 是否存在
     */
    boolean existsByMailIdAndDeletedFalse(String mailId);
    
    /**
     * 查询回复邮件
     * 
     * @param replyTo 回复目标
     * @return 回复邮件列表
     */
    List<MailEntity> findByReplyToAndDeletedFalseOrderBySendTimeDesc(String replyTo);
    
    /**
     * 分页查询邮件
     * 
     * @param receiver 接收者
     * @param pageable 分页参数
     * @return 分页邮件列表
     */
    @Query("SELECT m FROM MailEntity m WHERE m.receiver = :receiver AND m.deleted = false ORDER BY m.sendTime DESC")
    org.springframework.data.domain.Page<MailEntity> findByReceiverWithPagination(@Param("receiver") String receiver, 
                                                                                 org.springframework.data.domain.Pageable pageable);
} 