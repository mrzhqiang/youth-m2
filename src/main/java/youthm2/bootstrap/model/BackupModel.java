package youthm2.bootstrap.model;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
//import io.reactivex.Subscriber;
import io.reactivex.schedulers.Schedulers;
import youthm2.bootstrap.model.backup.BackupData;
//import youthm2.common.Environment;

/**
 * 文件备份管理器。
 *
 * @author mrzhqiang
 */
public final class BackupModel {
  private static final String BACKUP_FILE = "backup.json";

  public interface OnLoadDataListener {
    void onLoaded(List<BackupData> dataList);
  }

  public final ObservableList<BackupData> dataList = FXCollections.observableArrayList();
  private final Scheduler mainScheduler = Schedulers.from(Platform::runLater);

  public int status;

  public void start() {

  }

  public void stop() {

  }

  public void loadConfig(OnLoadDataListener listener) {
    Preconditions.checkNotNull(listener, "listener == null");
    Observable.just(getBackupFile())
        .subscribeOn(Schedulers.io())
        .map(ConfigFactory::parseFile)
        .filter(config -> config.hasPath("dataList"))
        .map(config -> config.getStringList("dataList").stream()
            .filter(s -> Strings.isNullOrEmpty(s) && config.hasPath(s))
            .map(config::getConfig)
            .map(this::toBackupData)
            .collect(Collectors.toList()))
        .observeOn(JavaFxScheduler.platform())
        .subscribe(/*new Subscriber<List<BackupData>>() {
          @Override public void onCompleted() {
            // no-op
          }

          @Override public void onError(Throwable e) {
            ThrowableDialog.show(e);
          }

          @Override public void onNext(List<BackupData> dataList) {
            listener.onLoaded(dataList);
          }
        }*/);
  }

  private File getBackupFile() {
    File backupFile;
    //if (Environment.isDebug()) {
      backupFile = new File(/*Environment.debugDirectory(), */BACKUP_FILE);
    //} else {
    //  backupFile = new File(Environment.workDirectory(), BACKUP_FILE);
    //}
    return backupFile;
  }

  private BackupData toBackupData(Config config) {
    Preconditions.checkNotNull(config, "config == null");
    BackupData data = new BackupData();
    data.source.setValue(config.getString("source"));
    data.sink.setValue(config.getString("sink"));
    data.hour.setValue(config.getInt("hour"));
    data.min.setValue(config.getInt("min"));
    data.backupMode.setValue(config.getInt("backupMode"));
    data.backupEnabled.setValue(config.getBoolean("backupEnabled"));
    data.zipEnabled.setValue(config.getBoolean("zipEnabled"));
    return data;
  }

  public void initialized() {

  }
}
