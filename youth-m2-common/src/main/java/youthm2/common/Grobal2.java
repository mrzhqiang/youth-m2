package youthm2.common;

import lombok.experimental.UtilityClass;

import java.util.Date;

/**
 * 游戏全局常量和数据结构定义
 * 从Pascal文件Grobal2.pas翻译而来
 */
@UtilityClass
public class Grobal2 {

    // 公共版本定义
    public static final int PUBLIC_TEST = 0;
    public static final int PUBLIC_RELEASE = 1;
    public static final int PUBLIC_FREE = 2;
    public static final int PUBLIC_VER = PUBLIC_FREE;
    public static final int VAR_FREE = 0;

    // 界面版本定义
    public static final int VAR_DEFAULT = 0;  // 剑侠界面
    public static final int VAR_MIR2 = 1;     // 盛大界面
    public static final int VAR_INTERFACE = VAR_MIR2;

    // 客户端版本号
    public static final int CLIENT_VERSION_NUMBER = 20201212;
    public static final int CLIENT_VERSION_MARK = 0x1FFFFFF;
    public static final int CLIENT_VERSIONEX_MIR2 = 0x2000000;
    public static final int CLIENT_VERSIONEX_TEST = 0x4000000;
    public static final int CLIENT_VERSIONEX_FREE = 0x8000000;
    public static final int CLIENT_VERSIONEX_DEBUG = 0x10000000;

    // 程序信息
    public static final String G_SPROGRAM = "程序制作: mrzhqiang";
    public static final String G_SWEBSITE = "兰达尔引擎，永久免费";

    // 重要常量
    public static final int SIZEOFTHUMAN = 111867;
    public static final int GROBAL2VER = CLIENT_VERSION_NUMBER;
    public static final int GATEMAXSESSION = 1000;
    public static final int PLAYOBJECTINDEXCOUNT = 1000000;

    // 强化数组
    public static final byte[] CAN_STRENGTHEN_ARR = {0, 3, 6, 9, 12, 15, 18};
    public static final byte[] CAN_STRENGTHEN_MAX = {16, 20, 21, 15, 24, 6};

    // 路径和名称长度
    public static final int MAXPATHLEN = 255;
    public static final int DIRPATHLEN = 80;
    public static final int MAP_NAME_LEN = 16;
    public static final int ACTOR_NAME_LEN = 14;
    public static final int GUILD_NAME_LEN = 14;
    public static final int DEFBLOCKSIZE = 27;
    public static final int BUFFERSIZE = 30000;
    public static final int DATABUFFERSIZE = 20000;
    public static final int DATA_BUFSIZE = 8192;

    // 游戏相关常量
    public static final int GROUPMAX = 11;
    public static final int BAGGOLD = 5000000;
    public static final int BODYLUCKUNIT = 10;
    public static final int MAX_STATUS_ATTRIBUTE = 12;
    public static final int MAXDEALITEMCOUNT = 12;
    public static final int MAXPULLULATION = 10000 * 60;
    public static final int MIDPULLULATION = 60;
    public static final int MAXTAXISCOUNT = 100;

    // 字符串常量
    public static final String STRING_GOLDNAME = "金币";
    public static final String STRING_BINDGOLDNAME = "绑定金币";
    public static final String STRING_GAMEPOINT = "点卷";
    public static final String STRING_GAMEGOLD = "元宝";
    public static final String STRING_GAMEDIAMOND = "积分";
    public static final String STRING_CREDITPOINT = "声望";
    public static final String STRING_CUSTOMVARIABLE = "人物变量";

    // 日志类型
    public static final int LOG_PLAYDIE = 0;           // 人物死亡
    public static final int LOG_GOLDCHANGED = 1;       // 金币改变
    public static final int LOG_BINDGOLDCHANGED = 2;   // 绑金改变
    public static final int LOG_GAMEPOINTCHANGED = 3;  // 点卷改变
    public static final int LOG_GAMEGOLDCHANGED = 4;   // 元宝改变
    public static final int LOG_GAMEDIAMONDCHANGED = 5; // 积分改变
    public static final int LOG_ITEMDURACHANGE = 6;    // 叠加改变
    public static final int LOG_ADDITEM = 7;           // 增加物品
    public static final int LOG_DELITEM = 8;           // 减少物品
    public static final int LOG_STORAGE = 9;           // 仓库存取
    public static final int LOG_ITEMLEVEL = 10;        // 强化改变
    public static final int LOG_UPDATEITEM = 11;       // 调整装备
    public static final int LOG_CREDITPOINT = 12;      // 声望改变
    public static final int LOG_CUSTOMVARIABLE = 13;   // 人物变量

