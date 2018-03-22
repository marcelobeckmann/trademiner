package com.rapidminer.operator.preprocessing.transformation;

import java.text.NumberFormat;

/**
 * Represents a prediction count given a period of time.
 */

public class PredictionCount2{


	private String datetime;
	private int hour;

	public int countOf0;
	public int countOf2;
	public float percentOf0;
	public float percentOf2;
	public int label;
	public static int  totalNumber=0;
	
	public static final int LABEL0=0;
	public static final int LABEL2=2;
	public static boolean hasPositivePredictions =false;
	public static boolean hasNegativePredictions =false;
	
	//Boolean operator constants
	static final int LESS_THAN=1;
	static final int LESS_OR_EQUALS_THAN=2;
	static final int EQUALS=3;
	static final int GREATER_OR_EQUALS_THAN=4;
	static final int GREATER_THAN=5;
	static final int NOT_EQUALS=6;
	private static String[] operatorsStr= new String[] {"NA","<","<=","==", ">=",">", "!=" };
	
	//This is set by AggregateDecisionByTradeTime parameter.
	static int minorityThreshold;
	static int majorityThreshold;
	
	static float minorityPercentThreshold;
	static float majorityPercentThreshold;

	static int hourThreshold=20;
	
	static int minorityBooleanOperator;
	static int majorityBooleanOperator;
	static int conjunctionOperator1;
	static boolean usePercent=false;

	public static final NumberFormat nf=setupNf();
	
	public String getDatetime() {
		return datetime;
	}
	/**
	 *  201301031330 
	 * @param datetime
	 */
	public  void setDatetime(String datetime) {
		this.datetime = datetime;
		if (datetime!=null)
		{
			hour = Integer.parseInt(datetime.substring(8, 10));
			
		}
	}
	
	static NumberFormat setupNf(){
		
		NumberFormat f = NumberFormat.getNumberInstance();
		f.setMaximumFractionDigits(2);
		return f;
	}

	private void calculatePercents()
	{
		
		//REVER, ISSO NAO ESTAH LEGAL, 0 ?

		percentOf2 = countOf0==0?0:(countOf2/((float)countOf0+countOf2));
		//percentOf2 = countOf0==0?0:(countOf2/((float)totalNumber));
		percentOf0 = 1-percentOf2;

		
	}
	
	public int getAssemblyPrediction()
	{

		calculatePercents();		
		
		if (processConjunction(processThreshold (
				
				processPercentThreshold(countOf2,percentOf2),
				processPercentThreshold(minorityThreshold,minorityPercentThreshold)
				,
				
				minorityBooleanOperator)  ,conjunctionOperator1,
		    processThreshold (
		    		
		    		
		    		processPercentThreshold(countOf0,percentOf0),
		    		processPercentThreshold(majorityThreshold,majorityPercentThreshold),
		    		
		    		majorityBooleanOperator)) && hour <= hourThreshold)
		{ 
			hasPositivePredictions=true;
			return LABEL2;
		}
		else 
		{
			hasNegativePredictions=true;
			return LABEL0;
		}
	}
	/*public String toString()
	{
		return countOf0+"\t"+ countOf2 + "\t" +nf.format(countOf0==0?0:(countOf2/((float)countOf0)))+"\t" +getAssemblyPrediction()+"\t"+ label;
	}*/
	public String toString()
	{
		
		double p2= countOf2/((float)countOf0+countOf2);
		
		
		
		return countOf0+";"+ countOf2 + ";" +nf.format(1-p2)
		+";"+nf.format(p2)
		+";" +getAssemblyPrediction()+";"+ label;
	}
	private static boolean processConjunction(boolean op1, int conjunctionOperator, boolean op2)
	{
		
		if (conjunctionOperator==1) {
			return op1 && op2;
		}
		else
		{
			return op1 || op2 ;
		}
		
		
	}
	
	private static float processPercentThreshold(int count, float percent)
	{
		
		if (usePercent)
		{
			return percent;
			
		}
		else
		{
			
			return count;
		}
		
		
	
	}

	
	public static String toStringThreshold()
	{
		
		StringBuilder sb=new StringBuilder();
		
		sb.append(operatorsStr[minorityBooleanOperator]);

		sb.append(usePercent?minorityPercentThreshold:minorityThreshold);
		
		sb.append(toStringConjunctionOperator(conjunctionOperator1));
		sb.append(operatorsStr[majorityBooleanOperator]);
		sb.append(usePercent?majorityPercentThreshold:majorityThreshold);

		
		return sb.toString();
	}
	
	private static String toStringConjunctionOperator(int conjunctionOperator)
	{
		if (conjunctionOperator==1)
		{
			return " & ";
		}
		else
		{
			return " | ";
		}
		
	}
	private boolean processThreshold(float count, float threshold, int operator)
	{	
		if (threshold<0)
		{
			return true;
		}
		
		switch (operator)
		{
		case LESS_THAN:
		{
			return count < threshold;
		}
		case LESS_OR_EQUALS_THAN:
		{
			return count <= threshold;
		}
		case EQUALS:
		{
			return count == threshold;
		}
		case GREATER_THAN:
		{
			return count > threshold;
		}
		
		case NOT_EQUALS:
		{
			return count != threshold;
		}
		default :
		case GREATER_OR_EQUALS_THAN:
		{
			return count >= threshold;
		}	
		}

	}
	
}
