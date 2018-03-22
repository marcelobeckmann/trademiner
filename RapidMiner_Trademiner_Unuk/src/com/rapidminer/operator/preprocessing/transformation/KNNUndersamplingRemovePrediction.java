package com.rapidminer.operator.preprocessing.transformation;

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Attribute;
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
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;

/**
 * KNN Undersampling implementation.
 * 
 * @author Marcelo Beckmann
 */

public class KNNUndersamplingRemovePrediction extends Operator {

	private static final String PARAMETER_PREDICTION_ATT="prediction_att";
	
	private InputPort exampleSetInput= getInputPorts().createPort("example set");
	private InputPort predictionInput= getInputPorts().createPort("lab prediction");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");
	
	/**
	 * Constructor
	 */
	public KNNUndersamplingRemovePrediction(OperatorDescription description) {
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
	
		ExampleSet prediction = predictionInput.getData();
		if (prediction.size()==0)
		{
			throw new OperatorException("Error: lab prediction is empty!");
		}
		
		if (prediction.size()!=inputSet.size())
		{
			throw new OperatorException("Error: prediction and inputset size doesn't match");
		}
		
		
		
		List<Integer> toBeRemoved=null;
		try {
			toBeRemoved = obtainInstancesToRemove(prediction);
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperatorException(e.getMessage());
		}
		
		exampleSetOutput.deliver(createCleanExampleSet(inputSet,toBeRemoved));
	}

	protected List<Integer> obtainInstancesToRemove(ExampleSet prediction) throws Exception {
		
		List<Integer> toRemove = new ArrayList<Integer>();
		Attribute predictionAtt = prediction.getAttributes().get(getParameter(this.PARAMETER_PREDICTION_ATT));
		
		for (int i = 0; i < prediction.size(); i++) {
			checkForStop();
			Example example = prediction.getExample(i);
	
			//System.out.println("---->" +predictionAtt.getMapping() +"/"+example.getPredictedLabel());
			if (example.getValue(predictionAtt)==1) {
					toRemove.add(i);
			}
			
		}
		
		return toRemove;
	}

	private ExampleSet createCleanExampleSet(ExampleSet oldExampleSet, List<Integer> toRemove) throws OperatorException {
		
		int partition[] = new int[oldExampleSet.size()];
		int i = 0;
		for (Example example : oldExampleSet) {
			if (!toRemove.contains(i)) {
				partition[i] = 1;
			}

			i++;
		}

		SplittedExampleSet result = new SplittedExampleSet(oldExampleSet, new Partition(partition, 2));
		result.selectSingleSubset(1);
		
		return result;
	}
	
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		ParameterType type = new ParameterTypeString(PARAMETER_PREDICTION_ATT,
				"The prediction attribute.", "prediction", false);
		type.setExpert(false);
		types.add(type);

		return types;
	}


}
