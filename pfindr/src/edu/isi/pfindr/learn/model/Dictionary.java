package edu.isi.pfindr.learn.model;

import java.io.Serializable;
import java.util.Map;

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
 * Dictionary.java 
 * Interface accessed by implementing classes:
 * * UMLSDictionary
 * * MWDictionary
 * The correct instance is created by the DictionaryFactory, depending on the user selection 
 * 
 * @author  Shefali Sharma
 *
 */ 

public interface Dictionary extends Serializable{

	static final long serialVersionUID = 42L;
	Map<String,String>loadDictionary();
	String getDictionaryPath();
}
