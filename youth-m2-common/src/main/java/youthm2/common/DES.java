package youthm2.common;

import lombok.experimental.UtilityClass;

import java.util.Arrays;

/**
 * DES加密解密工具类
 * 从Pascal/Delphi代码翻译而来
 */
@UtilityClass
public class DES {

    // 枚举类型定义
    public enum DesMode {
        ENCRYPT, DECRYPT
    }

    // 类型定义
    private static class KeyByte {
        byte[] data = new byte[6];
    }

    private static class SubKey {
        KeyByte[] keys = new KeyByte[16];

        public SubKey() {
            for (int i = 0; i < keys.length; i++) {
                keys[i] = new KeyByte();
            }
        }
    }

    // 初始置换IP
    private static final int[] BIT_IP = {
            57, 49, 41, 33, 25, 17, 9, 1,
            59, 51, 43, 35, 27, 19, 11, 3,
            61, 53, 45, 37, 29, 21, 13, 5,
            63, 55, 47, 39, 31, 23, 15, 7,
            56, 48, 40, 32, 24, 16, 8, 0,
            58, 50, 42, 34, 26, 18, 10, 2,
            60, 52, 44, 36, 28, 20, 12, 4,
            62, 54, 46, 38, 30, 22, 14, 6
    };

    // 逆初始置换IP-1
    private static final int[] BIT_CP = {
            39, 7, 47, 15, 55, 23, 63, 31,
            38, 6, 46, 14, 54, 22, 62, 30,
            37, 5, 45, 13, 53, 21, 61, 29,
            36, 4, 44, 12, 52, 20, 60, 28,
            35, 3, 43, 11, 51, 19, 59, 27,
            34, 2, 42, 10, 50, 18, 58, 26,
            33, 1, 41, 9, 49, 17, 57, 25,
            32, 0, 40, 8, 48, 16, 56, 24
    };

    // 位选择函数E
    private static final int[] BIT_EXP = {
            31, 0, 1, 2, 3, 4, 3, 4, 5, 6, 7, 8, 7, 8, 9, 10,
            11, 12, 11, 12, 13, 14, 15, 16, 15, 16, 17, 18, 19, 20, 19, 20,
            21, 22, 23, 24, 23, 24, 25, 26, 27, 28, 27, 28, 29, 30, 31, 0
    };

    // 置换函数P
    private static final int[] BIT_PM = {
            15, 6, 19, 20, 28, 11, 27, 16, 0, 14, 22, 25, 4, 17, 30, 9,
            1, 7, 23, 13, 31, 26, 2, 8, 18, 12, 29, 5, 21, 10, 3, 24
    };

    // S盒
    private static final int[][][] S_BOX = {
            {{14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7},
                    {0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8},
                    {4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0},
                    {15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13}},

            {{15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10},
                    {3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5},
                    {0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15},
                    {13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9}},

            {{10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8},
                    {13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1},
                    {13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7},
                    {1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12}},

            {{7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15},
                    {13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9},
                    {10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4},
                    {3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14}},

            {{2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9},
                    {14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6},
                    {4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14},
                    {11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3}},

            {{12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11},
                    {10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8},
                    {9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6},
                    {4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13}},

            {{4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1},
                    {13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6},
                    {1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2},
                    {6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12}},

            {{13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7},
                    {1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2},
                    {7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8},
                    {2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11}}
    };

    // 选择置换PC-1
    private static final int[] BIT_PMC1 = {
            56, 48, 40, 32, 24, 16, 8,
            0, 57, 49, 41, 33, 25, 17,
            9, 1, 58, 50, 42, 34, 26,
            18, 10, 2, 59, 51, 43, 35,
            62, 54, 46, 38, 30, 22, 14,
            6, 61, 53, 45, 37, 29, 21,
            13, 5, 60, 52, 44, 36, 28,
            20, 12, 4, 27, 19, 11, 3
    };

