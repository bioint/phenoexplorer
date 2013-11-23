package edu.isi.pfindr.learn.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Properties;

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
 * One of the implementations for dictionary expansion. The factory for dictionary expansion calls this implementation if the user chooses dict.aug.method as nodict
 * 
 *  @author  Shefali Sharma
 */
public class NoDictionaryModel implements Model{

	static final long serialVersionUID = 42L;
	transient Properties properties;
	Object model = null;
	
	NoDictionaryModel(Properties properties){
		this.properties = properties;
		loadModel();
	}
	public String getModelName(){
		return (String)properties.get("dict.aug.method");
	}
	public String getModelDirectory(){
		return (String)properties.get("run.nodict.dir");
	}
	public String getModelEvaluateOrgFileName(){
		return (String)properties.get("run.nodict.evaluate.org.file");
	}
	public String getModelEvaluateOrgPairsFileName(){
		return (String)properties.get("run.nodict.evaluate.org.pairs.file");
	}
	public String getModelOrgTrainFileName(){
		return (String)properties.get("run.nodict.org.train.file");
	}
	public String getModelOrgTestFileName(){
		return (String)properties.get("run.nodict.org.test.file");
	}
	public String getModelTrainPairsFileName(){
		return (String)properties.get("run.nodict.train.pairs.file");
	}
	public String getModelTestPairsFileName(){
		return (String)properties.get("run.nodict.test.pairs.file");
	}
	public String getModelTestPairsLearnOutputFileName(){
		return (String)properties.get("run.nodict.test.pairs.file.learn");
	}
	public String getModelBinaryFileName(){
		return (String)properties.get("run.nodict.model.binary.file");
	}

	public Object getModel(){
		return model;
	}

	public void loadModel(){
		System.out.println("Before loading model");
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
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("After loading model");
	}

	public StringBuilder expandPhenotypeString(StringBuilder firstPhenotypeStringBuilder, 
			StringBuilder secondPhenotypeStringBuilder){

		StringBuilder dataBuilder = new StringBuilder();
		dataBuilder.append(firstPhenotypeStringBuilder.toString()).append("\t")
			.append(secondPhenotypeStringBuilder.toString());
		return dataBuilder;
	}

	/*
	 * No expansion with dictionary
	 */
	public String expandPhenotypeString(String phenotypeString){
		return phenotypeString;
	}

	public String expandWithDictionaryForString(String data){
		return data;
	}
}
