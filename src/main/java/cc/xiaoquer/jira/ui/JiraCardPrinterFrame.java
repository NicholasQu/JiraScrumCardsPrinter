/*
 * Created by JFormDesigner on Sat Sep 02 21:04:27 CST 2017
 */

package cc.xiaoquer.jira.ui;

import cc.xiaoquer.jira.api.JIRA;
import cc.xiaoquer.jira.api.beans.JiraBoard;
import cc.xiaoquer.jira.api.beans.JiraEpic;
import cc.xiaoquer.jira.api.beans.JiraIssue;
import cc.xiaoquer.jira.api.beans.JiraSprint;
import cc.xiaoquer.jira.autoupdater.JiraAutoUpdater;
import cc.xiaoquer.jira.excel.ExcelProcessor4Sprint;
import cc.xiaoquer.jira.excel.VPLViaExcel;
import cc.xiaoquer.jira.html.HtmlGenerator;
import cc.xiaoquer.jira.html.VPLViaHtml;
import cc.xiaoquer.jira.storage.PropertiesCache;
import cc.xiaoquer.jira.uicomponents.checkboxtree.CheckBoxNodeData;
import cc.xiaoquer.jira.uicomponents.checkboxtree.CheckBoxNodeEditor;
import cc.xiaoquer.jira.uicomponents.checkboxtree.CheckBoxNodeRenderer;
import cc.xiaoquer.jira.uicomponents.jlist.JiraListCellRender;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * @author unknown
 */
public class JiraCardPrinterFrame {
    public JiraCardPrinterFrame() {
        initComponents();

        initCompSize();

        easterEggs();
    }

    private void initCompSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        int preferredWidth  = Math.min((int)(screenWidth * 0.7), 900);
        int preferredHeight = Math.min((int)(screenHeight * 0.9), screenHeight - 100); //Math.min(767, screenHeight - 100);

        jiraFrame.setSize(preferredWidth, preferredHeight);
        jiraFrame.setLocation((screenWidth - preferredWidth) / 2, 10);

        repaintSize();

