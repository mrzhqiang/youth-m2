package com.mir2.repository;

import com.mir2.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 背包数据访问接口
 * 
 * <p>提供背包相关的数据库操作方法。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long>, JpaSpecificationExecutor<InventoryEntity> {
    
    /**
     * 根据玩家名称查询背包
     * 
     * @param playerName 玩家名称
     * @return 背包实体
     */
    Optional<InventoryEntity> findByPlayerNameAndDeletedFalse(String playerName);
    
    /**
     * 根据玩家名称查询背包（包含已删除）
     * 
     * @param playerName 玩家名称
     * @return 背包实体
     */
    Optional<InventoryEntity> findByPlayerName(String playerName);
    
    /**
     * 根据容量查询背包列表
     * 
     * @param capacity 容量
     * @return 背包列表
     */
    List<InventoryEntity> findByCapacityAndDeletedFalse(Integer capacity);
    
    /**
     * 根据扩展等级查询背包列表
     * 
     * @param expansionLevel 扩展等级
     * @return 背包列表
     */
    List<InventoryEntity> findByExpansionLevelAndDeletedFalse(Integer expansionLevel);
    
    /**
     * 查询锁定的背包
     * 
     * @return 锁定背包列表
     */
    List<InventoryEntity> findByLockedTrueAndDeletedFalse();
    
    /**
     * 查询未锁定的背包
     * 
     * @return 未锁定背包列表
     */
    List<InventoryEntity> findByLockedFalseAndDeletedFalse();
    
    /**
     * 根据时间范围查询背包
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 背包列表
     */
    List<InventoryEntity> findByCreatedTimeBetweenAndDeletedFalse(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查询最后清理时间在指定时间之前的背包
     * 
     * @param time 时间
     * @return 背包列表
     */
    List<InventoryEntity> findByLastCleanupTimeBeforeAndDeletedFalse(LocalDateTime time);
    
    /**
     * 查询容量大于指定值的背包
     * 
     * @param capacity 容量
     * @return 背包列表
     */
    List<InventoryEntity> findByCapacityGreaterThanAndDeletedFalse(Integer capacity);
    
    /**
     * 查询容量小于指定值的背包
     * 
     * @param capacity 容量
     * @return 背包列表
     */
    List<InventoryEntity> findByCapacityLessThanAndDeletedFalse(Integer capacity);
    
    /**
     * 查询扩展等级大于指定值的背包
     * 
     * @param expansionLevel 扩展等级
     * @return 背包列表
     */
    List<InventoryEntity> findByExpansionLevelGreaterThanAndDeletedFalse(Integer expansionLevel);
    
    /**
     * 统计背包总数
     * 
     * @return 背包总数
     */
    @Query("SELECT COUNT(i) FROM InventoryEntity i WHERE i.deleted = false")
    long countActiveInventories();
    
    /**
     * 统计锁定的背包数量
     * 
     * @return 锁定背包数量
     */
    @Query("SELECT COUNT(i) FROM InventoryEntity i WHERE i.locked = true AND i.deleted = false")
    long countLockedInventories();
    
    /**
     * 统计容量大于指定值的背包数量
     * 
     * @param capacity 容量
     * @return 背包数量
     */
    @Query("SELECT COUNT(i) FROM InventoryEntity i WHERE i.capacity > :capacity AND i.deleted = false")
    long countInventoriesWithCapacityGreaterThan(@Param("capacity") Integer capacity);
    
    /**
     * 统计扩展等级大于指定值的背包数量
     * 
     * @param expansionLevel 扩展等级
     * @return 背包数量
     */
    @Query("SELECT COUNT(i) FROM InventoryEntity i WHERE i.expansionLevel > :expansionLevel AND i.deleted = false")
    long countInventoriesWithExpansionLevelGreaterThan(@Param("expansionLevel") Integer expansionLevel);
    
    /**
     * 查询平均容量
     * 
     * @return 平均容量
     */
    @Query("SELECT AVG(i.capacity) FROM InventoryEntity i WHERE i.deleted = false")
    Double getAverageCapacity();
    
    /**
     * 查询最大容量
     * 
     * @return 最大容量
     */
    @Query("SELECT MAX(i.capacity) FROM InventoryEntity i WHERE i.deleted = false")
    Integer getMaxCapacity();
    
    /**
     * 查询最小容量
     * 
     * @return 最小容量
     */
    @Query("SELECT MIN(i.capacity) FROM InventoryEntity i WHERE i.deleted = false")
    Integer getMinCapacity();
    
    /**
     * 根据玩家名称删除背包（逻辑删除）
     * 
     * @param playerName 玩家名称
     */
    @Query("UPDATE InventoryEntity i SET i.deleted = true WHERE i.playerName = :playerName")
    void deleteByPlayerName(@Param("playerName") String playerName);
    
    /**
     * 批量删除背包（逻辑删除）
     * 
     * @param playerNames 玩家名称列表
     */
    @Query("UPDATE InventoryEntity i SET i.deleted = true WHERE i.playerName IN :playerNames")
    void deleteByPlayerNames(@Param("playerNames") List<String> playerNames);
    
    /**
     * 更新背包容量
     * 
     * @param playerName 玩家名称
     * @param capacity 新容量
     */
    @Query("UPDATE InventoryEntity i SET i.capacity = :capacity WHERE i.playerName = :playerName AND i.deleted = false")
    void updateCapacity(@Param("playerName") String playerName, @Param("capacity") Integer capacity);
    
    /**
     * 更新背包扩展等级
     * 
     * @param playerName 玩家名称
     * @param expansionLevel 扩展等级
     */
    @Query("UPDATE InventoryEntity i SET i.expansionLevel = :expansionLevel WHERE i.playerName = :playerName AND i.deleted = false")
    void updateExpansionLevel(@Param("playerName") String playerName, @Param("expansionLevel") Integer expansionLevel);
    
    /**
     * 更新背包锁定状态
     * 
     * @param playerName 玩家名称
     * @param locked 锁定状态
     */
    @Query("UPDATE InventoryEntity i SET i.locked = :locked WHERE i.playerName = :playerName AND i.deleted = false")
    void updateLocked(@Param("playerName") String playerName, @Param("locked") Boolean locked);
    
    /**
     * 更新背包槽位数据
     * 
     * @param playerName 玩家名称
     * @param slotsData 槽位数据
     */
    @Query("UPDATE InventoryEntity i SET i.slotsData = :slotsData WHERE i.playerName = :playerName AND i.deleted = false")
    void updateSlotsData(@Param("playerName") String playerName, @Param("slotsData") String slotsData);
    
    /**
     * 更新最后清理时间
     * 
     * @param playerName 玩家名称
     * @param lastCleanupTime 最后清理时间
     */
    @Query("UPDATE InventoryEntity i SET i.lastCleanupTime = :lastCleanupTime WHERE i.playerName = :playerName AND i.deleted = false")
    void updateLastCleanupTime(@Param("playerName") String playerName, @Param("lastCleanupTime") LocalDateTime lastCleanupTime);
    
    /**
     * 检查玩家是否有背包
     * 
     * @param playerName 玩家名称
     * @return 是否存在
     */
    boolean existsByPlayerNameAndDeletedFalse(String playerName);
    
    /**
     * 检查玩家是否有背包（包含已删除）
     * 
     * @param playerName 玩家名称
     * @return 是否存在
     */
    boolean existsByPlayerName(String playerName);
    
    /**
     * 查询需要清理的背包
     * 
     * @param thresholdTime 阈值时间
     * @return 需要清理的背包列表
     */
    @Query("SELECT i FROM InventoryEntity i WHERE i.lastCleanupTime < :thresholdTime AND i.deleted = false")
    List<InventoryEntity> findInventoriesNeedingCleanup(@Param("thresholdTime") LocalDateTime thresholdTime);
    
    /**
     * 查询最近创建的背包
     * 
     * @param limit 限制数量
     * @return 最近创建的背包列表
     */
    @Query("SELECT i FROM InventoryEntity i WHERE i.deleted = false ORDER BY i.createdTime DESC")
    List<InventoryEntity> findRecentInventories(@Param("limit") int limit);
    
    /**
     * 查询最近更新的背包
     * 
     * @param limit 限制数量
     * @return 最近更新的背包列表
     */
    @Query("SELECT i FROM InventoryEntity i WHERE i.deleted = false ORDER BY i.updatedTime DESC")
    List<InventoryEntity> findRecentlyUpdatedInventories(@Param("limit") int limit);
    
    /**
     * 分页查询背包
     * 
     * @param pageable 分页参数
     * @return 分页背包列表
     */
    @Query("SELECT i FROM InventoryEntity i WHERE i.deleted = false ORDER BY i.createdTime DESC")
    org.springframework.data.domain.Page<InventoryEntity> findAllWithPagination(org.springframework.data.domain.Pageable pageable);
} 