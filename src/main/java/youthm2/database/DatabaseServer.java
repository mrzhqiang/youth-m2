package youthm2.database;

import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import youthm2.common.Monitor;
import youthm2.common.dialog.AlertDialog;

/**
 * 数据库服务。
 *
 * @author mrzhqiang
 */
public final class DatabaseServer extends Application {
  private static final String TITLE = "数据库服务";
  private static final URL FXML =
      DatabaseServer.class.getResource("/youthm2/database/database.fxml");
  private static final URL CSS =
      DatabaseServer.class.getResource("/youthm2/database/database.css");

  private DatabaseViewModel databaseVM;

  @Override public void start(Stage primaryStage) {
    Monitor monitor = Monitor.getInstance();
    try {
      primaryStage.setTitle(TITLE);
      FXMLLoader loader = new FXMLLoader(FXML);
      databaseVM = loader.getController();
      Scene scene = new Scene(loader.load());
      scene.getStylesheets().add(CSS.toExternalForm());
      primaryStage.setScene(scene);
      primaryStage.show();
      monitor.report("database started");
    } catch (IOException e) {
      AlertDialog.showError(e);
    }
  }

  @Override public void stop() {
    if (databaseVM != null) {
      databaseVM.onDestroy();
    }
  }
}
