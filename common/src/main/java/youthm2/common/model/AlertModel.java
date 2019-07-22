package youthm2.common.model;

import com.google.common.base.Preconditions;
import java.util.Optional;
import java.util.function.Predicate;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javax.annotation.Nullable;

/**
 * 警告模型。
 * <p>
 * 为什么要有这种的一个模型呢？
 * <p>
 * 因为有时候系统 API 在你的代码里泛滥成灾，而当你想重构的时候，你会发现重复操作非常之多。
 * <p>
 * 与其这样，还不如从一开始就在模型里面限定系统 API 的调用方式，从而避免诸多问题。
 *
 * @author qiang.zhang
 */
public final class AlertModel {
  private AlertModel() {
    throw new AssertionError("No instance");
  }

  public static void show(String message, @Nullable String content) {
    Preconditions.checkNotNull(message, "message == null");
    Alert alert = new Alert(Alert.AlertType.NONE);
    alert.setHeaderText(message);
    alert.setContentText(content);
    alert.show();
  }

  public static void showInfo(String message, @Nullable String content) {
    Preconditions.checkNotNull(message, "message == null");
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("信息");
    alert.setHeaderText(message);
    alert.setContentText(content);
    alert.show();
  }

  public static void showWarn(String message, @Nullable String content) {
    Preconditions.checkNotNull(message, "message == null");
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle("警告");
    alert.setHeaderText(message);
    alert.setContentText(content);
    alert.show();
  }

  public static Optional<ButtonType> waitConfirm(String message, @Nullable String content) {
    Preconditions.checkNotNull(message, "message == null");
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("确认");
    alert.setHeaderText(message);
    alert.setContentText(content);
    return alert.showAndWait();
  }

  public static void showError(Throwable error) {
    Preconditions.checkNotNull(error, "error == null");
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("错误");
    alert.setHeaderText(error.getMessage());
    alert.setContentText(ThrowableModel.print(error));
    alert.show();
  }

  public static Predicate<? super ButtonType> isOK() {
    return type -> ButtonType.OK == type;
  }
}
