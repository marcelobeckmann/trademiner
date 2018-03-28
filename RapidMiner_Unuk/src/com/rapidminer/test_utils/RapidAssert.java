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
package com.rapidminer.test_utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.junit.ComparisonFailure;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.table.SparseDataRow;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.IOObjectCollection;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.meta.ParameterSet;
import com.rapidminer.operator.meta.ParameterValue;
import com.rapidminer.operator.nio.file.FileObject;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.visualization.dependencies.NumericalMatrix;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.math.Averagable;
import com.rapidminer.tools.math.AverageVector;

/**
 * Extension for JUnit's Assert for testing RapidMiner objects.
 * 
 * @author Simon Fischer, Marcin Skirzynski, Marius Helf
 * 
 */
public class RapidAssert extends Assert {

	public static final double DELTA = 0.000000001;
	public static final double MAX_RELATIVE_ERROR = 0.000000001;

	public static final AsserterRegistry ASSERTER_REGISTRY = new AsserterRegistry();

	/** 
	 * init asserter registry
	 */
	static {
		/* asserter for ParameterSet */
		ASSERTER_REGISTRY.registerAsserter(new Asserter() {
			
			@Override
			public Class<?> getAssertable() {
				return ParameterSet.class;
			}
			
			@Override
			public void assertEquals(String message, Object expectedObj, Object actualObj) {
				ParameterSet expected = (ParameterSet) expectedObj;
				ParameterSet actual = (ParameterSet) actualObj;
				
				RapidAssert.assertEquals(message + " (performance vectors do not match)", expected.getPerformance(), actual.getPerformance());
				
				Iterator<ParameterValue> expectedIt = expected.getParameterValues();
				Iterator<ParameterValue> actualIt = actual.getParameterValues();
				
				while (expectedIt.hasNext()) {
					assertTrue(message + "(expected parameter vector is longer than actual parameter vector)", actualIt.hasNext());
					ParameterValue expectedParValue = expectedIt.next();
					ParameterValue actualParValue = actualIt.next();
					RapidAssert.assertEquals(message + " (parameter values)", expectedParValue, actualParValue);
				}
				assertFalse(message + "(expected parameter vector is shorter than actual parameter vector)", actualIt.hasNext());
			}
		});
		
		/* asserter for PerformanceCriterion */
		ASSERTER_REGISTRY.registerAsserter(new Asserter() {

			/**
			 * Tests for equality by testing all averages, standard deviation and variances, as well as the fitness, max fitness 
			 * and example count.
			 *  
			 * @param message		message to display if an error occurs
			 * @param expected		expected criterion
			 * @param actual		actual criterion
			 */
			@Override
			public void assertEquals(String message, Object expectedObj, Object actualObj) {
				PerformanceCriterion expected = (PerformanceCriterion) expectedObj;
				PerformanceCriterion actual = (PerformanceCriterion) actualObj;

				List<Asserter> averegableAsserter = ASSERTER_REGISTRY.getAsserterForClass(Averagable.class);
				if (averegableAsserter != null) {
					for (Asserter asserter : averegableAsserter) {
						asserter.assertEquals(message, (Averagable) expected, (Averagable) actual);
					}
				} else {
					throw new ComparisonFailure("Comparison of " + Averagable.class.toString() + " is not supported. ", expectedObj.toString(), actualObj.toString());
				}
				Assert.assertEquals(message + " (fitness is not equal)", expected.getFitness(), actual.getFitness());
				Assert.assertEquals(message + " (max fitness is not equal)", expected.getMaxFitness(), actual.getMaxFitness());
				Assert.assertEquals(message + " (example count is not equal)", expected.getExampleCount(), actual.getExampleCount());
			}

			@Override
			public Class<?> getAssertable() {
				return PerformanceCriterion.class;
			}
		});

		ASSERTER_REGISTRY.registerAsserter(new Asserter() {

			/**
			 * Tests for equality by testing all averages, standard deviation and variances.
			 * 
			 * @param message		message to display if an error occurs
			 * @param expected		expected averagable
			 * @param actual		actual averagable
			 */
			@Override
			public void assertEquals(String message, Object expectedObj, Object actualObj) {
				Averagable expected = (Averagable) expectedObj;
				Averagable actual = (Averagable) actualObj;

				Assert.assertEquals(message + " (average is not equal)", expected.getAverage(), actual.getAverage());
				Assert.assertEquals(message + " (makro average is not equal)", expected.getMakroAverage(), actual.getMakroAverage());
				Assert.assertEquals(message + " (mikro average is not equal)", expected.getMikroAverage(), actual.getMikroAverage());
				Assert.assertEquals(message + " (average count is not equal)", expected.getAverageCount(), actual.getAverageCount());
				Assert.assertEquals(message + " (makro standard deviation is not equal)", expected.getMakroStandardDeviation(), actual.getMakroStandardDeviation());
				Assert.assertEquals(message + " (mikro standard deviation is not equal)", expected.getMikroStandardDeviation(), actual.getMikroStandardDeviation());
				Assert.assertEquals(message + " (standard deviation is not equal)", expected.getStandardDeviation(), actual.getStandardDeviation());
				Assert.assertEquals(message + " (makro variance is not equal)", expected.getMakroVariance(), actual.getMakroVariance());
				Assert.assertEquals(message + " (mikro variance is not equal)", expected.getMikroVariance(), actual.getMikroVariance());
				Assert.assertEquals(message + " (variance is not equal)", expected.getVariance(), actual.getVariance());

			}

			@Override
			public Class<?> getAssertable() {
				return Averagable.class;
			}
		});

		ASSERTER_REGISTRY.registerAsserter(new Asserter() {

			/**
			 * Tests the two average vectors for equality by testing the size and each averagable.
			 * 
			 * @param message		message to display if an error occurs
			 * @param expected		expected vector
			 * @param actual		actual vector
			 */
			@Override
			public void assertEquals(String message, Object expectedObj, Object actualObj) {
				AverageVector expected = (AverageVector) expectedObj;
				AverageVector actual = (AverageVector) actualObj;

				message = message + "Average vectors are not equals";

				int expSize = expected.getSize();
				int actSize = actual.getSize();
				Assert.assertEquals(message + " (size of the average vector is not equal)", expSize, actSize);
				int size = expSize;

				for (int i = 0; i < size; i++) {
					RapidAssert.assertEquals(message, expected.getAveragable(i), actual.getAveragable(i));
				}
			}

			@Override
			public Class<?> getAssertable() {
				return AverageVector.class;
			}

		});

		// Asserter for ExampleSet
		ASSERTER_REGISTRY.registerAsserter(new Asserter() {
			/**
			 * Tests two example sets by iterating over all examples.
			 * 
			 * @param message		message to display if an error occurs
			 * @param expected		expected value
			 * @param actual		actual value
			 */
			@Override
			public void assertEquals(String message, Object expectedObj, Object actualObj) {
				ExampleSet expected = (ExampleSet) expectedObj;
				ExampleSet actual = (ExampleSet) actualObj;

				message = message + " - ExampleSets are not equal";

				boolean compareAttributeDefaultValues = true;
				if (expected.getExampleTable().size() > 0) {
					compareAttributeDefaultValues = expected.getExampleTable().getDataRow(0) instanceof SparseDataRow;
				}
				
				
				// compare attributes
				RapidAssert.assertEquals(message, expected.getAttributes(), actual.getAttributes(), compareAttributeDefaultValues);
				
				// compare number of examples
				Assert.assertEquals(message + " (number of examples)", expected.size(), actual.size());
				
				// compare example values
				Iterator<Example> i1 = expected.iterator();
				Iterator<Example> i2 = actual.iterator();
				int row = 1;
				while (i1.hasNext() && i2.hasNext()) {
					RapidAssert.assertEquals(message + "(example number " + row + ", {0} value of {1})", i1.next(), i2.next());
					row++;
				}
			}

			@Override
			public Class<?> getAssertable() {
				return ExampleSet.class;
			}
		});

		ASSERTER_REGISTRY.registerAsserter(new Asserter() {

			/**
			 * Tests the collection of ioobjects
			 * 
			 * @param expected
			 * @param actual
			 */
			@Override
			public void assertEquals(String message, Object expectedObj, Object actualObj) {
				@SuppressWarnings("unchecked")
				IOObjectCollection<IOObject> expected = (IOObjectCollection) expectedObj;
				@SuppressWarnings("unchecked")
				IOObjectCollection<IOObject> actual = (IOObjectCollection) actualObj;

				message = message + "Collection of IOObjects are not equal: ";
				Assert.assertEquals(message + " (number of items)", expected.size(), actual.size());
				RapidAssert.assertEquals(message, expected.getObjects(), actual.getObjects());
			}

			@Override
			public Class<?> getAssertable() {
				return IOObjectCollection.class;
			}

		});

		ASSERTER_REGISTRY.registerAsserter(new Asserter() {

			/**
			 * Test two numerical matrices for equality. This contains tests about the number of columns and rows, as well as column&row names and if
			 * the matrices are marked as symmetrical and if every value within the matrix is equal.
			 *  
			 * @param message		message to display if an error occurs
			 * @param expected		expected matrix
			 * @param actual		actual matrix
			 */
			@Override
			public void assertEquals(String message, Object expectedObj, Object actualObj) {
				NumericalMatrix expected = (NumericalMatrix) expectedObj;
				NumericalMatrix actual = (NumericalMatrix) actualObj;

				message = message + "Numerical matrices are not equal";

				int expNrOfCols = expected.getNumberOfColumns();
				int actNrOfCols = actual.getNumberOfColumns();
				Assert.assertEquals(message + " (column number is not equal)", expNrOfCols, actNrOfCols);

				int expNrOfRows = expected.getNumberOfRows();
				int actNrOfRows = actual.getNumberOfRows();
				Assert.assertEquals(message + " (row number is not equal)", expNrOfRows, actNrOfRows);

				int cols = expNrOfCols;
				int rows = expNrOfRows;

				for (int col = 0; col < cols; col++) {
					String expectedColName = expected.getColumnName(col);
					String actualColName = actual.getColumnName(col);
					Assert.assertEquals(message + " (column name at index " + col + " is not equal)", expectedColName, actualColName);
				}

				for (int row = 0; row < rows; row++) {
					String expectedRowName = expected.getRowName(row);
					String actualRowName = actual.getRowName(row);
					Assert.assertEquals(message + " (row name at index " + row + " is not equal)", expectedRowName, actualRowName);
				}

				Assert.assertEquals(message + " (matrix symmetry is not equal)", expected.isSymmetrical(), actual.isSymmetrical());

				for (int row = 0; row < rows; row++) {
					for (int col = 0; col < cols; col++) {

						double expectedVal = expected.getValue(row, col);
						double actualVal = actual.getValue(row, col);
						Assert.assertEquals(message + " (value at row " + row + " and column " + col + " is not equal)", expectedVal, actualVal);

					}
				}

			}

			@Override
			public Class<?> getAssertable() {
				return NumericalMatrix.class;
			}

		});

		ASSERTER_REGISTRY.registerAsserter(new Asserter() {

			/**
			 * Tests the two performance vectors for equality by testing the size, the criteria names, the main criterion and each criterion.
			 * 
			 * @param message		message to display if an error occurs
			 * @param expected		expected vector
			 * @param actual		actual vector
			 */
			@Override
			public void assertEquals(String message, Object expectedObj, Object actualObj) {
				PerformanceVector expected = (PerformanceVector) expectedObj;
				PerformanceVector actual = (PerformanceVector) actualObj;

				message = message + "Performance vectors are not equal";

				int expSize = expected.getSize();
				int actSize = actual.getSize();
				Assert.assertEquals(message + " (size of the performance vector is not equal)", expSize, actSize);
				int size = expSize;

				RapidAssert.assertArrayEquals(message, expected.getCriteriaNames(), actual.getCriteriaNames());
				RapidAssert.assertEquals(message, expected.getMainCriterion(), actual.getMainCriterion());

				for (int i = 0; i < size; i++) {
					RapidAssert.assertEquals(message, expected.getCriterion(i), actual.getCriterion(i));
				}
			}

			@Override
			public Class<?> getAssertable() {
				return PerformanceVector.class;
			}
		});

		ASSERTER_REGISTRY.registerAsserter(new Asserter() {

			/**
			 * Tests the two performance vectors for equality by testing the
			 * size, the criteria names, the main criterion and each criterion.
			 * 
			 * @param message
			 *            message to display if an error occurs
			 * @param expected
			 *            expected vector
			 * @param actual
			 *            actual vector
			 */
			@Override
			public void assertEquals(String message, Object expectedObj, Object actualObj) throws RuntimeException {
				FileObject fo1 = (FileObject) expectedObj;
				FileObject fo2 = (FileObject) actualObj;
				InputStream is1 = null;
				InputStream is2 = null;
				ByteArrayOutputStream bs1 = null;
				ByteArrayOutputStream bs2 = null;
				try {
					is1 = fo1.openStream();
					is2 = fo2.openStream();
					bs1 = new ByteArrayOutputStream();
					bs2 = new ByteArrayOutputStream();
					Tools.copyStreamSynchronously(is1, bs1, true);
					Tools.copyStreamSynchronously(is2, bs2, true);
					byte[] fileData1 = bs1.toByteArray();
					byte[] fileData2 = bs2.toByteArray();
					assertArrayEquals("file object data", fileData1, fileData2);
				} catch (OperatorException e) {
					throw new RuntimeException("Stream Error");
				} catch (IOException e) {
					throw new RuntimeException("Stream Error");
				} finally {
					if (is1 != null) {
						try {
							is1.close();
						} catch (IOException e) {
							// silent
						}
					}
					if (is2 != null) {
						try {
							is2.close();
						} catch (IOException e) {
							// silent
						}
					}
					if (bs1 != null) {
						try {
							bs1.close();
						} catch (IOException e) {
							// silent
						}
					}
					if (bs2 != null) {
						try {
							bs2.close();
						} catch (IOException e) {
							// silent
						}
					}
				}
			}

			@Override
			public Class<?> getAssertable() {
				return FileObject.class;
			}
		});
	}

