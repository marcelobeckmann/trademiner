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

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.nfunk.jep.JEP;
import org.nfunk.jep.SymbolTable;
import org.nfunk.jep.Variable;
import org.nfunk.jep.type.Complex;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.rapidminer.MacroHandler;
import com.rapidminer.Process;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.generator.GenerationException;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeName;
import com.rapidminer.tools.LoggingHandler;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.math.function.expressions.Average;
import com.rapidminer.tools.math.function.expressions.BitwiseAnd;
import com.rapidminer.tools.math.function.expressions.BitwiseNot;
import com.rapidminer.tools.math.function.expressions.BitwiseOr;
import com.rapidminer.tools.math.function.expressions.BitwiseXor;
import com.rapidminer.tools.math.function.expressions.Constant;
import com.rapidminer.tools.math.function.expressions.LogarithmDualis;
import com.rapidminer.tools.math.function.expressions.Maximum;
import com.rapidminer.tools.math.function.expressions.Minimum;
import com.rapidminer.tools.math.function.expressions.Missing;
import com.rapidminer.tools.math.function.expressions.ParameterValue;
import com.rapidminer.tools.math.function.expressions.Signum;
import com.rapidminer.tools.math.function.expressions.date.Date2String;
import com.rapidminer.tools.math.function.expressions.date.Date2StringCustom;
import com.rapidminer.tools.math.function.expressions.date.Date2StringWithLocale;
import com.rapidminer.tools.math.function.expressions.date.DateAdd;
import com.rapidminer.tools.math.function.expressions.date.DateAfter;
import com.rapidminer.tools.math.function.expressions.date.DateBefore;
import com.rapidminer.tools.math.function.expressions.date.DateCreate;
import com.rapidminer.tools.math.function.expressions.date.DateDiff;
import com.rapidminer.tools.math.function.expressions.date.DateGet;
import com.rapidminer.tools.math.function.expressions.date.DateParse;
import com.rapidminer.tools.math.function.expressions.date.DateParseCustom;
import com.rapidminer.tools.math.function.expressions.date.DateParseWithLocale;
import com.rapidminer.tools.math.function.expressions.date.DateSet;
import com.rapidminer.tools.math.function.expressions.number.Str;
import com.rapidminer.tools.math.function.expressions.text.CharAt;
import com.rapidminer.tools.math.function.expressions.text.Compare;
import com.rapidminer.tools.math.function.expressions.text.Concat;
import com.rapidminer.tools.math.function.expressions.text.Contains;
import com.rapidminer.tools.math.function.expressions.text.EndsWith;
import com.rapidminer.tools.math.function.expressions.text.Equals;
import com.rapidminer.tools.math.function.expressions.text.EscapeHTML;
import com.rapidminer.tools.math.function.expressions.text.Finds;
import com.rapidminer.tools.math.function.expressions.text.IndexOf;
import com.rapidminer.tools.math.function.expressions.text.Length;
import com.rapidminer.tools.math.function.expressions.text.LowerCase;
import com.rapidminer.tools.math.function.expressions.text.Matches;
import com.rapidminer.tools.math.function.expressions.text.ParseNumber;
import com.rapidminer.tools.math.function.expressions.text.Prefix;
import com.rapidminer.tools.math.function.expressions.text.Replace;
import com.rapidminer.tools.math.function.expressions.text.ReplaceRegex;
import com.rapidminer.tools.math.function.expressions.text.StartsWith;
import com.rapidminer.tools.math.function.expressions.text.Substring;
import com.rapidminer.tools.math.function.expressions.text.Suffix;
import com.rapidminer.tools.math.function.expressions.text.Trim;
import com.rapidminer.tools.math.function.expressions.text.UpperCase;

