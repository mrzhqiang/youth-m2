package youthm2.common.model;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import youthm2.common.exception.FileException;

/**
 * 文件模型。
 * <p>
 * 提示：这个类可以用 java.nio.file.Files 替代。
 * <p>
 * 提出文件模型的主要原因在于，我们希望在可控范围内，提供通用的方法实现，这样即使出现异常，查找起来也十分有效。
 *
 * @author qiang.zhang
 */
public final class FileModel {
  private FileModel() {
    throw new AssertionError("No instance");
  }

  public static void createOrExists(File file) {
    Preconditions.checkNotNull(file, "file == null");
    if (file.exists()) {
      return;
    }
    try {
      if (file.isDirectory() && file.mkdirs()) {
        LoggerModel.COMMON.info("创建新目录：{}", file.getCanonicalPath());
        return;
      }
      // file.isFile 必须要有文件格式后缀，否则返回 false 导致无法创建文件
      if (file.createNewFile()) {
        LoggerModel.COMMON.info("创建新文件：{}", file.getCanonicalPath());
      }
    } catch (SecurityException e) {
      String message = String.format("无法读写 [%s]", file.getPath());
      throw new FileException(message, e);
    } catch (IOException e) {
      String message = String.format("创建失败 [%s]", file.getPath());
      throw new FileException(message, e);
    }
  }

  public static void deleteOrNotExists(File file) {
    Preconditions.checkNotNull(file, "file == null");
    if (!file.exists()) {
      return;
    }
    try {
      if (file.delete()) {
        LoggerModel.COMMON.info("已删除：{}", file.getCanonicalPath());
      }
    } catch (IOException e) {
      String message = String.format("删除 [%s] 失败", file.getPath());
      throw new FileException(message, e);
    }
  }

  public static void onceWrite(File file, String content) {
    createOrExists(file);
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(content);
      writer.flush();
    } catch (IOException e) {
      String message = String.format("无法写入内容到 [%s]", file.getPath());
      throw new FileException(message, e);
    }
  }

  public static void appleWrite(File file, String content) {
    createOrExists(file);
    try (FileWriter writer = new FileWriter(file, true)) {
      writer.write(content);
      writer.flush();
    } catch (IOException e) {
      String message = String.format("无法追加内容到 [%s]", file.getPath());
      throw new FileException(message, e);
    }
  }
}
