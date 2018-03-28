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
package com.rapidminer.tools;

import java.util.Map;

import javax.xml.ws.BindingProvider;

/**
 * 
 * @author Simon Fischer
 *
 */
public class WebServiceTools {

	private static final int TIMEOUT = 4000;

	public static void setTimeout(BindingProvider port) {
		setTimeout(port, TIMEOUT);
	}
	
	/** Sets the timeout for this web service client. Every port created
	 *  by a JAX-WS can be cast to BindingProvider. */
	public static void setTimeout(BindingProvider port, int timeout) {
		Map<String, Object> ctxt = (port).getRequestContext();
		ctxt.put("com.sun.xml.ws.developer.JAXWSProperties.CONNECT_TIMEOUT", timeout);
		ctxt.put("com.sun.xml.ws.connect.timeout", timeout);
		ctxt.put("com.sun.xml.ws.internal.connect.timeout", timeout);
		ctxt.put("com.sun.xml.ws.request.timeout", timeout); 
		ctxt.put("com.sun.xml.internal.ws.request.timeout", timeout);
		// We don't want to use proprietary Sun code
//		ctxt.put(BindingProviderProperties.REQUEST_TIMEOUT, timeout);
//		ctxt.put(BindingProviderProperties.CONNECT_TIMEOUT, timeout);
	}
}