	/**
	 * Returns <code>true</code> if the ioobjects class is supported for
	 * comparison in the test extension and <code>false</code> otherwise.
	 */
	public static boolean comparable(IOObject ioobject) {
		return ASSERTER_REGISTRY.getAsserterForObject(ioobject) != null;
	}

	/**
	 * Returns <code>true</code> if both ioobject classes are comparable to
	 * each other and <code>false</code> otherwise.
	 */
	public static boolean comparable(IOObject ioobject1, IOObject ioobject2) {
		return ASSERTER_REGISTRY.getAsserterForObjects(ioobject1, ioobject2) != null;
	}

	/**
	 * Extends the Junit assertEquals method by additionally checking the doubles for NaN.
	 *  
	 * @param message		message to display if an error occurs
	 * @param expected		expected value
	 * @param actual		actual value
	 */
	public static void assertEqualsNaN(String message, double expected, double actual) {
		if (Double.isNaN(expected)) {
			if (!Double.isNaN(actual)) {
				throw new AssertionFailedError(message + " expected: <" + expected + "> but was: <" + actual + ">");
			}
		} else {
			assertEquals(message, expected, actual, DELTA);
		}
	}

	/**
	 * Attention: Does not work with values near 0!!
	 */
	public static void assertEqualsWithRelativeErrorOrBothNaN(String message, double expected, double actual) {
		if (expected == actual) {
			return;
		}

		if (Double.isNaN(expected) && !Double.isNaN(actual)) {
			throw new AssertionFailedError(message + " expected: <" + expected + "> but was: <" + actual + ">");
		}

		if (!Double.isNaN(expected) && Double.isNaN(actual)) {
			throw new AssertionFailedError(message + " expected: <" + expected + "> but was: <" + actual + ">");
		}

		double relativeError;
		if (Math.abs(actual) > Math.abs(expected)) {
			relativeError = Math.abs((expected - actual) / actual);
		} else {
			relativeError = Math.abs((expected - actual) / expected);
		}
		if (relativeError > MAX_RELATIVE_ERROR) {
			throw new AssertionFailedError(message + " expected: <" + expected + "> but was: <" + actual + ">");
		}
	}

