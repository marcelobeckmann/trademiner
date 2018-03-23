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
package com.rapidminer.test.samples;

import java.util.Collection;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;

/**
 * Performs the sample process and checks if the given IOObjects are part of
 * the output.
 * 
 * @author Ingo Mierswa
 *          Exp $
 */
public class IOObjectSampleTest extends SampleTest {

	private Collection<Class<IOObject>> ioObjects;

	public IOObjectSampleTest(String file, Collection<Class<IOObject>> ioObjects) {
		super(file);
		this.ioObjects = ioObjects;
	}

	@Override
	public void checkOutput(IOContainer output) throws MissingIOObjectException {
		for (Class<IOObject> o : ioObjects)
			output.get(o);
	}
}
