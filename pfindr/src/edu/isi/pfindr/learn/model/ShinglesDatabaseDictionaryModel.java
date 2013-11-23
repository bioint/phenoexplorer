package edu.isi.pfindr.learn.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import edu.isi.pfindr.learn.db.DatabaseQueryInterface;
import edu.isi.pfindr.learn.util.CleanDataUtil;
import edu.isi.pfindr.learn.util.SortList;
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
 * One of the implementations for dictionary expansion. The factory for dictionary expansion calls this implementation if the user chooses dict.aug.method as shingle-db
 * 
 *  @author  Shefali Sharma
 */

public class ShinglesDatabaseDictionaryModel implements Model{

	static final long serialVersionUID = 42L;
	Properties properties;
	Connection conn;
	private Map<String, String> domainMap = new HashMap<String, String>();
	Object model = null;
	static private Logger logger = Logger.getLogger("AppLogging");    

	ShinglesDatabaseDictionaryModel(Properties properties){
		this.properties = properties;
		domainMap = DomainKnowledge.loadDomainKnowledge(properties);
		//Load model
		loadModel();
	}
	//Set the database connection. Only used for Database Dictionary model
	public void setDataBaseConnection(Connection conn){
		this.conn = conn;
	}
	public String getModelName(){
		return (String)properties.get("dict.aug.method");
	}
	public String getModelDirectory(){
		return (String)properties.get("run.shingle.dir");
	}
	////
	public String getModelEvaluateOrgFileName(){
		return (String)properties.get("run.shingle.evaluate.org.file");
	}
	public String getModelEvaluateOrgPairsFileName(){
		return (String)properties.get("run.shingle.evaluate.org.pairs.file");
	}
	///
	public String getModelOrgTrainFileName(){
		return (String)properties.get("run.shingle.org.train.file");
	}
	public String getModelOrgTestFileName(){
		return (String)properties.get("run.shingle.org.test.file");
	}
	public String getModelTrainPairsFileName(){
		return (String)properties.get("run.shingle.train.pairs.file");
	}
	public String getModelTestPairsFileName(){
		return (String)properties.get("run.shingle.test.pairs.file");
	}
	public String getModelTestPairsLearnOutputFileName(){
		return (String)properties.get("run.shingle.test.pairs.file.learn");
	}

	public String getModelBinaryFileName(){
		//System.out.println("getModelBinaryFileName "+ (String)properties.get("run.shingle.model.binary.file") );
		return (String)properties.get("run.shingle.model.binary.file");
	}

	public Object getModel(){
		return model;
	}

	public void loadModel(){
		logger.info("*** Loading model, current time(ms) : " + System.currentTimeMillis() );
		ObjectInputStream inputStream = null;
		try{
			//Read the classifier
			inputStream = new ObjectInputStream(new FileInputStream(
					ServletContextInfo.getContextPath() + getModelDirectory() + getModelBinaryFileName()));
			model = inputStream.readObject();
		}catch (IOException ioe) {
			ioe.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			 //Close the ObjectInputStream
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
		}
		logger.info("*** Completed loading model, current time(ms) : "+ System.currentTimeMillis() );
	}

	/*
	 * Expand both sides of the phenotype pairs
	 */
	public StringBuilder expandPhenotypeString(StringBuilder firstPhenotypeStringBuilder, 
			StringBuilder secondPhenotypeStringBuilder){
		StringBuilder dataBuilder = new StringBuilder();
		dataBuilder
		.append(CleanDataUtil.removeSpecificCharacters(expandWithDictionaryForString(firstPhenotypeStringBuilder.toString())))
		.append("\t")
		.append(CleanDataUtil.removeSpecificCharacters(expandWithDictionaryForString(secondPhenotypeStringBuilder.toString())));

		return dataBuilder;
	}

	/*
	 * Expand one sides of the phenotype string with the dictionary
	 */
	public String expandPhenotypeString(String phenotypeString ){
		return CleanDataUtil.removeSpecificCharacters(expandWithDictionaryForString(phenotypeString));
	}

	/*
	 * Expand the phenotype definition with results from the dictionary for each string (only bi-gram, tri-gram and tetra-gram)
	 */
	public String expandWithDictionaryForString(String data) {

		StringBuilder dataString = new StringBuilder();
		StringBuilder domainKnowledgeExpandString = new StringBuilder();
		//System.out.println("****************** Size of DomainMap " + domainMap.size());
		
		dataString.append(" ").append(DomainKnowledge.expandVariableWithDomainKnowledge(domainMap, data).trim()); //For single words, expand with domain knowledge (abbreviation etc.)

		List<String> shingleList = Shingles.computeShingles(dataString.toString());
		//sort the shingles on the length of the string, longer ones first
		Collections.sort(shingleList, new SortList());
		StringBuilder sqlStringBuilder = new StringBuilder();
		//if the dictionary contains the shingles, then append the definition
		for (String shingle : shingleList) {
			//System.out.println("Inside shingle: "+ shingle);

			sqlStringBuilder.append("SELECT array_agg(definition) combined_definition FROM ")
			.append("(SELECT distinct concept_id, definition FROM ")
			.append("(SELECT c.concept_name, d.* FROM umls_concept_name c, umls_source_definition d WHERE c.concept_id = d.concept_id order by c.concept_id) a ")
			.append("WHERE concept_name = '").append(shingle.trim()).append("')b GROUP BY concept_id");
			//System.out.println("****************" + sqlStringBuilder.toString());

			dataString.append(" ").append( 
					(DatabaseQueryInterface.executeQueryReturnOneColumnAsString(conn, sqlStringBuilder.toString())));

			if(domainMap.containsKey(shingle.trim().toLowerCase())) { //For shingles (bi,tri,tetra grams), expand with domain knowledge (abbreviation etc.)
				domainKnowledgeExpandString.append(" ").append(domainMap.get(shingle.trim()));
			}
			sqlStringBuilder.setLength(0);
		}
		
		dataString.append(" ").append(domainKnowledgeExpandString);
		//System.out.println("After expanding with dictionary");
		
		return dataString.toString().replaceAll("\\<.*?\\>", " ").replaceAll("(\\\")|(\\{)|(\\})"," ").replaceAll("\\s+", " ");
	}
}
