package cc.xiaoquer.jira.storage;

import cc.xiaoquer.jira.constant.FoldersConsts;
import cc.xiaoquer.utils.JSCPUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
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
    public static final String P_JIRA_CUSTOM_FIELD_KEYS  = "jira.customfield.keys";
    public static final String P_JIRA_CUSTOM_FIELD_NAMES = "jira.customfield.names";

    public static final String P_ISSUE_STATUS_CATEGORY_3KEYS = "issue.status.category.3keys";


    public static final String P_SMB_DOMAIN = "smb.domain";
    public static final String P_SMB_USER = "smb.login.user";
    public static final String P_SMB_PASSWORD = "smb.login.password";
    public static final String P_SMB_LOGIN_DOMAIN = "smb.login.domain";
    public static final String P_SMB_SHARE_ROOT = "smb.share.root";
    public static final String P_SMB_SHARE_PATH = "smb.share.path";
    public static final String P_WEBSERVER_URL = "webserver.url";

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
        configProp.put(P_CUSTOM_FIELD_KEYS, "customfield_11400");
        configProp.put(P_CUSTOM_FIELD_NAMES, "epicPriority");
        configProp.put(P_UPDATE_URL, "");

        configProp.put(P_ISSUE_STATUS_CATEGORY_3KEYS, "new,indeterminate,done");
        read();
    }

    private static Properties read() {
        File configFile = new File(FoldersConsts.CONFIG_FILE);

        if (configFile == null) {
            return configProp;
        }

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
        configProp.put(P_PWD, JSCPUtils.encrypt(value, KEY));
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
        return JSCPUtils.decrypt(pwd, KEY);
    }

    public static String getBoardFilter() {
        return (String)configProp.get(P_BOARD_FILTER);
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

    public static Map<String, String> getJiraCustomFields() {
        String customFieldKeyStr = (String)configProp.get(P_JIRA_CUSTOM_FIELD_KEYS);
        String customFieldNameStr = (String)configProp.get(P_JIRA_CUSTOM_FIELD_NAMES);

        if (StringUtils.isAnyBlank(customFieldKeyStr,customFieldNameStr)) {
            return new LinkedHashMap<>();
        }

        String[] customFieldKeyArr = StringUtils.split(customFieldKeyStr,",");
        String[] customFieldNameArr = StringUtils.split(customFieldNameStr,",");

        if (customFieldKeyArr.length != customFieldNameArr.length) {
            System.out.println("配置异常!" + P_JIRA_CUSTOM_FIELD_KEYS + "!=" + P_JIRA_CUSTOM_FIELD_NAMES);
            return new LinkedHashMap<>();
        }

        Map<String, String> jiraCustomFieldMap = new LinkedHashMap<String, String>();
        for (int i = 0; i<customFieldKeyArr.length; i++) {
            jiraCustomFieldMap.put(customFieldNameArr[i], customFieldKeyArr[i]);
        }

        return jiraCustomFieldMap;
    }
}