/**
 * <p>
 * This class can be used as expression parser in order to generate new
 * attributes. The parser constructs new attributes from the attributes of the
 * input example set.
 * </p>
 * 
 * <p>
 * The following <em>operators</em> are supported:
 * <ul>
 * <li>Addition: +</li>
 * <li>Subtraction: -</li>
 * <li>Multiplication: *</li>
 * <li>Division: /</li>
 * <li>Power: ^</li>
 * <li>Modulus: %</li>
 * <li>Less Than: &lt;</li>
 * <li>Greater Than: &gt;</li>
 * <li>Less or Equal: &lt;=</li>
 * <li>More or Equal: &gt;=</li>
 * <li>Equal: ==</li>
 * <li>Not Equal: !=</li>
 * <li>Boolean Not: !</li>
 * <li>Boolean And: &&</li>
 * <li>Boolean Or: ||</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The following <em>log and exponential functions</em> are supported:
 * <ul>
 * <li>Natural Logarithm: ln(x)</li>
 * <li>Logarithm Base 10: log(x)</li>
 * <li>Logarithm Dualis (Base 2): ld(x)</li>
 * <li>Exponential (e^x): exp(x)</li>
 * <li>Power: pow(x,y)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The following <em>trigonometric functions</em> are supported:
 * <ul>
 * <li>Sine: sin(x)</li>
 * <li>Cosine: cos(x)</li>
 * <li>Tangent: tan(x)</li>
 * <li>Arc Sine: asin(x)</li>
 * <li>Arc Cosine: acos(x)</li>
 * <li>Arc Tangent: atan(x)</li>
 * <li>Arc Tangent (with 2 parameters): atan2(x,y)</li>
 * <li>Hyperbolic Sine: sinh(x)</li>
 * <li>Hyperbolic Cosine: cosh(x)</li>
 * <li>Hyperbolic Tangent: tanh(x)</li>
 * <li>Inverse Hyperbolic Sine: asinh(x)</li></li>
 * <li>Inverse Hyperbolic Cosine: acosh(x)</li></li>
 * <li>Inverse Hyperbolic Tangent: atanh(x)</li></li>
 * </ul>
 * </p>
 * 
 * <p>
 * The following <em>statistical functions</em> are supported:
 * <ul>
 * <li>Round: round(x)</li>
 * <li>Round to p decimals: round(x,p)</li>
 * <li>Floor: floor(x)</li>
 * <li>Ceiling: ceil(x)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The following <em>aggregation functions</em> are supported:
 * <ul>
 * <li>Average: avg(x,y,z...)</li>
 * <li>Minimum: min(x,y,z...)</li>
 * <li>Maximum: max(x,y,z...)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The following <em>text functions</em> are supported:
 * <ul>
 * <li>Number to string: str(x)</li>
 * <li>String to number: parse(text)</li>
 * <li>Substring: cut(text, start, length)</li>
 * <li>Concatenation (also possible by &quot+&quot;): concat(text1, text2,
 * text3...)</li>
 * <li>Replace: replace(text, what, by)</li>
 * <li>Replace All: replaceAll(text, what, by)</li>
 * <li>To lower case: lower(text)</li>
 * <li>To upper case: upper(text)</li>
 * <li>First position of string in text: index(text, string)</li>
 * <li>Length: length(text)</li>
 * <li>Character at position pos in text: char(text, pos)</li>
 * <li>Compare: compare(text1, text2)</li>
 * <li>Contains string in text: contains(text, string)</li>
 * <li>Equals: equals(text1, text2)</li>
 * <li>Starts with string: starts(text, string)</li>
 * <li>Ends with string: ends(text, string)</li>
 * <li>Matches with regular expression exp: matches(text, exp)</li>
 * <li>Suffix of length: suffix(text, length)</li>
 * <li>Prefix of length: prefix(text, length)</li>
 * <li>Trim (remove leading and trailing whitespace): trim(text)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The following <em>date functions</em> are supported:
 * <ul>
 * <li>Parse date: date_parse(x)</li>
 * <li>Parse date using locale: date_parse_loc(x, code)</li>
 * <li>Parse date using custom format: date_parse_custom(x, format, code)</li>
 * <li>Date before: date_before(x, y)</li>
 * <li>Date after: date_after(x, y)</li>
 * <li>Date to string: date_str(x)</li>
 * <li>Date to string using locale: date_str_loc(x, code)</li>
 * <li>Date to string with custom pattern: date_str_custom(x, pattern, code)</li>
 * <li>Current date: date_now()</li>
 * <li>Date difference: date_diff(x, y)</li>
 * <li>Date add: date_add(x, y, unit)</li>
 * <li>Date set: date_set(x, y, unit)</li>
 * <li>Date get: date_get(x, unit)</li>
 * </ul>
 * 
 * <p>
 * The following <em>process related functions</em> are supported:
 * <ul>
 * <li>Retrieving a parameter value: param("operator", "parameter")</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The following <em>miscellaneous functions</em> are supported:
 * <ul>
 * <li>If-Then-Else: if(cond,true-evaluation, false-evaluation)</li>
 * <li>Absolute: abs(x)</li>
 * <li>Constant: const(x)</li>
 * <li>Square Root: sqrt(x)</li>
 * <li>Signum (delivers the sign of a number): sgn(x)</li>
 * <li>Random Number (between 0 and 1): rand()</li>
 * <li>Modulus (x % y): mod(x,y)</li>
 * <li>Sum of k Numbers: sum(x,y,z...)</li>
 * <li>Binomial Coefficients: binom(n, i)</li>
 * <li>Check for Missing: missing(x)</li>
 * <li>Bitwise OR: bit_or(x, y)</li>
 * <li>Bitwise AND: bit_and(x, y)</li>
 * <li>Bitwise XOR: bit_xor(x, y)</li>
 * <li>Bitwise NOT: bit_not(x)</li>
 * </ul>
 * </p>
 * 
 * 
 * <p>
 * Beside those operators and functions, this operator also supports the
 * constants pi and e if this is indicated by the corresponding parameter
 * (default: true). You can also use strings in formulas (for example in a
 * conditioned if-formula) but the string values have to be enclosed in double
 * quotes.
 * </p>
 * 
 * <p>
 * Please note that there are some restrictions for the attribute names in order
 * to let this operator work properly:
 * <ul>
 * <li>If the standard constants are usable, attribute names with names like
 * &quot;e&quot; or &quot;pi&quot; are not allowed.</li>
 * <li>Attribute names with function or operator names are also not allowed.</li>
 * <li>Attribute names containing parentheses are not allowed.</li>
 * </ul>
 * If these conditions are not fulfilled, the names must be changed beforehand,
 * for example with the {@link ChangeAttributeName} operator.
 * </p>
 * 
 * <p>
 * <br/>
 * <em>Examples:</em><br/>
 * a1+sin(a2*a3)<br/>
 * if (att1>5, att2*att3, -abs(att1))<br/>
 * </p>
 * 
 * @author Ingo Mierswa
 */
public class ExpressionParser {

	private static final String[] FUNCTION_GROUPS = { "Basic Operators", "Log and Exponential", "Trigonometric", "Statistical", "Text", "Date", "Process", "Miscellaneous" };

	private static final Map<String, List<FunctionDescription>> FUNCTIONS = new HashMap<String, List<FunctionDescription>>();

