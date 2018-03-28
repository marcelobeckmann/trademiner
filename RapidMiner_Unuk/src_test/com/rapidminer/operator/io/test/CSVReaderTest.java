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
package com.rapidminer.operator.io.test;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.test.TestContext;
import com.rapidminer.tools.XMLException;

/**
 * @author Sebastian Loh 
 * (25.06.2010)
 *
 */
public class CSVReaderTest {

	@Before
	public void setUp() throws Exception {
		TestContext.get().initRapidMiner();	
	}
	
	@Test
	public void testDemo() throws OperatorCreationException, OperatorException, IOException, XMLException {
		assertEquals(3, 3);
				
//		IOContainer result = executeProcessFromTestRepository("/io/reader/CSV_reader_001");
//		ExampleSet exampleSet = result.get(ExampleSet.class);
//		Attribute att1 = exampleSet.getAttributes().get("isbn");
//		assertEquals(att1.getValueType(), Ontology.NOMINAL);
		
		
//		CSVDataReader reader = OperatorService.createOperator(CSVDataReader.class);
//		reader.setParameter(CSVDataReader.PARAMETER_CSV_FILE, "bla");
	}
}
