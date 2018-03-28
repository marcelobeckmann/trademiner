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
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.learner.bayes.DistributionModel;
import com.rapidminer.tools.Ontology;


/**
 * Test for the data of a DistributionModel. Expects a double array of length i which
 * have to be the confidences of the first class for each of the i examples in the example set.
 *
 * @author Marcin Skirzynski, Tobias Malbrecht
 */
public class DistributionModelSampleDataTest extends OperatorDataSampleTest {
	
	private double[] expectedValue;
	
	public DistributionModelSampleDataTest(String file, double[] expectedValue) {
		super(file);
		this.expectedValue = expectedValue;
	}
	

	@Override
	public void checkOutput(IOContainer output) throws MissingIOObjectException {
		DistributionModel distributionModel = output.get(DistributionModel.class);
		ExampleSet exampleSet = output.get(ExampleSet.class);

		Attribute labelPrediction = AttributeFactory.createAttribute("labelPrediction", Ontology.NOMINAL);
		distributionModel.performPrediction(exampleSet, labelPrediction);
		String classValue = exampleSet.getAttributes().getLabel().getMapping().mapIndex(0);
		int counter = 0;		
		for (Example example : exampleSet) {
			assertEquals(example.getConfidence(classValue), expectedValue[counter++]);
		}
	}
}
