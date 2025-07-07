package youthm2.common;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.io.*;

/**
 * 快速ID记录类
 */
class QuickID {
    public String sAccount;
    public String sChrName;
    public int nIndex;
    public int nSelectID;
    
    public QuickID(String account, String chrName, int index, int selectID) {
        this.sAccount = account;
        this.sChrName = chrName;
        this.nIndex = index;
        this.nSelectID = selectID;
    }
}

/**
 * 快速列表类 - 线程安全的字符串列表
 */
class QuickList {
    private List<String> strings;
    private List<Object> objects;
    private ReentrantReadWriteLock lock;
    private boolean caseSensitive = false;
    private boolean sorted = false;
    
    public QuickList() {
        this.strings = new ArrayList<>();
        this.objects = new ArrayList<>();
        this.lock = new ReentrantReadWriteLock();
    }
    
    public void lock() {
        lock.writeLock().lock();
    }
    
    public void unlock() {
        lock.writeLock().unlock();
    }
    
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
    
    public boolean isSorted() {
        return sorted;
    }
    
    public void setSorted(boolean sorted) {
        this.sorted = sorted;
    }
    
    public int size() {
        return strings.size();
    }
    
    public String getString(int index) {
        return strings.get(index);
    }
    
    public Object getObject(int index) {
        return objects.get(index);
    }
    
    public void add(String str, Object obj) {
        strings.add(str);
        objects.add(obj);
    }
    
    public void insert(int index, String str, Object obj) {
        strings.add(index, str);
        objects.add(index, obj);
    }
    
    public void delete(int index) {
        strings.remove(index);
        objects.remove(index);
    }
    
    public int indexOf(String str) {
        for (int i = 0; i < strings.size(); i++) {
            if (compareText(str, strings.get(i)) == 0) {
                return i;
            }
        }
        return -1;
    }
    
    private int compareText(String s1, String s2) {
        if (caseSensitive) {
            return s1.compareTo(s2);
        } else {
            return s1.compareToIgnoreCase(s2);
        }
    }
    
    public void exchange(int index1, int index2) {
        Collections.swap(strings, index1, index2);
        Collections.swap(objects, index1, index2);
    }
    
    /**
     * 快速排序字符串
     */
    public void sortString(int nMin, int nMax) {
        if (size() > 0) {
            quickSort(nMin, nMax);
        }
    }
    
    private void quickSort(int nMin, int nMax) {
        while (true) {
            int ntMin = nMin;
            int ntMax = nMax;
            String pivot = strings.get((nMin + nMax) >> 1);
            
            while (true) {
                while (compareText(strings.get(ntMin), pivot) < 0) {
                    ntMin++;
                }
                while (compareText(strings.get(ntMax), pivot) > 0) {
                    ntMax--;
                }
                if (ntMin <= ntMax) {
                    exchange(ntMin, ntMax);
                    ntMin++;
                    ntMax--;
                }
                if (ntMin > ntMax) {
                    break;
                }
            }
            if (nMin < ntMax) {
                quickSort(nMin, ntMax);
            }
            nMin = ntMin;
            if (ntMin >= nMax) {
                break;
            }
        }
    }
    
    /**
     * 获取索引
     */
    public int getIndex(String sName) {
        if (size() == 0) {
            return -1;
        }
        
        if (size() == 1) {
            if (compareText(sName, strings.get(0)) == 0) {
                return 0;
            }
            return -1;
        }
        
        int nLow = 0;
        int nHigh = size() - 1;
        int nMed = (nHigh - nLow) / 2 + nLow;
        
        while (true) {
            if ((nHigh - nLow) == 1) {
                if (compareText(sName, strings.get(nHigh)) == 0) {
                    return nHigh;
                }
                if (compareText(sName, strings.get(nLow)) == 0) {
                    return nLow;
                }
                break;
            } else {
                int nCompareVal = compareText(sName, strings.get(nMed));
                if (nCompareVal > 0) {
                    nLow = nMed;
                    nMed = (nHigh - nLow) / 2 + nLow;
                    continue;
                }
                if (nCompareVal < 0) {
                    nHigh = nMed;
                    nMed = (nHigh - nLow) / 2 + nLow;
                    continue;
                }
                return nMed;
            }
        }
        return -1;
    }
    
