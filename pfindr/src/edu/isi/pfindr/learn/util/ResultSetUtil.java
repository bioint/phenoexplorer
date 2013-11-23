package edu.isi.pfindr.learn.util;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cc.mallet.types.Instance;

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
 * * ResultSetUtil.java 
 * 
 * Utility functions to handle the ResultSet returned from a SQL query to a database
 * 
 * @author sharma@isi.edu 
 * 
 */

public class ResultSetUtil {

	static private Logger logger = Logger.getLogger("AppLogging");    
	/*
	 * Converts the ResultSet returned from the query to a java : <List<List<Objects>> 
	 */
	public static List<List<Object>> resultSetToListOfList(ResultSet rs) throws Exception{ 

		if(rs == null)
			return null;
		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns = metaData.getColumnCount();
			List<Object> columnNames = new ArrayList<Object>(numberOfColumns);
			// Get the column names
			for (int column = 0; column < numberOfColumns; column++) {
				columnNames.add((metaData.getColumnLabel(column + 1)));
			}
			// Get all rows, but first make the first row the column names
			List<List<Object>> rows = new ArrayList<List<Object>>();
			rows.add(columnNames);
			List<Object> newRow;
			while (rs.next()) {
				newRow = new ArrayList<Object>(numberOfColumns);
				for (int i = 1; i <= numberOfColumns; i++) {
					//	System.out.println(" Inside resultSetToListOfList each column");	
					newRow.add( (rs.getString(i) == null) ? "null" : rs.getString(i));
				}
				rows.add(newRow);
			}
			return rows;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e); 
		}
	}
	
	public static List<Instance> resultSetToMalletInstanceList(ResultSet rs) throws Exception{ 

		if(rs == null)
			return null;
		try {
			//ResultSetMetaData metaData = rs.getMetaData();
			//int numberOfColumns = metaData.getColumnCount();
			List<Instance> instList = new ArrayList<Instance>();
			/*Instance(java.lang.Object data,
	                java.lang.Object target,
	                java.lang.Object name,
	                java.lang.Object source)*/
			/*"SELECT '" + description + "','" + descriptionExpanded + "', variable_desc_org, variable_desc_expanded, '0' FROM " +
			"dbgap_variable_expanded_stem_token_bi "*/
			StringBuilder data, target, source;
			int i=0;
			while (rs.next()) {
				data = new StringBuilder(); 
				target = new StringBuilder(); 
				source = new StringBuilder(); 
				
				//System.out.println("Number of columns:"+ numberOfColumns);
				
				data.append(rs.getString(2).trim()).append("\t").append(rs.getString(4).trim());
				target.append(rs.getString(5).trim());
				source.append(rs.getString(1).trim()).append("\t")
					.append(rs.getString(3).trim()).append("\t").append(rs.getString(5).trim());
				
				instList.add(new Instance(data.toString(), target.toString(), new Integer(i), source.toString()));
				//instList.add(new Instance(data.toString(), target.toString(), source.toString(), source.toString()));
				data.setLength(0);
				target.setLength(0);
				source.setLength(0);
				
				data = null;
				target = null;
				source = null;
				i++;
			}
			return instList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e); 
		}
	}

	/* Used for queries that select only one column, so the result is
	 * returned as a java List<Object>, if the ResultSet has more than one column
	 * it returns a null
	 */
	public static List<Object> resultSetToOneColumnAsList(ResultSet rs) throws Exception{
		if(rs == null)
			return null;

		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns = metaData.getColumnCount();
			//System.out.println("numberOfColumns:"+ numberOfColumns);
			
			if(numberOfColumns > 1)
				return null;

			List<Object> rows = new ArrayList<Object>(numberOfColumns);    
			// Get the column names
			for (int column = 0; column < numberOfColumns; column++) {
				rows.add((metaData.getColumnLabel(column + 1)));
			}
			// Get all rows
			while (rs.next()) {
				for (int i = 1; i <= numberOfColumns; i++) {     
					if (rs.getObject(i) == null) 
						return null;
					rows.add(rs.getString(i));
				}
			}
			return rows;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e); 
		}
	}

	/* Used for queries that select only one column and we know
	 * that the number of results returned is small. The result is
	 * returned as a String with rows(with only one column) that are comma separated,
	 * If the ResultSet has more than one column it returns a null
	 */
	public static String resultSetToOneColumnAsCommamaSeparatedString(ResultSet rs) throws Exception{
		if(rs == null)
			return null;

		try {
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns = metaData.getColumnCount();

			if(numberOfColumns > 1)
				return null;

			StringBuilder columnValues = new StringBuilder(numberOfColumns);
			int count = 0;
			while (rs.next()) {  		
				if (rs.getObject(1) == null) 
					return null;

				if(count == 0)
					columnValues.append("'").append(rs.getString(1)).append("'");
				else
					columnValues.append(",'").append(rs.getString(1)).append("'");
				count++;
			}
			return columnValues.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e); 
		}
	}


	/* This is used for queries that return data that is very large in size. 
	 * So, this function allows it to be written directly 
	 * to a file on the server. The filename and path of the file and resultset are provided as parameters.
	 * TODO: have this return success or failure and handle the case in the calling function
	 */
	public static void saveResultsetAsFile(ResultSet rs, String filePath, String fileName){
		logger.info("Inside cacheResultSet function");
		//logger.info("The path of the file passed here is"+ filePath);
		logger.info("The name of the file passed here is"+ fileName);
		
		FileWriter out = null;
		try{
			out = new FileWriter(filePath+fileName);
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns = metaData.getColumnCount();
			/*List<Object> columnNames = new ArrayList<Object>(numberOfColumns);
			// Get the column names
			for (int column = 0; column < numberOfColumns; column++) {
				columnNames.add((metaData.getColumnLabel(column + 1)));
			}
			out.write(columnNames+"\n");*/
			
			while (rs.next()) {
				for (int i = 1; i <= numberOfColumns; i++) {
					out.write((rs.getString(i) == null) ? "" : rs.getString(i));
					if(i != numberOfColumns)
						out.write("\t");
				}
				out.write("\n");
			}
		}catch(IOException io){
			logger.error("IOException while writing cache file from resultset : "+ io.getMessage());
			io.printStackTrace();
		}catch(SQLException sqle){
			logger.error("SQL Exception while writing cache file from resultset : "+ sqle.getMessage());
			sqle.printStackTrace();
		}catch(Exception e){
			logger.error("Exception while writing cache file from resultset : "+ e.getMessage());
			e.printStackTrace();
		}finally{
			if (out != null) { 
				//System.out.println("Closing PrintWriter");
				try {
					out.flush();
					out.close();
					out = null;
				} catch (IOException io) {
					logger.error("IOException while closing ouputStream for cache resultset : "+ io.getMessage());
					io.printStackTrace();
				} 
			}
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
		}
		logger.info("After saving result to file");
		return;
	}
	
	
	public static void cacheResultSetAppend(ResultSet rs, String filePath, String fileName, int limitStart){
		
		//System.out.println("Inside cacheResultSetAppending function");
		
		FileWriter out = null;
		try{
			out = new FileWriter(filePath+fileName, true);
			ResultSetMetaData metaData = rs.getMetaData();
			int numberOfColumns = metaData.getColumnCount();
			
			if(limitStart == 0){ //Write the column names only the first time
				List<Object> columnNames = new ArrayList<Object>(numberOfColumns);
				// Get the column names
				for (int column = 0; column < numberOfColumns; column++) {
					columnNames.add((metaData.getColumnLabel(column + 1)));
				}
				out.write(columnNames+"\n");
			}
			
			while (rs.next()) {
				for (int i = 1; i <= numberOfColumns; i++) {
					out.write((rs.getString(i) == null) ? "null" : rs.getString(i));
					//out.write(rs.getString(i));
					if(i != numberOfColumns)
						out.write(",");
				}
				out.write("\n");
			}
		}catch(IOException io){
			logger.error("IOException while appending cache file from resultset : "+ io.getMessage());
			io.printStackTrace();
		}catch(SQLException sqle){
			logger.error("SQL Exception while appending cache file from resultset : "+ sqle.getMessage());
			sqle.printStackTrace();
		}catch(Exception e){
			logger.error("Exception while appending cache file from resultset : "+ e.getMessage());
			e.printStackTrace();
		}finally{
			if (out != null) { 
				//System.out.println("Closing PrintWriter");
				try {
					out.close();
				} catch (IOException io) {
					logger.error("IOException while closing ouputStream for cache resultset : "+ io.getMessage());
					io.printStackTrace();
				} 
			}
			
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
		}
		return;
	}
	
	/* Count the number of elements in the ResultSet */
	public static int resultSetAsCount(ResultSet rs, String countColumn) throws Exception{
		int count = 0;
	
		if(rs != null){	
			try {
				while(rs.next()){
					//System.out.println(" before count" );
					count =  Integer.parseInt(rs.getString(countColumn));
					//System.out.println(" after count"+ count );
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception(e); 
			}
		}
		return count;
	}
	

}
