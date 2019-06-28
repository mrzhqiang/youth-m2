package youthm2.bootstrap;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;
import youthm2.bootstrap.program.Program;
import youthm2.common.monitor.Monitor;

public final class Bootstrap extends Application implements ChangeListener {
  public JPanel contentPanel;
  public JTabbedPane menuTab;

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

  public JTextField homePathInput;
  public JTextField gameNameInput;
  public JTextField dbNameInput;
  public JTextField gameAddressInput;
  public JCheckBox disableWuXingActionCheckBox;
  public JButton saveButton;
  public JButton defaultButton;

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
  public JCheckBox enableZipCheckBox;
  public JCheckBox autoRunningCheckBox;
  public JButton changeBackupButton;
  public JButton deleteBackupButton;
  public JButton newBackupButton;
  public JButton saveBackupButton;
  public JButton startBackupButton;
  public JTable backupDataTable;
  public JLabel backupStateLabel;

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

  public static void main(String[] args) {
    launch(args);
  }

  private static final Logger LOGGER = LoggerFactory.getLogger("bootstrap");
  private static final String PROGRAM_TITLE = "引导程序 - 青春引擎";
  private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
  private static final String CONFIG_FILE = "bootstrap.json";
  private static final String BACKUP_FILE = "backup.json";
  private static final int MAX_CONSOLE_COLUMNS = 1000;

  private Gson gson;
  private State state;
  private File configFile;
  private File backupFile;
  private HomeConfig homeConfig;
  private BackupManager backupManager;
  private Scheduler mainScheduler;
  private Subscription subscription;

  private final Program database = new Program();
  private final Program account = new Program();
  private final Program logger = new Program();
  private final Program core = new Program();
  private final Program game = new Program();
  private final Program role = new Program();
  private final Program login = new Program();
  private final Program rank = new Program();

  private Instant startInstant;
  private Duration waitDuration;

  @Override
  public void init() {
    //LookUtil.setSystemDefaultLookAndFeel();
    // 提醒：init 方法中不允许操作窗体，因为它不在 javafx application thread 上运行
    Monitor monitor = Monitor.simple();
    gson = new GsonBuilder()
        .setPrettyPrinting()
        .setDateFormat(DATE_FORMAT)
        .serializeNulls()
        .create();
    state = State.DEFAULT;
    configFile = new File(CONFIG_FILE);
    backupFile = new File(BACKUP_FILE);
    homeConfig = new HomeConfig();
    mainScheduler = Schedulers.from(Platform::runLater);
    backupManager = new BackupManager();
    monitor.report("bootstrap init");
  }

  @Override
  public void start(Stage primaryStage) {
    Monitor monitor = Monitor.simple();
    try {
      // Read file fxml and draw interface.
      Parent root = FXMLLoader.load(getClass().getResource("/bootstrap.fxml"));
      primaryStage.setTitle(PROGRAM_TITLE);
      primaryStage.setScene(new Scene(root));
      primaryStage.show();
    } catch(Exception e) {
      LOGGER.error("启动失败", e);
      System.exit(1);
    }

    //JFrame frame = new JFrame(PROGRAM_TITLE);
    //frame.setContentPane(contentPanel);
    //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    //frame.pack();
    //frame.setResizable(false);
    //frame.setLocationRelativeTo(frame.getOwner());
    //frame.setVisible(true);

    // 居中显示（双屏幕会显示在两个屏幕中间，不友好；已使用上面的代码来实现）
    // Dimension displaySize = Toolkit.getDefaultToolkit().getScreenSize();
    // Dimension frameSize = frame.getSize();
    // int x = (displaySize.width - frameSize.width) / 2;
    // int y = (displaySize.height - frameSize.height) / 2;
    // frame.setLocation(x, y);

    //loadConfig();
    //monitor.record("load config");
    //updateLayout();
    //monitor.record("update layout");
    //initEvent();
    //monitor.record("init event");
    //prepareStart();
    //monitor.report("bootstrap start");
  }

  @Override
  public void stop() {
  }

