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
package com.rapidminer.operator.collections;

import java.util.List;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.IOObjectCollection;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.CollectingPortPairExtender;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.CollectionMetaData;
import com.rapidminer.operator.ports.metadata.MDTransformationRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.SubprocessTransformRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;

/** Iterates over a collection and executes the subprocess on each element.
 *  The outputs of the subprocesses are collected and returned as collections.
 * 
 * @author Simon Fischer
 *
 */
public class CollectionIterationOperator extends OperatorChain {

	protected static final String PARAMETER_UNFOLD = "unfold";

	private final InputPort collectionInput = getInputPorts().createPort("collection", new CollectionMetaData(new MetaData()));
	private final OutputPort singleInnerSource = getSubprocess(0).getInnerSources().createPort("single");
	private final CollectingPortPairExtender outExtender = new CollectingPortPairExtender("output", getSubprocess(0).getInnerSinks(), getOutputPorts()); 

	public CollectionIterationOperator(OperatorDescription description) {
		super(description, "Iteration");
		outExtender.start();
		getTransformer().addRule(new MDTransformationRule() {
			@Override
			public void transformMD() {
				MetaData md = collectionInput.getMetaData();
				if ((md != null) && (md instanceof CollectionMetaData)) {
					if (getParameterAsBoolean(PARAMETER_UNFOLD)) {
						singleInnerSource.deliverMD(((CollectionMetaData) md).getElementMetaDataRecursive());
					} else {
						singleInnerSource.deliverMD(((CollectionMetaData) md).getElementMetaData());
					}
				} else {
					singleInnerSource.deliverMD(null);
				}
			}
		});
		getTransformer().addRule(new SubprocessTransformRule(getSubprocess(0)));
		getTransformer().addRule(outExtender.makePassThroughRule());
	}

	@Override
	public void doWork() throws OperatorException {
		IOObjectCollection<IOObject> data = collectionInput.getData(IOObjectCollection.class);
		List<IOObject> list;
		if (getParameterAsBoolean(PARAMETER_UNFOLD)) {
			list = data.getObjectsRecursive(); 
		} else {
			list = data.getObjects();
		}
		outExtender.reset();
		for (IOObject o : list) {
			singleInnerSource.deliver(o);
			getSubprocess(0).execute();
			outExtender.collect();
		}
		//outExtender.passDataThrough();
	}

	@Override
	public List<ParameterType> getParameterTypes() {		
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean(PARAMETER_UNFOLD, "Determines if the input collection is unfolded.", false));
		return types;
	}
}