	/**
	 * Tests if the special names of the attribute roles are equal and the associated attributes themselves.
	 * 
	 * @param message		message to display if an error occurs
	 * @param expected		expected value
	 * @param actual		actual value
	 * @param compareDefaultValues 
	 */
	public static void assertEquals(String message, AttributeRole expected, AttributeRole actual, boolean compareDefaultValues) {
		Assert.assertEquals(message + " (attribute role)", expected.getSpecialName(), actual.getSpecialName());
		Attribute expectedAttribute = expected.getAttribute();
		Attribute actualAttribute = actual.getAttribute();
		assertEquals(message, expectedAttribute, actualAttribute, compareDefaultValues);
	}

	/**
	 * Tests two attributes by using the name, type, block, type, default value and the nominal mapping
	 * 
	 * @param message		message to display if an error occurs
	 * @param expected		expected value
	 * @param actual		actual value
	 */
	public static void assertEquals(String message, Attribute expected, Attribute actual, boolean compareDefaultValues) {
		Assert.assertEquals(message + " (attribute name)", expected.getName(), actual.getName());
		Assert.assertEquals(message + " (attribute type of attribute '"+expected.getName()+"': expected '" + Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(expected.getValueType()) + "' but was '" + Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(actual.getValueType()) + "')", expected.getValueType(), actual.getValueType());
		Assert.assertEquals(message + " (attribute block type of attribute '"+expected.getName()+": expected '" + Ontology.ATTRIBUTE_BLOCK_TYPE.mapIndex(expected.getBlockType()) + "' but was '" + Ontology.ATTRIBUTE_BLOCK_TYPE.mapIndex(actual.getBlockType()) + "')", expected.getBlockType(), actual.getBlockType());

		if (compareDefaultValues) {
			Assert.assertEquals(message + " (default value of attribute '"+expected.getName()+")", expected.getDefault(), actual.getDefault());
		}
		if (expected.isNominal()) {
			assertEqualsIgnoreOrder(message + " (nominal mapping of attribute '" + expected.getName() + ")", expected.getMapping(), actual.getMapping());
		}
	}

