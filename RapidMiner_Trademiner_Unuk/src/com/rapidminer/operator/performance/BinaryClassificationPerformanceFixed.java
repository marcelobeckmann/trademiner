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
package com.rapidminer.operator.performance;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.math.Averagable;


/**
 * This class encapsulates the well known binary classification criteria
 * precision and recall. Furthermore it can be used to calculate the fallout,
 * the equally weighted f-measure (f1-measure), the lift, and the values for
 * TRUE_POSITIVE, FALSE_POSITIVE, TRUE_NEGATIVE, and FALSE_NEGATIVE. With
 * &quot;positive&quot; we refer to the first class and with
 * &quot;negative&quot; we refer to the second.
 * 
 * @author Ingo Mierswa, Simon Fischer
 */
public class BinaryClassificationPerformanceFixed extends BinaryClassificationPerformance {

	private static final long serialVersionUID = 7475134460409215015L;

	public static final int GMEAN=15;
	
	private static final int N = 0;

	private static final int P = 1;


	private int type = 0;

	/** true label, predicted label. PP = TP, PN = FN, NP = FP, NN = TN. */
	private double[][] counter = new double[2][2];

	/** Name of the positive class. */
	private String positiveClassName = "";

	/** Name of the negative class. */
	private String negativeClassName = "";

	/** The predicted label attribute. */
	private Attribute predictedLabelAttribute;

	/** The  label attribute. */
	private Attribute labelAttribute;

	/** The weight attribute. Might be null. */
	private Attribute weightAttribute;

	public BinaryClassificationPerformanceFixed() {
		type = -1;
	}

	public BinaryClassificationPerformanceFixed(BinaryClassificationPerformanceFixed o) {
		super(o);
		this.type = o.type;
		this.counter = new double[2][2];
		this.counter[N][N] = o.counter[N][N];
		this.counter[P][N] = o.counter[P][N];
		this.counter[N][P] = o.counter[N][P];
		this.counter[P][P] = o.counter[P][P];
		if (o.predictedLabelAttribute != null)
			this.predictedLabelAttribute = (Attribute)o.predictedLabelAttribute.clone();
		if (o.labelAttribute != null)
			this.labelAttribute = (Attribute)o.labelAttribute.clone();
		if (o.weightAttribute != null)
			this.weightAttribute = (Attribute)o.weightAttribute.clone();
		this.positiveClassName = o.positiveClassName;
		this.negativeClassName = o.negativeClassName;
	}

	public BinaryClassificationPerformanceFixed(int type) {
		this.type = type;
	}

	/** For test cases only. */
	public BinaryClassificationPerformanceFixed(int type, double[][] counter) {
		this.type = type;
		this.counter[N][N] = counter[N][N];
		this.counter[N][P] = counter[N][P];
		this.counter[P][N] = counter[P][N];
		this.counter[P][P] = counter[P][P];
	}

	public static BinaryClassificationPerformanceFixed newInstance(String name) {
		for (int i = 0; i < NAMES.length; i++) {
			if (NAMES[i].equals(name))
				return new BinaryClassificationPerformanceFixed(i);
		}
		return null;
	}

	@Override
	public double getExampleCount() {
		return counter[P][P] + counter[N][P] + counter[P][N] + counter[N][N];
	}

	// ================================================================================

