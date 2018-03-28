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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.dialogs.ButtonDialog;

/** This dialog helps the user to deactivate conflicting extensions.
 * 
 * @author Simon Fischer
 *
 */
public class LicenseConflictDialog extends ButtonDialog {

	private static final long serialVersionUID = 1L;

	public LicenseConflictDialog(final Collection<ManagedExtension> gpl, final Collection<ManagedExtension> comm) {
		super("license_conflict");
		
		JPanel main = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = c.weighty = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		main.add(makeListLabel(gpl, "GPL"), c);
		main.add(makeListLabel(comm, "Commercial License"), c);		
		
		JButton gplButton = new JButton(new ResourceAction("license_conflict_resolve_gpl") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				deactivate(comm);
			}
		});
		JButton commButton = new JButton(new ResourceAction("license_conflict_resolve_commercial") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				deactivate(gpl);
			}
		});
		layoutDefault(main, gplButton, commButton, makeOkButton("license_conflict_resolve_manually"));
	}

	private Component makeListLabel(Collection<ManagedExtension> extensions, String licenseName) {
		StringBuilder b = new StringBuilder();
		b.append("<html><strong>Extensions under ").append(licenseName).append("</strong><br/>");
		boolean first = false;
		for (ManagedExtension ext : extensions) {
			if (first) {
				first = false;
			} else {
				b.append(", ");
			}
			b.append(ext.getName());
		}
		b.append(".</html>");
		return new JLabel(b.toString());
	}

	private void deactivate(Collection<ManagedExtension> extensions) {
		for (ManagedExtension ext : extensions) {
			ext.setActive(false);
		}
		ManagedExtension.saveConfiguration();
	}
}
