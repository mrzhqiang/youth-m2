package youthm2.bootstrap.setting;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javax.annotation.Nullable;
import youthm2.bootstrap.config.ServerConfig;

/**
 * @author mrzhqiang
 */
public class ServerSettingViewModel extends ProgramSettingViewModel {

  public final ObjectProperty<SpinnerValueFactory<Integer>> port =
      new SimpleObjectProperty<>(PORT_VALUE_FACTORY);
  public final ObjectProperty<SpinnerValueFactory<Integer>> serverPort =
      new SimpleObjectProperty<>(PORT_VALUE_FACTORY);

  public final ObjectProperty<ServerConfig> config = new SimpleObjectProperty<>();

  public ServerSettingViewModel() {
    config.addListener((observable, oldValue, newValue) -> {
      x.get().setValue(newValue.x);
      y.get().setValue(newValue.y);
      enabled.setValue(newValue.enabled);
      path.setValue(newValue.path);
      port.get().setValue(newValue.port);
      if (newValue.serverPort != null) {
        serverPort.get().setValue(newValue.serverPort);
      }
    });
  }

  public void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path,
      Spinner<Integer> port, @Nullable Spinner<Integer> serverPort) {
    super.bind(x, y, enabled, path);
    port.valueFactoryProperty().bindBidirectional(this.port);
    if (serverPort != null) {
      serverPort.valueFactoryProperty().bindBidirectional(this.serverPort);
    }
  }
}
