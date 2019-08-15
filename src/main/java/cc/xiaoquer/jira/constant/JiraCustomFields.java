package cc.xiaoquer.jira.constant;

import cc.xiaoquer.jira.storage.PropertiesCache;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Nicholas on 2019/7/10.
 */
public class JiraCustomFields {

    public static final String EPIC_PRIORITY = "epicPriority";


    public static String epicPriority() {
        return PropertiesCache.getJiraCustomFields().get(EPIC_PRIORITY);
    }
}