    // 选择置换PC-2
    private static final int[] BIT_PMC2 = {
            13, 16, 10, 23, 0, 4,
            2, 27, 14, 5, 20, 9,
            22, 18, 11, 3, 25, 7,
            15, 6, 26, 19, 12, 1,
            40, 51, 30, 36, 46, 54,
            29, 39, 50, 44, 32, 47,
            43, 48, 38, 55, 33, 52,
            45, 41, 49, 35, 28, 31
    };

    // 辅助函数
    private static int min(int n1, int n2) {
        return Math.min(n1, n2);
    }

    private static int max(int n1, int n2) {
        return Math.max(n1, n2);
    }

    // 初始置换
    private static void initPermutation(byte[] inData) {
        byte[] newData = new byte[8];
        Arrays.fill(newData, (byte) 0);

        for (int i = 0; i < 64; i++) {
            if ((inData[BIT_IP[i] >> 3] & (1 << (7 - (BIT_IP[i] & 0x07)))) != 0) {
                newData[i >> 3] |= (1 << (7 - (i & 0x07)));
            }
        }

        System.arraycopy(newData, 0, inData, 0, 8);
    }

    // 逆初始置换
    private static void conversePermutation(byte[] inData) {
        byte[] newData = new byte[8];
        Arrays.fill(newData, (byte) 0);

        for (int i = 0; i < 64; i++) {
            if ((inData[BIT_CP[i] >> 3] & (1 << (7 - (BIT_CP[i] & 0x07)))) != 0) {
                newData[i >> 3] |= (1 << (7 - (i & 0x07)));
            }
        }

        System.arraycopy(newData, 0, inData, 0, 8);
    }

    // 扩展置换
    private static void expand(byte[] inData, byte[] outData) {
        Arrays.fill(outData, (byte) 0);

        for (int i = 0; i < 48; i++) {
            if ((inData[BIT_EXP[i] >> 3] & (1 << (7 - (BIT_EXP[i] & 0x07)))) != 0) {
                outData[i >> 3] |= (1 << (7 - (i & 0x07)));
            }
        }
    }

    // P置换
    private static void permutation(byte[] inData) {
        byte[] newData = new byte[4];
        Arrays.fill(newData, (byte) 0);

        for (int i = 0; i < 32; i++) {
            if ((inData[BIT_PM[i] >> 3] & (1 << (7 - (BIT_PM[i] & 0x07)))) != 0) {
                newData[i >> 3] |= (1 << (7 - (i & 0x07)));
            }
        }

        System.arraycopy(newData, 0, inData, 0, 4);
    }

    // S盒变换
    private static int si(int s, int inByte) {
        int c = (inByte & 0x20) | ((inByte & 0x1E) >> 1) | ((inByte & 0x01) << 4);
        return (S_BOX[s][c >> 4][c & 0x0F] & 0x0F);
    }

    // 选择置换1
    private static void permutationChoose1(byte[] inData, byte[] outData) {
        Arrays.fill(outData, (byte) 0);

        for (int i = 0; i < 56; i++) {
            if ((inData[BIT_PMC1[i] >> 3] & (1 << (7 - (BIT_PMC1[i] & 0x07)))) != 0) {
                outData[i >> 3] |= (1 << (7 - (i & 0x07)));
            }
        }
    }

    // 选择置换2
    private static void permutationChoose2(byte[] inData, byte[] outData) {
        Arrays.fill(outData, (byte) 0);

        for (int i = 0; i < 48; i++) {
            if ((inData[BIT_PMC2[i] >> 3] & (1 << (7 - (BIT_PMC2[i] & 0x07)))) != 0) {
                outData[i >> 3] |= (1 << (7 - (i & 0x07)));
            }
        }
    }

