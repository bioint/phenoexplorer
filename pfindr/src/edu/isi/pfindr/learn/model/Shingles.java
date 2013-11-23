package edu.isi.pfindr.learn.model;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;

import edu.isi.pfindr.learn.util.Tokenizer;

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
 * Computes shingles, shingles or n-gram. An n-gram is a consecutive sequence of words of fixed window size n.
 * This class computes bi-gram, tri-gram, and tetra-grams for the variable description. For example, for a variable description: Age Diagnosed Coronary Bypass. 
 * The shingles or n-gram words will be ‘age diagnosed coronary bypass’, ‘age diagnosed coronary’, ‘diagnosed coronary bypass’, age diagnosed’, ‘diagnosed coronary’, ‘coronary bypass’.
 *  
 *  @author  Shefali Sharma
 */

public class Shingles {

	static private Logger logger = Logger.getLogger("AppLogging");    
	public static List<String> computeShingles(String data){

		//System.out.println("I an here");
		data = data.toLowerCase();
		List<String> shingleList = new ArrayList<String>();
		//System.out.println("DATA inside expandWithDictionaryForShingles "+ data);
		try{  
			Tokenizer analyzer = new Tokenizer(Version.LUCENE_30);
			TokenStream tokenStream = analyzer.tokenStream("", new StringReader(data));
			ShingleFilter filter = new ShingleFilter(tokenStream, 4); 
			filter.setOutputUnigrams(false); 
			TermAttribute termAtt = (TermAttribute) filter.getAttribute(TermAttribute.class); 

			//System.out.print("Printing the shingles ");
			while (filter.incrementToken()) {  
				shingleList.add(termAtt.term().trim()); //.replaceAll("_", " ").replaceAll("\\s+", " ").trim());
				//System.out.print(termAtt.term()+ "\t"); 
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		logger.info("Shingle List size returned: "+ shingleList.size());
		return shingleList;
	}
}