	static {
		// basic operators
		List<FunctionDescription> operatorFunctions = new LinkedList<FunctionDescription>();
		operatorFunctions.add(new FunctionDescription("+", "Addition", "Calculates the addition of the two terms surrounding this operator; example: att1 + 7", 2));
		operatorFunctions.add(new FunctionDescription("-", "Subtraction", "Calculates the subtraction of the first term by the second one; example: 42 - att2", 2));
		operatorFunctions.add(new FunctionDescription("*", "Multiplication", "Calculates the multiplication of the two terms surrounding this operator; example: 5 * att3", 2));
		operatorFunctions.add(new FunctionDescription("/", "Division", "Calculates the division of the first term by the second one; example: 12 / 4", 2));
		operatorFunctions.add(new FunctionDescription("^", "Power", "Calculates the first term to the power of the second one; example: 2^3", 2));
		operatorFunctions.add(new FunctionDescription("%", "Modulus", "Calculates the modulus of the first term by the second one; example: 11 % 2", 2));
		operatorFunctions.add(new FunctionDescription("<", "Less Than", "Delivers true if the first term is less than the second; example: att1 < 4", 2));
		operatorFunctions.add(new FunctionDescription(">", "Greater Than", "Delivers true if the first term is greater than the second; example: att2 > 3", 2));
		operatorFunctions.add(new FunctionDescription("<=", "Less Equals", "Delivers true if the first term is less than or equal to the second; example: att3 <= 5", 2));
		operatorFunctions.add(new FunctionDescription(">=", "Greater Equals", "Delivers true if the first term is greater than or equal to the second; example: att4 >= 4", 2));
		operatorFunctions.add(new FunctionDescription("==", "Equals", "Delivers true if the first term is equal to the second; example: att1 == att2", 2));
		operatorFunctions.add(new FunctionDescription("!=", "Not Equals", "Delivers true if the first term is not equal to the second; example: att1 != att2", 2));
		operatorFunctions.add(new FunctionDescription("!", "Boolean Not", "Delivers true if the following term is false or vice versa; example: !(att1 > 2)", 1));
		operatorFunctions.add(new FunctionDescription("&&", "Boolean And", "Delivers true if both surrounding terms are true; example: (att1 > 2) && (att2 < 4)", 2));
		operatorFunctions.add(new FunctionDescription("||", "Boolean Or", "Delivers true if at least one of the surrounding terms is true; example: (att1 < 3) || (att2 > 1)", 2));
		FUNCTIONS.put(FUNCTION_GROUPS[0], operatorFunctions);

		// log and exponential functions
		List<FunctionDescription> logFunctions = new LinkedList<FunctionDescription>();
		logFunctions.add(new FunctionDescription("ln()", "Natural Logarithm", "Calculates the logarithm of the argument to the base e; example: ln(5)", 1));
		logFunctions.add(new FunctionDescription("log()", "Logarithm Base 10", "Calculates the logarithm of the argument to the base 10; example: log(att1)", 1));
		logFunctions.add(new FunctionDescription("ld()", "Logarithm Base 2", "Calculates the logarithm of the argument to the base 2; example: ld(att2)", 1));
		logFunctions.add(new FunctionDescription("exp()", "Exponential", "Calculates the value of the constant e to the power of the argument; example: exp(att3)", 1));
		logFunctions.add(new FunctionDescription("pow()", "Power", "Calculates the first term to the power of the second one; example: pow(att1, 3)", 2));
		FUNCTIONS.put(FUNCTION_GROUPS[1], logFunctions);

		// trigonometric functions
		List<FunctionDescription> trigonometricFunctions = new LinkedList<FunctionDescription>();
		trigonometricFunctions.add(new FunctionDescription("sin()", "Sine", "Calculates the sine of the given argument; example: sin(att1)", 1));
		trigonometricFunctions.add(new FunctionDescription("cos()", "Cosine", "Calculates the cosine of the given argument; example: cos(att2)", 1));
		trigonometricFunctions.add(new FunctionDescription("tan()", "Tangent", "Calculates the tangent of the given argument; example: tan(att3)", 1));
		trigonometricFunctions.add(new FunctionDescription("asin()", "Arc Sine", "Calculates the inverse sine of the given argument; example: asin(att1)", 1));
		trigonometricFunctions.add(new FunctionDescription("acos()", "Arc Cos", "Calculates the inverse cosine of the given argument; example: acos(att2)", 1));
		trigonometricFunctions.add(new FunctionDescription("atan()", "Arc Tangent", "Calculates the inverse tangent of the given argument; example: atan(att3)", 1));
		trigonometricFunctions.add(new FunctionDescription("atan2()", "Arc Tangent 2", "Calculates the inverse tangent based on the two given arguments; example: atan(att1, 0.5)",
				2));
		trigonometricFunctions.add(new FunctionDescription("sinh()", "Hyperbolic Sine", "Calculates the hyperbolic sine of the given argument; example: sinh(att2)", 1));
		trigonometricFunctions.add(new FunctionDescription("cosh()", "Hyperbolic Cosine", "Calculates the hyperbolic cosine of the given argument; example: cosh(att3)", 1));
		trigonometricFunctions.add(new FunctionDescription("tanh()", "Hyperbolic Tangent", "Calculates the hyperbolic tangent of the given argument; example: tanh(att1)", 1));
		trigonometricFunctions.add(new FunctionDescription("asinh()", "Inverse Hyperbolic Sine",
				"Calculates the inverse hyperbolic sine of the given argument; example: asinh(att2)", 1));
		trigonometricFunctions.add(new FunctionDescription("acosh()", "Inverse Hyperbolic Cosine",
				"Calculates the inverse hyperbolic cosine of the given argument; example: acosh(att3)", 1));
		trigonometricFunctions.add(new FunctionDescription("atanh()", "Inverse Hyperbolic Tangent",
				"Calculates the inverse hyperbolic tangent of the given argument; example: atanh(att1)", 1));
		FUNCTIONS.put(FUNCTION_GROUPS[2], trigonometricFunctions);

		// statistical functions
		List<FunctionDescription> statisticalFunctions = new LinkedList<FunctionDescription>();
		statisticalFunctions
				.add(new FunctionDescription(
						"round()",
						"Round",
						"Rounds the given number to the next integer. If two arguments are given, the first one is rounded to the number of digits indicated by the second argument; example: round(att1) or round(att2, 3)",
						2));
		statisticalFunctions.add(new FunctionDescription("floor()", "Floor", "Calculates the next integer less than the given argument; example: floor(att3)", 1));
		statisticalFunctions.add(new FunctionDescription("ceil()", "Ceil", "Calculates the next integer greater than the given argument; example: ceil(att1)", 1));
		statisticalFunctions.add(new FunctionDescription("avg()", "Average", "Calculates the average of the given arguments; example: avg(att1, att3)",
				FunctionDescription.UNLIMITED_NUMBER_OF_ARGUMENTS));
		statisticalFunctions.add(new FunctionDescription("min()", "Minimum", "Calculates the minimum of the given arguments; example: min(0, att2, att3)",
				FunctionDescription.UNLIMITED_NUMBER_OF_ARGUMENTS));
		statisticalFunctions.add(new FunctionDescription("max()", "Maximum", "Calculates the maximum of the given arguments; example: max(att1, att2)",
				FunctionDescription.UNLIMITED_NUMBER_OF_ARGUMENTS));
		FUNCTIONS.put(FUNCTION_GROUPS[3], statisticalFunctions);

		// text functions
		List<FunctionDescription> textFunctions = new LinkedList<FunctionDescription>();
		textFunctions.add(new FunctionDescription("str()", "To String", "Transforms the given number into a string (nominal value); example: str(17)", 1));
		textFunctions.add(new FunctionDescription("parse()", "To Number", "Transforms the given string (nominal value) into a number by parsing it; example: parse(att2)", 1));
		textFunctions.add(new FunctionDescription("cut()", "Cut",
				"Cuts the substring of given length at the given start out of a string; example: cut(\"Text\", 1, 2) delivers \"ex\"", 3));
		textFunctions.add(new FunctionDescription("concat()", "Concatenation",
				"Concatenates the given arguments (the + operator can also be used for this); <br>example: both concat(\"At\", \"om\") and \"At\" + \"om\" deliver \"Atom\"",
				FunctionDescription.UNLIMITED_NUMBER_OF_ARGUMENTS));
		textFunctions
				.add(new FunctionDescription(
						"replace()",
						"Replace",
						"Replaces all occurences of a search string by the defined replacement; <br>example: replace(att1, \"am\", \"pm\") replaces all occurences of \"am\" in each value of attribute att1 by \"pm\"",
						3));
		textFunctions
				.add(new FunctionDescription(
						"replaceAll()",
						"Replace All",
						"Evaluates the first argument as regular expression and replaces all matches by the defined replacement; <br>example: replaceAll(att1, \"[abc]\", \"X\") replaces all occurences of \"a\", \"b\" or \"c\" by \"X\" in each value of attribute att1",
						3));
		textFunctions.add(new FunctionDescription("lower()", "Lower", "Transforms the given argument into lower case characters; example: lower(att2)", 1));
		textFunctions.add(new FunctionDescription("upper()", "Upper", "Transforms the given argument into upper case characters; example: upper(att3)", 1));
		textFunctions.add(new FunctionDescription("index()", "Index",
				"Delivers the first position of the given search string in the text; example: index(\"Text\", \"e\") delivers 1", 2));
		textFunctions.add(new FunctionDescription("length()", "Length", "Delivers the length of the given argument; example: length(att1)", 1));
		textFunctions.add(new FunctionDescription("char()", "Character At", "Delivers the character at the specified position; example: char(att2, 3)", 2));
		textFunctions.add(new FunctionDescription("compare()", "Compare",
				"Compares the two arguments and deliver a negative value, if the first argument is lexicographically smaller; example: compare(att2, att3)", 2));
		textFunctions.add(new FunctionDescription("contains()", "Contains", "Delivers true if the second argument is part of the first one; example: contains(att1, \"pa\")", 2));
		textFunctions.add(new FunctionDescription("equals()", "Equals",
				"Delivers true if the two arguments are lexicographically equal to each other; example: equals(att1, att2)", 2));
		textFunctions.add(new FunctionDescription("starts()", "Starts With", "Delivers true if the first argument starts with the second; example: starts(att1, \"OS\")", 2));
		textFunctions.add(new FunctionDescription("ends()", "Ends With", "Delivers true if the first argument ends with the second; example: ends(att2, \"AM\")", 2));
		textFunctions.add(new FunctionDescription("matches()", "Matches",
				"Delivers true if the first argument matches the regular expression defined by the second argument; example: matches(att3, \".*mm.*\"", 2));
		textFunctions.add(new FunctionDescription("finds()", "Finds",
				"Delivers true if, and only if, a subsequence of the first matches the regular expression defined by the second argument; example: finds(att3, \".*AM.*|.*PM.*\"",
				2));
		textFunctions.add(new FunctionDescription("suffix()", "Suffix", "Delivers the suffix of the specified length; example: suffix(att1, 2)", 2));
		textFunctions.add(new FunctionDescription("prefix()", "Prefix", "Delivers the prefix of the specified length; example: prefix(att2, 3)", 2));
		textFunctions.add(new FunctionDescription("trim()", "Trim", "Removes all leading and trailing white space characters; example: trim(att3)", 1));
		textFunctions.add(new FunctionDescription("escape_html()", "Escape HTML", "Escapes the given string with HTML entities; example: escape_html(att1)", 1));
		FUNCTIONS.put(FUNCTION_GROUPS[4], textFunctions);

		// date functions
		List<FunctionDescription> dateFunctions = new LinkedList<FunctionDescription>();
		dateFunctions.add(new FunctionDescription("date_parse()", "Parse Date", "Parses the given string or double to a date; example: date_parse(att1)", 1));
		dateFunctions.add(new FunctionDescription("date_parse_loc()", "Parse Date with Locale",
				"Parses the given string or double to a date with the given locale (via lowercase two-letter ISO-639 code); <br>example: date_parse(att1, en)", 2));
		dateFunctions
				.add(new FunctionDescription(
						"date_parse_custom()",
						"Parse Custom Date",
						"Parses the given date string to a date using a custom pattern and the given locale (via lowercase two-letter ISO-639 code); <br>example: date_parse_custom(att1, \"dd|MM|yy\", \"de\")",
						3));
		dateFunctions.add(new FunctionDescription("date_before()", "Date Before",
				"Determines if the first date is strictly earlier than the second date; example: date_before(att1, att2)", 2));
		dateFunctions.add(new FunctionDescription("date_after()", "Date After",
				"Determines if the first date is strictly later than the second date; example: date_after(att1, att2)", 2));
		dateFunctions.add(new FunctionDescription("date_str()", "Date to String",
				"Changes a date to a string using the specified format; example: date_str(att1, DATE_FULL, DATE_SHOW_DATE_AND_TIME)", 3));
		dateFunctions
				.add(new FunctionDescription(
						"date_str_loc()",
						"Date to String with Locale",
						"Changes a date to a string using the specified format and the given locale (via lowercase two-letter ISO-639 code); <br>example: date_str_loc(att1, DATE_MEDIUM, DATE_SHOW_TIME_ONLY, \"us\")",
						4));
		dateFunctions
				.add(new FunctionDescription(
						"date_str_custom()",
						"Date to String with custom pattern",
						"Changes a date to a string using the specified custom format pattern and the (optional) given locale (via lowercase two-letter ISO-639 code); <br>example: date_str_custom(att1, \"dd|MM|yy\", \"us\")",
						4));
		dateFunctions.add(new FunctionDescription("date_now()", "Create Date", "Creates the current date; example: date_now()", 0));
		dateFunctions
				.add(new FunctionDescription(
						"date_diff()",
						"Date Difference",
						"Calculates the elapsed time between two dates. Locale and time zone arguments are optional; example: date_diff(timeStart, timeEnd, \"us\", \"America/Los_Angeles\")",
						4));
		dateFunctions
				.add(new FunctionDescription(
						"date_add()",
						"Add Time",
						"Allows to add a custom amount of time to a given date. Note that only the integer portion of a given value will be used! <br>Locale and Timezone arguments are optional; example: date_add(date, value, DATE_UNIT_DAY, \"us\", \"America/Los_Angeles\")",
						5));
		dateFunctions
				.add(new FunctionDescription(
						"date_set()",
						"Set Time",
						"Allows to set a custom value for a portion of a given date, e.g. set the day to 23. Note that only the integer portion of a given value will be used! <br>Locale and Timezone arguments are optional; example: date_set(date, value, DATE_UNIT_DAY, \"us\", \"America/Los_Angeles\")",
						5));
		dateFunctions
				.add(new FunctionDescription(
						"date_get()",
						"Get Time",
						"Allows to get a portion of a given date, e.g. get the day of a month only. Locale and Timezone arguments are optional; example: date_get(date, DATE_UNIT_DAY, \"us\", \"America/Los_Angeles\")",
						4));
		FUNCTIONS.put(FUNCTION_GROUPS[5], dateFunctions);

		// process functions
		List<FunctionDescription> processFunctions = new LinkedList<FunctionDescription>();
		processFunctions.add(new FunctionDescription("param()", "Parameter",
				"Delivers the specified parameter of the specified operator; example: param(\"Read Excel\", \"file\")", 2));
		FUNCTIONS.put(FUNCTION_GROUPS[6], processFunctions);

		// miscellaneous functions
		List<FunctionDescription> miscellaneousFunctions = new LinkedList<FunctionDescription>();
		miscellaneousFunctions
				.add(new FunctionDescription(
						"if()",
						"If-Then-Else",
						"Delivers the result of the second argument if the first one is evaluated to true and the result of the third argument otherwise; <br>example: if(att1 > 5, 7 * att1, att2 / 2)",
						3));
		miscellaneousFunctions.add(new FunctionDescription("const()", "Constant", "Delivers the argument as numerical constant value; example: const(att1)", 1));
		miscellaneousFunctions.add(new FunctionDescription("sqrt()", "Square Root", "Delivers the square root of the given argument; example: sqrt(att2)", 1));
		miscellaneousFunctions.add(new FunctionDescription("sgn()", "Signum", "Delivers -1 or +1 depending on the signum of the argument; example: sgn(-5)", 1));
		miscellaneousFunctions.add(new FunctionDescription("rand()", "Random", "Delivers a random number between 0 and 1; example: rand()", 0));
		miscellaneousFunctions.add(new FunctionDescription("mod()", "Modulus", "Calculates the modulus of the first term by the second one; example: 11 % 2", 2));
		miscellaneousFunctions.add(new FunctionDescription("sum()", "Sum", "Calculates the sum of all arguments; example: sum(att1, att3, 42)",
				FunctionDescription.UNLIMITED_NUMBER_OF_ARGUMENTS));
		miscellaneousFunctions.add(new FunctionDescription("binom()", "Binomial", "Calculates the binomial coefficients; example: binom(5, 2)", 2));
		miscellaneousFunctions.add(new FunctionDescription("missing()", "Missing", "Checks if the given number is missing; example: missing(att1)", 1));
		miscellaneousFunctions.add(new FunctionDescription("bit_or()", "Bitwise OR", "Calculate the bitwise OR of two integer arguments; example: bit_or(att1, att2)", 2));
		miscellaneousFunctions.add(new FunctionDescription("bit_and()", "Bitwise AND", "Calculate the bitwise AND of two integer arguments; example: bit_and(att2, att3)", 2));
		miscellaneousFunctions.add(new FunctionDescription("bit_xor()", "Bitwise XOR", "Calculate the bitwise XOR of two integer arguments; example: bit_xor(att1, att3)", 2));
		miscellaneousFunctions.add(new FunctionDescription("bit_not()", "Bitwise NOT", "Calculate the bitwise NOT of the integer argument; example: bit_not(att2)", 1));
		FUNCTIONS.put(FUNCTION_GROUPS[7], miscellaneousFunctions);
	}

