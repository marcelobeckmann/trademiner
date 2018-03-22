package com.rapidminer.operator.preprocessing.transformation;

/*
 *  RapidMiner Text Processing Extension
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeString;


/**
 * Filters terms specified in an external file. The file must contain one term per line.
 * 
 * @author Sebastian Land
 */
public class RemoveWordsNotInDictionary extends Operator {

	private static final String PARAMETER_TEXT_ATTRIBUTE = "text_attribute";
	private static final String [] punctuation={",",".",";","!",":","?","@","&","#","(",")","-","$"};
	private InputPort exampleSetInput = getInputPorts().createPort("example set");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");
		
	
	public static final String PARAMETER_FILE = "file";
	
	
	public RemoveWordsNotInDictionary(OperatorDescription description) {
		super(description);
	    getTransformer().addPassThroughRule(exampleSetInput, exampleSetOutput);
	    
	
	}
    
    private Set <String>dictionary = new HashSet<String>();
    
	private void loadDictionary() throws Exception
	{
		dictionary.clear();
		InputStreamReader reader=null;
		try {
				reader = new InputStreamReader(new FileInputStream(getParameterAsFile(PARAMETER_FILE))); //, Encoding.getEncoding(this));
				BufferedReader br = new BufferedReader(reader);
				String line=null;
				while ((line=br.readLine())!=null)
				{
					dictionary.add(line.toLowerCase());
				}
				
				for (String p : punctuation)
					dictionary.add(p);
			
		}
		finally {
			
			if (reader!=null)
				reader.close();
			
		}
	}
	public void doWork() throws OperatorException {
	
	    ExampleSet input = exampleSetInput.getData(ExampleSet.class); 

	    String attName = getParameterAsString(PARAMETER_TEXT_ATTRIBUTE);
		Attribute att = input.getAttributes().get(attName);
	    
		try {
			loadDictionary();
			
			for(Example e : input)
			{
				
				processTokens(e,att);
				
			}
			
		
		} catch (FileNotFoundException e) {
			throw new UserError(this, 301, getParameterAsFile(PARAMETER_FILE).getPath());
		} catch (IOException e) {
			throw new UserError(this, 302, getParameterAsFile(PARAMETER_FILE).getPath(), e.getMessage());
		
		} catch (Exception e) {
		throw new UserError(this, 303, "", e.getMessage());
		}
		exampleSetOutput.deliver(input);
	}
	protected void processTokens(Example example,Attribute att) throws IOException
	{

		BufferedReader br = new BufferedReader(new StringReader(example.getValueAsString(att)));
		StringBuilder sb= new StringBuilder();	
		String line;
		
		while ((line=br.readLine())!=null) {
		
			StringTokenizer st= new StringTokenizer(line," ");

				
			while (st.hasMoreElements())
			{
				String tk = st.nextToken();
				//TODO CHECK IF THE LAST CHAR IS S
				if (dictionary.contains(tk.toLowerCase()))
				{
					sb.append(tk);
					sb.append(' ');
				}
			
			}
			sb.append("\r\n");
		}
		example.setValue(att,sb.toString());
		sb=null;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_FILE, "Filename for the word dictionary (one word per line).", "fil", false));

		types.add(new ParameterTypeString(PARAMETER_TEXT_ATTRIBUTE, "The name of the text attribute.", false));
		
		
		return types;
	}
}
