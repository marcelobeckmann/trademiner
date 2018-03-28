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
package com.rapidminer.test;

import java.io.File;
import com.rapidminer.Process;
import com.rapidminer.RapidMiner;
import com.rapidminer.operator.IOConsumeOperator;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.ParameterService;

/**
 * Tests the output-data of the operators IOConsumer and IOMultiplier
 * 
 * @author Christian Lohmann
 */
public class IOConsumingDataSampleTest extends OperatorDataSampleTest {

	private static final int DELETE_ONE = 0;
	private static final int DELETE_ALL = 1;
	private int copies = 0;
	private Operator op = null;
	private Process process = null;
	private IOObject obj = null;

	public IOConsumingDataSampleTest(String file, int copies) {
		super(file);
		this.copies = copies;
	}
	
	@Override
	public void checkOutput(IOContainer output) throws MissingIOObjectException {
		try {
			if(process.getOperator("IOConsumer") == null) {
				obj = output.getElementAt(0);
				for(int i = copies; i >= 0; i--) {
					assertEquals(obj.getClass(), output.getElementAt(i).getClass());
					output.remove(obj.getClass(), i);
				}
				assertEquals(0, output.size());
			}
			else {
				op = process.getOperator("IOConsumer");
				switch(op.getParameterAsInt(IOConsumeOperator.PARAMETER_DELETION_TYPE)) { 
					case DELETE_ONE:
						assertEquals(copies, output.size());
						break;
					case DELETE_ALL:
						assertEquals(0, output.size());
						break;
				}
			}
		}
		catch(UndefinedParameterError e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void sampleTest() throws Exception {
		File processFile = new File(ParameterService.getSourceRoot(), "test" + File.separator + file);
		if (!processFile.exists())
			throw new Exception("File '" + processFile.getAbsolutePath() + "' does not exist!");
		LogService.getGlobal().setVerbosityLevel(LogService.OFF);
		process = RapidMiner.readProcessFile(processFile);
		op = process.getOperator("IOMultiplier");
		if(copies > 1) {
			op.setParameter("number_of_copies", Integer.toString(copies));
		}		
		IOContainer output = process.run(new IOContainer(), LogService.OFF);
		checkOutput(output);
	}
}
