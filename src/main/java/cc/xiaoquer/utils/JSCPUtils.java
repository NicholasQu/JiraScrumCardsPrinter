package cc.xiaoquer.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;

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

    public static String encrypt(String content, String key) {
        if (content == null) {
            return "";
        }

        byte[] contentBytes = content.getBytes();
        byte[] keyBytes = key.getBytes();

        byte dkey = 0;
        for(byte b : keyBytes){
            dkey ^= b;
        }

        byte salt = 0;  //随机盐值
        byte[] result = new byte[contentBytes.length];
        for(int i = 0 ; i < contentBytes.length; i++){
            salt = (byte)(contentBytes[i] ^ dkey ^ salt);
            result[i] = salt;
        }
        return Base64.encodeBase64String(result);
    }

    public static String decrypt(String content, String key) {
        if (content == null) {
            return "";
        }
        byte[] contentBytes = Base64.decodeBase64(content);
        byte[] keyBytes = key.getBytes();

        byte dkey = 0;
        for(byte b : keyBytes){
            dkey ^= b;
        }

        byte salt = 0;  //随机盐值
        byte[] result = new byte[contentBytes.length];
        for(int i = contentBytes.length - 1 ; i >= 0 ; i--){
            if(i == 0){
                salt = 0;
            }else{
                salt = contentBytes[i - 1];
            }
            result[i] = (byte)(contentBytes[i] ^ dkey ^ salt);
        }

        return new String(result);
    }

    public static void main(String[] args) {
        System.out.println(getNumAtLast("A"));
        System.out.println(getNumAtLast("A1"));
        System.out.println(getNumAtLast("A1BC"));
        System.out.println(getNumAtLast("A123"));
        System.out.println(getNumAtLast("A-123"));

        System.out.println(encrypt("曲健","1"));
        System.out.println(decrypt(encrypt("曲健","1"),"1"));
    }
}
