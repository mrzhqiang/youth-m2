# M2Engine 架构文档

## 概述
M2Engine是一个基于Delphi开发的传奇游戏服务器引擎，负责处理游戏的核心逻辑、网络通信、数据存储和客户端交互。

## 主要架构组件

### 1. 核心引擎模块

#### 1.1 用户引擎 (UserEngine)
- **文件**: `UsrEngn.pas`
- **功能**: 管理游戏中的所有玩家对象和游戏逻辑
- **主要职责**:
  - 玩家对象管理 (`m_PlayObjectList`)
  - 怪物生成和管理 (`m_MonGenList`)
  - 物品系统管理 (`StdItemList`)
  - 魔法技能系统 (`m_MagicArr`)
  - 游戏消息处理 (`ProcessUserMessage`)
  - 游戏主循环 (`Run()`)

#### 1.2 前端引擎 (FrontEngine)
- **文件**: `FrnEngn.pas`
- **功能**: 处理客户端连接和数据库操作
- **主要职责**:
  - 数据库读写操作 (`m_LoadRcdList`, `m_SaveRcdList`)
  - 玩家数据加载和保存
  - 游戏时间管理 (`GetGameTime()`)
  - 多线程处理 (`Execute()`)

#### 1.3 网络通信引擎 (RunSock)
- **文件**: `RunSock.pas`
- **功能**: 处理客户端网络连接和消息传输
- **主要职责**:
  - 客户端连接管理 (`g_GateArr`)
  - 消息封包处理
  - 网络数据传输
  - 连接状态监控

### 2. 游戏对象系统

#### 2.1 基础对象 (BaseObject)
- **文件**: `ObjBase.pas`
- **功能**: 所有游戏对象的基类
- **主要属性**:
  - 位置坐标 (`m_nCurrX`, `m_nCurrY`)
  - 生命值系统 (`m_nHP`, `m_nMaxHP`)
  - 状态管理 (`m_nState`)

#### 2.2 玩家对象 (PlayObject)
- **文件**: `ObjPlay.pas`
- **功能**: 继承自BaseObject，管理玩家特有属性
- **主要功能**:
  - 角色属性管理 (`m_Abil`: 力量、敏捷、体力、智力等)
  - 背包系统 (`m_ItemList`)
  - 技能系统 (`m_MagicList`)
  - 任务系统 (`m_QuestFlag`)
  - 游戏币管理 (`m_nGameGold`)
  - 社交系统 (`m_DearName`, `m_MasterList`)

#### 2.3 怪物对象 (Monster)
- **文件**: `ObjMon.pas`, `ObjMon2.pas`, `ObjMon3.pas`
- **功能**: 各种怪物类型的实现
- **主要类型**:
  - 基础怪物 (`TMonster`)
  - 特殊怪物 (`TATMonster`, `TCowKingMonster`)
  - 守卫 (`TGuard`)
  - BOSS怪物

#### 2.4 NPC对象 (NPC)
- **文件**: `ObjNpc.pas`
- **功能**: 非玩家角色管理
- **主要功能**:
  - 商店NPC (`TMerchant`)
  - 任务NPC (`TQuestNPC`)
  - 功能NPC (`TFunctionalNPC`)

### 3. 地图和环境系统

#### 3.1 环境管理 (Environment)
- **文件**: `Envir.pas`
- **功能**: 管理游戏地图和环境
- **主要功能**:
  - 地图加载和管理 (`TEnvirnoment`)
  - 地图标志设置 (`TMapFlag`)
  - 安全区域管理
  - 地图事件处理
  - 怪物刷新区域管理

#### 3.2 事件系统 (Event)
- **文件**: `Event.pas`
- **功能**: 管理游戏中的各种事件
- **事件类型**:
  - 魔法事件 (`TMagicEvent`)
  - 火墙事件 (`TFireBurnEvent`)
  - 安全区事件 (`TSafeEvent`)
  - 机关事件 (`TMachineryEvent`)

### 4. 游戏系统

#### 4.1 物品系统 (Item)
- **文件**: `ItmUnit.pas`
- **功能**: 管理游戏物品
- **主要功能**:
  - 物品数据库 (`TStdItem`)
  - 物品属性系统
  - 物品绑定系统
  - 物品升级系统

#### 4.2 魔法系统 (Magic)
- **文件**: `Magic.pas`
- **功能**: 管理游戏技能和魔法
- **主要功能**:
  - 技能数据库 (`TMagic`)
  - 技能施放逻辑
  - 技能效果处理
  - 技能冷却管理

#### 4.3 行会系统 (Guild)
- **文件**: `Guild.pas`
- **功能**: 管理游戏行会
- **主要功能**:
  - 行会创建和管理
  - 行会成员管理
  - 行会等级系统
  - 行会战争系统

#### 4.4 城堡系统 (Castle)
- **文件**: `Castle.pas`
- **功能**: 管理攻城战系统
- **主要功能**:
  - 城堡占领机制
  - 攻城战事件处理
  - 城堡税收系统
  - 城堡守卫管理

### 5. 数据管理系统

#### 5.1 数据库接口 (LocalDB)
- **文件**: `LocalDB.pas`
- **功能**: 数据库操作接口
- **主要功能**:
  - 玩家数据读写
  - 物品数据管理
  - 游戏配置管理
  - 日志记录

#### 5.2 共享数据 (M2Share)
- **文件**: `M2Share.pas`
- **功能**: 全局共享数据和配置
- **主要内容**:
  - 全局变量定义
  - 游戏配置参数
  - 系统常量定义
  - 公共数据结构

### 6. 插件系统

#### 6.1 插件管理 (Plugin)
- **文件**: `PlugInManage.pas`
- **功能**: 管理游戏插件
- **主要功能**:
  - 插件加载和卸载
  - 插件接口管理
  - 插件配置管理

