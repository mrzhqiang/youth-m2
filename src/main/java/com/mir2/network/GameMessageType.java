package com.mir2.network;

/**
 * 游戏消息类型常量
 * 
 * <p>定义传奇游戏中使用的各种消息类型。
 * 这些常量与客户端协议保持一致，确保消息的正确处理。</p>
 * 
 * @author Mir2 Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public final class GameMessageType {
    
    // ================================
    // 连接和认证相关消息
    // ================================
    
    /** 心跳包 */
    public static final short HEARTBEAT = 0x001;
    
    /** 客户端连接请求 */
    public static final short CLIENT_CONNECT = 0x002;
    
    /** 服务器响应连接 */
    public static final short SERVER_CONNECT_ACK = 0x003;
    
    /** 客户端断开连接 */
    public static final short CLIENT_DISCONNECT = 0x004;
    
    /** 版本验证 */
    public static final short VERSION_CHECK = 0x005;
    
    // ================================
    // 登录和账户相关消息
    // ================================
    
    /** 登录请求 */
    public static final short LOGIN_REQUEST = 0x100;
    
    /** 登录成功 */
    public static final short LOGIN_SUCCESS = 0x101;
    
    /** 登录失败 */
    public static final short LOGIN_FAILED = 0x102;
    
    /** 退出登录 */
    public static final short LOGOUT = 0x103;
    
    /** 创建角色 */
    public static final short CREATE_CHARACTER = 0x110;
    
    /** 删除角色 */
    public static final short DELETE_CHARACTER = 0x111;
    
    /** 选择角色 */
    public static final short SELECT_CHARACTER = 0x112;
    
    /** 角色列表 */
    public static final short CHARACTER_LIST = 0x113;
    
    // ================================
    // 游戏世界相关消息
    // ================================
    
    /** 进入游戏 */
    public static final short ENTER_GAME = 0x200;
    
    /** 离开游戏 */
    public static final short LEAVE_GAME = 0x201;
    
    /** 地图切换 */
    public static final short MAP_CHANGE = 0x202;
    
    /** 获取地图信息 */
    public static final short GET_MAP_INFO = 0x203;
    
    // ================================
    // 移动和位置相关消息
    // ================================
    
    /** 移动请求 */
    public static final short MOVE_REQUEST = 0x300;
    
    /** 移动确认 */
    public static final short MOVE_ACK = 0x301;
    
    /** 位置同步 */
    public static final short POSITION_SYNC = 0x302;
    
    /** 瞬移 */
    public static final short TELEPORT = 0x303;
    
    // ================================
    // 聊天和通信相关消息
    // ================================
    
    /** 聊天消息 */
    public static final short CHAT_MESSAGE = 0x400;
    
    /** 私聊消息 */
    public static final short PRIVATE_MESSAGE = 0x401;
    
    /** 系统消息 */
    public static final short SYSTEM_MESSAGE = 0x402;
    
    /** 公告消息 */
    public static final short NOTICE_MESSAGE = 0x403;
    
    // ================================
    // 战斗相关消息
    // ================================
    
    /** 攻击请求 */
    public static final short ATTACK_REQUEST = 0x500;
    
    /** 攻击结果 */
    public static final short ATTACK_RESULT = 0x501;
    
    /** 使用技能 */
    public static final short USE_SKILL = 0x502;
    
    /** 技能效果 */
    public static final short SKILL_EFFECT = 0x503;
    
    /** 生命值变化 */
    public static final short HP_CHANGE = 0x504;
    
    /** 魔法值变化 */
    public static final short MP_CHANGE = 0x505;
    
    // ================================
    // 物品和装备相关消息
    // ================================
    
    /** 拾取物品 */
    public static final short PICK_UP_ITEM = 0x600;
    
    /** 丢弃物品 */
    public static final short DROP_ITEM = 0x601;
    
    /** 使用物品 */
    public static final short USE_ITEM = 0x602;
    
    /** 装备物品 */
    public static final short EQUIP_ITEM = 0x603;
    
    /** 卸下装备 */
    public static final short UNEQUIP_ITEM = 0x604;
    
    /** 背包更新 */
    public static final short INVENTORY_UPDATE = 0x605;
    
    /** 装备更新 */
    public static final short EQUIPMENT_UPDATE = 0x606;
    
    // ================================
    // 交易相关消息
    // ================================
    
    /** 交易请求 */
    public static final short TRADE_REQUEST = 0x700;
    
    /** 交易确认 */
    public static final short TRADE_CONFIRM = 0x701;
    
    /** 交易取消 */
    public static final short TRADE_CANCEL = 0x702;
    
    /** 交易完成 */
    public static final short TRADE_COMPLETE = 0x703;
    
    // ================================
    // 状态和属性相关消息
    // ================================
    
    /** 属性更新 */
    public static final short ATTRIBUTE_UPDATE = 0x800;
    
    /** 经验值更新 */
    public static final short EXP_UPDATE = 0x801;
    
    /** 等级提升 */
    public static final short LEVEL_UP = 0x802;
    
    /** 状态效果 */
    public static final short STATUS_EFFECT = 0x803;
    
    // ================================
    // 社交和组队相关消息
    // ================================
    
    /** 组队邀请 */
    public static final short PARTY_INVITE = 0x900;
    
    /** 加入队伍 */
    public static final short JOIN_PARTY = 0x901;
    
    /** 离开队伍 */
    public static final short LEAVE_PARTY = 0x902;
    
    /** 队伍信息 */
    public static final short PARTY_INFO = 0x903;
    
    // ================================
    // 错误和异常消息
    // ================================
    
    /** 一般错误 */
    public static final short ERROR = 0xF00;
    
    /** 服务器繁忙 */
    public static final short SERVER_BUSY = 0xF01;
    
    /** 非法操作 */
    public static final short ILLEGAL_OPERATION = 0xF02;
    
    /** 反外挂检测 */
    public static final short ANTI_CHEAT = 0xF03;
    
    /** 私有构造函数，防止实例化 */
    private GameMessageType() {
        throw new UnsupportedOperationException("常量类不能实例化");
    }
    
    /**
     * 获取消息类型的描述
     * 
     * @param messageType 消息类型
     * @return 消息类型描述
     */
    public static String getDescription(short messageType) {
        switch (messageType) {
            case HEARTBEAT: return "心跳包";
            case CLIENT_CONNECT: return "客户端连接";
            case LOGIN_REQUEST: return "登录请求";
            case LOGIN_SUCCESS: return "登录成功";
            case LOGIN_FAILED: return "登录失败";
            case ENTER_GAME: return "进入游戏";
            case MOVE_REQUEST: return "移动请求";
            case CHAT_MESSAGE: return "聊天消息";
            case ATTACK_REQUEST: return "攻击请求";
            case USE_ITEM: return "使用物品";
            case TRADE_REQUEST: return "交易请求";
            case ATTRIBUTE_UPDATE: return "属性更新";
            case PARTY_INVITE: return "组队邀请";
            case ERROR: return "错误消息";
            default: return "未知消息类型(" + messageType + ")";
        }
    }
} 