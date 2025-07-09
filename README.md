# Mir2-Server - 传奇游戏服务器

[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Netty](https://img.shields.io/badge/Netty-4.1.77-blue.svg)](https://netty.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

基于Java Spring Boot和Netty的现代化传奇游戏服务器实现，完全重写原M2Engine，提供高性能、可扩展的游戏服务。

## 🚀 主要特性

### 核心功能
- **高性能网络引擎**：基于Netty 4.1的异步IO处理
- **多线程架构**：优化的线程池管理和任务调度
- **现代化设计**：Spring Boot 2.7微服务架构
- **完整的游戏逻辑**：玩家系统、物品装备、技能战斗等
- **强大的监控**：实时统计、性能监控、健康检查
- **高可用性**：支持集群部署、故障转移

### 技术优势
- **异步处理**：全异步消息处理，高并发支持
- **内存优化**：智能缓存管理，减少GC压力
- **配置灵活**：支持多环境配置，动态参数调整
- **扩展性强**：模块化设计，易于功能扩展
- **运维友好**：丰富的管理接口，便于部署和维护

## 📋 系统要求

- **Java**: 8 或更高版本
- **Maven**: 3.6 或更高版本
- **内存**: 建议4GB以上
- **操作系统**: Windows/Linux/macOS

## 🏗️ 系统架构

```
Mir2-Server
├── 网络通信层 (Network Layer)
│   ├── NettyServerEngine     # Netty服务器引擎
│   ├── ClientSession         # 客户端会话管理
│   ├── GameMessageHandler    # 游戏消息处理
│   ├── GameMessageDecoder    # 游戏消息解码器
│   ├── GameMessageEncoder    # 游戏消息编码器
│   ├── GameChannelHandler    # 游戏通道处理器
│   └── NetworkStatistics     # 网络统计监控
├── 游戏逻辑层 (Game Logic Layer)
│   ├── UserEngine            # 用户引擎
│   ├── Player                # 玩家对象模型
│   ├── BaseObject            # 游戏对象基类
│   ├── Item                  # 物品系统
│   ├── Skill                 # 技能系统
│   ├── GameMap               # 地图系统
│   ├── Guild                 # 行会系统
│   └── 枚举类                # 游戏常量定义
├── 数据存储层 (Data Layer)
│   ├── Entity                # 实体类
│   │   ├── UserEntity        # 用户实体
│   │   └── CharacterEntity   # 角色实体
│   └── Repository            # 数据访问接口
│       ├── UserRepository    # 用户数据访问
│       └── CharacterRepository # 角色数据访问
├── 业务服务层 (Service Layer)
│   ├── PlayerService         # 玩家服务
│   ├── ItemService           # 物品服务
│   ├── SkillService          # 技能服务
│   ├── MapService            # 地图服务
│   └── GuildService          # 行会服务
├── 配置管理层 (Configuration Layer)
│   ├── GameServerConfig      # 服务器配置
│   └── 环境配置文件          # 多环境配置
└── Web管理层 (Web Management Layer)
    └── GameController        # REST API管理接口
```

## 🎯 新增功能（已完成）

### 1. 完整的物品系统
- **物品分类**: 武器、防具、首饰、药品、材料等40+种物品类型
- **物品属性**: 攻击力、防御力、魔法属性、特殊效果等完整属性系统
- **物品鉴定**: 支持物品属性随机生成和鉴定系统
- **使用限制**: 等级、职业、性别等多维度使用限制
- **物品描述**: 详细的物品说明和属性展示

### 2. 完整的技能系统
- **三职业技能**: 战士、法师、道士各20+种技能
- **技能升级**: 技能等级、经验、熟练度系统
- **技能效果**: 伤害、治疗、增益、减益等多种技能效果
- **技能冷却**: 施法时间、冷却时间、魔法消耗等限制
- **技能学习**: 技能书学习、技能点分配机制

### 3. 完整的地图系统
- **地图管理**: 地图加载、地形数据、碰撞检测
- **对象管理**: 玩家、NPC、怪物、物品等对象统一管理
- **刷怪系统**: 怪物刷新点、刷新规则、数量控制
- **传送系统**: 传送点、传送条件、传送费用等
- **地图事件**: 支持地图特殊事件和触发器

### 4. 完整的数据存储层
- **用户管理**: 用户注册、登录、状态管理、统计信息
- **角色管理**: 角色创建、属性存储、数据同步、排行榜
- **数据仓库**: 完整的Repository接口，支持复杂查询和统计
- **事务管理**: 确保数据一致性，支持分布式事务

### 5. 完整的行会系统
- **行会管理**: 行会创建、解散、成员管理、权限控制
- **职位系统**: 会长、副会长、长老、精英、会员等职位
- **行会升级**: 行会等级、经验、成员数量扩展、技能加成
- **行会功能**: 行会资金、公告、留言、战争记录、建筑系统

### 6. 完整的业务服务层
- **玩家服务**: 用户认证、角色管理、游戏逻辑处理
- **在线管理**: 在线玩家缓存、会话管理、数据同步
- **游戏逻辑**: 移动处理、技能使用、聊天系统、PK系统
- **数据持久化**: 玩家数据实时保存、定时同步、异常恢复

## 🔧 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/yourusername/mir2-server.git
cd mir2-server
```

### 2. 构建项目
```bash
mvn clean compile
```

### 3. 启动服务器
```bash
mvn spring-boot:run
```

### 4. 验证启动
访问 `http://localhost:8080/api/game/health` 检查服务状态

## ⚙️ 配置说明

### 基础配置 (application.yml)
```yaml
# 服务器配置
server:
  port: 8080

# 游戏服务器配置
game:
  server:
    name: "传奇游戏服务器"
    max-players: 1000
    
    # 网络配置
    network:
      port: 7000
      boss-threads: 1
      worker-threads: 4
      max-connections: 1000
      read-timeout: 30
      write-timeout: 10
      heartbeat-interval: 60
    
    # 逻辑配置
    logic:
      main-loop-interval: 50
      save-interval: 60000
    
    # 安全配置
    security:
      anti-cheat: true
      speed-threshold: 500
```

### 数据库配置
```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mir2_game?useSSL=false&serverTimezone=UTC
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
```

### Redis配置
```yaml
# Redis配置
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
```

## 🎮 核心组件

### 网络引擎 (NettyServerEngine)
- **高性能网络通信**：基于Netty的异步IO处理
- **连接管理**：自动连接池管理和会话维护
- **消息编解码**：高效的消息序列化和反序列化
- **心跳检测**：自动检测客户端连接状态

### 用户引擎 (UserEngine)
- **玩家管理**：完整的玩家生命周期管理
- **游戏逻辑**：核心游戏逻辑处理
- **数据持久化**：自动定时保存玩家数据
- **反外挂系统**：多层次的安全检测

### 游戏对象系统
- **BaseObject**：游戏对象基类，提供基础属性和方法
- **Player**：玩家对象，包含完整的角色系统
- **装备系统**：物品、装备、背包管理
- **技能系统**：技能学习和使用机制

## 📊 监控和管理

### REST API接口

#### 服务器状态
```http
GET /api/game/status
```
返回服务器运行状态、内存使用情况、在线玩家数量等

#### 网络统计
```http
GET /api/game/statistics
```
返回网络连接统计、消息处理统计、性能指标等

#### 在线玩家
```http
GET /api/game/players/online
```
返回当前在线玩家列表

#### 玩家管理
```http
POST /api/game/players/kick?characterName=角色名
```
踢出指定玩家

#### 系统消息
```http
POST /api/game/message/system
Content-Type: application/json

{
    "message": "系统维护通知"
}
```

#### 服务器控制
```http
POST /api/game/server/start   # 启动服务器
POST /api/game/server/stop    # 停止服务器
POST /api/game/server/restart # 重启服务器
```

### 统计监控
- **实时统计**：连接数、消息数、流量统计
- **性能监控**：处理时间、成功率、资源使用
- **健康检查**：系统状态、组件状态监控

## 🔒 安全特性

### 反外挂系统
- **速度检测**：移动速度异常检测
- **行为分析**：异常操作模式识别
- **实时监控**：持续的安全状态监控

### 网络安全
- **连接限制**：IP连接数限制
- **消息验证**：消息格式和内容验证
- **超时处理**：自动断开异常连接

## 🚀 部署指南

### 开发环境
```bash
# 启动开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 生产环境
```bash
# 构建生产包
mvn clean package -Pproduction

# 启动生产服务
java -jar target/mir2-server-1.0.0.jar --spring.profiles.active=prod
```

### Docker部署
```dockerfile
FROM openjdk:8-jdk-alpine
COPY target/mir2-server-1.0.0.jar app.jar
EXPOSE 8080 7000
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# 构建镜像
docker build -t mir2-server:latest .

# 运行容器
docker run -d -p 8080:8080 -p 7000:7000 mir2-server:latest
```

## 📈 性能优化

### JVM调优
```bash
java -Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 \
     -XX:+PrintGCDetails -XX:+PrintGCTimeStamps \
     -jar mir2-server.jar
```

### 网络优化
- **连接池配置**：合理的连接池大小
- **缓冲区配置**：适当的发送/接收缓冲区
- **线程池优化**：CPU核心数相关的线程配置

## 🛠️ 开发指南

### 添加新功能
1. 在相应的包中创建新的类文件
2. 实现业务逻辑
3. 添加相应的配置和测试
4. 更新API文档

### 消息处理扩展
1. 在`GameMessageType`中添加新的消息类型
2. 在`GameMessageHandler`中添加处理方法
3. 实现客户端协议对应的处理逻辑

### 数据库扩展
1. 创建实体类
2. 创建Repository接口
3. 添加Service层逻辑
4. 更新数据库配置

## 📚 API文档

详细的API文档请参考：
- [游戏服务器API文档](docs/api.md)
- [消息协议文档](docs/protocol.md)
- [数据库设计文档](docs/database.md)

## 🤝 贡献指南

1. Fork项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 📄 许可证

本项目采用MIT许可证 - 详情请查看 [LICENSE](LICENSE) 文件

## 🙏 致谢

- 感谢原M2Engine的开发者们
- 感谢Spring Boot和Netty社区
- 感谢所有贡献者和测试者

## 📞 联系我们

- 项目主页: https://github.com/yourusername/mir2-server
- 问题反馈: https://github.com/yourusername/mir2-server/issues
- 邮箱: your.email@example.com

---

**注意**：本项目仅用于学习和研究目的，请遵守相关法律法规。 