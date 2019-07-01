package youthm2.bootstrap;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javax.swing.event.ChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;
import youthm2.bootstrap.backup.BackupModel;
import youthm2.bootstrap.config.ConfigModel;
import youthm2.common.monitor.Monitor;

/**
 * 引导程序的视图模型。
 *
 * @author qiang.zhang
 */
public final class BootstrapViewModel {
  private static final Logger LOGGER = LoggerFactory.getLogger("bootstrap");
  private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
  private static final String CONFIG_FILE = "bootstrap.json";
  private static final String BACKUP_FILE = "backup.json";
  private static final int MAX_CONSOLE_COLUMNS = 1000;

  @FXML
  private CheckBox databaseCheckBox;

  private final Gson gson = new GsonBuilder()
      .setPrettyPrinting()
      .setDateFormat(DATE_FORMAT)
      .serializeNulls()
      .create();
  private final File configFile = new File(CONFIG_FILE);
  private final File backupFile = new File(BACKUP_FILE);
  private final ConfigModel configModel = new ConfigModel();
  private final BackupModel backupModel = new BackupModel();
  private final Scheduler mainScheduler = Schedulers.from(Platform::runLater);

  private State state = State.DEFAULT;
  private Instant startInstant = Instant.EPOCH;
  private Duration waitDuration = Duration.ZERO;
  private Subscription subscription = null;

  @FXML
  private void initialize() {
    bindModel();
    loadConfig();
    LOGGER.info("bootstrap view model init");
  }

  private void bindModel() {
    configModel.config.database.enabled = databaseCheckBox.selectedProperty();
  }

  @FXML
  private void onDatabaseCheckChange(ActionEvent event) {
    LOGGER.info("database check change");
  }

  private void loadConfig() {
    // 以 configFile 为主，缺失的由默认配置 reference.conf 填补
    Config config = ConfigFactory.parseFile(configFile).withFallback(ConfigFactory.load());
    configModel.load(config);
    // 加载备份数据
    backupModel.load(backupFile);
  }

  private void startGame() {
    Monitor monitor = Monitor.getInstance();
    startInstant = Instant.now();
    initProgram();
    startProgram();
    state = State.STARTING;
    monitor.report("start game");
  }

  private void stopGame() {
    stopProgram();
    state = State.DEFAULT;
  }

  private void initProgram() {
  }

  private void startProgram() {
    stopProgram();
    subscription = Observable.interval(0, 1, TimeUnit.SECONDS)
        .observeOn(mainScheduler)
        .filter(aLong -> startDatabase())
        .subscribe(aLong -> LOGGER.info("tick: " + aLong));
  }

  private Boolean startDatabase() {
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
      writer.write(gson.toJson(configModel));
      writer.flush();
    } catch (IOException e) {
      LOGGER.error("保存配置出错", e);
    }
  }

  public void stateChanged(ChangeEvent e) {
  }

  private enum State {
    DEFAULT("启动服务"),
    STARTING("正在启动.."),
    RUNNING("停止服务"),
    STOPPING("正在停止.."),
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
