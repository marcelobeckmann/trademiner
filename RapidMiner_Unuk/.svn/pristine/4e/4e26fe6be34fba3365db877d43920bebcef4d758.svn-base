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
package com.rapidminer.operator.ports.metadata;

import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;

/** Delivers the union of two example sets to an output port. If a prefix is specified,
 *  duplicate names will be renamed. Otherwise, duplicates are skipped.
 * 
 * @author Simon Fischer
 */
public class ExampleSetUnionRule implements MDTransformationRule {

	private final InputPort inputPort1;
	private final InputPort inputPort2;
	private final OutputPort outputPort;
	private final String prefixForDuplicates;

	public ExampleSetUnionRule(InputPort inputPort1, InputPort inputPort2, OutputPort outputPort, String prefixForDuplicates) {
		this.inputPort1 = inputPort1;
		this.inputPort2 = inputPort2;
		this.outputPort = outputPort;
		this.prefixForDuplicates = prefixForDuplicates;
	}

	protected String getPrefix() {
		return prefixForDuplicates;
	}

	@Override
	public void transformMD() {
		MetaData md1 = inputPort1.getMetaData();
		MetaData md2 = inputPort2.getMetaData();
		if ((md1 != null) && (md2 != null)) {
			if ((md1 instanceof ExampleSetMetaData) && (md2 instanceof ExampleSetMetaData)) {
				ExampleSetMetaData emd1 = (ExampleSetMetaData) md1;
				ExampleSetMetaData newEMD = (emd1.clone()).joinAttributes((ExampleSetMetaData)md2, getPrefix());
				for (AttributeMetaData possibleNew : ((ExampleSetMetaData)md2).getAllAttributes()) {
					if (emd1.containsAttributeName(possibleNew.getName()) != MetaDataInfo.YES) {
						transformAddedAttributeMD(newEMD, newEMD.getAttributeByName(possibleNew.getName()));
					}
				}
				outputPort.deliverMD(newEMD);
			} else {	
				outputPort.deliverMD(new ExampleSetMetaData());
			}
		} else {
			outputPort.deliverMD(null);
		}
	}

	protected void transformAddedAttributeMD(ExampleSetMetaData emd, AttributeMetaData newAttribute) {

	}

}
