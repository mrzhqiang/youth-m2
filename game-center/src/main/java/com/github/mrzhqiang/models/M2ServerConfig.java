package com.github.mrzhqiang.models;

import com.github.mrzhqiang.util.Sections;
import javax.annotation.Nullable;
import org.ini4j.Profile;

public class M2ServerConfig extends ProgramConfig {
  public static final String MSG_SRV_PORT = "MsgSrvPort";

  public int msgSrvPort;

  @Override public void readOf(@Nullable Profile.Section section) {
    super.readOf(section);
    msgSrvPort = Sections.getInt(section, MSG_SRV_PORT, msgSrvPort);
  }
}
