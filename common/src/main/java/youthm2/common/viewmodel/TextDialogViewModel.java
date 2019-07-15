package youthm2.common.viewmodel;

import java.awt.TextArea;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.stage.Stage;
import youthm2.common.model.AlertModel;
import youthm2.common.model.LoggerModel;

/**
 * TextDialogViewModel
 *
 * @author qiang.zhang
 */
public final class TextDialogViewModel {
  @FXML TextArea contentTextArea;

  @FXML void onCopyClicked() {
    Map<DataFormat, Object> content = new HashMap<>();
    content.put(DataFormat.PLAIN_TEXT, contentTextArea.getText());
    Clipboard.getSystemClipboard().setContent(content);
  }

  @FXML void onCloseClicked() {
    stage.close();
  }

  private Stage stage;

  public void showAndWait() {
    stage.showAndWait();
  }

  public void setText(String text) {
    contentTextArea.setText(text);
  }

  public static TextDialogViewModel newInstance() {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(TextDialogViewModel.class.getResource("/text-dialog.fxml"));
    Stage stage = new Stage();
    try {
      Parent parent = loader.load();
      Scene scene = new Scene(parent);
      scene.getStylesheets()
          .add(TextDialogViewModel.class.getResource("/application.css").toExternalForm());
      stage.setScene(scene);
    } catch (IOException e) {
      LoggerModel.COMMON.error("创建文本对话框出错", e);
      AlertModel.showError(e);
    }
    TextDialogViewModel viewModel = loader.getController();
    viewModel.stage = stage;
    return viewModel;
  }
}
