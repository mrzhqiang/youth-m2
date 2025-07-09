package com.mir2.web;

import com.mir2.core.engine.UserEngine;
import com.mir2.network.NettyServerEngine;
import com.mir2.network.NetworkStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 游戏管理控制器
 * 
 * <p>提供游戏服务器的管理接口，包括服务器状态查询、统计信息获取、
 * 玩家管理等功能。这些接口主要用于运维和监控。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>服务器状态管理</li>
 *   <li>统计信息查询</li>
 *   <li>玩家管理</li>
 *   <li>系统监控</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/game")
public class GameController {
    
    @Autowired
    private UserEngine userEngine;
    
    @Autowired
    private NettyServerEngine nettyServerEngine;
    
    @Autowired
    private NetworkStatistics networkStatistics;
    
    /**
     * 获取服务器状态
     * 
     * @return 服务器状态信息
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServerStatus() {
        log.info("获取服务器状态");
        
        Map<String, Object> status = new HashMap<>();
        status.put("running", nettyServerEngine.isRunning());
        status.put("port", nettyServerEngine.getPort());
        status.put("startTime", nettyServerEngine.getStartTime());
        status.put("uptime", System.currentTimeMillis() - nettyServerEngine.getStartTime());
        
        // 在线玩家统计
        status.put("onlinePlayerCount", userEngine.getOnlinePlayerCount());
        status.put("maxPlayerCount", userEngine.getMaxPlayerCount());
        
        // 系统信息
        Runtime runtime = Runtime.getRuntime();
        status.put("memoryUsed", runtime.totalMemory() - runtime.freeMemory());
        status.put("memoryTotal", runtime.totalMemory());
        status.put("memoryMax", runtime.maxMemory());
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * 获取网络统计信息
     * 
     * @return 网络统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getNetworkStatistics() {
        log.info("获取网络统计信息");
        
        Map<String, Object> stats = new HashMap<>();
        
        // 连接统计
        stats.put("totalConnections", networkStatistics.getTotalConnections());
        stats.put("activeConnections", networkStatistics.getActiveConnections());
        stats.put("peakConnections", networkStatistics.getPeakConnections());
        stats.put("failedConnections", networkStatistics.getFailedConnections());
        
        // 消息统计
        stats.put("receivedMessages", networkStatistics.getReceivedMessages());
        stats.put("sentMessages", networkStatistics.getSentMessages());
        stats.put("failedMessages", networkStatistics.getFailedMessages());
        stats.put("droppedMessages", networkStatistics.getDroppedMessages());
        
        // 流量统计
        stats.put("receivedBytes", networkStatistics.getReceivedBytes());
        stats.put("sentBytes", networkStatistics.getSentBytes());
        
        // 性能统计
        stats.put("averageProcessingTime", networkStatistics.getAverageProcessingTime());
        stats.put("messageSuccessRate", networkStatistics.getMessageSuccessRate());
        stats.put("connectionSuccessRate", networkStatistics.getConnectionSuccessRate());
        stats.put("messagesPerSecond", networkStatistics.getMessagesPerSecond());
        stats.put("bytesPerSecond", networkStatistics.getBytesPerSecond());
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 获取统计摘要
     * 
     * @return 统计摘要文本
     */
    @GetMapping("/statistics/summary")
    public ResponseEntity<String> getStatisticsSummary() {
        log.info("获取统计摘要");
        
        String summary = networkStatistics.getSummary();
        
        return ResponseEntity.ok(summary);
    }
    
