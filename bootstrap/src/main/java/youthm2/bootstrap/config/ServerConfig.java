package youthm2.bootstrap.config;

import com.typesafe.config.Config;

/**
 * 服务配置。
 *
 * @author qiang.zhang
 */
public class ServerConfig extends ProgramConfig {
    public int port;

    @Override
    public void load(Config config) {
        super.load(config);
        if (config.hasPath("port")) {
            port = config.getInt("port");
        }
    }
}
