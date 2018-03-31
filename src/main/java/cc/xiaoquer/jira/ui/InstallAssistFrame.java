/*
 * Created by JFormDesigner on Wed Mar 28 10:57:48 CST 2018
 */

package cc.xiaoquer.jira.ui;

import cc.xiaoquer.jira.autoupdater.JiraAutoUpdater;
import cc.xiaoquer.jira.constant.FoldersConsts;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author Khainx
 */
public class InstallAssistFrame {
    private boolean everSucceed = false; //成功下载过一次，不在发起下载。

//    private Task task;
//    class Task extends SwingWorker<Void, Void> {
//        /*
//         * Main task. Executed in background thread.
//         */
//        @Override
//        public Void doInBackground() {
//            Random random = new Random();
//            int progress = 0;
//            //Initialize progress property.
//            setProgress(0);
//
//            while (progress < 100) {
//                int size = (int) jscpJarFile.length();
//                progress = (int)(size / 9633289.0 * 100);
//                System.out.println("Progress已下载..." + size);
//                System.out.println("Progress已下载%.." + progress);
//
//                //Sleep for up to one second.
//                try {
//                    Thread.sleep(500 + random.nextInt(500));
//                } catch (InterruptedException ignore) {}
//                //Make random progress.
//                setProgress(Math.min(progress, 100));
//            }
//            return null;
//        }
//
//        /*
//         * Executed in event dispatching thread
//         */
//        @Override
//        public void done() {
//            Toolkit.getDefaultToolkit().beep();
//            setCursor(null); //turn off the wait cursor
//        }
//    }
//
//    /**
//     * Invoked when the user presses the start button.
//     */
//    public void actionPerformed(ActionEvent evt) {
//        try {
//            FileUtils.forceDelete(jscpJarFile);
//        } catch (IOException e1) {
//        }
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    System.out.println("开始下载...");
//                    FileUtils.copyURLToFile(new URL(downloadUrl), jscpJarFile);
//                    System.out.println("下载完毕");
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
//            }
//        }).start();
//
//        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//        //Instances of javax.swing.SwingWorker are not reusuable, so
//        //we create new instances as needed.
//        task = new Task();
//        task.addPropertyChangeListener(this);
//        task.execute();
//    }
//
//    /**
//     * Invoked when task's progress property changes.
//     */
//    public void propertyChange(PropertyChangeEvent evt) {
//        if ("progress" == evt.getPropertyName()) {
//            int progress = (Integer) evt.getNewValue();
//            progressBar.setValue(progress);
//        }
//    }

    public InstallAssistFrame() {
        initComponents();
    }

    private void downFrameWindowActivated(WindowEvent e) {
        // Can't download here!!!!!!!!!!!!!!!!!
        // Can't download here!!!!!!!!!!!!!!!!!
        // Can't download here!!!!!!!!!!!!!!!!!
        // Can't download here!!!!!!!!!!!!!!!!!
    }

    private void assistFrameWindowOpened(WindowEvent e) {
        doDownload();
    }

    private int last_size = 0;
    private void doDownload() {
        //保证同一时刻只能开启一次下载，否则窗体激活一次就开启一次下载。
        try {
            if (!everSucceed) {
                last_size = 0;
                JiraAutoUpdater.asyncDownloadJar(this);
            }
        } catch (Exception e1) {
            System.out.println("Frame DownloadJar Failed " + e1.getMessage());
            e1.printStackTrace();
            JiraAutoUpdater.stop();
        }
    }

    public void renderUI(int size, int progress) {
        if (size == last_size) {
            int threshold = JiraAutoUpdater.GIT_NOT_CONNECTED_COUNT.incrementAndGet();
            //轮训10次后下载的文件大小保持不变，说明没响应
            if (threshold > 10) {
                everSucceed = false;
                lblStatus.setText("服务器没有响应或者网络异常，下载中断！");
                btnRestart.setText("重试下载");
                btnRestart.setEnabled(true);
                JiraAutoUpdater.stop();
                return;
            }
        }

        last_size = size;

        boolean isDone = (progress > 100);

        progressBar.setValue(Math.min(progress, 100));
        lblStatus.setText(isDone ? "新版本下载完毕." : "下载中......");
        txtUrl.setText(JiraAutoUpdater.LATEST_DOWN_URL);
        txtLocal.setText(FoldersConsts.JAR_FILE);
        btnRestart.setText("重启应用");
        btnRestart.setEnabled(isDone);
        everSucceed = isDone;
    }

    private JFrame jiraFrame;
    public void show(JFrame jiraFrame) {
        assistFrame.setVisible(true);
        this.jiraFrame = jiraFrame;
    }

    private void assistFrameWindowClosing(WindowEvent e) {
        if (this.jiraFrame != null) {
            this.jiraFrame.setVisible(true);
        }
        JiraAutoUpdater.stop();
    }

