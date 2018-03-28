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
package com.rapidminer.gui.new_plotter.engine.jfreechart.link_and_brush.listener;

import java.util.List;

import org.jfree.data.Range;

import com.rapidminer.tools.container.Pair;

/**
 * @author Nils Woehler
 * 
 */
public class LinkAndBrushSelection {

	public enum SelectionType {
		ZOOM_IN, ZOOM_OUT, RESTORE_AUTO_BOUNDS, SELECTION, RESTORE_SELECTION
	}

	private final List<Pair<Integer, Range>> domainAxisRanges;
	private final List<Pair<Integer, Range>> valueAxisRanges;
	private final SelectionType type;

	/**
	 * 
	 * @param type the zooming type
	 * @param domainAxisRanges a list of pairs with indices for domain axis and their zoomed ranges
	 * @param rangeAxisRanges a list of pairs with indices for range axis and their zoomed ranges
	 */
	public LinkAndBrushSelection(SelectionType type, List<Pair<Integer, Range>> domainAxisRanges, List<Pair<Integer, Range>> rangeAxisRanges) {
		if (domainAxisRanges == null || rangeAxisRanges == null) {
			throw new IllegalArgumentException("Null range axes are not allowed!");
		}
		this.type = type;
		this.domainAxisRanges = domainAxisRanges;
		this.valueAxisRanges = rangeAxisRanges;
	}

	/**
	 * @return the domainRanges
	 */
	public List<Pair<Integer, Range>> getDomainAxisRanges() {
		return domainAxisRanges;
	}

	/**
	 * @return the first new domain axis range. <code>null</code> if list is empty
	 */
	public Pair<Integer, Range> getDomainAxisRange() {
		if (domainAxisRanges.size() > 0) {
			return domainAxisRanges.get(0);
		}
		return null;
	}

	/**
	 * @return the type
	 */
	public SelectionType getType() {
		return type;
	}

	/**
	 * @return the valueRanges
	 */
	public List<Pair<Integer, Range>> getValueAxisRanges() {
		return valueAxisRanges;
	}

}
