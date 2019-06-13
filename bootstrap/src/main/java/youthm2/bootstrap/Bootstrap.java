package youthm2.bootstrap;

import com.google.common.base.Strings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.stage.Stage;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youthm2.bootstrap.backup.BackupData;
import youthm2.bootstrap.backup.BackupDataModel;
import youthm2.bootstrap.backup.BackupManager;
import youthm2.bootstrap.config.HomeConfig;

public final class Bootstrap extends Application {
  private static final Logger logger = LoggerFactory.getLogger("bootstrap");

  /*主面板*/
  public JPanel contentPanel;
  public JTabbedPane menuTab;
  /*控制面板*/
  public JCheckBox databaseCheckBox;
  public JCheckBox accountCheckBox;
  public JCheckBox coreCheckBox;
  public JCheckBox loggerCheckBox;
  public JCheckBox gameCheckBox;
  public JCheckBox roleCheckBox;
  public JCheckBox loginCheckBox;
  public JCheckBox rankCheckBox;
  public JComboBox startModeComboBox;
  public JSpinner hoursSpinner;
  public JSpinner minuteSpinner;
  public JTextArea startInfoTextArea;
  public JButton startButton;
  /*参数配置*/
  public JTextField gameDirInput;
  public JTextField gameNameInput;
  public JTextField databaseNameInput;
  public JTextField gameAddressInput;
  public JCheckBox disableWuXingActionCheckBox;
  public JButton saveButton;
  public JButton defaultButton;
  /*备份管理*/
  public JTextField dataDirInput;
  public JButton chooseDataButton;
  public JLabel backupDirInput;
  public JButton chooseBackupButton;
  public JRadioButton daysRadio;
  public JSpinner daysHoursSpinner;
  public JSpinner daysMinutesSpinner;
  public JRadioButton intervalRadio;
  public JSpinner intervalHoursSpinner;
  public JSpinner intervalMinuteSpinner;
  public JCheckBox enableBackupCheckBox;
  public JCheckBox enableZIPCheckBox;
  public JCheckBox autoRunningCheckBox;
  public JButton changeBackupButton;
  public JButton deleteBackupButton;
  public JButton newBackupButton;
  public JButton saveBackupButton;
  public JButton startBackupButton;
  /*数据清理*/
  public JCheckBox clearPlayerCheckBox;
  public JCheckBox clearNPCCheckBox;
  public JCheckBox clearAccountCheckBox;
  public JCheckBox clearEMailCheckBox;
  public JCheckBox clearGuildCheckBox;
  public JCheckBox clearAccountLoggerCheckBox;
  public JCheckBox clearShaBaKeCheckBox;
  public JCheckBox clearCoreLoggerCheckBox;
  public JCheckBox clearGlobalCheckBox;
  public JCheckBox clearGameLoggerCheckBox;
  public JCheckBox clearIndexCheckBox;
  public JCheckBox clearOtherCheckBox;
  public JButton clearAllButton;
  public JTable backupDataTable;

  private static final int DEFAULT_MAX_CONSOLE_COUNT = 1000;
  private static final String CONFIG_FILE = "bootstrap.json";
  private static final ConfigRenderOptions RENDER_OPTIONS = ConfigRenderOptions.defaults()
      .setComments(false)
      .setFormatted(true)
      .setOriginComments(false);

  public static void main(String[] args) {
    // 获取系统默认的样式/主题
    String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
    try {
      // 改变程序外观
      UIManager.setLookAndFeel(lookAndFeel);
    } catch (Exception e) {
      logger.warn("can not set look and feel: ", e);
    }
    // 启动程序
    launch(args);
  }

  private boolean isBusy;
  private StartState startState;
  private BackupState backupState;
  private int maxConsoleCount;
  private Config defaultConfig;
  private HomeConfig homeConfig;
  private BackupManager backupManager;
  private BackupDataModel backupDataModel;

  private boolean gateStoped;
  private int startTimestamp;
  private int startModeHours;
  private int startModeMinutes;
  private long startModeDuration;
  private long coreStopDuration;

