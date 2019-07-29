package youthm2.common.dialog;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import youthm2.common.model.AlertModel;
import youthm2.common.model.LoggerModel;
import youthm2.common.model.ThrowableModel;
import youthm2.common.viewmodel.ThrowableDialogViewModel;

/**
 * 异常对话框。
 *
 * @author qiang.zhang
 */
public final class ThrowableDialog {
  private ThrowableDialog() {
    throw new AssertionError("No instance");
  }

  public static void show(Throwable throwable) {
    show("一个意料之外的异常", throwable);
  }

  public static void show(String title, Throwable throwable) {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(ThrowableDialog.class.getResource("/throwable-dialog.fxml"));
    Stage stage = new Stage();
    try {
      Parent parent = loader.load();
      Scene scene = new Scene(parent);
      scene.getStylesheets()
          .add(ThrowableDialog.class.getResource("/application.css").toExternalForm());
      stage.setScene(scene);
    } catch (IOException e) {
      LoggerModel.COMMON.error("创建错误对话框出错", e);
      AlertModel.showError(e);
      return;
    }
    stage.setTitle("错误");
    ThrowableDialogViewModel viewModel = loader.getController();
    viewModel.setStage(stage)
        .setTitle(title)
        .setContent(throwable.getMessage())
        .setDebugInfo(ThrowableModel.print(throwable))
        .showAndWait();
  }
}
