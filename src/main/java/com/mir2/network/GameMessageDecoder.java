package com.mir2.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 游戏消息解码器
 * 
 * <p>负责将从客户端接收到的字节流解码为GameMessage对象。
 * 处理传奇游戏特有的消息格式和协议。</p>
 * 
 * <p>消息格式：</p>
 * <ul>
 *   <li>消息类型：2字节</li>
 *   <li>消息序号：4字节</li>
 *   <li>消息标志：2字节</li>
 *   <li>消息数据：变长</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
public class GameMessageDecoder extends ByteToMessageDecoder {
    
    /** 消息头最小长度 */
    private static final int MIN_MESSAGE_LENGTH = 8; // 2 + 4 + 2
    
    /** 最大消息长度 */
    private static final int MAX_MESSAGE_LENGTH = 65536;
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 检查是否有足够的字节来读取消息头
        if (in.readableBytes() < MIN_MESSAGE_LENGTH) {
            return;
        }
        
        // 标记当前读取位置
        in.markReaderIndex();
        
        try {
            // 读取消息类型
            short messageType = in.readShort();
            
            // 读取消息序号
            int sequence = in.readInt();
            
            // 读取消息标志
            short flags = in.readShort();
            
            // 计算数据长度
            int dataLength = in.readableBytes();
            
            // 检查消息长度是否合理
            if (dataLength > MAX_MESSAGE_LENGTH - MIN_MESSAGE_LENGTH) {
                log.warn("消息长度过大: {} 字节，最大允许: {} 字节", 
                        dataLength, MAX_MESSAGE_LENGTH - MIN_MESSAGE_LENGTH);
                ctx.close();
                return;
            }
            
            // 读取消息数据
            byte[] data = new byte[dataLength];
            if (dataLength > 0) {
                in.readBytes(data);
            }
            
            // 创建GameMessage对象
            GameMessage message = new GameMessage(messageType, sequence, flags, data);
            
            log.debug("解码消息成功 [类型: {}, 序号: {}, 标志: {}, 数据长度: {}]", 
                    messageType, sequence, flags, dataLength);
            
            // 添加到输出列表
            out.add(message);
            
        } catch (Exception e) {
            log.error("消息解码失败", e);
            
            // 重置读取位置
            in.resetReaderIndex();
            
            // 跳过损坏的数据
            in.skipBytes(in.readableBytes());
            
            // 关闭连接
            ctx.close();
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("消息解码器发生异常 [远程地址: {}]", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }
} 