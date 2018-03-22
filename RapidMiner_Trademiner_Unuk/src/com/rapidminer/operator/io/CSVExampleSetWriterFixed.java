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
package com.rapidminer.operator.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.DateFormat;
import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.io.Encoding;

/**
 * <p>
 * This operator can be used to write data into CSV files (Comma Separated
 * Values). The values and columns are separated by &quot;;&quot;. Missing data
 * values are indicated by empty cells.
 * </p>
 * 
 * @author Ingo Mierswa
 */
public class CSVExampleSetWriterFixed extends CSVExampleSetWriter {

	public CSVExampleSetWriterFixed(OperatorDescription description) {
		super(description);
	}

	/**
	 This is to fix the bug of putting headers in the middle of file when appending to them.
	 * @throws UndefinedParameterError 
	 * @throws IOException 
	 **/
	public boolean writeAttributeNames(java.io.OutputStream outputStream) throws UndefinedParameterError 
	{
		try {
			outputStream.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			//this will propagate the error
			return true;
		}
		
		File file = new File(getParameterAsString(PARAMETER_CSV_FILE));
		
		if (!file.exists())
		{
			return true;
		}
		
		if (file.length()==0) {
			return true;
		}
		
		return false;
	}
	
	
	public void writeStream(ExampleSet exampleSet,
			java.io.OutputStream outputStream) throws OperatorException {
		String columnSeparator = getParameterAsString(PARAMETER_COLUMN_SEPARATOR);
		boolean quoteNominalValues = getParameterAsBoolean(PARAMETER_QUOTE_NOMINAL_VALUES);
		PrintWriter out = null;
		try {
			out = new PrintWriter(new OutputStreamWriter(outputStream, Encoding
					.getEncoding(this)));

			
			
			
			// write column names
			if (getParameterAsBoolean(PARAMETER_WRITE_ATTRIBUTE_NAMES)  && writeAttributeNames(outputStream)) {
				Iterator<Attribute> a = exampleSet.getAttributes()
						.allAttributes();
				boolean first = true;
				while (a.hasNext()) {
					if (!first)
						out.print(columnSeparator);
					Attribute attribute = a.next();
					String name = attribute.getName();
					if (quoteNominalValues) {
						name = name.replaceAll("\"", "'");
						name = "\"" + name + "\"";
					}
					out.print(name);
					first = false;
				}
				out.println();
			}

			// write data
			for (Example example : exampleSet) {
				Iterator<Attribute> a = exampleSet.getAttributes()
						.allAttributes();
				boolean first = true;
				while (a.hasNext()) {
					Attribute attribute = a.next();
					if (!first)
						out.print(columnSeparator);
					if (!Double.isNaN(example.getValue(attribute))) {
						if (attribute.isNominal()) {
							String stringValue = example
									.getValueAsString(attribute);
							if (quoteNominalValues) {
								stringValue = stringValue.replaceAll("\"", "'");
								stringValue = "\"" + stringValue + "\"";
							}
							out.print(stringValue);
						} else {
							Double value = example.getValue(attribute);
							if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute
									.getValueType(), Ontology.DATE_TIME)) {
								if (getParameterAsBoolean(PARAMETER_FORMAT_DATE)) {
									Date date = new Date(value.longValue());
									String s = DateFormat.getInstance().format(
											date);
									out.print(s);
								} else {
									out.print(value);
								}
							} else {
								out.print(value);
							}

						}
					}
					first = false;
				}
				out.println();
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
}
