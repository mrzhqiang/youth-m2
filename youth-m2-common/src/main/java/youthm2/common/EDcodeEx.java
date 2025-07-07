package youthm2.common;

import lombok.experimental.UtilityClass;

/**
 * 编码解码工具类
 * 从Pascal文件EDcodeEx.pas翻译而来
 */
@UtilityClass
public class EDcodeEx {

    // 常量定义
    private static final byte BITMASKS = (byte) 0xAA;
    private static final String RSADECODESTRING2 = "_DqDNXi";

    // 掩码数组 (对应Pascal中的Masks数组)
    private static final byte[] MASKS = {0, 0, (byte) 0xFC, (byte) 0xF8, (byte) 0xF0, (byte) 0xE0, (byte) 0xC0};

    /**
     * 创建默认消息
     */
    public static Grobal2.DefaultMessage makeDefaultMsg(int wIdent, int nRecog, int wParam, int wTag, int wSeries) {
        return new Grobal2.DefaultMessage(nRecog, wIdent, wParam, wTag, wSeries);
    }

    /**
     * 6位编码缓冲区
     */
    public static void encode6BitBuf(byte[] pSrc, byte[] pDest, int nSrcLen, int nDestLen) {
        int nRestCount = 0;
        byte btRest = 0;
        int nDestPos = 0;

        for (int i = 0; i < nSrcLen; i++) {
            if (nDestPos >= nDestLen) {
                break;
            }

            byte btCh = pSrc[i];

            // XOR操作
            byte btXor = BITMASKS;
            btXor += i;
            btCh = (byte) (btCh ^ btXor);

            byte btMade = (byte) ((btRest | (btCh >> (2 + nRestCount))) & 0x3F);
            btRest = (byte) (((btCh << (8 - (2 + nRestCount))) >> 2) & 0x3F);
            nRestCount += 2;

            if (nRestCount < 6) {
                pDest[nDestPos] = (byte) (btMade + 0x3C);
                nDestPos++;
            } else {
                if (nDestPos < nDestLen - 1) {
                    pDest[nDestPos] = (byte) (btMade + 0x3C);
                    pDest[nDestPos + 1] = (byte) (btRest + 0x3C);
                    nDestPos += 2;
                } else {
                    pDest[nDestPos] = (byte) (btMade + 0x3C);
                    nDestPos++;
                }
                nRestCount = 0;
                btRest = 0;
            }
        }

        if (nRestCount > 0) {
            pDest[nDestPos] = (byte) (btRest + 0x3C);
            nDestPos++;
        }

        if (nDestPos < nDestLen) {
            pDest[nDestPos] = 0;
        }
    }

    /**
     * 6位解码缓冲区
     */
    public static void decode6BitBuf(byte[] sSource, byte[] pBuf, int nSrcLen, int nBufLen) {
        try {
            int nBitPos = 2;
            int nMadeBit = 0;
            int nBufPos = 0;
            byte btTmp = 0;
            byte btCh = 0;

            for (int i = 0; i < nSrcLen; i++) {
                if ((sSource[i] & 0xFF) - 0x3C >= 0) {
                    btCh = (byte) ((sSource[i] & 0xFF) - 0x3C);
                } else {
                    nBufPos = 0;
                    break;
                }

                if (nBufPos >= nBufLen) {
                    break;
                }

                if ((nMadeBit + 6) >= 8) {
                    byte btByte = (byte) (btTmp | ((btCh & 0x3F) >> (6 - nBitPos)));

                    // XOR操作
                    byte btXor = BITMASKS;
                    btXor += nBufPos;
                    btByte = (byte) (btByte ^ btXor);

                    pBuf[nBufPos] = btByte;
                    nBufPos++;
                    nMadeBit = 0;

                    if (nBitPos < 6) {
                        nBitPos += 2;
                    } else {
                        nBitPos = 2;
                        continue;
                    }
                }

                btTmp = (byte) ((btCh << nBitPos) & MASKS[nBitPos]);
                nMadeBit += 8 - nBitPos;
            }

            if (nBufPos < nBufLen) {
                pBuf[nBufPos] = 0;
            }
        } catch (Exception e) {
            // 异常处理，对应Pascal中的Except块
        }
    }

    /**
     * 解码消息
     */
    public static Grobal2.DefaultMessage decodeMessage(String str) {
        byte[] encBuf = new byte[Grobal2.BUFFERSIZE];
        byte[] strBytes = str.getBytes();
        decode6BitBuf(strBytes, encBuf, strBytes.length, encBuf.length);

        // 从字节数组重构DefaultMessage
        Grobal2.DefaultMessage msg = new Grobal2.DefaultMessage();
        if (encBuf.length >= 20) { // 假设DefaultMessage需要20字节
            msg.recog = bytesToInt(encBuf, 0);
            msg.ident = bytesToInt(encBuf, 4);
            msg.param = bytesToInt(encBuf, 8);
            msg.tag = bytesToInt(encBuf, 12);
            msg.series = bytesToInt(encBuf, 16);
        }
        return msg;
    }

