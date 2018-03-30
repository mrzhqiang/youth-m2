package com.github.mrzhqiang.controllers;

import com.github.mrzhqiang.GameCenter;
import javax.swing.Timer;

public final class GameCenterController {

  private static int startStatus = 0;
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

  public void loadConfig(GameCenter gameCenter) {

  }

  public void saveLog(GameCenter gameCenter) {

  }
}
