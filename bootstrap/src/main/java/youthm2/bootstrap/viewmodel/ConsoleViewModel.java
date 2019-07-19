package youthm2.bootstrap.viewmodel;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import helper.DateTimeHelper;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextArea;

/**
 * 控制台模型。
 *
 * @author qiang.zhang
 */
final class ConsoleViewModel {
  private static final String FORMAT_CONSOLE = "[%s]: %s\r\n";

  private final StringProperty newMessage = new SimpleStringProperty();
  private final StringProperty text = new SimpleStringProperty();
  private ChangeListener<String> changeListener;

  void bind(TextArea textArea) {
    Preconditions.checkNotNull(textArea, "text area == null");
    if (changeListener == null) {
      changeListener = (observable, oldValue, newValue) -> textArea.appendText(newValue);
      newMessage.addListener(changeListener);
    }
    textArea.textProperty().bindBidirectional(text);
  }

  void unBind(TextArea textArea) {
    if (changeListener != null) {
      newMessage.removeListener(changeListener);
      changeListener = null;
    }
    textArea.textProperty().unbindBidirectional(text);
  }

  void clean() {
    text.setValue("");
  }

  void append(String message) {
    if (Strings.isNullOrEmpty(message)) {
      return;
    }
    String timestamp = DateTimeHelper.format(Date.from(Instant.now()));
    String console = String.format(Locale.getDefault(), FORMAT_CONSOLE, timestamp, message);
    newMessage.setValue(console);
  }
}
