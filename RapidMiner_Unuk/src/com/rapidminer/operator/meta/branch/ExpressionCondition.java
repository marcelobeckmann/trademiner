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
package com.rapidminer.operator.meta.branch;

import org.nfunk.jep.JEP;

import com.rapidminer.generator.GenerationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.function.ExpressionParser;

/**
 * This condition will parse the condition value as an expression and return
 * it's boolean value if possible. Otherwise an error will be thrown.
 * 
 * @author Sebastian Land
 */
public class ExpressionCondition implements ProcessBranchCondition {

	private ExpressionParser expressionParser = new ExpressionParser(true);
	
	@Override
	public boolean check(ProcessBranch operator, String value) throws OperatorException {
		JEP parser = expressionParser.getParser();
		parser.parseExpression(value);
		//check for errors
		if (parser.hasError()) {
			throw new GenerationException(value + ": " + parser.getErrorInfo());
		}
		
		// create the new attribute from the delivered type 
		Object result = parser.getValueAsObject();
		
		if (result instanceof Double) {
			double resultValue = (Double) result;
			if (resultValue == 1d || resultValue == 0d){
				return resultValue == 1d;
			}
		}
		throw new GenerationException("Must return boolean value");
	}

}
