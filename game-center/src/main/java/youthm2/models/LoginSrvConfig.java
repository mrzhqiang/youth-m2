package youthm2.models;

import youthm2.util.Sections;
import javax.annotation.Nullable;
import org.ini4j.Profile;

public class LoginSrvConfig extends ProgramConfig {
  public static final String SERVER_PORT = "ServerPort";
  public static final String MON_PORT = "MonPort";

  public int serverPort;
  public int monPort;

  @Override public void readOf(@Nullable Profile.Section section) {
    super.readOf(section);
    serverPort = Sections.getInt(section, SERVER_PORT, serverPort);
    monPort = Sections.getInt(section, MON_PORT, monPort);
  }
}
