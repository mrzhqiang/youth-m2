package youthm2.common.model;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常模型。
 *
 * @author qiang.zhang
 */
public final class ThrowableModel {
  public static String print(Throwable e) {
    // --- expandable content
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    return sw.toString();
  }

  public static String printStackTrace() {
    StringBuilder builder = new StringBuilder();
    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
      if (builder.length() > 0) {
        builder.append("\r\n");
      }
      builder.append(element.toString());
    }
    return builder.toString();
  }
}
