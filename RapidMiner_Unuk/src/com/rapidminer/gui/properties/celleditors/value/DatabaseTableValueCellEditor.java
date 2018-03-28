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
package com.rapidminer.gui.properties.celleditors.value;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.AbstractCellEditor;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.rapidminer.gui.tools.ProgressThread;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDatabaseSchema;
import com.rapidminer.parameter.ParameterTypeDatabaseTable;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.jdbc.ColumnIdentifier;
import com.rapidminer.tools.jdbc.DatabaseHandler;
import com.rapidminer.tools.jdbc.TableName;
import com.rapidminer.tools.jdbc.connection.ConnectionEntry;
import com.rapidminer.tools.jdbc.connection.ConnectionProvider;

/** Displays a combo box with table names. Can work in two operation {@link Mode}s, one displaying
 *  table names, and one displaying schema names.
 * 
 * 
 * @author Tobias Malbrecht
 */
public class DatabaseTableValueCellEditor extends AbstractCellEditor implements PropertyValueCellEditor {

	private static final long serialVersionUID = -771727412083431607L;

	private enum Mode {
		TABLE, SCHEMA
	};
	private Mode mode;

	class DatabaseTableComboBoxModel extends AbstractListModel implements ComboBoxModel, Serializable {
		private static final long serialVersionUID = -2984664300141879731L;

		private String lastURL = null;
		
		private String lastSelectedSchema = null;		

		private LinkedList<Object> list = new LinkedList<Object>();

		private Object selected = null;

		public boolean updateModel() {
			final Object selected = getValue();
			boolean schemaChanged = false;
			if (mode == Mode.TABLE) {
				String selectedSchema = null;
				if (DatabaseTableValueCellEditor.this.operator != null) {
					if (!DatabaseTableValueCellEditor.this.operator.getParameterAsBoolean(DatabaseHandler.PARAMETER_USE_DEFAULT_SCHEMA)) {
						try {
							selectedSchema = DatabaseTableValueCellEditor.this.operator.getParameterAsString(DatabaseHandler.PARAMETER_SCHEMA_NAME);
							if ((selectedSchema != null) && selectedSchema.isEmpty()) {
								selectedSchema = null;
							}
						} catch (UndefinedParameterError e) {
							selectedSchema = null;
						}
					}
				}			
				schemaChanged = !Tools.equals(selectedSchema, lastSelectedSchema);
				lastSelectedSchema = selectedSchema;				
			}
			
			if (connectionProvider != null) {
				final ConnectionEntry entry = connectionProvider.getConnectionEntry();
				if (entry != null && (schemaChanged || (lastURL == null) || !lastURL.equals(entry.getURL()))) {
					lastURL = entry.getURL();
					ProgressThread t = new ProgressThread("fetching_database_tables") { 
						@Override
						public void run() {
							getProgressListener().setTotal(100);
							getProgressListener().setCompleted(10);
							try {
								list.clear();
								DatabaseHandler handler = null;
								try {
									handler = DatabaseHandler.getConnectedDatabaseHandler(entry);
								} catch (SQLException e1) {
									//LogService.getRoot().log(Level.WARNING, "Failed to fetch database tables: "+e1, e1);
									LogService.getRoot().log(Level.WARNING,
											I18N.getMessage(LogService.getRoot().getResourceBundle(), 
											"com.rapidminer.gui.properties.celleditors.value.DatabaseTableValueCellEditor.fetching_database_tables_error", 
											e1),
											e1);

									SwingTools.showSimpleErrorMessage("failed_to_fetch_database_tables", e1, entry.getName(), e1.getMessage());
									return;
								}

								getProgressListener().setCompleted(20);

								if (handler != null) {
									Map<TableName, List<ColumnIdentifier>> tableMap;
									try {
										tableMap = handler.getAllTableMetaData(getProgressListener(), 20, 90, false);
										for (TableName tn : tableMap.keySet()) {
											switch (DatabaseTableValueCellEditor.this.mode) {
											case TABLE:
												if (lastSelectedSchema != null) {
													if (!lastSelectedSchema.equals(tn.getSchema())) {
														continue;
													}
												}
												list.add(tn.getTableName());
												break;
											case SCHEMA:
												if (!list.contains(tn.getSchema())) {
													list.add(tn.getSchema());
												}
												break;
											default:
												throw new RuntimeException("Illegal mode: "+DatabaseTableValueCellEditor.this.mode);
											}
											
										}
										//list.addAll(tableMap.keySet());
										getProgressListener().setCompleted(90);
									} catch (SQLException e) {
										//LogService.getRoot().log(Level.WARNING, "Failed to fetch database tables: "+e, e);
										LogService.getRoot().log(Level.WARNING,
												I18N.getMessage(LogService.getRoot().getResourceBundle(), 
												"com.rapidminer.gui.properties.celleditors.value.DatabaseTableValueCellEditor.fetching_database_tables_error", 
												e),
												e);

										SwingTools.showSimpleErrorMessage("failed_to_fetch_database_tables", e, entry.getName(), e.getMessage());
										return;
									}
									try {
										handler.disconnect();
									} catch (SQLException e) {
									}
								}
								if (getSelectedItem() == null) {
									if (model.getSize() == 0) {
										setSelectedItem(null);
									} else {
										if (selected != null) {
											setSelectedItem(selected);
										} else {
											if (model.getSize() > 0) {
												setSelectedItem(model.getElementAt(0));
											}
										}
									}
								}
								fireContentsChanged(this, 0, list.size() - 1);
							} finally {
								getProgressListener().complete();
							}
						}
					};
					t.start();
					return true;
				}
			}
			return false;
		}