        jiraFrame.addComponentListener( new ComponentAdapter() {
            @Override
            public void componentResized( ComponentEvent e ) {
                repaintSize();
            }
        } );

    }

    private void repaintSize() {
//        for (Component c : dialogPane.getComponents()) {
//        }

        resetPanel(dialogPane, jiraFrame.getWidth(), 0, jiraFrame.getHeight(), 1);

        int boardPanelWidth  = jiraFrame.getWidth() - 5;
        int boardPanelHeight = jiraFrame.getHeight() - serverPanel.getHeight() - statusPanel.getHeight() - 50;
        resetPanel(boardPanel, boardPanelWidth, 3, boardPanelHeight, 2);

//        resetPanel(serverPanel, boardPanelWidth, -1, -1, -1);
        resetPanel(statusPanel, boardPanelWidth, 9, -1, -1);
//                serverPanel.setSize(jiraFrame.getWidth() - 5, serverPanel.getHeight());
//                serverPanel.repaint();
//
//                statusPanel.setSize(jiraFrame.getWidth() - 5, statusPanel.getHeight());
//                statusPanel.repaint();
//                scrollPaneBoard.setSize(scrollPaneBoard.getWidth(), treeHeight);
//                scrollPaneBoard.repaint();
//                scrollPaneSprint.setSize(scrollPaneSprint.getWidth(), treeHeight);
//                scrollPaneSprint.repaint();
//                scrollPaneCard.setSize(boardPanel.getWidth(), treeHeight);
//                scrollPaneCard.repaint();
//                scrollPane3.setSize(jiraFrame.getWidth() - scrollPaneBoard.getWidth() - scrollPaneSprint.getWidth() - 2, scrollPane3.getHeight());
//                scrollPane3.repaint();
    }

    //动态变化某一行或者某一列的长度
    private void resetPanel(JPanel panel, int totalColWidth, int colIdex, int totalRowHeight, int rowIndex) {

        GridBagLayout myPanel = ((GridBagLayout) panel.getLayout());

        if (totalColWidth > 0) {
            int[] cols = myPanel.columnWidths;

            if (colIdex >= 0) {
                //传入的序号是正值，就把这个序号当做动态变化列
                myPanel.columnWidths = resetSpecificIndex(cols, totalColWidth, colIdex);
//                cols[colIdex] = totalColWidth - calcSumOfArrayExcept(cols, colIdex);
//                ((GridBagLayout) panel.getLayout()).columnWidths = cols;
            } else {
                //传入的序号是负值，表示所有行列按照比例增加或缩小
                myPanel.columnWidths = resetAll(cols, totalColWidth);
            }
        }

        if (totalRowHeight > 0) {
            int[] rows = myPanel.rowHeights;

            if (rowIndex >= 0) {
                myPanel.rowHeights = resetSpecificIndex(rows, totalRowHeight, rowIndex);
//                rows[rowIndex] = totalRowHeight - calcSumOfArrayExcept(rows, rowIndex);
//                ((GridBagLayout) panel.getLayout()).rowHeights = rows;
            } else {
                myPanel.rowHeights = resetAll(rows, totalRowHeight);
            }
        }

        panel.repaint();
    }

    private int[] resetSpecificIndex(int[] arrays, int total, int index) {
        int temp = total - calcSumOfArrayExcept(arrays, index);
        if (temp > 0) {
            arrays[index] = total - calcSumOfArrayExcept(arrays, index);
        }
        return arrays;
    }

    private int[] resetAll(int[] arrays, int total) {
        int sum = calcSumOfArrayExcept(arrays, -1);
        if (sum == 0) return arrays;
        double rate = (double)total / (double)sum;

//        if (rate < 1.1 && rate > 0.9) {
//            //缩放比例太小
//            arrays[arrays.length - 1] = total - calcSumOfArrayExcept(arrays, arrays.length - 1);
//            return arrays;
//        }

        int sumWithoutLast = 0;
        for (int i=0; i<arrays.length; i++) {
            if (i == arrays.length - 1) {
                arrays[i] = total - sumWithoutLast;
                break;
            }

            int tmp = (int)(arrays[i] * rate);
            sumWithoutLast += tmp;
            arrays[i] = tmp;
        }

        return arrays;
    }

    private int calcSumOfArrayExcept(int[] arrays, int exclude_index) {
        int sum = 0;
        for (int i=0; i<arrays.length; i++) {
            if (i != exclude_index) {
                sum += arrays[i];
            }
        }
        return sum;
    }

    /**
     * frame中的控件自适应frame大小：改变大小位置和字体
     * @param frame 要控制的窗体
     * @param proportionW 当前和原始的比例
     */
    public static void modifyComponentSize(JFrame frame,float proportionW,float proportionH){

        try
        {
            Component[] components = frame.getRootPane().getContentPane().getComponents();
            for(Component co:components)
            {
//              String a = co.getClass().getName();//获取类型名称
//              if(a.equals("javax.swing.JLabel"))
//              {
//              }
                float locX = co.getX() * proportionW;
                float locY = co.getY() * proportionH;
                float width = co.getWidth() * proportionW;
                float height = co.getHeight() * proportionH;
                co.setLocation((int)locX, (int)locY);
                co.setSize((int)width, (int)height);
                int size = (int)(co.getFont().getSize() * proportionH);
                Font font = new Font(co.getFont().getFontName(), co.getFont().getStyle(), size);
                co.setFont(font);
            }
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
    }

    private final static String SPLIT                   = "___";
    private final static int    PADDING_LEN             = 100;

    private final static String PADDING_SPACE;  //空白填充
    static {
        char[] b = new char[PADDING_LEN];
        Arrays.fill(b, ' ');
        PADDING_SPACE = new String(b);
    }

    private final static String BOARD_FILTER_TIPS           = "模糊查询回车过滤";
    private final static String SERV_STATUS_CONNECTING      = "连接中...";
    private final static String STATUS_BOARD_COUNT          = "Boards:[{}]个";
    private final static String STATUS_SPRINT_COUNT         = "Sprints:[{}]个";
    private final static String STATUS_ISSUE_CHECKED_COUNT  = "选中Issues:[{}]个";


    public static final String DIMENSION_1_STORY    = "Story";
    public static final String DIMENSION_1_TASK     = "Task";
    public static final String DIMENSION_1_SUBTASK  = "Subtask";
    public static final String DIMENSION_2_TODO     = "Todo";
    public static final String DIMENSION_2_DOING    = "Doing";
    public static final String DIMENSION_2_DONE     = "Done";

    private DefaultListModel boardsModel = new DefaultListModel();
    private JList jListBoards = new JList(boardsModel);

    private DefaultListModel sprintsModel = new DefaultListModel();
    private JList jlistSprints = new JList(sprintsModel);

    private DefaultMutableTreeNode root = new DefaultMutableTreeNode("Kanban");
    private DefaultTreeModel treeModel = new DefaultTreeModel(root);
    private JTree tree = new JTree(treeModel);

    /**
     * 启动设置password焦点
     * @param e
     */
    private void jiraFrameWindowActivated(WindowEvent e) {

        if (txtJiraUrl.getText().length() == 0) {
            txtJiraUrl.setText(PropertiesCache.getHost());
            txtJiraUrl.requestFocus();
        }
        if (txtUser.getText().length() == 0) {
            txtUser.setText(PropertiesCache.getUserName());
            txtUser.requestFocus();
        }
        if (txtPassword.getPassword().length == 0) {
            txtPassword.setText(PropertiesCache.getPassword());
            txtPassword.requestFocus();
        }

        if (!checkConnectInputsValid() || JIRA.connectStatus.length() > 0) return;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        });
    }

    private boolean checkConnectInputsValid() {
        String url = txtJiraUrl.getText();
        String user = txtUser.getText();
        String password = String.valueOf(txtPassword.getPassword());

        if (url == null || url.trim().length() == 0
                || !(url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://"))
                || user == null || user.trim().length() ==0
                || password == null || password.trim().length() == 0) {
            return false;
        }

        return true;
    }

    private void btnConnectMouseClicked(MouseEvent e) {
        if (!checkConnectInputsValid()) {
            JOptionPane.showMessageDialog
                    (null, "请输入有效的Jira地址和登录账号！", "警告", JOptionPane.ERROR_MESSAGE);
            return;
        }

        lblStatus.setText(SERV_STATUS_CONNECTING);
        lblStatus.updateUI();
        statusPanel.setBackground(Color.ORANGE);
        btnConnect.setEnabled(false);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                connect();
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e1) {
                }
                btnConnect.setEnabled(true);
            }
        });

    }

    private void txtPasswordKeyPressed(KeyEvent e) {
        if (e.getKeyChar() == e.VK_ENTER) {
            connect();
        }
    }

    private void connect() {
        String url = txtJiraUrl.getText();
        String user = txtUser.getText();
        String password = String.valueOf(txtPassword.getPassword());
        boolean isConnected = JIRA.connect(url, user, password);

        lblStatus.setText(JIRA.connectStatus);

        resetResultSetAndText();

        if (isConnected) {
            PropertiesCache.setHost(url);
            PropertiesCache.setUserName(user);
            PropertiesCache.setPassword(password);
            PropertiesCache.flush();


            boardPanel.setEnabled(true);

            txtFilterBoard.setText(PropertiesCache.getBoardFilter());
            txtFilterBoard.setEnabled(true);
            txtFilterBoard.setBackground(Color.WHITE);
            txtFilterBoardFocusLost(null);

            statusPanel.setBackground(new Color(136, 202, 121));
            //通过txtFilterBoard的焦点触发
//            renderBoards(PropertiesCache.getBoardFilter());
        } else {
            JOptionPane.showMessageDialog
                    (null, JIRA.connectStatus, "警告", JOptionPane.ERROR_MESSAGE);
            return;
        }


    }
    private void btnLogoutMouseClicked(MouseEvent e) {
        disconnect();
    }

    private void jiraFrameWindowClosing(WindowEvent e) {
        disconnect();
    }

    private void disconnect() {
        JIRA.disconnect();

        lblStatus.setText("断开连接");
        lblQueryStatus.setText("");
        lblBoardCount.setText(STATUS_BOARD_COUNT);
        lblSprintCount.setText(STATUS_SPRINT_COUNT);
        lblIssueCount.setText(STATUS_ISSUE_CHECKED_COUNT);

        statusPanel.setBackground(Color.LIGHT_GRAY);

        txtFilterBoard.setEnabled(false);
        txtFilterBoard.setBackground(new Color(214, 217, 223));
        boardPanel.setEnabled(false);

        resetResultSetAndText();
    }

    private void txtFilterBoardFocusGained(FocusEvent e) {
        if (BOARD_FILTER_TIPS.equals(txtFilterBoard.getText())) {
            txtFilterBoard.setText("");
            txtFilterBoard.setForeground(Color.BLACK);
        }
    }

    private void txtFilterBoardKeyReleased(KeyEvent e) {
        if (e.getKeyChar() == e.VK_ENTER) {
            filterBoard();
        }
    }

    private void txtFilterBoardFocusLost(FocusEvent e) {
        String prefix = txtFilterBoard.getText();
        if (prefix.length() == 0) {
            txtFilterBoard.setText(BOARD_FILTER_TIPS);
            txtFilterBoard.setForeground(Color.LIGHT_GRAY);
        }

        filterBoard();
    }

    private void filterBoard() {
        String prefix = txtFilterBoard.getText();

        if (BOARD_FILTER_TIPS.equalsIgnoreCase(prefix)) {
            prefix = "";
        }

        PropertiesCache.setBoardFilter(prefix);
        PropertiesCache.flush();

        renderBoards(prefix.trim());
    }

    private void asyncQuerySprints() {
        lblSelectedBoard.setText("查询中......");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                renderSprints(getIdByDisplayName((String)jListBoards.getSelectedValue()));
            }
        });
    }

    private void asyncQueryTree() {
        lblSelectedBoardSprint.setText("查询中......");
        final String s1 = (String)jListBoards.getSelectedValue();
        final String s2 = (String)jlistSprints.getSelectedValue();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                renderIssueTree(s1, s2);
            }
        });
    }

    public void show() {
        jiraFrame.setVisible(true);
    }

    private static String getByteStr(String str, int start, int end) {
        byte[] b = new byte[0];
        try {
            b = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("getByteStr error:" + str);
            e.printStackTrace();
        }

        return new String(b, start, end);
    }

    private String combineDisplayName(String id, String name) {
        if (name.length() >= PADDING_LEN) {
            return name + SPLIT + id;
        } else {
            return getByteStr(name + PADDING_SPACE, 0, PADDING_LEN) + SPLIT + id;
//            return (name + PADDING_SPACE).substring(0, PADDING_LEN) + SPLIT + id;
        }
    }

    private String getIdByDisplayName(String displayName) {
        if (displayName == null) return "";
        return displayName.split(SPLIT)[1];
    }

    private String getNameByDisplayName(String displayName) {
        if (displayName == null) return "";
        return displayName.split(SPLIT)[0].trim();
    }

    private String getHHMMSS() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

        return sdf.format(new Date());
    }

    private void renderBoards(String prefix) {
        resetResultSetAndText();
        //不强制刷新boards列表，只有点击登录才会强制刷新
        Map<String, JiraBoard> map = JIRA.getBoardMap(false);

        List<JiraBoard> sorted = new ArrayList<>();
        sorted.addAll(map.values());
        Collections.sort(sorted, new Comparator<JiraBoard>() {
            @Override
            public int compare(JiraBoard o1, JiraBoard o2) {
                return o1.getBoardName().compareTo(o2.getBoardName());
            }
        });

        for (JiraBoard jiraBoard : sorted) {

            if (prefix != null && prefix.length() > 0 && !jiraBoard.getBoardName().toLowerCase().contains(prefix.toLowerCase())) {
                continue;
            }

            boardsModel.addElement(combineDisplayName(jiraBoard.getBoardId(), jiraBoard.getBoardName()));
        }

//        for (int i=0;i<80;i++) {
//            boardsModel.addElement(i + "PMO-团队-大数据ETL_________" + i);
//        }

        scrollPaneBoard.setViewportView(jListBoards);
        scrollPaneBoard.updateUI();

        lblBoardCount.setText(STATUS_BOARD_COUNT.replace("{}", boardsModel.size() + ""));
        lblQueryStatus.setText(getHHMMSS() + "查询Boards成功");
    }

    private void resetResultSetAndText() {
        //清理子数据集
        boardsModel.clear();
        sprintsModel.clear();

        root.removeAllChildren();
        treeModel.reload();
        tree.setModel(null);

        lblBoardCount.setText(STATUS_BOARD_COUNT);
        lblQueryStatus.setText("");

        lblSelectedBoard.setText("");
        lblSelectedBoardSprint.setText("");
    }

    private void resetButton() {
        btnGenerate.setEnabled(false);
        btnExport.setEnabled(false);
        btnProductHtml.setEnabled(false);
        btnProductExcel.setEnabled(false);
    }

    private void renderSprints(String boardId) {
        resetButton();

        if (boardId == null || boardId.length() == 0){
            return;
        }

        //清理子数据集
        sprintsModel.clear();

        root.removeAllChildren();
        treeModel.reload();
        tree.setModel(null);

        Map<String, JiraSprint> map = JIRA.getSprintMap(boardId);
        for (JiraSprint jiraSprint : map.values()) {

//            if (prefix != null && prefix.length() > 0 && !jiraBoard.getBoardName().startsWith(prefix)) {
//                continue;
//            }

            sprintsModel.addElement(combineDisplayName(jiraSprint.getSprintId(), jiraSprint.getSprintName()));
        }
        //自定义render, 关闭的sprint放在尾端，并灰色显示
        jlistSprints.setCellRenderer(new JiraListCellRender(new ArrayList(map.values())));

        scrollPaneSprint.setViewportView(jlistSprints);
        scrollPaneSprint.updateUI();

        lblSelectedBoard.setText("看板:" + getNameByDisplayName((String)jListBoards.getSelectedValue()));
        lblSelectedBoardSprint.setText("");

        lblSprintCount.setText(STATUS_SPRINT_COUNT.replace("{}", sprintsModel.size() + ""));
        lblQueryStatus.setText(getHHMMSS() + "查询Sprints成功");

        btnExport.setEnabled(true);
        btnProductHtml.setEnabled(true);
        btnProductExcel.setEnabled(true);
    }

    private void renderIssueTree(String boardDisplayName, String sprintDisplayName) {
        String boardId      = getIdByDisplayName(boardDisplayName);
        String sprintId     = getIdByDisplayName(sprintDisplayName);

        if (boardId == null || boardId.length() == 0 || sprintId == null || sprintId.length() == 0){
            return;
        }

        root.removeAllChildren();
        treeModel.reload();

        tree.setModel(null);

        int issuesCnt = 0;

        Map<String, JiraIssue> issueMap = JIRA.getIssueMapWithSameOrder(boardId, sprintId);

        for (JiraIssue issue : issueMap.values()) {
            final DefaultMutableTreeNode storyOrTask = add(root, issue, true);

            issuesCnt++;
            Map<String, JiraIssue> subTaskMap = issue.getSubTaskMap();
            if (subTaskMap != null && subTaskMap.size() > 0) {
                for (JiraIssue subTask : subTaskMap.values()) {
                    add(storyOrTask, subTask, true);
                    issuesCnt++;
                }
            }
            root.add(storyOrTask);
        }

        tree = new JTree(treeModel);
        final CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
        tree.setCellRenderer(renderer);

        final CheckBoxNodeEditor editor = new CheckBoxNodeEditor(tree);
        tree.setCellEditor(editor);
        tree.setEditable(true);

        // listen for changes in the selection
//        tree.addTreeSelectionListener(new TreeSelectionListener() {
//
//            @Override
//            public void valueChanged(final TreeSelectionEvent e) {
//                lblIssueCount.setText(STATUS_ISSUE_COUNT.replace("{}", countCheckbox() + ""));
//            }
//        });

        // listen for changes in the model (including check box toggles)
        treeModel.addTreeModelListener(new TreeModelListener() {

            @Override
            public void treeNodesChanged(final TreeModelEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                CheckBoxNodeData data = (CheckBoxNodeData) node.getUserObject();
                refreshingTree(node, data.isChecked(), null);
            }

            @Override
            public void treeNodesInserted(final TreeModelEvent e) {
//                System.out.println(System.currentTimeMillis() + ": nodes inserted");
            }

            @Override
            public void treeNodesRemoved(final TreeModelEvent e) {
//                System.out.println(System.currentTimeMillis() + ": nodes removed");
            }

            @Override
            public void treeStructureChanged(final TreeModelEvent e) {
            }
        });


        scrollPaneCard.setViewportView(tree);
        scrollPaneCard.updateUI();
        expandTree(tree);

        String issueCountStr = STATUS_ISSUE_CHECKED_COUNT.replace("{}", issuesCnt + "");
        lblIssueCount.setText(issueCountStr);
        lblQueryStatus.setText(getHHMMSS() + "查询Issues成功");

        lblSelectedBoardSprint.setText(
                "看板:" + getNameByDisplayName((String)jListBoards.getSelectedValue())
                        + " => 迭代:"
                        + getNameByDisplayName((String)jlistSprints.getSelectedValue()));

        if (root.getChildCount() > 0) {
            btnGenerate.setEnabled(true);
//            btnExport.setEnabled(true);
        } else {
            btnGenerate.setEnabled(false);
//            btnExport.setEnabled(false);
        }

        btnExport.setEnabled(true);

    }

    private DefaultMutableTreeNode add(
            final DefaultMutableTreeNode parent, JiraIssue jiraIssue,
            final boolean checked) {
        final CheckBoxNodeData data = new CheckBoxNodeData(checked,
                JIRA.toIssueDisplayName(jiraIssue));

        final DefaultMutableTreeNode node = new DefaultMutableTreeNode(data);
        parent.add(node);
        return node;
    }

    /**
     * 展开一棵树
     * @param tree
     */
    private void expandTree(JTree tree) {
        // 根节点

        TreeNode node = (TreeNode) tree.getModel().getRoot();
        expandAll(tree, new TreePath(node), true);
    }

    /**
     * 完全展开一棵树或关闭一棵树
     * @param tree JTree
     * @param parent 父节点
     * @param expand true 表示展开，false 表示关闭
     */
    private void expandAll(JTree tree, TreePath parent, boolean expand) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();

        if (node.getChildCount() > 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    private void optAllMouseClicked(MouseEvent e) {
        TreeNode node = (TreeNode)tree.getModel().getRoot();
        //递归
        refreshingTree(node, true, null);
//        treeModel.reload(node);
//        tree.updateUI();
//        expandTree(tree);
    }

    private void optCancelMouseClicked(MouseEvent e) {
        TreeNode node = (TreeNode)tree.getModel().getRoot();
        //递归
        refreshingTree(node, false, null);
//        treeModel.reload(node);
//        tree.updateUI();
//        expandTree(tree);
    }

    private void chkStoryMouseClicked(MouseEvent e) {
        appendCheckToTreeByItems();
    }

    private void chkTaskMouseClicked(MouseEvent e) {
        appendCheckToTreeByItems();
    }

    private void chkSubtaskMouseClicked(MouseEvent e) {
        appendCheckToTreeByItems();
    }

    private void chkTodoMouseClicked(MouseEvent e) {
        appendCheckToTreeByItems();
    }

    private void chkDoingMouseClicked(MouseEvent e) {
        appendCheckToTreeByItems();
    }

    private void chkDoneMouseClicked(MouseEvent e) {
        appendCheckToTreeByItems();
    }

    //两个维度选择树节点
    private void appendCheckToTreeByItems() {
//        String text = checkBox.getText();
//        int currentClickChecked = checkBox.isSelected() ? 1 : 0;

        List<String> chosenCheckBox = new ArrayList<>();
        if (chkStory.isSelected()) chosenCheckBox.add(chkStory.getText());
        if (chkTask.isSelected()) chosenCheckBox.add(chkTask.getText());
        if (chkSubtask.isSelected()) chosenCheckBox.add(chkSubtask.getText());
        if (chkTodo.isSelected()) chosenCheckBox.add(chkTodo.getText());
        if (chkDoing.isSelected()) chosenCheckBox.add(chkDoing.getText());
        if (chkDone.isSelected()) chosenCheckBox.add(chkDone.getText());

        //所有checkbox中当前选中的唯一一个，先刷新树为None
//        if ((currentClickChecked == 1 && totalChosen == 1) || totalChosen == 0) {
//            refreshingTree(rootNode, false, null);
//        }

        TreeNode rootNode = (TreeNode) tree.getModel().getRoot();
        refreshingTree(rootNode, null, chosenCheckBox.toArray(new String[]{}));
    }

    //递归子节点，用可变的check变量
    private static int GLOBAL_REFRESHING = 0;
    private void refreshingTree(final TreeNode node, final Boolean check, final String... item) {
        if (GLOBAL_REFRESHING == 1) {
//            System.out.println("正在刷新节点中,不要操作太快....");
//            System.out.println("正在刷新节点中,不要操作太快....");
//            System.out.println("正在刷新节点中,不要操作太快....");
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                GLOBAL_REFRESHING = 1;
                try {
                    loopChildCheckboxByItem(node, check, item);
                } finally {
                    tree.updateUI();
                    lblIssueCount.setText(STATUS_ISSUE_CHECKED_COUNT.replace("{}", countCheckbox() + ""));
                    GLOBAL_REFRESHING = 0;
                }
            }
        });

    }

    /**
     *
     * @param node
     * @param check
     * @param items 需要显示的维度
     */
    private void loopChildCheckboxByItem(TreeNode node, Boolean check, String... items) {
        if (node.getChildCount() > 0) {
            for (Enumeration child = node.children(); child.hasMoreElements(); ) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) child.nextElement();

                CheckBoxNodeData data = (CheckBoxNodeData) childNode.getUserObject();

                if (check != null) {
                    data.setChecked(check);
                } else {
                    JiraIssue issue = JIRA.getObjectFromDisplayName(data.getItem());

                    List<String> itemList = Arrays.asList(items);

                    boolean isDimension1 = false;
                    boolean ischeck1 = false;
                    if (itemList.contains(DIMENSION_1_STORY)) {
                        ischeck1 |= issue.isStory() ? true : false;
                        isDimension1 = true;
                    }
                    if (itemList.contains(DIMENSION_1_TASK)) {
                        ischeck1 |= issue.isTask() ? true : false;
                        isDimension1 = true;
                    }
                    if (itemList.contains(DIMENSION_1_SUBTASK)) {
                        ischeck1 |= issue.isSubTask() ? true : false;
                        isDimension1 = true;
                    }

                    boolean isDimension2 = false;
                    boolean ischeck2 = false;
                    if (itemList.contains(DIMENSION_2_TODO)) {
                        ischeck2 |= issue.isTodo() ? true : false;
                        isDimension2 = true;
                    }
                    if (itemList.contains(DIMENSION_2_DOING)) {
                        ischeck2 |= issue.isDoing() ? true : false;
                        isDimension2 = true;
                    }
                    if (itemList.contains(DIMENSION_2_DONE)) {
                        ischeck2 |= issue.isDone() ? true : false;
                        isDimension2 = true;
                    }

                    //同一维度内是 或 关系， 不通维度内是 与 的关系
                    data.setChecked((isDimension1 ? ischeck1 : false) && (isDimension2 ? ischeck2 : false));
                }


                loopChildCheckboxByItem(childNode, check, items);

            }
        }
    }

    private int countCheckbox() {
        int cnt = 0;

        if (tree == null || tree.getModel() == null || tree.getModel().getRoot() == null) {
            return 0;
        }

        TreeNode rootNode = (TreeNode)tree.getModel().getRoot();
        if (rootNode.getChildCount() > 0) {
            for (Enumeration child = rootNode.children(); child.hasMoreElements();) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) child.nextElement();

                CheckBoxNodeData data = (CheckBoxNodeData)childNode.getUserObject();
                if (data.isChecked()) {
                    cnt++;
                }

                for (int i = 0; i < childNode.getChildCount(); i++) {
                    DefaultMutableTreeNode grandsonNode = (DefaultMutableTreeNode)childNode.getChildAt(i);
                    CheckBoxNodeData grandson = (CheckBoxNodeData)grandsonNode.getUserObject();

                    if (grandson.isChecked()) {
                        cnt++;
                    }
                }
            }
        }
        return cnt;
    }

    private void btnGenerateMouseClicked(MouseEvent e) {
        if (!btnGenerate.isEnabled()) {
            return;
        }
        btnGenerate.setEnabled(false);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                printHTML();
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e1) {
                }
                btnGenerate.setEnabled(true);
            }
        });
    }

    private void printHTML() {
        Set<JiraIssue> issueSet = getCheckedIssue();

        String boardName = getNameByDisplayName((String)jListBoards.getSelectedValue());
        String sprintName = getNameByDisplayName((String)jlistSprints.getSelectedValue());

        if (issueSet.size() == 0 || boardName == "" || sprintName == "") {
            JOptionPane.showMessageDialog(null, "请至少选中Board & Sprint内一个可打印的卡片!", "提示", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String htmlPath = HtmlGenerator.generate(boardName, sprintName, issueSet);

        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(htmlPath);
        clip.setContents(tText, null);

        int ret = JOptionPane.showOptionDialog
                (null, "HTML格式的Jira卡片生成成功, 路径已复制到粘贴板！\n\n【路径:" + htmlPath + "】\n\n是否直接打开？", "提示",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);

        if (JOptionPane.OK_OPTION == ret) {
            try {
//                Runtime.getRuntime().exec(new String[] { "open", htmlPath });
                Desktop.getDesktop().open(new File(htmlPath));
//                ProcessBuilder pb = new ProcessBuilder("open", htmlPath);
//                Process p = pb.start();
//                int exitCode = p.waitFor();
            } catch (Exception e) {
                System.out.println("Opening html file throws exception");
                e.printStackTrace();
            }
        }
    }

    private Set<JiraIssue> getCheckedIssue() {
        Set<JiraIssue> issueSet = new LinkedHashSet<>();

        TreeNode rootNode = (TreeNode)tree.getModel().getRoot();
        if (rootNode.getChildCount() > 0) {
            for (Enumeration child = rootNode.children(); child.hasMoreElements();) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) child.nextElement();

                CheckBoxNodeData data = (CheckBoxNodeData)childNode.getUserObject();
                if (data.isChecked()) {
                    issueSet.add(JIRA.getObjectFromDisplayName(data.getItem()));
                }

                for (int i = 0; i < childNode.getChildCount(); i++) {
                    DefaultMutableTreeNode grandsonNode = (DefaultMutableTreeNode)childNode.getChildAt(i);
                    CheckBoxNodeData grandson = (CheckBoxNodeData)grandsonNode.getUserObject();

                    if (grandson.isChecked()) {
                        issueSet.add(JIRA.getObjectFromDisplayName(grandson.getItem()));
                    }
                }
            }
        }

        return issueSet;
    }

    private void btnExportMouseClicked(MouseEvent e) {
        if (!btnExport.isEnabled()) {
            return;
        }
        btnExport.setEnabled(false);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    export();
                    Thread.sleep(100L);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                btnExport.setEnabled(true);
            }
        });
    }

    private void export() {
        String boardId      = getIdByDisplayName((String)jListBoards.getSelectedValue());
        String sprintId     = getIdByDisplayName((String)jlistSprints.getSelectedValue());

        String excelPath = ExcelProcessor4Sprint.write(boardId, sprintId);

        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(excelPath);
        clip.setContents(tText, null);

        int ret = JOptionPane.showOptionDialog
                (null, "Excel导出成功, 路径已复制到粘贴板！\n\n【路径:" + excelPath + "】\n\n是否直接打开？", "提示",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);

        if (JOptionPane.OK_OPTION == ret) {
            try {
//                Runtime.getRuntime().exec(new String[] { "open", excelPath });
                Desktop.getDesktop().open(new File(excelPath));
//                ProcessBuilder pb = new ProcessBuilder("open", excelPath);
//                Process p = pb.start();
//                int exitCode = p.waitFor();
            } catch (Exception ex) {
                System.out.println("Opening excel file throws exception");
                ex.printStackTrace();
            }
        }
    }

    private void lblUpdateMouseClicked(MouseEvent e) {
        lblUpdate.setEnabled(false);
//        jiraFrame.setVisible(false);

        boolean hasUpdates = JiraAutoUpdater.checkUpdates();

        if (hasUpdates) {
            int ret = JOptionPane.showOptionDialog
                    (jiraFrame, "存在新版本【TAG:" + JiraAutoUpdater.LATEST_TAG_NAME + "】" +
                                    "\n\n 更新说明：" +
                                    "\n-------------------------------\n" +
                                    JiraAutoUpdater.LATEST_DESC +
                                    "\n-------------------------------\n" +
                                    "\n 是否更新？", "提示",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);

            if (JOptionPane.OK_OPTION == ret) {
                jiraFrame.setVisible(false);
                new InstallAssistFrame().show(jiraFrame);
                jiraFrame.dispose();
            }
        } else {
            JOptionPane.showMessageDialog
                    (jiraFrame, "该版本已经最新, 无需更新!");
        }

//        jiraFrame.setVisible(true);
        lblUpdate.setEnabled(true);
    }

    /**
     * --------------------------------------------------------
     * 快捷键彩蛋
     * --------------------------------------------------------
     */
    private boolean EASTER_EGG_SHOW = false;
    private int RANDOM_EGG_INDEX = 1;
    private void showEasterEggsDlg(String title, String body) {
        EASTER_EGG_SHOW = true;
        int ret = JOptionPane.showOptionDialog
                (jiraFrame, body, title, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
        if (JOptionPane.OK_OPTION == ret) {
            EASTER_EGG_SHOW = false;
        }
    }

    // 注册应用程序全局键盘事件, 所有的键盘事件都会被此事件监听器处理.
    private void easterEggs() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.addAWTEventListener(
                new java.awt.event.AWTEventListener() {
                    public void eventDispatched(AWTEvent event) {
                        if (event.getClass() == KeyEvent.class) {
                            KeyEvent kE = ((KeyEvent) event);
                            // https://bugs.openjdk.java.net/browse/JDK-8079801
                            // Window.java preProcessKeyEvent() 在提示框关闭的时候，会自动sysout打印所有的子窗体...而且无法关闭
                            // 只针对F1发生的情况，所以彩蛋不能使用F1
                            // 处理按键事件 Ctrl+Shift+FX
                            if ( (kE.getKeyCode() >= KeyEvent.VK_F2 && kE.getKeyCode() <= KeyEvent.VK_F12)
                                    && !EASTER_EGG_SHOW
                                    && (((InputEvent) event).isControlDown())
                                    && (((InputEvent) event).isShiftDown())
                                    && kE.getID() == KeyEvent.KEY_PRESSED) {

                                showEasterEggsDlg("天使彩蛋", getGithubDoc("csf" + ((RANDOM_EGG_INDEX++) % 6) + ".md"));

                            } else if ( kE.getKeyCode() == KeyEvent.VK_7
                                    && !EASTER_EGG_SHOW
                                    && (((InputEvent) event).isControlDown())
                                    && (((InputEvent) event).isShiftDown())
                                    && (((InputEvent) event).isAltDown())
                                    && kE.getID() == KeyEvent.KEY_PRESSED) {
                                /* Ctrl+Shift+Alt+7 */
                                showEasterEggsDlg("77",getGithubDoc("77.md"));
                            }
                        }
                    }
                }, java.awt.AWTEvent.KEY_EVENT_MASK);

    }
    private String getGithubDoc(String fileName) {

//        String response = JIRA.getResponse("https://api.github.com/repos/NicholasQu/JiraScrumCardsPrinter/contents/README.md", null,null, 5);
        String response = JIRA.getResponse("https://api.github.com/repos/NicholasQu/JiraScrumCardsPrinter/contents/raw/eastereggs/" + fileName, null, null, 5);

        String eggStr = "这是一颗彩蛋提示语!";
        boolean isReal = false; //是否下載了真正的文件
        try {
            String base64Content = JSON.parseObject(response).getString("content");
            base64Content = base64Content.replaceAll("\\n", "");

            if (StringUtils.isNotEmpty(base64Content)) {
                eggStr = new String(org.apache.commons.codec.binary.Base64.decodeBase64(base64Content));
            }
            isReal = true;
        } catch (Exception e) {
        }

        //github有接口調用次數限制，下載不了就用jar包本地的。
        if (!isReal) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/md/" + fileName)));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = in.readLine()) != null) {
                    buffer.append(line);
                }
                eggStr = buffer.toString();
            } catch (IOException e) {
            }
        }
        return eggStr;
    }

    private void btnProductHtmlMouseClicked(MouseEvent e) {
        if (!btnProductHtml.isEnabled()) {
            return;
        }

        String boardId = getIdByDisplayName((String)jListBoards.getSelectedValue());
        if ( StringUtils.isBlank(boardId) ) {
            int ret = JOptionPane.showOptionDialog
                    (jiraFrame, "请选中产品池看板，再点击导出产品视图！", "提示",
                            JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
            return;
        }

        btnProductHtml.setEnabled(false);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    exportProductView(boardId, "html");
                    Thread.sleep(100L);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                btnProductHtml.setEnabled(true);
            }
        });
    }

    //生成产品视图
    private void exportProductView(String boardId, String fileType) {

        //更新为导出顺序与jira看板中的顺序一致
        Map<String, Map<String, JiraIssue>> productLines = JiraEpic.viewProductLine(boardId);
//        String filePath = ExcelProcessor4ProductLine.write(productLines);

        String filePath = "";
        int ret = 0;
        if ("html".equals(fileType)) {
            filePath = VPLViaHtml.generate(productLines);
        } else {
            filePath = VPLViaExcel.generate(productLines);
        }

        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(filePath);
        clip.setContents(tText, null);

        String desc = "";
        if ("html".equals(fileType)) {
            desc = "HTML远程发布" + (filePath.startsWith("http") ? "成功" : "失败");
            ret = JOptionPane.showOptionDialog
                    (null, desc + ", 路径已复制到粘贴板！\n\n【路径:" + filePath + "】\n\n是否直接打开？", "提示",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
        } else {
            ret = JOptionPane.showOptionDialog
                    (null, "产品视图Excel生成, 路径已复制到粘贴板！\n\n【路径:" + filePath + "】\n\n是否直接打开？", "提示",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
        }

        if (JOptionPane.OK_OPTION == ret) {
            try {
//                Runtime.getRuntime().exec(new String[] { "open", filePath });
                if (filePath.startsWith("http")) {
                    Desktop.getDesktop().browse(new URI(filePath));
                } else {
                    Desktop.getDesktop().open(new File(filePath));
                }
//                ProcessBuilder pb = new ProcessBuilder("open", filePath);
//                Process p = pb.start();
//                int exitCode = p.waitFor();
            } catch (Exception ex) {
                System.out.println("Opening excel file throws exception");
                ex.printStackTrace();
            }
        }
    }

    private void btnProductExcelMouseClicked(MouseEvent e) {
        if (!btnProductExcel.isEnabled()) {
            return;
        }

        String boardId = getIdByDisplayName((String)jListBoards.getSelectedValue());
        if ( StringUtils.isBlank(boardId) ) {
            int ret = JOptionPane.showOptionDialog
                    (jiraFrame, "请选中产品池看板，再点击导出产品视图！", "提示",
                            JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
            return;
        }

        btnProductExcel.setEnabled(false);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    exportProductView(boardId, "excel");
                    Thread.sleep(100L);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                btnProductExcel.setEnabled(true);
            }
        });
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        jiraFrame = new JFrame();
        dialogPane = new JPanel();
        serverPanel = new JPanel();
        label1 = new JLabel();
        txtJiraUrl = new JTextField();
        label2 = new JLabel();
        txtUser = new JTextField();
        label3 = new JLabel();
        txtPassword = new JPasswordField();
        btnConnect = new JButton();
        btnLogout = new JButton();
        statusPanel = new JPanel();
        jiraconnection = new JLabel();
        lblStatus = new JLabel();
        lblStatus2 = new JLabel();
        lblQueryStatus = new JLabel();
        lblBoardCount = new JLabel();
        lblSprintCount = new JLabel();
        lblIssueCount = new JLabel();
        lblUpdate = new JLabel();
        boardPanel = new JPanel();
        lblTitleBoards = new JLabel();
        label5 = new JLabel();
        label6 = new JLabel();
        boardFilterPanel = new JScrollPane();
        txtFilterBoard = new JTextField();
        scrollPane2 = new JScrollPane();
        lblSelectedBoard = new JLabel();
        scrollPane3 = new JScrollPane();
        lblSelectedBoardSprint = new JLabel();
        scrollPaneBoard = new JScrollPane();
        scrollPaneSprint = new JScrollPane();
        scrollPaneCard = new JScrollPane();
        panel1 = new JPanel();
        btnProductExcel = new JButton();
        btnProductHtml = new JButton();
        checkTreePanel = new JPanel();
        label9 = new JLabel();
        optAll = new JRadioButton();
        optCancel = new JRadioButton();
        btnGenerate = new JButton();
        label7 = new JLabel();
        chkStory = new JCheckBox();
        chkTask = new JCheckBox();
        chkSubtask = new JCheckBox();
        btnExport = new JButton();
        label8 = new JLabel();
        chkTodo = new JCheckBox();
        chkDoing = new JCheckBox();
        chkDone = new JCheckBox();

        //======== jiraFrame ========
        {
            jiraFrame.setForeground(SystemColor.textHighlight);
            jiraFrame.setBackground(new Color(204, 204, 255));
            jiraFrame.setTitle("Jira\u770b\u677f\u6253\u5370 - v2.0 By Nicholas");
            jiraFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            jiraFrame.setFont(new Font("Lucida Grande", Font.BOLD, 13));
            jiraFrame.setMinimumSize(new Dimension(800, 700));
            jiraFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowActivated(WindowEvent e) {
                    jiraFrameWindowActivated(e);
                }
                @Override
                public void windowClosing(WindowEvent e) {
                    jiraFrameWindowClosing(e);
                }
            });
            Container jiraFrameContentPane = jiraFrame.getContentPane();
            jiraFrameContentPane.setLayout(new GridBagLayout());
            ((GridBagLayout)jiraFrameContentPane.getLayout()).columnWidths = new int[] {0, 0};
            ((GridBagLayout)jiraFrameContentPane.getLayout()).rowHeights = new int[] {0, 0};
            ((GridBagLayout)jiraFrameContentPane.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
            ((GridBagLayout)jiraFrameContentPane.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

            //======== dialogPane ========
            {
                dialogPane.setMinimumSize(new Dimension(812, 90));
                dialogPane.setPreferredSize(new Dimension(655, 90));
                dialogPane.setLayout(new GridBagLayout());
                ((GridBagLayout)dialogPane.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)dialogPane.getLayout()).rowHeights = new int[] {77, 544, 25, 0};
                ((GridBagLayout)dialogPane.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                ((GridBagLayout)dialogPane.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

                //======== serverPanel ========
                {
                    serverPanel.setBackground(new Color(204, 204, 204));
                    serverPanel.setPreferredSize(new Dimension(655, 80));
                    serverPanel.setMinimumSize(new Dimension(812, 80));
                    serverPanel.setBorder(new TitledBorder(null, "Jira", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION,
                        new Font("Arial Black", Font.PLAIN, 16), Color.red));
                    serverPanel.setMaximumSize(new Dimension(900, 80));
                    serverPanel.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
                    serverPanel.setLayout(new GridBagLayout());
                    ((GridBagLayout)serverPanel.getLayout()).columnWidths = new int[] {15, 77, 209, 71, 127, 71, 127, 86, 64, 0};
                    ((GridBagLayout)serverPanel.getLayout()).rowHeights = new int[] {27, 0};
                    ((GridBagLayout)serverPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
                    ((GridBagLayout)serverPanel.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

                    //---- label1 ----
                    label1.setText("Jira\u5730\u5740");
                    label1.setHorizontalTextPosition(SwingConstants.CENTER);
                    label1.setFont(new Font("Lucida Grande", Font.BOLD, 13));
                    serverPanel.add(label1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 7), 0, 0));

                    //---- txtJiraUrl ----
                    txtJiraUrl.setColumns(2);
                    txtJiraUrl.setMinimumSize(new Dimension(50, 26));
                    serverPanel.add(txtJiraUrl, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 7), 0, 0));

                    //---- label2 ----
                    label2.setText("\u7528\u6237\u540d");
                    label2.setFont(new Font("Lucida Grande", Font.BOLD, 13));
                    serverPanel.add(label2, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 7), 0, 0));
                    serverPanel.add(txtUser, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 7), 0, 0));

                    //---- label3 ----
                    label3.setText("\u5bc6\u7801");
                    label3.setFont(new Font("Lucida Grande", Font.BOLD, 13));
                    serverPanel.add(label3, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 7), 0, 0));

                    //---- txtPassword ----
                    txtPassword.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(KeyEvent e) {
                            txtPasswordKeyPressed(e);
                        }
                    });
                    serverPanel.add(txtPassword, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 7), 0, 0));

                    //---- btnConnect ----
                    btnConnect.setText("\u767b\u5f55");
                    btnConnect.setBackground(Color.lightGray);
                    btnConnect.setOpaque(true);
                    btnConnect.setFont(new Font("Lucida Grande", Font.BOLD, 13));
                    btnConnect.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            btnConnectMouseClicked(e);
                        }
                    });
                    serverPanel.add(btnConnect, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 7), 0, 0));

                    //---- btnLogout ----
                    btnLogout.setText("\u65ad\u5f00");
                    btnLogout.setBackground(Color.lightGray);
                    btnLogout.setOpaque(true);
                    btnLogout.setFont(new Font("Lucida Grande", Font.BOLD, 13));
                    btnLogout.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            btnLogoutMouseClicked(e);
                        }
                    });
                    serverPanel.add(btnLogout, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
                }
                dialogPane.add(serverPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));

                //======== statusPanel ========
                {
                    statusPanel.setBackground(Color.lightGray);
                    statusPanel.setPreferredSize(new Dimension(346, 30));
                    statusPanel.setBorder(LineBorder.createBlackLineBorder());
                    statusPanel.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
                    statusPanel.setLayout(new GridBagLayout());
                    ((GridBagLayout)statusPanel.getLayout()).columnWidths = new int[] {90, 76, 78, 157, 107, 106, 97, 70, 42, 0};
                    ((GridBagLayout)statusPanel.getLayout()).rowHeights = new int[] {30, 0};
                    ((GridBagLayout)statusPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
                    ((GridBagLayout)statusPanel.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

                    //---- jiraconnection ----
                    jiraconnection.setText("Jira\u8fde\u63a5\u72b6\u6001:");
                    jiraconnection.setHorizontalAlignment(SwingConstants.RIGHT);
                    jiraconnection.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
                    jiraconnection.setBackground(Color.white);
                    statusPanel.add(jiraconnection, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- lblStatus ----
                    lblStatus.setText("\u672a\u8fde\u63a5");
                    lblStatus.setHorizontalAlignment(SwingConstants.LEFT);
                    lblStatus.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
                    lblStatus.setBackground(Color.white);
                    statusPanel.add(lblStatus, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- lblStatus2 ----
                    lblStatus2.setText("\u67e5\u8be2\u72b6\u6001:");
                    lblStatus2.setHorizontalAlignment(SwingConstants.RIGHT);
                    lblStatus2.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
                    lblStatus2.setBackground(Color.white);
                    statusPanel.add(lblStatus2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- lblQueryStatus ----
                    lblQueryStatus.setHorizontalAlignment(SwingConstants.LEFT);
                    lblQueryStatus.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
                    lblQueryStatus.setBackground(Color.white);
                    statusPanel.add(lblQueryStatus, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- lblBoardCount ----
                    lblBoardCount.setText("Boards:[]\u4e2a");
                    lblBoardCount.setHorizontalAlignment(SwingConstants.CENTER);
                    lblBoardCount.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
                    statusPanel.add(lblBoardCount, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- lblSprintCount ----
                    lblSprintCount.setText("Sprints:[]\u4e2a");
                    lblSprintCount.setHorizontalAlignment(SwingConstants.CENTER);
                    lblSprintCount.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
                    statusPanel.add(lblSprintCount, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- lblIssueCount ----
                    lblIssueCount.setText("\u9009\u4e2dIssues:[]\u4e2a");
                    lblIssueCount.setHorizontalAlignment(SwingConstants.CENTER);
                    lblIssueCount.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
                    statusPanel.add(lblIssueCount, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- lblUpdate ----
                    lblUpdate.setText("=\u68c0\u67e5\u66f4\u65b0=");
                    lblUpdate.setHorizontalAlignment(SwingConstants.CENTER);
                    lblUpdate.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
                    lblUpdate.setBorder(LineBorder.createBlackLineBorder());
                    lblUpdate.setPreferredSize(new Dimension(74, 25));
                    lblUpdate.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            lblUpdateMouseClicked(e);
                        }
                    });
                    statusPanel.add(lblUpdate, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                        new Insets(0, 0, 0, 0), 0, 0));
                }
                dialogPane.add(statusPanel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));

                //======== boardPanel ========
                {
                    boardPanel.setBackground(new Color(204, 204, 204));
                    boardPanel.setBorder(LineBorder.createBlackLineBorder());
                    boardPanel.setLayout(new GridBagLayout());
                    ((GridBagLayout)boardPanel.getLayout()).columnWidths = new int[] {10, 225, 225, 439, 0};
                    ((GridBagLayout)boardPanel.getLayout()).rowHeights = new int[] {53, 41, 391, 144, 0};
                    ((GridBagLayout)boardPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
                    ((GridBagLayout)boardPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

                    //---- lblTitleBoards ----
                    lblTitleBoards.setText("Boards:");
                    lblTitleBoards.setHorizontalAlignment(SwingConstants.CENTER);
                    lblTitleBoards.setFont(new Font("Tahoma", Font.BOLD, 13));
                    lblTitleBoards.setIcon(new ImageIcon(getClass().getResource("/images/jira/board.png")));
                    boardPanel.add(lblTitleBoards, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //---- label5 ----
                    label5.setText("Active Sprints");
                    label5.setFont(new Font("Tahoma", Font.BOLD, 13));
                    label5.setIcon(new ImageIcon(getClass().getResource("/images/jira/sprint.png")));
                    label5.setBackground(new Color(204, 204, 255));
                    boardPanel.add(label5, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //---- label6 ----
                    label6.setText("Story / Task / Subtask");
                    label6.setFont(new Font("Tahoma", Font.BOLD, 13));
                    label6.setIcon(new ImageIcon(getClass().getResource("/images/jira/card.png")));
                    boardPanel.add(label6, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
                        new Insets(0, 0, 5, 0), 0, 0));

                    //======== boardFilterPanel ========
                    {
                        boardFilterPanel.setBorder(LineBorder.createBlackLineBorder());

                        //---- txtFilterBoard ----
                        txtFilterBoard.setToolTipText("\u6a21\u7cca\u67e5\u8be2\u56de\u8f66\u8fc7\u6ee4");
                        txtFilterBoard.setBorder(LineBorder.createBlackLineBorder());
                        txtFilterBoard.setText("\u6a21\u7cca\u67e5\u8be2\u56de\u8f66\u8fc7\u6ee4");
                        txtFilterBoard.setForeground(Color.blue);
                        txtFilterBoard.setPreferredSize(new Dimension(90, 25));
                        txtFilterBoard.setEnabled(false);
                        txtFilterBoard.setBackground(new Color(214, 217, 223));
                        txtFilterBoard.setOpaque(true);
                        txtFilterBoard.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
                        txtFilterBoard.addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyReleased(KeyEvent e) {
                                txtFilterBoardKeyReleased(e);
                            }
                        });
                        txtFilterBoard.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusGained(FocusEvent e) {
                                txtFilterBoardFocusGained(e);
                            }
                            @Override
                            public void focusLost(FocusEvent e) {
                                txtFilterBoardFocusLost(e);
                            }
                        });
                        boardFilterPanel.setViewportView(txtFilterBoard);
                    }
                    boardPanel.add(boardFilterPanel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //======== scrollPane2 ========
                    {
                        scrollPane2.setBorder(LineBorder.createBlackLineBorder());
                        scrollPane2.setBackground(Color.white);

                        //---- lblSelectedBoard ----
                        lblSelectedBoard.setFont(new Font("Lucida Grande", Font.BOLD, 12));
                        lblSelectedBoard.setBorder(LineBorder.createBlackLineBorder());
                        lblSelectedBoard.setBackground(new Color(214, 217, 223));
                        lblSelectedBoard.setOpaque(true);
                        scrollPane2.setViewportView(lblSelectedBoard);
                    }
                    boardPanel.add(scrollPane2, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //======== scrollPane3 ========
                    {
                        scrollPane3.setBorder(LineBorder.createBlackLineBorder());

                        //---- lblSelectedBoardSprint ----
                        lblSelectedBoardSprint.setFont(new Font("Lucida Grande", Font.BOLD, 12));
                        lblSelectedBoardSprint.setBorder(LineBorder.createBlackLineBorder());
                        lblSelectedBoardSprint.setOpaque(true);
                        lblSelectedBoardSprint.setBackground(new Color(214, 217, 223));
                        lblSelectedBoardSprint.setHorizontalAlignment(SwingConstants.CENTER);
                        scrollPane3.setViewportView(lblSelectedBoardSprint);
                    }
                    boardPanel.add(scrollPane3, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));
                    boardPanel.add(scrollPaneBoard, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));
                    boardPanel.add(scrollPaneSprint, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));
                    boardPanel.add(scrollPaneCard, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                    //======== panel1 ========
                    {
                        panel1.setBackground(new Color(204, 204, 204));
                        panel1.setLayout(new GridBagLayout());
                        ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {32, 137, 0, 0};
                        ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {43, 43, 37, 0, 0};
                        ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
                        ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

                        //---- btnProductExcel ----
                        btnProductExcel.setText("\u4ea7\u54c1\u89c6\u56feExcel");
                        btnProductExcel.setFont(new Font("Lucida Grande", Font.BOLD, 12));
                        btnProductExcel.setEnabled(false);
                        btnProductExcel.setIcon(new ImageIcon(getClass().getResource("/images/active.png")));
                        btnProductExcel.setMinimumSize(new Dimension(119, 25));
                        btnProductExcel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                btnProductExcelMouseClicked(e);
                            }
                        });
                        panel1.add(btnProductExcel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- btnProductHtml ----
                        btnProductHtml.setText("\u53d1\u5e03\u4ea7\u54c1\u89c6\u56fe");
                        btnProductHtml.setFont(new Font("Lucida Grande", Font.BOLD, 12));
                        btnProductHtml.setEnabled(false);
                        btnProductHtml.setIcon(new ImageIcon(getClass().getResource("/images/active.png")));
                        btnProductHtml.setMinimumSize(new Dimension(119, 25));
                        btnProductHtml.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                btnProductHtmlMouseClicked(e);
                            }
                        });
                        panel1.add(btnProductHtml, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));
                    }
                    boardPanel.add(panel1, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //======== checkTreePanel ========
                    {
                        checkTreePanel.setBackground(new Color(204, 204, 204));
                        checkTreePanel.setLayout(new GridBagLayout());
                        ((GridBagLayout)checkTreePanel.getLayout()).columnWidths = new int[] {21, 82, 77, 73, 118, 140, 14, 0};
                        ((GridBagLayout)checkTreePanel.getLayout()).rowHeights = new int[] {43, 43, 37, 25, 0};
                        ((GridBagLayout)checkTreePanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
                        ((GridBagLayout)checkTreePanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

                        //---- label9 ----
                        label9.setText("\u7ef4\u5ea6\u4e00\uff1a");
                        label9.setHorizontalAlignment(SwingConstants.TRAILING);
                        checkTreePanel.add(label9, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- optAll ----
                        optAll.setText("All");
                        optAll.setFont(new Font("Tahoma", Font.BOLD, 12));
                        optAll.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                optAllMouseClicked(e);
                            }
                        });
                        checkTreePanel.add(optAll, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- optCancel ----
                        optCancel.setText("None");
                        optCancel.setFont(new Font("Tahoma", Font.BOLD, 12));
                        optCancel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                optCancelMouseClicked(e);
                            }
                        });
                        checkTreePanel.add(optCancel, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- btnGenerate ----
                        btnGenerate.setText("\u6253\u5370Card");
                        btnGenerate.setIcon(new ImageIcon(getClass().getResource("/images/printer.png")));
                        btnGenerate.setPreferredSize(new Dimension(121, 10));
                        btnGenerate.setMinimumSize(new Dimension(121, 25));
                        btnGenerate.setMaximumSize(new Dimension(121, 30));
                        btnGenerate.setBackground(SystemColor.textHighlight);
                        btnGenerate.setFont(new Font("Lucida Grande", Font.BOLD, 12));
                        btnGenerate.setEnabled(false);
                        btnGenerate.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                btnGenerateMouseClicked(e);
                            }
                        });
                        checkTreePanel.add(btnGenerate, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- label7 ----
                        label7.setText("\u7ef4\u5ea6\u4e8c\uff1a");
                        label7.setHorizontalAlignment(SwingConstants.TRAILING);
                        checkTreePanel.add(label7, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- chkStory ----
                        chkStory.setText("Story");
                        chkStory.setSelected(true);
                        chkStory.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                chkStoryMouseClicked(e);
                            }
                        });
                        checkTreePanel.add(chkStory, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- chkTask ----
                        chkTask.setText("Task");
                        chkTask.setSelected(true);
                        chkTask.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                chkTaskMouseClicked(e);
                            }
                        });
                        checkTreePanel.add(chkTask, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- chkSubtask ----
                        chkSubtask.setText("Subtask");
                        chkSubtask.setSelected(true);
                        chkSubtask.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                chkSubtaskMouseClicked(e);
                            }
                        });
                        checkTreePanel.add(chkSubtask, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- btnExport ----
                        btnExport.setText("\u5bfc\u51faExcel");
                        btnExport.setFont(new Font("Lucida Grande", Font.BOLD, 12));
                        btnExport.setEnabled(false);
                        btnExport.setIcon(new ImageIcon(getClass().getResource("/images/database.png")));
                        btnExport.setMinimumSize(new Dimension(119, 25));
                        btnExport.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                btnExportMouseClicked(e);
                            }
                        });
                        checkTreePanel.add(btnExport, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- label8 ----
                        label8.setText("\u7ef4\u5ea6\u4e09:  ");
                        label8.setHorizontalAlignment(SwingConstants.TRAILING);
                        label8.setBackground(new Color(204, 204, 204));
                        checkTreePanel.add(label8, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- chkTodo ----
                        chkTodo.setText("Todo");
                        chkTodo.setSelected(true);
                        chkTodo.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                chkTodoMouseClicked(e);
                            }
                        });
                        checkTreePanel.add(chkTodo, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- chkDoing ----
                        chkDoing.setText("Doing");
                        chkDoing.setSelected(true);
                        chkDoing.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                chkDoingMouseClicked(e);
                            }
                        });
                        checkTreePanel.add(chkDoing, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));

                        //---- chkDone ----
                        chkDone.setText("Done");
                        chkDone.setSelected(true);
                        chkDone.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                chkDoneMouseClicked(e);
                            }
                        });
                        checkTreePanel.add(chkDone, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(0, 0, 5, 5), 0, 0));
                    }
                    boardPanel.add(checkTreePanel, new GridBagConstraints(2, 3, 2, 1, 0.0, 0.0,
                        GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                        new Insets(0, 0, 0, 0), 0, 0));
                }
                dialogPane.add(boardPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            jiraFrameContentPane.add(dialogPane, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
            jiraFrame.pack();
            jiraFrame.setLocationRelativeTo(null);
        }

        //---- buttonGroup1 ----
        ButtonGroup buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(optAll);
        buttonGroup1.add(optCancel);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents



        // 888888888888888888888888888
        jListBoards.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
        jListBoards.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jListBoards.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1 && jListBoards.getSelectedIndex() > -1) {
                    asyncQuerySprints();
                }
            }
        });
