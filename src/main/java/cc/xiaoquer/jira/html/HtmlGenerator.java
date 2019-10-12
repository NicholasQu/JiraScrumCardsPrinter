package cc.xiaoquer.jira.html;

import cc.xiaoquer.jira.api.beans.JiraIssue;
import cc.xiaoquer.jira.constant.FoldersConsts;
import cc.xiaoquer.jira.constant.JiraColor;
import cc.xiaoquer.jira.storage.PropertiesCache;
import cc.xiaoquer.utils.JSCPUtils;
import j2html.tags.ContainerTag;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static j2html.TagCreator.*;

/**
 * Created by Nicholas on 2017/9/6.
 */
public class HtmlGenerator {
//    public static final String HTML_FILE = PropertiesCache.OUTPUT_FOLDER + "/JiraScrumCards-{boardAndSprintName}-{date}.html";
    public static final String HTML_FILE = FoldersConsts.OUTPUT_FOLDER + "/JiraScrumCards-{boardName}-{sprintName}.html";
    public static final String CSS_CARD_ID_LIGHT = "cardview_light";
    public static final String CSS_CARD_ID_DARK = "cardview_dark";

    public static final String TAG_LEFT = "<";
    public static final String TAG_LEFT_ESCAPE = "&lt;";
    public static final String TAG_RIGHT = ">";
    public static final String TAG_RIGHT_ESCAPE = "&gt;";
    public static final String TAG_QUOT = "\"";
    public static final String TAG_QUOT_ESCAPE = "&quot;";

    private static final String PX_TITLE_HEIGHT = "130px";
    private static final String PX_ONEROW_HEIGHT = "110px";
    private static final String PX_THREEROWS_HEIGHT = "280px";

    private static final String CIRCLE_OPACITY    = "0.8";  //小圆点的透明度，以防完全挡住背后的文字

    private static final int A4_CARD_COLS = 2; //A4纸多少列

    private static final int A4_CARD_ROWS = 3; //A4纸多少行
    private static final String TEMPLATE_CARD_TITLE   = "<p><u><div class=\"kanban_name\">{boardName}</div></u></p>" +
                                                     "<div class=\"parent_name\">[{parentType}][{parentKey}]: {parentName}</div>";
    private static final String TEMPLATE_CARD_CONTENT = "<div class=\"issue_name\">[{issueType}][{issueKey}]: {issueName}</div>";
    private static final String TEMPLATE_OWNER        = "<div class=\"owner_name\">{ownerName}</div>";
    private static final String TEMPLATE_CIRCLE        =
            "<div style=\"border-style:solid; border-color:black; border-width:1px; border-radius:50%; display: inline-block; " +
            "   position:absolute;z-index:100;float:{float};top:{circletop}; left:{circleleft}; " +
            "   background: {bgcolor}; opacity:{opacity}; height: {dimension}; width: {dimension}; \">  \n" +
            "   <span style=\"display: block; color:{fontcolor}; font-size:{fontsize}; text-align: center; " +
            "    height:{dimension}; line-height: {dimension};\"> " +
            "       {content} " +
            "   </span>" +
            "</div>";

    private static final String TEMPLATE_CIRCLE_PRIORITY = TEMPLATE_CIRCLE
            .replaceAll("\\{float\\}",      "left")
            .replaceAll("\\{dimension\\}",  "80px")
            .replaceAll("\\{opacity\\}",    "0.8")
            .replaceAll("\\{circletop\\}",  "10px")
            .replaceAll("\\{circleleft}",   "10px");

    private static final String TEMPLATE_CIRCLE_BLOOD = TEMPLATE_CIRCLE
            .replaceAll("\\{float\\}",      "right")
            .replaceAll("\\{dimension\\}",  "40px")
            .replaceAll("\\{opacity\\}",    "0.8")
            .replaceAll("\\{circletop\\}",  "10px")
            .replaceAll("\\{circleleft}",   "445px");

