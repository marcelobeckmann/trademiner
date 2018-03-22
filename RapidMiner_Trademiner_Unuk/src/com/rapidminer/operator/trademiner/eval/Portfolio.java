package com.rapidminer.operator.trademiner.eval;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

public class Portfolio {
	
	public boolean isLogEveryOperation() {
		return logEveryOperation;
	}

	private Map<String,Position> portfolio=new HashMap<String,Position>();
	private List<Position> done=new ArrayList<Position>();
	private boolean logEveryOperation=true;
	
	public void setLogEveryOperation(boolean logEveryOperation) {
		this.logEveryOperation = logEveryOperation;
	}
	public void clear()
	{
		portfolio.clear();
		done.clear();
		
	}
	
	public Position getPosition(String symbol)
	{
		return portfolio.get(symbol);
		
	}
	public void buy(long predictionId, long mktdataId,String symbol, double quantity, double price, Date dateTime, Date predictionDateTime)
	{
		if (portfolio.containsKey(symbol))
		{
			throw new IllegalStateException("Can not buy, " + symbol + " is already in portfolio");
		}
		Position position = new Position(predictionId,mktdataId,symbol,quantity, price, dateTime, predictionDateTime);
		portfolio.put(symbol,position);
		
		
	}
	
	public boolean isOverdue(String symbol,Date actualDateTime, int minutesToHold)
	{
		if (!portfolio.containsKey(symbol))
		{
			return false;
		}
		Position position = portfolio.get(symbol);
		
		return position.isOverdue(actualDateTime, minutesToHold);
	}
	
	public Date getOverdueDatetime(String symbol, int minutesToHold)
	{
		if (!portfolio.containsKey(symbol))
		{
			return null;
		}
		Position position = portfolio.get(symbol);
		return position.getOverdueDatetime(minutesToHold);
		
	}
	
	public void sell(long mktdataId,String symbol, double price, Date dateTime)
	{
		if (!portfolio.containsKey(symbol))
		{
			throw new IllegalStateException("Can not sell, " + symbol + " is not in portfolio");
		}
		
		
		Assert.assertNotNull("Sell date time can't be null",dateTime);
		Assert.assertTrue("Sell price can't be zero or negative",price>0);
		

		Position position = portfolio.get(symbol);
		if (position.getSellDateTime()!=null)
		{
			throw new IllegalStateException("Can not sell, " + symbol + " position is already sold");
		}
		
		position.setSellMktdataId(mktdataId);
		position.setSellPrice(price);
		position.setSellDateTime(dateTime);
		portfolio.remove(symbol);
		done.add(position);
		

		if (logEveryOperation)
		{
			System.out.println(position+", seelMktdataId="+ mktdataId);
			
		}

		
		
	}
	/**
	 * @return the done
	 */
	public List<Position> getDone() {
		return done;
	}
	
	public boolean isInPortfolio(String symbol)
	{
		
		return portfolio.containsKey(symbol);
		
		
	}
	
	
}
