package youthm2.database;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import youthm2.common.Monitor;
import youthm2.common.dialog.ThrowableDialog;
import youthm2.common.model.LoggerModel;

/**
 * 数据库服务。
 *
 * @author qiang.zhang
 */
public final class Database extends Application {
  private static final String TITLE = "数据库服务 - 青春引擎";

  @Override
  public void start(Stage primaryStage) {
    Monitor monitor = Monitor.getInstance();
    try {
      Parent root = FXMLLoader.load(getClass().getResource("/view-database.fxml"));
      primaryStage.setTitle(TITLE);
      primaryStage.setScene(new Scene(root));
      primaryStage.show();
    } catch (Exception e) {
      LoggerModel.DATABASE.error("数据库服务启动失败！", e);
      ThrowableDialog.show(e);
    }
    monitor.report("database started");
  }
}
