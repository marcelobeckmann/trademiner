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
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.rapidminer.gui.tools.ProgressThread;
import com.rapidminer.gui.tools.ResourceLabel;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.repository.Repository;
import com.rapidminer.repository.RepositoryManager;
import com.rapidminer.repository.remote.RemoteRepository;

/** Panel to add remote repositories
 * 
 * @author Simon Fischer
 *
 */
public class RemoteRepositoryPanel extends JPanel implements RepositoryConfigurationPanel {

	private static final long serialVersionUID = 1L;
	
	private final JTextField urlField = new JTextField("http://localhost:8080/", 30);
	private final JTextField aliasField = new JTextField("NewRepository", 30);
	private final JTextField userField = new JTextField(System.getProperty("user.name"), 20);
	private final JPasswordField passwordField = new JPasswordField(20); 

	public RemoteRepositoryPanel() {
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weighty = 1;
		c.weightx = .5;
		c.insets = new Insets(4, 4, 4, 4);

		// ALIAS
		c.gridwidth = GridBagConstraints.RELATIVE;
		JLabel label = new ResourceLabel("repositorydialog.alias");		
		label.setLabelFor(aliasField);
		gbl.setConstraints(label, c);
		add(label);		

		c.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(aliasField, c);
		add(aliasField);

		// URL
		c.gridwidth = GridBagConstraints.RELATIVE;
		label = new ResourceLabel("repositorydialog.url");
		label.setLabelFor(urlField);
		gbl.setConstraints(label, c);
		add(label);

		c.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(urlField, c);
		add(urlField);

		// USERNAME
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.insets = new Insets(10, 4, 4, 4);
		label = new ResourceLabel("repositorydialog.user");
		label.setLabelFor(userField);
		gbl.setConstraints(label, c);
		add(label);

		c.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(userField, c);
		add(userField);

		// Password
		c.insets = new Insets(4, 4, 4, 4);
		c.gridwidth = GridBagConstraints.RELATIVE;
		label = new ResourceLabel("repositorydialog.password");
		label.setLabelFor(passwordField);
		gbl.setConstraints(label, c);
		add(label);

		c.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(passwordField, c);
		add(passwordField);

		aliasField.selectAll();
		urlField.selectAll();
		userField.selectAll();
	}
	
	@Override
	public void makeRepository() {
		final URL url;
		try {
			url = new URL(urlField.getText());
		} catch (MalformedURLException e) {
			SwingTools.showSimpleErrorMessage("illegal_url",e);
			return;
		}
		String alias = aliasField.getText().trim();
		if (alias.length() == 0) {
			alias = url.toString();
		}
		final String finalAlias = alias;

		ProgressThread pt = new ProgressThread("add_repository") {		
			@Override
			public void run() {
				getProgressListener().setTotal(100);
				getProgressListener().setCompleted(10);
				Repository repository = new RemoteRepository(url, finalAlias, userField.getText(), passwordField.getPassword(), false);
				getProgressListener().setCompleted(90);
				if (repository != null) {
					RepositoryManager.getInstance(null).addRepository(repository);
				}
				getProgressListener().setCompleted(100);
				getProgressListener().complete();
			}
		};
		pt.start();
	}

	@Override
	public void configureUIElementsFrom(Repository remote) {
		aliasField.setText(((RemoteRepository) remote).getAlias());
		urlField.setText(((RemoteRepository) remote).getBaseUrl().toString());
		userField.setText(((RemoteRepository) remote).getUsername());
	}

	@Override
	public boolean configure(Repository repository) {
		URL url;
		try {
			url = new URL(urlField.getText());
		} catch (MalformedURLException e) {
			SwingTools.showSimpleErrorMessage("illegal_url",e);
			return false;
		}
		((RemoteRepository) repository).setBaseUrl(url);
		if ((passwordField.getPassword() != null) && (passwordField.getPassword().length > 0)) {
			((RemoteRepository) repository).setPassword(passwordField.getPassword());
		}
		((RemoteRepository) repository).setUsername(userField.getText());
		((RemoteRepository) repository).rename(aliasField.getText());
		return true;
	}
	

	@Override
	public JComponent getComponent() {
		return this;
	}
}
