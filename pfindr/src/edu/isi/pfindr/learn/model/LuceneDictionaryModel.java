package edu.isi.pfindr.learn.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import edu.isi.pfindr.learn.search.LuceneDictionaryAugmenter;
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
 * One of the implementations for dictionary expansion. The factory for dictionary expansion calls this implementation if the user chooses dict.aug.method as lucene
 * Only implemented for MW dictionary, currently
 * 
 *  @author  Shefali Sharma
 */
public class LuceneDictionaryModel implements Model{

	static final long serialVersionUID = 42L;

	LuceneDictionaryModel(){}
	transient Properties properties;
	private transient LuceneDictionaryAugmenter luceneDict;
	Object model = null;
	static private Logger logger = Logger.getLogger("AppLogging");

	LuceneDictionaryModel(Properties properties){
		this.properties = properties;
		luceneDict = new LuceneDictionaryAugmenter(properties);
		loadModel();
	}
	public String getModelName(){
		return (String)properties.get("dict.aug.method");
	}
	public String getModelDirectory(){
		return (String)properties.get("run.lucene.dir");
	}
	public String getModelEvaluateOrgFileName(){
		return (String)properties.get("run.lucene.evaluate.org.file");
	}
	public String getModelEvaluateOrgPairsFileName(){
		return (String)properties.get("run.lucene.evaluate.org.pairs.file");
	}
	///
	public String getModelOrgTrainFileName(){
		return (String)properties.get("run.lucene.org.train.file");
	}
	public String getModelOrgTestFileName(){
		return (String)properties.get("run.lucene.org.test.file");
	}
	public String getModelTrainPairsFileName(){
		return (String)properties.get("run.lucene.train.pairs.file");
	}
	public String getModelTestPairsFileName(){
		return (String)properties.get("run.lucene.test.pairs.file");
	}
	public String getModelTestPairsLearnOutputFileName(){
		return (String)properties.get("run.lucene.test.pairs.file.learn");
	}

	public String getModelBinaryFileName(){
		return (String)properties.get("run.lucene.model.binary.file");
	}
	public Object getModel(){
		return model;
	}

	public void loadModel(){
		logger.info("************ Before loading model, current time(ms)" + System.currentTimeMillis() );
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
	 * Expand both sides of the phenotype description pairs
	 */
	public StringBuilder expandPhenotypeString(StringBuilder firstPhenotypeStringBuilder, 
			StringBuilder secondPhenotypeStringBuilder){

		StringBuilder dataBuilder = new StringBuilder();
		dataBuilder
		.append(expandWithDictionaryForString(firstPhenotypeStringBuilder.toString()))
		.append("\t")
		.append(expandWithDictionaryForString(secondPhenotypeStringBuilder.toString()));
		return dataBuilder;
	}

	/*
	 * Expand one side of the phenotype description pairs
	 */
	public String expandPhenotypeString(String phenotypeString){
		return expandWithDictionaryForString(phenotypeString);
	}

	/*
	 * Expand the phenotype definition with results from the dictionary index for lucene
	 */
	public String expandWithDictionaryForString(String data) {
		System.out.println("LuceneDict"+ luceneDict);
		return luceneDict.expandWithDictionaryFromTopLuceneIndexTerms(
				MWDictionary.preprocessDictionaryFrequentWords(data));
	}
}
