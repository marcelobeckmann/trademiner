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
import com.rapidminer.operator.learner.associations.AssociationRules;

/**
 * The test class for association rules.
 * 
 * @author Christian Lohmann
 */
public class AssociationRuleGeneratorDataSampleTest extends
		OperatorDataSampleTest {
	
	private double[] confidences;

	public AssociationRuleGeneratorDataSampleTest(String file, double[] confidences) {
		super(file);
		this.confidences = confidences;
	}

	/* (non-Javadoc)
	 * @see com.rapidminer.test.OperatorDataSampleTest#checkOutput(com.rapidminer.operator.IOContainer)
	 */
	@Override
	public void checkOutput(IOContainer output) throws MissingIOObjectException {
		AssociationRules rules = output.get(AssociationRules.class);
		int ruleindex = confidences.length;
		for(int i = 0; i < confidences.length; i++, ruleindex--) {
			assertEquals(rules.getRule(ruleindex-1).getConfidence(), confidences[i]);	
		}
		
	}

}
