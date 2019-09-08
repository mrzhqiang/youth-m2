package youthm2.bootstrap.model;

import com.google.common.base.Preconditions;
import com.typesafe.config.ConfigFactory;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import java.io.File;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import youthm2.bootstrap.model.config.BootstrapConfig;
import youthm2.common.util.Files;

//import static youthm2.common.Environment.debugDirectory;
//import static youthm2.common.Environment.isDebug;
//import static youthm2.common.Environment.workDirectory;

/**
 * 配置模型。
 *
 * @author qiang.zhang
 */
public final class ConfigModel {
  private static final String CONFIG_FILE = "bootstrap.json";

  public interface OnLoadListener {
    void onLoaded(BootstrapConfig config);
  }

  public interface OnSaveListener {
    void onSaved(BootstrapConfig config);
  }

  public void load(OnLoadListener listener) {
    Preconditions.checkNotNull(listener, "listener == null");
    Observable.just(getConfigFile())
        // 丢到 IO 线程池执行，subscribeOn 方法只能被调用一次，多次调用无效
        .subscribeOn(Schedulers.io())
        .map(ConfigFactory::parseFile)
        .map(config -> config.withFallback(ConfigFactory.load()))
        .map(BootstrapConfig::of)
        // 订阅在主线程，可以更新 UI
        .observeOn(JavaFxScheduler.platform())
        .subscribe(listener::onLoaded, e -> ThrowableDialog.show("加载配置出错", e));
  }

  public void loadDefault(OnLoadListener listener) {
    Preconditions.checkNotNull(listener, "listener == null");
    // empty 方法是为了立即执行完成回调，比如在执行过程中发现问题，立即 flatMap 一个 empty，从而结束此次任务
    // 所以这里不能用 empty，而应该是 just 一个空串
    Observable.just("")
        .subscribeOn(Schedulers.io())
        .map(s -> ConfigFactory.load())
        .map(BootstrapConfig::of)
        .observeOn(JavaFxScheduler.platform())
        .subscribe(listener::onLoaded, e -> ThrowableDialog.show("加载默认配置出错", e));
  }

  public void saveConfig(BootstrapConfig config, OnSaveListener listener) {
    Preconditions.checkNotNull(listener, "listener == null");
    Observable.just(getConfigFile())
        .subscribeOn(Schedulers.io())
        .doOnNext(Files::createOrExists)
        //.doOnNext(file -> FileModel.onceWrite(file, Json.prettyPrint(Json.toJson(config))))
        .map(file -> config)
        .observeOn(JavaFxScheduler.platform())
        .subscribe(listener::onSaved, e -> ThrowableDialog.show("保存配置出错", e));
  }

  private File getConfigFile() {
    // debug 模式是指，在 IDEA 中调试程序，其他 IDE 暂未支持。
    //String parent = isDebug() ? debugDirectory() : workDirectory();
    return new File(/*parent, */CONFIG_FILE);
  }
}
