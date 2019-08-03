package youthm2.bootstrap;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import youthm2.common.Monitor;
import youthm2.common.model.LoggerModel;

/**
 * 引导程序。
 * <p>
 * 作为 Application 的扩展类，它允许使用 new 关键字创建并启动，与创建者同属 JavaFx 进程。
 * <pre>{@code
 * new Bootstrap().start(new Stage());
 * }</pre>
 * <p>
 * 除此之外，似乎可以使用反射进行启动：
 * <pre>{@code
 * Class<Bootstrap> clazz = Class.forName(Bootstrap.class.getName());
 * clazz.newInstance().start(new Stage());
 * }</pre>
 * <p>
 * 不过，反射稍微麻烦一点（需要对应的上下文），但不需要依赖具体实现。
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
    } catch (Exception e) {
      String msg = "引导程序启动失败！";
      LoggerModel.BOOTSTRAP.error(msg, e);
      //ThrowableDialog.show(msg, e);
    }
    monitor.report("bootstrap started");
  }
}
