/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2012 by Rapid-I and the contributors
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
package com.rapidminer.operator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.rapidminer.operator.ports.DummyPortPairExtender;
import com.rapidminer.operator.ports.PortPairExtender;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Tools;


/**
 * <p>This operator executes a system command. The command and all its arguments
 * are specified by the parameter <code>command</code>. The standard output
 * stream and the error stream of the process can be redirected to the logfile. <br/>
 * Please note also that the command is system dependent. Characters that have
 * special meaning on the shell like e.g. the pipe symbol or brackets and braces
 * do not have a special meaning to Java. <br/> The method
 * <code>Runtime.exec(String)</code> is used to execute the command. Please
 * note, that this (Java) method parses the string into tokens before it is
 * executed. These tokens are <em>not</em> interpreted by a shell (which?). If
 * the desired command involves piping, redirection or other shell features, it
 * is best to create a small shell script to handle this.</p>
 * 
 * <p>
 * A hint for Windows / MS DOS users: simple commands should be preceeded by 
 * <code>cmd call</code> which opens a new shell, executes the command and closes
 * the shell again. After this, the rest of the process will be executed.
 * Another option would be to preceed the command with <code>cmd start</code>
 * which opens the shell and keeps it open. The rest process will not be  
 * executed until the shell is closed by the user.
 * </p>
 * 
 * 
 * @author Ingo Mierswa, Simon Fischer
 */
public class CommandLineOperator extends Operator {

	public static final String PARAMETER_COMMAND = "command";

	public static final String PARAMETER_LOG_STDOUT = "log_stdout";

	public static final String PARAMETER_LOG_STDERR = "log_stderr";

	private PortPairExtender dummyPorts = new DummyPortPairExtender("through", getInputPorts(), getOutputPorts());

	public CommandLineOperator(OperatorDescription description) {
		super(description);

		dummyPorts.start();

		getTransformer().addRule(dummyPorts.makePassThroughRule());
	}

	@Override
	public void doWork() throws OperatorException {
		String command = getParameterAsString(PARAMETER_COMMAND);
		boolean logOut = getParameterAsBoolean(PARAMETER_LOG_STDOUT);
		boolean logErr = getParameterAsBoolean(PARAMETER_LOG_STDERR);

		try {
			Process process = Runtime.getRuntime().exec(command);
			if (logErr)
				logOutput("stderr:", process.getErrorStream());
			if (logOut)
				logOutput("stdout:", process.getInputStream());
			Tools.waitForProcess(this, process, command);
			getLogger().info("Program exited succesfully.");
		} catch (IOException e) {
			throw new UserError(this, e, 310, new Object[] { command, e.getMessage() });
		}

		dummyPorts.passDataThrough();
	}

	/** Sends the output to the LogService. */
	private void logOutput(String message, InputStream in) throws IOException {
		BufferedReader bin = new BufferedReader(new InputStreamReader(in));
		String line = null;
		StringBuffer buffer = new StringBuffer(message);
		while ((line = bin.readLine()) != null) {
			buffer.append(Tools.getLineSeparator());
			buffer.append(line);
		}
		logNote(buffer.toString());
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_COMMAND, "Command to execute.", false, false));
		types.add(new ParameterTypeBoolean(PARAMETER_LOG_STDOUT, "If set to true, the stdout stream of the command is redirected to the logfile.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_LOG_STDERR, "If set to true, the stderr stream of the command is redirected to the logfile.", true));
		return types;
	}

}
