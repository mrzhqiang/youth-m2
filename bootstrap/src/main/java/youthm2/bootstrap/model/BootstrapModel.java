package youthm2.bootstrap.model;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javax.annotation.Nullable;
import org.controlsfx.dialog.ExceptionDialog;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;
import youthm2.bootstrap.model.config.BootstrapConfig;
import youthm2.common.Environment;
import youthm2.common.Json;
import youthm2.common.dialog.ThrowableDialog;
import youthm2.common.exception.FileException;
import youthm2.common.model.ConfigModel;
import youthm2.common.model.FileModel;

/**
 * 引导模型。
 *
 * @author mrzhqiang
 */
public final class BootstrapModel {
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

  public final BootstrapConfig config = new BootstrapConfig();

  public State state;

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

  public void loadConfig(OnLoadConfigListener listener) {
    Preconditions.checkNotNull(listener, "listener == null");
    Observable.just(getConfigFile())
        .subscribeOn(Schedulers.io())
        .map(ConfigModel::load)
        .map(ConfigModel::mergeDefault)
        .observeOn(mainScheduler)
        .subscribe(new Subscriber<Config>() {
          @Override public void onCompleted() {
            // no-op
          }

          @Override public void onError(Throwable e) {
            ThrowableDialog.show(e);
          }

          @Override public void onNext(Config config) {
            listener.onLoaded(config);
          }
        });
  }

  public void loadBootstrapConfig(@Nullable Config bootstrap) {
    if (bootstrap == null) {
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
    }
    config.database.onLoad(bootstrap.getConfig(CONFIG_DATABASE));
  }

  public void loadAccountConfig(@Nullable Config bootstrap) {
    if (bootstrap == null) {
    }
    config.account.onLoad(bootstrap.getConfig(CONFIG_ACCOUNT));
  }

  public void loadLoggerConfig(@Nullable Config bootstrap) {
    if (bootstrap == null) {
    }
    config.logger.onLoad(bootstrap.getConfig(CONFIG_LOGGER1));
  }

  public void loadCoreConfig(@Nullable Config bootstrap) {
    if (bootstrap == null) {
    }
    config.core.onLoad(bootstrap.getConfig(CONFIG_CORE));
  }

  public void loadGameConfig(@Nullable Config bootstrap) {
    if (bootstrap == null) {
    }
    config.game.onLoad(bootstrap.getConfig(CONFIG_GAME));
  }

  public void loadLoginConfig(@Nullable Config bootstrap) {
    if (bootstrap == null) {
    }
    config.login.onLoad(bootstrap.getConfig(CONFIG_LOGIN));
  }

  public void loadRoleConfig(@Nullable Config bootstrap) {
    if (bootstrap == null) {
    }
    config.role.onLoad(bootstrap.getConfig(CONFIG_ROLE));
  }

  public void loadRankConfig(@Nullable Config bootstrap) {
    if (bootstrap == null) {
    }
    config.rank.onLoad(bootstrap.getConfig(CONFIG_RANK));
  }

  public void saveConfig() {
    File configFile = getConfigFile();
    try {
      FileModel.createOrExists(configFile);
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
      // DEBUG 模式，读取 sample 目录下的配置。
      configFile = new File(Environment.debugDirectory(), CONFIG_FILE);
    } else {
      // 非 DEBUG 模式，读取当前目录下的配置。
      // 注意：直接在 IDEA 中 Run 的话，那么使用内置的默认配置。
      configFile = new File(Environment.workDirectory(), CONFIG_FILE);
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
