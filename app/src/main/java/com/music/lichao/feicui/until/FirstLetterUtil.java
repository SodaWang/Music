package com.music.lichao.feicui.until;

/**
 * Created by Administrator on 2016/5/19.
 */
public class FirstLetterUtil {
    // 国标码和区位码转换常量
    static final int GB_SP_DIFF = 160;
    // 存放国标一级汉字不同读音的起始区位码
    static final int[] secPosvalueList = {1601, 1637, 1833, 2078, 2274, 2302,
            2433, 2594, 2787, 3106, 3212, 3472, 3635, 3722, 3730, 3858, 4027,
            4086, 4390, 4558, 4684, 4925, 5249, 5600};
    // 存放国标一级汉字不同读音的起始区位码对应读音
    static final char[] firstLetter = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
            'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'w', 'x',
            'y', 'z'};

    // 获取一个字符串的拼音码
    public static String getFirstLetter(String oriStr) {
        String str = oriStr.toLowerCase();
        StringBuffer buffer = new StringBuffer();
        char ch;
        char[] temp;
        for (int i = 0; i < str.length(); i++) { // 依次处理str中每个字符
            ch = str.charAt(i);
            temp = new char[]{ch};
            byte[] uniCode = new String(temp).getBytes();
            if (uniCode[0] < 128 && uniCode[0] > 0) { // 非汉字
                buffer.append(temp);
            } else {
                buffer.append(convert(uniCode));
            }
        }
        return buffer.toString();
    }

    /**
     * 获取字符串首字的首个大写拼音
     *
     * @param word 待检字符串
     * @return 首字的首字母大写拼音
     */
    public static String getFirstSpell(String word) {
        //首先判断是中文还是英文
        if (firstIsEnglish(word)) {
            return word.substring(0).toUpperCase();//英文
        } else {
            String str = getFirstLetter(word);
            return str.substring(0).toUpperCase();//中文，先进行拼音转码
        }

    }

    static char convert(byte[] bytes) {
        char result = '-';
        int secPosvalue = 0;
        int i;
        for (i = 0; i < bytes.length; i++) {
            bytes[i] -= GB_SP_DIFF;
        }
        secPosvalue = bytes[0] * 100 + bytes[1];
        for (i = 0; i < 23; i++) {
            if (secPosvalue >= secPosvalueList[i]
                    && secPosvalue < secPosvalueList[i + 1]) {
                result = firstLetter[i];
                break;
            }
        }
        return result;
    }

    /**
     * 判断字符串首字符是否全为英文
     *
     * @param word 待检字符串
     * @return false 中文 true 英文
     */
    private static boolean firstIsEnglish(String word) {
        boolean sign = true; // 初始化标志为为'true'

        if (!(word.charAt(0) >= 'A' && word.charAt(0) <= 'Z')
                && !(word.charAt(0) >= 'a' && word.charAt(0) <= 'z')) {
            return false;
        }

        return true;
    }
}
