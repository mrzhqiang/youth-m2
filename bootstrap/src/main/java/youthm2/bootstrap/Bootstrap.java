package youthm2.bootstrap;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import youthm2.common.ErrorDialog;
import youthm2.common.Monitor;
import youthm2.common.model.LoggerModel;

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
      Parent root = FXMLLoader.load(getClass().getResource("/bootstrap-layout.fxml"));
      primaryStage.setTitle(TITLE);
      Scene scene = new Scene(root);
      scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());
      primaryStage.setScene(scene);
      primaryStage.show();
      throw new RuntimeException("测试一下 ErrorDialogViewModel");
    } catch (Exception e) {
      LoggerModel.BOOTSTRAP.error("引导程序启动失败！", e);
      ErrorDialog.show(e);
    }
    monitor.report("bootstrap started");
  }
}