	/**
	 * Tests two nominal mappings for its size and values.
	 * 
	 * @param message		message to display if an error occurs
	 * @param expected		expected value
	 * @param actual		actual value
	 * @param ignoreOrder	if <code>true</code> the order of the mappings is not checked, 
	 * 						but only the size of the mapping and that all values of <code>expected</code> 
	 * 						are present in <code>actual</code>. 
	 */
	public static void assertEquals(String message, NominalMapping expected, NominalMapping actual, boolean ignoreOrder) {
		if (expected == actual) {
			return;
		}
		Assert.assertTrue((expected == null && actual == null) || (expected != null && actual != null));
		if (expected == null) {
			// also actual == null
			return;
		}

		Assert.assertEquals(message + " (nominal mapping size)", expected.size(), actual.size());

		List<String> expectedValues = expected.getValues();
		List<String> actualValues = actual.getValues();

		// check that we have the same values in both mappings:
		Set<String> expectedValuesSet = new HashSet<String>(expectedValues);
		Set<String> actualValuesSet = new HashSet<String>(actualValues);
		Assert.assertEquals(message + " (different nominal values)", expectedValuesSet, actualValuesSet);

		if (!ignoreOrder) {
			// check order

			Iterator<String> expectedIt = expectedValues.iterator();
			while (expectedIt.hasNext()) {
				String expectedValue = expectedIt.next();
				Assert.assertEquals(message + " (index of nominal value '" + expectedValue + "')", expected.mapString(expectedValue), actual.mapString(expectedValue));
			}
		}
	}

