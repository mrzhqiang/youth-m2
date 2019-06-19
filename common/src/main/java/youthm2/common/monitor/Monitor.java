package youthm2.common.monitor;

public interface Monitor {
    static Monitor simple() {
        return new SimpleMonitor();
    }

    void record(String name);

    void report(String name);
}
