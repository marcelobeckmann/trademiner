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

import junit.framework.Test;
import junit.framework.TestSuite;

import com.rapidminer.Process;
import com.rapidminer.RapidMiner;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.ParameterService;

/**
 * Extends the JUnit test case by a method for checking the output of an
 * process.
 * 
 * @author Marcin Skirzynski, Christian Lohmann, Tobias Beckers
 */
public abstract class OperatorDataSampleTest extends RapidMinerTestCase {

	protected String file;

	public OperatorDataSampleTest(String file) {
		super("sampleTest");
		this.file = file;
	}

	@Override
	public String getName() {
		return "Sample '" + file + "'";
	}

	public void sampleTest() throws Exception {
		File processFile = new File(ParameterService.getSourceRoot(), "test" + File.separator + file);
		if (!processFile.exists())
			throw new Exception("File '" + processFile.getAbsolutePath() + "' does not exist!");
		LogService.getGlobal().setVerbosityLevel(LogService.OFF);
		Process process = RapidMiner.readProcessFile(processFile);
		IOContainer output = process.run(new IOContainer(), LogService.OFF);
		checkOutput(output);
	}

	public abstract void checkOutput(IOContainer output) throws MissingIOObjectException;

	public static Test suite() throws Exception{
		//initializes Rapidminer first before any test is run 
		RapidMiner.init();
		LogService.getGlobal().setVerbosityLevel(LogService.OFF);
		
		TestSuite suite = new TestSuite("Sample test");
		
		// general
		suite.addTest(new ExampleSetDataSampleTest("ModelApplier.xml", "confidence(positive)", new double[] {0.3333333333333333, 0.3333333333333333, 0.3333333333333333}));
		
		// IO 
		
		// Generator
		suite.addTest(new ExampleSetDataSampleTest("IO" + File.separator + "Generator" + File.separator +  "ExampleSetGeneratorSum.xml", "label",new double[] {1.9528130464362032, -8.363305876037725, -3.1404801374055165}));
		suite.addTest(new ExampleSetDataSampleTest("IO" + File.separator + "Generator" + File.separator + "ExampleSetGeneratorSinus.xml", "label", new double[] {-0.053648489595151116, -1.7483138995837275, 0.18967612209762974}));
		suite.addTest(new ExampleSetDataSampleTest("IO" + File.separator + "Generator" + File.separator +  "ExampleSetGeneratorSimplePolynomialClassification.xml", "label",new String[] {"negative", "positive", "positive"}));
		suite.addTest(new ExampleSetDataSampleTest("IO" + File.separator + "Generator" + File.separator +  "ExampleSetGeneratorThreeRingClusters.xml", "label",new String[] {"first_ring", "core", "second_ring"}));

		suite.addTest(new ExampleSetDataSampleTest("IO" + File.separator + "Generator" + File.separator +  "NominalExampleSetGenerator.xml", "label",new String[] {"negative","positive", "positive"}));
		
		suite.addTest(new ExampleSetDataSampleTest("IO" + File.separator + "Generator" + File.separator +  "MassiveDataGenerator.xml", "label",new String[] {"negative","positive", "negative"}));
		
		suite.addTest(new ExampleSetDataSampleTest("IO" + File.separator + "Generator" + File.separator +  "MultipleLabelGenerator.xml", "label1",new String[] {"negative","positive", "positive"}));
		suite.addTest(new ExampleSetDataSampleTest("IO" + File.separator + "Generator" + File.separator +  "MultipleLabelGenerator.xml", "label2",new String[] {"positive","negative", "positive"}));
		suite.addTest(new ExampleSetDataSampleTest("IO" + File.separator + "Generator" + File.separator +  "MultipleLabelGenerator.xml", "label3",new String[] {"negative","negative", "negative"}));
		suite.addTest(new ExampleSetDataSampleTest("IO" + File.separator + "Generator" + File.separator +  "MultipleLabelGeneratorWithRegression.xml", "label1",new double[] {-14.463832742801474, 11.879309946870846, 15.043732722905334}));
		suite.addTest(new ExampleSetDataSampleTest("IO" + File.separator + "Generator" + File.separator +  "MultipleLabelGeneratorWithRegression.xml", "label2",new double[] {12.186104360682329, -1.448569467214801, 0.304010038276898}));
		suite.addTest(new ExampleSetDataSampleTest("IO" + File.separator + "Generator" + File.separator +  "MultipleLabelGeneratorWithRegression.xml", "label3",new double[] {51.438286531689805, 75.4127485170661, 29.08974474321256}));
		suite.addTest(new ExampleSetDataSampleTest("IO" + File.separator + "Examples" + File.separator + "BibTexExampleSource.xml", "bibtype", new String[] {"incollection", "book"}));
		suite.addTest(new AttributeWeightsDataSampleTest("IO" + File.separator + "Attributes" + File.separator + "AttributeWeightsWriter.xml",new String[] {"att1", "att2"}, new double[] {0.0, 1.0}));
		suite.addTest(new AttributeWeightsDataSampleTest("IO" + File.separator + "Attributes" + File.separator + "AttributeWeightsLoader.xml",new String[] {"att1", "att2"}, new double[] {0.0, 1.0}));
		suite.addTest(new IOConsumingDataSampleTest("Core" + File.separator + "IOMultiplierAttributeWeights.xml", 1));
		suite.addTest(new IOConsumingDataSampleTest("Core" + File.separator + "IOMultiplierExampleSet.xml", 1));
		suite.addTest(new IOConsumingDataSampleTest("Core" + File.separator + "IOConsumerAttributeWeights.xml", 1));
		suite.addTest(new IOConsumingDataSampleTest("Core" + File.separator + "IOConsumerExampleSet.xml", 1));
		suite.addTest(new IOConsumingDataSampleTest("Core" + File.separator + "IOConsumer_delete_one.xml", 1));
		suite.addTest(new ExampleSetDataSampleTest("IO" + File.separator + "Examples" + File.separator + "ArffExampleSource.xml", "label", new double[] {1.9528130464362032, -8.363305876037725, -3.1404801374055165}));

		// Learner in "Learner\Supervised"
		//					Bayes
		suite.addTest(new DistributionModelSampleDataTest("Learner" + File.separator + "Supervised" + File.separator + "Bayes" + File.separator +  "NaiveBayes.xml", new double[] {0.9,0.1666666666666666,0.0384615385,0.9782608696}));
		//					Functions
		suite.addTest(new SVMModelSampleDataTest("Learner" + File.separator + "Supervised" + File.separator + "Functions" + File.separator +  "EvoSVM.xml", new double[] {77.23214088668192,-7.332858952003095,536.0619409998441,234.7202021155585}));
		suite.addTest(new SVMModelSampleDataTest("Learner" + File.separator + "Supervised" + File.separator + "Functions" + File.separator +  "GaussianProcesses.xml", new double[] {-14.471909578199531,-5.723394720354495,21.791351540462667,6.7619388781725105,-8.572869269432962}));
		suite.addTest(new SVMModelSampleDataTest("Learner" + File.separator + "Supervised" + File.separator + "Functions" + File.separator +  "JMySVMLearner.xml", new double[] {-0.08466553720730552,-3.813066593713039,1.0499144813373242}));
		suite.addTest(new SVMModelSampleDataTest("Learner" + File.separator + "Supervised" + File.separator + "Functions" + File.separator +  "LibSVMLearner.xml", new double[] {1.0,-0.345457362826704,-0.32712327331360336,-0.3274193638596926}));
		suite.addTest(new SVMModelSampleDataTest("Learner" + File.separator + "Supervised" + File.separator + "Functions" + File.separator +  "PsoSVM.xml", new double[] {-0.6362404930321264,-0.1294041385736771,-1.0}));
		suite.addTest(new SVMModelSampleDataTest("Learner" + File.separator + "Supervised" + File.separator + "Functions" + File.separator +  "RVMLearner.xml", new double[] {-1.6367200978545418,2.8715457495411156}));
		suite.addTest(new SVMModelSampleDataTest("Learner" + File.separator + "Supervised" + File.separator + "Functions" + File.separator +  "MyKLRLearner.xml", new double[] {0.812589827620005,-0.37081139693030474,0.5000000001,-0.1291886031696953}));
   		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Functions" + File.separator +  "LinearRegression.xml", "prediction(label)", new double[] {0.49769388220887306,-0.48919864419717374,-0.38597602207893167,-0.6213089983982649}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Functions" + File.separator +  "LogisticRegression.xml", "confidence(negative)", new double[] {0.3935975586006201,0.6224411539423208,0.37876411715595226,0.37893116784114583}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Functions" + File.separator +  "NeuralNet.xml", "confidence(negative)", new double[] {0.3030661429430265,0.7153779840732098,0.2997422624229046,0.29921794684298986}));
		//					Lazy
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Lazy" + File.separator +  "AttributeBasedVote.xml", "prediction(label)", new double[] {-0.9974318698487723,-6.698643011401726,-0.1695184445644422,5.227419088571385}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Lazy" + File.separator +  "DefaultLearner.xml", "prediction(label)", new double[] {-0.09501652738425875,1.3171696991768367,0.6471053689374655,-0.09501652738425875}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Lazy" + File.separator +  "NearestNeighbors.xml", "prediction(label)", new String[] {"core","first_ring","core","core","first_ring"}));
		//					Meta
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Meta" + File.separator +  "AdaBoost.xml", "confidence(negative)", new double[] {0.5185185185185187, 0.5185185185185187, 0.5185185185185187, 0.5555555555555556}));
		//					Rules
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Rules" + File.separator +  "BasicRuleLearner.xml", "prediction(label)", new String[] {"cluster3", "cluster3", "cluster1"}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Rules" + File.separator +  "BestRuleInduction.xml", "prediction(label)", new String[] {"positive", "negative", "positive"}));
		//suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Rules" + File.separator +  "IteratingGSS.xml", "confidence(positive)", new double[] {1.0, 0.5423977609676378, 0.7528089887640449,1.0,0.40880069500944627}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Rules" + File.separator +  "OneR.xml", "prediction(label)", new String[] {"positive","negative","positive"}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Rules" + File.separator +  "RuleLearner.xml", "prediction(label)", new String[] {"positive","negative","negative"}));
		//					Meta
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Meta" + File.separator +  "AdaBoost.xml", "confidence(negative)", new double[] {0.5185185185185187, 0.5185185185185187, 0.5185185185185187, 0.5555555555555556}));
		//					Trees
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Trees" + File.separator +  "CHAID.xml", "confidence(negative)", new double[] {0.06666666666666667,0.9166666666666666,0.9166666666666666,0.06666666666666667,0.9166666666666666}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Trees" + File.separator +  "DecisionStump.xml", "confidence(negative)", new double[] {0.8260869565217391,0.8,0.2222222222222222,0.09090909090909091,0.8}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Trees" + File.separator +  "DecisionTree.xml", "confidence(core)", new double[] {0.0,0.9,0.8181818181818182,0.9}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Trees" + File.separator +  "ID3.xml", "confidence(negative)", new double[] {0.75,1.0,0.5714285714285714,0.0,0.5555555555555556}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Trees" + File.separator +  "ID3Numerical.xml", "confidence(negative)", new double[] {1.0,1.0,1.0,0.09090909090909091,1.0}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Trees" + File.separator +  "MultiCriterionDecisionStump.xml", "prediction(label)", new String[] {"negative","negative","negative","positive","negative","positive","negative","positive"}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Trees" + File.separator +  "RandomForest.xml", "prediction(label)", new String[] {"core","first_ring","core","first_ring","first_ring","first_ring","first_ring","second_ring"}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Trees" + File.separator +  "RandomTree.xml", "prediction(label)", new String[] {"first_ring","core","second_ring","core","first_ring","second_ring","second_ring","core"}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Trees" + File.separator +  "RelevanceTree.xml", "prediction(label)", new String[] {"second_ring","core","second_ring","core","first_ring","core","first_ring","core"}));
		//					Just a few WEKA-Operators
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Weka" + File.separator +  "W-J48.xml", "prediction(label)", new String[] {"first_ring","core","second_ring","core","first_ring","first_ring","first_ring","core"}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Weka" + File.separator +  "W-M5Rules.xml", "prediction(label)", new double[] {13894.524850835136,11294.9365366086,5380.763904916719,10444.78985699569,17353.230976287305,2936.2985714882197,13743.825210494664}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Supervised" + File.separator + "Weka" + File.separator +  "W-SimpleLinearRegression.xml", "prediction(label)", new double[] {15680.31318695116,5552.663075337349,6745.007140990247,3799.3466182731026,9563.151904535132,13100.833236244614,16460.532143142416}));
		
		
		//		Learners in "Learner\Unsupervised"
		//					Clustering
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Unsupervised" + File.separator + "Clustering" + File.separator +  "AgglomerativeClustering.xml", "cluster", new String[] {"id 203","id 209","id 209","id 209","id 206","id 206","id 203"}));
	    //ClusterModel2ExampleSet tested in the other Clusteringtests
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Unsupervised" + File.separator + "Clustering" + File.separator +  "DBScanClustering.xml", "cluster", new String[] {"1","2","2","2","3","3","1"}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Unsupervised" + File.separator + "Clustering" + File.separator +  "ExampleSet2ClusterModel.xml", "cluster", new String[] {"1","2","2","2","3","3","1"}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Unsupervised" + File.separator + "Clustering" + File.separator +  "KMeans.xml", "cluster", new String[] {"2","0","0","0","1","1","2"}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Unsupervised" + File.separator + "Clustering" + File.separator +  "KMedoids.xml", "cluster", new String[] {"1","0","0","0","0","0","1"}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Unsupervised" + File.separator + "Clustering" + File.separator +  "KernelKMeans.xml", "cluster", new String[] {"0","0","0","0","1","1","0"}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Unsupervised" + File.separator + "Clustering" + File.separator +  "RandomFlatClustering.xml", "cluster", new String[] {"1","1","1","1","1","1","0"}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Unsupervised" + File.separator + "Clustering" + File.separator +  "SupportVectorClustering.xml", "cluster", new String[] {"1","2","2","2","3","3","1"}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Unsupervised" + File.separator + "Clustering" + File.separator +  "TopDownClustering.xml", "cluster", new String[] {"cl.1","cl.0.1","cl.0.0.0.1","cl.0.0.0.0","cl.2","cl.3","cl.0.2"}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Unsupervised" + File.separator + "Clustering" + File.separator +  "TopDownRandomClustering.xml", "cluster", new String[] {"cl.1.1.1.0.0","cl.1.1.0.0.2","cl.0.1.0.1.2","cl.0.0.1.1.0","cl.0.1.1.1.1","cl.1.2","cl.1.1.0.1.0"}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Unsupervised" + File.separator + "Clustering" + File.separator +  "UPGMAClustering.xml", "cluster", new String[] {"0.0","0.0","0.1.0","0.1.0","0.1.0","0.1.0","0.1.1"}));
		//						Itemsets
		suite.addTest(new FrequentItemSetsDataSampleTest("Learner" + File.separator + "Unsupervised" + File.separator + "Itemsets" + File.separator +  "FPGrowth.xml", 0, new String[] {"att3", "att4", "att2", "att5", "att1"}));
		suite.addTest(new AssociationRuleGeneratorDataSampleTest("Learner" + File.separator + "Unsupervised" + File.separator + "Itemsets" + File.separator +  "AssociationRuleGenerator.xml", new double[] {1.0}));
				
		//					Weka
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Unsupervised" + File.separator + "Clustering" + File.separator +  "Weka" + File.separator + "W-SimpleKMeans.xml", "cluster", new String[] {"cluster1","cluster1","cluster0", "cluster0", "cluster0", "cluster0", "cluster0"}));
		suite.addTest(new ExampleSetDataSampleTest("Learner" + File.separator + "Unsupervised" + File.separator + "Clustering" + File.separator +  "Weka" + File.separator + "W-FarthestFirst.xml", "cluster", new String[] {"cluster0","cluster0","cluster1", "cluster1", "cluster1", "cluster1", "cluster1"}));
	

		// Meta
		//suite.addTest(new PerformanceDataSampleTest("Meta" + File.separator + "AverageBuilder_with_simple_validation.xml", new String[] { "accuracy", "precision", "recall", "AUC" }, new double[] { 0.5833333333333333,0.631578947368421,0.4,0.25333333333333335 }));
		suite.addTest(new AttributeWeightsDataSampleTest("Meta" + File.separator + "AverageBuilder_with_attribute_weighting.xml",new String[] {"att1","att2","att3","att4","att5"}, new double[] {0.0296060589130663, 0.45370765617859954, 1.0, 0.0428611612601767643, 0.1929268735189445}));
		suite.addTest(new ExampleSetDataSampleTest("Meta" + File.separator + "ClusterIteration.xml", "cluster", new double[] {0, 1, 0, 1, 0}));
		suite.addTest(new ExampleSetsDataSampleTest("Meta" + File.separator + "ExampleSetIterator.xml", "att1", new String[][] {{"true", "false", "true", "true", "false"}, {"true", "false", "false", "false", "true"}}));
		suite.addTest(new ExampleSetDataSampleTest("Meta" + File.separator +"ProcessEmbedder.xml", "prediction(label)", new double[] {-0.09501652738425875, 1.3171696991768367, 0.6471053689374655, -0.09501652738425875, 1.3171696991768367}));
		suite.addTest(new ExampleSetDataSampleTest("Meta" + File.separator +"MultipleLabelIterator.xml", "label", new String[] {"up", "down", "up", "up", "down"}));
		suite.addTest(new ExampleSetDataSampleTest("Meta" + File.separator +"OperatorEnabler.xml", "label", new double[] {1.6431634962865138, 1.9475844940566178, 2.218002556787387, 3.370940882491232, 3.4238369022633175}));
		suite.addTest(new ExampleSetDataSampleTest("Meta" + File.separator +"OperatorEnabler_disabled.xml", "label", new double[] {10.976406523218103, 5.818347061981138, 8.429759931297243, 14.801574008444156, 2.218002556787387}));
		suite.addTest(new ExampleSetDataSampleTest("Meta" + File.separator +"XVPrediction.xml", 1));
		// Meta/Parameter
		suite.addTest(new ParameterSetDataSampleTest("Meta" + File.separator +"Parameter" + File.separator + "EvolutionaryParameterOptimization.xml", 120.573));
		suite.addTest(new ParameterSetDataSampleTest("Meta" + File.separator +"Parameter" + File.separator + "GridParameterOptimization.xml", 111.421));
		suite.addTest(new ExampleSetDataSampleTest("Meta" + File.separator +"Parameter" + File.separator + "ParameterCloner.xml", 2));
		suite.addTest(new ParameterIterationDataSampleTest("Meta" + File.separator + "Parameter" + File.separator + "ParameterIteration.xml", 6, new int[] {100,150,200,100,150,200}, new double[]{-3.1875059108590245,24.15281909749461,-13.194648154237068,0.2711377216669635,0.41740095709333125,0.6066513878406207}));
		suite.addTest(new ExampleSetDataSampleTest("Meta" + File.separator +"Parameter" + File.separator + "ParameterSetter.xml", 1));
		suite.addTest(new ParameterSetDataSampleTest("Meta" + File.separator +"Parameter" + File.separator + "QuadraticParameterOptimization.xml", 111.421));
		
		/*
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
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "03_Sampling.xml", 34, 4));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "04_Discretization.xml", 208, 60));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "05_MinimalEntropyPartitioning.xml", 208, 21));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "06_ExampleFilter.xml", 1, 16));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "07_MissingValueReplenishment.xml", 40, 16));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "08_NoiseGenerator.xml", 200, 8));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "10_RemoveCorrelatedFeatures.xml", 208, 21));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "11_RemoveUselessAttributes.xml", 208, 38));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "12_FeatureGenerationByUser.xml", 200, 8));
		suite.addTest(new ExampleSetSampleTest("03_Preprocessing" + File.separator + "14_StratifiedSampling.xml", 40, 2));

		// Validation
		suite.addTest(new PerformanceSampleTest("04_Validation" + File.separator + "01_PerformanceEvaluator_Nominal.xml", new String[] { "accuracy" }, new double[] { 0.650000 }));
		suite.addTest(new PerformanceSampleTest("04_Validation" + File.separator + "02_PerformanceEvaluator_Regression.xml", new String[] { "relative_error" }, new double[] { 5.960858 }));
		suite.addTest(new PerformanceSampleTest("04_Validation" + File.separator + "03_XValidation_Numerical.xml", new String[] { "correlation" }, new double[] { 0.999077 }));
		suite.addTest(new PerformanceSampleTest("04_Validation" + File.separator + "04_XValidation_Nominal.xml", new String[] { "classification_error" }, new double[] { 0.275000 }));
		suite.addTest(new PerformanceSampleTest("04_Validation" + File.separator + "07_AreaUnderCurve.xml", new String[] { "AUC" }, new double[] { 0.970026 }));
		suite.addTest(new PerformanceSampleTest("04_Validation" + File.separator + "08_SimpleValidation.xml", new String[] { "accuracy" }, new double[] { 0.822222 }));

		// Features
		suite.addTest(new ExampleSetSampleTest("05_Features" + File.separator + "03_PrincipalComponents.xml", 150, 2));
		suite.addTest(new PerformanceSampleTest("05_Features" + File.separator + "09_FeatureSelectionFilter.xml", new String[] { "CorrelationFS" }, new double[] { 0.834587 }));
		suite.addTest(new PerformanceSampleTest("05_Features" + File.separator + "10_ForwardSelection.xml", new String[] { "root_mean_squared_error" }, new double[] { 37.495692 }));
		//suite.addTest(new PerformanceSampleTest("05_Features" + File.separator + "13_EvolutionaryWeighting.xml", new String[] { "accuracy", "precision", "recall" }, new double[] { 0.920000, 0.952941, 0.931034 }));
		suite.addTest(new PerformanceSampleTest("05_Features" + File.separator + "19_YAGGA.xml", new String[] { "root_relative_squared_error" }, new double[] { 0.000000 }));
		suite.addTest(new ExampleSetSampleTest("05_Features" + File.separator + "20_YAGGAResultAttributeSetting.xml", 200, 5));

		// Meta
		suite.addTest(new PerformanceSampleTest("07_Meta" + File.separator + "01_ParameterOptimization.xml", new String[] { "absolute_error" }, new double[] { 8.377223 }));
		suite.addTest(new SimpleSampleTest("07_Meta" + File.separator + "02_ParameterSetter.xml"));
		suite.addTest(new SimpleSampleTest("07_Meta" + File.separator + "04_LearningCurve.xml"));
		suite.addTest(new PerformanceSampleTest("07_Meta" + File.separator + "05_MultipleLabelLearning.xml", new String[] { "accuracy" }, new double[] { 0.912500 }));

		// Other
		suite.addTest(new ExampleSetSampleTest("09_Other" + File.separator + "02_Obfuscation.xml", 14, 4));

		*/
		
		
		return suite;
	}
	
	public static void main(String[] argv) throws Exception{
		// RapidMiner initialized only once (for performance reasons)
		LogService.getGlobal().setVerbosityLevel(LogService.OFF);
		RapidMiner.init();
		LogService.getGlobal().setVerbosityLevel(LogService.OFF);

		junit.textui.TestRunner.run(suite());
	}
	
	// TODO: remove this method if possible
	/*
	@SuppressWarnings("unchecked")
	private static Collection<Class<IOObject>> createClassCollection(Class[] classes) {
		List<Class<IOObject>> result = new LinkedList<Class<IOObject>>();
		for (Class clazz : classes) {
			result.add(clazz);
		}
		return result;
	}
	*/
}
