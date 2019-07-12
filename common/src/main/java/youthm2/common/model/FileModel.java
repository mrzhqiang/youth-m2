package youthm2.common.model;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import youthm2.common.exception.FileException;

/**
 * FileModel
 *
 * @author qiang.zhang
 */
public final class FileModel {
  private FileModel() {
    throw new AssertionError("No instance");
  }

  public static void existsOrCreate(File file) {
    Preconditions.checkNotNull(file, "file == null");
    try {
      if (file.exists()) {
        return;
      }
      if (file.isDirectory() && file.mkdirs()) {
        LoggerModel.COMMON.info("创建了新目录：{}", file.getPath());
      } else if (file.createNewFile()) {
        LoggerModel.COMMON.info("创建了新文件：{}", file.getPath());
      }
    } catch (IOException e) {
      String msg = "无法创建目录或文件：" + e.getMessage();
      LoggerModel.COMMON.error(msg);
      throw new FileException(msg, e);
    } catch (SecurityException e) {
      String msg = "无法访问目录或文件：" + e.getMessage();
      LoggerModel.COMMON.error(msg);
      throw new FileException(msg, e);
    }
  }

  public static void notExistsOrDelete(File file) {
    Preconditions.checkNotNull(file, "file == null");
    try {
      if (!file.exists()) {
        return;
      }
      if (file.delete()) {
        LoggerModel.COMMON.info("已删除目录或文件：{}", file.getPath());
      }
    } catch (SecurityException e) {
      LoggerModel.COMMON.error("无法访问目录或文件：{}", e.getMessage());
    }
  }

  public static void onceWrite(File file, String content) {
    existsOrCreate(file);
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(content);
      writer.flush();
    } catch (Exception e) {
      String msg = "写入数据到文件出错：" + e.getMessage();
      LoggerModel.COMMON.error(msg);
      throw new FileException(msg, e);
    }
  }

  public static void appleWrite(File file, String content) {
    existsOrCreate(file);
    try (FileWriter writer = new FileWriter(file, true)) {
      writer.write(content);
      writer.flush();
    } catch (Exception e) {
      String msg = "追加数据到文件出错：" + e.getMessage();
      LoggerModel.COMMON.error(msg);
      throw new FileException(msg, e);
    }
  }
}
