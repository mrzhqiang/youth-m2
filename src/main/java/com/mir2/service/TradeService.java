package com.mir2.service;

import com.mir2.core.model.Item;
import com.mir2.core.model.Player;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 交易系统服务
 * 
 * <p>负责处理玩家之间的交易，包括物品交易、金币交易等。
 * 对应原M2Engine中的Trade模块。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Slf4j
public class TradeService {
    
    /** 活跃交易 */
    private final Map<String, TradeSession> activeTrades = new ConcurrentHashMap<>();
    
    /** 交易邀请 */
    private final Map<String, TradeInvitation> tradeInvitations = new ConcurrentHashMap<>();
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private ItemService itemService;
    
    /**
     * 发起交易邀请
     * 
     * @param from 发起者
     * @param to 接收者
     * @return 邀请结果
     */
    public TradeResult inviteTrade(Player from, Player to) {
        if (from == null || to == null) {
            return new TradeResult(false, "玩家不存在");
        }
        
        // 检查玩家状态
        if (!from.isOnline() || !to.isOnline()) {
            return new TradeResult(false, "玩家不在线");
        }
        
        // 检查是否在同一地图
        if (!from.getMapName().equals(to.getMapName())) {
            return new TradeResult(false, "不在同一地图");
        }
        
        // 检查距离
        if (from.distanceTo(to) > 3) {
            return new TradeResult(false, "距离过远");
        }
        
        // 检查是否已经在交易中
        if (isPlayerInTrade(from.getName()) || isPlayerInTrade(to.getName())) {
            return new TradeResult(false, "玩家正在交易中");
        }
        
        // 检查是否已经发出邀请
        String invitationKey = from.getName() + "_to_" + to.getName();
        if (tradeInvitations.containsKey(invitationKey)) {
            return new TradeResult(false, "已发出交易邀请");
        }
        
        // 创建邀请
        TradeInvitation invitation = new TradeInvitation(from.getName(), to.getName());
        tradeInvitations.put(invitationKey, invitation);
        
        log.info("玩家 {} 邀请 {} 交易", from.getName(), to.getName());
        
        return new TradeResult(true, "交易邀请已发送");
    }
    
    /**
     * 接受交易邀请
     * 
     * @param playerName 接受者名称
     * @param inviterName 邀请者名称
     * @return 交易结果
     */
    public TradeResult acceptTrade(String playerName, String inviterName) {
        String invitationKey = inviterName + "_to_" + playerName;
        TradeInvitation invitation = tradeInvitations.get(invitationKey);
        
        if (invitation == null) {
            return new TradeResult(false, "交易邀请不存在");
        }
        
        // 检查邀请是否过期
        if (invitation.isExpired()) {
            tradeInvitations.remove(invitationKey);
            return new TradeResult(false, "交易邀请已过期");
        }
        
        // 获取玩家
        Player inviter = playerService.getPlayerByName(inviterName);
        Player accepter = playerService.getPlayerByName(playerName);
        
        if (inviter == null || accepter == null) {
            return new TradeResult(false, "玩家不存在");
        }
        
        // 检查玩家状态
        if (!inviter.isOnline() || !accepter.isOnline()) {
            return new TradeResult(false, "玩家不在线");
        }
        
        // 检查是否在交易中
        if (isPlayerInTrade(inviter.getName()) || isPlayerInTrade(accepter.getName())) {
            return new TradeResult(false, "玩家正在交易中");
        }
        
        // 移除邀请
        tradeInvitations.remove(invitationKey);
        
        // 创建交易会话
        String sessionId = UUID.randomUUID().toString();
        TradeSession session = new TradeSession(sessionId, inviter, accepter);
        activeTrades.put(sessionId, session);
        
        log.info("玩家 {} 和 {} 开始交易 [{}]", inviter.getName(), accepter.getName(), sessionId);
        
        return new TradeResult(true, "交易开始", sessionId);
    }
    
    /**
     * 拒绝交易邀请
     * 
     * @param playerName 拒绝者名称
     * @param inviterName 邀请者名称
     * @return 交易结果
     */
    public TradeResult rejectTrade(String playerName, String inviterName) {
        String invitationKey = inviterName + "_to_" + playerName;
        TradeInvitation invitation = tradeInvitations.remove(invitationKey);
        
        if (invitation == null) {
            return new TradeResult(false, "交易邀请不存在");
        }
        
        log.info("玩家 {} 拒绝了 {} 的交易邀请", playerName, inviterName);
        
        return new TradeResult(true, "交易邀请已拒绝");
    }
    
