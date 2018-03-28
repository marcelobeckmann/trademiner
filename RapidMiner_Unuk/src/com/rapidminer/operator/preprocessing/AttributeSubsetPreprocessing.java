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
package com.rapidminer.operator.preprocessing;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.NonSpecialAttributesExampleSet;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.annotation.ResourceConsumptionEstimator;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.PortPairExtender;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.AttributeSubsetPassThroughRule;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.MetaDataInfo;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.operator.ports.metadata.SubprocessTransformRule;
import com.rapidminer.operator.tools.AttributeSubsetSelector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.OperatorResourceConsumptionHandler;

/**
 * <p>This operator can be used to select one attribute (or a subset) by defining a 
 * regular expression for the attribute name and applies its inner operators to
 * the resulting subset. Please note that this operator will also use special 
 * attributes which makes it necessary for all preprocessing steps which should
 * be performed on special attributes (and are normally not performed on special
 * attributes).</p>
 * 
 * <p>This operator is also able to deliver the additional results of the inner
 * operator if desired.</p> 
 * 
 * <p>Afterwards, the remaining original attributes are added
 * to the resulting example set if the parameter &quot;keep_subset_only&quot; is set to 
 * false (default).</p>
 * 
 * <p>Please note that this operator is very powerful and can be used to create
 * new preprocessing schemes by combining it with other preprocessing operators.
 * However, there are two major restrictions (among some others): first, since the inner result
 * will be combined with the rest of the input example set, the number of 
 * examples (data points) is not allowed to be changed inside of the subset preprocessing. 
 * Second, attribute role changes will not be delivered to the outside since internally all special
 * attributes will be changed to regular for the inner operators and role changes can afterwards
 * not be delivered.</p> 
 * 
 * @author Ingo Mierswa, Shevek
 */
public class AttributeSubsetPreprocessing extends OperatorChain {

	/** The parameter name for &quot;Indicates if the additional results (other than example set) of the inner operator should also be returned.&quot; */
	public static final String PARAMETER_DELIVER_INNER_RESULTS = "deliver_inner_results";

	/** The parameter name for &quot;Indicates if the attributes which did not match the regular expression should be removed by this operator.&quot; */
	public static final String PARAMETER_KEEP_SUBSET_ONLY = "keep_subset_only";

	private final InputPort exampleSetInput = getInputPorts().createPort("example set", ExampleSet.class);
	private final OutputPort innerExampleSetSource = getSubprocess(0).getInnerSources().createPort("exampleSet");
	private final InputPort innerExampleSetSink = getSubprocess(0).getInnerSinks().createPort("example set", ExampleSet.class);
	private final OutputPort exampleSetOutput = getOutputPorts().createPort("example set");
	private final PortPairExtender innerResultPorts = new PortPairExtender("through", getSubprocess(0).getInnerSinks(), getOutputPorts());

	private final AttributeSubsetSelector attributeSelector = new AttributeSubsetSelector(this, exampleSetInput); 

	public AttributeSubsetPreprocessing(OperatorDescription description) {
		super(description, "Subset Process");
		getTransformer().addRule(new AttributeSubsetPassThroughRule(exampleSetInput, innerExampleSetSource, this, false));
		getTransformer().addRule(new SubprocessTransformRule(getSubprocess(0)));
		getTransformer().addRule(new ExampleSetPassThroughRule(innerExampleSetSink, exampleSetOutput, SetRelation.UNKNOWN) {
			@Override
			public ExampleSetMetaData modifyExampleSet(ExampleSetMetaData inputMetaData) {
				if (getParameterAsBoolean(PARAMETER_KEEP_SUBSET_ONLY)) {
					return inputMetaData;
				} else {
					MetaData metaData = exampleSetInput.getMetaData();
					if (metaData instanceof ExampleSetMetaData) {
						inputMetaData = (ExampleSetMetaData) metaData;
						ExampleSetMetaData subsetAmd = attributeSelector.getMetaDataSubset(inputMetaData, false);

						// storing unused attributes
						List<AttributeMetaData> unusedAttributes = new LinkedList<AttributeMetaData>();
						Iterator<AttributeMetaData> iterator = inputMetaData.getAllAttributes().iterator();
						while (iterator.hasNext()) {
							AttributeMetaData amd = iterator.next();
							if (!(subsetAmd.containsAttributeName(amd.getName()) == MetaDataInfo.YES)) {
								unusedAttributes.add(amd);
							}
						}

						// retrieving result
						if (innerExampleSetSink.getMetaData() instanceof ExampleSetMetaData) {
							ExampleSetMetaData resultMetaData = (ExampleSetMetaData) innerExampleSetSink.getMetaData().clone();

							// merge result with unusedAttributes: restore special types
							Iterator<AttributeMetaData> r = resultMetaData.getAllAttributes().iterator();
							while (r.hasNext()) {
								AttributeMetaData newMetaData = r.next();
								AttributeMetaData oldMetaData = inputMetaData.getAttributeByName(newMetaData.getName());
								if (oldMetaData != null) {
									if (oldMetaData.isSpecial()) {
										String specialName = oldMetaData.getRole();
										newMetaData.setRole(specialName);
									}
								}
							}

							// add unused attributes again
							resultMetaData.addAllAttributes(unusedAttributes);
							return resultMetaData;
						}
					}
					return inputMetaData;
				}
			}
		});
		getTransformer().addRule(innerResultPorts.makePassThroughRule());
		innerResultPorts.start();
	}

