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

import com.rapidminer.gui.tools.dialogs.ButtonDialog;
import com.rapidminer.repository.Repository;

/** Dialog to configure an existing repository.
 * 
 * @author Simon Fischer
 *
 */
public class RepositoryConfigurationDialog extends ButtonDialog {

	private static final long serialVersionUID = 1L;

	private RepositoryConfigurationPanel configurationPanel;
	private Repository repository;
	
	public RepositoryConfigurationDialog(Repository repository) {
		super("repositoryconfigdialog", true);
		this.repository = repository;
		configurationPanel = repository.makeConfigurationPanel();
		configurationPanel.configureUIElementsFrom(repository);

		layoutDefault(configurationPanel.getComponent(), DEFAULT_SIZE, makeCancelButton(), makeOkButton());
	}

	@Override
	protected void ok() {
		configurationPanel.configure(repository);
		super.ok();
	}
	
}
