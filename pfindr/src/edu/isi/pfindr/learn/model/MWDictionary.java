package edu.isi.pfindr.learn.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
 * One of the implementations for dictionary. The factory for dictionary calls this implementation if the user chooses dict.type = mw
 * 
 *  @author  Shefali Sharma
 */
public class MWDictionary implements Dictionary{

	static final long serialVersionUID = 42L;
	private String dictionaryPath;
	MWDictionary(Properties properties){
		this.dictionaryPath = ServletContextInfo.getContextPath() + (String)properties.get("mw.file.path");
	}
	
	public String getDictionaryPath(){
		return dictionaryPath;
	}
	
	public Map<String,String> loadDictionary() {
		System.out.println("Loading dictionary MW ..");

		BufferedReader br;
		String thisLine;
		Map<String, String> dictionaryMap = new HashMap<String, String>();
		String temp;
		try {
			br = new BufferedReader(new FileReader(dictionaryPath));

			while ((thisLine = br.readLine()) != null) {
				thisLine = thisLine.trim();
				if (thisLine.equals(""))
					continue;
				String[] fields = thisLine.split("\\t");
				if (fields.length == 2){
					temp = preprocessDictionaryFrequentWords(fields[1]);
					dictionaryMap.put(fields[0], temp);
					//System.out.println(fields[0] + " : "+ fields[1]);
				}
			}
		} catch(IOException io){
			io.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return dictionaryMap;
	}
	
	/*
	 * Replacing all occurrences of words included as part of the definition in the MW medical dictionary, that should not be ideally included.
	 * TODO : Redo dictionary construction without using these words
	 */
	public static String preprocessDictionaryFrequentWords(String line){
		//System.out.println("Preprocess dictionary frequent words ..");
		line = line.replaceAll("(transitive verb)|(verb)|(noun)|(plural)|(intransitive verb)|(variants)|(adjective)", " ");
		line = line.replaceAll("(the part of the)|(a product of)|(the process of)|(see)", " ");
		line = line.replaceAll("(of or)|(relating)|(characterised)|(characteristic)|(consisting)|(or utilizing the)|(or constituting a)|(or employed in)", " ");
		line = line.replaceAll("(british)|(also british)|(similar)|(closely related to)|(variant)|(act or process of)|(variants of)|(of or similar to)", " ");
		line = line.replaceAll("\\s+", " ");

		return line;
	}
}
