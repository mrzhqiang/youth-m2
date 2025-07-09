package com.mir2.repository;

import com.mir2.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 * 
 * <p>提供用户相关的数据库操作方法。
 * 对应原M2Engine中的用户数据访问。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户实体
     */
    Optional<UserEntity> findByUsername(String username);
    
    /**
     * 根据邮箱查询用户
     * 
     * @param email 邮箱
     * @return 用户实体
     */
    Optional<UserEntity> findByEmail(String email);
    
    /**
     * 根据手机号查询用户
     * 
     * @param phone 手机号
     * @return 用户实体
     */
    Optional<UserEntity> findByPhone(String phone);
    
    /**
     * 根据用户名和密码查询用户
     * 
     * @param username 用户名
     * @param password 密码
     * @return 用户实体
     */
    Optional<UserEntity> findByUsernameAndPassword(String username, String password);
    
    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 检查手机号是否存在
     * 
     * @param phone 手机号
     * @return 是否存在
     */
    boolean existsByPhone(String phone);
    
    /**
     * 根据状态查询用户列表
     * 
     * @param status 状态
     * @return 用户列表
     */
    List<UserEntity> findByStatus(Integer status);
    
    /**
     * 根据用户类型查询用户列表
     * 
     * @param userType 用户类型
     * @return 用户列表
     */
    List<UserEntity> findByUserType(Integer userType);
    
    /**
     * 查询指定时间范围内注册的用户
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户列表
     */
    List<UserEntity> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查询指定时间范围内最后登录的用户
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户列表
     */
    List<UserEntity> findByLastLoginTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查询充值总额大于指定金额的用户
     * 
     * @param amount 金额
     * @return 用户列表
     */
    List<UserEntity> findByTotalRechargeGreaterThan(Long amount);
    
    /**
     * 查询在线时长大于指定时长的用户
     * 
     * @param minutes 在线时长（分钟）
     * @return 用户列表
     */
    List<UserEntity> findByOnlineTimeGreaterThan(Integer minutes);
    
    /**
     * 根据推荐人查询用户
     * 
     * @param referrer 推荐人
     * @return 用户列表
     */
    List<UserEntity> findByReferrer(String referrer);
    
    /**
     * 统计用户总数
     * 
     * @return 用户总数
     */
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.deleted = false")
    long countActiveUsers();
    
    /**
     * 统计指定状态的用户数量
     * 
     * @param status 状态
     * @return 用户数量
     */
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.status = :status AND u.deleted = false")
    long countByStatus(@Param("status") Integer status);
    
    /**
     * 统计今日新增用户数
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 新增用户数
     */
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.createTime BETWEEN :startTime AND :endTime")
    long countNewUsersToday(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计今日活跃用户数
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 活跃用户数
     */
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.lastLoginTime BETWEEN :startTime AND :endTime")
    long countActiveUsersToday(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询充值排行榜
     * 
     * @param limit 限制数量
     * @return 用户列表
     */
    @Query("SELECT u FROM UserEntity u WHERE u.deleted = false ORDER BY u.totalRecharge DESC")
    List<UserEntity> findTopRechargeUsers(@Param("limit") int limit);
    
    /**
     * 查询在线时长排行榜
     * 
     * @param limit 限制数量
     * @return 用户列表
     */
    @Query("SELECT u FROM UserEntity u WHERE u.deleted = false ORDER BY u.onlineTime DESC")
    List<UserEntity> findTopOnlineUsers(@Param("limit") int limit);
    
    /**
     * 更新用户最后登录信息
     * 
     * @param userId 用户ID
     * @param loginTime 登录时间
     * @param loginIp 登录IP
     * @param loginCount 登录次数
     */
    @Query("UPDATE UserEntity u SET u.lastLoginTime = :loginTime, u.lastLoginIp = :loginIp, u.loginCount = :loginCount WHERE u.id = :userId")
    void updateLastLoginInfo(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime, 
                           @Param("loginIp") String loginIp, @Param("loginCount") Integer loginCount);
    
    /**
     * 更新用户在线时长
     * 
     * @param userId 用户ID
     * @param onlineTime 在线时长
     */
    @Query("UPDATE UserEntity u SET u.onlineTime = :onlineTime WHERE u.id = :userId")
    void updateOnlineTime(@Param("userId") Long userId, @Param("onlineTime") Integer onlineTime);
    
    /**
     * 更新用户点数余额
     * 
     * @param userId 用户ID
     * @param pointBalance 点数余额
     */
    @Query("UPDATE UserEntity u SET u.pointBalance = :pointBalance WHERE u.id = :userId")
    void updatePointBalance(@Param("userId") Long userId, @Param("pointBalance") Long pointBalance);
    
    /**
     * 更新用户状态
     * 
     * @param userId 用户ID
     * @param status 状态
     */
    @Query("UPDATE UserEntity u SET u.status = :status WHERE u.id = :userId")
    void updateStatus(@Param("userId") Long userId, @Param("status") Integer status);
    
    /**
     * 删除用户（逻辑删除）
     * 
     * @param userId 用户ID
     */
    @Query("UPDATE UserEntity u SET u.deleted = true WHERE u.id = :userId")
    void deleteUser(@Param("userId") Long userId);
    
    /**
     * 根据用户名模糊查询
     * 
     * @param username 用户名
     * @return 用户列表
     */
    List<UserEntity> findByUsernameContaining(String username);
    
    /**
     * 根据真实姓名模糊查询
     * 
     * @param realName 真实姓名
     * @return 用户列表
     */
    List<UserEntity> findByRealNameContaining(String realName);
    
    /**
     * 查询指定IP注册的用户
     * 
     * @param ip IP地址
     * @return 用户列表
     */
    List<UserEntity> findByRegisterIp(String ip);
    
    /**
     * 查询指定IP最后登录的用户
     * 
     * @param ip IP地址
     * @return 用户列表
     */
    List<UserEntity> findByLastLoginIp(String ip);
} 