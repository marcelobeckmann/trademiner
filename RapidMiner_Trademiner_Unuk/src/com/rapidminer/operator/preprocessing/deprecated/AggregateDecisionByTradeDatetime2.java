package com.rapidminer.operator.preprocessing.deprecated;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.trademiner.util.ConnectionFactory;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;

/**
 * Aggregate the decision of several documents, from the same symbol, same minute
 * 
 * @author Marcelo Beckmann
 */


public class AggregateDecisionByTradeDatetime2 extends Operator {

	private static final String PARAMETER_PREDICTION_ATT="prediction_att";
	
	private static final String PARAMETER_MINORITY_THRESHOLD="minority_threshold";

	private static final String PARAMETER_MAJORITY_THRESHOLD="majority_threshold";
	
	private static final String PARAMETER_MINORITY_BOOLEAN_OPERATOR="minority_boolean_operator";

	private static final String PARAMETER_MAJORITY_BOOLEAN_OPERATOR="majority_boolean_operator";
	
	private static final String PARAMETER_HOUR="hour";
	
	private static final String PARAMETER_CONJUNCTION_BOOLEAN_OPERATOR_1="conjunction_boolean_operator";

	private static final String PARAMETER_MINORITY_THRESHOLD_PERCENT="minority_threshold_percent";

	private static final String PARAMETER_MAJORITY_THRESHOLD_PERCENT="majority_threshold_percent";


	// --------------------------------------------
	private static final String PARAMETER_MINORITY_THRESHOLD_2="minority_threshold_2";

	private static final String PARAMETER_MAJORITY_THRESHOLD_2="majority_threshold_2";
	
	private static final String PARAMETER_MINORITY_BOOLEAN_OPERATOR_2="minority_boolean_operator_2";

	private static final String PARAMETER_MAJORITY_BOOLEAN_OPERATOR_2="majority_boolean_operator_2";
	
	private static final String PARAMETER_CONJUNCTION_BOOLEAN_OPERATOR_2="conjunction_boolean_operator_2";
	
	
	// ----------------------------------------------
	
	private static final String PARAMETER_REMOVE_DUPLICATES_AT_END="remove_duplicates_at_end";
	
	private static final String PARAMETER_USE_PERCENTS="use_percents";

	private static final String PARAMETER_CHECK_BLACK_LIST="check_black_list";
	
	//private static final String PARAMETER_DUMMY_ID="dummy_id";
	
	private static final String PARAMETER_VERBOSE="verbose";
	
	private static final String PARAMETERS_TO_STRING="parameters_to_string";
	
	
	private InputPort predictionInput= getInputPorts().createPort("label prediction");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("label prediction adjusted");
	/** List with alignment ids that must not be considered in the aggregation count. 
	   Reasons to not consider these ids:
	   
	   1- News was provided by a noisy source (ex. a crap blog with intent to decrease/increase the stock prices from a specific company).
	   
	 * */
	private InputPort blacklistInput= getInputPorts().createPort("blk list");	

	private  Attribute predictionAttribute;

	private  Attribute labelAttribute;

	private int majorityClassValue;
	

	
	private boolean removeDuplicatesAtEnd;
	
	private final int DUPLICATED_EXAMPLE=3;
	
	private boolean checkBlackList = false;
	
	private boolean verbose;
	
	//private long dummy_id=-1;

	private List<Long>blackList;
	
	/**
	 * Constructor
	 */
	public AggregateDecisionByTradeDatetime2(OperatorDescription description) {
	
		super(description);
		
	
		
	//	exampleSetInput.addPrecondition(new AttributeSetPrecondition(exampleSetInput,
	//			AttributeSetPrecondition.getAttributesByParameter(this, PARAMETER_ATTRIBUTE_NAME), Ontology.NOMINAL));

		
	}
	
