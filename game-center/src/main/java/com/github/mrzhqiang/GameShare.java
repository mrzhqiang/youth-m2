package com.github.mrzhqiang;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GameShare {
  // 最大运行网关数量，通常是指游戏网关数量
  public static final int MAX_RUN_GATE_COUNT = 8;

  /* 以下，应该是启动的服务或网关编号，TODO 考虑用枚举来做 */
  public static final int T_DB_SERVER = 0;
  public static final int T_LOGIN_SRV = 1;
  public static final int T_LOG_SERVER = 2;
  public static final int T_M2_SERVER = 3;
  public static final int T_LOGIN_GATE = 4;
  public static final int T_SEL_GATE = 6;
  public static final int T_RUN_GATE = 8;

  /* 以下，是各个组件的名字，暂时不清楚有什么用 */
  public static final String BASIC_SECTION_NAME = "GameConfig";
  public static final String DB_SERVER_SECTION_NAME = "DBServer";
  public static final String LOGIN_SRV_SECTION_NAME = "LoginSrv";
  public static final String M2_SERVER_SECTION_NAME = "M2Server";
  public static final String LOG_SERVER_SECTION_NAME = "LogServer";
  public static final String RUN_GATE_SECTION_NAME = "RunGate";
  public static final String SEL_GATE_SECTION_NAME = "SelGate";
  public static final String LOGIN_GATE_SECTION_NAME = "LoginGate";

  // 所有IP地址，外网？
  public static final String S_ALL_IP_ADDR = "0.0.0.0";
  // 本机IP地址，默认
  public static final String S_LOCAL_IP_ADDR = "127.0.0.1";
  // 目前不准备开启双IP同区模式
  //public static final String S_LOCAL_IP_ADDR2 = "127.0.0.2";
  // 服务器最高在线人数
  public static final int N_LIMIT_ONLINE_USER = 2000;

  /* 下面是一些服务端配置文件的名字 */
  public static final String SERVER_CONFIG_DIR = "Config\\";
  public static final String SERVER_CONFIG_FILE = "Config.ini";
  public static final String SERVER_GAME_DATA_DIR = "GameData\\";
  public static final String SERVER_LOG_DIR = "Log\\";

  public static final String DB_SERVER_SECTION_NAME2 = "DBServer";
  public static final String DB_SERVER_DB_DIR = "DB\\";
  public static final String DB_SERVER_ALLOW_ADDR = "AllowAddr.txt";
  public static final String DB_SERVER_GATE_INFO = "GateInfo.txt";

  public static final String LOGIN_SERVER_SECTION_NAME2 = "LoginSrv";
  public static final String LOGIN_SERVER_CHR_LOG_NAME = SERVER_LOG_DIR + "ChrLog\\";
  public static final String LOGIN_SERVER_ALLOW_ADDR = "LoginSrv_AllowAddr.txt";
  public static final String LOGIN_SERVER_GETE_INFO = "LoginSrv_GateInfo.txt";
  public static final String LOGIN_SERVER_USER_LIMIT = "LoginSrv_UserLimit.txt";

  public static final String M2_SERVER_CONFIG_FILE = "!Setup.txt";
  public static final String M2_SERVER_SECTION_NAME1 = "Server";
  public static final String M2_SERVER_SECTION_NAME2 = "Share";
  public static final String M2_SERVER_GUILD_BASE = SERVER_GAME_DATA_DIR + "GuildBase\\";
  public static final String M2_SERVER_GUILD_DIR = M2_SERVER_GUILD_BASE + "Guilds\\";
  public static final String M2_SERVER_GUILD_FILE = M2_SERVER_GUILD_BASE + "GuildList.txt";
  public static final String M2_SERVER_CON_LOG_DIR = SERVER_LOG_DIR + "M2ConLog\\";
  public static final String M2_SERVER_CASTLE_DIR = SERVER_GAME_DATA_DIR + "Castle\\";
  public static final String M2_SERVER_CASTLE_FILE = M2_SERVER_CASTLE_DIR + "List.txt";
  public static final String M2_SERVER_LOG_DIR = SERVER_LOG_DIR + "M2Log\\";
  public static final String M2_SERVER_EMAIL_DIR = M2_SERVER_LOG_DIR + "M2Log\\";
  public static final String M2_SERVER_ENVIR_DIR = "Envir\\";
  public static final String M2_SERVER_MAP_DIR = "Map\\";
  public static final String M2_SERVER_ALLOW_ADDR = "M2server_AllowAddr.txt";
  public static final String M2_SERVER_E_MAIL_DIR = SERVER_GAME_DATA_DIR + "EMail\\";

  public static final String LOG_SERVER_SECTION_NAME2 = "LogDataServer";
  public static final String LOG_SERVER_BASE_DIR = SERVER_GAME_DATA_DIR + "GameLog\\";

  public static final String RUN_GATE_SECTION_NAME2 = "RunGate";

  public static final String SEL_GATE_SECTION_NAME2 = "SelGate";

  public static final String LOGIN_GATE_SECTION_NAME2 = "LoginGate";

  public static class Program {
    public boolean getStart;
    public boolean reStart;
    // 0,1,2,3 默认，正在启动，已启动，正在关闭
    public int startStatus;
    public String[] programFile = new String[50];
    public String[] directory = new String[100];
    // TProcessInformation
    // TODO 进程信息？
    public String processInfo;
    // THandle
    // TODO 进程处理？
    public String processHandle;
    // THandle
    // TODO 主窗口处理？
    public String mainFormHandle;
    // 窗口坐标
    public int mainFormX;
    public int mainFormY;
  }

  public static class DataListInfo {
    public String[] fileName = new String[255];
    // THandle
    public String mapFileHandle;
    // PChar
    public String mapFileBuffer;
    // TDateTime
    public LocalDate dateTime;
    // PChar
    public String data;
    public long dataSize;
    // TListItem
    public List<String> item;
  }

  public static abstract class CheckCode {
    abstract String dwThread();
    abstract String sThread();
  }

  public static class ServerConfig {
    public int mainFormX;
    public int mainFormY;
    public int gatePort;
    public boolean getStart;
    public String[] programFile = new String[50];
  }

  public static class DBServerConfig extends ServerConfig {
    public int serverPort;
  }

  public static class LoginSrvConfig extends ServerConfig {
    public int serverPort;
    public int monPort;
  }

  public static class M2ServerConfig extends ServerConfig {
    public int msgSrvPort;
  }

  public static class LogServerConfig extends ServerConfig {
    // no gatePort
    public int port;
  }

  public static class RunGateConfig extends ServerConfig {
    // no getStart
    public List<Boolean> getStartList = new ArrayList<>(MAX_RUN_GATE_COUNT-1);
    // no gatePort
    public List<Integer> gatePortList = new ArrayList<>(MAX_RUN_GATE_COUNT - 1);
  }

  public static class SelGateConfig extends ServerConfig {
    // no gatePort
    public int[] gatePort = new int[2];
    // no getStart
    public boolean getStart1;
    public boolean getStart2;
  }

  public static class LoginGateConfig extends ServerConfig {
  }

  public static class Config {
    public DBServerConfig dbServer;
    public LoginSrvConfig loginSrv;
    public M2ServerConfig m2Server;
    public LogServerConfig logServer;
    public RunGateConfig runGate;
    public SelGateConfig selGate;
    public LoginGateConfig loginGate;
  }

  public Program program = new Program();
  public DataListInfo dataListInfo = new DataListInfo();
  public Config config = new Config();

  public void loadConfig() {

  }

  public void saveConfig() {

  }

  public long runProgram(Program program, String handle, long waitTime) {
    return 0;
  }

  public int stopProgram(Program program, long waitTime) {
    return 0;
  }

  // desForm Handle
  public void sendProramMsg(int desForm, int ident, String sendMsg) {

  }

  public String g_sDataListAddrs = "127.0.0.1";
  public int g_wDataListPort = 18888;
  public String g_sDataListPassword = "123456";
  public boolean g_boGetDataListOK = false;

  public String g_DataListReadBuffer;
  public int g_nDataListReadLength;
  public List<String> g_GetDataList;

  public int g_nFormIdx;
  public Properties iniConf;
  public static final String g_sButtonStartGame = "启动游戏服务器%s";
  public static final String g_sButtonStopGame = "停止游戏服务器%s";
  public static final String g_sButtonStopStartGame = "中止启动游戏服务器%s";
  public static final String g_sButtonStopStopGame = "中止停止游戏服务器%s";

  public String g_sConfFile = ".\\Config.ini";
  public String g_sBackListFile = ".\\BackupList.txt";

  public String g_sGameName = "GameOfMir";
}