  private void initEvent() {
    databaseCheckBox.addChangeListener(this);
    accountCheckBox.addChangeListener(this);
    coreCheckBox.addChangeListener(this);
    loggerCheckBox.addChangeListener(this);
    gameCheckBox.addChangeListener(this);
    roleCheckBox.addChangeListener(this);
    loginCheckBox.addChangeListener(this);
    rankCheckBox.addChangeListener(this);
    disableWuXingActionCheckBox.addChangeListener(this);
    enableBackupCheckBox.addChangeListener(this);
    enableZipCheckBox.addChangeListener(
        e -> backupManager.zipChange(enableZipCheckBox.isSelected()));
    autoRunningCheckBox.addChangeListener(
        e -> backupManager.autoRunChange(autoRunningCheckBox.isSelected()));
    // 启动模式按钮
    startModeComboBox.addItemListener(e -> {
      boolean startModeEnabled =
          startModeComboBox.getSelectedIndex() > 0 && startModeComboBox.isEnabled();
      hoursSpinner.setEnabled(startModeEnabled);
      minuteSpinner.setEnabled(startModeEnabled);
    });
    // 启动服务按钮
    startButton.addActionListener(e -> {
      int value = JOptionPane.showConfirmDialog(menuTab, "确定要 [" + state + "] 吗？", "请确认",
          JOptionPane.YES_NO_OPTION);
      if (JOptionPane.OK_OPTION == value) {
        switch (state) {
          case DEFAULT:
            startGame();
            break;
          case STARTING:
            stopGame();
          case CANCEL:
          case RUNNING:
          case STOPPING:
        }
      }
    });
    // 启动备份按钮
    startBackupButton.addActionListener(e -> {
      if (homeConfig.backupAction) {
        checkBackupState();
      }
    });
  }

  private void loadConfig() {
    // 以 config file 为主，缺失的由默认配置 reference.conf 填补
    Config config = ConfigFactory.parseFile(configFile).withFallback(ConfigFactory.load());
    homeConfig.load(config);
    backupManager.load(backupFile);
  }

  private void updateLayout() {
    // 控制概览
    databaseCheckBox.setSelected(homeConfig.database.enabled);
    accountCheckBox.setSelected(homeConfig.account.enabled);
    loggerCheckBox.setSelected(homeConfig.logger.enabled);
    coreCheckBox.setSelected(homeConfig.core.enabled);
    gameCheckBox.setSelected(homeConfig.game.enabled);
    roleCheckBox.setSelected(homeConfig.role.enabled);
    loginCheckBox.setSelected(homeConfig.login.enabled);
    rankCheckBox.setSelected(homeConfig.rank.enabled);
    // 配置设定
    homePathInput.setText(homeConfig.path);
    dbNameInput.setText(homeConfig.dbName);
    gameNameInput.setText(homeConfig.gameName);
    gameAddressInput.setText(homeConfig.gameAddress);
    disableWuXingActionCheckBox.setSelected(homeConfig.wuxingAction);
    // 备份管理
    enableBackupCheckBox.setSelected(homeConfig.backupAction);
    backupManager.updateModel(backupDataTable);
    // todo 清理数据
  }

  private void prepareStart() {
    menuTab.setSelectedIndex(0);
    startInfoTextArea.setText("");
    startInfoTextArea.setColumns(MAX_CONSOLE_COLUMNS);
    startButton.setText(state.toString());
    if (homeConfig.backupAction) {
      checkBackupState();
    }
    boolean startModeEnabled =
        startModeComboBox.getSelectedIndex() > 0 && startModeComboBox.isEnabled();
    hoursSpinner.setEnabled(startModeEnabled);
    minuteSpinner.setEnabled(startModeEnabled);
    startInfoTextArea.append("准备就绪..");
  }

  private void startGame() {
    Monitor monitor = Monitor.simple();
    startInstant = Instant.now();
    if (homeConfig.backupAction) {
      long hours = (int) hoursSpinner.getValue();
      long minutes = (int) minuteSpinner.getValue();
      waitDuration = Duration.ofHours(hours).plusMinutes(minutes);
    }
    initProgram();
    startProgram();
    state = State.STARTING;
    startButton.setText(state.toString());
    monitor.report("start game");
  }

