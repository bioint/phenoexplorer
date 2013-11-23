package edu.isi.pfindr.learn.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
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
 * One of the implementations for dictionary. The factory for dictionary calls this implementation if the user chooses dict.type = umls
 * 
 *  @author  Shefali Sharma
 */

public class UMLSDictionary implements Dictionary{

	static final long serialVersionUID = 42L;
	private String dictionaryPath;
	UMLSDictionary(Properties properties){
		this.dictionaryPath = ((String)properties.get("umls.file.path")).trim();
	}

	public String getDictionaryPath(){
		return dictionaryPath;
	}

	public Map<String,String> loadDictionary() {
		System.out.println("Loading dictionary UMLS ..");

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
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dictionaryMap;
	}
	
	public static String preprocessDictionaryFrequentWords(String line){
		//System.out.println("Preprocess dictionary frequent words ..");
		
		//System.out.println("  line before : "+ line);
		
		line = line.replaceAll("&quot", "")
				.replaceAll("(?i)McGraw-Hill Dictionary of Scientific and Technical Terms 6th ed", "")
				.replaceAll("(?i)From Stedman 25th ed"," ").replaceAll("(?i)NIH", " ").replaceAll("(?i)NCI", " ")
				.replaceAll("(?i)National Heart Lung and Blood Institute", " ")
				.replaceAll("(?i)National Institute of Dental and Craniofacial Research", " ")
				.replaceAll("(?i)MeSH"," ")
				.replaceAll("\\[.*:+.*\\]", "").replaceAll("\\s+", " ");

		//System.out.println("  line after : "+ line);
		return line;
	}
}