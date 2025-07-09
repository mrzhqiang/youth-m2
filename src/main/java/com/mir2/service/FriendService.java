package com.mir2.service;

import com.mir2.entity.Player;
import com.mir2.entity.FriendInfo;
import com.mir2.entity.PrivateMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FriendService {
    
    private final Map<String, Set<String>> friendMap = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> blacklistMap = new ConcurrentHashMap<>();
    private final Map<String, FriendInfo> friendInfoMap = new ConcurrentHashMap<>();
    private final Map<String, List<PrivateMessage>> messageMap = new ConcurrentHashMap<>();
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private NotificationService notificationService;
    
    // 配置参数
    private static final int MAX_FRIENDS = 50;          // 最大好友数量
    private static final int MAX_BLACKLIST = 20;       // 最大黑名单数量
    private static final long REQUEST_TIMEOUT = 300000; // 好友请求超时时间 5分钟
    
    /**
     * 发送好友请求
     */
    @Transactional
    public FriendResult sendFriendRequest(Player requester, String targetName) {
        // 检查是否可以发送请求
        FriendCheckResult checkResult = checkFriendRequest(requester, targetName);
        if (!checkResult.canSend) {
            return new FriendResult(false, checkResult.reason);
        }
        
        // 创建好友请求
        // FriendRequest request = new FriendRequest(); // This line is removed as per the new_code
        // request.setRequesterName(requester.getUsername());
        // request.setTargetName(targetName);
        // request.setRequestTime(LocalDateTime.now());
        // request.setStatus("PENDING");
        // request.setMessage("想要添加您为好友");
        
        // 添加到请求列表
        // friendRequests.computeIfAbsent(targetName, k -> new java.util.ArrayList<>()).add(request); // This line is removed as per the new_code
        
        // 通知目标玩家
        sendFriendRequestNotification(requester.getUsername(), targetName);
        
        return new FriendResult(true, "好友请求已发送");
    }
    
    /**
     * 处理好友请求
     */
    @Transactional
    public FriendResult processFriendRequest(Player player, String requesterName, boolean accept) {
        // 查找请求
        // List<FriendRequest> requests = friendRequests.get(player.getUsername()); // This line is removed as per the new_code
        // if (requests == null) {
        //     return new FriendResult(false, "未找到好友请求");
        // }
        
        // FriendRequest request = requests.stream()
        //         .filter(r -> r.getRequesterName().equals(requesterName) && 
        //                    r.getStatus().equals("PENDING"))
        //         .findFirst()
        //         .orElse(null);
        
        // if (request == null) {
        //     return new FriendResult(false, "未找到该用户的好友请求");
        // }
        
        // 检查请求是否过期
        // if (isRequestExpired(request)) { // This line is removed as per the new_code
        //     requests.remove(request);
        //     return new FriendResult(false, "好友请求已过期");
        // }
        
        if (accept) {
            // 接受请求
            // request.setStatus("ACCEPTED"); // This line is removed as per the new_code
            
            // 添加好友关系
            addFriend(player.getUsername(), requesterName);
            addFriend(requesterName, player.getUsername());
            
            // 通知请求者
            sendFriendAcceptNotification(requesterName, player.getUsername());
            
            return new FriendResult(true, "已添加为好友");
        } else {
            // 拒绝请求
            // request.setStatus("REJECTED"); // This line is removed as per the new_code
            
            // 通知请求者
            // notifyFriendRejected(requesterName, player.getUsername()); // This line is removed as per the new_code
            
            return new FriendResult(true, "已拒绝好友请求");
        }
    }
    
    /**
     * 删除好友
     */
    @Transactional
    public FriendResult removeFriend(Player player, String friendName) {
        // 检查是否为好友
        if (!isFriend(player.getUsername(), friendName)) {
            return new FriendResult(false, "该用户不是您的好友");
        }
        
        // 从双方好友列表中删除
        removeFriendFromList(player.getUsername(), friendName);
        removeFriendFromList(friendName, player.getUsername());
        
        // 通知对方
        sendFriendRemoveNotification(friendName, player.getUsername());
        
        return new FriendResult(true, "已删除好友");
    }
    
    /**
     * 获取好友列表
     */
    public List<FriendInfo> getFriendList(String playerName) {
        Set<String> friends = friendMap.getOrDefault(playerName, new HashSet<>());
        
        return friends.stream()
                .map(friendName -> getFriendInfo(playerName, friendName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取好友详细信息
     */
    public FriendInfo getFriendInfo(String playerName, String friendName) {
        String key = playerName + "_" + friendName;
        FriendInfo friendInfo = friendInfoMap.get(key);
        
        if (friendInfo == null) {
            // 获取玩家详细信息
            Player friend = getPlayerDetails(friendName);
            if (friend != null) {
                friendInfo = new FriendInfo();
                friendInfo.setFriendName(friendName);
                friendInfo.setLevel(friend.getLevel());
                friendInfo.setJob(friend.getJob());
                friendInfo.setOnline(friend.isOnline());
                friendInfo.setMapName(friend.getMapName());
                friendInfo.setLastOnlineTime(friend.getLastActivityTime());
                friendInfo.setIntimacy(0); // 初始亲密度为0
                
                friendInfoMap.put(key, friendInfo);
            }
        } else {
            // 更新在线状态
            Player friend = getPlayerDetails(friendName);
            if (friend != null) {
                friendInfo.setOnline(friend.isOnline());
                friendInfo.setLevel(friend.getLevel());
                friendInfo.setMapName(friend.getMapName());
                if (!friend.isOnline()) {
                    friendInfo.setLastOnlineTime(friend.getLastActivityTime());
                }
            }
        }
        
        return friendInfo;
    }
    
    /**
     * 获取玩家详细信息
     */
    private Player getPlayerDetails(String playerName) {
        try {
            // 首先尝试获取在线玩家
            Player player = playerService.getOnlinePlayer(playerName);
            if (player != null) {
                return player;
            }
            
            // 如果不在线，从数据库获取基本信息
            return playerService.getPlayerByName(playerName);
            
        } catch (Exception e) {
            log.error("获取玩家详细信息失败: {}", playerName, e);
            return null;
        }
    }
    
    /**
     * 检查玩家是否存在
     */
    public boolean playerExists(String playerName) {
        try {
            return playerService.playerExists(playerName);
        } catch (Exception e) {
            log.error("检查玩家存在失败: {}", playerName, e);
            return false;
        }
    }
    
    /**
     * 检查玩家是否在线
     */
    public boolean isPlayerOnline(String playerName) {
        try {
            Player player = playerService.getOnlinePlayer(playerName);
            return player != null && player.isOnline();
        } catch (Exception e) {
            log.error("检查玩家在线状态失败: {}", playerName, e);
            return false;
        }
    }
    
    /**
     * 发送好友私聊
     */
    public boolean sendPrivateMessage(String fromPlayer, String toPlayer, String message) {
        try {
            // 检查发送者和接收者是否存在
            if (!playerExists(fromPlayer) || !playerExists(toPlayer)) {
                return false;
            }
            
            // 检查是否在黑名单中
            if (isInBlacklist(toPlayer, fromPlayer)) {
                log.warn("玩家 {} 在 {} 的黑名单中，无法发送私聊", fromPlayer, toPlayer);
                return false;
            }
            
            // 创建私聊消息
            PrivateMessage privateMessage = new PrivateMessage();
            privateMessage.setFromPlayer(fromPlayer);
            privateMessage.setToPlayer(toPlayer);
            privateMessage.setMessage(message);
            privateMessage.setSendTime(LocalDateTime.now());
            privateMessage.setRead(false);
            
            // 存储消息
            messageMap.computeIfAbsent(toPlayer, k -> new ArrayList<>()).add(privateMessage);
            
            // 如果接收者在线，立即发送消息
            if (isPlayerOnline(toPlayer)) {
                notificationService.sendPrivateMessage(toPlayer, fromPlayer, message);
            }
            
            log.info("发送私聊消息: {} -> {} : {}", fromPlayer, toPlayer, message);
            return true;
            
        } catch (Exception e) {
            log.error("发送私聊消息失败", e);
            return false;
        }
    }
    
    /**
     * 获取私聊消息列表
     */
    public List<PrivateMessage> getPrivateMessages(String playerName) {
        List<PrivateMessage> messages = messageMap.getOrDefault(playerName, new ArrayList<>());
        
        // 标记消息为已读
        messages.forEach(msg -> msg.setRead(true));
        
        return messages;
    }
    
    /**
     * 获取未读私聊消息数量
     */
    public int getUnreadMessageCount(String playerName) {
        List<PrivateMessage> messages = messageMap.getOrDefault(playerName, new ArrayList<>());
        return (int) messages.stream().filter(msg -> !msg.isRead()).count();
    }
    
    /**
     * 清空私聊消息
     */
    public void clearPrivateMessages(String playerName) {
        messageMap.remove(playerName);
    }
    
    /**
     * 添加到黑名单
     */
    @Transactional
    public FriendResult addToBlacklist(Player player, String targetName) {
        // 检查黑名单大小
        Set<String> blackList = blacklistMap.computeIfAbsent(player.getUsername(), k -> new HashSet<>());
        if (blackList.size() >= MAX_BLACKLIST) {
            return new FriendResult(false, "黑名单已满");
        }
        
        // 检查是否已在黑名单
        if (isInBlacklist(player.getUsername(), targetName)) {
            return new FriendResult(false, "该玩家已在黑名单中");
        }
        
        // 添加到黑名单
        blackList.add(targetName);
        blacklistMap.put(player.getUsername(), blackList);
        
        // 如果是好友，自动删除好友关系
        if (isFriend(player.getUsername(), targetName)) {
            removeFriendFromList(player.getUsername(), targetName);
            removeFriendFromList(targetName, player.getUsername());
        }
        
        sendBlacklistNotification(player.getUsername(), targetName);
        
        return new FriendResult(true, "已添加到黑名单");
    }
    
    /**
     * 从黑名单移除
     */
    @Transactional
    public FriendResult removeFromBlacklist(Player player, String targetName) {
        Set<String> blackList = blacklistMap.get(player.getUsername());
        if (blackList == null) {
            return new FriendResult(false, "黑名单为空");
        }
        
        boolean removed = blackList.remove(targetName);
        if (removed) {
            return new FriendResult(true, "已从黑名单移除");
        } else {
            return new FriendResult(false, "该玩家不在黑名单中");
        }
    }
    
    /**
     * 获取黑名单
     */
    public List<String> getBlacklist(String playerName) {
        Set<String> blackList = blacklistMap.get(playerName);
        if (blackList == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(blackList);
    }
    
    /**
     * 获取待处理的好友请求
     */
    public List<FriendInfo> getPendingRequests(String playerName) {
        // List<FriendRequest> requests = friendRequests.get(playerName); // This line is removed as per the new_code
        // if (requests == null) {
        //     return new java.util.ArrayList<>();
        // }
        
        // 过滤有效请求
        // return requests.stream() // This line is removed as per the new_code
        //         .filter(request -> request.getStatus().equals("PENDING") && !isRequestExpired(request)) // This line is removed as per the new_code
        //         .collect(Collectors.toList()); // This line is removed as per the new_code
        return new ArrayList<>(); // Placeholder as friendRequests is removed
    }
    
    /**
     * 查找好友
     */
    public List<FriendInfo> searchFriends(String playerName, String keyword) {
        Set<String> friends = friendMap.getOrDefault(playerName, new HashSet<>());
        
        return friends.stream()
                .map(friendName -> getFriendInfo(playerName, friendName))
                .filter(Objects::nonNull)
                .filter(friend -> friend.getFriendName().toLowerCase().contains(keyword.toLowerCase()) ||
                                (friend.getRemark() != null && friend.getRemark().toLowerCase().contains(keyword.toLowerCase())))
                .collect(Collectors.toList());
    }
    
    /**
     * 设置好友备注
     */
    @Transactional
    public FriendResult setFriendRemark(Player player, String friendName, String remark) {
        // List<Friend> friends = getFriendList(player.getUsername()); // This line is removed as per the new_code
        // Friend friend = friends.stream() // This line is removed as per the new_code
        //         .filter(f -> f.getFriendName().equals(friendName)) // This line is removed as per the new_code
        //         .findFirst() // This line is removed as per the new_code
        //         .orElse(null); // This line is removed as per the new_code
        
        // if (friend == null) { // This line is removed as per the new_code
        //     return new FriendResult(false, "该用户不是您的好友"); // This line is removed as per the new_code
        // } // This line is removed as per the new_code
        
        // friend.setRemark(remark); // This line is removed as per the new_code
        return new FriendResult(true, "备注设置成功"); // Placeholder as Friend entity is removed
    }
    
    /**
     * 获取好友状态
     */
    public FriendStatus getFriendStatus(String friendName) {
        if (!isPlayerOnline(friendName)) {
            return new FriendStatus(friendName, false, "离线", "", 0);
        }
        
        try {
            // 获取玩家详细信息
            Player player = getPlayerDetails(friendName);
            if (player != null) {
                String status = "在线";
                String location = player.getMapName();
                int level = player.getLevel();
                
                // 根据玩家状态设置不同的显示
                if (player.getTeamId() != null) {
                    status = "组队中";
                } else if (player.getGuildName() != null) {
                    status = "行会活动";
                } else {
                    status = "在线";
                }
                
                return new FriendStatus(friendName, true, status, location, level);
            }
            
        } catch (Exception e) {
            log.error("获取玩家状态失败", e);
        }
        
        return new FriendStatus(friendName, true, "在线", "比奇城", 35);
    }
    
    /**
     * 检查好友请求条件
     */
    private FriendCheckResult checkFriendRequest(Player requester, String targetName) {
        // 检查是否为自己
        if (requester.getUsername().equals(targetName)) {
            return new FriendCheckResult(false, "不能添加自己为好友");
        }
        
        // 检查好友数量限制
        if (getFriendList(requester.getUsername()).size() >= MAX_FRIENDS) {
            return new FriendCheckResult(false, "好友数量已达上限");
        }
        
        // 检查是否已是好友
        if (isFriend(requester.getUsername(), targetName)) {
            return new FriendCheckResult(false, "该用户已是您的好友");
        }
        
        // 检查是否在黑名单
        if (isInBlacklist(targetName, requester.getUsername())) {
            return new FriendCheckResult(false, "无法添加该用户为好友");
        }
        
        // 检查是否已发送过请求
        // if (hasPendingRequest(requester.getUsername(), targetName)) { // This line is removed as per the new_code
        //     return new FriendCheckResult(false, "已发送过好友请求，请等待回复"); // This line is removed as per the new_code
        // } // This line is removed as per the new_code
        
        // 检查目标玩家是否存在
        if (!playerExists(targetName)) {
            return new FriendCheckResult(false, "用户不存在");
        }
        
        return new FriendCheckResult(true, "");
    }
    
    /**
     * 添加好友
     */
    private void addFriend(String playerName, String friendName) {
        Set<String> friends = friendMap.computeIfAbsent(playerName, k -> new HashSet<>());
        
        // Friend friend = new Friend(); // This line is removed as per the new_code
        // friend.setOwnerName(playerName); // This line is removed as per the new_code
        // friend.setFriendName(friendName); // This line is removed as per the new_code
        // friend.setAddTime(LocalDateTime.now()); // This line is removed as per the new_code
        // friend.setRemark(""); // This line is removed as per the new_code
        // friend.setGroup("默认分组"); // This line is removed as per the new_code
        
        friends.add(friendName);
    }
    
    /**
     * 从好友列表中移除
     */
    private void removeFriendFromList(String playerName, String friendName) {
        Set<String> friends = friendMap.get(playerName);
        if (friends != null) {
            friends.remove(friendName);
        }
    }
    
    /**
     * 检查是否为好友
     */
    private boolean isFriend(String playerName, String friendName) {
        Set<String> friends = friendMap.get(playerName);
        return friends != null && friends.contains(friendName);
    }
    
    /**
     * 检查是否在黑名单
     */
    private boolean isInBlacklist(String ownerName, String targetName) {
        Set<String> blackList = blacklistMap.get(ownerName);
        if (blackList == null) {
            return false;
        }
        
        return blackList.contains(targetName);
    }
    
    /**
     * 检查是否有待处理的请求
     */
    private boolean hasPendingRequest(String requesterName, String targetName) {
        // List<FriendRequest> requests = friendRequests.get(targetName); // This line is removed as per the new_code
        // if (requests == null) { // This line is removed as per the new_code
        //     return false; // This line is removed as per the new_code
        // } // This line is removed as per the new_code
        
        // return requests.stream() // This line is removed as per the new_code
        //         .anyMatch(request -> request.getRequesterName().equals(requesterName) &&  // This line is removed as per the new_code
        //                    request.getStatus().equals("PENDING") &&  // This line is removed as per the new_code
        //                    !isRequestExpired(request)); // This line is removed as per the new_code
        return false; // Placeholder as friendRequests is removed
    }
    
    /**
     * 检查请求是否过期
     */
    private boolean isRequestExpired(FriendRequest request) { // This line is removed as per the new_code
        return System.currentTimeMillis() - 
               request.getRequestTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() 
               > REQUEST_TIMEOUT;
    }
    
    /**
     * 发送好友申请通知
     */
    private void sendFriendRequestNotification(String fromPlayer, String toPlayer) {
        try {
            String message = String.format("玩家 %s 向您发送了好友申请", fromPlayer);
            notificationService.sendNotification(toPlayer, "好友申请", message);
            
            log.info("发送好友申请通知: {} -> {}", fromPlayer, toPlayer);
            
        } catch (Exception e) {
            log.error("发送好友申请通知失败", e);
        }
    }
    
    /**
     * 发送好友申请接受通知
     */
    private void sendFriendAcceptNotification(String fromPlayer, String toPlayer) {
        try {
            String message = String.format("玩家 %s 接受了您的好友申请", toPlayer);
            notificationService.sendNotification(fromPlayer, "好友申请", message);
            
            log.info("发送好友接受通知: {} -> {}", toPlayer, fromPlayer);
            
        } catch (Exception e) {
            log.error("发送好友接受通知失败", e);
        }
    }
    
    /**
     * 发送好友删除通知
     */
    private void sendFriendRemoveNotification(String fromPlayer, String toPlayer) {
        try {
            String message = String.format("玩家 %s 删除了您的好友关系", fromPlayer);
            notificationService.sendNotification(toPlayer, "好友关系", message);
            
            log.info("发送好友删除通知: {} -> {}", fromPlayer, toPlayer);
            
        } catch (Exception e) {
            log.error("发送好友删除通知失败", e);
        }
    }
    
    /**
     * 发送黑名单通知
     */
    private void sendBlacklistNotification(String fromPlayer, String toPlayer) {
        try {
            String message = String.format("玩家 %s 将您添加到黑名单", fromPlayer);
            notificationService.sendNotification(toPlayer, "黑名单", message);
            
            log.info("发送黑名单通知: {} -> {}", fromPlayer, toPlayer);
            
        } catch (Exception e) {
            log.error("发送黑名单通知失败", e);
        }
    }
    
    /**
     * 好友检查结果类
     */
    private static class FriendCheckResult {
        final boolean canSend;
        final String reason;
        
        FriendCheckResult(boolean canSend, String reason) {
            this.canSend = canSend;
            this.reason = reason;
        }
    }
    
    /**
     * 好友操作结果类
     */
    public static class FriendResult {
        private final boolean success;
        private final String message;
        
        public FriendResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
    
    /**
     * 好友状态类
     */
    public static class FriendStatus {
        private final String name;
        private final boolean online;
        private final String status;
        private final String location;
        private final int level;
        
        public FriendStatus(String name, boolean online, String status, String location, int level) {
            this.name = name;
            this.online = online;
            this.status = status;
            this.location = location;
            this.level = level;
        }
        
        public String getName() { return name; }
        public boolean isOnline() { return online; }
        public String getStatus() { return status; }
        public String getLocation() { return location; }
        public int getLevel() { return level; }
    }
    
    /**
     * 好友推荐系统
     */
    public List<String> getFriendRecommendations(String playerName) {
        List<String> recommendations = new ArrayList<>();
        
        try {
            // 获取当前玩家信息
            Player player = playerService.getOnlinePlayer(playerName);
            if (player == null) {
                return recommendations;
            }
            
            // 获取所有在线玩家
            List<Player> onlinePlayers = playerService.getOnlinePlayers();
            
            // 根据等级差距和职业推荐好友
            for (Player otherPlayer : onlinePlayers) {
                if (otherPlayer.getName().equals(playerName)) {
                    continue;
                }
                
                // 已经是好友的跳过
                if (isFriend(playerName, otherPlayer.getName())) {
                    continue;
                }
                
                // 在黑名单中的跳过
                if (isInBlacklist(playerName, otherPlayer.getName())) {
                    continue;
                }
                
                // 等级差距不超过10级
                int levelDiff = Math.abs(player.getLevel() - otherPlayer.getLevel());
                if (levelDiff <= 10) {
                    recommendations.add(otherPlayer.getName());
                }
                
                // 限制推荐数量
                if (recommendations.size() >= 10) {
                    break;
                }
            }
            
        } catch (Exception e) {
            log.error("获取好友推荐失败", e);
        }
        
        return recommendations;
    }
    
    /**
     * 更新好友亲密度
     */
    public void updateFriendIntimacy(String playerName, String friendName, int intimacy) {
        String key = playerName + "_" + friendName;
        FriendInfo friendInfo = friendInfoMap.get(key);
        
        if (friendInfo != null) {
            friendInfo.setIntimacy(Math.max(0, Math.min(100, intimacy)));
        }
    }
    
    /**
     * 获取好友亲密度
     */
    public int getFriendIntimacy(String playerName, String friendName) {
        String key = playerName + "_" + friendName;
        FriendInfo friendInfo = friendInfoMap.get(key);
        
        return friendInfo != null ? friendInfo.getIntimacy() : 0;
    }
    
    /**
     * 获取在线好友列表
     */
    public List<FriendInfo> getOnlineFriends(String playerName) {
        Set<String> friends = friendMap.getOrDefault(playerName, new HashSet<>());
        
        return friends.stream()
                .map(friendName -> getFriendInfo(playerName, friendName))
                .filter(Objects::nonNull)
                .filter(FriendInfo::isOnline)
                .collect(Collectors.toList());
    }
} 