  private void stopGame() {
    stopProgram();
    state = State.DEFAULT;
    startButton.setText(state.toString());
  }

  private void initProgram() {
    database.state = Program.State.DEFAULT;
    database.path = homeConfig.path + "DBServer" + File.separator;
    database.config = homeConfig.database;
    account.state = Program.State.DEFAULT;
    account.path = homeConfig.path + "LoginSrv" + File.separator;
    account.config = homeConfig.account;
    logger.state = Program.State.DEFAULT;
    logger.path = homeConfig.path + "LogServer" + File.separator;
    logger.config = homeConfig.logger;
    core.state = Program.State.DEFAULT;
    core.path = homeConfig.path + "Mir200" + File.separator;
    core.config = homeConfig.core;
    game.state = Program.State.DEFAULT;
    game.path = homeConfig.path + "RunGate" + File.separator;
    game.config = homeConfig.game;
    role.state = Program.State.DEFAULT;
    role.path = homeConfig.path + "SelGate" + File.separator;
    role.config = homeConfig.role;
    login.state = Program.State.DEFAULT;
    login.path = homeConfig.path + "LoginGate" + File.separator;
    login.config = homeConfig.login;
    rank.state = Program.State.DEFAULT;
    rank.path = homeConfig.path + "Mir200" + File.separator;
    rank.config = homeConfig.rank;
  }

  private void startProgram() {
    stopProgram();
    subscription = Observable.interval(0, 1, TimeUnit.SECONDS)
        .observeOn(mainScheduler)
        .filter(aLong -> startDatabase())
        .subscribe(aLong -> LOGGER.info("tick: " + aLong));
  }

  private Boolean startDatabase() {
    if (database.config.enabled) {
      switch (database.state) {
        case DEFAULT:
          database.run();

      }
    }
    return null;
  }

  private void stopProgram() {
    if (subscription != null && !subscription.isUnsubscribed()) {
      subscription.unsubscribe();
    }
  }

  private void saveConfig() {
    //noinspection UnstableApiUsage
    try (BufferedWriter writer = Files.newWriter(configFile, Charset.forName("UTF-8"))) {
      writer.write(gson.toJson(homeConfig));
      writer.flush();
    } catch (IOException e) {
      LOGGER.error("保存配置出错", e);
    }
  }

  private void checkBackupState() {
    if (!homeConfig.backupAction) {
      backupManager.enabled = true;
      startBackupButton.setText("停止");
      backupManager.start();
      backupStateLabel.setForeground(Color.GREEN);
      backupStateLabel.setText("数据备份功能已激活");
    } else {
      backupManager.enabled = false;
      startBackupButton.setText("启动");
      backupManager.stop();
      backupStateLabel.setForeground(Color.RED);
      backupStateLabel.setText("数据备份功能已关闭");
    }
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    homeConfig.database.enabled = databaseCheckBox.isSelected();
    homeConfig.account.enabled = accountCheckBox.isSelected();
    homeConfig.core.enabled = coreCheckBox.isSelected();
    homeConfig.logger.enabled = loggerCheckBox.isSelected();
    homeConfig.game.enabled = gameCheckBox.isSelected();
    homeConfig.role.enabled = roleCheckBox.isSelected();
    homeConfig.login.enabled = loginCheckBox.isSelected();
    homeConfig.rank.enabled = rankCheckBox.isSelected();

    homeConfig.wuxingAction = disableWuXingActionCheckBox.isSelected();
    boolean selected = enableBackupCheckBox.isSelected();
    homeConfig.backupAction = selected;
    startBackupButton.setEnabled(selected);
    saveConfig();
  }

  private enum State {
    DEFAULT("启动服务"),
    STARTING("取消启动"),
    CANCEL("停止启动"),
    RUNNING("停止服务"),
    STOPPING("取消停止"),
    ;

    private final String label;

    State(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
