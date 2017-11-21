package cc.xiaoquer.jira.api.jsonchain;

import com.alibaba.fastjson.JSONArray;

/**
 * Created by Nicholas on 2017/9/4.
 */
public class JiraJSONArray extends JSONArray{
    private JSONArray jsonArray;

    public JiraJSONArray(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    @Override
    public JiraJSONObject get(int index) {
        return new JiraJSONObject(jsonArray.getJSONObject(index));
    }

}
