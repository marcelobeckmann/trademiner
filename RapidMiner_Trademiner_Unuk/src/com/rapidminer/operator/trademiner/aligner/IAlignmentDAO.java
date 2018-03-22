package com.rapidminer.operator.trademiner.aligner;

import java.sql.SQLException;
import java.util.List;

public interface IAlignmentDAO {
	
	public List <Alignment> getRawAlignment(String symbol,String delta, String shift) throws SQLException;
	
	public void deleteTicket(String ticket);

	public void save(int news_id,String symbol,String delta, String ticket,String label, String shift);
	
	public String getValignmentSQL(String delta, String shift);
	
	public double getPrevious(Alignment al);
	
	public double getNext(Alignment al);
	
	public double getFitness(String sql);
	
	public void replaceTicket(String ticketOrig,String ticketDest);

}
