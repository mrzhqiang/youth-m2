package youthm2.common.viewmodel;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import youthm2.common.dialog.TextDialog;
import youthm2.common.model.ThrowableModel;

/**
 * ErrorDialogViewModel
 *
 * @author qiang.zhang
 */
public final class ErrorDialogViewModel {
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

  public ErrorDialogViewModel setTitle(String title) {
    errorDialogTitle.setText(title);
    return this;
  }

  public ErrorDialogViewModel setContent(String content) {
    errorDialogContent.setText(content);
    return this;
  }

  public ErrorDialogViewModel setStage(Stage stage) {
    Preconditions.checkNotNull(stage, "stage == null");
    this.stage = stage;
    return this;
  }

  public ErrorDialogViewModel setDebugInfo(String debugInfo) {
    this.debugInfo =
        Strings.isNullOrEmpty(debugInfo) ? ThrowableModel.printStackTrace() : debugInfo;
    return this;
  }

  public void showAndWait() {
    stage.showAndWait();
  }
}
