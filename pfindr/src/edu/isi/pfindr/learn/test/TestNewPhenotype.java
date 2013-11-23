package edu.isi.pfindr.learn.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import edu.isi.pfindr.helper.Utils;
import edu.isi.pfindr.learn.db.DatabaseQueryInterface;
import edu.isi.pfindr.learn.maxent.MaxEntDistTrainTest;
import edu.isi.pfindr.learn.model.Model;
import edu.isi.pfindr.learn.model.ModelFactory;
import edu.isi.pfindr.learn.model.ShinglesDatabaseDictionaryModel;
import edu.isi.pfindr.learn.model.UMLSDictionary;
import edu.isi.pfindr.learn.search.LuceneSearchEngine;
import edu.isi.pfindr.learn.util.CleanDataUtil;
import edu.isi.pfindr.learn.util.FileUtil;
import edu.isi.pfindr.learn.util.MapUtil;
import edu.isi.pfindr.learn.util.ReadProperties;
import edu.isi.pfindr.listeners.ServletContextInfo;

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
 * * TestNewPhenotype.java 
 * 
 * Returns all dbGaP variables, semantically similar to the user query, using Machine Learning and Information Retrieval Techniques
 * 
 * @author sharma@isi.edu 
 * 
 */
public class TestNewPhenotype {

	private static Logger logger = Logger.getLogger("AppLogging");

	//Read properties
	private static Properties properties = ReadProperties.readProperties();
	private static final int LUCENE_TOP_N = Integer.parseInt((String)properties.get("lucene.norm.top.n"));
	private static double luceneWeight = Double.parseDouble((String)properties.get("lucene.weight"));
	private static double mlWeight = Double.parseDouble((String)properties.get("ml.weight"));
	private static double onlyMlWeight = Double.parseDouble((String)properties.get("only.ml.weight"));

