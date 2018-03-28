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
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.DateParser;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.io.Encoding;

/**
 * <p>This operator can be used to write data into Microsoft Excel spreadsheets. 
 * This operator creates Excel files readable by Excel 95, 97, 2000, XP, 2003 
 * and newer. Missing data values are indicated by empty cells.</p>
 *
 * @author Ingo Mierswa
 */
public class ExcelExampleSetWriter extends AbstractStreamWriter {

	/** The parameter name for &quot;The Excel spreadsheet file which should be written.&quot; */
	public static final String PARAMETER_EXCEL_FILE = "excel_file";
	
	public ExcelExampleSetWriter(OperatorDescription description) {
		super(description);
	}
	
	public static void write(ExampleSet exampleSet, Charset encoding, OutputStream out) throws IOException, WriteException {
		try {
			WorkbookSettings ws = new WorkbookSettings();		
			ws.setEncoding(encoding.name());
			ws.setLocale(Locale.US);

			WritableWorkbook workbook = Workbook.createWorkbook(out, ws);
			WritableSheet s = workbook.createSheet("RapidMiner Data", 0);
			writeDataSheet(s, exampleSet);
			workbook.write();
			workbook.close();
		} finally {
			try {
				out.close();
			} catch (Exception e) {
				// silent. exception will trigger warning anyway
			}
		}
	}
	
	private static void writeDataSheet(WritableSheet s, ExampleSet exampleSet) throws WriteException {

		// Format the Font
		WritableFont wf = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
		WritableCellFormat cf = new WritableCellFormat(wf);
		
		Iterator<Attribute> a = exampleSet.getAttributes().allAttributes();
		int counter = 0;
		while (a.hasNext()) {
			Attribute attribute = a.next();
			s.addCell(new Label(counter++, 0, attribute.getName(), cf));
		}
		
		NumberFormat nf = new NumberFormat("#.0");
	    WritableCellFormat nfCell = new WritableCellFormat(nf);
		WritableFont wf2 = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);
		WritableCellFormat cf2 = new WritableCellFormat(wf2);
		
		DateFormat df = new DateFormat(DateParser.DEFAULT_DATE_TIME_FORMAT);

		WritableCellFormat dfCell = new WritableCellFormat(df);
		int rowCounter = 1;
		for (Example example : exampleSet) {
			a = exampleSet.getAttributes().allAttributes();
			int columnCounter = 0;
			while (a.hasNext()) {
				Attribute attribute = a.next();
				if (!Double.isNaN(example.getValue(attribute))) {
					if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), Ontology.NOMINAL)) {
						s.addCell(new Label(columnCounter, rowCounter, replaceForbiddenChars(example.getValueAsString(attribute)), cf2));
					} else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), Ontology.DATE_TIME)) {
						DateTime dateTime = new DateTime(columnCounter, rowCounter, new Date((long) example.getValue(attribute)), dfCell);
						s.addCell(dateTime);
					} else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), Ontology.NUMERICAL)) {
					    Number number = new Number(columnCounter, rowCounter, example.getValue(attribute), nfCell);
					    s.addCell(number);
					} else {
						// default: write as a String
						s.addCell(new Label(columnCounter, rowCounter, replaceForbiddenChars(example.getValueAsString(attribute)), cf2));
					}
				}
				columnCounter++;
			}
			rowCounter++;
		}
	}
	
	private static String replaceForbiddenChars(String originalValue) {
		return originalValue.replace((char) 0, ' ');
	}	
	
	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(makeFileParameterType());
		types.addAll(Encoding.getParameterTypes(this));
		return types;
	}

	@Override
	String getFileExtension() {
		return "xls";
	}

	@Override
	String getFileParameterName() {
		return PARAMETER_EXCEL_FILE;
	}

	@Override
	void writeStream(ExampleSet exampleSet, OutputStream outputStream)
			throws OperatorException {
		File file = getParameterAsFile(PARAMETER_EXCEL_FILE, true);
		
		WorkbookSettings ws = new WorkbookSettings();
		Charset encoding = Encoding.getEncoding(this);
		ws.setEncoding(encoding.name());
		ws.setLocale(Locale.US);

		try {
			write(exampleSet, encoding, outputStream);			
		} catch (Exception e) {
			throw new UserError(this, 303, file.getName(), e.getMessage());
		}
	}
}
