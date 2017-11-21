package cc.xiaoquer.jira.api;

import cc.xiaoquer.jira.api.beans.JiraBoard;
import cc.xiaoquer.jira.api.beans.JiraIssue;
import cc.xiaoquer.jira.api.beans.JiraProject;
import cc.xiaoquer.jira.api.beans.JiraSprint;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Realm;
import org.asynchttpclient.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Nicholas on 2017/9/4.
 */
public class JIRA {
    //https://docs.atlassian.com/jira-software/REST/cloud/
    //该api默认最大返回50条记录，所以maxResult不能大于50，且只能分页查询。
    public static final String AGILE_BOARDS_URL  = "/rest/agile/1.0/board?type=scrum&startAt={start}&maxResults=50";
    public static final String AGILE_BOARD_PROJECT_URL = "/rest/agile/1.0/board/{boardId}/project?startAt=0&maxResults=50";
    public static final String AGILE_SPRINTS_URL = "/rest/agile/1.0/board/{boardId}/sprint?startAt={start}&maxResults=50";
    public static final String AGILE_ISSUES_URL  = "/rest/agile/1.0/board/{boardId}/sprint/{sprintId}/issue?startAt={start}&maxResults={max}";
    public static final String GREENHOPPER_ISSUES_URL  = "/rest/greenhopper/1.0/xboard/work/allData.json?rapidViewId={boardId}&activeSprints={sprintId}";
    public static final String AGILE_ISSUE_URL   = "/rest/agile/1.0/issue/{issueId}";
    public static final String BROWSE_ISSUE_URL   = "/browse/{issueKey}";

    //https://confluence.atlassian.com/jiracoreserver073/advanced-searching-861257209.html#Advancedsearching-reference
    //https://developer.atlassian.com/static/rest/jira/6.1.html#d2e4071
    //*all,-comment,-watches表示查询所有属性除了comment waches
    //如此搜索用于缓存数据，不用一条一条的查询issue了
    public static final String SEARCH_JQL_BY_PROJECT_URL   = "/rest/api/2/search?startAt={start}&maxResults=50&fields=*all%2c-comment%2c-watches&jql={jql}";

    //http://jira/rest/greenhopper/1.0/xboard/plan/backlog/data.json?rapidViewId=97
    public static final String GREENHOPPER_KANBAN = "/rest/greenhopper/1.0/xboard/plan/backlog/data.json?rapidViewId={boardId}";

    public static boolean isConnected = false;
    public static String  connectStatus = "";
    public static String  serverUrl;

    private static Realm  realm;
    private static final  int PAGE_SIZE            = 50;

    /**
     * Key=BoardId, Value=JiraBoard
     */
    public static Map<String, JiraBoard> ALL_BOARD_MAP = new TreeMap<>();
//    private static Vector<String> boardNames;
    /**
     * Key=BoardId, Value=Map<String, JiraSprint>
     */
    public static Map<String, Map<String, JiraSprint>> BOARD_SPRINT_MAP = new LinkedHashMap<>();

    /**
     * Key=SprintId, Value=JiraSprint
     */
    public static Map<String, JiraSprint> ALL_SPRINT_MAP = new TreeMap<>();

    /**
     * Key=BoardId+","+SprintId, value= 只包含Story或者任务
     */
    public static Map<String, Map<String, JiraIssue>> BOARD_SPRINT_PARENT_MAP = new LinkedHashMap<>();

    /**
     * Key=IssueId
     */
    public static Map<String, JiraIssue> ALL_ISSUE_MAP = new TreeMap<>();

//    private static Map<String, JiraSprint> sprintMap;
//    private static Vector<String> sprintNames;

    private static AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();

    private JIRA() {
    }

