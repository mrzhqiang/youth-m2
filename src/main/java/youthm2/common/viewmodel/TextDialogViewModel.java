package youthm2.common.viewmodel;

import java.net.URL;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

/**
 * 文本对话框视图模型。
 *
 * @author qiang.zhang
 */
public final class TextDialogViewModel {
  private static final URL FXML =
      TextDialogViewModel.class.getResource("/youthm2/common/text-dialog.fxml");
  private static final URL CSS =
      TextDialogViewModel.class.getResource("/youthm2/common/common.css");

  public static void show(String text) {
    try {
      Stage stage = new Stage();
      stage.setTitle("详情");
      FXMLLoader loader = new FXMLLoader(FXML);
      Scene scene = new Scene(loader.load());
      scene.getStylesheets().add(CSS.toExternalForm());
      stage.setScene(scene);
      TextDialogViewModel viewModel = loader.getController();
      viewModel.stage = stage;
      viewModel.contentTextArea.setText(text);
      stage.showAndWait();
    } catch (Exception e) {
      AlertViewModel.showError(e);
    }
  }

  @FXML TextArea contentTextArea;

  private Stage stage;

  @FXML void onCopyClicked() {
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    content.putString(contentTextArea.getText());
    clipboard.setContent(content);
  }

  @FXML void onCloseClicked() {
    stage.close();
  }
}
