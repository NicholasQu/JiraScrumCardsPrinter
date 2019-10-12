package cc.xiaoquer.jira.api.beans;

import cc.xiaoquer.jira.storage.PropertiesCache;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Nicholas on 2017/9/4.
 */
@Data
public abstract class AbstractJiraEntity implements Serializable {
    protected SimpleDateFormat jiraDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");
    protected SimpleDateFormat defaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected static final DecimalFormat NUM_FORMAT_2D = new DecimalFormat("#.##");
    protected static final DecimalFormat NUM_FORMAT_INT = new DecimalFormat("#");


    protected static String STATUS_CATEGORY_KEYS = "";
    protected static String STATUS_CATEGORY_TODO_KEY;
    protected static String STATUS_CATEGORY_DOING_KEY;
    protected static String STATUS_CATEGORY_DONE_KEY;
    protected String statusCategoryKey = ""; //ISSUE 的 状态类型只有“待办”“进行中”“完成

    //这个字段需要大家自己去修改映射关系，用于扩展
    private Map<String, String> customFields = new LinkedHashMap<String, String>();

    protected static String nullToBlank(String s) {
        return s == null ? "" : s;
    }

    protected static void refreshStatusCategory() {
        String cache = PropertiesCache.getProp(PropertiesCache.P_ISSUE_STATUS_CATEGORY_3KEYS);
        if (!STATUS_CATEGORY_KEYS.equals(cache)) {
            STATUS_CATEGORY_KEYS = cache;

            String[] keys = StringUtils.split(cache, ",");

            STATUS_CATEGORY_TODO_KEY = StringUtils.trimToEmpty(keys[0]);
            STATUS_CATEGORY_DOING_KEY = StringUtils.trimToEmpty(keys[1]);
            STATUS_CATEGORY_DONE_KEY = StringUtils.trimToEmpty(keys[2]);
        }
    }

    public boolean isDone() {
        refreshStatusCategory();
        return STATUS_CATEGORY_DONE_KEY.equals(this.statusCategoryKey);
    }

    public boolean isDoing() {
        refreshStatusCategory();
        return STATUS_CATEGORY_DOING_KEY.equals(this.statusCategoryKey);
    }

    public boolean isTodo() {
        refreshStatusCategory();
        return STATUS_CATEGORY_TODO_KEY.equals(this.statusCategoryKey);
    }

    public int getStatusCategoryNum() {
        if (statusCategoryKey == null) return 40;

        if (this.isDone()) {
            return 60;
        } else if (this.isTodo()) {
            return 40;
        } else if (this.isDoing()) {
            return 20;
        }
        return 40;
    }
    //倒排序
    public String getStatusCategory4Sort() {
        return String.valueOf(getStatusCategoryNum());
    }

    protected String getCustomField(String key) {
        return StringUtils.defaultIfBlank(customFields.get(PropertiesCache.getProp(PropertiesCache.ISSUE_CUSTOMFIELD_PREFIX + key)),"");
    }

    //工时（人天）转为成本（万元）的系数 3.5万元/21.75
    protected BigDecimal getWorkingToCostRate() {
        return new BigDecimal(StringUtils.defaultIfBlank(PropertiesCache.getProp(PropertiesCache.P_WORKING_TO_COST), "0.16091954"));
    }
}
