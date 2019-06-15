package youthm2.bootstrap.config;

import com.typesafe.config.Config;

/**
 * 内部服务配置。
 *
 * @author qiang.zhang
 */
public class IntervalServerConfig extends ServerConfig {
    public int serverPort;

    @Override
    public void load(Config config) {
        super.load(config);
        if (config.hasPath("serverPort")) {
            serverPort = config.getInt("serverPort");
        }
    }
}
