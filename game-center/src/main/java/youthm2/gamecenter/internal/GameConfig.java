package youthm2.gamecenter.internal;

import javax.annotation.Nonnull;
import org.ini4j.Profile;
import youthm2.gamecenter.IConfig;

/**
 * 游戏配置。
 * <p>
 * 对应 Config.ini 中的 [GameConfig] 部分。
 *
 * @author mrzhqiang
 */
public final class GameConfig implements IConfig {
  private static final String OPTION_DIRECTORY = "GameDirectory";
  private static final String OPTION_DATABASE = "HeroDBName";
  private static final String OPTION_NAME = "GameName";
  private static final String OPTION_HOST = "ExtIPaddr";
  private static final String OPTION_AUTO_BACKUP = "AutoRunBak";
  private static final String OPTION_FIVE_ELEMENTS = "CloseWuXin";

  private static final String DEFAULT_NAME = "GameOfMir";
  private static final String DEFAULT_DIRECTORY = "D:\\MirServer";
  private static final String DEFAULT_DATABASE = "HeroDB";
  private static final String DEFAULT_HOST = "127.0.0.1";
  private static final boolean DEFAULT_AUTO_BACKUP = false;
  private static final boolean DEFAULT_OPEN_FIVE_ELEMENTS = false;

  public static GameConfig newInstant() {
    return new GameConfig();
  }

  public String directory = DEFAULT_DIRECTORY;
  public String database = DEFAULT_DATABASE;
  public String name = DEFAULT_NAME;
  public String host = DEFAULT_HOST;
  public boolean isAutoBackup = DEFAULT_AUTO_BACKUP;
  public boolean isOpenFiveElements = DEFAULT_OPEN_FIVE_ELEMENTS;

  @Nonnull @Override public String sectionName() {
    return "GameConfig";
  }

  @Override public void readFrom(@Nonnull Profile.Section section) {
    directory = section.get(OPTION_DIRECTORY, DEFAULT_DIRECTORY);
    database = section.get(OPTION_DATABASE, DEFAULT_DATABASE);
    name = section.get(OPTION_NAME, DEFAULT_NAME);
    host = section.get(OPTION_HOST, DEFAULT_HOST);
    isAutoBackup = Util.getBoolean(section, OPTION_AUTO_BACKUP, DEFAULT_AUTO_BACKUP);
    isOpenFiveElements = Util.getBoolean(section, OPTION_FIVE_ELEMENTS, DEFAULT_OPEN_FIVE_ELEMENTS);
  }

  @Override public void writeTo(@Nonnull Profile.Section section) {
    section.put(OPTION_DIRECTORY, directory);
    section.put(OPTION_DATABASE, database);
    section.put(OPTION_NAME, name);
    section.put(OPTION_HOST, host);
    Util.putBoolean(section, OPTION_AUTO_BACKUP, isAutoBackup);
    Util.putBoolean(section, OPTION_FIVE_ELEMENTS, isOpenFiveElements);
  }
}
