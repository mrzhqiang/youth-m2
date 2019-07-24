package youthm2.bootstrap.model.program;

import java.io.InputStream;

/**
 * Program
 *
 * @author qiang.zhang
 */
public final class Program {
  public boolean enabled;
  public byte status;
  public String filename;
  public String dir;
  public Process process;
  public InputStream resultStream;
  public InputStream errorStream;
  public int x;
  public int y;
}
