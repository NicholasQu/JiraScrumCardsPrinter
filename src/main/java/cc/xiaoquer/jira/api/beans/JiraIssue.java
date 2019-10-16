package cc.xiaoquer.jira.api.beans;

import cc.xiaoquer.jira.api.JIRA;
import cc.xiaoquer.jira.storage.PropertiesCache;
import cc.xiaoquer.utils.JSONParsingUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Nicholas on 2017/9/4.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssue extends AbstractJiraEntity {
    private String issueType = "";
    private String issueId = "";
    private String issueKey = "";
    private String issueName = "";
    private String issueStatus = "";

    private String owner = "";

    private String estimate = "";   //估算时间
    private String remainingTime = ""; //剩余时间
    private String estimateInSeconds = "";   //估算时间(单位秒)
    private String remainingTimeInSeconds = ""; //剩余时间(单位秒)

    private String parentType = "";
    private String parentId = "";
    private String parentKey = "";
    private String parentName = "";

    private JiraProject jiraProject;
//    private String boardId = "";        //从jListBoard中获取，issue的接口中的orginalBoardId是错误的（ETS而不是ETS-Arch的看板）
    private JiraBoard  jiraBoard;
    private JiraSprint jiraSprint;
    private JiraEpic   jiraEpic;

    //add at 2017.9.25
    private String priority = "";
    private String createdAt = "";
    private String updatedAt = "";
    private String description = "";
    private String dueDate = "";
    private String comments = "";

    private String allJson; //缓存所有的json数据

    private boolean hasSubtask = false;
    private Map<String, JiraIssue> subTaskMap;
    private JSONArray subTaskJo;

    public JiraIssue(JiraEpic jiraEpic) {
        this.jiraEpic = jiraEpic;
    }

    private String formatDate(String d) {
        try {
            if (StringUtils.isNotBlank(d)) {
                return defaultDateFormat.format(jiraDateFormat.parse(d));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return d;
    }

    public String getCreatedAt() {
        return formatDate(this.createdAt);
    }

    public String getUpdatedAt() {
        return formatDate(this.updatedAt);
    }

    public int getPriorityNum() {
        if (priority == null) return 99;

        String priorityToNumJson = PropertiesCache.getProp(PropertiesCache.P_ISSUE_PRIORITY_KEY_TO_NUM);
        Map<String, Integer> priorityToNumMap = null;
        try {
            priorityToNumMap = JSON.parseObject(priorityToNumJson, Map.class);
        } catch (Exception ignore) {
            priorityToNumMap.put("highest", 10);
            priorityToNumMap.put("high", 30);
            priorityToNumMap.put("medium", 50);
            priorityToNumMap.put("low", 70);
            priorityToNumMap.put("lowest", 90);
        }

        return priorityToNumMap.get(priority) == null ? 50 : priorityToNumMap.get(priority);
    }

    //倒排序 填充固定长度的优先级数字,JIRA内的priority输入的数值最多只允许3位
    public String getPriority4Sort() {
        return String.valueOf(getPriorityNum());
    }

    public String getCustomEstimateDone() {
        String estimateDone = getCustomField("estimatedone");
        if (StringUtils.isNotBlank(estimateDone)) {
            return formatDate(estimateDone);
        } else {
            return "";
        }
    }
    //优先级的数字越大，优先级越高
    public String getCustomActualDone() {
        String actualdone = getCustomField("actualdone");
        if (StringUtils.isNotBlank(actualdone)) {
            return formatDate(actualdone);
        } else {
            return "";
        }
    }

//    {
//        "expand":"schema,names",
//            "startAt":0,
//            "maxResults":50,
//            "total":52,
//            "issues":[
//                {
//                    "expand":"operations,versionedRepresentations,editmeta,changelog,renderedFields",
//                        "id":"15650",
//                        "self":"http://99.48.46.160:8080/rest/agile/1.0/issue/15650",
//                        "key":"ETS-119",
//                        "fields":{
//                    "issuetype":{
//                        "self":"http://99.48.46.160:8080/rest/api/2/issuetype/10001",
//                                "id":"10001",
//                                "description":"gh.issue.story.desc",
//                                "iconUrl":"http://99.48.46.160:8080/images/icons/issuetypes/story.svg",
//                                "name":"Story",
//                                "subtask":false
//                    },
//                    "timespent":null,
//                            "sprint":{
//                        "id":88,
//                                "self":"http://99.48.46.160:8080/rest/agile/1.0/sprint/88",
//                                "state":"active",
//                                "name":"ETS 0915 Sprint",
//                                "startDate":"2017-09-04T11:55:16.390+08:00",
//                                "endDate":"2017-09-15T11:55:00.000+08:00",
//                                "originBoardId":97
//                    },
//                    "project":{
//                        "self":"http://99.48.46.160:8080/rest/api/2/project/11100",
//                                "id":"11100",
//                                "key":"ETS",
//                                "name":"ETS",
//                                "avatarUrls":{
//                            "48x48":"http://99.48.46.160:8080/secure/projectavatar?avatarId=10324",
//                                    "24x24":"http://99.48.46.160:8080/secure/projectavatar?size=small&avatarId=10324",
//                                    "16x16":"http://99.48.46.160:8080/secure/projectavatar?size=xsmall&avatarId=10324",
//                                    "32x32":"http://99.48.46.160:8080/secure/projectavatar?size=medium&avatarId=10324"
//                        }
//                    },
//                    "fixVersions":[
//
//                        ],
//                    "aggregatetimespent":null,
//                            "resolution":null,
//                            "customfield_10104":null,
//                            "customfield_10105":null,
//                            "customfield_10106":null,
//                            "customfield_10107":null,
//                            "resolutiondate":null,
//                            "workratio":-1,
//                            "lastViewed":null,
//                            "watches":{
//                        "self":"http://99.48.46.160:8080/rest/api/2/issue/ETS-119/watchers",
//                                "watchCount":1,
//                                "isWatching":false
//                    },
//                    "created":"2017-08-07T18:40:53.000+0800",
//                            "epic":{
//                        "id":15645,
//                                "key":"ETS-114",
//                                "self":"http://99.48.46.160:8080/rest/agile/1.0/epic/15645",
//                                "name":"消息中心",
//                                "summary":"消息中心",
//                                "color":{
//                            "key":"color_3"
//                        },
//                        "done":false
//                    },
//                    "priority":{
//                        "self":"http://99.48.46.160:8080/rest/api/2/priority/3",
//                                "iconUrl":"http://99.48.46.160:8080/images/icons/priorities/medium.svg",
//                                "name":"Medium",
//                                "id":"3"
//                    },
//                    "customfield_10100":null,
//                            "customfield_10101":null,
//                            "customfield_10102":null,
//                            "labels":[
//
//                        ],
//                    "customfield_10103":null,
//                            "timeestimate":null,
//                            "aggregatetimeoriginalestimate":201630,
//                            "versions":[
//
//                        ],
//                    "issuelinks":[
//
//                        ],
//                    "assignee":{
//                        "self":"http://99.48.46.160:8080/rest/api/2/user?username=qiaojicheng",
//                                "name":"qiaojicheng",
//                                "key":"qiaojicheng",
//                                "emailAddress":"jicheng.qiao@mi-me.com",
//                                "avatarUrls":{
//                            "48x48":"http://99.48.46.160:8080/secure/useravatar?ownerId=qiaojicheng&avatarId=11601",
//                                    "24x24":
//                            "http://99.48.46.160:8080/secure/useravatar?size=small&ownerId=qiaojicheng&avatarId=11601",
//                                    "16x16":
//                            "http://99.48.46.160:8080/secure/useravatar?size=xsmall&ownerId=qiaojicheng&avatarId=11601",
//                                    "32x32":
//                            "http://99.48.46.160:8080/secure/useravatar?size=medium&ownerId=qiaojicheng&avatarId=11601"
//                        },
//                        "displayName":"乔继成",
//                                "active":true,
//                                "timeZone":"Asia/Shanghai"
//                    },
//                    "updated":"2017-09-04T15:19:32.000+0800",
//                            "status":{
//                        "self":"http://99.48.46.160:8080/rest/api/2/status/10408",
//                                "description":"",
//                                "iconUrl":"http://99.48.46.160:8080/",
//                                "name":"待办",
//                                "id":"10408",
//                                "statusCategory":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/statuscategory/2",
//                                    "id":2,
//                                    "key":"new",
//                                    "colorName":"blue-gray",
//                                    "name":"待办"
//                        }
//                    },
//                    "components":[
//
//                        ],
//                    "timeoriginalestimate":null,
//                            "description":"作为海米业务系统，我想要和消息中心进行解耦，这样我就可以使用消息中心的模板进行独立配置消息，并通过消息中心发送消息，而不用消息中心做任何配置。",
//                            "timetracking":{
//
//                    },
//                    "customfield_10006":null,
//                            "customfield_10601":[
//                    {
//                        "self":"http://99.48.46.160:8080/rest/api/2/customFieldOption/10708",
//                            "value":"架构",
//                            "id":"10708"
//                    }
//                        ],
//                    "attachment":[
//
//                        ],
//                    "customfield_10603":null,
//                            "aggregatetimeestimate":172800,
//                            "flagged":false,
//                            "summary":"消息网关介入海米（原消息平台改造）",
//                            "creator":{
//                        "self":"http://99.48.46.160:8080/rest/api/2/user?username=yan.qian",
//                                "name":"yan.qian",
//                                "key":"yan.qian",
//                                "emailAddress":"yan.qian@mi-me.com",
//                                "avatarUrls":{
//                            "48x48":"http://www.gravatar.com/avatar/2ad0f4aa3da8bfa5fae5195cbc4f5fed?d=mm&s=48",
//                                    "24x24":"http://www.gravatar.com/avatar/2ad0f4aa3da8bfa5fae5195cbc4f5fed?d=mm&s=24",
//                                    "16x16":"http://www.gravatar.com/avatar/2ad0f4aa3da8bfa5fae5195cbc4f5fed?d=mm&s=16",
//                                    "32x32":"http://www.gravatar.com/avatar/2ad0f4aa3da8bfa5fae5195cbc4f5fed?d=mm&s=32"
//                        },
//                        "displayName":"钱雁",
//                                "active":true,
//                                "timeZone":"Asia/Shanghai"
//                    },
//                    "subtasks":[
//                    {
//                        "id":"16257",
//                            "key":"ETS-348",
//                            "self":"http://99.48.46.160:8080/rest/api/2/issue/16257",
//                            "fields":{
//                        "summary":"生产端组件业务系统接入启动会",
//                                "status":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/status/10001",
//                                    "description":"",
//                                    "iconUrl":"http://99.48.46.160:8080/",
//                                    "name":"完成",
//                                    "id":"10001",
//                                    "statusCategory":{
//                                "self":"http://99.48.46.160:8080/rest/api/2/statuscategory/3",
//                                        "id":3,
//                                        "key":"done",
//                                        "colorName":"green",
//                                        "name":"Done"
//                            }
//                        },
//                        "priority":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/priority/3",
//                                    "iconUrl":"http://99.48.46.160:8080/images/icons/priorities/medium.svg",
//                                    "name":"Medium",
//                                    "id":"3"
//                        },
//                        "issuetype":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/issuetype/10006",
//                                    "id":"10006",
//                                    "description":"问题的子任务",
//                                    "iconUrl":
//                            "http://99.48.46.160:8080/secure/viewavatar?size=xsmall&avatarId=10316&avatarType=issuetype",
//                                    "name":"子任务",
//                                    "subtask":true,
//                                    "avatarId":10316
//                        }
//                    }
//                    },
//                    {
//                        "id":"16258",
//                            "key":"ETS-349",
//                            "self":"http://99.48.46.160:8080/rest/api/2/issue/16258",
//                            "fields":{
//                        "summary":"现金贷进度跟进",
//                                "status":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/status/10001",
//                                    "description":"",
//                                    "iconUrl":"http://99.48.46.160:8080/",
//                                    "name":"完成",
//                                    "id":"10001",
//                                    "statusCategory":{
//                                "self":"http://99.48.46.160:8080/rest/api/2/statuscategory/3",
//                                        "id":3,
//                                        "key":"done",
//                                        "colorName":"green",
//                                        "name":"Done"
//                            }
//                        },
//                        "priority":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/priority/3",
//                                    "iconUrl":"http://99.48.46.160:8080/images/icons/priorities/medium.svg",
//                                    "name":"Medium",
//                                    "id":"3"
//                        },
//                        "issuetype":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/issuetype/10006",
//                                    "id":"10006",
//                                    "description":"问题的子任务",
//                                    "iconUrl":
//                            "http://99.48.46.160:8080/secure/viewavatar?size=xsmall&avatarId=10316&avatarType=issuetype",
//                                    "name":"子任务",
//                                    "subtask":true,
//                                    "avatarId":10316
//                        }
//                    }
//                    },
//                    {
//                        "id":"16271",
//                            "key":"ETS-358",
//                            "self":"http://99.48.46.160:8080/rest/api/2/issue/16271",
//                            "fields":{
//                        "summary":"技术支持联调",
//                                "status":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/status/10001",
//                                    "description":"",
//                                    "iconUrl":"http://99.48.46.160:8080/",
//                                    "name":"完成",
//                                    "id":"10001",
//                                    "statusCategory":{
//                                "self":"http://99.48.46.160:8080/rest/api/2/statuscategory/3",
//                                        "id":3,
//                                        "key":"done",
//                                        "colorName":"green",
//                                        "name":"Done"
//                            }
//                        },
//                        "priority":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/priority/3",
//                                    "iconUrl":"http://99.48.46.160:8080/images/icons/priorities/medium.svg",
//                                    "name":"Medium",
//                                    "id":"3"
//                        },
//                        "issuetype":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/issuetype/10006",
//                                    "id":"10006",
//                                    "description":"问题的子任务",
//                                    "iconUrl":
//                            "http://99.48.46.160:8080/secure/viewavatar?size=xsmall&avatarId=10316&avatarType=issuetype",
//                                    "name":"子任务",
//                                    "subtask":true,
//                                    "avatarId":10316
//                        }
//                    }
//                    },
//                    {
//                        "id":"16309",
//                            "key":"ETS-370",
//                            "self":"http://99.48.46.160:8080/rest/api/2/issue/16309",
//                            "fields":{
//                        "summary":"接入qq沟通群创建",
//                                "status":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/status/10001",
//                                    "description":"",
//                                    "iconUrl":"http://99.48.46.160:8080/",
//                                    "name":"完成",
//                                    "id":"10001",
//                                    "statusCategory":{
//                                "self":"http://99.48.46.160:8080/rest/api/2/statuscategory/3",
//                                        "id":3,
//                                        "key":"done",
//                                        "colorName":"green",
//                                        "name":"Done"
//                            }
//                        },
//                        "priority":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/priority/3",
//                                    "iconUrl":"http://99.48.46.160:8080/images/icons/priorities/medium.svg",
//                                    "name":"Medium",
//                                    "id":"3"
//                        },
//                        "issuetype":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/issuetype/10006",
//                                    "id":"10006",
//                                    "description":"问题的子任务",
//                                    "iconUrl":
//                            "http://99.48.46.160:8080/secure/viewavatar?size=xsmall&avatarId=10316&avatarType=issuetype",
//                                    "name":"子任务",
//                                    "subtask":true,
//                                    "avatarId":10316
//                        }
//                    }
//                    },
//                    {
//                        "id":"16311",
//                            "key":"ETS-372",
//                            "self":"http://99.48.46.160:8080/rest/api/2/issue/16311",
//                            "fields":{
//                        "summary":"接入资料wiki整理",
//                                "status":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/status/10001",
//                                    "description":"",
//                                    "iconUrl":"http://99.48.46.160:8080/",
//                                    "name":"完成",
//                                    "id":"10001",
//                                    "statusCategory":{
//                                "self":"http://99.48.46.160:8080/rest/api/2/statuscategory/3",
//                                        "id":3,
//                                        "key":"done",
//                                        "colorName":"green",
//                                        "name":"Done"
//                            }
//                        },
//                        "priority":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/priority/3",
//                                    "iconUrl":"http://99.48.46.160:8080/images/icons/priorities/medium.svg",
//                                    "name":"Medium",
//                                    "id":"3"
//                        },
//                        "issuetype":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/issuetype/10006",
//                                    "id":"10006",
//                                    "description":"问题的子任务",
//                                    "iconUrl":
//                            "http://99.48.46.160:8080/secure/viewavatar?size=xsmall&avatarId=10316&avatarType=issuetype",
//                                    "name":"子任务",
//                                    "subtask":true,
//                                    "avatarId":10316
//                        }
//                    }
//                    },
//                    {
//                        "id":"16392",
//                            "key":"ETS-416",
//                            "self":"http://99.48.46.160:8080/rest/api/2/issue/16392",
//                            "fields":{
//                        "summary":"消息中心-生产端新增业务消息发送功能方案设计并和业务方沟通",
//                                "status":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/status/10001",
//                                    "description":"",
//                                    "iconUrl":"http://99.48.46.160:8080/",
//                                    "name":"完成",
//                                    "id":"10001",
//                                    "statusCategory":{
//                                "self":"http://99.48.46.160:8080/rest/api/2/statuscategory/3",
//                                        "id":3,
//                                        "key":"done",
//                                        "colorName":"green",
//                                        "name":"Done"
//                            }
//                        },
//                        "priority":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/priority/3",
//                                    "iconUrl":"http://99.48.46.160:8080/images/icons/priorities/medium.svg",
//                                    "name":"Medium",
//                                    "id":"3"
//                        },
//                        "issuetype":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/issuetype/10006",
//                                    "id":"10006",
//                                    "description":"问题的子任务",
//                                    "iconUrl":
//                            "http://99.48.46.160:8080/secure/viewavatar?size=xsmall&avatarId=10316&avatarType=issuetype",
//                                    "name":"子任务",
//                                    "subtask":true,
//                                    "avatarId":10316
//                        }
//                    }
//                    },
//                    {
//                        "id":"16407",
//                            "key":"ETS-419",
//                            "self":"http://99.48.46.160:8080/rest/api/2/issue/16407",
//                            "fields":{
//                        "summary":"消息中心-生产端新增业务方消息发送功能编码开发并自测",
//                                "status":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/status/10001",
//                                    "description":"",
//                                    "iconUrl":"http://99.48.46.160:8080/",
//                                    "name":"完成",
//                                    "id":"10001",
//                                    "statusCategory":{
//                                "self":"http://99.48.46.160:8080/rest/api/2/statuscategory/3",
//                                        "id":3,
//                                        "key":"done",
//                                        "colorName":"green",
//                                        "name":"Done"
//                            }
//                        },
//                        "priority":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/priority/3",
//                                    "iconUrl":"http://99.48.46.160:8080/images/icons/priorities/medium.svg",
//                                    "name":"Medium",
//                                    "id":"3"
//                        },
//                        "issuetype":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/issuetype/10006",
//                                    "id":"10006",
//                                    "description":"问题的子任务",
//                                    "iconUrl":
//                            "http://99.48.46.160:8080/secure/viewavatar?size=xsmall&avatarId=10316&avatarType=issuetype",
//                                    "name":"子任务",
//                                    "subtask":true,
//                                    "avatarId":10316
//                        }
//                    }
//                    },
//                    {
//                        "id":"16609",
//                            "key":"ETS-438",
//                            "self":"http://99.48.46.160:8080/rest/api/2/issue/16609",
//                            "fields":{
//                        "summary":"目前现金贷业务为海米APP提供新消息发送进度跟踪",
//                                "status":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/status/10100",
//                                    "description":"",
//                                    "iconUrl":"http://99.48.46.160:8080/images/icons/status_generic.gif",
//                                    "name":"进行中",
//                                    "id":"10100",
//                                    "statusCategory":{
//                                "self":"http://99.48.46.160:8080/rest/api/2/statuscategory/4",
//                                        "id":4,
//                                        "key":"indeterminate",
//                                        "colorName":"yellow",
//                                        "name":"进行中"
//                            }
//                        },
//                        "priority":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/priority/3",
//                                    "iconUrl":"http://99.48.46.160:8080/images/icons/priorities/medium.svg",
//                                    "name":"Medium",
//                                    "id":"3"
//                        },
//                        "issuetype":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/issuetype/10006",
//                                    "id":"10006",
//                                    "description":"问题的子任务",
//                                    "iconUrl":
//                            "http://99.48.46.160:8080/secure/viewavatar?size=xsmall&avatarId=10316&avatarType=issuetype",
//                                    "name":"子任务",
//                                    "subtask":true,
//                                    "avatarId":10316
//                        }
//                    }
//                    },
//                    {
//                        "id":"16916",
//                            "key":"ETS-569",
//                            "self":"http://99.48.46.160:8080/rest/api/2/issue/16916",
//                            "fields":{
//                        "summary":"新增爱海米APP消息模板",
//                                "status":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/status/10001",
//                                    "description":"",
//                                    "iconUrl":"http://99.48.46.160:8080/",
//                                    "name":"完成",
//                                    "id":"10001",
//                                    "statusCategory":{
//                                "self":"http://99.48.46.160:8080/rest/api/2/statuscategory/3",
//                                        "id":3,
//                                        "key":"done",
//                                        "colorName":"green",
//                                        "name":"Done"
//                            }
//                        },
//                        "priority":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/priority/3",
//                                    "iconUrl":"http://99.48.46.160:8080/images/icons/priorities/medium.svg",
//                                    "name":"Medium",
//                                    "id":"3"
//                        },
//                        "issuetype":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/issuetype/10006",
//                                    "id":"10006",
//                                    "description":"问题的子任务",
//                                    "iconUrl":
//                            "http://99.48.46.160:8080/secure/viewavatar?size=xsmall&avatarId=10316&avatarType=issuetype",
//                                    "name":"子任务",
//                                    "subtask":true,
//                                    "avatarId":10316
//                        }
//                    }
//                    },
//                    {
//                        "id":"16918",
//                            "key":"ETS-571",
//                            "self":"http://99.48.46.160:8080/rest/api/2/issue/16918",
//                            "fields":{
//                        "summary":"修改代码支持爱海米消息发送",
//                                "status":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/status/10001",
//                                    "description":"",
//                                    "iconUrl":"http://99.48.46.160:8080/",
//                                    "name":"完成",
//                                    "id":"10001",
//                                    "statusCategory":{
//                                "self":"http://99.48.46.160:8080/rest/api/2/statuscategory/3",
//                                        "id":3,
//                                        "key":"done",
//                                        "colorName":"green",
//                                        "name":"Done"
//                            }
//                        },
//                        "priority":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/priority/3",
//                                    "iconUrl":"http://99.48.46.160:8080/images/icons/priorities/medium.svg",
//                                    "name":"Medium",
//                                    "id":"3"
//                        },
//                        "issuetype":{
//                            "self":"http://99.48.46.160:8080/rest/api/2/issuetype/10006",
//                                    "id":"10006",
//                                    "description":"问题的子任务",
//                                    "iconUrl":
//                            "http://99.48.46.160:8080/secure/viewavatar?size=xsmall&avatarId=10316&avatarType=issuetype",
//                                    "name":"子任务",
//                                    "subtask":true,
//                                    "avatarId":10316
//                        }
//                    }
//                    }
//                        ],
//                    "reporter":{
//                        "self":"http://99.48.46.160:8080/rest/api/2/user?username=yan.qian",
//                                "name":"yan.qian",
//                                "key":"yan.qian",
//                                "emailAddress":"yan.qian@mi-me.com",
//                                "avatarUrls":{
//                            "48x48":"http://www.gravatar.com/avatar/2ad0f4aa3da8bfa5fae5195cbc4f5fed?d=mm&s=48",
//                                    "24x24":"http://www.gravatar.com/avatar/2ad0f4aa3da8bfa5fae5195cbc4f5fed?d=mm&s=24",
//                                    "16x16":"http://www.gravatar.com/avatar/2ad0f4aa3da8bfa5fae5195cbc4f5fed?d=mm&s=16",
//                                    "32x32":"http://www.gravatar.com/avatar/2ad0f4aa3da8bfa5fae5195cbc4f5fed?d=mm&s=32"
//                        },
//                        "displayName":"钱雁",
//                                "active":true,
//                                "timeZone":"Asia/Shanghai"
//                    },
//                    "customfield_10000":"0|i00r02:zzzx",
//                            "aggregateprogress":{
//                        "progress":0,
//                                "total":172800,
//                                "percent":0
//                    },
//                    "customfield_10001":[
//                    "com.atlassian.greenhopper.service.sprint.Sprint@638148bd[id=84,rapidViewId=95,state=CLOSED,name=ETS 0901 Sprint,startDate=2017-08-21T12:58:21.482+08:00,endDate=2017-09-01T12:58:00.000+08:00,completeDate=2017-09-04T11:51:53.968+08:00,sequence=84]",
//                            "com.atlassian.greenhopper.service.sprint.Sprint@72c45c71[id=88,rapidViewId=97,state=ACTIVE,name=ETS 0915 Sprint,startDate=2017-09-04T11:55:16.390+08:00,endDate=2017-09-15T11:55:00.000+08:00,completeDate=<null>,sequence=88]"
//                        ],
//                    "customfield_10002":"ETS-114",
//                            "customfield_10200":null,
//                            "environment":null,
//                            "duedate":null,
//                            "closedSprints":[
//                    {
//                        "id":84,
//                            "self":"http://99.48.46.160:8080/rest/agile/1.0/sprint/84",
//                            "state":"closed",
//                            "name":"ETS 0901 Sprint",
//                            "startDate":"2017-08-21T12:58:21.482+08:00",
//                            "endDate":"2017-09-01T12:58:00.000+08:00",
//                            "completeDate":"2017-09-04T11:51:53.968+08:00",
//                            "originBoardId":95
//                    }
//                        ],
//                    "progress":{
//                        "progress":0,
//                                "total":0
//                    },
//                    "comment":{
//                        "comments":[
//
//                            ],
//                        "maxResults":0,
//                                "total":0,
//                                "startAt":0
//                    },
//                    "votes":{
//                        "self":"http://99.48.46.160:8080/rest/api/2/issue/ETS-119/votes",
//                                "votes":0,
//                                "hasVoted":false
//                    },
//                    "worklog":{
//                        "startAt":0,
//                                "maxResults":20,
//                                "total":0,
//                                "worklogs":[
//
//                            ]
//                    }
//                }
//                }
//        ]
//    }

    //解析应答，顺序与Jira看板保持一致
    public static Map<String, JiraIssue> toMapWithKanbanOrder(String responseBody, String boardId, String sprintId) {
        Map<String, JiraIssue> sameOrderIssueMap = new LinkedHashMap<String, JiraIssue>();
        Map<String, JiraIssue> noSubTaskIssueMap = new LinkedHashMap<String, JiraIssue>(); //没有子任务的issue沉到底端 在看板中是Other Issue一类

        JSONObject responseJo = JSON.parseObject(responseBody);
        JSONArray  issuesArrayInJiraOrder = responseJo.getJSONObject("issuesData").getJSONArray("issues");

        if (issuesArrayInJiraOrder != null && issuesArrayInJiraOrder.size() > 0) {

            for (int i = 0; i < issuesArrayInJiraOrder.size(); i++) {
                String issueId = String.valueOf(issuesArrayInJiraOrder.getJSONObject(i).getInteger("id"));

                JiraIssue issueObj = JIRA.getIssueObjById(issueId, boardId, sprintId);

                _handleMap(issueObj, boardId, sprintId, sameOrderIssueMap, noSubTaskIssueMap);
            }
        }

        sameOrderIssueMap.putAll(noSubTaskIssueMap);

        return sameOrderIssueMap;
    }

    private static void _handleMap(JiraIssue issueObj,
                                  String boardId, String sprintId,
                                  Map<String, JiraIssue> sameOrderIssueMap,
                                  Map<String, JiraIssue> noSubTaskIssueMap) {
        JiraIssue parentIssue = null;

        boolean needPutParent = false;
        String parentId = issueObj.getParentId();
        if (issueObj.isParent()) {
            parentIssue = issueObj;
            needPutParent = true;
        } else if (parentIssue == null
                || (parentId != null && parentId.length()> 0
                && !parentIssue.getIssueId().equals(parentId))) {
            //由于Jira过滤器的存在，可能parent的团队录错了，导致api返回的第一个值是子任务，在报文中会出现 missingParents 的字段
            parentIssue = JIRA.getIssueObjById(parentId, boardId, sprintId);
            needPutParent = true;
        }

        if (needPutParent && parentIssue != null) {
            if (parentIssue.hasSubtask) {
                //树形结构里只放epic, story或者任务，等可包含子任务的节点
                sameOrderIssueMap.put(parentIssue.getIssueId(), parentIssue);
            } else {
                noSubTaskIssueMap.put(parentIssue.getIssueId(), parentIssue);
            }
        }

        if (!issueObj.isParent()) {
            Map<String, JiraIssue> subtaskMap = parentIssue.getSubTaskMap();
            if (subtaskMap == null) {
                subtaskMap = new LinkedHashMap<String, JiraIssue>();
            }

            subtaskMap.put(issueObj.getIssueId(), issueObj);
            parentIssue.setSubTaskMap(subtaskMap);
        }
    }

    //优先级（high medium low）, 产品状态（doing>todo>done）
    public Map<String, JiraIssue> getSortedSubTaskMap() {
        Map<String, JiraIssue> sortedSubTaskMap = new TreeMap<>();

        for (JiraIssue subJiraIssue : this.getSubTaskMap().values()) {
            sortedSubTaskMap.put(subJiraIssue.getSortedKey(), subJiraIssue);
        }

        return sortedSubTaskMap;
    }

    public String getSortedKey() {
        return this.getPriority4Sort()
                + this.getStatusCategory4Sort()
                + StringUtils.leftPad(this.getIssueId(), 10, "0");
    }


    //已关闭的Sprint从缓存里面取issue
    public static Map<String, JiraIssue> toMap4FromCache(Map<String, JiraIssue> jiraIssueMap,String boardId, String sprintId) {
        Map<String, JiraIssue> sameOrderIssueMap = new LinkedHashMap<String, JiraIssue>();
        Map<String, JiraIssue> noSubTaskIssueMap = new LinkedHashMap<String, JiraIssue>(); //没有子任务的issue沉到底端 在看板中是Other Issue一类

        if (jiraIssueMap != null && jiraIssueMap.size() > 0) {
            for (JiraIssue issueObj : jiraIssueMap.values()) {
                if (issueObj.hasSubtask) {
                    JSONArray subIssueArray = issueObj.getSubTaskJo();
                    sameOrderIssueMap.put(issueObj.getIssueId(), issueObj);

                    for (int i = 0; i < subIssueArray.size(); i++) {
                        String subissueId = String.valueOf(subIssueArray.getJSONObject(i).getInteger("id"));
                        JiraIssue subIssue = JIRA.getIssueObjById(subissueId, boardId, sprintId);

                        Map<String, JiraIssue> subIssueMap = issueObj.getSubTaskMap();
                        if (subIssueMap == null) subIssueMap = new LinkedHashMap<String, JiraIssue>();
                        subIssueMap.put(subissueId, subIssue);
                        issueObj.setSubTaskMap(subIssueMap);
                    }
                }
            }
        }

        return sameOrderIssueMap;
    }

    //解析缓存用
    public static Map<String, JiraIssue> toMap(String responseBody, String boardId, String sprintId) {
        Map<String, JiraIssue> localIssueMap = new LinkedHashMap<>();

        JSONObject jsonObject = JSON.parseObject(responseBody);

        JSONArray issueArray = jsonObject.getJSONArray("issues");
        if (issueArray == null)  return localIssueMap;

        for (int i = 0; i < issueArray.size(); i++) {
            JSONObject backlogJo = issueArray.getJSONObject(i);
            JSONObject fieldJo = backlogJo.getJSONObject("fields");

            JiraIssue backlog = _parse(backlogJo, boardId, sprintId);

            localIssueMap.put(backlog.getIssueId(), backlog);            //本次issue的集合Map
            JIRA.ALL_ISSUE_MAP.put(backlog.getIssueId(), backlog);  //全局issue的集合Map

            JSONArray subTasksJo = fieldJo.getJSONArray("subtasks");
            backlog.setSubTaskJo(subTasksJo);
        }

        //树形结构里只放story或者任务，用于展示
        for (JiraIssue jiraIssue : localIssueMap.values()) {
            if (jiraIssue.isParent()) {
                JIRA.putStoryOrTaskMap(boardId, sprintId, jiraIssue.getIssueId(), jiraIssue);
            }
        }

        return localIssueMap;
    }

    public boolean isParent() {
        return !isSubTask();
    }
//    public boolean isStoryOrTask() {
//        return isStory() || isTask();
//    }
    public boolean isEpic() {
        return "Epic".equalsIgnoreCase(this.getIssueType());
    }
    public boolean isStory() {
        return "Story".equalsIgnoreCase(this.getIssueType());
    }
    public boolean isTask() {
        return "任务".equalsIgnoreCase(this.getIssueType());
    }
    public boolean isSubTask() {
        return "子任务".equalsIgnoreCase(this.getIssueType());
    }

    public void setProjectId(String projectId) {
        getJiraProject().setProjectId(projectId);
    }

    public String getProjectId() {
        return this.getJiraProject().getProjectId();
    }

    public JiraProject getJiraProject() {
        if (this.jiraProject == null) {
            this.jiraProject = new JiraProject();
        }
        return this.jiraProject;
    }

    public void setBoardId(String boardId) {
        getJiraBoard().setBoardId(boardId);
    }
    public String getBoardId() {
        return this.getJiraBoard().getBoardId();
    }

    public JiraBoard getJiraBoard() {
        if (this.jiraBoard == null) {
            this.jiraBoard = new JiraBoard();
        }
        return this.jiraBoard;
    }

    public void setSprintId(String sprintId) {
        getJiraSprint().setSprintId(sprintId);
    }

    public String getSprintId() {
        return this.getJiraSprint().getSprintId();
    }

    public JiraSprint getJiraSprint() {
        if (this.jiraSprint == null) {
            this.jiraSprint = new JiraSprint();
        }
        return this.jiraSprint;
    }

    public void setEpicId(String epicId) {
        getJiraEpic().setEpicId(epicId);
    }

    public JiraEpic getJiraEpic() {
        if (this.jiraEpic == null) {
            this.jiraEpic = new JiraEpic();
        }
        return this.jiraEpic;
    }

    public static JiraIssue _parse(JSONObject inputJo, String boardId, String sprintId) {
        refreshStatusCategory();

        JiraIssue jiraIssue = new JiraIssue();
        jiraIssue.setAllJson(inputJo.toJSONString());

        jiraIssue.setIssueId(inputJo.getString("id"));
        jiraIssue.setIssueKey(inputJo.getString("key"));

        JSONObject fieldJo = inputJo.getJSONObject("fields");
        jiraIssue.setIssueType(fieldJo.getJSONObject("issuetype").getString("name"));
        jiraIssue.setIssueName(fieldJo.getString("summary"));
        jiraIssue.setIssueStatus(fieldJo.getJSONObject("status").getString("name"));
        jiraIssue.setStatusCategoryKey(fieldJo.getJSONObject("status").getJSONObject("statusCategory").getString("key"));

        jiraIssue.setBoardId(boardId);

        //设置Sprint对象
        jiraIssue.setSprintId(sprintId);
        JSONObject sprintJo = fieldJo.getJSONObject("sprint");
        if (sprintJo == null) {
            //只有spring active的时候才能取到sprint属性，否则是closeSprints数组
            JSONArray sprintArray = fieldJo.getJSONArray("closedSprints");
            if (sprintArray != null) {
                sprintJo = sprintArray.getJSONObject(0);

                if (sprintId != null) {
                    for (int i = 0; i < sprintArray.size(); i++) {
                        sprintJo = sprintArray.getJSONObject(i);
                        if (sprintId.equals(sprintJo.getString("id"))) {
                            break;
                        }
                    }
                }
            }
        }
        if (sprintJo != null) {
            jiraIssue.getJiraSprint().setJSONObject(sprintJo);
        }

        //设置Project对象
        JSONObject projectJo = fieldJo.getJSONObject("project");
        if (projectJo != null) {
            jiraIssue.getJiraProject().setJSONObject(projectJo);
        }

        //设置Epic对象
        JSONObject epicJo = fieldJo.getJSONObject("epic");
        if (epicJo != null) {
            jiraIssue.getJiraEpic().setJSONObject(epicJo);
        }

        try {
            jiraIssue.setOwner(nullToBlank(fieldJo.getJSONObject("assignee").getString("displayName")));
        } catch (Exception e) {
            jiraIssue.setOwner("");
        }

        try {
            jiraIssue.setEpicId(nullToBlank(fieldJo.getJSONObject("epic").getString("id")));
        } catch (Exception e) {
            jiraIssue.setEpicId("");
        }

        try {
            jiraIssue.getJiraEpic().setEpicKey(nullToBlank(fieldJo.getJSONObject("epic").getString("key")));
        } catch (Exception e) {
            jiraIssue.getJiraEpic().setEpicKey("");
        }

        try {
            jiraIssue.getJiraEpic().setEpicName(nullToBlank(fieldJo.getJSONObject("epic").getString("summary")));
        } catch (Exception e) {
            jiraIssue.getJiraEpic().setEpicName("");
        }

        try {
            jiraIssue.setEstimate(nullToBlank(fieldJo.getJSONObject("timetracking").getString("originalEstimate")));
            jiraIssue.setEstimateInSeconds(nullToBlank(fieldJo.getJSONObject("timetracking").getString("originalEstimateSeconds")));
        } catch (Exception e) {
            jiraIssue.setEstimate("");
            jiraIssue.setEstimateInSeconds("");
        }
        try {
            jiraIssue.setRemainingTime(nullToBlank(fieldJo.getJSONObject("timetracking").getString("remainingEstimate")));
            jiraIssue.setRemainingTimeInSeconds(nullToBlank(fieldJo.getJSONObject("timetracking").getString("remainingEstimateSeconds")));
        } catch (Exception e) {
            jiraIssue.setRemainingTime("");
            jiraIssue.setRemainingTimeInSeconds("");
        }

        try {
            jiraIssue.setPriority(nullToBlank(fieldJo.getJSONObject("priority").getString("name")));
        } catch (Exception e) {
            jiraIssue.setPriority("");
        }
        try {
            jiraIssue.setCreatedAt(nullToBlank(fieldJo.getString("created")));
        } catch (Exception e) {
            jiraIssue.setCreatedAt("");
        }
        try {
            jiraIssue.setUpdatedAt(nullToBlank(fieldJo.getString("updated")));
        } catch (Exception e) {
            jiraIssue.setUpdatedAt("");
        }
        try {
            jiraIssue.setDescription(nullToBlank(fieldJo.getString("description")));
        } catch (Exception e) {
            jiraIssue.setDescription("");
        }
        try {
            jiraIssue.setDueDate(nullToBlank(fieldJo.getString("duedate")));
        } catch (Exception e) {
            jiraIssue.setDueDate("");
        }

        StringBuffer commentStr = new StringBuffer();
        try {
            JSONObject commentArrayJO = fieldJo.getJSONObject("comment");
            int totalComments = commentArrayJO.getInteger("total");

            if (totalComments > 0) {
                JSONArray commentsArray = commentArrayJO.getJSONArray("comments");
                //倒排序只取最后的3个（JIRA默认是按照created时间正排序的，这里因为只取3个，不用updated倒排序了）
                for (int i = commentsArray.size() - 1; i >= 0 && i >= commentsArray.size() - 3; i--) {
                    JSONObject commentJO = commentsArray.getJSONObject(i);
                    String updated = commentJO.getString("updated");
                    String body = commentJO.getString("body");
                    String author = commentJO.getJSONObject("author").getString("displayName");

                    String updatedFormtted = jiraIssue.defaultDateFormat.format(jiraIssue.jiraDateFormat.parse(updated));

                    if (commentStr.length() > 0) {
                        commentStr.append("\r\n");
                    }
                    commentStr.append(updatedFormtted).append(" ").append(author).append(": \r\n").append(body);
                }
            }
        } catch (Exception e) {
        }
        jiraIssue.setComments(commentStr.toString());


        //2018-3-22 Nicholas
        //递归解析JSON映射关系Map, 调用JIRA.AGILE_ISSUE_URL返回的JSON。
        //根节点是基于fields，对应的动态添加的Key写法应该是 fields>assignee>name
        //示例：
//        fields>assignee>displayName ============= 曲
//        fields>assignee>name ============= nicholas.qu
//        fields>assignee>self ============= http://xxxxx/rest/api/2/user?username=debao.fan
//        fields>assignee>active ============= true
//        fields>assignee>timeZone ============= Asia/Shanghai
//        fields>assignee>key ============= nicholas
//        fields>worklog>total ============= 0
//        fields>worklog>maxResults ============= 20
//        fields>worklog>startAt ============= 0
//        fields>updated ============= 2018-03-16T17:34:54.000+0800
        jiraIssue.getCustomFields().putAll(JSONParsingUtil.getAllMapping(inputJo.toString()));

        //Team customfield_10601
        try {
            jiraIssue.getCustomFields().put("teamName", nullToBlank(fieldJo.getJSONArray("customfield_10601").getJSONObject(0).getString("value")));
            jiraIssue.getCustomFields().put("teamId",   nullToBlank(fieldJo.getJSONArray("customfield_10601").getJSONObject(0).getString("id")));
        } catch (Exception e) {
            jiraIssue.getCustomFields().put("teamName", "");
            jiraIssue.getCustomFields().put("teamId",   boardId);
        }

        JSONObject parentJo = fieldJo.getJSONObject("parent");
        if (parentJo != null && parentJo.size() > 0) {
            jiraIssue.setParentId(nullToBlank(parentJo.getString("id")));
            jiraIssue.setParentKey(nullToBlank(parentJo.getString("key")));
            jiraIssue.setParentName(nullToBlank(parentJo.getJSONObject("fields").getString("summary")));
            jiraIssue.setParentType(nullToBlank(parentJo.getJSONObject("fields").getJSONObject("issuetype").getString("name")));

        } else if (jiraIssue.isParent()) { //(jiraIssue.getEpicKey() != null && jiraIssue.getEpicKey().length() > 0) {
            jiraIssue.setParentType("Epic");
            jiraIssue.setParentId(nullToBlank(jiraIssue.getJiraEpic().getEpicId()));
            jiraIssue.setParentKey(nullToBlank(jiraIssue.getJiraEpic().getEpicKey()));
            jiraIssue.setParentName(nullToBlank(jiraIssue.getJiraEpic().getEpicName()));
        }

        JSONArray subTasksJo = fieldJo.getJSONArray("subtasks");
        if (subTasksJo == null || subTasksJo.size() == 0) {
            jiraIssue.setHasSubtask(false);
        } else {
            jiraIssue.setHasSubtask(true);
        }

        return jiraIssue;
    }


}
