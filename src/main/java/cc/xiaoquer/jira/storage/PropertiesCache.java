package cc.xiaoquer.jira.storage;

import cc.xiaoquer.jira.constant.FoldersConsts;

import java.io.*;
import java.util.Properties;

/**
 * Created by Nicholas on 2017/9/5.
 */
public class PropertiesCache {
//    public static final String OUTPUT_FOLDER = System.getProperty("user.dir") + File.separator + "JSCP_FILES" ;
//    public static final String CONFIG_FILE = System.getProperty("user.dir") + File.separator +"CONF" + File.separator + "jscp.properties";

    public static final String P_UPDATE_URL         = "autoupdate.url";
    public static final String P_BOARD_FILTER       = "boardfilter";
    public static final String P_HOST               = "host";
    public static final String P_USER               = "user";
    public static final String P_PWD                = "pwd";
    public static final String P_COLORFUL           = "colorful";
    public static final String P_SHOWALLCOLS        = "excelShowAllColumns";
    public static final String P_CUSTOM_FIELD_KEYS  = "excel.customfield.keys";
    public static final String P_CUSTOM_FIELD_NAMES = "excel.customfield.names";

    private static final String KEY = System.getProperty("java.version");

    private static final Properties configProp = new OrderedProperties();
    private static Properties baselineProp = new OrderedProperties();

    private PropertiesCache() {
    }

    static {
        configProp.put("JSCP_OWNER","nicholas.qu");
        configProp.put(P_BOARD_FILTER,"");
        configProp.put(P_HOST,"");
        configProp.put(P_USER,"");
        configProp.put(P_PWD,"");
        //New Features
        configProp.put(P_COLORFUL, "1");
        configProp.put(P_SHOWALLCOLS, "0");
        configProp.put(P_CUSTOM_FIELD_KEYS, "fields>customfield_11001");
        configProp.put(P_CUSTOM_FIELD_NAMES, "实际耗费时间");
        configProp.put(P_UPDATE_URL, "");
        read();
    }

    private static Properties read() {
        File configFile = new File(FoldersConsts.CONFIG_FILE);

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
            int hashCode1 = configProp.hashCode();
            Properties properties = new OrderedProperties();
            properties.load(reader);
            int hashCode2 = properties.hashCode();
            if (hashCode1 != hashCode2) {
                baselineProp.clear();
                configProp.putAll(properties);
                flush();
            }

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
        File configFile = new File(FoldersConsts.CONFIG_FILE);

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
        configProp.put(P_HOST, value);
    }

    public static void setUserName(String value) {
        configProp.put(P_USER, value);
    }

    public static void setPassword(String value) {
        try {
            configProp.put(P_PWD, encryption(value));
        } catch (UnsupportedEncodingException e) {
        }
    }

    public static void setBoardFilter(String value) {
        configProp.put(P_BOARD_FILTER, value);
    }

    public static String getHost() {
        return (String)configProp.get(P_HOST);
    }

    public static String getUserName() {
        return (String)configProp.get(P_USER);
    }

    public static String getPassword() {
        String pwd = (String)configProp.get(P_PWD);

        try {
            return decipher(pwd);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static String getBoardFilter() {
        return (String)configProp.get(P_BOARD_FILTER);
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

    public static String getProp(String key) {
        String value = System.getenv(key);
        if (value == null) {
            value = System.getProperty(key);
        }
        if (value == null) {
            value = (String) configProp.get(key);
        }
        return value;
    }

    public static String getUpdateUrl() {
        return getProp(P_UPDATE_URL);
    }
    public static void setProp(String key, String value) {
        configProp.put(key, value);
    }

}