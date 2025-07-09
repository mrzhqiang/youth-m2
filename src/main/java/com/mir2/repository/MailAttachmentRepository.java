package com.mir2.repository;

import com.mir2.entity.MailAttachmentEntity;
import com.mir2.entity.MailEntity;
import com.mir2.entity.MailAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 邮件附件数据访问接口
 * 
 * <p>提供邮件附件相关的数据库操作方法。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface MailAttachmentRepository extends JpaRepository<MailAttachmentEntity, Long>, JpaSpecificationExecutor<MailAttachmentEntity> {
    
    /**
     * 根据附件ID查询附件
     * 
     * @param attachmentId 附件ID
     * @return 附件实体
     */
    Optional<MailAttachmentEntity> findByAttachmentId(String attachmentId);
    
    /**
     * 根据邮件查询附件列表
     * 
     * @param mail 邮件实体
     * @return 附件列表
     */
    List<MailAttachmentEntity> findByMailAndDeletedFalse(MailEntity mail);
    
    /**
     * 根据邮件ID查询附件列表
     * 
     * @param mailId 邮件ID
     * @return 附件列表
     */
    @Query("SELECT a FROM MailAttachmentEntity a WHERE a.mail.mailId = :mailId AND a.deleted = false")
    List<MailAttachmentEntity> findByMailId(@Param("mailId") String mailId);
    
    /**
     * 根据邮件和附件类型查询附件列表
     * 
     * @param mail 邮件实体
     * @param type 附件类型
     * @return 附件列表
     */
    List<MailAttachmentEntity> findByMailAndTypeAndDeletedFalse(MailEntity mail, MailAttachment.AttachmentType type);
    
    /**
     * 根据邮件和附件状态查询附件列表
     * 
     * @param mail 邮件实体
     * @param status 附件状态
     * @return 附件列表
     */
    List<MailAttachmentEntity> findByMailAndStatusAndDeletedFalse(MailEntity mail, MailAttachment.AttachmentStatus status);
    
    /**
     * 根据邮件查询未领取的附件
     * 
     * @param mail 邮件实体
     * @return 未领取附件列表
     */
    @Query("SELECT a FROM MailAttachmentEntity a WHERE a.mail = :mail AND a.status = 'PENDING' AND a.deleted = false")
    List<MailAttachmentEntity> findPendingAttachments(@Param("mail") MailEntity mail);
    
    /**
     * 根据邮件查询已领取的附件
     * 
     * @param mail 邮件实体
     * @return 已领取附件列表
     */
    @Query("SELECT a FROM MailAttachmentEntity a WHERE a.mail = :mail AND a.status = 'RETRIEVED' AND a.deleted = false")
    List<MailAttachmentEntity> findRetrievedAttachments(@Param("mail") MailEntity mail);
    
    /**
     * 查询过期的附件
     * 
     * @param currentTime 当前时间
     * @return 过期附件列表
     */
    @Query("SELECT a FROM MailAttachmentEntity a WHERE a.expireTime < :currentTime AND a.deleted = false")
    List<MailAttachmentEntity> findExpiredAttachments(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 根据物品ID查询附件
     * 
     * @param itemId 物品ID
     * @return 附件列表
     */
    List<MailAttachmentEntity> findByItemIdAndDeletedFalse(Integer itemId);
    
    /**
     * 根据装备ID查询附件
     * 
     * @param equipmentId 装备ID
     * @return 附件列表
     */
    List<MailAttachmentEntity> findByEquipmentIdAndDeletedFalse(Integer equipmentId);
    
    /**
     * 根据技能ID查询附件
     * 
     * @param skillId 技能ID
     * @return 附件列表
     */
    List<MailAttachmentEntity> findBySkillIdAndDeletedFalse(Integer skillId);
    
    /**
     * 查询金币附件
     * 
     * @param mail 邮件实体
     * @return 金币附件列表
     */
    @Query("SELECT a FROM MailAttachmentEntity a WHERE a.mail = :mail AND a.type = 'GOLD' AND a.deleted = false")
    List<MailAttachmentEntity> findGoldAttachments(@Param("mail") MailEntity mail);
    
    /**
     * 查询经验附件
     * 
     * @param mail 邮件实体
     * @return 经验附件列表
     */
    @Query("SELECT a FROM MailAttachmentEntity a WHERE a.mail = :mail AND a.type = 'EXPERIENCE' AND a.deleted = false")
    List<MailAttachmentEntity> findExperienceAttachments(@Param("mail") MailEntity mail);
    
    /**
     * 查询物品附件
     * 
     * @param mail 邮件实体
     * @return 物品附件列表
     */
    @Query("SELECT a FROM MailAttachmentEntity a WHERE a.mail = :mail AND a.type = 'ITEM' AND a.deleted = false")
    List<MailAttachmentEntity> findItemAttachments(@Param("mail") MailEntity mail);
    
    /**
     * 查询装备附件
     * 
     * @param mail 邮件实体
     * @return 装备附件列表
     */
    @Query("SELECT a FROM MailAttachmentEntity a WHERE a.mail = :mail AND a.type = 'EQUIPMENT' AND a.deleted = false")
    List<MailAttachmentEntity> findEquipmentAttachments(@Param("mail") MailEntity mail);
    
    /**
     * 统计邮件的附件数量
     * 
     * @param mail 邮件实体
     * @return 附件数量
     */
    @Query("SELECT COUNT(a) FROM MailAttachmentEntity a WHERE a.mail = :mail AND a.deleted = false")
    long countAttachmentsByMail(@Param("mail") MailEntity mail);
    
    /**
     * 统计邮件的未领取附件数量
     * 
     * @param mail 邮件实体
     * @return 未领取附件数量
     */
    @Query("SELECT COUNT(a) FROM MailAttachmentEntity a WHERE a.mail = :mail AND a.status = 'PENDING' AND a.deleted = false")
    long countPendingAttachmentsByMail(@Param("mail") MailEntity mail);
    
    /**
     * 批量删除附件（逻辑删除）
     * 
     * @param attachmentIds 附件ID列表
     */
    @Query("UPDATE MailAttachmentEntity a SET a.deleted = true WHERE a.attachmentId IN :attachmentIds")
    void deleteByAttachmentIds(@Param("attachmentIds") List<String> attachmentIds);
    
    /**
     * 批量更新附件状态
     * 
     * @param attachmentIds 附件ID列表
     * @param status 附件状态
     */
    @Query("UPDATE MailAttachmentEntity a SET a.status = :status WHERE a.attachmentId IN :attachmentIds")
    void updateStatusByAttachmentIds(@Param("attachmentIds") List<String> attachmentIds, @Param("status") MailAttachment.AttachmentStatus status);
    
    /**
     * 根据邮件删除附件（逻辑删除）
     * 
     * @param mail 邮件实体
     */
    @Query("UPDATE MailAttachmentEntity a SET a.deleted = true WHERE a.mail = :mail")
    void deleteByMail(@Param("mail") MailEntity mail);
    
    /**
     * 根据邮件ID删除附件（逻辑删除）
     * 
     * @param mailId 邮件ID
     */
    @Query("UPDATE MailAttachmentEntity a SET a.deleted = true WHERE a.mail.mailId = :mailId")
    void deleteByMailId(@Param("mailId") String mailId);
    
    /**
     * 清理过期附件（逻辑删除）
     * 
     * @param expireTime 过期时间
     */
    @Query("UPDATE MailAttachmentEntity a SET a.deleted = true WHERE a.expireTime < :expireTime")
    void deleteExpiredAttachments(@Param("expireTime") LocalDateTime expireTime);
    
    /**
     * 检查附件是否存在
     * 
     * @param attachmentId 附件ID
     * @return 是否存在
     */
    boolean existsByAttachmentIdAndDeletedFalse(String attachmentId);
    
    /**
     * 根据邮件和附件类型检查是否存在
     * 
     * @param mail 邮件实体
     * @param type 附件类型
     * @return 是否存在
     */
    boolean existsByMailAndTypeAndDeletedFalse(MailEntity mail, MailAttachment.AttachmentType type);
    
    /**
     * 根据等级要求查询附件
     * 
     * @param mail 邮件实体
     * @param level 等级
     * @return 附件列表
     */
    @Query("SELECT a FROM MailAttachmentEntity a WHERE a.mail = :mail AND (a.minLevel IS NULL OR a.minLevel <= :level) AND (a.maxLevel IS NULL OR a.maxLevel >= :level) AND a.deleted = false")
    List<MailAttachmentEntity> findByMailAndLevel(@Param("mail") MailEntity mail, @Param("level") Integer level);
    
    /**
     * 根据职业要求查询附件
     * 
     * @param mail 邮件实体
     * @param job 职业
     * @return 附件列表
     */
    @Query("SELECT a FROM MailAttachmentEntity a WHERE a.mail = :mail AND (a.requiredJob IS NULL OR a.requiredJob = :job) AND a.deleted = false")
    List<MailAttachmentEntity> findByMailAndJob(@Param("mail") MailEntity mail, @Param("job") Integer job);
    
    /**
     * 根据名称模糊查询附件
     * 
     * @param mail 邮件实体
     * @param name 名称关键字
     * @return 附件列表
     */
    List<MailAttachmentEntity> findByMailAndNameContainingAndDeletedFalse(MailEntity mail, String name);
    
    /**
     * 根据描述模糊查询附件
     * 
     * @param mail 邮件实体
     * @param description 描述关键字
     * @return 附件列表
     */
    List<MailAttachmentEntity> findByMailAndDescriptionContainingAndDeletedFalse(MailEntity mail, String description);
} 