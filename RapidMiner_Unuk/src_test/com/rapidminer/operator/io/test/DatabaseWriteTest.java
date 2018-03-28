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
package com.rapidminer.operator.io.test;

import static junit.framework.Assert.assertEquals;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.DatabaseDataReader;
import com.rapidminer.operator.io.DatabaseExampleSetWriter;
import com.rapidminer.repository.Entry;
import com.rapidminer.repository.IOObjectEntry;
import com.rapidminer.repository.RepositoryException;
import com.rapidminer.repository.RepositoryLocation;
import com.rapidminer.test.TestContext;
import com.rapidminer.test_utils.RapidAssert;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.jdbc.DatabaseHandler;

/**
 * 
 * @author Simon Fischer
 *
 */
public class DatabaseWriteTest {

	private static final String TABLE_NAME = "unit_test_table";
	private static final String TEST_DATA_LOCATION = "//Samples/data/Labor-Negotiations";
	private static final String TEST_REPOS_DATE_LOCATION = "//Samples/data/";
	
	/** The labor negotiations data set. */
	private ExampleSet laborNegotiationsExampleSet;
	
	/** Data set containing with infinite values. */
	@SuppressWarnings("unused")
	private ExampleSet infinityExampleSet;

	private static class DatabaseRef {
		private final String url, user, password;
		private String driverClass;
		private DatabaseRef(String url, String user, String password, String driverClass) {
			super();
			this.url = url;
			this.user = user;
			this.password = password;
			this.setDriverClass(driverClass);
		}
		public String getUrl() {
			return url;
		}
		public String getPassword() {
			return password;
		}
		public String getUser() {
			return user;
		}
		public void setDriverClass(String driverClass) {
			this.driverClass = driverClass;
		}
		/** May be null for bundled drivers. */
		public String getDriverClass() {
			return driverClass;
		}
	}
	
	private static final String TEST_DB_SERVER = "192.168.1.10";
	
	private static final DatabaseRef DB_SQL_SERVER = new DatabaseRef("jdbc:jtds:sqlserver://"+TEST_DB_SERVER+":1433/junit", "junit", "junit", null);// "net.sourceforge.jtds.jdbc.Driver");
	private static final DatabaseRef DB_MY_SQL = new DatabaseRef("jdbc:mysql://"+TEST_DB_SERVER+":3306/junit", "junit", "junit", null); // "com.mysql.jdbc.Driver");
	private static final DatabaseRef DB_ORACLE = new DatabaseRef("jdbc:oracle:thin:@"+TEST_DB_SERVER+":1521: ", "junit", "junit", "oracle.jdbc.driver.OracleDriver");
//	private static final DatabaseRef DB_ORACLE = new DatabaseRef("jdbc:oracle:thin:@"+TEST_DB_SERVER+":1521: ", "junit", "junit", null);
	private static final DatabaseRef DB_INGRES = new DatabaseRef("jdbc:ingres://192.168.1.7:28047/demodb", "ingres", "vw2010", null);
	
	@Before
	public void setUp() throws Exception {
		TestContext.get().initRapidMiner(); // for read database operator
		final Entry entry = new RepositoryLocation(TEST_DATA_LOCATION).locateEntry();
		this.laborNegotiationsExampleSet = (ExampleSet) ((IOObjectEntry)entry).retrieveData(null);
		// Make all non-special: Will be dropped by DB anyway
		final Iterator<AttributeRole> allAttributeRoles = laborNegotiationsExampleSet.getAttributes().allAttributeRoles();
		while (allAttributeRoles.hasNext()) {
			allAttributeRoles.next().setSpecial(null);
		}

		
		Attribute pos = AttributeFactory.createAttribute("pos", Ontology.NUMERICAL);
		Attribute neg = AttributeFactory.createAttribute("neg", Ontology.NUMERICAL);
		Attribute nan = AttributeFactory.createAttribute("nan", Ontology.NUMERICAL);
		Attribute one = AttributeFactory.createAttribute("one", Ontology.NUMERICAL);
		MemoryExampleTable table = new MemoryExampleTable(pos, neg, nan, one);
		table.addDataRow(new DoubleArrayDataRow(new double[] {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN, 1d}));
		infinityExampleSet = table.createExampleSet();
	}
	
