package youthm2.bootstrap.model;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javax.annotation.Nullable;
import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;
import youthm2.bootstrap.model.config.BootstrapConfig;
import youthm2.common.Environment;
import youthm2.common.Json;
import youthm2.common.exception.FileException;
import youthm2.common.model.FileModel;

/**
 * 引导模型。
 *
 * @author mrzhqiang
 */
public final class BootstrapModel {
  private static final Logger LOGGER = LoggerFactory.getLogger("bootstrap");
  private static final Config DEFAULT_CONFIG = ConfigFactory.load();
  private static final String CONFIG_FILE = "bootstrap.json";
  private static final String CONFIG_BOOTSTRAP = "bootstrap";
  private static final String CONFIG_DATABASE = "database";
  private static final String CONFIG_ACCOUNT = "account";
  private static final String CONFIG_LOGGER1 = "logger";
  private static final String CONFIG_CORE = "core";
  private static final String CONFIG_GAME = "game";
  private static final String CONFIG_ROLE = "role";
  private static final String CONFIG_LOGIN = "login";
  private static final String CONFIG_RANK = "rank";
  private static final String DEBUG_ROOT_DIR = "sample";

  public final BootstrapConfig config = new BootstrapConfig();

  public State state = State.INITIALIZED;

  private final ProgramModel databaseModel = new ProgramModel(config.database);
  private final ProgramModel accountModel = new ProgramModel(config.account);
  private final ProgramModel loggerModel = new ProgramModel(config.logger);
  private final ProgramModel coreModel = new ProgramModel(config.core);
  private final ProgramModel gameModel = new ProgramModel(config.game);
  private final ProgramModel roleModel = new ProgramModel(config.role);
  private final ProgramModel loginModel = new ProgramModel(config.login);
  private final ProgramModel rankModel = new ProgramModel(config.rank);
  private final Scheduler mainScheduler = Schedulers.from(Platform::runLater);

  private Subscription subscription = null;

  public void loadConfig() {
    File configFile = getConfigFile();
    // 以 configFile 为主，缺失的由默认配置 reference.conf 填补
    Config conf = ConfigFactory.parseFile(configFile).withFallback(DEFAULT_CONFIG);
    // 只接受其中的 bootstrap 节点
    Config bootstrap = conf.getConfig(CONFIG_BOOTSTRAP);
    loadBootstrapConfig(bootstrap);
    loadDatabaseConfig(bootstrap);
    loadAccountConfig(bootstrap);
    loadLoggerConfig(bootstrap);
    loadCoreConfig(bootstrap);
    loadGameConfig(bootstrap);
    loadRoleConfig(bootstrap);
    loadLoginConfig(bootstrap);
    loadRankConfig(bootstrap);
  }

  public void loadBootstrapConfig(@Nullable Config bootstrap) {
    if (bootstrap == null) {
      bootstrap = DEFAULT_CONFIG.getConfig(CONFIG_BOOTSTRAP);
    }
    config.home.setValue(bootstrap.getString("home"));
    config.dbName.setValue(bootstrap.getString("dbName"));
    config.gameName.setValue(bootstrap.getString("gameName"));
    config.gameAddress.setValue(bootstrap.getString("gameAddress"));
    config.backupAction.setValue(bootstrap.getBoolean("backupAction"));
    config.compoundAction.setValue(bootstrap.getBoolean("compoundAction"));
  }

  public void loadDatabaseConfig(@Nullable Config bootstrap) {
    if (bootstrap == null) {
      bootstrap = DEFAULT_CONFIG.getConfig(CONFIG_BOOTSTRAP);
    }
    config.database.onLoad(bootstrap.getConfig(CONFIG_DATABASE));
  }

  public void loadAccountConfig(@Nullable Config bootstrap) {
    if (bootstrap == null) {
      bootstrap = DEFAULT_CONFIG.getConfig(CONFIG_BOOTSTRAP);
    }
    config.account.onLoad(bootstrap.getConfig(CONFIG_ACCOUNT));
  }

  public void loadLoggerConfig(@Nullable Config bootstrap) {
    if (bootstrap == null) {
      bootstrap = DEFAULT_CONFIG.getConfig(CONFIG_BOOTSTRAP);
    }
    config.logger.onLoad(bootstrap.getConfig(CONFIG_LOGGER1));
  }

  public void loadCoreConfig(@Nullable Config bootstrap) {
    if (bootstrap == null) {
      bootstrap = DEFAULT_CONFIG.getConfig(CONFIG_BOOTSTRAP);
    }
    config.core.onLoad(bootstrap.getConfig(CONFIG_CORE));
  }