	/**
	 * Tests two nominal mappings for its size and values.
	 * 
	 * @param message		message to display if an error occurs
	 * @param expected		expected value
	 * @param actual		actual value
	 */
	public static void assertEqualsIgnoreOrder(String message, NominalMapping expected, NominalMapping actual) {
		assertEquals(message, expected, actual, true);
	}

	/**
	 * Tests all objects in the array.
	 * 
	 * @param expected	array with expected objects
	 * @param actual	array with actual objects
	 */
	public static void assertArrayEquals(String message, Object[] expected, Object[] actual) {
		if (expected == null) {
			junit.framework.Assert.assertEquals((Object) null, actual);
			return;
		}
		if (actual == null) {
			throw new AssertionFailedError(message + " (expected " + expected.toString() + " , but is null)");
		}
		junit.framework.Assert.assertEquals(message + " (array length is not equal)", expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			junit.framework.Assert.assertEquals(message, expected[i], actual[i]);
		}
	}

	public static void assertArrayEquals(String message, byte[] expected, byte[] actual) {
		if (expected == null) {
			junit.framework.Assert.assertEquals((Object) null, actual);
			return;
		}
		if (actual == null) {
			throw new AssertionFailedError(message + " (expected " + expected.toString() + " , but is null)");
		}
		junit.framework.Assert.assertEquals(message + " (array length is not equal)", expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			junit.framework.Assert.assertEquals(message, expected[i], actual[i]);
		}
	}

