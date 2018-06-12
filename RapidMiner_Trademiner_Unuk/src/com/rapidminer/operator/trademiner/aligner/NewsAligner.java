package com.rapidminer.operator.trademiner.aligner;


/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2009 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.DummyPortPairExtender;
import com.rapidminer.operator.ports.PortPairExtender;
import com.rapidminer.operator.preprocessing.deprecated.RestrictiveAlignment2M;
import com.rapidminer.operator.preprocessing.deprecated.RestrictiveAlignment2MPercent;
import com.rapidminer.operator.trademiner.util.ConnectionFactory;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;

/**est
 * This is the Numerical2Date tutorial operator.
 * 
 * @author Marcelo Beckmann
 */
public class NewsAligner extends Operator {

	//private InputPort input = getInputPorts().createPort("through");
	//private OutputPort output = getOutputPorts().createPort("through");
	
	public static final String PARAMETER_ALIGNER_WINDOW = "aligner_window";
	public static final String PARAMETER_EXPERIMENT_SYMBOL = "experiment_symbol";
	public static final String PARAMETER_EXPERIMENT_EXCHANGE = "experiment_exchange";
	public static final String PARAMETER_TICKET = "ticket";
	public static final String PARAMETER_MKTDATA_TABLE = "mktdata_table";
	public static final String PARAMETER_INDEX = "news_index_filter";
	public static final String PARAMETER_MKTDATA_SHIFT = "mktdata_time_shift";
	public static final String PARAMETER_ALIGNER = "aligner_impl";
	
	public static final String ALIGNMENT_TYPE_RESTRICTIVE="RestrictiveAlignment";
	public static final String ALIGNMENT_TYPE_RESTRICTIVE2M="RestrictiveAlignment2M";
	public static final String ALIGNMENT_TYPE_SIMPLE="AlignmentSimple";
	public static final String ALIGNMENT_TYPE_RESTRICTIVE_PERCENT="RestrictiveAlignment2MPercent";
	public static final String ALINGMENT_PERCENT="percent";
	
	
	private PortPairExtender dummyPorts = new DummyPortPairExtender("through", getInputPorts(), getOutputPorts());
	private double percent;
	
	private static final String MACRO_FILE="C:\\home\\nelson\\rm\\REPOSITORY\\macros.csv";
	/**
	 * Constructor
	 */
	public NewsAligner(OperatorDescription description) {
		super(description);
		
		dummyPorts.start();
		
		getTransformer().addRule(dummyPorts.makePassThroughRule());
	
	}

	@Override
	public void doWork() throws OperatorException {
		
		
		
		String delta =  getParameterAsString(PARAMETER_ALIGNER_WINDOW);
		String table = getParameterAsString(PARAMETER_MKTDATA_TABLE) ;
		String index = getParameterAsString(PARAMETER_INDEX);
		String shift = getParameterAsString(PARAMETER_MKTDATA_SHIFT);
		String suffix = getParameterAsString(PARAMETER_TICKET);
		
		percent = getParameterAsDouble(ALINGMENT_PERCENT);
		
		
		
		
		if (suffix==null || suffix.length()==0)
		{
			Calendar cal = Calendar.getInstance();
			suffix =cal.get(Calendar.YEAR)+""+ (cal.get(Calendar.MONTH)+1)+""+cal.get(Calendar.DAY_OF_MONTH);
		}
		
		try {
			List<String> symbols = getSymbols();
			int totalRecords=0;
			for (String symbol : symbols) {
				logError("### Processing: " + symbol);
				logWarning("### Processing: " + symbol);
				SimpleAlignment a1 ;
				
				a1 = getAlignerInstance(table, index);
				totalRecords+=a1.alignment(symbol, delta, symbol+"_"+suffix, shift);
				this.checkForStop();
		}
			System.out.println("######## total alignment records generated: "+totalRecords);
		} catch (Exception e) {
			e.printStackTrace();

		}
					
		dummyPorts.passDataThrough();
		
	}
    /** Obtain the aligner according the alignment type */
	
