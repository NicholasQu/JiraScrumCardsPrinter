package cc.xiaoquer.database;

import cc.xiaoquer.jira.storage.PropertiesCache;
import cc.xiaoquer.utils.JSCPUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nicholas on 2019/6/13.
 */
public class DBUtils {

    public static void test() {
//        String s = JSCPUtils.encrypt("1qaz@WSX3edc$RFV", System.getProperty("java.version"));
//        System.out.println("["+s+"]");
//        System.out.println("["+JSCPUtils.decrypt(s, System.getProperty("java.version"))+"]");
//        System.out.println();


        HikariConfig config = new HikariConfig();
        System.out.println(JSCPUtils.decrypt(PropertiesCache.getProp("dbpwd"), System.getProperty("java.version")));
        config.setJdbcUrl(PropertiesCache.getProp("dburl"));
        config.setUsername(PropertiesCache.getProp("dbuser"));
        config.setPassword(PropertiesCache.getProp("dbpwd"));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("minimumIdle", "1");
        config.addDataSourceProperty("maximumPoolSize", "3");
        config.addDataSourceProperty("connectionTimeout", "10000"); //10s
        config.addDataSourceProperty("idleTimeout", "600000"); //10min

        HikariDataSource ds = new HikariDataSource(config);

        //https://blog.csdn.net/u010986080/article/details/51813056/
//        try {
//            ResultSet rs = ds.getConnection().prepareStatement("select count(1) from employee")
//                    .executeQuery();
//
//            while (rs.next()) {
//                System.out.println("return result:" + rs.getInt(1));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }

    public static List query() {
        return new ArrayList();
    }

}
