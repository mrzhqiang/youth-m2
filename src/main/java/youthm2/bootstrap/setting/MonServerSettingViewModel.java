package youthm2.bootstrap.setting;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import youthm2.bootstrap.config.MonServerConfig;

/**
 * @author mrzhqiang
 */
public class MonServerSettingViewModel extends ServerSettingViewModel {
  public final ObjectProperty<SpinnerValueFactory<Integer>> monPort =
      new SimpleObjectProperty<>(PORT_VALUE_FACTORY);

  public final ObjectProperty<MonServerConfig> config = new SimpleObjectProperty<>();

  public MonServerSettingViewModel() {
    config.addListener((observable, oldValue, newValue) -> {
      x.get().setValue(newValue.x);
      y.get().setValue(newValue.y);
      enabled.setValue(newValue.enabled);
      path.setValue(newValue.path);
      port.get().setValue(newValue.port);
      serverPort.get().setValue(newValue.serverPort);
    });
  }

  public void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path,
      Spinner<Integer> port, Spinner<Integer> serverPort, Spinner<Integer> publicPort) {
    super.bind(x, y, enabled, path, port, serverPort);
    publicPort.valueFactoryProperty().bindBidirectional(this.monPort);
  }
}
