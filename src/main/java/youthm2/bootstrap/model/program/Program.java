package youthm2.bootstrap.model.program;

import youthm2.bootstrap.model.config.ProgramConfig;

/**
 * Program
 *
 * @author qiang.zhang
 */
public enum Program {
  DATABASE,
  ACCOUNT,
  LOGGER,
  CORE,
  GAME,
  ROLE,
  LOGIN,
  RANK,
  ;

  public boolean enabled;
  public Status status;
  public int x;
  public int y;

  public void from(ProgramConfig config) {
    enabled = config.enabled;
    status = Program.Status.DEFAULT;
    x = config.x;
    y = config.y;
  }

  public enum Status {
    DEFAULT,
    STARTING,
    RUNNING,
    ERROR,
  }
}
