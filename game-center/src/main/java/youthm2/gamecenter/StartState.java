package youthm2.gamecenter;

/**
 * 启动状态。
 */
public enum StartState {
  DEFAULT("启动服务器"),
  STARTING("取消启动"),
  CALCEL_START("继续启动"),
  RUNNING("停止服务器"),
  STOPING("取消停止"),
  CALCEL_STOP("继续停止"),
  ;

  private final String label;

  StartState(String label) {
    this.label = label;
  }

  @Override public String toString() {
    return label;
  }
}
