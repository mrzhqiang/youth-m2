package youthm2.common;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Java版本的HUtil32工具类
 * 翻译自Pascal/Delphi的HUtil32.pas
 */
public class HUtil32 {

    // 常量定义
    public static final int MAXDEFCOLOR = 16;
    public static final int MAXSOCKETBUFFLEN = 8192;
    public static final int MAXLISTMARKER = 3;
    public static final int MAXPREDEFINE = 3;

    // 颜色名称映射
    private static final Map<String, Integer> COLOR_NAMES = new HashMap<>();

    static {
        COLOR_NAMES.put("BLACK", Color.BLACK.getRGB());
        COLOR_NAMES.put("BROWN", new Color(128, 0, 0).getRGB());
        COLOR_NAMES.put("MARGENTA", Color.MAGENTA.getRGB());
        COLOR_NAMES.put("GREEN", Color.GREEN.getRGB());
        COLOR_NAMES.put("LTGREEN", new Color(128, 128, 0).getRGB());
        COLOR_NAMES.put("BLUE", new Color(0, 0, 128).getRGB());
        COLOR_NAMES.put("LTBLUE", Color.BLUE.getRGB());
        COLOR_NAMES.put("PURPLE", new Color(128, 0, 128).getRGB());
        COLOR_NAMES.put("CYAN", new Color(0, 128, 128).getRGB());
        COLOR_NAMES.put("LTCYAN", Color.CYAN.getRGB());
        COLOR_NAMES.put("GRAY", Color.GRAY.getRGB());
        COLOR_NAMES.put("LTGRAY", Color.LIGHT_GRAY.getRGB());
        COLOR_NAMES.put("YELLOW", Color.YELLOW.getRGB());
        COLOR_NAMES.put("LIME", new Color(0, 255, 0).getRGB());
        COLOR_NAMES.put("WHITE", Color.WHITE.getRGB());
        COLOR_NAMES.put("RED", Color.RED.getRGB());
    }

    // 列表标记类型
    private static final Map<String, Integer> LIST_MARKER_NAMES = new HashMap<>();

    static {
        LIST_MARKER_NAMES.put("DISC", 0);
        LIST_MARKER_NAMES.put("CIRCLE", 1);
        LIST_MARKER_NAMES.put("SQUARE", 2);
    }

    // 预定义对齐方式
    private static final Map<String, Integer> PREDEFINE_NAMES = new HashMap<>();

    static {
        PREDEFINE_NAMES.put("LEFT", 0);
        PREDEFINE_NAMES.put("RIGHT", 1);
        PREDEFINE_NAMES.put("CENTER", 2);
    }

    /**
     * 矩形类
     */
    public static class LRect {
        public int left, top, right, bottom;

        public LRect(int l, int t, int r, int b) {
            this.left = l;
            this.top = t;
            this.right = r;
            this.bottom = b;
        }
    }

    /**
     * 安全填充字符
     */
    public static void safeFillChar(byte[] buffer, int count, byte value) {
        Arrays.fill(buffer, 0, Math.min(count, buffer.length), value);
    }

    /**
     * 获取邮件金币数量
     */
    public static int getEmailGold(int gold, boolean hasItem) {
        int result = 2000;
        if (gold > 0) {
            result += Math.round(gold * 0.1) + 1;
        }
        if (hasItem) {
            result += 20000;
        }
        return result;
    }

    /**
     * 获取商店折扣价格
     */
    public static int getShopAgio(int price, int agio) {
        if (price <= 0) return price;

        double discount = 1.0;
        switch (agio) {
            case 1:
                discount = 0.95;
                break;
            case 2:
                discount = 0.9;
                break;
            case 3:
                discount = 0.85;
                break;
            case 4:
                discount = 0.8;
                break;
            case 5:
                discount = 0.75;
                break;
            case 6:
                discount = 0.7;
                break;
            case 7:
                discount = 0.65;
                break;
            case 8:
                discount = 0.6;
                break;
            case 9:
                discount = 0.55;
                break;
            case 10:
                discount = 0.5;
                break;
            case 11:
                discount = 0.45;
                break;
            case 12:
                discount = 0.4;
                break;
            case 13:
                discount = 0.35;
                break;
            case 14:
                discount = 0.3;
                break;
            case 15:
                discount = 0.25;
                break;
            case 16:
                discount = 0.2;
                break;
            case 17:
                discount = 0.15;
                break;
            case 18:
                discount = 0.1;
                break;
            case 19:
                discount = 0.05;
                break;
        }

        int result = (int) Math.round(price * discount);
        return result <= 0 ? 1 : result;
    }

