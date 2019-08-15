package cc.xiaoquer.jira.html;

import cc.xiaoquer.jira.storage.PropertiesCache;
import cc.xiaoquer.utils.JSCPUtils;
import com.hierynomus.security.jce.JceSecurityProvider;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.utils.SmbFiles;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.security.Security;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nicholas on 2019/7/17.
 */
public class Publisher {
    private static final String PUBLISH_NAME    = "OQPL.html";

    private static SMBClient smbClient;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }


    public static void init() {
        SmbConfig config = SmbConfig.builder()
                .withSecurityProvider(new JceSecurityProvider(new BouncyCastleProvider()))
                .withTimeout(120, TimeUnit.SECONDS) // Timeout sets Read, Write, and Transact timeouts (default is 60 seconds)
                .withSoTimeout(180, TimeUnit.SECONDS) // Socket Timeout (default is 0 seconds, blocks forever)
                .build();
        smbClient = new SMBClient(config);
    }

    public static String publish(String localPath) throws Exception {
        if (smbClient == null) init();

        String smbDomain        = PropertiesCache.getProp(PropertiesCache.P_SMB_DOMAIN);
        String smbUser          = PropertiesCache.getProp(PropertiesCache.P_SMB_USER);
        String smbPassword      = JSCPUtils.decrypt(PropertiesCache.getProp(PropertiesCache.P_SMB_PASSWORD),"jira");
        String smbLoginDomain   = PropertiesCache.getProp(PropertiesCache.P_SMB_LOGIN_DOMAIN);
        String smbShareRoot     = PropertiesCache.getProp(PropertiesCache.P_SMB_SHARE_ROOT);
        String smbSharePath     = PropertiesCache.getProp(PropertiesCache.P_SMB_SHARE_PATH);
        String webServerUrl     = PropertiesCache.getProp(PropertiesCache.P_WEBSERVER_URL);

        File localFile = new File(localPath);
        String publishUrl = webServerUrl + (webServerUrl.endsWith("/") ? "": "/") + PUBLISH_NAME;

        try (Connection connection = smbClient.connect(smbDomain)) {
            System.out.println("连接SMB " + smbDomain + " 成功...");
            AuthenticationContext ac = new AuthenticationContext(smbUser, smbPassword.toCharArray(), smbLoginDomain);
            Session session = connection.authenticate(ac);
            System.out.println(smbUser + " 登入SMB成功...");

            // Connect to Share
            try (DiskShare share = (DiskShare) session.connectShare(smbShareRoot)) {

                System.out.println("进入SMB 共享目录 " + smbShareRoot + " 成功...");
                SmbFiles.copy(localFile, share, smbSharePath + "\\" + PUBLISH_NAME, true);
                SmbFiles.copy(localFile, share, smbSharePath + "\\archive\\" + localFile.getName(), false);
                System.out.println("发布远程服务器成功." + publishUrl);
            }
        }

        return publishUrl;
    }
}