    // 整型操作
    public static final int INT_ADD = 0;
    public static final int INT_DEL = 1;
    public static final int INT_SET = 2;

    // 地图模式
    public static final int NEWMAPMODE = 0;
    public static final int OLDMAPMODE = 1;
    public static final int CHANGEMAPMODE = OLDMAPMODE;

    // 最大值常量
    public static final int MAXINTCOUNT = 2000000000;
    public static final int MAXWORDCOUNT = 65535;
    public static final int MAXBYTECOUNT = 255;

    // 方向常量
    public static final int DR_UP = 0;        // 正北
    public static final int DR_UPRIGHT = 1;   // 东北向
    public static final int DR_RIGHT = 2;     // 东
    public static final int DR_DOWNRIGHT = 3; // 东南向
    public static final int DR_DOWN = 4;      // 南
    public static final int DR_DOWNLEFT = 5;  // 西南向
    public static final int DR_LEFT = 6;      // 西
    public static final int DR_UPLEFT = 7;    // 西北向

    // 装备位置
    public static final int U_DRESS = 0;      // 衣服
    public static final int U_WEAPON = 1;     // 武器
    public static final int U_HELMET = 2;     // 头盔
    public static final int U_NECKLACE = 3;   // 项链
    public static final int U_RIGHTHAND = 4;  // 右手
    public static final int U_ARMRINGL = 5;   // 左手手镯
    public static final int U_ARMRINGR = 6;   // 右手手镯
    public static final int U_RINGL = 7;      // 左戒指
    public static final int U_RINGR = 8;      // 右戒指
    public static final int U_BUJUK = 9;      // 物品
    public static final int U_BELT = 10;      // 腰带
    public static final int U_BOOTS = 11;     // 鞋
    public static final int U_CHARM = 12;     // 宝石
    public static final int U_HOUSE = 13;
    public static final int U_CIMELIA = 14;
    public static final int U_REIN = 16;
    public static final int U_BELL = 17;
    public static final int U_SADDLE = 18;
    public static final int U_DECORATION = 19;
    public static final int U_NAIL = 20;

    // 中毒和状态类型
    public static final int POISON_DECHEALTH = 0;     // 绿毒
    public static final int POISON_DAMAGEARMOR = 1;   // 红毒
    public static final int POISON_STONE = 5;         // 麻痹
    public static final int POISON_COBWEB = 6;        // 蛛网
    public static final int STATE_BUBBLEDEFENCEUPEX = 7; // 金刚护盾
    public static final int STATE_TRANSPARENT = 8;    // 隐身
    public static final int STATE_DEFENCEUP = 9;      // 加防
    public static final int STATE_MAGDEFENCEUP = 10;  // 加魔
    public static final int STATE_BUBBLEDEFENCEUP = 11; // 魔法盾

    // 物品数量限制
    public static final int MAXSHOPITEMS = 12;
    public static final int MAXBAGITEMS = 105;
    public static final int MAXMAGIC = 30;
    public static final int MAXUSEITEMS = 16;
    public static final int MAXSTORAGEITEMS = 49;
    public static final int MAXRETURNITEMS = 4;
    public static final int MAXAPPENDBAGITEMS = 3;
    public static final int MAXFLUTECOUNT = 3;
    public static final int LOGICALMAPUNIT = 40;
    public static final int MAXFRIENDS = 30;
    public static final int MAXRETURNITEMSCOUNT = 30;
    public static final int MAXEMAILCOUNT = 20;
    public static final int MAXPHOTODATASIZE = 4000;
    public static final int MAXITEMSSETUPCOUNT = 1000;

    // 动作类型枚举
    public enum OnActionType {
        AT_WALK,      // 走
        AT_PUSHED,    // 推
        AT_HIT,       // 击中
        AT_SPELL,     // 符咒
        AT_CHANGEMAP, // 切换地图
        AT_STRUCK     // 打击
    }

