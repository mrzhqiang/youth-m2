package com.mir2.network;

import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 客户端会话类
 * 
 * <p>代表一个客户端连接的会话，包含连接信息、状态管理和消息收发功能。
 * 每个连接到服务器的客户端都对应一个ClientSession对象。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>管理客户端连接状态</li>
 *   <li>存储会话相关信息</li>
 *   <li>提供消息发送接口</li>
 *   <li>统计会话流量信息</li>
 *   <li>处理会话生命周期</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Slf4j
public class ClientSession {
    
    /** 会话ID */
    private final String sessionId;
    
    /** 网络通道 */
    private final Channel channel;
    
    /** 网络引擎引用 */
    private final NettyServerEngine networkEngine;
    
    /** 会话状态 */
    private final AtomicBoolean active = new AtomicBoolean(true);
    
    /** 创建时间 */
    private final long createTime;
    
    /** 最后活跃时间 */
    private volatile long lastActiveTime;
    
    /** 发送消息计数 */
    private final AtomicLong sentMessages = new AtomicLong(0);
    
    /** 接收消息计数 */
    private final AtomicLong receivedMessages = new AtomicLong(0);
    
    /** 发送字节数 */
    private final AtomicLong sentBytes = new AtomicLong(0);
    
    /** 接收字节数 */
    private final AtomicLong receivedBytes = new AtomicLong(0);
    
    /** 关联的玩家ID */
    private volatile Long playerId;
    
    /** 关联的账号名 */
    private volatile String accountName;
    
    /** 关联的角色名 */
    private volatile String characterName;
    
    /** 客户端IP地址 */
    private final String clientIp;
    
    /** 客户端端口 */
    private final int clientPort;
    
    /** 会话属性（用于存储自定义数据） */
    private final java.util.Map<String, Object> attributes = new java.util.concurrent.ConcurrentHashMap<>();
    
    /**
     * 构造函数
     * 
     * @param sessionId 会话ID
     * @param channel 网络通道
     * @param networkEngine 网络引擎
     */
    public ClientSession(String sessionId, Channel channel, NettyServerEngine networkEngine) {
        this.sessionId = sessionId;
        this.channel = channel;
        this.networkEngine = networkEngine;
        this.createTime = System.currentTimeMillis();
        this.lastActiveTime = createTime;
        
        // 获取客户端地址信息
        InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
        this.clientIp = remoteAddress.getAddress().getHostAddress();
        this.clientPort = remoteAddress.getPort();
        
        log.debug("创建客户端会话 [ID: {}, 地址: {}:{}]", sessionId, clientIp, clientPort);
    }
    
    /**
     * 检查会话是否活跃
     * 
     * @return 是否活跃
     */
    public boolean isActive() {
        return active.get() && channel.isActive();
    }
    
    /**
     * 发送消息
     * 
     * @param message 消息对象
     */
    public void sendMessage(GameMessage message) {
        if (!isActive() || message == null) {
            return;
        }
        
        try {
            channel.writeAndFlush(message);
            sentMessages.incrementAndGet();
            sentBytes.addAndGet(message.getLength());
            updateLastActiveTime();
            
            log.debug("会话 {} 发送消息 [类型: {}, 长度: {}]", 
                    sessionId, message.getType(), message.getLength());
            
        } catch (Exception e) {
            log.error("会话 {} 发送消息失败", sessionId, e);
        }
    }
    
    /**
     * 处理接收到的消息
     * 
     * @param message 消息对象
     */
    public void handleReceivedMessage(GameMessage message) {
        if (message == null) {
            return;
        }
        
        receivedMessages.incrementAndGet();
        receivedBytes.addAndGet(message.getLength());
        updateLastActiveTime();
        
        log.debug("会话 {} 接收消息 [类型: {}, 长度: {}]", 
                sessionId, message.getType(), message.getLength());
    }
    
    /**
     * 关闭会话
     */
    public void close() {
        if (active.compareAndSet(true, false)) {
            log.info("关闭客户端会话 [ID: {}, 地址: {}:{}, 存活时间: {}ms]", 
                    sessionId, clientIp, clientPort, System.currentTimeMillis() - createTime);
            
            try {
                if (channel.isActive()) {
                    channel.close();
                }
            } catch (Exception e) {
                log.error("关闭会话通道失败 [ID: {}]", sessionId, e);
            }
            
            // 从网络引擎中移除
            if (networkEngine != null) {
                networkEngine.removeSession(sessionId);
            }
        }
    }
    
