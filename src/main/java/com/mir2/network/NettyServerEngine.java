package com.mir2.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于Netty的网络通信引擎
 * 
 * <p>负责处理客户端的网络连接、消息传输和会话管理。
 * 对应原M2Engine中的RunSock模块，提供高性能的网络通信服务。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>客户端连接管理</li>
 *   <li>消息编解码和传输</li>
 *   <li>连接状态监控</li>
 *   <li>心跳检测和超时处理</li>
 *   <li>消息队列管理</li>
 *   <li>网络流量统计</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class NettyServerEngine {
    
    /** 服务器启动器 */
    private ServerBootstrap serverBootstrap;
    
    /** Boss线程组（用于接受客户端连接） */
    private EventLoopGroup bossGroup;
    
    /** Worker线程组（用于处理客户端IO） */
    private EventLoopGroup workerGroup;
    
    /** 服务器通道 */
    private Channel serverChannel;
    
    /** 客户端会话映射 */
    private final Map<String, ClientSession> sessions = new ConcurrentHashMap<>();
    
    /** 引擎运行状态 */
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    /** 连接计数器 */
    private final AtomicLong connectionCount = new AtomicLong(0);
    
    /** 消息计数器 */
    private final AtomicLong messageCount = new AtomicLong(0);
    
    /** 流量统计（字节） */
    private final AtomicLong bytesReceived = new AtomicLong(0);
    private final AtomicLong bytesSent = new AtomicLong(0);
    
    /** 消息处理器 */
    @Autowired
    private GameMessageHandler messageHandler;
    
    /** 网络统计 */
    @Autowired
    private NetworkStatistics networkStatistics;
    
    /** 服务器启动时间 */
    private volatile long startTime = 0;
    
    /** 配置参数 */
    @Value("${game.server.network.port:7000}")
    private int serverPort;
    
    @Value("${game.server.network.boss-threads:1}")
    private int bossThreads;
    
    @Value("${game.server.network.worker-threads:4}")
    private int workerThreads;
    
    @Value("${game.server.network.max-connections:1000}")
    private int maxConnections;
    
    @Value("${game.server.network.read-timeout:30}")
    private int readTimeout;
    
    @Value("${game.server.network.write-timeout:10}")
    private int writeTimeout;
    
    @Value("${game.server.network.heartbeat-interval:60}")
    private int heartbeatInterval;
    
    /**
     * 初始化网络引擎
     */
    @PostConstruct
    public void initialize() {
        log.info("正在初始化网络通信引擎...");
        
        // 创建线程组
        bossGroup = new NioEventLoopGroup(bossThreads);
        workerGroup = new NioEventLoopGroup(workerThreads);
        
        // 配置服务器启动器
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_RCVBUF, 32 * 1024)
                .childOption(ChannelOption.SO_SNDBUF, 32 * 1024)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        initializeChannelPipeline(ch);
                    }
                });
        
        log.info("网络通信引擎初始化完成 [端口: {}, Boss线程: {}, Worker线程: {}, 最大连接: {}]",
                serverPort, bossThreads, workerThreads, maxConnections);
    }
    
    /**
     * 启动网络服务
     */
    public void start() {
        if (running.get()) {
            log.warn("网络服务已经在运行中");
            return;
        }
        
        try {
            log.info("正在启动网络服务，端口: {}", serverPort);
            
            // 绑定端口并启动服务器
            ChannelFuture future = serverBootstrap.bind(serverPort).sync();
            serverChannel = future.channel();
            
            running.set(true);
            
            log.info("网络服务启动成功，监听端口: {}", serverPort);
            
            // 记录启动时间
            startTime = System.currentTimeMillis();
            
            // 启动统计任务
            startStatisticsTask();
            
        } catch (Exception e) {
            log.error("启动网络服务失败", e);
            throw new RuntimeException("网络服务启动失败", e);
        }
    }
    
    /**
     * 停止网络服务
     */
    @PreDestroy
    public void stop() {
        if (!running.get()) {
            return;
        }
        
        log.info("正在停止网络服务...");
        
        running.set(false);
        
        // 关闭所有客户端连接
        closeAllSessions();
        
        // 关闭服务器通道
        if (serverChannel != null) {
            try {
                serverChannel.close().sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // 关闭线程组
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        
        log.info("网络服务已停止 [总连接数: {}, 总消息数: {}, 接收字节: {}, 发送字节: {}]",
                connectionCount.get(), messageCount.get(), bytesReceived.get(), bytesSent.get());
    }
    
    /**
     * 初始化通道管道
     * 
     * @param channel 客户端通道
     */
    private void initializeChannelPipeline(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        
        // 添加空闲状态处理器（心跳检测）
        pipeline.addLast("idleStateHandler", 
                new IdleStateHandler(readTimeout, writeTimeout, heartbeatInterval, TimeUnit.SECONDS));
        
        // 添加长度字段解码器（解决TCP粘包问题）
        pipeline.addLast("frameDecoder", 
                new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 4));
        
        // 添加长度字段编码器
        pipeline.addLast("frameEncoder", 
                new LengthFieldPrepender(4));
        
        // 添加自定义消息编解码器
        pipeline.addLast("messageDecoder", new GameMessageDecoder());
        pipeline.addLast("messageEncoder", new GameMessageEncoder());
        
        // 添加游戏消息处理器
        pipeline.addLast("gameHandler", new GameChannelHandler(this));
    }
    
    /**
     * 创建客户端会话
     * 
     * @param channel 客户端通道
     * @return 会话对象
     */
    public ClientSession createSession(Channel channel) {
        // 检查连接数限制
        if (sessions.size() >= maxConnections) {
            log.warn("达到最大连接数限制: {}", maxConnections);
            channel.close();
            return null;
        }
        
        String sessionId = generateSessionId(channel);
        ClientSession session = new ClientSession(sessionId, channel, this);
        
        sessions.put(sessionId, session);
        connectionCount.incrementAndGet();
        
        log.info("客户端连接建立 [会话ID: {}, 远程地址: {}, 当前连接数: {}]",
                sessionId, channel.remoteAddress(), sessions.size());
        
        return session;
    }
    
    /**
     * 移除客户端会话
     * 
     * @param sessionId 会话ID
     */
    public void removeSession(String sessionId) {
        ClientSession session = sessions.remove(sessionId);
        if (session != null) {
            log.info("客户端连接断开 [会话ID: {}, 当前连接数: {}]", sessionId, sessions.size());
            
            // 通知消息处理器
            if (messageHandler != null) {
                messageHandler.onSessionClosed(session);
            }
        }
    }
    
    /**
     * 获取客户端会话
     * 
     * @param sessionId 会话ID
     * @return 会话对象
     */
    public ClientSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    /**
     * 处理接收到的消息
     * 
     * @param session 客户端会话
     * @param message 消息对象
     */
    public void handleMessage(ClientSession session, GameMessage message) {
        if (session == null || message == null) {
            return;
        }
        
        messageCount.incrementAndGet();
        bytesReceived.addAndGet(message.getLength());
        
        log.debug("接收消息 [会话: {}, 类型: {}, 长度: {}]", 
                session.getSessionId(), message.getType(), message.getLength());
        
        // 委托给消息处理器
        if (messageHandler != null) {
            try {
                messageHandler.handleMessage(session, message);
            } catch (Exception e) {
                log.error("处理消息时发生异常 [会话: {}, 消息类型: {}]", 
                        session.getSessionId(), message.getType(), e);
            }
        }
    }
    
    /**
     * 发送消息给客户端
     * 
     * @param session 客户端会话
     * @param message 消息对象
     */
    public void sendMessage(ClientSession session, GameMessage message) {
        if (session == null || message == null || !session.isActive()) {
            return;
        }
        
        try {
            session.getChannel().writeAndFlush(message);
            bytesSent.addAndGet(message.getLength());
            
            log.debug("发送消息 [会话: {}, 类型: {}, 长度: {}]", 
                    session.getSessionId(), message.getType(), message.getLength());
            
        } catch (Exception e) {
            log.error("发送消息失败 [会话: {}, 消息类型: {}]", 
                    session.getSessionId(), message.getType(), e);
        }
    }
    
    /**
     * 广播消息给所有在线客户端
     * 
     * @param message 消息对象
     */
    public void broadcastMessage(GameMessage message) {
        if (message == null) {
            return;
        }
        
        log.info("广播消息 [类型: {}, 接收者数量: {}]", message.getType(), sessions.size());
        
        sessions.values().forEach(session -> {
            sendMessage(session, message);
        });
    }
    
    /**
     * 踢出客户端
     * 
     * @param sessionId 会话ID
     * @param reason 踢出原因
     */
    public void kickSession(String sessionId, String reason) {
        ClientSession session = sessions.get(sessionId);
        if (session != null) {
            log.info("踢出客户端 [会话: {}, 原因: {}]", sessionId, reason);
            session.close();
        }
    }
    
    /**
     * 关闭所有客户端会话
     */
    private void closeAllSessions() {
        log.info("正在关闭所有客户端连接...");
        
        sessions.values().forEach(session -> {
            try {
                session.close();
            } catch (Exception e) {
                log.error("关闭会话失败 [会话ID: {}]", session.getSessionId(), e);
            }
        });
        
        sessions.clear();
        log.info("所有客户端连接已关闭");
    }
    
    /**
     * 启动统计任务
     */
    private void startStatisticsTask() {
        // 使用worker线程组执行统计任务
        workerGroup.scheduleAtFixedRate(() -> {
            if (running.get()) {
                logNetworkStatistics();
            }
        }, 60, 60, TimeUnit.SECONDS);
        
        log.debug("网络统计任务已启动");
    }
    
    /**
     * 记录网络统计信息
     */
    private void logNetworkStatistics() {
        long connections = connectionCount.get();
        long messages = messageCount.get();
        long received = bytesReceived.get();
        long sent = bytesSent.get();
        int activeSessions = sessions.size();
        
        log.info("网络统计 [活跃连接: {}, 总连接: {}, 总消息: {}, 接收: {}KB, 发送: {}KB]",
                activeSessions, connections, messages, received / 1024, sent / 1024);
    }
    
    /**
     * 生成会话ID
     * 
     * @param channel 客户端通道
     * @return 会话ID
     */
    private String generateSessionId(Channel channel) {
        return "SESSION_" + channel.id().asShortText() + "_" + System.currentTimeMillis();
    }
    
    /**
     * 获取网络引擎状态
     * 
     * @return 状态信息
     */
    public String getEngineStatus() {
        return String.format("NettyServerEngine[运行中: %s, 活跃连接: %d/%d, 总消息: %d]",
                running.get(), sessions.size(), maxConnections, messageCount.get());
    }
    
    /**
     * 获取连接统计信息
     * 
     * @return 连接统计
     */
    public NetworkStatistics getNetworkStatistics() {
        return NetworkStatistics.builder()
                .activeConnections(sessions.size())
                .totalConnections(connectionCount.get())
                .totalMessages(messageCount.get())
                .bytesReceived(bytesReceived.get())
                .bytesSent(bytesSent.get())
                .maxConnections(maxConnections)
                .build();
    }
    
    /**
     * 检查引擎是否运行中
     * 
     * @return 是否运行中
     */
    public boolean isRunning() {
        return running.get();
    }
    
    /**
     * 获取服务器端口
     * 
     * @return 端口号
     */
    public int getPort() {
        return serverPort;
    }
    
    /**
     * 获取最大连接数
     * 
     * @return 最大连接数
     */
    public int getMaxConnections() {
        return maxConnections;
    }
    
    /**
     * 获取读超时时间
     * 
     * @return 读超时时间（秒）
     */
    public int getReadTimeout() {
        return readTimeout;
    }
    
    /**
     * 获取写超时时间
     * 
     * @return 写超时时间（秒）
     */
    public int getWriteTimeout() {
        return writeTimeout;
    }
    
    /**
     * 获取启动时间
     * 
     * @return 启动时间戳
     */
    public long getStartTime() {
        return startTime;
    }
} 