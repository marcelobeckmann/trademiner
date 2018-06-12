package com.rapidminer.operator.trademiner.aligner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.rapidminer.operator.Operator;

public class SimpleAlignment {

	private Operator operator;
	public static AlignmentDAOImpl dao;

	public SimpleAlignment(Operator operator, String table, String index) throws SQLException {
		this.operator = operator;
		dao = new AlignmentDAOImpl(table, index);

	}

	public int alignment(String symbol, String delta, String ticket, String shift)
			throws SQLException {
		// Deletes all with the same ticket
		// dao.delete(ticket);

		List<Alignment> alignments = dao.getRawAlignment(symbol, delta, shift);
		int news_id = -1;
		int count=alignments.size();
		List<Alignment> al2 = new ArrayList<Alignment>();

		for (Alignment al : alignments) {
			if (news_id == -1) {
				news_id = al.news_id;

			}
			if (al.news_id != news_id) {
				processAlignment(symbol,delta, ticket, al2,shift);

				news_id = al.news_id;
				al2.clear();
			}
			al2.add(al);

		}

		return count;
	}

	public void processAlignment(String symbol,String delta, String ticket,
			List<Alignment> algs, String shift) {
		//operator.logError(Alignment.getHeader());
		int surgeCount = 0;
		int plungeCount = 0;
		int news_id = -1;
		
		for (Alignment al : algs) {
			if (news_id == -1) {
				news_id = al.news_id;

			}
			if (al.label > 1)
				surgeCount++;
			if (al.label < -1)
				plungeCount++;

			// operator.log(al.toString());

		}

		double deltal = calculateDelta(algs);
		String label = "0";
		if (surgeCount > plungeCount && deltal > 0)
			label = "2";

		if (surgeCount < plungeCount && deltal < 0)
			label = "-2";
		// TODO WRITE THE STUFFS
		// news_id,String symbol,String delta, String ticket,String label
		dao.save(news_id, symbol, delta, ticket, label, shift);
		//operator.logError("########### label:" + label+",shift:"+shift);
	}

	public double calculateDelta(List<Alignment> als) {

		double last1 = dao.getPrevious(als.get(0));
		if (last1 == -1) {
			last1 = als.get(0).last + als.get(0).delta;

		}

		double last2 = dao.getPrevious(als.get(als.size() - 1));

		if (last2 == -1) {
			last1 = als.get(als.size() - 1).last;
		}
		return last2 - last1;
	}
	
	

}
