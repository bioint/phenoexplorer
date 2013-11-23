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
 * * Monitor.java 
 * 
 * Runs periodically on the server. Makes a dummy query to check that queries are running correctly. 
 * Emails is sent to the admin, in case of errors
 * 
 * @author sharma@isi.edu 
 * 
 */
public class Monitor {

	private static Logger logger = Logger.getLogger("AppLogging");
	private final static int LUCENE_TOP_N = 40;
	private static String description = "BMI";

	public static String testNewPhenotype(Connection conn){

		//StringBuilder studiesStringBuilder = new StringBuilder();
		long currentTimeMilisecond = System.currentTimeMillis(); //Its OK to use current time for unique table IDs, since it is only used by the debug application

		String finalTempTable = null;
		FileReader fileReader = null;
		String modelDirectoryPath = null;
		String originalPairsFileName = null;
		String scoreFileName = null;
		try{
			Model model  = ModelFactory.createModel();
			CleanDataUtil.loadStopwordFile(ServletContextInfo.getContextPath() +"data/stopwords.txt");

			modelDirectoryPath = ServletContextInfo.getContextPath()+model.getModelDirectory();

			if(model.getModelName().equals("shingle-db")){
				((ShinglesDatabaseDictionaryModel)model).setDataBaseConnection(conn);
			}

			originalPairsFileName = (new StringBuilder())
					.append("new_existing_variables_pairs")
					.append(currentTimeMilisecond).append(".txt").toString();

			StringBuilder processedPhenotypeStringBuilder = new StringBuilder();
			CleanDataUtil.preProcessWordsSpecialCharacters(description, 
					processedPhenotypeStringBuilder);

			String descriptionExpandedStemmed = model.expandPhenotypeString (
					CleanDataUtil.preprocessRemoveStopWords(processedPhenotypeStringBuilder.toString()));

			String descriptionExpandedNotStemmed;
			processedPhenotypeStringBuilder.setLength(0);

			descriptionExpandedStemmed = CleanDataUtil.preprocessRemoveStopWords(CleanDataUtil.removeSpecificCharacters(
					UMLSDictionary.preprocessDictionaryFrequentWords(descriptionExpandedStemmed)));

			descriptionExpandedNotStemmed = new String(descriptionExpandedStemmed);
			descriptionExpandedStemmed = CleanDataUtil.preprocessStemAndTokenizeAddBigramsInString(descriptionExpandedStemmed);

			DatabaseQueryInterface.executeQuerySaveToFile(conn, 
					"SELECT '" + description + "','" + descriptionExpandedStemmed + "', variable_desc_org, variable_desc_expanded, '0' FROM " +
							"dbgap_all_variable_expanded_stem_token_bi ", 
							ServletContextInfo.getContextPath() + model.getModelDirectory(),
							originalPairsFileName);

			//Test on the model
			MaxEntDistTrainTest maxEnt = new MaxEntDistTrainTest();
			maxEnt.test(ServletContextInfo.getContextPath() + 
					model.getModelDirectory(),
					originalPairsFileName,
					//"new_existing_variables_pairs"+currentTimeMilisecond+".txt", 
					model.getModel());

			scoreFileName = "new_existing_variables_pairs"+currentTimeMilisecond+"_classification.txt";
			//Use the classification result, and combine scores from lucene
			//Read file into a map <description, match score

			Map<String,Double> searchResultMap = LuceneSearchEngine.search(description,
					descriptionExpandedNotStemmed, descriptionExpandedStemmed);
			if(searchResultMap.size() > 0){
				scoreFileName = computeCombinedScore(searchResultMap, 
						ServletContextInfo.getContextPath()+model.getModelDirectory(),scoreFileName);
			}

			StringBuilder sql = new StringBuilder();
			sql.append("(description1 text, description2 text, lucene_score numeric(8,6), model_score numeric(8,6), ") 
			.append("combined_match_score numeric(8,6));");
			List<String> indexColumns = new ArrayList<String>();
			indexColumns.add("description2");

			String auxTempTableName = "tmp_phenotype_desc_score" + currentTimeMilisecond;

			DatabaseQueryInterface.createTempTable(conn, sql.toString(), auxTempTableName, indexColumns);

			//load data into the intermediary temporary table
			CopyManager copyManager = new CopyManager((BaseConnection) conn);
			fileReader = new FileReader(
					ServletContextInfo.getContextPath()+model.getModelDirectory()+scoreFileName);
			copyManager.copyIn("COPY " + auxTempTableName + "  FROM STDIN WITH DELIMITER '\t'", 
					fileReader );

			//////////////////////////////////////

			finalTempTable = "tmp_new_phenotype"+currentTimeMilisecond;
			sql.setLength(0);
			
			sql.append(" AS SELECT result.combined_match_score variable_match_score, result.description,  ");
			sql.append("result.variable_href variable, result.study_href study FROM ");
			sql.append("(SELECT * FROM ").append(Utils.VARIABLES_TABLE).append(" dbgap LEFT OUTER JOIN ");
			sql.append( auxTempTableName).append(" temp ON dbgap.description = temp.description2) result "); 
			sql.append(" WHERE result.description2 IS NOT NULL");
			
			indexColumns.clear();
			indexColumns.add("description");
			DatabaseQueryInterface.createTempTable(conn, sql.toString(), finalTempTable, indexColumns);
			DatabaseQueryInterface.dropTempTable(conn, auxTempTableName);

		}catch(SQLException sqle){
			sqle.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if (fileReader != null) { 
				try {
					fileReader.close();
					fileReader = null;
					//delete both files
					if(FileUtil.deleteFile(modelDirectoryPath, originalPairsFileName)){//model, and score file is created, so we can delete the org file
						logger.error("Successfully deleted file:  "+ originalPairsFileName);
					}
					if(FileUtil.deleteFile(modelDirectoryPath, scoreFileName)){//model, and score file is created, so we can delete the org file
						logger.error("Successfully deleted file:  "+ scoreFileName);
					}
				} catch (Exception e) {
					logger.error("Exception while closing in the finally block of Monitor ");
					e.printStackTrace();
				} 
			}

		}
		return finalTempTable;
	}