	private SimpleAlignment getAlignerInstance(String table, String index) throws UndefinedParameterError, SQLException {
		SimpleAlignment a1;
		String parameter = getParameterAsString(PARAMETER_ALIGNER);
		if (parameter.equals(ALIGNMENT_TYPE_RESTRICTIVE)) {

			 a1 = new RestrictiveAlignment(this,table, index);
			 System.out.println("### Alignment type: " +ALIGNMENT_TYPE_RESTRICTIVE);
		}
		else if (parameter.equals(ALIGNMENT_TYPE_RESTRICTIVE2M)) {

			 a1 = new RestrictiveAlignment2M(this,table, index);
			 System.out.println("### Alignment type: " +ALIGNMENT_TYPE_RESTRICTIVE2M);
		}
		else if (parameter.equals(ALIGNMENT_TYPE_RESTRICTIVE_PERCENT)) {

			 a1 = new RestrictiveAlignment2MPercent(this,table, index);
			 ((RestrictiveAlignment2MPercent)a1).setPercentThreshold(percent);
			 System.out.println("###### percent threshold was set to "+ percent);
			 System.out.println("### Alignment type: " +ALIGNMENT_TYPE_RESTRICTIVE_PERCENT);
		}
		else if (parameter.equals(ALIGNMENT_TYPE_SIMPLE))
		{
			 a1 = new SimpleAlignment(this,table, index);
			 System.out.println("### Alignment type: " +ALIGNMENT_TYPE_SIMPLE);
			
		}
		else
		{
			throw new IllegalArgumentException("Alignment type " + parameter +" is not valid.");
		}

		return a1;
	}
		public List<ParameterType> getParameterTypes() {
	List<ParameterType> types = super.getParameterTypes();
	
		types.add(new ParameterTypeString(PARAMETER_ALIGNER_WINDOW, "The time window that a asset price takes to be affected by a new.","00:02:00" , false));
	
		types.add(new ParameterTypeString(PARAMETER_EXPERIMENT_SYMBOL, "The asset symbol to align, use blakn to all, >, >=, or a set of symbols separated by comma.","" , false));

		types.add(new ParameterTypeString(PARAMETER_EXPERIMENT_EXCHANGE, "The exchange where the stocks are traded.","NYSE" , false));

		types.add(new ParameterTypeString(PARAMETER_MKTDATA_TABLE, "The table with asset prices.","mktdata" , false));

		types.add(new ParameterTypeString(PARAMETER_INDEX, "The index wich a set of assets makes parts. The associated symbols of this index will be used to filter the news.", "" , false));

		
		Calendar cal = Calendar.getInstance();
		//DateFormat df = DateFormat.getDateInstance();
		
		String suffix =cal.get(Calendar.YEAR)+""
		+ (cal.get(Calendar.MONTH)+1)+""+cal.get(Calendar.DAY_OF_MONTH);

		types.add(new ParameterTypeString(PARAMETER_TICKET, "A ticket id to identify the alignment operation in an experiment.",suffix , false));
		
		ParameterTypeCategory alignmentTypes= 
				new ParameterTypeCategory(PARAMETER_ALIGNER,
						"The alignment implementation.",
						new String[] {ALIGNMENT_TYPE_RESTRICTIVE, ALIGNMENT_TYPE_RESTRICTIVE+"2M",ALIGNMENT_TYPE_RESTRICTIVE_PERCENT,ALIGNMENT_TYPE_SIMPLE},0,false);
		
		types.add(new ParameterTypeDouble(ALINGMENT_PERCENT, "The percent of higs and lows in the labeling.",0,1, 0.75, false));

		
		
		types.add(alignmentTypes);
		
		//------------- new optimization stuff ----------------
		types.add(
		new ParameterTypeString(PARAMETER_MKTDATA_SHIFT, "Performs a time shift (put - at the begning for negative shifts) to adjust and make a shift in the mktdata utc_time_ field. Useful to fix timezone problems and possible adjustments for information leaking. Use time format HH:mm:ss","" , true));
			
		return types;
	}
	

	public  List<String> getSymbols() throws Exception {

		List<String> symbols = new ArrayList<String>();

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			conn = ConnectionFactory.getConnection();
			stmt = conn.createStatement();
			String symbol = this.getParameterAsString(PARAMETER_EXPERIMENT_SYMBOL);
			String sql = "SELECT symbol FROM symbol s where STATUS=1 " ;
			if (symbol!=null && symbol.length()!=0)
			{
				if (symbol.contains(","))
				{
					if (!symbol.contains("'"))
					{
						symbol = "'"+ symbol.replace(" ","").replace(",","','") + "'";
					}
					sql+=" AND symbol in ("+ symbol +")";
				}
				else if (symbol.startsWith(">="))
				{
					sql+=" AND symbol>='"+ symbol.replace(">=","") +"'";
				}
				else if (symbol.startsWith(">"))
				{
					sql+=" AND symbol>='"+ symbol.replace(">","") +"'";
				}
				else {
					sql+=" AND symbol='"+ symbol +"'";
				}
			}

			String ex = this.getParameterAsString(PARAMETER_EXPERIMENT_EXCHANGE);
			if (ex!=null && ex.length()!=0)
			{
				sql+=" AND EXCHANGE>='"+ ex +"'";
				
			}

			
			sql+=" order by symbol";
			rs = stmt.executeQuery(sql);

			while (rs.next())
				symbols.add(rs.getString("symbol"));
			return symbols;
		} finally {

			ConnectionFactory.closeConnection(rs, stmt, conn);
		}

	}


}