    /**
     * 添加记录
     */
    public boolean addRecord(String sName, int nIndex) {
        if (size() == 0) {
            add(sName, nIndex);
            return true;
        }
        
        if (size() == 1) {
            int nMed = compareText(sName, strings.get(0));
            if (nMed > 0) {
                add(sName, nIndex);
            } else if (nMed < 0) {
                insert(0, sName, nIndex);
            } else {
                return false; // 已存在
            }
            return true;
        }
        
        int nLow = 0;
        int nHigh = size() - 1;
        int nMed = (nHigh - nLow) / 2 + nLow;
        
        while (true) {
            if ((nHigh - nLow) == 1) {
                int nMedVal = compareText(sName, strings.get(nHigh));
                if (nMedVal > 0) {
                    insert(nHigh + 1, sName, nIndex);
                    break;
                } else {
                    nMedVal = compareText(sName, strings.get(nLow));
                    if (nMedVal > 0) {
                        insert(nLow + 1, sName, nIndex);
                        break;
                    } else if (nMedVal < 0) {
                        insert(nLow, sName, nIndex);
                        break;
                    } else {
                        return false;
                    }
                }
            } else {
                int nCompareVal = compareText(sName, strings.get(nMed));
                if (nCompareVal > 0) {
                    nLow = nMed;
                    nMed = (nHigh - nLow) / 2 + nLow;
                    continue;
                }
                if (nCompareVal < 0) {
                    nHigh = nMed;
                    nMed = (nHigh - nLow) / 2 + nLow;
                    continue;
                }
                return false;
            }
        }
        return true;
    }
    
    /**
     * 删除记录
     */
    public boolean delRecord(int nIndex, String sChrName) {
        lock();
        try {
            int i = indexOf(sChrName);
            if (i > -1 && nIndex == (Integer) objects.get(i)) {
                delete(i);
                return true;
            }
            return false;
        } finally {
            unlock();
        }
    }
    
    /**
     * 删除记录（扩展版本）
     */
    public boolean delRecordEx(String sChrName) {
        lock();
        try {
            int i = indexOf(sChrName);
            if (i > -1) {
                delete(i);
                return true;
            }
            return false;
        } finally {
            unlock();
        }
    }
}

/**
 * 快速ID列表类
 */
class QuickIDList {
    private List<String> strings;
    private List<List<QuickID>> objects;
    
    public QuickIDList() {
        this.strings = new ArrayList<>();
        this.objects = new ArrayList<>();
    }
    
    public int size() {
        return strings.size();
    }
    
    public String getString(int index) {
        return strings.get(index);
    }
    
    public List<QuickID> getObject(int index) {
        return objects.get(index);
    }
    
    private int compareText(String s1, String s2) {
        return s1.compareToIgnoreCase(s2);
    }
    
    /**
     * 添加记录
     */
    public void addRecord(String sAccount, String sChrName, int nIndex, int nSelIndex) {
        QuickID quickID = new QuickID(sAccount, sChrName, nIndex, nSelIndex);
        
        if (size() == 0) {
            List<QuickID> chrList = new ArrayList<>();
            chrList.add(quickID);
            strings.add(sAccount);
            objects.add(chrList);
            return;
        }
        
        if (size() == 1) {
            int nMed = compareText(sAccount, strings.get(0));
            if (nMed > 0) {
                List<QuickID> chrList = new ArrayList<>();
                chrList.add(quickID);
                strings.add(sAccount);
                objects.add(chrList);
            } else if (nMed < 0) {
                List<QuickID> chrList = new ArrayList<>();
                chrList.add(quickID);
                strings.add(0, sAccount);
                objects.add(0, chrList);
            } else {
                objects.get(0).add(quickID);
            }
            return;
        }
        
        int nLow = 0;
        int nHigh = size() - 1;
        int nMed = (nHigh - nLow) / 2 + nLow;
        
        while (true) {
            if ((nHigh - nLow) == 1) {
                int n20 = compareText(sAccount, strings.get(nHigh));
                if (n20 > 0) {
                    List<QuickID> chrList = new ArrayList<>();
                    chrList.add(quickID);
                    strings.add(nHigh + 1, sAccount);
                    objects.add(nHigh + 1, chrList);
                    break;
                } else if (compareText(sAccount, strings.get(nHigh)) == 0) {
                    objects.get(nHigh).add(quickID);
                    break;
                } else {
                    n20 = compareText(sAccount, strings.get(nLow));
                    if (n20 > 0) {
                        List<QuickID> chrList = new ArrayList<>();
                        chrList.add(quickID);
                        strings.add(nLow + 1, sAccount);
                        objects.add(nLow + 1, chrList);
                        break;
                    } else if (n20 < 0) {
                        List<QuickID> chrList = new ArrayList<>();
                        chrList.add(quickID);
                        strings.add(nLow, sAccount);
                        objects.add(nLow, chrList);
                        break;
                    } else {
                        objects.get(nLow).add(quickID);
                        break;
                    }
                }
            } else {
                int n1C = compareText(sAccount, strings.get(nMed));
                if (n1C > 0) {
                    nLow = nMed;
                    nMed = (nHigh - nLow) / 2 + nLow;
                    continue;
                }
                if (n1C < 0) {
                    nHigh = nMed;
                    nMed = (nHigh - nLow) / 2 + nLow;
                    continue;
                }
                objects.get(nMed).add(quickID);
                break;
            }
        }
    }
    
