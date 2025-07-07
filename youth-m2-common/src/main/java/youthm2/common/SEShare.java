package youthm2.common;

import java.util.Date;

/**
 * SEShare - 共享消息定义
 * 从Pascal的SEShare.pas翻译而来
 */
public class SEShare {
    
    // 常量定义
    public static final String EXEDIRNAME = "1EXE\\";
    
    public static final long MSGHEADCODE = 0xAA55AA55L;

    public static final int SEC_CHECKPASS = 1000;
    public static final int SEC_GETFILELIST = 1001;
    public static final int SEC_GETFILE = 1002;
    public static final int SEC_PASS_GETFILE = 1004;

    public static final int SES_CHECKPASS_OK = 10000;
    public static final int SES_CHECKPASS_FAIR = 10001;
    public static final int SES_FILELIST = 10002;
    public static final int SES_FILE = 10003;
    public static final int SEC_PASS_GETFILE_FAIL = 10004;

    /**
     * 消息定义结构
     * 对应Pascal中的TDefMessage
     */
    public static class TDefMessage {
        public long recog;      // LongWord -> long
        public short ident;     // Word -> short  
        public int param;       // Integer -> int
        public Date dataTime;   // TDateTime -> Date
        public int dataSize;    // Integer -> int

        public TDefMessage() {
            this.recog = 0;
            this.ident = 0;
            this.param = 0;
            this.dataTime = new Date();
            this.dataSize = 0;
        }

        public TDefMessage(long recog, short ident, int param, Date dataTime, int dataSize) {
            this.recog = recog;
            this.ident = ident;
            this.param = param;
            this.dataTime = dataTime;
            this.dataSize = dataSize;
        }
    }

    /**
     * 创建默认消息
     * 对应Pascal中的MakeDefMessage函数
     * 
     * @param ident 标识符
     * @param param 参数
     * @param dataTime 数据时间
     * @param dataSize 数据大小
     * @return TDefMessage对象
     */
    public static TDefMessage makeDefMessage(short ident, int param, Date dataTime, int dataSize) {
        TDefMessage result = new TDefMessage();
        result.recog = MSGHEADCODE;
        result.ident = ident;
        result.param = param;
        result.dataTime = dataTime;
        result.dataSize = dataSize;
        return result;
    }
}