package youthm2.bootstrap.viewmodel.control;

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
 * 程序视图模型。
 *
 * @author mrzhqiang
 */
public final class ProgramViewModel {
  private static final String TEXT_DISABLED = "已禁用";
  private static final Color COLOR_DISABLED = Color.valueOf("#CFD8DC");
  private static final String TEXT_STOPPED = "未启动";
  private static final Color COLOR_STOPPED = Color.valueOf("#455A64");
  private static final String TEXT_STARTING = "正在启动";
  private static final Color COLOR_STARTING = Color.valueOf("#9E9E9E");
  private static final String TEXT_STARTED = "已启动";
  private static final Color COLOR_STARTED = Color.valueOf("#388E3C");
  private static final String TEXT_STOPPING = "正在停止";
  private static final Color COLOR_STOPPING = Color.valueOf("#F57C00");

  private final BooleanProperty disable = new SimpleBooleanProperty(false);
  private final StringProperty text = new SimpleStringProperty(TEXT_STOPPED);
  private final Property<Paint> color = new SimpleObjectProperty<>(COLOR_STOPPED);

  public void bind(Button button, Label label) {
    // bindBidirectional 是指双向绑定，意味着数据改动会影响组件状态，同时组件状态改变会更新到数据。
    // 需要注意的是：绑定从一开始就将目标对象的值设置给调用者；解绑则只是移除了监听器。
    button.disableProperty().bindBidirectional(disable);
    label.textProperty().bindBidirectional(text);
    label.textFillProperty().bindBidirectional(color);
  }

  public void setEnabled(boolean enabled) {
    if (!enabled) {
      this.disable.setValue(true);
      text.setValue(TEXT_DISABLED);
      color.setValue(COLOR_DISABLED);
    }
  }

  public void starting() {
    disable.setValue(true);
    text.setValue(TEXT_STARTING);
    color.setValue(COLOR_STARTING);
  }

  public void started() {
    disable.setValue(false);
    text.setValue(TEXT_STARTED);
    color.setValue(COLOR_STARTED);
  }

  public void stopping() {
    disable.setValue(true);
    text.setValue(TEXT_STOPPING);
    color.setValue(COLOR_STOPPING);
  }

  public void stopped() {
    disable.setValue(false);
    text.setValue(TEXT_STOPPED);
    color.setValue(COLOR_STOPPED);
  }
}
