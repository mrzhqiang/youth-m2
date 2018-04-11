package com.github.mrzhqiang;

import org.junit.Test;

public class GameShareTest {

  @Test
  public void testIni() {
    System.out.println(GameShare.iniConf.get(GameShare.BASIC_SECTION_NAME).get("GameDirectory"));
  }
}
