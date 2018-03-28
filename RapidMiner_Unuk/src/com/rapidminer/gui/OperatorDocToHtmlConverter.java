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
package com.rapidminer.gui;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.Precondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.Parameters;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.documentation.ExampleProcess;

/**
 * 
 * The OperatorDocToHtmlConverter-Class is made for handling a request for a documantation accompanying an operator given.
 * This is done by using the method convert().
 * 
 * @author Philipp Kersting
 *
 */
public class OperatorDocToHtmlConverter {


    //private static final String HOST_NAME = "http://www.rapid-i.com";
	/**
	 * 
	 * The convert()-method tries to convert the given xml-File into an Html-File using the given xslt-stylesheet.
	 * If this fails (most commonly because the given xml-file doesn't exist), it'll pass the creation of an Html-Document over to the method makeDocumentation(), which will generate a String from the local ressources.
	 * 
	 * @param file
	 * @param stylesheet
	 * @param operator
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static String convert(String file, String stylesheet, Operator operator)
			throws MalformedURLException, IOException {
		StringWriter buffer = new StringWriter();

		File xmlFile = new File(file);

		Source xmlSource = new StreamSource(xmlFile);
		Source xsltSource = new StreamSource(
				OperatorDocToHtmlConverter.class
						.getResourceAsStream(stylesheet));

		TransformerFactory transFact = TransformerFactory.newInstance();
		String html = "";
		try {
			Transformer trans = transFact.newTransformer(xsltSource);
			trans.transform(xmlSource, new StreamResult(buffer));
			html = buffer.toString();
		} catch (TransformerException e) {
//				
//				
//				try {
//
//					URL url = new URL (WIKI_PREFIX_FOR_OPERATORS);
//					url.openConnection().connect();
//					html = retrieveAndMakeDocumentation (operator);
//				} catch (SAXException e1) {
//					html = makeDocumentation(operator);
//					e1.printStackTrace();
//				} catch (ParserConfigurationException e1) {
//					html = makeDocumentation(operator);
//				} catch (TransformerException e1) {
//					html = makeDocumentation(operator);
//				}catch (IOException e1) {
//					html = makeDocumentation(operator);
//				}
					
					html = makeDocumentation(operator);
				
			
		
		}
		
		return html;
	}

/**
 * 
 * Makes a documentation in Html of the ressources. 
 * It's called if it was impossible to make a help page of a xml-file.
 * The page that is generated here would probably lack a tutorial process, but if it would, it would also be supported.
 * 
 * @param displayedOperator
 * @return HtmlString, that contains the helppage.
 */
	@SuppressWarnings("unchecked")
	private static String makeDocumentation (Operator displayedOperator){

        OperatorDescription descr = displayedOperator.getOperatorDescription();
        StringBuilder buf = new StringBuilder("<html><head></head><body>");
        String iconName = "icons/24/" + displayedOperator.getOperatorDescription().getIconName();
        URL resource = Tools.getResource(iconName);
    	buf.append("<table> <tr> <td>	");
        if (resource != null) {
            buf.append("<img src=\"" + resource + "\"/> ");
        }else{
        	buf.append("<img src\""+SwingTools.getIconPath("48/information2.png")+"\" class=\"HeadIcon\"/>");
        }
    	buf.append("</td><td>");
        buf.append ("<h2>" + displayedOperator.getOperatorDescription().getName()+"</h2></td></tr></table><hr />");
        buf.append("<h3>Synopsis</h3>");
        buf.append("<p>"+descr.getShortDescription() + "</p>");
        buf.append("<h3>Description</h3>");
		buf.append("<p>"+descr.getLongDescriptionHTML()+"</p>");
		buf.append("<h3>Input</h3>");
		buf.append("<table cellspacing=7>");
		for (InputPort port: displayedOperator.getInputPorts().getAllPorts()){
			buf.append("<tr>");
			buf.append("<td>");
			buf.append("<table>");
			buf.append("<tr>");
			buf.append("<td class=\"lilIcon\">");
			//print port.getMetaData().toString()
			Class <?extends IOObject> typeClass = null;
			List<Precondition> preconditions = new LinkedList(port.getAllPreconditions());
			for (Precondition precondition : preconditions){
				if (precondition.getDescription().contains("expects")){
					MetaData metaData = (precondition.getExpectedMetaData());
					System.out.println();
					typeClass = metaData.getObjectClass();
				}
			}
			String imgSrc = OperatorDocumentationBrowser.getIconNameForType(typeClass);
			System.out.println(imgSrc);
			buf.append("<img src=\""+ imgSrc  +"\" class=\"typeIcon\" />");
			buf.append("</td><td>");
			buf.append("<b>" + port.getName() + "</b>"); 
			if (typeClass!=null){ 
				buf.append("<i> ("+ port.getDescription() + ")</i>");
			}
			buf.append("</td");
			buf.append("</tr>");
			buf.append("</table>");
			buf.append("</td");
			buf.append("</tr>");
			
		}
		buf.append("</table>");
		
		buf.append("<h3>Output</h3>");
		buf.append("<table cellspacing=7>");
		for (OutputPort port: displayedOperator.getOutputPorts().getAllPorts()){
			buf.append("<tr>");
			buf.append("<td>");
			buf.append("<table>");
			buf.append("<tr>");
			buf.append("<td class=\"lilIcon\">");

			String imgSrc = SwingTools.getIconPath("24/plug.png");
			buf.append("<img src=\""+ imgSrc  +"\" class=\"typeIcon\" />");
			buf.append("</td><td>");
			buf.append("<b>" + port.getName() + "</b>"); 
			if (!port.getDescription().equals("")){ 
				buf.append("<i> ("+ port.getDescription() + ") </i>");
			}
			buf.append("</td>");
			buf.append("</tr>");
			buf.append("</table>");
			buf.append("</td>");
			buf.append("</tr>");
			
		}
		buf.append("</table>");
		
		Parameters parameters = displayedOperator.getParameters();
		
		Set<String> keys =  parameters.getKeys();
		
		if (keys.size()>0){
			buf.append("<h3>Parameters</h3>");
			buf.append("<table cellspacing=7>");
			for (String key : keys){
				ParameterType type = displayedOperator.getParameterType(key);
				buf.append("<tr>");
				buf.append("<td>");
				buf.append("<b>");
                buf.append("<dt>");
				buf.append(type.getKey().replace('_', ' '));
				buf.append("</dt>");
				buf.append("</b>");
				if (type.isExpert()){
					buf.append(" (Expert) ");
				}
				buf.append("</td>");
				buf.append("</tr>");
				buf.append("<tr>");
				buf.append("<td>");
				buf.append(type.getDescription());
				buf.append("<b> Range: </b>");
				String range = type.getRange();
				if (range.split("default:") [0]!=null){
					range = range.split("default:") [0];	
				}
				buf.append(range);
				if (type.getDefaultValue()!=null){
					buf.append("<b> Default: </b>");
					buf.append(type.getDefaultValueAsString());
				}
				buf.append("</td>");
				buf.append("</tr>");
				
			}
			buf.append("</table>");
		}
		if (displayedOperator.getOperatorDescription().getOperatorDocumentation().getExamples().size()>0)
		{
			if (displayedOperator.getOperatorDescription().getOperatorDocumentation().getExamples().size()==1){
				buf.append("<h3>Turorial Process</h3>");
				buf.append("<a href=\"l1\">ShowExampleProcess</a>");
				buf.append(displayedOperator.getOperatorDescription().getOperatorDocumentation().getExamples().get(0).getComment());
			}
			if (displayedOperator.getOperatorDescription().getOperatorDocumentation().getExamples().size()>1){
				buf.append("<h3>Turorial Processes</h3>");
				int i = 0;
				for (ExampleProcess example : displayedOperator.getOperatorDescription().getOperatorDocumentation().getExamples()){

					buf.append("<a href=\"l"+i+"\">ShowExampleProcess "+i+"</a>");
					buf.append(example.getComment());
				}
			}
		}
        buf.append("</body></html>");
		return buf.toString();
		}


