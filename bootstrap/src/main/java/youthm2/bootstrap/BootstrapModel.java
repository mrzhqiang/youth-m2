package youthm2.bootstrap;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;
import youthm2.bootstrap.config.BootstrapConfig;
import youthm2.common.Json;
import youthm2.common.monitor.Monitor;

/**
 * 引导模型。
 *
 * @author mrzhqiang
 */
final class BootstrapModel {
  private static final Logger LOGGER = LoggerFactory.getLogger("bootstrap");
  private static final String CONFIG_FILE = "bootstrap.json";

  final BootstrapConfig config = new BootstrapConfig();

  private final Scheduler mainScheduler = Schedulers.from(Platform::runLater);

  State state = State.DEFAULT;

  private Subscription subscription = null;

  void loadConfig() {
    File configFile = new File(CONFIG_FILE);
    // 以 configFile 为主，缺失的由默认配置 reference.conf 填补
    Config conf = ConfigFactory.parseFile(configFile).withFallback(ConfigFactory.load());
    Config bootstrap = conf.getConfig("bootstrap");
    Preconditions.checkNotNull(bootstrap, "bootstrap config == null");
    config.path.setValue(bootstrap.getString("path"));
    config.dbName.setValue(bootstrap.getString("dbName"));
    config.gameName.setValue(bootstrap.getString("gameName"));
    config.gameAddress.setValue(bootstrap.getString("gameAddress"));
    config.backupAction.setValue(bootstrap.getBoolean("backupAction"));

    config.database.onLoad(bootstrap.getConfig("database"));
    config.account.onLoad(bootstrap.getConfig("account"));
    config.logger.onLoad(bootstrap.getConfig("logger"));
    config.core.onLoad(bootstrap.getConfig("core"));
    config.game.onLoad(bootstrap.getConfig("game"));
    config.role.onLoad(bootstrap.getConfig("role"));
    config.login.onLoad(bootstrap.getConfig("login"));
    config.rank.onLoad(bootstrap.getConfig("rank"));
  }

  void saveConfig() {
    File configFile = new File(CONFIG_FILE);
    if (!configFile.exists()) {
      try {
        boolean newFile = configFile.createNewFile();
        LOGGER.info("create new config file: " + newFile);
      } catch (Exception e) {
        LOGGER.error("创建新的配置文件出错", e);
        ExceptionDialog dialog = new ExceptionDialog(e);
        dialog.setHeaderText("无法创建新的配置文件");
        dialog.show();
        return;
      }
    }
    try (FileWriter fileWriter = new FileWriter(configFile)) {
      fileWriter.write(Json.prettyPrint(config.toJsonNode()));
      fileWriter.flush();
    } catch (Exception e) {
      LOGGER.error("保存当前配置出错", e);
      ExceptionDialog dialog = new ExceptionDialog(e);
      dialog.setHeaderText("无法保存当前配置");
      dialog.show();
    }
  }

  void startGame(LocalDateTime targetTime, Subscriber<Long> subscriber) {
    Monitor monitor = Monitor.getInstance();
    subscription = Observable.interval(0, 1, TimeUnit.SECONDS)
        .doOnNext(aLong -> LOGGER.info("wait: " + aLong))
        .filter(aLong -> LocalDateTime.now().isAfter(targetTime))
        .doOnNext(aLong -> LOGGER.info("tick: " + aLong))
        .doOnNext(aLong -> {
          if (aLong > 10) {
            throw new RuntimeException("完成");
          }
        })
        .observeOn(mainScheduler)
        .subscribe(subscriber);
    monitor.report("start game");
  }

  void cancelStart() {
    if (subscription != null && !subscription.isUnsubscribed()) {
      subscription.unsubscribe();
    }
  }

  void stopGame() {
    stopProgram();
  }

  void cancelStop() {

  }

  private void startProgram() {
  }

  private void stopProgram() {
  }

  enum State {
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
