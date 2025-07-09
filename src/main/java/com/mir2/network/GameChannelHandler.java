package com.mir2.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 游戏通道处理器
 * 
 * <p>处理客户端连接的生命周期事件，包括连接建立、断开、消息接收等。
 * 这是Netty网络层和游戏逻辑层的桥梁。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>处理客户端连接和断开事件</li>
 *   <li>接收和转发游戏消息</li>
 *   <li>处理心跳超时</li>
 *   <li>异常处理和连接清理</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
public class GameChannelHandler extends ChannelInboundHandlerAdapter {
    
    /** 网络引擎引用 */
    private final NettyServerEngine networkEngine;
    
    /** 客户端会话 */
    private ClientSession clientSession;
    
    /**
     * 构造函数
     * 
     * @param networkEngine 网络引擎
     */
    public GameChannelHandler(NettyServerEngine networkEngine) {
        this.networkEngine = networkEngine;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端连接建立 [远程地址: {}]", ctx.channel().remoteAddress());
        
        try {
            // 创建客户端会话
            clientSession = networkEngine.createSession(ctx.channel());
            
            if (clientSession != null) {
                // 将会话绑定到通道
                ctx.channel().attr(SessionAttributeKeys.CLIENT_SESSION).set(clientSession);
                
                log.debug("客户端会话创建成功 [会话ID: {}]", clientSession.getSessionId());
            } else {
                log.warn("客户端会话创建失败，关闭连接 [远程地址: {}]", ctx.channel().remoteAddress());
                ctx.close();
            }
            
        } catch (Exception e) {
            log.error("处理客户端连接时发生异常", e);
            ctx.close();
        }
        
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端连接断开 [远程地址: {}]", ctx.channel().remoteAddress());
        
        try {
            // 获取客户端会话
            ClientSession session = ctx.channel().attr(SessionAttributeKeys.CLIENT_SESSION).get();
            
            if (session != null) {
                log.debug("清理客户端会话 [会话ID: {}]", session.getSessionId());
                
                // 解绑玩家（如果有）
                session.unbindPlayer();
                
                // 从网络引擎中移除会话
                networkEngine.removeSession(session.getSessionId());
            }
            
        } catch (Exception e) {
            log.error("处理客户端断开时发生异常", e);
        }
        
        super.channelInactive(ctx);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof GameMessage)) {
            log.warn("接收到非游戏消息类型: {}", msg.getClass().getSimpleName());
            return;
        }
        
        GameMessage message = (GameMessage) msg;
        ClientSession session = ctx.channel().attr(SessionAttributeKeys.CLIENT_SESSION).get();
        
        if (session == null) {
            log.warn("接收到消息但会话不存在，丢弃消息 [类型: {}]", message.getType());
            return;
        }
        
        try {
            // 更新会话活跃时间
            session.handleReceivedMessage(message);
            
            // 委托给网络引擎处理
            networkEngine.handleMessage(session, message);
            
        } catch (Exception e) {
            log.error("处理接收消息时发生异常 [会话: {}, 消息类型: {}]", 
                    session.getSessionId(), message.getType(), e);
        }
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleEvent = (IdleStateEvent) evt;
            
            if (idleEvent.state() == IdleState.READER_IDLE) {
                // 读超时 - 客户端可能已断开
                log.info("客户端读超时，关闭连接 [远程地址: {}]", ctx.channel().remoteAddress());
                ctx.close();
                
            } else if (idleEvent.state() == IdleState.WRITER_IDLE) {
                // 写超时 - 发送心跳包
                sendHeartbeat(ctx);
                
            } else if (idleEvent.state() == IdleState.ALL_IDLE) {
                // 读写都超时 - 关闭连接
                log.info("客户端读写超时，关闭连接 [远程地址: {}]", ctx.channel().remoteAddress());
                ctx.close();
            }
        }
        
        super.userEventTriggered(ctx, evt);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("通道处理器发生异常 [远程地址: {}]", ctx.channel().remoteAddress(), cause);
        
        // 关闭连接
        ctx.close();
    }
    
    /**
     * 发送心跳包
     * 
     * @param ctx 通道上下文
     */
    private void sendHeartbeat(ChannelHandlerContext ctx) {
        try {
            GameMessage heartbeat = GameMessage.builder()
                    .type(GameMessageType.HEARTBEAT)
                    .sequence(0)
                    .build();
            
            ctx.writeAndFlush(heartbeat);
            
            log.debug("发送心跳包 [远程地址: {}]", ctx.channel().remoteAddress());
            
        } catch (Exception e) {
            log.error("发送心跳包失败", e);
            ctx.close();
        }
    }
} 