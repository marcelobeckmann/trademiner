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
package com.rapidminer.tools.math.similarity.nominal;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.SimilarityMeasure;

/**
 * This is the abstract superclass for all nominal similarity measures.
 * 
 * @author Sebastian Land
 */
public abstract class AbstractNominalSimilarity extends SimilarityMeasure {

	private static final long serialVersionUID = 3932502337712338892L;

	private boolean[] binominal;

	private double[] falseIndex;
	
	@Override
	public double calculateDistance(double[] value1, double[] value2) {
		return -calculateSimilarity(value1, value2);
	}

	@Override
	public double calculateSimilarity(double[] value1, double[] value2) {
		int equalNonFalseValues = 0;
		int unequalValues = 0;
		int falseValues = 0;
		for (int i = 0; i < value1.length; i++) {
			if (value1[i] == value2[i])
				if (binominal[i]) {
					if (value1[i] == falseIndex[i])
						falseValues++;
					else
						equalNonFalseValues++;
				} else
					equalNonFalseValues++;
			else {
				unequalValues++;
			}
		}
		return calculateSimilarity(equalNonFalseValues, unequalValues, falseValues);
	}

	/**
	 * Calculate a similarity given the number of attributes for which both examples agree/disagree.
	 * 
	 * @param equalNonFalseValues
	 *            the number of attributes for which both examples are equal and non-zero
	 * @param unequalValues
	 *            the number of attributes for which both examples have unequal values
	 * @param falseValues
	 *            the number of attributes for which both examples have zero values
	 * @return the similarity
	 */
	protected abstract double calculateSimilarity(double equalNonFalseValues, double unequalValues, double falseValues);
	
	@Override
	public void init(ExampleSet exampleSet) throws OperatorException {
	    super.init(exampleSet);
	    Tools.onlyNominalAttributes(exampleSet, "nominal similarities");
		binominal = new boolean[exampleSet.getAttributes().size()];
		falseIndex = new double[exampleSet.getAttributes().size()];
		int index = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			binominal[index] = attribute.isNominal() && attribute.getMapping().size() == 2;
			if (binominal[index])
				falseIndex[index] = attribute.getMapping().getNegativeIndex();
			else
				falseIndex[index] = Double.NaN;
			index++;
		}
	}

}
