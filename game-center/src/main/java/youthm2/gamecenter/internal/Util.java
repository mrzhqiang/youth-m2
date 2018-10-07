package youthm2.gamecenter.internal;

import javax.annotation.Nonnull;
import org.ini4j.Profile;
import org.ini4j.Wini;

/**
 * 内部工具类。
 *
 * @author mrzhqiang
 */
public final class Util {
  private Util() {
    throw new AssertionError("No instance.");
  }

  @Nonnull
  public static Profile.Section getSection(@Nonnull Wini wini, @Nonnull String name) {
    Profile.Section section = wini.get(name);
    if (section == null) {
      section = wini.add(name);
    }
    return section;
  }

  @Nonnull
  static Boolean getBoolean(@Nonnull Profile.Section section, @Nonnull String key,
      @Nonnull Boolean defaultValue) {
    Integer intValue = defaultValue ? 1 : 0;
    return section.get(key, Integer.class, intValue) == 1;
  }

  static void putBoolean(@Nonnull Profile.Section section, @Nonnull String key,
      @Nonnull Boolean value) {
    section.put(key, value ? 1 : 0);
  }
}
