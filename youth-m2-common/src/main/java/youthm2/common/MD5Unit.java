package youthm2.common;

import java.io.*;
import java.util.Arrays;

/**
 * MD5Unit - MD5哈希算法和CRC32计算的Java实现
 * 从Pascal代码翻译而来
 */
public class MD5Unit {
    
    // CRC32常量
    private static final int CRCPOLY = 0xEDB88320;
    
    // MD5填充字节
    private static final byte[] PADDING = {
        (byte)0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    
    // CRC32查找表
    private static final int[] CRCTable = new int[256];
    
    // 进度回调接口
    public interface ProgressCallback {
        void onProgress(int percent);
    }
    
    // MD5上下文类
    private static class MD5Context {
        int[] state = new int[4];      // MD5State
        long[] count = new long[2];    // MD5Count
        byte[] buffer = new byte[64];  // MD5Buffer
    }
    
    // MD5辅助函数F
    private static int F(int x, int y, int z) {
        return (x & y) | ((~x) & z);
    }
    
    // MD5辅助函数G
    private static int G(int x, int y, int z) {
        return (x & z) | (y & (~z));
    }
    
    // MD5辅助函数H
    private static int H(int x, int y, int z) {
        return x ^ y ^ z;
    }
    
    // MD5辅助函数I
    private static int I(int x, int y, int z) {
        return y ^ (x | (~z));
    }
    
    // 循环左移
    private static int rotateLeft(int x, int n) {
        return (x << n) | (x >>> (32 - n));
    }
    
    // FF变换
    private static void FF(int[] a, int b, int c, int d, int x, int s, int ac) {
        a[0] += F(b, c, d) + x + ac;
        a[0] = rotateLeft(a[0], s);
        a[0] += b;
    }
    
    // GG变换
    private static void GG(int[] a, int b, int c, int d, int x, int s, int ac) {
        a[0] += G(b, c, d) + x + ac;
        a[0] = rotateLeft(a[0], s);
        a[0] += b;
    }
    
    // HH变换
    private static void HH(int[] a, int b, int c, int d, int x, int s, int ac) {
        a[0] += H(b, c, d) + x + ac;
        a[0] = rotateLeft(a[0], s);
        a[0] += b;
    }
    
    // II变换
    private static void II(int[] a, int b, int c, int d, int x, int s, int ac) {
        a[0] += I(b, c, d) + x + ac;
        a[0] = rotateLeft(a[0], s);
        a[0] += b;
    }
    
    // 编码：将字节数组转换为int数组
    private static void encode(byte[] source, int[] target, int count) {
        int j = 0;
        for (int i = 0; i < count / 4; i++) {
            target[i] = (source[j] & 0xff) |
                       ((source[j + 1] & 0xff) << 8) |
                       ((source[j + 2] & 0xff) << 16) |
                       ((source[j + 3] & 0xff) << 24);
            j += 4;
        }
    }
    
    // 解码：将int数组转换为字节数组
    private static void decode(int[] source, byte[] target, int count) {
        int j = 0;
        for (int i = 0; i < count; i++) {
            target[j] = (byte) (source[i] & 0xff);
            target[j + 1] = (byte) ((source[i] >>> 8) & 0xff);
            target[j + 2] = (byte) ((source[i] >>> 16) & 0xff);
            target[j + 3] = (byte) ((source[i] >>> 24) & 0xff);
            j += 4;
        }
    }
    
    // MD5变换核心算法
    private static void transform(byte[] buffer, int[] state) {
        int[] block = new int[16];
        encode(buffer, block, 64);
        
        int a = state[0];
        int b = state[1]; 
        int c = state[2];
        int d = state[3];
        
        // 第一轮
        FF(new int[]{a}, b, c, d, block[0], 7, 0xd76aa478); a = new int[]{a}[0];
        FF(new int[]{d}, a, b, c, block[1], 12, 0xe8c7b756); d = new int[]{d}[0];
        FF(new int[]{c}, d, a, b, block[2], 17, 0x242070db); c = new int[]{c}[0];
        FF(new int[]{b}, c, d, a, block[3], 22, 0xc1bdceee); b = new int[]{b}[0];
        FF(new int[]{a}, b, c, d, block[4], 7, 0xf57c0faf); a = new int[]{a}[0];
        FF(new int[]{d}, a, b, c, block[5], 12, 0x4787c62a); d = new int[]{d}[0];
        FF(new int[]{c}, d, a, b, block[6], 17, 0xa8304613); c = new int[]{c}[0];
        FF(new int[]{b}, c, d, a, block[7], 22, 0xfd469501); b = new int[]{b}[0];
        FF(new int[]{a}, b, c, d, block[8], 7, 0x698098d8); a = new int[]{a}[0];
        FF(new int[]{d}, a, b, c, block[9], 12, 0x8b44f7af); d = new int[]{d}[0];
        FF(new int[]{c}, d, a, b, block[10], 17, 0xffff5bb1); c = new int[]{c}[0];
        FF(new int[]{b}, c, d, a, block[11], 22, 0x895cd7be); b = new int[]{b}[0];
        FF(new int[]{a}, b, c, d, block[12], 7, 0x6b901122); a = new int[]{a}[0];
        FF(new int[]{d}, a, b, c, block[13], 12, 0xfd987193); d = new int[]{d}[0];
        FF(new int[]{c}, d, a, b, block[14], 17, 0xa679438e); c = new int[]{c}[0];
        FF(new int[]{b}, c, d, a, block[15], 22, 0x49b40821); b = new int[]{b}[0];
        
        // 第二轮
        GG(new int[]{a}, b, c, d, block[1], 5, 0xf61e2562); a = new int[]{a}[0];
        GG(new int[]{d}, a, b, c, block[6], 9, 0xc040b340); d = new int[]{d}[0];
        GG(new int[]{c}, d, a, b, block[11], 14, 0x265e5a51); c = new int[]{c}[0];
        GG(new int[]{b}, c, d, a, block[0], 20, 0xe9b6c7aa); b = new int[]{b}[0];
        GG(new int[]{a}, b, c, d, block[5], 5, 0xd62f105d); a = new int[]{a}[0];
        GG(new int[]{d}, a, b, c, block[10], 9, 0x02441453); d = new int[]{d}[0];
        GG(new int[]{c}, d, a, b, block[15], 14, 0xd8a1e681); c = new int[]{c}[0];
        GG(new int[]{b}, c, d, a, block[4], 20, 0xe7d3fbc8); b = new int[]{b}[0];
        GG(new int[]{a}, b, c, d, block[9], 5, 0x21e1cde6); a = new int[]{a}[0];
        GG(new int[]{d}, a, b, c, block[14], 9, 0xc33707d6); d = new int[]{d}[0];
        GG(new int[]{c}, d, a, b, block[3], 14, 0xf4d50d87); c = new int[]{c}[0];
        GG(new int[]{b}, c, d, a, block[8], 20, 0x455a14ed); b = new int[]{b}[0];
        GG(new int[]{a}, b, c, d, block[13], 5, 0xa9e3e905); a = new int[]{a}[0];
        GG(new int[]{d}, a, b, c, block[2], 9, 0xfcefa3f8); d = new int[]{d}[0];
        GG(new int[]{c}, d, a, b, block[7], 14, 0x676f02d9); c = new int[]{c}[0];
        GG(new int[]{b}, c, d, a, block[12], 20, 0x8d2a4c8a); b = new int[]{b}[0];
        
        // 第三轮
        HH(new int[]{a}, b, c, d, block[5], 4, 0xfffa3942); a = new int[]{a}[0];
        HH(new int[]{d}, a, b, c, block[8], 11, 0x8771f681); d = new int[]{d}[0];
        HH(new int[]{c}, d, a, b, block[11], 16, 0x6d9d6122); c = new int[]{c}[0];
        HH(new int[]{b}, c, d, a, block[14], 23, 0xfde5380c); b = new int[]{b}[0];
        HH(new int[]{a}, b, c, d, block[1], 4, 0xa4beea44); a = new int[]{a}[0];
        HH(new int[]{d}, a, b, c, block[4], 11, 0x4bdecfa9); d = new int[]{d}[0];
        HH(new int[]{c}, d, a, b, block[7], 16, 0xf6bb4b60); c = new int[]{c}[0];
        HH(new int[]{b}, c, d, a, block[10], 23, 0xbebfbc70); b = new int[]{b}[0];
        HH(new int[]{a}, b, c, d, block[13], 4, 0x289b7ec6); a = new int[]{a}[0];
        HH(new int[]{d}, a, b, c, block[0], 11, 0xeaa127fa); d = new int[]{d}[0];
        HH(new int[]{c}, d, a, b, block[3], 16, 0xd4ef3085); c = new int[]{c}[0];
        HH(new int[]{b}, c, d, a, block[6], 23, 0x04881d05); b = new int[]{b}[0];
        HH(new int[]{a}, b, c, d, block[9], 4, 0xd9d4d039); a = new int[]{a}[0];
        HH(new int[]{d}, a, b, c, block[12], 11, 0xe6db99e5); d = new int[]{d}[0];
        HH(new int[]{c}, d, a, b, block[15], 16, 0x1fa27cf8); c = new int[]{c}[0];
        HH(new int[]{b}, c, d, a, block[2], 23, 0xc4ac5665); b = new int[]{b}[0];
        
        // 第四轮
        II(new int[]{a}, b, c, d, block[0], 6, 0xf4292244); a = new int[]{a}[0];
        II(new int[]{d}, a, b, c, block[7], 10, 0x432aff97); d = new int[]{d}[0];
        II(new int[]{c}, d, a, b, block[14], 15, 0xab9423a7); c = new int[]{c}[0];
        II(new int[]{b}, c, d, a, block[5], 21, 0xfc93a039); b = new int[]{b}[0];
        II(new int[]{a}, b, c, d, block[12], 6, 0x655b59c3); a = new int[]{a}[0];
        II(new int[]{d}, a, b, c, block[3], 10, 0x8f0ccc92); d = new int[]{d}[0];
        II(new int[]{c}, d, a, b, block[10], 15, 0xffeff47d); c = new int[]{c}[0];
        II(new int[]{b}, c, d, a, block[1], 21, 0x85845dd1); b = new int[]{b}[0];
        II(new int[]{a}, b, c, d, block[8], 6, 0x6fa87e4f); a = new int[]{a}[0];
        II(new int[]{d}, a, b, c, block[15], 10, 0xfe2ce6e0); d = new int[]{d}[0];
        II(new int[]{c}, d, a, b, block[6], 15, 0xa3014314); c = new int[]{c}[0];
        II(new int[]{b}, c, d, a, block[13], 21, 0x4e0811a1); b = new int[]{b}[0];
        II(new int[]{a}, b, c, d, block[4], 6, 0xf7537e82); a = new int[]{a}[0];
        II(new int[]{d}, a, b, c, block[11], 10, 0xbd3af235); d = new int[]{d}[0];
        II(new int[]{c}, d, a, b, block[2], 15, 0x2ad7d2bb); c = new int[]{c}[0];
        II(new int[]{b}, c, d, a, block[9], 21, 0xeb86d391); b = new int[]{b}[0];
        
        state[0] += a;
        state[1] += b;
        state[2] += c;
        state[3] += d;
    }
    
    // 初始化MD5上下文
    private static void MD5Init(MD5Context context) {
        context.state[0] = 0x67452301;
        context.state[1] = 0xefcdab89;
        context.state[2] = 0x98badcfe;
        context.state[3] = 0x10325476;
        context.count[0] = 0;
        context.count[1] = 0;
        Arrays.fill(context.buffer, (byte) 0);
    }
    
    // 更新MD5上下文
    private static void MD5Update(MD5Context context, byte[] input, int length) {
        int index = (int) ((context.count[0] >>> 3) & 0x3F);
        
        context.count[0] += (length << 3);
        if (context.count[0] < (length << 3)) {
            context.count[1]++;
        }
        context.count[1] += (length >>> 29);
        
        int partLen = 64 - index;
        int i;
        
        if (length >= partLen) {
            System.arraycopy(input, 0, context.buffer, index, partLen);
            transform(context.buffer, context.state);
            
            for (i = partLen; i + 63 < length; i += 64) {
                byte[] temp = new byte[64];
                System.arraycopy(input, i, temp, 0, 64);
                transform(temp, context.state);
            }
            index = 0;
        } else {
            i = 0;
        }
        
        System.arraycopy(input, i, context.buffer, index, length - i);
    }
    
    // 完成MD5计算并获取结果
    private static void MD5Result(MD5Context context, byte[] digest) {
        byte[] bits = new byte[8];
        decode(new int[]{(int) context.count[0], (int) context.count[1]}, bits, 2);
        
        int index = (int) ((context.count[0] >>> 3) & 0x3F);
        int padLen = (index < 56) ? (56 - index) : (120 - index);
        
        MD5Update(context, PADDING, padLen);
        MD5Update(context, bits, 8);
        
        decode(context.state, digest, 4);
        
        // 清空上下文
        Arrays.fill(context.state, 0);
        Arrays.fill(context.count, 0);
        Arrays.fill(context.buffer, (byte) 0);
    }
    
    /**
     * 获取字符串的MD5哈希值（32位十六进制字符串）
     */
    public static String getMD5Text(String input) {
        MD5Context context = new MD5Context();
        byte[] digest = new byte[16];
        
        MD5Init(context);
        byte[] inputBytes = input.getBytes();
        MD5Update(context, inputBytes, inputBytes.length);
        MD5Result(context, digest);
        
        return bytesToHex(digest);
    }
    
    /**
     * 获取字符串的MD5哈希值（16位十六进制字符串，取中间8个字节）
     */
    public static String getMD5TextOf16(String input) {
        MD5Context context = new MD5Context();
        byte[] digest = new byte[16];
        
        MD5Init(context);
        byte[] inputBytes = input.getBytes();
        MD5Update(context, inputBytes, inputBytes.length);
        MD5Result(context, digest);
        
        StringBuilder result = new StringBuilder();
        for (int i = 4; i <= 11; i++) {
            result.append(String.format("%02x", digest[i] & 0xff));
        }
        return result.toString();
    }
    
    /**
     * 获取字节缓冲区的MD5哈希值
     */
    public static String getMD5TextByBuffer(byte[] buffer, int buffLen) {
        final int BufSize = 16384;
        long totalSize = buffLen;
        long curSize = 0;
        MD5Context context = new MD5Context();
        byte[] digest = new byte[16];
        
        MD5Init(context);
        while (curSize < totalSize) {
            int size = (curSize + BufSize <= totalSize) ? BufSize : (int) (totalSize - curSize);
            byte[] temp = new byte[size];
            System.arraycopy(buffer, (int) curSize, temp, 0, size);
            MD5Update(context, temp, size);
            curSize += size;
        }
        MD5Result(context, digest);
        
        return bytesToHex(digest);
    }
    
    /**
     * 构建CRC32查找表
     */
    private static void buildCRCTable() {
        for (int i = 0; i < 256; i++) {
            int r = i << 1;
            for (int j = 8; j >= 0; j--) {
                if ((r & 1) != 0) {
                    r = (r >>> 1) ^ CRCPOLY;
                } else {
                    r = r >>> 1;
                }
            }
            CRCTable[i] = r;
        }
    }
    
    /**
     * 重新计算CRC32值
     */
    private static int recountCRC(byte b, int crcOld) {
        return CRCTable[(crcOld ^ (b & 0xff)) & 0xff] ^ ((crcOld >>> 8) & 0x00FFFFFF);
    }
    
    /**
     * 获取文件的CRC32校验值
     */
    public static int getCRC32(String fileName) {
        buildCRCTable();
        int crc = 0xFFFFFFFF;
        
        try (FileInputStream fis = new FileInputStream(fileName)) {
            byte[] buffer = new byte[256];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    crc = recountCRC(buffer[i], crc);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        
        return ~crc;
    }
    
    /**
     * 获取文件的MD5哈希值（字符串形式）
     */
    public static String fileToMD5Text(String fileName) {
        return bytesToHex(getMD5OfFile(fileName, null));
    }
    
    /**
     * 将MD5摘要转换为十六进制字符串
     */
    private static String bytesToHex(byte[] digest) {
        StringBuilder result = new StringBuilder();
        for (byte b : digest) {
            result.append(String.format("%02x", b & 0xff));
        }
        return result.toString();
    }
    
    /**
     * 获取文件的MD5摘要（带进度回调）
     */
    public static byte[] getMD5OfFile(String fileName, ProgressCallback progress) {
        return getMD5OfFile(fileName, progress, 0);
    }
    
    /**
     * 获取文件的MD5摘要（带进度回调和偏移量）
     */
    public static byte[] getMD5OfFile(String fileName, ProgressCallback progress, int offset) {
        final int BufSize = 16384;
        byte[] result = new byte[16];
        
        File file = new File(fileName);
        if (!file.exists()) {
            return result;
        }
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[BufSize];
            long totalSize = file.length() + offset;
            long curSize = 0;
            MD5Context context = new MD5Context();
            
            MD5Init(context);
            if (progress != null) {
                progress.onProgress(0);
            }
            
            int size;
            while (curSize < totalSize) {
                int bytesToRead = (curSize + BufSize <= totalSize) ? BufSize : (int) (totalSize - curSize);
                Arrays.fill(buffer, (byte) 0);
                size = fis.read(buffer, 0, bytesToRead);
                if (size == -1) break;
                
                curSize += size;
                MD5Update(context, buffer, size);
                
                if (progress != null) {
                    progress.onProgress((int) Math.round((curSize * 100.0) / totalSize));
                }
            }
            
            MD5Result(context, result);
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (progress != null) {
                progress.onProgress(0);
            }
        }
        
        return result;
    }
}