package youthm2.bootstrap;

import com.google.common.base.Preconditions;
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
import javax.swing.event.ChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;
import youthm2.bootstrap.config.BootstrapConfig;
import youthm2.common.monitor.Monitor;

/**
 * 引导模型。
 *
 * @author mrzhqiang
 */
public final class BootstrapModel {
  private static final Logger LOGGER = LoggerFactory.getLogger("bootstrap");
  private static final String CONFIG_FILE = "bootstrap.json";
  private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

  public final BootstrapConfig config = new BootstrapConfig();

  private final Scheduler mainScheduler = Schedulers.from(Platform::runLater);
  private final Gson gson = new GsonBuilder()
      .setPrettyPrinting()
      .setDateFormat(DATE_FORMAT)
      .serializeNulls()
      .create();

  private State state = State.DEFAULT;
  private Instant startInstant = Instant.EPOCH;
  private Duration waitDuration = Duration.ZERO;
  private Subscription subscription = null;

  public void loadConfig() {
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
    config.wuxingAction.setValue(bootstrap.getBoolean("wuxingAction"));

    config.database.onLoad(bootstrap.getConfig("database"));
    config.account.onLoad(bootstrap.getConfig("account"));
    config.logger.onLoad(bootstrap.getConfig("logger"));
    config.core.onLoad(bootstrap.getConfig("core"));
    config.game.onLoad(bootstrap.getConfig("game"));
    config.role.onLoad(bootstrap.getConfig("role"));
    config.login.onLoad(bootstrap.getConfig("login"));
    config.rank.onLoad(bootstrap.getConfig("rank"));
  }

  public void saveConfig() {
    File configFile = new File(CONFIG_FILE);
    try (BufferedWriter writer = Files.newWriter(configFile, Charset.forName("UTF-8"))) {
      //writer.write(gson.toJson(configModel));
      writer.flush();
    } catch (IOException e) {
      LOGGER.error("保存配置出错", e);
    }
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