	private static String computeCombinedScore(Map<String,Double> searchMap, String inputFilePath, String inputFileName){

		BufferedReader inputBufferedReader = null;
		BufferedWriter outputBufferedWriter = null;

		FileReader fileReader = null;
		FileWriter fileWriter = null;
		String thisLine;
		double luceneScore;

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
			for( Double score : luceneTopList ){
				luceneTopScoreAverage += ((Double)score).doubleValue();
			}
		}else{
			luceneTopScoreAverage = searchValues.get(0);
		}
		luceneTopScoreAverage = luceneTopScoreAverage / (double) LUCENE_TOP_N;

		try {
			fileReader = new FileReader(inputFilePath+inputFileName);
			inputBufferedReader = new BufferedReader(fileReader);

			fileWriter = new FileWriter(resultFilePath);
			outputBufferedWriter = new BufferedWriter(fileWriter);

			String description;
			while ((thisLine = inputBufferedReader.readLine()) != null) {
				thisLine = thisLine.trim();
				if (thisLine.equals(""))
					continue;

				outBuilder = new StringBuilder();

				description = thisLine.split("\\t")[1];
				if(searchMap.containsKey(description)){
					//modify score with searchMap score
					luceneScore = ((Double)searchMap.get(description)).doubleValue();

					luceneScore =  ((luceneScore/ luceneTopScoreAverage));
					luceneScore = (luceneScore	> 1) ? 1: (luceneScore);
					combinedScore = ((luceneScore) * (double)(0.90))
							+ ((Double.parseDouble(thisLine.split("\\t")[4].trim())) * (double)(0.10));

				}else{
					//modify model score adding no other score
					luceneScore = (double)0;
					combinedScore = (Double.parseDouble(thisLine.split("\\t")[4].trim())) * (double)(0.50);
				}
				outBuilder.append(thisLine.split("\\t")[0]).append("\t").append(thisLine.split("\\t")[1]).append("\t")
				.append(luceneScore).append("\t").append(thisLine.split("\\t")[4].trim()).append("\t")
				.append(combinedScore).append("\n");

				outputBufferedWriter.append(outBuilder.toString());
				outBuilder.setLength(0);
			}
			outputBufferedWriter.flush();

		}catch (IOException io) {
			logger.error("IO exception thrown  "+ outputBufferedWriter);
			io.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
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
		return resultFileName;
	}
}
