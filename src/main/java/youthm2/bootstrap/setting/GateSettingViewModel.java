package youthm2.bootstrap.setting;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import youthm2.bootstrap.config.GateConfig;

/**
 * @author mrzhqiang
 */
public class GateSettingViewModel extends ProgramSettingViewModel {
  public final ObjectProperty<SpinnerValueFactory<Integer>> port =
      new SimpleObjectProperty<>(PORT_VALUE_FACTORY);

  public final ObjectProperty<GateConfig> config = new SimpleObjectProperty<>();

  public GateSettingViewModel() {
    config.addListener((observable, oldValue, newValue) -> {
      x.get().setValue(newValue.x);
      y.get().setValue(newValue.y);
      enabled.setValue(newValue.enabled);
      path.setValue(newValue.path);
      port.get().setValue(newValue.port);
    });
  }

  public void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path,
      Spinner<Integer> port) {
    super.bind(x, y, enabled, path);
    port.valueFactoryProperty().bindBidirectional(this.port);
  }
}
