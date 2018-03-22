package com.rapidminer.operator.preprocessing.transformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
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

public class KNNUndersamplingDistancesOutput extends Operator {

	public static final String PARAMETER_MAJORITYT_LABEL = "majority_label";
	public static final String PARAMETER_K = "k";
	public static final String PARAMETER_THRESHOLD = "threshold"; 
	public static final String PARAMETER_USE_DISTANCE_WEIGHT = "use_distance_weighting";	
	public static final String PARAMETER_OUTPUT_FILE = "output_file";
	
	
	private InputPort exampleSetInput= getInputPorts().createPort("test set");
	private InputPort exampleSetInputTrain= getInputPorts().createPort("training set");

	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");
	private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);
	private GeometricDataCollection<Integer> knn;
	
//	private Attribute labelAtt;
	private int majorityClassIndex;
	//private Attributes attributes;
	private boolean useDistanceWeight;	
	//private PrintWriter writer;
	private Attribute minCountAtt;
	private Attribute majCountAtt;
	private Attribute decisionAtt;
	private ExampleSet outputExampleSet;
	private List<Attribute> newAttributes;
	
	
	/**
	 * Constructor
	 */
	public KNNUndersamplingDistancesOutput(OperatorDescription description) {
		super(description);

		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput, exampleSetOutput, SetRelation.EQUAL) {

			@Override
			public ExampleSetMetaData modifyExampleSet(ExampleSetMetaData metaData) throws UndefinedParameterError {

				return metaData;
			}
		});
	}
	
	
	

	@Override
	public void doWork() throws OperatorException {

		ExampleSet inputSet = exampleSetInput.getData();
		if (inputSet.size()==0)
		{
			throw new OperatorException("Error: inputset is empty!");
		}
		
		
		ExampleSet train = exampleSetInputTrain.getData();
		if (train.size()==0)
		{
			throw new OperatorException("Error: train inputset is empty!");
		}
		
		
		useDistanceWeight = getParameterAsBoolean(PARAMETER_USE_DISTANCE_WEIGHT);
		Attributes attributes = inputSet.getAttributes();
		/*
		try {
		//----------------------------------------------------------
			writer = new PrintWriter(getParameterAsString(PARAMETER_OUTPUT_FILE));
			writer.println("id;min;maj;label");
		//----------------------------------------------------------
		} catch (IOException e)
		{
			throw new OperatorException(e.getMessage());
		}
		*/
		// Figure out the nominal index for the mapping
		Attribute labelAtt = attributes.getLabel();
		if (labelAtt == null) {
			throw new OperatorException("Label attribute is not present. Use set role to define a label to exampleset.");
		}
		String majorityClasslabel = this.getParameterAsString(PARAMETER_MAJORITYT_LABEL);
	
		
		ExampleSet labelExampleSet = getExampleSetByLabelValue(inputSet, majorityClasslabel);
		int k = getK(inputSet);		
		
		majorityClassIndex = labelAtt.getMapping().getIndex(majorityClasslabel);
		
		if (labelExampleSet == null) {
			throw new OperatorException("Label " + majorityClasslabel + " does not exist in the exampleset.");
		}
		
		try {
			outputExampleSet = iterateInstances(train,inputSet, majorityClasslabel, k);
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		
		/*finally {
			writer.flush();
			writer.close();
		}*/
		exampleSetOutput.deliver(outputExampleSet);
	}


	/** Transforms an Example in a double array */
	
	private double[] toDoubleArray(Example example,Attributes attributes, int extraSize)
	{
		double values[] = new double[attributes.size()+extraSize];
		int j=0;
		for (Iterator <Attribute>i$ = attributes.iterator(); i$.hasNext();) {
			Attribute attribute = i$.next();
			values[j] = example.getValue(attribute);
			j++;
		}
		return values;
	}
	
	
	private double[] toDoubleArrayAllAttributes(Example example,Attributes attributes)
	{
		Iterator <Attribute> itAttributes = attributes.allAttributes();
		double values[] = new double[attributes.allSize()];
		int j=0;
		while (itAttributes.hasNext()) {
			Attribute attribute = itAttributes.next();
			values[j] = example.getValue(attribute);
			j++;
		}
		return values;
	}


	private ExampleSet getExampleSetByLabelValue(ExampleSet inputSet, String label) {
		Attribute labelAtt = inputSet.getAttributes().getLabel();
		SplittedExampleSet e = SplittedExampleSet.splitByAttribute(inputSet, labelAtt);


		int labelIndex = labelAtt.getMapping().getIndex(label);
		if (labelIndex == -1) {
			return null;
		}

		SplittedExampleSet b = (SplittedExampleSet) e.clone();
		b.selectSingleSubset(labelIndex);

		return b;
	}

	
		
	protected ExampleSet iterateInstances(ExampleSet train , ExampleSet test, String majorityClassLabel, int k) throws Exception {

		/*
		 * Compute k nearest neighbors for i, and save the indices in the nnarray
		 */
		int threshold = getParameterAsInt(PARAMETER_THRESHOLD);
		knn = obtainNeighbors(train);
		
		ExampleSet newInputSet = createEmptyStructure(test);
		MemoryExampleTable table  = (MemoryExampleTable)newInputSet.getExampleTable();
		
		
		Attributes attributes = test.getAttributes();
		for (int i = 0; i < test.size(); i++) {
			checkForStop();
			Example example = test.getExample(i);
			double values[] = toDoubleArray(example, attributes,3);
			Collection<Tupel<Double, Integer>> neighbours = knn.getNearestValueDistances(k, values);
			countDistances(neighbours, majorityClassIndex, threshold, example,table,newInputSet);
		}
		
		return newInputSet;
	}

	/**
	 * Create an empty structure, from a existing exampleset, this is the best example of structure copy
	 * @param exampleSet
	 * @return
	 * @throws OpeOratorException
	 */
	
	protected ExampleSet createEmptyStructure(ExampleSet exampleSet) throws OperatorException {

		// create new attributes and table
		newAttributes = new ArrayList<Attribute>();
		Map<Attribute,String> specialAttributes = new HashMap<Attribute, String>();
		Iterator<AttributeRole> a = exampleSet.getAttributes().allAttributeRoles();
		while (a.hasNext()) {
			AttributeRole role = a.next();
			Attribute attribute = role.getAttribute();

			Attribute newAttribute = AttributeFactory.createAttribute(attribute.getName(), attribute.getValueType());
			//newAttribute.setName(attribute.getName());
			newAttributes.add(newAttribute);

			if (attribute.isNominal()) {
				newAttribute.setMapping(attribute.getMapping());
			}

			if (role.isSpecial()) {
				specialAttributes.put(newAttribute, role.getSpecialName());
			}
		}
		newAttributes.add(minCountAtt=AttributeFactory.createAttribute("minCount", Ontology.INTEGER));
		newAttributes.add(majCountAtt=AttributeFactory.createAttribute("majCount", Ontology.INTEGER));
		newAttributes.add(decisionAtt=AttributeFactory.createAttribute("decisionAtt", Ontology.INTEGER));
		
		// fill table with data
		MemoryExampleTable table = new MemoryExampleTable(newAttributes);

		return table.createExampleSet();
				

	}
	
	public void addNewExample(Example example,MemoryExampleTable table, int dataManagement ) { 
		DataRowFactory factory = new DataRowFactory(dataManagement, '.');
	
		Iterator<Attribute> i = example.getAttributes().allAttributes();
		int attributeCounter = 0;
		DataRow row = factory.create(newAttributes.size());
		while (i.hasNext()) {
			Attribute attribute = i.next();
			double value = example.getValue(attribute);
			Attribute newAttribute = newAttributes.get(attributeCounter); 
			if (attribute.isNominal()) {
				if (!Double.isNaN(value)) {
					String nominalValue = attribute.getMapping().mapIndex((int)value);
					value = newAttribute.getMapping().mapString(nominalValue);
				}
			}
			row.set(newAttribute, value);
			attributeCounter++;
		}
		table.addDataRow(row);
		
	}
	/* Function to take a decision about remove or not the instance */
	protected void countDistances(Collection<Tupel<Double, Integer>> neighbours, int majorityClass, int threshold, Example example, MemoryExampleTable table, ExampleSet newExampleSet) {

		int numberFromMinorityClasses = 0;
		int numberFromMajorityClasses = 0;
		
		double maxDistance=0;
		if (useDistanceWeight) {
			maxDistance = calculateMaxDistance(neighbours);
		}
		
		int actualLabel = (int)example.getLabel();

		Iterator <Tupel<Double, Integer>>iterator = neighbours.iterator();
		while (iterator.hasNext()) {
			Tupel<Double, Integer> neighbor =  iterator.next();
			Integer neighborLabel = neighbor.getSecond();
			
			
			if (useDistanceWeight) {
				double weight = neighbor.getFirst()/maxDistance;
				if (neighborLabel != majorityClass) {
					numberFromMinorityClasses+= weight;
				}
				else
				{
					numberFromMajorityClasses+= weight;
					
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
		
	
//FROM DECISION TREE			
/*
min <= 2: 0 (983.0)
min > 2
|   min <= 8: 1 (327.0/85.0)
|   min > 8: 0 (65.0/10.0)
*/					
			//writer.println((int) example.getId() +";"+ numberFromMinorityClasses+";"+numberFromMajorityClasses  +";" + ((actualLabel==majorityClass && (numberFromMinorityClasses >= threshold))?1:0));
			
			//Transfer the example and counting to the new exampleSet
			addNewExample(example, table, 1);
			Example newExample = newExampleSet.getExample(newExampleSet.size()-1);
			
			newExample.setValue(minCountAtt,numberFromMinorityClasses);
			newExample.setValue(majCountAtt,numberFromMajorityClasses);
			newExample.setValue(decisionAtt, (( (numberFromMinorityClasses >= threshold))?1:0));
			
			
	}

	// From KNNLearnar
	private GeometricDataCollection obtainNeighbors(ExampleSet exampleSet) throws Exception {

		Attribute labelAtt = exampleSet.getAttributes().getLabel();
		Attributes attributes = exampleSet.getAttributes();
		
		DistanceMeasure measure = measureHelper.getInitializedMeasure(exampleSet);
		if (labelAtt.isNominal()) {
			// classification
			GeometricDataCollection<Integer> samples = new LinearList<Integer>(measure);

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
				int labelValue = (int) example.getValue(labelAtt);
				samples.add(values, labelValue);
				checkForStop();
			}
			return samples;
			// return new KNNClassificationModel(exampleSet, samples,k, getParameterAsBoolean(PARAMETER_WEIGHTED_VOTE));

		}
		else
			throw new OperatorException("Nominal label is required");
		
	}
	
	
	private int getK(ExampleSet inputSet) throws UndefinedParameterError
	{
		int k = getParameterAsInt(PARAMETER_K);
		
		if (k==-1)
		{
			k =(int) Math.round( Math.pow(inputSet.size() ,0.5));
		}
		
		//If even, get the nearest odd number
		if (k%2==0) {
			k++;
		}
		return k;
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
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		ParameterType type = new ParameterTypeString(PARAMETER_MAJORITYT_LABEL, "The class label that will be undersampled.", "", false);
		type.setExpert(false);
		types.add(type);


		types.add(new ParameterTypeInt(PARAMETER_K, "The number of k neighbors. It's suggested to use odd numbers, or -1 to k=sqrt(|N|).", -1, Integer.MAX_VALUE, 5,false));


		
		types.add(new ParameterTypeInt(PARAMETER_THRESHOLD, "The threshold number of k nearest instances of different classes to remove the majority instance.", 0,
				Integer.MAX_VALUE, 1,false));
		
		types.addAll(DistanceMeasures.getParameterTypes(this));

		types.add(new ParameterTypeBoolean(PARAMETER_USE_DISTANCE_WEIGHT, "Use the distance as a weight for the knn undersampling count.", true,false));

	
		types.add(new ParameterTypeString(PARAMETER_OUTPUT_FILE, "Outputfile for count decision.", "",false));

		
		return types;
	}

}
