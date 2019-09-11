package youthm2.bootstrap.control;

import youthm2.bootstrap.config.BootstrapConfig;
import youthm2.bootstrap.control.ConsoleViewModel;
import youthm2.bootstrap.control.ProgramViewModel;
import youthm2.bootstrap.control.StartGameViewModel;
import youthm2.bootstrap.control.StartModeViewModel;

/**
 * 控制面板视图模型。
 *
 * @author qiang.zhang
 */
public final class ControlViewModel {
  public final ProgramViewModel database = new ProgramViewModel();
  public final ProgramViewModel account = new ProgramViewModel();
  public final ProgramViewModel logger = new ProgramViewModel();
  public final ProgramViewModel core = new ProgramViewModel();
  public final ProgramViewModel game = new ProgramViewModel();
  public final ProgramViewModel role = new ProgramViewModel();
  public final ProgramViewModel login = new ProgramViewModel();
  public final ProgramViewModel rank = new ProgramViewModel();
  public final StartModeViewModel startMode = new StartModeViewModel();
  public final ConsoleViewModel console = new ConsoleViewModel();
  public final StartGameViewModel startGame = new StartGameViewModel();

  public void update(BootstrapConfig config) {
    if (config != null) {
      database.setEnabled(config.database.enabled);
      account.setEnabled(config.account.enabled);
      logger.setEnabled(config.logger.enabled);
      core.setEnabled(config.core.enabled);
      game.setEnabled(config.game.enabled);
      role.setEnabled(config.role.enabled);
      login.setEnabled(config.login.enabled);
      rank.setEnabled(config.rank.enabled);
    }
  }
}
