package com.rapidminer.operator.trademiner.eval;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.operator.trademiner.util.Constants;
import com.rapidminer.operator.trademiner.util.Util;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;

public class SimpleStrategySimulator extends Operator {

	private InputPort exampleSetInput = getInputPorts().createPort("prediction");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("pl");
	private InputPort mktdataInput = getInputPorts().createPort("mktdata");
	private int batchSize=1;

	// Parameters
	public static final String PARAMETER_ALIGNER_WINDOW = "aligner_window";
	public static final String PARAMETER_SYMBOL = "symbol";
	public static final String PARAMETER_TICKET_AMMOUNT = "ticketSize";
	public static final String PARAMETER_PREDICTION_ATTRIBUTE = "predictionAtt";
	public static final String PARAMETER_NEWS_DATETIME_ATTRIBUTE = "dateTimeAtt";
	public static final String PARAMETER_MKTDATA_DATETIME_ATTRIBUTE = "mktdataDateTimeAtt";
	public static final String PARAMETER_MKTDATA_CLOSE_ATTRIBUTE = "mktdataOpenAtt";
	public static final String PARAMETER_MKTDATA_PRICE_ATTRIBUTE = "mktdataPriceAtt";
	public static final String PARAMETER_MKTDATA_MINIMUM_PROFIT_PERCENT = "minimumProfitPercent";
	public static final String PARAMETER_TIMEZONE="timezone";
	public static final String PARAMETER_HOLD_PERIOD = "holdPeriod";
	public static final String PARAMETER_BATCH_SIZE="batchSize";
	
	public static final String PARAMETER_EXCAHNGE_OPEN_TIME="exchange_opentime";
	public static final String PARAMETER_EXCAHNGE_CLOSE_TIME="exchange_closetime";
	

	private Portfolio portfolio = new Portfolio();
	private Attribute newsDateTimeAtt;
	private Attribute mktdataDateTimeAtt;
	private Attribute mktdataPriceAtt;
	private Attribute mktdataIdAtt;
	private Attribute predictionIdAtt;
	private Attribute mktdataCloseAtt ;
	private double ticketAmmount;
	private ExampleSet mktdata;
	private String symbol;
	private int timeWindowInMinutes;
	private Date open;
	private Date close;
	private int holdPeriod;
	private double minProfitPercent=1;
	