    /**
     * 检查字节状态
     */
    public static boolean checkByteStatus(byte value, int bitType) {
        if (value == 0) return false;
        return ((128 >> bitType) & value) != 0;
    }

    /**
     * 设置字节状态
     */
    public static byte setByteStatus(byte value, int bitType, boolean status) {
        if (status) {
            return (byte) ((128 >> bitType) | value);
        } else {
            return (byte) ((~(128 >> bitType)) & value);
        }
    }

    /**
     * 检查字状态
     */
    public static boolean checkWordStatus(short value, int bitType) {
        int valType = bitType % 8;
        byte lowByte = (byte) (value & 0xFF);
        byte highByte = (byte) ((value >> 8) & 0xFF);

        if ((bitType / 8) == 0) {
            return checkByteStatus(lowByte, valType);
        } else {
            return checkByteStatus(highByte, valType);
        }
    }

    /**
     * 设置字状态
     */
    public static short setWordStatus(short value, int bitType, boolean status) {
        int valType = bitType % 8;
        byte lowByte = (byte) (value & 0xFF);
        byte highByte = (byte) ((value >> 8) & 0xFF);

        if ((bitType / 8) == 0) {
            lowByte = setByteStatus(lowByte, valType, status);
        } else {
            highByte = setByteStatus(highByte, valType, status);
        }

        return (short) ((highByte << 8) | (lowByte & 0xFF));
    }

    /**
     * 检查整数状态
     */
    public static boolean checkIntStatus(int value, int bitType) {
        if (value == 0) return false;
        return ((0x80000000 >> bitType) & value) != 0;
    }

    /**
     * 设置整数状态
     */
    public static int setIntStatus(int value, int bitType, boolean status) {
        if (status) {
            return (0x80000000 >> bitType) | value;
        } else {
            return (~(0x80000000 >> bitType)) & value;
        }
    }

    /**
     * 16位颜色转RGB
     */
    public static void bit16ToRGB(short color, int[] rgb) {
        rgb[2] = ((color & 0x1F) << 3); // B
        rgb[1] = ((color & 0x7E0) >> 3); // G
        rgb[0] = ((color & 0xF800) >> 8); // R
    }

    /**
     * 24位RGB转16位
     */
    public static short bit24To16Bit(int r, int g, int b) {
        return (short) (((r & 0xF8) << 8) + ((g & 0xFC) << 3) + (b >> 3));
    }

    /**
     * 制作人类特征值
     */
    public static int makeHumanFeature(byte raceImg, byte dress, byte weapon, byte hair) {
        return ((hair & 0xFF) << 24) | ((dress & 0xFF) << 16) | ((weapon & 0xFF) << 8) | (raceImg & 0xFF);
    }

    /**
     * 制作怪物特征值
     */
    public static int makeMonsterFeature(byte raceImg, byte weapon, short appr) {
        return ((appr & 0xFFFF) << 16) | ((weapon & 0xFF) << 8) | (raceImg & 0xFF);
    }

    /**
     * 布尔值转整数字符串
     */
    public static String boolToIntStr(boolean value) {
        return value ? "1" : "0";
    }

    /**
     * 获取天数差
     */
    public static int getDayCount(LocalDate maxDate, LocalDate minDate) {
        long days = ChronoUnit.DAYS.between(minDate, maxDate);
        return days > 0 ? (int) days : 0;
    }

    /**
     * 获取编码消息大小
     */
    public static int getCodeMsgSize(double x) {
        return (int) x < x ? (int) x + 1 : (int) x;
    }

    /**
     * 整数转性别字符串
     */
    public static String intToSex(byte sex) {
        switch (sex) {
            case 0:
                return "男";
            case 1:
                return "女";
            default:
                return "未知";
        }
    }

    /**
     * 整数转职业字符串
     */
    public static String intToJob(byte job) {
        switch (job) {
            case 0:
                return "战士";
            case 1:
                return "魔法师";
            case 2:
                return "道士";
            case 10:
                return "刺客";
            case 11:
                return "弓箭手";
            case 12:
                return "龙纹剑士";
            case 20:
                return "元素法师";
            case 21:
                return "召唤法师";
            case 22:
                return "暗黑法师";
            case 30:
                return "牧师";
            case 31:
                return "刺杀者";
            case 32:
                return "圣战士";
            default:
                return "未知";
        }
    }

