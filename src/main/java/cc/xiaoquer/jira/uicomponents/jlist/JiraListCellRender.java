package cc.xiaoquer.jira.uicomponents.jlist;

import cc.xiaoquer.jira.api.beans.JiraSprint;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Nicholas on 2018/12/14.
 */
public class JiraListCellRender extends DefaultListCellRenderer {
    private ArrayList jiraList;

    public JiraListCellRender(ArrayList jiraList) {
        this.jiraList = jiraList;
    }
    
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        Object object = jiraList.get(index);

        if (object instanceof JiraSprint) {
            JiraSprint jiraSprint = (JiraSprint) object;

            if (!jiraSprint.isActive()) {
                setForeground(Color.lightGray);

                Font font = getFont();
                Map attributes = font.getAttributes();
                attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                setFont(new Font(attributes));
            }
        }

        return this;
    }
}