		@Override
		public Object getSelectedItem() {
			return selected;
		}

		@Override
		public void setSelectedItem(Object object) {
			if ((selected != null && !selected.equals(object)) || selected == null && object != null) {
				selected = object;
				fireContentsChanged(this, -1, -1);
			}
		}

		@Override
		public Object getElementAt(int index) {
			if (index >= 0 && index < list.size()) {
				return list.get(index);
			}
			return null;
		}

		@Override
		public int getSize() {
			return list.size();
		}
	}

	class DatabaseTableComboBox extends JComboBox {
		private static final long serialVersionUID = 7641636749562465262L;

		private DatabaseTableComboBox() {
			super(model);
			setEditable(true);
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}			
			});
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					fireEditingStopped();
				}

				@Override
				public void focusGained(FocusEvent e) {
					model.updateModel();
				}
			});
			addPopupMenuListener(new PopupMenuListener() {
				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					if (model.updateModel()) {
						hidePopup();
						showPopup();
					}
				}
			});
		}
	}

	private DatabaseTableComboBoxModel model = new DatabaseTableComboBoxModel();

	private JComboBox comboBox = new DatabaseTableComboBox();

	private Operator operator;

	private ParameterType type;

	private ConnectionProvider connectionProvider;

	public DatabaseTableValueCellEditor(final ParameterTypeDatabaseSchema type) {
		this.type = type;
		this.mode = Mode.SCHEMA;
		comboBox.setToolTipText(type.getDescription());		
	}
	
	public DatabaseTableValueCellEditor(final ParameterTypeDatabaseTable type) {
		this.type = type;
		this.mode = Mode.TABLE;
		comboBox.setToolTipText(type.getDescription());
	}

	private String getValue() {
		String value = null;
		value = operator.getParameters().getParameterOrNull(type.getKey());
		return value;
	}

	@Override
	public boolean rendersLabel() {
		return false;
	}

	@Override
	public boolean useEditorAsRenderer() {
		return true;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		model.updateModel();
		comboBox.setSelectedItem(value);
		return comboBox;
	}

	@Override
	public Object getCellEditorValue() {
		return comboBox.getSelectedItem();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		model.updateModel();
		comboBox.setSelectedItem(value);
		return comboBox;
	}

	@Override
	public void setOperator(Operator operator) {
		this.operator = operator;
		if (operator != null && operator instanceof ConnectionProvider) {
			this.connectionProvider = (ConnectionProvider) operator; 
		}
	}
}
