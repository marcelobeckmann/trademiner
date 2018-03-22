package com.rapidminer.operator.trademiner.eval;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.operator.trademiner.util.ConnectionFactory;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;

import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class WekaPerformance extends Operator {

	private InputPort exampleSetInput = getInputPorts().createPort("example set");
	private OutputPort exampleSetOutput = getOutputPorts().createPort("example set");

	private Attribute label;

	private Attribute prediction;
	private int m_NumClasses;
	private Evaluation evaluation = new Evaluation();
	private String[] labels;
	private int labelCount[];
	private int totalLabelCount;

	public static final String PARAMETER_EXPERIMENT_DESCRIPTION = "experiment_description";
	public static final String PARAMETER_PREDICTION_ATTRIBUTE = "predictionAtt";
	public static final String PARAMETER_EXPERIMENT_SYMBOL = "experiment_symbol";

	public static final String PARAMETER_WRITE_ACC = "accuracy";
	public static final String PARAMETER_WRITE_RECALL = "recall";
	public static final String PARAMETER_WRITE_AUC = "AUC";
	public static final String PARAMETER_WRITE_FMEASURE = "f-measure";

	public static final String PARAMETER_WRITE_CONFUSION_MATRIX = "confusion_matrix";
	public static final String PARAMETER_WRITE_LABELS = "labels";
	public static final String PARAMETER_LISTOF_LABELS = "list_of_labels";
	public static final String PARAMETER_MAIN_ALGO = "main_algorithm";
	public static final String PARAMETER_DELTA = "delta";

	public static final String PARAMETER_USE_WEIGHTED_AVG = "use_weighted_avg";

	public static final String PARAMETER_DATABASE_URL = "database_url";

	public WekaPerformance(OperatorDescription description) {
		super(description);

		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput, exampleSetOutput, SetRelation.EQUAL) {

			@Override
			public ExampleSetMetaData modifyExampleSet(ExampleSetMetaData metaData) throws UndefinedParameterError {

				return metaData;
			}
		});
	}

	public String[] parseLabels() throws UndefinedParameterError {

		String parameterListLabels = getParameterAsString(PARAMETER_LISTOF_LABELS);
		String[] labels;
		if (parameterListLabels.length() == 0) {

			labels = label.getMapping().getValues().toArray(new String[] {});
		} else {
			labels = parameterListLabels.split(",");

		}
		return labels;
	}

	@Override
	public void doWork() throws OperatorException {

		ExampleSet inputSet = exampleSetInput.getData(ExampleSet.class);

		boolean writeLabels = getParameterAsBoolean(PARAMETER_WRITE_LABELS);
		boolean createConfusionMatrix = getParameterAsBoolean(PARAMETER_WRITE_CONFUSION_MATRIX);
		labels = parseLabels();
		// Calculate measures (Weka)
		calculateMeasures(inputSet);

		// Create the outputset
		ExampleSet outputSet = createStructure();
		MemoryExampleTable table = (MemoryExampleTable) outputSet.getExampleTable();
		DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_SPARSE_ARRAY, '.');
		DataRow row = null;

		// Create attribute variables
		Attribute idAttribute = outputSet.getAttributes().get("EXPERIMENT_ID");
		Attribute symbolAttribute = outputSet.getAttributes().get("SYMBOL");
		row = factory.create(table.getNumberOfAttributes());
		String symbollValue = getParameterAsString(PARAMETER_EXPERIMENT_SYMBOL);

		int id = createExperimentDescrition();

		// Start to fill the columns
		row.set(idAttribute, id);
		row.set(symbolAttribute, symbolAttribute.getMapping().mapString(symbollValue));

		if (writeLabels) {
			Attribute alabels = outputSet.getAttributes().get("LABELS");
			row.set(alabels, alabels.getMapping().mapString(Arrays.toString(labels)));
		}
		if (createConfusionMatrix) {

			for (int i = 0; i < labels.length; ++i) {
				for (int j = 0; j < labels.length; ++j) {
					Attribute att = outputSet.getAttributes().get(labels[i] + " x " + labels[j]);
					row.set(att, evaluation.m_ConfusionMatrix[i][j]);
				}

			}

		}
		// ACC
		if (getParameterAsBoolean(PARAMETER_WRITE_ACC)) {
			double sum[] = new double[labels.length];
			for (int i = 0; i < labels.length; i++) {
				Attribute att = outputSet.getAttributes().get("acc " + labels[i]);
				double value = evaluation.accuracy(i);
				sum[i] = value;
				row.set(att, value);
			}

			Attribute att = outputSet.getAttributes().get("avg acc");
			row.set(att, calculateAvg(sum));
		}

		// RECALL
		if (getParameterAsBoolean(PARAMETER_WRITE_RECALL)) {
			double sum[] = new double[labels.length];
			for (int i = 0; i < labels.length; i++) {
				Attribute att = outputSet.getAttributes().get("recall " + labels[i]);

				double value = evaluation.recall(i);
				sum[i] = value;
				row.set(att, value);

			}
			Attribute att = outputSet.getAttributes().get("avg recall");
			row.set(att, calculateAvg(sum));
		}
		// AUC
		if (getParameterAsBoolean(PARAMETER_WRITE_AUC)) {
			double sum[] = new double[labels.length];
			for (int i = 0; i < labels.length; i++) {
				Attribute att = outputSet.getAttributes().get("auc " + labels[i]);
				double value = evaluation.areaUnderROC(i);
				sum[i] = value;
				row.set(att, value);

			}
			Attribute att = outputSet.getAttributes().get("avg auc");
			row.set(att, calculateAvg(sum));
		}
		// FMEASURE
		if (getParameterAsBoolean(PARAMETER_WRITE_FMEASURE)) {
			double sum[] = new double[labels.length];
			for (int i = 0; i < labels.length; i++) {
				Attribute att = outputSet.getAttributes().get("fmeasure " + labels[i]);
				double value = evaluation.fMeasure(i);
				sum[i] = value;
				row.set(att, value);

			}
			Attribute att = outputSet.getAttributes().get("avg fmeasure");
			row.set(att, calculateAvg(sum));
		}

		table.addDataRow(row);
		exampleSetOutput.deliver(outputSet);

	}

	/**
	 * Calculates the weighted average.
	 */
	private double calculateAvg(double[] values) {

		boolean useWeightedAvg = getParameterAsBoolean(PARAMETER_USE_WEIGHTED_AVG);
		if (useWeightedAvg) {
			double wavg = 0;
			for (int i = 0; i < labelCount.length; i++) {
				wavg += values[i] * labelCount[i];
			}
			return wavg / totalLabelCount;

		} else {

			double sum = 0;
			for (int i = 0; i < labelCount.length; i++) {
				sum += values[i] ;
			}
			return sum / labelCount.length;
		}
	}

	private ExampleSet calculateMeasures(ExampleSet inputSet) throws UserError, UndefinedParameterError {

		label = inputSet.getAttributes().getLabel();
		m_NumClasses = labels.length;
		prediction = inputSet.getAttributes().get(getParameterAsString(PARAMETER_PREDICTION_ATTRIBUTE));
		evaluation.m_ConfusionMatrix = new double[m_NumClasses][m_NumClasses];
		labelCount = new int[labels.length];
		int i = 0;
		for (Example example : inputSet) {

			evaluation.evaluateModelOnceAndRecordPrediction(example,i);
			int labelValue = (int)Double.parseDouble( example.getValueAsString(label));
			int labelIndex = Arrays.binarySearch(labels, String.valueOf(labelValue));
			// This was necessary because the exampleset behave different when come straight from Apply Model operator,
			// than to be read from CSV and roles applied
			if (labelIndex < 0) {
				labelIndex = labelValue;
			}
			labelCount[labelIndex] = labelCount[labelIndex] + 1;
			i++;
		}
		totalLabelCount = 0;
		for (i = 0; i < labelCount.length; i++) {
			totalLabelCount += labelCount[i];

		}
		if (inputSet.size() != totalLabelCount) {
			throw new IllegalStateException("Example set size (" + inputSet + ") is different from label count (" + totalLabelCount + ")");

		}
		return inputSet;
	}

	private ExampleSet createStructure() throws OperatorException {

		List<Attribute> attributes = new ArrayList<Attribute>();

		attributes.add(AttributeFactory.createAttribute("EXPERIMENT_ID", Ontology.INTEGER));

		Attribute textAttribute = AttributeFactory.createAttribute("SYMBOL", Ontology.STRING);
		attributes.add(textAttribute);

		boolean writeLabels = getParameterAsBoolean(PARAMETER_WRITE_LABELS);
		if (writeLabels) {
			attributes.add(AttributeFactory.createAttribute("LABELS", Ontology.STRING));
		}

		boolean createConfusionMatrix = getParameterAsBoolean(PARAMETER_WRITE_CONFUSION_MATRIX);
		if (createConfusionMatrix) {
			for (int i = 0; i < labels.length; i++) {
				for (int j = 0; j < labels.length; j++) {
					attributes.add(AttributeFactory.createAttribute(labels[i] + " x " + labels[j], Ontology.REAL));
				}

			}
		}
		// ACC
		if (getParameterAsBoolean(PARAMETER_WRITE_ACC)) {
			for (int i = 0; i < labels.length; i++) {
				attributes.add(AttributeFactory.createAttribute("acc " + labels[i], Ontology.REAL));
			}
			attributes.add(AttributeFactory.createAttribute("avg acc", Ontology.REAL));
		}
		// RECALL
		if (getParameterAsBoolean(PARAMETER_WRITE_RECALL)) {
			for (int i = 0; i < labels.length; i++) {
				attributes.add(AttributeFactory.createAttribute("recall " + labels[i], Ontology.REAL));
			}
			attributes.add(AttributeFactory.createAttribute("avg recall", Ontology.REAL));
		}

		// AUC
		if (getParameterAsBoolean(PARAMETER_WRITE_AUC)) {
			for (int i = 0; i < labels.length; i++) {
				attributes.add(AttributeFactory.createAttribute("auc " + labels[i], Ontology.REAL));
			}
			attributes.add(AttributeFactory.createAttribute("avg auc", Ontology.REAL));
		}
		// FMEASURE
		if (getParameterAsBoolean(PARAMETER_WRITE_FMEASURE)) {
			for (int i = 0; i < labels.length; i++) {
				attributes.add(AttributeFactory.createAttribute("fmeasure " + labels[i], Ontology.REAL));
			}
			attributes.add(AttributeFactory.createAttribute("avg fmeasure", Ontology.REAL));
		}

		MemoryExampleTable table = new MemoryExampleTable(attributes);
		ExampleSet exampleSet = table.createExampleSet();

		return exampleSet;

	}

	private int createExperimentDescrition() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		int id = 0;
		try {
			String description = getParameterAsString(PARAMETER_EXPERIMENT_DESCRIPTION);
			ConnectionFactory.URL = getParameterAsString(PARAMETER_DATABASE_URL);
			conn = ConnectionFactory.getConnection();
			stmt = conn.createStatement();

			rs = stmt.executeQuery("select id from experiment where description='" + description + "'");

			if (rs.next()) {
				return rs.getInt(1);

			}
			String algo = getParameterAsString(PARAMETER_MAIN_ALGO);
			String strDelta = getParameterAsString(PARAMETER_DELTA);
			int delta = 0;
			try {
				delta = Integer.parseInt(strDelta);
			} catch (Exception e) {
			}

			String sql = "insert into experiment (description, delta, algo) values ('" + description + "'," + delta + ",'" + algo + "')";

			stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

			rs = stmt.getGeneratedKeys();
			rs.next();
			id = rs.getInt(1);

		}

		catch (Exception e) {
			e.printStackTrace();
		} finally {
			ConnectionFactory.closeConnection(rs, stmt, conn);
		}

		return id;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		types.add(new ParameterTypeString(PARAMETER_EXPERIMENT_DESCRIPTION, "The experiment description.",
				"%{algo_}, %{ticket_}, %{window_size_},%{experiment_description_}", false));
		ParameterType type = new ParameterTypeString(PARAMETER_PREDICTION_ATTRIBUTE, "The attribute name which contains the algorithm prediction.",
				"prediction", false);
		types.add(type);
		types.add(new ParameterTypeString(PARAMETER_EXPERIMENT_SYMBOL, "The experiment stock symbol.", "%{symbol_}"));

		types.add(new ParameterTypeBoolean(PARAMETER_WRITE_ACC, "Writes accuracy.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_WRITE_RECALL, "Writes recall.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_WRITE_AUC, "Writes auc.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_WRITE_FMEASURE, "Writes f-measure.", false));

		types.add(new ParameterTypeBoolean(PARAMETER_WRITE_LABELS,
				"Defines if the labels used in classification will be added to the performance exampleset.", true, true));

		types.add(new ParameterTypeString(PARAMETER_LISTOF_LABELS, "The experiment labels.", "-2,0,2", true));

		types.add(new ParameterTypeBoolean(PARAMETER_WRITE_CONFUSION_MATRIX,
				"Defines if a confusion matrix will be created with the performance exampleset.", true, false));

		types.add(new ParameterTypeString(PARAMETER_MAIN_ALGO, "The main algo used in the experiment.", "%{algo_}", true));

		types.add(new ParameterTypeString(PARAMETER_DELTA, "The time in minutes of the window size.", "%{delta_}", true));

		// TODO GENERATE A SECOND RESULTSET INSTEAD TO WRITE STRAIGHT AWAY TO DB
		types.add(new ParameterTypeString(PARAMETER_DATABASE_URL, "The trademiner database url (necessary to write the experiment description).",
				"jdbc:mysql://%{db_host}/trademiner", true));

		types.add(new ParameterTypeBoolean(PARAMETER_USE_WEIGHTED_AVG,
				"Use weighted average to calculate the overal f-measure, auc, and acuracy ammong the classes", false));

		return types;
	}

	// TODO USE Weka class instead of this one
	public class Evaluation {

		public Evaluation() {
			m_Predictions = new FastVector();

		}

		public FastVector m_Predictions;
		/** Array for storing the confusion matrix. */
		public double[][] m_ConfusionMatrix;

		public void evaluateModelOnceAndRecordPrediction(Example example , int i) {

			int actualIndex;
			try {
				int actualValue = (int)Double.parseDouble(example.getValueAsString(label));
				actualIndex = Arrays.binarySearch(labels, String.valueOf(actualValue));
				// This was necessary because the exampleset behave different when come straight from Apply Model operator,
				// than to be read from CSV and roles applied
				if (actualIndex < 0) {
					actualIndex = actualValue;

				}
			} catch (Exception e)
			{
				throw new IllegalStateException("Error parsing label value " + label + " at row "+i);
				
			}
			double[] dist = new double[m_NumClasses];
			int predictionIndex;
			try {
				int predictionValue = (int)Double.parseDouble(example.getValueAsString(prediction));
				predictionIndex = Arrays.binarySearch(labels, String.valueOf(predictionValue));
				// Same here
				if (predictionIndex < 0) {
					predictionIndex = predictionValue;
				}

			} catch (Exception e)
			{
				throw new IllegalStateException("Error parsing prediction value " + prediction + " at row "+i);
				
			}

			try {

				// Update for AUC
				dist[predictionIndex] = 1;
				m_Predictions.addElement(new NominalPrediction(actualIndex, dist, 1));

				// Update the confusion matrix
				m_ConfusionMatrix[actualIndex][predictionIndex] += 1;
			} catch (RuntimeException e) {
				System.out.println("### evaluateModelOnceAndRecordPrediction - Numclasses: " + m_NumClasses + ", actual index: " + actualIndex
						+ ", prediction: " + prediction + " prediction index: " + predictionIndex 
						+ ", Labels: " + Arrays.toString(labels) + ", actualIndex: " + actualIndex );
				throw e;
			}

		}

		public double fMeasure(int classIndex) {

			double precision = precision(classIndex);
			double recall = recall(classIndex);
			if ((precision + recall) == 0) {
				return 0;
			}
			return 2 * precision * recall / (precision + recall);
		}

		public double recall(int classIndex) {

			return truePositiveRate(classIndex);
		}

		public double areaUnderROC(int classIndex) {

			// Check if any predictions have been collected
			if (m_Predictions == null) {
				return Instance.missingValue();
			} else {
				ThresholdCurve tc = new ThresholdCurve();
				Instances result = tc.getCurve(m_Predictions, classIndex);
				return ThresholdCurve.getROCArea(result);

			}
		}

		public double precision(int classIndex) {

			double correct = 0, total = 0;
			for (int i = 0; i < m_NumClasses; i++) {
				if (i == classIndex) {
					correct += m_ConfusionMatrix[i][classIndex];
				}
				total += m_ConfusionMatrix[i][classIndex];
			}
			if (total == 0) {
				return 0;
			}
			return correct / total;
		}

		public double truePositiveRate(int classIndex) {

			double correct = 0, total = 0;
			for (int j = 0; j < m_NumClasses; j++) {
				if (j == classIndex) {
					correct += m_ConfusionMatrix[classIndex][j];
				}
				total += m_ConfusionMatrix[classIndex][j];
			}
			if (total == 0) {
				return 0;
			}
			return correct / total;
		}

		public double accuracy(int classIndex) {
			double dividend = this.numTruePositives(classIndex) + numTrueNegatives(classIndex);

			double divisor = dividend + numFalsePositives(classIndex) + numFalseNegatives(classIndex);

			if (divisor == 0)
				return 0;
			return dividend / divisor;
		}

		public double numTrueNegatives(int classIndex) {

			double correct = 0;
			for (int i = 0; i < m_NumClasses; i++) {
				if (i != classIndex) {
					for (int j = 0; j < m_NumClasses; j++) {
						if (j != classIndex) {
							correct += m_ConfusionMatrix[i][j];
						}
					}
				}
			}
			return correct;
		}

		public double numTruePositives(int classIndex) {

			double correct = 0;
			for (int j = 0; j < m_NumClasses; j++) {
				if (j == classIndex) {
					correct += m_ConfusionMatrix[classIndex][j];
				}
			}
			return correct;
		}

		public double numFalsePositives(int classIndex) {

			double incorrect = 0;
			for (int i = 0; i < m_NumClasses; i++) {
				if (i != classIndex) {
					for (int j = 0; j < m_NumClasses; j++) {
						if (j == classIndex) {
							incorrect += m_ConfusionMatrix[i][j];
						}
					}
				}
			}
			return incorrect;
		}

		public double falsePositiveRate(int classIndex) {

			double incorrect = 0, total = 0;
			for (int i = 0; i < m_NumClasses; i++) {
				if (i != classIndex) {
					for (int j = 0; j < m_NumClasses; j++) {
						if (j == classIndex) {
							incorrect += m_ConfusionMatrix[i][j];
						}
						total += m_ConfusionMatrix[i][j];
					}
				}
			}
			if (total == 0) {
				return 0;
			}
			return incorrect / total;
		}

		public double numFalseNegatives(int classIndex) {

			double incorrect = 0;
			for (int i = 0; i < m_NumClasses; i++) {
				if (i == classIndex) {
					for (int j = 0; j < m_NumClasses; j++) {
						if (j != classIndex) {
							incorrect += m_ConfusionMatrix[i][j];
						}
					}
				}
			}
			return incorrect;
		}

	}
}
