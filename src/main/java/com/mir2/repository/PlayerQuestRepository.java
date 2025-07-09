package com.mir2.repository;

import com.mir2.entity.PlayerQuestEntity;
import com.mir2.entity.Quest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 玩家任务数据访问接口
 * 
 * <p>提供玩家任务相关的数据库操作方法。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface PlayerQuestRepository extends JpaRepository<PlayerQuestEntity, Long>, JpaSpecificationExecutor<PlayerQuestEntity> {
    
    /**
     * 根据玩家任务ID查询任务
     * 
     * @param playerQuestId 玩家任务ID
     * @return 玩家任务实体
     */
    Optional<PlayerQuestEntity> findByPlayerQuestId(String playerQuestId);
    
    /**
     * 根据玩家名称查询任务列表
     * 
     * @param playerName 玩家名称
     * @return 任务列表
     */
    List<PlayerQuestEntity> findByPlayerNameAndDeletedFalse(String playerName);
    
    /**
     * 根据玩家名称和任务ID查询任务
     * 
     * @param playerName 玩家名称
     * @param questId 任务ID
     * @return 玩家任务实体
     */
    Optional<PlayerQuestEntity> findByPlayerNameAndQuestIdAndDeletedFalse(String playerName, Integer questId);
    
    /**
     * 根据玩家名称和任务状态查询任务列表
     * 
     * @param playerName 玩家名称
     * @param status 任务状态
     * @return 任务列表
     */
    List<PlayerQuestEntity> findByPlayerNameAndStatusAndDeletedFalse(String playerName, Quest.QuestStatus status);
    
    /**
     * 根据玩家名称查询进行中的任务
     * 
     * @param playerName 玩家名称
     * @return 任务列表
     */
    List<PlayerQuestEntity> findByPlayerNameAndStatusInAndDeletedFalse(String playerName, List<Quest.QuestStatus> statuses);
    
    /**
     * 根据任务ID查询所有玩家的任务
     * 
     * @param questId 任务ID
     * @return 任务列表
     */
    List<PlayerQuestEntity> findByQuestIdAndDeletedFalse(Integer questId);
    
    /**
     * 查询已过期的任务
     * 
     * @param currentTime 当前时间
     * @return 过期任务列表
     */
    @Query("SELECT pq FROM PlayerQuestEntity pq WHERE pq.expireTime < :currentTime AND pq.deleted = false")
    List<PlayerQuestEntity> findExpiredQuests(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 根据玩家名称查询可重复的已完成任务
     * 
     * @param playerName 玩家名称
     * @return 任务列表
     */
    @Query("SELECT pq FROM PlayerQuestEntity pq WHERE pq.playerName = :playerName AND pq.status = 'TURNED_IN' AND pq.repeatCount > 0 AND pq.deleted = false")
    List<PlayerQuestEntity> findRepeatableCompletedQuests(@Param("playerName") String playerName);
    
    /**
     * 根据玩家名称和NPC ID查询任务
     * 
     * @param playerName 玩家名称
     * @param npcId NPC ID
     * @return 任务列表
     */
    List<PlayerQuestEntity> findByPlayerNameAndSourceNpcIdAndDeletedFalse(String playerName, Integer npcId);
    
    /**
     * 根据时间范围查询任务
     * 
     * @param playerName 玩家名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 任务列表
     */
    List<PlayerQuestEntity> findByPlayerNameAndAcceptTimeBetweenAndDeletedFalse(String playerName, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计玩家完成的任务数量
     * 
     * @param playerName 玩家名称
     * @return 完成任务数量
     */
    @Query("SELECT COUNT(pq) FROM PlayerQuestEntity pq WHERE pq.playerName = :playerName AND pq.status = 'TURNED_IN' AND pq.deleted = false")
    long countCompletedQuests(@Param("playerName") String playerName);
    
    /**
     * 统计玩家当前进行中的任务数量
     * 
     * @param playerName 玩家名称
     * @return 进行中任务数量
     */
    @Query("SELECT COUNT(pq) FROM PlayerQuestEntity pq WHERE pq.playerName = :playerName AND pq.status IN ('ACCEPTED', 'IN_PROGRESS') AND pq.deleted = false")
    long countActiveQuests(@Param("playerName") String playerName);
    
    /**
     * 查询需要重置的日常任务
     * 
     * @param resetTime 重置时间
     * @return 任务列表
     */
    @Query("SELECT pq FROM PlayerQuestEntity pq WHERE pq.lastCompletedTime < :resetTime AND pq.status = 'TURNED_IN' AND pq.deleted = false")
    List<PlayerQuestEntity> findDailyQuestsToReset(@Param("resetTime") LocalDateTime resetTime);
    
    /**
     * 根据玩家名称删除任务（逻辑删除）
     * 
     * @param playerName 玩家名称
     */
    @Query("UPDATE PlayerQuestEntity pq SET pq.deleted = true WHERE pq.playerName = :playerName")
    void deleteByPlayerName(@Param("playerName") String playerName);
    
    /**
     * 根据任务ID删除所有玩家的任务（逻辑删除）
     * 
     * @param questId 任务ID
     */
    @Query("UPDATE PlayerQuestEntity pq SET pq.deleted = true WHERE pq.questId = :questId")
    void deleteByQuestId(@Param("questId") Integer questId);
    
    /**
     * 清理过期任务（逻辑删除）
     * 
     * @param expireTime 过期时间
     */
    @Query("UPDATE PlayerQuestEntity pq SET pq.deleted = true WHERE pq.expireTime < :expireTime")
    void deleteExpiredQuests(@Param("expireTime") LocalDateTime expireTime);
    
    /**
     * 批量保存任务
     * 
     * @param quests 任务列表
     * @return 保存的任务列表
     */
    @Override
    <S extends PlayerQuestEntity> List<S> saveAll(Iterable<S> quests);
    
    /**
     * 根据玩家名称和任务ID列表查询任务
     * 
     * @param playerName 玩家名称
     * @param questIds 任务ID列表
     * @return 任务列表
     */
    List<PlayerQuestEntity> findByPlayerNameAndQuestIdInAndDeletedFalse(String playerName, List<Integer> questIds);
    
    /**
     * 查询最近更新的任务
     * 
     * @param playerName 玩家名称
     * @param sinceTime 起始时间
     * @return 任务列表
     */
    List<PlayerQuestEntity> findByPlayerNameAndLastUpdateTimeAfterAndDeletedFalse(String playerName, LocalDateTime sinceTime);
    
    /**
     * 查询特定状态的任务数量
     * 
     * @param playerName 玩家名称
     * @param status 任务状态
     * @return 任务数量
     */
    long countByPlayerNameAndStatusAndDeletedFalse(String playerName, Quest.QuestStatus status);
    
    /**
     * 检查玩家是否有特定任务
     * 
     * @param playerName 玩家名称
     * @param questId 任务ID
     * @return 是否存在
     */
    boolean existsByPlayerNameAndQuestIdAndDeletedFalse(String playerName, Integer questId);
} 