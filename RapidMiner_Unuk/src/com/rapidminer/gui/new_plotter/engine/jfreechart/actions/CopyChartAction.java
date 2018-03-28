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
package com.rapidminer.gui.new_plotter.engine.jfreechart.actions;

import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;

import org.jfree.chart.ChartTransferable;

import com.rapidminer.gui.new_plotter.engine.jfreechart.JFreeChartPlotEngine;
import com.rapidminer.gui.tools.ResourceAction;

/**
 * This action allows the user to copy the current chart to the system clipboard.
 * 
 * @author Marco Boeck
 *
 */
public class CopyChartAction extends ResourceAction {
	
	/** the {@link JFreeChartPlotEngine} instance for this action */
	private JFreeChartPlotEngine engine;

	
	private static final long serialVersionUID = 7788302558857099622L;
	
	
	/**
	 * Creates a new {@link CopyChartAction}.
	 * @param engine
	 */
	public CopyChartAction(JFreeChartPlotEngine engine) {
		super(true, "plotter.popup_menu.copy");
		this.engine = engine;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		copyChart(engine);
	}
	
	/**
	 * Copies the current chart to the system clipboard.
	 */
	public static synchronized void copyChart(final JFreeChartPlotEngine engine) {
		Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Insets insets = engine.getChartPanel().getInsets();
		int w = engine.getChartPanel().getWidth() - insets.left - insets.right;
		int h = engine.getChartPanel().getHeight() - insets.top - insets.bottom;
		ChartTransferable selection = new ChartTransferable(engine.getChartPanel().getChart(), w, h);
		systemClipboard.setContents(selection, null);
	}

}