    /**
     * 添加交易物品
     * 
     * @param playerName 玩家名称
     * @param sessionId 交易会话ID
     * @param itemId 物品ID
     * @param quantity 数量
     * @return 交易结果
     */
    public TradeResult addTradeItem(String playerName, String sessionId, int itemId, int quantity) {
        TradeSession session = activeTrades.get(sessionId);
        if (session == null) {
            return new TradeResult(false, "交易会话不存在");
        }
        
        // 检查玩家是否在交易中
        if (!session.getPlayer1().getName().equals(playerName) && 
            !session.getPlayer2().getName().equals(playerName)) {
            return new TradeResult(false, "玩家不在此交易会话中");
        }
        
        // 检查交易状态
        if (session.getStatus() != TradeSession.TradeStatus.TRADING) {
            return new TradeResult(false, "交易状态不正确");
        }
        
        // 获取玩家
        Player player = session.getPlayer1().getName().equals(playerName) ? 
                session.getPlayer1() : session.getPlayer2();
        
        // 检查物品是否存在
        Item item = player.getInventory().get(itemId);
        if (item == null) {
            return new TradeResult(false, "物品不存在");
        }
        
        // 检查数量
        if (item.getQuantity() < quantity) {
            return new TradeResult(false, "物品数量不足");
        }
        
        // 添加到交易物品
        Map<Integer, Integer> playerItems = session.getPlayer1().getName().equals(playerName) ? 
                session.getPlayer1Items() : session.getPlayer2Items();
        
        playerItems.put(itemId, quantity);
        
        // 重置确认状态
        session.setPlayer1Confirmed(false);
        session.setPlayer2Confirmed(false);
        
        log.info("玩家 {} 添加交易物品: {} 数量: {}", playerName, itemId, quantity);
        
        return new TradeResult(true, "物品已添加到交易");
    }
    
    /**
     * 移除交易物品
     * 
     * @param playerName 玩家名称
     * @param sessionId 交易会话ID
     * @param itemId 物品ID
     * @return 交易结果
     */
    public TradeResult removeTradeItem(String playerName, String sessionId, int itemId) {
        TradeSession session = activeTrades.get(sessionId);
        if (session == null) {
            return new TradeResult(false, "交易会话不存在");
        }
        
        // 检查玩家是否在交易中
        if (!session.getPlayer1().getName().equals(playerName) && 
            !session.getPlayer2().getName().equals(playerName)) {
            return new TradeResult(false, "玩家不在此交易会话中");
        }
        
        // 检查交易状态
        if (session.getStatus() != TradeSession.TradeStatus.TRADING) {
            return new TradeResult(false, "交易状态不正确");
        }
        
        // 移除交易物品
        Map<Integer, Integer> playerItems = session.getPlayer1().getName().equals(playerName) ? 
                session.getPlayer1Items() : session.getPlayer2Items();
        
        playerItems.remove(itemId);
        
        // 重置确认状态
        session.setPlayer1Confirmed(false);
        session.setPlayer2Confirmed(false);
        
        log.info("玩家 {} 移除交易物品: {}", playerName, itemId);
        
        return new TradeResult(true, "物品已从交易中移除");
    }
    
    /**
     * 设置交易金币
     * 
     * @param playerName 玩家名称
     * @param sessionId 交易会话ID
     * @param gold 金币数量
     * @return 交易结果
     */
    public TradeResult setTradeGold(String playerName, String sessionId, int gold) {
        TradeSession session = activeTrades.get(sessionId);
        if (session == null) {
            return new TradeResult(false, "交易会话不存在");
        }
        
        // 检查玩家是否在交易中
        if (!session.getPlayer1().getName().equals(playerName) && 
            !session.getPlayer2().getName().equals(playerName)) {
            return new TradeResult(false, "玩家不在此交易会话中");
        }
        
        // 检查交易状态
        if (session.getStatus() != TradeSession.TradeStatus.TRADING) {
            return new TradeResult(false, "交易状态不正确");
        }
        
        // 获取玩家
        Player player = session.getPlayer1().getName().equals(playerName) ? 
                session.getPlayer1() : session.getPlayer2();
        
        // 检查金币数量
        if (player.getGold() < gold) {
            return new TradeResult(false, "金币不足");
        }
        
        // 设置交易金币
        if (session.getPlayer1().getName().equals(playerName)) {
            session.setPlayer1Gold(gold);
        } else {
            session.setPlayer2Gold(gold);
        }
        
        // 重置确认状态
        session.setPlayer1Confirmed(false);
        session.setPlayer2Confirmed(false);
        
        log.info("玩家 {} 设置交易金币: {}", playerName, gold);
        
        return new TradeResult(true, "交易金币已设置");
    }
    
