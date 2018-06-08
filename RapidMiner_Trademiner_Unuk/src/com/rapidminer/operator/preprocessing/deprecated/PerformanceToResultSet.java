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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.trademiner.util.ConnectionFactory;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.Averagable;

/**
 * Transforms a Peformance input into a ExampleSet.
 * 
 * @author Marcelo Beckmann
 */
public class PerformanceToResultSet extends Operator {

	
	
	private InputPort performanceInput = getInputPorts().createPort("performance");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");
	public static final String PARAMETER_EXPERIMENT_SYMBOL = "experiment_symbol";
	public static final String PARAMETER_CREATE_CONFUSION_MATRIX = "create_confusion_matrix";
	public static final String PARAMETER_WRITE_LABELS = "write_labels";
	public static final String PARAMETER_DATABASE_URL = "database_url";
	public static final String PARAMETER_EXPERIMENT_DESCRIPTION = "experiment_description";
	/**
	 * Constructor
	 */
	public PerformanceToResultSet(OperatorDescription description) {
		super(description);
		getTransformer().addPassThroughRule(performanceInput, exampleSetOutput);
	
	}
	
	public void doWork() throws OperatorException {
		

		PerformanceVector inputPerformance = performanceInput.getData(PerformanceVector.class);
		
		inputPerformance.initWriting();
		//System.out.println("#####-################\n"+ inputPerformance.toString());
		
		boolean writeLabels =getParameterAsBoolean(PARAMETER_WRITE_LABELS);
		

		boolean createConfusionMatrix =getParameterAsBoolean(PARAMETER_CREATE_CONFUSION_MATRIX);
		String labels[] = ParseLabels(inputPerformance.getAveragable(0));

		//if (createConfusionMatrix) 
///	{
			
			double[][] d= ParseValues(labels, inputPerformance.getAveragable(0));
			
		//}
			//System.out.println("#####-################ LABELS:"+ Arrays.toString(labels));
			//System.out.println("#####-################ CONFUSION MATRIX:"+ Arrays.toString(d[0])+"\n" + Arrays.toString(d[1]));
		ExampleSet outputSet = createStructure(labels);
		MemoryExampleTable table  = (MemoryExampleTable)outputSet.getExampleTable();

		for (int i=0;i<inputPerformance.getSize();i++) {


		
		DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_SPARSE_ARRAY, '.');
		DataRow row=null;
	

		Attribute idAttribute = outputSet.getAttributes().get("EXPERIMENT_ID");
	
		Attribute textAttribute = outputSet.getAttributes().get("SYMBOL");
		row = factory.create(table.getNumberOfAttributes());
		String nominalValue =getParameterAsString(PARAMETER_EXPERIMENT_SYMBOL);
		
		int id = createExperimentDescrition();

		row.set(textAttribute, textAttribute.getMapping().mapString(nominalValue));

		if (writeLabels)
		{
			Attribute alabels = outputSet.getAttributes().get("LABELS");
			row.set(alabels, alabels.getMapping().mapString(Arrays.toString(labels)));
		}
		//row.set(attribute, value)
		
		//double d[]=new double[inputPerformance.getSize()];
		int j=0;
		int r=0;
		int confusionMatrix= labels.length * labels.length +1-1;
		for (Attribute att : outputSet.getAttributes()) {
		
			if (j>=outputSet.getAttributes().size()-1)
			{
				break;
				
			}
			//TODO IMPROVE THIS, DON'T USE THE ORDINAL POSITION OF ATTRIBUTES USE ITS NAMES
			Averagable avg=null;
			if (createConfusionMatrix) {
				
				if (j<=confusionMatrix) {
					//if (j>0)
					{
						
						double ii= ((double)j-1)/labels.length;
						double dd= (ii-((int)ii));
						int jj = (int)(dd*labels.length+0.00001);
						row.set(att,d[(int)ii][jj]);
					}
					
					j++;
					continue;
				}
				avg=null;
				/*
				try {
					if (r<inputPerformance.size())
						avg = inputPerformance.getAveragable(r++);
					//avg = inputPerformance.getAveragable(j-confusionMatrix-1);
				} catch (Exception e){avg=null;}
				 */
			}
			else {
				if (j<inputPerformance.size())
					avg = inputPerformance.getAveragable(j);
			}
			
			if (avg!=null) {
				row.set(att,avg.getAverage());	
			}
			j++;
		}
		
		for (r=0;r<inputPerformance.size();r++) {
			Averagable avg = inputPerformance.getAveragable(r++);
			Attribute att = outputSet.getAttributes().get(avg.getName());
			//TODO GET THE AVERAGE HERE
			row.set(att,avg.getAverage());
		} 
		
		row.set(idAttribute, id);
			
		table.addDataRow(row);
		}
		