    // 循环左移
    private static void cycleMove(byte[] inData, int bitMove) {
        for (int i = 0; i < bitMove; i++) {
            inData[0] = (byte) ((inData[0] << 1) | (inData[1] >> 7));
            inData[1] = (byte) ((inData[1] << 1) | (inData[2] >> 7));
            inData[2] = (byte) ((inData[2] << 1) | (inData[3] >> 7));
            inData[3] = (byte) ((inData[3] << 1) | ((inData[0] & 0x10) >> 4));
            inData[0] = (byte) (inData[0] & 0x0F);
        }
    }

    // 生成子密钥
    private static void makeKey(byte[] inKey, SubKey outKey) {
        final int[] bitDisplace = {1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};

        byte[] outData56 = new byte[7];
        byte[] key28l = new byte[4];
        byte[] key28r = new byte[4];
        byte[] key56o = new byte[7];

        permutationChoose1(inKey, outData56);

        key28l[0] = (byte) (outData56[0] >> 4);
        key28l[1] = (byte) ((outData56[0] << 4) | (outData56[1] >> 4));
        key28l[2] = (byte) ((outData56[1] << 4) | (outData56[2] >> 4));
        key28l[3] = (byte) ((outData56[2] << 4) | (outData56[3] >> 4));
        key28r[0] = (byte) (outData56[3] & 0x0F);
        key28r[1] = outData56[4];
        key28r[2] = outData56[5];
        key28r[3] = outData56[6];

        for (int i = 0; i < 16; i++) {
            cycleMove(key28l, bitDisplace[i]);
            cycleMove(key28r, bitDisplace[i]);
            key56o[0] = (byte) ((key28l[0] << 4) | (key28l[1] >> 4));
            key56o[1] = (byte) ((key28l[1] << 4) | (key28l[2] >> 4));
            key56o[2] = (byte) ((key28l[2] << 4) | (key28l[3] >> 4));
            key56o[3] = (byte) ((key28l[3] << 4) | (key28r[0]));
            key56o[4] = key28r[1];
            key56o[5] = key28r[2];
            key56o[6] = key28r[3];
            permutationChoose2(key56o, outKey.keys[i].data);
        }
    }

    // 加密函数
    private static void encry(byte[] inData, byte[] subKey, byte[] outData) {
        byte[] outBuf = new byte[6];
        byte[] buf = new byte[8];

        expand(inData, outBuf);

        for (int i = 0; i < 6; i++) {
            outBuf[i] ^= subKey[i];
        }

        buf[0] = (byte) (outBuf[0] >> 2);
        buf[1] = (byte) (((outBuf[0] & 0x03) << 4) | (outBuf[1] >> 4));
        buf[2] = (byte) (((outBuf[1] & 0x0F) << 2) | (outBuf[2] >> 6));
        buf[3] = (byte) (outBuf[2] & 0x3F);
        buf[4] = (byte) (outBuf[3] >> 2);
        buf[5] = (byte) (((outBuf[3] & 0x03) << 4) | (outBuf[4] >> 4));
        buf[6] = (byte) (((outBuf[4] & 0x0F) << 2) | (outBuf[5] >> 6));
        buf[7] = (byte) (outBuf[5] & 0x3F);

        for (int i = 0; i < 8; i++) {
            buf[i] = (byte) si(i, buf[i] & 0xFF);
        }

        for (int i = 0; i < 4; i++) {
            outBuf[i] = (byte) ((buf[i * 2] << 4) | buf[i * 2 + 1]);
        }

        permutation(outBuf);

        System.arraycopy(outBuf, 0, outData, 0, 4);
    }

