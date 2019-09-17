package youthm2.bootstrap.setting;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import youthm2.bootstrap.config.ProgramConfig;

/**
 * @author mrzhqiang
 */
public class ProgramSettingViewModel {
  static final SpinnerValueFactory.IntegerSpinnerValueFactory PORT_VALUE_FACTORY =
      new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 65535, 0);
  private static final SpinnerValueFactory.IntegerSpinnerValueFactory LOCATION_VALUE_FACTORY =
      new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999, 0);

  public final ObjectProperty<SpinnerValueFactory<Integer>> x =
      new SimpleObjectProperty<>(LOCATION_VALUE_FACTORY);
  public final ObjectProperty<SpinnerValueFactory<Integer>> y =
      new SimpleObjectProperty<>(LOCATION_VALUE_FACTORY);
  public final BooleanProperty enabled = new SimpleBooleanProperty();
  public final StringProperty path = new SimpleStringProperty();

  public final ObjectProperty<ProgramConfig> config = new SimpleObjectProperty<>();

  public ProgramSettingViewModel() {
    config.addListener((observable, oldValue, newValue) -> {
      x.get().setValue(newValue.x);
      y.get().setValue(newValue.y);
      enabled.setValue(newValue.enabled);
      path.setValue(newValue.path);
    });
  }

  public void bind(Spinner<Integer> x, Spinner<Integer> y, CheckBox enabled, TextField path) {
    x.valueFactoryProperty().bindBidirectional(this.x);
    y.valueFactoryProperty().bindBidirectional(this.y);
    enabled.selectedProperty().bindBidirectional(this.enabled);
    path.textProperty().bindBidirectional(this.path);
  }
}
