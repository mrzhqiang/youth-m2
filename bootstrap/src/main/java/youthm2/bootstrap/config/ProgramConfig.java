package youthm2.bootstrap.config;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;

/**
 * 程序配置。
 *
 * @author qiang.zhang
 */
public class ProgramConfig {
    public int x;
    public int y;
    public boolean enabled;
    public String filename;

    public void load(Config config) {
        Preconditions.checkNotNull(config, "config == null");
        if (config.hasPath("x")) {
            x = config.getInt("x");
        }
        if (config.hasPath("y")) {
            y = config.getInt("y");
        }
        if (config.hasPath("enabled")) {
            enabled = config.getBoolean("enabled");
        }
        if (config.hasPath("filename")) {
            filename = config.getString("filename");
        }
    }
}
