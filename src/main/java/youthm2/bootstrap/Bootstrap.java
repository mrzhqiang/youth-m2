package youthm2.bootstrap;

import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import youthm2.bootstrap.viewmodel.BootstrapViewModel;
import youthm2.common.Monitor;
import youthm2.common.model.LoggerModel;
import youthm2.common.viewmodel.ThrowableDialogViewModel;

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
  private static final URL FXML =
      Bootstrap.class.getResource("/youthm2/bootstrap/application.fxml");
  private static final URL CSS =
      Bootstrap.class.getResource("/youthm2/bootstrap/application.css");

  public static void main(String[] args) {
    launch(args);
  }

  private BootstrapViewModel viewModel;

  @Override
  public void start(Stage primaryStage) {
    try {
      Monitor monitor = Monitor.getInstance();
      primaryStage.setTitle(TITLE);
      FXMLLoader loader = new FXMLLoader(FXML);
      Parent root = loader.load();
      viewModel = loader.getController();
      Scene scene = new Scene(root);
      scene.getStylesheets().add(CSS.toExternalForm());
      primaryStage.setScene(scene);
      primaryStage.show();
      monitor.report("bootstrap started");
    } catch (Exception e) {
      LoggerModel.BOOTSTRAP.error("引导程序启动失败！", e);
      ThrowableDialogViewModel.show(e);
    }
  }

  @Override public void stop() throws Exception {
    viewModel.onDestroy();
  }
}