    /**
     * 检查是否为IP地址
     */
    public static boolean isIPAddr(String ip) {
        if (ip == null || ip.isEmpty()) return false;

        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;

        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 捕获字符串（提取有效字符串）
     */
    public static CaptureResult captureString(String source) {
        CaptureResult result = new CaptureResult();
        if (source == null || source.isEmpty()) {
            result.captured = "";
            result.remaining = "";
            return result;
        }

        int c = 0;
        int len = source.length();

        // 跳过前导空格
        while (c < len && source.charAt(c) == ' ') {
            c++;
        }

        if (c >= len) {
            result.captured = "";
            result.remaining = "";
            return result;
        }

        int start, end;

        // 如果以双引号开始
        if (source.charAt(c) == '"' && c < len - 1) {
            start = c + 1;
            end = len;

            // 查找结束的双引号
            for (int i = c + 1; i < len; i++) {
                if (source.charAt(i) == '"') {
                    end = i;
                    break;
                }
            }

            result.captured = source.substring(start, end);
            if (len >= end + 2) {
                result.remaining = source.substring(end + 2);
            } else {
                result.remaining = "";
            }
        } else {
            start = c;
            end = len;

            // 查找空格结束位置
            for (int i = c; i < len; i++) {
                if (source.charAt(i) == ' ') {
                    end = i;
                    break;
                }
            }

            result.captured = source.substring(start, end);
            if (len >= end + 1) {
                result.remaining = source.substring(end + 1);
            } else {
                result.remaining = "";
            }
        }

        return result;
    }

    /**
     * 捕获字符串结果类
     */
    public static class CaptureResult {
        public String captured;
        public String remaining;
    }

    /**
     * 获取空格数量
     */
    public static int getSpaceCount(String str) {
        if (str == null) return 0;

        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ' ') count++;
        }
        return count;
    }

    /**
     * 移除空格
     */
    public static String removeSpace(String str) {
        if (str == null) return "";
        return str.replaceAll(" ", "");
    }

    /**
     * 移除前导空格并返回移除的数量
     */
    public static RemoveSpaceResult killFirstSpace(String str) {
        RemoveSpaceResult result = new RemoveSpaceResult();
        if (str == null || str.isEmpty()) {
            result.result = str;
            result.removedCount = 0;
            return result;
        }

        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != ' ') {
                result.result = str.substring(i);
                result.removedCount = i;
                return result;
            }
        }

        result.result = "";
        result.removedCount = str.length();
        return result;
    }

    /**
     * 移除空格结果类
     */
    public static class RemoveSpaceResult {
        public String result;
        public int removedCount;
    }

    /**
     * 移除前后无用空格
     */
    public static String killGarbageSpace(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.trim();
    }

    /**
     * 获取第一个单词
     */
    public static FirstWordResult getFirstWord(String str) {
        FirstWordResult result = new FirstWordResult();
        if (str == null || str.isEmpty()) {
            result.word = "";
            result.remaining = "";
            result.frontSpace = 0;
            return result;
        }

        int frontSpace = 0;
        int len = str.length();

        // 计算前导空格
        for (int i = 0; i < len; i++) {
            if (str.charAt(i) == ' ') {
                frontSpace++;
            } else {
                break;
            }
        }

        // 提取第一个单词
        StringBuilder word = new StringBuilder();
        boolean inWord = false;

        for (int i = frontSpace; i < len; i++) {
            if (str.charAt(i) != ' ') {
                word.append(str.charAt(i));
                inWord = true;
            } else if (inWord) {
                result.word = word.toString();
                result.remaining = str.substring(i);
                result.frontSpace = frontSpace;
                return result;
            }
        }

        result.word = word.toString();
        result.remaining = "";
        result.frontSpace = frontSpace;
        return result;
    }

    /**
     * 第一个单词结果类
     */
    public static class FirstWordResult {
        public String word;
        public String remaining;
        public int frontSpace;
    }

    /**
     * 十六进制字符串转整数（带0x前缀）
     */
    public static int hexToIntEx(String hexStr) {
        if (hexStr == null || hexStr.length() < 2) return 0;
        return hexToInt(hexStr.substring(2));
    }

    /**
     * 十六进制字符串转整数
     */
    public static int hexToInt(String hexStr) {
        if (hexStr == null || hexStr.isEmpty()) return 0;

        try {
            return Integer.parseInt(hexStr, 16);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 字符串转日期
     */
    public static LocalDate strToDate(String str) {
        if (str == null || str.trim().isEmpty()) {
            return LocalDate.now();
        }

        try {
            return LocalDate.parse(str.trim());
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    /**
     * 字符串转时间
     */
    public static LocalTime strToTime(String str) {
        if (str == null || str.trim().isEmpty()) {
            return LocalTime.now();
        }

        try {
            return LocalTime.parse(str.trim());
        } catch (Exception e) {
            return LocalTime.now();
        }
    }

    /**
     * 字符串转浮点数
     */
    public static double strToFloat(String str) {
        if (str == null || str.isEmpty()) return 0.0;

        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * 提取文件名（不含扩展名）
     */
    public static String extractFileNameOnly(String fileName) {
        if (fileName == null || fileName.isEmpty()) return "";

        Path path = Paths.get(fileName);
        String name = path.getFileName().toString();
        int dotIndex = name.lastIndexOf('.');

        return dotIndex > 0 ? name.substring(0, dotIndex) : name;
    }

    /**
     * 浮点数转字符串
     */
    public static String floatToString(double value) {
        return floatToStrFixFmt(value, 5, 2);
    }

    /**
     * 浮点数转固定格式字符串
     */
    public static String floatToStrFixFmt(double value, int precision, int digits) {
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < precision; i++) {
            pattern.append('#');
        }
        pattern.append('.');
        for (int i = 0; i < digits; i++) {
            pattern.append('0');
        }

        DecimalFormat df = new DecimalFormat(pattern.toString());
        return df.format(value);
    }

    /**
     * 获取文件大小
     */
    public static long getFileSize(String fileName) {
        try {
            Path path = Paths.get(fileName);
            return Files.size(path);
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * 文件复制
     */
    public static boolean fileCopy(String source, String dest) {
        try {
            Path sourcePath = Paths.get(source);
            Path destPath = Paths.get(dest);
            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 根据名称获取预定义颜色
     */
    public static Color getDefColorByName(String name) {
        if (name == null) return Color.BLACK;

        Integer colorValue = COLOR_NAMES.get(name.toUpperCase());
        return colorValue != null ? new Color(colorValue) : Color.BLACK;
    }

    /**
     * 获取列表标记类型
     */
    public static int getULMarkerType(String name) {
        if (name == null) return 1;

        Integer markerType = LIST_MARKER_NAMES.get(name.toUpperCase());
        return markerType != null ? markerType : 1;
    }

    /**
     * 获取预定义值
     */
    public static int getDefines(String name) {
        if (name == null) return -1;

        Integer defineValue = PREDEFINE_NAMES.get(name.toUpperCase());
        return defineValue != null ? defineValue : -1;
    }

    /**
     * 获取有效字符串（根据分隔符）
     */
    public static ValidStrResult getValidStr(String str, char... dividers) {
        ValidStrResult result = new ValidStrResult();
        if (str == null || str.isEmpty()) {
            result.dest = "";
            result.remaining = str;
            return result;
        }

        Set<Character> dividerSet = new HashSet<>();
        for (char c : dividers) {
            dividerSet.add(c);
        }

        StringBuilder dest = new StringBuilder();
        int i = 1;

        for (char c : str.toCharArray()) {
            if (dividerSet.contains(c)) {
                result.dest = dest.toString();
                result.remaining = str.substring(i);
                return result;
            }
            dest.append(c);
            i++;
        }

        result.dest = dest.toString();
        result.remaining = "";
        return result;
    }

    /**
     * 有效字符串结果类
     */
    public static class ValidStrResult {
        public String dest;
        public String remaining;
    }

    /**
     * 字符串转坐标矩形
     */
    public static Rectangle getStrToCoords(String str) {
        if (str == null || str.isEmpty()) {
            return new Rectangle(0, 0, 0, 0);
        }

        String[] parts = str.split("[,\\s]+");
        int[] coords = new int[4];

        for (int i = 0; i < Math.min(parts.length, 4); i++) {
            try {
                coords[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                coords[i] = 0;
            }
        }

        return new Rectangle(coords[0], coords[1], coords[2], coords[3]);
    }

    /**
     * 组合目录和文件名
     */
    public static String combineDirFile(String dir, String fileName) {
        if (dir == null || dir.isEmpty()) {
            return fileName != null ? fileName : "";
        }
        if (fileName == null || fileName.isEmpty()) {
            return dir;
        }

        if (dir.endsWith("\\") || dir.endsWith("/")) {
            return dir + fileName;
        } else {
            return dir + File.separator + fileName;
        }
    }

    /**
     * 比较字符串前n个字符（忽略大小写）
     */
    public static boolean compareLStr(String src, String target, int count) {
        if (src == null || target == null) return false;
        if (count <= 0) return false;
        if (src.length() < count || target.length() < count) return false;

        return src.substring(0, count).equalsIgnoreCase(target.substring(0, count));
    }

    /**
     * 比较字符串后n个字符（忽略大小写）
     */
    public static boolean compareBackLStr(String src, String target, int count) {
        if (src == null || target == null) return false;
        if (count <= 0) return false;
        if (src.length() < count || target.length() < count) return false;

        String srcEnd = src.substring(src.length() - count);
        String targetEnd = target.substring(target.length() - count);
        return srcEnd.equalsIgnoreCase(targetEnd);
    }

    /**
     * 判断字符是否为英文字母
     */
    public static boolean isEnglish(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
    }

    /**
     * 判断字符是否为英文字母或数字
     */
    public static boolean isEngNumeric(char ch) {
        return isEnglish(ch) || (ch >= '0' && ch <= '9');
    }

    /**
     * 判断字符串是否为浮点数
     */
    public static boolean isFloatNumeric(String str) {
        if (str == null || str.trim().isEmpty()) return false;

        try {
            Double.parseDouble(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 替换字符
     */
    public static String replaceChar(String src, char srcChar, char repChar) {
        if (src == null) return "";
        return src.replace(srcChar, repChar);
    }

    /**
     * 判断字符串是否为纯数字
     */
    public static boolean isStringNumber(String str) {
        if (str == null || str.isEmpty()) return false;

        for (char c : str.toCharArray()) {
            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    /**
     * 判断是否为变量数字
     */
    public static boolean isVarNumber(String str) {
        if (str == null) return false;

        String upper = str.toUpperCase();
        return upper.startsWith("HUMAN") ||
                upper.startsWith("GUILD") ||
                upper.startsWith("GLOBAL");
    }

    /**
     * 整数转2位字符串（补零）
     */
    public static String intToStr2(int n) {
        return n < 10 ? "0" + n : String.valueOf(n);
    }

    /**
     * 整数转指定长度字符串（用指定字符填充）
     */
    public static String intToStrFill(int num, int length, char fillChar) {
        String str = String.valueOf(num);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length - str.length(); i++) {
            result.append(fillChar);
        }
        result.append(str);

        return result.toString();
    }

    /**
     * 判断点是否在矩形内
     */
    public static boolean isInRect(int x, int y, Rectangle rect) {
        return x >= rect.x && x <= rect.x + rect.width &&
                y >= rect.y && y <= rect.y + rect.height;
    }

    /**
     * 标签计数
     */
    public static int tagCount(String source, char tag) {
        if (source == null) return 0;

        int count = 0;
        for (char c : source.toCharArray()) {
            if (c == tag) count++;
        }
        return count;
    }

    /**
     * 获取最小值
     */
    public static int min(int n1, int n2) {
        return Math.min(n1, n2);
    }

    /**
     * 获取最大值
     */
    public static int max(int n1, int n2) {
        return Math.max(n1, n2);
    }

    /**
     * 布尔值转字符串
     */
    public static String boolToStr(boolean value) {
        return value ? "TRUE" : "FALSE";
    }

    /**
     * 布尔值转中文字符串
     */
    public static String booleanToStr(boolean value) {
        return value ? "是" : "否";
    }

    /**
     * 获取当前日期字符串（YYYYMMDD格式）
     */
    public static String getMonDay() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return now.format(formatter);
    }

    /**
     * 检查单字节字符
     */
    public static boolean checkOneByteChar(char ch) {
        return (ch >= '0' && ch <= '9') ||
                (ch >= 'A' && ch <= 'Z') ||
                (ch >= 'a' && ch <= 'z');
    }

    /**
     * 检查公司字符
     */
    public static boolean checkCorpsChr(String name) {
        if (name == null || name.isEmpty()) return true;

        for (char c : name.toCharArray()) {
            if (c > 127) {
                // 双字节字符处理（简化）
                continue;
            } else {
                if (!checkOneByteChar(c)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 数值单位转换（万、亿）
     */
    public static String intUnit(int n) {
        if (n > 9999 && n < 100000000) {
            return (n / 10000) + "万";
        }
        if (n > 99999999) {
            return (n / 100000000) + "亿";
        }
        return String.valueOf(n);
    }
}