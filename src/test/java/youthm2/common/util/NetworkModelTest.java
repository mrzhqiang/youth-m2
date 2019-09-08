package youthm2.common.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * NetworkModelTest
 *
 * @author qiang.zhang
 */
public class NetworkModelTest {

  @Test
  public void isAddressV4() {
    Assert.assertTrue(Networks.isAddressV4("127.0.0.1"));
    Assert.assertFalse(Networks.isAddressV4("256.1.255.0"));
  }

  @Test
  public void isAddressV6() {
    Assert.assertTrue(Networks.isAddressV6("2001:0:53aa:64c:14df:7bc:8d24:ebef"));
  }
}