		exampleSetOutput.deliver(outputSet);
			
		
	}	  

private ExampleSet createStructure(String labels[]) throws OperatorException {
	
	PerformanceVector inputPerformance = performanceInput.getData(PerformanceVector.class);
	List <Attribute> attributes=new ArrayList<Attribute>();

	//String symbol =getParameterAsString(PARAMETER_EXPERIMENT_SYMBOL);
	
	
	boolean createConfusionMatrix =getParameterAsBoolean(PARAMETER_CREATE_CONFUSION_MATRIX);
	
	if (createConfusionMatrix) {
		for (int i=0;i<labels.length;i++)
		{
			for (int j=0;j<labels.length;j++)
			{
				attributes.add(AttributeFactory.createAttribute(
						labels[i]+" x "+labels[j] ,Ontology.REAL));
			}
			
		}
	}
	for (int j = 0; j < inputPerformance.getSize(); j++) {
		Averagable avg = inputPerformance.getAveragable(j);
		
	
		attributes.add(AttributeFactory.createAttribute(
				avg.getName(),Ontology.REAL));
	
		
	}

	attributes.add(AttributeFactory.createAttribute(	"EXPERIMENT_ID", Ontology.INTEGER));

	Attribute textAttribute=AttributeFactory.createAttribute(	"SYMBOL", Ontology.STRING);
	attributes.add(textAttribute);
	
	boolean writeLabels =getParameterAsBoolean(PARAMETER_WRITE_LABELS);
	if (writeLabels)
	{
		attributes.add(AttributeFactory.createAttribute("LABELS", Ontology.STRING));
	}
	MemoryExampleTable table= new MemoryExampleTable(attributes);
			ExampleSet exampleSet = table.createExampleSet();


			
	return exampleSet;

}

