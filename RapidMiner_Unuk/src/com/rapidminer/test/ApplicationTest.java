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

import com.rapidminer.Process;
import com.rapidminer.RapidMiner;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.tools.LogService;


/**
 * Extends the JUnit test case by a method for checking the creation and
 * runnning of an process from an external application.
 * 
 * @author Ingo Mierswa
 */
public class ApplicationTest extends RapidMinerTestCase {

	@Override
	public String getName() {
		return "Application test";
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		LogService.getGlobal().setVerbosityLevel(LogService.OFF);
		RapidMiner.init();
		LogService.getGlobal().setVerbosityLevel(LogService.OFF);
	}

	public void testProcessCreation() throws Exception {
		Process exp = ProcessCreator.createProcess();
		LogService.getGlobal().setVerbosityLevel(LogService.OFF);
		exp.run(new IOContainer(), LogService.OFF);
	}
}