    /**
     * 确认交易
     * 
     * @param playerName 玩家名称
     * @param sessionId 交易会话ID
     * @return 交易结果
     */
    public TradeResult confirmTrade(String playerName, String sessionId) {
        TradeSession session = activeTrades.get(sessionId);
        if (session == null) {
            return new TradeResult(false, "交易会话不存在");
        }
        
        // 检查玩家是否在交易中
        if (!session.getPlayer1().getName().equals(playerName) && 
            !session.getPlayer2().getName().equals(playerName)) {
            return new TradeResult(false, "玩家不在此交易会话中");
        }
        
        // 检查交易状态
        if (session.getStatus() != TradeSession.TradeStatus.TRADING) {
            return new TradeResult(false, "交易状态不正确");
        }
        
        // 设置确认状态
        if (session.getPlayer1().getName().equals(playerName)) {
            session.setPlayer1Confirmed(true);
        } else {
            session.setPlayer2Confirmed(true);
        }
        
        log.info("玩家 {} 确认交易", playerName);
        
        // 检查是否双方都确认
        if (session.isPlayer1Confirmed() && session.isPlayer2Confirmed()) {
            return executeTrade(session);
        }
        
        return new TradeResult(true, "交易已确认，等待对方确认");
    }
    
    /**
     * 取消交易
     * 
     * @param playerName 玩家名称
     * @param sessionId 交易会话ID
     * @return 交易结果
     */
    public TradeResult cancelTrade(String playerName, String sessionId) {
        TradeSession session = activeTrades.get(sessionId);
        if (session == null) {
            return new TradeResult(false, "交易会话不存在");
        }
        
        // 检查玩家是否在交易中
        if (!session.getPlayer1().getName().equals(playerName) && 
            !session.getPlayer2().getName().equals(playerName)) {
            return new TradeResult(false, "玩家不在此交易会话中");
        }
        
        // 移除交易会话
        activeTrades.remove(sessionId);
        
        log.info("玩家 {} 取消交易 [{}]", playerName, sessionId);
        
        return new TradeResult(true, "交易已取消");
    }
    
