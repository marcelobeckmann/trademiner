/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2012 by Rapid-I and the contributors
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
package com.rapidminer.operator.preprocessing.transformation;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ValueString;
import com.rapidminer.operator.ports.CollectingPortPairExtender;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.PortPairExtender;
import com.rapidminer.operator.ports.metadata.AttributeParameterPrecondition;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.SubprocessTransformRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeAttribute;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;


/**
 * <p> In each iteration step, this meta operator executes its inner process
 * to the input example set. This will happen for each
 * possible attribute value of the specified attributes if <code>all</code> is
 * selected for the <code>values</code> parameter. If <code>above p</code> is selected,
 * an iteration is only performed for those values which exhibit an occurrence ratio of at
 * least p. This may be helpful, if only large subgroups should be considered.</p>
 * 
 * <p>The current value of the loop can be accessed with the specified macro name.</p>
 * 
 * @author Tobias Malbrecht, Ingo Mierswa
 */
public class ValueIterationFixed extends OperatorChain {

	public static final String PARAMETER_ATTRIBUTE = "attribute";

	public static final String PARAMETER_ITERATION_MACRO = "iteration_macro";

	public static final String DEFAULT_ITERATION_MACRO_NAME = "loop_value";

	
	public static final String PARAMETER_ATTRIBUTE_2 = "attribute2";

	public static final String PARAMETER_ITERATION_MACRO_2 = "iteration_macro2";

	
	
	public static final String PARAMETER_ATTRIBUTE_3 = "attribute3";

	public static final String PARAMETER_ITERATION_MACRO_3 = "iteration_macro3";
	
	
	private String currentValue = null; // for logging 

	private final InputPort exampleSetInput = getInputPorts().createPort("example set", new ExampleSetMetaData());
	private final OutputPort exampleInnerSource = getSubprocess(0).getInnerSources().createPort("example set");
	private final CollectingPortPairExtender outExtender = new CollectingPortPairExtender("out", getSubprocess(0).getInnerSinks(), getOutputPorts());

	public ValueIterationFixed(OperatorDescription description) {
		super(description, "Iteration");

		outExtender.start();
		exampleSetInput.addPrecondition(new AttributeParameterPrecondition(exampleSetInput, this, PARAMETER_ATTRIBUTE, Ontology.NOMINAL));

		getTransformer().addPassThroughRule(exampleSetInput, exampleInnerSource);
		getTransformer().addRule(new SubprocessTransformRule(getSubprocess(0)));
		getTransformer().addRule(outExtender.makePassThroughRule());

		addValue(new ValueString("current_value", "The nominal value of the current loop.") {
			@Override
			public String getStringValue() {
				return currentValue;
			}
		});
	}

	@Override
	public void doWork() throws OperatorException {
		ExampleSet exampleSet = exampleSetInput.getData(ExampleSet.class);
		exampleSet.recalculateAllAttributeStatistics();
		outExtender.reset();

		String attributeName = getParameterAsString(PARAMETER_ATTRIBUTE);
		String attributeName2 = getParameterAsString(PARAMETER_ATTRIBUTE_2);		
		String attributeName3 = getParameterAsString(PARAMETER_ATTRIBUTE_3);		

		Attribute attribute = exampleSet.getAttributes().get(attributeName);		
		Attribute attribute2 = exampleSet.getAttributes().get(attributeName2);		
		Attribute attribute3 = exampleSet.getAttributes().get(attributeName3);
	
		if (attribute == null) {
			throw new UserError(this, 111, attributeName);
		}
		if (!attribute.isNominal()) {
			throw new UserError(this, 119, attributeName, getName());
		}

		if (attribute2 == null) {
			throw new UserError(this, 111, attributeName2);
		}
		if (!attribute2.isNominal()) {
			throw new UserError(this, 119, attributeName2, getName());
		}

		
		
		String iterationMacro = getParameterAsString(PARAMETER_ITERATION_MACRO);
		String iterationMacro2 = getParameterAsString(PARAMETER_ITERATION_MACRO_2);
		String iterationMacro3 = getParameterAsString(PARAMETER_ITERATION_MACRO_3);

		
		//List<String> values = new LinkedList<String>(attribute.getMapping().getValues());
		for (Example example: exampleSet) {
			String value = example.getValueAsString(attribute);
			
			getProcess().getMacroHandler().addMacro(iterationMacro, value);
		
			value = example.getValueAsString(attribute2);
			
			getProcess().getMacroHandler().addMacro(iterationMacro2, value);
		
			value = example.getValueAsString(attribute3);
			
			getProcess().getMacroHandler().addMacro(iterationMacro3, value);
		
			
				// store for logging
				this.currentValue = value;

				exampleInnerSource.deliver((ExampleSet)exampleSet.clone());

				getSubprocess(0).execute();

				for (PortPairExtender.PortPair pair : outExtender.getManagedPairs()) {
					IOObject result = pair.getInputPort().getDataOrNull(IOObject.class);
					if (result != null) {
						result.setSource(this.getName() + ":" + value);
					}
				}				
				outExtender.collect();
			
			inApplyLoop();
		}

		if (iterationMacro != null) {
			getProcess().getMacroHandler().addMacro(iterationMacro, null);
			getProcess().getMacroHandler().addMacro(iterationMacro2, null);
			getProcess().getMacroHandler().addMacro(iterationMacro3, null);
					
		}
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		ParameterType type = new ParameterTypeAttribute(PARAMETER_ATTRIBUTE, "The nominal attribute for which the iteration should be defined", exampleSetInput, false);
		types.add(type);

		types.add(new ParameterTypeString(PARAMETER_ITERATION_MACRO, "Name of macro which is set in each iteration.", DEFAULT_ITERATION_MACRO_NAME, false));

		

		type = new ParameterTypeAttribute(PARAMETER_ATTRIBUTE_2, "The nominal attribute for which the iteration should be defined", exampleSetInput, false);
		types.add(type);

		types.add(new ParameterTypeString(PARAMETER_ITERATION_MACRO_2, "Name of macro which is set in each iteration.", DEFAULT_ITERATION_MACRO_NAME, false));



		type = new ParameterTypeAttribute(PARAMETER_ATTRIBUTE_3, "The nominal attribute for which the iteration should be defined", exampleSetInput, false);
		types.add(type);

		types.add(new ParameterTypeString(PARAMETER_ITERATION_MACRO_3, "Name of macro which is set in each iteration.", DEFAULT_ITERATION_MACRO_NAME, false));

		
		
		return types;
	}
}
