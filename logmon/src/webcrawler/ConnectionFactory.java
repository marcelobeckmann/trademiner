package webcrawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionFactory {
	/** Atributos basicos necessarios para efetuar uma conexao jdbc */
	public static  String URL = "jdbc:mysql://localhost/trademiner";
	public static  String DRIVER = "com.mysql.jdbc.Driver";
	public static  String USERNAME = "root";
	public static  String PASSWORD = "root";
	/**
	 * Instancia o driver
	 */
	static {
		
		//1 - o driver do banco tem que estar no buildpath

			try {
				Class.forName(DRIVER);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
	/** Obtem a conexao 
	 * @throws SQLException */
	public static Connection getConnection() throws SQLException  {
		//TODO Obter a conexao
		return DriverManager.getConnection(URL, USERNAME, PASSWORD);

	}

		
	
	public static void closeConnection(ResultSet rs, Statement stmt, Connection conn) {

		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}

	}

}
