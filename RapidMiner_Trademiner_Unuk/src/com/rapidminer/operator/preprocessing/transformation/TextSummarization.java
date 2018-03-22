package com.rapidminer.operator.preprocessing.transformation;

/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2009 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */

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
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;

/**
 * This is the implementation of text sumarization proposed in
 * 
 * @author Marcelo Beckmann
 */

public class TextSummarization extends Operator {

	private static final String PARAMETER_TEXT_ATTRIBUTE = "text_attribute";
	public static final String PARAMETER_TOPIC_OPENING = "topicOpening";
	public static final String PARAMETER_TOPIC_CENTRAL = "topicCentral";
	public static final String PARAMETER_TOPIC_CLOSING = "topicClosing";
	public static final String PARAMETER_BOND_THRESHOLD = "bondThreshold";
	
	
	private InputPort exampleSetInput = getInputPorts().createPort("example set");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");
	
	/**
	 * Constructor
	 */
	public TextSummarization(OperatorDescription description) {
		super(description);

//		exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, new String[] { "relative time" }, Ontology.ATTRIBUTE_VALUE));

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
		
		String text_attribute = getParameterAsString(PARAMETER_TEXT_ATTRIBUTE);
		
		double topicOpening= getParameterAsDouble(PARAMETER_TOPIC_OPENING);
		double topicCentral= getParameterAsDouble(PARAMETER_TOPIC_CENTRAL);
		double topicClosing=getParameterAsDouble(PARAMETER_TOPIC_CLOSING);
		int bondThreshold= getParameterAsInt(PARAMETER_BOND_THRESHOLD);
		
		Attribute attribute = inputSet.getAttributes().get(text_attribute);
		
		for (Example example : inputSet) {
		
			String text =example.getValueAsString(attribute); 
				
			Summary summary = new Summary(true,topicOpening,topicCentral, topicClosing, bondThreshold);
            //TODO PUT A WORD LIST OF REMOVAL WORDS IN THE 2ND PARAMETER
            summary.summariseText(text, "");
			
			example.setValue(attribute,summary.result());
			
		}

		exampleSetOutput.deliver(inputSet);
		
		
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		types.add(new ParameterTypeString(PARAMETER_TEXT_ATTRIBUTE, "The name of the text attribute.", false));
		
		
		ParameterType type = new ParameterTypeDouble(PARAMETER_TOPIC_OPENING,"Topic Opening (%)", 0, 100, 10);
		type.setExpert(false);
		types.add(type);

		
		type = new ParameterTypeDouble(PARAMETER_TOPIC_CENTRAL, "Topic Central (%)", 0, 100,10);
		type.setExpert(false);
		types.add(type);

		
		type = new ParameterTypeDouble(PARAMETER_TOPIC_CLOSING, "Topic Closing (%)", 0, 100,10);
		type.setExpert(false);
		types.add(type);

		type = new ParameterTypeInt(PARAMETER_BOND_THRESHOLD, "Bond Threshold", 1, 10,3);
		type.setExpert(false);
		types.add(type);


		
		return types;
	}
	
	
		

}
