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
package com.rapidminer.gui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.processeditor.results.ResultDisplayTools;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJTabbedPane;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.XMLEditor;
import com.rapidminer.tools.math.AnovaCalculator;
import com.rapidminer.tools.math.SignificanceCalculationException;
import com.rapidminer.tools.math.SignificanceTestResult;
import com.rapidminer.tools.math.TestGroup;


/**
 * This dialog shows the complete result history. The currently selected process definition will be displayed
 * in an XML view and the corresponding results will be displayed in the result area.
 * 
 * @author Ingo Mierswa
 */
public class ResultHistoryDialog extends JDialog {

	private static final long serialVersionUID = 7498142147390911809L;

	private class AnovaAction extends AbstractAction {

		private static final long serialVersionUID = 6768585475843634549L;

		public AnovaAction() {
			super("Anova");
		}

		public void actionPerformed(ActionEvent e) {
			AnovaCalculator calculator = new AnovaCalculator();
			calculator.setAlpha(0.05);

			Iterator<TestGroup> i = selectedTestGroups.iterator();
			while (i.hasNext()) {
				TestGroup group = i.next();
				calculator.addGroup(group);
			}

			try {
				SignificanceTestResult result = calculator.performSignificanceTest();
				JOptionPane.showMessageDialog(ResultHistoryDialog.this, ResultDisplayTools.createVisualizationComponent(result, null, "ANOVA Calculation"), "ANOVA result", JOptionPane.PLAIN_MESSAGE);
			} catch (SignificanceCalculationException ex) {
				SwingTools.showSimpleErrorMessage("cannot_calc_statistical_significance", ex);
			}
		}
	}

	private final transient Action ANOVA_ACTION = new AnovaAction();

	private List<TestGroup> selectedTestGroups = new LinkedList<TestGroup>();

	public ResultHistoryDialog(Frame owner) {
		super(owner, "Result Comparator");
		setLayout(new BorderLayout());

		// left part
		JSplitPane resultSelectionSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		resultSelectionSplitPane.setBorder(null);

		// list
		JPanel processSelectionPanel = new JPanel();
		processSelectionPanel.setBorder(null);
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		processSelectionPanel.setLayout(layout);
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.0d;
		c.weightx = 1.0d;
		c.gridwidth = GridBagConstraints.REMAINDER;

		final JList resultSelectionList = new JList(RapidMinerGUI.getResultHistory());
		resultSelectionList.setBorder(null);
		resultSelectionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane listPane = new ExtendedJScrollPane(resultSelectionList);
		c.weighty = 1.0d;
		layout.setConstraints(listPane, c);
		processSelectionPanel.add(listPane);

		JPanel testPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		testPanel.setBorder(null);
		ANOVA_ACTION.setEnabled(false);
		JButton testButton = new JButton(ANOVA_ACTION);
		testPanel.add(testButton);
		c.weighty = 0.0d;
		layout.setConstraints(testPanel, c);
		processSelectionPanel.add(testPanel);

		JTabbedPane processTabbedPane = new ExtendedJTabbedPane();
		processTabbedPane.add("Process", processSelectionPanel);
		resultSelectionSplitPane.add(processTabbedPane);

		// operator xml text area
		JPanel xmlPanel = new JPanel();
		layout = new GridBagLayout();
		c = new GridBagConstraints();
		xmlPanel.setLayout(layout);
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.0d;
		c.weightx = 1.0d;
		c.gridwidth = GridBagConstraints.REMAINDER;

		final XMLEditor xmlArea = new XMLEditor();
		xmlArea.setEditable(false);
		c.weighty = 1.0d;
		layout.setConstraints(xmlArea, c);
		xmlPanel.add(xmlArea);

		JTabbedPane xmlTabbedPane = new ExtendedJTabbedPane();
		xmlTabbedPane.add("XML Setup", xmlPanel);
		resultSelectionSplitPane.add(xmlTabbedPane);

		// main split pane (left: result selection and tree, right: results)
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		mainSplitPane.setBorder(null);
		mainSplitPane.add(resultSelectionSplitPane);

		// result text area
		JPanel resultsPanel = new JPanel();
		resultsPanel.setBorder(null);
		layout = new GridBagLayout();
		c = new GridBagConstraints();
		resultsPanel.setLayout(layout);
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.0d;
		c.weightx = 1.0d;
		c.gridwidth = GridBagConstraints.REMAINDER;

		final JTextArea resultsArea = new JTextArea();
		resultsArea.setBorder(null);
		resultsArea.setEditable(false);
		resultsArea.setLineWrap(true);
		resultsArea.setWrapStyleWord(true);
		JScrollPane resultsPane = new ExtendedJScrollPane(resultsArea);
		c.weighty = 1.0d;
		layout.setConstraints(resultsPane, c);
		resultsPanel.add(resultsPane);

		JTabbedPane resultsTabbedPane = new ExtendedJTabbedPane();
		resultsTabbedPane.add("Results", resultsPanel);
		mainSplitPane.add(resultsTabbedPane);

		// add listener to list
		resultSelectionList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int[] indices = resultSelectionList.getSelectedIndices();
					if (indices.length > 0) {
						if (indices.length == 1) {
							ResultContainer selectedContainer = (ResultContainer)resultSelectionList.getSelectedValue();
							xmlArea.setText(selectedContainer.getProcess());
							resultsArea.setText(selectedContainer.getResults());
							ANOVA_ACTION.setEnabled(false);
						} else {
							xmlArea.setText("");
							resultsArea.setText("");
							selectedTestGroups.clear();
							for (int i = 0; i < indices.length; i++) {
								ResultContainer selectedContainer = (ResultContainer)RapidMinerGUI.getResultHistory().getElementAt(indices[i]);
								TestGroup group = selectedContainer.getTestGroup();
								if (group != null)
									selectedTestGroups.add(group);
							}
							if (indices.length == selectedTestGroups.size())
								ANOVA_ACTION.setEnabled(true);
							else
								ANOVA_ACTION.setEnabled(false);
						}
					} else {
						xmlArea.setText("");
						resultsArea.setText("");
						ANOVA_ACTION.setEnabled(false);
					}
				}
			}
		});

		mainSplitPane.setBorder(BorderFactory.createEtchedBorder());
		getContentPane().add(mainSplitPane, BorderLayout.CENTER);

		// button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPanel.add(closeButton);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		setSize(640, 480);
		resultSelectionSplitPane.setDividerLocation(150);
		mainSplitPane.setDividerLocation(200);
		setLocationRelativeTo(owner);
	}
}
