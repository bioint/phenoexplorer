package edu.isi.pfindr.learn.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import cc.mallet.types.Instance;
import edu.isi.pfindr.learn.util.ReadProperties;
import edu.isi.pfindr.learn.util.ResultSetUtil;

/*
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**  
 * DatabaseQueryInterface.java 
 * Contains Utility functions and API to the back-end database. It is called from all places in the 
 * program that need to access the database. Retrieves the SQL and returns the result
 * 
 * @author  Shefali Sharma
 *
 */ 

public class DatabaseQueryInterface {

	public DatabaseQueryInterface(){}
	static private Logger logger = Logger.getLogger("AppLogging");   

	public static Connection getDatabaseConnection(ServletContext context) throws SQLException, ClassNotFoundException {

		Connection conn = null;
		try{
		String dburl, dbusername, driverclass, dbpassword;
		if(context == null){ //Get the database connection parameters from the properties file
			Properties properties = ReadProperties.readDatabaseProperties();
			driverclass = (String)properties.get("driverclass");
			dburl = (String)properties.get("dbUrl");
			dbusername = (String)properties.get("dbUserName");
			dbpassword = (String)properties.get("dbPassword");
		}else{ //Get the database connection parameters from the servlet context
			driverclass = context.getInitParameter("driverclass"); 
			dburl=context.getInitParameter("dbUrl");
		    dbusername=context.getInitParameter("dbUserName");
		    dbpassword=context.getInitParameter("dbPassword");
		}
		Class.forName(driverclass);
		conn = DriverManager.getConnection(dburl, dbusername, dbpassword);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw e;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			throw e;
		}
		return conn;
	}

	public static int createEmptyTempTable(Connection dbConn, List<String> columnList, String tmpTableName ){
		int rowSize = 0;
		try{ 
			dropTempTable(dbConn, tmpTableName);
			String sql = "CREATE TABLE " + tmpTableName + "(";
			for(int i = 0; i < columnList.size(); i++ ){
				logger.info("Column name value : "+ columnList.get(i));

				//TODO: change this to get the actual data types
				sql += columnList.get(i) + " " + "VARCHAR(255) ";
				if (i != columnList.size()-1)
					sql += ",";
			}
			sql += ");";
			rowSize = executeUpdate (dbConn, sql);
		} catch( Exception e){
			System.err.println("Exception" + e.getMessage());
			throw new RuntimeException(e); 
		}/*finally{
			try{
				if(dbConn != null){
					if(!dbConn.isClosed())
						dbConn.close();
					dbConn = null;
				}
				logger.info("Database connection terminated successfully !!");	
			} catch(SQLException sqle) {
				logger.error("SQL-Exception when closing the connection: "+ sqle.getMessage(),sqle);
				sqle.printStackTrace();
			}
		}*/
		return rowSize;
	}

	public static int createTempTable(Connection dbConn, String sql, String tmpTableName, List<String> indexColumns) {

		int numRows = 0;
		try{ 
			StringBuilder createSql = new StringBuilder();
			logger.info("tmpTableName: " + tmpTableName);

			//drop temp table that will probably be the same session (appended session id)
			dropTempTable(dbConn, tmpTableName);

			//create temp table
			createSql.append("CREATE TABLE ").append(tmpTableName).append(" ").append(sql).append(" ");

			logger.info("createSql: " + createSql.toString());

			numRows = executeUpdate (dbConn, createSql.toString()); //Number of rows in the temp table

			//Add indexes
			if(indexColumns != null && indexColumns.size() > 0){
				createSql.setLength(0);
				for (String colName : indexColumns){
					createSql.append(" CREATE INDEX idx_"+tmpTableName+"_"+colName+" ON " + tmpTableName + " USING btree("+ colName + ");"); 
					logger.info("value of createSql is: "+ createSql.toString());
					executeUpdate (dbConn, createSql.toString());
					createSql.setLength(0);
				}
			}
		} catch( Exception e){
			System.err.println("Exception" + e.getMessage());
			e.printStackTrace();
		}/*finally{
			try{
				if(dbConn != null){
					if(!dbConn.isClosed())
						dbConn.close();
					dbConn = null;
				}
			} catch(SQLException sqle) {
				sqle.printStackTrace();
			}catch( Exception e){
				System.err.println("Exception" + e.getMessage());
				e.printStackTrace();
			}
		}*/
		return numRows;
	}


