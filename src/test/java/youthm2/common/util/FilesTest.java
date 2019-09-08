package youthm2.common.util;

import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * FileModelTest
 *
 * @author qiang.zhang
 */
public class FilesTest {

  private File testFile = new File("./temp_test_files.txt");

  @Before
  public void setUp() throws Exception {
    Files.createOrExists(testFile);
  }

  @After
  public void tearDown() throws Exception {
    Files.deleteOrNotExists(testFile);
  }

  @Test
  public void test() throws Exception {
    Files.onceWrite(testFile, "你好，文件！");
    Files.appleWrite(testFile, "追加到末尾。");
  }
}