    /**
     * 执行交易
     */
    private TradeResult executeTrade(TradeSession session) {
        Player player1 = session.getPlayer1();
        Player player2 = session.getPlayer2();
        
        try {
            // 验证交易条件
            if (!validateTrade(session)) {
                return new TradeResult(false, "交易验证失败");
            }
            
            // 执行物品交换
            exchangeItems(session);
            
            // 执行金币交换
            exchangeGold(session);
            
            // 更新交易状态
            session.setStatus(TradeSession.TradeStatus.COMPLETED);
            session.setCompletedTime(System.currentTimeMillis());
            
            // 移除交易会话
            activeTrades.remove(session.getSessionId());
            
            log.info("交易完成: {} 和 {} [{}]", 
                    player1.getName(), player2.getName(), session.getSessionId());
            
            return new TradeResult(true, "交易完成");
            
        } catch (Exception e) {
            log.error("交易执行失败: {}", e.getMessage());
            return new TradeResult(false, "交易执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证交易
     */
    private boolean validateTrade(TradeSession session) {
        Player player1 = session.getPlayer1();
        Player player2 = session.getPlayer2();
        
        // 验证玩家1的物品
        for (Map.Entry<Integer, Integer> entry : session.getPlayer1Items().entrySet()) {
            Item item = player1.getInventory().get(entry.getKey());
            if (item == null || item.getQuantity() < entry.getValue()) {
                return false;
            }
        }
        
        // 验证玩家2的物品
        for (Map.Entry<Integer, Integer> entry : session.getPlayer2Items().entrySet()) {
            Item item = player2.getInventory().get(entry.getKey());
            if (item == null || item.getQuantity() < entry.getValue()) {
                return false;
            }
        }
        
        // 验证金币
        if (player1.getGold() < session.getPlayer1Gold() || 
            player2.getGold() < session.getPlayer2Gold()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 交换物品
     */
    private void exchangeItems(TradeSession session) {
        Player player1 = session.getPlayer1();
        Player player2 = session.getPlayer2();
        
        // 玩家1的物品给玩家2
        for (Map.Entry<Integer, Integer> entry : session.getPlayer1Items().entrySet()) {
            int itemId = entry.getKey();
            int quantity = entry.getValue();
            
            // 从玩家1移除物品
            player1.removeItem(itemId, quantity);
            
            // 给玩家2添加物品
            player2.addItem(itemId, quantity);
        }
        
        // 玩家2的物品给玩家1
        for (Map.Entry<Integer, Integer> entry : session.getPlayer2Items().entrySet()) {
            int itemId = entry.getKey();
            int quantity = entry.getValue();
            
            // 从玩家2移除物品
            player2.removeItem(itemId, quantity);
            
            // 给玩家1添加物品
            player1.addItem(itemId, quantity);
        }
    }
    
    /**
     * 交换金币
     */
    private void exchangeGold(TradeSession session) {
        Player player1 = session.getPlayer1();
        Player player2 = session.getPlayer2();
        
        int player1Gold = session.getPlayer1Gold();
        int player2Gold = session.getPlayer2Gold();
        
        // 扣除和添加金币
        player1.setGold(player1.getGold() - player1Gold + player2Gold);
        player2.setGold(player2.getGold() - player2Gold + player1Gold);
    }
    
    /**
     * 检查玩家是否在交易中
     */
    private boolean isPlayerInTrade(String playerName) {
        for (TradeSession session : activeTrades.values()) {
            if (session.getPlayer1().getName().equals(playerName) || 
                session.getPlayer2().getName().equals(playerName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取玩家的交易会话
     */
    public TradeSession getPlayerTradeSession(String playerName) {
        for (TradeSession session : activeTrades.values()) {
            if (session.getPlayer1().getName().equals(playerName) || 
                session.getPlayer2().getName().equals(playerName)) {
                return session;
            }
        }
        return null;
    }
    
    /**
     * 清理过期的交易邀请
     */
    public void cleanupExpiredInvitations() {
        Iterator<Map.Entry<String, TradeInvitation>> iterator = tradeInvitations.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TradeInvitation> entry = iterator.next();
            if (entry.getValue().isExpired()) {
                iterator.remove();
                log.debug("清理过期交易邀请: {}", entry.getKey());
            }
        }
    }
    
    /**
     * 交易邀请类
     */
    @Data
    public static class TradeInvitation {
        private String from;
        private String to;
        private long createdTime;
        private long expireTime;
        
        public TradeInvitation(String from, String to) {
            this.from = from;
            this.to = to;
            this.createdTime = System.currentTimeMillis();
            this.expireTime = createdTime + 30000; // 30秒过期
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
    
    /**
     * 交易会话类
     */
    @Data
    public static class TradeSession {
        private String sessionId;
        private Player player1;
        private Player player2;
        private Map<Integer, Integer> player1Items;
        private Map<Integer, Integer> player2Items;
        private int player1Gold;
        private int player2Gold;
        private boolean player1Confirmed;
        private boolean player2Confirmed;
        private TradeStatus status;
        private long createdTime;
        private long completedTime;
        
        public TradeSession(String sessionId, Player player1, Player player2) {
            this.sessionId = sessionId;
            this.player1 = player1;
            this.player2 = player2;
            this.player1Items = new HashMap<>();
            this.player2Items = new HashMap<>();
            this.player1Gold = 0;
            this.player2Gold = 0;
            this.player1Confirmed = false;
            this.player2Confirmed = false;
            this.status = TradeStatus.TRADING;
            this.createdTime = System.currentTimeMillis();
            this.completedTime = 0;
        }
        
        public enum TradeStatus {
            TRADING, COMPLETED, CANCELLED
        }
    }
    
    /**
     * 交易结果类
     */
    @Data
    public static class TradeResult {
        private boolean success;
        private String message;
        private String sessionId;
        
        public TradeResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public TradeResult(boolean success, String message, String sessionId) {
            this.success = success;
            this.message = message;
            this.sessionId = sessionId;
        }
    }
} 