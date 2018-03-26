package cc.xiaoquer.jira.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.JSONToken;

import java.io.IOException;
import java.util.*;

/**
 * Created by Nicholas on 2018/3/16.
 */
public class JSONParsingUtil {

    public static void main(String[] args) throws IOException {
        String rawJson = "{\"expand\":\"renderedFields,names,schema,operations,editmeta,changelog,versionedRepresentations\",\"id\":\"24976\",\"self\":\"http://99.48.46.160:8080/rest/agile/1.0/issue/24976\",\"key\":\"BAOSHENG-112\",\"fields\":{\"issuetype\":{\"self\":\"http://99.48.46.160:8080/rest/api/2/issuetype/10006\",\"id\":\"10006\",\"description\":\"问题的子任务\",\"iconUrl\":\"http://99.48.46.160:8080/secure/viewavatar?size=xsmall&avatarId=10316&avatarType=issuetype\",\"name\":\"子任务\",\"subtask\":true,\"avatarId\":10316},\"parent\":{\"id\":\"24975\",\"key\":\"BAOSHENG-111\",\"self\":\"http://99.48.46.160:8080/rest/api/2/issue/24975\",\"fields\":{\"summary\":\"【技术准备事宜】运维技术准备工作\",\"status\":{\"self\":\"http://99.48.46.160:8080/rest/api/2/status/10701\",\"description\":\"\",\"iconUrl\":\"http://99.48.46.160:8080/\",\"name\":\"进行中\",\"id\":\"10701\",\"statusCategory\":{\"self\":\"http://99.48.46.160:8080/rest/api/2/statuscategory/4\",\"id\":4,\"key\":\"indeterminate\",\"colorName\":\"yellow\",\"name\":\"进行中\"}},\"priority\":{\"self\":\"http://99.48.46.160:8080/rest/api/2/priority/3\",\"iconUrl\":\"http://99.48.46.160:8080/images/icons/priorities/medium.svg\",\"name\":\"Medium\",\"id\":\"3\"},\"issuetype\":{\"self\":\"http://99.48.46.160:8080/rest/api/2/issuetype/10001\",\"id\":\"10001\",\"description\":\"gh.issue.story.desc\",\"iconUrl\":\"http://99.48.46.160:8080/images/icons/issuetypes/story.svg\",\"name\":\"Story\",\"subtask\":false}}},\"timespent\":null,\"sprint\":{\"id\":234,\"self\":\"http://99.48.46.160:8080/rest/agile/1.0/sprint/234\",\"state\":\"active\",\"name\":\"BAOSHENG Sprint 0410\",\"startDate\":\"2018-03-07T18:23:57.152+08:00\",\"endDate\":\"2018-03-21T06:23:00.000+08:00\",\"originBoardId\":154},\"project\":{\"self\":\"http://99.48.46.160:8080/rest/api/2/project/11501\",\"id\":\"11501\",\"key\":\"BAOSHENG\",\"name\":\"宝生项目\",\"avatarUrls\":{\"48x48\":\"http://99.48.46.160:8080/secure/projectavatar?avatarId=10324\",\"24x24\":\"http://99.48.46.160:8080/secure/projectavatar?size=small&avatarId=10324\",\"16x16\":\"http://99.48.46.160:8080/secure/projectavatar?size=xsmall&avatarId=10324\",\"32x32\":\"http://99.48.46.160:8080/secure/projectavatar?size=medium&avatarId=10324\"}},\"fixVersions\":[],\"customfield_11001\":34567.0,\"aggregatetimespent\":null,\"resolution\":{\"self\":\"http://99.48.46.160:8080/rest/api/2/resolution/10301\",\"id\":\"10301\",\"description\":\"GreenHopper Managed Resolution\",\"name\":\"完成\"},\"customfield_10104\":null,\"customfield_10105\":null,\"customfield_10106\":null,\"customfield_10107\":null,\"customfield_10900\":null,\"resolutiondate\":\"2018-03-10T11:25:05.000+0800\",\"workratio\":0,\"lastViewed\":\"2018-03-16T17:34:54.427+0800\",\"watches\":{\"self\":\"http://99.48.46.160:8080/rest/api/2/issue/BAOSHENG-112/watchers\",\"watchCount\":1,\"isWatching\":false},\"created\":\"2018-03-09T10:20:10.000+0800\",\"priority\":{\"self\":\"http://99.48.46.160:8080/rest/api/2/priority/3\",\"iconUrl\":\"http://99.48.46.160:8080/images/icons/priorities/medium.svg\",\"name\":\"Medium\",\"id\":\"3\"},\"customfield_10100\":null,\"customfield_10101\":null,\"customfield_10102\":null,\"labels\":[],\"timeestimate\":18000,\"aggregatetimeoriginalestimate\":18000,\"versions\":[],\"issuelinks\":[],\"assignee\":{\"self\":\"http://99.48.46.160:8080/rest/api/2/user?username=debao.fan\",\"name\":\"debao.fan\",\"key\":\"debao.fan\",\"emailAddress\":\"biefayoujiangeiwo@mi-me.com\",\"avatarUrls\":{\"48x48\":\"http://www.gravatar.com/avatar/b85137662d8823723131d5e9e98e6680?d=mm&s=48\",\"24x24\":\"http://www.gravatar.com/avatar/b85137662d8823723131d5e9e98e6680?d=mm&s=24\",\"16x16\":\"http://www.gravatar.com/avatar/b85137662d8823723131d5e9e98e6680?d=mm&s=16\",\"32x32\":\"http://www.gravatar.com/avatar/b85137662d8823723131d5e9e98e6680?d=mm&s=32\"},\"displayName\":\"樊得宝\",\"active\":true,\"timeZone\":\"Asia/Shanghai\"},\"updated\":\"2018-03-16T17:34:54.000+0800\",\"status\":{\"self\":\"http://99.48.46.160:8080/rest/api/2/status/10001\",\"description\":\"\",\"iconUrl\":\"http://99.48.46.160:8080/\",\"name\":\"完成\",\"id\":\"10001\",\"statusCategory\":{\"self\":\"http://99.48.46.160:8080/rest/api/2/statuscategory/3\",\"id\":3,\"key\":\"done\",\"colorName\":\"green\",\"name\":\"Done\"}},\"components\":[],\"timeoriginalestimate\":18000,\"description\":null,\"timetracking\":{\"originalEstimate\":\"5h\",\"remainingEstimate\":\"5h\",\"originalEstimateSeconds\":18000,\"remainingEstimateSeconds\":18000},\"attachment\":[],\"aggregatetimeestimate\":18000,\"flagged\":false,\"summary\":\"完成私服\",\"creator\":{\"self\":\"http://99.48.46.160:8080/rest/api/2/user?username=huiqi.hu\",\"name\":\"huiqi.hu\",\"key\":\"huiqi.hu\",\"emailAddress\":\"huiqi.hu@mi-me.com\",\"avatarUrls\":{\"48x48\":\"http://www.gravatar.com/avatar/9ea755d452f01e7d5c95e63e2ae1c49e?d=mm&s=48\",\"24x24\":\"http://www.gravatar.com/avatar/9ea755d452f01e7d5c95e63e2ae1c49e?d=mm&s=24\",\"16x16\":\"http://www.gravatar.com/avatar/9ea755d452f01e7d5c95e63e2ae1c49e?d=mm&s=16\",\"32x32\":\"http://www.gravatar.com/avatar/9ea755d452f01e7d5c95e63e2ae1c49e?d=mm&s=32\"},\"displayName\":\"胡晖祺\",\"active\":true,\"timeZone\":\"Asia/Shanghai\"},\"subtasks\":[],\"reporter\":{\"self\":\"http://99.48.46.160:8080/rest/api/2/user?username=huiqi.hu\",\"name\":\"huiqi.hu\",\"key\":\"huiqi.hu\",\"emailAddress\":\"huiqi.hu@mi-me.com\",\"avatarUrls\":{\"48x48\":\"http://www.gravatar.com/avatar/9ea755d452f01e7d5c95e63e2ae1c49e?d=mm&s=48\",\"24x24\":\"http://www.gravatar.com/avatar/9ea755d452f01e7d5c95e63e2ae1c49e?d=mm&s=24\",\"16x16\":\"http://www.gravatar.com/avatar/9ea755d452f01e7d5c95e63e2ae1c49e?d=mm&s=16\",\"32x32\":\"http://www.gravatar.com/avatar/9ea755d452f01e7d5c95e63e2ae1c49e?d=mm&s=32\"},\"displayName\":\"胡晖祺\",\"active\":true,\"timeZone\":\"Asia/Shanghai\"},\"customfield_10000\":\"0|i01zfz:\",\"aggregateprogress\":{\"progress\":0,\"total\":18000,\"percent\":0},\"customfield_10001\":[\"com.atlassian.greenhopper.service.sprint.Sprint@542e3b8c[id=234,rapidViewId=154,state=ACTIVE,name=BAOSHENG Sprint 0410,startDate=2018-03-07T18:23:57.152+08:00,endDate=2018-03-21T06:23:00.000+08:00,completeDate=<null>,sequence=234]\"],\"customfield_10002\":null,\"customfield_10200\":null,\"environment\":null,\"duedate\":null,\"progress\":{\"progress\":0,\"total\":18000,\"percent\":0},\"comment\":{\"comments\":[],\"maxResults\":0,\"total\":0,\"startAt\":0},\"votes\":{\"self\":\"http://99.48.46.160:8080/rest/api/2/issue/BAOSHENG-112/votes\",\"votes\":0,\"hasVoted\":false},\"worklog\":{\"startAt\":0,\"maxResults\":20,\"total\":0,\"worklogs\":[]}}}";
//        String rawJson = "{\n" +
//                "    \"name\": \"三班\",\n" +
//                "    \"students\": [\n" +
//                "        {\n" +
//                "            \"age\": 25,\n" +
//                "            \"gender\": \"female\",\n" +
//                "            \"grades\": \"三班\",\n" +
//                "            \"name\": \"露西\",\n" +
//                "            \"score\": {\n" +
//                "                \"网络协议\": 98,\n" +
//                "                \"JavaEE\": 92,\n" +
//                "                \"计算机基础\": 93\n" +
//                "            },\n" +
//                "            \"weight\": 51.3\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"age\": 26,\n" +
//                "            \"gender\": \"male\",\n" +
//                "            \"grades\": \"三班\",\n" +
//                "            \"name\": \"杰克\",\n" +
//                "            \"score\": {\n" +
//                "                \"网络安全\": 75,\n" +
//                "                \"Linux操作系统\": 81,\n" +
//                "                \"计算机基础\": 92\n" +
//                "            },\n" +
//                "            \"weight\": 66.5\n" +
//                "        },\n" +
//                "        {\n" +
//                "            \"age\": 25,\n" +
//                "            \"gender\": \"female\",\n" +
//                "            \"grades\": \"三班\",\n" +
//                "            \"name\": \"莉莉\",\n" +
//                "            \"score\": {\n" +
//                "                \"网络安全\": 95,\n" +
//                "                \"Linux操作系统\": 98,\n" +
//                "                \"SQL数据库\": 88,\n" +
//                "                \"数据结构\": 89\n" +
//                "            },\n" +
//                "            \"weight\": 55\n" +
//                "        }\n" +
//                "    ]\n" +
//                "}";


        Map<String, String> allMappings =  new LinkedHashMap<String, String>();

        getAllMappings(null, rawJson, allMappings);

        for (String key : allMappings.keySet()) {
            System.out.println(key + " ============= " + allMappings.get(key));
        }

    }

