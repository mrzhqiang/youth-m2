package youthm2.controllers;

import youthm2.GameCenter;
import youthm2.GameShare;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javax.swing.Timer;

public final class GameCenterController {

  private static boolean isGateStop = false;

  private static Timer startGameTimer = new Timer(1000, e -> System.out.println("这里是在启动什么？"));
  private static Timer stopGameTimer = new Timer(1000, e -> System.out.println("这里是在停止什么？"));
  private static Timer checkRunTimer = new Timer(1000, e -> System.out.println("这里是在检查什么？"));

  private void startGame() {
    // 一系列程序的初始化和相关 checkBox 按钮的状态赋值
    //btnStartServer.setText(GameShare.g_sButtonStopStartGame);
    startStatus = 1;
    startGameTimer.start();
  }

  private void stopGame() {
    //btnStartServer.setText(GameShare.g_sButtonStopStopGame);
    //textAreaStartInfo.append("正在开始停止服务器...\n");
    checkRunTimer.stop();
    stopGameTimer.start();
    isGateStop = false;
    startStatus = 3;
  }

  // m_boOpen
  // 是否打开，这实际上意味着，已经打开则不能修改任何参数（主要是配置文件）
  private boolean isOpen = false;
  // m_nStartStatus
  // 启动状态：0 默认；1 启动中；2 已经中止或启动，可以停止；3 已经停止，可以启动
  // TODO enum
  private int startStatus = 0;
  // m_nBackStartStatus
  // 备份状态：0 启动；1 停止
  // TODO enum
  private int backUpStaus = 0;
  // g_GetDatList
  // 需要更新的数据列表
  private List<String> needUpdateDataList = Collections.emptyList();
  // m_dwRefTick
  // 数据更新间隔——FIXME 未明确的字段
  private long dataUpdateInterval = ManagementFactory.getRuntimeMXBean().getStartTime();

  public void init(GameCenter gameCenter) {
    isOpen = false;
    gameCenter.tabContent.setSelectedIndex(0);
    startStatus = 0;
    backUpStaus = 0;
    gameCenter.textStartInfo.setText("");
    needUpdateDataList = new ArrayList<>();
    dataUpdateInterval = ManagementFactory.getRuntimeMXBean().getStartTime();
    GameShare.loadConfig();
    loadBackList();
    refBackListToView();
    // ListViewDataList.Clear 这个不重要，丢弃
    if (!StartService(gameCenter)) {
      // 需要注意的是，在 init 阶段调用退出方法，将有可能导致 stop 方法不被调用
      Platform.exit();
      return;
    }

    //RefGameConsole();
    // TabSheetDebug.TabVisible = false 测试模块，弃用
    // tsDataList.TabVisible = false 同上
    //
    gameCenter.checkAutoRun.setSelected(GameShare.g_boAutoRunBak);
    isOpen = true;

    if (GameShare.g_boAutoRunBak) {
      // TODO 启动备份功能，事实上在 checkBox 变化的同时，就应该做出对应的动作
    }
  }

  private boolean StartService(GameCenter gameCenter) {
    gameCenter.textStartInfo.append("正在启动游戏客户端控制器...");
    gameCenter.textStartInfo.append("\n");

    // m_dwShowTick= GetTickCount 没用任何用处


    return false;
  }

  private void refBackListToView() {
    // TODO 将备份文件显示出来
  }

  private void loadBackList() {
    // TODO 加载所有备份文件
  }

  public void stop(GameCenter gameCenter) {

  }
}
