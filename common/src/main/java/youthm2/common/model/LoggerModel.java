package youthm2.common.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志模型。
 *
 * @author qiang.zhang
 */
public final class LoggerModel {
  /**
   * 公共模块日志打印器。
   */
  public static final Logger COMMON = LoggerFactory.getLogger("common");
  /**
   * 引导模块日志打印器。
   */
  public static final Logger BOOTSTRAP = LoggerFactory.getLogger("bootstrap");
  /**
   * 启动模块日志打印器。
   */
  public static final Logger LAUNCHER = LoggerFactory.getLogger("launcher");
  /**
   * 数据库模块日志打印器。
   */
  public static final Logger DATABASE = LoggerFactory.getLogger("database");
  /**
   * 账号模块日志打印器。
   */
  public static final Logger ACCOUNT = LoggerFactory.getLogger("account");
  /**
   * 记录模块日志打印器。
   */
  public static final Logger LOGGER = LoggerFactory.getLogger("logger");
  /**
   * 核心模块日志打印器。
   */
  public static final Logger CORE = LoggerFactory.getLogger("core");
  /**
   * 游戏网关日志打印器。
   */
  public static final Logger GAME = LoggerFactory.getLogger("game");
  /**
   * 角色网关日志打印器。
   */
  public static final Logger ROLE = LoggerFactory.getLogger("role");
  /**
   * 登陆网关日志打印器
   */
  public static final Logger LOGIN = LoggerFactory.getLogger("login");
  /**
   * 排行榜插件日志打印器。
   */
  public static final Logger RANK = LoggerFactory.getLogger("rank");
}
