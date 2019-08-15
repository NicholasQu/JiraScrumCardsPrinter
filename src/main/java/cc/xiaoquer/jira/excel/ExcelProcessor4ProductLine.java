//package cc.xiaoquer.jira.excel;
//
//import cc.xiaoquer.jira.api.JIRA;
//import cc.xiaoquer.jira.api.beans.JiraEpic;
//import cc.xiaoquer.jira.api.beans.JiraIssue;
//import cc.xiaoquer.jira.constant.FoldersConsts;
//import cc.xiaoquer.jira.storage.PropertiesCache;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.poi.common.usermodel.HyperlinkType;
//import org.apache.poi.hssf.usermodel.*;
//import org.apache.poi.hssf.util.HSSFColor;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.ss.util.CellRangeAddress;
//
//import java.io.*;
//import java.text.DecimalFormat;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//
///**
// * Created by Nicholas on 2017/9/20.
// *
// */
//public class ExcelProcessor4ProductLine {
//
//    public static final String EXCEL_FILE = FoldersConsts.OUTPUT_FOLDER + "/ViewOfProductLine-{date}.xls";
//    public static final int    PADDING_LEN    = 10;
//
//    /**
//     * 主题ID, hiddenKeys，团队，Tree，类型， 主题Key，
//     * 主题，状态，经办人，预估时间(H)，剩余时间(H), 预估时间，剩余时间
//     * 项目Key，项目名称，优先级, 描述
//     * 创建时间,更新时间,截止时间
//     * Browse
//     */
//    public static short[] SPRINT_COL_WIDTH_PIXEL = new short[] {
//            0,      0,      150,    80,     80,     100,
//            100,    300,     120,    80,    120,
//            100,    100,
//            100,    100,
//            100,    100,
//            100
//    };
//
//    public static final int COL_INIT_NUM = SPRINT_COL_WIDTH_PIXEL.length;
//
//    //显示所有列 对应环境变量 excelShowAllColumns
//    public static short[] SPRINT_COL_WIDTH_PIXEL_ALL = SPRINT_COL_WIDTH_PIXEL;
//
//    public static int COL_ADD_NUM  = 0;          //自定义字段的数量
//    public static String[] CUSTOM_KEY_ARR;
//    public static String[] CUSTOM_NAME_ARR;
//    public static Double[] CUSTOM_RECORDS_SUM;    //自定义字段如果是数值型，自动计算一个合计值
//    public static boolean CUSTOM_FIELDS_ADDED = false;   //是否已经添加了自定义的列
//
//    public static String[] SPRINT_COL_NAME_ARR = new String[] {
//            "Id",           "hiddenKeys",   "产品名称(&版本)",  "产品\r\n负责人",   "产品\n优先级",   "产品\n状态",
//            "迭代名称",     "用户故事",     "PO",        "优先级",       "US状态",
//            "预估\r\n开始", "预计\r\n结束",
//            "实际\r\n开始", "实际\r\n结束",
//            "创建\r\n时间", "更新\r\n时间",
//            "LINK JIRA"
//    };
//
//
//    private static final DecimalFormat DF = new DecimalFormat("#.#");
//    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    private static final SimpleDateFormat DATE_FORMAT_SLIM = new SimpleDateFormat("yyyyMMddHHmmss");
//
//    public static int ROW_IDX_TITLE         = 0;    //标题行号
//    public static int ROW_IDX_SUBTITLE      = 0;    //子标题行号
//    public static int ROW_IDX_HEADER        = 0;    //列头行号
//    public static int ROW_IDX_SUM           = 0;    //汇总行号
//    public static int ROW_IDX_DATA          = 0;    //数据起始行号
//
//    public static int COL_IDX_ID            = 0;    //
//    public static int COL_IDX_PRODUCT       = 2;    //
//    public static int COL_IDX_PD_OWNER      = 3;    //产品负责人
//    public static int COL_IDX_PD_PRIORITY   = 4;    //产品优先级
//    public static int COL_IDX_PD_STAUS      = 5;    //产品状态
//    public static int COL_IDX_US_STATUS     = 10;   //故事状态
//
//    public static final List<Integer> PRODUCT_GROUP_START_ROW = new ArrayList<>();
//    public static final List<Integer> PROJECT_GROUP_START_ROW = new ArrayList<>();
////    public static Set<String> PEOPLE = new HashSet<>();//负责人姓名
////    public static double TOTAL_ESTIMATE = 0;
////    public static double TOTAL_REMAIN   = 0;
//
//
//    public static String write(Map<JiraEpic, List<JiraIssue>> epicIssuesMap) {
//        if (epicIssuesMap == null || epicIssuesMap.size() == 0)  return null;
//
//        PROJECT_GROUP_START_ROW.clear();
//        PRODUCT_GROUP_START_ROW.clear();
////        PEOPLE.clear();
////        TOTAL_ESTIMATE = 0;
////        TOTAL_REMAIN = 0;
//
//        int rowIdx = 0;
//        //1 创建一个workbook对应一个excel文件
//        HSSFWorkbook workbook = new HSSFWorkbook();
//
//        //2 在workbook中创建一个sheet对应excel中的sheet
//        HSSFSheet sheet = workbook.createSheet("ViewOfProductLine");
//
//        //3 标题
//        HSSFRow titleRow = sheet.createRow(rowIdx++); ROW_IDX_TITLE = rowIdx - 1;
//        titleRow.createCell(0).setCellValue("奥琪金科产品线统一视图");
//        //子标题
//        HSSFRow subtitleRow = sheet.createRow(rowIdx++); ROW_IDX_SUBTITLE = rowIdx - 1;
//        subtitleRow.createCell(0).setCellValue(DATE_FORMAT.format(new Date()));
//
//        //4 在sheet表中添加表头，老版本的poi对sheet的行列有限制
//        HSSFRow headerRow = sheet.createRow(rowIdx++);  ROW_IDX_HEADER = rowIdx - 1;
//
//        //创建单元格，设置表头
//        for (int columnIdx = 0; columnIdx < SPRINT_COL_WIDTH_PIXEL.length; columnIdx++) {
//            headerRow.createCell(columnIdx).setCellValue(new HSSFRichTextString(SPRINT_COL_NAME_ARR[columnIdx]));
//        }
//
//        //5 汇总行
//        HSSFRow sumRow = sheet.createRow(rowIdx++); ROW_IDX_SUM = rowIdx - 1;
//        for (int colIdx = 0; colIdx < SPRINT_COL_WIDTH_PIXEL.length; colIdx++) {
//            sumRow.createCell(colIdx).setCellValue("");
//        }
//
//        //6 写入行数据
//        HSSFRow dataRow = null;
//        for (JiraEpic jiraEpic : epicIssuesMap.keySet()) {
//            //Epic是产品线的分组起始行
//            PRODUCT_GROUP_START_ROW.add(rowIdx);
//
//            List<JiraIssue> issuesInEpicList = epicIssuesMap.get(jiraEpic);
//            //EPIC下没有issue 打印一行后跳出
//            if (issuesInEpicList == null || issuesInEpicList.size() == 0) {
//                dataRow = sheet.createRow(rowIdx++);
//                createColumns(dataRow, jiraEpic, new JiraIssue());
//                break;
//            }
//
//            String previousProjectId = "";
//            for (JiraIssue jiraIssue : issuesInEpicList) {
//
//                if (!jiraIssue.getJiraProject().getProjectId().equals(previousProjectId)) {
//                    previousProjectId = jiraIssue.getJiraProject().getProjectId();
//                    PROJECT_GROUP_START_ROW.add(rowIdx);
//                }
//
//                dataRow = sheet.createRow(rowIdx++);
//                createColumns(dataRow, jiraEpic, jiraIssue);
//            }
//        }
//
//        renderExcel(workbook, sheet, rowIdx - 1, SPRINT_COL_WIDTH_PIXEL.length - 1);
//
//        //写入汇总数据
////        sumRow.getCell(COL_IDX_OWNER).setCellValue("总人力: " + PEOPLE.size());
////        sumRow.getCell(COL_IDX_EST_HOUR).setCellValue("总估时: " + DF.format(TOTAL_ESTIMATE));
////        sumRow.getCell(COL_IDX_LEFT_HOUR).setCellValue("总剩余: " + DF.format(TOTAL_REMAIN));
////        for (int i = 0; i < COL_ADD_NUM; i++) {
////            Double d = CUSTOM_RECORDS_SUM[i];
////            if (d!=null && d > 0.0) {
////                sumRow.getCell(COL_INIT_NUM + i).setCellValue(DF.format(d));
////            }
////        }
//
//        //分组的加减号放到上方
//        sheet.setRowSumsBelow(false);
//        sheet.setRowSumsRight(false);
//
//        File saveFile = new File(EXCEL_FILE.replace("{date}", DATE_FORMAT_SLIM.format(new Date())));
//
//        //将文件保存到指定的位置
//        try {
//            FileOutputStream fos = new FileOutputStream(saveFile);
//            workbook.write(fos);
//            System.out.println("View OOQI ProductLines Succ!" + saveFile.getAbsolutePath());
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return saveFile.getAbsolutePath();
//    }
//
//    private static void createColumns(HSSFRow row, JiraEpic jiraEpic, JiraIssue jiraIssue) {
//        String hiddenKey = String.valueOf(jiraEpic.getEpicId());
//        String productName = jiraEpic.getEpicName();
//
//        //产品进入一个Project项目，这个项目可能没有Sprint, 这时候使用projectName
//        String sprintName =  jiraIssue.getJiraSprint().getSprintName();
//        if (StringUtils.isBlank(sprintName)) {
//            sprintName = jiraIssue.getJiraProject().getProjectName();
//        }
//
//        //创建单元格设值
//        int columnIdx = 0;
//        row.createCell(columnIdx++).setCellValue(jiraIssue.getIssueId());
//        row.createCell(columnIdx++).setCellValue(hiddenKey);
//        row.createCell(columnIdx++).setCellValue(productName);
//        row.createCell(columnIdx++).setCellValue("韦娜");
//        row.createCell(columnIdx++).setCellValue(jiraEpic.getEpicPriority());
//        row.createCell(columnIdx++).setCellValue(jiraEpic.getEpicStatus());
//
//        row.createCell(columnIdx++).setCellValue(sprintName);
//        row.createCell(columnIdx++).setCellValue(jiraIssue.getIssueName());
//
//        row.createCell(columnIdx++).setCellValue(jiraIssue.getOwner());
//        row.createCell(columnIdx++).setCellValue(jiraIssue.getPriority());
//        row.createCell(columnIdx++).setCellValue(jiraIssue.getIssueStatus());
//
//        row.createCell(columnIdx++).setCellValue("");
//        row.createCell(columnIdx++).setCellValue("");
//        row.createCell(columnIdx++).setCellValue("");
//        row.createCell(columnIdx++).setCellValue("");
//
//        row.createCell(columnIdx++).setCellValue(jiraIssue.getCreatedAt());
//        row.createCell(columnIdx++).setCellValue(jiraIssue.getUpdatedAt());
//
//        Cell linkCell = row.createCell(columnIdx++);
//        CreationHelper createHelper = row.getSheet().getWorkbook().getCreationHelper();
//        Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
//        if (StringUtils.isNotBlank(jiraIssue.getIssueKey())) {
//            link.setAddress(JIRA.serverUrl + JIRA.BROWSE_ISSUE_URL.replace("{issueKey}", jiraIssue.getIssueKey()));
//        } else {
//            link.setAddress(JIRA.serverUrl);
//        }
//        linkCell.setCellValue("CLICK");
//        linkCell.setHyperlink(link);
//    }
//
//    //渲染excel
//    private static void renderExcel(HSSFWorkbook workbook, HSSFSheet sheet, int rowCount, int colCount) {
//        //合并标题列
//        sheet.addMergedRegion(new CellRangeAddress(ROW_IDX_TITLE,ROW_IDX_TITLE,0,SPRINT_COL_WIDTH_PIXEL.length - 1));
//        sheet.getRow(ROW_IDX_TITLE).setHeightInPoints(40);
//
//        sheet.addMergedRegion(new CellRangeAddress(ROW_IDX_SUBTITLE,ROW_IDX_SUBTITLE,0,SPRINT_COL_WIDTH_PIXEL.length - 1));
//        sheet.getRow(ROW_IDX_SUBTITLE).setHeightInPoints(20);
//
//        sheet.getRow(ROW_IDX_HEADER).setHeightInPoints(40);
//
//        //隐藏hiddenKeys列, 其他列自动调整
////        sheet.setColumnHidden(0, true);
//
//        String excelColumns = PropertiesCache.getProp("excelShowAllColumns");
//        for (int i = 0; i <= colCount; i++){
//            if (i < SPRINT_COL_WIDTH_PIXEL.length) {
//                int wu = 0;
//                if ("1".equals(excelColumns)) {
//                    wu = ExcelUtils.pixel2WidthUnits(SPRINT_COL_WIDTH_PIXEL_ALL[i]);
//                } else {
//                    wu = ExcelUtils.pixel2WidthUnits(SPRINT_COL_WIDTH_PIXEL[i]);
//                }
//
//                sheet.setColumnWidth(i, wu);//自动调整列宽度
//            } else {
//                sheet.autoSizeColumn(i);
//            }
//        }
//
//        //标题样式
//        HSSFCellStyle titleStyle = workbook.createCellStyle();
//        HSSFFont titleFont = workbook.createFont();
//        titleFont.setFontName("微软雅黑");
//        titleFont.setFontHeightInPoints((short) 24);//设置字体大小
//        titleFont.setBold(true);
//        titleStyle.setFont(titleFont);
//        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//        titleStyle.setAlignment(HorizontalAlignment.CENTER);
//        //子标题样式
//        HSSFCellStyle subtitleStyle = workbook.createCellStyle();
//        HSSFFont subtitleFont = workbook.createFont();
//        subtitleFont.setFontName("微软雅黑");
//        subtitleFont.setFontHeightInPoints((short) 14);//设置字体大小
//        subtitleFont.setBold(false);
//        subtitleStyle.setFont(subtitleFont);
//        subtitleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//        subtitleStyle.setAlignment(HorizontalAlignment.RIGHT);
//
//        //边框样式
//        HSSFCellStyle borderStyle = workbook.createCellStyle();
//        borderStyle.setBorderBottom(BorderStyle.THIN);    //下边框
//        borderStyle.setBorderLeft(BorderStyle.THIN);      //左边框
//        borderStyle.setBorderTop(BorderStyle.THIN);       //上边框
//        borderStyle.setBorderRight(BorderStyle.THIN);     //右边框
//        borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//
//        //PRODUCT字体样式
//        HSSFCellStyle productStyle = workbook.createCellStyle();
//        productStyle.cloneStyleFrom(borderStyle);
//        productStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//        productStyle.setAlignment(HorizontalAlignment.CENTER);
//        HSSFFont teamFont = workbook.createFont();
//        teamFont.setFontName("微软雅黑");
//        teamFont.setFontHeightInPoints((short) 14);//设置字体大小
//        teamFont.setBold(true);
//        productStyle.setFont(teamFont);
//
//        //列头字体样式
//        HSSFCellStyle headerStyle = workbook.createCellStyle();
//        headerStyle.cloneStyleFrom(borderStyle);
//        HSSFFont headerFont = workbook.createFont();
//        headerFont.setFontName("微软雅黑");
//        headerFont.setFontHeightInPoints((short) 14);//设置字体大小
//        headerFont.setBold(true);
//        headerStyle.setWrapText(true); //列头可以换行
//        headerStyle.setFont(headerFont);  //选择需要用到的字体格式
//        headerStyle.setAlignment(HorizontalAlignment.CENTER);
////        headerStyle.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.LIGHT_YELLOW.getIndex());
//        headerStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_YELLOW.getIndex());
//        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//
//        //汇总字体样式
//        HSSFCellStyle sumStyle = workbook.createCellStyle();
//        sumStyle.cloneStyleFrom(headerStyle);
//        sumStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex()); //去除背景色
//        sumStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//
//        //内容字体样式
//        HSSFCellStyle issueStyle = workbook.createCellStyle();
//        issueStyle.cloneStyleFrom(borderStyle);
//        HSSFFont issueFont = workbook.createFont();
//        issueFont.setFontName("微软雅黑");
//        issueFont.setFontHeightInPoints((short) 12);//设置字体大小
//        issueStyle.setFont(issueFont);      //选择需要用到的字体格式
//        issueStyle.setAlignment(HorizontalAlignment.LEFT);
//        HSSFDataFormat df = workbook.createDataFormat();
//        issueStyle.setDataFormat(df.getFormat("@"));    //设置文本格式
//
//        //类型 = 故事/任务 加粗
//        HSSFCellStyle storyOrTaskStyle = workbook.createCellStyle();
//        storyOrTaskStyle.cloneStyleFrom(issueStyle);
//        HSSFFont storyOrTaskFont = workbook.createFont();
//        storyOrTaskFont.setBold(true);
//        storyOrTaskFont.setFontName("微软雅黑");
//        storyOrTaskFont.setFontHeightInPoints((short) 12);//设置字体大小
//        storyOrTaskStyle.setFont(storyOrTaskFont);      //选择需要用到的字体格式
//        storyOrTaskStyle.setAlignment(HorizontalAlignment.LEFT);
//        storyOrTaskStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_CORNFLOWER_BLUE.getIndex());
//        storyOrTaskStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//
//        //状态颜色
//        HSSFCellStyle todoStyle = workbook.createCellStyle();
//        todoStyle.cloneStyleFrom(issueStyle);
//        todoStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_ORANGE.getIndex());
//        todoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//
//        HSSFCellStyle doingStyle = workbook.createCellStyle();
//        doingStyle.cloneStyleFrom(issueStyle);
//        doingStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_GREEN.getIndex());
//        doingStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//
//        HSSFCellStyle doneStyle = workbook.createCellStyle();
//        doneStyle.cloneStyleFrom(issueStyle);
//        doneStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT.getIndex());
//        doneStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//
//        //链接样式
//        CellStyle linkStyle = workbook.createCellStyle();
//        linkStyle.cloneStyleFrom(borderStyle);
//        Font hlink_font = workbook.createFont();
//        hlink_font.setFontName("微软雅黑");
//        hlink_font.setFontHeightInPoints((short) 12);
//        hlink_font.setUnderline(Font.U_SINGLE);
//        hlink_font.setColor(IndexedColors.BLUE.getIndex());
//        linkStyle.setFont(hlink_font);
//
//        for (int row = 0; row <= rowCount; row++) {
//            HSSFRow excelRow = sheet.getRow(row);
//
//            JiraIssue jiraIssue = null;
//            for(int col = 0; col <= colCount; col++) {
//                HSSFCell excelCell = excelRow.getCell(col);
//
//                if (excelCell == null) break;
//
//                if (row > ROW_IDX_SUM && col == COL_IDX_ID) {
//                    String issueId = excelCell.getStringCellValue();
//                    jiraIssue = JIRA.getIssueCached(issueId);
//                }
//
//                excelCell.setCellStyle(borderStyle);
//                if (row == ROW_IDX_TITLE) {
//                    excelCell.setCellStyle(titleStyle);
//                } else if (row == ROW_IDX_SUBTITLE) {
//                    excelCell.setCellStyle(subtitleStyle);
//                } else if (row == ROW_IDX_HEADER) {
//                    excelCell.setCellStyle(headerStyle);
//                } else if (row == ROW_IDX_SUM) {
//                    excelCell.setCellStyle(sumStyle);
////                } else if (jiraIssue != null && jiraIssue.isParent()){
////                    excelCell.setCellStyle(storyOrTaskStyle);
//                } else if (col == COL_IDX_US_STATUS && jiraIssue != null && jiraIssue.isSubTask()) {
//                    if (jiraIssue.isTodo()) {
//                        excelCell.setCellStyle(todoStyle);
//                    } else if (jiraIssue.isDoing()) {
//                        excelCell.setCellStyle(doingStyle);
//                    } else if (jiraIssue.isDone()) {
//                        excelCell.setCellStyle(doneStyle);
//                    }
//                } else {
//                    excelCell.setCellStyle(issueStyle);
//                }
//
//                if (excelCell.getHyperlink() != null) {
//                    excelCell.setCellStyle(linkStyle);
//                }
//
//            }
//        }
//
//        //Team分组
//        for (int i = 0; i < PRODUCT_GROUP_START_ROW.size(); i++) {
//            int groupStart = PRODUCT_GROUP_START_ROW.get(i);
//            int groupEnd  = 0;
//            if (i + 1 >= PRODUCT_GROUP_START_ROW.size()) {
//                groupEnd = rowCount;
//            } else {
//                groupEnd = PRODUCT_GROUP_START_ROW.get(i + 1) - 1;
//            }
//
//            try {
//                if (groupStart < groupEnd) {
//                    sheet.groupRow(groupStart + 1, groupEnd);       //Otherwise group会粘连
//                    CellRangeAddress regionProduct = new CellRangeAddress(groupStart, groupEnd, COL_IDX_PRODUCT, COL_IDX_PRODUCT);
//                    sheet.addMergedRegion(regionProduct);
//                    CellRangeAddress regionPdOwner = new CellRangeAddress(groupStart, groupEnd, COL_IDX_PD_OWNER, COL_IDX_PD_OWNER);
//                    sheet.addMergedRegion(regionPdOwner);
//                    CellRangeAddress regionPdPriority = new CellRangeAddress(groupStart, groupEnd, COL_IDX_PD_PRIORITY, COL_IDX_PD_PRIORITY);
//                    sheet.addMergedRegion(regionPdPriority);
//                    CellRangeAddress regionPdStatus = new CellRangeAddress(groupStart, groupEnd, COL_IDX_PD_STAUS, COL_IDX_PD_STAUS);
//                    sheet.addMergedRegion(regionPdStatus);
//                }
//
//                HSSFRow regionRow = sheet.getRow(groupStart);
//                HSSFCell regionCell = regionRow.getCell(COL_IDX_PRODUCT);
//                regionCell.setCellStyle(productStyle);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            //HSSFRegionUtil;
//            //RegionUtil
//            //HSSFRegionUtil
//        }
//
//        //Story分组
//        for (int i = 0; i < PROJECT_GROUP_START_ROW.size(); i++) {
//            int groupStart = PROJECT_GROUP_START_ROW.get(i);
//            int groupEnd  = 0;
//            if (i + 1 >= PROJECT_GROUP_START_ROW.size()) {
//                groupEnd = rowCount;
//            } else {
//                groupEnd = PROJECT_GROUP_START_ROW.get(i + 1) - 1;
//            }
//            if (groupStart < groupEnd) {
//                sheet.groupRow(groupStart + 1, groupEnd);
//            }
//        }
//    }
//
//    private static double toDouble(String s) {
//        try {
//            return Double.parseDouble(s);
//        } catch (Exception e) {
//            return Double.MAX_VALUE;
//        }
//    }
//
//    private static String renderIssueTypeInTree(JiraIssue jiraIssue) {
//        String issueType = jiraIssue.getIssueType();
//        if (jiraIssue.isParent()) {
//            return "|- ";
//        } else if (jiraIssue.isSubTask()) {
//            return "|----- ";
//        }
//
//        return issueType;
//    }
//
//    private static String key4Sort(JiraIssue jiraIssue, int seq) {
//        return StringUtils.leftPad(jiraIssue.getCustomFields().get("teamId"), PADDING_LEN, "0") +
//                StringUtils.leftPad(String.valueOf(seq), PADDING_LEN, "0");
//    }
//
//    private static boolean isSameTeam(String hiddenKey1, String hiddenKey2) {
//        return getTeamId(hiddenKey1).equals(getTeamId(hiddenKey2));
//    }
//
//    private static String getTeamId(String hiddenKey) {
//        return StringUtils.substring(hiddenKey, 0, PADDING_LEN);
//    }
//
//    private static String getParentGroup(String hiddenKey) {
//        return StringUtils.substring(hiddenKey, 0, PADDING_LEN * 2);
//    }
//
//    private static boolean _isCellBlank(HSSFCell cell) {
//        String s = _getCellValue(cell);
//        return s == null || s.trim().length() == 0;
//    }
//
//    private static String _getCellValue(HSSFCell cell) {
//        if (cell==null) {
//            return "";
//        }
//        String cellValue = null;
//
//        FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
//
//        CellType type = cell.getCellTypeEnum();
//
//        if (CellType.STRING == type) {
//            cellValue = cell.getStringCellValue().trim();
//        } else if (CellType.NUMERIC == type) {
//            cellValue = String.valueOf(cell.getNumericCellValue()).trim();
//        } else if (CellType.BOOLEAN == type) {
//            cellValue = String.valueOf(cell.getBooleanCellValue()).trim();
//        } else if (CellType.FORMULA == type) {
//            cellValue = evaluator.evaluateInCell(cell).toString().trim();
//        } else {
//            cellValue = "";
//        }
//
//        return cellValue;
//    }
//
//
//    public static void main(String[] args) {
//        System.out.println(StringUtils.substring("000000009500000154830000015483", 0, PADDING_LEN));
//        System.out.println(StringUtils.substring("000000009500000154830000015483", PADDING_LEN, PADDING_LEN*2));
//    }
//
//
//}
