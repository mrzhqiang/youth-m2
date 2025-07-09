package com.mir2.repository;

import com.mir2.entity.CharacterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 角色数据访问接口
 * 
 * <p>提供角色相关的数据库操作方法。
 * 对应原M2Engine中的角色数据访问。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface CharacterRepository extends JpaRepository<CharacterEntity, Long>, JpaSpecificationExecutor<CharacterEntity> {
    
    /**
     * 根据角色名查询角色
     * 
     * @param name 角色名
     * @return 角色实体
     */
    Optional<CharacterEntity> findByName(String name);
    
    /**
     * 根据用户ID查询角色列表
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    List<CharacterEntity> findByUserId(Long userId);
    
    /**
     * 根据用户ID和角色名查询角色
     * 
     * @param userId 用户ID
     * @param name 角色名
     * @return 角色实体
     */
    Optional<CharacterEntity> findByUserIdAndName(Long userId, String name);
    
    /**
     * 检查角色名是否存在
     * 
     * @param name 角色名
     * @return 是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 检查用户是否有角色
     * 
     * @param userId 用户ID
     * @return 是否有角色
     */
    boolean existsByUserId(Long userId);
    
    /**
     * 根据职业查询角色列表
     * 
     * @param job 职业
     * @return 角色列表
     */
    List<CharacterEntity> findByJob(Integer job);
    
    /**
     * 根据性别查询角色列表
     * 
     * @param gender 性别
     * @return 角色列表
     */
    List<CharacterEntity> findByGender(Integer gender);
    
    /**
     * 根据等级范围查询角色列表
     * 
     * @param minLevel 最小等级
     * @param maxLevel 最大等级
     * @return 角色列表
     */
    List<CharacterEntity> findByLevelBetween(Integer minLevel, Integer maxLevel);
    
    /**
     * 根据当前地图查询角色列表
     * 
     * @param mapName 地图名
     * @return 角色列表
     */
    List<CharacterEntity> findByCurrentMap(String mapName);
    
    /**
     * 根据行会名查询角色列表
     * 
     * @param guildName 行会名
     * @return 角色列表
     */
    List<CharacterEntity> findByGuildName(String guildName);
    
    /**
     * 根据师傅名查询角色列表
     * 
     * @param masterName 师傅名
     * @return 角色列表
     */
    List<CharacterEntity> findByMasterName(String masterName);
    
    /**
     * 根据配偶名查询角色
     * 
     * @param spouseName 配偶名
     * @return 角色实体
     */
    Optional<CharacterEntity> findBySpouseName(String spouseName);
    
    /**
     * 根据状态查询角色列表
     * 
     * @param status 状态
     * @return 角色列表
     */
    List<CharacterEntity> findByStatus(Integer status);
    
    /**
     * 查询指定时间范围内创建的角色
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 角色列表
     */
    List<CharacterEntity> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查询指定时间范围内最后登录的角色
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 角色列表
     */
    List<CharacterEntity> findByLastLoginTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计角色总数
     * 
     * @return 角色总数
     */
    @Query("SELECT COUNT(c) FROM CharacterEntity c WHERE c.deleted = false")
    long countActiveCharacters();
    
    /**
     * 统计指定职业的角色数量
     * 
     * @param job 职业
     * @return 角色数量
     */
    @Query("SELECT COUNT(c) FROM CharacterEntity c WHERE c.job = :job AND c.deleted = false")
    long countByJob(@Param("job") Integer job);
    
    /**
     * 统计指定等级范围的角色数量
     * 
     * @param minLevel 最小等级
     * @param maxLevel 最大等级
     * @return 角色数量
     */
    @Query("SELECT COUNT(c) FROM CharacterEntity c WHERE c.level BETWEEN :minLevel AND :maxLevel AND c.deleted = false")
    long countByLevelRange(@Param("minLevel") Integer minLevel, @Param("maxLevel") Integer maxLevel);
    
    /**
     * 统计指定地图的角色数量
     * 
     * @param mapName 地图名
     * @return 角色数量
     */
    @Query("SELECT COUNT(c) FROM CharacterEntity c WHERE c.currentMap = :mapName AND c.deleted = false")
    long countByMap(@Param("mapName") String mapName);
    
    /**
     * 统计今日新增角色数
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 新增角色数
     */
    @Query("SELECT COUNT(c) FROM CharacterEntity c WHERE c.createTime BETWEEN :startTime AND :endTime")
    long countNewCharactersToday(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计今日活跃角色数
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 活跃角色数
     */
    @Query("SELECT COUNT(c) FROM CharacterEntity c WHERE c.lastLoginTime BETWEEN :startTime AND :endTime")
    long countActiveCharactersToday(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询等级排行榜
     * 
     * @param limit 限制数量
     * @return 角色列表
     */
    @Query("SELECT c FROM CharacterEntity c WHERE c.deleted = false ORDER BY c.level DESC, c.experience DESC")
    List<CharacterEntity> findTopLevelCharacters(@Param("limit") int limit);
    
    /**
     * 查询金币排行榜
     * 
     * @param limit 限制数量
     * @return 角色列表
     */
    @Query("SELECT c FROM CharacterEntity c WHERE c.deleted = false ORDER BY c.gold DESC")
    List<CharacterEntity> findTopGoldCharacters(@Param("limit") int limit);
    
    /**
     * 查询在线时长排行榜
     * 
     * @param limit 限制数量
     * @return 角色列表
     */
    @Query("SELECT c FROM CharacterEntity c WHERE c.deleted = false ORDER BY c.onlineTime DESC")
    List<CharacterEntity> findTopOnlineCharacters(@Param("limit") int limit);
    
    /**
     * 更新角色位置
     * 
     * @param characterId 角色ID
     * @param mapName 地图名
     * @param x X坐标
     * @param y Y坐标
     * @param direction 方向
     */
    @Query("UPDATE CharacterEntity c SET c.currentMap = :mapName, c.currentX = :x, c.currentY = :y, c.direction = :direction WHERE c.id = :characterId")
    void updatePosition(@Param("characterId") Long characterId, @Param("mapName") String mapName, 
                       @Param("x") Integer x, @Param("y") Integer y, @Param("direction") Integer direction);
    
    /**
     * 更新角色等级和经验
     * 
     * @param characterId 角色ID
     * @param level 等级
     * @param experience 经验
     */
    @Query("UPDATE CharacterEntity c SET c.level = :level, c.experience = :experience WHERE c.id = :characterId")
    void updateLevelAndExperience(@Param("characterId") Long characterId, @Param("level") Integer level, @Param("experience") Long experience);
    
    /**
     * 更新角色生命值和魔法值
     * 
     * @param characterId 角色ID
     * @param hp 生命值
     * @param mp 魔法值
     */
    @Query("UPDATE CharacterEntity c SET c.hp = :hp, c.mp = :mp WHERE c.id = :characterId")
    void updateHpAndMp(@Param("characterId") Long characterId, @Param("hp") Integer hp, @Param("mp") Integer mp);
    
    /**
     * 更新角色基本属性
     * 
     * @param characterId 角色ID
     * @param strength 力量
     * @param agility 敏捷
     * @param constitution 体力
     * @param intelligence 智力
     * @param statPoints 属性点
     */
    @Query("UPDATE CharacterEntity c SET c.strength = :strength, c.agility = :agility, c.constitution = :constitution, c.intelligence = :intelligence, c.statPoints = :statPoints WHERE c.id = :characterId")
    void updateAttributes(@Param("characterId") Long characterId, @Param("strength") Integer strength, 
                         @Param("agility") Integer agility, @Param("constitution") Integer constitution, 
                         @Param("intelligence") Integer intelligence, @Param("statPoints") Integer statPoints);
    
    /**
     * 更新角色财富
     * 
     * @param characterId 角色ID
     * @param gold 金币
     * @param gamePoint 游戏点数
     * @param gameDiamond 游戏钻石
     */
    @Query("UPDATE CharacterEntity c SET c.gold = :gold, c.gamePoint = :gamePoint, c.gameDiamond = :gameDiamond WHERE c.id = :characterId")
    void updateWealth(@Param("characterId") Long characterId, @Param("gold") Long gold, 
                     @Param("gamePoint") Long gamePoint, @Param("gameDiamond") Long gameDiamond);
    
    /**
     * 更新角色PK值和声望
     * 
     * @param characterId 角色ID
     * @param pkValue PK值
     * @param reputation 声望
     */
    @Query("UPDATE CharacterEntity c SET c.pkValue = :pkValue, c.reputation = :reputation WHERE c.id = :characterId")
    void updatePkAndReputation(@Param("characterId") Long characterId, @Param("pkValue") Integer pkValue, @Param("reputation") Integer reputation);
    
    /**
     * 更新角色行会信息
     * 
     * @param characterId 角色ID
     * @param guildName 行会名
     * @param guildRank 行会职位
     */
    @Query("UPDATE CharacterEntity c SET c.guildName = :guildName, c.guildRank = :guildRank WHERE c.id = :characterId")
    void updateGuildInfo(@Param("characterId") Long characterId, @Param("guildName") String guildName, @Param("guildRank") String guildRank);
    
    /**
     * 更新角色师傅信息
     * 
     * @param characterId 角色ID
     * @param masterName 师傅名
     */
    @Query("UPDATE CharacterEntity c SET c.masterName = :masterName WHERE c.id = :characterId")
    void updateMasterInfo(@Param("characterId") Long characterId, @Param("masterName") String masterName);
    
    /**
     * 更新角色配偶信息
     * 
     * @param characterId 角色ID
     * @param spouseName 配偶名
     */
    @Query("UPDATE CharacterEntity c SET c.spouseName = :spouseName WHERE c.id = :characterId")
    void updateSpouseInfo(@Param("characterId") Long characterId, @Param("spouseName") String spouseName);
    
    /**
     * 更新角色登录时间
     * 
     * @param characterId 角色ID
     * @param loginTime 登录时间
     */
    @Query("UPDATE CharacterEntity c SET c.lastLoginTime = :loginTime WHERE c.id = :characterId")
    void updateLastLoginTime(@Param("characterId") Long characterId, @Param("loginTime") LocalDateTime loginTime);
    
    /**
     * 更新角色在线时长
     * 
     * @param characterId 角色ID
     * @param onlineTime 在线时长
     */
    @Query("UPDATE CharacterEntity c SET c.onlineTime = :onlineTime WHERE c.id = :characterId")
    void updateOnlineTime(@Param("characterId") Long characterId, @Param("onlineTime") Integer onlineTime);
    
    /**
     * 更新角色保存时间
     * 
     * @param characterId 角色ID
     * @param saveTime 保存时间
     */
    @Query("UPDATE CharacterEntity c SET c.lastSaveTime = :saveTime WHERE c.id = :characterId")
    void updateLastSaveTime(@Param("characterId") Long characterId, @Param("saveTime") LocalDateTime saveTime);
    
    /**
     * 删除角色（逻辑删除）
     * 
     * @param characterId 角色ID
     */
    @Query("UPDATE CharacterEntity c SET c.deleted = true WHERE c.id = :characterId")
    void deleteCharacter(@Param("characterId") Long characterId);
    
    /**
     * 根据角色名模糊查询
     * 
     * @param name 角色名
     * @return 角色列表
     */
    List<CharacterEntity> findByNameContaining(String name);
    
    /**
     * 根据行会名模糊查询
     * 
     * @param guildName 行会名
     * @return 角色列表
     */
    List<CharacterEntity> findByGuildNameContaining(String guildName);
    
    /**
     * 查询指定用户的活跃角色
     * 
     * @param userId 用户ID
     * @return 角色列表
     */
    @Query("SELECT c FROM CharacterEntity c WHERE c.userId = :userId AND c.status = 0 AND c.deleted = false ORDER BY c.lastLoginTime DESC")
    List<CharacterEntity> findActiveCharactersByUserId(@Param("userId") Long userId);
    
    /**
     * 查询指定用户的最后登录角色
     * 
     * @param userId 用户ID
     * @return 角色实体
     */
    @Query("SELECT c FROM CharacterEntity c WHERE c.userId = :userId AND c.status = 0 AND c.deleted = false ORDER BY c.lastLoginTime DESC LIMIT 1")
    Optional<CharacterEntity> findLastLoginCharacterByUserId(@Param("userId") Long userId);
} 