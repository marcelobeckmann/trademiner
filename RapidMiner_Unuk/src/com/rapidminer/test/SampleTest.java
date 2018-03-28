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
package com.rapidminer.test;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.Process;
import com.rapidminer.RapidMiner;
import com.rapidminer.example.Attributes;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.ParameterService;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Extends the JUnit test case by a method for checking the output of an
 * process.
 * 
 * @author Ingo Mierswa, Simon Fischer
 */
public abstract class SampleTest extends RapidMinerTestCase {

	private String file;

	public SampleTest(String file) {
		super("sampleTest");
		this.file = file;
	}

	public String getName() {
		return "Sample '" + file + "'";
	}

	public void sampleTest() throws Exception {
		File processFile = new File(ParameterService.getSourceRoot(), "sample" + File.separator + file);
		if (!processFile.exists())
			throw new Exception("File '" + processFile.getAbsolutePath() + "' does not exist!");
		LogService.getGlobal().setVerbosityLevel(LogService.OFF);
		Process process = RapidMiner.readProcessFile(processFile);
		IOContainer output = process.run(new IOContainer(), LogService.OFF);
		checkOutput(output);
	}

	public abstract void checkOutput(IOContainer output) throws MissingIOObjectException;

	public static Test suite() {
		TestSuite suite = new TestSuite("Sample test");

		// general
		suite.addTest(new SimpleSampleTest("Empty.xml"));

		// IO
		suite.addTest(new ExampleSetSampleTest("01_IO" + File.separator + "01_ExampleSource.xml", 14, 4));
		suite.addTest(new ExampleSetSampleTest("01_IO" + File.separator + "02_ArffExampleSource.xml", 150, 4));
		suite.addTest(new ExampleSetSampleTest("01_IO" + File.separator + "03_Sparse.xml", 4, 30));
		suite.addTest(new IOObjectSampleTest("01_IO" + File.separator + "04_C45ExampleSource.xml", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new ExampleSetSampleTest("01_IO" + File.separator + "05_CSVExampleSource.xml", 14, 4));
		suite.addTest(new ExampleSetSampleTest("01_IO" + File.separator + "08_ExampleSourceFromMultipleSources.xml", 14, 4));
		suite.addTest(new ExampleSetSampleTest("01_IO" + File.separator + "11_ExampleSetWriter.xml", 14, 4));
		suite.addTest(new ExampleSetSampleTest("01_IO" + File.separator + "12_ExampleSetWriterPredictions.xml", 14, 4, new String[] { Attributes.PREDICTION_NAME }));
		suite.addTest(new ExampleSetSampleTest("01_IO" + File.separator + "13_ArffExampleSetWriter.xml", 14, 4));
		suite.addTest(new IOObjectSampleTest("01_IO" + File.separator + "18_ModelWriter.xml", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new ExampleSetSampleTest("01_IO" + File.separator + "19_ModelLoader.xml", 14, 4, new String[] { Attributes.PREDICTION_NAME }));

		// Learner
		suite.addTest(new IOObjectSampleTest("02_Learner" + File.separator + "01_DecisionTree.xml", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("02_Learner" + File.separator + "02_LinearRegression.xml", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("02_Learner" + File.separator + "03_NeuralNetwork.xml", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("02_Learner" + File.separator + "04_NearestNeighbors.xml", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("02_Learner" + File.separator + "05_NaiveBayes.xml", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("02_Learner" + File.separator + "06_SupportVectorMachine.xml", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("02_Learner" + File.separator + "07_LogisticRegression.xml", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("02_Learner" + File.separator + "08_RuleLearning.xml", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("02_Learner" + File.separator + "09_Bagging.xml", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new IOObjectSampleTest("02_Learner" + File.separator + "10_Boosting.xml", createClassCollection(new Class[] { com.rapidminer.operator.Model.class })));
		suite.addTest(new ExampleSetSampleTest("02_Learner" + File.separator + "11_ModelApplier.xml", 14, 4, new String[] { Attributes.PREDICTION_NAME }));
		suite.addTest(new PerformanceSampleTest("02_Learner" + File.separator + "13_AsymmetricCostLearning.xml", new String[] { "accuracy" }, new double[] { 0.725000 }));

		// Preprocessing
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "01_Normalization.xml", 150, 4));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "02_IdTagging.xml", 150, 4, new String[] { Attributes.ID_NAME }));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "03_Sampling.xml", 29, 4));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "04_Discretization.xml", 208, 60));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "05_MinimalEntropyPartitioning.xml", 208, 21));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "06_ExampleFilter.xml", 1, 16));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "07_MissingValueReplenishment.xml", 40, 16));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "08_NoiseGenerator.xml", 200, 8));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "09_MergingAndRemoving.xml", 8, 2));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "10_RemoveCorrelatedFeatures.xml", 208, 21));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "11_RemoveUselessAttributes.xml", 208, 38));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "12_FeatureGenerationByUser.xml", 200, 8));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "14_StratifiedSampling.xml", 40, 2));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "19_AttributeSubsetPreprocessing.xml", 14, 4));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "22_CreateAndNameOutlierCluster.xml", 200, 2));

		// Validation
		suite.addTest(new PerformanceSampleTest("04_Validation" + File.separator + "01_PerformanceEvaluator_Nominal.xml", new String[] { "accuracy" }, new double[] { 0.875000 }));
		suite.addTest(new PerformanceSampleTest("04_Validation" + File.separator + "02_PerformanceEvaluator_Regression.xml", new String[] { "relative_error_lenient" }, new double[] { 0.306774 }));
		suite.addTest(new PerformanceSampleTest("04_Validation" + File.separator + "03_XValidation_Numerical.xml", new String[] { "correlation" }, new double[] { 0.999077 }));		
		suite.addTest(new PerformanceSampleTest("04_Validation" + File.separator + "04_XValidation_Nominal.xml", new String[] { "classification_error" }, new double[] { 0.075000 }));
		suite.addTest(new PerformanceSampleTest("04_Validation" + File.separator + "07_AreaUnderCurve.xml", new String[] { "AUC" }, new double[] { 0.980017 }));
		suite.addTest(new PerformanceSampleTest("04_Validation" + File.separator + "08_SimpleValidation.xml", new String[] { "accuracy" }, new double[] { 0.866666 }));

		// Features
		suite.addTest(new ExampleSetSampleTest("05_Features" + File.separator + "03_PrincipalComponents.xml", 150, 2));
		suite.addTest(new PerformanceSampleTest("05_Features" + File.separator + "09_FeatureSelectionFilter.xml", new String[] { "CorrelationFS" }, new double[] { 0.834588 }));
		suite.addTest(new PerformanceSampleTest("05_Features" + File.separator + "10_ForwardSelection.xml", new String[] { "root_mean_squared_error" }, new double[] { 37.495692 }));
		suite.addTest(new PerformanceSampleTest("05_Features" + File.separator + "19_YAGGA.xml", new String[] { "root_relative_squared_error" }, new double[] { 0.0386407 }));
		suite.addTest(new ExampleSetSampleTest("05_Features" + File.separator + "20_YAGGAResultAttributeSetting.xml", 200, 10));

		// Meta
		suite.addTest(new PerformanceSampleTest("07_Meta" + File.separator + "01_ParameterOptimization.xml", new String[] { "absolute_error" }, new double[] { 8.377368 }));
		suite.addTest(new SimpleSampleTest("07_Meta" + File.separator + "02_ParameterSetter.xml"));
		suite.addTest(new SimpleSampleTest("07_Meta" + File.separator + "04_LearningCurve.xml"));
		suite.addTest(new PerformanceSampleTest("07_Meta" + File.separator + "05_MultipleLabelLearning.xml", new String[] { "accuracy" }, new double[] { 0.893333 }));

		// Other
		suite.addTest(new ExampleSetSampleTest("09_Other" + File.separator + "02_Obfuscation.xml", 14, 4));

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