    // DES主算法
    private static void desData(DesMode desMode, byte[] inData, SubKey subKey, byte[] outData) {
        byte[] temp = new byte[4];
        byte[] buf = new byte[4];

        System.arraycopy(inData, 0, outData, 0, 8);
        initPermutation(outData);

        if (desMode == DesMode.ENCRYPT) {
            for (int i = 0; i < 16; i++) {
                // temp = Ln
                System.arraycopy(outData, 0, temp, 0, 4);
                // Ln+1 = Rn
                System.arraycopy(outData, 4, outData, 0, 4);
                // Rn ==Kn==> buf
                encry(outData, subKey.keys[i].data, buf);
                // Rn+1 = Ln^buf
                for (int j = 0; j < 4; j++) {
                    outData[j + 4] = (byte) (temp[j] ^ buf[j]);
                }
            }

            // 交换左右两部分
            System.arraycopy(outData, 4, temp, 0, 4);
            System.arraycopy(outData, 0, outData, 4, 4);
            System.arraycopy(temp, 0, outData, 0, 4);
        } else if (desMode == DesMode.DECRYPT) {
            for (int i = 15; i >= 0; i--) {
                System.arraycopy(outData, 0, temp, 0, 4);
                System.arraycopy(outData, 4, outData, 0, 4);
                encry(outData, subKey.keys[i].data, buf);
                for (int j = 0; j < 4; j++) {
                    outData[j + 4] = (byte) (temp[j] ^ buf[j]);
                }
            }

            // 交换左右两部分
            System.arraycopy(outData, 4, temp, 0, 4);
            System.arraycopy(outData, 0, outData, 4, 4);
            System.arraycopy(temp, 0, outData, 0, 4);
        }

        conversePermutation(outData);
    }

    // 公共方法：加密缓冲区
    public static void encryptBuffer(String key, byte[] source, byte[] dest, int sourceLen, int destLen) {
        byte[] strByte = new byte[8];
        byte[] outByte = new byte[8];
        byte[] keyByte = new byte[8];
        SubKey subKey = new SubKey();

        Arrays.fill(keyByte, (byte) 0);

        for (int j = 0; j < min(8, key.length()); j++) {
            keyByte[j] = (byte) key.charAt(j);
        }
        makeKey(keyByte, subKey);

        int srcLen = sourceLen + (8 - (sourceLen % 8));
        byte[] pSource = new byte[srcLen];
        System.arraycopy(source, 0, pSource, 0, sourceLen);

        int desLen = 0;
        for (int i = 0; i < srcLen / 8; i++) {
            for (int j = 0; j < 8; j++) {
                strByte[j] = pSource[i * 8 + j];
            }
            desData(DesMode.ENCRYPT, strByte, subKey, outByte);
            for (int j = 0; j < 8; j++) {
                if (desLen >= destLen) return;
                dest[desLen] = outByte[j];
                desLen++;
            }
        }
    }

    // 公共方法：解密缓冲区
    public static void decryptBuffer(String key, byte[] source, byte[] dest, int sourceLen, int destLen) {
        byte[] strByte = new byte[8];
        byte[] outByte = new byte[8];
        byte[] keyByte = new byte[8];
        SubKey subKey = new SubKey();

        Arrays.fill(keyByte, (byte) 0);

        for (int j = 0; j < min(8, key.length()); j++) {
            keyByte[j] = (byte) key.charAt(j);
        }
        makeKey(keyByte, subKey);

        int desLen = 0;
        for (int i = 0; i < sourceLen / 8; i++) {
            for (int j = 0; j < 8; j++) {
                strByte[j] = source[i * 8 + j];
            }
            desData(DesMode.DECRYPT, strByte, subKey, outByte);
            for (int j = 0; j < 8; j++) {
                if (desLen >= destLen) return;
                dest[desLen] = outByte[j];
                desLen++;
            }
        }
    }

    // 公共方法：加密字符串
    public static String encryptStr(String str, String key) {
        if (str.length() > 0 && str.charAt(str.length() - 1) == '\0') {
            throw new IllegalArgumentException("Error: the last char is NULL char.");
        }

        // 补充密钥长度到8位
        while (key.length() < 8) {
            key += '\0';
        }

        // 补充字符串长度到8的倍数
        while (str.length() % 8 != 0) {
            str += '\0';
        }

        byte[] keyByte = new byte[8];
        for (int j = 0; j < 8; j++) {
            keyByte[j] = (byte) key.charAt(j);
        }

        SubKey subKey = new SubKey();
        makeKey(keyByte, subKey);

        StringBuilder strResult = new StringBuilder();

        for (int i = 0; i < str.length() / 8; i++) {
            byte[] strByte = new byte[8];
            byte[] outByte = new byte[8];

            for (int j = 0; j < 8; j++) {
                strByte[j] = (byte) str.charAt(i * 8 + j);
            }

            desData(DesMode.ENCRYPT, strByte, subKey, outByte);

            for (int j = 0; j < 8; j++) {
                strResult.append((char) (outByte[j] & 0xFF));
            }
        }

        return strResult.toString();
    }

