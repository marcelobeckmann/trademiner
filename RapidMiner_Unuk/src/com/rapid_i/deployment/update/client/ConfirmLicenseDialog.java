/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2012 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapid_i.deployment.update.client;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import com.rapidminer.deployment.client.wsimport.PackageDescriptor;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.dialogs.ButtonDialog;

/**
 * 
 * @author Simon Fischer
 *
 */
public class ConfirmLicenseDialog extends ButtonDialog {

	private static final long serialVersionUID = 1L;
	private JRadioButton accept, reject;
	private JButton okButton;

	public ConfirmLicenseDialog(PackageDescriptor desc, String license) {
		super("confirm_license", desc.getName());
		setModal(true);
		
		JPanel main = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		JEditorPane pane = new JEditorPane("text/plain", license);
		pane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(pane);
		scrollPane.setPreferredSize(new Dimension(400, 400));
		main.add(scrollPane, c);
		
		c.weighty = 0;
		accept = new JRadioButton(new ResourceAction("accept_license") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				enableButtons();
			}
		});
		reject = new JRadioButton(new ResourceAction("reject_license") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				enableButtons();
			}
		});		
		main.add(reject, c);
		main.add(accept, c);
		ButtonGroup group = new ButtonGroup();
		group.add(accept);
		group.add(reject);
		reject.setSelected(true);
		
		okButton = makeOkButton();
		okButton.setEnabled(false);
		layoutDefault(main, makeCancelButton("skip_install"), okButton);
	}

	private void enableButtons() {
		okButton.setEnabled(accept.isSelected());
	}			

	/** Returns true iff the user chooses to confirm the license. */
	public static boolean confirm(PackageDescriptor desc, String license) {
		ConfirmLicenseDialog d = new ConfirmLicenseDialog(desc, license);
		d.setVisible(true);
		return d.wasConfirmed();
	}
}
