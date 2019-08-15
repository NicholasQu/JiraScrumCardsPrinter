package cc.xiaoquer.jira.api.beans;

import cc.xiaoquer.jira.api.JIRA;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by Nicholas on 2019/7/10.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiraEpic extends AbstractJiraEntity {
    //
    private String      boardId = "";
    private String      projectId = "";
    private String      projectKey = "";
    private String      projectName = "";
    private String      epicId = "";
    private String      epicKey = "";
    private String      epicName = "";
    private String      epicSummary = "";
    private String      epicPriority = "";
    private String      epicStatus = "";
    private String      epicStatusCategoryKey = ""; //状态类型只有“待办”“进行中”“完成

    private String      epicOwner = "";

    private String[]    epicLabels;
    private String      epicDesc;
    private boolean     epicDone;

    private String      customPriority = "";
    private String      customTotalWorking = "";
    private String      customRevenueInfo = "";

    public JiraEpic(String epicId) {
        this.epicId = epicId;
    }

    //优先级的数字越大，优先级越高
    public String getCustomPriority() {
        String priorityStr = StringUtils.defaultIfBlank(getCustomField("priority"),"0");
        BigDecimal priority = new BigDecimal(priorityStr);
        return NUM_FORMAT_INT.format(priority);
    }

    public String getCustomTotalWorking() {
        return StringUtils.defaultIfBlank(getCustomField("totalworking"),"0");
    }

    public String getCost() {
        BigDecimal rate = getWorkingToCostRate();
        BigDecimal working = new BigDecimal(getCustomTotalWorking());
        return NUM_FORMAT_2D.format(rate.multiply(working));
    }

    public String getCustomRevenueInfo() {
        return getCustomField("revenueinfo");
    }

//    {
//        "expand":"renderedFields,names,schema,operations,editmeta,changelog,versionedRepresentations",
//            "id":"48557",
//            "self":"http://jira.immd.cn/rest/agile/1.0/issue/48557",
//            "key":"CPC-1",
//            "fields":{
//        "issuetype":{
//            "self":"http://jira.immd.cn/rest/api/2/issuetype/10000",
//                    "id":"10000",
//                    "description":"gh.issue.epic.desc",
//                    "iconUrl":"http://jira.immd.cn/images/icons/issuetypes/epic.svg",
//                    "name":"Epic",
//                    "subtask":false
//        },
//        "timespent":null,
//                "project":{
//            "self":"http://jira.immd.cn/rest/api/2/project/12800",
//                    "id":"12800",
//                    "key":"CPC",
//                    "name":"产品池",
//                    "avatarUrls":{
//                "48x48":"http://jira.immd.cn/secure/projectavatar?avatarId=10324",
//                        "24x24":"http://jira.immd.cn/secure/projectavatar?size=small&avatarId=10324",
//                        "16x16":"http://jira.immd.cn/secure/projectavatar?size=xsmall&avatarId=10324",
//                        "32x32":"http://jira.immd.cn/secure/projectavatar?size=medium&avatarId=10324"
//            }
//        },
//        "fixVersions":[
//
//        ],
//        "customfield_11001":null,
//                "customfield_11200":null,
//                "aggregatetimespent":null,
//                "resolution":null,
//                "customfield_11201":null,
//                "customfield_11400":111222,
//                "customfield_10900":null,
//                "resolutiondate":null,
//                "workratio":-1,
//                "lastViewed":"2019-07-10T17:27:48.816+0800",
//                "watches":{
//            "self":"http://jira.immd.cn/rest/api/2/issue/CPC-1/watchers",
//                    "watchCount":1,
//                    "isWatching":false
//        },
//        "created":"2019-07-09T19:39:44.000+0800",
//                "priority":{
//            "self":"http://jira.immd.cn/rest/api/2/priority/3",
//                    "iconUrl":"http://jira.immd.cn/images/icons/priorities/medium.svg",
//                    "name":"Medium",
//                    "id":"3"
//        },
//        "customfield_10100":null,
//                "labels":[
//        "nicholas",
//                "qu"
//        ],
//        "timeestimate":null,
//                "aggregatetimeoriginalestimate":null,
//                "versions":[
//
//        ],
//        "issuelinks":[
//
//        ],
//        "assignee":{
//            "self":"http://jira.immd.cn/rest/api/2/user?username=nicholas.qu",
//                    "name":"nicholas.qu",
//                    "key":"nicholas.qu",
//                    "emailAddress":"nicholas.qu@ooqi.cn",
//                    "avatarUrls":{
//                "48x48":"http://jira.immd.cn/secure/useravatar?ownerId=nicholas.qu&avatarId=11600",
//                        "24x24":"http://jira.immd.cn/secure/useravatar?size=small&ownerId=nicholas.qu&avatarId=11600",
//                        "16x16":"http://jira.immd.cn/secure/useravatar?size=xsmall&ownerId=nicholas.qu&avatarId=11600",
//                        "32x32":"http://jira.immd.cn/secure/useravatar?size=medium&ownerId=nicholas.qu&avatarId=11600"
//            },
//            "displayName":"曲健",
//                    "active":true,
//                    "timeZone":"Asia/Shanghai"
//        },
//        "updated":"2019-07-10T17:27:49.000+0800",
//                "status":{
//            "self":"http://jira.immd.cn/rest/api/2/status/10100",
//                    "description":"",
//                    "iconUrl":"http://jira.immd.cn/images/icons/status_generic.gif",
//                    "name":"进行中",
//                    "id":"10100",
//                    "statusCategory":{
//                "self":"http://jira.immd.cn/rest/api/2/statuscategory/4",
//                        "id":4,
//                        "key":"indeterminate",
//                        "colorName":"yellow",
//                        "name":"进行中"
//            }
//        },
//        "components":[
//
//        ],
//        "timeoriginalestimate":null,
//                "description":null,
//                "customfield_11100":null,
//                "customfield_11300":null,
//                "timetracking":{
//
//        },
//        "customfield_10005":"ghx-label-1",
//                "customfield_10006":null,
//                "attachment":[
//
//        ],
//        "aggregatetimeestimate":null,
//                "flagged":false,
//                "summary":"XX产品",
//                "creator":{
//            "self":"http://jira.immd.cn/rest/api/2/user?username=b.z",
//                    "name":"b.z",
//                    "key":"b.z",
//                    "emailAddress":"b.z@ooqi.cn",
//                    "avatarUrls":{
//                "48x48":"http://jira.immd.cn/secure/useravatar?ownerId=b.z&avatarId=13202",
//                        "24x24":"http://jira.immd.cn/secure/useravatar?size=small&ownerId=b.z&avatarId=13202",
//                        "16x16":"http://jira.immd.cn/secure/useravatar?size=xsmall&ownerId=b.z&avatarId=13202",
//                        "32x32":"http://jira.immd.cn/secure/useravatar?size=medium&ownerId=b.z&avatarId=13202"
//            },
//            "displayName":"张XXX",
//                    "active":true,
//                    "timeZone":"Asia/Shanghai"
//        },
//        "subtasks":[
//
//        ],
//        "reporter":{
//            "self":"http://jira.immd.cn/rest/api/2/user?username=b.z",
//                    "name":"b.z",
//                    "key":"b.z",
//                    "emailAddress":"b.z@ooqi.cn",
//                    "avatarUrls":{
//                "48x48":"http://jira.immd.cn/secure/useravatar?ownerId=b.z&avatarId=13202",
//                        "24x24":"http://jira.immd.cn/secure/useravatar?size=small&ownerId=b.z&avatarId=13202",
//                        "16x16":"http://jira.immd.cn/secure/useravatar?size=xsmall&ownerId=b.z&avatarId=13202",
//                        "32x32":"http://jira.immd.cn/secure/useravatar?size=medium&ownerId=b.z&avatarId=13202"
//            },
//            "displayName":"张XXX",
//                    "active":true,
//                    "timeZone":"Asia/Shanghai"
//        },
//        "customfield_10000":"0|i05da3:",
//                "aggregateprogress":{
//            "progress":0,
//                    "total":0
//        },
//        "customfield_10001":null,
//                "customfield_10002":null,
//                "customfield_10200":null,
//                "customfield_10003":{
//            "self":"http://jira.immd.cn/rest/api/2/customFieldOption/10000",
//                    "value":"To Do",
//                    "id":"10000"
//        },
//        "customfield_10004":"XX产品",
//                "environment":null,
//                "duedate":null,
//                "progress":{
//            "progress":0,
//                    "total":0
//        },
//        "comment":{
//            "comments":[
//
//            ],
//            "maxResults":0,
//                    "total":0,
//                    "startAt":0
//        },
//        "votes":{
//            "self":"http://jira.immd.cn/rest/api/2/issue/CPC-1/votes",
//                    "votes":0,
//                    "hasVoted":false
//        },
//        "worklog":{
//            "startAt":0,
//                    "maxResults":20,
//                    "total":0,
//                    "worklogs":[
//
//            ]
//        }
//    }
//    }

    private static final int pageSize=50;

    //产品线视图生成
    public static Map<String, Map<String, JiraIssue>> viewProductLine(String boardId) {
        Map<String, Map<String, JiraIssue>> result = new LinkedHashMap<>();

        if (StringUtils.isBlank(boardId)) {
            return result;
        }

        boolean isLast = false;
        int i = 0;

        Map<String, JiraEpic> jiraEpicMap = null;
        while (!isLast) {
            String responseBody = JIRA.getResponseRelative(JIRA.AGILE_EPICS_URL
                    .replace("{boardId}", boardId)
                    .replace("{start}", String.valueOf(i))
                    .replace("{max}", String.valueOf(pageSize)));
            isLast = JSON.parseObject(responseBody).getBooleanValue("isLast");
            i = i + pageSize;

            jiraEpicMap = _parseEpicByPage(boardId, responseBody);
        }

        Map<String, JiraIssue> jiraIssueInEpicMap = null;
        for (JiraEpic jiraEpic: jiraEpicMap.values()) {
            //将Epic下的所有issue查询出来
            jiraIssueInEpicMap = _parseIssue4Epic(jiraEpic);
            result.put(jiraEpic.getEpicId(), jiraIssueInEpicMap);
        }

        return result;
    }

    //分页解析Epic列表
    private static Map<String, JiraEpic> _parseEpicByPage(String boardId, String responseBody) {
        Map<String, JiraEpic> jiraEpicMap = JIRA.BOARD_EPICS_MAP.get(boardId);
        if (jiraEpicMap == null) {
            jiraEpicMap = new LinkedHashMap<>();
        }
        JIRA.BOARD_EPICS_MAP.put(boardId, jiraEpicMap);

        JSONObject responseJo = JSON.parseObject(responseBody);
        JSONArray  epicJsonArray = responseJo.getJSONArray("values");

        if (epicJsonArray != null && epicJsonArray.size() > 0) {

            for (int i = 0; i < epicJsonArray.size(); i++) {
                JiraEpic jiraEpic = new JiraEpic();
                JSONObject epicJo = epicJsonArray.getJSONObject(i);
                jiraEpic.setEpicId(epicJo.getString("id"));
                jiraEpic.setEpicKey(epicJo.getString("key"));
                jiraEpic.setEpicName(epicJo.getString("name"));
                jiraEpic.setEpicSummary(epicJo.getString("summary"));
                jiraEpic.setEpicDone(epicJo.getBooleanValue("done"));
                jiraEpic.setBoardId(boardId);

                //补齐Epic的详细信息
                JiraIssue issueDetail = JIRA.getIssueObjById(jiraEpic.getEpicId(), boardId, "");
                jiraEpic.setEpicOwner(issueDetail.getOwner());
                jiraEpic.setEpicPriority(issueDetail.getPriority());
                jiraEpic.setEpicStatus(issueDetail.getIssueStatus());
                jiraEpic.setStatusCategoryKey(issueDetail.getStatusCategoryKey());

                jiraEpic.setProjectId(issueDetail.getProjectId());
                jiraEpic.setProjectKey(issueDetail.getJiraProject().getProjectKey());
                jiraEpic.setProjectName(issueDetail.getJiraProject().getProjectName());

                jiraEpic.setCustomFields(issueDetail.getCustomFields());


                //刷新缓存
                JIRA.ALL_EPIC_MAP.put(jiraEpic.getEpicId(), jiraEpic);

                jiraEpicMap.put(jiraEpic.getEpicId(), jiraEpic);
            }
        }

        return jiraEpicMap;
    }

    private static Map<String, JiraIssue> _parseIssue4Epic(JiraEpic jiraEpic) {
        //每个Epic下的issue List都要重新
        Map<String, JiraIssue> jiraIssuesInEpicMap = new LinkedHashMap<>();
        if (jiraIssuesInEpicMap == null) {
            jiraIssuesInEpicMap = new LinkedHashMap<>();
        }
        JIRA.EPIC_ISSUES_MAP.put(jiraEpic.getEpicId(), jiraIssuesInEpicMap);

        StringBuffer jql = new StringBuffer();
        jql.append(" \"epic link\" = ").append(jiraEpic.getEpicKey());

        String encodedJQL = jql.toString();
        try {
            encodedJQL = URLEncoder.encode(encodedJQL, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        int pageStart = 0;
        int total = 1;
        while (total > pageStart) {
            String responseBody = JIRA.getResponseRelative(JIRA.SEARCH_BY_JQL_URL
                    .replace("{jql}", encodedJQL).replace("{start}", String.valueOf(pageStart)));

            total = JSON.parseObject(responseBody).getInteger("total");
            pageStart = pageStart + pageSize;

            Map<String, JiraIssue> jiraIssue4EpicByPageMap = JiraIssue.toMap(responseBody, "", "");

            //这里epic的信息会比jiraIssue里的完整，塞回去
            for (JiraIssue jiraIssue : jiraIssue4EpicByPageMap.values()) {
                jiraIssue.setJiraEpic(jiraEpic);
            }
            jiraIssuesInEpicMap.putAll(jiraIssue4EpicByPageMap);
        }

        return jiraIssuesInEpicMap;
    }

    public void setJSONObject(JSONObject epicJo) {
        String id = epicJo.getString("id");
        if (this.epicId != null && this.epicId.equals(id)) {
            throw new RuntimeException("已有ID "+this.epicId+ " 与 " + id + " 不一致");
        }

        this.setEpicId(id);
        this.setEpicKey(epicJo.getString("key"));
        this.setEpicName(epicJo.getString("name"));
    }


    public static void main(String[] args) {
        String s = "{\n" +
                "                    \"self\":\"http://jira.immd.cn/rest/api/2/project/12800\",\n" +
                "                    \"id\":\"12800\",\n" +
                "                    \"key\":\"CPC\",\n" +
                "                    \"name\":\"奥琪产品池\",\n" +
                "                    \"avatarUrls\":{\n" +
                "                        \"48x48\":\"http://jira.immd.cn/secure/projectavatar?avatarId=10324\",\n" +
                "                        \"24x24\":\"http://jira.immd.cn/secure/projectavatar?size=small&avatarId=10324\",\n" +
                "                        \"16x16\":\"http://jira.immd.cn/secure/projectavatar?size=xsmall&avatarId=10324\",\n" +
                "                        \"32x32\":\"http://jira.immd.cn/secure/projectavatar?size=medium&avatarId=10324\"\n" +
                "                    }\n" +
                "                }";

        System.out.println(JSON.parseObject(s).getDouble("id"));
    }
}
