package edu.isi.pfindr.learn.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.isi.pfindr.learn.db.DatabaseQueryInterface;
import edu.isi.pfindr.learn.model.Model;
import edu.isi.pfindr.learn.model.ModelFactory;
import edu.isi.pfindr.learn.model.ShinglesDatabaseDictionaryModel;
import edu.isi.pfindr.learn.model.UMLSDictionary;
import edu.isi.pfindr.listeners.ServletContextInfo;

public class ExpandWithDictionaryUsingFile {

	public static void main(String[] argsv){

		String basePath = "";
		String inputFileName = "";
		//Get the absolute path from the command-line
		if (argsv.length > 0) {
		    try {
		    	basePath = argsv[0];
		    	inputFileName = argsv[1];
		    	ServletContextInfo.setContextPath(basePath);
		    } catch (Exception e) {
		        System.err.println("Base Path not correctly specified!!");
		        System.exit(1);
		    }
		}
		
		Model model =  ModelFactory.createModel();
		BufferedWriter bw = null;
		CleanDataUtil.loadStopwordFile(basePath+"/data/stopwords.txt");
		
		Connection conn = null;
		try{
			List<String> variableList = FileUtils.readLines(new File(basePath + inputFileName));
			
			bw = new BufferedWriter(new FileWriter(basePath + inputFileName.split("\\.")[0] + "_expanded.txt"));
			String variable;
			//Set database connection, if model is defined to be a database model
			conn = DatabaseQueryInterface.getDatabaseConnection(null);
			if(model.getModelName().equals("shingle-db")){
				((ShinglesDatabaseDictionaryModel)model).setDataBaseConnection(conn);
			}
			
			String expandedVariable = "";
			for(String line : variableList){

				variable = line.split("\\t")[0];
				
				StringBuilder processedPhenotypeStringBuilder = new StringBuilder();
				//Deal with some special characters in the existing words (like '-' / ? etc.)
				CleanDataUtil.preProcessWordsSpecialCharacters(variable, processedPhenotypeStringBuilder); //Clean original string

				expandedVariable = model.expandWithDictionaryForString (
						CleanDataUtil.preprocessRemoveStopWords(processedPhenotypeStringBuilder.toString())); //Expand with dictionary
				//System.out.println("After domain expansion:"+ expandedVariable);
				
				processedPhenotypeStringBuilder.setLength(0);

				expandedVariable = UMLSDictionary.preprocessDictionaryFrequentWords(expandedVariable);
				expandedVariable = CleanDataUtil.removeSpecificCharacters(expandedVariable); //Clean expanded string
				
				expandedVariable = CleanDataUtil.preprocessStemAndTokenizeAddBigramsInString(expandedVariable);

				//System.out.println("Expand word with dictionary (using shingles) .."+ expandedVariable);

				bw.write(variable + "\t" + expandedVariable + "\n");
				bw.flush();
			}
		}catch(IOException io){
			io.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally {
			if(bw != null){
				try {
					bw.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
