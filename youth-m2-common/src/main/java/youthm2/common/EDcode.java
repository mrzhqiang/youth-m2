package youthm2.common;

import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 编码解码工具类
 * 从Pascal文件EDcode.pas翻译而来
 * 用于处理默认消息和字符串的编码解码
 */
@UtilityClass
public class EDcode {

    // 旧模式和新模式常量
    public static final int OLDMODE = 0;
    public static final int NEWMODE = 1;

    // 编码模式 - 使用新模式
    public static final int ENDECODEMODE = NEWMODE;

    // 位掩码常量
    private static final int BITMASKS = 0xAA;

    // RSA解码字符串
    public static String RSADECODESTRING2 = "_DqDNXi";

    // 掩码数组，用于位操作
    private static final byte[] MASKS = {
            (byte) 0xFC, (byte) 0xF8, (byte) 0xF0, (byte) 0xE0, (byte) 0xC0
    };

    /**
     * 创建默认消息
     *
     * @param ident  识别符
     * @param recog  识别码
     * @param param  参数
     * @param tag    标签
     * @param series 系列
     * @return 默认消息对象
     */
    public static Grobal2.DefaultMessage makeDefaultMsg(int ident, int recog, int param, int tag, int series) {
        return new Grobal2.DefaultMessage(recog, ident, param, tag, series);
    }

    /**
     * 6位编码缓冲区
     *
     * @param src     源数据
     * @param srcLen  源数据长度
     * @param destLen 目标缓冲区长度
     * @return 编码后的字符串
     */
    public static String encode6BitBuf(byte[] src, int srcLen, int destLen) {
        if (src == null || srcLen <= 0) {
            return "";
        }

        byte[] dest = new byte[destLen];
        int nRestCount = 0;
        byte btRest = 0;
        int nDestPos = 0;

        for (int i = 0; i < srcLen && nDestPos < destLen; i++) {
            byte btCh = src[i];

            // 如果是新模式，进行XOR操作
            if (ENDECODEMODE == NEWMODE) {
                int btXor = BITMASKS + i;
                btCh ^= (byte) btXor;
            }

            byte btMade = (byte) ((btRest | (btCh >> (2 + nRestCount))) & 0x3F);
            btRest = (byte) (((btCh << (8 - (2 + nRestCount))) >> 2) & 0x3F);
            nRestCount += 2;

            if (nRestCount < 6) {
                dest[nDestPos] = (byte) (btMade + 0x3C);
                nDestPos++;
            } else {
                if (nDestPos < destLen - 1) {
                    dest[nDestPos] = (byte) (btMade + 0x3C);
                    dest[nDestPos + 1] = (byte) (btRest + 0x3C);
                    nDestPos += 2;
                } else {
                    dest[nDestPos] = (byte) (btMade + 0x3C);
                    nDestPos++;
                }
                nRestCount = 0;
                btRest = 0;
            }
        }

        if (nRestCount > 0 && nDestPos < destLen) {
            dest[nDestPos] = (byte) (btRest + 0x3C);
            nDestPos++;
        }

        return new String(dest, 0, nDestPos, StandardCharsets.ISO_8859_1);
    }

    /**
     * 6位解码缓冲区
     *
     * @param source 源字符串
     * @param bufLen 缓冲区长度
     * @return 解码后的字节数组
     */
    public static byte[] decode6BitBuf(String source, int bufLen) {
        if (source == null || source.isEmpty()) {
            return new byte[0];
        }

        byte[] buf = new byte[bufLen];
        byte[] sourceBytes = source.getBytes(StandardCharsets.ISO_8859_1);
        int srcLen = sourceBytes.length;

        int nBitPos = 2;
        int nMadeBit = 0;
        int nBufPos = 0;
        byte btTmp = 0;

        for (int i = 0; i < srcLen && nBufPos < bufLen; i++) {
            int btChInt = sourceBytes[i] & 0xFF;
            byte btCh;

            if (btChInt - 0x3C >= 0) {
                btCh = (byte) (btChInt - 0x3C);
            } else {
                break;
            }

            if ((nMadeBit + 6) >= 8) {
                byte btByte = (byte) (btTmp | ((btCh & 0x3F) >> (6 - nBitPos)));

                // 如果是新模式，进行XOR操作
                if (ENDECODEMODE == NEWMODE) {
                    int btXor = BITMASKS + nBufPos;
                    btByte ^= (byte) btXor;
                }

                buf[nBufPos] = btByte;
                nBufPos++;
                nMadeBit = 0;

                if (nBitPos < 6) {
                    nBitPos += 2;
                } else {
                    nBitPos = 2;
                    continue;
                }
            }

            btTmp = (byte) ((btCh << nBitPos) & MASKS[nBitPos - 2]);
            nMadeBit += 8 - nBitPos;
        }

        return buf;
    }

    /**
     * 编码默认消息
     *
     * @param msg 默认消息对象
     * @return 编码后的字符串
     */
    public static String encodeMessage(Grobal2.DefaultMessage msg) {
        if (msg == null) {
            return "";
        }

        // 将消息转换为字节数组
        ByteBuffer buffer = ByteBuffer.allocate(20); // sizeof(TDefaultMessage)
        buffer.putInt(msg.recog);
        buffer.putInt(msg.ident);
        buffer.putInt(msg.param);
        buffer.putInt(msg.tag);
        buffer.putInt(msg.series);

        byte[] msgBytes = buffer.array();
        return encode6BitBuf(msgBytes, msgBytes.length, Grobal2.BUFFERSIZE);
    }

