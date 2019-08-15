package cc.xiaoquer.jira.html;

import cc.xiaoquer.jira.api.JIRA;
import cc.xiaoquer.jira.api.beans.JiraEpic;
import cc.xiaoquer.jira.api.beans.JiraIssue;
import cc.xiaoquer.jira.constant.FoldersConsts;
import j2html.tags.ContainerTag;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static j2html.TagCreator.*;

/**
 * Created by Nicholas on 2017/9/6.
 */
public class VPLViaHtml extends ViewOfProductLine {
    public static final String HTML_FILE = FoldersConsts.OUTPUT_FOLDER + "/OQPL-{date}.html";

    public static String generate(Map<String, Map<String, JiraIssue>> epicIssuesMap) {
        if (epicIssuesMap == null || epicIssuesMap.size() == 0)  return null;

        //解析数据，输入易于HTML排版的数据二维信息
        sortIssues(epicIssuesMap);

        //html渲染
        String htmlPath = HTML_FILE.replace("{date}", DATE_FORMAT_SLIM.format(new Date()));

        File htmlFile = new File(htmlPath);
        if (htmlFile.exists()) {
            htmlFile.delete();
        }

        String htmlCode = renderHTML();
        PrintStream printStream = null;
        try{
            //打开文件
            printStream = new PrintStream(new FileOutputStream(htmlFile));
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }

        //将HTML文件内容写入文件中
        printStream.println(htmlCode);
        System.out.println("本地生成HTML成功! 路径：" + htmlPath);

        String webUrl = "";
        try {
            webUrl = Publisher.publish(htmlPath);
        } catch (Exception e) {
            System.out.println("发布到Web服务器失败!");
            e.printStackTrace();
            //远程发布失败，打开本地
            return htmlPath;
        }

        return webUrl;
    }
    private static String renderHTML() {
        int totalWidth = 0;
        for (int pixel : COL_WIDTH_PIXEL) {
            totalWidth += pixel;
        }

        ContainerTag productTBody = tbody();
        ContainerTag html = html(
                head(
                        title(TITLE),
                        meta().attr("http-equiv", "Content-Type").attr("content", "text/html;charset=utf-8"),
                        style().withText(HtmlCss.TABLE_COMMON)
//                link().withRel("stylesheet").withHref("/css/main.css")
                ),
                body(div().withId("divCenterWithMargin")
                        .with(
                            h1(TITLE).attr("align","center"),
                            h4(DATE_FORMAT.format(new Date())).attr("align","right"),
                            table().withId("defaultTable").with(productTBody)
                        )
                )

        );

        int colIdx = 0;
        for (String headerName : COL_NAME_ARR) {
            productTBody.with(th(headerName).attr("width", (int)(COL_WIDTH_PIXEL[colIdx++] * 100 / totalWidth)+"%"));
        }

        String previousEpicGroupKey = "";
        String previousProjectGroupKey = "";

        String currEpicGroupKey = "";
        String currProjectGroupKey = "";

        ContainerTag newLine = null;
        for (String combinedKey : SORTED_ISSUES_MAP.keySet()) {
            JiraIssue jiraIssue = SORTED_ISSUES_MAP.get(combinedKey);
            JiraEpic jiraEpic   = jiraIssue.getJiraEpic();

            newLine = tr();

            int epicGroupCount    = 0;
            int projectGroupCount = 0;

            currEpicGroupKey = getEpicGroupKey(combinedKey);
            currProjectGroupKey = getEpicProjectGroupKey(combinedKey);

            //新的Epic
            if (!currEpicGroupKey.equals(previousEpicGroupKey)) {
                previousEpicGroupKey = currEpicGroupKey;
                epicGroupCount = getEpicGroupCount(combinedKey);
            }
            //新的Project
            if (!getEpicProjectGroupKey(combinedKey).equals(previousProjectGroupKey)) {
                previousProjectGroupKey = currProjectGroupKey;
                projectGroupCount = getProjectGroupCount(combinedKey);
            }

            if (epicGroupCount >= 1) {
                String epicUrl = JIRA.serverUrl + JIRA.BROWSE_ISSUE_URL.replace("{issueKey}", jiraIssue.getJiraEpic().getEpicKey());
                newLine.with(td()
                                .attr("rowspan", epicGroupCount)
                                .with(a(jiraIssue.getJiraEpic().getEpicName()).withTarget("_blank").withHref(epicUrl)));
                newLine.with(td().withStyle(HtmlCss.CELL_TEXT_CENTER).attr("rowspan", epicGroupCount).withText(jiraEpic.getEpicOwner()));
                newLine.with(td().withStyle(HtmlCss.CELL_TEXT_CENTER).attr("rowspan", epicGroupCount).withText(jiraEpic.getCustomPriority()));
                newLine.with(td().withStyle(HtmlCss.CELL_TEXT_CENTER).attr("rowspan", epicGroupCount).withText(jiraEpic.getEpicStatus()));
                newLine.with(td().withStyle(HtmlCss.CELL_TEXT_CENTER).attr("rowspan", epicGroupCount).withText(jiraEpic.getCustomTotalWorking()));
                newLine.with(td().withStyle(HtmlCss.CELL_TEXT_CENTER).attr("rowspan", epicGroupCount).withText(jiraEpic.getCost()));
                newLine.with(td().withStyle(HtmlCss.CELL_TEXT_CENTER).attr("rowspan", epicGroupCount).withText(jiraEpic.getCustomRevenueInfo()));
            }
            if (projectGroupCount >= 1) {
                String[] displayProject = getDisplayProject(jiraEpic, jiraIssue);
                String projectKey  = displayProject[0];
                String projectName =  displayProject[1];

                String backlogUrl = JIRA.serverUrl
                        + JIRA.BROWSE_ISSUE_URL.replace("{issueKey}", projectKey)
                        + "?view=planning";

                newLine.with(td()
                                .attr("rowspan", projectGroupCount)
                                .with(a(projectName).withTarget("_blank").withHref(backlogUrl)));
            }
            if (StringUtils.isNotBlank(jiraIssue.getIssueKey())) {
                String issueUrl = JIRA.serverUrl
                        + JIRA.BROWSE_ISSUE_URL.replace("{issueKey}", jiraIssue.getIssueKey());
                newLine.with(td()
                        .with(a(jiraIssue.getIssueName()).withTarget("_blank").withHref(issueUrl)));
            } else {
                newLine.with(td().withText(jiraIssue.getIssueName()));
            }
            newLine.with(td().withStyle(HtmlCss.CELL_TEXT_CENTER).withText(jiraIssue.getOwner()));
            newLine.with(td().withStyle(HtmlCss.CELL_TEXT_CENTER).withText(jiraIssue.getPriority()));
            newLine.with(td().withStyle(HtmlCss.CELL_TEXT_CENTER).withText(jiraIssue.getIssueStatus()));
            newLine.with(td().withText(jiraIssue.getDescription()));
            newLine.with(td().withText(jiraIssue.getCustomEstimateDone()));
            newLine.with(td().withText(jiraIssue.getCustomActualDone()));
            newLine.with(td().withText(jiraIssue.getUpdatedAt()));

            productTBody.with(newLine);
        }

        return html.renderFormatted()
                .replaceAll(TAG_LEFT_ESCAPE, TAG_LEFT)
                .replaceAll(TAG_RIGHT_ESCAPE, TAG_RIGHT)
                .replaceAll(TAG_QUOT_ESCAPE, TAG_QUOT)
                ;
        //EscapeUtil.class Text.class When rendering it will escape some tags.
    }

}
