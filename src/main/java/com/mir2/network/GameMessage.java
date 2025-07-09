package com.mir2.network;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 游戏消息类
 * 
 * <p>代表客户端和服务器之间传输的游戏消息，包含消息类型、数据内容等信息。
 * 支持自定义消息格式和序列化。</p>
 * 
 * <p>消息格式：</p>
 * <ul>
 *   <li>长度字段：4字节（已由Netty的LengthFieldPrepender处理）</li>
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
@Data
@Slf4j
public class GameMessage {
    
    /** 消息类型 */
    private short type;
    
    /** 消息序号 */
    private int sequence;
    
    /** 消息标志 */
    private short flags;
    
    /** 消息数据 */
    private byte[] data;
    
    /** 消息长度（不包括长度字段本身） */
    private int length;
    
    /** 创建时间 */
    private long createTime;
    
    /**
     * 默认构造函数
     */
    public GameMessage() {
        this.createTime = System.currentTimeMillis();
    }
    
    /**
     * 构造函数
     * 
     * @param type 消息类型
     * @param sequence 消息序号
     * @param data 消息数据
     */
    public GameMessage(short type, int sequence, byte[] data) {
        this();
        this.type = type;
        this.sequence = sequence;
        this.flags = 0;
        this.data = data != null ? data : new byte[0];
        this.length = calculateLength();
    }
    
    /**
     * 构造函数（带标志）
     * 
     * @param type 消息类型
     * @param sequence 消息序号
     * @param flags 消息标志
     * @param data 消息数据
     */
    public GameMessage(short type, int sequence, short flags, byte[] data) {
        this();
        this.type = type;
        this.sequence = sequence;
        this.flags = flags;
        this.data = data != null ? data : new byte[0];
        this.length = calculateLength();
    }
    
    /**
     * 设置消息数据
     * 
     * @param data 消息数据
     */
    public void setData(byte[] data) {
        this.data = data != null ? data : new byte[0];
        this.length = calculateLength();
    }
    
    /**
     * 计算消息长度
     * 
     * @return 消息长度
     */
    private int calculateLength() {
        // 消息类型(2) + 序号(4) + 标志(2) + 数据长度
        return 2 + 4 + 2 + (data != null ? data.length : 0);
    }
    
    /**
     * 获取数据长度
     * 
     * @return 数据长度
     */
    public int getDataLength() {
        return data != null ? data.length : 0;
    }
    
    /**
     * 检查是否有数据
     * 
     * @return 是否有数据
     */
    public boolean hasData() {
        return data != null && data.length > 0;
    }
    
    /**
     * 获取数据副本
     * 
     * @return 数据副本
     */
    public byte[] getDataCopy() {
        return data != null ? Arrays.copyOf(data, data.length) : new byte[0];
    }
    
    /**
     * 从字节数组读取字符串
     * 
     * @param offset 起始偏移量
     * @param length 字符串长度
     * @return 字符串
     */
    public String readString(int offset, int length) {
        if (data == null || offset < 0 || offset + length > data.length) {
            return "";
        }
        
        try {
            return new String(data, offset, length, "UTF-8").trim();
        } catch (Exception e) {
            log.warn("读取字符串失败 [offset: {}, length: {}]", offset, length, e);
            return "";
        }
    }
    
    /**
     * 从字节数组读取整数
     * 
     * @param offset 起始偏移量
     * @return 整数值
     */
    public int readInt(int offset) {
        if (data == null || offset < 0 || offset + 4 > data.length) {
            return 0;
        }
        
        return ((data[offset] & 0xFF) << 24) |
               ((data[offset + 1] & 0xFF) << 16) |
               ((data[offset + 2] & 0xFF) << 8) |
               (data[offset + 3] & 0xFF);
    }
    
    /**
     * 从字节数组读取短整数
     * 
     * @param offset 起始偏移量
     * @return 短整数值
     */
    public short readShort(int offset) {
        if (data == null || offset < 0 || offset + 2 > data.length) {
            return 0;
        }
        
        return (short) (((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF));
    }
    
    /**
     * 从字节数组读取字节
     * 
     * @param offset 起始偏移量
     * @return 字节值
     */
    public byte readByte(int offset) {
        if (data == null || offset < 0 || offset >= data.length) {
            return 0;
        }
        
        return data[offset];
    }
    
    /**
     * 创建消息构建器
     * 
     * @return 消息构建器
     */
    public static MessageBuilder builder() {
        return new MessageBuilder();
    }
    
    /**
     * 消息构建器
     */
    public static class MessageBuilder {
        private short type;
        private int sequence;
        private short flags;
        private byte[] data = new byte[0];
        
        public MessageBuilder type(short type) {
            this.type = type;
            return this;
        }
        
        public MessageBuilder sequence(int sequence) {
            this.sequence = sequence;
            return this;
        }
        
        public MessageBuilder flags(short flags) {
            this.flags = flags;
            return this;
        }
        
        public MessageBuilder data(byte[] data) {
            this.data = data != null ? data : new byte[0];
            return this;
        }
        
        public MessageBuilder writeString(String str) {
            if (str != null) {
                try {
                    byte[] strBytes = str.getBytes("UTF-8");
                    byte[] newData = new byte[data.length + strBytes.length];
                    System.arraycopy(data, 0, newData, 0, data.length);
                    System.arraycopy(strBytes, 0, newData, data.length, strBytes.length);
                    this.data = newData;
                } catch (Exception e) {
                    log.warn("写入字符串失败: {}", str, e);
                }
            }
            return this;
        }
        
        public MessageBuilder writeInt(int value) {
            byte[] newData = new byte[data.length + 4];
            System.arraycopy(data, 0, newData, 0, data.length);
            
            int offset = data.length;
            newData[offset] = (byte) ((value >>> 24) & 0xFF);
            newData[offset + 1] = (byte) ((value >>> 16) & 0xFF);
            newData[offset + 2] = (byte) ((value >>> 8) & 0xFF);
            newData[offset + 3] = (byte) (value & 0xFF);
            
            this.data = newData;
            return this;
        }
        
        public MessageBuilder writeShort(short value) {
            byte[] newData = new byte[data.length + 2];
            System.arraycopy(data, 0, newData, 0, data.length);
            
            int offset = data.length;
            newData[offset] = (byte) ((value >>> 8) & 0xFF);
            newData[offset + 1] = (byte) (value & 0xFF);
            
            this.data = newData;
            return this;
        }
        
        public MessageBuilder writeByte(byte value) {
            byte[] newData = new byte[data.length + 1];
            System.arraycopy(data, 0, newData, 0, data.length);
            newData[data.length] = value;
            this.data = newData;
            return this;
        }
        
        public GameMessage build() {
            return new GameMessage(type, sequence, flags, data);
        }
    }
    
    @Override
    public String toString() {
        return String.format("GameMessage[type: %d, seq: %d, flags: %d, length: %d, dataLen: %d]",
                type, sequence, flags, length, getDataLength());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameMessage that = (GameMessage) o;
        return type == that.type && sequence == that.sequence && flags == that.flags && 
               Arrays.equals(data, that.data);
    }
    
    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + sequence;
        result = 31 * result + flags;
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
} 