    /**
     * 解码默认消息
     *
     * @param str 编码字符串
     * @return 默认消息对象
     */
    public static Grobal2.DefaultMessage decodeMessage(String str) {
        if (str == null || str.isEmpty()) {
            return new Grobal2.DefaultMessage();
        }

        byte[] buf = decode6BitBuf(str, 20); // sizeof(TDefaultMessage)

        if (buf.length < 20) {
            return new Grobal2.DefaultMessage();
        }

        ByteBuffer buffer = ByteBuffer.wrap(buf);
        int recog = buffer.getInt();
        int ident = buffer.getInt();
        int param = buffer.getInt();
        int tag = buffer.getInt();
        int series = buffer.getInt();

        return new Grobal2.DefaultMessage(recog, ident, param, tag, series);
    }

    /**
     * 编码字符串
     *
     * @param str 原始字符串
     * @return 编码后的字符串
     */
    public static String encodeString(String str) {
        if (str == null) {
            return "";
        }

        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        return encode6BitBuf(strBytes, strBytes.length, Grobal2.BUFFERSIZE);
    }

    /**
     * 解码字符串
     *
     * @param str 编码字符串
     * @return 解码后的字符串
     */
    public static String decodeString(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }

        byte[] buf = decode6BitBuf(str, Grobal2.BUFFERSIZE);

        // 找到字符串结束位置
        int len = 0;
        for (int i = 0; i < buf.length; i++) {
            if (buf[i] == 0) {
                len = i;
                break;
            }
        }

        if (len == 0) {
            len = buf.length;
        }

        return new String(buf, 0, len, StandardCharsets.UTF_8);
    }

    /**
     * 编码字节缓冲区
     *
     * @param buf     字节缓冲区
     * @param bufSize 缓冲区大小
     * @return 编码后的字符串
     */
    public static String encodeBuffer(byte[] buf, int bufSize) {
        if (buf == null || bufSize <= 0 || bufSize > Grobal2.BUFFERSIZE) {
            return "";
        }

        return encode6BitBuf(buf, bufSize, Grobal2.BUFFERSIZE);
    }

    /**
     * 解码字符串到缓冲区
     *
     * @param src     源字符串
     * @param bufSize 缓冲区大小
     * @return 解码后的字节数组
     */
    public static byte[] decodeBuffer(String src, int bufSize) {
        if (src == null || src.isEmpty() || bufSize <= 0) {
            return new byte[bufSize];
        }

        return decode6BitBuf(src, bufSize);
    }

    /**
     * 编码长缓冲区
     *
     * @param buf     字节缓冲区
     * @param bufSize 缓冲区大小
     * @return 编码后的字符串
     */
    public static String encodeLongBuffer(byte[] buf, int bufSize) {
        if (buf == null || bufSize <= 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        if (bufSize > Grobal2.DATABUFFERSIZE) {
            int whiteLen = 0;
            while (bufSize > Grobal2.DATABUFFERSIZE) {
                byte[] tempBuf = new byte[Grobal2.DATABUFFERSIZE];
                System.arraycopy(buf, whiteLen, tempBuf, 0, Grobal2.DATABUFFERSIZE);
                result.append(encodeBuffer(tempBuf, Grobal2.DATABUFFERSIZE));
                whiteLen += Grobal2.DATABUFFERSIZE;
                bufSize -= Grobal2.DATABUFFERSIZE;
            }

            if (bufSize > 0) {
                byte[] tempBuf = new byte[bufSize];
                System.arraycopy(buf, whiteLen, tempBuf, 0, bufSize);
                result.append(encodeBuffer(tempBuf, bufSize));
            }
        } else {
            result.append(encodeBuffer(buf, bufSize));
        }

        return result.toString();
    }

    /**
     * 解码长缓冲区
     *
     * @param src     源字符串
     * @param bufSize 缓冲区大小
     * @return 解码后的字节数组
     */
    public static byte[] decodeLongBuffer(String src, int bufSize) {
        if (src == null || src.isEmpty() || bufSize <= 0) {
            return new byte[bufSize];
        }

        byte[] result = new byte[bufSize];
        int maxLen = getCodeMsgSize(Grobal2.DATABUFFERSIZE * 4.0 / 3.0);
        int textLen = src.length();

        if (textLen > maxLen) {
            int whiteLen = 0;
            while (textLen >= maxLen && bufSize >= Grobal2.DATABUFFERSIZE) {
                String cStr = src.substring(0, maxLen);
                src = src.substring(maxLen);
                textLen -= maxLen;

                byte[] decodedData = decodeBuffer(cStr, Grobal2.DATABUFFERSIZE);
                System.arraycopy(decodedData, 0, result, whiteLen,
                        Math.min(decodedData.length, Grobal2.DATABUFFERSIZE));

                whiteLen += Grobal2.DATABUFFERSIZE;
                bufSize -= Grobal2.DATABUFFERSIZE;
            }

            if (bufSize > 0 && textLen > 0 && !src.isEmpty()) {
                byte[] decodedData = decodeBuffer(src, bufSize);
                System.arraycopy(decodedData, 0, result, whiteLen,
                        Math.min(decodedData.length, bufSize));
            }
        } else {
            byte[] decodedData = decodeBuffer(src, bufSize);
            System.arraycopy(decodedData, 0, result, 0,
                    Math.min(decodedData.length, bufSize));
        }

        return result;
    }

    /**
     * 获取编码消息大小
     *
     * @param x 输入值
     * @return 编码消息大小
     */
    private static int getCodeMsgSize(double x) {
        if ((int) x < x) {
            return (int) Math.floor(x) + 1;
        } else {
            return (int) Math.floor(x);
        }
    }
}
