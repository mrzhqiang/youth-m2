package youthm2.common;

import lombok.experimental.UtilityClass;

/**
 * 通用常量类
 * 从Pascal的GeneralCommon单元翻译而来
 */
@UtilityClass
public class GeneralCommon {

    // MG相关代码标记
    public static final char MG_CODE_HEAD = '%';
    public static final char MG_CODE_END = '$';

    // 通用代码标记
    public static final char G_CODE_HEAD = '#';
    public static final char G_CODE_END = '!';

    // MG用户操作标记
    public static final char MG_OPEN_USER = 'O';
    public static final char MG_SEND_USER = 'S';
    public static final char MG_CLOSE_USER = 'C';

}
