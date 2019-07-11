package youthm2.common.model;

import com.google.common.base.Preconditions;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javax.annotation.Nullable;

/**
 * 警告模型。
 *
 * @author qiang.zhang
 */
public final class AlertModel {
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
}