	@Test
	public void testCreateTableMicrosoftSQLServer() throws SQLException, OperatorException, ClassNotFoundException, OperatorCreationException {
		testCreateTable(DB_SQL_SERVER);
	}

	@Test
	public void testCreateTableMySQL() throws SQLException, OperatorException, ClassNotFoundException, OperatorCreationException {
		testCreateTable(DB_MY_SQL);
	}

	@Test
	public void testCreateTableOracle() throws SQLException, OperatorException, ClassNotFoundException, OperatorCreationException {
		testCreateTable(DB_ORACLE);
	}

	@Test@Ignore
	public void testCreateTableIngres() throws SQLException, OperatorException, ClassNotFoundException, OperatorCreationException {
		testCreateTable(DB_INGRES);
	}
	
	@Test
	public void testWriteOperator() throws OperatorCreationException, OperatorException {
		DatabaseExampleSetWriter writer = OperatorService.createOperator(DatabaseExampleSetWriter.class);
		writer.setParameter(DatabaseHandler.PARAMETER_DATABASE_SYSTEM, "MySQL");
		writer.setParameter(DatabaseHandler.PARAMETER_DEFINE_CONNECTION, DatabaseHandler.CONNECTION_MODES[DatabaseHandler.CONNECTION_MODE_URL]);
		writer.setParameter(DatabaseHandler.PARAMETER_DATABASE_URL, DB_MY_SQL.getUrl());
		writer.setParameter(DatabaseHandler.PARAMETER_USERNAME, DB_MY_SQL.getUser());
		writer.setParameter(DatabaseHandler.PARAMETER_PASSWORD, DB_MY_SQL.getPassword());
		writer.setParameter(DatabaseExampleSetWriter.PARAMETER_OVERWRITE_MODE, DatabaseHandler.OVERWRITE_MODES[DatabaseHandler.OVERWRITE_MODE_OVERWRITE]);
		
		writer.setParameter(DatabaseHandler.PARAMETER_TABLE_NAME, "LaborNegotiationOp");
		writer.write(laborNegotiationsExampleSet);
	}
	
