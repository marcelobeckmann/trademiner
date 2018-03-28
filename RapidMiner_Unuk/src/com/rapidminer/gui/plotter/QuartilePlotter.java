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
package com.rapidminer.gui.plotter;

import javax.swing.Icon;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.tools.math.MathFunctions;

/**
 * This plotter can be used to create quartile plots for several columns of the data table. 
 * 
 * The mean value of the data is shown as the black point, while the median (the lower median on a pair number of
 * examples) is shown as a horizontal line in the box. The vertical line crossing the mean value's point is the standard
 * deviation. The box marks the two center quartiles from 25% to 75%. The whiskers show the 5% and 95% percentiles.
 * Circles beyond the whiskers mark outliers.
 * 
 *  @author Ingo Mierswa
 */
public class QuartilePlotter extends ColorQuartilePlotter {

    private static final long serialVersionUID = -5115095967846809152L;
    
    private boolean[] columns = null;
    
    
    public QuartilePlotter(PlotterConfigurationModel settings) {
        super(settings);
    }
    
    public QuartilePlotter(PlotterConfigurationModel settings, DataTable dataTable) {
    	super(settings, dataTable);
    }
    
    @Override
	public void setDataTable(DataTable dataTable) {
        this.columns = new boolean[dataTable.getNumberOfColumns()];
        super.setDataTable(dataTable);
    }
    
    @Override
	public int getNumberOfAxes() {
        return 0;
    }
    
    @Override
	public String getPlotName() {
        return "Dimensions";
    }
    
	@Override
	public int getValuePlotSelectionType() {
		return MULTIPLE_SELECTION;
	}
	
	@Override
	public String getPlotterName() {
		return PlotterConfigurationModel.QUARTILE_PLOT;
	}
	
    @Override
	public void setPlotColumn(int index, boolean plot) {
    	columns[index] = plot;
        repaint();
    }

    @Override
	public boolean getPlotColumn(int index) {
        return columns[index];
    }
    
	@Override
	public Icon getIcon(int index) {
		return null;
	}
	
    @Override
	protected void prepareData() {
        allQuartiles.clear();
        this.globalMin = Double.POSITIVE_INFINITY;
        this.globalMax = Double.NEGATIVE_INFINITY;
        
        if (columns != null) {
        	int totalCount = 0;
        	for (int i = 0; i < this.dataTable.getNumberOfColumns(); i++) {
        		if (columns[i]) {
        			totalCount++;
        		}
        	}
        	
        	for (int i = 0; i < this.dataTable.getNumberOfColumns(); i++) {
        		if (columns[i]) {
                    Quartile quartile = Quartile.calculateQuartile(this.dataTable, i); 
                    quartile.setColor(getColorProvider().getPointColor((double) i / (double) totalCount));
                    allQuartiles.add(quartile); 
                    this.globalMin = MathFunctions.robustMin(this.globalMin, quartile.getMin());
                    this.globalMax = MathFunctions.robustMax(this.globalMax, quartile.getMax());
        		}
        	}
        }
    }
}
