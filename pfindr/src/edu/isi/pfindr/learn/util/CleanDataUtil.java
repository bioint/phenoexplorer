package edu.isi.pfindr.learn.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.englishStemmer;

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
 * * CleanDataUtil.java 
 * 
 * Utility functions for data pre-processing; stemming,tokenization,bi-gram construction 
 * 
 * @author sharma@isi.edu 
 * 
 */
public class CleanDataUtil {

	private static englishStemmer stemmer = new englishStemmer(); ///////MOVE TO PREPROCESS
	private static Set<String> stopwords = new HashSet<String>(); ///////MOVE TO PREPROCESS
	private static final Pattern digitPattern = Pattern.compile("\\d"); ////MOVE TO PREPROCESS

	static private Logger logger = Logger.getLogger("AppLogging");    
	
	public static void loadStopwordFile(String pathname) {
		logger.info("Loading stop words ..");
		BufferedReader br;
		String thisLine;
		try {
			br = new BufferedReader(new FileReader(pathname));
			while ((thisLine = br.readLine()) != null) {
				thisLine = thisLine.trim();
				if (thisLine.equals(""))
					continue;
				stopwords.add(thisLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*
	 * Words containing special characters : ?, (), (, ), !, /, :, ?, /, ', -
	 * 
	 * These characters are replaced with single whitespace : ?, (), (, ), !, /, :, ?, /, '
	 * Extra whitespace is removed
	 * For words containing hyphen(-) e.g. aa-bb, they are replaced with: aabb, aa, bb
	 */
	public static void preProcessWordsSpecialCharacters(String line, StringBuilder newLine){

		//System.out.println("Preprocess words with special characaters ...");
		List<String> newLineList = new ArrayList<String>();
		String eachWord;
		String[] hyphenWordArray;
		//replace all forward slash in the string with whitespace
		//line = line.replaceAll("[/|:|?|..|adverb|noun]", " ");
		//System.out.println("Line before modification:  "+ line);
		line = line.replaceAll("(\\?)|(\\()|(\\))|(\\,)|(\\.)|(\\!)|(\\/)|(\\:)|(\\?)|(\\')|(\\])|(\\[)", " ");
		//System.out.println("Line after first modification: "+ line);
		line = line.replaceAll("\\s+", " ");

		newLineList = Arrays.asList(line.split("\\s+"));
		Iterator<String> iter = newLineList.iterator();

		//replace the hyphen words like aaa-bb-ccc with aaabbccc, aaa, bb, cc
		while(iter.hasNext()){
			eachWord = (String)iter.next();
			newLine.append(" ").append(eachWord);
			if(eachWord.contains("-")){ //if the word contains a hyphen
				//System.out.println("Word containing hyphen: "+ eachWord);
				hyphenWordArray = eachWord.split("\\-");
				//adding aaabbccc for aaa-bb-ccc 
				newLine.append(" ").append(eachWord.replaceAll("\\-", ""));
				for(int i = 0; i < hyphenWordArray.length; i++ )
					//adding aaa, bb, cc for aaa-bb-ccc 
					newLine.append(" ").append(hyphenWordArray[i]);
			}
		}
		//System.out.println("Line after modification: "+ newLine.toString());
	}

	/*
	 * Words containing special characters : ?, (), (, ), !, /, :, ?, /, ', -
	 * 
	 * These characters are replaced with single whitespace : ?, (), (, ), !, /, :, ?, /, '
	 * Extra whitespace is removed
	 * For words containing hyphen(-) e.g. aa-bb, they are replaced with: aabb, aa, bb
	 */
	public static String preProcessWordsSpecialCharacters(String line){

		StringBuilder newLine = new StringBuilder();
		//System.out.println("Preprocess words with special characaters ...");
		List<String> newLineList = new ArrayList<String>();
		String eachWord;
		String[] hyphenWordArray;
		//replace all forward slash in the string with whitespace
		//line = line.replaceAll("[/|:|?|..|adverb|noun]", " ");
		//System.out.println("Line before modification:  "+ line);
		line = line.replaceAll("(\\?)|(\\()|(\\))|(\\,)|(\\.)|(\\!)|(\\/)|(\\:)|(\\?)|(\\')|(\\])|(\\[)", " ");
		//System.out.println("Line after first modification: "+ line);
		line = line.replaceAll("\\s+", " ");

		newLineList = Arrays.asList(line.split("\\s+"));
		Iterator<String> iter = newLineList.iterator();

		while(iter.hasNext()){
			eachWord = (String)iter.next();
			newLine.append(" ").append(eachWord);
			if(eachWord.contains("-")){ //if the word contains a hyphen
				//System.out.println("Word containing hyphen: "+ eachWord);
				hyphenWordArray = eachWord.split("\\-");
				//adding aaabbccc for aaa-bb-ccc 
				newLine.append(" ").append(eachWord.replaceAll("\\-", ""));
				for(int i = 0; i < hyphenWordArray.length; i++ )
					//adding aaa, bb, cc for aaa-bb-ccc 
					newLine.append(" ").append(hyphenWordArray[i]);
			}/*else{ //if the word does not contain hyphen, add it as it is
				newLine.append(" ").append(eachWord);
			}*/
		}
		//System.out.println("Line after modification: "+ newLine.toString());
		return newLine.toString();
	}

	/* Preprocess data
	 * Remove stop words, stem, tokenize
	 */
	public static String preprocessStemAndTokenize(String data) {

		Set<String> transformedSet = new HashSet<String>(); //Set will make sure only unique terms are kept
		StringBuilder strBuilder = new StringBuilder();
		Tokenizer analyzer = new Tokenizer(Version.LUCENE_30);
		TokenStream tokenStream = analyzer.tokenStream("", new StringReader(data));
		TermAttribute termAttribute;
		String term;
		//System.out.println("The value of data in tokenizeAndStem: "+ data);
		try {
			while (tokenStream.incrementToken()) {
				termAttribute = tokenStream.getAttribute(TermAttribute.class);
				term = termAttribute.term(); 
				if (stopwords.contains(term)){ //ignore stopwords
					//System.out.println("Contains stopword: "+ term);
					continue;
				}
				if (digitPattern.matcher(term).find()) //ignore digits
					continue;
				if(term.length() <= 1) //ignore 1 letter words
					continue;
				
				if (!digitPattern.matcher(term).find()){ //ignore digits
					stemmer.setCurrent(term);
					stemmer.stem();
					transformedSet.add(stemmer.getCurrent());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println("transormed set size in tokenizeAndStem: "+ transformedSet.size());
		for(Object token: transformedSet.toArray()){
			strBuilder.append(token).append(" ");
		}
		//System.out.println("String returned in tokenizeAndStem:"+ strBuilder.toString());
		return strBuilder.toString();
	}
	
	public static String preprocessRemoveStopWords(String data) {

		StringBuilder strBuilder = new StringBuilder();
		Tokenizer analyzer = new Tokenizer(Version.LUCENE_30);
		TokenStream tokenStream = analyzer.tokenStream("", new StringReader(data));
		TermAttribute termAttribute;
		String term;
		//System.out.println("The value of data in tokenizeAndStem: "+ data);
		try {
			while (tokenStream.incrementToken()) {
				termAttribute = tokenStream.getAttribute(TermAttribute.class);
				term = termAttribute.term();
				if (digitPattern.matcher(term).find()) //ignore digits
					continue;
				if(term.length() <= 1)
					continue;
				if (stopwords.contains(term))
					continue;
				strBuilder.append(term).append(" ");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println("String returned in tokenizeAndStem:"+ strBuilder.toString());
		return strBuilder.toString().trim();
	}

	/*
	 * Remove special characters : ?, (), , !, /, :, ?, ', [, ], {, } are replaced with whote space
	 * Extra white space is removed
	 */
	public static String removeSpecificCharacters(String line){	
		//System.out.println("\nLine before modification: "+ line);
		line = line.replaceAll("(\\?)|(\\()|(\\))|(\\,)|(\\.)|(\\!)|(\\:)|(\\/)|(\\')|(\\])|(\\[)|(\\})|(\\{)|(\\;)|(\")", " ");
		line = line.replaceAll("\\s+", " ");
		//System.out.println("\nLine after modification: "+ line);
		return line;
	}

	/*
	 * Preprocess variable: removes stop words, stems, tokenizes and gets token uni/bi-grams from the variable description
	 */
	public static Set<String> preprocessStemAndTokenizeAddBigramsInSet(String data) {
		//System.out.println("Preprocess data, remove stop words, stem, tokenize and get bi-grams ..");

		Set<String> transformedSet = new LinkedHashSet<String>();
		List<String> stemmedList = new ArrayList<String>();

		//System.out.println("Stop words length:" + stopwords.size());
		Tokenizer analyzer = new Tokenizer(Version.LUCENE_30);
		TokenStream tokenStream = analyzer.tokenStream("", new StringReader(data));
		TermAttribute termAttribute;
		String term;
		try {
			while (tokenStream.incrementToken()) {
				termAttribute = tokenStream.getAttribute(TermAttribute.class);
				term = termAttribute.term();
				if (digitPattern.matcher(term).find()) //ignore digits
					continue;
				if (stopwords.contains(term)) //ignore stopwords
					continue;
				if (term.length() <= 1) //ignore single letter words
					continue;
				stemmer.setCurrent(term);
				stemmer.stem();
				stemmedList.add(stemmer.getCurrent());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String[] ds = stemmedList.toArray(new String[0]);

		/*for(int i=0; i<stemmedList.size(); i++)
			System.out.print(ds[i]+"\t");*/

		//add bi-grams
		final int size = 2;
		for (int i = 0; i < ds.length; i++) {
			transformedSet.add(ds[i]); //add single words
			if (i + size <= ds.length) {
				String t = "";
				for (int j = i; j < i + size; j++) {
					t += " " + ds[j];
				}
				t = t.trim().replaceAll("\\s+", "_");
				transformedSet.add(t); //add bi-gram combined with "_"
			}
		}
		//System.out.println(" ")
		stemmedList.clear();
		stemmedList = null;
		ds = null;
		return transformedSet;
	}

	/*
	 * Preprocess variable: removes stop words, stems, tokenizes the variable description, returning a distinct tokens
	 */
	public static String preprocessStemAndTokenizeReturnDistinctTokens(String data) {
		//System.out.println("Preprocess data, remove stop words, stem, tokenize ..");
		Set<String> transformedSet = new LinkedHashSet<String>();
		List<String> stemmedList = new ArrayList<String>();

		Tokenizer analyzer = new Tokenizer(Version.LUCENE_30);
		TokenStream tokenStream = analyzer.tokenStream("", new StringReader(data));
		TermAttribute termAttribute;
		String term;
		try {
			while (tokenStream.incrementToken()) {
				termAttribute = tokenStream.getAttribute(TermAttribute.class);
				term = termAttribute.term();
				if (digitPattern.matcher(term).find()) //ignore digits
					continue;
				if (stopwords.contains(term)) //ignore stopwords
					continue;
				if (term.length() <= 1) //ignore single letter words
					continue;
				stemmer.setCurrent(term);
				stemmer.stem();
				stemmedList.add(stemmer.getCurrent());
			}
			transformedSet.addAll(stemmedList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		stemmedList.clear();
		stemmedList = null;
		
		return StringUtils.join(transformedSet.toArray(), " ");
	}


	/*
	 * Preprocess variable: removes stop words, stems, tokenizes and gets token uni/bi-grams from the variable description
	 */
	public static String preprocessStemAndTokenizeAddBigramsInString(String data) {
		//System.out.println("Preprocess data, remove stop words, stem, tokenize and get bi-grams ..");

		Set<String> transformedSet = new LinkedHashSet<String>();
		List<String> stemmedList = new ArrayList<String>();

		Tokenizer analyzer = new Tokenizer(Version.LUCENE_30);
		TokenStream tokenStream = analyzer.tokenStream("", new StringReader(data));
		TermAttribute termAttribute;
		String term;
		try {
			while (tokenStream.incrementToken()) {
				termAttribute = tokenStream.getAttribute(TermAttribute.class);
				term = termAttribute.term();
				if (digitPattern.matcher(term).find()) //ignore digits
					continue;
				if (stopwords.contains(term)) //ignore stopwords
					continue;
				if (term.length() <= 1) //ignore stopwords
					continue;
				stemmer.setCurrent(term);
				stemmer.stem();
				stemmedList.add(stemmer.getCurrent());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String[] ds = stemmedList.toArray(new String[0]);

		/*for(int i=0; i<stemmedList.size(); i++)
			System.out.print(ds[i]+"\t");*/

		//add bi-grams
		final int size = 2;
		for (int i = 0; i < ds.length; i++) {
			transformedSet.add(ds[i]); //add single words
			if (i + size <= ds.length) {
				String t = "";
				for (int j = i; j < i + size; j++) {
					t += " " + ds[j];
				}
				t = t.trim().replaceAll("\\s+", "_");
				transformedSet.add(t); //add bi-gram combined with "_"
			}
		}
		//System.out.println(transformedSet.toArray(new String[transformedSet.size()]).toString());
		return StringUtils.join( transformedSet.toArray(new String[transformedSet.size()]), " ");
		
	}
}
