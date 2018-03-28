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
package com.rapidminer.test.samples;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.learner.associations.FrequentItemSets;
import com.rapidminer.operator.learner.associations.Item;

/**
 * This class tests the Items of FrequentItemSets
 * @author Christian Lohmann
 *
 */
public class FrequentItemSetsDataSampleTest extends OperatorDataSampleTest {
	
	private String[] values;
	private int item;

	/**
	 * Gets as additional input the number of the tested item of a FrequentItemSet 
	 * @param item The number of the item which will be tested
	 */
	public FrequentItemSetsDataSampleTest(String file,int item, String[] values) {
		super(file);
		this.item = item;
		this.values = values;
	}

	/* (non-Javadoc)
	 * @see com.rapidminer.test.OperatorDataSampleTest#checkOutput(com.rapidminer.operator.IOContainer)
	 */
	@Override
	public void checkOutput(IOContainer output) throws MissingIOObjectException {
		FrequentItemSets sets = output.get(FrequentItemSets.class);
		for(int i=0; i < values.length; i++) {
			Item it = sets.getItemSet(i).getItem(item);
			assertEquals(it.toString(), values[i++]);	
		}
	}
}
