package youthm2.bootstrap.model;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import youthm2.bootstrap.model.config.BootstrapConfig;
import youthm2.bootstrap.model.program.Program;
import youthm2.common.Environment;
import youthm2.common.Json;
import youthm2.common.dialog.ThrowableDialog;
import youthm2.common.exception.FileException;
import youthm2.common.model.FileModel;
import youthm2.common.model.SchedulerModel;

/**
 * 引导模型。
 *
 * @author mrzhqiang
 */
public final class BootstrapModel {
  private static final String CONFIG_FILE = "bootstrap.json";

  private static final String CONFIG_BOOTSTRAP = "bootstrap";

  public interface OnConfigLoadListener {
    void onLoaded(BootstrapConfig config);
  }

  public interface OnStartProgramListener {
    void onStart();

    void onError(Throwable e);

    void onFinish(Program program);
  }

  public final ProgramModel programModel = new ProgramModel();

  public State state;

  public void loadConfig(OnConfigLoadListener listener) {
    Preconditions.checkNotNull(listener, "listener == null");
    // just 方法表示传入几个参数，就执行几次任务
    Observable.just(getConfigFile())
        // 丢到 IO 线程池执行，subscribeOn 方法只能被调用一次，多次调用无效
        .subscribeOn(Schedulers.io())
        // 通过解析方法读取文件内容
        .map(ConfigFactory::parseFile)
        // 未填写的内容由默认配置补充
        .map(config -> config.withFallback(ConfigFactory.load()))
        .filter(config -> config.hasPath(CONFIG_BOOTSTRAP))
        .map(config -> config.getConfig(CONFIG_BOOTSTRAP))
        .map(BootstrapConfig::of)
        // 订阅在主线程，可以更新 UI
        .observeOn(SchedulerModel.main())
        .subscribe(listener::onLoaded, e -> ThrowableDialog.show("加载配置出错", e));
  }

  public void loadDefaultConfig(OnConfigLoadListener listener) {
    Preconditions.checkNotNull(listener, "listener == null");
    // empty 方法是为了立即执行完成回调，比如在执行过程中发现问题，立即 flatMap 一个 empty，从而结束此次任务
    // 所以这里不能用 empty，而应该是 just 一个空串
    Observable.just("")
        .subscribeOn(Schedulers.io())
        .map(s -> ConfigFactory.load())
        .observeOn(SchedulerModel.main())
        .subscribe(new Subscriber<Config>() {
          @Override public void onCompleted() {
            // no-op
          }

          @Override public void onError(Throwable e) {
            ThrowableDialog.show(e);
          }

          @Override public void onNext(Config config) {
            listener.onLoaded(BootstrapConfig.of(config.getConfig("bootstrap")));
          }
        });
  }

  public void updateProgram(BootstrapConfig config) {
    Preconditions.checkNotNull(config, "bootstrap config == null");
    Program.DATABASE.from(config.database);
    Program.ACCOUNT.from(config.account);
    Program.LOGGER.from(config.logger);
    Program.CORE.from(config.core);
    Program.GAME.from(config.game);
    Program.ROLE.from(config.role);
    Program.LOGIN.from(config.login);
    Program.RANK.from(config.rank);
  }

  public void startProgram(LocalDateTime time, OnStartProgramListener listener) {
    Preconditions.checkNotNull(time, "target time == null");
    Preconditions.checkNotNull(listener, "listener == null");
    // interval 方法是间隔一段时间执行任务，与 timer 不同的是，这个方法是永久重复执行，直到被取消订阅
    Observable.interval(0, 1, TimeUnit.SECONDS)
        // 是否满足预期时间
        .filter(aLong -> LocalDateTime.now().isAfter(time))
        // RxJava 只是一种异步调度器，建议组装一下逻辑实现
        .filter(aLong -> programModel.start(Program.DATABASE))
        // todo 应该把程序状态对外暴露，而不是留在内部判断
        .doOnNext(aLong -> listener.onFinish(Program.DATABASE))
        .filter(aLong -> programModel.start(Program.ACCOUNT))
        .doOnNext(aLong -> listener.onFinish(Program.ACCOUNT))
        .filter(aLong -> programModel.start(Program.LOGGER))
        .doOnNext(aLong -> listener.onFinish(Program.LOGGER))
        .filter(aLong -> programModel.start(Program.CORE))
        .doOnNext(aLong -> listener.onFinish(Program.CORE))
        .filter(aLong -> programModel.start(Program.GAME))
        .doOnNext(aLong -> listener.onFinish(Program.GAME))
        .filter(aLong -> programModel.start(Program.ROLE))
        .doOnNext(aLong -> listener.onFinish(Program.ROLE))
        .filter(aLong -> programModel.start(Program.LOGIN))
        .doOnNext(aLong -> listener.onFinish(Program.LOGIN))
        .filter(aLong -> programModel.start(Program.RANK))
        .doOnNext(aLong -> listener.onFinish(Program.RANK))
        // 回调就需要更新 UI，此时应该放到主线程上运行
        .observeOn(SchedulerModel.main())
        .subscribe(new Subscriber<Long>() {
          @Override public void onStart() {
            state = BootstrapModel.State.STARTING;
            listener.onStart();
          }

          @Override public void onCompleted() {
            // the method will not be call
          }

          @Override public void onError(Throwable e) {
            state = BootstrapModel.State.INITIALIZED;
            listener.onError(e);
          }

          @Override public void onNext(Long aLong) {
            state = BootstrapModel.State.RUNNING;
            unsubscribe();
            listener.onFinish(null);
          }
        });
  }

  public void saveConfig(BootstrapConfig config) {
    File configFile = getConfigFile();
    try {
      FileModel.createOrExists(configFile);
      FileModel.onceWrite(configFile, Json.prettyPrint(Json.toJson(config)));
    } catch (FileException e) {
      ThrowableDialog.show(e);
    }
  }

  public void cancelStart() {
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
