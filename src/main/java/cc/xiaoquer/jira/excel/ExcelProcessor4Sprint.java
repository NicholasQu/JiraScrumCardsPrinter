package cc.xiaoquer.jira.excel;

import cc.xiaoquer.jira.api.JIRA;
import cc.xiaoquer.jira.api.beans.JiraBoard;
import cc.xiaoquer.jira.api.beans.JiraIssue;
import cc.xiaoquer.jira.api.beans.JiraSprint;
import cc.xiaoquer.jira.constant.FoldersConsts;
import cc.xiaoquer.jira.storage.PropertiesCache;
import cc.xiaoquer.utils.JSONParsingUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by Nicholas on 2017/9/20.
 *
 */
public class ExcelProcessor4Sprint {

    public static final String EXCEL_FILE = FoldersConsts.OUTPUT_FOLDER + "/Export-{boardName}-{sprintName}-{date}.xls";
    public static final int    PADDING_LEN    = 10;

    /**
     * 主题ID, hiddenKeys，团队，Tree，类型， 主题Key，
     * 主题，状态，经办人，预估时间(H)，剩余时间(H), 预估时间，剩余时间
     * 项目Key，项目名称，优先级, 描述
     * 创建时间,更新时间,截止时间
     * Browse
     */
    public static short[] SPRINT_COL_WIDTH_PIXEL = new short[] {
            0,      0,      50,    50,     50,     100,
            300,    80,     120,    120,    120,    0,    0,
            0,      0,      120,      0,
            0,      0,      0
    };

    public static final int COL_INIT_NUM = SPRINT_COL_WIDTH_PIXEL.length;

    public static int COL_ADD_NUM  = 0;          //自定义字段的数量
    public static String[] CUSTOM_KEY_ARR;
    public static String[] CUSTOM_NAME_ARR;
    public static Double[] CUSTOM_RECORDS_SUM;    //自定义字段如果是数值型，自动计算一个合计值
    public static boolean CUSTOM_FIELDS_ADDED = false;   //是否已经添加了自定义的列

    //显示所有列 对应环境变量 excelShowAllColumns
    public static short[] SPRINT_COL_WIDTH_PIXEL_ALL = new short[] {
            0,      0,      50,    50,     50,    100,
            300,    80,     120,    120,    120,    100,    100,
            100,    100,    100,    100,
            200,    100,    100
    };

    public static String[] SPRINT_COL_NAME_ARR = new String[] {
            "Id",        "hiddenKeys",   "团队",  "T",   "类型",  "Key",
            "主题",       "状态",       "经办人",   "预估时间(H)",   "剩余时间(H)",  "预估时间",  "剩余时间",
            "项目Key",    "项目名称",    "优先级",    "描述",
            "创建时间",    "更新时间",    "截止时间"
    };

    public static short[] BACKLOG_COL_WIDTH_PIXEL = new short[] {
            0,      0,      50,     50,     50,    60,
            60,     300,    60,     300,
            100,    100,
            300
    };

    public static String[] BACKLOG_COL_NAME_ARR = new String[] {
            "Id",        "hiddenKeys",   "看板", "T",   "类型",  "Key",
            "优先级",    "主题",       "状态",       "描述",
            "创建时间",    "更新时间",
            "备注"
    };

    private static final DecimalFormat DF = new DecimalFormat("#.#");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static int ROW_IDX_TITLE         = 0;    //标题行号
    public static int ROW_IDX_SUBTITLE      = 0;    //子标题行号
    public static int ROW_IDX_HEADER        = 0;    //列头行号
    public static int ROW_IDX_SUM           = 0;    //汇总行号
    public static int ROW_IDX_DATA          = 0;    //数据起始行号

    public static int COL_IDX_ID            = 0;    //
    public static int COL_IDX_TEAM          = 2;    //
    public static int COL_IDX_TYPE          = 4;    //issue类型列号
    public static int COL_IDX_KEY           = 5;    //
    public static int COL_IDX_STATUS        = 7;    //状态列号
    public static int COL_IDX_OWNER         = 8;    //经办人列号
    public static int COL_IDX_EST_HOUR      = 9;    //估算时间(小时)列号
    public static int COL_IDX_LEFT_HOUR     = 10;    //剩余时间(小时)列号
    public static int COL_IDX_DESC          = 0;     //描述
    public static int COL_IDX_COMMENTS      = 0;     //备注

    public static final List<Integer> TEAM_GROUP_START_ROW = new ArrayList<>();
    public static final List<Integer> STORY_GROUP_START_ROW = new ArrayList<>();
    public static Set<String> PEOPLE = new HashSet<>();//负责人姓名
    public static double TOTAL_ESTIMATE = 0;
    public static double TOTAL_REMAIN   = 0;


