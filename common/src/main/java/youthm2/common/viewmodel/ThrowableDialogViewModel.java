package youthm2.common.viewmodel;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import youthm2.common.dialog.TextDialog;
import youthm2.common.model.ThrowableModel;

/**
 * 异常对话框视图模型。
 *
 * @author qiang.zhang
 */
public final class ThrowableDialogViewModel {
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

  public ThrowableDialogViewModel setTitle(String title) {
    errorDialogTitle.setText(title);
    return this;
  }

  public ThrowableDialogViewModel setContent(String content) {
    errorDialogContent.setText(content);
    return this;
  }

  public ThrowableDialogViewModel setStage(Stage stage) {
    Preconditions.checkNotNull(stage, "stage == null");
    this.stage = stage;
    return this;
  }

  public ThrowableDialogViewModel setDebugInfo(String debugInfo) {
    this.debugInfo =
        Strings.isNullOrEmpty(debugInfo) ? ThrowableModel.printStackTrace() : debugInfo;
    return this;
  }

  public void showAndWait() {
    stage.showAndWait();
  }
}
