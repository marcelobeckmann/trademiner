package com.rapidminer.operator.preprocessing.deprecated;


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

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.DummyPortPairExtender;
import com.rapidminer.operator.ports.PortPairExtender;

/**
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
	public static final String PARAMETER_OPTMIZE_MKTDATA_SHIFT = "optimize_time_shift";
	public static final String PARAMETER_OPTMIZE_VALIDATION_PROCESS = "opt_validation_process";
	public static final String PARAMETER_OPTMIZE_SQL_FITNESS_RETRIEVAL = "opt_sql_fitness";
	public static final String PARAMETER_OPTMIZE_STEP = "opt_shift_step";
	public static final String PARAMETER_OPTMIZE_MIN = "opt_shift_min_value";
	public static final String PARAMETER_OPTMIZE_MAX = "opt_shift_max_value";
	
	public static final String ALIGNMENT_TYPE_RESTRICTIVE="RestrictiveAlignment";
	public static final String ALIGNMENT_TYPE_SIMPLE="RestrictiveSimple";
	
	
	private PortPairExtender dummyPorts = new DummyPortPairExtender("through", getInputPorts(), getOutputPorts());
	
	
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
		
		/*
		
		String delta =  getParameterAsString(PARAMETER_ALIGNER_WINDOW);
		boolean optmize = getParameterAsBoolean(PARAMETER_OPTMIZE_MKTDATA_SHIFT);
		String table = getParameterAsString(PARAMETER_MKTDATA_TABLE) ;
		String index = getParameterAsString(PARAMETER_INDEX);
		String shift = getParameterAsString(PARAMETER_MKTDATA_SHIFT);
		String suffix = getParameterAsString(PARAMETER_TICKET);
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
		//////////////////////////////////////////
				if (optmize)
				{
					optmizeShift(symbol);
				}
				else
				{
					SimpleAlignment a1 ;
					
				//	a1 = getAlignerInstance(table, index);
					//totalRecords+=a1.alignment(symbol, delta, symbol+"_"+suffix, shift);
					
					this.checkForStop();
				}
			}
			System.out.println("######## total alignment records generated: "+totalRecords);
		} catch (Exception e) {
			e.printStackTrace();

		}
					
		dummyPorts.passDataThrough();
		
		*/
	}
    /*
	private SimpleAlignment getAlignerInstance(String table, String index) throws UndefinedParameterError, SQLException {
		SimpleAlignment a1;
		String parameter = getParameterAsString(PARAMETER_ALIGNER);
		if (parameter.equals(ALIGNMENT_TYPE_RESTRICTIVE)) {

			 a1 = new RestrictiveAlignment(this,table, index);
			 System.out.println("### Alignment type: " +ALIGNMENT_TYPE_RESTRICTIVE);
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
	
	private void optmizeShift(String symbol) throws Exception
	{
		String delta =  getParameterAsString(PARAMETER_ALIGNER_WINDOW);
		String table = getParameterAsString(PARAMETER_MKTDATA_TABLE) ;
		String index = getParameterAsString(PARAMETER_INDEX);
		String shift;
		String suffix = getParameterAsString(PARAMETER_TICKET);

		int min = getParameterAsInt(PARAMETER_OPTMIZE_MIN);
		int max = getParameterAsInt(PARAMETER_OPTMIZE_MAX);
		int step = getParameterAsInt(PARAMETER_OPTMIZE_STEP);
		Map<String,Double> fitnesses = new HashMap<String,Double>();
		

		String rmp = getParameterAsString(PARAMETER_OPTMIZE_VALIDATION_PROCESS);
		
		String destTicket = symbol+"_"+suffix;
		for (int i=min;i<=max;i+=step)
		{
			shift = String.format("%02d", Math.abs(i));
			shift +=":00:00";
			if (i<0) {
				shift = "-"+shift;
			}
			System.out.println("### Optmizing for shift "+ shift);
			
			String optTicket = destTicket+"_"+shift;
			
			SimpleAlignment a1 = getAlignerInstance(table, index);
			//delete the pre existing ticket first
			SimpleAlignment.dao.deleteTicket(optTicket);
			a1.alignment(symbol, delta, optTicket, shift);
			this.checkForStop();
			
	//		prepareMacroFile(optTicket,symbol);
			Executor exec = new Executor();
			
			exec.executeProcess(rmp);
			double fitness=retrieveFitness();
			
			fitnesses.put(optTicket,fitness);
		}
		
		//What to do with this ticket?
		String bestTicket=discardLeastAndKeepHighestTicket(fitnesses, destTicket);
		
		//restoreMacroFile();
		
	}
	
	private void prepareMacroFile(String ticket, String symbol) throws Exception
	{
		final String MACRO="ticket_";
		final String MACRO_SYMBOL="symbol__";
		final String EOL="\r";
		Files.copy(new File(MACRO_FILE), new File(MACRO_FILE+".bak"));
		StringBuilder sb= new StringBuilder();
		BufferedReader in   =null;
		PrintWriter writer = null;
		try
		{
			in   = new BufferedReader(new FileReader(MACRO_FILE));
			String line;
			while ((line=in.readLine())!=null)
			{
				if (line.startsWith(MACRO))
				{
					line = MACRO +";"+ticket.replace(':', '_');
					
				}
				else if (line.startsWith(MACRO_SYMBOL))
				{
					line = MACRO_SYMBOL +";"+symbol;
					
				}
				sb.append(line+EOL);
			}
			
			writer = new PrintWriter (MACRO_FILE);
			writer.print(sb.toString());
		
		} catch (Exception e)
		{
			Files.copy(new File(MACRO_FILE+".bak"), new File(MACRO_FILE));
			throw e;
		}
		finally {
			if (in!=null)
			{
				in.close();
			}
			
			if (writer!=null)
			{
				writer.close();
			}
		}
		
		
	}
	

	
	private void executeProcess(String ticket) throws OperatorException
	{
	

		
		ProcessEmbeddingOperator processOp = new ProcessEmbeddingOperator(this.getOperatorDescription());
		
		processOp.setParameter(ProcessEmbeddingOperator.PARAMETER_PROCESS_FILE, getParameterAsString(PARAMETER_OPTMIZE_VALIDATION_PROCESS));
		Object st = processOp.getParameterAsRepositoryLocation(PARAMETER_OPTMIZE_VALIDATION_PROCESS);
		System.out.println("### Executing "+getParameterAsString(PARAMETER_OPTMIZE_VALIDATION_PROCESS));
		processOp.doWork();
		
		
	}
	
	public void doExecuteProcess() throws OperatorException {
		Process process=null;
		try {
			process = loadIncludedProcess();
		} catch (RepositoryException e) {
			throw new UserError(this, e, 312, getParameterAsString(PARAMETER_OPTMIZE_VALIDATION_PROCESS), e.getMessage());
		}
		Map<String, String> macroMap = new HashMap<String, String>();
		process.run(new IOContainer(),LogService.UNKNOWN_LEVEL, macroMap, false);
		
	}
	private Process loadIncludedProcess() throws UndefinedParameterError, UserError, RepositoryException {
		
		RepositoryLocation location = getParameterAsRepositoryLocation(PARAMETER_OPTMIZE_VALIDATION_PROCESS);
		Entry entry = location.locateEntry();
		if (entry == null) {
			throw new RepositoryException("Entry '"+location+"' does not exist.");
		} else if (entry instanceof ProcessEntry) {
			Process process;
			try {
				process = new RepositoryProcessLocation(location).load(null);
				process.setRepositoryAccessor(getProcess().getRepositoryAccessor());
				
				for (Operator op : process.getRootOperator().getAllInnerOperators()) {
					op.setBreakpoint(BreakpointListener.BREAKPOINT_AFTER, false);
					op.setBreakpoint(BreakpointListener.BREAKPOINT_BEFORE, false);
				}

			} catch (IOException e) {
				throw new UserError(this, 302, location, e.getMessage());
			} catch (XMLException e) {
				throw new UserError(this, 401, e.getMessage());
			}			
		
			return process;
		} else {
			throw new RepositoryException("Entry '"+location+"' is not a data entry, but "+entry.getType());
		}		
	}
	

	private double retrieveFitness() throws UndefinedParameterError
	{
		String sql = getParameterAsString(PARAMETER_OPTMIZE_SQL_FITNESS_RETRIEVAL);
		return SimpleAlignment.dao.getFitness(sql);
		
	}
	
	private String discardLeastAndKeepHighestTicket(Map<String,Double> fitnesses,String destTicket)
	{
		Set<String> keys = fitnesses.keySet();
		double maxFitness=0;
		String maxTicket=null;
		for (String key:keys)
		{
			double actualFitness = fitnesses.get(key);
			if (actualFitness>maxFitness) {
				maxFitness=actualFitness;
				maxTicket=key;
			}
			
		}
		//Remove the loosers from db
		for (String key:keys)
		{
			if (!key.equals(maxTicket)) {
				SimpleAlignment.dao.deleteTicket(key);
			}
		}
		
		SimpleAlignment.dao.replaceTicket(maxTicket, destTicket);
		return maxTicket;
	}
	
	private void restoreMacroFile() throws IOException
	{
		

		Files.copy(new File(MACRO_FILE+".bak"), new File(MACRO_FILE));
	
		
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
						new String[] {ALIGNMENT_TYPE_RESTRICTIVE, ALIGNMENT_TYPE_SIMPLE},0,false);
			
		types.add(alignmentTypes);
		
		//------------- new optimization stuff ----------------
		types.add(
		new ParameterTypeString(PARAMETER_MKTDATA_SHIFT, "Performs a time shift (put - at the begning for negative shifts) to adjust and make a shift in the mktdata utc_time_ field. Useful to fix timezone problems and possible adjustments for information leaking. Use time format HH:mm:ss","" , true));
		
		//TODO CHECK IF IS POSSIBLE TO PERFORM A BINARY SEARCH
		types.add(
		new ParameterTypeBoolean(PARAMETER_OPTMIZE_MKTDATA_SHIFT, "Executes an exaustive search in the shift parameter, given the result of a specified experiment below.",false , true)
		);
		
		ParameterType param=
				new ParameterTypeRepositoryLocation(PARAMETER_OPTMIZE_VALIDATION_PROCESS, "The rapidminer rmp process to validate the optimization. This process must store a fitness result (acc, auc, fmeasure, etc...) on Trademiner database, and this result must be able to be retrieved as a scalar with SQL.", true);
		param.registerDependencyCondition(new BooleanParameterCondition(this,PARAMETER_OPTMIZE_MKTDATA_SHIFT, true, true));
		types.add(param);
		
		param =new ParameterTypeString(PARAMETER_OPTMIZE_SQL_FITNESS_RETRIEVAL, "The SQL to retrieve the fitness result as a scalar from Trademiner database.","" , false);
		param.registerDependencyCondition(new BooleanParameterCondition(this,PARAMETER_OPTMIZE_MKTDATA_SHIFT, true, true));
		types.add(param);

		param =new ParameterTypeInt(PARAMETER_OPTMIZE_STEP, "The shift step of optmization (in hours).",1,7,1, false);
		param.registerDependencyCondition(new BooleanParameterCondition(this,PARAMETER_OPTMIZE_MKTDATA_SHIFT, true, true));
		types.add(param);

		param =new ParameterTypeInt(PARAMETER_OPTMIZE_MIN, "The min value accepted for optmization (in hours). Negative values are accepted with -.",-72,72,-5 , false);
		param.registerDependencyCondition(new BooleanParameterCondition(this,PARAMETER_OPTMIZE_MKTDATA_SHIFT, true, true));
		types.add(param);

		param =new ParameterTypeInt(PARAMETER_OPTMIZE_MAX, "The max value accepted for optmization (in hours).",-72,72,5 , false);
		param.registerDependencyCondition(new BooleanParameterCondition(this,PARAMETER_OPTMIZE_MKTDATA_SHIFT, true, true));
		types.add(param);
		
		param =new ParameterTypeInt(PARAMETER_OPTMIZE_MAX, "The max value accepted for optmization (in hours).",-72,72,5 , false);
		param.registerDependencyCondition(new BooleanParameterCondition(this,PARAMETER_OPTMIZE_MKTDATA_SHIFT, true, true));
		types.add(param);
		
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
*/

}