    //Key = P1, Value = {BackgroundColor, FontColor}
    private static final Map<String, String[]> PRIORITY_COLORS = new HashMap<>();

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
                td().withStyle("position:relative").with(renderSingleCard(jiraIssue, boardName, sprintName))
            );

            i++;
        }

        return
        html(
            head(
                title("Jira看板打印 - v2.0"),
                meta().attr("http-equiv", "Content-Type").attr("content", "text/html;charset=utf-8"),
                style().withText(HtmlCss.CARD_COMMON).withText(HtmlCss.PAPER)
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
        String card_bgcolor = PropertiesCache.getProp(PropertiesCache.P_COLOR_STORY_BG, JiraColor.STORY.getHex()); //Story背景色

        if (jiraIssue.isTask()){
            card_bgcolor = PropertiesCache.getProp(PropertiesCache.P_COLOR_TASK_BG, JiraColor.TASK.getHex());
        }

//        JiraBoard jiraBoard = JIRA.getBoardCache(jiraIssue.getBoardId());
//        String boardName = (jiraBoard == null ? "" : jiraBoard.getBoardName());

        //增加优先级的圆形，图层向左上角浮动

        //增加血缘关系序号展示位，图层向右上角浮动，用于关联故事与子任务的关系
        String bloodBgColor = "#e14fa6";
        String bloodFontColor = "#FFF";
        String bloodContent = "";

        //生成卡片颜色：背景色 + 血缘小圆点色
        String[] cardColors;
        if (jiraIssue.isParent()) {
            bloodContent = String.valueOf(JSCPUtils.getNumAtLast(jiraIssue.getIssueKey()));
            cardColors = JiraColor.getCardColor(jiraIssue, jiraIssue.getOwner(), bloodContent);
        } else {
            bloodContent = String.valueOf(JSCPUtils.getNumAtLast(jiraIssue.getParentKey()));
            cardColors = JiraColor.getCardColor(jiraIssue, jiraIssue.getOwner(), bloodContent);
        }

        bloodBgColor = cardColors[1];
        bloodFontColor = JiraColor.isColorDark(bloodBgColor) ? "#FFF" : "#000";
        if (jiraIssue.isSubTask()) {
            card_bgcolor = cardColors[0];
        }
        String cardIdCss = JiraColor.isColorDark(card_bgcolor) ? CSS_CARD_ID_DARK : CSS_CARD_ID_LIGHT;

        //bloodFontSize，防止数字溢出小圆点,
        //20190818 这都考虑到了，我好牛逼啊
        int bloodFontSize = 18;
        if (bloodContent.length() == 4) {
            bloodFontSize = 15;
        } else if (bloodContent.length() > 4) {
            bloodFontSize = 11;
        } else if (bloodContent.length() > 6) {
            bloodFontSize = 9;
        }

//        ContainerTag bloodTD = td().attr("style", "border-top:0px none #000;font-size:" + bloodFontSize + "px;")
//                .attr("bgcolor", "#FFF")
//                .attr("rowspan", "4")
//                .attr("valign", "top")
//                .withText(
//                    TEMPLATE_CIRCLE_BLOOD
//                        .replaceAll("\\{bgcolor\\}", bloodBgColor)
//                        .replaceAll("\\{fontcolor\\}", bloodFontColor)
//                        .replaceAll("\\{content\\}", bloodContent));

        String bloodDiv = TEMPLATE_CIRCLE_BLOOD
                .replaceAll("\\{bgcolor\\}",    bloodBgColor)
                .replaceAll("\\{fontcolor\\}",  bloodFontColor)
                .replaceAll("\\{fontsize\\}",   String.valueOf(bloodFontSize))
                .replaceAll("\\{content\\}",    bloodContent);
        //BLOOD展示位构建完毕................................................


        String issuePriority = jiraIssue.getPriority();
        String priorityDiv = "";
        if (StringUtils.isNotBlank(issuePriority)) {
            priorityDiv = TEMPLATE_CIRCLE_PRIORITY
                    .replaceAll("\\{bgcolor\\}", getPriorityColor(issuePriority, true))
                    .replaceAll("\\{fontcolor\\}", getPriorityColor(issuePriority, false))
                    .replaceAll("\\{fontsize\\}", "20")
                    .replaceAll("\\{content\\}", issuePriority);
        }


        //标题的缩略长度，需要减去类型和key的长度，尽量精确吧。
        int lenOfparentNameAbbr = Integer.valueOf(PropertiesCache.getProp(PropertiesCache.P_CARD_TITLE_ABBR_LEN))
                - jiraIssue.getParentType().length() - jiraIssue.getParentKey().length();

        ContainerTag cardTable = table().withId(cardIdCss)
                .attr("bgcolor", card_bgcolor)
                .with(
                 thead(
                        tr(
                                th().attr("scope","col").attr("width", "70%"),
                                th().attr("scope","col").attr("width", "30%")
                        )
                ),
                tr(
                        td().attr("colspan","2")
                            .attr("height", PX_TITLE_HEIGHT)
                            .attr("align", "center")
                            .attr("style", "border-top:0px none #000;")
                            .with(b(TEMPLATE_CARD_TITLE
                                    .replace("{boardName}",     boardName)
                                    .replace("{parentType}",    jiraIssue.getParentType())
                                    .replace("{parentKey}",     jiraIssue.getParentKey())
                                    .replace("{parentName}",    StringUtils.abbreviate(jiraIssue.getParentName(), lenOfparentNameAbbr))))
//                                    .replace("{parentName}",    StringUtils.abbreviate(lenStr4Test, lenOfparentNameAbbr))))
                )
        );

        if (jiraIssue.isParent()) {

            cardTable.with(
                tr(
                        td().attr("rowspan","3")
                            .attr("colspan", "2")
                            .attr("height", PX_THREEROWS_HEIGHT)
                            .attr("align", "left")
                            .attr("valign", "middle")
                            .withText(StringUtils.abbreviate(
                                    TEMPLATE_CARD_CONTENT
                                    .replace("{issueType}",     jiraIssue.getIssueType())
                                    .replace("{issueKey}",      jiraIssue.getIssueKey())
                                    .replace("{issueName}",     jiraIssue.getIssueName()),
//                                    .replace("{issueName}",     lenStr4Test),
                                    Integer.valueOf(PropertiesCache.getProp(PropertiesCache.P_CARD_STORY_ABBR_LEN))))
                )
            );
        } else {
            cardTable.with(
                tr(
                        td().attr("rowspan","3")
                            .attr("colspan", "1")
                            .attr("height", PX_THREEROWS_HEIGHT)
                            .attr("align", "left")
                            .attr("valign", "middle")
                            .withText(StringUtils.abbreviate(TEMPLATE_CARD_CONTENT
                                    .replace("{issueType}",     jiraIssue.getIssueType())
                                    .replace("{issueKey}",      jiraIssue.getIssueKey())
                                    .replace("{issueName}",     jiraIssue.getIssueName()),
//                                    .replace("{issueName}",     lenStr4Test),
                                    Integer.valueOf(PropertiesCache.getProp(PropertiesCache.P_CARD_ISSUE_ABBR_LEN)))),
//
                        td(TEMPLATE_OWNER.replace("{ownerName}",jiraIssue.getOwner()))
                            .attr("height", PX_ONEROW_HEIGHT)
                            .attr("width", "100px")
                ),
                tr(
                        td("估算:" + jiraIssue.getEstimate()).attr("rowspan", "2").attr("valign", "top")
                )
            );
        }

        return div()
                //添加优先级的浮层在左上角，血缘ID的浮层在右上角
                .withText(priorityDiv)
                .withText(bloodDiv)
                .with(cardTable);
    }

    //获取优先级的颜色
    private static String getPriorityColor(String priority, boolean isBackground) {
        //初始化获取配置属性
        if (PRIORITY_COLORS == null || PRIORITY_COLORS.isEmpty()) {
            String priorityKeys = PropertiesCache.getProp(PropertiesCache.P_COLOR_PRIORITY_KEY);
            String priorityBgColors = PropertiesCache.getProp(PropertiesCache.P_COLOR_PRIORITY_BG);
            String priorityFontColors = PropertiesCache.getProp(PropertiesCache.P_COLOR_PRIORITY_FONT);

            String[] priorityKeyArr = StringUtils.split(priorityKeys, ",");
            String[] priorityBgColorArr = StringUtils.split(priorityBgColors, ",");
            String[] priorityFontColorArr = StringUtils.split(priorityFontColors, ",");

            if (priorityKeyArr.length != priorityBgColorArr.length || priorityKeyArr.length != priorityFontColorArr.length) {
                return isBackground ? "#FFF" : "000";
            }

            for (int i = 0; i < priorityKeyArr.length; i++) {
                PRIORITY_COLORS.put(StringUtils.trim(priorityKeyArr[i]), new String[]{priorityBgColorArr[i], priorityFontColorArr[i]});
            }
        }

        String[] colors = PRIORITY_COLORS.get(priority);
        if (colors!=null) {
            return isBackground ? colors[0] : colors[1];
        }

        return isBackground ? "#FFF" : "000";
    }

    private static String lenStr4Test="US5-3_【AAAA】用户在么么钱包申请现金贷获得放款后，可通过支付宝、银行转账等线下还款方式进行还款，这样用户可以线下完成主动还款【海尔云贷现金贷项目】用户在么么钱包申请现金贷获得放款后，可通过支付宝、银行转账等线下还款方式进行还款，这样用户可以线下完成主动还款";
    public static void main(String[] args) {
//        generate("ETS", "0915", null);

        System.out.println(TEMPLATE_CIRCLE_PRIORITY);
    }
}