/*
	@Override
	public void doWork() throws OperatorException {
		

		PerformanceVector inputPerformance = performanceInput.getData(PerformanceVector.class);

		inputPerformance.initWriting();
		System.out.println("#####-################\n"+ inputPerformance.toString());
		
		String labels[] = ParseLabels(inputPerformance.getAveragable(0));

		double[][] d= ParseValues(labels, inputPerformance.getAveragable(0));
		ExampleSet outputSet = createStructure(labels);
		
		MemoryExampleTable table  = (MemoryExampleTable)outputSet.getExampleTable();

		String symbol =getParameterAsString(PARAMETER_EXPERIMENT_SYMBOL);
		
		
		DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_SPARSE_ARRAY, '.');
		DataRow row=null;
	

		Attribute experimentId = outputSet.getAttributes().get("EXPERIMENT_ID");
		
		
		
		
		int id = createExperimentDescrition();
		

		
		
		Attribute textAttribute = outputSet.getAttributes().get("SYMBOL");
		//NominalMapping bm = new BinominalMapping();
		//bm.setMapping(symbol,0);
		//textAttribute.setMapping(bm);
		//bm.setMapping(symbol, 0);
		
		row = factory.create(table.getNumberOfAttributes());
		
		row.set(experimentId,   id);
		row.set(textAttribute,   textAttribute.getMapping().mapString(symbol));

		int j=0;
try {
		int confusionMatrix= labels.length * labels.length +1-1;
		for (Attribute att : outputSet.getAttributes()) {
			if (j<=confusionMatrix) {
				if (j>0)
				{
					
					double ii= ((double)j-1)/labels.length;
					double dd= (ii-((int)ii));
					int jj = (int)(dd*labels.length+0.00001);
					row.set(att,d[(int)ii][jj]);
				}
				
				j++;
				continue;
			}

			Averagable avg = inputPerformance.getAveragable(j-confusionMatrix-1);
			
			row.set(att,avg.getAverage());	
			//	d[j]= avg.getAverage();
				j++;
		}
}
catch (Exception e)
{
	e.printStackTrace();
}
		table.addDataRow(row);
			
		
		exampleSetOutput.deliver(outputSet);
			
		
	}	
	
	private ExampleSet createStructure(String labels[]) throws OperatorException {
	
		PerformanceVector inputPerformance = performanceInput.getData(PerformanceVector.class);
		List <Attribute> attributes=new ArrayList<Attribute>();

		attributes.add(AttributeFactory.createAttribute(	"EXPERIMENT_ID", Ontology.INTEGER));

		attributes.add(AttributeFactory.createAttribute(	"SYMBOL", Ontology.STRING));

		
		for (int i=0;i<labels.length;i++)
		{
			for (int j=0;j<labels.length;j++)
			{
				attributes.add(AttributeFactory.createAttribute(
						labels[i]+" x "+labels[j] ,Ontology.REAL));
			}
			
		}
		for (int j = 0; j < inputPerformance.getSize(); j++) {
			Averagable avg = inputPerformance.getAveragable(j);
			
		
			attributes.add(AttributeFactory.createAttribute(
					avg.getName(),Ontology.REAL));
		
			
		}
		
		MemoryExampleTable table= new MemoryExampleTable(attributes);
				ExampleSet exampleSet = table.createExampleSet();

		return exampleSet;
	
	}
	
*/
	
	private String[] ParseLabels(Averagable avg)
	{
		StringTokenizer st = new StringTokenizer(avg.toString(),"\r\n");
		boolean hasNextLbl=false;
		while (st.hasMoreElements())
		{
			String line = st.nextToken();
			if (line.startsWith("ConfusionMatrix"))
			{
				hasNextLbl=true;
				continue;
			}
			if (hasNextLbl)
			{
				StringTokenizer st2 = new StringTokenizer(line);
				List <String> list = new ArrayList<String>();
				while (st2.hasMoreElements())
				{
					list.add(st2.nextToken());
				}
				String array[]= new String[list.size()-1];
				for (int i=1;i<list.size();i++)
				{
					array[i-1]=list.get(i);
					
				}
				return array;
			}
			
		}
		
		
		return null;
		
	}
	
	private double[][] ParseValues(String [] labels, Averagable avg)
	{
		double [][]d=new double[labels.length][labels.length];
		
		StringTokenizer st = new StringTokenizer(avg.toString(),"\r\n");
		boolean hasNextLbl=false;
		int i=0;
		while (st.hasMoreElements())
		{
			String line = st.nextToken();
			if (line.startsWith("True"))
			{
				hasNextLbl=true;
				continue;
			}
			if (hasNextLbl)
			{
				StringTokenizer st2 = new StringTokenizer(line);
				int j=-1;
				while (st2.hasMoreElements())
				{
					if (j==-1) {
						j=0;
						st2.nextToken();
						continue;
					}
					String value = st2.nextToken();
					d[i][j++]=Double.parseDouble(value);
				}
				i++;
				
				
			}
			if (i>=labels.length)
				break;
			
		}
		
		
		return d;
		
	}
	
	
	private int createExperimentDescrition()
	{ 
		
		
		
		Connection conn=null;
		
		Statement stmt=null;
		
		ResultSet rs = null;
		int id=0;
		try {
			String description =getParameterAsString(PARAMETER_EXPERIMENT_DESCRIPTION);
			ConnectionFactory.URL = getParameterAsString(PARAMETER_DATABASE_URL);
			conn = ConnectionFactory.getConnection();
			stmt = conn.createStatement(); 
			
			rs = stmt.executeQuery("select id from experiment where description='"  + description +  "'");
			
			if (rs.next())
			{
				return rs.getInt(1);
				
			}
			//ConnectionFactory.closeConnection(rs, stmt, null);
			
			
			String sql="insert into experiment (description) values ('"+description+"')";
			//stmt= conn.createStatement(sql,Statement.RETURN_GENERATED_KEYS);
			
			//stmt.setString(1,description);
			stmt.executeUpdate(sql,Statement.RETURN_GENERATED_KEYS);
			
			
			rs = stmt.getGeneratedKeys();
			rs.next();
			id = rs.getInt(1);
			
		}
		
		catch (Exception e)
		{
			
			e.printStackTrace();
		}
		finally {
			
			
			ConnectionFactory.closeConnection(rs, stmt, conn);
		}

		return id;
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		
			types.add(new ParameterTypeString(PARAMETER_EXPERIMENT_DESCRIPTION, "The experiment description.", true));
			types.add(new ParameterTypeString(PARAMETER_EXPERIMENT_SYMBOL, "The experiment stock symbol.","%{symbol_}"));
			types.add(new ParameterTypeString(PARAMETER_DATABASE_URL, "The database url.","jdbc:mysql://%{db_host}/trademiner"));
			types.add(new ParameterTypeBoolean(PARAMETER_CREATE_CONFUSION_MATRIX, "Defines if a confusion matrix will be created with the performance exampleset.",false));			
			types.add(new ParameterTypeBoolean(PARAMETER_WRITE_LABELS, "Defines if the labels used in classification will be added to the performance exampleset.",true));
			
		return types;
	}

}