package com.rapidminer.operator.preprocessing.deprecated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Partition;
import com.rapidminer.example.set.SplittedExampleSet;
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

public class KNNUndersamplingNoLabel extends Operator {

	public static final String PARAMETER_MAJORITYT_LABEL = "majority_label";
	public static final String PARAMETER_K = "k";
	public static final String PARAMETER_THRESHOLD = "threshold";
	public static final String PARAMETER_USE_0T3 = "use 0<t<3 threshold";

	public static final String PARAMETER_LOG_VERBOSITY = "log_verbosity";
	public static final String PARAMETER_USE_DISTANCE_WEIGHT = "use_distance_weighting";

	
	private InputPort exampleSetInput= getInputPorts().createPort("example set");
	private InputPort exampleSetInputTrain = getInputPorts().createPort("training set");

	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");
	private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);
	private GeometricDataCollection<Integer> knn;
	
	private int majorityClassIndex;
	private int k;
	private int logVerbosity=0;
	private boolean useDistanceWeight;
	private boolean use0t3=false;
	
	
	/**
	 * Constructor
	 */
	public KNNUndersamplingNoLabel(OperatorDescription description) {
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
		ExampleSet test = exampleSetInput.getData();
		ExampleSet training= exampleSetInputTrain.getData();
		System.out.println("!!!### removing 0 instances only");
		
		logVerbosity = getParameterAsInt(PARAMETER_LOG_VERBOSITY);
		useDistanceWeight = getParameterAsBoolean(PARAMETER_USE_DISTANCE_WEIGHT);
		use0t3= getParameterAsBoolean(PARAMETER_USE_0T3);
		
		
		if (test.size()==0 || training.size()==0)
		{
			throw new OperatorException("Error: inputset is empty!");
		}

		Attributes attributes = training.getAttributes();
	
		//Figure out the nominal index for the mapping
		Attribute labelAtt = attributes.getLabel();
		if (labelAtt == null) {
			throw new OperatorException("Label attribute is not present. Use set role to define a label to exampleset.");
		}

		k = getK(training);
		
		List<Integer> toBeRemoved=null;

		try {
			String majorityClasslabel = this.getParameterAsString(PARAMETER_MAJORITYT_LABEL);
			knn = obtainNeighbors(training);
			majorityClassIndex = labelAtt.getMapping().getIndex(majorityClasslabel);
			toBeRemoved = obtainInstancesToRemove(test, majorityClasslabel, k);
		} 
		catch (Exception e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		if (logVerbosity==1) {
			System.out.println("############### total size: " + test.size()+ ", to be removed: " + toBeRemoved.size());
		}
		exampleSetOutput.deliver(createCleanExampleSet(test,toBeRemoved));
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
	
	/** Transforms an Example in a double array */
	
	private double[] toDoubleArray(Example example,Attributes attributes)
	{	
		double values[] = new double[attributes.size()+1];
		int j=0;
		for (Iterator <Attribute>i$ = attributes.iterator(); i$.hasNext();) {
			Attribute attribute = i$.next();
			values[j] = example.getValue(attribute);
			j++;
		}
		return values;
	}
	
	protected List<Integer> obtainInstancesToRemove(ExampleSet test, String majorityClassLabel, int k) throws Exception {
			
		// Instances for synthetic samples
		List<Integer> toRemove = new ArrayList();
		/*
		 * Compute k nearest neighbors for i, and save the indices in the nnarray
		 */
		int threshold = getParameterAsInt(PARAMETER_THRESHOLD);
		System.out.println("####### k = "+ k +", t=" + threshold);
		Attributes attributes = test.getAttributes();
		Attribute labelAtt = attributes.getLabel();
		
		for (int i = 0; i < test.size(); i++) {
			checkForStop();
			Example example = test.getExample(i);
			///All the records pass trough the decide to remove (this class), in a opposite way, for the supervised KNN, only the ones from majority goes throug.  
			double values[] = toDoubleArray(example, attributes);
			Collection<Tupel<Double, Integer>> neighbours = knn.getNearestValueDistances(k, values);
			
			if (decideToRemove(neighbours, majorityClassIndex, threshold, example,i)) {
				toRemove.add(i);
			}
		}
		//	System.out.println("### knn und exited NORMALLY, best k=" + k+ ", to be removed: "+toRemove.size()+" , remaining : " + (test.size()-toRemove.size()) );
		
		return toRemove;
	}

	private ExampleSet createCleanExampleSet(ExampleSet oldExampleSet, List<Integer> toRemove) throws OperatorException {
		
		int partition[] = new int[oldExampleSet.size()];
		int i = 0;
		int ct = 0;
		for (Example example : oldExampleSet) {
			if (!toRemove.contains(i)) {
				partition[i] = 1;
				ct++;
			}

			i++;
		}

		SplittedExampleSet result = new SplittedExampleSet(oldExampleSet, new Partition(partition, 2));
		result.selectSingleSubset(1);
		//System.out.println("old:" + oldExampleSet.size() + "result:" + result.size() + "/data:" + oldExampleSet.size() + "/ct:" + ct);
		return result;
	}

	/* Function to take a decision about remove or not the instance */
	protected boolean decideToRemove(Collection<Tupel<Double, Integer>> neighbours, int majorityClass, int threshold, Example example,int i ) {

		double numberFromMinorityClasses = 0;
		double numberFromMajorityClasses=0;
		double minorityDistanceWeight=0;
		double majorityDistanceWeight=0;
		
		double avgDistanceMaj=0;
		double avgDistanceMin=0;

		double[] exampleValues = toDoubleArray(example, example.getAttributes());
		Iterator <Tupel<Double, Integer>>iterator = neighbours.iterator();
		while (iterator.hasNext()) {
			Tupel<Double, Integer> neighbor =  iterator.next();
			Integer neighborLabel = neighbor.getSecond();
			
			double distance = neighbor.getFirst();
			double weight = 1/distance;
			if (Double.isNaN(weight) || Double.isInfinite(weight))
			{
				weight=0;
			}
			
			
			if (useDistanceWeight) {
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
					minorityDistanceWeight+=weight;
					numberFromMinorityClasses++;
					avgDistanceMin += distance;
				}
				else
				{
					majorityDistanceWeight+=weight;
					numberFromMajorityClasses++;
					avgDistanceMaj += distance;
				}
				
			}

		}
		boolean removeIt;
		//TENTATIVA, E ESTAH FUNCIONANDO
		if (use0t3) {
			removeIt = (numberFromMinorityClasses >0 && numberFromMinorityClasses < threshold);
		}
		//ESTA E A REGRA NORMAL ATHE AGORA
		else {
			removeIt = (numberFromMinorityClasses >= threshold);
		}

		avgDistanceMin = avgDistanceMin/numberFromMinorityClasses;
		avgDistanceMaj = avgDistanceMaj/numberFromMajorityClasses;
		
		
	//	boolean removeIt = (numberFromMinorityClasses >= threshold && numberFromMinorityClasses<=threshold+5);

		if (logVerbosity==1)
		{

			System.out.println("#### " +(int) example.getId() + ", label:"+example.getLabel()+", min_neigh:" +numberFromMinorityClasses+", maj_neigh:"+numberFromMajorityClasses +", weighted: "+
			minorityDistanceWeight  + "/"+majorityDistanceWeight+", avg: "+avgDistanceMin + "/"+ avgDistanceMaj +" "+(removeIt?"*":"") );
						
		}
		// TODO HOW TO DECIDE IF NOT ALL NEIGHBORS ARE FROM MINORITY?
		return (removeIt);

	}


	// From KNNLearnar
	private GeometricDataCollection obtainNeighbors(ExampleSet exampleSet) throws Exception {

		Attributes attributes = exampleSet.getAttributes();
		Attribute labelAtt = attributes.getLabel();
	
		DistanceMeasure measure = measureHelper.getInitializedMeasure(exampleSet);
		if (labelAtt.isNominal()) {
			// classification
			GeometricDataCollection<Integer> samples = new LinearList(measure);

			
			int valuesSize = attributes.size();
			for (Example example : exampleSet) {
				double[] values = new double[valuesSize];
				int i = 0;
				for (Attribute attribute : attributes) {
					values[i] = example.getValue(attribute);
					i++;
				}
				int labelValue = (int) example.getValue(labelAtt);
				///System.out.println("##" +labelValue);
				samples.add(values, labelValue);
				checkForStop();
			}
			return samples;
			// return new KNNClassificationModel(exampleSet, samples,k, getParameterAsBoolean(PARAMETER_WEIGHTED_VOTE));

		}
		else
			throw new OperatorException("Nominal label is required");
		
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		ParameterType type = new ParameterTypeString(PARAMETER_MAJORITYT_LABEL, "The class label that will be undersampled.", "", false);
		type.setExpert(false);
		types.add(type);
		
		types.add(new ParameterTypeInt(PARAMETER_THRESHOLD, "The number of k nearest instances of different classes to remove the majority instance.", -1,
				Integer.MAX_VALUE, 1));

		types.add(new ParameterTypeInt(PARAMETER_K, "The number of k neighbors. It's suggested to use odd numbers, or -1 to k=sqrt(|N|).", -1, Integer.MAX_VALUE, 5));
		
		types.addAll(DistanceMeasures.getParameterTypes(this));

		types.add(new ParameterTypeBoolean(PARAMETER_USE_DISTANCE_WEIGHT, "Use the distance as a weight for the knn undersampling count.", true,false));
	
		
		types.add(new ParameterTypeBoolean(PARAMETER_USE_0T3, "Use 0 < t <3 as removal decision.", false,true));
		
		
		types.add(new ParameterTypeInt(PARAMETER_LOG_VERBOSITY, "The level of log produced on standard output.", 0,3,true));
		
		
		
		
		return types;
	}

}
