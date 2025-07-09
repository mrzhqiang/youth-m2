package com.mir2.network;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 网络统计类
 * 
 * <p>负责收集和统计网络相关的性能指标，包括连接数、消息数量、流量等。
 * 这些统计数据用于监控系统性能、诊断问题和优化配置。</p>
 * 
 * <p>统计指标：</p>
 * <ul>
 *   <li>连接统计：总连接数、活跃连接数、峰值连接数</li>
 *   <li>消息统计：接收消息数、发送消息数、处理失败数</li>
 *   <li>流量统计：接收字节数、发送字节数</li>
 *   <li>性能统计：平均响应时间、错误率</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@Data
public class NetworkStatistics {
    
    // ================================
    // 连接统计
    // ================================
    
    /** 总连接数 */
    private final AtomicLong totalConnections = new AtomicLong(0);
    
    /** 当前活跃连接数 */
    private final AtomicLong activeConnections = new AtomicLong(0);
    
    /** 峰值连接数 */
    private final AtomicLong peakConnections = new AtomicLong(0);
    
    /** 连接失败数 */
    private final AtomicLong failedConnections = new AtomicLong(0);
    
    /** 超时连接数 */
    private final AtomicLong timeoutConnections = new AtomicLong(0);
    
    // ================================
    // 消息统计
    // ================================
    
    /** 接收消息总数 */
    private final AtomicLong receivedMessages = new AtomicLong(0);
    
    /** 发送消息总数 */
    private final AtomicLong sentMessages = new AtomicLong(0);
    
    /** 处理失败消息数 */
    private final AtomicLong failedMessages = new AtomicLong(0);
    
    /** 丢弃消息数 */
    private final AtomicLong droppedMessages = new AtomicLong(0);
    
    // ================================
    // 流量统计
    // ================================
    
    /** 接收字节总数 */
    private final AtomicLong receivedBytes = new AtomicLong(0);
    
    /** 发送字节总数 */
    private final AtomicLong sentBytes = new AtomicLong(0);
    
    // ================================
    // 性能统计
    // ================================
    
    /** 消息处理总时间（毫秒） */
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    
    /** 处理的消息数量（用于计算平均处理时间） */
    private final AtomicLong processedMessages = new AtomicLong(0);
    
    /** 启动时间 */
    private final long startTime = System.currentTimeMillis();
    
    /**
     * 记录新连接
     */
    public void recordConnection() {
        totalConnections.incrementAndGet();
        long current = activeConnections.incrementAndGet();
        
        // 更新峰值连接数
        long peak = peakConnections.get();
        if (current > peak) {
            peakConnections.compareAndSet(peak, current);
        }
        
        log.debug("记录新连接 [总连接数: {}, 活跃连接数: {}, 峰值连接数: {}]",
                totalConnections.get(), current, peakConnections.get());
    }
    
    /**
     * 记录连接断开
     */
    public void recordDisconnection() {
        long current = activeConnections.decrementAndGet();
        
        log.debug("记录连接断开 [活跃连接数: {}]", current);
    }
    
    /**
     * 记录连接失败
     */
    public void recordFailedConnection() {
        failedConnections.incrementAndGet();
        log.debug("记录连接失败 [失败连接数: {}]", failedConnections.get());
    }
    
    /**
     * 记录超时连接
     */
    public void recordTimeoutConnection() {
        timeoutConnections.incrementAndGet();
        log.debug("记录超时连接 [超时连接数: {}]", timeoutConnections.get());
    }
    
    /**
     * 记录接收消息
     * 
     * @param messageSize 消息大小
     */
    public void recordReceivedMessage(int messageSize) {
        receivedMessages.incrementAndGet();
        receivedBytes.addAndGet(messageSize);
        
        log.debug("记录接收消息 [消息数: {}, 字节数: {}]",
                receivedMessages.get(), receivedBytes.get());
    }
    
    /**
     * 记录发送消息
     * 
     * @param messageSize 消息大小
     */
    public void recordSentMessage(int messageSize) {
        sentMessages.incrementAndGet();
        sentBytes.addAndGet(messageSize);
        
        log.debug("记录发送消息 [消息数: {}, 字节数: {}]",
                sentMessages.get(), sentBytes.get());
    }
    
