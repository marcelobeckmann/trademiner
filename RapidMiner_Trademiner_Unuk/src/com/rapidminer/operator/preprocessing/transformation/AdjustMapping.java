package com.rapidminer.operator.preprocessing.transformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.table.PolynominalMapping;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;

/**
 * Aggregate the decision of several documents, from the same symbol, same minute
 * 
 * @author Marcelo Beckmann
 */

public class AdjustMapping extends Operator {
	
	private InputPort predictionInput= getInputPorts().createPort("label prediction");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("label prediction adjusted");
	private static final String PARAMETER_ADD_3LABEL="add_3label"; 
	
	/**
	 * Constructor
	 */
	public AdjustMapping(OperatorDescription description) {
	
		super(description);
		
	}
	

	@Override
	public void doWork() throws OperatorException {

		ExampleSet inputSet = predictionInput.getData();
	

		
		Map<Integer, String> map = new HashMap<Integer, String>();

		boolean add3label = getParameterAsBoolean(PARAMETER_ADD_3LABEL);
		
		map.put(0, "0");
		map.put(1, "2");
		if (add3label) {
			map.put(2, "3");
		}
		NominalMapping pmap = new PolynominalMapping(map);
		
		//This is to adjust as the mappings between label and prediction are not matching
		if (inputSet.getAttributes().getPredictedLabel()!=null) {
			inputSet.getAttributes().getPredictedLabel().setMapping(pmap);
		}
		if (inputSet.getAttributes().getLabel()!=null) {
			inputSet.getAttributes().getLabel().setMapping(pmap);
		}
		

		exampleSetOutput.deliver(inputSet);
	}
	
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		
		types.add(new ParameterTypeBoolean(PARAMETER_ADD_3LABEL, "Add 3 label (For removal purposes later).", true,false));

		

		
		return types;
	}
	
	
}