	private void prepareBlackList() throws OperatorException
	{
		blackList = new ArrayList<Long>();
		ExampleSet inputSet = blacklistInput.getData();
		Attribute id = inputSet.getAttributes().get("id");
		//Remove the duplicated entries
		for (int i=0;i<inputSet.size();++i)
		{
			Example example = inputSet.getExample(i);
			blackList.add((long)example.getValue(id));
		}
	}
	

	@Override
	public void doWork() throws OperatorException {

		try { 
		ExampleSet inputSet = predictionInput.getData();
		if (inputSet.size()==0)
		{
			throw new OperatorException("Error: inputset is empty!");
		}
		
		checkBlackList = getParameterAsBoolean(PARAMETER_CHECK_BLACK_LIST);
		
		if (checkBlackList) {
			prepareBlackList();
		}
		//This is to adjust as the mappings between label and prediction are not matching
		//inputSet.getAttributes().getPredictedLabel().setMapping(inputSet.getAttributes().getLabel().getMapping());

		predictionAttribute = inputSet.getAttributes().get(getParameterAsString(PARAMETER_PREDICTION_ATT));
		labelAttribute = inputSet.getAttributes().get("label");
		
		removeDuplicatesAtEnd = getParameterAsBoolean( PARAMETER_REMOVE_DUPLICATES_AT_END);
		
		/*if (!"".equals(getParameterAsInt(PARAMETER_DUMMY_ID))) {
			dummy_id = getParameterAsInt(PARAMETER_DUMMY_ID);
		}*/
		PredictionCount2.minorityThreshold=getParameterAsInt(PARAMETER_MINORITY_THRESHOLD);

		PredictionCount2.majorityThreshold=getParameterAsInt(PARAMETER_MAJORITY_THRESHOLD);

		PredictionCount2.minorityBooleanOperator=getParameterAsInt(PARAMETER_MINORITY_BOOLEAN_OPERATOR);

		PredictionCount2.majorityBooleanOperator=getParameterAsInt(PARAMETER_MAJORITY_BOOLEAN_OPERATOR);
		
		PredictionCount2.conjunctionOperator1=getParameterAsInt(PARAMETER_CONJUNCTION_BOOLEAN_OPERATOR_1);

		PredictionCount2.hourThreshold= getParameterAsInt(PARAMETER_HOUR);

		PredictionCount2.usePercent = getParameterAsBoolean(PARAMETER_USE_PERCENTS);
		
		PredictionCount2.totalNumber= inputSet.size();
		
		PredictionCount2.minorityPercentThreshold = (float)getParameterAsDouble(PARAMETER_MINORITY_THRESHOLD_PERCENT);
		PredictionCount2.majorityPercentThreshold = (float)getParameterAsDouble(PARAMETER_MAJORITY_THRESHOLD_PERCENT);
		
		
		setParameter(PARAMETERS_TO_STRING,PredictionCount2.toStringThreshold());
		PredictionCount2.hasPositivePredictions=false;
		
		verbose = getParameterAsBoolean(PARAMETER_VERBOSE);
		
		majorityClassValue=0;
		
		/*
		if (predictionAttribute.getMapping().getIndex("0")!=predictionAttribute.getMapping().getIndex("0"))
		{
			//Swap the label indexeds
			int tmp =PredictionCount2.label0; 
			PredictionCount2.label0=PredictionCount2.label1;
			PredictionCount2.label1=tmp;
			System.out.println("########### WARNING! label indexes swaped because of mapping differences");
			
			
			throw new OperatorException("Error: label 0 and prediction mappings are not matching:" + inputSet.getAttributes().getLabel().getMapping().getIndex("0")+","+inputSet.getAttributes().getPredictedLabel().getMapping().getIndex("0"));
		}*/
		
		List <String> alreadyKeptDateTime = new ArrayList<String> ();
		//List <DataRow> toRemove = new ArrayList<DataRow>();
		List <Integer> toRemove = new ArrayList<Integer>();
 		
			Map<Long, PredictionCount2> predictionCounts=aggregatePredictionByDateTime(inputSet);
	
			for (int i=0;i<inputSet.size();++i)
			{
				Example example = inputSet.getExample(i);
				long id = (long)example.getId();
				
				
				if (!predictionCounts.containsKey(id)) {
						throw new OperatorException(id + " is not aggregated");
				}
				PredictionCount2 count = predictionCounts.get(id);
				int newPrediction=predictionCounts.get(id).getAssemblyPrediction();
				example.setPredictedLabel(newPrediction);
				
				if (!alreadyKeptDateTime.contains(count.getDatetime())) {
					alreadyKeptDateTime.add(count.getDatetime());
				}
				else
				{
					toRemove.add(i);
				}
				
			}
	
			//Remove the duplicated entries
			for (int i=0;i<inputSet.size();++i)
			{
				
				if (toRemove.contains(i)) {
					Example example = inputSet.getExample(i);
				//toRemove.remove(toRemove.get(i));
				//example.setPredictedLabel(newPrediction);
					if (removeDuplicatesAtEnd) {
						//MARK AS DUPLICATED ENTRY
						//TODO USE A FILTER TO REMOVE THIS AT END
					     example.setLabel(DUPLICATED_EXAMPLE);
					}
				}
			}
			//THIS WILL CAUSE AN ERROR INTO THE PERFORMANCE CALCULATOR!
			//if (!PredictionCount2.hasPositivePredictions || !PredictionCount2.hasNegativePredictions)
			//TODO FIX THE RAPIDMINER BUG AND REMOVE THIS CRAP CODE!
			
			//THIS IS NOT NEEDED ANYMORE, USE OUR "Adjust Mapping" operator before the performance, same for dummy id.
			/*
			int missingClass;
			if ((missingClass=checkMissingClass(inputSet))>-1)
			{
				if (missingClass==3)
				{
					throw new RuntimeException("Error: missing class 3");
				}
				
				MemoryExampleTable table = (MemoryExampleTable)inputSet.getExampleTable();
			
				System.out.println("Include a dummy id example with id "+ dummy_id);
				//TODO INCLUDE A POSITIVE EXAMPLE WITH PREDICTION AND LABEL=2
				addDummyExample(table,inputSet, missingClass);
				
				//eXTREME DECISION
				if (checkMissingClass(inputSet)>-1)
				{
					System.out.println("Extreme measure as the dummy example is not present in the output: changed the last record to the missing class"+missingClass);
					Example ex = inputSet.getExample(inputSet.size()-1);
					ex.setValue(predictionAttribute, missingClass);
					
							
				}
				
			}*/
			
			
			exampleSetOutput.deliver(inputSet);
			
		}
		catch (Exception e)
		{
			e.printStackTrace(); 
			throw new OperatorException(e.getMessage(),e);
			
		}
		//TODO Deliver output
	}