	private JEP parser;

	public ExpressionParser(boolean useStandardConstants) {
		initParser(useStandardConstants);
	}

	/**
	 * This constructor allows additional functions if called within a process.
	 */
	public ExpressionParser(boolean useStandardConstants, Process process) {
		this(useStandardConstants);
		if (process != null) {
			parser.addFunction("param", new ParameterValue(process));
		}
	}

	private void addCustomFunctions(JEP parser) {
		parser.addFunction("const", new Constant());

		parser.addFunction("str", new Str());
		parser.addFunction("avg", new Average());
		parser.addFunction("min", new Minimum());
		parser.addFunction("max", new Maximum());
		parser.addFunction("ld", new LogarithmDualis());
		parser.addFunction("sgn", new Signum());
		parser.addFunction("missing", new Missing());
		parser.addFunction("bit_or", new BitwiseOr());
		parser.addFunction("bit_and", new BitwiseAnd());
		parser.addFunction("bit_xor", new BitwiseXor());
		parser.addFunction("bit_not", new BitwiseNot());

		// text functions
		parser.addFunction("parse", new ParseNumber());
		parser.addFunction("cut", new Substring());
		parser.addFunction("concat", new Concat());
		parser.addFunction("replace", new Replace());
		parser.addFunction("replaceAll", new ReplaceRegex());
		parser.addFunction("lower", new LowerCase());
		parser.addFunction("upper", new UpperCase());
		parser.addFunction("index", new IndexOf());
		parser.addFunction("length", new Length());
		parser.addFunction("char", new CharAt());
		parser.addFunction("compare", new Compare());
		parser.addFunction("equals", new Equals());
		parser.addFunction("contains", new Contains());
		parser.addFunction("starts", new StartsWith());
		parser.addFunction("ends", new EndsWith());
		parser.addFunction("matches", new Matches());
		parser.addFunction("finds", new Finds());
		parser.addFunction("prefix", new Prefix());
		parser.addFunction("suffix", new Suffix());
		parser.addFunction("trim", new Trim());
		parser.addFunction("escape_html", new EscapeHTML());

		// date functions
		parser.addFunction("date_parse", new DateParse());
		parser.addFunction("date_parse_loc", new DateParseWithLocale());
		parser.addFunction("date_parse_custom", new DateParseCustom());
		parser.addFunction("date_before", new DateBefore());
		parser.addFunction("date_after", new DateAfter());
		parser.addFunction("date_str", new Date2String());
		parser.addFunction("date_str_loc", new Date2StringWithLocale());
		parser.addFunction("date_str_custom", new Date2StringCustom());
		parser.addFunction("date_now", new DateCreate());
		parser.addFunction("date_diff", new DateDiff());
		parser.addFunction("date_add", new DateAdd());
		parser.addFunction("date_set", new DateSet());
		parser.addFunction("date_get", new DateGet());
	}

