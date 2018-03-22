
package com.rapidminer.operator.preprocessing.transformation;

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.example.table.DoubleArrayDataRow;
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
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.math.container.GeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;

import weka.core.Debug.Random;

/**
 * SMOTE implementation.
 * 
 * @author Marcelo Beckmann
 */

public class SMOTE extends Operator {

	public static final String PARAMETER_LABEL = "label";
	public static final String PARAMETER_OVERSAMPLING_PERCENT = "oversampling_percent";

	public static final String PARAMETER_K = "k";

	private InputPort exampleSetInput = getInputPorts().createPort("example set");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");
	private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);
	private GeometricDataCollection<Integer> knn;

	private Random random;
	
	/**
	 * Constructor
	 */
	public SMOTE(OperatorDescription description) {
		super(description);

		getTransformer().addRule(
				new ExampleSetPassThroughRule(exampleSetInput,
						exampleSetOutput, SetRelation.EQUAL) {

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

		ExampleSet inputSet = exampleSetInput.getData();

		Attributes attributes =  inputSet.getAttributes();
		
		random=new Random(System.currentTimeMillis()*System.currentTimeMillis());
		int j=0;
		
		
		// Figure out the nominal index for the mapping
		Attribute labelAtt = attributes.getLabel();
		if (labelAtt == null) {
			throw new OperatorException("Label attribute is not present. Use set role to define a label to exampleset.");
		}
		String labelToOversampling = this.getParameterAsString(PARAMETER_LABEL);

		ExampleSet labelExampleSet = getExampleSetByLabelValue(inputSet,labelToOversampling);

		
		if (labelExampleSet == null) {
			throw new OperatorException("Label " + labelToOversampling	+ " does not exist in the exampleset.");
		}
	

		int labelIndex = labelAtt.getMapping().getIndex(labelToOversampling);

		List <double[]> synthetics= generateSMOTEdSamples(inputSet, labelToOversampling,labelIndex);
		
		
		MemoryExampleTable table = (MemoryExampleTable) inputSet.getExampleTable();
		for (int i = 0; i < synthetics.size(); i++) {
			
			int id=(int) (random.nextDouble()*10000000) ;
			table.addDataRow(new DoubleArrayDataRow(synthetics.get(i)));
			Example example=inputSet.getExample(inputSet.size()-1);
			example.setLabel(labelIndex);
			example.setId(id);
			
		}
		exampleSetOutput.deliver(inputSet);
	}

	

	
	
	private ExampleSet getExampleSetByLabelValue(ExampleSet inputSet, String label) throws OperatorException {
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
	
	

	private List <double[]> generateSMOTEdSamples(ExampleSet data, String label,int labelIndex) throws OperatorException  {

		// Obtain the samples from class w
		ExampleSet samples = getExampleSetByLabelValue(data, label);
		
		int T = samples.size();
		
		double amountOfSMOTE = getParameterAsInt(PARAMETER_OVERSAMPLING_PERCENT);
		/*
		 * IF N is less than 100%, randomize the minority class samples as only
		 * a random percent of them will be SMOTEd
		 */
		if (amountOfSMOTE < 100) {
			T = (int) ((amountOfSMOTE / 100.0) * T);
			amountOfSMOTE = 100;
		}

		/* The amount of SMOTEFilter is assumed to be in integral multiples of 100 */
		amountOfSMOTE = (int) (amountOfSMOTE / 100.0);


		// Instances for synthetic samples
		List <double[]> synthetics =new ArrayList<double[]>();

		/*
		 * Compute k nearest neighbors for i, and save the indices in the
		 * nnarray
		 */
		ExampleSet neighborsData;
		
		// Compute k nearest neighbors for i
		/*This option puts all data to process KNN and synthetic generation is according the future work proposed by Chawla (pp 348):
		 "A minority class sample could possibly have a majority class sample as its nearest neighbor rater than a minority class sample" 
		 */
		//neighborsData=data;
		
		
		/*This other option puts only the minority class data to process KNN and synthetic generation is according the original work 
		  published by Chawla (pp 328):
		  
		  "The minority class is over-sampled by taking each minority class sample and introducing synthetic examples along the line 
		  segments joining any/all of the k minority class nearest neighbors."
		     
		 */
		neighborsData=samples;			
		
		knn = obtainNeighbors(neighborsData);
		Attributes sampleAttributes = samples.getAttributes();


		MemoryExampleTable table = (MemoryExampleTable) neighborsData.getExampleTable();
		
		for (int i=0;i<neighborsData.size();i++)
		{
			double[] values = new double[sampleAttributes.size()];
			Example example= neighborsData.getExample(i);
			int j = 0;
			for (Attribute attribute : sampleAttributes) {
				values[j] = example.getValue(attribute);
				j++;
			}
			
			if (example.getLabel()==labelIndex){
				
			//	displayExample(labelIndex, sampleAttributes, example);
			}
			
			knn.getNearestValueDistances(labelIndex,values);
			synthetics.addAll(populate((int)amountOfSMOTE, example, ((LinearListFixed)knn).getLatestValues()));
		
		}

		return synthetics;
	}





	
	

	/* Function to generate synthetic samples */
	protected List<double[]> populate(int N, Example sample, double[][] neighbors) {
		
		Attributes attributes= sample.getAttributes();
		
		List<double[]> synthetics= new ArrayList<double[]>();
		while (N != 0) {

			/*
			 * Choose a random number between 1 and k, call it nn. This step
			 * chooses one of the k nearest neighbors of i
			 */

			//Does not permit that the same nn be selected more than 2 times: Not a good approach
			/*int nn;
			do { */
			  int nn = (int) (random.nextDouble() * neighbors.length);
			  
			/*}
			while (blackList.contains(nn));
			blackList.add(nn);*/
			
			//Instance neighbor = data.instance(((ClassDistance) knnList.get(nn)).index);
			double[] neighbor = neighbors[nn];
			//remove to avoid duplication instances, applicable, but only with N>1
			//neighbors.remove(nn);
		
			
			
			double syntethic[] = new double[attributes.size()];
			int i = 0; 
			for (Attribute attribute: attributes) {
			// Process all the attributes, less the class attribute
				double dif = neighbor[i] - sample.getValue(attribute);
				// The original algorithm proposed by Chawla was like this
				// double gap = random.nextDouble();
				double gap = random.nextDouble();
				// In this work was proposed the following way to calculate
				// the gap:
				// double gap = random.nextDouble()*Math.abs(dif);
				syntethic[i]= sample.getValue(attribute) + gap + dif;
				i++;
			
			}
			synthetics.add(syntethic);

			N--;
		}

		return synthetics;
	}

	
	
		//From KNNLearnar
	private GeometricDataCollection obtainNeighbors(ExampleSet exampleSet) throws OperatorException 
	{
		
		DistanceMeasure measure = measureHelper.getInitializedMeasure(exampleSet);
		Attribute label = exampleSet.getAttributes().getLabel();
		if (label.isNominal()) {
			// classification
			GeometricDataCollection<Integer> samples = new LinearListFixed<Integer>(measure);

			Attributes attributes = exampleSet.getAttributes();

			int valuesSize = attributes.size();
			for(Example example: exampleSet) {
				//if (firstCount++<10)
				//	System.out.println("############" + example.getClass().getCanonicalName());
				double[] values = new double[valuesSize];
				int i = 0;
				for (Attribute attribute: attributes) {
					values[i] = example.getValue(attribute);
					i++;
				}
				int labelValue = (int) example.getValue(label);
				samples.add(values, labelValue);
				checkForStop();
			}
			return samples;
			//return new KNNClassificationModel(exampleSet, samples,k, getParameterAsBoolean(PARAMETER_WEIGHTED_VOTE));
		
		}
		return null;
	}
			

		

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		ParameterType type = new ParameterTypeString(PARAMETER_LABEL,
				"The class label that will be oversampled.", "", false);
		type.setExpert(false);
		types.add(type);

		types.add(new ParameterTypeInt(PARAMETER_OVERSAMPLING_PERCENT,
				"The percent of instances to be removed from this class. Use multiples of 100.", 0, Integer.MAX_VALUE,
				100));

		types.add(new ParameterTypeInt(PARAMETER_K,
				"The number of k neighbors.", 0, 100,5));

				
		types.addAll(DistanceMeasures.getParameterTypes(this));


		
		return types;
	}

}