	/*
	public static String retrieveAndMakeDocumentation(Operator operator) throws SAXException, IOException, ParserConfigurationException, TransformerException{
        String operatorWikiName = StringUtils.EMPTY;
        OperatorDescription descr = operator.getOperatorDescription();
        if (!descr.isDeprecated()) {
            operatorWikiName = descr.getName().replace(" ", "_");
            if (descr.getProvider() != null) {
                String prefix = descr.getProvider().getPrefix();
                prefix = Character.toUpperCase(prefix.charAt(0)) + prefix.substring(1);
                operatorWikiName = prefix + ":" + operatorWikiName;
            }
        }
        Document documentOperator = OperatorDocLoader.parseDocumentForOperator(operatorWikiName, descr);
        if (documentOperator != null) {
            // writing html back to string
        	NodeList liS = documentOperator.getElementsByTagName("li");
        	for (int q = 0 ; q < liS.getLength() ; q++ ){
        		NodeList subList = liS.item(q).getChildNodes();
            	for (int p = 0 ; p < subList.getLength() ; p++ ){
            		subList.item(p);
            		if (subList.item(p).getTextContent().equals(": ")||subList.item(p).getTextContent().equals(" : ")||subList.item(p).getTextContent().equals(" :")||subList.item(p).getTextContent().equals(":")||subList.item(p).getTextContent().equals(" :")||subList.item(p).getTextContent().equals(" :")||subList.item(p).getTextContent().equals("\n:")||subList.item(p).getTextContent().equals(":\n")){
            			subList.item(p).getParentNode().removeChild(subList.item(p));
					}
            	}
        	}
//        	Node newChild = documentOperator.createElement("table");
//        	Node newtr = documentOperator.createElement("tr");
////        	Node newtd = documentOperator.createElement("td");
//        	newChild.appendChild(newtd)
        	//Node newChild = documentOperator.createElement("<table>"<tr><td><img src=\""+SwingTools.getIconPath("24/"+descr.getIconName()) +"/></td><td><h2>"+ descr.getName() +"</h2></td></tr></table>");
        	
        	Node newTable = documentOperator.createElement("table");
        	Node newChild = documentOperator.createElement("div");
        	Node newTr = documentOperator.createElement("tr");
        	Node imgTd = documentOperator.createElement("td");
        	Node h2Td = documentOperator.createElement("td");
        	Node newImg = documentOperator.createElement("img");
        	Node srcNode = documentOperator.createAttribute("src");
        	Node classNode = documentOperator.createAttribute("class");
        	Node classNode2 = documentOperator.createAttribute("class");
        	Node newHeading = documentOperator.createElement("h2");
        	Node newHr = documentOperator.createElement("hr");
        	
        	srcNode.setTextContent(SwingTools.getIconPath("24/"+descr.getIconName()));
        	classNode.setTextContent("HeadIcon");
        	classNode.setTextContent("Heading");
        	newHeading.setTextContent(descr.getName());
        	
        	newImg.getAttributes().setNamedItem(srcNode);
        	newImg.getAttributes().setNamedItem(classNode);
        	newHeading.getAttributes().setNamedItem(classNode2);
        	imgTd.appendChild(newImg);
        	newTr.appendChild(imgTd);
        	h2Td.appendChild(newHeading);
        	newTr.appendChild(h2Td);
        	newTable.appendChild(newTr);
        	newChild.appendChild(newTable);
        	newChild.appendChild(newHr);
        	
        	documentOperator.getElementsByTagName("h2").item(0).getParentNode().insertBefore(newChild,documentOperator.getElementsByTagName("h2").item(0));
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            // initialize StreamResult with File object to save to file
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(documentOperator);
            transformer.transform(source, result);

            String HTMLString = result.getWriter().toString();
            HTMLString = HTMLString.replace("<h2>", "<h3>");
            HTMLString = HTMLString.replace("<ul>", "<table cellspacing=7>");
            HTMLString = HTMLString.replace("<li>", "<tr><td>");
            HTMLString = HTMLString.replace("</b>", "</td></tr><tr><td>");
            HTMLString = HTMLString.replace("<i>expects:</i>", "<b>expects:</b>");
            HTMLString = HTMLString.replace("<i> expects: </i>", "<b>expects:</b>");
            HTMLString = HTMLString.replace("<i>expects: </i>", "<b>expects:</b>");
            HTMLString = HTMLString.replace("<i> expects:</i>", "<b>expects:</b>");
            
            HTMLString = HTMLString.replace("?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"," ");
            HTMLString = HTMLString.replace("</li>", "</td></tr>");
            HTMLString = HTMLString.replace("</ul>", "</table>");
            HTMLString = HTMLString.replace("Example Process", " ");
            HTMLString = HTMLString.replace("[[Category:]]","");
            return HTMLString;
        }
		return operatorWikiName;
	}
	*/
}
