package cc.xiaoquer.jira.api.beans;

import cc.xiaoquer.jira.api.JIRA;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Nicholas on 2017/9/4.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiraSprint extends AbstractJiraEntity {

    private String sprintId;
    private String sprintName;
    private String startDate;
    private String endDate;
    private String state; //closed, active

//    {
//        "maxResults":50,
//            "startAt":0,
//            "isLast":true,
//            "values":[
//            {
//                "id":83,
//                    "self":"http://99.48.46.160:8080/rest/agile/1.0/sprint/83",
//                    "state":"closed",
//                    "name":"ETS 0818 Sprint",
//                    "startDate":"2017-08-07T09:18:04.527+08:00",
//                    "endDate":"2017-08-18T09:18:00.000+08:00",
//                    "completeDate":"2017-08-21T12:58:45.934+08:00",
//                    "originBoardId":95
//            },
//            {
//                "id":88,
//                    "self":"http://99.48.46.160:8080/rest/agile/1.0/sprint/88",
//                    "state":"active",
//                    "name":"ETS 0915 Sprint",
//                    "startDate":"2017-09-04T11:55:16.390+08:00",
//                    "endDate":"2017-09-15T11:55:00.000+08:00",
//                    "originBoardId":97
//            }
//        ]
//    }


    /**
     * Key = SprintName
     * Value = JiraSprint
     * @param responseBody
     * @return
     */
    public static Map<String, JiraSprint> toMap(String responseBody, String key) {
        Map<String, JiraSprint> localSprintsMap = new TreeMap<String, JiraSprint>();

        JSONObject jsonObject = JSON.parseObject(responseBody);

        JSONArray sprintArray = jsonObject.getJSONArray("values");

        if (sprintArray != null) {
            for (int i = 0; i < sprintArray.size(); i++) {
                JSONObject tmpJo = sprintArray.getJSONObject(i);
                JiraSprint jiraSprint = new JiraSprint();
                jiraSprint.setSprintId(String.valueOf(tmpJo.get("id")));
                jiraSprint.setSprintName(tmpJo.getString("name"));
                jiraSprint.setStartDate(tmpJo.getString("startDate"));
                jiraSprint.setEndDate(tmpJo.getString("endDate"));
                jiraSprint.setState(tmpJo.getString("state"));

                if ("active".equalsIgnoreCase(jiraSprint.getState())) {
                    localSprintsMap.put(jiraSprint.getSprintId(), jiraSprint);
                }
            }
        }

        Map<String, JiraSprint> thisTimeSprintMap = JIRA.BOARD_SPRINT_MAP.get(key);
        if (thisTimeSprintMap == null) {
            thisTimeSprintMap = new HashMap<>();
        }
        thisTimeSprintMap.putAll(localSprintsMap);
        JIRA.BOARD_SPRINT_MAP.put(key, thisTimeSprintMap);

        JIRA.ALL_SPRINT_MAP.putAll(localSprintsMap);

        return localSprintsMap;
    }

}
