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
package com.rapidminer.operator.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.DummyPortPairExtender;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.ParameterTypeText;
import com.rapidminer.parameter.TextType;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;
import com.rapidminer.tools.MailUtilities;

/**
 * 
 * @author Simon Fischer
 *
 */
public class SendMailOperator extends Operator {

	private DummyPortPairExtender through = new DummyPortPairExtender("through", getInputPorts(), getOutputPorts());
	
	public static final String PARAMETER_TO = "to";
	public static final String PARAMETER_SUBJECT = "subject";
	public static final String PARAMETER_BODY_PLAIN = "body_plain";
	public static final String PARAMETER_BODY_HTML = "body_html";
	public static final String PARAMETER_USE_HTML = "use_html";
	
	public static final String PARAMETER_HEADERS = "headers";
	
	
	
	public SendMailOperator(OperatorDescription description) {
		super(description);
		through.start();
		getTransformer().addRule(through.makePassThroughRule());
	}

	@Override
	public void doWork() throws OperatorException {
		String to = getParameterAsString(PARAMETER_TO);
		String subject = getParameterAsString(PARAMETER_SUBJECT);
		
		Map<String,String> headers = new HashMap<String,String>();
		for (String[] entry : getParameterList(PARAMETER_HEADERS)) {
			headers.put(entry[0], entry[1]);
		}		 
		String body;
		if (getParameterAsBoolean(PARAMETER_USE_HTML)) {
			body = getParameterAsString(PARAMETER_BODY_HTML);
			headers.put("Content-Type", "text/html");
		} else {
			body = getParameterAsString(PARAMETER_BODY_PLAIN);			
		}				
		MailUtilities.sendEmail(to, subject, body, headers);
		through.passDataThrough();
	}
	
	@Override
	public List<ParameterType> getParameterTypes() {
		final List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_TO, "Receiver of the email.", false, false));
		types.add(new ParameterTypeString(PARAMETER_SUBJECT, "Subject the email.", false, false));
		
		types.add(new ParameterTypeBoolean(PARAMETER_USE_HTML, "Format text as HTML?.", false, false));

		ParameterType type = new ParameterTypeText(PARAMETER_BODY_PLAIN, "Body of the email.", TextType.PLAIN, false);
		type.registerDependencyCondition(new BooleanParameterCondition(this, PARAMETER_USE_HTML, false, false));			
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeText(PARAMETER_BODY_HTML, "Body of the email in HTML format.", TextType.HTML, 
				"<html>\n" +
				"	<head>\n" +
				"		<title>RapidMiner Mail Message</title>\n" +
				"	</head>\n" +
				"	<body>\n" +
				"		<p>\n" +
				"		</p>\n" +
				"	</body>\n" +
				"</html>\n");
		type.registerDependencyCondition(new BooleanParameterCondition(this, PARAMETER_USE_HTML, false, true));
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeList(PARAMETER_HEADERS, "Additional mail headers", 
				new ParameterTypeString("header", "Name of the header"), 
				new ParameterTypeString("value", "value of the header"));
		type.setExpert(true);
		types.add(type);
		return types;
	}
}
