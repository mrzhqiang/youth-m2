package youthm2.database;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.LoggerFactory;
import youthm2.common.Monitor;

/**
 * 数据库服务。
 *
 * @author qiang.zhang
 */
public final class Database extends Application {
  private static final String TITLE = "数据库服务 - 青春引擎";

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    Monitor monitor = Monitor.getInstance();
    try {
      Parent root = FXMLLoader.load(getClass().getResource("/view-database.fxml"));
      primaryStage.setTitle(TITLE);
      primaryStage.setScene(new Scene(root));
      primaryStage.show();
    } catch (Exception e) {
      LoggerFactory.getLogger("database").error("数据库服务启动失败！", e);
      new ExceptionDialog(e)
          .showAndWait()
          .filter(buttonType -> buttonType == ButtonType.OK)
          .ifPresent(buttonType -> System.exit(-1));
    }
    monitor.report("database started");
    System.out.println("ok");
  }
}