    /**
     * 删除记录
     */
    public void delRecord(int nIndex, String sChrName) {
        if (nIndex >= size()) {
            return;
        }
        
        List<QuickID> chrList = objects.get(nIndex);
        for (int i = 0; i < chrList.size(); i++) {
            QuickID quickID = chrList.get(i);
            if (quickID.sChrName.equals(sChrName)) {
                chrList.remove(i);
                break;
            }
        }
        
        if (chrList.isEmpty()) {
            strings.remove(nIndex);
            objects.remove(nIndex);
        }
    }
    
    /**
     * 删除记录（扩展版本）
     */
    public void delRecordEx(int nIndex) {
        if (nIndex >= size()) {
            return;
        }
        strings.remove(nIndex);
        objects.remove(nIndex);
    }
    
    /**
     * 获取角色列表
     */
    public int getChrList(String sAccount, List<QuickID> chrNameList) {
        if (size() == 0) {
            return -1;
        }
        
        if (size() == 1) {
            if (compareText(sAccount, strings.get(0)) == 0) {
                chrNameList.addAll(objects.get(0));
                return 0;
            }
            return -1;
        }
        
        int nLow = 0;
        int nHigh = size() - 1;
        int nMed = (nHigh - nLow) / 2 + nLow;
        int n24 = -1;
        
        while (true) {
            if ((nHigh - nLow) == 1) {
                if (compareText(sAccount, strings.get(nHigh)) == 0) {
                    n24 = nHigh;
                }
                if (compareText(sAccount, strings.get(nLow)) == 0) {
                    n24 = nLow;
                }
                break;
            } else {
                int n20 = compareText(sAccount, strings.get(nMed));
                if (n20 > 0) {
                    nLow = nMed;
                    nMed = (nHigh - nLow) / 2 + nLow;
                    continue;
                }
                if (n20 < 0) {
                    nHigh = nMed;
                    nMed = (nHigh - nLow) / 2 + nLow;
                    continue;
                }
                n24 = nMed;
                break;
            }
        }
        
        if (n24 != -1) {
            chrNameList.addAll(objects.get(n24));
        }
        return n24;
    }
}

/**
 * 快速索引列表类
 */
class QuickIndexList {
    private List<String> strings;
    private List<List<String>> chrLists;
    private List<List<Integer>> indexLists;
    
    public QuickIndexList() {
        this.strings = new ArrayList<>();
        this.chrLists = new ArrayList<>();
        this.indexLists = new ArrayList<>();
    }
    
    public int size() {
        return strings.size();
    }
    
    private int compareText(String s1, String s2) {
        return s1.compareToIgnoreCase(s2);
    }
    
    /**
     * 添加记录
     */
    public void addRecord(String sChrName, String sDelChrName, int nIndex) {
        if (size() == 0) {
            List<String> chrList = new ArrayList<>();
            List<Integer> indexList = new ArrayList<>();
            chrList.add(sDelChrName);
            indexList.add(nIndex);
            strings.add(sChrName);
            chrLists.add(chrList);
            indexLists.add(indexList);
            return;
        }
        
        // 类似的二分查找插入逻辑...
        // 为了简化，这里使用简单的查找和插入
        int index = getIndex(sChrName);
        if (index != -1) {
            chrLists.get(index).add(sDelChrName);
            indexLists.get(index).add(nIndex);
        } else {
            List<String> chrList = new ArrayList<>();
            List<Integer> indexList = new ArrayList<>();
            chrList.add(sDelChrName);
            indexList.add(nIndex);
            strings.add(sChrName);
            chrLists.add(chrList);
            indexLists.add(indexList);
        }
    }
    
