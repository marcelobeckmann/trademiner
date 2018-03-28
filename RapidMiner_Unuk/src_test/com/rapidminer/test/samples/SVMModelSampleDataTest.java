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
package com.rapidminer.test.samples;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.MissingIOObjectException;

/**
 * Tests for the Data of an ExampleSet
 *
 * @author Marcin Skirzynski
 */
public class SVMModelSampleDataTest extends OperatorDataSampleTest {
	
	//private double[] expectedValues;


	public SVMModelSampleDataTest(String file, double[] expectedValues) {
		super(file);
		//this.expectedValues = expectedValues;
	}
	
	@Override
	public void checkOutput(IOContainer output) throws MissingIOObjectException {
		/*
		ContainerModel containerModel = output.get(ContainerModel.class);
		
		if (containerModel.getModel(0).getClass().getName().equals("com.rapidminer.operator.learner.functions.kernel.evosvm.EvoSVMModel")) {
			EvoSVMModel model = containerModel.getModel(EvoSVMModel.class);
			for(int i=0;i<expectedValues.length;i++){
				assertEquals(model.getFunctionValue(i),expectedValues[i]);
			}
		}

		if (containerModel.getModel(0).getClass().getName().equals("com.rapidminer.operator.learner.functions.kernel.GPModel")) {
			GPModel model = containerModel.getModel(GPModel.class);
			for(int i=0;i<expectedValues.length;i++){
				assertEquals(model.getFunctionValue(i),expectedValues[i]);
			}
		}
		
		if (containerModel.getModel(0).getClass().getName().equals("com.rapidminer.operator.learner.functions.kernel.JMySVMModel")) {
			JMySVMModel model= containerModel.getModel(JMySVMModel.class);
			for(int i=0;i<expectedValues.length;i++){
				assertEquals(model.getFunctionValue(i),expectedValues[i]);
			}
		}
		

		if (containerModel.getModel(0).getClass().getName().equals("com.rapidminer.operator.learner.functions.kernel.LibSVMModel")) {
			LibSVMModel model= containerModel.getModel(LibSVMModel.class);
			for(int i=0;i<expectedValues.length;i++){
				assertEquals(model.getAlpha(i),expectedValues[i]);
			}
		}
		
		if (containerModel.getModel(0).getClass().getName()=="com.rapidminer.operator.learner.functions.kernel.RVMModel"){
			RVMModel model= containerModel.getModel(RVMModel.class);
			for(int i=0;i<expectedValues.length;i++){
				assertEquals(model.getAlpha(i),expectedValues[i]);
			}
		}
		
		if (containerModel.getModel(0).getClass().getName()=="com.rapidminer.operator.learner.functions.kernel.MyKLRModel"){
			MyKLRModel model= containerModel.getModel(MyKLRModel.class);
			assertEquals(model.getFunctionValue(0),expectedValues[0]);
			for(int i=1;i<expectedValues.length;i++){
				assertEquals(model.getAlpha(i-1),expectedValues[i]);
			}
		}
		*/		
	}
}
