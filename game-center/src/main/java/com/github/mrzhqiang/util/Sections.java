package com.github.mrzhqiang.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ini4j.Profile;

public final class Sections {
  private Sections() {
    // no instance
  }

  public static Integer getInt(@Nullable Profile.Section section, @Nonnull String keyName,
      @Nonnull Integer defaultValue) {
    if (section == null) {
      return defaultValue;
    }
    return section.get(keyName, Integer.class, defaultValue);
  }

  public static Long getLong(@Nullable Profile.Section section, @Nonnull String keyName,
      @Nonnull Long defaultValue) {
    if (section == null) {
      return defaultValue;
    }
    return section.get(keyName, Long.class, defaultValue);
  }

  public static Boolean getBoolean(@Nullable Profile.Section section, @Nonnull String keyName,
      @Nonnull Boolean defaultValue) {
    if (section == null) {
      return defaultValue;
    }
    Integer intValue = defaultValue ? 1 : 0;
    return section.get(keyName, Integer.class, intValue) == 1;
  }

  public static void setInt(@Nullable Profile.Section section, @Nonnull String keyName,
      @Nonnull Integer newValue) {
    if (section == null) {
      return;
    }
    section.put(keyName, newValue);
  }

  public static void setLong(@Nullable Profile.Section section, @Nonnull String keyName,
      @Nonnull Long newValue) {
    if (section == null) {
      return;
    }
    section.put(keyName, newValue);
  }

  public static void setBoolean(@Nullable Profile.Section section, @Nonnull String keyName,
      @Nonnull Boolean newValue) {
    if (section == null) {
      return;
    }
    section.put(keyName, newValue);
  }
}