	private void addCustomConstants(JEP parser) {
		parser.addConstant("true", Boolean.valueOf(true));
		parser.addConstant("TRUE", Boolean.valueOf(true));
		parser.addConstant("false", Boolean.valueOf(false));
		parser.addConstant("FALSE", Boolean.valueOf(false));
		parser.addConstant("NaN", Double.NaN);
		parser.addConstant("NAN", Double.NaN);

		parser.addConstant("DATE_SHORT", ExpressionParserConstants.DATE_FORMAT_SHORT);
		parser.addConstant("DATE_MEDIUM", ExpressionParserConstants.DATE_FORMAT_MEDIUM);
		parser.addConstant("DATE_LONG", ExpressionParserConstants.DATE_FORMAT_LONG);
		parser.addConstant("DATE_FULL", ExpressionParserConstants.DATE_FORMAT_FULL);
		parser.addConstant("DATE_SHOW_DATE_ONLY", ExpressionParserConstants.DATE_SHOW_DATE_ONLY);
		parser.addConstant("DATE_SHOW_TIME_ONLY", ExpressionParserConstants.DATE_SHOW_TIME_ONLY);
		parser.addConstant("DATE_SHOW_DATE_AND_TIME", ExpressionParserConstants.DATE_SHOW_DATE_AND_TIME);
		parser.addConstant("DATE_UNIT_YEAR", ExpressionParserConstants.DATE_UNIT_YEAR);
		parser.addConstant("DATE_UNIT_MONTH", ExpressionParserConstants.DATE_UNIT_MONTH);
		parser.addConstant("DATE_UNIT_WEEK", ExpressionParserConstants.DATE_UNIT_WEEK);
		parser.addConstant("DATE_UNIT_DAY", ExpressionParserConstants.DATE_UNIT_DAY);
		parser.addConstant("DATE_UNIT_HOUR", ExpressionParserConstants.DATE_UNIT_HOUR);
		parser.addConstant("DATE_UNIT_MINUTE", ExpressionParserConstants.DATE_UNIT_MINUTE);
		parser.addConstant("DATE_UNIT_SECOND", ExpressionParserConstants.DATE_UNIT_SECOND);
		parser.addConstant("DATE_UNIT_MILLISECOND", ExpressionParserConstants.DATE_UNIT_MILLISECOND);
	}

