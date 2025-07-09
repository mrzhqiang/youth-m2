package com.mir2.service;

import com.mir2.core.model.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知服务
 * 
 * <p>负责游戏内各种消息通知的发送，包括：</p>
 * <ul>
 *   <li>系统消息</li>
 *   <li>私聊消息</li>
 *   <li>行会消息</li>
 *   <li>好友通知</li>
 *   <li>任务通知</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class NotificationService {
    
    @Autowired
    private PlayerService playerService;
    
    // 消息队列，存储离线玩家的消息
    private final Map<String, List<NotificationMessage>> offlineMessages = new ConcurrentHashMap<>();
    
    /**
     * 发送系统消息
     * 
     * @param playerName 玩家名称
     * @param message 消息内容
     */
    public void sendSystemMessage(String playerName, String message) {
        try {
            Player player = playerService.getOnlinePlayer(playerName);
            
            if (player != null && player.isOnline()) {
                // 玩家在线，直接发送
                sendMessageToClient(player, "SYSTEM", message);
                log.debug("发送系统消息给玩家 {}: {}", playerName, message);
            } else {
                // 玩家离线，存储消息
                storeOfflineMessage(playerName, "SYSTEM", "系统消息", message);
                log.debug("存储离线系统消息给玩家 {}: {}", playerName, message);
            }
            
        } catch (Exception e) {
            log.error("发送系统消息失败: playerName={}, message={}", playerName, message, e);
        }
    }
    
    /**
     * 发送私聊消息
     * 
     * @param toPlayer 接收者
     * @param fromPlayer 发送者
     * @param message 消息内容
     */
    public void sendPrivateMessage(String toPlayer, String fromPlayer, String message) {
        try {
            Player receiver = playerService.getOnlinePlayer(toPlayer);
            
            if (receiver != null && receiver.isOnline()) {
                // 接收者在线，直接发送
                String formattedMessage = String.format("[私聊] %s: %s", fromPlayer, message);
                sendMessageToClient(receiver, "PRIVATE", formattedMessage);
                log.debug("发送私聊消息: {} -> {}: {}", fromPlayer, toPlayer, message);
            } else {
                // 接收者离线，存储消息
                String title = String.format("来自 %s 的私聊", fromPlayer);
                storeOfflineMessage(toPlayer, "PRIVATE", title, message);
                log.debug("存储离线私聊消息: {} -> {}: {}", fromPlayer, toPlayer, message);
            }
            
        } catch (Exception e) {
            log.error("发送私聊消息失败: from={}, to={}, message={}", fromPlayer, toPlayer, message, e);
        }
    }
    
    /**
     * 发送行会消息
     * 
     * @param playerName 玩家名称
     * @param message 消息内容
     */
    public void sendGuildMessage(String playerName, String message) {
        try {
            Player player = playerService.getOnlinePlayer(playerName);
            
            if (player != null && player.isOnline()) {
                // 玩家在线，直接发送
                String formattedMessage = String.format("[行会] %s", message);
                sendMessageToClient(player, "GUILD", formattedMessage);
                log.debug("发送行会消息给玩家 {}: {}", playerName, message);
            } else {
                // 玩家离线，存储消息
                storeOfflineMessage(playerName, "GUILD", "行会消息", message);
                log.debug("存储离线行会消息给玩家 {}: {}", playerName, message);
            }
            
        } catch (Exception e) {
            log.error("发送行会消息失败: playerName={}, message={}", playerName, message, e);
        }
    }
    
    /**
     * 发送通知消息
     * 
     * @param playerName 玩家名称
     * @param title 通知标题
     * @param message 消息内容
     */
    public void sendNotification(String playerName, String title, String message) {
        try {
            Player player = playerService.getOnlinePlayer(playerName);
            
            if (player != null && player.isOnline()) {
                // 玩家在线，直接发送
                String formattedMessage = String.format("[%s] %s", title, message);
                sendMessageToClient(player, "NOTIFICATION", formattedMessage);
                log.debug("发送通知给玩家 {}: {} - {}", playerName, title, message);
            } else {
                // 玩家离线，存储消息
                storeOfflineMessage(playerName, "NOTIFICATION", title, message);
                log.debug("存储离线通知给玩家 {}: {} - {}", playerName, title, message);
            }
            
        } catch (Exception e) {
            log.error("发送通知失败: playerName={}, title={}, message={}", playerName, title, message, e);
        }
    }
    
    /**
     * 发送全服公告
     * 
     * @param message 公告内容
     */
    public void sendServerAnnouncement(String message) {
        try {
            List<Player> onlinePlayers = playerService.getAllOnlinePlayers();
            
            String formattedMessage = String.format("[系统公告] %s", message);
            
            for (Player player : onlinePlayers) {
                if (player.isOnline()) {
                    sendMessageToClient(player, "ANNOUNCEMENT", formattedMessage);
                }
            }
            
            log.info("发送全服公告: {}", message);
            
        } catch (Exception e) {
            log.error("发送全服公告失败: message={}", message, e);
        }
    }
    
    /**
     * 发送地图消息
     * 
     * @param mapName 地图名称
     * @param message 消息内容
     */
    public void sendMapMessage(String mapName, String message) {
        try {
            // 获取指定地图上的玩家
            List<Player> playersOnMap = playerService.getAllOnlinePlayers()
                    .stream()
                    .filter(player -> mapName.equals(player.getMapName()))
                    .collect(java.util.stream.Collectors.toList());
            
            String formattedMessage = String.format("[地图消息] %s", message);
            
            for (Player player : playersOnMap) {
                if (player.isOnline()) {
                    sendMessageToClient(player, "MAP", formattedMessage);
                }
            }
            
            log.debug("发送地图消息到 {}: {}", mapName, message);
            
        } catch (Exception e) {
            log.error("发送地图消息失败: mapName={}, message={}", mapName, message, e);
        }
    }
    
    /**
     * 发送邮件通知
     * 
     * @param receiverName 接收者名称
     * @param subject 邮件主题
     */
    public void sendMailNotification(String receiverName, String subject) {
        try {
            Player receiver = playerService.getOnlinePlayer(receiverName);
            if (receiver == null || !receiver.isOnline()) {
                return;
            }
            
            String notificationMessage = String.format("您收到了一封新邮件: %s", subject);
            sendMessageToClient(receiver, "MAIL_NOTIFICATION", notificationMessage);
            
            log.info("发送邮件通知: {} -> {}", receiverName, subject);
            
        } catch (Exception e) {
            log.error("发送邮件通知失败: {} -> {}", receiverName, subject, e);
        }
    }
    
    /**
     * 发送任务通知
     * 
     * @param playerName 玩家名称
     * @param questName 任务名称
     * @param message 通知内容
     */
    public void sendQuestNotification(String playerName, String questName, String message) {
        try {
            Player player = playerService.getOnlinePlayer(playerName);
            if (player == null || !player.isOnline()) {
                return;
            }
            
            String notificationMessage = String.format("[任务: %s] %s", questName, message);
            sendMessageToClient(player, "QUEST_NOTIFICATION", notificationMessage);
            
            log.info("发送任务通知: {} -> {}: {}", playerName, questName, message);
            
        } catch (Exception e) {
            log.error("发送任务通知失败: {} -> {}: {}", playerName, questName, message, e);
        }
    }
    
    /**
     * 获取离线消息
     * 
     * @param playerName 玩家名称
     * @return 离线消息列表
     */
    public List<NotificationMessage> getOfflineMessages(String playerName) {
        List<NotificationMessage> messages = offlineMessages.get(playerName);
        if (messages != null) {
            // 返回消息后清空
            offlineMessages.remove(playerName);
            log.debug("获取玩家 {} 的离线消息 {} 条", playerName, messages.size());
            return messages;
        }
        return List.of();
    }
    
    /**
     * 发送消息到客户端
     * 
     * @param player 玩家
     * @param type 消息类型
     * @param message 消息内容
     */
    private void sendMessageToClient(Player player, String type, String message) {
        try {
            // 这里应该通过网络连接发送消息到客户端
            // 目前使用日志模拟
            log.info("发送消息到客户端 [{}] {}: {}", type, player.getName(), message);
            
            // 实际实现中应该类似：
            // if (player.getConnection() != null) {
            //     player.getConnection().sendMessage(type, message);
            // }
            
        } catch (Exception e) {
            log.error("发送消息到客户端失败", e);
        }
    }
    
    /**
     * 存储离线消息
     * 
     * @param playerName 玩家名称
     * @param type 消息类型
     * @param title 消息标题
     * @param content 消息内容
     */
    private void storeOfflineMessage(String playerName, String type, String title, String content) {
        try {
            NotificationMessage message = new NotificationMessage();
            message.setType(type);
            message.setTitle(title);
            message.setContent(content);
            message.setSendTime(LocalDateTime.now());
            
            offlineMessages.computeIfAbsent(playerName, k -> new java.util.ArrayList<>()).add(message);
            
        } catch (Exception e) {
            log.error("存储离线消息失败", e);
        }
    }
    
    /**
     * 通知消息类
     */
    public static class NotificationMessage {
        private String type;        // 消息类型
        private String title;       // 消息标题
        private String content;     // 消息内容
        private LocalDateTime sendTime; // 发送时间
        private boolean read;       // 是否已读
        
        public NotificationMessage() {
            this.read = false;
        }
        
        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public LocalDateTime getSendTime() { return sendTime; }
        public void setSendTime(LocalDateTime sendTime) { this.sendTime = sendTime; }
        
        public boolean isRead() { return read; }
        public void setRead(boolean read) { this.read = read; }
    }
} 