    // 公共方法：解密字符串
    public static String decryptStr(String str, String key) {
        // 补充密钥长度到8位
        while (key.length() < 8) {
            key += '\0';
        }

        byte[] keyByte = new byte[8];
        for (int j = 0; j < 8; j++) {
            keyByte[j] = (byte) key.charAt(j);
        }

        SubKey subKey = new SubKey();
        makeKey(keyByte, subKey);

        StringBuilder strResult = new StringBuilder();

        for (int i = 0; i < str.length() / 8; i++) {
            byte[] strByte = new byte[8];
            byte[] outByte = new byte[8];

            for (int j = 0; j < 8; j++) {
                strByte[j] = (byte) str.charAt(i * 8 + j);
            }

            desData(DesMode.DECRYPT, strByte, subKey, outByte);

            for (int j = 0; j < 8; j++) {
                strResult.append((char) (outByte[j] & 0xFF));
            }
        }

        // 移除尾部的空字符
        String result = strResult.toString();
        while (result.length() > 0 && result.charAt(result.length() - 1) == '\0') {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    // 公共方法：加密字符串并返回十六进制
    public static String encryptStrHex(String str, String key) {
        if (str.length() > 0 && str.charAt(str.length() - 1) == '\0') {
            throw new IllegalArgumentException("Error: the last char is NULL char.");
        }

        // 补充密钥长度到8位
        while (key.length() < 8) {
            key += '\0';
        }

        // 补充字符串长度到8的倍数
        while (str.length() % 8 != 0) {
            str += '\0';
        }

        byte[] keyByte = new byte[8];
        for (int j = 0; j < 8; j++) {
            keyByte[j] = (byte) key.charAt(j);
        }

        SubKey subKey = new SubKey();
        makeKey(keyByte, subKey);

        StringBuilder strResult = new StringBuilder();

        for (int i = 0; i < str.length() / 8; i++) {
            byte[] strByte = new byte[8];
            byte[] outByte = new byte[8];

            for (int j = 0; j < 8; j++) {
                strByte[j] = (byte) str.charAt(i * 8 + j);
            }

            desData(DesMode.ENCRYPT, strByte, subKey, outByte);

            for (int j = 0; j < 8; j++) {
                strResult.append(String.format("%02x", outByte[j] & 0xFF));
            }
        }

        return strResult.toString();
    }

    // 公共方法：解密十六进制字符串
    public static String decryptStrHex(String strHex, String key) {
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < strHex.length() / 2; i++) {
            String temp = strHex.substring(i * 2, i * 2 + 2);
            str.append((char) Integer.parseInt(temp, 16));
        }

        return decryptStr(str.toString(), key);
    }

    // 测试用例
    public static void main(String[] args) {
        try {
            String original = "Hello World!";
            String key = "12345678";

            System.out.println("原始字符串: " + original);
            System.out.println("密钥: " + key);

            // 测试字符串加密解密
            String encrypted = encryptStr(original, key);
            System.out.println("加密后: " + encrypted);

            String decrypted = decryptStr(encrypted, key);
            System.out.println("解密后: " + decrypted);

            // 测试十六进制加密解密
            String encryptedHex = encryptStrHex(original, key);
            System.out.println("十六进制加密: " + encryptedHex);

            String decryptedHex = decryptStrHex(encryptedHex, key);
            System.out.println("十六进制解密: " + decryptedHex);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
