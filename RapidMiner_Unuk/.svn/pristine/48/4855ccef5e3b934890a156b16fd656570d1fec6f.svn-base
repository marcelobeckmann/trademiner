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
package com.rapidminer.operator.nio.file;

import java.io.File;
import java.io.InputStream;

import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ResultObjectAdapter;

/**
 * 
 * This class represents buffers, files or streams that can be parsed by Operators.
 * 
 * @author Nils Woehler
 *
 */
public abstract class FileObject extends ResultObjectAdapter {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Open Stream to read data in this Object.
	 * @throws OperatorException 
	 * 
	 */
	public abstract InputStream openStream() throws OperatorException;
	
	/**
	 * Returns the data as a file. Maybe slow if underlaying implementation needs to copy the data into the file first.
	 * 
	 */
	public abstract File getFile() throws OperatorException ;

	@Override
	public String getName() {
		return "File";
	}
}
