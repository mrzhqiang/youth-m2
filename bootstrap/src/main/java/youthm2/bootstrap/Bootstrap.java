package youthm2.bootstrap;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.typesafe.config.ConfigRenderOptions;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youthm2.bootstrap.backup.BackupManager;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

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
    public JTextField homePathInput;
    public JTextField gameNameInput;
    public JTextField dbNameInput;
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
    public JTable backupDataTable;
    public JLabel backupStateLabel;
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

    private static final int MAX_CONSOLE_COLUMNS = 1000;
    private static final String CONFIG_FILE = "bootstrap.json";
    private static final String BACKUP_FILE = "backup.json";

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
    private Gson gson;
    private File configFile;
    private File backupFile;
    private BootstrapState state;
    private BootstrapConfig config;
    private BackupManager backupManager;

    private long startModeDuration;

    @Override
    public void init() {
        isBusy = false;
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .serializeNulls()
                .create();
        configFile = new File(CONFIG_FILE);
        backupFile = new File(BACKUP_FILE);
        state = BootstrapState.DEFAULT;
        config = new BootstrapConfig();
        backupManager = new BackupManager();
        // 提醒：init 方法中不允许操作窗体
    }

    @Override
    public void start(Stage primaryStage) {
        JFrame frame = new JFrame("引导程序");
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
        hoursSpinner.setEnabled(startModeComboBox.getSelectedIndex() > 0 && startModeComboBox.isEnabled());
        minuteSpinner.setEnabled(startModeComboBox.getSelectedIndex() > 0 && startModeComboBox.isEnabled());
        menuTab.setSelectedIndex(0);
        startInfoTextArea.setColumns(MAX_CONSOLE_COLUMNS);
        startInfoTextArea.setText("");
        config.load(configFile);
        backupManager.load(backupFile);
        deleteBackupButton.setEnabled(false);
        changeBackupButton.setEnabled(false);
        startButton.setText(state.label);
        updateLayout();
        createEvent();
        startInfoTextArea.append("启动就绪..");
        isBusy = false;
    }

    @Override
    public void stop() {
    }

    private void saveConfig() {
        //noinspection UnstableApiUsage
        try (BufferedWriter writer = Files.newWriter(configFile, Charset.forName("UTF-8"))) {
            writer.write(gson.toJson(config));
            writer.flush();
        } catch (IOException e) {
            logger.error("保存配置出错", e);
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
        enableBackupCheckBox.setSelected(config.backupAction);
        backupDataTable.setModel(backupManager.dataModel);
        if (config.backupAction) {
            checkBackupState();
        }
    }

    private void checkBackupState() {
        if (!backupManager.isEnabled()) {
            backupManager.setEnabled(true);
            startBackupButton.setText("停止");
            backupManager.start();
            backupStateLabel.setForeground(Color.GREEN);
            backupStateLabel.setText("数据备份功能已激活");
        } else {
            backupManager.setEnabled(false);
            startBackupButton.setText("启动");
            backupManager.stop();
            backupStateLabel.setForeground(Color.RED);
            backupStateLabel.setText("数据备份功能已关闭");
        }
    }

    private void updateSettingLayout() {
        homePathInput.setText(config.homePath);
        dbNameInput.setText(config.dbName);
        gameNameInput.setText(config.gameName);
        gameAddressInput.setText(config.gameAddress);
        disableWuXingActionCheckBox.setSelected(config.wuxingAction);
    }

    private void updateHomeLayout() {
        // 控制概览
        databaseCheckBox.setSelected(config.database.enabled);
        accountCheckBox.setSelected(config.account.enabled);
        loggerCheckBox.setSelected(config.logger.enabled);
        coreCheckBox.setSelected(config.core.enabled);
        gameCheckBox.setSelected(config.game.enabled);
        roleCheckBox.setSelected(config.role.enabled);
        loginCheckBox.setSelected(config.login.enabled);
        rankCheckBox.setSelected(config.rank.enabled);
    }

    private void createEvent() {
        databaseCheckBox.addChangeListener(e -> config.database.enabled = databaseCheckBox.isSelected());
        accountCheckBox.addChangeListener(e -> config.account.enabled = accountCheckBox.isSelected());
        coreCheckBox.addChangeListener(e -> config.core.enabled = coreCheckBox.isSelected());
        loggerCheckBox.addChangeListener(e -> config.logger.enabled = loggerCheckBox.isSelected());
        gameCheckBox.addChangeListener(e -> config.game.enabled = gameCheckBox.isSelected());
        roleCheckBox.addChangeListener(e -> config.role.enabled = roleCheckBox.isSelected());
        loginCheckBox.addChangeListener(e -> config.login.enabled = loginCheckBox.isSelected());
        rankCheckBox.addChangeListener(e -> config.rank.enabled = rankCheckBox.isSelected());
        startModeComboBox.addItemListener(e -> {
            hoursSpinner.setEnabled(startModeComboBox.getSelectedIndex() > 0 && startModeComboBox.isEnabled());
            minuteSpinner.setEnabled(startModeComboBox.getSelectedIndex() > 0 && startModeComboBox.isEnabled());
            long hours = (long) hoursSpinner.getValue();
            long minutes = (long) minuteSpinner.getValue();
            startModeDuration = TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes);
        });
        startBackupButton.addActionListener(e -> checkBackupState());
    }

    public enum BootstrapState {
        DEFAULT("启动服务器"),
        STARTING("取消启动"),
        CALCEL_START("继续启动"),
        RUNNING("停止服务器"),
        STOPING("取消停止"),
        CALCEL_STOP("继续停止"),
        ;

        private final String label;

        BootstrapState(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
