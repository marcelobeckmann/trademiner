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
package com.rapidminer.repository.remote;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import com.rapid_i.repository.wsimport.AccessRights;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.gui.tools.ProgressThread;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.dialogs.ButtonDialog;
import com.rapidminer.repository.AccessFlag;
import com.rapidminer.repository.AccessType;
import com.rapidminer.repository.RepositoryException;

/**
 * 
 * @author Simon Fischer
 *
 */
public class AccessRightsDialog extends ButtonDialog {

	private class AccessRightsTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		@Override
		public int getColumnCount() {
			return 4;
		}
		@Override
		public int getRowCount() {
			return accessRights.size();
		}
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			AccessRights ar = accessRights.get(rowIndex);
			switch (columnIndex) {
			case 0:	return ar.getGroup();
			case 1:	return ar.getRead();
			case 2:	return ar.getWrite();
			case 3:	return ar.getExecute();
			default: throw new IndexOutOfBoundsException(columnIndex+"");
			}			
		}
		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:	return "Group";
			case 1:	return AccessType.READ.toString();
			case 2:	return AccessType.WRITE.toString();
			case 3:	return AccessType.EXECUTE.toString();
			default: throw new IndexOutOfBoundsException(column+"");
			}
		}
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			AccessRights ar = accessRights.get(rowIndex);
			switch (columnIndex) {
			case 0:	ar.setGroup((String) aValue); break;
			case 1:	ar.setRead(aValue.toString()); break;
			case 2:	ar.setWrite(aValue.toString()); break;
			case 3:	ar.setExecute(aValue.toString()); break;
			default: throw new IndexOutOfBoundsException(columnIndex+"");
			}
		}
	}
	
	private TableCellEditor accessRightsCellEditor = new DefaultCellEditor(new JComboBox(AccessFlag.values())) {
		private static final long serialVersionUID = 1L;
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			JComboBox c = (JComboBox)super.getTableCellEditorComponent(table, value, isSelected, row, column);
			c.setSelectedItem(value);
			return c;
		}
	};

	private TableCellEditor groupsCellEditor;

	private static final long serialVersionUID = 1L;

	private RemoteEntry entry;
	private List<AccessRights> accessRights;

	private AccessRightsTableModel accessRightsTableModel;

	public AccessRightsDialog(RemoteEntry entry, List<AccessRights> accessRights, final List<String> groupNames) {
		super("repository.edit_access_rights", entry.getLocation());
		this.entry = entry;
		this.accessRights = accessRights;
		Collections.sort(groupNames);
		
		groupsCellEditor = new DefaultCellEditor(new JComboBox(groupNames.toArray())) {
			private static final long serialVersionUID = 1L;
			@Override
			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
				JComboBox c = (JComboBox)super.getTableCellEditorComponent(table, value, isSelected, row, column);
				c.setSelectedItem(value);
				return c;
			}
		};
		
		accessRightsTableModel = new AccessRightsTableModel();
		final ExtendedJTable table = new ExtendedJTable(accessRightsTableModel, false) {
			private static final long serialVersionUID = 1L;
			public javax.swing.table.TableCellEditor getCellEditor(int row, int column) {
				if (column == 0) {
					return groupsCellEditor;
				} else {
					return accessRightsCellEditor;
				}
			};
		};

		JButton addRowButton = new JButton(new ResourceAction("accessrights.add_row") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				AccessRights newRights = new AccessRights();
				newRights.setGroup(groupNames.get(0));
				newRights.setRead(AccessFlag.IGNORE.toString());
				newRights.setWrite(AccessFlag.IGNORE.toString());
				newRights.setExecute(AccessFlag.IGNORE.toString());
				
				if (table.getSelectedRow() == -1) {
					AccessRightsDialog.this.accessRights.add(newRights);
				} else {
					AccessRightsDialog.this.accessRights.add(table.getSelectedRow()+1, newRights);
				}
				accessRightsTableModel.fireTableStructureChanged();
			}			
		});
		JButton removeRowButton = new JButton(new ResourceAction("accessrights.remove_row") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				if (table.getSelectedRow() == -1) {
					return;
				}
				AccessRightsDialog.this.accessRights.remove(table.getSelectedRow());
				accessRightsTableModel.fireTableStructureChanged();
			}			
		});
		JScrollPane tablePane = new ExtendedJScrollPane(table);
		tablePane.setBorder(createBorder());
		layoutDefault(tablePane, NORMAL, addRowButton, removeRowButton, makeOkButton(), makeCancelButton());
	}


	@Override
	protected void ok() {	
		new ProgressThread("connect_to_repository") {
			public void run() {
				try {
					entry.setAccessRights(accessRights);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() { dispose(); }
					});
				} catch (RepositoryException e) {
					SwingTools.showSimpleErrorMessage("error_contacting_repository", e, e.getMessage());
				}				
			}
		}.start();
	}
}
