package cc.xiaoquer.jira.storage;

import java.io.*;
import java.util.Properties;
import java.util.Set;

/**
 * Created by Nicholas on 2017/9/5.
 */
public class PropertiesCache {
    public static final String CONFIG_PATH = System.getProperty("user.dir") + File.separator + "JSCP_FILES" ;
    public static final String CONFIG_FILE_NAME = CONFIG_PATH + File.separator +"conf" + File.separator + "jscp.properties";
    private static final String KEY = System.getProperty("java.version");

    private static final Properties configProp = new Properties();
    private static Properties baselineProp = new Properties();

    private PropertiesCache() {
    }

    static {
        configProp.put("JSCP_OWNER","nicholas.qu");
        read();
    }

    private static Properties read() {
        File configFile = new File(CONFIG_FILE_NAME);

        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileReader reader = null;
        try {
            reader = new FileReader(configFile);
            configProp.load(reader);

            baselineProp.clear();
            baselineProp.putAll(configProp);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return configProp;
    }

    public static void flush() {
        if (baselineProp.hashCode() == configProp.hashCode()) {
            return;
        }

        System.out.println("FLUSH CACHE.");
        File configFile = new File(CONFIG_FILE_NAME);

        if (!configFile.exists()) {
            System.out.println("生成缓存文件..." + configFile.getAbsolutePath());
        }

        FileWriter writer = null;
        try {
            writer = new FileWriter(configFile);
            configProp.store(writer, "Jira Card Printer Cache");
            baselineProp.putAll(configProp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setHost(String value) {
        configProp.put("host", value);
    }

    public static void setUserName(String value) {
        configProp.put("user", value);
    }

    public static void setPassword(String value) {
        try {
            configProp.put("pwd", encryption(value));
        } catch (UnsupportedEncodingException e) {
        }
    }

    public static void setBoardFilter(String value) {
        configProp.put("boardfilter", value);
    }

    public static String getHost() {
        return (String)configProp.get("host");
    }

    public static String getUserName() {
        return (String)configProp.get("user");
    }

    public static String getPassword() {
        String pwd = (String)configProp.get("pwd");

        try {
            return decipher(pwd);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static String getBoardFilter() {
        return (String)configProp.get("boardfilter");
    }

    private static String encryption(String content) throws UnsupportedEncodingException {
        if (content == null) {
            return "";
        }

        byte[] contentBytes = content.getBytes();
        byte[] keyBytes = KEY.getBytes();

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
        return new String(result, "utf-8");
    }

    private static String decipher(String content) throws UnsupportedEncodingException {
        if (content == null) {
            return "";
        }
        byte[] contentBytes = content.getBytes();
        byte[] keyBytes = KEY.getBytes();

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
        return new String(result, "utf-8");
    }

}