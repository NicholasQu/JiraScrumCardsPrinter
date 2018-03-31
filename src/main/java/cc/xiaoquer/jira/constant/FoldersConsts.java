package cc.xiaoquer.jira.constant;

import java.io.File;

/**
 * Created by Nicholas on 2018/3/28.
 */
public class FoldersConsts {
    public static String BASE_PATH                  ; //= System.getProperty("user.dir");
//    public static String BASE_PATH                ; //  = FoldersConsts.class.getClassLoader().getResource("").getFile();

    public static String JAR_FILE                   ; //= BASE_PATH + File.separator + "JiraScrumCardsPrinter-1.0.jar-jar-with-dependencies.jar";
    public static String JAR_FILE_DOWN_ING          ; //= BASE_PATH + File.separator + "JiraScrumCardsPrinter-1.0.jar-jar-with-dependencies.jar";
    public static String JAR_FILE_DOWN_DONE         ; //= BASE_PATH + File.separator + "JiraScrumCardsPrinter-1.0.jar-jar-with-dependencies.jar";
    public static String JAR_BAK_FILE               ; //= BASE_PATH + File.separator + "JiraScrumCardsPrinter-1.0.jar-jar-with-dependencies.jar_tmp";
    public static String RUN_FILE                   ; //= BASE_PATH + File.separator + "RUN.bat";

    public static String OUTPUT_FOLDER              ; //= BASE_PATH + File.separator + "JSCP_FILES" ;

    public static String TAG_FILE                   ; //= OUTPUT_FOLDER + File.separator + "TAG_"; //TAG_20180101

    public static String CONFIG_FOLDER              ; //= OUTPUT_FOLDER + File.separator + "CONF" ;
    public static String CONFIG_FILE                ; //= CONFIG_FOLDER + File.separator + "jscp.properties";
}
