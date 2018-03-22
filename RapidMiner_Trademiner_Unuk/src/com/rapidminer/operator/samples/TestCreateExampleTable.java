package com.rapidminer.operator.samples;

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.tools.Ontology;

public class TestCreateExampleTable {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		List <Attribute> attributes=new ArrayList<Attribute>();
		int size=10;
		for (int i=0;i<size;i++){
			
			attributes.add(AttributeFactory.createAttribute(
					"v" +i,Ontology.REAL));
					
		}
		Attribute label= AttributeFactory.createAttribute("label",Ontology.NOMINAL);
		attributes.add(label);
		MemoryExampleTable table= new MemoryExampleTable(attributes);
		for (int d=0;d<100;d++)
		{
			double data[]= new double[attributes.size()];
			//fill double array
			
			data[data.length-1]=label.getMapping().mapString("0");
			table.addDataRow(new DoubleArrayDataRow(data));
		
		  
		}
		ExampleSet exampleSet = table.createExampleSet();

	}

}
