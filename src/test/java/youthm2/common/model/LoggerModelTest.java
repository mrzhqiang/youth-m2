package youthm2.common.model;

import org.junit.Test;

/**
 * LoggerModelTest
 *
 * @author qiang.zhang
 */
public class LoggerModelTest {

  @Test
  public void test() {
    LoggerModel.COMMON.info("1");
    LoggerModel.BOOTSTRAP.info("2");
    LoggerModel.LAUNCHER.info("3");
    LoggerModel.DATABASE.info("4");
    LoggerModel.ACCOUNT.info("5");
    LoggerModel.LOGGER.info("6");
    LoggerModel.CORE.info("7");
    LoggerModel.GAME.info("8");
    LoggerModel.ROLE.info("9");
    LoggerModel.LOGIN.info("10");
    LoggerModel.RANK.info("11");
  }
}