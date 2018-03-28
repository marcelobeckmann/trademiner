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
package com.rapidminer.repository.gui.actions;

import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.dialogs.ConfirmDialog;
import com.rapidminer.repository.Entry;
import com.rapidminer.repository.gui.RepositoryTree;

/**
 * This action deletes the selected entry.
 *
 * @author Simon Fischer
 */
public class DeleteAction extends AbstractRepositoryAction<Entry> {
	
	private static final long serialVersionUID = 1L;

	
	public DeleteAction(RepositoryTree tree) {
		super(tree, Entry.class, false, "repository_delete_entry");
	}

	@Override
	public void actionPerformed(Entry entry) {
		if (SwingTools.showConfirmDialog("file_chooser.delete", ConfirmDialog.YES_NO_OPTION, entry.getName()) == ConfirmDialog.YES_OPTION) {
			try {
				entry.delete();
			} catch (Exception e1) {
				SwingTools.showSimpleErrorMessage("cannot_delete_entry", e1, entry.getLocation());
			}
		}
	}

}
