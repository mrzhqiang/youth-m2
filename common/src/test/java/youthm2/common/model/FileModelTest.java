package youthm2.common.model;

import java.io.File;
import java.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * FileModelTest
 *
 * @author qiang.zhang
 */
public class FileModelTest {

  private File testFile = new File("./temp_test_file_model.txt");

  @Before
  public void setUp() throws Exception {
    FileModel.existsOrCreate(testFile);
  }

  @After
  public void tearDown() throws Exception {
    FileModel.notExistsOrDelete(testFile);
  }

  @Test
  public void test() {
    FileModel.onceWrite(testFile, "你好，文件！");
    FileModel.appleWrite(testFile, "追加到末尾。");
  }
}