/*
 * Created by JFormDesigner on Tue Jun 05 10:09:28 CST 2018
 */

package cc.xiaoquer.jira.ui;


import cc.xiaoquer.jira.enums.IssueStatus;
import cc.xiaoquer.jira.enums.IssueType;
import com.github.lgooddatepicker.components.*;
import com.github.lgooddatepicker.components.ComponentEvent;
import com.github.lgooddatepicker.components.ComponentListener;
import com.github.lgooddatepicker.optionalusertools.DateChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;
import org.apache.commons.lang3.RandomUtils;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;

/**
 * @author Khainx
 */
public class DailyTaskRecordFrame {
    public DailyTaskRecordFrame() {
        initComponents();

        initOthers();
    }

    static DatePicker datePicker;
    static DatePickerSettings dateSettings;
    static Locale chineseLocale = new Locale("zh");
    //初始化其他自定义控件
    private void initOthers() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd EEE", chineseLocale);
        final LocalDate today = LocalDate.now();
        dateSettings = new DatePickerSettings(chineseLocale);
        dateSettings.setVisibleDateTextField(false);
        datePicker = new DatePicker(dateSettings);
        datePicker.setDateToToday();
        txtChosenDay.setText(dtf.format(today));

        //设置icon点击图片
        URL dateImageURL = DailyTaskRecordFrame.class.getResource("/images/datepickerbutton1.png");
        Image dateExampleImage = Toolkit.getDefaultToolkit().getImage(dateImageURL);
        ImageIcon dateExampleIcon = new ImageIcon(dateExampleImage);

        JButton datePickerButton = datePicker.getComponentToggleCalendarButton();
        datePickerButton.setText("");
        datePickerButton.setIcon(dateExampleIcon);

        datePicker.addDateChangeListener(new DateChangeListener() {
            @Override
            public void dateChanged(DateChangeEvent dateChangeEvent) {
                if (dateChangeEvent.getNewDate() != null) {
                    txtChosenDay.setText(dtf.format(dateChangeEvent.getNewDate()));
                }
            }
        });

        datePanel.add(datePicker);

        //Open Issue表格的列头
        TAB_OPENISSUES_COL_HEADERS.add("序号");
        TAB_OPENISSUES_COL_HEADERS.add("项目名称");
        TAB_OPENISSUES_COL_HEADERS.add("Issue类型");
        TAB_OPENISSUES_COL_HEADERS.add("Issue名称");
        TAB_OPENISSUES_COL_HEADERS.add("状态");
        TAB_OPENISSUES_COL_HEADERS.add("总估算");
        TAB_OPENISSUES_COL_HEADERS.add("剩余");