    public static String write(String boardId, String sprintId) {
        if (StringUtils.isBlank(boardId))  return null;

        JiraBoard jiraBoard = JIRA.getBoardCache(boardId);
        String boardName = jiraBoard.getBoardName();

        String sprintName = "";

        //1 创建一个workbook对应一个excel文件
        HSSFWorkbook workbook = new HSSFWorkbook();

        if (StringUtils.isNotBlank(sprintId)) {
            JiraSprint jiraSprint = JIRA.getSprintCache(sprintId);

            sprintName = jiraSprint.getSprintName();

            Map<String, JiraIssue> issuesInSprintMap = JIRA.getIssueMapWithSameOrder(boardId, sprintId);
            createSprintSheet(workbook, boardName, sprintName, issuesInSprintMap);
        }

        Map<String, JiraIssue> backlogMap = JIRA.getBacklogIdByJiraUIOrder(boardId);
        createBacklogSheet(workbook, boardName, backlogMap);
        File saveFile = new File(EXCEL_FILE.replace("{boardName}", boardName)
                .replace("{sprintName}", sprintName)
                .replace("{date}", System.currentTimeMillis() + ""));

        //将文件保存到指定的位置
        try {
            FileOutputStream fos = new FileOutputStream(saveFile);
            workbook.write(fos);
            System.out.println("Export Succ!" + saveFile.getAbsolutePath());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return saveFile.getAbsolutePath();
    }

    private static void _clearData() {
        STORY_GROUP_START_ROW.clear();
        TEAM_GROUP_START_ROW.clear();
        PEOPLE.clear();
        TOTAL_ESTIMATE = 0;
        TOTAL_REMAIN = 0;
    }

    private static void createSprintSheet(HSSFWorkbook workbook, String boardName, String sprintName, Map<String, JiraIssue> issuesInSprintMap) {
        _clearData();

        //将TeamId， 保持issue原序号 拼装为等长字符，实现分组和父子排序
        Map<String, JiraIssue> excelSprintIssue = new TreeMap<String, JiraIssue>();

        int seq = 0;
        for (JiraIssue issue : issuesInSprintMap.values()) {
            seq++;
            // 以防所有的没有子任务的issue都沉到底部，导致team无法分组，仍旧以team分组，
            // 然后再把no subtask的故事或者任务沉到团队内的底部
            excelSprintIssue.put(key4Sort(issue, seq), issue);
        }

        int rowIdx = 0;
        //2 在workbook中创建一个sheet对应excel中的sheet
        HSSFSheet sheet = workbook.createSheet("Sprint");

        //3 标题
        HSSFRow titleRow = sheet.createRow(rowIdx++); ROW_IDX_TITLE = rowIdx - 1;
        titleRow.createCell(0).setCellValue(boardName + "-" + sprintName);
        //子标题
        HSSFRow subtitleRow = sheet.createRow(rowIdx++); ROW_IDX_SUBTITLE = rowIdx - 1;
        subtitleRow.createCell(0).setCellValue(DATE_FORMAT.format(new Date()));

        //4 在sheet表中添加表头，老版本的poi对sheet的行列有限制
        HSSFRow headerRow = sheet.createRow(rowIdx++);  ROW_IDX_HEADER = rowIdx - 1;

        //2018-3-22： 新增自定义的字段，通过json的格式自动拼装到excel里。
        if (!CUSTOM_FIELDS_ADDED) {
            Object keys = PropertiesCache.getProp("excel.customfield.keys");

            if ( keys != null && StringUtils.isNotBlank(keys.toString())) {
                String customKeys = keys.toString();
                String customNames = PropertiesCache.getProp("excel.customfield.names").toString();

                CUSTOM_KEY_ARR = StringUtils.split(customKeys, ",");
                CUSTOM_NAME_ARR = StringUtils.split(customNames, ",");

                if (CUSTOM_KEY_ARR.length != CUSTOM_NAME_ARR.length) {
                    System.out.println("配置都能不一致，你还能干点啥！");
                } else {
                    COL_ADD_NUM = CUSTOM_KEY_ARR.length;
                }

                SPRINT_COL_WIDTH_PIXEL = Arrays.copyOf(SPRINT_COL_WIDTH_PIXEL,      COL_INIT_NUM + COL_ADD_NUM);
                SPRINT_COL_WIDTH_PIXEL_ALL = Arrays.copyOf(SPRINT_COL_WIDTH_PIXEL_ALL,  COL_INIT_NUM + COL_ADD_NUM);
                SPRINT_COL_NAME_ARR = Arrays.copyOf(SPRINT_COL_NAME_ARR,         COL_INIT_NUM + COL_ADD_NUM);

                for (int i = 0; i < COL_ADD_NUM; i++) {
                    SPRINT_COL_WIDTH_PIXEL[COL_INIT_NUM + i] = 100;
                    SPRINT_COL_WIDTH_PIXEL_ALL[COL_INIT_NUM + i] = 100;
                    SPRINT_COL_NAME_ARR[COL_INIT_NUM + i] =  CUSTOM_NAME_ARR[i];
                }
            }

            CUSTOM_FIELDS_ADDED = true;
        }

        //创建单元格，设置表头
        for (int columnIdx = 0; columnIdx < SPRINT_COL_WIDTH_PIXEL.length; columnIdx++) {
            headerRow.createCell(columnIdx).setCellValue(SPRINT_COL_NAME_ARR[columnIdx]);
        }

        //5 汇总行
        HSSFRow sumRow = sheet.createRow(rowIdx++); ROW_IDX_SUM = rowIdx - 1;
        for (int colIdx = 0; colIdx < SPRINT_COL_WIDTH_PIXEL.length; colIdx++) {
            sumRow.createCell(colIdx).setCellValue("");
        }
        //如果自定义字段都是数值，那自动给一个合计值在汇总行
        CUSTOM_RECORDS_SUM = new Double[COL_ADD_NUM];

        //写入实体数据，实际应用中这些数据从数据库得到,对象封装数据，集合包对象。对象的属性值对应表的每行的值
        String previousTeam = null;
        HSSFRow dataRow = null;
        ROW_IDX_DATA = rowIdx;
        for (Map.Entry<String, JiraIssue> entry : excelSprintIssue.entrySet()) {
            //每个故事/任务就是一个分组的开始行
            STORY_GROUP_START_ROW.add(rowIdx);

            String hiddenKey = entry.getKey();
            JiraIssue issue = entry.getValue();

            String currentTeam = StringUtils.trimToEmpty(issue.getCustomFields().get("teamName"));
            if (currentTeam.length() == 0) {
                currentTeam = boardName;
            }

            if (!currentTeam.equalsIgnoreCase(previousTeam)) {
                previousTeam = currentTeam;
                TEAM_GROUP_START_ROW.add(rowIdx);
            }

            dataRow = sheet.createRow(rowIdx++);
            createSprintColumns(dataRow, hiddenKey, issue, currentTeam);

            Map<String, JiraIssue> subTaskMap = issue.getSubTaskMap();
            if (subTaskMap != null && subTaskMap.size() > 0) {
                for (JiraIssue subTask : subTaskMap.values()) {
                    HSSFRow subRow = sheet.createRow(rowIdx++);
                    createSprintColumns(subRow, hiddenKey, subTask, currentTeam);
                }
            }
        }

        renderExcel("Sprint", workbook, sheet, rowIdx - 1, SPRINT_COL_WIDTH_PIXEL.length - 1);

        //写入汇总数据
        sumRow.getCell(COL_IDX_OWNER).setCellValue("总人力: " + PEOPLE.size());
        sumRow.getCell(COL_IDX_EST_HOUR).setCellValue("总估时: " + DF.format(TOTAL_ESTIMATE));
        sumRow.getCell(COL_IDX_LEFT_HOUR).setCellValue("总剩余: " + DF.format(TOTAL_REMAIN));
        for (int i = 0; i < COL_ADD_NUM; i++) {
            Double d = CUSTOM_RECORDS_SUM[i];
            if (d!=null && d > 0.0) {
                sumRow.getCell(COL_INIT_NUM + i).setCellValue(DF.format(d));
            }
        }

        //分组的加减号放到上方
        sheet.setRowSumsBelow(false);
        sheet.setRowSumsRight(false);
    }

    private static void createBacklogSheet(HSSFWorkbook workbook, String boardName, Map<String, JiraIssue> backlogMap) {
        _clearData();

        //将TeamId， 保持issue原序号 拼装为等长字符，实现分组和父子排序
//        Map<String, JiraIssue> excelBacklog = new TreeMap<String, JiraIssue>();
//
//        for (JiraIssue issue : backlogMap.values()) {
//            // 优先级高的靠前，进行中的靠前
//            excelBacklog.put(issue.getSortedKey(), issue);
//        }

        int rowIdx = 0;
        //2 在workbook中创建一个sheet对应excel中的sheet
        HSSFSheet sheet = workbook.createSheet("Backlog");

        //3 标题
        HSSFRow titleRow = sheet.createRow(rowIdx++); ROW_IDX_TITLE = rowIdx - 1;
        titleRow.createCell(0).setCellValue(boardName + " Backlog");
        //子标题
        HSSFRow subtitleRow = sheet.createRow(rowIdx++); ROW_IDX_SUBTITLE = rowIdx - 1;
        subtitleRow.createCell(0).setCellValue(DATE_FORMAT.format(new Date()));

        //4 在sheet表中添加表头，老版本的poi对sheet的行列有限制
        HSSFRow headerRow = sheet.createRow(rowIdx++);  ROW_IDX_HEADER = rowIdx - 1;

        //创建单元格，设置表头
        for (int columnIdx = 0; columnIdx < BACKLOG_COL_WIDTH_PIXEL.length; columnIdx++) {
            headerRow.createCell(columnIdx).setCellValue(BACKLOG_COL_NAME_ARR[columnIdx]);
        }

        //5 汇总行
        HSSFRow sumRow = sheet.createRow(rowIdx++); ROW_IDX_SUM = rowIdx - 1;
        for (int colIdx = 0; colIdx < BACKLOG_COL_WIDTH_PIXEL.length; colIdx++) {
            sumRow.createCell(colIdx).setCellValue("");
        }

        //如果自定义字段都是数值，那自动给一个合计值在汇总行
        CUSTOM_RECORDS_SUM = new Double[COL_ADD_NUM];

        //写入实体数据，实际应用中这些数据从数据库得到,对象封装数据，集合包对象。对象的属性值对应表的每行的值
        HSSFRow dataRow = null;
        ROW_IDX_DATA = rowIdx;
        TEAM_GROUP_START_ROW.add(rowIdx);
        for (Map.Entry<String, JiraIssue> entry : backlogMap.entrySet()) {
            //每个故事/任务就是一个分组的开始行
            STORY_GROUP_START_ROW.add(rowIdx);

            String hiddenKey = entry.getKey();
            JiraIssue issue = entry.getValue();

            dataRow = sheet.createRow(rowIdx++);
            createBacklogColumns(dataRow, hiddenKey, issue);

            if (issue.isHasSubtask()) {
                Map<String, JiraIssue> subTaskMap = issue.getSortedSubTaskMap();

                for (JiraIssue subTask : subTaskMap.values()) {
                    HSSFRow subRow = sheet.createRow(rowIdx++);
                    createBacklogColumns(subRow, hiddenKey, subTask);
                }
            }
        }

        renderExcel("backlog", workbook, sheet, rowIdx - 1, SPRINT_COL_WIDTH_PIXEL.length - 1);

        //分组的加减号放到上方
        sheet.setRowSumsBelow(false);
        sheet.setRowSumsRight(false);
    }

    private static void createBacklogColumns(HSSFRow row, String hiddenKey, JiraIssue issue) {
        int columnIdx = 0;
        row.createCell(columnIdx++).setCellValue(issue.getIssueId());   COL_IDX_ID = columnIdx - 1;
        row.createCell(columnIdx++).setCellValue(hiddenKey);
        row.createCell(columnIdx++).setCellValue(JIRA.getBoardCache(issue.getBoardId()).getBoardName()); COL_IDX_TEAM = columnIdx - 1;
        row.createCell(columnIdx++).setCellValue(renderIssueTypeInTree(issue));
        row.createCell(columnIdx++).setCellValue(issue.getIssueType()); COL_IDX_TYPE = columnIdx - 1;
        Cell linkCell = row.createCell(columnIdx++);
        CreationHelper createHelper = row.getSheet().getWorkbook().getCreationHelper();
        Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
        link.setAddress(JIRA.serverUrl + JIRA.BROWSE_ISSUE_URL.replace("{issueKey}", issue.getIssueKey()));
        linkCell.setCellValue(issue.getIssueKey());                     COL_IDX_KEY = columnIdx - 1;
        linkCell.setHyperlink(link);

        row.createCell(columnIdx++).setCellValue(issue.getPriority());
        row.createCell(columnIdx++).setCellValue(issue.getIssueName());
        row.createCell(columnIdx++).setCellValue(issue.getIssueStatus());   COL_IDX_STATUS = columnIdx - 1;
        row.createCell(columnIdx++).setCellValue(issue.getDescription());   COL_IDX_DESC = columnIdx - 1;
        row.createCell(columnIdx++).setCellValue(issue.getCreatedAt());
        row.createCell(columnIdx++).setCellValue(issue.getUpdatedAt());
        row.createCell(columnIdx++).setCellValue(issue.getComments());      COL_IDX_COMMENTS = columnIdx - 1;
    }

    private static void createSprintColumns(HSSFRow row, String hiddenKey, JiraIssue issue, String currentTeam) {
        //创建单元格设值
        int columnIdx = 0;
        row.createCell(columnIdx++).setCellValue(issue.getIssueId());   COL_IDX_ID = columnIdx - 1;
        row.createCell(columnIdx++).setCellValue(hiddenKey);
        row.createCell(columnIdx++).setCellValue(currentTeam);          COL_IDX_TEAM = columnIdx - 1;
        row.createCell(columnIdx++).setCellValue(renderIssueTypeInTree(issue));
        row.createCell(columnIdx++).setCellValue(issue.getIssueType()); COL_IDX_TYPE = columnIdx - 1;

        Cell linkCell = row.createCell(columnIdx++);
        CreationHelper createHelper = row.getSheet().getWorkbook().getCreationHelper();
        Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
        link.setAddress(JIRA.serverUrl + JIRA.BROWSE_ISSUE_URL.replace("{issueKey}", issue.getIssueKey()));
        linkCell.setCellValue(issue.getIssueKey());                     COL_IDX_KEY = columnIdx - 1;
        linkCell.setHyperlink(link);

        row.createCell(columnIdx++).setCellValue(issue.getIssueName());
        row.createCell(columnIdx++).setCellValue(issue.getIssueStatus()); COL_IDX_STATUS = columnIdx - 1;

        row.createCell(columnIdx++).setCellValue(issue.getOwner());
        if (StringUtils.isNotBlank(issue.getOwner())) {
            PEOPLE.add(issue.getOwner());
        }

        String est = issue.getEstimate();
        String remain = issue.getRemainingTime();
        String estInSec = issue.getEstimateInSeconds();
        String remainInSec = issue.getRemainingTimeInSeconds();

        if (issue.isSubTask()) {
            String estInHour = "未估算";
            if (StringUtils.isNumeric(estInSec)) {
                //时间转换成小时,该实现是基于估算时间最小颗粒为小时
                double _estinSec = Double.parseDouble(estInSec);

                if (_estinSec > 0 && _estinSec < 1800) {
                    estInHour = "估算不足半小时";
                } else {
                    double d = _estinSec / 3600;
                    estInHour = DF.format(d);
                    TOTAL_ESTIMATE += d;
                }
            }

            //完成任务的剩余时间自动置0
            String remainInHour = "0";
            if (!issue.isDone() && StringUtils.isNumeric(remainInSec)) {
                double _remaininSec = Double.parseDouble(remainInSec);

                if (_remaininSec > 0 && _remaininSec < 1800) {
                } else {
                    double d = _remaininSec / 3600;
                    remainInHour = DF.format(d);
                    TOTAL_REMAIN += d;
                }
            }

            row.createCell(columnIdx++).setCellValue(estInHour);    COL_IDX_EST_HOUR = columnIdx - 1;
            row.createCell(columnIdx++).setCellValue(remainInHour); COL_IDX_LEFT_HOUR = columnIdx - 1;

            row.createCell(columnIdx++).setCellValue(est);
            row.createCell(columnIdx++).setCellValue(remain);
        } else {
            row.createCell(columnIdx++).setCellValue("");
            row.createCell(columnIdx++).setCellValue("");

            row.createCell(columnIdx++).setCellValue("");
            row.createCell(columnIdx++).setCellValue("");
        }

        row.createCell(columnIdx++).setCellValue(issue.getJiraProject().getProjectKey());
        row.createCell(columnIdx++).setCellValue(issue.getJiraProject().getProjectName());
        row.createCell(columnIdx++).setCellValue(issue.getPriority());
        row.createCell(columnIdx++).setCellValue(issue.getDescription()); COL_IDX_DESC = columnIdx - 1;
        row.createCell(columnIdx++).setCellValue(issue.getCreatedAt());
        row.createCell(columnIdx++).setCellValue(issue.getUpdatedAt());
        row.createCell(columnIdx++).setCellValue(issue.getDueDate());

        Map<String, String> customFieldMap = issue.getCustomFields();
        for (int i = 0; i < COL_ADD_NUM; i++) {
            //支持正则表达式的展示 fields>customfield_12101>.*>value
            String customValue = JSONParsingUtil.getValueByExp(customFieldMap,CUSTOM_KEY_ARR[i]);
            row.createCell(columnIdx++).setCellValue(customValue);

            double addedValue = toDouble(customValue);
            if (addedValue != Double.MAX_VALUE) {
                CUSTOM_RECORDS_SUM[i] = (CUSTOM_RECORDS_SUM[i]==null ? 0.0:CUSTOM_RECORDS_SUM[i]);
                CUSTOM_RECORDS_SUM[i] += addedValue;
            }
        }
    }

    private static double toDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return Double.MAX_VALUE;
        }
    }

    //判断一行内某些单元格的内容自动换行后是否过高
    private static void setHighRowToDefault(HSSFRow row, short maxHeight, int... colsIdx) {
        boolean isRowHigh = false;
        for (int colIdx : colsIdx) {
            HSSFCell cell = row.getCell(colIdx);
            String cellValue = (cell == null ? "" : cell.getStringCellValue());
            int lines = _getCellLines(cellValue);
            int totalChars = StringUtils.length(cellValue);
            //默认就有4个换行，或者4个每行有25个字符
            if (lines >= 4 || totalChars > 25 * 4) {
                isRowHigh = true;
                break;
            }
        }

        if (isRowHigh) row.setHeightInPoints(maxHeight);
    }

    private static int _getCellLines(String str) {
        try {
            return str.split("\r\n").length;
        } catch (Exception e) {
        }
        return 0;
    }

    //渲染excel
    private static void renderExcel(String type, HSSFWorkbook workbook, HSSFSheet sheet, int rowCount, int colCount) {
        boolean isBacklogSheet = StringUtils.equalsIgnoreCase("backlog", type);


        short[] col_width_pixel = SPRINT_COL_WIDTH_PIXEL;
        short[] col_width_pixel_all = SPRINT_COL_WIDTH_PIXEL_ALL;
        if (isBacklogSheet) {
            col_width_pixel = BACKLOG_COL_WIDTH_PIXEL;
            col_width_pixel_all = BACKLOG_COL_WIDTH_PIXEL;
        }

        //合并标题列
        sheet.addMergedRegion(new CellRangeAddress(ROW_IDX_TITLE,ROW_IDX_TITLE,0, col_width_pixel.length - 1));
        sheet.getRow(ROW_IDX_TITLE).setHeightInPoints(40);

        sheet.addMergedRegion(new CellRangeAddress(ROW_IDX_SUBTITLE,ROW_IDX_SUBTITLE,0, col_width_pixel.length - 1));
        sheet.getRow(ROW_IDX_SUBTITLE).setHeightInPoints(20);

        sheet.getRow(ROW_IDX_HEADER).setHeightInPoints(35);

        //冰冻抬头
        sheet.createFreezePane(COL_IDX_STATUS, ROW_IDX_DATA);

        //隐藏hiddenKeys列, 其他列自动调整
//        sheet.setColumnHidden(0, true);

        String excelColumns = PropertiesCache.getProp("excelShowAllColumns");

        for (int i = 0; i <= colCount; i++){
            if (i < col_width_pixel.length) {
                int wu = 0;
                if ("1".equals(excelColumns)) {
                    wu = ExcelUtils.pixel2WidthUnits(col_width_pixel_all[i]);
                } else {
                    wu = ExcelUtils.pixel2WidthUnits(col_width_pixel[i]);
                }

                sheet.setColumnWidth(i, wu);//自动调整列宽度
            } else {
                sheet.autoSizeColumn(i);
            }
        }

        short contentSize = (short)(isBacklogSheet? 9 : 12);

        //标题样式
        HSSFCellStyle titleStyle = workbook.createCellStyle();
        HSSFFont titleFont = workbook.createFont();
        titleFont.setFontName("微软雅黑");
        titleFont.setFontHeightInPoints((short) 24);//设置字体大小
        titleFont.setBold(true);
        titleStyle.setFont(titleFont);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        //子标题样式
        HSSFCellStyle subtitleStyle = workbook.createCellStyle();
        HSSFFont subtitleFont = workbook.createFont();
        subtitleFont.setFontName("微软雅黑");
        subtitleFont.setFontHeightInPoints((short) 14);//设置字体大小
        subtitleFont.setBold(false);
        subtitleStyle.setFont(subtitleFont);
        subtitleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        subtitleStyle.setAlignment(HorizontalAlignment.RIGHT);

        //边框样式
        HSSFCellStyle borderStyle = workbook.createCellStyle();
        borderStyle.setBorderBottom(BorderStyle.THIN);    //下边框
        borderStyle.setBorderLeft(BorderStyle.THIN);      //左边框
        borderStyle.setBorderTop(BorderStyle.THIN);       //上边框
        borderStyle.setBorderRight(BorderStyle.THIN);     //右边框
        borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        //Team字体样式
        HSSFCellStyle teamStyle = workbook.createCellStyle();
        teamStyle.cloneStyleFrom(borderStyle);
        teamStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        teamStyle.setAlignment(HorizontalAlignment.CENTER);
        HSSFFont teamFont = workbook.createFont();
        teamFont.setFontName("微软雅黑");
        teamFont.setFontHeightInPoints((short) 14);//设置字体大小
        teamFont.setBold(true);
        teamStyle.setFont(teamFont);
        teamStyle.setRotation((short)255);

        //列头字体样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.cloneStyleFrom(borderStyle);
        HSSFFont headerFont = workbook.createFont();
        headerFont.setFontName("微软雅黑");
        headerFont.setFontHeightInPoints((short) 16);//设置字体大小
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);  //选择需要用到的字体格式
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
//        headerStyle.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.LIGHT_YELLOW.getIndex());
        headerStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_YELLOW.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        //汇总字体样式
        HSSFCellStyle sumStyle = workbook.createCellStyle();
        sumStyle.cloneStyleFrom(headerStyle);
        sumStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); //去除背景色
        sumStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        //内容字体样式
        HSSFCellStyle issueStyle = workbook.createCellStyle();
        issueStyle.cloneStyleFrom(borderStyle);
        HSSFFont issueFont = workbook.createFont();
        issueFont.setFontName("微软雅黑");
        issueFont.setFontHeightInPoints(contentSize);//设置字体大小
        issueStyle.setFont(issueFont);      //选择需要用到的字体格式
        issueStyle.setAlignment(HorizontalAlignment.LEFT);
        HSSFDataFormat df = workbook.createDataFormat();
        issueStyle.setDataFormat(df.getFormat("@"));    //设置文本格式
        if (isBacklogSheet) {
            issueStyle.setWrapText(true);
        }

        //类型 = 故事/任务 加粗
        HSSFCellStyle storyOrTaskStyle = workbook.createCellStyle();
        storyOrTaskStyle.cloneStyleFrom(issueStyle);
        HSSFFont storyOrTaskFont = workbook.createFont();
        storyOrTaskFont.setBold(true);
        storyOrTaskFont.setFontName("微软雅黑");
        storyOrTaskFont.setFontHeightInPoints(contentSize);//设置字体大小
        storyOrTaskStyle.setFont(storyOrTaskFont);      //选择需要用到的字体格式
        storyOrTaskStyle.setAlignment(HorizontalAlignment.LEFT);
        storyOrTaskStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_CORNFLOWER_BLUE.getIndex());
        storyOrTaskStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        //状态颜色
        HSSFCellStyle todoStyle = workbook.createCellStyle();
        todoStyle.cloneStyleFrom(issueStyle);
        todoStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_ORANGE.getIndex());
        todoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        HSSFCellStyle doingStyle = workbook.createCellStyle();
        doingStyle.cloneStyleFrom(issueStyle);
        doingStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_GREEN.getIndex());
        doingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        HSSFCellStyle doneStyle = workbook.createCellStyle();
        doneStyle.cloneStyleFrom(issueStyle);
        doneStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getIndex());
        doneStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        //链接样式
        CellStyle linkStyle4Issue = workbook.createCellStyle();
        linkStyle4Issue.cloneStyleFrom(issueStyle);
        Font hlink_font = workbook.createFont();
        hlink_font.setFontName("微软雅黑");
        hlink_font.setFontHeightInPoints(contentSize);
        hlink_font.setUnderline(Font.U_SINGLE);
        hlink_font.setColor(IndexedColors.BLACK.getIndex());
        linkStyle4Issue.setFont(hlink_font);
        linkStyle4Issue.setAlignment(HorizontalAlignment.CENTER);

        CellStyle linkStyle4Story = workbook.createCellStyle();
        linkStyle4Story.cloneStyleFrom(issueStyle);
        Font hlink_font_story = workbook.createFont();
        hlink_font_story.setFontName("微软雅黑");
        hlink_font_story.setFontHeightInPoints(contentSize);
        hlink_font_story.setUnderline(Font.U_SINGLE);
        hlink_font_story.setColor(IndexedColors.BLACK.getIndex());
        linkStyle4Story.setFont(hlink_font_story);
        linkStyle4Story.setAlignment(HorizontalAlignment.CENTER);
        linkStyle4Story.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_CORNFLOWER_BLUE.getIndex());
        linkStyle4Story.setFillPattern(FillPatternType.SOLID_FOREGROUND);


        for (int row = 0; row <= rowCount; row++) {
            HSSFRow excelRow = sheet.getRow(row);

            JiraIssue jiraIssue = null;
            for(int col = 0; col <= colCount; col++) {
                HSSFCell excelCell = excelRow.getCell(col);

                if (excelCell == null) break;

                if (row > ROW_IDX_SUM && col == COL_IDX_ID) {
                    String issueId = excelCell.getStringCellValue();
                    jiraIssue = JIRA.getIssueCached(issueId);
                }

                excelCell.setCellStyle(borderStyle);
                if (row == ROW_IDX_TITLE) {
                    excelCell.setCellStyle(titleStyle);
                } else if (row == ROW_IDX_SUBTITLE) {
                    excelCell.setCellStyle(subtitleStyle);
                } else if (row == ROW_IDX_HEADER) {
                    excelCell.setCellStyle(headerStyle);
                } else if (row == ROW_IDX_SUM) {
                    excelCell.setCellStyle(sumStyle);
                } else if (col == COL_IDX_STATUS && jiraIssue.isSubTask()) {
                    if (jiraIssue.isTodo()) {
                        excelCell.setCellStyle(todoStyle);
                    } else if (jiraIssue.isDoing()) {
                        excelCell.setCellStyle(doingStyle);
                    } else if (jiraIssue.isDone()) {
                        excelCell.setCellStyle(doneStyle);
                    }
                } else if (jiraIssue.isParent()){
                    if (isBacklogSheet && col >= COL_IDX_DESC) {
                        //backlog里的描述备注不需要使用故事的加粗和着色
                        excelCell.setCellStyle(issueStyle);
                    } else {
                        excelCell.setCellStyle(storyOrTaskStyle);
                    }
                } else {
                    excelCell.setCellStyle(issueStyle);
                }

                if (excelCell.getHyperlink() != null) {
                    if (jiraIssue.isParent()) {
                        excelCell.setCellStyle(linkStyle4Story);
                    } else {
                        excelCell.setCellStyle(linkStyle4Issue);
                    }
                }

            }
        }

        //若行数太多，以防自动换行表太长，设置一个最大高度。
        if (isBacklogSheet) {
            for (int rowIdx = ROW_IDX_DATA; rowIdx <= rowCount; rowIdx++) {
                HSSFRow excelRow = sheet.getRow(rowIdx);
                setHighRowToDefault(excelRow, (short)60, COL_IDX_DESC, COL_IDX_COMMENTS);
            }
        }

        //合并分组功能报错，无视。
        try {
            //Team分组
            for (int i = 0; i < TEAM_GROUP_START_ROW.size(); i++) {
                int groupStart = TEAM_GROUP_START_ROW.get(i);
                int groupEnd  = 0;
                if (i + 1 >= TEAM_GROUP_START_ROW.size()) {
                    groupEnd = rowCount;
                } else {
                    groupEnd = TEAM_GROUP_START_ROW.get(i + 1) - 1;
                }

                if (groupStart < groupEnd) {
                    sheet.groupRow(groupStart + 1, groupEnd);       //Otherwise group会粘连
                }
                //多于1行的合并才有意义
                if (groupStart < groupEnd) {
                    CellRangeAddress region = new CellRangeAddress(groupStart, groupEnd, COL_IDX_TEAM, COL_IDX_TEAM);
                    sheet.addMergedRegion(region);
                }

                HSSFRow regionRow = sheet.getRow(groupStart);
                if (regionRow != null) {
                    HSSFCell regionCell = regionRow.getCell(COL_IDX_TEAM);
                    regionCell.setCellStyle(teamStyle);
                }

                //HSSFRegionUtil;
                //RegionUtil
                //HSSFRegionUtil
            }

            //Story分组
            for (int i = 0; i < STORY_GROUP_START_ROW.size(); i++) {
                int groupStart = STORY_GROUP_START_ROW.get(i);
                int groupEnd  = 0;
                if (i + 1 >= STORY_GROUP_START_ROW.size()) {
                    groupEnd = rowCount;
                } else {
                    groupEnd = STORY_GROUP_START_ROW.get(i + 1) - 1;
                }
                if (groupStart < groupEnd) {
                    sheet.groupRow(groupStart + 1, groupEnd);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String renderIssueTypeInTree(JiraIssue jiraIssue) {
        String issueType = jiraIssue.getIssueType();
        if (jiraIssue.isParent()) {
            return "|- ";
        } else if (jiraIssue.isSubTask()) {
            return "|----- ";
        }

        return issueType;
    }

    public static void read(String excelPath) {
        File excelFile = new File(excelPath);

        POIFSFileSystem fileSystem = null;
        HSSFWorkbook workbook = null;

        try {
            fileSystem = new POIFSFileSystem(new BufferedInputStream(new FileInputStream(excelFile)));
            workbook = new HSSFWorkbook(fileSystem);
            HSSFSheet sheet = workbook.getSheetAt(workbook.getActiveSheetIndex());

            int lastRowIndex = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowIndex; i++) {
                HSSFRow row = sheet.getRow(i);

                short lastCellNum = row.getLastCellNum();

//                StringBuffer sb = new StringBuffer();
//                sb.append("COLOR_").append(_getCellValue(row.getCell(0)).toUpperCase()).append(" ")
//                        .append("(\"").append(_getCellValue(row.getCell(0)).toUpperCase()).append("\",")
//                        .append("\"").append(_getCellValue(row.getCell(1))).append("\",")
//                        .append("\"").append(_getCellValue(row.getCell(2))).append("\"),")
//                        ;
//
//                System.out.println(sb.toString());
//                for (int j = 0; j < lastCellNum; j++) {
//                    HSSFCell cell = row.getCell(j);
//                    String cellValue = _getCellValue(cell);
//
//
//                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
            }
            try {
                fileSystem.close();
            } catch (IOException e) {
            }
        }
    }

    private static String key4Sort(JiraIssue jiraIssue, int seq) {
        return StringUtils.leftPad(jiraIssue.getCustomFields().get("teamId"), PADDING_LEN, "0") +
                StringUtils.leftPad(String.valueOf(seq), PADDING_LEN, "0");
    }

    private static String key4SortBacklog(JiraIssue jiraIssue, int seq) {
        return jiraIssue.getPriority4Sort() +
                jiraIssue.getStatusCategory4Sort() +
                StringUtils.leftPad(String.valueOf(seq), PADDING_LEN, "0");
    }

    private static boolean isSameTeam(String hiddenKey1, String hiddenKey2) {
        return getTeamId(hiddenKey1).equals(getTeamId(hiddenKey2));
    }

    private static String getTeamId(String hiddenKey) {
        return StringUtils.substring(hiddenKey, 0, PADDING_LEN);
    }

    private static String getParentGroup(String hiddenKey) {
        return StringUtils.substring(hiddenKey, 0, PADDING_LEN * 2);
    }

    private static boolean _isCellBlank(HSSFCell cell) {
        String s = _getCellValue(cell);
        return s == null || s.trim().length() == 0;
    }

    private static String _getCellValue(HSSFCell cell) {
        if (cell==null) {
            return "";
        }
        String cellValue = null;

        FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();

        CellType type = cell.getCellTypeEnum();

        if (CellType.STRING == type) {
            cellValue = cell.getStringCellValue().trim();
        } else if (CellType.NUMERIC == type) {
            cellValue = String.valueOf(cell.getNumericCellValue()).trim();
        } else if (CellType.BOOLEAN == type) {
            cellValue = String.valueOf(cell.getBooleanCellValue()).trim();
        } else if (CellType.FORMULA == type) {
            cellValue = evaluator.evaluateInCell(cell).toString().trim();
        } else {
            cellValue = "";
        }

        return cellValue;
    }


    public static void main(String[] args) {
        System.out.println(StringUtils.substring("000000009500000154830000015483", 0, PADDING_LEN));
        System.out.println(StringUtils.substring("000000009500000154830000015483", PADDING_LEN, PADDING_LEN*2));
    }


}