## 核心工作流程

### 1. 服务器启动流程
```
1. 主程序启动 (M2Server.dpr)
2. 初始化系统配置 (M2Share)
3. 加载数据库连接 (LocalDB)
4. 创建各种引擎实例
5. 加载游戏数据 (物品、怪物、魔法等)
6. 启动网络监听 (RunSock)
7. 进入主循环 (UserEngine.Run)
```

### 2. 游戏主循环
```
UserEngine.Run() 循环执行：
1. 处理玩家消息 (ProcessHumans)
2. 处理怪物逻辑 (ProcessMonsters)
3. 处理NPC逻辑 (ProcessNpcs)
4. 处理商人逻辑 (ProcessMerchants)
5. 处理地图事件 (ProcessEvents)
6. 处理任务系统 (ProcessMissions)
7. 处理地图传送门 (ProcessMapDoor)
8. 保存玩家数据 (SaveHumanRcd)
```

### 3. 客户端连接处理
```
1. 客户端连接到GateSocket
2. 验证玩家登录信息
3. 从数据库加载玩家数据 (FrontEngine)
4. 创建PlayObject对象
5. 将玩家加入游戏世界
6. 开始处理客户端消息
```

### 4. 消息处理流程
```
1. 接收客户端消息 (RunSock)
2. 解析消息包格式
3. 分发到对应处理函数 (ProcessUserMessage)
4. 执行游戏逻辑
5. 发送响应消息给客户端
```

## 主要数据结构

### 1. 玩家数据结构
```pascal
THumDataInfo = record
  sAccount: string;        // 账号
  sChrName: string;        // 角色名
  btJob: Byte;            // 职业
  btHair: Byte;           // 发型
  btSex: Byte;            // 性别
  btLevel: Byte;          // 等级
  Abil: TAbility;         // 基础属性
  nExp: LongWord;         // 经验值
  nHP: Word;              // 当前生命值
  nMP: Word;              // 当前魔法值
  sMapName: string;       // 当前地图
  wX, wY: Word;           // 当前坐标
  btDir: Byte;            // 当前方向
  // ... 其他属性
end;
```

### 2. 物品数据结构
```pascal
TStdItem = record
  Name: string;           // 物品名称
  StdMode: Byte;          // 物品类型
  Shape: Byte;            // 外观编号
  Weight: Byte;           // 重量
  AniCount: Byte;         // 动画帧数
  Source: Byte;           // 资源类型
  Reserved: Byte;         // 保留字段
  NeedIdentify: Byte;     // 是否需要鉴定
  DuraMax: Word;          // 最大持久度
  AC: Word;               // 防御力
  MAC: Word;              // 魔法防御
  DC: Word;               // 物理攻击
  MC: Word;               // 魔法攻击
  SC: Word;               // 道术攻击
  Need: Byte;             // 需求等级
  NeedLevel: Byte;        // 需求等级
  Price: Integer;         // 价格
  // ... 其他属性
end;
```

### 3. 地图标志结构
```pascal
TMapFlag = record
  boSAFE: Boolean;        // 安全区
  boFIGHT: Boolean;       // 可战斗区
  boDAY: Boolean;         // 白天地图
  boNIGHT: Boolean;       // 夜晚地图
  boFIGHT3: Boolean;      // 行会战区域
  boNORECONNECT: Boolean; // 不允许重连
  boNORANDOMMOVE: Boolean;// 不允许随机移动
  boNODRUG: Boolean;      // 不能使用药品
  boMINE: Boolean;        // 挖矿区
  boNOPOSITIONMOVE: Boolean; // 不能使用传送
  boNOATTACK: Boolean;    // 不能攻击
  boNOTHROWITEM: Boolean; // 不能扔物品
  boNOCHAT: Boolean;      // 不能聊天
  boNOSHOUTING: Boolean;  // 不能喊话
  // ... 其他标志
end;
```

## 配置文件系统

### 1. 主配置文件
- **!Setup.txt**: 基础服务器配置
- **!GameConfig.txt**: 游戏逻辑配置
- **!ItemEvent.txt**: 物品事件配置
- **!MonsterSayMsg.txt**: 怪物说话配置

### 2. 数据库文件
- **StdItems.DB**: 物品数据库
- **Monster.DB**: 怪物数据库
- **Magic.DB**: 魔法数据库
- **MapInfo.txt**: 地图信息配置

## 安全机制

### 1. 数据校验
- 客户端消息包校验
- 玩家数据完整性检查
- 物品数据验证
- 移动坐标验证

### 2. 反外挂机制
- 速度检测
- 坐标合法性验证
- 操作频率限制
- 数据包完整性校验

### 3. 数据保护
- 关键数据加密
- 定期数据备份
- 异常情况处理
- 日志记录系统

## 性能优化

### 1. 内存管理
- 对象池技术
- 内存回收机制
- 数据缓存策略

### 2. 网络优化
- 消息队列管理
- 网络包压缩
- 连接池管理

### 3. 数据库优化
- 批量数据操作
- 异步数据保存
- 索引优化

## 扩展性

### 1. 插件系统
- 标准插件接口
- 动态加载机制
- 插件间通信

### 2. 模块化设计
- 独立的功能模块
- 清晰的接口定义
- 低耦合设计

### 3. 配置化管理
- 灵活的配置系统
- 热更新支持
- 版本兼容性

## 总结

M2Engine是一个功能完善的游戏服务器引擎，采用模块化设计，具有良好的扩展性和稳定性。其核心架构包括用户引擎、前端引擎、网络通信、游戏对象系统、地图环境系统等多个模块，能够支持大规模的在线游戏服务。通过合理的数据结构设计和高效的算法实现，保证了游戏的流畅运行和数据的安全可靠。 