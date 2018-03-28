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
package com.rapidminer.test.samples;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.rapidminer.Process;
import com.rapidminer.RepositoryProcessLocation;
import com.rapidminer.example.Attributes;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.repository.RepositoryLocation;
import com.rapidminer.test.TestContext;
import com.rapidminer.tools.LogService;

/**
 * Extends the JUnit test case by a method for checking the output of an
 * process.
 * 
 * @author Ingo Mierswa, Simon Fischer
 */
public abstract class SampleTest extends TestCase {

	private String file;

	public SampleTest(String file) {
		super("sampleTest");
		this.file = "//Samples/processes/"+file;
	}

	public String getName() {
		return "Sample '" + file + "'";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TestContext.get().initRapidMiner();
	}
	
	public void sampleTest() throws Exception {
		Process process = new RepositoryProcessLocation(new RepositoryLocation(file)).load(null);
		IOContainer output = process.run(new IOContainer(), LogService.OFF);
		checkOutput(output);
	}

	public abstract void checkOutput(IOContainer output) throws MissingIOObjectException;

	public static Test suite() {
		TestSuite suite = new TestSuite("Sample test");

//		// general
//		suite.addTest(new SimpleSampleTest("Empty"));

		// IO
//		suite.addTest(new ExampleSetSampleTest("01_IO/01_ExampleSource", 14, 4));
//		suite.addTest(new ExampleSetSampleTest("01_IO/02_ArffExampleSource", 150, 4));
//		suite.addTest(new ExampleSetSampleTest("01_IO/03_Sparse", 4, 30));
//		suite.addTest(new IOObjectSampleTest("01_IO/04_C45ExampleSource", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
//		suite.addTest(new ExampleSetSampleTest("01_IO/05_CSVExampleSource", 14, 4));
//		suite.addTest(new ExampleSetSampleTest("01_IO/08_ExampleSourceFromMultipleSources", 14, 4));
//		suite.addTest(new ExampleSetSampleTest("01_IO/11_ExampleSetWriter", 14, 4));
//		suite.addTest(new ExampleSetSampleTest("01_IO/12_ExampleSetWriterPredictions", 14, 4, new String[] { Attributes.PREDICTION_NAME }));
//		suite.addTest(new ExampleSetSampleTest("01_IO/13_ArffExampleSetWriter", 14, 4));
//		suite.addTest(new IOObjectSampleTest("01_IO/18_ModelWriter", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
//		suite.addTest(new ExampleSetSampleTest("01_IO/19_ModelLoader", 14, 4, new String[] { Attributes.PREDICTION_NAME }));

		// Learner
		suite.addTest(new IOObjectSampleTest("01_Learner/01_DecisionTree", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("01_Learner/02_LinearRegression", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("01_Learner/03_NeuralNetwork", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("01_Learner/04_NearestNeighbors", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("01_Learner/05_NaiveBayes", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("01_Learner/06_SupportVectorMachine", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("01_Learner/07_LogisticRegression", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("01_Learner/08_RuleLearning", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("01_Learner/09_Bagging", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("01_Learner/10_Boosting", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new ExampleSetSampleTest("01_Learner/11_ModelApplier", 14, 4, new String[] { Attributes.PREDICTION_NAME }));
		suite.addTest(new PerformanceSampleTest("01_Learner/13_AsymmetricCostLearning", new String[] { "accuracy" }, new double[] { 0.725000 }));

		// Preprocessing
		suite.addTest(new ExampleSetSampleTest("02_Preprocessing/01_Normalization", 150, 4));
		suite.addTest(new ExampleSetSampleTest("02_Preprocessing/02_IdTagging", 150, 4, new String[] { Attributes.ID_NAME }));
		suite.addTest(new ExampleSetSampleTest("02_Preprocessing/03_Sampling", 29, 4));
		suite.addTest(new ExampleSetSampleTest("02_Preprocessing/04_Discretization", 208, 60));
		suite.addTest(new ExampleSetSampleTest("02_Preprocessing/05_MinimalEntropyPartitioning", 208, 21));
		suite.addTest(new ExampleSetSampleTest("02_Preprocessing/06_ExampleFilter", 1, 16));
		suite.addTest(new ExampleSetSampleTest("02_Preprocessing/07_MissingValueReplenishment", 40, 16));
		suite.addTest(new ExampleSetSampleTest("02_Preprocessing/08_NoiseGenerator", 200, 8));
		suite.addTest(new ExampleSetSampleTest("02_Preprocessing/09_MergingAndRemoving", 8, 2));
		suite.addTest(new ExampleSetSampleTest("02_Preprocessing/10_RemoveCorrelatedFeatures", 208, 21));
		suite.addTest(new ExampleSetSampleTest("02_Preprocessing/11_RemoveUselessAttributes", 208, 38));
		suite.addTest(new ExampleSetSampleTest("02_Preprocessing/12_FeatureGenerationByUser", 200, 8));
		suite.addTest(new ExampleSetSampleTest("02_Preprocessing/14_StratifiedSampling", 40, 2));
		suite.addTest(new ExampleSetSampleTest("02_Preprocessing/19_AttributeSubsetPreprocessing", 14, 4));
		suite.addTest(new ExampleSetSampleTest("02_Preprocessing/22_CreateAndNameOutlierCluster", 200, 2));

		// Validation
		suite.addTest(new PerformanceSampleTest("03_Validation/01_PerformanceEvaluator_Nominal", new String[] { "accuracy" }, new double[] { 0.875000 }));
		suite.addTest(new PerformanceSampleTest("03_Validation/02_PerformanceEvaluator_Regression", new String[] { "relative_error_lenient" }, new double[] { 0.306774 }));
		suite.addTest(new PerformanceSampleTest("03_Validation/03_XValidation_Numerical", new String[] { "correlation" }, new double[] { 0.999077 }));		
		suite.addTest(new PerformanceSampleTest("03_Validation/04_XValidation_Nominal", new String[] { "classification_error" }, new double[] { 0.075000 }));
		suite.addTest(new PerformanceSampleTest("03_Validation/07_AreaUnderCurve", new String[] { "AUC" }, new double[] { 0.980017 }));
		suite.addTest(new PerformanceSampleTest("03_Validation/08_SimpleValidation", new String[] { "accuracy" }, new double[] { 0.866666 }));

		// Features
		suite.addTest(new ExampleSetSampleTest("04_Attributes/03_PrincipalComponents", 150, 2));
		suite.addTest(new PerformanceSampleTest("04_Attributes/09_FeatureSelectionFilter", new String[] { "CorrelationFS" }, new double[] { 0.834588 }));
		suite.addTest(new PerformanceSampleTest("04_Attributes/10_ForwardSelection", new String[] { "root_mean_squared_error" }, new double[] { 37.495692 }));
		suite.addTest(new PerformanceSampleTest("04_Attributes/19_YAGGA", new String[] { "root_relative_squared_error" }, new double[] { 0.0386407 }));
		suite.addTest(new ExampleSetSampleTest("04_Attributes/20_YAGGAResultAttributeSetting", 200, 10));

		// Meta
		suite.addTest(new PerformanceSampleTest("06_Meta/01_ParameterOptimization", new String[] { "absolute_error" }, new double[] { 8.377368 }));
		suite.addTest(new SimpleSampleTest("06_Meta/02_ParameterSetter"));
		suite.addTest(new SimpleSampleTest("06_Meta/04_LearningCurve"));
		suite.addTest(new PerformanceSampleTest("06_Meta/05_MultipleLabelLearning", new String[] { "accuracy" }, new double[] { 0.893333 }));

		// Other
		suite.addTest(new ExampleSetSampleTest("08_Other/02_Obfuscation", 14, 4));

		return suite;
	}
	
	// TODO: remove this method if possible
	@SuppressWarnings("unchecked")
	private static Collection<Class<IOObject>> createClassCollection(Class[] classes) {
		List<Class<IOObject>> result = new LinkedList<Class<IOObject>>();
		for (Class clazz : classes) {
			result.add(clazz);
		}
		return result;
	}
}
