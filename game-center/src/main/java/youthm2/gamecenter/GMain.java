package youthm2.gamecenter;

import javafx.application.Application;
import javafx.stage.Stage;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GMain extends Application {
  private static final Logger logger = LoggerFactory.getLogger(GMain.class);
  private static final int MAX_CONSOLE_COUNT = 1000;

  public JPanel panelContent;
  public JTabbedPane tabContent;
  public JTextField gameDirInput;
  public JTextField databaseNameInput;
  public JTextField gameNameInput;
  public JTextField gameAddressInput;
  public JButton saveButton;
  public JButton defaultButton;
  public JCheckBox disableWuXingActionCheckBox;
  public JTextField dataDirInput;
  public JButton chooseDataButton;
  public JButton chooseBackupButton;
  public JRadioButton daysRadio;
  public JRadioButton intervalRadio;
  public JSpinner daysHoursSpinner;
  public JSpinner daysMinutesSpinner;
  public JCheckBox enableBackupCheckBox;
  public JCheckBox enableZIPCheckBox;
  public JCheckBox autoRunningCheckBox;
  public JButton changeBackupButton;
  public JButton deleteBackupButton;
  public JButton newBackupButton;
  public JButton saveBackupButton;
  public JButton startBackupButton;
  public JSpinner intervalHoursSpinner;
  public JSpinner intervalMinuteSpinner;
  public JCheckBox clearPlayerCheckBox;
  public JCheckBox clearNPCCheckBox;
  public JCheckBox clearEMailCheckBox;
  public JCheckBox clearAccountCheckBox;
  public JCheckBox clearGuildCheckBox;
  public JCheckBox clearShaBaKeCheckBox;
  public JCheckBox clearGlobalCheckBox;
  public JCheckBox clearIndexCheckBox;
  public JCheckBox clearAccountLoggerCheckBox;
  public JCheckBox clearCoreLoggerCheckBox;
  public JCheckBox clearGameLoggerCheckBox;
  public JCheckBox clearOtherCheckBox;
  public JButton clearAllButton;
  public JList backupInfoList;
  public JButton startButton;
  public JLabel backupDirInput;
  public JTextArea startTextArea;
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

  public static void main(String[] args) {
    // 获取系统默认的样式/主题
    String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
    try {
      // 作为我们程序的皮肤
      UIManager.setLookAndFeel(lookAndFeel);
    } catch (Exception e) {
      logger.warn("can not set look and feel: ", e);
    }
    // 启动程序
    launch(args);
  }

  private boolean isBusy;
  private StartState startState;
  private int backupState;
  private int clearConsoleLimit;
  private int startTimestamp;
  private int startModeHours;
  private int startModeMinutes;
  private long startModeDuration;
  private boolean gateStoped;
  private long coreStopDuration;
  /**
   * m_boGateStopTick 游戏网关停止后，需要等待的时间间隔。延迟关闭主引擎是一个不错的主意。
   * <p>
   * TODO 应该优化为 Instant
   */
  private long gateStopTick = -1;

  /**
   * 程序初始化。
   * <p>
   * 准备一切必要的参数，比如：从 config 中读取程序配置。
   */
  @Override public void init() {
    isBusy = true;
    GShare.loadConfig();
    GShare.loadBackupConfig();
    isBusy = false;
  }

  @Override public void start(Stage primaryStage) {
    JFrame frame = new JFrame("游戏控制器");
    frame.setContentPane(panelContent);
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
    // 执行启动逻辑（在这里实现显得有点臃肿）
    GShare.start(this);
  }

  @Override public void stop() {
    GShare.stop(this);
  }
}
