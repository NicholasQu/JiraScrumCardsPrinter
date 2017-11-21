package cc.xiaoquer.jira.api.jsonchain;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by Nicholas on 2017/9/4.
 */
public class JiraJSONObject extends JSONObject{
    private JSONObject jsonObject;

    public JiraJSONObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public JiraJSONObject get(String key) {
        return new JiraJSONObject(jsonObject.getJSONObject(key));
    }

    public String getString(String key) {
        return jsonObject.getString(key);
    }

    public JiraJSONArray getJSONArray(String key) {
        return new JiraJSONArray(jsonObject.getJSONArray(key));
    }
}
