package youthm2.common.viewmodel;

import java.io.File;
import java.util.Optional;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javax.annotation.Nullable;

/**
 * 选择弹窗视图模型。
 *
 * @author qiang.zhang
 */
public final class ChooserViewModel {
  private ChooserViewModel() {
    throw new AssertionError("No instance");
  }

  public static Optional<File> directory(String title) {
    return directory(title, null);
  }

  public static Optional<File> directory(String title, @Nullable File workDir) {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle(title);
    if (workDir != null && workDir.exists()) {
      chooser.setInitialDirectory(workDir);
    }
    return Optional.ofNullable(chooser.showDialog(null))
        .filter(file -> file.exists() && file.isDirectory());
  }

  public static Optional<File> file(String title) {
    return file(title, null);
  }

  public static Optional<File> file(String title, @Nullable File workDir) {
    return file(title, workDir, null);
  }

  public static Optional<File> file(String title, @Nullable File workDir,
      @Nullable FileChooser.ExtensionFilter filter) {
    FileChooser chooser = new FileChooser();
    chooser.setTitle(title);
    if (workDir != null && workDir.exists()) {
      chooser.setInitialDirectory(workDir);
    }
    if (filter != null) {
      chooser.getExtensionFilters().add(filter);
    }
    return Optional.ofNullable(chooser.showOpenDialog(null))
        .filter(file -> file.exists() && file.isFile());
  }

  public static FileChooser.ExtensionFilter exeFilter() {
    return new FileChooser.ExtensionFilter("程序文件", "*.exe");
  }

  public static FileChooser.ExtensionFilter pngFilter() {
    return new FileChooser.ExtensionFilter("图像文件", "*.png");
  }

  public static FileChooser.ExtensionFilter txtFilter() {
    return new FileChooser.ExtensionFilter("文本文件", "*.txt");
  }
}