	/**
	 * Compares a string linewise, i.e. ignores different linebreak characters.
	 * 
	 * Does this by transforming all linebreaks into a single \n and then comparing
	 * the complete texts. Thus you get a nice diff when using junit within eclipse.
	 * 
	 * @param message
	 * @param expected
	 * @param actual
	 */
	public static void assertLinewiseEquals(String message, String expected, String actual) {
		Scanner expectedScanner = new Scanner(expected);
		Scanner actualScanner = new Scanner(actual);
		
		StringBuilder expectedBuilder = new StringBuilder();
		StringBuilder actualBuilder = new StringBuilder();
		
		while (expectedScanner.hasNextLine()) {
			expectedBuilder.append(expectedScanner.nextLine()).append("\n");
		}
		while (actualScanner.hasNextLine()) {
			actualBuilder.append(actualScanner.nextLine()).append("\n");
		}
		assertEquals(message, expectedBuilder.toString(), actualBuilder.toString());
	}
	
	/**
	 * Tests all objects in the array.
	 * 
	 * @param message		message to display if an error occurs
	 * @param expected	array with expected objects
	 * @param actual	array with actual objects
	 */
	public static void assertArrayEquals(Object[] expected, Object[] actual) {
		assertArrayEquals("", expected, actual);
	}

	/**
	 * Tests if both list of ioobjects are equal.
	 * 
	 * @param expected		expected value
	 * @param actual		actual value
	 */
	public static void assertEquals(String message, List<IOObject> expected, List<IOObject> actual) {
		assertSize(expected, actual);

		Iterator<IOObject> expectedIter = expected.iterator();
		Iterator<IOObject> actualIter = actual.iterator();

		while (expectedIter.hasNext() && actualIter.hasNext()) {
			IOObject expectedIOO = expectedIter.next();
			IOObject actualIOO = actualIter.next();
			assertEquals(message, expectedIOO, actualIOO);
		}

	}

	/**
	 * Tests if both list of ioobjects are equal.
	 * 
	 * @param expected		expected value
	 * @param actual		actual value
	 */
	public static void assertEquals(List<IOObject> expected, List<IOObject> actual) {
		RapidAssert.assertEquals("", expected, actual);
	}

	/**
	 * Tests if both lists of IOObjects have the same size.
	 * 
	 * @param expected
	 * @param actual
	 */
	public static void assertSize(List<IOObject> expected, List<IOObject> actual) {
		assertEquals(
				"Number of connected output ports in the process is not equal with the number of ioobjects contained in the same folder with the format 'processname-expected-port-1', 'processname-expected-port-2', ...",
				expected.size(), actual.size());
	}

