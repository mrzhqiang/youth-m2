package com.github.mrzhqiang.models;

import com.github.mrzhqiang.util.Sections;
import javax.annotation.Nullable;
import org.ini4j.Profile;

public abstract class ProgramConfig {
  public static final String MAIN_FORM_X = "MainFormX";
  public static final String MAIN_FORM_Y = "MainFormY";
  public static final String GATE_PORT = "GatePort";
  public static final String GET_START = "GetStart";

  public int mainFormX;
  public int mainFormY;
  public int gatePort;
  public boolean getStart;
  public String[] programFile = new String[50];

  public void readOf(@Nullable Profile.Section section) {
    mainFormX = Sections.getInt(section, MAIN_FORM_X, mainFormX);
    mainFormY = Sections.getInt(section, MAIN_FORM_Y, mainFormY);
    gatePort = Sections.getInt(section, GATE_PORT, gatePort);
    getStart = Sections.getBoolean(section, GET_START, getStart);
  }
}
