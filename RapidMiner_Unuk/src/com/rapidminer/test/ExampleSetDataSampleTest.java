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
package com.rapidminer.test;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.MissingIOObjectException;

/**
 * Tests for the Data of an ExampleSet
 *
 * @author Marcin Skirzynski, Tobias Beckers, Christian Lohmann
 */
public class ExampleSetDataSampleTest extends OperatorDataSampleTest {
	
	private String attributeName;

	private double[] values;
	
	private String[] stringValues;
	
	private boolean isDouble;

	private int amount = -1;

	private boolean isAmount = false;

	public ExampleSetDataSampleTest(String file, String attributeName, double[] values) {
		super(file);
		this.attributeName = attributeName;
		this.values = values;
		this.isDouble = true;
	}
	
	public ExampleSetDataSampleTest(String file, String attributeName, String[] stringValues) {
		super(file);
		this.attributeName = attributeName;
		this.stringValues = stringValues;
		this.isDouble = false;
	}
	/**
	 * This constructor is used for checking whether an ExampleSet contains a specified amount of Examples
	 * @param file
	 * @param amount the specified amount of Examples to check in an ExampleSet
	 */
	public ExampleSetDataSampleTest(String file, int amount) {
		super(file);
		this.amount = amount;
		this.isAmount  = true;
	}
	
	@Override
	public void checkOutput(IOContainer output) throws MissingIOObjectException {
		ExampleSet exampleSet = output.get(ExampleSet.class);
		int counter = 0;
		if(isAmount) {
			assertEquals(amount, exampleSet.size());
		}
		else if (isDouble) {
			Attribute attribute = exampleSet.getAttributes().get(attributeName);
			assertNotNull(attribute);
			double value = 0;
			
			for (int i=0;i<values.length;i++) {
				value = exampleSet.getExample(i).getValue(attribute);
				assertEquals(value, values[counter++]);
			}
		}
		else {
			Attribute attribute = exampleSet.getAttributes().get(attributeName);
			assertNotNull(attribute);
			String value = "";
			
			for (int i=0;i<stringValues.length;i++) {
				value = exampleSet.getExample(i).getValueAsString(attribute);
				assertEquals(value, stringValues[counter++]);
			}
		}
	}
}
