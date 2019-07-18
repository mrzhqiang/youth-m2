package youthm2.bootstrap.viewmodel;

import com.google.common.base.Preconditions;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * ProgramGroupViewModel
 *
 * @author qiang.zhang
 */
final class ProgramGroupViewModel {
  final ProgramViewModel database = new ProgramViewModel();
  final ProgramViewModel account = new ProgramViewModel();
  final ProgramViewModel logger = new ProgramViewModel();
  final ProgramViewModel core = new ProgramViewModel();
  final ProgramViewModel game = new ProgramViewModel();
  final ProgramViewModel role = new ProgramViewModel();
  final ProgramViewModel login = new ProgramViewModel();
  final ProgramViewModel rank = new ProgramViewModel();

  void init() {
    database.stopped();
    account.stopped();
    logger.stopped();
    core.stopped();
    game.stopped();
    role.stopped();
    login.stopped();
    rank.stopped();
  }

  static final class ProgramViewModel {
    private static final String TEXT_STOPPED = "未启动";
    private static final Color COLOR_STOPPED = Color.valueOf("#388E3C");
    private static final String TEXT_STARTING = "正在启动";
    private static final Color COLOR_STARTING = Color.valueOf("#FFA000");
    private static final String TEXT_STARTED = "已启动";
    private static final Color COLOR_STARTED = Color.valueOf("#D32F2F");
    private static final String TEXT_STOPPING = "正在停止";
    private static final Color COLOR_STOPPING = Color.valueOf("#9E9E9E");

    private final BooleanProperty disable = new SimpleBooleanProperty(false);
    private final StringProperty text = new SimpleStringProperty(TEXT_STOPPED);
    private final Property<Paint> color = new SimpleObjectProperty<>(COLOR_STOPPED);

    void bind(Button button, Label label) {
      Preconditions.checkNotNull(button, "button == null");
      Preconditions.checkNotNull(label, "label == null");
      button.disableProperty().bindBidirectional(disable);
      label.textProperty().bindBidirectional(text);
      label.textFillProperty().bindBidirectional(color);
    }

    void unBind(Button button, Label label) {
      Preconditions.checkNotNull(button, "button == null");
      Preconditions.checkNotNull(label, "label == null");
      button.disableProperty().unbindBidirectional(disable);
      label.textProperty().unbindBidirectional(text);
      label.textFillProperty().unbindBidirectional(color);
    }

    void stopped() {
      disable.setValue(false);
      text.setValue(TEXT_STOPPED);
      color.setValue(COLOR_STOPPED);
    }

    void starting() {
      disable.setValue(true);
      text.setValue(TEXT_STARTING);
      color.setValue(COLOR_STARTING);
    }

    void started() {
      disable.setValue(false);
      text.setValue(TEXT_STARTED);
      color.setValue(COLOR_STARTED);
    }

    void stopping() {
      disable.setValue(true);
      text.setValue(TEXT_STOPPING);
      color.setValue(COLOR_STOPPING);
    }
  }
}
