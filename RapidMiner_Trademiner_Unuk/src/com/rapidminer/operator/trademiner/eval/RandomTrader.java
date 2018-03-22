package com.rapidminer.operator.trademiner.eval;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
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
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;

public class RandomTrader extends Operator {

	private InputPort exampleSetInput = getInputPorts().createPort("prediction");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("rand prediction");

	// Parameters
	
	public static final String PARAMETER_LABEL_ATTRIBUTE = "labelAtt";
	
	public static final String PARAMETER_PREDICTION_ATTRIBUTE = "predictionAtt";

	public static final String PARAMETER_USE_GAUSSIAN_DISTR = "use_gausian_distribution";
	
	public static final String PARAMETER_LISTOF_LABELS = "list_of_labels";
	

	public static final String PARAMETER_FORCE_DISTRIBUTION = "force_distribution";
	
	
	private double[] distribution;
	private Attribute predictionAtt; 
	private Attribute labelAtt; 
	private boolean useGaussian;
	private Random random;
	private List<String> labels;
	
	public RandomTrader(OperatorDescription description) {
		super(description);

		// exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, new String[] { "relative time" }, Ontology.ATTRIBUTE_VALUE));

		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput, exampleSetOutput, SetRelation.EQUAL) {

			@Override
			public ExampleSetMetaData modifyExampleSet(ExampleSetMetaData metaData) throws UndefinedParameterError {

				return metaData;
			}
		});
	}

	@Override
	public void doWork() throws OperatorException {
		ExampleSet predictionSet = exampleSetInput.getData(ExampleSet.class);
		labels = parseLabels();
		try {
			
			
			// Retrieve parameters
			
			labelAtt = predictionSet.getAttributes().get(getParameterAsString(PARAMETER_LABEL_ATTRIBUTE));
			// Retrieve parameters
			predictionAtt = predictionSet.getAttributes().get(getParameterAsString(PARAMETER_PREDICTION_ATTRIBUTE));
	
			random = new Random(System.currentTimeMillis());
			
					
			distribution=obtainDistribution(predictionSet);	
			for (int i = 0; i < predictionSet.size(); ++i) {
				Example example = predictionSet.getExample(i);

				double prediction = generateRandomPrediciton();

				example.setValue(predictionAtt, prediction);

				checkForStop();
			}

		} catch (Exception e) {

			throw new OperatorException(e.getMessage(), e);
		}
		
		
		exampleSetOutput.deliver(predictionSet);
	}
	
	public double[] obtainDistribution(ExampleSet exampleSet) throws UndefinedParameterError
	{
		double[] dist = calculateDistribution(exampleSet);
		
		Arrays.sort(dist);
		
		return dist;
	}
	

	public double[] calculateDistribution(ExampleSet exampleSet) throws UndefinedParameterError
	{
		
		String forcedDistribution= getParameterAsString(PARAMETER_FORCE_DISTRIBUTION);
		double[] distribution= new double [labels.size()];
		System.out.println("Random trader\nTest set size:" + distribution.length);
		
		//This is to force a distribution, instead to use the actual label distribution in the underlying test sets
		if (forcedDistribution!=null && !forcedDistribution.isEmpty())
		{
			String[] valuesStr = forcedDistribution.split(",");
			
			for (int i=0;i<valuesStr.length;++i)
			{
				
				distribution[i] = Double.parseDouble(valuesStr[i]);
				System.out.print("Label " + labels.get(i) + ", distribution:" +distribution[i] );
			}
			return distribution;
		}
		
		
		
		for (int i = 0; i < exampleSet.size(); ++i) {
			Example example = exampleSet.getExample(i);
			int prediction =labels.indexOf(String.valueOf((int)example.getValue(labelAtt)));
			distribution[prediction]= distribution[prediction] +1;
		}
		
		//Calculate the percent of distribution
		for (int i = 0; i < distribution.length; ++i) {
			System.out.print("Label " + labels.get(i) + " size:" +distribution[i] );
			distribution[i] =  distribution[i] / exampleSet.size();
			System.out.println(", distribution:"+ distribution[i]);
		}

		return distribution;
	}
	
	public double generateRandomPrediciton()
	{
		
		double prediction;
		
		if (useGaussian)
		{
			prediction = random.nextGaussian();
		}
		else
		{
			prediction= random.nextDouble();
		}
		
		
		String label;
		for (int i=0;i<distribution.length;++i)
		{
			//TODO THIS ONLY WILL WORK FOR label 0,2
			if (i==0)
			{
				label="2";
			}	
			else
			{
				label="0";
			}
			//------------------------------------
			if (prediction<=distribution[i])
			{
				return Integer.parseInt(label);
			}
		}
		
		return -1;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		ParameterType type;
		
		type = new ParameterTypeString(PARAMETER_LABEL_ATTRIBUTE,
				"The attribute name which contains the actual label in prediction exampleset input. The label distribution to generate the random decision will come from here.", "label", false);
		types.add(type);
		
		
		type = new ParameterTypeString(PARAMETER_PREDICTION_ATTRIBUTE,
				"The attribute name which contains the algorithm outcome in prediction exampleset input. The random decision will replace this value.", "prediction", false);
		types.add(type);

		type = new ParameterTypeBoolean(PARAMETER_USE_GAUSSIAN_DISTR,
				"Use a gaussian distribution for random number generation", false, false);
		types.add(type);


		types.add(new ParameterTypeString(PARAMETER_LISTOF_LABELS, "The experiment labels.", "-2,0,2", true));

		
		types.add(new ParameterTypeString(PARAMETER_FORCE_DISTRIBUTION, "Forces a distribution of labels to be followed by random trader, separated by commas, for example, 0.90,0.10.", "", true));
		
		types.add(type);

		
		
		return types;
	}
	
	public List<String> parseLabels() throws UndefinedParameterError {

		String parameterListLabels = getParameterAsString(PARAMETER_LISTOF_LABELS);
		String[] labels;
		
		labels = parameterListLabels.split(",");

		
		return Arrays.asList(labels);
	}

}
