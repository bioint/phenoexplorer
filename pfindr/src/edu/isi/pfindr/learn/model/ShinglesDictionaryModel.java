package edu.isi.pfindr.learn.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

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
 * One of the implementations for dictionary expansion. The factory for dictionary expansion calls this implementation if the user chooses dict.aug.method as shingle
 * This class backend functionality that takes 
 * 
 */ 

public class ShinglesDictionaryModel implements Model{
	
	static final long serialVersionUID = 42L;
	Properties properties;
	private Map<String, String> dictionaryMap = new HashMap<String, String>();
	private Map<String, String> domainMap = new HashMap<String, String>();
	Dictionary dictionary;
	Object model = null;
	static private Logger logger = Logger.getLogger("AppLogging");     
	
	ShinglesDictionaryModel(Properties properties){
		this.properties = properties;
		//System.out.println("The path of mw is : "+ getDictionaryPath());
		dictionary = DictionaryFactory.chooseDictionary(properties);
		dictionaryMap = dictionary.loadDictionary();
		domainMap = DomainKnowledge.loadDomainKnowledge(properties);
		loadModel();
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
		return (String)properties.get("run.shingle.model.binary.file");
	}
	
	public String getDictionaryPath(){
		return dictionary.getDictionaryPath();
	}
	
	public Object getModel(){
		return model;
	}

	public void loadModel(){
		logger.info("************ Before loading model, current time(ms) :" + System.currentTimeMillis() );
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
		logger.info("*************** Completed loading model, current time(ms) : "+ System.currentTimeMillis() );
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
	public String expandPhenotypeString(String phenotypeString){
		return CleanDataUtil.removeSpecificCharacters(expandWithDictionaryForString(phenotypeString));
	}
	
	/*
	 * Expand the phenotype definition with results from the dictionary for (only bi-gram, tri-gram and tetra-gram)
	 */
	public String expandWithDictionaryForString(String data) {
		
		StringBuilder dataString = new StringBuilder();
		StringBuilder domainKnowledgeExpandString = new StringBuilder();
		
		dataString.append(data);
		//Tokenize, Lookup abbr. for each token, append to datastring
		//shingles for abbrv, and append with dictionary
		//Expand single words first with domain knowledge then seach in the dictionary
		dataString.append(DomainKnowledge.expandVariableWithDomainKnowledge(domainMap, dataString.toString()));
		
		System.out.println("After domain expansion"+ dataString.toString());
		
		List<String> shingleList = Shingles.computeShingles(dataString.toString());
		//sort the shingles on the length of the string, longer ones first
		Collections.sort(shingleList, new SortList());
		//if the dictionary contains the shingles, then append the definition
		for (String shingle : shingleList) {
			//System.out.println("Shingle is "+ shingle.trim());
			if (dictionaryMap.containsKey(shingle.trim())) {
				dataString.append(" ").append(dictionaryMap.get(shingle.trim()));
				//System.out.println("*****************Appending :"+ temp);
			}
			if(domainMap.containsKey(shingle.trim().toLowerCase())) {
				domainKnowledgeExpandString.append(" ").append(domainMap.get(shingle.trim()));
			}
		}
		
		dataString.append(" ").append(domainKnowledgeExpandString);
		//Append to the end of the original string
		//System.out.println("After expansion with dictionary :"+ dataString.toString().replaceAll("\\<.*?\\>", " ")
		//				   .replaceAll("\\s+", " "));
		return dataString.toString().replaceAll("\\<.*?\\>", " ").replaceAll("\\s+", " ");
	}
}
