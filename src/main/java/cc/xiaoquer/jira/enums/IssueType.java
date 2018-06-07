package cc.xiaoquer.jira.enums;

/**
 * Created by Nicholas on 2018/6/7.
 */
public enum IssueType {
    EPIC("Epic", "Epic"),
    Story("Story", "故事"),
    Task("Task", "任务"),
    SubTask("Sub-task", "子任务"),
    BUG("BUG", "缺陷"),
    BUGPRO("BUG-PRO", "生产缺陷"),
    ChgReq("Change-request", "需求变更"),
    TestTask("TestTask", "测试任务"),
    ARC("ARC", "ARC评审");

    private String id;
    private String desc;

    private IssueType(String id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public String getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

    public static IssueType of(String id) {
        for (IssueType issueStatus : IssueType.values()) {
            if (issueStatus.id.equalsIgnoreCase(id) || issueStatus.desc.equalsIgnoreCase(id)) {
                return issueStatus;
            }
        }
        return null;
    }
}
