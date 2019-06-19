package youthm2.bootstrap;

enum BootstrapState {
    DEFAULT("启动服务"),
    STARTING("取消启动"),
    CANCEL("停止启动"),
    RUNNING("停止服务"),
    STOPPING("取消停止"),
    ;

    private final String label;

    BootstrapState(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