	@Override
	public void startCounting(ExampleSet eSet, boolean useExampleWeights) throws OperatorException {
		super.startCounting(eSet, useExampleWeights);
		this.predictedLabelAttribute = eSet.getAttributes().getPredictedLabel();
		this.labelAttribute = eSet.getAttributes().getLabel();
		if (!labelAttribute.isNominal()) {
			throw new UserError(null, 120, labelAttribute.getName(), Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(labelAttribute.getValueType()), Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(Ontology.NOMINAL));
		}
		if (!predictedLabelAttribute.isNominal()) {
			throw new UserError(null, 120, predictedLabelAttribute.getName(), Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(predictedLabelAttribute.getValueType()), Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(Ontology.NOMINAL));
		}
		if (labelAttribute.getMapping().size() != 2) {
			throw new UserError(null, 118, new Object[] { "'" + labelAttribute.getName() + "'", Integer.valueOf(labelAttribute.getMapping().getValues().size()), "2 for calculation of '" + getName() + "'" });
		}
		if (predictedLabelAttribute.getMapping().size() != 2) {
			throw new UserError(null, 118, new Object[] { "'" + predictedLabelAttribute.getName() + "'", Integer.valueOf(predictedLabelAttribute.getMapping().getValues().size()), "2 for calculation of '" + getName() + "'" });
		}
		if (!labelAttribute.getMapping().equals(predictedLabelAttribute.getMapping())) {
			throw new UserError(null, 157);
		}
		
		this.negativeClassName = predictedLabelAttribute.getMapping().getNegativeString();
		this.positiveClassName = predictedLabelAttribute.getMapping().getPositiveString();
		if (useExampleWeights)
			this.weightAttribute = eSet.getAttributes().getWeight();
		this.counter = new double[2][2];
	}

	@Override
	public void countExample(Example example) {
		String labelString = example.getNominalValue(labelAttribute);
		int label = labelAttribute.getMapping().getIndex(labelString);
		String predString = example.getNominalValue(predictedLabelAttribute);
		int plabel = predictedLabelAttribute.getMapping().getIndex(predString);

		double weight = 1.0d;
		if (weightAttribute != null)
			weight = example.getValue(weightAttribute);
		counter[label][plabel] += weight;
	}

	@Override
	public double getMikroAverage() {
		double x = 0.0d, y = 0.0d;
		switch (type) {
		case PRECISION:
			x = counter[P][P];
			y = counter[P][P] + counter[N][P];
			break;
		case RECALL:
			x = counter[P][P];
			y = counter[P][P] + counter[P][N];
			break;
		case LIFT:
			x = counter[P][P] / (counter[P][P] + counter[P][N]);
			y = (counter[P][P] + counter[N][P]) / (counter[P][P] + counter[P][N] + counter[N][P] + counter[N][N]);
			break;
		case FALLOUT:
			x = counter[N][P];
			y = counter[N][P] + counter[N][N];
			break;

		case F_MEASURE:
	//		x = counter[P][P];
	//		x *= x;
	//		x *= 2;
	//		y = x + counter[P][P] * counter[P][N] + counter[P][P] * counter[N][P];
	//		break;
	//	case GMEAN:
			x = Math.sqrt(counter[N][N]/(counter[N][N]+counter[N][P])*counter[P][P]/(counter[P][P]+counter[P][N]));
			y=1;	
			break;
			
		case FALSE_NEGATIVE:
			x = counter[P][N];
			y = 1;
			break;
		case FALSE_POSITIVE:
			x = counter[N][P];
			y = 1;
			break;
		case TRUE_NEGATIVE:
			x = counter[N][N];
			y = 1;
			break;
		case TRUE_POSITIVE:
			x = counter[P][P];
			y = 1;
			break;
		case SENSITIVITY:
			x = counter[P][P];
			y = counter[P][P] + counter[P][N];
			break;
		case SPECIFICITY:
			x = counter[N][N];
			y = counter[N][N] + counter[N][P];
			break;
		case YOUDEN:
			x = counter[N][N] * counter[P][P] - counter[P][N] * counter[N][P];
			y = (counter[P][P] + counter[P][N]) * (counter[N][P] + counter[N][N]);
			break;
		case POSITIVE_PREDICTIVE_VALUE:
			x = counter[P][P];
			y = counter[P][P] + counter[N][P];
			break;
		case NEGATIVE_PREDICTIVE_VALUE:
			x = counter[N][N];
			y = counter[N][N] + counter[P][N];
			break;
		case PSEP:
			x = counter[N][N] * counter[P][P] + counter[N][N] * counter[N][P] - counter[N][P] * counter[N][N] - counter[N][P] * counter[P][N];
			y = counter[P][P] * counter[N][N] + counter[P][P] * counter[P][N] + counter[N][P] * counter[N][N] + counter[N][P] * counter[P][N];
			break;
		default:
			throw new RuntimeException("Illegal value for type in BinaryClassificationPerformance: " + type);
		}
		if (y == 0)
			return Double.NaN;
		return x / y;
	}