    // 物品类型枚举
    public enum StdMode {
        TM_DRUG,        // 药品
        TM_RESTRICT,    // 使用次数物品
        TM_REEL,        // 特殊物品
        TM_BOOK,        // 技能书籍
        TM_WEAPON,      // 武器
        TM_ROCK,        // 气血石
        TM_COWRY,       // 宝物
        TM_ADDBAG,      // 额外包裹
        TM_HOUSE,       // 坐骑
        TM_DRESS,       // 男、女衣服
        TM_HELMET,      // 头盔
        TM_NECKLACE,    // 项链
        TM_RING,        // 戒指
        TM_ARMRING,     // 手镯
        TM_AMULET,      // 毒符
        TM_BELT,        // 腰带
        TM_BOOT,        // 靴子
        TM_STONE,       // 宝石
        TM_LIGHT,       // 勋章
        TM_OPEN,        // 解包、双击触发
        TM_FLESH,       // 肉类
        TM_ORE,         // 矿类
        TM_DICE,        // 骰子
        TM_MISSION,     // 任务物品
        TM_MISSIONSP,   // 任务物品-可叠加
        TM_MAKESTONE,   // 装备宝石
        TM_MAKEPROP,    // 普通道具
        TM_MAKEPROPSP,  // 普通道具-可叠加
        TM_UNKNOWN,     // 未知
        TM_RESETSTONE,  // 洗装备属性石
        TM_PROP,        // 普通道具
        TM_PROPSP,      // 普通道具-可叠加
        TM_REVIVE,      // 还魂丹
        TM_REIN,        // 坐骑缰绳
        TM_BELL,        // 坐骑铃铛
        TM_SADDLE,      // 坐骑马鞍
        TM_DECORATION,  // 坐骑装饰
        TM_NAIL         // 坐骑脚钉
    }

    // 消息颜色枚举
    public enum MsgColor {
        C_RED,
        C_GREEN,
        C_BLUE,
        C_WHITE
    }

    // 消息类型枚举
    public enum MsgType {
        T_NOTICE,  // 公告
        T_HINT,    // 暗示
        T_SYSTEM,  // 系统
        T_SAY,
        T_MON,
        T_GM,
        T_CUST,
        T_CASTLE,
        T_CUDT
    }

    // 默认消息结构体
    public static class DefaultMessage {
        public int recog;      // 识别码
        public int ident;
        public int param;
        public int tag;
        public int series;

        public DefaultMessage() {
        }

        public DefaultMessage(int recog, int ident, int param, int tag, int series) {
            this.recog = recog;
            this.ident = ident;
            this.param = param;
            this.tag = tag;
            this.series = series;
        }
    }

    // 短消息结构体
    public static class ShortMessage {
        public short ident;
        public short wMsg;

        public ShortMessage() {
        }

        public ShortMessage(short ident, short wMsg) {
            this.ident = ident;
            this.wMsg = wMsg;
        }
    }

    // 消息体结构体
    public static class MessageBodyW {
        public short param1;
        public short param2;
        public short tag1;
        public short tag2;

        public MessageBodyW() {
        }

        public MessageBodyW(short param1, short param2, short tag1, short tag2) {
            this.param1 = param1;
            this.param2 = param2;
            this.tag1 = tag1;
            this.tag2 = tag2;
        }
    }

    // 长消息体结构体
    public static class MessageBodyWL {
        public int lParam1;
        public int lParam2;
        public int lTag1;
        public int lTag2;

        public MessageBodyWL() {
        }

        public MessageBodyWL(int lParam1, int lParam2, int lTag1, int lTag2) {
            this.lParam1 = lParam1;
            this.lParam2 = lParam2;
            this.lTag1 = lTag1;
            this.lTag2 = lTag2;
        }
    }

    // 用户物品结构体
    public static class UserItem {
        public int idx;
        public int makeIndex;
        public short dura;
        public short duraMax;

        public UserItem() {
        }

        public UserItem(int idx, int makeIndex, short dura, short duraMax) {
            this.idx = idx;
            this.makeIndex = makeIndex;
            this.dura = dura;
            this.duraMax = duraMax;
        }
    }

    // 角色能力结构体
    public static class Ability {
        public short level;
        public int ac;
        public int mac;
        public int dc;
        public int mc;
        public int sc;
        public int hp;
        public int mp;
        public int maxHp;
        public int maxMp;
        public long exp;
        public long maxExp;
        public short weight;
        public short maxWeight;
        public short handWeight;
        public short maxHandWeight;
        public short wearWeight;
        public short maxWearWeight;

