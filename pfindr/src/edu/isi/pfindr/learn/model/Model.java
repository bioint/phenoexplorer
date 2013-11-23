package edu.isi.pfindr.learn.model;

import java.io.Serializable;

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
 * Model.java 
 * Interface with method signatures for training/testing/accessing the learned binary model, 
 * implemented by different implementations of classes, depending on user selections.
 * Implementing classes:
 * * NoDictionaryModel
 * * LuceneDictionaryModel
 * * ShinglesDatabaseDictionaryModel
 * * ShinglesDatabaseDictionaryModel
 * 
 * The correct instance is created by the ModelFactory, depending on the user selection 
 * 
 * @author  Shefali Sharma
 *
 */ 
public interface Model extends Serializable{
	
	static final long serialVersionUID = 42L;
	
	String getModelName();
	String getModelDirectory();
	String getModelEvaluateOrgFileName();
	String getModelEvaluateOrgPairsFileName();
	String getModelOrgTrainFileName();
	String getModelOrgTestFileName();
	String getModelTrainPairsFileName();
	String getModelTestPairsFileName();
	String getModelTestPairsLearnOutputFileName();
	String getModelBinaryFileName();
	Object getModel();
	void loadModel();
	
	StringBuilder expandPhenotypeString(StringBuilder firstPhenotypeStringBuilder, 
			StringBuilder secondPhenotypeStringBuilder);
	
	String expandPhenotypeString(String phenotypeString);
	
	String expandWithDictionaryForString(String data);
}
