package com.rapidminer.operator.trademiner.aligner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.rapidminer.operator.Operator;

public class RestrictiveAlignment extends SimpleAlignment {

	RestrictiveAlignment(Operator operator, String table, String index) throws SQLException {
		super(operator, table, index);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public int alignment(String symbol, String delta, String ticket, String shift)
			throws SQLException {
		// Deletes all with the same ticket
		// dao.delete(ticket);
		
		List<Alignment> alignments = dao.getRawAlignment(symbol, delta, shift);
		int news_id = -1;
		int count=alignments.size();
		List<Alignment> al2 = new ArrayList<Alignment>();
		int numberOfDuplicated=0;
		
		for (Alignment al : alignments) {
			if (news_id == -1) {
				news_id = al.news_id;

			}
			//This is to avoid processing duplicated records retrieved by query
			if (al.news_id != news_id) {
				processAlignment(symbol,delta, ticket, al2,shift);

				news_id = al.news_id;
				al2.clear();
			}
			else
			{
				numberOfDuplicated++;
				
				
				
			}
			al2.add(al);

		}
		System.out.println("########## number of not stored:"+ numberOfDuplicated);
		return count;
	}



    @Override
	public void processAlignment(String symbol,String delta, String ticket,
			List<Alignment> algs, String shift) {
		//operator.logError(Alignment.getHeader());
		int surgeCount = 0;
		int plungeCount = 0;
		int news_id = -1;

	//	if (algs.size()>1)
		//{
			//throw new IllegalStateException("Error for ="+ algs.get(0).news_id + "/"+algs.get(1).news_id);
		//}
		for (Alignment al : algs) {
			if (news_id == -1) {
				news_id = al.news_id;
			}
			if (al.label > 1)
				surgeCount++;
			if (al.label < -1)
				plungeCount++;


		}

		
		
		double deltal = calculateDelta(algs);
		String label = "0";
		if (surgeCount > 0 && plungeCount==0   && deltal > 0)  //NO DELTAS
		{
			label = "2";
		}
		else if (plungeCount >0 && surgeCount ==0  && deltal < 0) 
		{
			label = "-2";
		}
		// TODO WRITE THE STUFFS
		// news_id,String symbol,String delta, String ticket,String label
		dao.save(news_id, symbol, delta, ticket, label, shift);
		//operator.logError("########### label:" + label+",shift:"+shift);
	}


}
