package com.rapidminer.operator.trademiner.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class Executor
{

	private boolean finished =false;
	public void executeProcess(String rmp) throws Exception
	{
		try {
			String command = getLaunchCommand(rmp);
//			System.out.println("######################################" + command);
			java.lang.Process process = Runtime.getRuntime().exec(command);
			
			final BufferedReader stdout = getInputStream(process);
			final BufferedReader stderr = getErrorStream(process);
			
			startOutThread(stdout);
			startOutThread(stderr);
			process.waitFor();
			
			
		}catch (Exception e)
		{
			throw new Exception(e);
			
		}
		finally {
			
			finished =true;
		}
		
	}
	
	private void startOutThread(final BufferedReader stdout) {
		
		//Remark about PMD rule: Launch this Thread is acceptable as this launch is a Java SE not a JEE application
		final Thread stdoutThread = new Thread() {
			@Override
			public void run() {
				try {
					printLineToLogger(stdout);
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			};
			stdoutThread.start();
		
	}


	protected BufferedReader getInputStream(final java.lang.Process process) {
		final InputStream inputStream = process.getInputStream();
		final BufferedReader stdout = new BufferedReader(new InputStreamReader(inputStream));
		return stdout;
	}

	
	protected BufferedReader getErrorStream(final java.lang.Process process) {
		final InputStream errorStream = process.getErrorStream();
		final BufferedReader stderr = new BufferedReader(new InputStreamReader(errorStream));
		return stderr;
	}
	private String getLaunchCommand(String rmp)  {
		
		final String DQ="\"";
		
		final String RAPIDMINER_HOME="c:/Program Files/Rapid-I/RapidMiner5";
		final String SCRIPT = "/scripts/rapidminer.bat" ; 
		final String COMMAND =  DQ+RAPIDMINER_HOME + SCRIPT +DQ+   		
		"  " +rmp;
		//-f PROCESS
		//+ "/home/nelson/rm/REPOSITORY/0_run_all.rmp";
		
		return COMMAND;
	
	}
	

	protected void printLineToLogger(final BufferedReader reader) throws IOException {
		final String mName = "printLineToLogger";
		
		String line;
		while ((line = reader.readLine()) != null && !finished) {
			if (line.length() > 0) {
				System.out.println(line);
			}
		}
		reader.close();
		
	}
	
	
}