    public static Map<String, String> getAllMapping(String json) {
        Map<String, String> allMappings =  new LinkedHashMap<String, String>();
        getAllMappings(null, json, allMappings);
        return allMappings;
    }

    private static void getAllMappings(String key, String valueJsonStr, Map<String, String> allMappings) {
        if (valueJsonStr == null && key == null) {
            return;
        }

        int type = checkType(valueJsonStr);
        //已是基本字符串，无法继续递归!
        if (type == 0) {
            allMappings.put(key, valueJsonStr);
            return;
        } else if (type == 1) {
            JSONObject json = JSON.parseObject(valueJsonStr);
            for (String k1 : json.keySet()) {
                String v = json.getString(k1);
                String k = (key == null ? k1 : key + ">" + k1);
                getAllMappings(k, v, allMappings);
            }
        } else if (type == 2) {
            JSONArray arr = JSON.parseArray(valueJsonStr);
            for (int i = 0; i < arr.size(); i++) {
                String obj = arr.getString(i);
                String k = (key == null ? String.valueOf(i) : key + ">" + i);
                getAllMappings(k, obj, allMappings);
            }
        }
    }

    /**
     * 0 - String
     * 1 - JsonObject
     * 2 - Array
     */
    public static int checkType(String jsonStr){
        if (jsonStr == null || jsonStr.trim().length() == 0) return 0;

        try {
            JSON.parseArray(jsonStr);
            return 2;
        } catch (Exception e) {
        }

        try {
            JSON.parseObject(jsonStr);
            return 1;
        } catch (Exception e) {
        }

        return 0;
    }

    public static boolean isString(int type) {
        return 0 == type;
    }
    public static boolean isJsonObject(int type) {
        return 1 == type;
    }
    public static boolean isArray(int type) {
        return 2 == type;
    }
}