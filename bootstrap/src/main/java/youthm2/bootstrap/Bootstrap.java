package youthm2.bootstrap;

import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.util.Locale;
import javafx.application.Application;
import javafx.stage.Stage;
import javax.annotation.Nullable;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youthm2.bootstrap.config.GameConfig;

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
  public JTextArea startTextArea;
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
  public JList backupInfoList;
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

  private static final String CONFIG_FILE = "bootstrap.json";
  private static final int MAX_CONSOLE_COUNT = 1000;
  public static final String PROPERTY_HOME = "youthm2.home";
  public static final String PROPERTY_HOME_PATH = "youthm2.home.path";

  private static String homePath;

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

  public static String getHomePath() {
    if (homePath != null) {
      return homePath;
    }
    String fromProperty = System.getProperty(PROPERTY_HOME_PATH, System.getProperty(PROPERTY_HOME));
    if (fromProperty != null) {
      homePath = getAbsolutePath(fromProperty);
      if (!new File(homePath).isDirectory()) {
        throw new RuntimeException("Invalid home path '" + homePath + "'");
      }
    }
    if (homePath != null &&
        System.getProperty("os.name").toLowerCase(Locale.ENGLISH).startsWith("windows")) {
      return homePath;
    }
    return homePath;
  }

  @Nullable
  private static String getAbsolutePath(String path) {
    if (path == null) {
      return null;
    }
    if (path.startsWith("~/") || path.startsWith("~\\")) {
      path = System.getProperty("user.home") + path.substring(1);
    }
    if (path.length() == 0) {
      return path;
    }
    if (path.charAt(0) == '.') {
      if (path.length() == 1) {
        return "";
      }
    }
    return new File(path).getAbsolutePath();
  }

  private boolean isBusy;
  private StartState startState;

  private boolean gateStoped;
  private int startTimestamp;
  private int startModeHours;
  private int startModeMinutes;
  private long startModeDuration;
  private long coreStopDuration;

  private final GameConfig config = new GameConfig();

  @Override public void init() {
    isBusy = false;
    startState = StartState.DEFAULT;
    ConfigFactory.parseFile(new File(getHomePath()));
    config.load(ConfigFactory.load("bootstrap"));
  }

  @Override public void start(Stage primaryStage) {
    JFrame frame = new JFrame("游戏引导");
    frame.setContentPane(contentPanel);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
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
  }
}