	public String testNewPhenotype(Connection conn, String sessionId, String phenotype, 
			String description, String[] studies_id){

		StringBuilder studiesStringBuilder = new StringBuilder();
		if(studies_id.length > 0){
			studiesStringBuilder.append("'").append(studies_id[0]).append("'");
			for(int i=1; i<studies_id.length;i++)
				studiesStringBuilder.append(",'").append(studies_id[i]).append("'");
		}
		String finalTempTable = null;
		FileReader fileReader = null;
		String modelDirectoryPath = null;
		String originalPairsFileName = null;
		String scoreFileName = null;
		try{
			//Create model
			Model model  = ModelFactory.createModel();

			//If description is not entered by the user, its a simple SQL query, and there is no need to access the ML and IR techniques
			if((description.trim() == "") || (description == null ))
				return onlyStudyMetaDataSelected(studiesStringBuilder.toString(), conn, sessionId );

			//Else, use the description and do the phenotype mapping using both the model saved from machine learning, and the IR results returned from Lucene
			CleanDataUtil.loadStopwordFile(ServletContextInfo.getContextPath() +"data/stopwords.txt");

			//Set database connection, if model is defined to be a database model
			if(model.getModelName().equals("shingle-db")){
				((ShinglesDatabaseDictionaryModel)model).setDataBaseConnection(conn);
			}

			//Prepare definitions (get stemmed, and dictionary expanded version of the definition). This is the new definition that the user has entered
			modelDirectoryPath = ServletContextInfo.getContextPath()+model.getModelDirectory();
			originalPairsFileName = (new StringBuilder()).append("new_existing_variables_pairs").append(sessionId).append(".txt").toString();

			StringBuilder processedPhenotypeStringBuilder = new StringBuilder();
			CleanDataUtil.preProcessWordsSpecialCharacters(description, 
					processedPhenotypeStringBuilder);

			String descriptionExpandedStemmed = model.expandPhenotypeString (
					CleanDataUtil.preprocessRemoveStopWords(processedPhenotypeStringBuilder.toString()));

			String descriptionExpandedNotStemmed;
			processedPhenotypeStringBuilder.setLength(0);

			descriptionExpandedStemmed = CleanDataUtil.preprocessRemoveStopWords(CleanDataUtil.removeSpecificCharacters(
					UMLSDictionary.preprocessDictionaryFrequentWords(descriptionExpandedStemmed)));

			if(descriptionExpandedStemmed.length() > 3000){
				descriptionExpandedStemmed = descriptionExpandedStemmed.substring(0, 3000);
			}
			descriptionExpandedNotStemmed = new String(descriptionExpandedStemmed);
			descriptionExpandedStemmed = CleanDataUtil.preprocessStemAndTokenizeAddBigramsInString(descriptionExpandedStemmed);
			//logger.info("User entered string after expansion : "+ descriptionExpandedStemmed);

			String distinctDescriptionAuxilliaryTempTable = null;
			/*
			 * 	Create pairs with all existing dbgap variables filtering studies if the user entered meta data for studies 
			 **/
			if(studies_id.length > 0){
				distinctDescriptionAuxilliaryTempTable = "tmp_phen_desc_distinct_aux" + sessionId;
				StringBuilder sql = new StringBuilder();

				sql.append(" AS SELECT distinct(description) descr FROM " + Utils.VARIABLES_TABLE + " "+
						"WHERE study_id in (" + studiesStringBuilder.toString() + ")");

				List<String> indexCols = new ArrayList<String>();
				indexCols.add("descr");

				logger.info("Creating auxilliary temporary table : "+ distinctDescriptionAuxilliaryTempTable); 
				DatabaseQueryInterface.createTempTable(conn, sql.toString(), distinctDescriptionAuxilliaryTempTable, indexCols);
				logger.info("Execute query, save to file");
				DatabaseQueryInterface.executeQuerySaveToFile(conn,
						"SELECT '" + description + "','" + descriptionExpandedStemmed + "', e.variable_desc_org, e.variable_desc_expanded, '0' FROM " +
								distinctDescriptionAuxilliaryTempTable + " a, " + Utils.VARIABLES_DICT_EXPANDED_TABLE + " e  "+
								"WHERE e.variable_desc_org = a.descr", 
								modelDirectoryPath,
								originalPairsFileName);

				DatabaseQueryInterface.dropTempTable(conn, distinctDescriptionAuxilliaryTempTable);
			}else{
				DatabaseQueryInterface.executeQuerySaveToFile(conn, 
						"SELECT '" + description + "','" + descriptionExpandedStemmed + "', variable_desc_org, variable_desc_expanded, '0' FROM " +
								Utils.VARIABLES_DICT_EXPANDED_TABLE, 
								modelDirectoryPath,
								originalPairsFileName);
			}
			logger.info("######### Before classifying on existing model for new phenotype ..");
			/*
			 * Pass the pairs file through the model, to get the classification results from machine learning
			 */
			MaxEntDistTrainTest maxEnt = new MaxEntDistTrainTest();
			maxEnt.test(modelDirectoryPath,
					originalPairsFileName, 
					model.getModel());

			logger.info("########## Completed testing/classifying, using existing model for new Phenotype ..");
			scoreFileName = "new_existing_variables_pairs"+sessionId+"_classification.txt";
			/*
			 * Use the classification result from machine learning, and combine them with scores from Lucene
			 */
			Map<String,Double> searchResultMap = LuceneSearchEngine.search(description,
					descriptionExpandedNotStemmed, descriptionExpandedStemmed);
			if(searchResultMap.size() > 0){
				logger.info("Search result from Lucene returned results: "+ searchResultMap.size());
				scoreFileName = computeCombinedScore(searchResultMap, 
						modelDirectoryPath,scoreFileName);
			}

			StringBuilder sql = new StringBuilder();
			sql.append("(description1 text, description2 text, lucene_score numeric(8,6), model_score numeric(8,6), ") 
			.append("combined_match_score numeric(8,6));");
			List<String> indexColumns = new ArrayList<String>();
			indexColumns.add("description2");

			String auxTempTableName = "tmp_phen_desc_score" + sessionId;
			logger.info("Creating auxilliary temporary table with scores: ");

			logger.info("Score File Name : "+ scoreFileName);
			DatabaseQueryInterface.createTempTable(conn, sql.toString(), auxTempTableName, indexColumns);

			/*
			 * Load the combined results score results into the intermediary temporary table
			 */
			CopyManager copyManager = new CopyManager((BaseConnection) conn);
			fileReader = new FileReader(
					modelDirectoryPath+scoreFileName);
			copyManager.copyIn("COPY " + auxTempTableName + "  FROM STDIN WITH DELIMITER '\t'", fileReader );

			logger.info("Loaded temp table : "+ auxTempTableName);
			//////////////////////////////////////

			finalTempTable = "tmp_new_phen"+sessionId;
			sql.setLength(0);


			/**
			 * Create the final table that will be displayed through the interface
			 */
			if(studies_id.length > 0){
				sql.append(" AS SELECT result.combined_match_score variable_match_score, result.description,  ");
				sql.append("result.variable_href variable, result.study_href study FROM ");
				sql.append("((SELECT * FROM ").append(Utils.VARIABLES_TABLE).append(" ");
				sql.append("WHERE study_id in (" + studiesStringBuilder.toString() + ") ");
				sql.append(") dbgap LEFT OUTER JOIN ");
				sql.append("(SELECT * FROM ").append(auxTempTableName).append(" WHERE description2 IS NOT NULL) temp ");
				sql.append(" ON dbgap.description = temp.description2) result "); 
				
			}else{
				sql.append(" AS SELECT result.combined_match_score variable_match_score, result.description,  ");
				sql.append("result.variable_href variable, result.study_href study FROM ");
				sql.append("(SELECT * FROM ").append(Utils.VARIABLES_TABLE).append(" dbgap LEFT OUTER JOIN ");
				sql.append( auxTempTableName).append(" temp ON dbgap.description = temp.description2) result "); 
				sql.append(" WHERE result.description2 IS NOT NULL");
			}

			logger.info("Creating temporary table : ");//+ sql.toString());

			indexColumns.clear();
			indexColumns.add("description");
			DatabaseQueryInterface.createTempTable( conn, sql.toString(), finalTempTable, indexColumns );

			logger.info("Completed creating temporary table : ");
			DatabaseQueryInterface.dropTempTable(conn, auxTempTableName);

		}catch(SQLException sqle){
			logger.error("SQL Exception while creating temporary table for new Phenotype");
			sqle.printStackTrace();
		}catch(Exception e){
			logger.error("Exception while adding new Phenotype");
			e.printStackTrace();
		}finally{
			if (fileReader != null) { 
				//logger.error("Closing PrintWriter");
				try {
					fileReader.close();
					fileReader = null;
					//delete both files
					if(FileUtil.deleteFile(modelDirectoryPath, originalPairsFileName)){//model, and score file is created, so we can delete the org file
						logger.error("Successfully deleted file:  "+ originalPairsFileName);
					}else{
						logger.error("Error deleting file :"+ originalPairsFileName);
					}
					if(FileUtil.deleteFile(modelDirectoryPath, scoreFileName)){//model, and score file is created, so we can delete the org file
						logger.error("Successfully deleted file:  "+ scoreFileName);
					}else{
						logger.error("Error deleting file :"+ scoreFileName);
					}
				} catch (Exception e) {
					logger.error("Exception while closing filereader ");
					e.printStackTrace();
				} 
			}
		}
		return finalTempTable;
	}

