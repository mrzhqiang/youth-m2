package bootstrap.model;

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
import bootstrap.model.config.BootstrapConfig;
import youthm2.common.Json;

/**
 * 引导模型。
 *
 * @author mrzhqiang
 */
public final class BootstrapModel {
  private static final Logger LOGGER = LoggerFactory.getLogger("bootstrap");
  private static final String CONFIG_FILE = "bootstrap.json";

  public final BootstrapConfig config = new BootstrapConfig();

  private final BackupModel backupModel = new BackupModel();
  private final ProgramModel programModel = new ProgramModel();

  private final Scheduler mainScheduler = Schedulers.from(Platform::runLater);

  public State state = State.DEFAULT;

  private Subscription subscription = null;

  public void loadConfig() {
    File configFile = new File(CONFIG_FILE);
    // 以 configFile 为主，缺失的由默认配置 reference.conf 填补
    Config conf = ConfigFactory.parseFile(configFile).withFallback(ConfigFactory.load());
    Config bootstrap = conf.getConfig("bootstrap");
    Preconditions.checkNotNull(bootstrap, "bootstrap config == null");
    config.home.setValue(bootstrap.getString("home"));
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

    backupModel.loadConfig();
  }

  public void saveConfig() {
    File configFile = new File(CONFIG_FILE);
    if (!configFile.exists()) {
      try {
        boolean newFile = configFile.createNewFile();
        LOGGER.info("创建新的配置文件: {}", newFile);
      } catch (Exception e) {
        LOGGER.error("创建新的配置文件出错", e);
        ExceptionDialog dialog = new ExceptionDialog(e);
        dialog.setHeaderText("无法创建新的配置文件");
        dialog.show();
        return;
      }
    }
    try (FileWriter writer = new FileWriter(configFile)) {
      writer.write(Json.prettyPrint(config.toJsonNode()));
      writer.flush();
    } catch (Exception e) {
      LOGGER.error("保存当前配置出错", e);
      ExceptionDialog dialog = new ExceptionDialog(e);
      dialog.setHeaderText("无法保存当前配置");
      dialog.show();
    }
  }

  public void startServer(LocalDateTime targetTime, Subscriber<Long> subscriber) {
    Preconditions.checkNotNull(targetTime, "target time == null");
    Preconditions.checkNotNull(subscriber, "subscriber == null");
    subscription = Observable.interval(0, 1, TimeUnit.SECONDS)
        .filter(aLong -> LocalDateTime.now().isAfter(targetTime))
        .doOnNext(aLong -> programModel.start("C:\\Windows\\notepad.exe"))
        .filter(aLong -> programModel.check("C:\\Windows\\notepad.exe"))
        //.doOnNext(aLong -> programModel.start(config.databaseCommand()))
        //.filter(aLong -> programModel.check(config.databaseCommand()))
        //.doOnNext(aLong -> programModel.start(config.accountCommand()))
        //.doOnNext(aLong -> programModel.start(config.loggerCommand()))
        //.doOnNext(aLong -> programModel.start(config.coreCommand()))
        //.doOnNext(aLong -> programModel.start(config.gameCommand()))
        //.doOnNext(aLong -> programModel.start(config.roleCommand()))
        //.doOnNext(aLong -> programModel.start(config.loginCommand()))
        //.doOnNext(aLong -> programModel.start(config.rankCommand()))
        .observeOn(mainScheduler)
        .subscribe(subscriber);
  }

  public void cancelStart() {
    if (subscription != null && !subscription.isUnsubscribed()) {
      subscription.unsubscribe();
    }
  }

  public void stopGame() {
  }

  public void cancelStop() {

  }

  public enum State {
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
