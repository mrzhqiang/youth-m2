package youthm2.bootstrap.config;

import com.typesafe.config.Config;

/**
 * 公开服务配置。
 *
 * @author qiang.zhang
 */
public class PublicServerConfig extends IntervalServerConfig {
    public int monPort;

    @Override
    public void load(Config config) {
        super.load(config);
        if (config.hasPath("monPort")) {
            monPort = config.getInt("monPort");
        }
    }
}
