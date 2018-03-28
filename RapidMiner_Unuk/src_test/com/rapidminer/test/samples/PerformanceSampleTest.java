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
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;

/**
 * Performs the sample process and checks the output performance.
 * 
 * @author Ingo Mierswa
 *          ingomierswa Exp $
 */
public class PerformanceSampleTest extends SampleTest {

	private String[] criteriaNames;

	private double[] values;

	public PerformanceSampleTest(String file, String[] criteriaNames, double[] values) {
		super(file);
		this.criteriaNames = criteriaNames;
		this.values = values;
	}

	@Override
	public void checkOutput(IOContainer output) throws MissingIOObjectException {
		PerformanceVector performance = output.get(PerformanceVector.class);
		for (int i = 0; i < criteriaNames.length; i++) {
			PerformanceCriterion criterion = performance.getCriterion(criteriaNames[i]);
			assertEquals(criterion.getName(), values[i], criterion.getAverage(), 0.00001);
		}
	}
}