    public static boolean connect(String url, String username, String password) {
        if (url.endsWith("/")) {
            serverUrl = url.substring(0, url.length() - 1);
        } else {
            serverUrl = url;
        }

        realm = new Realm.Builder(username, password)
                .setUsePreemptiveAuth(true)
                .setScheme(Realm.AuthScheme.BASIC)
                .build();

        if (asyncHttpClient == null) {
            asyncHttpClient = new DefaultAsyncHttpClient();
        }

        try {
            //连接就获取看板列表
            getBoardMap(true);
            isConnected = true;
            connectStatus = "连接成功";
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof JSONException) {
                if (e.getMessage().contains("Unauthorized")) {
                    connectStatus = "账号或密码错误";
                } else if (e.getMessage().contains("Forbidden")) {
                    connectStatus = "账号被锁定";
                } else {
                    connectStatus = "账号异常,联系管理员";
                }

            } else {
                Throwable t = e.getCause();
                if (t instanceof TimeoutException) {
                    connectStatus = "连接超时";
                } else if (t instanceof InterruptedException || t instanceof ExecutionException) {
                    connectStatus = "连接异常";
                } else {
                    connectStatus = "连接失败";
                }
            }
            isConnected = false;
        }

        return isConnected;
    }

    public static void disconnect() {
        serverUrl = "";
        realm = null;

        try {
            ALL_BOARD_MAP.clear();
            ALL_SPRINT_MAP.clear();
            ALL_ISSUE_MAP.clear();
            BOARD_SPRINT_MAP.clear();
            BOARD_SPRINT_PARENT_MAP.clear();

            asyncHttpClient.close();
        } catch (Exception e) {
        } finally {
            asyncHttpClient = null;
        }

    }

    public static Map<String, JiraBoard> getBoardMap(boolean forceRefresh) {
        if (forceRefresh) {
            for (int i = 0; ; i = i + PAGE_SIZE) {
                String responseBody = getResponse(serverUrl + AGILE_BOARDS_URL.replace("{start}", String.valueOf(i)));
                JiraBoard.toMap(responseBody, null);

                boolean isLast = JSON.parseObject(responseBody).getBoolean("isLast");
                if (isLast) break;
            }
        }
        return ALL_BOARD_MAP;
    }

    public static Map<String, JiraBoard> getBoardMap() {
        return getBoardMap(false);
    }

    public static JiraBoard getBoardCache(String boardId) {
        return ALL_BOARD_MAP.get(boardId);
    }

    public static Map<String, JiraProject> getProjectMap(String boardId) {
        String projectJoStr = JIRA.getResponseRelative(JIRA.AGILE_BOARD_PROJECT_URL.replace("{boardId}", boardId));
        return JiraProject.toMap(projectJoStr);
    }

    public static Map<String, JiraSprint> getSprintMap(String boardId) {
//        if (BOARD_SPRINT_MAP.get(boardId) != null) {
//            return BOARD_SPRINT_MAP.get(boardId);
//        }

        for (int i = 0;; i = i + PAGE_SIZE) {
            String responseBody = getResponse(serverUrl + AGILE_SPRINTS_URL
                    .replace("{boardId}", boardId).replace("{start}", String.valueOf(i)));

            JiraSprint.toMap(responseBody, boardId);

            boolean isLast = JSON.parseObject(responseBody).getBoolean("isLast");
            if (isLast) break;
        }

        return BOARD_SPRINT_MAP.get(boardId);
    }

    public static JiraSprint getSprintCache(String sprintId) {
        return ALL_SPRINT_MAP.get(sprintId);
    }

    public static Map<String, JiraIssue> getIssueMap(String boardId, String sprintId) {

        int issuePageSize = PAGE_SIZE * 2;

        int total = _getIssuePagination(boardId, sprintId, 0, issuePageSize);

        for (int i = issuePageSize; i <= total; i = i + issuePageSize) {
            _getIssuePagination(boardId, sprintId, i, issuePageSize);
        }

        //把子任务解析到story或者任务的类属性里
        parseSubTasks(boardId, sprintId);

        return getStoryOrTaskMap(boardId, sprintId);
    }

    /**
     * 获取issue，与jira看板中看到的顺序保持一致。
     */
    public static Map<String, JiraIssue> getIssueMapWithSameOrder(String boardId, String sprintId) {
        String key = boardId + "," + sprintId;
        Map<String, JiraIssue> thisTimeIssueMap = BOARD_SPRINT_PARENT_MAP.get(key);
        if (thisTimeIssueMap == null) {
            refreshIssuesCacheInBatch(boardId, sprintId); //用分页查询的方式获取issue存入缓存，这样比by issueId查询要快
        }

        String responseBody = getResponseRelative(GREENHOPPER_ISSUES_URL
                .replace("{boardId}", boardId)
                .replace("{sprintId}", sprintId));

        return JiraIssue.toMapWithKanbanOrder(responseBody, boardId, sprintId);
    }

    public static void refreshIssuesCacheInBatch(String boardId, String sprintId) {
        JiraBoard jiraBoard = ALL_BOARD_MAP.get(boardId);

        StringBuffer jql = new StringBuffer();
        int x = 0;

        //未知问题：BoardId查询Project可能返回空
        if (jiraBoard.getProjects().size() > 0) {
            jql.append("(");
            for (JiraProject jp : jiraBoard.getProjects().values()) {
                if (x++ > 0) {
                    jql.append(" OR ");
                }
                jql.append("Project=").append(jp.getProjectKey());
            }
            jql.append(")");

            jql.append(" AND ");
        }

        jql.append(" Sprint=").append(sprintId);

        String encodedJQL = jql.toString();
        try {
            encodedJQL = URLEncoder.encode(encodedJQL, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        int total = Integer.MAX_VALUE;
        for (int i = 0; i <= total; i = i + PAGE_SIZE) {
            String responseBody = getResponseRelative(SEARCH_JQL_BY_PROJECT_URL
                    .replace("{jql}", encodedJQL).replace("{start}", String.valueOf(i)));

            //将issue解析进入缓存
            JiraIssue.toMap(responseBody, boardId, sprintId);

            total = JSON.parseObject(responseBody).getInteger("total");
        }
    }

    /**
     * 解析story或者任务的子任务，拼装到JiraIssue类属性中
     */
    public static void parseSubTasks(String boardId, String sprintId) {
        Map<String, JiraIssue> localIssueMap = JIRA.getStoryOrTaskMap(boardId, sprintId);

        for (JiraIssue storyOrTask : localIssueMap.values()) {
            JSONArray subTasksJo = storyOrTask.getSubTaskJo();

            if (storyOrTask.isParent() && subTasksJo != null && subTasksJo.size() > 0) {
                for (int j = 0; j < subTasksJo.size(); j++) {
                    String subTaskId = subTasksJo.getJSONObject(j).getString("id");
                    JiraIssue subTask = JIRA.getIssueCached(subTaskId);

                    // 通过board/sprint/查询出的issue中可能缺失某一类issue：（在ALL_ISSUE_MAP中不存在）
                    // 这种issue本来应该在board/sprint下面的，但是他可能录入错了团队，
                    // 导致通过 /board/xx/sprint/xx/issue/xx查不到，但是 /issue/xx 查得到
                    // 所以日志只要打印了 /rest/agile/1.0/issue/16917 类似的日志，说明issue属性写错了！
                    if (subTask == null) {
                        subTask = JIRA.getIssueObjById(subTaskId, boardId, sprintId);
                        ALL_ISSUE_MAP.put(subTaskId, subTask);
//                        System.out.println("【警告】Issue " + subTask.getIssueKey() + " 的属性(比如团队或其他自定义属性)可能填写错误, 请修正！");
                    }

                    Map<String, JiraIssue> subtaskMap = storyOrTask.getSubTaskMap();
                    if (subtaskMap == null) {
                        subtaskMap = new LinkedHashMap<>();
                    }

                    subtaskMap.put(subTask.getIssueId(), subTask);
                    storyOrTask.setSubTaskMap(subtaskMap);
                }
            }
        }
    }

    public static void putStoryOrTaskMap(String boardId, String sprintId, String storyOrTaskId, JiraIssue storyOrTaskObj) {
        String key = boardId + "," + sprintId;
        Map<String, JiraIssue> thisTimeIssueMap = BOARD_SPRINT_PARENT_MAP.get(key);
        if (thisTimeIssueMap == null) {
            thisTimeIssueMap = new LinkedHashMap<>();
        }
        thisTimeIssueMap.put(storyOrTaskId, storyOrTaskObj);
        BOARD_SPRINT_PARENT_MAP.put(key, thisTimeIssueMap);
    }

    public static Map<String, JiraIssue> getStoryOrTaskMap(String boardId, String sprintId) {
        String key = boardId + "," + sprintId;
        return BOARD_SPRINT_PARENT_MAP.get(key);
    }


    private static int _getIssuePagination(String boardId, String sprintId, int start, int max) {
        String key = boardId + "," + sprintId;

        String responseBody = getResponse(serverUrl + AGILE_ISSUES_URL
                .replace("{boardId}", boardId).replace("{sprintId}", sprintId)
                .replace("{start}", String.valueOf(start))
                .replace("{max}", String.valueOf(max)));

        JiraIssue.toMap(responseBody, boardId, sprintId);

        try {
            return JSON.parseObject(responseBody).getInteger("total");
        } catch (Exception e) {
            System.out.println("获取总记录数失败！！！");
            e.printStackTrace();
            return 0;
        }
    }

    public static JiraIssue getIssueCached(String issueId) {
        try {
            return ALL_ISSUE_MAP.get(issueId);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getIssueById(String issueId) {
        // 通过board/sprint/查询出的issue中可能缺失某一类issue：（在ALL_ISSUE_MAP中不存在）
        // 这种issue本来应该在board/sprint下面的，但是他可能录入错了团队，
        // 导致通过 /board/xx/sprint/xx/issue/xx查不到，但是 /issue/xx 查得到
        // 所以日志只要打印了 /rest/agile/1.0/issue/16917 类似的日志，说明issue属性写错了！
        String url = serverUrl + (AGILE_ISSUE_URL.replace("{issueId}", issueId));
//        System.out.println("【警告警告警告】请求" + url + "对应的Issue属性(比如团队或不能是Epic或其他自定义属性)可能填写错误！");
        return getResponse(url);
    }

    /**
     * 先查询缓存，不存在再发起http请求查询
     * @param issueId
     * @param boardId
     * @param sprintId
     * @return
     */
    public static JiraIssue getIssueObjById(String issueId, String boardId, String sprintId) {
        JiraIssue jiraIssue = getIssueCached(issueId);
        if (jiraIssue == null) {
            System.out.println("该Issue不在缓存中,需刷新" + issueId);
            String tempIssueBody = JIRA.getIssueById(issueId);
            jiraIssue = JiraIssue._parse(JSON.parseObject(tempIssueBody), boardId, sprintId);
        }
        return jiraIssue;
    }

    public static String getResponseRelative(String relativeUrl) {
        String url = serverUrl + relativeUrl;
        return getResponse(url);
    }

    public static String getResponse(String url) {
        System.out.println("请求数据From：" + url);
        Future<Response> f = asyncHttpClient.prepareGet(url)
                .setRealm(realm)
                .addHeader("Accept", "application/json")
                .addHeader("X-Atlassian-Token", "nocheck")
                .execute();
        try {
            Response r = f.get(5L, TimeUnit.SECONDS);
            String s = r.getResponseBody(Charset.forName("UTF-8"));
            return s;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //显示在树节点中的名称
    public static String toIssueDisplayName(JiraIssue jiraIssue) {
        StringBuffer sb = new StringBuffer();

        sb.append("")
                .append("[").append(jiraIssue.getIssueStatus()).append("]")
                .append("[").append(jiraIssue.getIssueKey()).append("]")
                .append("[").append(jiraIssue.getIssueName()).append("]")
                .append("[").append(jiraIssue.getOwner()).append("]")
                .append("[").append(jiraIssue.getIssueId()).append("]");

        return sb.toString();
    }

    //从树节点名称中获取问题类型
    public static String getIssueStatusFromIssueDisplayName(String displayName) {
        return displayName.substring(1, displayName.indexOf("]"));
    }

    //从树节点名称中获取Id
    public static String getIdFromIssueDisplayName(String displayName) {
        int startIdx = 0;

        for (int i = displayName.length() - 1; i >= 0; i--) {
            if (displayName.charAt(i) == '[') {
                startIdx = i;
                break;
            }
        }

        return displayName.substring(startIdx + 1, displayName.length() - 1);
    }

    //根据树节点名称获取JiraIssue对象
    public static JiraIssue getObjectFromDisplayName(String displayName) {
        String id = getIdFromIssueDisplayName(displayName);
        return JIRA.getIssueCached(id);
    }


    public static void main(String[] args) throws UnsupportedEncodingException {
//        JIRA.connect("http://99.48.46.160:8080/", "nicholas.qu", "*****");
//        System.out.println(JIRA.getSprintMap("97"));
//        JIRA.disconnect();

    }
}
