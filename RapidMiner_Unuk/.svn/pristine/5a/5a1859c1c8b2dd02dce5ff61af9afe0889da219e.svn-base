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
package com.rapidminer.repository.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.ResourceLabel;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.repository.Repository;
import com.rapidminer.repository.RepositoryException;
import com.rapidminer.repository.RepositoryManager;
import com.rapidminer.repository.local.LocalRepository;
/**
 * @author Simon Fischer
 */
public class LocalRepositoryPanel extends JPanel implements RepositoryConfigurationPanel {

	private static final long serialVersionUID = 1L;
	
	private final JTextField fileField = new JTextField(30);
	private final JTextField aliasField = new JTextField("NewLocalRepository", 30);
	
	public LocalRepositoryPanel() {
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.weighty = 0;
		c.weightx = .5;
		c.insets = new Insets(4, 4, 4, 4);
		c.fill = GridBagConstraints.HORIZONTAL;

		// ALIAS
		c.gridwidth = 1;
		JLabel label = new ResourceLabel("repositorydialog.alias");		
		label.setLabelFor(aliasField);
		gbl.setConstraints(label, c);
		add(label);		

		c.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(aliasField, c);
		add(aliasField);

		// URL
		c.gridwidth = 1;
		label = new ResourceLabel("repositorydialog.root_directory");
		label.setLabelFor(fileField);
		gbl.setConstraints(label, c);
		add(label);

		c.gridwidth = GridBagConstraints.RELATIVE;
		gbl.setConstraints(fileField, c);
		add(fileField);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		JButton chooseFileButton = new JButton(new ResourceAction(true, "choose_file") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = SwingTools.chooseFile(LocalRepositoryPanel.this, null, true, true, (String)null, null);
				if (file != null) {
					fileField.setText(file.toString());
				}
			}			
		});
		add(chooseFileButton, c);
		
		JPanel dummy = new JPanel();
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1;
		gbl.setConstraints(dummy, c);
		add(dummy);
		
		aliasField.selectAll();
		fileField.selectAll();
	}

	@Override
	public void makeRepository() {
		File file = new File(fileField.getText());
		file.mkdir();
		String alias = aliasField.getText().trim();
		if (alias.length() == 0) {
			alias = file.getName();
		}
		try {
			RepositoryManager.getInstance(null).addRepository(new LocalRepository(alias, file));
		} catch (RepositoryException e) {
			SwingTools.showSimpleErrorMessage("cannot_create_repository", e);
		}
	}

	@Override
	public void configureUIElementsFrom(Repository repository) {
		aliasField.setText(((LocalRepository) repository).getName());
		fileField.setText(((LocalRepository) repository).getRoot().getAbsolutePath());
	}

	@Override
	public boolean configure(Repository repository) {
		((LocalRepository) repository).setRoot(new File(fileField.getText()));
		((LocalRepository) repository).rename(aliasField.getText());
		return true;
	}

	@Override
	public JComponent getComponent() {
		return this;
	}
}
