package youthm2.common.dialog;

import java.net.URL;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import youthm2.common.util.Throwables;

/**
 * 异常对话框。
 *
 * @author qiang.zhang
 */
public final class ThrowableDialog {
  private static final URL FXML =
      ThrowableDialog.class.getResource("/youthm2/common/throwable-dialog.fxml");
  private static final URL CSS =
      ThrowableDialog.class.getResource("/youthm2/common/common.css");

  public static void show(Throwable throwable) {
    try {
      Stage stage = new Stage();
      stage.setTitle("出错了");
      FXMLLoader loader = new FXMLLoader(FXML);
      Scene scene = new Scene(loader.load());
      scene.getStylesheets().add(CSS.toExternalForm());
      stage.setScene(scene);
      ThrowableDialog viewModel = loader.getController();
      viewModel.errorDialogTitle.setText("意料之外的错误");
      viewModel.errorDialogContent.setText(throwable.getMessage());
      viewModel.debugInfo = Throwables.print(throwable);
      viewModel.stage = stage;
      stage.showAndWait();
    } catch (Exception e) {
      AlertDialog.showError(e);
    }
  }

  @FXML Label errorDialogTitle;
  @FXML Label errorDialogContent;

  private Stage stage;
  private String debugInfo;

  @FXML void onShowDetailsClicked() {
    TextDialog.show(debugInfo);
  }

  @FXML void onCloseClicked() {
    stage.close();
  }
}