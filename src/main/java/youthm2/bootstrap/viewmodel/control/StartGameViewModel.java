package youthm2.bootstrap.viewmodel.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;

/**
 * 启动游戏视图模型。
 *
 * @author mrzhqiang
 */
public final class StartGameViewModel {
  private static final String LABEL_STOPPED = "一键启动";
  private static final String LABEL_WAITING = "等待启动";
  private static final String LABEL_STARTING = "正在启动";
  private static final String LABEL_RUNNING = "正在运行";
  private static final String LABEL_STOPPING = "正在停止";

  private final StringProperty text = new SimpleStringProperty(LABEL_STOPPED);
  private final BooleanProperty disable = new SimpleBooleanProperty(false);

  public void bind(Button button) {
    button.textProperty().bindBidirectional(text);
    button.disableProperty().bindBidirectional(disable);
  }

  public void stopped() {
    text.setValue(LABEL_STOPPED);
    disable.setValue(false);
  }

  public void waiting() {
    text.setValue(LABEL_WAITING);
    disable.setValue(false);
  }

  public void starting() {
    text.setValue(LABEL_STARTING);
    disable.setValue(true);
  }

  public void running() {
    text.setValue(LABEL_RUNNING);
    disable.setValue(false);
  }

  public void stopping() {
    text.setValue(LABEL_STOPPING);
    disable.setValue(true);
  }
}
