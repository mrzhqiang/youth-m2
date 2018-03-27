package com.github.mrzhqiang;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

  public GameCenter() {
    btnStartServer.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) {
        switch (startStatus) {
          case 0:
            JOptionPane.showMessageDialog(panelRoot, "1111");
        }
      }
    });
  }
}
