package youthm2.gamecenter;

import javax.annotation.Nonnull;
import org.ini4j.Profile;

/**
 * 一个简单的配置接口。
 *
 * @author mrzhqiang
 */
public interface IConfig {

  /**
   * 配置文件中的片段名字。
   *
   * @return 字符串，片段名字。
   */
  @Nonnull String sectionName();

  /**
   * 从 Wini 中读取配置。
   *
   * @param section 配置文件中的片段。
   */
  void readFrom(@Nonnull Profile.Section section);

  /**
   * 写入配置到 Wini 中。
   *
   * @param section 配置文件中的片段。
   */
  void writeTo(@Nonnull Profile.Section section);
}
