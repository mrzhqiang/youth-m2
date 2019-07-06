package youthm2.bootstrap.model;

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
import youthm2.bootstrap.model.config.BootstrapConfig;
import youthm2.common.Environments;
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

  public State state = State.INITIALIZED;

  private final ProgramModel programModel = new ProgramModel();
  private final Scheduler mainScheduler = Schedulers.from(Platform::runLater);

  private Subscription subscription = null;

  public void loadConfig() {
    File configFile;
    if (Environments.isDebug()) {
      // 调试模式，就读取 sample 目录下的配置。
      configFile = new File("sample", CONFIG_FILE);
    } else {
      // 非调试模式，读取当前目录下的配置。
      // 注意：直接在 IDEA 中 Run 的话，那么使用的是内置配置。
      configFile = new File(CONFIG_FILE);
    }
    // 以 configFile 为主，缺失的由默认配置 reference.conf 填补
    Config conf = ConfigFactory.parseFile(configFile).withFallback(getDefaultConfig());
    Config bootstrap = conf.getConfig("bootstrap");
    loadBasicConfig(bootstrap);

    config.database.onLoad(bootstrap.getConfig("database"));
    config.account.onLoad(bootstrap.getConfig("account"));
    config.logger.onLoad(bootstrap.getConfig("logger"));
    config.core.onLoad(bootstrap.getConfig("core"));
    config.game.onLoad(bootstrap.getConfig("game"));
    config.role.onLoad(bootstrap.getConfig("role"));
    config.login.onLoad(bootstrap.getConfig("login"));
    config.rank.onLoad(bootstrap.getConfig("rank"));
  }

  public void loadBasicConfig(Config bootstrap) {
    Preconditions.checkNotNull(bootstrap, "bootstrap config == null");
    config.home.setValue(bootstrap.getString("home"));
    config.dbName.setValue(bootstrap.getString("dbName"));
    config.gameName.setValue(bootstrap.getString("gameName"));
    config.gameAddress.setValue(bootstrap.getString("gameAddress"));
    config.backupAction.setValue(bootstrap.getBoolean("backupAction"));
  }

  public Config getDefaultConfig() {
    return ConfigFactory.load();
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
        // RxJava 只是一种异步调度神器，实现逻辑还放在私有方法里
        .filter(aLong -> {
          startServer();
          return checkAllServer();
        })
        // 回调就需要更新 UI，此时应该放到主线程上运行
        .observeOn(mainScheduler)
        .subscribe(subscriber);
  }

  public void cancelStart() {
    if (subscription != null && !subscription.isUnsubscribed()) {
      subscription.unsubscribe();
    }
  }

  private void startServer() {
    String homeValue = config.home.getValue();
    programModel.start(homeValue, config.database);
    programModel.start(homeValue, config.account);
    programModel.start(homeValue, config.logger);
    programModel.start(homeValue, config.core);
    programModel.start(homeValue, config.game);
    programModel.start(homeValue, config.role);
    programModel.start(homeValue, config.login);
    programModel.start(homeValue, config.rank);
  }

  private Boolean checkAllServer() {
    if (Environments.isDebug()) {
      return true;
    }
    String homeValue = config.home.getValue();
    boolean check = programModel.check(homeValue, config.database);
    check = check && programModel.check(homeValue, config.account);
    check = check && programModel.check(homeValue, config.logger);
    check = check && programModel.check(homeValue, config.core);
    check = check && programModel.check(homeValue, config.game);
    check = check && programModel.check(homeValue, config.role);
    check = check && programModel.check(homeValue, config.login);
    check = check && programModel.check(homeValue, config.rank);
    return check;
  }

  public void stopServer() {

  }

  public void cancelStop() {

  }

  public enum State {
    INITIALIZED("启动服务"),
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
