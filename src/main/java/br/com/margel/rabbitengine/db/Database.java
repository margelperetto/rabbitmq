package br.com.margel.rabbitengine.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
	private static Database instance;
	
	public static synchronized Database getInstance() throws SQLException {
		if(instance == null) {
			instance = new Database();
			createTables();
		}
		return instance;
	}

	private Database() {/**/}

	public Connection newConnection() throws SQLException {
		Properties props = new Properties();
		props.setProperty("user","postgres");
		props.setProperty("password","123");
		return DriverManager.getConnection("jdbc:postgresql://localhost/postgres", props);
	}
	
	private static void createTables() throws SQLException {
		try(
				Connection conn = instance.newConnection();
				PreparedStatement ppst = conn.prepareStatement(CREATE_TABLES);
				){
			ppst.execute();
		}
	}
	
	private static final String CREATE_TABLES = 
			"CREATE TABLE IF NOT EXISTS JOBS("
					+ "	OID VARCHAR PRIMARY KEY,"
					+ "	QUEUE VARCHAR,"
					+ "	MSG VARCHAR,"
					+ "	PRIORITY INT,"
					+ "	FINISHED BOOLEAN,"
					+ "	DTCREATED BIGINT,"
					+ "	DTRECEIVED BIGINT"
					+ ");";
}