	@Override
	public void doWork() throws OperatorException {
		ExampleSet inputSet = exampleSetInput.getData(ExampleSet.class);
		ExampleSet workingExampleSet = (ExampleSet)inputSet.clone();
		Set<Attribute> selectedAttributes = attributeSelector.getAttributeSubset(workingExampleSet, false);

		List<Attribute> unusedAttributes = new LinkedList<Attribute>();
		Iterator<Attribute> iterator = workingExampleSet.getAttributes().allAttributes();
		while (iterator.hasNext()) {
			Attribute attribute = iterator.next();
			if (!selectedAttributes.contains(attribute)) {
				unusedAttributes.add(attribute);
				iterator.remove();
			}
		}

		// converting special to normal
		workingExampleSet = new NonSpecialAttributesExampleSet(workingExampleSet);

		// perform inner operators
		innerExampleSetSource.deliver(workingExampleSet);
		getSubprocess(0).execute();

		// retrieve transformed example set
		ExampleSet resultSet = innerExampleSetSink.getData(ExampleSet.class);

		// transform special attributes back
		Iterator<AttributeRole> r = resultSet.getAttributes().allAttributeRoles();
		while (r.hasNext()) {
			AttributeRole newRole = r.next();
			AttributeRole oldRole = inputSet.getAttributes().getRole(newRole.getAttribute().getName());
			if (oldRole != null) {
				if (oldRole.isSpecial()) {
					String specialName = oldRole.getSpecialName();
					newRole.setSpecial(specialName);
				}
			}
		}

		// add old attributes if desired
		if (!getParameterAsBoolean(PARAMETER_KEEP_SUBSET_ONLY)) {
			if (resultSet.size() != inputSet.size()) {
				throw new UserError(this, 127, "changing the size of the example set is not allowed if the non-processed attributes should be kept.");
			}

			if (resultSet.getExampleTable().equals(inputSet.getExampleTable())) {
				for (Attribute attribute : unusedAttributes) {
					AttributeRole role = inputSet.getAttributes().getRole(attribute);
					resultSet.getAttributes().add(role);
				}
			} else {
				logWarning("Underlying example table has changed: data copy into new table is necessary in order to keep non-processed attributes.");
				for (Attribute oldAttribute : unusedAttributes) {
					AttributeRole oldRole = inputSet.getAttributes().getRole(oldAttribute);

					// create and add copy of attribute 
					Attribute newAttribute = (Attribute)oldAttribute.clone();
					resultSet.getExampleTable().addAttribute(newAttribute);
					AttributeRole newRole = new AttributeRole(newAttribute);
					if (oldRole.isSpecial())
						newRole.setSpecial(oldRole.getSpecialName());
					resultSet.getAttributes().add(newRole);

					// copy data for the new attribute
					Iterator<Example> oldIterator = inputSet.iterator();
					Iterator<Example> newIterator = resultSet.iterator();
					while (oldIterator.hasNext()) {
						Example oldExample = oldIterator.next();
						Example newExample = newIterator.next();
						newExample.setValue(newAttribute, oldExample.getValue(oldAttribute));
					}
				}
			}
		} 

		// add all other results if desired
		innerResultPorts.passDataThrough();

		// deliver example set
		exampleSetOutput.deliver(resultSet);
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.addAll(attributeSelector.getParameterTypes());

		types.add(new ParameterTypeBoolean(PARAMETER_KEEP_SUBSET_ONLY, "Indicates if the attributes which did not match the regular expression should be removed by this operator.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_DELIVER_INNER_RESULTS, "Indicates if the additional results (other than example set) of the inner operator should also be returned.", false));

		return types;
	}
	
	@Override
	public ResourceConsumptionEstimator getResourceConsumptionEstimator() {
		return OperatorResourceConsumptionHandler.getResourceConsumptionEstimator(getInputPorts().getPortByIndex(0), AttributeSubsetPreprocessing.class, attributeSelector);
	}
}
