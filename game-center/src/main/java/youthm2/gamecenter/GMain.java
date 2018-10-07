package youthm2.gamecenter;

import javafx.application.Application;
import javafx.stage.Stage;
import javax.swing.*;

public final class GMain extends Application {
  public JPanel panelContent;
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
    String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
    try {
      UIManager.setLookAndFeel(lookAndFeel);
    } catch (Exception e) {
      e.printStackTrace();
    }
    launch(args);
  }

  /**
   * m_boOpen
   * 是否打开？从逻辑上来看，当执行窗体创建时，不能对配置文件进行任何操作。
   */
  private boolean isExecuting = false;
  /**
   * m_nStartStatus
   * 启动状态？
   * 0：初始状态，可以启动服务端；
   * 1：启动中，可以中断；
   * 2：已经中断，可以停止；
   * 3：停止中，可以中断；如果停止完毕，回归初始状态。
   */
  private int startStatus = 0;
  /**
   * m_dwShowTick
   * 系统自启动以来经历过的毫秒值，引擎中没有被使用。
   */
  @Deprecated
  private long showTick = -1;
  /**
   * m_dwRefTick
   * 窗体创建时获取的系统启动时间，引擎中用于数据更新，没有实际意义。
   */
  @Deprecated
  private long refTick = -1;
  /**
   * m_btHour
   * 延时或定时启动的小时数值。
   */
  private byte hour = 0;
  /**
   * m_btMinute
   * 延时或定时启动的分钟数值。
   */
  private byte minute = 0;
  /**
   * m_dwRunTime
   * 延时启动的间隔时间。
   * TODO 应该优化为 LocalTime
   */
  private long runTime = -1;

  /**
   * m_dwRunTick
   * 记录程序启动时的系统启动时间。
   * TODO 应该优化为 Instant 瞬间
   */
  private long runTick = -1;

  /**
   * m_boGateStop
   * 游戏网关是否已经停止。只有当这个状态为 true 时，才可以关停游戏主引擎。
   */
  private boolean gateStop = false;

  /**
   * m_boGateStopTick
   * 游戏网关停止后，需要等待的时间间隔。延迟关闭主引擎是一个不错的主意。
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
    isExecuting = true;
    GShare.loadConfig();
    GShare.loadBackupConfig();
    isExecuting = false;
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
