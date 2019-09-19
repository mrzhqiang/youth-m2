package youthm2.bootstrap.config;

import com.typesafe.config.ConfigFactory;
import io.reactivex.Observable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import youthm2.common.util.Environments;
import youthm2.common.util.Files;

/**
 * 配置辅助工具。
 *
 * @author qiang.zhang
 */
public enum Configs {
  ;

  private static final String CONFIG_FILE = "bootstrap.json";

  public static Observable<BootstrapConfig> load() {
    return Observable.just(getConfigFile())
        // 丢到 IO 线程池执行，subscribeOn 方法只能被调用一次，多次调用无效
        .subscribeOn(Schedulers.io())
        .map(ConfigFactory::parseFile)
        .map(config -> config.withFallback(ConfigFactory.load()))
        .map(BootstrapConfig::of)
        // 订阅在主线程，可以更新 UI
        .observeOn(JavaFxScheduler.platform());
  }

  public static Observable save(BootstrapConfig config) {
    return Observable.just(getConfigFile())
        .subscribeOn(Schedulers.io())
        .doOnNext(Files::createOrExists)
        //.doOnNext(file -> FileModel.onceWrite(file, Json.prettyPrint(Json.toJson(config))))
        .map(file -> config)
        .observeOn(JavaFxScheduler.platform());
  }

  private static File getConfigFile() {
    // debug 模式是指，在 IDEA 中调试程序，其他 IDE 暂未支持。
    String parent = Environments.debugMode() ? Environments.debugWork() : Environments.homework();
    return new File(parent, CONFIG_FILE);
  }
}