	public String[] getFunctionGroups() {
		return FUNCTION_GROUPS;
	}

	public List<FunctionDescription> getFunctions(String functionGroup) {
		return FUNCTIONS.get(functionGroup);
	}

	public void initParser(boolean useStandardConstants) {
		parser = new JEP();
		parser.addStandardFunctions();
		if (useStandardConstants)
			parser.addStandardConstants();

		addCustomFunctions(parser);
		addCustomConstants(parser);

		parser.setAllowUndeclared(false);
		parser.setImplicitMul(false);
	}

	/**
	 * This method allows to derive a value from the given function and store it
	 * as a macro in the macroHandler under the given name.
	 */
	public void addMacro(MacroHandler macroHandler, String name, String function) throws GenerationException {
		// parse expression
		parser.parseExpression(function);

		// check for errors
		if (parser.hasError()) {
			throw new GenerationException(parser.getErrorInfo());
		}

		// create the new attribute from the delivered type
		Object result = parser.getValueAsObject();

		// check for errors
		if (parser.hasError()) {
			throw new GenerationException(parser.getErrorInfo());
		}

		// set result as macro
		if (result != null) {
			if (result instanceof Calendar) {
				Calendar calendar = (Calendar) result;
				macroHandler.addMacro(name, Tools.formatDateTime(new Date(calendar.getTimeInMillis())));
			} else {
				try {
					macroHandler.addMacro(name, Tools.formatIntegerIfPossible(Double.parseDouble(result.toString())));
				} catch (NumberFormatException e) {
					macroHandler.addMacro(name, result.toString());
				}
			}
		}
	}