	/*
	 * Combines the scores from ML (inputfile) and Lucene (searchMap), and stores the result in an output filename. The o/p filename is returned.
	 */
	private String computeCombinedScore(Map<String,Double> searchMap, String inputFilePath, String inputFileName){

		BufferedReader inputBufferedReader = null;
		BufferedWriter outputBufferedWriter = null;
		FileReader fileReader = null;
		FileWriter fileWriter = null;

		//logger.info("Inside computeCombinedScore ");

		String thisLine;
		double luceneScore = 0;

		//Get machine learning scores in a list
		double combinedScore;
		StringBuilder outBuilder;
		String resultFileName = inputFileName.replaceAll("_classification.txt","_classification_lucene.txt" );
		String resultFilePath =  inputFilePath + resultFileName;

		searchMap = MapUtil.sortByValueDesc(searchMap);

		List<Double> searchValues = new ArrayList<Double>(searchMap.values());
		double luceneTopScoreAverage = 0;
		if(searchValues.size() > LUCENE_TOP_N){
			List<Double> luceneTopList = searchValues.subList(0, LUCENE_TOP_N);
			//logger.info(" top list size:"+ luceneTopList);
			for( Double score : luceneTopList ){
				luceneTopScoreAverage += ((Double)score).doubleValue();
				//logger.info("Lucene Average Top score :"+ luceneTopScoreAverage);
			}
		}else{
			luceneTopScoreAverage = searchValues.get(0);
		}
		luceneTopScoreAverage = luceneTopScoreAverage / (double) LUCENE_TOP_N;
		System.out.println("Lucene Average Top score final :"+ luceneTopScoreAverage);

		try {
			fileReader = new FileReader(inputFilePath+inputFileName);
			inputBufferedReader = new BufferedReader(fileReader);

			fileWriter = new FileWriter(resultFilePath);
			outputBufferedWriter = new BufferedWriter(fileWriter);

			String description;
			//Read each line from the ML input file
			while ((thisLine = inputBufferedReader.readLine()) != null) {
				thisLine = thisLine.trim();
				if (thisLine.equals(""))
					continue;

				outBuilder = new StringBuilder();
				description = thisLine.split("\\t")[1];
				//If the the same phenotype description is also returned from Lucene
				if(searchMap.containsKey(description)){
					//Get the Lucene score and normalize it
					luceneScore = ((Double)searchMap.get(description)).doubleValue();
					luceneScore =  ((luceneScore/ luceneTopScoreAverage));
					luceneScore = (luceneScore	> 1) ? 1: (luceneScore);
					//logger.debug("Lucene Normalized Score : " + luceneScore);

					//Combine the two scores
					combinedScore = ((luceneScore) * (luceneWeight)
							+ ((Double.parseDouble(thisLine.split("\\t")[4].trim())) * (mlWeight)));

					//logger.debug("Machine Learning model score :" +(Double.parseDouble(thisLine.split("\\t")[4].trim())));
					//logger.debug("Value of combined score :" + combinedScore);
				}else{
					//If the description is only returned by ML, modify model score adding no other score
					luceneScore = (double)0;
					combinedScore = (Double.parseDouble(thisLine.split("\\t")[4].trim())) * onlyMlWeight;
				}
				//logger.debug("Combined Score:"+ combinedScore);
				//write output str1, str2, lucenescore, modelscore, combinedscore
				outBuilder.append(thisLine.split("\\t")[0]).append("\t").append(thisLine.split("\\t")[1]).append("\t")
				.append(luceneScore).append("\t").append(thisLine.split("\\t")[4].trim()).append("\t")
				.append(combinedScore).append("\n");

				//logger.debug("outBuilder.toString());
				outputBufferedWriter.append(outBuilder.toString());
				outBuilder.setLength(0);
			}
			outputBufferedWriter.flush();
		}catch (IOException io) {
			logger.error("IO exception thrown  "+ outputBufferedWriter);
			io.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				if(fileWriter != null){
					fileWriter.close();
					fileWriter = null;
				}
			} catch (IOException e) {
				logger.error("Problem occured while closing output stream  "+ fileWriter);
				e.printStackTrace();
			}
			try {
				if(outputBufferedWriter != null){
					outputBufferedWriter.close();
					outputBufferedWriter = null;
				}
			} catch (IOException e) {
				logger.error("Problem occured while closing output stream  "+ outputBufferedWriter);
				e.printStackTrace();
			}
			try {
				if(fileReader != null){
					fileReader.close();
					fileReader = null;
				}
			} catch (IOException e) {
				logger.error("Problem occured while closing output stream  "+ fileReader);
				e.printStackTrace();
			}
			try {
				if(inputBufferedReader != null){
					inputBufferedReader.close();
					inputBufferedReader = null;
				}
				if(	FileUtil.deleteFile(inputFilePath,inputFileName)){
					logger.error("Successfully deleted file:  "+ inputFileName);
				}else{
					logger.error("Error deleting file:  "+ inputFileName);
				}
			} catch (IOException e) {
				logger.error("Problem occured while closing output stream  "+ inputBufferedReader);
				e.printStackTrace();
			}
		}

		System.out.println("Returning file name"+ resultFileName);
		return resultFileName;
	}

	/*
	 * If no description is provided by the user, only the study meta data is provided, then its a simple SQL query
	 */
	public String onlyStudyMetaDataSelected(String studies, Connection conn, String sessionId){
		String finalTempTable = "tmp_new_phen"+sessionId;

		StringBuilder sql = new StringBuilder();
		sql.append(" AS SELECT CAST(0 AS NUMERIC(8,6)) as variable_match_score, description, ");
		sql.append(" variable_href variable, study_href study FROM ");
		sql.append( Utils.VARIABLES_TABLE ).append(" dbgap ");

		//SELECT all, filtering the selected studies
		if( studies.length() > 0){
			sql.append(" WHERE dbgap.study_id in (" + studies + ") "); 
		}
		DatabaseQueryInterface.createTempTable( conn, sql.toString(), finalTempTable, null );
		return finalTempTable;
	}

}
