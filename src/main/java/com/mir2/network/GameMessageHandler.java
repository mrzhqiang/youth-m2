package com.mir2.network;

import com.mir2.core.engine.UserEngine;
import com.mir2.core.model.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 游戏消息处理器
 * 
 * <p>负责处理从客户端接收到的各种游戏消息，将网络消息转换为游戏逻辑操作。
 * 这是网络层和游戏逻辑层之间的桥梁，确保消息的正确处理和游戏状态的维护。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>消息分发和路由</li>
 *   <li>权限验证和安全检查</li>
 *   <li>游戏逻辑调用</li>
 *   <li>响应消息发送</li>
 * </ul>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class GameMessageHandler {
    
    @Autowired
    private UserEngine userEngine;
    
    /**
     * 处理游戏消息
     * 
     * @param session 客户端会话
     * @param message 游戏消息
     */
    public void handleMessage(ClientSession session, GameMessage message) {
        if (session == null || message == null) {
            log.warn("接收到空的会话或消息");
            return;
        }
        
        log.debug("处理游戏消息 [会话: {}, 类型: {}, 描述: {}]", 
                session.getSessionId(), 
                message.getType(), 
                GameMessageType.getDescription(message.getType()));
        
        try {
            // 根据消息类型进行分发处理
            switch (message.getType()) {
                case GameMessageType.HEARTBEAT:
                    handleHeartbeat(session, message);
                    break;
                    
                case GameMessageType.CLIENT_CONNECT:
                    handleClientConnect(session, message);
                    break;
                    
                case GameMessageType.LOGIN_REQUEST:
                    handleLoginRequest(session, message);
                    break;
                    
                case GameMessageType.CREATE_CHARACTER:
                    handleCreateCharacter(session, message);
                    break;
                    
                case GameMessageType.SELECT_CHARACTER:
                    handleSelectCharacter(session, message);
                    break;
                    
                case GameMessageType.ENTER_GAME:
                    handleEnterGame(session, message);
                    break;
                    
                case GameMessageType.MOVE_REQUEST:
                    handleMoveRequest(session, message);
                    break;
                    
                case GameMessageType.CHAT_MESSAGE:
                    handleChatMessage(session, message);
                    break;
                    
                case GameMessageType.ATTACK_REQUEST:
                    handleAttackRequest(session, message);
                    break;
                    
                case GameMessageType.USE_ITEM:
                    handleUseItem(session, message);
                    break;
                    
                case GameMessageType.PICK_UP_ITEM:
                    handlePickUpItem(session, message);
                    break;
                    
                case GameMessageType.TRADE_REQUEST:
                    handleTradeRequest(session, message);
                    break;
                    
                case GameMessageType.LOGOUT:
                    handleLogout(session, message);
                    break;
                    
                default:
                    log.warn("未处理的消息类型: {} [会话: {}]", 
                            message.getType(), session.getSessionId());
                    sendErrorMessage(session, "不支持的消息类型");
                    break;
            }
            
        } catch (Exception e) {
            log.error("处理游戏消息时发生异常 [会话: {}, 消息类型: {}]", 
                    session.getSessionId(), message.getType(), e);
            sendErrorMessage(session, "服务器内部错误");
        }
    }
    
    /**
     * 处理心跳包
     */
    private void handleHeartbeat(ClientSession session, GameMessage message) {
        // 心跳包不需要特殊处理，只需要更新会话活跃时间
        // 这在session.handleReceivedMessage()中已经完成了
        log.debug("收到心跳包 [会话: {}]", session.getSessionId());
    }
    
    /**
     * 处理客户端连接
     */
    private void handleClientConnect(ClientSession session, GameMessage message) {
        log.info("处理客户端连接 [会话: {}]", session.getSessionId());
        
        // 发送连接确认
        GameMessage response = GameMessage.builder()
                .type(GameMessageType.SERVER_CONNECT_ACK)
                .sequence(message.getSequence())
                .data("连接成功".getBytes())
                .build();
        
        session.sendMessage(response);
    }
    
    /**
     * 处理登录请求
     */
    private void handleLoginRequest(ClientSession session, GameMessage message) {
        log.info("处理登录请求 [会话: {}]", session.getSessionId());
        
        // 解析登录数据
        String loginData = new String(message.getData());
        String[] parts = loginData.split("\\|");
        
        if (parts.length < 2) {
            sendLoginFailedMessage(session, "登录数据格式错误");
            return;
        }
        
        String username = parts[0];
        String password = parts[1];
        
        // 验证用户登录
        boolean loginSuccess = userEngine.authenticateUser(username, password);
        
        if (loginSuccess) {
            // 登录成功
            session.setUsername(username);
            session.setAuthenticated(true);
            
            GameMessage response = GameMessage.builder()
                    .type(GameMessageType.LOGIN_SUCCESS)
                    .sequence(message.getSequence())
                    .data("登录成功".getBytes())
                    .build();
            
            session.sendMessage(response);
            
            log.info("用户登录成功 [用户: {}, 会话: {}]", username, session.getSessionId());
            
        } else {
            // 登录失败
            sendLoginFailedMessage(session, "用户名或密码错误");
            log.warn("用户登录失败 [用户: {}, 会话: {}]", username, session.getSessionId());
        }
    }
    
    /**
     * 处理创建角色
     */
    private void handleCreateCharacter(ClientSession session, GameMessage message) {
        if (!session.isAuthenticated()) {
            sendErrorMessage(session, "未登录");
            return;
        }
        
        log.info("处理创建角色 [会话: {}]", session.getSessionId());
        
        // 解析角色数据
        String characterData = new String(message.getData());
        String[] parts = characterData.split("\\|");
        
        if (parts.length < 3) {
            sendErrorMessage(session, "角色数据格式错误");
            return;
        }
        
        String characterName = parts[0];
        String jobStr = parts[1];
        String genderStr = parts[2];
        
        // 创建角色
        boolean success = userEngine.createCharacter(session.getUsername(), 
                characterName, jobStr, genderStr);
        
        if (success) {
            sendSuccessMessage(session, "角色创建成功");
            log.info("角色创建成功 [用户: {}, 角色: {}]", session.getUsername(), characterName);
        } else {
            sendErrorMessage(session, "角色创建失败");
        }
    }
    
    /**
     * 处理选择角色
     */
    private void handleSelectCharacter(ClientSession session, GameMessage message) {
        if (!session.isAuthenticated()) {
            sendErrorMessage(session, "未登录");
            return;
        }
        
        String characterName = new String(message.getData());
        log.info("处理选择角色 [会话: {}, 角色: {}]", session.getSessionId(), characterName);
        
        // 加载角色数据
        Player player = userEngine.loadCharacter(session.getUsername(), characterName);
        
        if (player != null) {
            // 绑定玩家到会话
            session.bindPlayer(player);
            
            sendSuccessMessage(session, "角色选择成功");
            log.info("角色选择成功 [用户: {}, 角色: {}]", session.getUsername(), characterName);
        } else {
            sendErrorMessage(session, "角色不存在或加载失败");
        }
    }
    
    /**
     * 处理进入游戏
     */
    private void handleEnterGame(ClientSession session, GameMessage message) {
        if (!session.isAuthenticated() || session.getPlayer() == null) {
            sendErrorMessage(session, "未登录或未选择角色");
            return;
        }
        
        log.info("处理进入游戏 [会话: {}, 角色: {}]", 
                session.getSessionId(), session.getPlayer().getCharacterName());
        
        // 玩家进入游戏世界
        boolean success = userEngine.enterGame(session.getPlayer());
        
        if (success) {
            sendSuccessMessage(session, "进入游戏成功");
            log.info("玩家进入游戏 [角色: {}]", session.getPlayer().getCharacterName());
        } else {
            sendErrorMessage(session, "进入游戏失败");
        }
    }
    
    /**
     * 处理移动请求
     */
    private void handleMoveRequest(ClientSession session, GameMessage message) {
        Player player = session.getPlayer();
        if (player == null) {
            sendErrorMessage(session, "未进入游戏");
            return;
        }
        
        // 解析移动数据
        String moveData = new String(message.getData());
        String[] parts = moveData.split("\\|");
        
        if (parts.length < 3) {
            sendErrorMessage(session, "移动数据格式错误");
            return;
        }
        
        int targetX = Integer.parseInt(parts[0]);
        int targetY = Integer.parseInt(parts[1]);
        int direction = Integer.parseInt(parts[2]);
        
        // 处理移动
        boolean success = userEngine.handlePlayerMove(player, targetX, targetY, direction);
        
        if (success) {
            // 发送移动确认
            GameMessage response = GameMessage.builder()
                    .type(GameMessageType.MOVE_ACK)
                    .sequence(message.getSequence())
                    .data(message.getData())
                    .build();
            
            session.sendMessage(response);
        } else {
            sendErrorMessage(session, "移动失败");
        }
    }
    
    /**
     * 处理聊天消息
     */
    private void handleChatMessage(ClientSession session, GameMessage message) {
        Player player = session.getPlayer();
        if (player == null) {
            sendErrorMessage(session, "未进入游戏");
            return;
        }
        
        String chatContent = new String(message.getData());
        log.debug("处理聊天消息 [角色: {}, 内容: {}]", 
                player.getCharacterName(), chatContent);
        
        // 处理聊天
        userEngine.handleChatMessage(player, chatContent);
    }
    
    /**
     * 处理攻击请求
     */
    private void handleAttackRequest(ClientSession session, GameMessage message) {
        Player player = session.getPlayer();
        if (player == null) {
            sendErrorMessage(session, "未进入游戏");
            return;
        }
        
        // 解析攻击数据
        String attackData = new String(message.getData());
        String[] parts = attackData.split("\\|");
        
        if (parts.length < 3) {
            sendErrorMessage(session, "攻击数据格式错误");
            return;
        }
        
        int targetX = Integer.parseInt(parts[0]);
        int targetY = Integer.parseInt(parts[1]);
        int direction = Integer.parseInt(parts[2]);
        
        // 处理攻击
        userEngine.handlePlayerAttack(player, targetX, targetY, direction);
    }
    
    /**
     * 处理使用物品
     */
    private void handleUseItem(ClientSession session, GameMessage message) {
        Player player = session.getPlayer();
        if (player == null) {
            sendErrorMessage(session, "未进入游戏");
            return;
        }
        
        String itemData = new String(message.getData());
        String[] parts = itemData.split("\\|");
        
        if (parts.length < 1) {
            sendErrorMessage(session, "物品数据格式错误");
            return;
        }
        
        int itemId = Integer.parseInt(parts[0]);
        
        // 处理使用物品
        boolean success = userEngine.handleUseItem(player, itemId);
        
        if (success) {
            sendSuccessMessage(session, "使用物品成功");
        } else {
            sendErrorMessage(session, "使用物品失败");
        }
    }
    
    /**
     * 处理拾取物品
     */
    private void handlePickUpItem(ClientSession session, GameMessage message) {
        Player player = session.getPlayer();
        if (player == null) {
            sendErrorMessage(session, "未进入游戏");
            return;
        }
        
        String itemData = new String(message.getData());
        String[] parts = itemData.split("\\|");
        
        if (parts.length < 3) {
            sendErrorMessage(session, "拾取数据格式错误");
            return;
        }
        
        int itemX = Integer.parseInt(parts[0]);
        int itemY = Integer.parseInt(parts[1]);
        int itemId = Integer.parseInt(parts[2]);
        
        // 处理拾取物品
        boolean success = userEngine.handlePickUpItem(player, itemX, itemY, itemId);
        
        if (success) {
            sendSuccessMessage(session, "拾取物品成功");
        } else {
            sendErrorMessage(session, "拾取物品失败");
        }
    }
    
    /**
     * 处理交易请求
     */
    private void handleTradeRequest(ClientSession session, GameMessage message) {
        Player player = session.getPlayer();
        if (player == null) {
            sendErrorMessage(session, "未进入游戏");
            return;
        }
        
        String tradeData = new String(message.getData());
        String targetPlayerName = tradeData.trim();
        
        // 处理交易请求
        boolean success = userEngine.handleTradeRequest(player, targetPlayerName);
        
        if (success) {
            sendSuccessMessage(session, "交易请求已发送");
        } else {
            sendErrorMessage(session, "交易请求失败");
        }
    }
    
    /**
     * 处理登出
     */
    private void handleLogout(ClientSession session, GameMessage message) {
        log.info("处理登出 [会话: {}]", session.getSessionId());
        
        // 保存玩家数据
        if (session.getPlayer() != null) {
            userEngine.savePlayerData(session.getPlayer());
            userEngine.leaveGame(session.getPlayer());
        }
        
        // 清理会话
        session.unbindPlayer();
        session.setAuthenticated(false);
        
        sendSuccessMessage(session, "登出成功");
    }
    
    /**
     * 发送登录失败消息
     */
    private void sendLoginFailedMessage(ClientSession session, String reason) {
        GameMessage response = GameMessage.builder()
                .type(GameMessageType.LOGIN_FAILED)
                .sequence(0)
                .data(reason.getBytes())
                .build();
        
        session.sendMessage(response);
    }
    
    /**
     * 发送错误消息
     */
    private void sendErrorMessage(ClientSession session, String error) {
        GameMessage response = GameMessage.builder()
                .type(GameMessageType.ERROR)
                .sequence(0)
                .data(error.getBytes())
                .build();
        
        session.sendMessage(response);
    }
    
    /**
     * 发送成功消息
     */
    private void sendSuccessMessage(ClientSession session, String message) {
        GameMessage response = GameMessage.builder()
                .type(GameMessageType.SYSTEM_MESSAGE)
                .sequence(0)
                .data(message.getBytes())
                .build();
        
        session.sendMessage(response);
    }
} 