	private int checkMissingClass(ExampleSet exampleSet)
	{
		Attribute att = exampleSet.getAttributes().get("prediction");
		boolean pos0=false;
		boolean pos2= false;
		for (int i=0;i<exampleSet.size();i++)
		{
			Example ex = exampleSet.getExample(i);
			int value = (int)ex.getValue(att);
			if (value==0)
			{
				pos0=true;
			}
			else if (value==2)
			{
				pos2=true;
			}
			//-----------------------------------
			if (pos0 && pos2)
			{
				return -1;
			}
			
		}
		
		if (!pos0)
		{
			return 0;
		}
		else if (!pos2)
		{
			return 2;
		}
		
		return 3;
		
	}
	
	/**
	 * Generate a dummy example from positive class
	 * @param table
	 * @param example
	 */
	/*
	public void addDummyExample(MemoryExampleTable table, ExampleSet inputSet, int missingClass) {
		DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.');
		//
		Attributes atts = inputSet.getAttributes();
		
		
		DataRow row = factory.create(atts.allSize());
		
		row.set(atts.get("id"), dummy_id);

		
		row.set(atts.get("label"),missingClass);
		
		row.set(atts.get("prediction"),missingClass);

		table.addDataRow(row);

	}
*/
	private Map<Long, PredictionCount2> aggregatePredictionByDateTime(ExampleSet inputSet) throws SQLException
	{	
		Map <String,PredictionCount2 > countByDateTime = new HashMap<String,PredictionCount2>();
		
		Map  <Long, String > dateTimesById = getDateTimesById(inputSet);
		
		
		
		Map <Long, PredictionCount2> predictionCountById= new HashMap<Long,PredictionCount2>();
		//Obtain the prediction counts by date time
		for (int i=0;i<inputSet.size();++i)
		{
			Example example = inputSet.getExample(i);
			long id= (long)example.getId();
			
			String currentDateTime=dateTimesById.get(id);
			PredictionCount2 predictionCount;
			if (!countByDateTime.containsKey(currentDateTime))
			{
				predictionCount= new PredictionCount2();
				countByDateTime.put(currentDateTime,predictionCount);
				predictionCount.label =(int) example.getLabel(); //.getValue(labelAttribute);
				predictionCount.setDatetime(currentDateTime);
			}
			else
			{
				predictionCount=countByDateTime.get(currentDateTime);
				
				
			}
			
			
			if (predictionCount.label !=(int) example.getValue(labelAttribute))
			{
				//throw new RuntimeException("Error, 2 labels for same datetime "+ currentDateTime);
				System.out.println("####### WARNING 2 labels for same datetime "+ currentDateTime+ ","+predictionCount.label+ "/"+ (int)example.getValue(labelAttribute));
			}
			
			
		/*	if (predictionAttribute.getMapping().getIndex("0")!=majorityClassIndex)
			{
				throw new RuntimeException("Error, label 0 is not returning index 0");
			
			}
			*/
			if (!checkBlackList || !blackList.contains(id)) {
			
				int prediction=(int)example.getValue(predictionAttribute);
			
				if (prediction==majorityClassValue)
				{
					predictionCount.countOf0++;
					
				}
				else
				{
					predictionCount.countOf2++;
				}
			}
			
			/*if (id==dummy_id)
			{
				predictionCount.countOf0=0;
				predictionCount.countOf2=3999;
			}*/
			
			predictionCountById.put(id, predictionCount);
			
		}
		if (verbose) {
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println(PredictionCount2.toStringThreshold());
			//System.out.println("datetime\tcount 0\tcount 2\t%\tdec\tlbl");
			System.out.println("datetime;hour;min;hourmin;count0;count2;perc0;perc2;dec;lbl");
		}
		Set <String>keys = countByDateTime.keySet();
		for(String key:keys)
		{
			PredictionCount2 pc= countByDateTime.get(key);
			if (verbose)
				{System.out.println(extractDateTime(key)+pc);}
		}
		if (verbose) {
			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		}
		return predictionCountById;
	}
	
