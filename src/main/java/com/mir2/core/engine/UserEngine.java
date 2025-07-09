package com.mir2.core.engine;

import com.mir2.core.model.BaseObject;
import com.mir2.core.model.Player;
import com.mir2.core.enums.Job;
import com.mir2.core.enums.Direction;
import com.mir2.service.PlayerService;
import com.mir2.service.MapService;
import com.mir2.service.CombatService;
import com.mir2.service.ItemService;
import com.mir2.service.TradeService;
import com.mir2.network.ClientSessionManager;
import com.mir2.network.ClientSession;
import com.mir2.network.GameMessage;
import com.mir2.network.GameMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 用户引擎类
 * 
 * <p>负责管理游戏中的所有玩家对象，处理玩家相关的游戏逻辑。
 * 这是游戏服务器的核心组件之一，对应原M2Engine中的UserEngine。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>玩家对象生命周期管理</li>
 *   <li>玩家登录和退出处理</li>
 *   <li>玩家数据持久化</li>
 *   <li>玩家行为验证和处理</li>
 *   <li>玩家状态更新和同步</li>
 *   <li>反外挂检测</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class UserEngine {
    
    /** 在线玩家映射 (玩家ID -> 玩家对象) */
    private final Map<Long, Player> onlinePlayers = new ConcurrentHashMap<>();
    
    /** 玩家名称映射 (玩家名 -> 玩家对象) */
    private final Map<String, Player> playersByName = new ConcurrentHashMap<>();
    
    /** 账号映射 (账号名 -> 玩家对象) */
    private final Map<String, Player> playersByAccount = new ConcurrentHashMap<>();
    
    /** 等待登录的玩家队列 */
    private final Map<String, Player> pendingPlayers = new ConcurrentHashMap<>();
    
    /** 玩家会话映射 */
    private final Map<String, String> playerSessions = new ConcurrentHashMap<>();
    
    /** 玩家最后移动时间 */
    private final Map<String, Long> lastMoveTime = new ConcurrentHashMap<>();
    
    /** 玩家异常行为统计 */
    private final Map<String, Integer> playerViolations = new ConcurrentHashMap<>();
    
    /** 引擎运行状态 */
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    /** 引擎启动时间 */
    private long startTime;
    
    /** 总处理的玩家数 */
    private final AtomicLong totalProcessedPlayers = new AtomicLong(0);
    
    /** 定时任务执行器 */
    private ScheduledExecutorService scheduler;
    
    /** 依赖服务 */
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private MapService mapService;
    
    @Autowired
    private CombatService combatService;
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private TradeService tradeService;
    
    @Autowired
    private ClientSessionManager sessionManager;
    
    /** 用户名到用户ID的缓存映射 */
    private final Map<String, Long> usernameToUserIdCache = new ConcurrentHashMap<>();
    
    /** 配置参数 */
    @Value("${game.server.max-players:1000}")
    private int maxPlayers;
    
    @Value("${game.server.logic.main-loop-interval:50}")
    private int mainLoopInterval;
    
    @Value("${game.server.logic.save-interval:60000}")
    private int saveInterval;
    
    @Value("${game.server.security.anti-cheat:true}")
    private boolean antiCheatEnabled;
    
    @Value("${game.server.security.speed-threshold:500}")
    private int speedThreshold;
    
    @Value("${game.server.security.max-violations:10}")
    private int maxViolations;
    
    @Value("${game.server.player.auto-heal-interval:5000}")
    private int autoHealInterval;
    
    @Value("${game.server.player.auto-heal-rate:5}")
    private int autoHealRate;
    
    /**
     * 初始化用户引擎
     */
    @PostConstruct
    public void initialize() {
        log.info("正在初始化用户引擎...");
        
        this.startTime = System.currentTimeMillis();
        this.scheduler = Executors.newScheduledThreadPool(4);
        
        // 启动主循环
        startMainLoop();
        
        // 启动数据保存任务
        startSaveTask();
        
        // 启动反外挂检测
        if (antiCheatEnabled) {
            startAntiCheatTask();
        }
        
        // 启动统计任务
        startStatisticsTask();
        
        this.running.set(true);
        
        log.info("用户引擎初始化完成 [最大玩家数: {}, 主循环间隔: {}ms, 保存间隔: {}ms]", 
                maxPlayers, mainLoopInterval, saveInterval);
    }
    
    /**
     * 销毁用户引擎
     */
    @PreDestroy
    public void destroy() {
        log.info("正在关闭用户引擎...");
        
        this.running.set(false);
        
        // 保存所有在线玩家数据
        saveAllPlayers();
        
        // 关闭所有玩家连接
        disconnectAllPlayers();
        
        // 关闭定时任务
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        log.info("用户引擎已关闭 [运行时长: {}ms, 总处理玩家数: {}]", 
                System.currentTimeMillis() - startTime, totalProcessedPlayers.get());
    }
    
    /**
     * 玩家登录处理
     * 
     * @param player 玩家对象
     * @return 登录是否成功
     */
    public boolean playerLogin(Player player) {
        if (player == null) {
            log.warn("玩家登录失败: 玩家对象为空");
            return false;
        }
        
        String playerName = player.getName();
        String accountName = player.getAccountName();
        
        // 检查服务器是否已满
        if (onlinePlayers.size() >= maxPlayers) {
            log.warn("玩家 {} 登录失败: 服务器已满 (当前: {}/{})", playerName, onlinePlayers.size(), maxPlayers);
            return false;
        }
        
        // 检查是否重复登录
        if (playersByName.containsKey(playerName)) {
            log.warn("玩家 {} 登录失败: 角色已在线", playerName);
            return false;
        }
        
        if (playersByAccount.containsKey(accountName)) {
            log.warn("账号 {} 登录失败: 账号已在线", accountName);
            return false;
        }
        
        // 添加到在线玩家列表
        onlinePlayers.put(player.getId(), player);
        playersByName.put(playerName, player);
        playersByAccount.put(accountName, player);
        
        // 设置登录时间
        player.setLoginTime(System.currentTimeMillis());
        
        // 初始化玩家状态
        player.setOnline(true);
        player.setLastActivityTime(System.currentTimeMillis());
        
        // 增加处理计数
        totalProcessedPlayers.incrementAndGet();
        
        log.info("玩家 {} 成功登录 [账号: {}, ID: {}, 在线人数: {}/{}]", 
                playerName, accountName, player.getId(), onlinePlayers.size(), maxPlayers);
        
        // 触发登录事件
        onPlayerLogin(player);
        
        return true;
    }
    
    /**
     * 玩家退出处理
     * 
     * @param player 玩家对象
     * @return 退出是否成功
     */
    public boolean playerLogout(Player player) {
        if (player == null) {
            return false;
        }
        
        String playerName = player.getName();
        String accountName = player.getAccountName();
        
        // 从在线玩家列表移除
        onlinePlayers.remove(player.getId());
        playersByName.remove(playerName);
        playersByAccount.remove(accountName);
        
        // 清理相关数据
        lastMoveTime.remove(playerName);
        playerViolations.remove(playerName);
        
        // 保存玩家数据
        savePlayerData(player);
        
        // 设置离线状态
        player.setOnline(false);
        
        // 计算在线时长
        long onlineTime = System.currentTimeMillis() - player.getLoginTime();
        player.setOnlineTime(player.getOnlineTime() + (int)(onlineTime / 60000)); // 转换为分钟
        
        log.info("玩家 {} 退出游戏 [账号: {}, 在线时长: {}ms, 在线人数: {}/{}]", 
                playerName, accountName, onlineTime, onlinePlayers.size(), maxPlayers);
        
        // 触发退出事件
        onPlayerLogout(player);
        
        return true;
    }
    
    /**
     * 根据玩家名获取玩家
     * 
     * @param playerName 玩家名
     * @return 玩家对象，如果不存在返回null
     */
    public Player getPlayerByName(String playerName) {
        return playersByName.get(playerName);
    }
    
    /**
     * 根据账号名获取玩家
     * 
     * @param accountName 账号名
     * @return 玩家对象，如果不存在返回null
     */
    public Player getPlayerByAccount(String accountName) {
        return playersByAccount.get(accountName);
    }
    
    /**
     * 根据玩家ID获取玩家
     * 
     * @param playerId 玩家ID
     * @return 玩家对象，如果不存在返回null
     */
    public Player getPlayerById(long playerId) {
        return onlinePlayers.get(playerId);
    }
    
    /**
     * 获取在线玩家数量
     * 
     * @return 在线玩家数量
     */
    public int getOnlinePlayerCount() {
        return onlinePlayers.size();
    }
    
    /**
     * 获取所有在线玩家
     * 
     * @return 在线玩家映射的副本
     */
    public Map<Long, Player> getOnlinePlayers() {
        return new HashMap<>(onlinePlayers);
    }
    
    /**
     * 踢出指定玩家
     * 
     * @param playerName 玩家名
     * @param reason 踢出原因
     * @return 操作是否成功
     */
    public boolean kickPlayer(String playerName, String reason) {
        Player player = playersByName.get(playerName);
        if (player == null) {
            return false;
        }
        
        // 发送踢出消息
        sendMessageToPlayer(player, "您已被踢出游戏: " + reason);
        
        // 延迟踢出
        scheduler.schedule(() -> {
            playerLogout(player);
        }, 3, TimeUnit.SECONDS);
        
        log.info("踢出玩家 {} [原因: {}]", playerName, reason);
        return true;
    }
    
    /**
     * 广播消息给所有玩家
     * 
     * @param message 消息内容
     */
    public void broadcastMessage(String message) {
        log.info("广播消息: {}", message);
        
        for (Player player : onlinePlayers.values()) {
            sendMessageToPlayer(player, message);
        }
    }
    
    /**
     * 验证玩家行为
     * 
     * @param player 玩家对象
     * @param action 行为类型
     * @param params 行为参数
     * @return 验证是否通过
     */
    public boolean validatePlayerAction(Player player, String action, Object... params) {
        if (player == null) {
            return false;
        }
        
        // 更新玩家活动时间
        player.setLastActivityTime(System.currentTimeMillis());
        
        // 反外挂检测
        if (antiCheatEnabled) {
            if (!antiCheatCheck(player, action, params)) {
                log.warn("玩家 {} 行为验证失败: {}", player.getName(), action);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 启动主循环
     */
    private void startMainLoop() {
        scheduler.scheduleWithFixedDelay(this::processPlayers, 
                mainLoopInterval, mainLoopInterval, TimeUnit.MILLISECONDS);
        log.debug("主循环已启动 [间隔: {}ms]", mainLoopInterval);
    }
    
    /**
     * 启动数据保存任务
     */
    private void startSaveTask() {
        scheduler.scheduleWithFixedDelay(this::saveAllPlayers, 
                saveInterval, saveInterval, TimeUnit.MILLISECONDS);
        log.debug("数据保存任务已启动 [间隔: {}ms]", saveInterval);
    }
    
    /**
     * 启动反外挂检测任务
     */
    private void startAntiCheatTask() {
        scheduler.scheduleWithFixedDelay(this::performAntiCheatCheck, 
                10000, 10000, TimeUnit.MILLISECONDS);
        log.debug("反外挂检测任务已启动 [间隔: 10s]");
    }
    
    /**
     * 启动统计任务
     */
    private void startStatisticsTask() {
        scheduler.scheduleWithFixedDelay(this::logStatistics, 
                60000, 60000, TimeUnit.MILLISECONDS);
        log.debug("统计任务已启动 [间隔: 60s]");
    }
    
    /**
     * 处理所有在线玩家
     */
    private void processPlayers() {
        for (Player player : onlinePlayers.values()) {
            try {
                // 更新玩家状态
                player.update();
                
                // 处理玩家AI逻辑
                processPlayerAI(player);
                
                // 检查玩家连接状态
                checkPlayerConnection(player);
                
            } catch (Exception e) {
                log.error("处理玩家 {} 时发生异常", player.getName(), e);
            }
        }
    }
    
    /**
     * 处理玩家AI逻辑
     * 
     * @param player 玩家对象
     */
    private void processPlayerAI(Player player) {
        long currentTime = System.currentTimeMillis();
        
        // 自动回血
        if (player.getHp() < player.getMaxHp() && 
            currentTime - player.getLastUpdateTime() >= autoHealInterval) {
            int healAmount = Math.min(autoHealRate, player.getMaxHp() - player.getHp());
            player.setHp(player.getHp() + healAmount);
            
            if (healAmount > 0) {
                log.debug("玩家 {} 自动回血 [{}]", player.getName(), healAmount);
            }
        }
        
        // 自动回蓝
        if (player.getMp() < player.getMaxMp() && 
            currentTime - player.getLastUpdateTime() >= autoHealInterval) {
            int healAmount = Math.min(autoHealRate, player.getMaxMp() - player.getMp());
            player.setMp(player.getMp() + healAmount);
            
            if (healAmount > 0) {
                log.debug("玩家 {} 自动回蓝 [{}]", player.getName(), healAmount);
            }
        }
        
        // 更新最后处理时间
        player.setLastUpdateTime(currentTime);
    }
    
    /**
     * 检查玩家连接状态
     * 
     * @param player 玩家对象
     */
    private void checkPlayerConnection(Player player) {
        long currentTime = System.currentTimeMillis();
        long lastActivity = player.getLastActivityTime();
        
        // 超时踢出（5分钟无活动）
        if (currentTime - lastActivity > 300000) {
            log.warn("玩家 {} 长时间无活动，踢出游戏", player.getName());
            kickPlayer(player.getName(), "长时间无活动");
        }
    }
    
    /**
     * 保存所有玩家数据
     */
    private void saveAllPlayers() {
        int savedCount = 0;
        for (Player player : onlinePlayers.values()) {
            try {
                savePlayerData(player);
                savedCount++;
            } catch (Exception e) {
                log.error("保存玩家 {} 数据时发生异常", player.getName(), e);
            }
        }
        
        if (savedCount > 0) {
            log.debug("已保存 {} 个玩家的数据", savedCount);
        }
    }
    
    /**
     * 保存单个玩家数据
     * 
     * @param player 玩家对象
     */
    private void savePlayerData(Player player) {
        if (player != null) {
            try {
                // 使用PlayerService保存数据
                playerService.savePlayerData(player);
                
                // 更新最后保存时间
                player.setLastSaveTime(System.currentTimeMillis());
                
                log.debug("保存玩家数据: {}", player.getName());
            } catch (Exception e) {
                log.error("保存玩家 {} 数据失败", player.getName(), e);
            }
        }
    }
    
    /**
     * 断开所有玩家连接
     */
    private void disconnectAllPlayers() {
        log.info("正在断开所有玩家连接...");
        
        for (Player player : onlinePlayers.values()) {
            try {
                playerLogout(player);
            } catch (Exception e) {
                log.error("断开玩家 {} 连接时发生异常", player.getName(), e);
            }
        }
        
        onlinePlayers.clear();
        playersByName.clear();
        playersByAccount.clear();
        pendingPlayers.clear();
        playerSessions.clear();
        lastMoveTime.clear();
        playerViolations.clear();
        
        log.info("所有玩家连接已断开");
    }
    
    /**
     * 反外挂检测
     * 
     * @param player 玩家对象
     * @param action 操作类型
     * @param params 操作参数
     * @return 检测是否通过
     */
    private boolean antiCheatCheck(Player player, String action, Object... params) {
        String playerName = player.getName();
        long currentTime = System.currentTimeMillis();
        
        // 移动速度检测
        if ("MOVE".equals(action) && params.length >= 2) {
            Long lastMove = lastMoveTime.get(playerName);
            if (lastMove != null) {
                long timeDiff = currentTime - lastMove;
                if (timeDiff < speedThreshold) {
                    // 记录违规行为
                    int violations = playerViolations.getOrDefault(playerName, 0) + 1;
                    playerViolations.put(playerName, violations);
                    
                    log.warn("玩家 {} 移动速度异常: 间隔 {}ms < 阈值 {}ms [违规次数: {}]", 
                            playerName, timeDiff, speedThreshold, violations);
                    
                    // 违规次数过多，踢出玩家
                    if (violations >= maxViolations) {
                        kickPlayer(playerName, "疑似使用外挂");
                        return false;
                    }
                    
                    return false;
                }
            }
            lastMoveTime.put(playerName, currentTime);
        }
        
        // 攻击频率检测
        if ("ATTACK".equals(action)) {
            Long lastAttack = lastMoveTime.get(playerName + "_ATTACK");
            if (lastAttack != null) {
                long timeDiff = currentTime - lastAttack;
                int minAttackInterval = 800; // 最小攻击间隔800ms
                
                if (timeDiff < minAttackInterval) {
                    // 记录违规行为
                    int violations = playerViolations.getOrDefault(playerName, 0) + 1;
                    playerViolations.put(playerName, violations);
                    
                    log.warn("玩家 {} 攻击频率异常: 间隔 {}ms < 阈值 {}ms [违规次数: {}]", 
                            playerName, timeDiff, minAttackInterval, violations);
                    
                    // 违规次数过多，踢出玩家
                    if (violations >= maxViolations) {
                        kickPlayer(playerName, "攻击频率异常，疑似使用外挂");
                        return false;
                    }
                    
                    return false;
                }
            }
            lastMoveTime.put(playerName + "_ATTACK", currentTime);
        }
        
        // 数据包完整性检测
        if ("PACKET".equals(action)) {
            if (params.length > 0) {
                Object packetData = params[0];
                
                // 检查数据包大小
                if (packetData instanceof byte[]) {
                    byte[] data = (byte[]) packetData;
                    int maxPacketSize = 8192; // 最大数据包大小8KB
                    
                    if (data.length > maxPacketSize) {
                        log.warn("玩家 {} 发送超大数据包: {}字节 > {}字节", 
                                playerName, data.length, maxPacketSize);
                        return false;
                    }
                    
                    // 检查数据包频率
                    String packetKey = playerName + "_PACKET_COUNT";
                    Integer packetCount = playerViolations.getOrDefault(packetKey, 0);
                    playerViolations.put(packetKey, packetCount + 1);
                    
                    // 每秒最多100个数据包
                    if (packetCount > 100) {
                        log.warn("玩家 {} 数据包频率过高: {}个/秒", playerName, packetCount);
                        
                        int violations = playerViolations.getOrDefault(playerName, 0) + 1;
                        playerViolations.put(playerName, violations);
                        
                        if (violations >= maxViolations) {
                            kickPlayer(playerName, "数据包频率异常，疑似使用外挂");
                            return false;
                        }
                        
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * 执行全局反外挂检测
     */
    private void performAntiCheatCheck() {
        // 检查异常玩家行为
        for (Map.Entry<String, Integer> entry : playerViolations.entrySet()) {
            String playerName = entry.getKey();
            int violations = entry.getValue();
            
            if (violations >= maxViolations) {
                log.warn("玩家 {} 违规次数过多，踢出游戏 [违规次数: {}]", playerName, violations);
                kickPlayer(playerName, "疑似使用外挂");
            }
        }
        
        // 清理过期的违规记录
        long currentTime = System.currentTimeMillis();
        playerViolations.entrySet().removeIf(entry -> {
            String playerName = entry.getKey();
            Player player = playersByName.get(playerName);
            return player == null || currentTime - player.getLastActivityTime() > 600000; // 10分钟
        });
    }
    
    /**
     * 记录统计信息
     */
    private void logStatistics() {
        long runTime = System.currentTimeMillis() - startTime;
        int onlineCount = onlinePlayers.size();
        long totalProcessed = totalProcessedPlayers.get();
        
        log.info("用户引擎统计 [运行时长: {}ms, 在线玩家: {}/{}, 总处理: {}]", 
                runTime, onlineCount, maxPlayers, totalProcessed);
    }
    
    /**
     * 发送消息给玩家
     * 
     * @param player 玩家对象
     * @param message 消息内容
     */
    private void sendMessageToPlayer(Player player, String message) {
        try {
            ClientSession session = sessionManager.getSessionByPlayer(player.getName());
            if (session != null) {
                GameMessage gameMessage = GameMessage.builder()
                        .type(GameMessageType.SYSTEM_MESSAGE)
                        .sequence(0)
                        .data(message.getBytes())
                        .build();
                
                session.sendMessage(gameMessage);
            }
        } catch (Exception e) {
            log.error("发送消息给玩家 {} 失败", player.getName(), e);
        }
    }
    
    // 事件处理方法
    
    /**
     * 玩家登录事件处理
     * 
     * @param player 玩家对象
     */
    protected void onPlayerLogin(Player player) {
        // 可被子类重写或通过事件系统处理
        log.debug("玩家登录事件: {}", player.getName());
        
        // 发送欢迎消息
        sendMessageToPlayer(player, "欢迎来到传奇世界！");
        
        // 通知其他玩家
        broadcastMessage(player.getName() + " 进入了游戏");
    }
    
    /**
     * 玩家退出事件处理
     * 
     * @param player 玩家对象
     */
    protected void onPlayerLogout(Player player) {
        // 可被子类重写或通过事件系统处理
        log.debug("玩家退出事件: {}", player.getName());
        
        // 通知其他玩家
        broadcastMessage(player.getName() + " 离开了游戏");
    }
    
    /**
     * 获取引擎状态信息
     * 
     * @return 状态信息字符串
     */
    public String getEngineStatus() {
        return String.format("UserEngine[运行中: %s, 在线玩家: %d/%d, 运行时长: %dms]",
                running.get(), onlinePlayers.size(), maxPlayers, 
                System.currentTimeMillis() - startTime);
    }
    
    /**
     * 通过用户名查找用户ID
     * 
     * @param username 用户名
     * @return 用户ID，如果不存在返回null
     */
    private Long getUserIdByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        // 先从缓存查找
        Long userId = usernameToUserIdCache.get(username);
        if (userId != null) {
            return userId;
        }
        
        try {
            // 从数据库查找
            var userEntity = playerService.getUserByUsername(username);
            if (userEntity != null) {
                userId = userEntity.getId();
                // 缓存结果
                usernameToUserIdCache.put(username, userId);
                return userId;
            }
        } catch (Exception e) {
            log.error("通过用户名 {} 查找用户ID失败", username, e);
        }
        
        return null;
    }
    
    /**
     * 获取最大玩家数
     * 
     * @return 最大玩家数
     */
    public int getMaxPlayerCount() {
        return maxPlayers;
    }
    
    /**
     * 获取在线玩家列表
     * 
     * @return 在线玩家列表
     */
    public Map<String, Object> getOnlinePlayerList() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> playerList = new ArrayList<>();
        
        for (Player player : onlinePlayers.values()) {
            Map<String, Object> playerInfo = new HashMap<>();
            playerInfo.put("name", player.getName());
            playerInfo.put("level", player.getLevel());
            playerInfo.put("job", player.getJob().name());
            playerInfo.put("loginTime", player.getLoginTime());
            playerInfo.put("mapName", player.getMapName());
            playerInfo.put("x", player.getX());
            playerInfo.put("y", player.getY());
            playerList.add(playerInfo);
        }
        
        result.put("players", playerList);
        result.put("count", playerList.size());
        result.put("maxCount", maxPlayers);
        
        return result;
    }
    
    /**
     * 查找在线玩家
     * 
     * @param characterName 角色名
     * @return 玩家对象，如果不存在返回null
     */
    public Player findOnlinePlayer(String characterName) {
        return playersByName.get(characterName);
    }
    
    /**
     * 踢出玩家
     * 
     * @param characterName 角色名
     * @return 操作是否成功
     */
    public boolean kickPlayer(String characterName) {
        Player player = findOnlinePlayer(characterName);
        if (player != null) {
            log.info("踢出玩家: {}", characterName);
            return playerLogout(player);
        }
        return false;
    }
    
    /**
     * 广播系统消息
     * 
     * @param message 消息内容
     * @return 操作是否成功
     */
    public boolean broadcastSystemMessage(String message) {
        log.info("广播系统消息: {}", message);
        broadcastMessage(message);
        return true;
    }
    
    /**
     * 用户认证
     * 
     * @param username 用户名
     * @param password 密码
     * @return 认证是否成功
     */
    public boolean authenticateUser(String username, String password) {
        try {
            return playerService.authenticateUser(username, password, "127.0.0.1") != null;
        } catch (Exception e) {
            log.error("用户认证失败: {}", username, e);
            return false;
        }
    }
    
    /**
     * 创建角色
     * 
     * @param username 用户名
     * @param characterName 角色名
     * @param jobStr 职业
     * @param genderStr 性别
     * @return 创建是否成功
     */
    public boolean createCharacter(String username, String characterName, String jobStr, String genderStr) {
        try {
            Job job = Job.valueOf(jobStr.toUpperCase());
            int gender = "MALE".equalsIgnoreCase(genderStr) ? 0 : 1;
            
            // 查找用户
            Long userId = getUserIdByUsername(username);
            if (userId == null) {
                log.error("创建角色失败: 用户 {} 不存在", username);
                return false;
            }
            
            return playerService.createCharacter(userId, characterName, job, gender) != null;
        } catch (Exception e) {
            log.error("创建角色失败: 用户={}, 角色={}, 职业={}, 性别={}", 
                    username, characterName, jobStr, genderStr, e);
            return false;
        }
    }
    
    /**
     * 加载角色
     * 
     * @param username 用户名
     * @param characterName 角色名
     * @return 玩家对象
     */
    public Player loadCharacter(String username, String characterName) {
        try {
            // 查找用户
            Long userId = getUserIdByUsername(username);
            if (userId == null) {
                log.error("加载角色失败: 用户 {} 不存在", username);
                return null;
            }
            
            return playerService.enterGame(userId, characterName, "session_" + System.currentTimeMillis());
        } catch (Exception e) {
            log.error("加载角色失败: 用户={}, 角色={}", username, characterName, e);
            return null;
        }
    }
    
    /**
     * 进入游戏
     * 
     * @param player 玩家对象
     * @return 操作是否成功
     */
    public boolean enterGame(Player player) {
        log.info("玩家 {} 进入游戏", player.getName());
        
        try {
            // 将玩家添加到地图
            return mapService.enterMap(player, player.getMapName(), player.getX(), player.getY());
        } catch (Exception e) {
            log.error("玩家 {} 进入游戏失败", player.getName(), e);
            return false;
        }
    }
    
    /**
     * 离开游戏
     * 
     * @param player 玩家对象
     * @return 操作是否成功
     */
    public boolean leaveGame(Player player) {
        log.info("玩家 {} 离开游戏", player.getName());
        
        try {
            // 从地图移除玩家
            mapService.leaveMap(player, player.getMapName());
            
            // 保存玩家数据
            savePlayerData(player);
            
            return true;
        } catch (Exception e) {
            log.error("玩家 {} 离开游戏失败", player.getName(), e);
            return false;
        }
    }
    
    /**
     * 启动定时任务
     */
    public void startScheduledTasks() {
        startMainLoop();
        startSaveTask();
        startAntiCheatTask();
        startStatisticsTask();
    }
    
    /**
     * 停止定时任务
     */
    public void stopScheduledTasks() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
    
    /**
     * 关闭引擎
     */
    public void shutdown() {
        running.set(false);
        stopScheduledTasks();
        disconnectAllPlayers();
        log.info("UserEngine已关闭");
    }
    
    /**
     * 保存所有玩家数据
     */
    public void saveAllPlayerData() {
        log.info("保存所有玩家数据...");
        saveAllPlayers();
        log.info("所有玩家数据保存完成");
    }
    
    /**
     * 处理玩家移动
     * 
     * @param player 玩家对象
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     * @param direction 方向
     * @return 操作是否成功
     */
    public boolean handlePlayerMove(Player player, int targetX, int targetY, int direction) {
        log.debug("玩家 {} 移动到 ({}, {}) 方向 {}", player.getName(), targetX, targetY, direction);
        
        try {
            // 验证移动
            if (!validatePlayerAction(player, "MOVE", targetX, targetY, direction)) {
                return false;
            }
            
            // 使用MapService处理移动
            Direction dir = Direction.fromValue(direction);
            boolean success = mapService.movePlayer(player, targetX, targetY, dir.getValue());
            
            if (success) {
                player.setDirection(dir);
                player.setLastActivityTime(System.currentTimeMillis());
            }
            
            return success;
        } catch (Exception e) {
            log.error("处理玩家 {} 移动失败", player.getName(), e);
            return false;
        }
    }
    
    /**
     * 处理聊天消息
     * 
     * @param player 玩家对象
     * @param content 聊天内容
     */
    public void handleChatMessage(Player player, String content) {
        log.debug("玩家 {} 聊天: {}", player.getName(), content);
        
        try {
            // 验证聊天
            if (!validatePlayerAction(player, "CHAT", content)) {
                return;
            }
            
            // 解析聊天类型
            int chatType = 0; // 普通聊天
            String message = content;
            
            if (content.startsWith("/")) {
                chatType = 1; // 私聊
                message = content.substring(1);
            } else if (content.startsWith("!")) {
                chatType = 2; // 喊话
                message = content.substring(1);
            } else if (content.startsWith("!!")) {
                chatType = 3; // 组队
                message = content.substring(2);
            } else if (content.startsWith("!~")) {
                chatType = 4; // 行会
                message = content.substring(2);
            }
            
            // 使用PlayerService处理聊天
            playerService.sendChat(player.getName(), message, chatType);
            
        } catch (Exception e) {
            log.error("处理玩家 {} 聊天失败", player.getName(), e);
        }
    }
    
    /**
     * 处理玩家攻击
     * 
     * @param player 玩家对象
     * @param targetX 目标X坐标
     * @param targetY 目标Y坐标
     * @param direction 方向
     */
    public void handlePlayerAttack(Player player, int targetX, int targetY, int direction) {
        log.debug("玩家 {} 攻击 ({}, {}) 方向 {}", player.getName(), targetX, targetY, direction);
        
        try {
            // 验证攻击
            if (!validatePlayerAction(player, "ATTACK", targetX, targetY, direction)) {
                return;
            }
            
            // 使用CombatService处理攻击
            combatService.handlePlayerAttack(player, targetX, targetY, direction);
            
        } catch (Exception e) {
            log.error("处理玩家 {} 攻击失败", player.getName(), e);
        }
    }
    
    /**
     * 处理使用物品
     * 
     * @param player 玩家对象
     * @param itemId 物品ID
     * @return 操作是否成功
     */
    public boolean handleUseItem(Player player, int itemId) {
        log.debug("玩家 {} 使用物品 {}", player.getName(), itemId);
        
        try {
            // 验证使用物品
            if (!validatePlayerAction(player, "USE_ITEM", itemId)) {
                return false;
            }
            
            // 使用ItemService处理物品使用
            return itemService.useItem(player.getName(), itemId);
            
        } catch (Exception e) {
            log.error("处理玩家 {} 使用物品失败", player.getName(), e);
            return false;
        }
    }
    
    /**
     * 处理拾取物品
     * 
     * @param player 玩家对象
     * @param itemX 物品X坐标
     * @param itemY 物品Y坐标
     * @param itemId 物品ID
     * @return 操作是否成功
     */
    public boolean handlePickUpItem(Player player, int itemX, int itemY, int itemId) {
        log.debug("玩家 {} 拾取物品 {} 在 ({}, {})", player.getName(), itemId, itemX, itemY);
        
        try {
            // 验证拾取物品
            if (!validatePlayerAction(player, "PICKUP_ITEM", itemX, itemY, itemId)) {
                return false;
            }
            
            // 使用ItemService处理物品拾取
            return itemService.pickupItem(player.getName(), itemId, itemX, itemY);
            
        } catch (Exception e) {
            log.error("处理玩家 {} 拾取物品失败", player.getName(), e);
            return false;
        }
    }
    
    /**
     * 处理交易请求
     * 
     * @param player 玩家对象
     * @param targetPlayerName 目标玩家名
     * @return 操作是否成功
     */
    public boolean handleTradeRequest(Player player, String targetPlayerName) {
        log.debug("玩家 {} 请求与 {} 交易", player.getName(), targetPlayerName);
        
        try {
            // 验证交易请求
            if (!validatePlayerAction(player, "TRADE_REQUEST", targetPlayerName)) {
                return false;
            }
            
            // 使用TradeService处理交易
            return tradeService.requestTrade(player.getName(), targetPlayerName);
            
        } catch (Exception e) {
            log.error("处理玩家 {} 交易请求失败", player.getName(), e);
            return false;
        }
    }
} 