package youthm2.bootstrap;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.controlsfx.dialog.ExceptionDialog;
import youthm2.common.monitor.Monitor;

/**
 * 引导程序。
 */
public final class Bootstrap extends Application {
  private static final String TITLE = "引导程序 - 青春引擎";

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    Monitor monitor = Monitor.getInstance();
    try {
      // Read file fxml and draw interface.
      Parent root = FXMLLoader.load(getClass().getResource("/bootstrap.fxml"));
      primaryStage.setTitle(TITLE);
      primaryStage.setScene(new Scene(root));
      primaryStage.show();
    } catch (Exception e) {
      monitor.report("bootstrap start failed: " + e.getMessage());
      new ExceptionDialog(e)
          .showAndWait()
          .filter(buttonType -> buttonType == ButtonType.OK)
          .ifPresent(buttonType -> System.exit(-1));
      return;
    }
    monitor.report("bootstrap start successful");
  }
}
