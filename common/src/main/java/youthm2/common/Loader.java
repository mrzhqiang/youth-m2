package youthm2.common;

import com.typesafe.config.Config;

/**
 * 加载器。
 * <p>
 * 可以通过加载填充内部属性。
 *
 * @author qiang.zhang
 */
public interface Loader {

  /**
   * 加载配置，填充或覆盖内部属性。
   *
   * @param config 类型安全的配置接口。
   */
  void onLoad(Config config);
}
