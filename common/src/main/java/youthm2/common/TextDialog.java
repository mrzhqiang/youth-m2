package youthm2.common;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import youthm2.common.model.AlertModel;
import youthm2.common.model.LoggerModel;
import youthm2.common.viewmodel.TextDialogViewModel;

/**
 * TextDialog
 *
 * @author qiang.zhang
 */
public final class TextDialog {
  private TextDialog() {
    throw new AssertionError("No instance");
  }

  public static void show(String text) {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(TextDialog.class.getResource("/text-dialog.fxml"));
    Stage stage = new Stage();
    try {
      Parent parent = loader.load();
      Scene scene = new Scene(parent);
      scene.getStylesheets()
          .add(TextDialog.class.getResource("/application.css").toExternalForm());
      stage.setScene(scene);
    } catch (IOException e) {
      LoggerModel.COMMON.error("创建文本对话框出错", e);
      AlertModel.showError(e);
      return;
    }
    stage.setTitle("详情");
    TextDialogViewModel viewModel = loader.getController();
    viewModel.setStage(stage)
        .setText(text)
        .showAndWait();
  }
}
