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
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ProcessStoppedException;
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

public class KNNPredictionUndersampling extends Operator {

	public static final String PARAMETER_MAJORITYT_LABEL = "majority_label";
	public static final String PARAMETER_K = "k";
	public static final String PARAMETER_THRESHOLD = "threshold";
	//public static final String PARAMETER_SEARCH_K = "search_k";
	//public static final String PARAMETER_PERCENT_OF_REMAINING_OVERSAMPLING = "percent_of_remaining_over";
	//public static final String PARAMETER_MINIMAL_K = "minimal_k";
	//public static final String PARAMETER_STEP_K="step_k";
	public static final String PARAMETER_USE_PREDICTION_AS_LABEL="use_prediction";

	private InputPort exampleSetInput= getInputPorts().createPort("example set");
//	private InputPort exampleSetInputTrain = getInputPorts().createPort("training set");

	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");
	private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);
	private GeometricDataCollection<Integer> knn;
	private double[] previousLabels;
	
	private Attribute labelAtt;
	private int majorityClassIndex;
	private Attribute predictionAtt;
	
	/**
	 * Constructor
	 */
	public KNNPredictionUndersampling(OperatorDescription description) {
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

		ExampleSet original = exampleSetInput.getData();
		if (original.size()==0)
		{
			throw new OperatorException("Error: inputset is empty!");
		}
		

		// Figure out the nominal index for the mapping
		labelAtt = original.getAttributes().getLabel();
		if (labelAtt == null) {
			throw new OperatorException("Label attribute is not present. Use set role to define a label to exampleset.");
		}
		String majorityClasslabel = this.getParameterAsString(PARAMETER_MAJORITYT_LABEL);
		ExampleSet inputSet;
		if ( getParameterAsBoolean(PARAMETER_USE_PREDICTION_AS_LABEL)) {
			predictionAtt = original.getAttributes().getPredictedLabel();
			if (predictionAtt==null) {
				throw new OperatorException("Prediction attribute is not present");
			}
			else
			{
				System.out.println("## Prediction attribute used as label: " + predictionAtt.getName());
			}
			inputSet = prepareForPrediction(original);
		}
		else
		{
			inputSet=original;
		}
	
		
		ExampleSet labelExampleSet = getExampleSetByLabelValue(inputSet, majorityClasslabel);
		int k = getParameterAsInt(PARAMETER_K);

		if (k==-1)
		{
			k =(int) Math.round( Math.pow(inputSet.size() ,0.5));
			if (k%2==0) {
				k++;
			}
			System.out.println("####### value of k calculated to "  + k);
		}
		
		
		majorityClassIndex = labelAtt.getMapping().getIndex(majorityClasslabel);
		
		if (labelExampleSet == null) {
			throw new OperatorException("Label " + majorityClasslabel + " does not exist in the exampleset.");
		}
		
		List<Integer> toBeRemoved=null;
		//int step_k = getParameterAsInt(PARAMETER_STEP_K);
		try {
			//int minimalK = getParameterAsInt(PARAMETER_MINIMAL_K);
			//while (toBeRemoved==null && k>minimalK) {
				toBeRemoved = obtainInstancesToRemove(inputSet, majorityClasslabel, k);
				/*if (toBeRemoved==null)
				{
					k-=step_k;
					if (k%2==0) {  //Preferentially odd numbers for k
						k++;
					}
					System.out.println("######### retrying for k="+k);
				}
			} */
		} catch (Exception e) {

			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		
		//Restore the labels in case to use the prediction attribute with knn instead of label (if I use the label in KNN I'll leak information),
		//As the predictive process is not finished
		restoreLabels(inputSet);

		exampleSetOutput.deliver(createCleanExampleSet(inputSet,toBeRemoved));
	}




	private ExampleSet getExampleSetByLabelValue(ExampleSet inputSet, String label) {
		SplittedExampleSet e = SplittedExampleSet.splitByAttribute(inputSet, labelAtt);

		int labelIndex = labelAtt.getMapping().getIndex(label);
		if (labelIndex == -1) {
			return null;
		}

		SplittedExampleSet b = (SplittedExampleSet) e.clone();
		b.selectSingleSubset(labelIndex);

		return b;
	}

	
	/**
	 * Calculates the average size of other classes
	 * @param data
	 * @param label
	 * @return
	 */
	private int calculateAverageSizeOfOtherClasses(ExampleSet data, String label,int countOfOvesampledClass)
	{	
		int labelCount = labelAtt.getMapping().size();
		
		System.out.println("############# majority "+ countOfOvesampledClass +", data size: " + data.size() + ", remaining: " +  (data.size()-countOfOvesampledClass) + ", avg per label: "+ (data.size()-countOfOvesampledClass)/(labelCount-1));
		return (data.size()-countOfOvesampledClass)/(labelCount-1);
	}
	
	private ExampleSet prepareForPrediction(ExampleSet data) throws UndefinedParameterError, ProcessStoppedException, OperatorException
	{	
			previousLabels = new double[data.size()];
			
			for (int i = 0; i < data.size(); i++) {
				checkForStop();
				Example example = data.getExample(i);
				
				
				previousLabels[i]= example.getValue(labelAtt);
				double prediction=example.getValue(predictionAtt);
				
				if (!Double.isNaN(prediction))
				{	
					example.setValue(labelAtt,prediction);
					//example.setValue(predictionAtt, Double.NaN);
				}
			}
			return data;
		
		
	}
	/** Restore the previous labels in case to use the prediction attribute */
	
	private void restoreLabels(ExampleSet data) throws ProcessStoppedException {
		if (previousLabels == null) {
			return;
		}
	
		for (int i = 0; i < data.size(); i++) {
			checkForStop();
			Example example = data.getExample(i);
			example.setValue(labelAtt, previousLabels[i]);
		}

	}
	
	protected List<Integer> obtainInstancesToRemove(ExampleSet data, String majorityClassLabel, int k) throws Exception {

		ExampleSet training=data;
		/*if ( getParameterAsBoolean(PARAMETER_USE_PREDICTION_AS_LABEL)) {
			training = exampleSetInputTrain.getData();
		}
		else {
			
			training=data;
		}*/
			
		// Instances for synthetic samples
		List<Integer> toRemove = new ArrayList();
		/*
		 * Compute k nearest neighbors for i, and save the indices in the nnarray
		 */
		Attributes sampleAttributes = data.getAttributes();
		int threshold = getParameterAsInt(PARAMETER_THRESHOLD);
		knn = obtainNeighbors(training);
		
		//int minimalK = getParameterAsInt(PARAMETER_MINIMAL_K);
		//	double remainingOversampling = getParameterAsInt(PARAMETER_PERCENT_OF_REMAINING_OVERSAMPLING)/100.0;
		//	boolean searchK = getParameterAsBoolean(PARAMETER_SEARCH_K);
		int countOfOvesampledClass = getExampleSetByLabelValue(data,majorityClassLabel).size();
		int averageSizeOfOtherClasses = calculateAverageSizeOfOtherClasses(data, majorityClassLabel,countOfOvesampledClass);
		boolean useClassification=false;
		
		for (int i = 0; i < data.size(); i++) {
			
			//THE OVERSAMPLED CLASS NEVER WILL HAVE LESS EXAMPLES THAN THE OTHER CLASS
			//if (searchK && k > minimalK && (countOfOvesampledClass-toRemove.size())<=averageSizeOfOtherClasses+(averageSizeOfOtherClasses*remainingOversampling)) {
			//	System.out.println("############# knn und exited,  k=" + k+ ", to be removed: "+toRemove.size()+" , remaining : " + (countOfOvesampledClass-toRemove.size()) + ", avg size per class:" + averageSizeOfOtherClasses);
			//	return null;
			//}
			checkForStop();
			Example example = data.getExample(i);
		
			
			if ( example.getValue(labelAtt) == majorityClassIndex)
			{
				int j = 0;
				double values[] = new double[sampleAttributes.size()+1];
				for (Iterator <Attribute>i$ = sampleAttributes.iterator(); i$.hasNext();) {
					Attribute attribute = i$.next();
					values[j] = example.getValue(attribute);
					j++;
				}
				String.valueOf(true);
				Collection<Tupel<Double, Integer>> neighbours = knn.getNearestValueDistances(k, values);
				boolean remove=false;
				//if (!useClassification) {
					remove = decideToRemove(neighbours, majorityClassIndex, threshold, example,i);
				/* }
				else
				{
					remove =decideToRemoveByClassification(neighbours, majorityClassIndex, threshold,example);
				}*/
				if (remove)
				{
					toRemove.add(i);
				}
			
			}
			//System.out.print("."); 
		}
		System.out.println("############# knn und exited NORMALLY, best k=" + k+ ", to be removed: "+toRemove.size()+" , remaining : " + (countOfOvesampledClass-toRemove.size()) + ", avg size per class:" + averageSizeOfOtherClasses);

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
		System.out.println("old:" + oldExampleSet.size() + "result:" + result.size() + "/data:" + oldExampleSet.size() + "/ct:" + ct);
		return result;
	}
	
	/* Function to take a decision about remove or not the instance */
	protected boolean decideToRemove(Collection<Tupel<Double, Integer>> neighbours, int majorityClass, int threshold, Example example,int i ) {

		int numberFromMinorityClasses = 0;
		int numberFromMajorityClasses=0;

		Iterator <Tupel<Double, Integer>>iterator = neighbours.iterator();
		while (iterator.hasNext()) {
			Tupel<Double, Integer> neighbor =  iterator.next();
			Integer neighborLabel = neighbor.getSecond();
			
			if (neighborLabel != majorityClass) {
				numberFromMinorityClasses++;
			}
			else
			{
				numberFromMajorityClasses++;
			}

		}
		double prediction=-1;
		double label=-1;
		if (predictionAtt!=null)
		{
			prediction= example.getValue(predictionAtt);
			label = previousLabels[i];
		}
		else
		{
			label = example.getValue(labelAtt);
		}
				
		
		System.out.println("#### pred:"+ prediction+",label:"+label+", min neigh:" +numberFromMinorityClasses+",maj neigh:"+numberFromMajorityClasses );
		
		// TODO HOW TO DECIDE IF NOT ALL NEIGHBORS ARE FROM MINORITY?
		return (numberFromMinorityClasses >= threshold);

	}


	/* Function to take a decision about remove or not the instance */
	protected boolean decideToRemoveByClassification(Collection<Tupel<Double, Integer>> neighbours, int majorityClass, int threshold, Example example) {

		int numberFromMinorityClasses = 0;
		int numberFromOtherClasses=0;

		int prediction =(int) example.getValue(predictionAtt);
		//int majorityClassIndex = labelAtt.getMapping().getIndex(majorityClassLabel);

		
		Iterator <Tupel<Double, Integer>>iterator = neighbours.iterator();
		while (iterator.hasNext()) {
			Tupel<Double, Integer> neighbor =  iterator.next();
			Integer neighborLabel = neighbor.getSecond();
			if (neighborLabel != majorityClass) {
				numberFromMinorityClasses++;
			}
			else
			{
				numberFromOtherClasses++;
			}

		}
		//A classification result will be returned
		if (majorityClassIndex==prediction)
		{
			//If this is true, a classification error occurred here
			return  (numberFromMinorityClasses>=numberFromOtherClasses);
		}
		else
		{
			//If this is true, a classification error occurred here
			return  (numberFromOtherClasses>= numberFromMinorityClasses); 
		}
		
	}

	// From KNNLearnar
	private GeometricDataCollection obtainNeighbors(ExampleSet exampleSet) throws Exception {

		DistanceMeasure measure = measureHelper.getInitializedMeasure(exampleSet);
		if (labelAtt.isNominal()) {
			// classification
			GeometricDataCollection<Integer> samples = new LinearList<Integer>(measure);

			Attributes attributes = exampleSet.getAttributes();
			Attribute labelAtt = attributes.getLabel();
			
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

		ParameterType type = new ParameterTypeString(PARAMETER_MAJORITYT_LABEL, "The class label that will be undersampled.", "", false);
		type.setExpert(false);
		types.add(type);

		type = new ParameterTypeBoolean(PARAMETER_USE_PREDICTION_AS_LABEL, "Use the prediction attribute as the label. This is useful when the label is unknow or should not be used for predictbility evaluation.", false, false);
		type.setExpert(false);
		types.add(type);


		
		
		types.add(new ParameterTypeInt(PARAMETER_THRESHOLD, "The number of k nearest instances of different classes to remove the majority instance.", 0,
				Integer.MAX_VALUE, 1));

		types.add(new ParameterTypeInt(PARAMETER_K, "The number of k neighbors. It's suggested to use odd numbers, or -1 to k=sqrt(|N|).", -1, Integer.MAX_VALUE, 5));

		//TODO PUT A PARAMETER OF PERFORM A K SEARCH
/*
		types.add(
				new ParameterTypeBoolean(PARAMETER_SEARCH_K, "Executes an exaustive search in the k parameter, in order to find the best k to give the right percent of remaining oversampling",false , true)
				);
		
		type = new ParameterTypeInt(PARAMETER_PERCENT_OF_REMAINING_OVERSAMPLING, "For k search purposes. This is the percent of remaining oversampling over the average number of exampler per class",0,100, 20, true);
		type.registerDependencyCondition(new BooleanParameterCondition(this,PARAMETER_SEARCH_K, true, true));
		types.add(type);

		
		type = new ParameterTypeInt(PARAMETER_MINIMAL_K, "This is the minimal k acceptable for search purposes.",2,100, 5, true);
		type.registerDependencyCondition(new BooleanParameterCondition(this,PARAMETER_SEARCH_K, true, true));
		types.add(type);

		
		type = new ParameterTypeInt(PARAMETER_STEP_K, "This is the step the k will be decreased.",1,100, 5, true);
		type.registerDependencyCondition(new BooleanParameterCondition(this,PARAMETER_SEARCH_K, true, true));
		types.add(type);
*/
		
		types.addAll(DistanceMeasures.getParameterTypes(this));

		return types;
	}

}