    /**
     * 记录处理失败消息
     */
    public void recordFailedMessage() {
        failedMessages.incrementAndGet();
        log.debug("记录处理失败消息 [失败消息数: {}]", failedMessages.get());
    }
    
    /**
     * 记录丢弃消息
     */
    public void recordDroppedMessage() {
        droppedMessages.incrementAndGet();
        log.debug("记录丢弃消息 [丢弃消息数: {}]", droppedMessages.get());
    }
    
    /**
     * 记录消息处理时间
     * 
     * @param processingTime 处理时间（毫秒）
     */
    public void recordProcessingTime(long processingTime) {
        totalProcessingTime.addAndGet(processingTime);
        processedMessages.incrementAndGet();
        
        log.debug("记录消息处理时间 [处理时间: {}ms, 平均处理时间: {}ms]",
                processingTime, getAverageProcessingTime());
    }
    
    /**
     * 获取平均消息处理时间
     * 
     * @return 平均处理时间（毫秒）
     */
    public double getAverageProcessingTime() {
        long processed = processedMessages.get();
        if (processed == 0) {
            return 0.0;
        }
        return (double) totalProcessingTime.get() / processed;
    }
    
    /**
     * 获取消息处理成功率
     * 
     * @return 成功率（百分比）
     */
    public double getMessageSuccessRate() {
        long total = receivedMessages.get();
        if (total == 0) {
            return 100.0;
        }
        long failed = failedMessages.get();
        return ((double) (total - failed) / total) * 100.0;
    }
    
    /**
     * 获取连接成功率
     * 
     * @return 成功率（百分比）
     */
    public double getConnectionSuccessRate() {
        long total = totalConnections.get();
        if (total == 0) {
            return 100.0;
        }
        long failed = failedConnections.get();
        return ((double) (total - failed) / total) * 100.0;
    }
    
    /**
     * 获取运行时间
     * 
     * @return 运行时间（毫秒）
     */
    public long getUptime() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * 获取每秒处理消息数
     * 
     * @return 每秒消息数
     */
    public double getMessagesPerSecond() {
        long uptime = getUptime();
        if (uptime == 0) {
            return 0.0;
        }
        return ((double) receivedMessages.get() / uptime) * 1000.0;
    }
    
    /**
     * 获取每秒流量（字节）
     * 
     * @return 每秒字节数
     */
    public double getBytesPerSecond() {
        long uptime = getUptime();
        if (uptime == 0) {
            return 0.0;
        }
        return ((double) (receivedBytes.get() + sentBytes.get()) / uptime) * 1000.0;
    }
    
    /**
     * 重置统计数据
     */
    public void reset() {
        totalConnections.set(0);
        activeConnections.set(0);
        peakConnections.set(0);
        failedConnections.set(0);
        timeoutConnections.set(0);
        
        receivedMessages.set(0);
        sentMessages.set(0);
        failedMessages.set(0);
        droppedMessages.set(0);
        
        receivedBytes.set(0);
        sentBytes.set(0);
        
        totalProcessingTime.set(0);
        processedMessages.set(0);
        
        log.info("网络统计数据已重置");
    }
    
    /**
     * 获取统计摘要
     * 
     * @return 统计摘要字符串
     */
    public String getSummary() {
        return String.format(
                "网络统计摘要:\n" +
                "  连接统计: 总连接数=%d, 活跃连接数=%d, 峰值连接数=%d, 失败连接数=%d\n" +
                "  消息统计: 接收消息=%d, 发送消息=%d, 失败消息=%d, 丢弃消息=%d\n" +
                "  流量统计: 接收字节=%d, 发送字节=%d\n" +
                "  性能统计: 平均处理时间=%.2fms, 消息成功率=%.2f%%, 连接成功率=%.2f%%\n" +
                "  运行时间: %d秒, 每秒消息数=%.2f, 每秒流量=%.2f字节",
                totalConnections.get(), activeConnections.get(), peakConnections.get(), failedConnections.get(),
                receivedMessages.get(), sentMessages.get(), failedMessages.get(), droppedMessages.get(),
                receivedBytes.get(), sentBytes.get(),
                getAverageProcessingTime(), getMessageSuccessRate(), getConnectionSuccessRate(),
                getUptime() / 1000, getMessagesPerSecond(), getBytesPerSecond()
        );
    }
    
    /**
     * 打印统计摘要
     */
    public void printSummary() {
        log.info(getSummary());
    }
} 