	@Test
	public void testWriteOperatorGetGeneratedKeys() throws OperatorCreationException, OperatorException {
		String testTableName = "test_get_gen_keys_back";
		// Delete existing entries
		DatabaseHandler handler;
		try {
			handler = DatabaseHandler.getConnectedDatabaseHandler(DB_MY_SQL.getUrl(), DB_MY_SQL.getUser(), DB_MY_SQL.getPassword());
			handler.emptyTable(testTableName);
		} catch (SQLException e1) {
			throw new OperatorException("can not delete table", e1);
		}
		// Use Iris with out the id attribute
		Entry entry =null;
		ExampleSet eSet =null;
		try {
			entry = new RepositoryLocation(TEST_REPOS_DATE_LOCATION+"Iris").locateEntry();
			 eSet = (ExampleSet) ((IOObjectEntry)entry).retrieveData(null);
		} catch (RepositoryException e) {
			throw new OperatorException("can not access repository", e);
		}
		Attribute idAttribute = eSet.getAttributes().get("id");
		// remove id attribute
		eSet.getAttributes().remove(idAttribute);
		
		DatabaseExampleSetWriter writer = OperatorService.createOperator(DatabaseExampleSetWriter.class);
		writer.setParameter(DatabaseHandler.PARAMETER_DATABASE_SYSTEM, "MySQL");
		writer.setParameter(DatabaseHandler.PARAMETER_DEFINE_CONNECTION, DatabaseHandler.CONNECTION_MODES[DatabaseHandler.CONNECTION_MODE_URL]);
		writer.setParameter(DatabaseHandler.PARAMETER_DATABASE_URL, DB_MY_SQL.getUrl());
		writer.setParameter(DatabaseHandler.PARAMETER_USERNAME, DB_MY_SQL.getUser());
		writer.setParameter(DatabaseHandler.PARAMETER_PASSWORD, DB_MY_SQL.getPassword());
		writer.setParameter(DatabaseHandler.PARAMETER_TABLE_NAME, testTableName);
		writer.setParameter(DatabaseExampleSetWriter.PARAMETER_OVERWRITE_MODE, DatabaseHandler.OVERWRITE_MODES[DatabaseHandler.OVERWRITE_MODE_APPEND]);
		writer.setParameter(DatabaseExampleSetWriter.PARAMETER_GET_GENERATED_PRIMARY_KEYS, Boolean.TRUE.toString());
		writer.setParameter(DatabaseExampleSetWriter.PARAMETER_GENERATED_KEYS_ATTRIBUTE_NAME, "id");
		ExampleSet result = writer.write(eSet);
		

		DatabaseDataReader reader = OperatorService.createOperator(DatabaseDataReader.class);		
		reader.setParameter(DatabaseHandler.PARAMETER_DATABASE_SYSTEM, "MySQL");
		reader.setParameter(DatabaseHandler.PARAMETER_DEFINE_CONNECTION, DatabaseHandler.CONNECTION_MODES[DatabaseHandler.CONNECTION_MODE_URL]);
		reader.setParameter(DatabaseHandler.PARAMETER_DATABASE_URL, DB_MY_SQL.getUrl());
		reader.setParameter(DatabaseHandler.PARAMETER_USERNAME, DB_MY_SQL.getUser());
		reader.setParameter(DatabaseHandler.PARAMETER_PASSWORD, DB_MY_SQL.getPassword());
		reader.setParameter(DatabaseHandler.PARAMETER_TABLE_NAME, testTableName);
		reader.setParameter(DatabaseHandler.PARAMETER_DEFINE_QUERY, DatabaseHandler.QUERY_MODES[DatabaseHandler.QUERY_TABLE]);
		ExampleSet dbSet = reader.read();
		
		
		assertEquals(result.size(), dbSet.size());
		
		Attribute resultAtt = result.getAttributes().get("id");
		Attribute dbAtt = dbSet.getAttributes().get("id");
		
		// compare results
		Iterator<Example> resultIt = result.iterator();
		Iterator<Example> dbSetIt = dbSet.iterator();
		while (resultIt.hasNext()){
			Example resultEx = resultIt.next();
			Example dbEx = dbSetIt.next();
			assertEquals(resultEx.getValue(resultAtt), dbEx.getValue(dbAtt));
		}
	}

	@Test
	public void testReadOperator() throws OperatorCreationException, OperatorException {
		DatabaseDataReader reader = OperatorService.createOperator(DatabaseDataReader.class);		
		reader.setParameter(DatabaseHandler.PARAMETER_DATABASE_SYSTEM, "MySQL");
		reader.setParameter(DatabaseHandler.PARAMETER_DEFINE_CONNECTION, DatabaseHandler.CONNECTION_MODES[DatabaseHandler.CONNECTION_MODE_URL]);
		reader.setParameter(DatabaseHandler.PARAMETER_DATABASE_URL, DB_MY_SQL.getUrl());
		reader.setParameter(DatabaseHandler.PARAMETER_USERNAME, DB_MY_SQL.getUser());
		reader.setParameter(DatabaseHandler.PARAMETER_PASSWORD, DB_MY_SQL.getPassword());
		reader.setParameter(DatabaseHandler.PARAMETER_DEFINE_QUERY, DatabaseHandler.QUERY_MODES[DatabaseHandler.QUERY_TABLE]);
		reader.setParameter(DatabaseHandler.PARAMETER_TABLE_NAME, "LaborNegotiationOp");
		ExampleSet exampleSet = reader.read();
		assertEquals(40, exampleSet.size());
		assertEquals(17, exampleSet.getAttributes().size());
		RapidAssert.assertEquals("labor negotiations", exampleSet, laborNegotiationsExampleSet);
	}

