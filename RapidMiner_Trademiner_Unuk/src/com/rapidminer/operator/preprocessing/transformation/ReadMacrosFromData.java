package com.rapidminer.operator.preprocessing.transformation;

import java.util.List;

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
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;

public class ReadMacrosFromData extends Operator {

	private static final String PARAMETER_MACRO_ATTRIBUTE = "macro_attribute";
	private static final String PARAMETER_VALUE_ATTRIBUTE = "value_attribute";
	private InputPort exampleSetInput = getInputPorts().createPort("example set");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");

	/**
	 * Constructor
	 */
	public ReadMacrosFromData(OperatorDescription description) {
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

		ExampleSet inputSet = exampleSetInput.getData(ExampleSet.class);

		
		Attribute attMacro = inputSet.getAttributes().get(getParameterAsString(PARAMETER_MACRO_ATTRIBUTE));
		Attribute attValue = inputSet.getAttributes().get(getParameterAsString(PARAMETER_VALUE_ATTRIBUTE));
		
		for (int i=0 ; i<inputSet.size();i++)
		{
			Example example = inputSet.getExample(i);
			String macro = example.getValueAsString(attMacro);
			String value = example.getValueAsString(attValue);
			if (macro.startsWith("#")) {  //Skipping comments
				System.out.println("Skipping comment " + macro);
				continue;
			}
			value="?".equals(value)?"":value;
			System.out.println(macro + ":"+value);
			getProcess().getMacroHandler().addMacro(macro, value);
		}	
		exampleSetOutput.deliver(inputSet);
	}


	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_MACRO_ATTRIBUTE, "The attribute name for macro name.", "macro", true));
		types.add(new ParameterTypeString(PARAMETER_VALUE_ATTRIBUTE, "The attribute name for macro value.", "value", true));

		return types;
	}


}