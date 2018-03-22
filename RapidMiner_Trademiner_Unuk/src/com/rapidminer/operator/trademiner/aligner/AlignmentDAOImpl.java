package com.rapidminer.operator.trademiner.aligner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.rapidminer.operator.trademiner.util.ConnectionFactory;



public class AlignmentDAOImpl implements IAlignmentDAO {

	
	Connection conn =null;

	private String table;
	private String index;
	public AlignmentDAOImpl(String table, String index) throws SQLException
	{
		conn = ConnectionFactory.getConnection();
		this.table=table;
		this.index=index;
	}
	
	public List <Alignment> getRawAlignment(String symbol,String delta, String shift)
	throws SQLException 
	{
		List<Alignment> als=new ArrayList<Alignment>();
		
		PreparedStatement stmt = null;
		ResultSet rs= null;
		try {
			conn=ConnectionFactory.getConnection();
			String sql = getValignmentSQL(delta, shift);
			System.out.println(sql);
			stmt= conn.prepareStatement(sql);
			
			System.out.println("symbol:" + symbol);
			stmt.setString(1,symbol);
			stmt.setString(2,delta.replace("-", ""));
			
			rs= stmt.executeQuery();

			while (rs.next())
			{ 
				Alignment al = new Alignment();
				al.load(rs);
				als.add(al);
				
			}
			
		}
		finally {
			

			ConnectionFactory.closeConnection(rs,stmt,null);
			
		}
		
		return als;
	}
	public void deleteTicket(String ticket)
	{
		PreparedStatement stmt= null;
		try {
		stmt=conn.prepareStatement("delete from alignment where ticket=?");
		stmt.setString(1,ticket);
		stmt.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
		}
		finally {
			
			ConnectionFactory.closeConnection(null, stmt, null);
			
		}
	}
	public void save(int news_id,String symbol,String delta, String ticket,String label, String shift)
	{
		
		String sql = "insert into alignment (news_id,symbol,delta,ticket,label, shift)" +
		" values (?,?,?,?,?,?)";
		
		//TODO TRATAR MINUTOS
		int timeShift=0;
		try {
			if (!"".equals(shift))
			{
				Calendar cal = Calendar.getInstance();
			  	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				cal.setTime(sdf.parse(shift.replace("-", "")));
				timeShift = cal.get(Calendar.HOUR);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		};
		if (shift.startsWith("-"))
		{
			timeShift = -timeShift;
		}
		
		PreparedStatement stmt = null;
		try {
			
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, news_id);
			stmt.setString(2,symbol);
			stmt.setString(3,delta);
			stmt.setString(4,ticket);
			stmt.setString(5,label);
			stmt.setInt(6,timeShift);
			
			stmt.execute();
		}
		catch (Exception e)
		{ e.printStackTrace(); }
		finally {
			
			ConnectionFactory.closeConnection(null, stmt,null);
		}
	}
	
	public String getValignmentSQL(String delta, String shift )
	{
		

		String sql=	"select " +
		"		  n.Id        AS news_id, " +
		"         n.symbol    as symbol," +
		"		  n.utc_time_ AS utc_time_n, " +
		"		  m.utc_time_ AS utc_time_m, " +
		"		  n.Published AS pub_n, " +
		"		  m.time      AS pub_m, " +
		"         m.date_     as pub_m_date, "+
		"		  m.id        AS mktdata_id, " +
		"		  m.last, " +
		"		  delta, " +
		"		  percent_change, " +
		"		  class as label " +
		"		 from "+table+" m , rss n " +
		"		where m.symbol = ? " ;
		sql +="		and m.date_ = next_trade_date " ;
		
		String mktdataTime="m.utc_time_";
		if (!"".equals(shift) && !shift.equals("00:00:00"))
		{
			if (shift.contains("-")) {
				mktdataTime = " subtime( m.utc_time_ ,'"+ shift.replace("-", "") +"')";
			}
			else
			{
				mktdataTime = " addtime( m.utc_time_ ,'"+ shift +"')";
			}
		}
		
		
		
		if (delta.contains("-")) {
			sql += " and " + mktdataTime+ "_ between  subtime(    next_trade_time       ,?) " +
			" and next_trade_time " ;
		} else
		{
			sql += "		and "+ mktdataTime +" between  next_trade_time " +
			" and addtime(    next_trade_time       ,?)" ;
		}
		if (!"".equals(index))
		{
			sql+=" 		and n.symbol in (select symbol from symbol where `index`='"+index+"' and status=1)" ;
			
		}
		else
		{	
			sql+="		and n.symbol = m.symbol " ;
		}
		sql+="		and clean_content is not null ";
					
		
		sql+="		order by n.id, m.utc_time_ " ; 
		// + "		limit 1000 ";
			
			
			return sql;
	}
	
	public double getPrevious(Alignment al)
	{
		double pal=-1;
		

		PreparedStatement stmt = null;
		ResultSet rs= null;
		try {
			stmt= conn.prepareStatement("select * from "+table+" where symbol=? and date_=? and time<? order by time desc limit 1" );
			
			stmt.setString(1,al.symbol);
			stmt.setDate(2,new java.sql.Date(al.pub_m_date.getTime()));
			stmt.setTimestamp(3,new java.sql.Timestamp(al.pub_m.getTime()));
			rs = stmt.executeQuery();
			
			if (rs.next())
			{
				pal = rs.getDouble("last");
				
				
			}
			
		}
		catch (Exception e)
		{
			
			e.printStackTrace();
			
		}
		finally {
			ConnectionFactory.closeConnection(rs, stmt, null);
			
		}
		
		return pal;
		
	}


	
	public double getNext(Alignment al)
	{
		double pal= -1;
		

		PreparedStatement stmt = null;
		ResultSet rs= null;
		try {
			stmt= conn.prepareStatement("select * from "+table+" where symbol=? and date_=? and time>? order by time limit 1" );
			
			stmt.setString(1,al.symbol);
			stmt.setDate(2,new java.sql.Date(al.pub_m_date.getTime()));
			stmt.setTimestamp(3,new java.sql.Timestamp(al.pub_m.getTime()));
			rs = stmt.executeQuery();
			
			if (rs.next())
			{
				pal= rs.getDouble("last");
				
			}
			
		}
		catch (Exception e)
		{
			
			e.printStackTrace();
			
		}
		finally {
			ConnectionFactory.closeConnection(rs, stmt, null);
			
		}
		
		return pal;
		
	}

	public double getFitness(String sql) {
		
		double fitness=0;


		PreparedStatement stmt = null;
		ResultSet rs= null;
		try {
			stmt= conn.prepareStatement(sql );
			
			rs = stmt.executeQuery();
			
			if (rs.next())
			{
				fitness= rs.getDouble(1);
				
			}
			
		}
		catch (Exception e)
		{
			
			e.printStackTrace();
			
		}
		finally {
			ConnectionFactory.closeConnection(rs, stmt, null);
			
		}
		
		
		
		return fitness;
	}

		

	public void replaceTicket(String ticketOrig,String ticketDest)
	{
		
		PreparedStatement stmt = null;

		try {
			stmt= conn.prepareStatement("update alignment set ticket=? where ticket = ?" );
			
			stmt.setString(1,ticketDest);
			stmt.setString(1,ticketOrig);
			stmt.execute();
						
		}
		catch (Exception e)
		{
			
			e.printStackTrace();
			
		}
		finally {
			ConnectionFactory.closeConnection(null, stmt, null);
			
		}


		
	}


}
