package cc.xiaoquer.jira.autoupdater;

import cc.xiaoquer.jira.constant.FoldersConsts;
import cc.xiaoquer.jira.ui.InstallAssistFrame;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Nicholas on 2018/3/29.
 */
public class JiraAutoUpdater {
    private static final String URL_LATEST_TAG = "https://api.github.com/repos/NicholasQu/JiraScrumCardsPrinter/releases/latest";
    private static final String URL_JAR_DOWNLOAD = "https://github.com/NicholasQu/JiraScrumCardsPrinter/releases/download/{tagName}/JiraScrumCardsPrinter-1.0.jar-jar-with-dependencies.jar";

    private static AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();

    public static String LATEST_TAG_NAME = "0";
    public static String LATEST_DOWN_URL = "http://github.xxx/";
    public static String LATEST_DESC = "release notes";
    public static double LATEST_ASSET_SIZE = 9633289.0;
    public static File LATEST_TAG_IDENTIFIER = null;

    public static AtomicInteger GIT_NOT_CONNECTED_COUNT = new AtomicInteger(0);

    //True: have updates
    public static boolean checkUpdates() {
        Future<Response> f = asyncHttpClient.prepareGet(URL_LATEST_TAG)
                .addHeader("Accept", "application/json")
                .execute();
        String responseBody;
        try {
            Response r = f.get(5L, TimeUnit.SECONDS);
            responseBody = r.getResponseBody(Charset.forName("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        InputStream in = JiraAutoUpdater.class.getResourceAsStream("/jscp_version");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String currentJarVersion = null;
        try {
            currentJarVersion = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jo = JSON.parseObject(responseBody);

        //检查最新的tag是否已经下载过，即查看 TAG_XXX 这个文件是否存在
        LATEST_TAG_NAME = jo.getString("tag_name");
        LATEST_DESC = jo.getString("body");

        try {
            JSONObject asset = jo.getJSONArray("assets").getJSONObject(0);

            LATEST_ASSET_SIZE = asset.getDoubleValue("size");

            LATEST_DOWN_URL = asset.getString("browser_download_url");
            if (LATEST_DOWN_URL == null) {
                LATEST_DOWN_URL = URL_JAR_DOWNLOAD.replace("{tagName}", LATEST_TAG_NAME);
            }

//            LATEST_DOWN_URL = LATEST_DOWN_URL.replace("github.com", "192.168.0.254");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

//        LATEST_TAG_IDENTIFIER = new File(FoldersConsts.TAG_FILE + LATEST_TAG_NAME);
//        if (!LATEST_TAG_IDENTIFIER.exists()) {
//            //有更新
//            return true;
//        }

        System.out.println("Lastest_TagName: " + LATEST_TAG_NAME + ", Current_Jar's_Version: " + currentJarVersion);
        System.out.println("Lastest_Size: " + LATEST_ASSET_SIZE);
        System.out.println("Lastest_ReleaseNotes: \n" + LATEST_DESC);

        if (!LATEST_TAG_NAME.equals(currentJarVersion)) return true;

        return false;
    }

    private static Thread downloadThread;
    private static Thread renderThread;

    private static volatile boolean downloading_mutex = false; //下载过程中不在允许重试下载

    private static volatile boolean stopFlag = false;
    private static volatile boolean isDownloadDone = false;
    private static volatile boolean isRenderDone = false;
    private static volatile int     totalSize = 0;

    private static String getThreadInfo() {
        return getThreadInfo(Thread.currentThread());
    }
    private static String getThreadInfo(Thread t) {
        return t.getName() + "[" + t.getId() + "]-";
    }

    public static void asyncDownloadJar(final InstallAssistFrame frame) {
        //只允许一个线程进入下载
        if (downloading_mutex) return;

        GIT_NOT_CONNECTED_COUNT.set(0);
        stopFlag = false;
        downloading_mutex = true;

        final File jarFileOrigin = new File(FoldersConsts.JAR_FILE);
        final File jarFileDownloading  = new File(FoldersConsts.JAR_FILE_DOWN_ING);
        final File jarFileDownloadDone = new File(FoldersConsts.JAR_FILE_DOWN_DONE);

        //删除原始的downloading不完整文件, 如果下载成功过，只要重新下载就删除重新开始
        try {
            jarFileDownloading.delete();
            jarFileDownloadDone.delete();
        } catch (Exception e) {
            System.out.println("Delete Failed:" + FoldersConsts.JAR_FILE_DOWN_ING);
//            throw new RuntimeException(e);
        }

        //独立线程下载
        downloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                isDownloadDone = false;

                System.out.println(getThreadInfo() + "-Downloading Starts From " + LATEST_DOWN_URL);
                System.out.println(getThreadInfo() + "-Downloading Saves To " + jarFileDownloading.getAbsolutePath());
//                while (!stopFlag && !isDownloadDone) {
                    //下载文件
                    try {
                        //connectionTimeout - the number of milliseconds until this method will timeout if no connection could be established to the source
                        //readTimeout - the number of milliseconds until this method will timeout if no data could be read from the source
                        FileUtils.copyURLToFile(new URL(LATEST_DOWN_URL), jarFileDownloading, 10*1000, 10*1000);
                        isDownloadDone = true;
                        System.out.println(getThreadInfo() + "-Downloading Done.");
                    } catch (IOException e1) {
                        System.out.println(getThreadInfo() + "-Downloading Failed " + e1.getMessage());
                        e1.printStackTrace();
                        //无需手动终止，render线程会持续查看文件大小，超过次数就当做下载异常处理。
    //                        JiraAutoUpdater.stop();
                    }
//                }
            }
        }, "downloadThread");

        //独立线程更新ProgressBar
        renderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                isRenderDone = false;

                System.out.println(getThreadInfo() + "-Check File Size..." + jarFileDownloading.getAbsolutePath());

                while (!stopFlag && !isRenderDone) {
                    Random random = new Random();

                    try {
                        Thread.sleep(1000 + random.nextInt(100));
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                    int size = (int) jarFileDownloading.length();
                    int percentage = (int) (size / LATEST_ASSET_SIZE * 100);

                    System.out.println(getThreadInfo() + "-Size is..." + size);
                    System.out.println(getThreadInfo() + "-Progress is " + percentage);

                    frame.renderUI(size, percentage);

                    if (percentage >= 100) {
                        System.out.println(getThreadInfo() + "-Progress Done.");
                        Toolkit.getDefaultToolkit().beep();
                        isRenderDone = true;
                        totalSize = size;
                    }
                }


                //下载完成更改文件名
                try {
                    Thread.sleep(1000L);

                    //第一步：备份源文件
                    File backupFile = new File(FoldersConsts.JAR_BAK_FILE + System.currentTimeMillis());
                    FileUtils.copyFile(jarFileOrigin, backupFile);
                    System.out.println(getThreadInfo() + "-Backup File From " + jarFileOrigin.getName() + " To " + backupFile.getName());
//                    boolean renameSucc = false;
//                    int loopCheck = 0;
//                    do {
//                        Thread.sleep(1000L);
//                        loopCheck++;
//                        FileUtils.copyFile(jarFileOrigin, backupFile);
//                        renameSucc = backupFile.exists();
//                        System.out.println(getThreadInfo() + "Rename Files " + loopCheck + " Times..." + renameSucc);
//                    } while (!renameSucc && loopCheck <= 5);
//                    System.out.println(getThreadInfo() + "-Origin Exists: " + jarFileOrigin.exists() + " Dest Exists: " + backupFile.exists());

                    //第二步：新建一个更新文件下载完成的标识新文件。
                    File downloadDoneFile = new File(FoldersConsts.JAR_FILE_DOWN_DONE);
                    FileUtils.touch(downloadDoneFile);
                    System.out.println(getThreadInfo() + "-Touch File " + downloadDoneFile.getName());
//                    renameSucc = false;
//                    loopCheck = 0;
//                    do {
//                        Thread.sleep(1000L);
//                        loopCheck++;
////                        FileUtils.copyFile(jarFileDownloading, tempFile);
//                        //将下载中的文件名更改为下载完成。
//                        FileUtils.moveFile(jarFileDownloading, downloadDoneFile);
//                        renameSucc = !jarFileDownloading.exists() && downloadDoneFile.exists();
////                        renameSucc = jarFileDownloading.renameTo(finalFile);
//                    } while (!renameSucc && loopCheck <= 5);

//                    System.out.println(getThreadInfo() + "-Origin Exists: " + jarFileDownloading.exists() + " Dest Exists: " + jarFileOrigin.exists());

                    //第三步：文件更名完毕，才算真正完成，才能enable重启应用按钮
                    frame.renderUI(totalSize, 101);
                } catch (Exception e1) {
                    throw new RuntimeException(e1);
                }

//                System.out.println("Create Tag Identifier File..." + LATEST_TAG_IDENTIFIER.getAbsolutePath());
//                try {
//                    LATEST_TAG_IDENTIFIER.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }, "renderThread");


        downloadThread.start();
        renderThread.start();
    }

    public static void stop() {
        stopFlag = true;
        downloading_mutex = false;

        interruptThread(downloadThread);
        interruptThread(renderThread);
    }

    private static void interruptThread(Thread t) {
        try {
            if (t != null) {
                t.interrupt();
                t = null;
            }
        } catch (Exception e) {
            System.out.println("Interrupt thread " + getThreadInfo(t) + " : " + t.isInterrupted());
        }
    }


}
