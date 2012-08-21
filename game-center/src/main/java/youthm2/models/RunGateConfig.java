package youthm2.models;

import youthm2.util.Sections;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.ini4j.Profile;

import static youthm2.GameShare.MAX_RUN_GATE_COUNT;

public class RunGateConfig extends ProgramConfig {
  public static final String GET_START = "GetStart";
  public static final String GATE_PORT = "GatePort";

  // no getStart
  public List<Boolean> getStartList = new ArrayList<>(MAX_RUN_GATE_COUNT);
  // no gatePort
  public List<Integer> gatePortList = new ArrayList<>(MAX_RUN_GATE_COUNT);

  @Override public void readOf(@Nullable Profile.Section section) {
    super.readOf(section);
    for (int i = 0; i < MAX_RUN_GATE_COUNT; i++) {
      getStartList.add(Sections.getBoolean(section, GET_START + (i + 1), false));
      gatePortList.add(Sections.getInt(section, GATE_PORT + (i + 1), 0));
    }
  }
}