	public void addAttributeMetaData(ExampleSetMetaData emd, String name, String function) {
		parser.setAllowUndeclared(true);

		// parse expression
		parser.parseExpression(function);

		// check for errors
		if (!parser.hasError()) {

			// derive all used variables
			SymbolTable symbolTable = parser.getSymbolTable();
			Map<String, AttributeMetaData> name2attributes = new HashMap<String, AttributeMetaData>();
			for (Object variableObj : symbolTable.values()) {
				Variable variable = (Variable) variableObj;// symbolTable.getVar(variableName.toString());
				if (!variable.isConstant()) {
					AttributeMetaData attribute = emd.getAttributeByName(variable.getName());
					if (attribute != null) {
						name2attributes.put(variable.getName(), attribute);
						if (attribute.isNominal()) {
							parser.addVariable(attribute.getName(), "");
						} else {
							parser.addVariable(attribute.getName(), Double.NaN);
						}
					}
				}
			}
			if (!parser.hasError()) {
				// create the new attribute from the delivered type
				Object result = parser.getValueAsObject();

				if (!parser.hasError()) {
					AttributeMetaData newAttribute = null;
					if (result instanceof Boolean) {
						newAttribute = new AttributeMetaData(name, Ontology.BINOMINAL);
						HashSet<String> values = new HashSet<String>();
						values.add("false");
						values.add("true");
						newAttribute.setValueSet(values, SetRelation.EQUAL);
					} else if (result instanceof Number) {
						newAttribute = new AttributeMetaData(name, Ontology.REAL);
					} else if (result instanceof Complex) {
						newAttribute = new AttributeMetaData(name, Ontology.REAL);
					} else if (result instanceof Date) {
						newAttribute = new AttributeMetaData(name, Ontology.DATE_TIME);
					} else if (result instanceof Calendar) {
						newAttribute = new AttributeMetaData(name, Ontology.DATE_TIME);
					} else {
						newAttribute = new AttributeMetaData(name, Ontology.NOMINAL);
					}
					emd.addAttribute(newAttribute);
				} else {
					emd.addAttribute(new AttributeMetaData(name, Ontology.ATTRIBUTE_VALUE));
				}
			} else {
				emd.addAttribute(new AttributeMetaData(name, Ontology.ATTRIBUTE_VALUE));
			}
		} else {
			emd.addAttribute(new AttributeMetaData(name, Ontology.ATTRIBUTE_VALUE));
		}
	}

	/**
	 * Iterates over the {@link ExampleSet}, interprets attributes as variables,
	 * evaluates the function and creates a new attribute with the given name
	 * that takes the expression's value. The type of the attribute depends on
	 * the expression type and is {@link Ontology#NOMINAL} for strings,
	 * {@link Ontology#NUMERICAL} for reals and complex numbers,
	 * {@link Ontology#DATE_TIME} for Dates and Calendars and
	 * {@link Ontology#BINOMINAL} with values &quot;true&quot; and
	 * &quot;false&quot; for booleans.
	 * 
	 * @return The generated attribute
	 * */
	public Attribute addAttribute(ExampleSet exampleSet, String name, String function) throws GenerationException {

		Object result = null;
		Map<String, Attribute> name2attributes = null;

		// expression parse only need to be called if there a examples present
		if (exampleSet.size() != 0) {
			parser.setAllowUndeclared(true);

			// parse expression
			parser.parseExpression(function);

			// check for errors
			if (parser.hasError()) {
				throw new GenerationException("Offending attribute: '" + name + "', Expression: '" + function + "', Error: '" + parser.getErrorInfo() + "'");
			}

			// let the parser know the attributes
			name2attributes = deriveVariablesFromExampleSet(parser, exampleSet);

			if (parser.hasError()) {
				throw new GenerationException("Offending attribute: '" + name + "', Expression: '" + function + "', Error: '" + parser.getErrorInfo() + "'");
			}

			// create the new attribute from the delivered type
			result = parser.getValueAsObject();

			if (parser.hasError()) {
				throw new GenerationException("Offending attribute: '" + name + "', Expression: '" + function + "', Error: '" + parser.getErrorInfo() + "'");
			}
		}

		Attribute newAttribute = null;
		// if != null this needs to be overriden
		Attribute existingAttribute = exampleSet.getAttributes().get(name);
		StringBuffer appendix = new StringBuffer();
		String targetName = name;
		if (existingAttribute != null) {
			// append a random string to the attribute's name until it's a unique attribute name
			do {
				appendix.append(RandomGenerator.getGlobalRandomGenerator().nextString(5));
			} while (exampleSet.getAttributes().get(name + appendix.toString()) != null);
			name = name + appendix.toString();
		}

		if (result instanceof Boolean || result == UnknownValue.UNKNOWN_BOOLEAN) {
			newAttribute = AttributeFactory.createAttribute(name, Ontology.BINOMINAL);
			newAttribute.getMapping().mapString("false");
			newAttribute.getMapping().mapString("true");
		} else if (result instanceof Number) {
			newAttribute = AttributeFactory.createAttribute(name, Ontology.REAL);
		} else if (result instanceof Complex) {
			newAttribute = AttributeFactory.createAttribute(name, Ontology.REAL);
		} else if (result instanceof Date || result == UnknownValue.UNKNOWN_DATE) {
			newAttribute = AttributeFactory.createAttribute(name, Ontology.DATE_TIME);
		} else if (result instanceof Calendar || result == UnknownValue.UNKNOWN_DATE) {
			newAttribute = AttributeFactory.createAttribute(name, Ontology.DATE_TIME);
		} else {
			newAttribute = AttributeFactory.createAttribute(name, Ontology.NOMINAL);
		}

		// set construction description
		newAttribute.setConstruction(function);

		// add new attribute to table and example set
		exampleSet.getExampleTable().addAttribute(newAttribute);
		exampleSet.getAttributes().addRegular(newAttribute);

		// create attribute of correct type and all values
		for (Example example : exampleSet) {

			// assign values to the variables
			assignVariableValuesFromExample(parser, example, name2attributes);

			// calculate result
			result = parser.getValueAsObject();

			// check for errors
			if (parser.hasError()) {
				throw new GenerationException("Offending attribute: '" + name + "', Expression: '" + function + "', Error: '" + parser.getErrorInfo() + "'");
			}

			// store result
			if (result instanceof Boolean) {
				if ((Boolean) result) {
					example.setValue(newAttribute, newAttribute.getMapping().mapString("true"));
				} else {
					example.setValue(newAttribute, newAttribute.getMapping().mapString("false"));
				}
			} else if (result instanceof Number) {
				example.setValue(newAttribute, ((Number) result).doubleValue());
			} else if (result instanceof Complex) {
				example.setValue(newAttribute, ((Complex) result).doubleValue());
			} else if (result instanceof Date) {
				example.setValue(newAttribute, ((Date) result).getTime());
			} else if (result instanceof Calendar) {
				example.setValue(newAttribute, ((Calendar) result).getTimeInMillis());
			} else if (result instanceof UnknownValue) {
				example.setValue(newAttribute, Double.NaN);
			} else {
				example.setValue(newAttribute, newAttribute.getMapping().mapString(result.toString()));
			}
		}

		// remove existing attribute (if necessary)
		if (existingAttribute != null) {
			/* FIXME: The following line cannot be used, as the attribute might
			* occur in other example sets, or other attribute instances might use the same 
			* ExampleTable's column.
			* 
			* exampleSet.getExampleTable().removeAttribute(existingAttribute);
			*/
			AttributeRole oldRole = exampleSet.getAttributes().getRole(existingAttribute);
			exampleSet.getAttributes().remove(existingAttribute);
			newAttribute.setName(targetName);
			// restore role from old attribute to new attribute
			exampleSet.getAttributes().setSpecialAttribute(newAttribute, oldRole.getSpecialName());
		}

		return newAttribute;
	}

