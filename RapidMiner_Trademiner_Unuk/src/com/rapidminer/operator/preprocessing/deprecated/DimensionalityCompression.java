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
package com.rapidminer.operator.preprocessing.deprecated;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.example.table.NumericalAttribute;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;

/**
 * This is the Numerical2Date tutorial operator.
 * 
 * @author Marcelo Beckmann
 */
public class DimensionalityCompression extends Operator {

	private InputPort exampleSetInput = getInputPorts().createPort("example set");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");
	private int newSize;
	/**
	 * Constructor
	 */
	public DimensionalityCompression(OperatorDescription description) {
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
		try {	
			
		ExampleSet outputSet = createStructure();
		
		MemoryExampleTable table  = (MemoryExampleTable)outputSet.getExampleTable();
		
		//System.out.println("oldAtts.size:"+inputSet.getAttributes().size()+"/"+ inputSet.getAttributes().specialSize());
		
		
		int i=0;
		for (Example example : inputSet) {
		
			//Attribute label = example.getAttributes().getLabel();
			
			//int lbl = (int)example.getLabel();
			
					
			double[] d = compress(example,outputSet);
			
			table.addDataRow(new DoubleArrayDataRow(d));

			i++;
		}

		exampleSetOutput.deliver(outputSet);
	}
	catch (Exception e) { e.printStackTrace(); }
	}


	private double[] compress(Example example,ExampleSet outputSet) {
		Attributes oldAtts = example.getAttributes();
		//System.out.println("2oldAtts.size:"+oldAtts.size());
		byte[] bytes = new byte[oldAtts.size()];
		int i=0;
		for (Attribute at :oldAtts ) {
			if (!at.isNominal()) {
				byte b = (byte)(example.getValue(at)*100.0);
				bytes[i]=  b;
				i++;
			}
		}
		double[] newDoubles = new double[newSize+1];
		Attributes newAttributes = outputSet.getAttributes();
		ByteBuffer buf2 = ByteBuffer.wrap(bytes);
		
		Iterator<AttributeRole> s = example.getAttributes().specialAttributes();
        Attribute label=null;
        
        //TODO SIMPLIFY THIS ONE
        while (s.hasNext()) {
        	label = s.next().getAttribute();
        	System.out.println("@@@@"+label.getName());
        }
		
		Object lbl= getValueWithCorrectClass(example,label);    //example.getNominalValue(label);
		
		//System.out.println("####"+ lbl);
		newDoubles[0]= label.getMapping().mapString(lbl.toString());
		
		for (i = 1; i < newSize; i++) {
			// This is returning a NAN value
			// double d = Double.longBitsToDouble(l);
			Attribute att= newAttributes.get("v" +i);
			
		
			long l = buf2.getLong(i * 8);
			newDoubles[i] = l;
			
		
		}
		bytes=null;

		return newDoubles;
		
	
	}
  private Object getValueWithCorrectClass(Example example, Attribute attribute) {
    	// TODO consolidate value string formatting which is spread widely among this model, the DataViewerTable, and a ColoredTableCellRenderer
        try {
        	double value = example.getValue(attribute);
    		if (Double.isNaN(value)) {
    			return "?";
    		}
        	if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), Ontology.DATE_TIME)) {
        		// simply return date, format is evaluated directly in jtable, see todo above
        		return new Date((long) value);
        	} else if (attribute.isNumerical()) {
                return Double.valueOf(example.getValue(attribute));
        	} else {
                return example.getValueAsString(attribute, NumericalAttribute.DEFAULT_NUMBER_OF_DIGITS, false);
        	}
        } catch (Throwable e) {
            //LogService.getGlobal().logWarning("Cannot show correct value: " + e.getMessage());
            LogService.getRoot().log(Level.WARNING, "com.rapidminer.gui.viewer.DataViewerTableModel.showing_correct_value_error", e.getMessage());           
            return "Error";
        }
    }
	  
	private ExampleSet createStructure() throws OperatorException {
	
		ExampleSet oldExampleSet = exampleSetInput.getData(ExampleSet.class);
		Attributes oldAttributes = oldExampleSet.getAttributes();
		newSize = oldAttributes.size()/8;
		List <Attribute> attributes=new ArrayList<Attribute>();
		
		Attribute label = oldAttributes.getLabel();
		attributes.add(label);
		for (int i=0;i<newSize;i++){
			attributes.add(AttributeFactory.createAttribute(
					"v" +i,Ontology.REAL));
					
		}
		MemoryExampleTable table= new MemoryExampleTable(attributes);
		/*for (int d=0;d<100;d++)
		{
			double data[]= new double[attributes.size()];
			//fill double array
			
			data[data.length-1]=label.getMapping().mapString("0");
			table.addDataRow(new DoubleArrayDataRow(data));
		
		
		}*/
		ExampleSet exampleSet = table.createExampleSet();

		exampleSet.getAttributes().setLabel(label);
		return exampleSet;
	
	}
}