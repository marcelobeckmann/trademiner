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
 * Performs the sample process and checks the output example set.
 * 
 * @author Ingo Mierswa
 *          Exp $
 */
public class ExampleSetSampleTest extends SampleTest {

	private int numberOfExamples;

	private int numberOfAttributes;

	private String[] specialAttributes = null;

	public ExampleSetSampleTest(String file, int numberOfExamples, int numberOfAttributes) {
		this(file, numberOfExamples, numberOfAttributes, null);
	}

	public ExampleSetSampleTest(String file, int numberOfExamples, int numberOfAttributes, String[] specialAttributes) {
		super(file);
		this.numberOfExamples = numberOfExamples;
		this.numberOfAttributes = numberOfAttributes;
		this.specialAttributes = specialAttributes;
	}

	@Override
	public void checkOutput(IOContainer output) throws MissingIOObjectException {
		ExampleSet exampleSet = output.get(ExampleSet.class);
		assertEquals("ExampleSet #examples", numberOfExamples, exampleSet.size(), 0);
		assertEquals("ExampleSet #attributes", numberOfAttributes, exampleSet.getAttributes().size(), 0);
		if (specialAttributes != null) {
			for (int i = 0; i < specialAttributes.length; i++) {
				Attribute special = exampleSet.getAttributes().getSpecial(specialAttributes[i]);
				assertNotNull(specialAttributes[i], special);
			}
		}
	}
}