        for (int i=0;i<30;i++) {
            Map<String, String> oneRow = new LinkedHashMap<String, String>();
            oneRow.put("sn", String.valueOf(i+1));
            oneRow.put("project", "ETS");
            oneRow.put("issuetype", IssueType.values()[RandomUtils.nextInt(0, IssueType.values().length)].getDesc());
            oneRow.put("issuename", "ETS-"+i+"-修改数据库CTA");
            oneRow.put("status", IssueStatus.values()[RandomUtils.nextInt(0, IssueStatus.values().length)].getStatus());
            int total = RandomUtils.nextInt(10,100);
            oneRow.put("total", String.valueOf(total));
            oneRow.put("remain", String.valueOf(total-10));
            DATA_MAP_LIST.add(oneRow);
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //只能修改7天之内的数据
                dateSettings.setDateRangeLimits(today.minusDays(7), today);
                //渲染表
                renderTable();

                //
                cbStatus.addItem(IssueStatus.DOING.getStatus());
                cbStatus.addItem(IssueStatus.DONE.getStatus());
                cbStatus.addItem(IssueStatus.TODO.getStatus());
            }
        });
    }

    private List<Map>    DATA_MAP_LIST              = new ArrayList<>();
    private List<String> TAB_OPENISSUES_COL_HEADERS = new ArrayList<>();   //
    private int TAB_OPENISSUES_COL_INDEX_NO         = 0;   //
    private int TAB_OPENISSUES_COL_INDEX_PROJECT    = 1;   //
    private int TAB_OPENISSUES_COL_INDEX_ISSUETYPE  = 2;   //
    private int TAB_OPENISSUES_COL_INDEX_ISSUENAME  = 3;   //
    private int TAB_OPENISSUES_COL_INDEX_STATUS     = 4;   //


    //渲染表行、列、边框样式，和表数据
    private void renderTable() {
        //将数据塞入二维数组内
        Object [ ][ ] data = new Object [DATA_MAP_LIST.size()][ ];  //动态创建第一维, 去除列头
        for (int i = 0; i < DATA_MAP_LIST.size(); i++ ) {
            Map<String, Object> rowMap = DATA_MAP_LIST.get(i);
            data [ i ] = new Object[rowMap.size()];                    //动态创建第二维
            int j = 0;
            for( Object o : rowMap.values()) {
                data [ i ][ j ] = o;
                j++;
            }
        }

        //model包含列头和行数据
        DefaultTableModel model = new DefaultTableModel(data, TAB_OPENISSUES_COL_HEADERS.toArray()) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return String.class;
                    case 1:
                        return String.class;
                    default:
                        return String.class;
                }
            }
        };

        tabMyOpenIssues = new JTable() {
            private Border outside = new MatteBorder(2, 0, 2, 0, Color.RED);
            private Border inside = new EmptyBorder(0, 2, 0, 2);
            private Border highlight = new CompoundBorder(outside, inside);

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {

//                Color top       = Color.WHITE;
                Color left = column % 2 == 0 ? Color.ORANGE : Color.PINK;
//            Color bottom    = Color.WHITE;
//            Color right     = Color.WHITE;

                Border border = BorderFactory.createCompoundBorder();
//            border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(2,0,0,0,top));
                border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(0, 1, 0, 0, left));
//            border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(0,0,2,0,bottom));
//            border = BorderFactory.createCompoundBorder(border, BorderFactory.createMatteBorder(0,0,0,2,right));

                JComponent comp = (JComponent) super.prepareRenderer(renderer, row, column);

                comp.setBorder(border);

//                Color background = row % 2 == 0 ? Color.LIGHT_GRAY : Color.WHITE;
                Color background = Color.WHITE;
                Color fontColor = Color.BLACK;

                if (column == TAB_OPENISSUES_COL_INDEX_STATUS) {
                    int modelRow = convertRowIndexToModel(row);
                    String status = (String) getModel().getValueAt(modelRow, TAB_OPENISSUES_COL_INDEX_STATUS);

                    if (IssueStatus.DOING == IssueStatus.of(status)) {
                       background = Color.ORANGE;
                    } else if (IssueStatus.TODO == IssueStatus.of(status)) {
                        background = Color.YELLOW;
                    } else if (IssueStatus.DONE == IssueStatus.of(status)) {
                        background = Color.DARK_GRAY;
                        fontColor = Color.white;
                    }
                }

                comp.setForeground(fontColor);
                comp.setBackground(background);

                if (isRowSelected(row)) {
                    //使用背景色来展示选中
//                    comp.setForeground(Color.BLUE);
//                    comp.setBackground(Color.ORANGE);
                    //使用边框来显示选中
                    comp.setBorder(highlight);
                }

                int rendererWidth = comp.getPreferredSize().width;
                TableColumn tableColumn = getColumnModel().getColumn(column);

                int newWidth = rendererWidth + getIntercellSpacing().width;
                int oldWidth = tableColumn.getPreferredWidth();
                //剩余宽度足以再扩张
                if (newWidth > oldWidth && blankWidth() > newWidth - oldWidth) {
                    tableColumn.setPreferredWidth(newWidth);
                }

                return comp;
            }
        };

        //设置cell间空白间隙
        tabMyOpenIssues.setIntercellSpacing(new Dimension(5,5));

        //设置行高
        tabMyOpenIssues.setRowHeight(20);
        /*
        JTable.getTableHeader().setPreferredSize(new Dimension(0, 50));
        上述的函數在一般 JTable 使用沒有問題，但是如果使用 JTable.setAutoResizeMode (JTable.AUTO_RESIZE_OFF); 則會造成 TableHeader 不會隨著 Scroll Bar 移動。
        主要原因是 TableHeader width 被設成 0，所以必須設定正確的 width 就沒有問題了。

        JTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTable.getTableHeader().setPreferredSize(ew Dimension(JTable.getColumnModel().getTotalColumnWidth(), 50));
        上述函數在一般狀況是沒有問題的，但是如果有使用者去更動 column width 時，就會發現最後的欄位有問題。
        總而言知，還是因為 TableHeader width 的問題。最後只好靠著修正 DefaultTableCellHeaderRenderer，才能真正改動 header height。
         */
        //header.setPreferredSize(new Dimension(20, 20));
        JTableHeader header = tabMyOpenIssues.getTableHeader();
        JLabel tmp = ((DefaultTableCellRenderer)header.getDefaultRenderer());
        tmp.setPreferredSize(new Dimension(30, 30));
        tmp.setHorizontalAlignment(JLabel.CENTER);

        tabMyOpenIssues.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabMyOpenIssues.setModel(model);

        tabMyOpenIssues.setAutoCreateRowSorter(true);
        tabMyOpenIssues.setBackground(Color.white);
        tabMyOpenIssues.setShowHorizontalLines(true);
        tabMyOpenIssues.setShowVerticalLines(true);
        tabMyOpenIssues.setOpaque(false);
        tabMyOpenIssues.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
        tabMyOpenIssues.setSelectionBackground(new Color(102, 102, 102));

        tabMyOpenIssues.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tabMyOpenIssues.rowAtPoint(e.getPoint());
                int col = tabMyOpenIssues.columnAtPoint(e.getPoint());
                __tabMyOpenIssueClick(row, col);
            }
        });

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //有效利用空白宽度，调宽子任务和故事列
                TableColumnModel columnModel = tabMyOpenIssues.getColumnModel();
                int remain = blankWidth() - 8; //以防贴太紧
                if (remain > 0) {
                    TableColumn tableColumn = columnModel.getColumn(TAB_OPENISSUES_COL_INDEX_ISSUENAME);
                    tableColumn.setPreferredWidth(tableColumn.getPreferredWidth() + (int)(remain * 0.9));
                }
            }
        });

        scrollPaneMyOpenIssues.setViewportView(tabMyOpenIssues);
    }

    private void __tabMyOpenIssueClick(int row, int col) {
        DefaultTableModel model = (DefaultTableModel)tabMyOpenIssues.getModel();
        int _selectedRow = tabMyOpenIssues.getSelectedRow();
        Map<String, String> map = DATA_MAP_LIST.get(row);
        cbStatus.setSelectedItem(map.get("status"));
        txtTotalEst.setText(map.get("total"));
        txtRemain.setText(map.get("remain"));
    }

    //判断表格在scrollpanel内仍有多少空白
    private int blankWidth() {
        TableColumnModel columnModel = tabMyOpenIssues.getColumnModel();
        int panelWidth = scrollPaneMyOpenIssues.getWidth();
//        int sumColsWidth = columnModel.getTotalColumnWidth();
        int sumColsWidth = 0;
        for (int i=0;i<columnModel.getColumnCount();i++) {
            sumColsWidth += columnModel.getColumn(i).getPreferredWidth();
        }
        return panelWidth - sumColsWidth;
    }

    public void show() {
        dailyFrame.setVisible(true);
    }

    private void dailyFrameWindowClosed(WindowEvent e) {
        System.exit(1);
    }

    private void dailyFrameWindowClosing(WindowEvent e) {
        System.exit(1);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("resources");
        dailyFrame = new JFrame();
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        datePanel = new JPanel();
        txtChosenDay = new JTextField();
        panel1 = new JPanel();
        label3 = new JLabel();
        scrollPaneMyOpenIssues = new JScrollPane();
        tabMyOpenIssues = new JTable();
        panel2 = new JPanel();
        label7 = new JLabel();
        label2 = new JLabel();
        cbStatus = new JComboBox();
        label1 = new JLabel();
        txtTotalEst = new JTextField();
        label8 = new JLabel();
        txtRemain = new JTextField();
        label5 = new JLabel();
        txtStartTime = new JTextField();
        label10 = new JLabel();
        txtConsumed = new JTextField();
        label6 = new JLabel();
        scrollPane2 = new JScrollPane();
        txtDesc = new JTextArea();
        button1 = new JButton();
        panel3 = new JPanel();
        label9 = new JLabel();
        scrollPane3 = new JScrollPane();
        table2 = new JTable();
        label11 = new JLabel();
        label15 = new JLabel();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();

        //======== dailyFrame ========
        {
            dailyFrame.setTitle("DailyTaskRecord");
            dailyFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    dailyFrameWindowClosed(e);
                    dailyFrameWindowClosed(e);
                }
                @Override
                public void windowClosing(WindowEvent e) {
                    dailyFrameWindowClosing(e);
                }
            });
            Container dailyFrameContentPane = dailyFrame.getContentPane();
            dailyFrameContentPane.setLayout(new BorderLayout());

            //======== dialogPane ========
            {
                dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
                dialogPane.setLayout(new BorderLayout());

                //======== contentPanel ========
                {
                    contentPanel.setLayout(new GridBagLayout());
                    ((GridBagLayout)contentPanel.getLayout()).columnWidths = new int[] {12, 732, 14, 0};
                    ((GridBagLayout)contentPanel.getLayout()).rowHeights = new int[] {30, 156, 11, 201, 10, 43, 0};
                    ((GridBagLayout)contentPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
                    ((GridBagLayout)contentPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                    //======== datePanel ========
                    {
                        datePanel.setLayout(new GridBagLayout());
                        ((GridBagLayout)datePanel.getLayout()).columnWidths = new int[] {287, 168, 126, 0};
                        ((GridBagLayout)datePanel.getLayout()).rowHeights = new int[] {35, 0};
                        ((GridBagLayout)datePanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
                        ((GridBagLayout)datePanel.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

                        //---- txtChosenDay ----
                        txtChosenDay.setFont(new Font("sansserif", Font.BOLD, 14));
                        txtChosenDay.setText("2018-06-06 \u661f\u671f\u4e09");
                        txtChosenDay.setEditable(false);
                        txtChosenDay.setBackground(Color.pink);
                        datePanel.add(txtChosenDay, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 5), 0, 0));
                    }
                    contentPanel.add(datePanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //======== panel1 ========
                    {
                        panel1.setBorder(LineBorder.createBlackLineBorder());
                        panel1.setLayout(new GridBagLayout());
                        ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {710, 0};
                        ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {36, 206, 0};
                        ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
                        ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

                        //---- label3 ----
                        label3.setText(bundle.getString("DailyTaskRecordFrame.label3.text"));
                        panel1.add(label3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //======== scrollPaneMyOpenIssues ========
                        {

                            //---- tabMyOpenIssues ----
                            tabMyOpenIssues.setPreferredScrollableViewportSize(new Dimension(450, 200));
                            tabMyOpenIssues.setBorder(LineBorder.createBlackLineBorder());
                            scrollPaneMyOpenIssues.setViewportView(tabMyOpenIssues);
                        }
                        panel1.add(scrollPaneMyOpenIssues, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 0), 0, 0));
                    }
                    contentPanel.add(panel1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //======== panel2 ========
                    {
                        panel2.setBorder(LineBorder.createBlackLineBorder());
                        panel2.setLayout(new GridBagLayout());
                        ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 143, 113, 232, 0};
                        ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 25, 0, 32, 0, 0};
                        ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
                        ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

                        //---- label7 ----
                        label7.setText(bundle.getString("DailyTaskRecordFrame.label7.text"));
                        panel2.add(label7, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- label2 ----
                        label2.setText(bundle.getString("DailyTaskRecordFrame.label2.text"));
                        panel2.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));
                        panel2.add(cbStatus, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- label1 ----
                        label1.setText(bundle.getString("DailyTaskRecordFrame.label1.text"));
                        panel2.add(label1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- txtTotalEst ----
                        txtTotalEst.setText(bundle.getString("DailyTaskRecordFrame.txtTotalEst.text"));
                        panel2.add(txtTotalEst, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- label8 ----
                        label8.setText(bundle.getString("DailyTaskRecordFrame.label8.text"));
                        panel2.add(label8, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- txtRemain ----
                        txtRemain.setText(bundle.getString("DailyTaskRecordFrame.txtRemain.text"));
                        txtRemain.setHorizontalAlignment(SwingConstants.LEFT);
                        panel2.add(txtRemain, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- label5 ----
                        label5.setText(bundle.getString("DailyTaskRecordFrame.label5.text"));
                        panel2.add(label5, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- txtStartTime ----
                        txtStartTime.setText(bundle.getString("DailyTaskRecordFrame.txtStartTime.text"));
                        panel2.add(txtStartTime, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- label10 ----
                        label10.setText(bundle.getString("DailyTaskRecordFrame.label10.text"));
                        panel2.add(label10, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));
                        panel2.add(txtConsumed, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- label6 ----
                        label6.setText(bundle.getString("DailyTaskRecordFrame.label6.text"));
                        panel2.add(label6, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //======== scrollPane2 ========
                        {

                            //---- txtDesc ----
                            txtDesc.setText("[UPDATE BY DTR]:");
                            txtDesc.setLineWrap(true);
                            scrollPane2.setViewportView(txtDesc);
                        }
                        panel2.add(scrollPane2, new GridBagConstraints(1, 4, 3, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- button1 ----
                        button1.setText(bundle.getString("DailyTaskRecordFrame.button1.text"));
                        panel2.add(button1, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 5), 0, 0));
                    }
                    contentPanel.add(panel2, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //======== panel3 ========
                    {
                        panel3.setBorder(LineBorder.createBlackLineBorder());
                        panel3.setLayout(new GridBagLayout());
                        ((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {624, 88, 0};
                        ((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 92, 1, 0};
                        ((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
                        ((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

                        //---- label9 ----
                        label9.setText(bundle.getString("DailyTaskRecordFrame.label9.text"));
                        panel3.add(label9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //======== scrollPane3 ========
                        {
                            scrollPane3.setViewportView(table2);
                        }
                        panel3.add(scrollPane3, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 0), 0, 0));

                        //---- label11 ----
                        label11.setText(bundle.getString("DailyTaskRecordFrame.label11.text"));
                        label11.setHorizontalAlignment(SwingConstants.TRAILING);
                        panel3.add(label11, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 5), 0, 0));

                        //---- label15 ----
                        label15.setText(bundle.getString("DailyTaskRecordFrame.label15.text"));
                        label15.setFont(label15.getFont().deriveFont(label15.getFont().getStyle() | Font.BOLD));
                        label15.setForeground(Color.red);
                        panel3.add(label15, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 0, 0), 0, 0));
                    }
                    contentPanel.add(panel3, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));
                }
                dialogPane.add(contentPanel, BorderLayout.CENTER);

                //======== buttonBar ========
                {
                    buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                    buttonBar.setLayout(new GridBagLayout());
                    ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
                    ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

                    //---- okButton ----
                    okButton.setText(bundle.getString("DailyTaskRecordFrame.okButton.text"));
                    buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- cancelButton ----
                    cancelButton.setText(bundle.getString("DailyTaskRecordFrame.cancelButton.text"));
                    buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
                }
                dialogPane.add(buttonBar, BorderLayout.SOUTH);
            }
            dailyFrameContentPane.add(dialogPane, BorderLayout.CENTER);
            dailyFrame.setSize(775, 805);
            dailyFrame.setLocationRelativeTo(null);
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JFrame dailyFrame;
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JPanel datePanel;
    private JTextField txtChosenDay;
    private JPanel panel1;
    private JLabel label3;
    private JScrollPane scrollPaneMyOpenIssues;
    private JTable tabMyOpenIssues;
    private JPanel panel2;
    private JLabel label7;
    private JLabel label2;
    private JComboBox cbStatus;
    private JLabel label1;
    private JTextField txtTotalEst;
    private JLabel label8;
    private JTextField txtRemain;
    private JLabel label5;
    private JTextField txtStartTime;
    private JLabel label10;
    private JTextField txtConsumed;
    private JLabel label6;
    private JScrollPane scrollPane2;
    private JTextArea txtDesc;
    private JButton button1;
    private JPanel panel3;
    private JLabel label9;
    private JScrollPane scrollPane3;
    private JTable table2;
    private JLabel label11;
    private JLabel label15;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