	public static void dropTempTable(Connection dbConn, String tmpTableName) throws RuntimeException{
		executeUpdate (dbConn, "DROP TABLE IF EXISTS " + tmpTableName + "; ");
	}

	private static int executeUpdate(Connection dbConn, String sql) throws RuntimeException{
		int numRows = 0;
		Statement stmt = null;
		try{ 
			if(dbConn != null) {
				stmt = dbConn.createStatement(); 
				numRows = stmt.executeUpdate (sql);
			}else{
				System.err.println("Cannot update the database, connection is closed");
				return -1;
			}
		}catch( SQLException sqle){
			throw new RuntimeException(sqle);
		}catch ( Exception e){
			throw new RuntimeException(e);
		}
		return numRows;
	}

	public static ResultSet executeQuery(Connection dbConn,String sql ) throws Exception
	{
		Statement stmt = null;
		ResultSet rs = null;
		if(dbConn != null) {
			stmt = dbConn.createStatement(); 
			rs = stmt.executeQuery (sql);
		} else{
			System.err.println("Cannot execute query, connection is closed");
		}
		//logger.info("Size of rs is: "+ rs.getFetchSize());
		return rs;
	}

	private static ResultSet executeQueryForLargeResult(Connection dbConn, String sql ) throws Exception
	{
		Statement stmt = null;
		ResultSet rs = null;

		if(dbConn != null) {
			stmt = dbConn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(Integer.MIN_VALUE);
			rs = stmt.executeQuery (sql);
		} else{
			System.err.println("Cannot execute query to database inside executeQueryWithPreparedStatmentForLargeResult, connection is closed");
		}
		return rs;
	}

	public static int[] executeBatch( Connection dbConn, List<List<String>> rowList, List<String> columnList, 
			String tableName ) throws Exception{

		Statement stmt = null;
		int[] updateCounts = new int[rowList.size()]; 
		try{ 
			StringBuilder batchSQL = new StringBuilder();

			if(dbConn != null) {
				stmt = dbConn.createStatement(); 

				for(int i = 0; i < rowList.size(); i++ ){
					batchSQL.setLength(0);
					batchSQL.append("INSERT INTO " + tableName + "(");

					for(int j = 0; j < columnList.size(); j++ ){
						batchSQL.append(columnList.get(j)) ;
						if (j != columnList.size()-1)
							batchSQL.append( ",");
					}
					batchSQL.append(") VALUES (");

					for(int j = 0; j < columnList.size(); j++ ){
						batchSQL.append("'"+ rowList.get(i).get(j) + "'");
						if (j != columnList.size()-1)
							batchSQL.append( ",");
					}

					batchSQL.append(");");

					stmt.addBatch(batchSQL.toString());
				}
				updateCounts = stmt.executeBatch();
			}else{
				System.err.println("Cannot update the database, connection is closed");
				return null;
			}
		}catch( SQLException sqle){
			throw new RuntimeException(sqle);
		}catch ( Exception e){
			throw new RuntimeException(e);
		}/*finally{
			try{
				if(stmt != null){
					stmt.close();
					stmt = null;
				}
			}catch (SQLException sqle) {
				System.err.println("SQL-Exception when closing the statement: "+ sqle.getMessage());
				sqle.printStackTrace();
			}
			try{
				if(dbConn != null){
					if(!dbConn.isClosed())
						dbConn.close();
					dbConn = null;
				}
			} catch(SQLException sqle) {
				sqle.printStackTrace();
			}
		}*/

		return updateCounts;
	}

	public static int executeQueryReturnCount(Connection dbConn, String sql, String countColumn){
		int count = 0;
		ResultSet rs = null;
		try{
			rs = executeQuery(dbConn, sql);
			count =  ResultSetUtil.resultSetAsCount(rs, countColumn);
		} catch( SQLException sqle){
			System.err.println("SQL-Exception" + sqle.getMessage());
			//throw exception back to the caller so eventually the connection is closed
			throw new RuntimeException(sqle); 
		} catch( Exception e){
			System.err.println("Exception" + e.getMessage());
			throw new RuntimeException(e); 
		}/*finally{

			if(rs != null){
				try{
					if(rs.getStatement() != null){
						rs.getStatement().close();
					}
					rs.close();
					rs = null;
				}catch (SQLException sqle) {
					System.err.println("SQL-Exception when closing the statement: "+ sqle.getMessage());
					sqle.printStackTrace();
				}
			}
			try{
				if(dbConn != null){
					if(!dbConn.isClosed())
						dbConn.close();
					dbConn = null;
				}	
			} catch(SQLException sqle) {
				sqle.printStackTrace();
			}
		}*/
		return count;
	}

