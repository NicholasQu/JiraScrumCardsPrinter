package cc.xiaoquer.jira.excel;

import cc.xiaoquer.jira.api.JIRA;
import cc.xiaoquer.jira.api.beans.JiraEpic;
import cc.xiaoquer.jira.api.beans.JiraIssue;
import cc.xiaoquer.jira.constant.FoldersConsts;
import cc.xiaoquer.jira.html.ViewOfProductLine;
import cc.xiaoquer.jira.storage.PropertiesCache;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Created by Nicholas on 2017/9/20.
 *
 */
public class VPLViaExcel extends ViewOfProductLine{

    public static final String EXCEL_FILE = FoldersConsts.OUTPUT_FOLDER + "/OQPL-{date}.xls";

    public static final int COL_INIT_NUM = COL_WIDTH_PIXEL.length;

    //显示所有列 对应环境变量 excelShowAllColumns
    public static short[] COL_WIDTH_PIXEL_ALL = COL_WIDTH_PIXEL;

    public static int COL_ADD_NUM  = 0;          //自定义字段的数量
    public static String[] CUSTOM_KEY_ARR;
    public static String[] CUSTOM_NAME_ARR;
    public static Double[] CUSTOM_RECORDS_SUM;    //自定义字段如果是数值型，自动计算一个合计值
    public static boolean CUSTOM_FIELDS_ADDED = false;   //是否已经添加了自定义的列

    public static int ROW_IDX_TITLE         = 0;    //标题行号
    public static int ROW_IDX_SUBTITLE      = 0;    //子标题行号
    public static int ROW_IDX_HEADER        = 0;    //列头行号
    public static int ROW_IDX_SUM           = 0;    //汇总行号
    public static int ROW_IDX_DATA          = 0;    //数据起始行号

    public static int COL_IDX_PRODUCT       = 0;    //产品名称
    public static int COL_IDX_PD_OWNER      = 1;    //产品负责人
    public static int COL_IDX_PD_PRIORITY   = 2;    //产品优先级
    public static int COL_IDX_PD_STATUS     = 3;    //产品状态
    public static int COL_IDX_PD_TOTAL_WORK = 4;    //产品总工时
    public static int COL_IDX_PD_TOTAL_COST = 5;    //产品总成本
    public static int COL_IDX_PD_REVENUE    = 6;    //产品收益情况
    public static int COL_IDX_PJ_NAME       = 7;    //所属迭代或者泳道

    public static int COL_IDX_US_STATUS     = 11;   //故事状态

    public static final List<Integer> PRODUCT_GROUP_START_ROW = new ArrayList<>();
    public static final List<Integer> PROJECT_GROUP_START_ROW = new ArrayList<>();

