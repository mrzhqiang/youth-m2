package youthm2.bootstrap.model;

import com.google.common.base.Preconditions;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import youthm2.bootstrap.model.config.BootstrapConfig;
import youthm2.bootstrap.model.program.Program;

/**
 * 引导模型。
 *
 * @author mrzhqiang
 */
public final class BootstrapModel {

  public interface OnWaitingListener {

    void onError(Throwable e);

    void onFinish();
  }

  public interface OnStartListener extends OnWaitingListener {
    void onStart(Program program);
  }

  public void waiting(LocalDateTime time, OnWaitingListener listener) {
    Preconditions.checkNotNull(time, "target time == null");
    Preconditions.checkNotNull(listener, "listener == null");
    // interval 方法是间隔一段时间执行任务，与 timer 不同的是，这个方法是永久重复执行，直到被取消订阅
    /*subscription = */Observable.interval(0, 1, TimeUnit.SECONDS)
        // 是否满足预期时间
        .filter(aLong -> LocalDateTime.now().isAfter(time))
        // RxJava 只是一种异步调度器，建议组装一下逻辑实现
        // 回调就需要更新 UI，此时应该放到主线程上运行
        .observeOn(JavaFxScheduler.platform())
        // interval 不会自动结束，所以 onCompleted 不会调用到，并且需要主动调用 unsubscribe 去结束
        .subscribe(/*new Subscriber<Long>() {
          @Override public void onCompleted() {
            // no-op
          }

          @Override public void onError(Throwable e) {
            listener.onError(e);
          }

          @Override public void onNext(Long aLong) {
            subscription.unsubscribe();
            subscription = null;
            listener.onFinish();
          }
        }*/);
  }

  public void stopWaiting() {
    //if (subscription != null && !subscription.isUnsubscribed()) {
    //  subscription.unsubscribe();
    //}
  }

  public void start(BootstrapConfig config, OnStartListener listener) {
    Preconditions.checkNotNull(config, "config == null");
    if (config.database.enabled) {
      //Database.launch();
      //listener.onStart("数据库服务启动成功");
    }
    if (config.account.enabled) {
      //listener.onStart("账号服务启动成功");
    }
    if (config.logger.enabled) {
      //listener.onStart("日志服务启动成功");
    }
    if (config.core.enabled) {
      //listener.onStart("账号服务启动成功");
    }
    if (config.game.enabled) {
      //listener.onStart("账号服务启动成功");
    }
    if (config.role.enabled) {
      //listener.onStart("账号服务启动成功");
    }
    if (config.login.enabled) {
      //listener.onStart("账号服务启动成功");
    }
    if (config.rank.enabled) {
      //listener.onStart("账号服务启动成功");
    }
    listener.onFinish();
  }

  public void cancelStart() {
  }

  public void stop() {

  }

  public void cancelStop() {

  }
}
