package com.github.mrzhqiang.models;

import com.github.mrzhqiang.util.Sections;
import javax.annotation.Nullable;
import org.ini4j.Profile;

public class LogServerConfig extends ProgramConfig {
  public static final String PORT = "Port";

  // no gatePort
  public int port;

  @Override public void readOf(@Nullable Profile.Section section) {
    super.readOf(section);
    port = Sections.getInt(section, PORT, port);
  }
}