    /**
     * 更新最后活跃时间
     */
    public void updateLastActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }
    
    /**
     * 获取会话存活时间（毫秒）
     * 
     * @return 存活时间
     */
    public long getAliveTime() {
        return System.currentTimeMillis() - createTime;
    }
    
    /**
     * 获取空闲时间（毫秒）
     * 
     * @return 空闲时间
     */
    public long getIdleTime() {
        return System.currentTimeMillis() - lastActiveTime;
    }
    
    /**
     * 检查会话是否超时
     * 
     * @param timeoutMs 超时时间（毫秒）
     * @return 是否超时
     */
    public boolean isTimeout(long timeoutMs) {
        return getIdleTime() > timeoutMs;
    }
    
    /**
     * 设置会话属性
     * 
     * @param key 属性键
     * @param value 属性值
     */
    public void setAttribute(String key, Object value) {
        if (key != null) {
            attributes.put(key, value);
        }
    }
    
    /**
     * 获取会话属性
     * 
     * @param key 属性键
     * @return 属性值
     */
    public Object getAttribute(String key) {
        return key != null ? attributes.get(key) : null;
    }
    
    /**
     * 获取会话属性（带默认值）
     * 
     * @param key 属性键
     * @param defaultValue 默认值
     * @param <T> 属性类型
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, T defaultValue) {
        Object value = getAttribute(key);
        return value != null ? (T) value : defaultValue;
    }
    
    /**
     * 移除会话属性
     * 
     * @param key 属性键
     * @return 被移除的属性值
     */
    public Object removeAttribute(String key) {
        return key != null ? attributes.remove(key) : null;
    }
    
    /**
     * 检查是否包含指定属性
     * 
     * @param key 属性键
     * @return 是否包含
     */
    public boolean hasAttribute(String key) {
        return key != null && attributes.containsKey(key);
    }
    
    /**
     * 获取会话统计信息
     * 
     * @return 统计信息字符串
     */
    public String getStatistics() {
        return String.format("会话统计[ID: %s, 存活: %dms, 空闲: %dms, 发送: %d/%dB, 接收: %d/%dB]",
                sessionId, getAliveTime(), getIdleTime(), 
                sentMessages.get(), sentBytes.get(), 
                receivedMessages.get(), receivedBytes.get());
    }
    
    /**
     * 绑定玩家
     * 
     * @param playerId 玩家ID
     * @param accountName 账号名
     * @param characterName 角色名
     */
    public void bindPlayer(Long playerId, String accountName, String characterName) {
        this.playerId = playerId;
        this.accountName = accountName;
        this.characterName = characterName;
        
        log.info("会话 {} 绑定玩家 [ID: {}, 账号: {}, 角色: {}]", 
                sessionId, playerId, accountName, characterName);
    }
    
    /**
     * 解绑玩家
     */
    public void unbindPlayer() {
        if (playerId != null) {
            log.info("会话 {} 解绑玩家 [ID: {}, 账号: {}, 角色: {}]", 
                    sessionId, playerId, accountName, characterName);
        }
        
        this.playerId = null;
        this.accountName = null;
        this.characterName = null;
    }
    
    /**
     * 检查是否已绑定玩家
     * 
     * @return 是否已绑定
     */
    public boolean isPlayerBound() {
        return playerId != null;
    }
    
    /**
     * 获取远程地址字符串
     * 
     * @return 远程地址
     */
    public String getRemoteAddress() {
        return clientIp + ":" + clientPort;
    }
    
    @Override
    public String toString() {
        return String.format("ClientSession[ID: %s, 地址: %s, 活跃: %s, 玩家: %s]",
                sessionId, getRemoteAddress(), isActive(), 
                isPlayerBound() ? characterName : "未绑定");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientSession that = (ClientSession) o;
        return sessionId.equals(that.sessionId);
    }
    
    @Override
    public int hashCode() {
        return sessionId.hashCode();
    }
} 