	public static List<Object> executeQueryReturnOneColumnAsList(Connection dbConn, String sql){
		ResultSet rs = null;
		List<Object> rows;
		try{ 
			rs = executeQuery(dbConn, sql);
			rows =  ResultSetUtil.resultSetToOneColumnAsList(rs);
		} catch( SQLException sqle){
			System.err.println("SQL-Exception" + sqle.getMessage());
			//throw exception back to the caller so eventually the connection is closed
			throw new RuntimeException(sqle); 
		} catch( Exception e){
			System.err.println("Exception" + e.getMessage());
			throw new RuntimeException(e); 
		}/*finally{
			if(rs != null){
				try{
					if(rs.getStatement() != null){
						rs.getStatement().close();
					}
					rs.close();
					rs = null;
				}catch (SQLException sqle) {
					System.err.println("SQL-Exception when closing the statement: "+ sqle.getMessage());
					sqle.printStackTrace();
				}
			}
			try{
				if(dbConn != null){
					if(!dbConn.isClosed())
						dbConn.close();
					dbConn = null;
				}
			} catch(SQLException sqle) {
				sqle.printStackTrace();
			}
		}*/
		return rows;
	}

	public static String executeQueryReturnOneColumnAsString(Connection dbConn, String sql){
		ResultSet rs = null;
		String rows;
		try{ 
			rs = executeQuery(dbConn, sql);
			rows =  ResultSetUtil.resultSetToOneColumnAsCommamaSeparatedString(rs);
		} catch( SQLException sqle){
			System.err.println("SQL-Exception" + sqle.getMessage());
			//throw exception back to the caller so eventually the connection is closed
			throw new RuntimeException(sqle); 
		} catch( Exception e){
			System.err.println("Exception" + e.getMessage());
			throw new RuntimeException(e); 
		}/*finally{
			if(rs != null){
				try{
					if(rs.getStatement() != null){
						rs.getStatement().close();
					}
					rs.close();
					rs = null;
				}catch (SQLException sqle) {
					System.err.println("SQL-Exception when closing the statement: "+ sqle.getMessage());
					sqle.printStackTrace();
				}
			}
			try{
				if(dbConn != null){
					if(!dbConn.isClosed())
						dbConn.close();
					dbConn = null;
				}
			} catch(SQLException sqle) {
				sqle.printStackTrace();
			}
		}*/
		return rows;
	}

	public static List<List<Object>> executeQueryReturnListofList(Connection dbConn, String sql){
		ResultSet rs = null;
		List<List<Object>> rows;
		try{ 
			rs = executeQuery(dbConn, sql);
			rows =  ResultSetUtil.resultSetToListOfList(rs);
		} catch( SQLException sqle){
			System.err.println("SQL-Exception" + sqle.getMessage());
			//throw exception back to the caller so eventually the connection is closed
			throw new RuntimeException(sqle); 
		} catch( Exception e){
			System.err.println("Exception" + e.getMessage());
			throw new RuntimeException(e); 
		}/*finally{
			if(rs != null){
				try{
					if(rs.getStatement() != null){
						rs.getStatement().close();
					}
					rs.close();
					rs = null;
				}catch (SQLException sqle) {
					System.err.println("SQL-Exception when closing the statement: "+ sqle.getMessage());
					sqle.printStackTrace();
				}
			}
			try{
				if(dbConn != null){
					if(!dbConn.isClosed())
						dbConn.close();
					dbConn = null;
				}
			} catch(SQLException sqle) {
				sqle.printStackTrace();
			}
		}*/
		return rows;
	}

	public static List<Instance> executeQueryReturnMalletInstanceList(Connection dbConn, String sql){
		ResultSet rs = null;
		List<Instance> rows;
		try{ 
			rs = executeQuery(dbConn, sql);
			rows =  ResultSetUtil.resultSetToMalletInstanceList(rs);
		} catch( SQLException sqle){
			System.err.println("SQL-Exception" + sqle.getMessage());
			//throw exception back to the caller so eventually the connection is closed
			throw new RuntimeException(sqle); 
		} catch( Exception e){
			System.err.println("Exception" + e.getMessage());
			throw new RuntimeException(e); 
		}
		return rows;
	}

