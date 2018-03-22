package com.rapidminer.operator.trademiner.eval;

import java.util.Calendar;
import java.util.Date;

//Assertion were put here to verify the results all the time, without the necessity of mocking or create specific tests.
import org.junit.Assert;

import com.rapidminer.operator.trademiner.util.Util;

public class Position  {

	private long predictionId;
	private long mktdataId;
	private long sellMktdataId;
	private String symbol;
	private double quantity;
	private double buyPrice;
	private double sellPrice;
	private Date buyDateTime;
	private Date sellDateTime;
	private Date predictionDateTime;
	
	public long getSellMktdataId() {
		return sellMktdataId;
	}


	public void setSellMktdataId(long sellMktdataId) {
		this.sellMktdataId = sellMktdataId;
	}

	public Position(){
		
	}
	
	
	public Position(long predictionId,long mktdataId,String symbol, double quantity, double buyPrice, Date buyDateTime, Date predictionDateTime)
	{
		
		Assert.assertNotNull("Symbol cant be null",symbol);
		Assert.assertTrue("Quantity can't be zero or negative",quantity>0);
		Assert.assertTrue("Price can't be zero or negative",buyPrice>0);
		Assert.assertNotNull("Buy Date cant be null",buyDateTime);
		Assert.assertTrue("Buy Date can't be zero",buyDateTime.getTime()>0);
		
		
		this.predictionId=predictionId;
		this.mktdataId=mktdataId;
		this.symbol=symbol;
		this.quantity= quantity;
		this.buyPrice=  buyPrice;
		this.buyDateTime=buyDateTime;
		this.predictionDateTime= predictionDateTime;
		
	}
	

	public Position(Position position)
	{
		this(position.getPredictionId(),position.getMktdataId(),position.getSymbol(),position.getQuantity(),position.getBuyPrice(),position.getBuyDateTime(),position.getPredictionDateTime());
	
		sellMktdataId= position.getSellMktdataId();
		sellPrice= position.getSellPrice();
		sellDateTime = position.getSellDateTime();
	}
	
	
	public Date getPredictionDateTime() {
		return predictionDateTime;
	}


	public void setPredictionDateTime(Date predictionDateTime) {
		this.predictionDateTime = predictionDateTime;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((buyDateTime == null) ? 0 : buyDateTime.hashCode());
		long temp;
		temp = Double.doubleToLongBits(buyPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (buyDateTime == null) {
			if (other.buyDateTime != null)
				return false;
		} else if (!buyDateTime.equals(other.buyDateTime))
			return false;
		if (Double.doubleToLongBits(buyPrice) != Double
				.doubleToLongBits(other.buyPrice))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public double getQuantity() {
		return quantity;
	}
	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}
	public double getBuyPrice() {
		return buyPrice;
	}
	public void setBuyPrice(double buyPrice) {
		this.buyPrice = buyPrice;
	}
	public double getSellPrice() {
		return sellPrice;
	}
	public void setSellPrice(double sellPrice) {
		this.sellPrice = sellPrice;
	}
	/**
	 * @return the buyDateTime
	 */
	public Date getBuyDateTime() {
		return buyDateTime;
	}
	/**
	 * @param buyDateTime the buyDateTime to set
	 */
	public void setBuyDateTime(Date buyDateTime) {
		this.buyDateTime = buyDateTime;
	}
	/**
	 * @return the sellDateTime
	 */
	public Date getSellDateTime() {
		return sellDateTime;
	}
	/**
	 * @param sellDateTime the sellDateTime to set
	 */
	public void setSellDateTime(Date sellDateTime) {
		this.sellDateTime = sellDateTime;
	}
	/**
	 * Calculates the profit
	 * @return
	 */
	public double getProfit()
	{
		
		if (sellDateTime!=null) {
			
			Assert.assertTrue("Quantity can't be zero or negative, quantity="+quantity+", sellDateTime="+sellDateTime,quantity>0);
			Assert.assertTrue("Buy price can't be zero or negative, buyPrice="+buyPrice,buyPrice>0);
			Assert.assertTrue("Sell price can't be zero or negative, sellPrice="+sellPrice,sellPrice>0);
			
			return (sellPrice*quantity) - (buyPrice * quantity);
		}
		else {
			
			throw new IllegalStateException("Can't get profit. Stock for symbol " + symbol + " is not sold.");
		}
	}
	
	public double getProfitPercent()
	{
		
		if (sellDateTime!=null) {
			
			Assert.assertTrue("Quantity can't be zero or negative, quantity="+quantity+", sellDateTime="+sellDateTime,quantity>0);
			Assert.assertTrue("Buy price can't be zero or negative, buyPrice="+buyPrice,buyPrice>0);
			Assert.assertTrue("Sell price can't be zero or negative, sellPrice="+sellPrice,sellPrice>0);
			
			return ((sellPrice/buyPrice) - 1)*100;
		}
		else {
			throw new IllegalStateException("Can't get profit. Stock for symbol " + symbol + " is not sold.");
		}
	}
	/**
	 * Indicates if this position is overdue, given the amount of minutes to hold this position
	 * @param minutes the amount of minutes to hold this position
	 * @return true if the position is overdue
	 */
	public boolean isOverdue(Date actualTime, int minutesToHold)
	{
		long initialMinutes=Util.convertToMinutes(buyDateTime.getTime());
		long actualMinutes =Util.convertToMinutes(actualTime.getTime());
	
		return (actualMinutes - initialMinutes) >= minutesToHold;
	}
	
	public Date getOverdueDatetime(int minutesToHold)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(buyDateTime);
		cal.add(Calendar.MINUTE,minutesToHold);

		return cal.getTime();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "predictionId="+ predictionId + ", mktdataId="+mktdataId+", symbol=" + symbol + ", quantity=" + quantity
				+ ", buyPrice=" + buyPrice + ", sellPrice=" + sellPrice
				+ ", buyDateTime=" + buyDateTime + ", sellDateTime="
				+ sellDateTime ;
	}


	public long getPredictionId() {
		return predictionId;
	}


	public void setPredictionId(long predictionId) {
		this.predictionId = predictionId;
	}


	public long getMktdataId() {
		return mktdataId;
	}


	public void setMktdataId(long mktdataId) {
		this.mktdataId = mktdataId;
	}

	

	
	
}
