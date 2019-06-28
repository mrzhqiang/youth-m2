package youthm2.common;

import javax.swing.UIManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LookUtil
 *
 * @author qiang.zhang
 */
public final class LookUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger("common");

  private LookUtil() {
    throw new AssertionError("This is factory class");
  }

  public static void setSystemDefaultLookAndFeel() {
    try {
      // 获取系统默认的样式/主题
      String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
      // 改变程序外观
      UIManager.setLookAndFeel(lookAndFeel);
    } catch (Exception e) {
      LOGGER.warn("将外观设置为系统主题出错", e);
    }
  }
}
