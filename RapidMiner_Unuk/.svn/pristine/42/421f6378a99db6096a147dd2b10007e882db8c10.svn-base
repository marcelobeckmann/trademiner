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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.MissingIOObjectException;

/**
* Tests for the data of several ExampleSets
*
* @author Christian Lohmann
*/
public class ExampleSetsDataSampleTest extends OperatorDataSampleTest {
	
	private String attributeName;
	private double[][] values;
	private String[][] stringValues;
	private boolean isDouble = false;

	public ExampleSetsDataSampleTest(String file, String attributeName, double[][] values) {
		super(file);
		this.attributeName = attributeName;
		this.values = values;
		this.isDouble  = true;
	}
	public ExampleSetsDataSampleTest(String file, String attributeName, String[][] stringValues) {
		super(file);
		this.attributeName = attributeName;
		this.stringValues = stringValues;
	}
	
	@Override
	public void checkOutput(IOContainer output) throws MissingIOObjectException {
		int no = output.size();
		ExampleSet[] examples = new ExampleSet[no];
		for(int i=0; i < no; i++) {
			examples[i] = output.get(ExampleSet.class, i);
		}
		Attribute attribute = examples[0].getAttributes().get(attributeName);
		assertNotNull(attribute);
		if (isDouble) {
			double value = 0;
			for (int i=0; i < examples.length; i++) {
				for(int j=0; j < values.length; j++) {
					value = examples[i].getExample(j).getValue(attribute);
					assertEquals(value, values[i][j]);
				}			
			}
		}
		else {
			String value = "";
			for (int i=0; i < examples.length; i++) {
				for(int j=0; j < stringValues[i].length; j++) {
					value = examples[i].getExample(j).getValueAsString(attribute);
					assertEquals(value, stringValues[i][j]);
				}	
			}
		}	 
	}
}
