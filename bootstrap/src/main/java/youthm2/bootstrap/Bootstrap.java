package youthm2.bootstrap;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youthm2.bootstrap.backup.BackupManager;
import youthm2.common.monitor.Monitor;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

public final class Bootstrap extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger("bootstrap");

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
    public JCheckBox enableZIPCheckBox;
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

    private static final String PROGRAM_TITLE = "引导程序 - 青春引擎";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String CONFIG_FILE = "bootstrap.json";
    private static final String BACKUP_FILE = "backup.json";
    private static final int MAX_CONSOLE_COLUMNS = 1000;

    public static void main(String[] args) {
        try {
            // 获取系统默认的样式/主题
            String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            // 改变程序外观
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception e) {
            LOGGER.warn("将外观设置为系统主题出错", e);
        }
        launch(args);
    }

    private boolean isBusy;
    private Gson gson;
    private File configFile;
    private File backupFile;
    private BootstrapState state;
    private BootstrapConfig config;
    private BackupManager backupManager;

    private long startModeDuration;
    private long startTimestamp;

    @Override
    public void init() {
        Monitor monitor = Monitor.simple();
        isBusy = false;
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat(DATE_FORMAT)
                .serializeNulls()
                .create();
        configFile = new File(CONFIG_FILE);
        backupFile = new File(BACKUP_FILE);
        state = BootstrapState.DEFAULT;
        config = new BootstrapConfig();
        backupManager = new BackupManager();
        // 提醒：init 方法中不允许操作窗体
        monitor.report("bootstrap init");
    }

    @Override
    public void start(Stage primaryStage) {
        Monitor monitor = Monitor.simple();
        JFrame frame = new JFrame(PROGRAM_TITLE);
        frame.setContentPane(contentPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(frame.getOwner());
        frame.setVisible(true);

        // 居中显示（双屏幕会显示在两个屏幕中间，不友好；已使用上面的代码来实现）
//        Dimension displaySize = Toolkit.getDefaultToolkit().getScreenSize();
//        Dimension frameSize = frame.getSize();
//        int x = (displaySize.width - frameSize.width) / 2;
//        int y = (displaySize.height - frameSize.height) / 2;
//        frame.setLocation(x, y);

        isBusy = true;
        monitor.record("init frame");
        initEvent();
        monitor.record("init event");
        loadConfig();
        monitor.record("load config");
        updateLayout();
        monitor.record("update layout");
        prepareStart();
        monitor.record("prepare start");
        isBusy = false;
        monitor.report("bootstrap start");
    }

    @Override
    public void stop() {
    }

    private void initEvent() {
        databaseCheckBox.addChangeListener(e -> config.database.enabled = databaseCheckBox.isSelected());
        accountCheckBox.addChangeListener(e -> config.account.enabled = accountCheckBox.isSelected());
        coreCheckBox.addChangeListener(e -> config.core.enabled = coreCheckBox.isSelected());
        loggerCheckBox.addChangeListener(e -> config.logger.enabled = loggerCheckBox.isSelected());
        gameCheckBox.addChangeListener(e -> config.game.enabled = gameCheckBox.isSelected());
        roleCheckBox.addChangeListener(e -> config.role.enabled = roleCheckBox.isSelected());
        loginCheckBox.addChangeListener(e -> config.login.enabled = loginCheckBox.isSelected());
        rankCheckBox.addChangeListener(e -> config.rank.enabled = rankCheckBox.isSelected());
        startModeComboBox.addItemListener(e -> {
            boolean startModeEnabled = startModeComboBox.getSelectedIndex() > 0 && startModeComboBox.isEnabled();
            hoursSpinner.setEnabled(startModeEnabled);
            minuteSpinner.setEnabled(startModeEnabled);
            if (startModeEnabled) {
                // getValue 返回的是 Integer 类型
                long hours = (int) hoursSpinner.getValue();
                long minutes = (int) minuteSpinner.getValue();
                startModeDuration = TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes);
            } else {
                startModeDuration = 0;
            }
        });
        enableBackupCheckBox.addChangeListener(e -> {
            boolean selected = enableBackupCheckBox.isSelected();
            startBackupButton.setEnabled(selected);
            config.backupAction = selected;
        });
        startBackupButton.addActionListener(e -> {
            if (config.backupAction) {
                checkBackupState();
            }
        });

        // 启动按钮
        startButton.addActionListener(e -> {
            int value = JOptionPane.showConfirmDialog(menuTab, "确定要 [" + state + "] 吗？", "请确认", JOptionPane.YES_NO_OPTION);
            if (JOptionPane.OK_OPTION == value) {
                switch (state) {
                    case DEFAULT:
                        startGame();
                        break;
                    case STARTING:
                    case CANCEL:
                    case RUNNING:
                    case STOPPING:
                }
            }
        });
    }

    private void loadConfig() {
        config.load(configFile);
        backupManager.load(backupFile);
    }

    private void updateLayout() {
        // 控制概览
        databaseCheckBox.setSelected(config.database.enabled);
        accountCheckBox.setSelected(config.account.enabled);
        loggerCheckBox.setSelected(config.logger.enabled);
        coreCheckBox.setSelected(config.core.enabled);
        gameCheckBox.setSelected(config.game.enabled);
        roleCheckBox.setSelected(config.role.enabled);
        loginCheckBox.setSelected(config.login.enabled);
        rankCheckBox.setSelected(config.rank.enabled);
        // 配置设定
        homePathInput.setText(config.homePath);
        dbNameInput.setText(config.dbName);
        gameNameInput.setText(config.gameName);
        gameAddressInput.setText(config.gameAddress);
        disableWuXingActionCheckBox.setSelected(config.wuxingAction);
        // 备份管理
        enableBackupCheckBox.setSelected(config.backupAction);
        backupManager.updateModel(backupDataTable);
        if (config.backupAction) {
            checkBackupState();
        }
        // todo 清理数据
    }

    private void prepareStart() {
        menuTab.setSelectedIndex(0);
        boolean startModeEnabled = startModeComboBox.getSelectedIndex() > 0 && startModeComboBox.isEnabled();
        hoursSpinner.setEnabled(startModeEnabled);
        minuteSpinner.setEnabled(startModeEnabled);
        startInfoTextArea.setText("");
        startInfoTextArea.setColumns(MAX_CONSOLE_COLUMNS);
        startButton.setText(state.toString());
        startInfoTextArea.append("准备就绪..");
    }

    private void startGame() {
        Monitor monitor = Monitor.simple();
        startTimestamp = System.currentTimeMillis();
//        if (startModeComboBox.isEnabled()) {
//        long hours = (int) hoursSpinner.getValue();
//        long minutes = (int) minuteSpinner.getValue();
//        startModeDuration = TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes);
//        }

        state = BootstrapState.STARTING;
        startButton.setText(state.toString());
//        timer.schedule(new StartGameTask(), TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(1));
        monitor.report("start game");
    }

    private void saveConfig() {
        //noinspection UnstableApiUsage
        try (BufferedWriter writer = Files.newWriter(configFile, Charset.forName("UTF-8"))) {
            writer.write(gson.toJson(config));
            writer.flush();
        } catch (IOException e) {
            LOGGER.error("保存配置出错", e);
        }
    }

    private void checkBackupState() {
        if (!backupManager.enabled) {
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
}
