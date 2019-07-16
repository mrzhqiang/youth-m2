package youthm2.common;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import youthm2.common.model.AlertModel;
import youthm2.common.model.LoggerModel;
import youthm2.common.model.ThrowableModel;
import youthm2.common.viewmodel.ErrorDialogViewModel;

/**
 * ErrorDialog
 *
 * @author qiang.zhang
 */
public final class ErrorDialog {
  private ErrorDialog() {
    throw new AssertionError("No instance");
  }

  public static void show(Throwable throwable) {
    show("一个意料之外的错误", throwable);
  }

  public static void show(String title, Throwable throwable) {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(ErrorDialog.class.getResource("/error-dialog.fxml"));
    Stage stage = new Stage();
    try {
      Parent parent = loader.load();
      Scene scene = new Scene(parent);
      scene.getStylesheets()
          .add(ErrorDialog.class.getResource("/application.css").toExternalForm());
      stage.setScene(scene);
    } catch (IOException e) {
      LoggerModel.COMMON.error("创建错误对话框出错", e);
      AlertModel.showError(e);
      return;
    }
    stage.setTitle("错误");
    ErrorDialogViewModel viewModel = loader.getController();
    viewModel.setStage(stage)
        .setTitle(title)
        .setContent(throwable.getMessage())
        .setDebugInfo(ThrowableModel.print(throwable))
        .showAndWait();
  }
}
