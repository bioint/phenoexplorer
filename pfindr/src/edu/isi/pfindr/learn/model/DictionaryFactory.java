package edu.isi.pfindr.learn.model;

import java.util.Properties;

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
 * DictionaryFactory.java 
 * The correct instance of the Dictionary is created by the DictionaryFactory, depending on the user selection (dict.type)
 * * UMLSDictionary
 * * MWDictionary
 * 
 * @author  Shefali Sharma
 *
 */ 
public class DictionaryFactory {

	public static Dictionary chooseDictionary (Properties properties) {

		if (properties.get("dict.type").equals("mw")){
			return new MWDictionary(properties);
		}else if (properties.get("dict.type").equals("umls")){
			return new UMLSDictionary(properties);
		}	
		throw new IllegalArgumentException("No such dictionary");
	}
}