	/**
	 * Tests if the two IOObjects are equal.
	 * 
	 * @param expectedIOO
	 * @param actualIOO
	 */
	public static void assertEquals(IOObject expectedIOO, IOObject actualIOO) {
		RapidAssert.assertEquals("", expectedIOO, actualIOO);
	}

	/**
	 * Tests if the two IOObjects are equal and appends the given message.
	 * 
	 * @param expectedIOO
	 * @param actualIOO
	 */
	public static void assertEquals(String message, IOObject expectedIOO, IOObject actualIOO) {

		/*
		 * Do not forget to add a newly supported class to the 
		 * ASSERTER_REGISTRY!!!
		 */
		List<Asserter> asserterList = ASSERTER_REGISTRY.getAsserterForObjects(expectedIOO, actualIOO);
		if (asserterList != null) {
			for (Asserter asserter : asserterList) {
				asserter.assertEquals(message, expectedIOO, actualIOO);
			}
		} else {
			throw new ComparisonFailure("Comparison of the two given IOObject classes " + expectedIOO.getClass() + " and " + actualIOO.getClass() + " is not supported. ",
					expectedIOO.toString(), actualIOO.toString());
		}

	}

	/**
	 * Tests the two examples by testing the value of the examples for every given attribute. 
	 * This method is sensitive to the attribute ordering.
	 * 
	 * @param message		message to display if an error occurs. If it contains "{0}" and "{1}", it will be replaced with the attribute name and attribute type, if an unequality occurs.
	 * @param expected		expected value
	 * @param actual		actual value
	 */
	public static void assertEquals(String message, Example expected, Example actual) {
		Iterator<Attribute> expectedAttributesToConsider = expected.getAttributes().allAttributes();
		Iterator<Attribute> actualAttributesToConsider = actual.getAttributes().allAttributes();
		while (expectedAttributesToConsider.hasNext() && actualAttributesToConsider.hasNext()) {
			Attribute a1 = expectedAttributesToConsider.next();
			Attribute a2 = actualAttributesToConsider.next();
			if (!a1.getName().equals(a2.getName())) {
				// this should have been detected by previous checks already
				throw new AssertionFailedError("Attribute ordering does not match: " + a1.getName() + "," + a2.getName());
			}
			if (a1.isNominal()) {
				Assert.assertEquals(MessageFormat.format(message, "nominal", a1.getName()), expected.getNominalValue(a1), actual.getNominalValue(a2));
			} else {
				assertEqualsWithRelativeErrorOrBothNaN(MessageFormat.format(message, "numerical", a1.getName()), expected.getValue(a1), actual.getValue(a2));
			}
		}
	}

	/**
	 * Tests if all attributes are equal. This method is sensitive to the attribute ordering.
	 * 
	 * Optionally compares the default values of the attributes. The default value is only relevant for sparse data rows, so it should
	 * not be compared for non-sparse data.
	 * 
	 * @param message		message to display if an error occurs
	 * @param expected		expected value
	 * @param actual		actual value
	 * @param compareDefaultValues specifies if the attributes default values should be compared.
	 */
	public static void assertEquals(String message, Attributes expected, Attributes actual, boolean compareDefaultValues) {
		Assert.assertEquals(message + " (number of attributes)", expected.allSize(), actual.allSize());
		Iterator<AttributeRole> expectedRoleIt = expected.allAttributeRoles();
		Iterator<AttributeRole> actualRoleIt = actual.allAttributeRoles();
		while (expectedRoleIt.hasNext()) {
			AttributeRole expectedRole = expectedRoleIt.next();
			AttributeRole actualRole = actualRoleIt.next();
			RapidAssert.assertEquals(message, expectedRole, actualRole, compareDefaultValues);
		}
	}
	
	public static void assertEquals(String message, ParameterValue expected, ParameterValue actual) {
		Assert.assertEquals(message + " - operator", expected.getOperator(), actual.getOperator());
		Assert.assertEquals(message + " - parameterKey", expected.getParameterKey(), actual.getParameterKey());
		Assert.assertEquals(message + " - parameterValue", expected.getParameterValue(), actual.getParameterValue());
	}
}