	/**
	 * Make the exampleSet's attributes available to the parser as variables.
	 * Returns a map which is used by {@link #assignVariableValuesFromExample(JEP, Example, Map)}.
	 * @param parser
	 * @param exampleSet
	 * @return
	 * @throws GenerationException
	 */
	public static Map<String, Attribute> deriveVariablesFromExampleSet(JEP parser, ExampleSet exampleSet) throws GenerationException {
		// derive all used variables
		SymbolTable symbolTable = parser.getSymbolTable();
		Map<String, Attribute> name2attributes = new HashMap<String, Attribute>();
		for (Object variableObj : symbolTable.values()) {
			Variable variable = (Variable) variableObj;// symbolTable.getVar(variableName.toString());
			if (!variable.isConstant()) {
				Attribute attribute = exampleSet.getAttributes().get(variable.getName());
				if (attribute == null) {
					throw new GenerationException("No such attribute: '" + variable.getName() + "'");
				} else {
					name2attributes.put(variable.getName(), attribute);
					// retrieve test example with real values (needed to
					// compliance checking!)
					if (exampleSet.size() > 0) {
						Example example = exampleSet.iterator().next();
						if (attribute.isNominal()) {
							if (Double.isNaN(example.getValue(attribute))) {
								parser.addVariable(attribute.getName(), UnknownValue.UNKNOWN_NOMINAL); // ExpressionParserConstants.MISSING_VALUE);
							} else {
								parser.addVariable(attribute.getName(), example.getValueAsString(attribute));
							}
						} else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), Ontology.DATE_TIME)) {
							Calendar cal = Calendar.getInstance();
							cal.setTime(new Date((long) example.getValue(attribute)));
							parser.addVariable(attribute.getName(), cal);
						} else {
							parser.addVariable(attribute.getName(), example.getValue(attribute));
						}
					} else {
						// nothing will be done later: no compliance to data
						// must be met
						if (attribute.isNominal()) {
							parser.addVariable(attribute.getName(), UnknownValue.UNKNOWN_NOMINAL);
						} else {
							parser.addVariable(attribute.getName(), Double.NaN);
						}
					}
				}
			}
		}
		return name2attributes;
	}

	/**
	 * Make the variable values from the example available to the parser.
	 * @param parser
	 * @param example
	 * @param name2attributes
	 */
	public static void assignVariableValuesFromExample(JEP parser, Example example, Map<String, Attribute> name2attributes) {
		// assign variable values
		for (Map.Entry<String, Attribute> entry : name2attributes.entrySet()) {
			String variableName = entry.getKey();
			Attribute attribute = entry.getValue();
			double value = example.getValue(attribute);
			if (attribute.isNominal()) {
				if (Double.isNaN(value)) {
					parser.setVarValue(variableName, UnknownValue.UNKNOWN_NOMINAL);
				} else {
					parser.setVarValue(variableName, example.getValueAsString(attribute));
				}
			} else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), Ontology.DATE_TIME)) {
				if (Double.isNaN(value)) {
					parser.setVarValue(variableName, UnknownValue.UNKNOWN_DATE);
				} else {
					Calendar cal = Calendar.getInstance();
					cal.setTime(new Date((long) value));
					parser.setVarValue(variableName, cal);
				}
			} else {
				parser.setVarValue(variableName, value);
			}
		}
	}

	public JEP getParser() {
		return parser;
	}

	/**
	 * Parses all lines of the AttributeConstruction file and returns a list
	 * containing all newly generated attributes.
	 */
	public static List<Attribute> generateAll(LoggingHandler logging, ExampleSet exampleSet, InputStream in) throws IOException, GenerationException {
		LinkedList<Attribute> generatedAttributes = new LinkedList<Attribute>();
		Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
		} catch (SAXException e1) {
			throw new IOException(e1.getMessage());
		} catch (ParserConfigurationException e1) {
			throw new IOException(e1.getMessage());
		}

		Element constructionsElement = document.getDocumentElement();
		if (!constructionsElement.getTagName().equals("constructions")) {
			throw new IOException("Outer tag of attribute constructions file must be <constructions>");
		}

		NodeList constructions = constructionsElement.getChildNodes();
		for (int i = 0; i < constructions.getLength(); i++) {
			Node node = constructions.item(i);
			if (node instanceof Element) {
				Element constructionTag = (Element) node;
				String tagName = constructionTag.getTagName();
				if (!tagName.equals("attribute"))
					throw new IOException("Only <attribute> tags are allowed for attribute description files, but found " + tagName);
				String attributeName = constructionTag.getAttribute("name");
				String attributeConstruction = constructionTag.getAttribute("construction");

				ExpressionParser parser = new ExpressionParser(true);
				if (attributeName == null) {
					throw new IOException("<attribute> tag needs 'name' attribute.");
				}
				if (attributeConstruction == null) {
					throw new IOException("<attribute> tag needs 'construction' attribute.");
				}
				if (attributeConstruction.equals(attributeName)) {
					Attribute presentAttribute = exampleSet.getAttributes().get(attributeName);
					if (presentAttribute != null) {
						generatedAttributes.add(presentAttribute);
						continue;
					} else {
						throw new GenerationException("No such attribute: " + attributeName);
					}
				} else {
					generatedAttributes.add(parser.addAttribute(exampleSet, attributeName, attributeConstruction));
				}
			}
		}
		return generatedAttributes;
	}
}
