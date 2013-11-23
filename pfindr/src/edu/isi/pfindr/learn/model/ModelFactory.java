package edu.isi.pfindr.learn.model;

import java.util.Properties;

import edu.isi.pfindr.learn.util.ReadProperties;

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
 * ModelFactory.java 
 * The correct instance of the Model is created by the ModelFactory, depending on the user selection 
 * * NoDictionaryModel
 * * LuceneDictionaryModel
 * * ShinglesDatabaseDictionaryModel
 * * ShinglesDatabaseDictionaryModel
 * 
 * @author  Shefali Sharma
 *
 */ 
public class ModelFactory {

	private static Model model;
	public static Model createModel() {
		if(model == null){
			Properties properties = ReadProperties.readProperties();
			if (properties.get("dict.aug.method").equals("nodict")){
				model = new NoDictionaryModel(properties);
			}else if (properties.get("dict.aug.method").equals("lucene")){
				model = new LuceneDictionaryModel(properties);
			}else if (properties.get("dict.aug.method").equals("shingle")){
				model = new ShinglesDictionaryModel(properties);
			}else if (properties.get("dict.aug.method").equals("shingle-db")){
				model = new ShinglesDatabaseDictionaryModel(properties);
			}else{
				throw new IllegalArgumentException("No such model");
			}
		}
		return model;
	}
}
