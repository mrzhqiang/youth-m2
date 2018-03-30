package com.github.mrzhqiang;

import javax.swing.*;

public class GameCenter {
  public JPanel panelRoot;
  private JButton startBegin;
  private JTextArea textAreaStartInfo;
  private JTextField textFiledServerDir;
  private JTextField textFiledDbName;
  private JTextField textFiledGameName;
  private JTextField textFiledGameAddress;
  private JTextField textFieldPortAdd;
  private JButton btnSaveSetting;
  private JButton btnDefaultSetting;
  private JCheckBox checkBoxCloseWuXing;
  private JTextField textFieldDataDir;
  private JTextField textFieldBackupDir;
  private JButton btnChooseDataDir;
  private JButton btnChooseBackupDir;
  private JRadioButton radioBtnDay;
  private JRadioButton radioBtnInterval;
  private JSpinner spinnerDayHours;
  private JSpinner spinnerDayMinute;
  private JCheckBox checkBoxBackup;
  private JCheckBox checkBoxZip;
  private JCheckBox checkBoxAutoRun;
  private JButton btnModification;
  private JButton btnDelete;
  private JButton btnAdd;
  private JButton btnSave;
  private JButton btnStart;
  private JSpinner spinnerIntervalHours;
  private JSpinner spinnerIntervalMinute;
  private JCheckBox checkBoxUserData;
  private JCheckBox checkBoxNpcData;
  private JCheckBox checkBoxEmailData;
  private JCheckBox checkBoxAccountData;
  private JCheckBox checkBoxGuildData;
  private JCheckBox checkBoxShaBkData;
  private JCheckBox checkBoxGlobalData;
  private JCheckBox checkBoxItemIdData;
  private JCheckBox checkBoxAccountLog;
  private JCheckBox checkBoxM2Log;
  private JCheckBox checkBoxGameLog;
  private JCheckBox checkBoxOtherData;
  private JButton btnStartClear;
  private JList listBackupInfo;
  private JTabbedPane tabContent;
  private JPanel panelServerControl;
  private JPanel panelStartControl;
  private JScrollPane panelStartInfo;
  private JButton btnStartServer;
  private JPanel panelConfig;
  private JPanel panelServerDirDbSetting;
  private JLabel labelServerDir;
  private JLabel labelDbName;
  private JLabel labelGameName;
  private JLabel labelGameAddress;
  private JLabel labelPortAdd;
  private JPanel panelFunctionOption;
  private JPanel panelDataBackup;
  private JPanel panelDirChoose;
  private JLabel labelDataDir;
  private JLabel labelBackupDir;
  private JPanel panelBackupMode;
  private JPanel panelTime;
  private JLabel labelDayHours;
  private JLabel labelDayMinute;
  private JLabel labelIntervalHours;
  private JLabel labelIntervalMinute;
  private JPanel panelBackupOption;
  private JPanel panelBackupControl;
  private JPanel panelDataClean;
  private JPanel panelCleanOption;

  private int startStatus = 0;
  private boolean isGateStop = false;

  private Timer startGameTimer = new Timer(1000, e -> System.out.println("这里是在启动什么？"));
  private Timer stopGameTimer = new Timer(1000, e -> System.out.println("这里是在停止什么？"));
  private Timer checkRunTimer = new Timer(1000, e -> System.out.println("这里是在检查什么？"));

  public GameCenter() {
    btnStartServer.addActionListener(e -> {
      switch (startStatus) {
        case 0:
          if (JOptionPane.showConfirmDialog(panelRoot, "确认是否启动游戏？", "确认信息",
              JOptionPane.YES_NO_OPTION) == 0) {
            startGame();
          }
          break;
        case 1:
        case 3:
          if (JOptionPane.showConfirmDialog(panelRoot, "确认是否中止启动？", "确认信息",
              JOptionPane.YES_NO_OPTION) == 0) {
            // TimerStartGame.Enable = false 估计是取消启动线程的Run
            startGameTimer.stop();
            startStatus = 2;
            btnStartServer.setText(GameShare.g_sButtonStopGame);
          }
          break;
        case 2:
          if (JOptionPane.showConfirmDialog(panelRoot, "确认是否停止启动？", "确认信息",
              JOptionPane.YES_NO_OPTION) == 0) {
            stopGame();
          }
          break;
      }

    });
  }

  private void startGame() {
    // 一系列程序的初始化和相关 checkBox 按钮的状态赋值
    btnStartServer.setText(GameShare.g_sButtonStopStartGame);
    startStatus = 1;
    startGameTimer.start();
  }

  private void stopGame() {
    btnStartServer.setText(GameShare.g_sButtonStopStopGame);
    textAreaStartInfo.append("正在开始停止服务器...\n");
    checkRunTimer.stop();
    stopGameTimer.start();
    isGateStop = false;
    startStatus = 3;
  }
}
