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

import com.rapidminer.tools.LogService;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Extends the JUnit test case by a method for asserting equality of doubles
 * with respect to Double.NaN
 * 
 * @author Simon Fischer
 */
public class RapidMinerTestCase extends TestCase {

	public RapidMinerTestCase() {
		super();
	}

	public RapidMinerTestCase(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		LogService.getGlobal().setVerbosityLevel(LogService.WARNING);
	}

	public void assertEqualsNaN(String message, double expected, double actual) {
		if (Double.isNaN(expected)) {
			if (!Double.isNaN(actual)) {
				throw new AssertionFailedError(message + " expected: <" + expected + "> but was: <" + actual + ">");
			}
		} else {
			assertEquals(message, expected, actual, 0.000000001);
		}
	}

}
