package com.rapidminer.operator.preprocessing.deprecated;

/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2009 by Rapid-I and the contributors
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Partition;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.container.Tupel;
import com.rapidminer.tools.math.container.GeometricDataCollection;
import com.rapidminer.tools.math.container.LinearList;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;

/**
 * This is the Numerical2Date tutorial operator.
 * 
 * @author Marcelo Beckmann
 */

public class AgglomerativeKNNReduction extends Operator {

	/** The parameter name for &quot;The used number of nearest neighbors.&quot; */
	public static final String PARAMETER_K = "k";

	public static final String PARAMETER_CUT_POINT_PERCENT = "cut_point_percent";

	/** The parameter name for &quot;Indicates if the votes should be weighted by similarity.&quot; */
	public static final String PARAMETER_WEIGHTED_VOTE = "weighted_vote";
	private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);

	private InputPort exampleSetInput = getInputPorts().createPort("example set");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");
	private Attribute id;
	private int k;
	private GeometricDataCollection<Integer> samples;

	/**
	 * Constructor
	 */
	public AgglomerativeKNNReduction(OperatorDescription description) {
		super(description);

		// exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, new String[] { "relative time" }, Ontology.ATTRIBUTE_VALUE));

		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput, exampleSetOutput, SetRelation.EQUAL) {

			@Override
			public ExampleSetMetaData modifyExampleSet(ExampleSetMetaData metaData) throws UndefinedParameterError {

				return metaData;
			}
		});
	}

	private Attribute idAttribute;

	@Override
	public void doWork() throws OperatorException {
		// todo change this to the parametrized one
		// DistanceMeasure df= new EuclideanDistance();
		ExampleSet inputSet = exampleSetInput.getData();

		Attributes atts = inputSet.getAttributes();
		id = atts.get("id");

		if (id == null)
			throw new OperatorException("No id atribute is present");

		k = getParameterAsInt(PARAMETER_K);
		double cutPointPercent = getParameterAsDouble(PARAMETER_CUT_POINT_PERCENT);
		List<Example> selected = new ArrayList<Example>();

		Map<String, ExampleSet> examplesByLabel = separeByLabelValue(inputSet);

		double cutPoint = 0;
		for (String labelValue : examplesByLabel.keySet()) {

			cutPoint = Math.max(cutPoint, examplesByLabel.get(labelValue).size() * cutPointPercent / 100.0);
		}

		try {
			for (String labelValue : examplesByLabel.keySet()) {
				ExampleSet examples = examplesByLabel.get(labelValue);
				samples = ObtainNeighbors(examples);
				List<ExampleDistanceData> distances = performDistanceCalculationByExample(examples);
				Collections.sort(distances);
				for (int i = 0; i < (int) Math.min(30, distances.size()); i++) {

					System.out.println("### id:" + ((int) distances.get(i).example.getValue(id)) + ". distance: " + i + ", td :"
							+ distances.get(i).totalDistance + ", avg:" + distances.get(i).avg);

				}
				ExampleDistanceData edd = null;
				for (int i = 0; i < distances.size(); ++i) {
					edd = distances.get(i);
					if (i > cutPoint)
						break;
					selected.add(edd.example);
				}
				// System.out.println("#### CUTPOINT SELECTED:"+
				// selected.size() +", distances: " + edd);
				// Remove the empty columns
			}
			// BE TIDY

			samples = null;
			examplesByLabel.clear();
			examplesByLabel = null;
			System.gc();
			Thread.currentThread().join(1000);

			exampleSetOutput.deliver(createCleanExampleSet(inputSet, selected));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class ExampleDistanceData implements Comparable {
		public static final int SORT_BY_TOTALDISTANCE = 1;
		public static final int SORT_BY_AVG = 2;
		public int sortType = SORT_BY_AVG;
		Example example;
		double distances[];
		double totalDistance;
		double avg;

		ExampleDistanceData(Example example, double distances[]) {
			this.example = example;
			this.distances = distances;
			for (double d : distances) {
				totalDistance += d;

			}
			avg = totalDistance / (double) distances.length;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();

			for (double d : distances) {
				sb.append(d);
				sb.append(',');
			}

			sb.append(" total:");
			sb.append(totalDistance);
			return sb.toString();

		}

		@Override
		public int compareTo(Object o) {

			if (!(o instanceof ExampleDistanceData)) {
				return 0;
			}
			ExampleDistanceData other = (ExampleDistanceData) o;
			int result = 0;
			if (sortType == SORT_BY_TOTALDISTANCE) {

				if (totalDistance == other.totalDistance)
					result = 0;
				if (totalDistance < other.totalDistance)
					return -1;
				else

					result = 1;

			} else {
				if (avg == other.avg)
					result = 0;
				if (avg < other.avg)
					result = -1;
				else
					result = 1;

			}
			return result;
		}
	}

	private Map<String, ExampleSet> separeByLabelValue(ExampleSet inputSet) throws OperatorException {
		Attribute label = inputSet.getAttributes().getLabel();
		Map<String, ExampleSet> newSets = new HashMap<String, ExampleSet>();
		SplittedExampleSet e = SplittedExampleSet.splitByAttribute(inputSet, label);

		for (int j = 0; j < label.getMapping().size(); j++) {
			SplittedExampleSet b = (SplittedExampleSet) e.clone();
			b.selectSingleSubset(j);
			String key = label.getMapping().mapIndex(j);
			newSets.put(key, b);
		}

		return newSets;
	}

	static int firstCount = 0;

	// From KNNLearnar
	public GeometricDataCollection ObtainNeighbors(ExampleSet exampleSet) throws Exception {

		DistanceMeasure measure = measureHelper.getInitializedMeasure(exampleSet);
		Attribute label = exampleSet.getAttributes().getLabel();
		if (label.isNominal()) {
			// classification
			GeometricDataCollection<Integer> samples = new LinearList<Integer>(measure);

			Attributes attributes = exampleSet.getAttributes();

			int valuesSize = attributes.size();
			for (Example example : exampleSet) {
				// if (firstCount++<10)
				// System.out.println("############" + example.getClass().getCanonicalName());
				double[] values = new double[valuesSize];
				int i = 0;
				for (Attribute attribute : attributes) {
					values[i] = example.getValue(attribute);
					i++;
				}
				int labelValue = (int) example.getValue(label);
				samples.add(values, labelValue);
				checkForStop();
			}
			return samples;
			// return new KNNClassificationModel(exampleSet, samples,k, getParameterAsBoolean(PARAMETER_WEIGHTED_VOTE));

		}
		return null;
	}

	// From KNNClassificationModel
	public List<ExampleDistanceData> performDistanceCalculationByExample(ExampleSet exampleSet) throws OperatorException {

		List<ExampleDistanceData> distances = new ArrayList<ExampleDistanceData>();
		System.out.println("### exampleset size:" + exampleSet.size());
		Attributes sampleAttributeNames = exampleSet.getAttributes();

		// building attribute order from trainingset
		ArrayList<Attribute> sampleAttributes = new ArrayList<Attribute>(sampleAttributeNames.size());
		Attributes attributes = exampleSet.getAttributes();
		for (Attribute attribute : sampleAttributeNames) {
			String attribteName = attribute.getName();
			sampleAttributes.add(attributes.get(attribteName));
		}

		double[] values = new double[sampleAttributes.size()];
		int c = 0;
		for (Example example : exampleSet) {
			// reading values
			int i = 0;
			for (Attribute attribute : sampleAttributes) {
				values[i] = example.getValue(attribute);
				i++;
			}

			// counting frequency of labels
			// double[] counter = new double[predictedLabel.getMapping().size()];
			// double totalDistance = 0;
			// finding next k neighbours and their distances
			// msx
			Collection<Tupel<Double, Integer>> neighbours = samples.getNearestValueDistances(k, values);

			double d[] = new double[neighbours.size()];
			i = 0;
			for (Tupel<Double, Integer> tupel : neighbours) {
				d[i] = tupel.getFirst();
				i++;
			}

			// TODO PRINT AND ASSERT MANUALLY THE DISTANCES
			ExampleDistanceData data = new ExampleDistanceData(example, d);

			// System.out.println(data);
			distances.add(data);

			// if (c%50==0)
			// ShowMemUsage(304);

			c++;
		}

		return distances;

	}

	/*
	 * private boolean exampleEquals(Example e1, Example e2) { return e1.getValue(id)==e2.getValue(id);
	 * 
	 * } private boolean find(Example ex, List <Example>data ) {
	 * 
	 * //TODO PUT A MAP INSTEAD for (Example example: data) { if (exampleEquals(example,ex) ) return true;
	 * 
	 * } return false;
	 * 
	 * }
	 */
	private ExampleSet createCleanExampleSet(ExampleSet oldExampleSet, List<Example> data) throws OperatorException {
		Map<Integer, Example> map = new HashMap<Integer, Example>();
		for (Example example : data) {
			Integer idKey = new Integer((int) example.getValue(id));
			map.put(idKey, example);
		}

		int partition[] = new int[oldExampleSet.size()];
		int i = 0;
		int ct = 0;
		for (Example example : oldExampleSet) {
			Integer idKey = new Integer((int) example.getValue(id));

			if (map.containsKey(idKey)) {
				partition[i] = 1;
				ct++;
			}

			i++;
		}

		SplittedExampleSet result = new SplittedExampleSet(oldExampleSet, new Partition(partition, 2));
		result.selectSingleSubset(1);
		System.out.println("old:" + oldExampleSet.size() + "result:" + result.size() + "/data:" + data.size() + "/ct:" + ct);
		return result;
	}

	private ExampleSet createEmptyExampleset(ExampleSet oldExampleSet) throws OperatorException {
		Attributes oldAttributes = oldExampleSet.getAttributes();
		List<Attribute> attributes = new ArrayList<Attribute>();

		Attribute label = oldAttributes.getLabel();
		attributes.add(label);
		for (Attribute att : oldAttributes) {
			attributes.add(att);

		}
		MemoryExampleTable table = new MemoryExampleTable(attributes);
		ExampleSet exampleSet = table.createExampleSet();
		
		exampleSet.getAttributes().setLabel(label);
		return exampleSet;

	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		ParameterType type = new ParameterTypeInt(PARAMETER_K, "The used number of nearest neighbors.", 1, Integer.MAX_VALUE, 1);
		type.setExpert(false);
		types.add(type);

		types.add(new ParameterTypeBoolean(PARAMETER_WEIGHTED_VOTE, "Indicates if the votes should be weighted by similarity.", false, false));

		type = new ParameterTypeInt(PARAMETER_CUT_POINT_PERCENT, "The threshold percent to cut off the more dense examples", 1, Integer.MAX_VALUE, 10);
		type.setExpert(false);
		types.add(type);

		types.addAll(DistanceMeasures.getParameterTypes(this));

		return types;
	}

}