	public static void executeQuerySaveToFile(Connection dbConn, String sql, String filePath, String fileName){
		ResultSet rs = null;
		try{ 
			rs = executeQuery(dbConn, sql);
			ResultSetUtil.saveResultsetAsFile(rs, filePath, fileName);
		} catch( SQLException sqle){
			System.err.println("SQL-Exception" + sqle.getMessage());
			//throw exception back to the caller so eventually the connection is closed
			throw new RuntimeException(sqle); 
		} catch( Exception e){
			System.err.println("Exception" + e.getMessage());
			throw new RuntimeException(e); 
		}
		/*
		finally{
			try{
				if(rs != null && !(rs.isClosed())){
					if(rs.getStatement() != null){
						rs.getStatement().close();
						rs.close();
						rs = null;
					}
				}
			}catch (SQLException sqle) {
				System.err.println("SQL-Exception when closing the statement: "+ sqle.getMessage());
				sqle.printStackTrace();
			}

			try{
				if(dbConn != null){
					if(!dbConn.isClosed())
						dbConn.close();
					dbConn = null;
				}
			} catch(SQLException sqle) {
				sqle.printStackTrace();
			}
		}
		 */
	}


	public static void executeLargeQuerySaveToFile(Connection dbConn, String sql, String filePath, String fileName){
		ResultSet rs = null;
		try{ 
			rs = executeQueryForLargeResult(dbConn, sql);
			ResultSetUtil.saveResultsetAsFile(rs, filePath, fileName);
		} catch( SQLException sqle){
			System.err.println("SQL-Exception" + sqle.getMessage());
			//throw exception back to the caller so eventually the connection is closed
			throw new RuntimeException(sqle); 
		} catch( Exception e){
			System.err.println("Exception" + e.getMessage());
			throw new RuntimeException(e); 
		}
		/*
		finally{

			if(rs != null){
				try{
					if(rs.getStatement() != null){
						rs.getStatement().close();
					}
					rs.close();
					rs = null;
				}catch (SQLException sqle) {
					System.err.println("SQL-Exception when closing the statement: "+ sqle.getMessage());
					sqle.printStackTrace();
				}
			}
			try{
				if(dbConn != null){
					if(!dbConn.isClosed())
						dbConn.close();
					dbConn = null;
				}
			} catch(SQLException sqle) {
				sqle.printStackTrace();
			}
		}
		 */
	}

	/*public static void executeQueryDropTempTable(String tmpTableName){
		Connection dbConn = null;
		try{ 
			dbConn = getConnection();
			dropTempTable(dbConn, tmpTableName);
		}catch ( Exception e){
			throw new RuntimeException(e);
		}finally{
			try{
				if(dbConn != null){
					if(!dbConn.isClosed())
						dbConn.close();
					dbConn = null;
				}
			} catch(SQLException sqle) {
				sqle.printStackTrace();
			}
		}
	}*/

	public static ResultSet executeQueryWithPreparedStatmentForLargeResult(Connection dbConn, String sql) 
			throws RuntimeException
			{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try{ 
			if(dbConn != null) {
				stmt = dbConn.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY, 
						java.sql.ResultSet.CONCUR_READ_ONLY);
				stmt.setFetchSize(Integer.MIN_VALUE);
				rs = stmt.executeQuery ();
			} else{
				System.err.println("Cannot execute query to database inside executeQueryWithPreparedStatmentForLargeResult, connection is closed");
			}
		} catch( SQLException sqle){
			//throw exception back to the caller and eventually to the servlet so that the connection is closed
			throw new RuntimeException(sqle); 
		} catch( Exception e){
			throw new RuntimeException(e); 
		}/*finally{
			if(rs != null){
				try{
					if(rs.getStatement() != null){
						rs.getStatement().close();
					}
					rs.close();
					rs = null;
				}catch (SQLException sqle) {
					System.err.println("SQL-Exception when closing the statement: "+ sqle.getMessage());
					sqle.printStackTrace();
				}
			}
			try{
				if(dbConn != null){
					if(!dbConn.isClosed())
						dbConn.close();
					dbConn = null;
				}
			} catch(SQLException sqle) {
				sqle.printStackTrace();
			}
		}*/
		return rs;
			}

}