    /**
     * 解码字符串
     */
    public static String decodeString(String str) {
        byte[] encBuf = new byte[Grobal2.BUFFERSIZE];
        byte[] strBytes = str.getBytes();
        decode6BitBuf(strBytes, encBuf, strBytes.length, encBuf.length);

        // 找到字符串结束位置
        int len = 0;
        while (len < encBuf.length && encBuf[len] != 0) {
            len++;
        }

        return new String(encBuf, 0, len);
    }

    /**
     * 解码缓冲区
     */
    public static void decodeBuffer(String src, byte[] buf, int bufSize) {
        byte[] encBuf = new byte[Grobal2.BUFFERSIZE];
        byte[] srcBytes = src.getBytes();
        decode6BitBuf(srcBytes, encBuf, srcBytes.length, encBuf.length);
        System.arraycopy(encBuf, 0, buf, 0, Math.min(bufSize, encBuf.length));
    }

    /**
     * 编码消息
     */
    public static String encodeMessage(Grobal2.DefaultMessage sMsg) {
        byte[] encBuf = new byte[Grobal2.BUFFERSIZE];
        byte[] tempBuf = new byte[Grobal2.BUFFERSIZE];

        // 将DefaultMessage转换为字节数组
        intToBytes(sMsg.recog, tempBuf, 0);
        intToBytes(sMsg.ident, tempBuf, 4);
        intToBytes(sMsg.param, tempBuf, 8);
        intToBytes(sMsg.tag, tempBuf, 12);
        intToBytes(sMsg.series, tempBuf, 16);

        int msgSize = 20; // DefaultMessage的大小
        encode6BitBuf(tempBuf, encBuf, msgSize, encBuf.length);

        // 找到编码结束位置
        int len = 0;
        while (len < encBuf.length && encBuf[len] != 0) {
            len++;
        }

        return new String(encBuf, 0, len);
    }

    /**
     * 编码字符串
     */
    public static String encodeString(String str) {
        byte[] encBuf = new byte[Grobal2.BUFFERSIZE];
        byte[] strBytes = str.getBytes();
        encode6BitBuf(strBytes, encBuf, strBytes.length, encBuf.length);

        // 找到编码结束位置
        int len = 0;
        while (len < encBuf.length && encBuf[len] != 0) {
            len++;
        }

        return new String(encBuf, 0, len);
    }

    /**
     * 编码缓冲区
     */
    public static String encodeBuffer(byte[] buf, int bufSize) {
        if (bufSize < Grobal2.BUFFERSIZE) {
            byte[] encBuf = new byte[Grobal2.BUFFERSIZE];
            byte[] tempBuf = new byte[Grobal2.BUFFERSIZE];
            System.arraycopy(buf, 0, tempBuf, 0, bufSize);
            encode6BitBuf(tempBuf, encBuf, bufSize, encBuf.length);

            // 找到编码结束位置
            int len = 0;
            while (len < encBuf.length && encBuf[len] != 0) {
                len++;
            }

            return new String(encBuf, 0, len);
        } else {
            return "";
        }
    }

    /**
     * 解码长缓冲区
     */
    public static void decodeLongBuffer(String src, byte[] buf, int bufSize) {
        int maxLen = getCodeMsgSize(Grobal2.DATABUFFERSIZE * 4.0 / 3.0);
        int textLen = src.length();

        if (textLen > maxLen) {
            int whiteLen = 0;
            String cStr;

            while (textLen >= maxLen && bufSize >= Grobal2.DATABUFFERSIZE) {
                cStr = src.substring(0, maxLen);
                src = src.substring(maxLen);
                textLen -= maxLen;

                byte[] tempBuf = new byte[Grobal2.DATABUFFERSIZE];
                decodeBuffer(cStr, tempBuf, Grobal2.DATABUFFERSIZE);
                System.arraycopy(tempBuf, 0, buf, whiteLen, Grobal2.DATABUFFERSIZE);

                whiteLen += Grobal2.DATABUFFERSIZE;
                bufSize -= Grobal2.DATABUFFERSIZE;
            }

            if (bufSize > 0 && textLen > 0 && !src.isEmpty()) {
                byte[] tempBuf = new byte[bufSize];
                decodeBuffer(src, tempBuf, bufSize);
                System.arraycopy(tempBuf, 0, buf, whiteLen, bufSize);
            }
        } else {
            decodeBuffer(src, buf, bufSize);
        }
    }

    /**
     * 编码长缓冲区
     */
    public static String encodeLongBuffer(byte[] buf, int bufSize) {
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

    // 辅助方法
    private static int getCodeMsgSize(double x) {
        if ((int) x < x) {
            return (int) x + 1;
        } else {
            return (int) x;
        }
    }

    /**
     * 将整数转换为字节数组
     */
    private static void intToBytes(int value, byte[] bytes, int offset) {
        bytes[offset] = (byte) (value & 0xFF);
        bytes[offset + 1] = (byte) ((value >> 8) & 0xFF);
        bytes[offset + 2] = (byte) ((value >> 16) & 0xFF);
        bytes[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    /**
     * 将字节数组转换为整数
     */
    private static int bytesToInt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) |
                ((bytes[offset + 1] & 0xFF) << 8) |
                ((bytes[offset + 2] & 0xFF) << 16) |
                ((bytes[offset + 3] & 0xFF) << 24);
    }
}
