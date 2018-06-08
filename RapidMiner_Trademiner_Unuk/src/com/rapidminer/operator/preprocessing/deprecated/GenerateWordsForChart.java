package com.rapidminer.operator.preprocessing.deprecated;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
//import com.rapidminer.operator.text.WordList;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;

public class GenerateWordsForChart extends Operator {
	private InputPort exampleSetInput = getInputPorts().createPort("example set");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");

	public static final String PARAMETER_FILE = "file";

	public GenerateWordsForChart(OperatorDescription description) {
		super(description);
		getTransformer().addPassThroughRule(exampleSetInput, exampleSetOutput);

	}

	public void doWork() throws OperatorException {

		ExampleSet input = exampleSetInput.getData(ExampleSet.class);

		Map<String, Map<String, Integer>> counts = countWords(input);

		
		File file = getParameterAsFile(PARAMETER_FILE);
		PrintWriter writer=null;
		Set <String>labels = counts.keySet();
		try {
			for (String label:labels){
				Map<String , Integer> wordCount = counts.get(label);
				writer = new PrintWriter(file.getAbsolutePath().replace(".","_"+ label+"."));
				Set <String>words = wordCount.keySet();
				for (String word: words)
				{
					int count= wordCount.get(word);
					for (int i=0;i<count;i++){
						writer.print(word+" ");
					}
					writer.println();
					writer.flush();
				}
				writer.close();	
					
			}
		} catch (FileNotFoundException e) {
			throw new OperatorException("File not found: " + e.getMessage());
		}
		finally {
			if (writer!=null)
			{
				writer.close();
			}
			
		}
		

		
	}

	private Map<String, Map<String, Integer>> countWords(ExampleSet input) {

		Map<String, Map<String, Integer>> counts = new HashMap<String, Map<String, Integer>>();

		Attributes atts = input.getAttributes();
		Attribute labelAtt = input.getAttributes().getLabel();

		for (Example e : input) {

			String labelValue = e.getValueAsString(labelAtt);

			Map<String, Integer> wordCount;
			if (!counts.containsKey(labelValue)) {
				counts.put(labelValue, wordCount = new HashMap<String, Integer>());
			} else {
				wordCount = counts.get(labelValue);
			}

			Iterator<Attribute> iterator = atts.iterator();
			while (iterator.hasNext()) {
				Attribute att = iterator.next();

				if (!wordCount.containsKey(att.getName())) {
					wordCount.put(att.getName(), 0);
				} else {
					wordCount.put(att.getName(), wordCount.get(att.getName()) + (int) e.getValue(att));
				}
			}

		}

		return counts;

	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_FILE, "Filename for the word archives.", "fil", false));

		return types;
	}

}
