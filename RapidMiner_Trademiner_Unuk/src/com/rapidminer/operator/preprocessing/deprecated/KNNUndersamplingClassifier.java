package com.rapidminer.operator.preprocessing.deprecated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.table.PolynominalMapping;
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
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.container.Tupel;
import com.rapidminer.tools.math.container.GeometricDataCollection;
import com.rapidminer.tools.math.container.LinearList;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;

/**
 * KNN Undersampling implementation.
 * 
 * @author Marcelo Beckmann
 */

public class KNNUndersamplingClassifier extends Operator {

	public static final String PARAMETER_MAJORITYT_LABEL = "majority_label";
	public static final String PARAMETER_K = "k";
	public static final String PARAMETER_THRESHOLD = "threshold";
	public static final String PARAMETER_USE_DISTANCE_WEIGHT = "use_distance_weighting";

	
	private InputPort exampleSetInput = getInputPorts().createPort("test set");
	private InputPort exampleSetInputTrain = getInputPorts().createPort(
			"training set");

	private OutputPort exampleSetOutputTest = getOutputPorts().createPort(
			"test output");

	private OutputPort exampleSetOutputTrain = getOutputPorts().createPort(
			"train output");

	private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(
			this);
	private GeometricDataCollection<Integer> knn = null;
	private int majorityClassIndex;
	private List<Attribute> newAttributes;
	private boolean useDistanceWeight	;
	
	/**
	 * Constructor
	 */
	public KNNUndersamplingClassifier(OperatorDescription description) {
		super(description);

		getTransformer().addRule(
				new ExampleSetPassThroughRule(exampleSetInput,
						exampleSetOutputTest, SetRelation.EQUAL) {

					@Override
					public ExampleSetMetaData modifyExampleSet(
							ExampleSetMetaData metaData)
							throws UndefinedParameterError {

						return metaData;
					}
				});
	}

