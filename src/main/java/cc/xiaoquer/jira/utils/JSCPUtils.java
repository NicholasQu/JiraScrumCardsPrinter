package cc.xiaoquer.jira.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Nicholas on 2019/1/23.
 */
public class JSCPUtils {

    //从issueKey中提取最后的几位数字序号
    public static int getNumAtLast(String str) {
        if (StringUtils.isBlank(str)) return 0;

        int result = 0;

        int exp = 0;
        for (int i=str.length() - 1; i >= 0; i--) {
            char c = str.charAt(i);

            if (Character.isDigit(c)) {
                result += Character.digit(c,10) * Math.pow(10, exp);
            } else{
                break;
            }
            exp++;
        }

        return result;
    }

    public static void main(String[] args) {
        System.out.println(getNumAtLast("A"));
        System.out.println(getNumAtLast("A1"));
        System.out.println(getNumAtLast("A1BC"));
        System.out.println(getNumAtLast("A123"));
        System.out.println(getNumAtLast("A-123"));
    }
}
