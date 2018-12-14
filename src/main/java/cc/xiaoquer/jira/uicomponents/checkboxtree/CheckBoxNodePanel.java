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

package cc.xiaoquer.jira.uicomponents.checkboxtree;

import java.awt.*;

import javax.swing.*;

/**
 * Helper class defining Swing components for check box node renderer UI.
 * <p>
 * Thanks to Jason S of StackOverflow for <a
 * href="http://stackoverflow.com/a/1224207">his example code</a>.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class CheckBoxNodePanel extends JPanel {

	public final JCheckBox  checkbox    = new JCheckBox();
	public final JLabel     item        = new JLabel();
//	public final JLabel     hiddenjson  = new JLabel();

	public CheckBoxNodePanel() {
		/**
		 * Columns： CheckBox，status, name, owner, type, key, id
		 */
//		setLayout(new GridBagLayout());
//		((GridBagLayout)getLayout()).columnWidths = new int[] {10, 100, 200};
//		((GridBagLayout)getLayout()).rowHeights = new int[] {20};
//		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0,1.0E-4};
//		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0E-4};
//
//		add(checkbox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
//				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
//				new Insets(0, 0, 0, 5), 0, 0));
//
//		add(item, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
//				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
//				new Insets(0, 0, 0, 5), 0, 0));
//
//		add(hiddenjson, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
//				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
//				new Insets(0, 0, 0, 5), 0, 0));

		setLayout(new BorderLayout());
		add(checkbox, BorderLayout.WEST);
		add(item, BorderLayout.CENTER);

//		hiddenjson.setForeground(Color.white);
//      hiddenjson.setOpaque(true);
//		add(hiddenjson, BorderLayout.EAST);

	}

}
