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
package com.rapidminer.operator.learner.test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowReader;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.learner.Learner;
import com.rapidminer.operator.learner.functions.kernel.RVMLearner;
import com.rapidminer.test.TestContext;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorService;

/** Creates all learners using the {@link OperatorService} and constructs input example sets
 *  according to their capabilities to check whether they operate without throwing exceptions /
 *  throwing the correct exceptions.
 *   
 *  @author Simon Fischer, Marcin Skirzynski, Marius Helf
 * */
public class LearnerTest extends TestCase {

	private static final int NUM_EXAMPLES = 100;
	
	/** Seed for creating random numbers. -1 means "random" random seed. */
	private static long RANDOM_SEED = -1;
	
	private OperatorDescription opDesc;	
	
	private Random random;

	
	public LearnerTest(OperatorDescription opDesc) {
		super("learnerTest");
		this.opDesc = opDesc;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TestContext.get().initRapidMiner();
		
		if (RANDOM_SEED == -1) {
			random = new Random();
		} else {
			random = new Random(RANDOM_SEED);
		}
	}

	@Override
	public String getName() {
		return "Learner "+opDesc.getName() + " - "+opDesc.getKey()+" - "+opDesc.getOperatorClass();
		//return "Test_learner_"+opDesc.getName(); // + " ("+opDesc.getKey()+", "+opDesc.getOperatorClass()+")";
	}
	
	public void learnerTest() throws Exception {
		Learner learner = (Learner) OperatorService.createOperator(opDesc);
		MemoryExampleTable exTable = new MemoryExampleTable();
		for (int i = 0; i < NUM_EXAMPLES; i++) {
			exTable.addDataRow(new DoubleArrayDataRow(new double[0]));
		}
		List<Attribute> regulars = new LinkedList<Attribute>();
		if (learner.supportsCapability(OperatorCapability.BINOMINAL_ATTRIBUTES)) {
			regulars.add(addBinomialAttribute(exTable));
			regulars.add(addBinomialAttribute(exTable));
		}
		if (learner.supportsCapability(OperatorCapability.POLYNOMINAL_ATTRIBUTES)) {
			regulars.add(addNominalAttribute(exTable));
			regulars.add(addNominalAttribute(exTable));
		}
		if (learner.supportsCapability(OperatorCapability.NUMERICAL_ATTRIBUTES)) {
			regulars.add(addNumericalAttribute(exTable));
			regulars.add(addNumericalAttribute(exTable));
		}
		if (regulars.isEmpty()) {
			throw new Exception("No regular attribute type supported.");
		}
		
		List<Attribute> labels = new LinkedList<Attribute>();
		if (learner.supportsCapability(OperatorCapability.POLYNOMINAL_LABEL)) {
			labels.add(addNominalAttribute(exTable));
		}
		if (learner.supportsCapability(OperatorCapability.NUMERICAL_LABEL)) {
			labels.add(addNumericalAttribute(exTable));
		}
		if (learner.supportsCapability(OperatorCapability.BINOMINAL_LABEL)) {
			labels.add(addBinomialAttribute(exTable));
		}
		if (labels.isEmpty()) {
			throw new Exception("No label type supported.");
		}
		
		for (Attribute label : labels) {
			// Some learner need special parameters for certain label types
			if( !checkLearnerCapability(learner, label) )
				continue;
			//AttributeSet attributes = new AttributeSet(regulars, Collections.singletonMap(Attributes.LABEL_NAME, label));
			//exTable.createExampleSet(specialAttributes)
			ExampleSet exampleSet = new SimpleExampleSet(exTable, regulars, Collections.singletonMap(label, Attributes.LABEL_NAME));
			//ExampleSet exampleSet = exTable.createExampleSet(attributes);
			learner.learn(exampleSet);
		}
	}

	private Attribute addBinomialAttribute(MemoryExampleTable exTable) {
		final Attribute att = AttributeFactory.createAttribute("binominal_"+(exTable.getNumberOfAttributes()+1), Ontology.BINOMINAL);
		att.getMapping().mapString("positive");
		att.getMapping().mapString("negative");
		exTable.addAttribute(att);
		DataRowReader dataRowReader = exTable.getDataRowReader();
		while (dataRowReader.hasNext()) {
			DataRow row = dataRowReader.next();
			row.set(att, random.nextInt(2));
		}
		return att;
	}
	
	private Attribute addNominalAttribute(MemoryExampleTable exTable) {
		final Attribute att = AttributeFactory.createAttribute("polynom_"+(exTable.getNumberOfAttributes()+1), Ontology.POLYNOMINAL);
		att.getMapping().mapString("one");
		att.getMapping().mapString("two");
		att.getMapping().mapString("three");
		att.getMapping().mapString("four");
		exTable.addAttribute(att);
		DataRowReader dataRowReader = exTable.getDataRowReader();
		while (dataRowReader.hasNext()) {
			DataRow row = dataRowReader.next();
			row.set(att, random.nextInt(4));
		}
		return att;
	}

	private Attribute addNumericalAttribute(MemoryExampleTable exTable) {
		final Attribute att = AttributeFactory.createAttribute("numeric_"+(exTable.getNumberOfAttributes()+1), Ontology.NUMERICAL);
		exTable.addAttribute(att);
		DataRowReader dataRowReader = exTable.getDataRowReader();
		while (dataRowReader.hasNext()) {
			DataRow row = dataRowReader.next();
			row.set(att, random.nextDouble()*10d-5d);
		}
		return att;
	}
	
	private boolean checkLearnerCapability( Learner learner, Attribute label ) {
		if( learner instanceof RVMLearner ) {
			RVMLearner rvm = (RVMLearner) learner;
			if( label.isNominal() ) {
				rvm.setParameter(RVMLearner.PARAMETER_RVM_TYPE, "1");
			}
			return true;
		}
		
		return true;
	}

}
