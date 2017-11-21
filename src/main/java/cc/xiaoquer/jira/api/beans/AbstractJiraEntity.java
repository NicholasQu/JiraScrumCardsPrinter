package cc.xiaoquer.jira.api.beans;

import cc.xiaoquer.jira.api.jsonchain.JiraJSONObject;
import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Nicholas on 2017/9/4.
 */
public abstract class AbstractJiraEntity implements Serializable {

    protected static String nullToBlank(String s) {
        return s == null ? "" : s;
    }

}