        public Ability() {
        }
    }

    // 会话信息结构体
    public static class SessionInfo {
        public String account;      // 账号，最大16字符
        public String ipAddr;       // IP地址，最大15字符
        public int sessionID;       // 会话ID
        public int payMent;
        public int payMode;
        public int userCDKey;
        public int gameGold;
        public int checkEMail;
        public int sessionStatus;
        public long startTick;
        public long activeTick;
        public int refCount;

        public SessionInfo() {
        }
    }

    // 公会信息结构体
    public static class ClientGuildInfo {
        public byte myRank;
        public String guildName;        // 最大14字符
        public byte maxMemberCount;
        public String createName;       // 最大14字符
        public Date createTime;
        public byte guildLevel;
        public int guildMoney;
        public int buildPoint;          // 建筑度
        public int flourishingPoint;    // 繁荣度
        public int stabilityPoint;      // 安定度
        public int activityPoint;       // 人气度
        public int levelGuildMoney;
        public int levelBuildPoint;
        public int levelFlourishingPoint;
        public int levelStabilityPoint;
        public short levelActivityPoint;
        public byte kickMonExp;
        public byte kickMonAttack;
        public short maxActivityPoint;

        public ClientGuildInfo() {
        }
    }

    // 角色描述结构体
    public static class CharDesc {
        public int feature;
        public int status;
        public byte strengthenIdx;
        public byte wuXin;

        public CharDesc() {
        }

        public CharDesc(int feature, int status, byte strengthenIdx, byte wuXin) {
            this.feature = feature;
            this.status = status;
            this.strengthenIdx = strengthenIdx;
            this.wuXin = wuXin;
        }
    }

    // 制作物品信息结构体
    public static class MakeItemInfo {
        public short wIdent;
        public short wCount;
        public boolean notGet;

        public MakeItemInfo() {
        }

        public MakeItemInfo(short wIdent, short wCount, boolean notGet) {
            this.wIdent = wIdent;
            this.wCount = wCount;
            this.notGet = notGet;
        }
    }

    // 制作物品结构体
    public static class MakeItem {
        public short wIdx;
        public MakeItemInfo[] itemArr = new MakeItemInfo[6];
        public int money;
        public byte rate;
        public byte maxRate;

        public MakeItem() {
            for (int i = 0; i < 6; i++) {
                itemArr[i] = new MakeItemInfo();
            }
        }
    }

    // 动态变量结构体
    public static class DynamicVar {
        public String name;
        public VarType varType;
        public int nInternet;
        public String sString;

        public DynamicVar() {
        }

        public DynamicVar(String name, VarType varType, int nInternet, String sString) {
            this.name = name;
            this.varType = varType;
            this.nInternet = nInternet;
            this.sString = sString;
        }
    }

    // 变量类型枚举
    public enum VarType {
        V_NONE,
        V_INTEGER,
        V_STRING
    }

    // 数组类型定义
    public static class Arrays {
        public static final int[] questFlag = new int[801];
        public static final int[] missionFlag = new int[801];
        public static final short[] statusTime = new short[MAX_STATUS_ATTRIBUTE];
        public static final int[] humCustomVariable = new int[1000];
        public static final String[] humBVariable = new String[1000];
        public static final String[] humMasterName = new String[7];
        public static final UserItem[] bagItems = new UserItem[MAXBAGITEMS];
        public static final UserItem[] useItems = new UserItem[MAXUSEITEMS];
        public static final UserItem[] storageItems = new UserItem[MAXSTORAGEITEMS];

        static {
            // 初始化数组
            for (int i = 0; i < MAXBAGITEMS; i++) {
                bagItems[i] = new UserItem();
            }
            for (int i = 0; i < MAXUSEITEMS; i++) {
                useItems[i] = new UserItem();
            }
            for (int i = 0; i < MAXSTORAGEITEMS; i++) {
                storageItems[i] = new UserItem();
            }
            for (int i = 0; i < 1000; i++) {
                humBVariable[i] = "";
            }
            for (int i = 0; i < 7; i++) {
                humMasterName[i] = "";
            }
        }
    }
}
