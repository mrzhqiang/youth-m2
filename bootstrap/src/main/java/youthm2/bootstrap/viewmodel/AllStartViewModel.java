package youthm2.bootstrap.viewmodel;

import com.google.common.base.Preconditions;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;

/**
 * AllStartViewModel
 *
 * @author qiang.zhang
 */
final class AllStartViewModel {
  private static final String LABEL_STOPPED = "一键启动";
  private static final String LABEL_STARTING = "正在启动";
  private static final String LABEL_RUNNING = "正在运行";
  private static final String LABEL_STOPPING = "正在停止";

  private final StringProperty text = new SimpleStringProperty();
  private final BooleanProperty disable = new SimpleBooleanProperty();

  void bind(Button button) {
    Preconditions.checkNotNull(button, "button == null");
    button.textProperty().bindBidirectional(text);
    button.disableProperty().bindBidirectional(disable);
  }

  void unBind(Button button) {
    Preconditions.checkNotNull(button, "button == null");
    button.textProperty().unbindBidirectional(text);
    button.disableProperty().unbindBidirectional(disable);
  }

  void stopped() {
    text.setValue(LABEL_STOPPED);
    disable.setValue(false);
  }

  void starting() {
    text.setValue(LABEL_STARTING);
    disable.setValue(true);
  }

  void running() {
    text.setValue(LABEL_RUNNING);
    disable.setValue(false);
  }

  void stopping() {
    text.setValue(LABEL_STOPPING);
    disable.setValue(true);
  }
}