    private void btnRestartMouseClicked(MouseEvent e) {
        if (!btnRestart.isEnabled()) return;

        if (btnRestart.getText().equals("重试下载")) {
            everSucceed = false; //重试下载可以重新激活窗体更新
            doDownload();
            return;
        }

        this.jiraFrame = null; //重启应用就不再激活之前的frame
        assistFrame.setVisible(false);
        assistFrame.dispose();

        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec("java -Dfile.encoding=utf-8 -jar " + FoldersConsts.JAR_FILE);

            // Then retreive the process output
            InputStream in = proc.getInputStream();
            InputStream err = proc.getErrorStream();
            System.exit(0);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void txtUrlMouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable tText = new StringSelection(JiraAutoUpdater.LATEST_DOWN_URL);
            clip.setContents(tText, null);
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("resources");
        assistFrame = new JFrame();
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        label1 = new JLabel();
        progressBar = new JProgressBar();
        label2 = new JLabel();
        txtUrl = new JTextField();
        label3 = new JLabel();
        txtLocal = new JTextField();
        buttonBar = new JPanel();
        lblStatus = new JLabel();
        btnRestart = new JButton();

        //======== assistFrame ========
        {
            assistFrame.setResizable(false);
            assistFrame.setType(Window.Type.POPUP);
            assistFrame.setTitle("\u66f4\u65b0");
            assistFrame.setAlwaysOnTop(true);
            assistFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowActivated(WindowEvent e) {
                    downFrameWindowActivated(e);
                }
                @Override
                public void windowClosing(WindowEvent e) {
                    assistFrameWindowClosing(e);
                }
                @Override
                public void windowOpened(WindowEvent e) {
                    assistFrameWindowOpened(e);
                }
            });
            Container assistFrameContentPane = assistFrame.getContentPane();
            assistFrameContentPane.setLayout(new BorderLayout());

            //======== dialogPane ========
            {
                dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
                dialogPane.setLayout(new BorderLayout());

                //======== contentPanel ========
                {
                    contentPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
                    contentPanel.setForeground(Color.black);
                    contentPanel.setLayout(new GridBagLayout());
                    ((GridBagLayout)contentPanel.getLayout()).columnWidths = new int[] {74, 325, 0, 0};
                    ((GridBagLayout)contentPanel.getLayout()).rowHeights = new int[] {20, 43, 0, 0, 0};
                    ((GridBagLayout)contentPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
                    ((GridBagLayout)contentPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

                    //---- label1 ----
                    label1.setText(bundle.getString("DownloadingFrame.label1.text"));
                    label1.setHorizontalAlignment(SwingConstants.CENTER);
                    contentPanel.add(label1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //---- progressBar ----
                    progressBar.setPreferredSize(new Dimension(146, 40));
                    progressBar.setMinimumSize(new Dimension(10, 40));
                    progressBar.setMaximumSize(new Dimension(32767, 40));
                    progressBar.setBorder(LineBorder.createBlackLineBorder());
                    progressBar.setBackground(Color.white);
                    progressBar.setStringPainted(true);
                    contentPanel.add(progressBar, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //---- label2 ----
                    label2.setText(bundle.getString("DownloadingFrame.label2.text"));
                    label2.setHorizontalAlignment(SwingConstants.CENTER);
                    contentPanel.add(label2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //---- txtUrl ----
                    txtUrl.setFont(new Font("sansserif", Font.PLAIN, 10));
                    txtUrl.setEditable(false);
                    txtUrl.setToolTipText("\u53cc\u51fb\u590d\u5236");
                    txtUrl.setHorizontalAlignment(SwingConstants.TRAILING);
                    txtUrl.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            txtUrlMouseClicked(e);
                        }
                    });
                    contentPanel.add(txtUrl, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 5), 0, 0));

                    //---- label3 ----
                    label3.setText(bundle.getString("DownloadingFrame.label3.text"));
                    label3.setHorizontalAlignment(SwingConstants.CENTER);
                    contentPanel.add(label3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- txtLocal ----
                    txtLocal.setFont(new Font("sansserif", Font.PLAIN, 10));
                    txtLocal.setEditable(false);
                    txtLocal.setToolTipText("\u53cc\u51fb\u590d\u5236");
                    txtLocal.setHorizontalAlignment(SwingConstants.TRAILING);
                    txtLocal.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            txtUrlMouseClicked(e);
                        }
                    });
                    contentPanel.add(txtLocal, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));
                }
                dialogPane.add(contentPanel, BorderLayout.CENTER);

                //======== buttonBar ========
                {
                    buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                    buttonBar.setLayout(new GridBagLayout());
                    ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {81, 197, 52, 80};
                    ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 0.0};
                    buttonBar.add(lblStatus, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                    //---- btnRestart ----
                    btnRestart.setText("\u91cd\u542f\u5e94\u7528");
                    btnRestart.setEnabled(false);
                    btnRestart.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            btnRestartMouseClicked(e);
                        }
                    });
                    buttonBar.add(btnRestart, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
                }
                dialogPane.add(buttonBar, BorderLayout.SOUTH);
            }
            assistFrameContentPane.add(dialogPane, BorderLayout.CENTER);
            assistFrame.setSize(455, 245);
            assistFrame.setLocationRelativeTo(null);
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JFrame assistFrame;
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel label1;
    private JProgressBar progressBar;
    private JLabel label2;
    private JTextField txtUrl;
    private JLabel label3;
    private JTextField txtLocal;
    private JPanel buttonBar;
    private JLabel lblStatus;
    private JButton btnRestart;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
