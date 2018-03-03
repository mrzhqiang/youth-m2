package com.github.mrzhqiang;

public class GameShare {
  // 最大运行网关数量，通常是指游戏网关数量
  public static final int MAX_RUNGATE_COUNT = 8;

  /* 以下，应该是启动的服务或网关编号，TODO 考虑用枚举来做 */
  public static final int T_DB_SERVER = 0;
  public static final int T_LOGIN_SERVER = 1;
  public static final int T_LOG_SERVER = 2;
  public static final int T_M2_SERVER = 3;
  public static final int T_LOGIN_GATE = 4;
  public static final int T_SEL_GATE = 6;
  public static final int T_RUN_GATE = 8;

  /* 以下，是各个组件的名字，暂时不清楚有什么用 */
  public static final String BASIC_SECTION_NAME = "GameConfig";
  public static final String DB_SERVER_SECTION_NAME = "DBServer";
  public static final String LOGIN_SERVER_SECTION_NAME = "LoginSrv";
  public static final String M2_SERVER_SECTION_NAME = "M2Server";
  public static final String LOG_SERVER_SECTION_NAME = "LogServer";
  public static final String RUN_GATE_SECTION_NAME = "RunGate";
  public static final String SEL_GATE_SECTION_NAME = "SelGate";
  public static final String LOGIN_GATE_SECTION_NAME = "LoginGate";




}