    public static String generate(Map<String, Map<String, JiraIssue>> epicIssuesMap) {
        if (epicIssuesMap == null || epicIssuesMap.size() == 0)  return null;

        PROJECT_GROUP_START_ROW.clear();
        PRODUCT_GROUP_START_ROW.clear();

        //解析数据，输入易于HTML排版的数据二维信息
        sortIssues(epicIssuesMap);

        //1 创建一个workbook对应一个excel文件
        HSSFWorkbook workbook = new HSSFWorkbook();

        renderExcel(workbook);

        File saveFile = new File(EXCEL_FILE.replace("{date}", DATE_FORMAT_SLIM.format(new Date())));

        //将文件保存到指定的位置
        try {
            FileOutputStream fos = new FileOutputStream(saveFile);
            workbook.write(fos);
            System.out.println("View OOQI ProductLines Succ!" + saveFile.getAbsolutePath());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return saveFile.getAbsolutePath();
    }

    private static void renderExcel(HSSFWorkbook workbook) {
        int rowIdx = 0;

        //2 在workbook中创建一个sheet对应excel中的sheet
        HSSFSheet sheet = workbook.createSheet("ViewOfProductLine");

        //3 标题
        HSSFRow titleRow = sheet.createRow(rowIdx++); ROW_IDX_TITLE = rowIdx - 1;
        titleRow.createCell(0).setCellValue(TITLE);
        //子标题
        HSSFRow subtitleRow = sheet.createRow(rowIdx++); ROW_IDX_SUBTITLE = rowIdx - 1;
        subtitleRow.createCell(0).setCellValue(DATE_FORMAT.format(new Date()));

        //4 在sheet表中添加表头，老版本的poi对sheet的行列有限制
        HSSFRow headerRow = sheet.createRow(rowIdx++);  ROW_IDX_HEADER = rowIdx - 1;

        //创建单元格，设置表头
        for (int columnIdx = 0; columnIdx < COL_WIDTH_PIXEL.length; columnIdx++) {
            headerRow.createCell(columnIdx).setCellValue(new HSSFRichTextString(COL_NAME_ARR[columnIdx].replaceAll("<br/>","\r\n")));
        }

        //5 汇总行
        HSSFRow sumRow = sheet.createRow(rowIdx++); ROW_IDX_SUM = rowIdx - 1;
        for (int colIdx = 0; colIdx < COL_WIDTH_PIXEL.length; colIdx++) {
            sumRow.createCell(colIdx).setCellValue("");
        }

        //6 写入行数据
        HSSFRow dataRow = null;
        ROW_IDX_DATA = rowIdx;
        for (String combinedKey : SORTED_ISSUES_MAP.keySet()) {
            JiraIssue jiraIssue = SORTED_ISSUES_MAP.get(combinedKey);

            dataRow = sheet.createRow(rowIdx++);

            createColumns(dataRow, jiraIssue.getJiraEpic(), jiraIssue);
        }

        //写入汇总数据

        //分组的加减号放到上方
        sheet.setRowSumsBelow(false);
        sheet.setRowSumsRight(false);

        renderStyle(workbook, sheet, rowIdx - 1, COL_WIDTH_PIXEL.length - 1);
    }

    private static void createColumns(HSSFRow row, JiraEpic jiraEpic, JiraIssue jiraIssue) {
        //创建单元格设值
        int columnIdx = 0;

        createCellWithLink(row, columnIdx++, jiraEpic.getEpicName(), jiraEpic.getEpicKey());
        row.createCell(columnIdx++).setCellValue(jiraEpic.getEpicOwner());
        row.createCell(columnIdx++).setCellValue(jiraEpic.getCustomPriority());
        row.createCell(columnIdx++).setCellValue(jiraEpic.getEpicStatus());
        row.createCell(columnIdx++).setCellValue(jiraEpic.getCustomTotalWorking());
        row.createCell(columnIdx++).setCellValue(jiraEpic.getCost());
        row.createCell(columnIdx++).setCellValue(jiraEpic.getCustomRevenueInfo());

        String[] displayProject = getDisplayProject(jiraEpic, jiraIssue);
        String projectKey  = displayProject[0];
        String projectName =  displayProject[1];
        createCellWithLink(row, columnIdx++, projectName, projectKey);

        createCellWithLink(row, columnIdx++, jiraIssue.getIssueName(), jiraIssue.getIssueKey());
        row.createCell(columnIdx++).setCellValue(jiraIssue.getOwner());
        row.createCell(columnIdx++).setCellValue(jiraIssue.getPriority());
        row.createCell(columnIdx++).setCellValue(jiraIssue.getIssueStatus());

        row.createCell(columnIdx++).setCellValue(jiraIssue.getDescription());
        row.createCell(columnIdx++).setCellValue(jiraIssue.getCustomEstimateDone());
        row.createCell(columnIdx++).setCellValue(jiraIssue.getCustomActualDone());
        row.createCell(columnIdx++).setCellValue(jiraIssue.getUpdatedAt());


    }

    private static void createCellWithLink(HSSFRow row, int columnIdx, String linkName, String issueKey) {
        Cell linkCell = row.createCell(columnIdx);

        CreationHelper createHelper = row.getSheet().getWorkbook().getCreationHelper();
        Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);

        if (StringUtils.isNotBlank(issueKey)) {
            link.setAddress(JIRA.serverUrl + JIRA.BROWSE_ISSUE_URL.replace("{issueKey}", issueKey));
        } else {
            link.setAddress(JIRA.serverUrl);
        }
        linkCell.setCellValue(linkName);
        linkCell.setHyperlink(link);
    }

