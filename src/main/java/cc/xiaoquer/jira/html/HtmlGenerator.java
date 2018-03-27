package cc.xiaoquer.jira.html;

import cc.xiaoquer.jira.api.beans.JiraIssue;
import cc.xiaoquer.jira.constant.JiraColor;
import cc.xiaoquer.jira.storage.PropertiesCache;
import j2html.tags.ContainerTag;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Set;

import static j2html.TagCreator.*;

/**
 * Created by Nicholas on 2017/9/6.
 */
public class HtmlGenerator {
//    public static final String HTML_FILE = PropertiesCache.CONFIG_PATH + "/JiraScrumCards-{boardAndSprintName}-{date}.html";
    public static final String HTML_FILE = PropertiesCache.CONFIG_PATH + "/JiraScrumCards-{boardName}-{sprintName}.html";
    public static final String CSS_CARD_ID_LIGHT = "cardview_light";
    public static final String CSS_CARD_ID_DARK = "cardview_dark";

    public static final String TAG_LEFT = "<";
    public static final String TAG_LEFT_ESCAPE = "&lt;";
    public static final String TAG_RIGHT = ">";
    public static final String TAG_RIGHT_ESCAPE = "&gt;";
    public static final String TAG_QUOT = "\"";
    public static final String TAG_QUOT_ESCAPE = "&quot;";

    private static final String PX_ONEROW_HEIGHT = "110px";
    private static final String PX_THREEROWS_HEIGHT = "280px";

    private static final int A4_CARD_COLS = 2; //A4纸多少列
    private static final int A4_CARD_ROWS = 3; //A4纸多少行

    public static final String TEMPLATE_CARD_TITLE   = "<p><u><div class=\"kanban_name\">{boardName}</div></u></p>" +
                                                     "<div class=\"parent_name\">[{parentType}][{parentKey}]: {parentName}</div>";
    public static final String TEMPLATE_CARD_CONTENT = "<div class=\"issue_name\">[{issueType}][{issueKey}]: {issueName}</div>";
    public static final String TEMPLATE_OWNER        = "<div class=\"owner_name\">{ownerName}</div>";

    public static String generate(String boardName, String sprintName, Set<JiraIssue> issueSet) {

        boardName = boardName.replaceAll(" ", "");
        sprintName = sprintName.replaceAll(" ", "");

        String htmlPath = HTML_FILE
                .replace("{boardName}", boardName)
                .replace("{sprintName}", sprintName)
                .replace("{date}", String.valueOf(System.currentTimeMillis()));

        File htmlFile = new File(htmlPath);
        if (htmlFile.exists()) {
            htmlFile.delete();
        }

        String htmlCode = renderHTML(issueSet, boardName, sprintName);
        PrintStream printStream = null;
        try{
            //打开文件
            printStream = new PrintStream(new FileOutputStream(htmlFile));
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }

        //将HTML文件内容写入文件中
        printStream.println(htmlCode);
        System.out.println("生成成功! 路径：" + htmlPath);
        return htmlPath;
    }

