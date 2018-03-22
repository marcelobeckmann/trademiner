package com.rapidminer.operator.preprocessing.transformation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.ExampleSource;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;

/**
 * This performs a random instance removal from a selected class.
 * 
 * @author Marcelo Beckmann
 */

public class RandomUndersampling extends Operator {

	public static final String PARAMETER_LABEL = "label";
	public static final String PARAMETER_UNDERSAMPLING_PERCENT = "undersampling_percent";

	public static final String PARAMETER_UNDERSAMPLING_AUTO = "undersampling_auto";

	private InputPort exampleSetInput = getInputPorts().createPort(
			"example set");
	private OutputPort exampleSetOutput = getOutputPorts().createPort(
			"example set");

	/**
	 * Constructor
	 */
	public RandomUndersampling(OperatorDescription description) {
		super(description);

		getTransformer().addRule(
				new ExampleSetPassThroughRule(exampleSetInput,
						exampleSetOutput, SetRelation.EQUAL) {

					@Override
					public ExampleSetMetaData modifyExampleSet(
							ExampleSetMetaData metaData)
							throws UndefinedParameterError {

						return metaData;
					}
				});
	}

	@Override
	public void doWork() throws OperatorException {

		ExampleSet inputSet = exampleSetInput.getData();

		// Figure out the nominal index for the mapping
		Attribute label = inputSet.getAttributes().getLabel();
		if (label == null)
			throw new OperatorException(
					"Label attribute is not present. Use set role to define a label to exampleset.");

		String labelToUndersampling = this.getParameterAsString(PARAMETER_LABEL);

		ExampleSet labelExampleSet = getExampleSetByLabelValue(inputSet,labelToUndersampling);

		Attribute labelAtt = inputSet.getAttributes().getLabel();

		if (labelExampleSet == null)
			throw new OperatorException("Label " + labelToUndersampling	+ " does not exist in the exampleset.");

		int underSamplingCount = getUndersamplingCount(labelExampleSet,inputSet);
		if (labelExampleSet.size() <= underSamplingCount)
			throw new OperatorException(
					"Undersampling of "
							+ underSamplingCount
							+ " examples is  bigger than the total quantity of examples for label ("
							+ labelExampleSet.size() + ")");

		int labelIndex = label.getMapping().getIndex(labelToUndersampling);
	
		MemoryExampleTable table = (MemoryExampleTable) inputSet.getExampleTable();
		for (int i = 0; i < underSamplingCount; i++) {
			int row = (int) (Math.random() * inputSet.size());
			Example example = inputSet.getExample(row);

			int labelValue = (int) example.getValue(label);

			if (labelIndex == labelValue) 
			{

				//if (!removed.contains(row)) 
				{
				//	removed.add(row);
					table.removeDataRow(row);
					continue;
				}

			}
		//	System.out.println("£oc: "+underSamplingCount +", i:"+i+", row:"+ row);
			i--;
		}
			
		

		exampleSetOutput.deliver(inputSet);
	}

	private int getUndersamplingCount(ExampleSet toUndersampling, ExampleSet wholeSet) throws UndefinedParameterError 
	{
		int undersamplingCount ;
		
		//fixed percent to undersampling, the basis is the size of class to undersample
		if (!this.getParameterAsBoolean(this.PARAMETER_UNDERSAMPLING_AUTO)) {
			double percentToUndersampling = this.getParameterAsDouble(this.PARAMETER_UNDERSAMPLING_PERCENT);
			undersamplingCount = (int) (toUndersampling.size() * percentToUndersampling);

		}
		//auto undersampling, the basis is the average of size in the other classes, I mean, the class will be undersampled until to reach the average size.
		else 
		{
			
			Attribute label = wholeSet.getAttributes().getLabel();
			int totalClasses = label.getMapping().size();
			int totalRemaining = wholeSet.size() - toUndersampling.size();
			
			undersamplingCount= toUndersampling.size() -(totalRemaining / (totalClasses-1));
		
		}
		//TODO Make a undersampling which the basis is the max size in the other classes 
		return undersamplingCount;
	}

	//TODO this will be used in the max undersampling
	private int getMaxCount(ExampleSet wholeSet, String labelToUndersampling) throws OperatorException 
	{
		int avg=0;
		 Map<String , Integer> counting=getCountByLabelValue(wholeSet);
		 
		 Iterator <String> iterator = counting.keySet().iterator();
		 double max=0;
		 
		 while (iterator.hasNext())
		 {
			 String key = iterator.next();
			 if (!key.equals(labelToUndersampling))
			 {
				 max = Math.max(max,counting.get(key));
			 }
		 }
		 
		return (int)max;
	}
	
	private ExampleSet getExampleSetByLabelValue(ExampleSet inputSet, String label) throws OperatorException {
		Attribute labelAtt = inputSet.getAttributes().getLabel();
		SplittedExampleSet e = SplittedExampleSet.splitByAttribute(inputSet,
				labelAtt);

		int labelIndex = labelAtt.getMapping().getIndex(label);
		if (labelIndex == -1) {
			return null;
		}

		SplittedExampleSet b = (SplittedExampleSet) e.clone();
		b.selectSingleSubset(labelIndex);

		return b;
	}
	
	
	private Map<String , Integer> getCountByLabelValue(ExampleSet inputSet) throws OperatorException {
		Attribute labelAtt = inputSet.getAttributes().getLabel();
		SplittedExampleSet e = SplittedExampleSet.splitByAttribute(inputSet,
				labelAtt);

		Map <String, Integer> counts = new HashMap<String, Integer>();
		
		List <String>values=labelAtt.getMapping().getValues();
		for (String label:values){
			
			e.selectSingleSubset(labelAtt.getMapping().getIndex(label));
			counts.put(label, e.size());
		}
		

		return counts;
	}


	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		ParameterType type = new ParameterTypeString(PARAMETER_LABEL,
				"The class label that will be undersampled.", "", false);
		type.setExpert(false);
		types.add(type);

		types.add(new ParameterTypeDouble(PARAMETER_UNDERSAMPLING_PERCENT,
				"The percent of instances to be removed from the class.", 0, 1,
				0.7));


		types.add(new ParameterTypeBoolean(
				PARAMETER_UNDERSAMPLING_AUTO,
				"Automatically calculates the undersampling percent in order to set the classes with the same number of .",
				true,
				false));


		
		types.add(new ParameterTypeCategory(
				ExampleSource.PARAMETER_DATAMANAGEMENT,
				"Determines, how the data is represented internally.",
				DataRowFactory.TYPE_NAMES, DataRowFactory.TYPE_DOUBLE_ARRAY,
				false));
		
		
		return types;
	}

}