	@Override
	public double getFitness() {
		switch (type) {
		case PRECISION:
		case RECALL:
		case LIFT:
		case TRUE_POSITIVE:
		case TRUE_NEGATIVE:
		case F_MEASURE:
		case SENSITIVITY:
		case SPECIFICITY:
		case YOUDEN:
		case POSITIVE_PREDICTIVE_VALUE:
		case NEGATIVE_PREDICTIVE_VALUE:
		case GMEAN:	
		case PSEP:
			return getAverage();
		case FALLOUT:
		case FALSE_POSITIVE:
		case FALSE_NEGATIVE:
			if (getAverage() == 0.0d)
				return Double.POSITIVE_INFINITY;
			return 1.0d / getAverage();
		default:
			throw new RuntimeException("Illegal value for type in BinaryClassificationPerformance: " + type);
		}
	}

	@Override
	public double getMaxFitness() {
		switch (type) {
		case PRECISION:
		case RECALL:
		case F_MEASURE:
		case SENSITIVITY:
		case SPECIFICITY:
		case GMEAN:	
			return 1.0d;
		case LIFT:
		case TRUE_POSITIVE:
		case TRUE_NEGATIVE:
		case FALLOUT:
		case FALSE_POSITIVE:
		case FALSE_NEGATIVE:
		case YOUDEN:
		case POSITIVE_PREDICTIVE_VALUE:
		case NEGATIVE_PREDICTIVE_VALUE:
		case PSEP:
			return Double.POSITIVE_INFINITY;
		default:
			throw new RuntimeException("Illegal value for type in BinaryClassificationPerformance: " + type);
		}
	}

	@Override
	public double getMikroVariance() {
		return Double.NaN;
	}

	// ================================================================================

	@Override
	public String getName() {
		return NAMES[type];
	}

	@Override
	public String getDescription() {
		return DESCRIPTIONS[type];
	}

	@Override
	public boolean formatPercent() {
		switch (type) {
		case TRUE_POSITIVE:
		case TRUE_NEGATIVE:
		case FALSE_POSITIVE:
		case FALSE_NEGATIVE:
		case YOUDEN:
		case PSEP:
			return false;
		default:
			return true;
		}
	}

	@Override
	public void buildSingleAverage(Averagable performance) {
		BinaryClassificationPerformanceFixed other = (BinaryClassificationPerformanceFixed) performance;
		if (this.type != other.type)
			throw new RuntimeException("Cannot build average of different error types (" + NAMES[this.type] + "/" + NAMES[other.type] + ").");
		if (!this.positiveClassName.equals(other.positiveClassName))
		{
			positiveClassName= other.positiveClassName;
			
		}	//throw new RuntimeException("Cannot build average for different positive classes (" + this.positiveClassName + "/" + other.positiveClassName + ").");
		this.counter[N][N] += other.counter[N][N];
		this.counter[N][P] += other.counter[N][P];
		this.counter[P][N] += other.counter[P][N];
		this.counter[P][P] += other.counter[P][P];
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (positive class: " + positiveClassName + ")");
		result.append(Tools.getLineSeparator() + "ConfusionMatrix:" + Tools.getLineSeparator() + "True:");
		result.append("\t" + negativeClassName);
		result.append("\t" + positiveClassName);
		result.append(Tools.getLineSeparator() + negativeClassName + ":");
		result.append("\t" + Tools.formatIntegerIfPossible(counter[N][N]));
		result.append("\t" + Tools.formatIntegerIfPossible(counter[P][N]));
		result.append(Tools.getLineSeparator() + positiveClassName + ":");
		result.append("\t" + Tools.formatIntegerIfPossible(counter[N][P]));
		result.append("\t" + Tools.formatIntegerIfPossible(counter[P][P]));
		return result.toString();
	}

	public double[][] getCounter() {
		return counter;
	}

	public String getNegativeClassName() {
		return negativeClassName;
	}

	public String getPositiveClassName() {
		return positiveClassName;
	}

	public String getTitle() {
		return super.toString() + " (positive class: " + getPositiveClassName() + ")";
	}
}
