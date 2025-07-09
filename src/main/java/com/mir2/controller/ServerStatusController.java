package com.mir2.controller;

import com.mir2.core.engine.UserEngine;
import com.mir2.service.SystemInitializationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务器状态监控控制器
 * 
 * <p>提供服务器运行状态、在线玩家统计等信息的REST API。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/server")
public class ServerStatusController {
    
    @Autowired
    private UserEngine userEngine;
    
    /**
     * 服务器健康检查
     * 
     * @return 健康状态信息
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("status", "UP");
            response.put("timestamp", System.currentTimeMillis());
            response.put("version", "1.0.0");
            response.put("message", "游戏服务器运行正常");
            
            // 添加详细信息
            Map<String, Object> details = new HashMap<>();
            details.put("onlinePlayers", userEngine.getOnlinePlayerCount());
            details.put("maxPlayers", userEngine.getMaxPlayerCount());
            details.put("uptime", System.currentTimeMillis() - getStartTime());
            details.put("memoryUsage", getMemoryUsage());
            
            response.put("details", details);
            
        } catch (Exception e) {
            log.error("获取服务器健康状态失败", e);
            response.put("status", "DOWN");
            response.put("message", "服务器异常: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取服务器统计信息
     * 
     * @return 统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("success", true);
            response.put("timestamp", System.currentTimeMillis());
            
            // 玩家统计
            Map<String, Object> playerStats = new HashMap<>();
            playerStats.put("onlineCount", userEngine.getOnlinePlayerCount());
            playerStats.put("maxCount", userEngine.getMaxPlayerCount());
            playerStats.put("onlineRate", String.format("%.2f%%", 
                (double) userEngine.getOnlinePlayerCount() / userEngine.getMaxPlayerCount() * 100));
            
            response.put("players", playerStats);
            
            // 系统统计
            Map<String, Object> systemStats = new HashMap<>();
            systemStats.put("uptime", System.currentTimeMillis() - getStartTime());
            systemStats.put("memoryUsage", getMemoryUsage());
            systemStats.put("threadCount", Thread.activeCount());
            systemStats.put("javaVersion", System.getProperty("java.version"));
            systemStats.put("osName", System.getProperty("os.name"));
            
            response.put("system", systemStats);
            
            // 引擎状态
            response.put("engineStatus", userEngine.getEngineStatus());
            
        } catch (Exception e) {
            log.error("获取服务器统计信息失败", e);
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取在线玩家列表
     * 
     * @return 在线玩家列表
     */
    @GetMapping("/players")
    public Map<String, Object> getOnlinePlayers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("success", true);
            response.put("timestamp", System.currentTimeMillis());
            response.putAll(userEngine.getOnlinePlayerList());
            
        } catch (Exception e) {
            log.error("获取在线玩家列表失败", e);
            response.put("success", false);
            response.put("message", "获取玩家列表失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 踢出玩家
     * 
     * @param playerName 玩家名称
     * @return 操作结果
     */
    @GetMapping("/kick")
    public Map<String, Object> kickPlayer(String playerName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (playerName == null || playerName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "玩家名称不能为空");
                return response;
            }
            
            boolean success = userEngine.kickPlayer(playerName.trim());
            response.put("success", success);
            response.put("message", success ? "玩家已被踢出" : "玩家不存在或踢出失败");
            response.put("playerName", playerName);
            
        } catch (Exception e) {
            log.error("踢出玩家失败", e);
            response.put("success", false);
            response.put("message", "踢出玩家失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 广播系统消息
     * 
     * @param message 消息内容
     * @return 操作结果
     */
    @GetMapping("/broadcast")
    public Map<String, Object> broadcastMessage(String message) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "消息内容不能为空");
                return response;
            }
            
            boolean success = userEngine.broadcastSystemMessage(message.trim());
            response.put("success", success);
            response.put("message", success ? "消息广播成功" : "消息广播失败");
            response.put("content", message);
            
        } catch (Exception e) {
            log.error("广播消息失败", e);
            response.put("success", false);
            response.put("message", "广播消息失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 保存所有玩家数据
     * 
     * @return 操作结果
     */
    @GetMapping("/save")
    public Map<String, Object> saveAllPlayers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            userEngine.saveAllPlayerData();
            response.put("success", true);
            response.put("message", "所有玩家数据保存成功");
            response.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("保存玩家数据失败", e);
            response.put("success", false);
            response.put("message", "保存玩家数据失败: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取内存使用情况
     * 
     * @return 内存使用情况
     */
    private Map<String, Object> getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        memory.put("max", maxMemory);
        memory.put("total", totalMemory);
        memory.put("used", usedMemory);
        memory.put("free", freeMemory);
        memory.put("usagePercent", String.format("%.2f%%", (double) usedMemory / maxMemory * 100));
        
        return memory;
    }
    
    /**
     * 获取服务器启动时间
     * 
     * @return 启动时间戳
     */
    private long getStartTime() {
        // 这里应该从某个地方获取实际的启动时间
        // 临时返回当前时间减去1小时
        return System.currentTimeMillis() - 3600000;
    }
} 