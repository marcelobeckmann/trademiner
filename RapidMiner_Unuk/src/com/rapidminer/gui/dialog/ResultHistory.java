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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Operator;

/**
 * This class holds the information about all performed processes and results.
 * 
 * @author Ingo Mierswa
 */
public class ResultHistory extends AbstractListModel {

	private static final long serialVersionUID = -3346913516615767439L;
	
	private final List<ResultContainer> allResults = new LinkedList<ResultContainer>();
	
	public ResultHistory() {}
	
	public void addResults(String name, Operator root, IOContainer results) {
		allResults.add(new ResultContainer(name, root.getXML(true), results));
		fireIntervalAdded(this, allResults.size() - 1, allResults.size() - 1);
	}
	
	public Iterator<ResultContainer> getResults() {
		return allResults.iterator();
	}
	
	public Object getElementAt(int index) {
		return allResults.get(index);
	}

	public int getSize() {
		return allResults.size();
	}
}