	@Override
	public void doWork() throws OperatorException {

		ExampleSet test = exampleSetInput.getData();
		if (test.size() == 0) {
			throw new OperatorException("Error: testset is empty!");
		}

		ExampleSet train = exampleSetInputTrain.getData();
		if (train.size() == 0) {
			throw new OperatorException("Error: training set is empty!");
		}

		if (train.getAttributes().size() != test.getAttributes().size()) {
			String msg = "Error: Train and test have different sizes of attributes: "
					+ train.getAttributes().size()
					+ "/"
					+ test.getAttributes().size();
			System.out.println("###########W WARNING: " + msg);
			// throw new OperatorException(msg);
		}

		useDistanceWeight = getParameterAsBoolean(PARAMETER_USE_DISTANCE_WEIGHT);
		
		
		Attributes attributes = test.getAttributes();

		// Figure out the nominal index for the mapping
		Attribute labelAtt = attributes.getLabel();
		if (labelAtt == null) {
			throw new OperatorException(
					"Label attribute is not present. Use set role to define a label to exampleset.");
		}
		String majorityClasslabel = this
				.getParameterAsString(PARAMETER_MAJORITYT_LABEL);

		ExampleSet labelExampleSet = getExampleSetByLabelValue(train,
				majorityClasslabel);
		int k = getK(train);

		majorityClassIndex =labelAtt.getMapping().getIndex(majorityClasslabel);

		if (labelExampleSet == null) {
			throw new OperatorException("Label " + majorityClasslabel
					+ " does not exist in the exampleset.");
		}
		ExampleSet outputExampleSet;
		try {
			outputExampleSet = iterateInstances(train, test,
					majorityClasslabel, k);
		} catch (Exception e) {

			e.getCause().printStackTrace();
			throw new OperatorException(e.getMessage());
		}

		exampleSetOutputTest.deliver(outputExampleSet);
		
		ExampleSet exampleSetTrain;

		try {
			exampleSetTrain = iterateInstances(train, train,
					majorityClasslabel, k);
		} catch (Exception e) {
			// e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}

		exampleSetOutputTrain.deliver(exampleSetTrain);

		knn = null;

	}

	/** Transforms an Example in a double array */

	private double[] toDoubleArray(Example example) {
		Attributes attributes = example.getAttributes();

		double values[] = new double[attributes.size() + 1];
		int j = 0;
		for (Iterator<Attribute> i$ = attributes.iterator(); i$.hasNext();) {
			Attribute attribute = i$.next();

			values[j] = example.getValue(attribute);
			j++;
		}
		return values;
	}

	private ExampleSet getExampleSetByLabelValue(ExampleSet inputSet,
			String label) {
		Attribute labelAtt = inputSet.getAttributes().getLabel();
		SplittedExampleSet e = SplittedExampleSet.splitByAttribute(inputSet,
				labelAtt);

		int labelIndex = labelAtt.getMapping().getIndex(label);
		if (labelIndex == -1) {
			return null;
		}

		SplittedExampleSet b = (SplittedExampleSet) e.clone();
		b.selectSingleSubset(labelIndex);

		return b;
	}

	protected ExampleSet iterateInstances(ExampleSet train, ExampleSet test,
			String majorityClassLabel, int k) throws Exception {

		/*
		 * Compute k nearest neighbors for i, and save the indices in the
		 * nnarray
		 */
		int threshold = getParameterAsInt(PARAMETER_THRESHOLD);

		Attributes attributes = test.getAttributes();

		if (knn == null) {
			knn = obtainNeighbors(train);
		}
		ExampleSet newInputSet = createTrainingStructure(test);
		MemoryExampleTable table = (MemoryExampleTable) newInputSet
				.getExampleTable();

		for (int i = 0; i < test.size(); i++) {
			checkForStop();
			Example example = test.getExample(i);
			double values[] = toDoubleArray(example);
			Collection<Tupel<Double, Integer>> neighbours = knn
					.getNearestValueDistances(k, values);
			countDistances(neighbours, majorityClassIndex, threshold, example,
					table, newInputSet);
		}

		return newInputSet;
	}

	/**
	 * Create an empty structure, from a existing exampleset, this is the best
	 * example of structure copy
	 * 
	 * @param exampleSet
	 * @return
	 * @throws OpeOratorException
	 */

	protected ExampleSet createTrainingStructure(ExampleSet test) throws OperatorException {

		// create new attributes and table
		newAttributes = new ArrayList<Attribute>();
		Attribute id;
		Attribute label;
		
		newAttributes.add(id = AttributeFactory.createAttribute("id",
				Ontology.INTEGER));
		newAttributes.add(AttributeFactory.createAttribute("minCount",
				Ontology.INTEGER));
		newAttributes.add(AttributeFactory.createAttribute("majCount",
				Ontology.INTEGER));
		
/*
		newAttributes.add(AttributeFactory.createAttribute("distancesForMinority",
				Ontology.NUMERICAL));
		newAttributes.add(AttributeFactory.createAttribute("distancesForMajority",
				Ontology.NUMERICAL));
	
			
		newAttributes.add(AttributeFactory.createAttribute("avgDistForMinority",
				Ontology.NUMERICAL));
		newAttributes.add(AttributeFactory.createAttribute("avgDistForMajority",
				Ontology.NUMERICAL));
		*/
		
		newAttributes.add(label = AttributeFactory.createAttribute("label",
				Ontology.NOMINAL));


		//TODO REMOVE THIS IF FAIL
		/*Attributes atts = test.getAttributes();
		for (Attribute att:atts)
		{
			newAttributes.add(att);
		}*/
		//END REMOVE
		
		
		Map<Integer, String> map = new HashMap<Integer, String>();

		map.put(0, "0");
		map.put(1, "1");

		NominalMapping pmap = new PolynominalMapping(map);
		label.setMapping(pmap);

		// fill table with data
		MemoryExampleTable table = new MemoryExampleTable(newAttributes);

		ExampleSet exampleSet = table.createExampleSet();
		exampleSet.getAttributes().setId(id);
		exampleSet.getAttributes().setLabel(label);
		return exampleSet;

	}

	public double calculateMaxDistance(Collection<Tupel<Double, Integer>> neighbours)
	{
		double max=0;
		for (Tupel<Double,Integer> tupel:neighbours)
		{
			max = Math.max(max,tupel.getFirst());
			
		}
		
		return max;
	}

	public void addNewExample(MemoryExampleTable table, long id, int minCount,
			int majCount, int label,double distancesForMinority,double distancesForMajority,
			
			double avgForMinority,double avgForMajority,
			
			Example example) {
		// Int
		DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
		DataRow row = factory.create(newAttributes.size());
		row.set(newAttributes.get(0), id);
		row.set(newAttributes.get(1), minCount);
		row.set(newAttributes.get(2), majCount);
	
		//row.set(newAttributes.get(3), distancesForMinority);
		//row.set(newAttributes.get(4), distancesForMajority);
	

//		row.set(newAttributes.get(3), avgForMinority);
//		row.set(newAttributes.get(4), avgForMajority);
	
		row.set(newAttributes.get(3), label);
		
		

		//TODO REMOVE THIS IF FAIL
		/*Attributes atts = example.getAttributes();
		for (Attribute att:atts)
		{
			row.set(att,example.getValue(att));
		}*/
		//END REMOVE
		
		
		table.addDataRow(row);

	}

	/* Function to take a decision about remove or not the instance */
	protected void countDistances(
			Collection<Tupel<Double, Integer>> neighbours, int majorityClass,
			int threshold, Example example, MemoryExampleTable table,
			ExampleSet newExampleSet) {

		int numberFromMinorityClasses = 0;
		int numberFromMajorityClasses = 0;
		double distancesForMinority=0;
		double distancesForMajority=0;
		
		double avgForMinority=0;
		double avgForMajority=0;
		
		
		double maxDistance=0;
		if (useDistanceWeight) {
			maxDistance = calculateMaxDistance(neighbours);
		}
		
		
		int actualLabel = (int) example.getLabel();

		Iterator<Tupel<Double, Integer>> iterator = neighbours.iterator();
		while (iterator.hasNext()) {
			Tupel<Double, Integer> neighbor = iterator.next();
			Integer neighborLabel = neighbor.getSecond();
			if (useDistanceWeight) {
				double distance =neighbor.getFirst();
				double weight = 1.0/distance ;  ///maxDistance;
				if (neighborLabel != majorityClass) {
					numberFromMinorityClasses++;
					distancesForMinority+= weight;
					avgForMinority+=distance;
				}
				else
				{
					numberFromMajorityClasses++;
					distancesForMajority+= weight;
					avgForMajority+=distance;
				}
			} else
			{
				if (neighborLabel != majorityClass) {
					numberFromMinorityClasses++;
				}
				else
				{
					numberFromMajorityClasses++;
				}
				
			}

		}
		
		boolean removeIt=false;
		if (actualLabel == majorityClass)
		{
			removeIt = numberFromMinorityClasses >= threshold;
		
		}
		
		
		// Transfer the example and counting to the new exampleSet
		addNewExample(
				table,
				(int) example.getId(),
				numberFromMinorityClasses,
				numberFromMajorityClasses,
				(actualLabel == majorityClass && numberFromMinorityClasses >= threshold) ? 1
						: 0, distancesForMinority,
						distancesForMajority, avgForMinority/numberFromMinorityClasses, avgForMajority/numberFromMajorityClasses,example);
		// addNewExample(table,(int)example.getId(),
		// numberFromMinorityClasses,numberFromMajorityClasses, ( actualLabel ==
		// majorityClass && numberFromMinorityClasses >= threshold)?1:0,(
		// actualLabel == 0?0:1));

	}

	// From KNNLearnar
	private GeometricDataCollection obtainNeighbors(ExampleSet exampleSet)
			throws Exception {

		Attribute labelAtt = exampleSet.getAttributes().getLabel();
		Attributes attributes = exampleSet.getAttributes();

		DistanceMeasure measure = measureHelper
				.getInitializedMeasure(exampleSet);
		if (labelAtt.isNominal()) {
			// classification
			GeometricDataCollection<Integer> samples = new LinearList<Integer>(
					measure);

			int valuesSize = attributes.size();
			for (Example example : exampleSet) {
				// if (firstCount++<10)
				// System.out.println("############" +
				// example.getClass().getCanonicalName());
				double[] values = toDoubleArray(example);
				int labelValue = (int) example.getValue(labelAtt);
				samples.add(values, labelValue);
				checkForStop();
			}
			return samples;
			// return new KNNClassificationModel(exampleSet, samples,k,
			// getParameterAsBoolean(PARAMETER_WEIGHTED_VOTE));

		} else {
			throw new OperatorException("Nominal label is required");
		}
	}

	private int getK(ExampleSet inputSet) throws UndefinedParameterError {
		int k = getParameterAsInt(PARAMETER_K);

		if (k == -1) {
			k = (int) Math.round(Math.pow(inputSet.size(), 0.5));
		}

		// If even, get the nearest odd number
		if (k % 2 == 0) {
			k++;
		}
		return k;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		ParameterType type = new ParameterTypeString(PARAMETER_MAJORITYT_LABEL,
				"The class label that will be undersampled.", "", false);
		type.setExpert(false);
		types.add(type);

		types.add(new ParameterTypeInt(
				PARAMETER_K,
				"The number of k neighbors. It's suggested to use odd numbers, or -1 to k=sqrt(|N|).",
				-1, Integer.MAX_VALUE, 5, false));

		types.add(new ParameterTypeInt(
				PARAMETER_THRESHOLD,
				"The threshold number of k nearest instances of different classes to remove the majority instance.",
				0, Integer.MAX_VALUE, 1, false));

		types.add(new ParameterTypeBoolean(PARAMETER_USE_DISTANCE_WEIGHT, "Use the distance as a weight for the knn undersampling count.", true,false));
		
		
		types.addAll(DistanceMeasures.getParameterTypes(this));

		return types;
	}

}
