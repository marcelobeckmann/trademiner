package webcrawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * 
 * @author Marcelo Beckmann
 * 
 */
public class Monitor implements Runnable {

	private static String mainTitle;

	private String logFile;
	private String title;
	private List<String> errorPatterns = new ArrayList<String>();;
	private List<String> successPatterns = new ArrayList<String>();;

	private List<String> errorResults = new ArrayList<String>();
	private List<String> successResults = new ArrayList<String>();

	private String table;
	private int tableCount = -1;
	private Thread thread;
	private int interval = 60;
	private int keepLast = 5;
	private BufferedReader reader;

	public static String getMainTitle() {
		return mainTitle;
	}

	public static void setMainTitle(String mainTitle) {
		Monitor.mainTitle = mainTitle;
	}

	public int getKeepLast() {
		return keepLast;
	}

	public void setKeepLast(int showLast) {
		this.keepLast = showLast;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public int getTableCount() {
		return tableCount;
	}

	public Monitor() {

		errorPatterns.add("error");
		errorPatterns.add("exception");
		errorPatterns.add("failure");

		successPatterns.add("success");
		interval = 60;
	}

	public List<String> getErrorPatterns() {
		return errorPatterns;
	}

	public void setErrorPatterns(List<String> errorPatterns) {
		this.errorPatterns = errorPatterns;
	}

	public List<String> getSuccessPatterns() {
		return successPatterns;
	}

	public void setSuccessPatterns(List<String> successPatterns) {
		this.successPatterns = successPatterns;
	}

	public List<String> getErrorResults() {
		return errorResults;
	}

	public void setErrorResults(List<String> errorResults) {
		this.errorResults = errorResults;
	}

	public List<String> getSuccessResults() {
		return successResults;
	}

	public void setSuccessResults(List<String> successResults) {
		this.successResults = successResults;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public String getLogFile() {
		return logFile;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void start() {

		thread = new Thread(this);

		thread.start();

	}

	private BufferedReader getReader() throws IOException 
	{
		logFile = logFile.replace("<NOT FOUND>","");
		if (reader == null && new File(logFile).exists()) {
			reader = new BufferedReader(new FileReader(logFile));
		}
		else if (!new File(logFile).exists())
		{
			logFile += "<NOT FOUND>";
			
		}	
		return reader;
	}
	
	public void run() {

		
		
		while (!thread.isInterrupted()) {
			try {

				readTable();

				readFile();
			} catch (IOException | SQLException e) {
				errorResults.add(new Date() + " - error collecting data :" + e.getMessage());
				e.printStackTrace();
			}
			try {
				thread.join(interval * 1000);
			} catch (InterruptedException e) {
			}
		}

		
	}

	public void stop() {
		if (thread != null) {
			thread.interrupt();

		}
		if (reader != null) {
			try {
				reader.close();
				reader = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void readFile() throws IOException {

		if (getReader() ==null)
		{	return; }
		
		String line = null;

		while ((line = reader.readLine()) != null && !thread.isInterrupted()) {

			String lineLower = line.toLowerCase();
			w1: {
				for (String errorPattern : errorPatterns) {
					if (lineLower.contains(errorPattern.toLowerCase())) {
						errorResults.add(new Date() + "- ERROR :" + line);
						break w1;
					}
				}

				for (String successPattern : successPatterns) {
					if (lineLower.contains(successPattern.toLowerCase())) {
						successResults.add(new Date() + "- SUCCESS:" + line);
						break w1;
					}

				}

				
			}

		}

		errorResults = keepLast(errorResults);
		successResults = keepLast(successResults);
	

	}

	private List<String> keepLast(List<String> toKeep) {
		List<String> remaining = new ArrayList<String>();
		int currentSize = toKeep.size();

		for (int i = currentSize - keepLast; i < currentSize; i++) {
			if (i < 0) {
				continue;
			}
			remaining.add(toKeep.get(i));

		}
		return remaining;

	}

	private void readTable() throws SQLException {

		if (table == null || table.isEmpty()) {
			return;
		}

		try (Connection conn = ConnectionFactory.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("select count(*) from " + table);

		) {

			if (rs.next()) {
				tableCount = rs.getInt(1);
			}
		} finally {

		}

	}

	public static List<Monitor> load() throws IOException {
		List<Monitor> monitors = new ArrayList<Monitor>();
		final String PROPERTIES_FILE = "logmon.properties";

		Properties properties = new Properties();

		try (InputStream is = Monitor.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);) {
			properties.load(is);
			mainTitle = properties.getProperty("main-title");

			int i = 1;

			while (properties.get("title-" + i) != null) {
				Monitor mon = new Monitor();

				mon.setTitle(properties.getProperty("title-" + i));
				mon.setLogFile(properties.getProperty("logfile-" + i));
				mon.setTable(properties.getProperty("table-" + i));
				mon.setInterval(Integer.parseInt(properties.getProperty("interval-" + i)));
				mon.setKeepLast(Integer.parseInt(properties.getProperty("keepLast-" + i)));

				if (properties.get("errorPatterns-" + i) != null) {
					mon.errorPatterns.clear();
					String patterns = properties.getProperty("errorPatterns-" + i);
					extractPatterns(mon.errorPatterns, patterns);
				}

				if (properties.get("successPatterns-" + i) != null) {
					mon.successPatterns.clear();
					String patterns = properties.getProperty("successPatterns-" + i);
					extractPatterns(mon.successPatterns, patterns);
				}

			

				monitors.add(mon);

				i++;
			}

		}

		return monitors;
	}

	private static void extractPatterns(List<String> listPatterns, String patterns) {
		String[] apatterns = patterns.split(",");
		for (String pattern : apatterns) {
			listPatterns.add(pattern);
		}
	}

	public static void main(String[] args) throws InterruptedException, IOException {

		List<Monitor> monitors = Monitor.load();
		for (Monitor mon : monitors) {
			System.out.println(mon);
			mon.start();
		}
		// Just wait a few to printout something
		Thread.currentThread().join(15 * 1000);

		while (true) {

			for (Monitor mon : monitors) {

				System.out.println("####### " + mon.getTitle() + "##########");
				System.out.println("logfile :" + mon.getLogFile());
				System.out.println("rowcount of " + mon.getTable() + ": " + mon.getTableCount());
				System.out.println("errors :" + mon.getErrorResults());
				System.out.println("success :" + mon.getSuccessResults());
				Thread.currentThread().join(mon.getInterval() * 1000);

			}

		}
	}

	@Override
	public String toString() {
		return "Monitor [logFile=" + logFile + ", title=" + title + ", table=" + table + ", tableCount=" + tableCount + ", interval=" + interval
				+ ", keepLast=" + keepLast + "]";
	}

}
