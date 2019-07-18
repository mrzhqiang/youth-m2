package youthm2.bootstrap.viewmodel;

import java.time.LocalDateTime;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import youthm2.bootstrap.model.StartModeModel;

/**
 * StartModeViewModel
 *
 * @author qiang.zhang
 */
final class StartModeViewModel {
  private static final String MODE_NORMAL = "正常启动";
  private static final String MODE_DELAY = "延时启动";
  private static final String MODE_TIMING = "定时启动";

  private final StringProperty modeValue = new SimpleStringProperty();
  private final BooleanProperty hoursDisable = new SimpleBooleanProperty(true);
  private final ObjectProperty<SpinnerValueFactory<Integer>> hoursValue =
      new SimpleObjectProperty<>(new IntegerSpinnerValueFactory(0, 23, 0));
  private final BooleanProperty minutesDisable = new SimpleBooleanProperty(true);
  private final ObjectProperty<SpinnerValueFactory<Integer>> minutesValue =
      new SimpleObjectProperty<>(new IntegerSpinnerValueFactory(0, 59, 0));

  void bind(ChoiceBox<String> startModeChoiceBox, Spinner<Integer> hoursSpinner,
      Spinner<Integer> minutesSpinner) {
    startModeChoiceBox.getItems().addAll(MODE_NORMAL, MODE_DELAY, MODE_TIMING);
    startModeChoiceBox.valueProperty().bindBidirectional(modeValue);
    hoursSpinner.disableProperty().bindBidirectional(hoursDisable);
    hoursSpinner.valueFactoryProperty().bindBidirectional(hoursValue);
    minutesSpinner.disableProperty().bindBidirectional(minutesDisable);
    minutesSpinner.valueFactoryProperty().bindBidirectional(minutesValue);
  }

  void unBind(ChoiceBox<String> startModeChoiceBox, Spinner<Integer> hoursSpinner,
      Spinner<Integer> minutesSpinner) {
    startModeChoiceBox.getItems().removeAll(MODE_NORMAL, MODE_DELAY, MODE_TIMING);
    startModeChoiceBox.valueProperty().unbindBidirectional(modeValue);
    hoursSpinner.disableProperty().unbindBidirectional(hoursDisable);
    hoursSpinner.valueFactoryProperty().unbindBidirectional(hoursValue);
    minutesSpinner.disableProperty().unbindBidirectional(minutesDisable);
    minutesSpinner.valueFactoryProperty().unbindBidirectional(minutesValue);
  }

  void normalMode() {
    hoursDisable.setValue(true);
    minutesDisable.setValue(true);
  }

  void delayMode() {
    hoursDisable.setValue(false);
    minutesDisable.setValue(false);
  }

  void timingMode() {
    hoursDisable.setValue(false);
    minutesDisable.setValue(false);
  }

  LocalDateTime computeStartDateTime() {
    Integer hours = hoursValue.getValue().getValue();
    Integer minutes = minutesValue.getValue().getValue();
    if (modeValue.get().equals(MODE_DELAY)) {
      return StartModeModel.delay(hours, minutes);
    } else if (modeValue.get().equals(MODE_TIMING)) {
      return StartModeModel.timing(hours, minutes);
    }
    return StartModeModel.normal();
  }
}
