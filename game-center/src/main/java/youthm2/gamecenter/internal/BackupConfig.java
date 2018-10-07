package youthm2.gamecenter.internal;

import com.google.common.base.Strings;
import javax.annotation.Nonnull;
import org.ini4j.Profile;
import youthm2.gamecenter.IConfig;

/**
 * 备份文件配置。
 *
 * @author mrzhqiang
 */
public final class BackupConfig implements IConfig {
  private static final String OPTION_SOURCE = "Source";
  private static final String OPTION_DEST = "Save";
  private static final String OPTION_HOUR = "Hour";
  private static final String OPTION_MINUTE = "Min";
  private static final String OPTION_MODE = "BackMode";
  private static final String OPTION_ENABLED = "GetBack";
  private static final String OPTION_ZIP_ENABLED = "Zip";

  private static int LAST_INDEX = 0;

  public static BackupConfig newInstant(int index) {
    BackupConfig config = new BackupConfig();
    config.index = index;
    if (index > LAST_INDEX) {
      LAST_INDEX = index;
    }
    return config;
  }

  public int index = LAST_INDEX + 1;
  public String source;
  public String dest;
  public byte hour;
  public byte minute;
  public int mode;
  public boolean enabled;
  public boolean zipEnabled;

  public boolean isInvalid() {
    return Strings.isNullOrEmpty(source) || Strings.isNullOrEmpty(dest);
  }

  @Nonnull @Override public String sectionName() {
    return String.valueOf(index);
  }

  @Override public void readFrom(@Nonnull Profile.Section section) {
    source = section.get(OPTION_SOURCE, "");
    dest = section.get(OPTION_DEST, "");
    hour = section.get(OPTION_HOUR, Byte.class, (byte) 0);
    minute = section.get(OPTION_MINUTE, Byte.class, (byte) 0);
    mode = section.get(OPTION_MODE, Integer.class, 0);
    enabled = Util.getBoolean(section, OPTION_ENABLED, true);
    zipEnabled = Util.getBoolean(section, OPTION_ZIP_ENABLED, true);
  }

  @Override public void writeTo(@Nonnull Profile.Section section) {
    section.put(OPTION_SOURCE, source);
    section.put(OPTION_DEST, dest);
    section.put(OPTION_HOUR, hour);
    section.put(OPTION_MINUTE, minute);
    section.put(OPTION_MODE, mode);
    Util.putBoolean(section, OPTION_ENABLED, enabled);
    Util.putBoolean(section, OPTION_ZIP_ENABLED, zipEnabled);
  }
}
