package cc.xiaoquer.jira.api.beans;

import cc.xiaoquer.jira.api.JIRA;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Nicholas on 2017/9/4.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiraProject extends AbstractJiraEntity {
    private String projectId;
    private String projectKey;
    private String projectName;
    //    {
//            "maxResults":50,
//            "startAt":0,
//            "total":1,
//            "isLast":true,
//            "values":[
//          {
//            "self":"http://99.48.46.160:8080/rest/api/2/project/11100",
//                "id":"11100",
//                "key":"ETS",
//                "name":"ETS",
//                "avatarUrls":{
//                    "48x48":"http://99.48.46.160:8080/secure/projectavatar?avatarId=10324",
//                    "24x24":"http://99.48.46.160:8080/secure/projectavatar?size=small&avatarId=10324",
//                    "16x16":"http://99.48.46.160:8080/secure/projectavatar?size=xsmall&avatarId=10324",
//                    "32x32":"http://99.48.46.160:8080/secure/projectavatar?size=medium&avatarId=10324"
//                 }
//          }
//    ]
//    }
    public static Map<String, JiraProject> toMap(String responseBody) {
        Map<String, JiraProject> projectsMap = new LinkedHashMap<String, JiraProject>();

        JSONObject jsonObject = JSON.parseObject(responseBody);

        JSONArray projectsArray = jsonObject.getJSONArray("values");

        for (int i=0; i< projectsArray.size(); i++) {
            JSONObject projectJo = projectsArray.getJSONObject(i);

            JiraProject jp = new JiraProject(
                    projectJo.getString("id"),
                    projectJo.getString("key"),
                    projectJo.getString("name")
            );
            projectsMap.put(jp.getProjectId(), jp);
        }

        return projectsMap;
    }

}
