package com.mir2.network;

import io.netty.util.AttributeKey;

/**
 * 会话属性键常量
 * 
 * <p>定义用于在Netty通道中存储会话相关信息的AttributeKey常量。
 * 这些键用于在通道生命周期内关联和检索会话数据。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public final class SessionAttributeKeys {
    
    /** 客户端会话属性键 */
    public static final AttributeKey<ClientSession> CLIENT_SESSION = AttributeKey.valueOf("CLIENT_SESSION");
    
    /** 玩家对象属性键 */
    public static final AttributeKey<com.mir2.core.model.Player> PLAYER = AttributeKey.valueOf("PLAYER");
    
    /** 认证状态属性键 */
    public static final AttributeKey<Boolean> AUTHENTICATED = AttributeKey.valueOf("AUTHENTICATED");
    
    /** 会话创建时间属性键 */
    public static final AttributeKey<Long> SESSION_CREATE_TIME = AttributeKey.valueOf("SESSION_CREATE_TIME");
    
    /** 最后活跃时间属性键 */
    public static final AttributeKey<Long> LAST_ACTIVITY_TIME = AttributeKey.valueOf("LAST_ACTIVITY_TIME");
    
    /** 客户端版本属性键 */
    public static final AttributeKey<String> CLIENT_VERSION = AttributeKey.valueOf("CLIENT_VERSION");
    
    /** 反外挂标记属性键 */
    public static final AttributeKey<Boolean> ANTI_CHEAT_FLAG = AttributeKey.valueOf("ANTI_CHEAT_FLAG");
    
    /** 私有构造函数，防止实例化 */
    private SessionAttributeKeys() {
        throw new UnsupportedOperationException("工具类不能实例化");
    }
} 