  public void loadGameConfig(@Nullable Config bootstrap) {
    if (bootstrap == null) {
      bootstrap = DEFAULT_CONFIG.getConfig(CONFIG_BOOTSTRAP);
    }
    config.game.onLoad(bootstrap.getConfig(CONFIG_GAME));
  }

  public void loadLoginConfig(@Nullable Config bootstrap) {
    if (bootstrap == null) {
      bootstrap = DEFAULT_CONFIG.getConfig(CONFIG_BOOTSTRAP);
    }
    config.login.onLoad(bootstrap.getConfig(CONFIG_LOGIN));
  }

  public void loadRoleConfig(@Nullable Config bootstrap) {
    if (bootstrap == null) {
      bootstrap = DEFAULT_CONFIG.getConfig(CONFIG_BOOTSTRAP);
    }
    config.role.onLoad(bootstrap.getConfig(CONFIG_ROLE));
  }

  public void loadRankConfig(@Nullable Config bootstrap) {
    if (bootstrap == null) {
      bootstrap = DEFAULT_CONFIG.getConfig(CONFIG_BOOTSTRAP);
    }
    config.rank.onLoad(bootstrap.getConfig(CONFIG_RANK));
  }

  public void saveConfig() {
    File configFile = getConfigFile();
    try {
      FileModel.existsOrCreate(configFile);
      FileModel.onceWrite(configFile, Json.prettyPrint(config.toJsonNode()));
    } catch (FileException e) {
      new ExceptionDialog(e).show();
    }
  }

  public void startServer(LocalDateTime targetTime, Subscriber<String> subscriber) {
    Preconditions.checkNotNull(targetTime, "target time == null");
    Preconditions.checkNotNull(subscriber, "subscriber == null");
    subscription = Observable.interval(0, 1, TimeUnit.SECONDS)
        // 是否抵达目标时间
        .filter(aLong -> LocalDateTime.now().isAfter(targetTime))
        // RxJava 只是一种异步调度神器，实现逻辑还放在私有方法里
        .map(aLong -> startDatabaseServer())
        //.filter(aLong -> startAccountServer())
        //.filter(aLong -> startLoggerServer())
        //.filter(aLong -> startCoreServer())
        //.filter(aLong -> startGameServer())
        //.filter(aLong -> startRoleServer())
        //.filter(aLong -> startLoginServer())
        //.filter(aLong -> startRankServer())
        // 回调就需要更新 UI，此时应该放到主线程上运行
        .observeOn(mainScheduler)
        .subscribe(subscriber);
  }

  public void cancelStart() {
    if (subscription != null && !subscription.isUnsubscribed()) {
      subscription.unsubscribe();
    }
  }

  public void stopServer() {

  }

  public void cancelStop() {

  }

  private File getConfigFile() {
    File configFile;
    if (Environment.isDebug()) {
      // DEBUG 模式，就读取 sample 目录下的配置。
      configFile = new File(DEBUG_ROOT_DIR, CONFIG_FILE);
    } else {
      // 非 DEBUG 模式，读取当前目录下的配置。
      // 注意：直接在 IDEA 中 Run 的话，那么使用的是内置配置。
      configFile = new File(CONFIG_FILE);
    }
    return configFile;
  }

  private String startDatabaseServer() {
    databaseModel.start();
    databaseModel.check();
    return "";
  }

  private Boolean startAccountServer() {
    accountModel.start();
    return accountModel.check();
  }

  private Boolean startLoggerServer() {
    loggerModel.start();
    return loggerModel.check();
  }

  private Boolean startCoreServer() {
    coreModel.start();
    return coreModel.check();
  }

  private Boolean startGameServer() {
    gameModel.start();
    return gameModel.check();
  }

  private Boolean startRoleServer() {
    roleModel.start();
    return roleModel.check();
  }

  private Boolean startLoginServer() {
    loginModel.start();
    return loginModel.check();
  }

  private Boolean startRankServer() {
    rankModel.start();
    return rankModel.check();
  }

  public enum State {
    INITIALIZED("启动服务", "是否启动服务？"),
    STARTING("正在启动..", "正在启动，是否停止？"),
    RUNNING("停止服务", "是否停止服务？"),
    STOPPING("正在停止..", "正在停止，是否取消？"),
    ;

    private final String label;
    private final String message;

    State(String label, String message) {
      this.label = label;
      this.message = message;
    }

    public String getMessage() {
      return message;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
