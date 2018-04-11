package com.github.mrzhqiang.models;

import com.github.mrzhqiang.util.Sections;
import javax.annotation.Nullable;
import org.ini4j.Profile;

public class SelGateConfig extends ProgramConfig {
  public static final String GATE_PORT_1 = "GatePort1";
  public static final String GATE_PORT_2 = "GatePort2";
  public static final String GET_START_1 = "GetStart1";
  public static final String GET_START_2 = "GetStart2";

  // no gatePort
  public int[] gatePort = new int[2];
  // no getStart
  public boolean getStart1;
  public boolean getStart2;

  @Override public void readOf(@Nullable Profile.Section section) {
    super.readOf(section);
    gatePort[0] = Sections.getInt(section, GATE_PORT_1, gatePort[0]);
    gatePort[1] = Sections.getInt(section, GATE_PORT_2, gatePort[1]);
    getStart1 = Sections.getBoolean(section, GET_START_1, getStart1);
    getStart2 = Sections.getBoolean(section, GET_START_2, getStart2);
  }
}
