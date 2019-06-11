package youthm2.common;

import java.io.File;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * CommandUtilTest
 *
 * @author mrzhqiang
 */
public class CommandUtilTest {

  @Test
  public void execute() {
    File notepad = new File("C:\\Windows","notepad.exe");
    CommandUtil.execute(notepad.getAbsolutePath());
  }
}