package youthm2.common.exception;

/**
 * 文件异常。
 *
 * @author qiang.zhang
 */
public final class FileException extends RuntimeException {
  public FileException(String message) {
    super(message);
  }

  public FileException(String message, Throwable cause) {
    super(message, cause);
  }
}
