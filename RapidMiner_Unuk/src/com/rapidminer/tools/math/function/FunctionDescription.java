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
package com.rapidminer.tools.math.function;

/**
 * 
 * @author Ingo Mierswa
 *
 */
public class FunctionDescription {

	public static final int UNLIMITED_NUMBER_OF_ARGUMENTS = -1;
	
	private String function;
	
	private String functionName;
	
	private String functionDescription;
	
	private int numberOfArguments;
	
	public FunctionDescription(String function, String name, String description, int numberOfArguments) {
		this.function = function;
		this.functionName = name;
		this.functionDescription = description;
		this.numberOfArguments = numberOfArguments;
	}
	
	public String getFunction() {
		return this.function;
	}
	
	public String getName() {
		return this.functionName;
	}
	
	public String getDescription() {
		return this.functionDescription;
	}
	
	public int getNumberOfArguments() {
		return this.numberOfArguments;
	}
}
