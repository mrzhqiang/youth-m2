package youthm2.bootstrap;

import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Locale;

/**
 * Program
 *
 * @author qiang.zhang
 */
final class Program {
    boolean enabled;
    int status;
    String filename;
    String path;
    Process process;
    String handler;
    String bootstrapHanlder;
    int x;
    int y;

    void run() {
        String command = String.format(Locale.getDefault(), "%s%s %s %d %d", path, filename, handler, x, y);
        try {
            process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            LoggerFactory.getLogger("bootstrap").error("启动程序失败：" + filename, e);
        }
    }

    static final Program DATABASE = new Program();
    static final Program ACCOUNT = new Program();
    static final Program LOGGER = new Program();
    static final Program CORE = new Program();
    static final Program GAME = new Program();
    static final Program ROLE = new Program();
    static final Program LOGIN = new Program();
    static final Program RANK = new Program();
}
