package com.github.mrzhqiang.models;

import com.github.mrzhqiang.util.Sections;
import javax.annotation.Nullable;
import org.ini4j.Profile;

public final class DBServerConfig extends ProgramConfig {
  public static final String SERVER_PORT = "ServerPort";

  public int serverPort;

  @Override public void readOf(@Nullable Profile.Section section) {
    super.readOf(section);
    serverPort = Sections.getInt(section, SERVER_PORT, serverPort);
  }
}