    /**
     * 重置统计数据
     * 
     * @return 操作结果
     */
    @PostMapping("/statistics/reset")
    public ResponseEntity<Map<String, Object>> resetStatistics() {
        log.info("重置统计数据");
        
        networkStatistics.reset();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "统计数据已重置");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取在线玩家列表
     * 
     * @return 在线玩家列表
     */
    @GetMapping("/players/online")
    public ResponseEntity<Map<String, Object>> getOnlinePlayers() {
        log.info("获取在线玩家列表");
        
        Map<String, Object> result = new HashMap<>();
        result.put("players", userEngine.getOnlinePlayerList());
        result.put("count", userEngine.getOnlinePlayerCount());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 根据角色名搜索玩家
     * 
     * @param characterName 角色名
     * @return 玩家信息
     */
    @GetMapping("/players/search")
    public ResponseEntity<Map<String, Object>> searchPlayer(@RequestParam String characterName) {
        log.info("搜索玩家 [角色名: {}]", characterName);
        
        Map<String, Object> result = new HashMap<>();
        
        // 搜索在线玩家
        Object player = userEngine.findOnlinePlayer(characterName);
        
        if (player != null) {
            result.put("found", true);
            result.put("online", true);
            result.put("player", player);
        } else {
            result.put("found", false);
            result.put("online", false);
            result.put("message", "玩家不在线或不存在");
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 踢出玩家
     * 
     * @param characterName 角色名
     * @return 操作结果
     */
    @PostMapping("/players/kick")
    public ResponseEntity<Map<String, Object>> kickPlayer(@RequestParam String characterName) {
        log.info("踢出玩家 [角色名: {}]", characterName);
        
        Map<String, Object> result = new HashMap<>();
        
        boolean success = userEngine.kickPlayer(characterName);
        
        result.put("success", success);
        result.put("message", success ? "玩家已被踢出" : "玩家不在线或踢出失败");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 发送系统消息
     * 
     * @param message 消息内容
     * @return 操作结果
     */
    @PostMapping("/message/system")
    public ResponseEntity<Map<String, Object>> sendSystemMessage(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        
        log.info("发送系统消息 [消息: {}]", message);
        
        Map<String, Object> result = new HashMap<>();
        
        boolean success = userEngine.broadcastSystemMessage(message);
        
        result.put("success", success);
        result.put("message", success ? "系统消息已发送" : "系统消息发送失败");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 启动服务器
     * 
     * @return 操作结果
     */
    @PostMapping("/server/start")
    public ResponseEntity<Map<String, Object>> startServer() {
        log.info("启动服务器");
        
        Map<String, Object> result = new HashMap<>();
        
        if (nettyServerEngine.isRunning()) {
            result.put("success", false);
            result.put("message", "服务器已经在运行");
        } else {
            try {
                nettyServerEngine.start();
                result.put("success", true);
                result.put("message", "服务器启动成功");
            } catch (Exception e) {
                log.error("启动服务器失败", e);
                result.put("success", false);
                result.put("message", "服务器启动失败: " + e.getMessage());
            }
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 停止服务器
     * 
     * @return 操作结果
     */
    @PostMapping("/server/stop")
    public ResponseEntity<Map<String, Object>> stopServer() {
        log.info("停止服务器");
        
        Map<String, Object> result = new HashMap<>();
        
        if (!nettyServerEngine.isRunning()) {
            result.put("success", false);
            result.put("message", "服务器未在运行");
        } else {
            try {
                nettyServerEngine.stop();
                result.put("success", true);
                result.put("message", "服务器停止成功");
            } catch (Exception e) {
                log.error("停止服务器失败", e);
                result.put("success", false);
                result.put("message", "服务器停止失败: " + e.getMessage());
            }
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 重启服务器
     * 
     * @return 操作结果
     */
    @PostMapping("/server/restart")
    public ResponseEntity<Map<String, Object>> restartServer() {
        log.info("重启服务器");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (nettyServerEngine.isRunning()) {
                nettyServerEngine.stop();
            }
            
            // 等待一段时间确保完全停止
            Thread.sleep(1000);
            
            nettyServerEngine.start();
            
            result.put("success", true);
            result.put("message", "服务器重启成功");
            
        } catch (Exception e) {
            log.error("重启服务器失败", e);
            result.put("success", false);
            result.put("message", "服务器重启失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取服务器配置
     * 
     * @return 服务器配置信息
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getServerConfig() {
        log.info("获取服务器配置");
        
        Map<String, Object> config = new HashMap<>();
        config.put("port", nettyServerEngine.getPort());
        config.put("maxConnections", nettyServerEngine.getMaxConnections());
        config.put("readTimeout", nettyServerEngine.getReadTimeout());
        config.put("writeTimeout", nettyServerEngine.getWriteTimeout());
        config.put("maxPlayerCount", userEngine.getMaxPlayerCount());
        
        return ResponseEntity.ok(config);
    }
    
    /**
     * 健康检查
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("serverRunning", nettyServerEngine.isRunning());
        
        return ResponseEntity.ok(health);
    }
} 