package com.rapidminer.operator.preprocessing.transformation;

import java.io.FileWriter;
import java.io.IOException;
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
import com.rapidminer.parameter.ParameterTypeDouble;
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

public class KNNUndersampling extends Operator {

	public static final String PARAMETER_MAJORITYT_LABEL = "majority_label";
	public static final String PARAMETER_K = "k";
	public static final String PARAMETER_THRESHOLD = "threshold"; 
//	public static final String PARAMETER_USE_DISTANCE_WEIGHT = "use_distance_weighting";	
	public static final String PARAMETER_LOG_VERBOSITY = "log_verbosity";
    public static final String PARAMETER_GENERATE_OUTPUT_FILE ="file_output";
	
	private InputPort exampleSetInput= getInputPorts().createPort("example set");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");
	private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);
	private GeometricDataCollection<Integer> knn;
	
	private Attribute labelAtt;
	private int majorityClassIndex;
	private Attributes attributes;
	private boolean useDistanceWeight;	
	private int logVerbosity=0;
	private static FileWriter out ;
	/*
	static 
	{
		KNNUndersampling.setupOutfile("c:/var/tmp/output/__knnund.csv");
		try {
			out.write("test\n");
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	public static void main(String args[])
	{}

	private  static void setupOutfile(String outputFile)  {
	
		try {
			File file =new File(outputFile);
			out = new FileWriter(file,true);
			
			if (!file.exists())
			{
				file.createNewFile();
			}
			if (file.length()==0) {
				String header ="id,numMinClasses,numMajClass,minDistWeight,majDistWeight,removed\n";
				out.write(header);
				out.flush();
			}
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	
	}

*/
	
	
	/**
	 * Constructor
	 */
	public KNNUndersampling(OperatorDescription description) {
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
		
	/*	String outFile = getParameterAsString(this.PARAMETER_GENERATE_OUTPUT_FILE);
		if (outFile!=null || outFile.isEmpty()) {
			setupOutfile(getParameterAsString(this.PARAMETER_GENERATE_OUTPUT_FILE));
		}
      */  
		
		useDistanceWeight = false ;//getParameterAsBoolean(PARAMETER_USE_DISTANCE_WEIGHT);
		attributes = inputSet.getAttributes();
		logVerbosity = getParameterAsInt(PARAMETER_LOG_VERBOSITY);
		
		
		// Figure out the nominal index for the mapping
		labelAtt = attributes.getLabel();
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
		
		List<Integer> toBeRemoved=null;
		try {
			toBeRemoved = obtainInstancesToRemove(inputSet, majorityClasslabel, k);
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		
		if (out!=null)
		{

			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		exampleSetOutput.deliver(createCleanExampleSet(inputSet,toBeRemoved));
	}




	/** Transforms an Example in a double array */
	
	private double[] toDoubleArray(Example example)
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

	
		
	protected List<Integer> obtainInstancesToRemove(ExampleSet data, String majorityClassLabel, int k) throws Exception {

				
		// Instances for synthetic samples
		List<Integer> toRemove = new ArrayList<Integer>();
		/*
		 * Compute k nearest neighbors for i, and save the indices in the nnarray
		 */
		double threshold = getParameterAsDouble(PARAMETER_THRESHOLD);
		knn = obtainNeighbors(data);
		
		for (int i = 0; i < data.size(); i++) {
			checkForStop();
			Example example = data.getExample(i);
			if ( example.getValue(labelAtt) == majorityClassIndex )
			{
				double values[] = toDoubleArray(example);
				Collection<Tupel<Double, Integer>> neighbours = knn.getNearestValueDistances(k, values);
				
				if (decideToRemove(neighbours, majorityClassIndex, threshold, example,i)) {
					toRemove.add(i);
				}
			
			}
		}
		//System.out.println("############# knn und exited NORMALLY, best k=" + k+ ", to be removed: "+toRemove.size()+" , remaining : " + (data.size()-toRemove.size()));

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
	protected boolean decideToRemove(Collection<Tupel<Double, Integer>> neighbours, int majorityClass, double threshold, Example example,int i ) {

		int numberFromMinorityClasses = 0;
		int numberFromMajorityClasses = 0;
		double minorityDistanceWeight=0;
		double majorityDistanceWeight=0;
		
		
		
		int actualLabel = (int)example.getLabel();

		Iterator <Tupel<Double, Integer>>iterator = neighbours.iterator();
		while (iterator.hasNext()) {
			Tupel<Double, Integer> neighbor =  iterator.next();
			Integer neighborLabel = neighbor.getSecond();
			
			double weight = 1/neighbor.getFirst();
			if (Double.isNaN(weight) || Double.isInfinite(weight))
			{
				weight=0;
			}
			//Distance weight is aka density
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
				}
				else
				{
					majorityDistanceWeight+=weight;
					numberFromMajorityClasses++;
					
				}
				
			}
			
			
		}

		
		//TENTATIVA, REMOVER SE NAO FUNCIONAR
	/*	if (numberFromMinorityClasses==0)
			return true;
		else if (true)
			return false;
	*/	
		
		boolean removeIt;
		

		//Distance weight is discarded from this decision because the bad results
		removeIt= (numberFromMinorityClasses >= threshold);
		


		
		if (logVerbosity==1)
		{
			System.out.println("#### " +(int) example.getId() + ", label:"+actualLabel+", min_neigh:" +numberFromMinorityClasses+", maj_neigh:"+numberFromMajorityClasses +", weighted: "+
			minorityDistanceWeight  + "/"+majorityDistanceWeight+" "+(removeIt?"*":"") );
		}
		else if (logVerbosity==2)
		{
			System.out.println(numberFromMinorityClasses+","+numberFromMajorityClasses  +"," +actualLabel );
		}

		if (out!=null)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(example.getId());
			sb.append(',');
			sb.append(numberFromMinorityClasses);
			sb.append(',');
			sb.append(numberFromMajorityClasses);
			sb.append(',');
			sb.append(minorityDistanceWeight);
			sb.append(',');
			sb.append(majorityDistanceWeight);
			sb.append(',');
			sb.append((removeIt?1:0));
			sb.append('\n');
			
			try {
				out.write(sb.toString());
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		}
		
		
		return removeIt;
			
	}

	// From KNNLearnar
	private GeometricDataCollection obtainNeighbors(ExampleSet exampleSet) throws Exception {

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

	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		ParameterType type = new ParameterTypeString(PARAMETER_MAJORITYT_LABEL, "The class label that will be undersampled.", "", false);
		type.setExpert(false);
		types.add(type);

		
		types.add(new ParameterTypeDouble(PARAMETER_THRESHOLD, "The threshold number of k nearest instances of different classes to remove the majority instance.", 0,
				Integer.MAX_VALUE, 1,false));

		types.add(new ParameterTypeInt(PARAMETER_K, "The number of k neighbors. It's suggested to use odd numbers, or -1 to k=sqrt(|N|).", -1, Integer.MAX_VALUE, 5,false));


		
		types.addAll(DistanceMeasures.getParameterTypes(this));

		//types.add(new ParameterTypeBoolean(PARAMETER_USE_DISTANCE_WEIGHT, "Use the distance as a weight for the knn undersampling count.", true,false));

		
		types.add(new ParameterTypeInt(PARAMETER_LOG_VERBOSITY, "The level of log produced on standard output.", 0,3,true));


		//types.add(new ParameterTypeString(PARAMETER_GENERATE_OUTPUT_FILE,"Writes the KNN UND output into a file",""));
		
		return types;
	}

}