	public String extractDateTime(String val)
	{
		StringBuilder sb= new StringBuilder();
		sb.append(val);
		sb.append(";");
		sb.append(val.substring(8, 10));
		sb.append(";");
		sb.append(val.substring(10, 12));
		sb.append(";");
		sb.append(val.substring(8, 12));
		sb.append(";");
		
		return sb.toString();
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		
		
		ParameterType type = new ParameterTypeString(PARAMETER_PREDICTION_ATT,
				"The prediction attribute.", "prediction", false);
		type.setExpert(false);
		types.add(type);
		

		
		type = new ParameterTypeInt(PARAMETER_MINORITY_BOOLEAN_OPERATOR,
				"The minority class boolean operator, identified by numbers. 1: <, 2: <=, 3: ==, 4: >=, 5: >, 6: !=",1,6,4, false);
		type.setExpert(false);
		types.add(type);
		
		
		
		type = new ParameterTypeInt(PARAMETER_MINORITY_THRESHOLD,
				"The minority class decision threshold.",-1,Integer.MAX_VALUE,2, false);
		type.setExpert(false);
		types.add(type);
		


		
		type = new ParameterTypeInt(PARAMETER_MAJORITY_BOOLEAN_OPERATOR,
				"The majority class boolean operator, identified by numbers. 1: <, 2: <=, 3: ==, 4: >=, 5: >, 6: !=",1,6,2, false);
		type.setExpert(false);
		types.add(type);


		type = new ParameterTypeInt(PARAMETER_CONJUNCTION_BOOLEAN_OPERATOR_1,
				"The conjunction operator &=1 or |=2",1,2,1, false);
		type.setExpert(false);
		types.add(type);

		

		type = new ParameterTypeInt(PARAMETER_MAJORITY_THRESHOLD,
				"The majority class decision threshold.",-1,Integer.MAX_VALUE,7, false);
		type.setExpert(false);
		types.add(type);



		type = new ParameterTypeInt(PARAMETER_HOUR,
				"The hour.",20,20,13, false);
		type.setExpert(false);
		types.add(type);


		type = new ParameterTypeBoolean(PARAMETER_CHECK_BLACK_LIST,
				"Verifies if the alignment id is in a black list, if yes, do not consider this to aggregation count.",false,false);
		type.setExpert(false);
		types.add(type);


		
		type = new ParameterTypeBoolean(PARAMETER_REMOVE_DUPLICATES_AT_END,
				"Remove the duplicates examples in the same date/time at the end of processing.",false, false);
		type.setExpert(false);
		types.add(type);

		

		type = new ParameterTypeBoolean(PARAMETER_VERBOSE,
				"Print the decisions made.",false, false);
		type.setExpert(false);
		types.add(type);
		
		

		/*type = new ParameterTypeString(PARAMETER_DUMMY_ID,
				"The dummy id to be classified as 2.", "%{dummy_id_}", false);
		type.setExpert(false);
		types.add(type);
		*/

		type = new ParameterTypeString(PARAMETERS_TO_STRING,
				"Parameters to string.", "", false);
		type.setExpert(false);
		types.add(type);
		


		type = new ParameterTypeBoolean(PARAMETER_USE_PERCENTS,
				"Use percents instead of counts.",false, false);
		type.setExpert(false);
		types.add(type);

		
		type = new ParameterTypeDouble(PARAMETER_MINORITY_THRESHOLD_PERCENT,
				"The minority class decision threshold percent.",-0.001,1,0.10, false);
		type.setExpert(false);
		types.add(type);
		
		
		
		type = new ParameterTypeDouble(PARAMETER_MAJORITY_THRESHOLD_PERCENT,
				"The majority class decision threshold percent.",-0.001,1,0.90, false);
		type.setExpert(false);
		types.add(type);
		

		
		return types;
	}

	private Map  <Long, String > getDateTimesById(ExampleSet inputSet) throws SQLException
	{
		
		String sql = "SELECT * FROM ( " +
		" SELECT a.id, CAST(DATE_FORMAT(TIMESTAMP(next_trade_date,next_trade_time),'%Y%m%d%H%i') AS UNSIGNED) AS trade_date_time " +
		" FROM alignment a, rss r WHERE a.id IN (?) AND news_id= r.id " +
		" ) m ORDER BY trade_date_time";
		

		StringBuilder ids = new StringBuilder();
		for (int i=0;i<inputSet.size();i++)
		{
			long id = (long)inputSet.getExample(i).getId();
			if (i!=0) {
				ids.append(",");
			}
			ids.append(id);
		}

		sql  =sql.replace("?", ids);
		try (
				Connection con = ConnectionFactory.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);
				ResultSet rs =pstmt.executeQuery();)
		{

			Map  <Long, String > dateTimes= new HashMap<Long, String>();
			while (rs.next())
			{
				dateTimes.put(rs.getLong("id"),rs.getString("trade_date_time"));
			}
			return dateTimes;
		}
		
	}
	

}