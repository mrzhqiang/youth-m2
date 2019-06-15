package youthm2.bootstrap;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;

/**
 * Program
 *
 * @author qiang.zhang
 */
public final class Program {
    public boolean enabled;
    public int status;
    public String filename;
    public String path;
    public Process process;
    public String handler;
    public String bootstrapHanlder;
    public int x;
    public int y;

    public long run(long waitTime) {
        String command = String.format(Locale.getDefault(), "%s%s %s %d %d", path, filename, handler, x, y);
        try {
            Process process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            LoggerFactory.getLogger("bootstrap").error("启动程序失败", e);
            return -1;
        }
        return 0;
    }
}
