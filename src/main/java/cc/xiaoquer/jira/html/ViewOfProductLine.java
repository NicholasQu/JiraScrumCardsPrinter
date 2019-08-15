package cc.xiaoquer.jira.html;

import cc.xiaoquer.jira.api.JIRA;
import cc.xiaoquer.jira.api.beans.JiraEpic;
import cc.xiaoquer.jira.api.beans.JiraIssue;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Nicholas on 2019/7/17.
 */
public class ViewOfProductLine {
    protected static final String TITLE = "奥琪产品线统一视图v1.0";

    protected static final int    PADDING_LEN    = 10;
    protected static final String SPLIT          = "|";

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected static final SimpleDateFormat DATE_FORMAT_SLIM = new SimpleDateFormat("yyyyMMddHHmmss");

    public static final String TAG_LEFT = "<";
    public static final String TAG_LEFT_ESCAPE = "&lt;";
    public static final String TAG_RIGHT = ">";
    public static final String TAG_RIGHT_ESCAPE = "&gt;";
    public static final String TAG_QUOT = "\"";
    public static final String TAG_QUOT_ESCAPE = "&quot;";

    //Key = epic id
    protected static final Map<String, Integer> ISSUE_COUNT_IN_EPIC = new LinkedHashMap<>();
    //Key = project id
    protected static final Map<String, Integer> ISSUE_COUNT_IN_PROJECT  = new LinkedHashMap<>();
    //Key = id组合起来的一个字符串，用于排序
    protected static final Map<String, JiraIssue> SORTED_ISSUES_MAP = new TreeMap<>();

    public static short[] COL_WIDTH_PIXEL = new short[] {
            150,    50,     50,     80,
            70,     70,     70,
            100,    300,     70,    70,    70,
            300,
            150,    150,
            150
    };
    public static String[] COL_NAME_ARR = new String[] {
            "产品名称(&版本)",        "产品<br/>负责人",       "产品<br/>优先级",   "产品<br/>状态",
            "总工时<br/>(人天)",      "总成本<br/>(万元)",     "收益<br/>情况",
            "迭代/泳道",               "用户故事",               "PO",               "优先级",       "US状态",
            "备注说明",
            "预估完成<br/>时间", "实际完成<br/>时间",
            "最后更新<br/>时间"
    };

    private static void init() {
        //清空数据
        SORTED_ISSUES_MAP.clear();
        ISSUE_COUNT_IN_EPIC.clear();
        ISSUE_COUNT_IN_PROJECT.clear();
    }

    protected static void sortIssues(Map<String, Map<String, JiraIssue>> epicIssuesMap) {
        init();

        //将每行的记录以有序的方式存入Map
        for (Map.Entry<String, Map<String, JiraIssue>> entry: epicIssuesMap.entrySet()) {
            String epicId = entry.getKey();
            Map<String, JiraIssue> issueMap = entry.getValue();

            JiraEpic jiraEpic = JIRA.ALL_EPIC_MAP.get(epicId);

            // 存在空的Epic还没有关联用户故事的情况
            if (issueMap == null || issueMap.size() == 0) {
                JiraIssue jiraIssue = new JiraIssue(jiraEpic);
                SORTED_ISSUES_MAP.put(key4SortAndGroup(jiraEpic,jiraIssue), jiraIssue);
                continue;
            }

            for (JiraIssue jiraIssue : issueMap.values()) {
                SORTED_ISSUES_MAP.put(key4SortAndGroup(jiraEpic, jiraIssue), jiraIssue);
            }
        }
    }

    // epicStatus + epicId + projectId + issueStatus
    // 排序规则：产品优先级（doing+todo）, 产品状态（进行中优先），用户故事优先级（doing+todo）,用户故事状态（进行中优先），
    protected static String key4SortAndGroup(JiraEpic jiraEpic, JiraIssue jiraIssue) {
        String k1_epic_priority = jiraEpic.getCustomPriority(); //len = 3
        String k2_epic_status = jiraEpic.getStatusCategory4Sort();     //len = 2
        String k3_epic_id = StringUtils.leftPad(jiraEpic.getEpicId(), PADDING_LEN, "0"); // len = 10
        //issue不存在projectId只有一种情况，就是这个issue是个空的Epic
        String k4_project_id = StringUtils.leftPad(StringUtils.trimToEmpty(jiraIssue.getProjectId()), PADDING_LEN, "0");
        String k5_issue_priority = jiraIssue.getPriority4Sort();
        String k6_issue_status = jiraIssue.getIssueStatus();
        String k7_issue_id = StringUtils.leftPad(StringUtils.trimToEmpty(jiraIssue.getIssueId()), PADDING_LEN, "0");

        //因为priority是数字越大优先级越高，为了将优先级高的排前，做个转换
        DecimalFormat NUM_FORMAT_INT = new DecimalFormat("#");
        BigDecimal positivePriority = new BigDecimal(100000).subtract(new BigDecimal(k1_epic_priority));
        k1_epic_priority = NUM_FORMAT_INT.format(positivePriority);

        //若issue所属的project与epic的一致，说明issue还在产品池中未分配
        //未分配的沉到下方
        if (jiraIssue.getProjectId().equals(jiraEpic.getProjectId())) {
            k4_project_id = StringUtils.leftPad("", PADDING_LEN, "9");
        }

        String epicGroupKey = k1_epic_priority + k2_epic_status + k3_epic_id;
        String epicProjectGroupKey = epicGroupKey + SPLIT + k4_project_id;
        String combinedKey = epicProjectGroupKey + SPLIT + k5_issue_priority + k6_issue_status + k7_issue_id;

        addGroupCount(epicGroupKey, ISSUE_COUNT_IN_EPIC);
        addGroupCount(epicProjectGroupKey, ISSUE_COUNT_IN_PROJECT);
        return combinedKey;
    }

    private static void addGroupCount(String groupKey, Map<String, Integer> groupMap) {
        Integer groupCount = groupMap.get(groupKey);
        if (groupCount == null) {
            groupCount = 0;
        }
        groupMap.put(groupKey, ++groupCount);
    }

    protected static String getEpicGroupKey(String combinedKey) {
        return StringUtils.substringBefore(combinedKey, SPLIT);
    }
    protected static String getEpicProjectGroupKey(String combinedKey) {
        return StringUtils.substringBeforeLast(combinedKey, SPLIT);
    }

    //groupCount = 0, 表示不需要这个TD
    //groupCount = 1, 表示需要一个TD
    //groupCount > 1, 表示需要一个RowSpan的TD
    protected static int getEpicGroupCount(String combinedKey) {
        Integer i =  ISSUE_COUNT_IN_EPIC.get(getEpicGroupKey(combinedKey));
        return i == null ? 0 : i;
    }

    protected static int getProjectGroupCount(String combinedKey) {
        Integer i = ISSUE_COUNT_IN_PROJECT.get(getEpicProjectGroupKey(combinedKey));
        return i == null ? 0 : i;
    }

    protected static String[] getDisplayProject(JiraEpic jiraEpic, JiraIssue jiraIssue) {
        String projectKey  = jiraEpic.getProjectKey();
        String projectName =  jiraEpic.getProjectName();
        if (StringUtils.isNotBlank(jiraIssue.getIssueKey())) {
            projectKey = jiraIssue.getJiraProject().getProjectKey();
            projectName = jiraIssue.getJiraProject().getProjectName();

            //若issue所属的project与epic的一致，说明issue还在产品池中未分配
            if (projectKey.equals(jiraEpic.getProjectKey())) {
                projectName = "暂未分派";
            }
        }

        return new String[]{projectKey, projectName};
    }

}