    private static String renderHTML(Set<JiraIssue> issueIdSet, String boardName, String sprintName) {
        ContainerTag pageTable = table().withStyle("width:92%; border-spacing:20px");

        int i = 1;

        ContainerTag pageBreak = null;
        ContainerTag pageNewLine = null;
        for (JiraIssue jiraIssue : issueIdSet) {
            //超过一页的卡片数量就是用TBody分页
            if (i % (A4_CARD_COLS * A4_CARD_ROWS) == 1) {
                pageBreak = tbody().withClass("sheet");
                pageTable.with(pageBreak);
            }

            //启动新的TR
            if (i % A4_CARD_COLS == 1) {
                pageNewLine = tr();
                pageBreak.with(pageNewLine);
//                pageTable.with(pageNewLine);
            }

            pageNewLine.with(
                td().with(renderSingleCard(jiraIssue, boardName, sprintName))
            );

            i++;
        }

        return
        html(
            head(
                title("Jira看板打印 - v1.0"),
                meta().attr("http-equiv", "Content-Type").attr("content", "text/html;charset=utf-8"),
                style().withText(HtmlCss.COMMON).withText(HtmlCss.PAPER)
                        .withText(HtmlCss.CARD_LIGHT).withText(HtmlCss.CARD_DARK)
//                link().withRel("stylesheet").withHref("/css/main.css")
            ),
            body(pageTable).withClass("A4")
        ).renderFormatted()
                .replaceAll(TAG_LEFT_ESCAPE, TAG_LEFT)
                .replaceAll(TAG_RIGHT_ESCAPE, TAG_RIGHT)
                .replaceAll(TAG_QUOT_ESCAPE, TAG_QUOT)
                ;
        //EscapeUtil.class Text.class When rendering it will escape some tags.
    }

    private static ContainerTag renderSingleCard(JiraIssue jiraIssue, String boardName, String sprintName) {

        String card_bgcolor = JiraColor.STORY.getHex(); //Story背景色

        if (jiraIssue.isTask()){
            card_bgcolor = JiraColor.TASK.getHex();
        } else if (jiraIssue.isSubTask()) {
            card_bgcolor = JiraColor.getColorByName(jiraIssue.getOwner());
        }

//        JiraBoard jiraBoard = JIRA.getBoardCache(jiraIssue.getBoardId());
//        String boardName = (jiraBoard == null ? "" : jiraBoard.getBoardName());

        String cardId = JiraColor.isColorDark(card_bgcolor) ? CSS_CARD_ID_DARK : CSS_CARD_ID_LIGHT;

        ContainerTag cardTable = table().withId(cardId).attr("bgcolor", card_bgcolor).with(
                thead(
                        tr(
                                th().attr("scope","col").attr("width", "70%"),
                                th().attr("scope","col").attr("width", "30%")
                        )
                ),
                tr(
                        td(b(TEMPLATE_CARD_TITLE
                                .replace("{boardName}",     boardName)
                                .replace("{parentType}",    jiraIssue.getParentType())
                                .replace("{parentKey}",     jiraIssue.getParentKey())
                                .replace("{parentName}",    jiraIssue.getParentName()))
                        )
                        .attr("colspan","2").attr("height", PX_ONEROW_HEIGHT).attr("align", "center")
                        .attr("style", "border-top:0px none #000;")
                )
        );

        if (jiraIssue.isParent()) {
            cardTable.with(
                tr(
                        td(TEMPLATE_CARD_CONTENT
                                .replace("{issueType}",     jiraIssue.getIssueType())
                                .replace("{issueKey}",      jiraIssue.getIssueKey())
                                .replace("{issueName}",     jiraIssue.getIssueName())
                        )
                        .attr("rowspan","3").attr("colspan", "2").attr("height", PX_THREEROWS_HEIGHT)
                        .attr("align", "left").attr("valign", "middle")
                )
            );
        } else {
            cardTable.with(
                tr(
                        td(TEMPLATE_CARD_CONTENT
                                .replace("{issueType}",     jiraIssue.getIssueType())
                                .replace("{issueKey}",      jiraIssue.getIssueKey())
                                .replace("{issueName}",     jiraIssue.getIssueName())
                        )
                        .attr("rowspan","3").attr("height", PX_THREEROWS_HEIGHT)
                        .attr("align", "left").attr("valign", "middle"),

                        td(TEMPLATE_OWNER.replace("{ownerName}",jiraIssue.getOwner())).attr("height", PX_ONEROW_HEIGHT)
                ),
                tr(
                        td("估算:" + jiraIssue.getEstimate()).attr("rowspan", "2").attr("valign", "top")
                )
            );
        }
        return cardTable;
    }

    public static void main(String[] args) {
        generate("ETS", "0915", null);
    }
}