	public SimpleStrategySimulator(OperatorDescription description) {
		super(description);

		// exampleSetInput.addPrecondition(new ExampleSetPrecondition(exampleSetInput, new String[] { "relative time" }, Ontology.ATTRIBUTE_VALUE));

		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput, exampleSetOutput, SetRelation.EQUAL) {

			@Override
			public ExampleSetMetaData modifyExampleSet(ExampleSetMetaData metaData) throws UndefinedParameterError {

				return metaData;
			}
		});
	}

	@Override
	public void doWork() throws OperatorException {
		ExampleSet prediction = exampleSetInput.getData(ExampleSet.class);
		mktdata = mktdataInput.getData(ExampleSet.class);
		// Starts everything
		portfolio.clear();
		// TODO MANAGE THE LOG LEVEL USING PARAMETERS
		portfolio.setLogEveryOperation(false);

		try {
			// Retrieve parameters
			Attribute predictionAtt = prediction.getAttributes().get(getParameterAsString(PARAMETER_PREDICTION_ATTRIBUTE));
			newsDateTimeAtt = prediction.getAttributes().get(getParameterAsString(PARAMETER_NEWS_DATETIME_ATTRIBUTE));
			mktdataDateTimeAtt = mktdata.getAttributes().get(getParameterAsString(PARAMETER_MKTDATA_DATETIME_ATTRIBUTE));
			mktdataPriceAtt = mktdata.getAttributes().get(getParameterAsString(PARAMETER_MKTDATA_PRICE_ATTRIBUTE));
			mktdataIdAtt = mktdata.getAttributes().get("id");
			mktdataCloseAtt = mktdata.getAttributes().get(PARAMETER_MKTDATA_CLOSE_ATTRIBUTE);
			predictionIdAtt = prediction.getAttributes().get("id");
			minProfitPercent = getParameterAsDouble(PARAMETER_MKTDATA_MINIMUM_PROFIT_PERCENT);
			
			java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(getParameterAsString(PARAMETER_TIMEZONE)));
			
			holdPeriod = getParameterAsInt(PARAMETER_HOLD_PERIOD);
				
			if (!getParameterAsString(PARAMETER_EXCAHNGE_OPEN_TIME).isEmpty() && 
					!getParameterAsString(PARAMETER_EXCAHNGE_CLOSE_TIME).isEmpty()	) {
				open = new Date("01/01/1970 " + getParameterAsString(PARAMETER_EXCAHNGE_OPEN_TIME));
				close =  new Date("01/01/1970 " +getParameterAsString(PARAMETER_EXCAHNGE_CLOSE_TIME));
			}
			
			symbol = getParameterAsString(PARAMETER_SYMBOL);
			ticketAmmount = getParameterAsDouble(PARAMETER_TICKET_AMMOUNT);
			// TODO Check if the time window in minutes retrieval is working properly
			getTimeWindowInMinutes();

			System.out.println("#### Start of simulation #####");
			System.out.println("#### legend: + buy, - sell, _ exit due, / exit with moving avg");
			for (int i = 0; i < prediction.size(); ++i) {
				Example example = prediction.getExample(i);
				String predictionValue = String.valueOf((int) example.getValue(predictionAtt));
				long predictionId = (long) example.getValue(predictionIdAtt);
				
				long id = (long) example.getValue(this.predictionIdAtt);
				Date newsDate = new Date((long) example.getValue(this.newsDateTimeAtt));

				newsDate =applyTimeWindow(newsDate.getTime());
				if (!verifyOpenClose(newsDate))
				{
					//System.out.println("\n##### Out of trade time: "+ symbol+"@"+newsDate);
					continue;
				}
				
				
				
				// System.out.println("id:" + id);
				try {
					if (Constants.LABEL_SURGE.equals(predictionValue)) {
							System.out.print( "+");
						/*if (!portfolio.isInPortfolio(symbol)) {
							long mktdataId =(long) findMarketData(applyTimeWindow(newsDate.getTime())).getValue(mktdataIdAtt);
							if (mktdataId!=predictionId) {
								System.out.print("+");
								buyStock(predictionId, example);
								System.out.print("-");
								sellStock(newsDate);
							}
						}*/
						

						if (!portfolio.isInPortfolio(symbol)) {
							//long mktdataId =(long) findMarketData(applyTimeWindow(newsDate.getTime())).getValue(mktdataIdAtt);
							//if (mktdataId!=predictionId) 
							{
								System.out.print("+");
								
								if (buyStock(predictionId, newsDate)) {
									
									System.out.print("-");
									decideToSell( newsDate);
									
									
								/*	
									Calendar cal = Calendar.getInstance();
									cal.setTime(newsDate);
									cal.add(Calendar.MINUTE, holdPeriod);
									sellStock(cal.getTime());
								*/
								}
							}
						}
						
					} 
					else if ( (Constants.LABEL_SURGE.equals(predictionValue)) )
					{
						System.out.print("-");
						
					}
					else
					{
						//System.out.print("o");
						}
				} catch (LackOfMktdataException e) {
					System.out.println("#### LackOfMktdataException: " + e.getMessage());

				}
				checkForStop();
			}
			exampleSetOutput.deliver(calculatePL());

		} catch (Exception e) {

			throw new OperatorException(e.getMessage(), e);
		}

		System.out.println("\n#### End of simulation #####");
	}

	
	/**
	 * Verify if the specified dateTime is in trading time.
	 * @param dateTime
	 * @return
	 */
	
	
	private boolean verifyOpenClose(Date dateTime)
	{	
		if (open==null || close==null)
			return true;
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateTime);
		cal.set(Calendar.DAY_OF_MONTH,1);
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH,0);
		
		//TODO TO PROCESS THIS MKTDATA OR NEWS FOR NYSE WE NEED TO ADJUST THIS DST, THIS IS A PROBLEM IN THE DATA COLLECTION, BUT FOR THESIS IT'S OK
		// 		<=2 >=10
		Date newOpen=open;
		Date newClose = close;
		if (dateTime.getMonth() <=2 || dateTime.getMonth() >=10)
		{
			//ADD ONE HOUR DUE TO DST
			newOpen= new Date(open.getTime());
			newClose = new Date(close.getTime());
			
			newOpen.setHours(newOpen.getHours()+1);
			newClose.setHours(newClose.getHours()+1);
			
		}
		
		long timeInMillis = cal.getTime().getTime();
		if (timeInMillis >= newOpen.getTime() && timeInMillis <=newClose.getTime() )
		{
			return true;
		}

		
		//System.out.println("###### dateTime="+ dateTime +", cal="+cal.getTime() +", timeInMillis="+timeInMillis +", open="+ open+", close="+ close );
				
		return false;
	}
	
	
	private void getTimeWindowInMinutes() throws UndefinedParameterError {
		String alignerWindow = getParameterAsString(PARAMETER_ALIGNER_WINDOW);
		if ("00:00:00".equals(alignerWindow)) {
			timeWindowInMinutes = 0;
			return;
		}
		int signal = 1;
		if (alignerWindow.contains("-")) {
			signal = -1;
			alignerWindow = alignerWindow.replace("-", "");

		}
		Date timeWindow = new Date("01/01/1970 " + alignerWindow);
		Calendar cal = Calendar.getInstance();
		cal.setTime(timeWindow);
		// TODO ASSERT THIS
		timeWindowInMinutes = cal.get(Calendar.MINUTE);

		timeWindowInMinutes *= signal;
	}

	protected ExampleSet calculatePL() throws OperatorException {
		ExampleSet pl = createPLStructure();
		MemoryExampleTable table = (MemoryExampleTable) pl.getExampleTable();
		DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_SPARSE_ARRAY, '.');
		// Attributes
		Attribute predictionId = pl.getAttributes().get("predictionId");
		Attribute mktdataId = pl.getAttributes().get("mktdataId");
		Attribute sellMktdataId = pl.getAttributes().get("sellMktdataId");
		Attribute qtyAtt = pl.getAttributes().get("qty");
		Attribute symbolAtt = pl.getAttributes().get("symbol");
		Attribute buyAtt = pl.getAttributes().get("buy");
		Attribute sellAtt = pl.getAttributes().get("sell");

		Attribute buyDateTimeAtt = pl.getAttributes().get("buyDateTime");
		Attribute sellDateTimeAtt = pl.getAttributes().get("sellDateTime");
		Attribute predictionDateTime= pl.getAttributes().get("predictionDateTime");
		batchSize = getParameterAsInt(PARAMETER_BATCH_SIZE);
		Attribute profitAtt = pl.getAttributes().get("profit");

		List<Position> done = portfolio.getDone();
		for (Position position : done) {
			DataRow row = factory.create(table.getNumberOfAttributes());
			row.set(predictionId, position.getPredictionId());
			row.set(mktdataId, position.getMktdataId());
			row.set(sellMktdataId, position.getSellMktdataId());
			row.set(qtyAtt, position.getQuantity());
			row.set(symbolAtt, symbolAtt.getMapping().mapString(symbol));
			row.set(buyAtt, position.getBuyPrice());
			row.set(sellAtt, position.getSellPrice());
			row.set(profitAtt, position.getProfit());
			row.set(buyDateTimeAtt, position.getBuyDateTime().getTime());
			row.set(sellDateTimeAtt, position.getSellDateTime().getTime());
			row.set(predictionDateTime, position.getPredictionDateTime().getTime());
			
			table.addDataRow(row);
		}
		return pl;
	}

	protected boolean buyStock(long predictionId, Example example) throws OperatorException {
		long actualDateTime = (long) example.getValue(newsDateTimeAtt);

		Date dt = applyTimeWindow(actualDateTime);
		return buyStock(predictionId, dt);

	}

	private Date applyTimeWindow(long actualDateTime) {
		// TODO ASSERT THIS
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(actualDateTime));

		cal.add(Calendar.MINUTE, this.timeWindowInMinutes);
		Date dt = cal.getTime();
		return dt;
	}


	

	protected boolean decideToSell( Date dateTime) throws OperatorException {
		
		Position initialPosition = new Position(portfolio.getPosition(symbol));
	
		boolean sold=false;
		for (int i=1;i<=holdPeriod;i++) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateTime);
			//Search or some profit, minute by minute.
			cal.add(Calendar.MINUTE, i);
			
			Example mktdata=this.findMarketData(cal.getTime());
			double currentPrice = mktdata.getValue(mktdataPriceAtt);
			
			initialPosition.setSellPrice(currentPrice);
			initialPosition.setSellDateTime(cal.getTime());
			double profitPercent= initialPosition.getProfitPercent();
			
			if (profitPercent>=minProfitPercent)
			{
				sold=true;
				sellStock(cal.getTime());
				break;
			}
			
			
		}
		
		if (!sold)
		{
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateTime);
			cal.add(Calendar.MINUTE, holdPeriod);
			sellStock(cal.getTime());
		
			
		}
		return true;
	}
	
	
	protected boolean buyStock(long predictionId, Date dateTime) throws OperatorException {
		Example mktdataExample = findMarketData(dateTime);
		
		//If not found mktdata, don't buy
		if (mktdataExample==null) {
			return false;
		}
		double price = mktdataExample.getValue(mktdataPriceAtt);
		long mktDataid = (long) mktdataExample.getValue(mktdataIdAtt);
		Date mktdataDateTime = mktdataExample.getDateValue(mktdataDateTimeAtt);
	
		int qty = (int) (ticketAmmount / price);
		// To have an approximate simulation, the trades will be done in the proper batch size, default 100
		int batches = qty / batchSize;

		Assert.assertTrue("Qty of batches is <=0, ticketAmmount=" + ticketAmmount + ", price=" + price + ", batch size=" + batchSize, batches > 0);
		
		portfolio.buy(predictionId, mktDataid, symbol, batches * batchSize, price,mktdataDateTime, dateTime);
		return true;
	}

	protected void sellStock(Date dateTime) throws OperatorException {
		Example mktdataExample = findMarketData(dateTime);
		double price = mktdataExample.getValue(mktdataPriceAtt);
		long mktDataid = (long) mktdataExample.getValue(mktdataIdAtt);

		
		portfolio.sell(mktDataid, symbol, price, dateTime);
	}

	/*protected void sellStock(Example example) throws OperatorException {
		long actualDateTime = (long) example.getValue(newsDateTimeAtt);
		// TODO ASSERT THIS
		Date dt = applyTimeWindow(actualDateTime);
		sellStock(dt);

	}*/

	/**
	 * Performs a search in the market data exampleset. This method assumes the market data exampleset is sorted by
	 * {@link com.rapidminer.operator.trademiner.eval.SimpleStrategySimulator#PARAMETER_NEWS_DATETIME_ATTRIBUTE }.
	 * 
	 * @param dateTime
	 *            the date and time to search the mktdata
	 * @return The nearest datetime mktdata example
	 * @throws OperatorException
	 */
	protected Example findMarketData(Date dateTime) throws OperatorException {
		// TODO IMPLEMENT SYMBOL FILTER
		// TODO PERFORM A BINARY SEARCH
		long searchDateTime = Util.convertToMinutes(dateTime.getTime());

		
		//TODO CHECK TRADING TIME
		long previousDateTime = 0;
		Example previousExample = null;

		for (int i = 0; i < mktdata.size(); i++) {
			Example example = mktdata.getExample(i);
			long currentDateTime = Util.convertToMinutes((long) example.getValue(mktdataDateTimeAtt));
			// TODO CONVERT TO MINUTES
			if (previousDateTime == 0) {
				previousDateTime = currentDateTime;
				previousExample = example;
				continue;

			}

			Assert.assertTrue("Not a time series. Market data is not sorted by date & time", previousDateTime <= currentDateTime);
			// Process delta

			if (searchDateTime >= previousDateTime && searchDateTime <= currentDateTime) {
				Example exampleToReturn ;

				
				if (Math.abs(searchDateTime-previousDateTime) < Math.abs(searchDateTime-currentDateTime) ){
					exampleToReturn= previousExample;
				} else {
					exampleToReturn= example;
				}
				
				double price = exampleToReturn.getValue(mktdataPriceAtt);
				// ASSERT PRICE
				Assert.assertTrue("The price of an asset never can be zero or negative, price=" + price + ", dateTime=" + dateTime, price > 0);
				
				long timeToReturn =  Util.convertToMinutes((long) exampleToReturn.getValue(mktdataDateTimeAtt));
				//Asserts a delta greater than 3
				long delta = Math.abs(searchDateTime - timeToReturn);
				if (delta > 3) {
					System.out.println("Delta between the searchDateTime and found mktdata is bigger than 3 mins ("
					+ delta + ") "+new Date(searchDateTime)+"/"+new Date(timeToReturn)+" , "+searchDateTime +"/"+currentDateTime);
					//return null;
				}
				
//				changePriceForOpenTime(exampleToReturn);
				
				return exampleToReturn;
			}
			previousDateTime = currentDateTime;
			previousExample = example;
		}

		throw new LackOfMktdataException("No market data for " + symbol + "@" + dateTime);
	}

	private void changePriceForOpenTime(Example exampleToReturn)
	{
		long time = Util.convertToMinutes((long) exampleToReturn.getValue(mktdataDateTimeAtt));

		if (time <= Util.convertToMinutes(open.getTime()))
		{
			double closePrice = exampleToReturn.getValue(mktdataCloseAtt);
			exampleToReturn.setValue(mktdataPriceAtt,closePrice);
		}
		
	}
	protected ExampleSet createPLStructure() throws OperatorException {

		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(AttributeFactory.createAttribute("symbol", Ontology.STRING));

		attributes.add(AttributeFactory.createAttribute("predictionId", Ontology.INTEGER));

		attributes.add(AttributeFactory.createAttribute("predictionDateTime", Ontology.DATE_TIME));

		
		attributes.add(AttributeFactory.createAttribute("mktdataId", Ontology.INTEGER));

		attributes.add(AttributeFactory.createAttribute("sellMktdataId", Ontology.INTEGER));

		attributes.add(AttributeFactory.createAttribute("qty", Ontology.REAL));

		attributes.add(AttributeFactory.createAttribute("buy", Ontology.REAL));
		attributes.add(AttributeFactory.createAttribute("buyDateTime", Ontology.DATE_TIME));
		
		
		
		attributes.add(AttributeFactory.createAttribute("sell", Ontology.REAL));

		attributes.add(AttributeFactory.createAttribute("sellDateTime", Ontology.DATE_TIME));
		

		attributes.add(AttributeFactory.createAttribute("profit", Ontology.REAL));

		MemoryExampleTable table = new MemoryExampleTable(attributes);
		ExampleSet exampleSet = table.createExampleSet();

		return exampleSet;

	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		types.add(new ParameterTypeString(PARAMETER_ALIGNER_WINDOW, "The time window that an asset price takes to be affected by a new.", "00:01:00",
				false));


		types.add(new ParameterTypeInt(PARAMETER_HOLD_PERIOD, "The quantity of minutes to keep a stock bought before to sell it.", 0,
				Integer.MAX_VALUE, 60));
			
		
		ParameterType type = new ParameterTypeString(PARAMETER_SYMBOL, "The stock symbol under simulation.", "", false);
		type.setExpert(false);
		type.setOptional(false);
		types.add(type);

		
		types.add(new ParameterTypeDouble(PARAMETER_TICKET_AMMOUNT, "The ammount of money available to buy a stock in a single order.", 0,
				Double.MAX_VALUE, 10000.00));
		type.setExpert(false);
		types.add(type);

		type = new ParameterTypeString(PARAMETER_PREDICTION_ATTRIBUTE,
				"The attribute name which contains the algorithm outcome in prediction exampleset input.", "prediction", true);
		types.add(type);

		type = new ParameterTypeString(PARAMETER_NEWS_DATETIME_ATTRIBUTE,
				"The attribute name which contains the date and time the news was published in prediction exampleset input.", "published", true);
		types.add(type);

		type = new ParameterTypeString(PARAMETER_MKTDATA_DATETIME_ATTRIBUTE,
				"The attribute name which contains the date and time in market data time series exampleset input.", "datetime", true);
		types.add(type);
		
		
		type = new ParameterTypeString(PARAMETER_MKTDATA_CLOSE_ATTRIBUTE,
				"The attribute name which contains the close price in market data time series exampleset input.", "close", true);
		types.add(type);
		

		type = new ParameterTypeString(PARAMETER_MKTDATA_PRICE_ATTRIBUTE,
				"The attribute name which contains the stock price in market data time series exampleset input.", "last", true);
		types.add(type);

		
		type = new ParameterTypeString(PARAMETER_TIMEZONE,"The timezone name used for trading.", "GMT", true);
		types.add(type);

		type = new ParameterTypeString(PARAMETER_EXCAHNGE_OPEN_TIME,
				"The time the exchange opens. Nothing will be negotiated before this time.", "13:30:00", true);
		type.setOptional(true);
		types.add(type);

		type = new ParameterTypeString(PARAMETER_EXCAHNGE_CLOSE_TIME,
				"The time the exchange opens. Nothing will be negotiated after this time.", "20:00:00", true);
		type.setOptional(true);
		types.add(type);
		

		type = new ParameterTypeInt(PARAMETER_BATCH_SIZE,
				"The batch size is the multiple quantity of a stock you can trade in a operation",0,Integer.MAX_VALUE,100, true);
		types.add(type);


		type = new ParameterTypeDouble(PARAMETER_MKTDATA_MINIMUM_PROFIT_PERCENT,
				"The minimal percent of profit acceptable to sell the position",0,Double.MAX_VALUE,1, true);
		types.add(type);


		
		
		
		
		

		return types;
	}

}
