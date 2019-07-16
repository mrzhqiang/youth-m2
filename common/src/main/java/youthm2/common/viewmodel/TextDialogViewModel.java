package youthm2.common.viewmodel;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

/**
 * TextDialogViewModel
 *
 * @author qiang.zhang
 */
public final class TextDialogViewModel {
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

  public TextDialogViewModel setText(String text) {
    contentTextArea.setText(text);
    return this;
  }

  public TextDialogViewModel setStage(Stage stage) {
    this.stage = stage;
    return this;
  }

  public void showAndWait() {
    stage.showAndWait();
  }
}