  @Override public void init() {
    isBusy = false;
    startState = StartState.DEFAULT;
    backupState = BackupState.DISABLED;
    maxConsoleCount = DEFAULT_MAX_CONSOLE_COUNT;
    defaultConfig = ConfigFactory.load();
    homeConfig = new HomeConfig();
    backupManager = new BackupManager();
    backupDataModel = new BackupDataModel();
    // 提醒：init 方法中不允许操作窗体
  }

  @Override public void start(Stage primaryStage) {
    JFrame frame = new JFrame("游戏引导");
    frame.setContentPane(contentPanel);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    /* 居中显示（双屏幕会显示在两个屏幕中间，不友好）
      Dimension displaySize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = frame.getSize();
      int x = (displaySize.width - frameSize.width) / 2;
      int y = (displaySize.height - frameSize.height) / 2;
      frame.setLocation(x, y);
    */
    frame.setResizable(false);
    frame.setLocationRelativeTo(frame.getOwner());
    frame.setVisible(true);

    isBusy = true;
    menuTab.setSelectedIndex(0);
    startInfoTextArea.setColumns(maxConsoleCount);
    startInfoTextArea.setText("");
    loadConfig();
    updateLayout();
    startInfoTextArea.append("启动就绪..");
    isBusy = false;
  }

  @Override public void stop() {
  }

  private void loadConfig() {
    Config config = ConfigFactory.parseFile(new File(CONFIG_FILE));
    homeConfig.load(config.withFallback(defaultConfig));

    loadBackupList();
  }

  private void loadBackupList() {
    deleteBackupButton.setEnabled(false);
    changeBackupButton.setEnabled(false);
    Config config = ConfigFactory.parseFile(new File("backup.json"));
    if (config.hasPath("list")) {
      List<String> backupList = config.getStringList("list");
      List<BackupData> dataList = backupList.stream()
          .filter(s -> !Strings.isNullOrEmpty(s))
          .map(config::getConfig)
          .map(BackupData::of)
          .filter(data ->
              !Strings.isNullOrEmpty(data.source) && !Strings.isNullOrEmpty(data.destination))
          .collect(Collectors.toList());
      backupDataModel.update(dataList);
    }
  }

  private void updateLayout() {
    updateHomeLayout();
    updateSettingLayout();
    updateBackupLayout();
    updateClearLayout();
  }

  private void updateClearLayout() {

  }

  private void updateBackupLayout() {
    enableBackupCheckBox.setSelected(homeConfig.isBackupAction());
    backupDataTable.setModel(backupDataModel);
  }

  private void updateSettingLayout() {
    gameDirInput.setText(homeConfig.getDir());
    gameNameInput.setText(homeConfig.getName());
    databaseNameInput.setText(homeConfig.getDb());
    gameAddressInput.setText(homeConfig.getAddress());
    disableWuXingActionCheckBox.setSelected(homeConfig.isWuxingAction());
  }

  private void updateHomeLayout() {
    // 控制概览
    databaseCheckBox.setSelected(homeConfig.databaseServer.isEnabled());
    accountCheckBox.setSelected(homeConfig.accountServer.isEnabled());
    loggerCheckBox.setSelected(homeConfig.loggerServer.isEnabled());
    coreCheckBox.setSelected(homeConfig.coreServer.isEnabled());
    gameCheckBox.setSelected(homeConfig.gameGate.isEnabled());
    roleCheckBox.setSelected(homeConfig.roleGate.isEnabled());
    loginCheckBox.setSelected(homeConfig.loginGate.isEnabled());
    rankCheckBox.setSelected(homeConfig.rankPlug.isEnabled());
  }

  public enum StartState {
    DEFAULT("启动服务器"),
    STARTING("取消启动"),
    CALCEL_START("继续启动"),
    RUNNING("停止服务器"),
    STOPING("取消停止"),
    CALCEL_STOP("继续停止"),
    ;

    private final String label;

    StartState(String label) {
      this.label = label;
    }

    @Override public String toString() {
      return label;
    }
  }

  public enum BackupState {
    DISABLED("禁用"),
    ENABLED("激活"),
    ;

    private final String label;

    BackupState(String label) {
      this.label = label;
    }

    @Override public String toString() {
      return label;
    }
  }
}
