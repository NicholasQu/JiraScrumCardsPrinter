package cc.xiaoquer.jira.api.beans;

import cc.xiaoquer.jira.api.JIRA;
import cc.xiaoquer.jira.api.jsonchain.JiraJSONArray;
import cc.xiaoquer.jira.api.jsonchain.JiraJSONObject;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Nicholas on 2017/9/4.
 */
@Data
public class JiraBoard extends AbstractJiraEntity {

    private String boardId;
    private String boardName;

    //理论上一个看板只有一个project, jira api是允许多个，不知道什么考虑
    private Map<String, JiraProject> projects = new LinkedHashMap<>();

//    {
//        "maxResults":50,
//            "startAt":0,
//            "isLast":true,
//            "values":[
//            {
//                "id":21,
//                    "self":"http://99.48.46.160:8080/rest/agile/1.0/board/21",
//                    "name":"CREDIT2 board",
//                    "type":"scrum"
//            },
//            {
//                "id":22,
//                    "self":"http://99.48.46.160:8080/rest/agile/1.0/board/22",
//                    "name":"CREDIT1 board",
//                    "type":"scrum"
//            }
//        ]
//    }

    public static Map<String, JiraBoard> toMap(String responseBody, String key) {
        Map<String, JiraBoard> boardsMap = new HashMap<String, JiraBoard>();

        JSONObject jsonObject = JSON.parseObject(responseBody);

        JSONArray boardsArray = jsonObject.getJSONArray("values");
        for (int i = 0; i < boardsArray.size(); i++) {
            JSONObject tmpJo = boardsArray.getJSONObject(i);
            JiraBoard jiraBoard = new JiraBoard();

            jiraBoard.setBoardId(String.valueOf(tmpJo.get("id")));
            jiraBoard.setBoardName(tmpJo.getString("name"));

            jiraBoard.getProjects().putAll(JIRA.getProjectMap(jiraBoard.getBoardId()));

            boardsMap.put(jiraBoard.getBoardId(), jiraBoard);
        }

        JIRA.ALL_BOARD_MAP.putAll(boardsMap);

        return boardsMap;
    }

}
