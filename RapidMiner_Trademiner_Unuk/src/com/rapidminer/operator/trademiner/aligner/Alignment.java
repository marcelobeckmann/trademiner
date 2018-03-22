package com.rapidminer.operator.trademiner.aligner;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Alignment {
	private static DateFormat dtf= new SimpleDateFormat("yyyyMMDD HH:mm:ss");
	private static DateFormat tf= new SimpleDateFormat("HH:mm:ss");
	
	public int news_id;
	public String symbol;
	public int mktdata_id;
	public Date utc_time_n;
	public Date utc_time_m;
	public Date pub_n;
	public Date pub_m;
	public Date pub_m_date;
	public double last;
	public double delta;
	public double percent_change;
	public int label;
	
	
	public void load(ResultSet rs)
	{
		try {
	

			news_id = rs.getInt("news_id");
			symbol=rs.getString("symbol");
			utc_time_n=rs.getTimestamp("utc_time_n");
			utc_time_m=rs.getTimestamp("utc_time_m");
			pub_n= rs.getTimestamp("pub_n");
			pub_m= rs.getTimestamp("pub_m");
			pub_m_date = rs.getDate("pub_m_date");
			mktdata_id=rs.getInt("mktdata_id");
			last=rs.getDouble("last");
			delta=rs.getDouble("delta");
			percent_change=rs.getDouble("percent_change");
			label=rs.getInt("label");
			
			
			
			
			
		
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
	}
	
	
	public String toString()
	{


		
		return padr(news_id,10) 
		+ padr(mktdata_id,15) 
		+ padr(symbol,8)
		+ padr(last,10)
		+padr(delta,10)
		+padr(percent_change,15)
		+padr(label,8)
		+padr(tf.format(utc_time_n),15)
		+padr(tf.format(utc_time_m),15)
		+padr(dtf.format(pub_n),20)
		+padr(tf.format(pub_m),12)
		;
		
	}
	public static String getHeader()
	{
		return padr("news_id",10) 
		+ padr("mktdata_id",15) 
		+ padr("symbol",8)
		+ padr("last",10)
		+padr("delta",10)
		+padr("percent_change",15)
		+padr("label",8)
		+padr("utc_time_n",15)
		+padr("utc_time_m",15)
		+padr("pub_n",20)
		+padr("pub_m",12)
		;
				
	}

	private static String padr(double v, int len)
	{
		return padr(String.valueOf(v),len);
		
	}
	private static String padr(int v, int len)
	{
		return padr(String.valueOf(v),len);
		
	}
	private static String padr(String v, int len)
	{
		StringBuilder sb= new StringBuilder(v.trim());
		int q= len-sb.length();
		if (q<=0 )
			return v.substring(0,len);
		
		for (int i=0;i<=q;i++)
			sb.append(" ");
		
		return sb.toString();	
	}
}