package com.rapidminer.operator.trademiner.acquisition;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.rapidminer.operator.trademiner.util.ConnectionFactory;

public class ContentUpdater  {

		
		public static void main(String args[]) 
		{
			new ContentUpdater().execute();
			
		}
		/**ttp
		 * @param args
		 * @throws SQLException 
		 */

		Connection con2 =null;
		PreparedStatement stmt2=null;
		PreparedStatement stmtMarkProblem=null;
		Connection con3= null;
		final int NUMBER_OF_LOOPS=5;
	
		public void execute()  {
			
		
			
		String sql = "select id,link,title from  rss where (content is null or content ='' or content ='?') and clean_content is null and problem<5 order by id limit 1000";
			
			Connection con =null;
			PreparedStatement stmt=null;
			
			ResultSet rs=null;

			
			try {
				con = ConnectionFactory.getConnection();
				con2 = ConnectionFactory.getConnection();
				con3= ConnectionFactory.getConnection();	
				stmt2 = con2.prepareStatement("update rss set content=? where id=?");
				stmtMarkProblem = con3.prepareStatement("update rss set problem=problem+1 where id=?");
				stmt= con.prepareStatement(sql);
				int count=0;
				while (true) {

					logError("Retrieving records..");

					rs= stmt.executeQuery();
				
					while (rs.next())
					{
						processUrl(rs);
						
					}
					
					count++;
					
					if (count>=NUMBER_OF_LOOPS)
						break;
					
					int i[] = new int[2];
					while (i[1]==0) {
						logError("Counting...");
						i = count();
						logError("Rss size:" + i[0] +", without content:" + i[1]);
						
						if (i[1]!=0)
							break;
						logError("Waiting for new updates...");
						Thread.currentThread().join(60000*3);
					} 
					
				}
				
			}catch (Exception e)
			{
				logError(e.getMessage());
				e.printStackTrace();
				
			}
			finally {
				
				ConnectionFactory.closeConnection(rs, stmt, con);
				ConnectionFactory.closeConnection(null, stmt2, con2);		
				ConnectionFactory.closeConnection(null, null, con3);		
				
			}
			
		}
		public void processUrl(ResultSet rs) 
		{
			int length;
			int id=0;
			try {

				con2.setAutoCommit(true);

				id = rs.getInt("id");
				String link= rs.getString("link");
				String title = rs.getString("title");
				if (link==null)
				{
					
					logError("url for id "+ id +" is null" );
					return;	
				}
				logError("## obtaining data from "+ link);

				String data = this.readContentApache(link);
				
				if (data==null || data.length()==0)
				{
					//Tentar recuperar o artigo que estah falhando de outra maneira
				//	data = googleIt(title);
				//	System.out.println(title + "\n" + data);
					
				}
					
				
				if (data.length()!=0) {
					stmt2.setString(1,data.replace("'", " "));
					stmt2.setInt(2,id);
					
					stmt2.execute();
					
					length=data.length();
			
				
					logError("## updated id " + id + ", " + length + " bytes." );
				}
				else
				{
					logError("## Error updating id " + id + ", length 0.");
				}
				
				System.gc();
				
				//br.close();
			
			//is.re
			
			
			}
			catch (Throwable e)
			{
				
				try {
				stmtMarkProblem.setInt(1, id);
				stmtMarkProblem.execute();
				logError("Id " + id + " marked as problem");
				}
				catch (Exception e2)
				{
					e2.printStackTrace();
					
				}
				logError("Error:" + e.getMessage());
				
				e.printStackTrace();
				//throw e;
				
			}
			finally {
				
			}
		}
		
		public static String readContentOld(String link) throws IOException 
		{	BufferedReader bufferedReader =null;
			StringBuilder sb= new StringBuilder();
			try {
				
				 URL url = new URL(link);
			     URLConnection urlConnection = url.openConnection();
			     
			     HttpClient client = HttpClientBuilder.create().build();
			     
			     
			     InputStream inputStream = urlConnection.getInputStream();
		         bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			     String line = bufferedReader.readLine();
			     while (line != null) {
			       // System.out.println(line);
			        line = bufferedReader.readLine();
			        sb.append(line);
			      }
			     if (sb.length()==0)
					{
			    	 Object o =urlConnection.getContent();
			    	 System.out.println(o);
						
					} 
			     
			}
			finally {
				
				
				if (bufferedReader!=null) {
					bufferedReader.close();
				}
				
			}
			return sb.toString();
		}
		public static String readContentApache(String link) throws Exception 
		{	BufferedReader bufferedReader =null;
			StringBuilder sb= new StringBuilder();
			try {
			     HttpClient client = HttpClientBuilder.create().build();
			     HttpGet request=new HttpGet(link);
			     
			     HttpResponse response = client.execute(request);

			     //System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
			    
			     int statusCode=response.getStatusLine().getStatusCode();
			     if (statusCode!=200)
			     {
			    	 logError("## Error! Status code " + statusCode + ","+ response.getStatusLine().getReasonPhrase());
			    	 return "" ;
			     }
			     BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			     String line=null;
			     while ((line=rd.readLine()) != null) {
			        sb.append(line);
			      }
			     
			}
			finally {
				
				
				if (bufferedReader!=null) {
					bufferedReader.close();
				}
				
			}
			return sb.toString();
		}
		
		
		public  int[] count() 
		{
			
			int i[]= new int[2];
			
			Connection conn= null;
			PreparedStatement pstmt = null;
			
			PreparedStatement pstmt2 = null;
			ResultSet rs=null;
			try {
			
				conn= ConnectionFactory.getConnection();
				pstmt2 = conn.prepareStatement("select count(*) c1 from rss where (content is null or content='' or content='?') and clean_content is null and problem<5");
			
				pstmt = conn.prepareStatement("select count(*) c1 from rss");
			
				
				rs= pstmt.executeQuery();
				
				rs.next();
				i[0]=rs.getInt("c1");
				
				rs.close();
				rs= pstmt2.executeQuery();
				rs.next();
				i[1]=rs.getInt("c1");
				
				
				
			}
			catch (Exception e)
			{ 
				
				logError("Error:" + e.getMessage());
				e.printStackTrace();
				}
			finally {
				try {
				
					ConnectionFactory.closeConnection(rs,pstmt, conn);
					ConnectionFactory.closeConnection(null,pstmt2, null);
					
				
				}
				catch (Exception e)
				{}
				
				
			}
			
			return i;
		}
		
		public static void logError(String msg)
		{
			
			System.out.println(msg);
		}
		
		
		//This won't work because it's against the google's TOS
/*		
		public static String googleIt(String title) throws Exception
		{
			
			String googleURl= "http://www.google.com/?gws_rd=cr&ei=hC0lWaS7M6nMgAbL952ABw#q="+ title.replace(' ', '+');
			
			String content = readContentApache(googleURl);
			
			
			return content;
			
		}
		
	*/	
	}
