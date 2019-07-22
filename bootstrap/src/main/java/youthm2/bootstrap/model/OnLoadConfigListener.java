package youthm2.bootstrap.model;

import com.typesafe.config.Config;

/**
 * 加载配置监听器。
 *
 * @author qiang.zhang
 */
public interface OnLoadConfigListener {
  void onLoaded(Config config);
}
