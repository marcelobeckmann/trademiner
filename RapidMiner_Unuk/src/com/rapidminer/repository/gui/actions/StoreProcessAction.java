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

import com.rapidminer.Process;
import com.rapidminer.RepositoryProcessLocation;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.ProgressThread;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.dialogs.ConfirmDialog;
import com.rapidminer.repository.Entry;
import com.rapidminer.repository.Folder;
import com.rapidminer.repository.ProcessEntry;
import com.rapidminer.repository.RepositoryException;
import com.rapidminer.repository.RepositoryLocation;
import com.rapidminer.repository.gui.RepositoryTree;

/**
 * This action stores the current process at the selected entry.
 *
 * @author Simon Fischer
 */
public class StoreProcessAction extends AbstractRepositoryAction<Entry> {
	
	private static final long serialVersionUID = 1L;

	
	public StoreProcessAction(RepositoryTree tree) {
		super(tree, Entry.class, false, "repository_store_process");
	}

	@Override
	public void actionPerformed(Entry entry) {
		if (entry instanceof Folder) {
			storeInFolder(Folder.class.cast(entry));
		}
		if (entry instanceof ProcessEntry) {
			overwriteProcess(ProcessEntry.class.cast(entry));
		}
	}
	
	private void storeInFolder(final Folder folder) {
		// get current process name (if present)
		String currentName = null;
		if (RapidMinerGUI.getMainFrame().getProcess().getProcessLocation() != null) {
			currentName = RapidMinerGUI.getMainFrame().getProcess().getProcessLocation().getShortName();
		}
		
		final String name = SwingTools.showInputDialog("store_process", currentName);
		if (name != null) {
			if (name.isEmpty()) {
				SwingTools.showVerySimpleErrorMessage("please_enter_non_empty_name");
				return;
			}
			try {
				if (folder.containsEntry(name)) {
					SwingTools.showVerySimpleErrorMessage("repository_entry_already_exists", name);
					return;
				}
			} catch (RepositoryException e1) {
				SwingTools.showSimpleErrorMessage("cannot_store_process_in_repository", e1, name);
				return;
			}

			ProgressThread storeProgressThread = new ProgressThread("store_process") {
				public void run() {
					getProgressListener().setTotal(100);
					try {
						getProgressListener().setCompleted(10);
						Process process = RapidMinerGUI.getMainFrame().getProcess();
						process.setProcessLocation(new RepositoryProcessLocation(new RepositoryLocation(folder.getLocation(), name)));
						folder.createProcessEntry(name, process.getRootOperator().getXML(false));
						tree.expandPath(tree.getSelectionPath());
						RapidMinerGUI.getMainFrame().processHasBeenSaved();
					} catch (Exception e) {
						SwingTools.showSimpleErrorMessage("cannot_store_process_in_repository", e, name);
					} finally {
						getProgressListener().setCompleted(10);
						getProgressListener().complete();
					}
				}
			};					
			storeProgressThread.start();
		}
	}
	
	private void overwriteProcess(final ProcessEntry processEntry) {
		if (SwingTools.showConfirmDialog("overwrite", ConfirmDialog.YES_NO_OPTION, processEntry.getLocation()) == ConfirmDialog.YES_OPTION) {
			ProgressThread storeProgressThread = new ProgressThread("store_process") {
				@Override
				public void run() {
					getProgressListener().setTotal(100);
					getProgressListener().setCompleted(10);
					try {								
						Process process = RapidMinerGUI.getMainFrame().getProcess();
						process.setProcessLocation(new RepositoryProcessLocation(processEntry.getLocation()));
						processEntry.storeXML(process.getRootOperator().getXML(false));
					} catch (Exception e) {
						SwingTools.showSimpleErrorMessage("cannot_store_process_in_repository", e, processEntry.getName());								
					} finally {
						getProgressListener().setCompleted(100);
						getProgressListener().complete();
					}
				}						
			};
			storeProgressThread.start();
		}
	}
	
	@Override
	public void enable() {
		Entry entry = tree.getSelectedEntry();
		setEnabled((Folder.class.isInstance(entry) || ProcessEntry.class.isInstance(entry)));
	}

}