    //渲染excel
    private static void renderStyle(HSSFWorkbook workbook, HSSFSheet sheet, int rowCount, int colCount) {
        //合并标题列
        sheet.addMergedRegion(new CellRangeAddress(ROW_IDX_TITLE,ROW_IDX_TITLE,0,COL_WIDTH_PIXEL.length - 1));
        sheet.getRow(ROW_IDX_TITLE).setHeightInPoints(40);

        sheet.addMergedRegion(new CellRangeAddress(ROW_IDX_SUBTITLE,ROW_IDX_SUBTITLE,0,COL_WIDTH_PIXEL.length - 1));
        sheet.getRow(ROW_IDX_SUBTITLE).setHeightInPoints(20);

        sheet.getRow(ROW_IDX_HEADER).setHeightInPoints(40);

        //隐藏hiddenKeys列, 其他列自动调整
//        sheet.setColumnHidden(0, true);

        String excelColumns = PropertiesCache.getProp("excelShowAllColumns");
        for (int i = 0; i <= colCount; i++){
            if (i < COL_WIDTH_PIXEL.length) {
                int wu = 0;
                if ("1".equals(excelColumns)) {
                    wu = ExcelUtils.pixel2WidthUnits(COL_WIDTH_PIXEL_ALL[i]);
                } else {
                    wu = ExcelUtils.pixel2WidthUnits(COL_WIDTH_PIXEL[i]);
                }

                sheet.setColumnWidth(i, wu);//自动调整列宽度
            } else {
                sheet.autoSizeColumn(i);
            }
        }

        //标题样式
        HSSFCellStyle titleStyle = workbook.createCellStyle();
        HSSFFont titleFont = workbook.createFont();
        titleFont.setFontName("微软雅黑");
        titleFont.setFontHeightInPoints((short) 20);//设置字体大小
        titleFont.setBold(true);
        titleStyle.setFont(titleFont);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        //子标题样式
        HSSFCellStyle subtitleStyle = workbook.createCellStyle();
        HSSFFont subtitleFont = workbook.createFont();
        subtitleFont.setFontName("微软雅黑");
        subtitleFont.setFontHeightInPoints((short) 10);//设置字体大小
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

        //PRODUCT字体样式
        HSSFCellStyle productStyle = workbook.createCellStyle();
        productStyle.cloneStyleFrom(borderStyle);
        productStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        productStyle.setAlignment(HorizontalAlignment.CENTER);
        HSSFFont teamFont = workbook.createFont();
        teamFont.setFontName("微软雅黑");
        teamFont.setFontHeightInPoints((short) 11);//设置字体大小
        teamFont.setBold(true);
        productStyle.setFont(teamFont);

        //列头字体样式
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.cloneStyleFrom(borderStyle);
        HSSFFont headerFont = workbook.createFont();
        headerFont.setFontName("微软雅黑");
        headerFont.setFontHeightInPoints((short) 11);//设置字体大小
        headerFont.setBold(true);
        headerStyle.setWrapText(true); //列头可以换行
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
        issueFont.setFontHeightInPoints((short) 10);//设置字体大小
        issueStyle.setFont(issueFont);      //选择需要用到的字体格式
        issueStyle.setAlignment(HorizontalAlignment.LEFT);
        HSSFDataFormat df = workbook.createDataFormat();
        issueStyle.setDataFormat(df.getFormat("@"));    //设置文本格式
//        issueStyle.setWrapText(true);

        //类型 = 故事/任务 加粗
        HSSFCellStyle storyOrTaskStyle = workbook.createCellStyle();
        storyOrTaskStyle.cloneStyleFrom(issueStyle);
        HSSFFont storyOrTaskFont = workbook.createFont();
        storyOrTaskFont.setBold(true);
        storyOrTaskFont.setFontName("微软雅黑");
        storyOrTaskFont.setFontHeightInPoints((short) 10);//设置字体大小
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
        CellStyle linkStyle = workbook.createCellStyle();
        linkStyle.cloneStyleFrom(borderStyle);
        Font hlink_font = workbook.createFont();
        hlink_font.setFontName("微软雅黑");
        hlink_font.setFontHeightInPoints((short) 10);
        hlink_font.setUnderline(Font.U_SINGLE);
        hlink_font.setColor(IndexedColors.BLUE.getIndex());
        linkStyle.setFont(hlink_font);

        List<JiraIssue> jiraIssueList = new ArrayList<>();
        jiraIssueList.addAll(SORTED_ISSUES_MAP.values());

        for (int row = 0; row <= rowCount; row++) {
            HSSFRow excelRow = sheet.getRow(row);

            if (row >= ROW_IDX_DATA) {
                JiraIssue jiraIssue = jiraIssueList.get(row - ROW_IDX_DATA);
                if (jiraIssue != null) {

                    HSSFCell pdStatusCell = excelRow.getCell(COL_IDX_PD_STATUS);
                    if (jiraIssue.getJiraEpic().isTodo()) {
                        pdStatusCell.setCellStyle(todoStyle);
                    } else if (jiraIssue.getJiraEpic().isDoing()) {
                        pdStatusCell.setCellStyle(doingStyle);
                    } else if (jiraIssue.getJiraEpic().isDone()) {
                        pdStatusCell.setCellStyle(doneStyle);
                    }

                    HSSFCell usStatusCell = excelRow.getCell(COL_IDX_US_STATUS);
                    if (jiraIssue.isTodo()) {
                        usStatusCell.setCellStyle(todoStyle);
                    } else if (jiraIssue.isDoing()) {
                        usStatusCell.setCellStyle(doingStyle);
                    } else if (jiraIssue.isDone()) {
                        usStatusCell.setCellStyle(doneStyle);
                    }
                }
            }

            for(int col = 0; col <= colCount; col++) {
                HSSFCell excelCell = excelRow.getCell(col);

                if (excelCell == null) break;

                excelCell.setCellStyle(borderStyle);
                if (row == ROW_IDX_TITLE) {
                    excelCell.setCellStyle(titleStyle);
                } else if (row == ROW_IDX_SUBTITLE) {
                    excelCell.setCellStyle(subtitleStyle);
                } else if (row == ROW_IDX_HEADER) {
                    excelCell.setCellStyle(headerStyle);
                } else if (row == ROW_IDX_SUM) {
                    excelCell.setCellStyle(sumStyle);
                } else {
                    excelCell.setCellStyle(issueStyle);
                }

                if (excelCell.getHyperlink() != null) {
                    excelCell.setCellStyle(linkStyle);
                }
            }
        }

        //将需要merge需要分组的行序号摘取出来
        String previousEpicGroupKey = "";
        String previousProjectGroupKey = "";

        String currEpicGroupKey = "";
        String currProjectGroupKey = "";

        int groupRowIdx = ROW_IDX_DATA;
        for (String combinedKey : SORTED_ISSUES_MAP.keySet()) {
            currEpicGroupKey = getEpicGroupKey(combinedKey);
            currProjectGroupKey = getEpicProjectGroupKey(combinedKey);

            //新的Epic
            if (!currEpicGroupKey.equals(previousEpicGroupKey)) {
                previousEpicGroupKey = currEpicGroupKey;
                PRODUCT_GROUP_START_ROW.add(groupRowIdx);
            }

            //新的Project
            if (!getEpicProjectGroupKey(combinedKey).equals(previousProjectGroupKey)) {
                previousProjectGroupKey = currProjectGroupKey;
                PROJECT_GROUP_START_ROW.add(groupRowIdx);
            }
            groupRowIdx++;
        }

        //Project分组
        for (int i = 0; i < PROJECT_GROUP_START_ROW.size(); i++) {
            int groupStart = PROJECT_GROUP_START_ROW.get(i);
            int groupEnd  = 0;
            if (i + 1 >= PROJECT_GROUP_START_ROW.size()) {
                groupEnd = rowCount;
            } else {
                groupEnd = PROJECT_GROUP_START_ROW.get(i + 1) - 1;
            }
            if (groupStart < groupEnd) {
                groupRows(sheet, groupStart + 1, groupEnd);

                mergeRows(sheet, groupStart, groupEnd, COL_IDX_PJ_NAME, COL_IDX_PJ_NAME);
            }
        }

        //产品分组
        for (int i = 0; i < PRODUCT_GROUP_START_ROW.size(); i++) {
            int groupStart = PRODUCT_GROUP_START_ROW.get(i);
            int groupEnd  = 0;
            if (i + 1 >= PRODUCT_GROUP_START_ROW.size()) {
                groupEnd = rowCount;
            } else {
                groupEnd = PRODUCT_GROUP_START_ROW.get(i + 1) - 1;
            }

            try {

                if (groupStart < groupEnd) {

                    groupRows(sheet, groupStart + 1, groupEnd);

                    mergeRows(sheet, groupStart, groupEnd, COL_IDX_PRODUCT, COL_IDX_PRODUCT);
                    mergeRows(sheet, groupStart, groupEnd, COL_IDX_PD_OWNER, COL_IDX_PD_OWNER);
                    mergeRows(sheet, groupStart, groupEnd, COL_IDX_PD_PRIORITY, COL_IDX_PD_PRIORITY);
                    mergeRows(sheet, groupStart, groupEnd, COL_IDX_PD_STATUS, COL_IDX_PD_STATUS);
                    mergeRows(sheet, groupStart, groupEnd, COL_IDX_PD_TOTAL_WORK, COL_IDX_PD_TOTAL_WORK);
                    mergeRows(sheet, groupStart, groupEnd, COL_IDX_PD_TOTAL_COST, COL_IDX_PD_TOTAL_COST);
                    mergeRows(sheet, groupStart, groupEnd, COL_IDX_PD_REVENUE, COL_IDX_PD_REVENUE);
                }

                HSSFRow regionRow = sheet.getRow(groupStart);
                HSSFCell regionCell = regionRow.getCell(COL_IDX_PRODUCT);
                regionCell.setCellStyle(productStyle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void mergeRows(HSSFSheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        CellRangeAddress regionProduct = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
        sheet.addMergedRegion(regionProduct);
    }
    private static void groupRows(HSSFSheet sheet, int firstRow, int lastRow) {
        sheet.groupRow(firstRow + 1, lastRow);
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


}
