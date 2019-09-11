package youthm2.common.dialog;

import com.google.common.base.Preconditions;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javax.annotation.Nullable;
import youthm2.common.util.Throwables;

/**
 * 警告对话框。
 *
 * @author mrzhqiang
 */
public enum AlertDialog {
  ;

  public static void showInfo(String message) {
    showInfo(message, null);
  }

  public static void showInfo(String message, @Nullable String content) {
    Preconditions.checkNotNull(message, "message == null");
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("信息");
    alert.setHeaderText(message);
    alert.setContentText(content);
    alert.show();
  }

  public static void showWarn(String message) {
    showWarn(message, null);
  }

  public static void showWarn(String message, @Nullable String content) {
    Preconditions.checkNotNull(message, "message == null");
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle("警告");
    alert.setHeaderText(message);
    alert.setContentText(content);
    alert.show();
  }

  public static void showError(Throwable error) {
    Preconditions.checkNotNull(error, "error == null");
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("错误");
    alert.setHeaderText(error.getMessage());
    alert.setContentText(Throwables.print(error));
    alert.show();
  }

  public static Optional<ButtonType> waitConfirm(String message) {
    return waitConfirm(message, null);
  }

  public static Optional<ButtonType> waitConfirm(String message, @Nullable String content) {
    Preconditions.checkNotNull(message, "message == null");
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("请确认");
    alert.setHeaderText(message);
    alert.setContentText(content);
    return alert.showAndWait().filter(buttonType -> buttonType == ButtonType.OK);
  }
}
