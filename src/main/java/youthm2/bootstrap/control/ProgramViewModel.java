package youthm2.bootstrap.control;

import io.reactivex.disposables.Disposable;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * 程序视图模型。
 *
 * @author mrzhqiang
 */
public final class ProgramViewModel {
  private final ObjectProperty<Status> status = new SimpleObjectProperty<>(Status.DEFAULT);

  public Disposable bind(CheckBox checkBox, Label label) {
    return JavaFxObservable.valuesOf(status)
        .subscribe(status -> {
          checkBox.setDisable(
              status != Status.DISABLED && status != Status.DEFAULT);
          checkBox.setSelected(status != Status.DISABLED);
          label.setText(status.text);
          label.setTextFill(status.color);
        });
  }

  public void setEnabled(Boolean enabled) {
    status.set(enabled ? Status.DEFAULT : Status.DISABLED);
  }

  public void starting() {
    status.set(Status.STARTING);
  }

  public void started() {
    status.set(Status.STARTED);
  }

  public void stopping() {
    status.set(Status.STOPPING);
  }

  public void stopped() {
    status.set(Status.DEFAULT);
  }

  public Status getStatus() {
    return status.get();
  }

  public ObjectProperty<Status> statusProperty() {
    return status;
  }

  public enum Status {
    DEFAULT("未启动", Color.valueOf("#455A64")),
    STARTING("正在启动", Color.valueOf("#9E9E9E")),
    STARTED("已启动", Color.valueOf("#388E3C")),
    STOPPING("正在停止", Color.valueOf("#F57C00")),
    DISABLED("已禁用", Color.valueOf("#AA0000"));

    public final String text;
    public final Color color;

    Status(String text, Color color) {
      this.text = text;
      this.color = color;
    }
  }
}