    /**
     * 删除记录
     */
    public void delRecord(int nIndex) {
        if (nIndex >= size()) {
            return;
        }
        strings.remove(nIndex);
        chrLists.remove(nIndex);
        indexLists.remove(nIndex);
    }
    
    /**
     * 获取索引
     */
    public int getIndex(String sChrName) {
        for (int i = 0; i < strings.size(); i++) {
            if (compareText(sChrName, strings.get(i)) == 0) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 从文件加载
     */
    public void loadFromFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith(";")) {
                    String[] parts = line.split("[\t ]");
                    if (parts.length >= 3) {
                        String sChrName = parts[0];
                        String sDelName = parts[1];
                        int nDelID = Integer.parseInt(parts[2]);
                        if (nDelID > 0) {
                            addRecord(sChrName, sDelName, nDelID);
                        }
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 保存到文件
     */
    public void saveToFile(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (int i = 0; i < size(); i++) {
                List<String> chrList = chrLists.get(i);
                List<Integer> indexList = indexLists.get(i);
                for (int j = 0; j < chrList.size(); j++) {
                    writer.write(strings.get(i) + "\t" + chrList.get(j) + "\t" + indexList.get(j));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * 快速整数列表类
 */
class QuickIntegerList {
    private List<Integer> list;
    
    public QuickIntegerList() {
        this.list = new ArrayList<>();
    }
    
    public int size() {
        return list.size();
    }
    
    public Integer get(int index) {
        return list.get(index);
    }
    
    /**
     * 添加整数
     */
    public boolean addInteger(int nIndex, boolean boAdd) {
        if (size() == 0) {
            list.add(nIndex);
            return false;
        }
        
        // 使用二分查找插入
        int low = 0;
        int high = size() - 1;
        
        while (low <= high) {
            int mid = (low + high) / 2;
            int midVal = list.get(mid);
            
            if (midVal < nIndex) {
                low = mid + 1;
            } else if (midVal > nIndex) {
                high = mid - 1;
            } else {
                // 找到相同的值
                if (boAdd) {
                    list.add(mid + 1, nIndex);
                }
                return true;
            }
        }
        
        // 未找到，插入到适当位置
        list.add(low, nIndex);
        return false;
    }
}

/**
 * 快速字符串列表类
 */
class QuickStringList {
    private List<String> strings;
    private List<Object> objects;
    
    public QuickStringList() {
        this.strings = new ArrayList<>();
        this.objects = new ArrayList<>();
    }
    
    public int size() {
        return strings.size();
    }
    
    private int compareText(String s1, String s2) {
        return s1.compareToIgnoreCase(s2);
    }
    
    /**
     * 添加字符串
     */
    public boolean addString(String nIndex, Object item, boolean boAdd) {
        if (size() == 0) {
            strings.add(nIndex);
            objects.add(item);
            return false;
        }
        
        // 简化版本的插入逻辑
        int index = Collections.binarySearch(strings, nIndex, String.CASE_INSENSITIVE_ORDER);
        if (index >= 0) {
            // 找到相同的字符串
            if (boAdd) {
                strings.add(index + 1, nIndex);
                objects.add(index + 1, item);
            }
            return true;
        } else {
            // 未找到，插入到适当位置
            int insertIndex = -(index + 1);
            strings.add(insertIndex, nIndex);
            objects.add(insertIndex, item);
            return false;
        }
    }
}

/**
 * 快速字符串指针列表类
 */
class QuickStringPointerList {
    private List<String> strings;
    private List<Object> pointers;
    
    public QuickStringPointerList() {
        this.strings = new ArrayList<>();
        this.pointers = new ArrayList<>();
    }
    
    public int size() {
        return strings.size();
    }
    
    private int compareText(String s1, String s2) {
        return s1.compareToIgnoreCase(s2);
    }
    
    /**
     * 添加字符串
     */
    public void addString(String nIndex, Object item) {
        int index = Collections.binarySearch(strings, nIndex, String.CASE_INSENSITIVE_ORDER);
        if (index < 0) {
            index = -(index + 1);
        } else {
            index++; // 如果找到相同的，插入到后面
        }
        strings.add(index, nIndex);
        pointers.add(index, item);
    }
    
    /**
     * 排序字符串
     */
    public void sortString(int nMin, int nMax) {
        // Java的Collections.sort已经是高效的排序算法
        List<String> subStrings = strings.subList(nMin, nMax + 1);
        List<Object> subPointers = pointers.subList(nMin, nMax + 1);
        
        // 创建索引数组进行排序
        Integer[] indices = new Integer[subStrings.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        
        Arrays.sort(indices, (a, b) -> subStrings.get(a).compareToIgnoreCase(subStrings.get(b)));
        
        // 重新排列
        List<String> sortedStrings = new ArrayList<>();
        List<Object> sortedPointers = new ArrayList<>();
        for (int index : indices) {
            sortedStrings.add(subStrings.get(index));
            sortedPointers.add(subPointers.get(index));
        }
        
        // 替换原来的子列表
        for (int i = 0; i < sortedStrings.size(); i++) {
            subStrings.set(i, sortedStrings.get(i));
            subPointers.set(i, sortedPointers.get(i));
        }
    }
}

/**
 * 快速字符串添加列表类
 */
class QuickStringAddList {
    private List<String> strings;
    
    public QuickStringAddList() {
        this.strings = new ArrayList<>();
    }
    
    public int size() {
        return strings.size();
    }
    
    /**
     * 添加字符串
     */
    public boolean addString(String nIndex, boolean boAdd) {
        int index = Collections.binarySearch(strings, nIndex, String.CASE_INSENSITIVE_ORDER);
        if (index >= 0) {
            return true; // 已存在
        } else {
            if (boAdd) {
                strings.add(-(index + 1), nIndex);
            }
            return false;
        }
    }
}

/**
 * 快速列表列表类
 */
class QuickListList {
    private List<String> strings;
    private List<Object> objects;
    
    public QuickListList() {
        this.strings = new ArrayList<>();
        this.objects = new ArrayList<>();
    }
    
    /**
     * 添加字符串
     */
    public void addString(String nIndex, Object item) {
        strings.add(nIndex);
        objects.add(item);
    }
}

/**
 * 快速点列表类
 */
class QuickPointList {
    private List<Integer> indices;
    private List<Object> pointers;
    
    public QuickPointList() {
        this.indices = new ArrayList<>();
        this.pointers = new ArrayList<>();
    }
    
    public int size() {
        return indices.size();
    }
    
    public void clear() {
        indices.clear();
        pointers.clear();
    }
    
    public void clearPointer() {
        pointers.clear();
    }
    
    public void deleteEx(int index) {
        if (index >= 0 && index < size()) {
            indices.remove(index);
            pointers.remove(index);
        }
    }
    
    public Object getPointer(int nIndex) {
        if (nIndex >= 0 && nIndex < pointers.size()) {
            return pointers.get(nIndex);
        }
        return null;
    }
    
    /**
     * 添加指针
     */
    public Object addPointer(int nIndex, Object item, boolean boAdd) {
        if (size() == 0) {
            indices.add(nIndex);
            pointers.add(item);
            return null;
        }
        
        // 使用二分查找
        int low = 0;
        int high = size() - 1;
        
        while (low <= high) {
            int mid = (low + high) / 2;
            int midVal = indices.get(mid);
            
            if (midVal < nIndex) {
                low = mid + 1;
            } else if (midVal > nIndex) {
                high = mid - 1;
            } else {
                // 找到相同的索引
                Object result = pointers.get(mid);
                if (boAdd) {
                    indices.add(mid + 1, nIndex);
                    pointers.add(mid + 1, item);
                }
                return result;
            }
        }
        
        // 未找到，插入到适当位置
        indices.add(low, nIndex);
        pointers.add(low, item);
        return null;
    }
}

/**
 * 主工具类
 */
public class MudUtil {
    // 可以添加一些静态工具方法
    
    /**
     * 获取有效字符串（模拟Pascal中的GetValidStr3函数）
     */
    public static String getValidStr3(String source, String[] result, char[] separators) {
        if (source == null || source.isEmpty()) {
            return "";
        }
        
        // 简化版本的字符串分割
        String[] parts = source.split("[\t ]", 2);
        if (parts.length > 0 && result.length > 0) {
            result[0] = parts[0];
            if (parts.length > 1) {
                return parts[1];
            }
        }
        return "";
    }
}