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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.logging.Level;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;

import com.rapidminer.gui.properties.PropertyDialog;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.dialogs.SQLQueryBuilder;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterTypeSQLQuery;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.jdbc.DatabaseHandler;
import com.rapidminer.tools.jdbc.connection.ConnectionEntry;
import com.rapidminer.tools.jdbc.connection.ConnectionProvider;


/**
 * @author Tobias Malbrecht
 */
public class SQLQueryValueCellEditor extends AbstractCellEditor implements PropertyValueCellEditor {
	
	private static final long serialVersionUID = -771727412083431607L;
	
	private Operator operator;
	
	private final JButton button;
	
	private String sqlQuery;

	public SQLQueryValueCellEditor(final ParameterTypeSQLQuery type) {
		button = new JButton(new ResourceAction(true, "build_sql") {
			private static final long serialVersionUID = -2911499842513746414L;

			public void actionPerformed(ActionEvent e) {
				DatabaseHandler handler;
				try {
					handler = DatabaseHandler.getConnectedDatabaseHandler(operator);
				} catch (Exception e2) {
					//LogService.getRoot().log(Level.WARNING, "Failed to connect to database: "+e2);
					LogService.getRoot().log(Level.WARNING, "com.rapidminer.gui.properties.celleditors.value.SQLQueryValueCellEditor.connecting_to_database_error", e2);
					// we can continue without a db handler
					handler = null;
					//SwingTools.showSimpleErrorMessage("db_connection_failed_simple", e2, e2.getMessage());
					//return;
				}
				final SQLQueryBuilder queryBuilder = new SQLQueryBuilder(handler);
				class SQLQueryPropertyDialog extends PropertyDialog {
					private static final long serialVersionUID = -5224113818406394872L;

					private SQLQueryPropertyDialog(boolean editOnly) {
						super(type, "sql");
						layoutDefault(queryBuilder.makeQueryBuilderPanel(editOnly), NORMAL, makeOkButton(), makeCancelButton());
					}
				}

				boolean connectionProvided = false;
				if (operator != null) {
					if (operator instanceof ConnectionProvider) {
						ConnectionEntry entry = ((ConnectionProvider) operator).getConnectionEntry();
						connectionProvided = (entry != null);
						queryBuilder.setConnectionEntry(entry);
					}
				}
				SQLQueryPropertyDialog dialog = new SQLQueryPropertyDialog(!connectionProvided);
				if (operator != null) {
					String query = null;
					try {
						query = operator.getParameters().getParameter(type.getKey());
					} catch (UndefinedParameterError e1) {
					}
					if (query != null) {
						queryBuilder.setQuery(query);
					}
				}
				dialog.setVisible(true);
				if (dialog.isOk()) {
					sqlQuery = queryBuilder.getQuery();
					fireEditingStopped();
				} else {
					fireEditingCanceled();
				}

			}
		});
		button.setMargin(new Insets(0, 0, 0, 0));
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
		return button;
	}

	@Override
	public Object getCellEditorValue() {
		return sqlQuery;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		return button;
	}

	@Override
	public void setOperator(Operator operator) {
		this.operator = operator;
	}
}
