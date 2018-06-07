package cc.xiaoquer.jira;

import cc.xiaoquer.jira.constant.FoldersConsts;
import cc.xiaoquer.jira.ui.DailyTaskRecordFrame;
import cc.xiaoquer.jira.ui.InstallAssistFrame;
import cc.xiaoquer.jira.ui.JiraCardPrinterFrame;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * Created by Nicholas on 2017/9/2.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        File jarPath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());

        FoldersConsts.BASE_PATH         = jarPath.getParent();

        FoldersConsts.JAR_FILE          = FoldersConsts.BASE_PATH + File.separator + "JiraScrumCardsPrinter-1.0.jar-jar-with-dependencies.jar";
        FoldersConsts.JAR_FILE_DOWN_ING = FoldersConsts.BASE_PATH + File.separator + "_downloading";
        FoldersConsts.JAR_FILE_DOWN_DONE= FoldersConsts.BASE_PATH + File.separator + "_done";
        FoldersConsts.JAR_BAK_FILE      = FoldersConsts.BASE_PATH + File.separator + "_backup_";
        FoldersConsts.RUN_FILE          = FoldersConsts.BASE_PATH + File.separator + "RUN.bat";

        FoldersConsts.OUTPUT_FOLDER    = FoldersConsts.BASE_PATH + File.separator + "JSCP_FILES" ;

        FoldersConsts.TAG_FILE         = FoldersConsts.OUTPUT_FOLDER + File.separator + "TAG_"; //TAG_20180101

        FoldersConsts.CONFIG_FOLDER    = FoldersConsts.OUTPUT_FOLDER + File.separator + "CONF" ;
        FoldersConsts.CONFIG_FILE      = FoldersConsts.CONFIG_FOLDER + File.separator + "jscp.properties";

        File jar = new File(FoldersConsts.JAR_FILE);
        File jarDownloading = new File(FoldersConsts.JAR_FILE_DOWN_ING);
        File jarDownloadingDone = new File(FoldersConsts.JAR_FILE_DOWN_DONE);

        if (jarPath.getName().endsWith("downloading") && jarDownloadingDone.exists()) {
            //当前打开的jar是downloading， 又存在更新完成的Done标识文件，需要将downloading覆盖安装到原本的jar
            FileUtils.copyFile(jarDownloading, jar);
            System.out.println("Copy File From " + jarDownloading.getAbsolutePath() + " To " + jar.getAbsolutePath());
            FileUtils.forceDelete(jarDownloadingDone);
            System.out.println("Delete File " + jarDownloadingDone.getAbsolutePath());
            runAnother(FoldersConsts.JAR_FILE);
            return;
        }

        //存在刚刚更新完成的标识文件
        if (jarDownloadingDone.exists()) {
            //关闭当前的jar进程，重启downloading jar，为了解锁windows 对jar的lock， 可以覆盖更新jar。
            runAnother(FoldersConsts.JAR_FILE_DOWN_ING);
        }

        //********************** 本地运行 **********************
//        new JiraCardPrinterFrame().show();
//        new InstallAssistFrame().show();
        new DailyTaskRecordFrame().show();
    }

    private static void runAnother(String filePath) {
        Process proc = null;
        try {
            System.out.println("重新启动文件..." + filePath);
            proc = Runtime.getRuntime().exec("java -Dfile.encoding=utf-8 -jar " + filePath);

            // Then retreive the process output
            InputStream in = proc.getInputStream();
            InputStream err = proc.getErrorStream();
            System.exit(0);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