	private void testCreateTable(DatabaseRef connection) throws SQLException, OperatorException, ClassNotFoundException, OperatorCreationException {
		testCreateTable(connection, laborNegotiationsExampleSet, "labor");
		//testCreateTable(connection, infinityExampleSet, "infinity");
	}
	
	private void testCreateTable(DatabaseRef connection, ExampleSet testSet, String testSetName) throws SQLException, OperatorException, ClassNotFoundException, OperatorCreationException {
		final String driverClass = connection.getDriverClass();
		if (driverClass != null) {
			Class.forName(driverClass);
		}
		DatabaseHandler handler = DatabaseHandler.getConnectedDatabaseHandler(connection.getUrl(), connection.getUser(), connection.getPassword());
		String tableName = TABLE_NAME+"_"+testSetName;
		
		try {
			handler.dropTable(tableName);
		} catch (SQLException e) {}
		//statement.executeUpdate(statementCreator.makeDropStatement(tableName));		
		handler.createTable(testSet, tableName, DatabaseHandler.OVERWRITE_MODE_OVERWRITE, true, -1);
		
		DatabaseDataReader readOp = OperatorService.createOperator(DatabaseDataReader.class);
		readOp.setParameter(DatabaseHandler.PARAMETER_DEFINE_CONNECTION, DatabaseHandler.CONNECTION_MODES[DatabaseHandler.CONNECTION_MODE_URL]);
		readOp.setParameter(DatabaseHandler.PARAMETER_DATABASE_URL, connection.getUrl());
		readOp.setParameter(DatabaseHandler.PARAMETER_USERNAME, connection.getUser());
		readOp.setParameter(DatabaseHandler.PARAMETER_PASSWORD, connection.getPassword());
		readOp.setParameter(DatabaseHandler.PARAMETER_DEFINE_QUERY, DatabaseHandler.QUERY_MODES[DatabaseHandler.QUERY_TABLE]);
		readOp.setParameter(DatabaseHandler.PARAMETER_TABLE_NAME, tableName);
		ExampleSet result = readOp.read();
		
		testSet = (ExampleSet)testSet.clone();
		adaptTestSet(connection, testSet);
		
		RapidAssert.assertEquals(testSetName, testSet, result);
	}

	/**
	 * <p>Modifies the test set to match the particularities of the connection.</p>
	 * 
	 * <p>For example the oracle database doesn't know the INTEGER or REAL data types, and thus
	 * the testset is modified before comparing to contain only NUMERICALs instead of subtypes of NUMERICAL.
	 * 
	 * @param connection
	 * @param testSet
	 */
	private void adaptTestSet(DatabaseRef connection, ExampleSet testSet) {
		if (connection.getUrl().contains("oracle")) {
			List<Attribute> removableAttributes = new LinkedList<Attribute>();
			List<Attribute> newAttributes = new LinkedList<Attribute>();
			for (Attribute oldAttribute : testSet.getAttributes()) {
				if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(oldAttribute.getValueType(), Ontology.NUMERICAL)) {
					Attribute newAttribute = AttributeFactory.changeValueType(oldAttribute, Ontology.NUMERICAL);
					removableAttributes.add(oldAttribute);
					newAttributes.add(newAttribute);
				}
			}
			Iterator<Attribute> oldIt = removableAttributes.iterator();
			Iterator<Attribute> newIt = newAttributes.iterator();
			while (oldIt.hasNext()) {
				testSet.getAttributes().replace(oldIt.next(), newIt.next());
			}
		} else {
			// do nothing
		}
	}	
}
