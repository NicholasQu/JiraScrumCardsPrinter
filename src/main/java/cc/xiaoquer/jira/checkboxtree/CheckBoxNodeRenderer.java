/*
 * #%L
 * Swing JTree check box nodes.
 * %%
 * Copyright (C) 2012 - 2017 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package cc.xiaoquer.jira.checkboxtree;

import cc.xiaoquer.jira.api.JIRA;
import cc.xiaoquer.jira.api.beans.JiraIssue;
import cc.xiaoquer.jira.constant.JiraSwingConsts;
import com.alibaba.fastjson.JSON;
import com.sun.javafx.font.FontFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.Map;

/**
 * A {@link TreeCellRenderer} for check box tree nodes.
 * <p>
 * Thanks to John Zukowski for the <a
 * href="http://www.java2s.com/Code/Java/Swing-JFC/CheckBoxNodeTreeSample.htm"
 * >sample code</a> upon which this is based.
 * </p>
 *
 * @author Curtis Rueden
 */
public class CheckBoxNodeRenderer implements TreeCellRenderer {

    private final CheckBoxNodePanel panel = new CheckBoxNodePanel();

    private final DefaultTreeCellRenderer defaultRenderer =
            new DefaultTreeCellRenderer();

    private final Color selectionForeground, selectionBackground;
    private final Color textForeground, textBackground;

    protected CheckBoxNodePanel getPanel() {
        return panel;
    }

    public CheckBoxNodeRenderer() {
        final Font fontValue = UIManager.getFont("Tree.font");
        if (fontValue != null) panel.item
                .setFont(fontValue);

        final Boolean focusPainted = (Boolean) UIManager.get("Tree.drawsFocusBorderAroundIcon");
        panel.checkbox.setFocusPainted(focusPainted != null && focusPainted);

        selectionForeground = UIManager.getColor("Tree.selectionForeground");
        selectionBackground = UIManager.getColor("Tree.selectionBackground");
        textForeground = UIManager.getColor("Tree.textForeground");
        textBackground = UIManager.getColor("Tree.textBackground");
    }

    // -- TreeCellRenderer methods --

    @Override
    public Component getTreeCellRendererComponent(final JTree tree,
                                                  final Object value, final boolean selected, final boolean expanded,
                                                  final boolean leaf, final int row, final boolean hasFocus) {
        CheckBoxNodeData data = null;
        if (value instanceof DefaultMutableTreeNode) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            final Object userObject = node.getUserObject();
            if (userObject instanceof CheckBoxNodeData) {
                data = (CheckBoxNodeData) userObject;
            }
        }

        if (data == null) {
            // not a check box node; return default cell renderer
            return defaultRenderer.getTreeCellRendererComponent(tree, value,
                    selected, expanded, leaf, row, hasFocus);
        }

        String displayName = data.getItem();
        JiraIssue jiraIssue = JIRA.getObjectFromDisplayName(displayName);

//        final String stringValue =
//                tree.convertValueToText(value, selected, expanded, leaf, row, false);

        panel.checkbox.setSelected(false);
        panel.item.setText(data.getItem());

        panel.setEnabled(tree.isEnabled());

        if (selected) {
            panel.setForeground(selectionForeground);
            panel.setBackground(selectionBackground);
            panel.item.setForeground(selectionForeground);
            panel.item.setBackground(selectionBackground);
        } else {
            panel.setForeground(textForeground);
            panel.setBackground(textBackground);
            panel.item.setForeground(textForeground);
            panel.item.setBackground(textBackground);
        }

        panel.checkbox.setSelected(data.isChecked());

        if (jiraIssue.isStory()) {
            panel.item.setIcon(JiraSwingConsts.IMG_STORY);
        } else if (jiraIssue.isEpic()) {
            panel.item.setIcon(JiraSwingConsts.IMG_EPIC);
        } else if (jiraIssue.isTask()) {
            panel.item.setIcon(JiraSwingConsts.IMG_TASK);
        } else if (jiraIssue.isSubTask()) {
            panel.item.setIcon(JiraSwingConsts.IMG_SUBTASK);
        }

        Font font = panel.item.getFont();
        Map attributes = font.getAttributes();
        attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
        Font doneFont = new Font(attributes);

        Map attributes2 = font.getAttributes();
        attributes2.put(TextAttribute.STRIKETHROUGH, false);
        Font usualFont  = new Font(attributes2);


        if (jiraIssue.isDone()) {
            panel.item.setFont(doneFont);
            panel.item.setForeground(Color.lightGray);
        } else if (jiraIssue.isDoing()) {
            panel.item.setFont(usualFont);
            panel.item.setForeground(new Color(78, 170, 222));
        } else {
            panel.item.setFont(usualFont);
            panel.item.setForeground(new Color(212, 159, 79));
        }

        return panel;
    }


}
