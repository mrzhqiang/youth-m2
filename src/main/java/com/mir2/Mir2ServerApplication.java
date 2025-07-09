package com.mir2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 传奇游戏服务器主启动类
 * 
 * <p>这是游戏服务器的入口点，负责启动Spring Boot应用程序
 * 并初始化所有必要的组件和服务。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>启动Spring Boot应用程序</li>
 *   <li>初始化游戏引擎</li>
 *   <li>启动网络服务</li>
 *   <li>加载游戏数据</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class Mir2ServerApplication {
    
    /**
     * 程序入口点
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 设置系统属性
        System.setProperty("java.awt.headless", "true");
        System.setProperty("file.encoding", "UTF-8");
        
        try {
            // 打印启动信息
            printStartupInfo();
            
            // 启动Spring Boot应用程序
            ConfigurableApplicationContext context = SpringApplication.run(Mir2ServerApplication.class, args);
            
            // 打印启动完成信息
            printStartupComplete(context);
            
        } catch (Exception e) {
            log.error("游戏服务器启动失败", e);
            System.exit(1);
        }
    }
    
    /**
     * 打印启动信息
     */
    private static void printStartupInfo() {
        log.info("=====================================================");
        log.info("        传奇游戏服务器 - Java Spring Boot 实现");
        log.info("=====================================================");
        log.info("版本: 1.0.0");
        log.info("启动时间: {}", java.time.LocalDateTime.now());
        log.info("Java版本: {}", System.getProperty("java.version"));
        log.info("操作系统: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
        log.info("=====================================================");
    }
    
    /**
     * 打印启动完成信息
     * 
     * @param context Spring应用程序上下文
     */
    private static void printStartupComplete(ConfigurableApplicationContext context) {
        log.info("=====================================================");
        log.info("        游戏服务器启动完成！");
        log.info("=====================================================");
        log.info("应用程序名称: {}", context.getEnvironment().getProperty("spring.application.name"));
        log.info("服务器端口: {}", context.getEnvironment().getProperty("server.port"));
        log.info("游戏端口: {}", context.getEnvironment().getProperty("game.server.network.port"));
        log.info("最大玩家数: {}", context.getEnvironment().getProperty("game.server.max-players"));
        log.info("=====================================================");
    }
} 