//        jListBoards.addListSelectionListener(new ListSelectionListener() {
//            @Override
//            public void valueChanged(ListSelectionEvent e) {
//                if (jListBoards.getSelectedIndex() > -1 && !e.getValueIsAdjusting()) {
//                    asyncQuerySprints();
//                }
//            }
//        });

        jlistSprints.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
        jlistSprints.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jlistSprints.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1 && jlistSprints.getSelectedIndex() > -1) {
                    asyncQueryTree();
                }
            }

        });

    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JFrame jiraFrame;
    private JPanel dialogPane;
    private JPanel serverPanel;
    private JLabel label1;
    private JTextField txtJiraUrl;
    private JLabel label2;
    private JTextField txtUser;
    private JLabel label3;
    private JPasswordField txtPassword;
    private JButton btnConnect;
    private JButton btnLogout;
    private JPanel statusPanel;
    private JLabel jiraconnection;
    private JLabel lblStatus;
    private JLabel lblStatus2;
    private JLabel lblQueryStatus;
    private JLabel lblBoardCount;
    private JLabel lblSprintCount;
    private JLabel lblIssueCount;
    private JLabel lblUpdate;
    private JPanel boardPanel;
    private JLabel lblTitleBoards;
    private JLabel label5;
    private JLabel label6;
    private JScrollPane boardFilterPanel;
    private JTextField txtFilterBoard;
    private JScrollPane scrollPane2;
    private JLabel lblSelectedBoard;
    private JScrollPane scrollPane3;
    private JLabel lblSelectedBoardSprint;
    private JScrollPane scrollPaneBoard;
    private JScrollPane scrollPaneSprint;
    private JScrollPane scrollPaneCard;
    private JPanel panel1;
    private JButton btnProductExcel;
    private JButton btnProductHtml;
    private JPanel checkTreePanel;
    private JLabel label9;
    private JRadioButton optAll;
    private JRadioButton optCancel;
    private JButton btnGenerate;
    private JLabel label7;
    private JCheckBox chkStory;
    private JCheckBox chkTask;
    private JCheckBox chkSubtask;
    private JButton btnExport;
    private JLabel label8;
    private JCheckBox chkTodo;
    private JCheckBox chkDoing;
    private JCheckBox chkDone;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
