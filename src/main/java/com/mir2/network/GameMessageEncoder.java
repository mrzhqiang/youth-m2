package com.mir2.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 游戏消息编码器
 * 
 * <p>负责将GameMessage对象编码为字节流发送给客户端。
 * 处理传奇游戏特有的消息格式和协议。</p>
 * 
 * <p>编码格式：</p>
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
public class GameMessageEncoder extends MessageToByteEncoder<GameMessage> {
    
    @Override
    protected void encode(ChannelHandlerContext ctx, GameMessage msg, ByteBuf out) throws Exception {
        if (msg == null) {
            log.warn("尝试编码空消息");
            return;
        }
        
        try {
            // 写入消息类型
            out.writeShort(msg.getType());
            
            // 写入消息序号
            out.writeInt(msg.getSequence());
            
            // 写入消息标志
            out.writeShort(msg.getFlags());
            
            // 写入消息数据
            if (msg.getData() != null && msg.getData().length > 0) {
                out.writeBytes(msg.getData());
            }
            
            log.debug("编码消息成功 [类型: {}, 序号: {}, 标志: {}, 数据长度: {}]", 
                    msg.getType(), msg.getSequence(), msg.getFlags(), msg.getDataLength());
            
        } catch (Exception e) {
            log.error("消息编码失败 [消息: {}]", msg, e);
            throw e;
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("消息编码器发生异常 [远程地址: {}]", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }
} 