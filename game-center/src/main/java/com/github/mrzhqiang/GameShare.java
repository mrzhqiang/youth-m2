package com.github.mrzhqiang;

import com.github.mrzhqiang.models.Config;
import com.github.mrzhqiang.models.DataListInfo;
import com.github.mrzhqiang.models.Program;
import com.github.mrzhqiang.util.Sections;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.ini4j.Profile;
import org.ini4j.Wini;

public final class GameShare {
  private GameShare() {
    // no instance
  }

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

  public static Program program = new Program();
  public static DataListInfo dataListInfo = new DataListInfo();
  public static Config config = new Config();

  public static void loadConfig() {
    Profile.Section basicSectionName = iniConf.get(BASIC_SECTION_NAME);
    g_sGameDirectory = basicSectionName.get("GameDirectory", g_sGameDirectory);
    g_sHeroDBName = basicSectionName.get("HeroDBName", g_sHeroDBName);
    g_sGameName = basicSectionName.get("GameName", g_sGameName);
    g_sExtIPaddr = basicSectionName.get("ExtIPaddr", g_sGameDirectory);
    g_sExtIPaddr2 = basicSectionName.get("ExtIPaddr2", g_sGameDirectory);

    g_boAutoRunBak = Sections.getBoolean(basicSectionName, "AutoRunBak", g_boAutoRunBak);
    g_boIP2 = Sections.getBoolean(basicSectionName, "IP2", g_boIP2);
    g_boCloseWuXin = Sections.getBoolean(basicSectionName, "CloseWuXin", g_boCloseWuXin);

    // TODO should is getInstance mode
    config.dbServer.readOf(iniConf.get(DB_SERVER_SECTION_NAME));
    config.loginSrv.readOf(iniConf.get(LOGIN_SRV_SECTION_NAME));
    config.m2Server.readOf(iniConf.get(M2_SERVER_SECTION_NAME));
    config.logServer.readOf(iniConf.get(LOG_SERVER_SECTION_NAME));
    config.runGate.readOf(iniConf.get(RUN_GATE_SECTION_NAME));
    config.selGate.readOf(iniConf.get(SEL_GATE_SECTION_NAME));
    config.loginGate.readOf(iniConf.get(LOGIN_GATE_SECTION_NAME));
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

  public static String g_sDataListAddrs = "127.0.0.1";
  public static int g_wDataListPort = 18888;
  public static String g_sDataListPassword = "123456";
  public static boolean g_boGetDataListOK = false;

  public static String g_DataListReadBuffer;
  public static int g_nDataListReadLength;
  public static List<String> g_GetDataList;

  public static int g_nFormIdx;
  public static Wini iniConf = new Wini();
  public static final String g_sButtonStartGame = "启动游戏服务器%s";
  public static final String g_sButtonStopGame = "停止游戏服务器%s";
  public static final String g_sButtonStopStartGame = "中止启动游戏服务器%s";
  public static final String g_sButtonStopStopGame = "中止停止游戏服务器%s";

  public static final String g_sConfFile = ".\\Config.ini";
  public static final String g_sBackListFile = ".\\BackupList.txt";

  public static String g_sGameName = "GameOfMir";
  public static String g_sGameDirectory = ".\\";
  public static String g_sHeroDBName = "HeroDB";
  public static String g_sExtIPaddr = "127.0.0.1";
  public static String g_sExtIPaddr2 = "127.0.0.1";
  public static Boolean g_boAutoRunBak = false;
  public static Boolean g_boCloseWuXin = false;
  public static Boolean g_boIP2 = false;

  static {
    try {
      iniConf.load(new File(g_sConfFile));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

