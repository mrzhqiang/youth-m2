package youthm2;

import youthm2.controllers.GameCenterController;
import javafx.application.Application;
import javafx.stage.Stage;
import javax.swing.*;

public final class GameCenter extends Application {
  public JPanel contentPanel;
  public JTabbedPane tabContent;
  public JTextField editServerDir;
  public JTextField editDbName;
  public JTextField editGameName;
  public JTextField editGameAddress;
  public JTextField editPortAdd;
  public JButton btnSettingSave;
  public JButton btnSettingDefault;
  public JCheckBox checkCloseFiveElement;
  public JTextField editDataDir;
  public JButton btnChooseDataDir;
  public JButton btnChooseBackupDir;
  public JRadioButton radioDayMode;
  public JRadioButton radioIntervalMode;
  public JSpinner spinnerDayHours;
  public JSpinner spinnerDayMinute;
  public JCheckBox checkBackup;
  public JCheckBox checkZip;
  public JCheckBox checkAutoRun;
  public JButton btnBackupModification;
  public JButton btnBackupDelete;
  public JButton btnBackupAdd;
  public JButton btnBackupSave;
  public JButton btnBackupStart;
  public JSpinner spinnerIntervalHours;
  public JSpinner spinnerIntervalMinute;
  public JCheckBox checkUserData;
  public JCheckBox checkNpcData;
  public JCheckBox checkEmailData;
  public JCheckBox checkAccountData;
  public JCheckBox checkGuildData;
  public JCheckBox checkShaBkData;
  public JCheckBox checkGlobalData;
  public JCheckBox checkItemIdData;
  public JCheckBox checkAccountLog;
  public JCheckBox checkM2Log;
  public JCheckBox checkGameLog;
  public JCheckBox checkOtherData;
  public JButton btnClear;
  public JList listBackupInfo;
  public JButton btnStart;
  public JLabel editBackupDir;
  public JTextArea textStartInfo;
  public JCheckBox checkDbServer;
  public JCheckBox checkLoginServer;
  public JCheckBox checkM2Server;
  public JCheckBox checkLogServer;
  public JCheckBox checkRunGate;
  public JCheckBox checkSelGate;
  public JCheckBox checkLoginGate;
  public JCheckBox checkRankPlugIn;
  public JComboBox comboStartMode;
  public JSpinner spinnerHours;
  public JSpinner spinnerMinute;

  public static void main(String[] args) {
    launch(args);
  }

  private final GameCenterController controller = new GameCenterController();

  @Override public void init() {
    controller.init(this);
  }

  @Override public void start(Stage primaryStage) {
    JFrame frame = new JFrame("服务端控制器");
    frame.setContentPane(contentPanel);
    // 右上角带有关闭按钮
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    // 不允许调整大小
    frame.setResizable(false);
    // 居中显示
    /*Dimension displaySize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    int x = (displaySize.width - frameSize.width) / 2;
    int y = (displaySize.height - frameSize.height) / 2;
    frame.setLocation(x, y);*/
    frame.setLocationRelativeTo(frame.getOwner());
    frame.setVisible(true);
  }

  @Override public void stop() {
    controller.stop(this);
  }
}
