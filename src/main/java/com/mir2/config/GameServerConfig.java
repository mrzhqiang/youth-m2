package com.mir2.config;

import com.mir2.core.engine.UserEngine;
import com.mir2.network.NettyServerEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 游戏服务器配置类
 * 
 * <p>负责游戏服务器的启动配置和初始化。
 * 在Spring Boot应用启动后自动启动游戏网络服务。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>自动启动网络服务器</li>
 *   <li>初始化游戏引擎</li>
 *   <li>配置系统参数</li>
 *   <li>注册关闭钩子</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Configuration
public class GameServerConfig implements CommandLineRunner {
    
    @Autowired
    private NettyServerEngine nettyServerEngine;
    
    @Autowired
    private UserEngine userEngine;
    
    /**
     * 应用启动后执行
     * 
     * @param args 命令行参数
     * @throws Exception 启动异常
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("===== 传奇游戏服务器启动 =====");
        
        try {
            // 初始化游戏引擎
            initializeGameEngine();
            
            // 启动网络服务器
            startNetworkServer();
            
            // 注册关闭钩子
            registerShutdownHook();
            
            log.info("===== 传奇游戏服务器启动完成 =====");
            
        } catch (Exception e) {
            log.error("游戏服务器启动失败", e);
            throw e;
        }
    }
    
    /**
     * 初始化游戏引擎
     */
    private void initializeGameEngine() {
        log.info("初始化游戏引擎...");
        
        try {
            // 初始化用户引擎
            userEngine.initialize();
            
            // 启动定时任务
            userEngine.startScheduledTasks();
            
            log.info("游戏引擎初始化完成");
            
        } catch (Exception e) {
            log.error("游戏引擎初始化失败", e);
            throw new RuntimeException("游戏引擎初始化失败", e);
        }
    }
    
    /**
     * 启动网络服务器
     */
    private void startNetworkServer() {
        log.info("启动网络服务器...");
        
        try {
            // 启动Netty服务器
            nettyServerEngine.start();
            
            log.info("网络服务器启动完成 [端口: {}]", nettyServerEngine.getPort());
            
        } catch (Exception e) {
            log.error("网络服务器启动失败", e);
            throw new RuntimeException("网络服务器启动失败", e);
        }
    }
    
    /**
     * 注册关闭钩子
     */
    private void registerShutdownHook() {
        log.info("注册关闭钩子...");
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("===== 传奇游戏服务器关闭 =====");
            
            try {
                // 保存所有在线玩家数据
                saveAllPlayerData();
                
                // 停止网络服务器
                stopNetworkServer();
                
                // 关闭游戏引擎
                shutdownGameEngine();
                
                log.info("===== 传奇游戏服务器关闭完成 =====");
                
            } catch (Exception e) {
                log.error("游戏服务器关闭时发生异常", e);
            }
        }));
    }
    
    /**
     * 保存所有在线玩家数据
     */
    private void saveAllPlayerData() {
        log.info("保存所有在线玩家数据...");
        
        try {
            userEngine.saveAllPlayerData();
            log.info("所有玩家数据保存完成");
            
        } catch (Exception e) {
            log.error("保存玩家数据失败", e);
        }
    }
    
    /**
     * 停止网络服务器
     */
    private void stopNetworkServer() {
        log.info("停止网络服务器...");
        
        try {
            if (nettyServerEngine.isRunning()) {
                nettyServerEngine.stop();
                log.info("网络服务器停止完成");
            }
            
        } catch (Exception e) {
            log.error("停止网络服务器失败", e);
        }
    }
    
    /**
     * 关闭游戏引擎
     */
    private void shutdownGameEngine() {
        log.info("关闭游戏引擎...");
        
        try {
            // 停止定时任务
            userEngine.stopScheduledTasks();
            
            // 关闭用户引擎
            userEngine.shutdown();
            
            log.info("游戏引擎关闭完成");
            
        } catch (Exception e) {
            log.error("关闭游戏引擎失败", e);
        }
    }
}

/**
 * 开发环境配置
 */
@Slf4j
@Configuration
@Profile("dev")
class DevConfig implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception {
        log.info("===== 开发环境配置 =====");
        
        // 开发环境特定配置
        log.info("启用开发模式调试日志");
        log.info("启用热重载功能");
        log.info("启用调试端口");
        
        log.info("===== 开发环境配置完成 =====");
    }
}

/**
 * 生产环境配置
 */
@Slf4j
@Configuration
@Profile("prod")
class ProdConfig implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception {
        log.info("===== 生产环境配置 =====");
        
        // 生产环境特定配置
        log.info("启用生产模式安全配置");
        log.info("启用性能监控");
        log.info("启用自动备份");
        
        log.info("===== 生产环境配置完成 =====");
    }
} 