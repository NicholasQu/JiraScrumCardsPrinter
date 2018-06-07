package cc.xiaoquer.jira.enums;

/**
 * Created by Nicholas on 2018/6/7.
 */
public enum IssueStatus {
    TODO("Todo", "待办"),
    DOING("Doing", "进行中"),
    DONE("Done", "完成");

    private String status;
    private String desc;

    private IssueStatus(String status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public String getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static IssueStatus of(String status) {
        for (IssueStatus issueStatus : IssueStatus.values()) {
            if (issueStatus.status.equalsIgnoreCase(status)) {
                return issueStatus;
            }
        }
        return null;
    }
}
