package edu.isi.pfindr.learn.search;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.isi.pfindr.learn.util.SortMap;
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
 * Augment a query term searched for, in the indexed dictionary with the top(most frequent) terms 
 * across the top documents retrieved in the results.
 * 
 * A query term is searched for, in a dictionary index, the top k results are retrieved. 
 * Then the most frequent terms are retrieved from the top documents and appended to the original query term
 * 
 * This is only used if dict.aug.method in the properties file is set to lucene
 * 
 * @author sharma@isi.edu 
 * 
 */

public class LuceneDictionaryAugmenter {

	private int HIT_COUNT;
	private int MAX_DICTIONARY_TERMS; 

	private String stopWordsDirectory;// = "data/data2/";
	//private static String INDEX_PATH = "data/data2/dict_index_clean";
	private String INDEX_PATH;
	
	//private static File indexFile = new File(INDEX_PATH);
	private static Directory indexDir = null;

	public LuceneDictionaryAugmenter(Properties properties){
		
		readProperties(properties);
		try{
			indexDir = FSDirectory.open(new File(INDEX_PATH));
		}catch (CorruptIndexException ce) {
			ce.printStackTrace();
		}catch (IOException io) {
			io.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getIndexPath(){
		return INDEX_PATH;
	}

	private void readProperties(Properties properties){
		/*
		 * Properties used: dict.aug.method,dict.aug.method.lucene.stem.detail,stopwords.file.path
		 * dict.aug.method.lucene.hit.count,dict.aug.method.lucene.max.terms 
		 */
		stopWordsDirectory = (String)properties.get("stopwords.file.path");
		HIT_COUNT = Integer.parseInt((String)properties.get("dict.aug.method.lucene.hit.count"));
		MAX_DICTIONARY_TERMS = Integer.parseInt((String)properties.get("dict.aug.method.lucene.max.terms "));
		
		if(((String)properties.get("dict.aug.method")).equals("mw")){ //using merriam webster dict
			if(((String)properties.get("dict.aug.method.lucene.stem.detail")).equals("stem")){
				INDEX_PATH = (String)properties.get("mw.stem.index.dir");	//path to stemed index
			}else{
				INDEX_PATH = (String)properties.get("mw.nostem.index.dir");	//path to unstemed index
			}
		}else if(((String)properties.get("dict.aug.method")).equals("umls")){ //using umls dict
			if(((String)properties.get("dict.aug.method.lucene.stem.detail")).equals("stem")){
				INDEX_PATH = (String)properties.get("umls.stem.index.dir"); //path to stemed index
			}else{
				INDEX_PATH = (String)properties.get("umls.nostem.index.dir"); //path to unstemed index
			}
		}
	}
	
	public String expandWithDictionaryFromTopLuceneIndexTerms(String data){

		//System.out.println("Original data"+ data);
		StringBuilder dictionaryDataBuilder = new StringBuilder();
		data = data.replaceAll("\\s+", " ");
		dictionaryDataBuilder.append(data);

		try{
			//Construct the query
			//Query q = new QueryParser(Version.LUCENE_30, "id_content", analyzer).parse(data);
			//Query q = new QueryParser(Version.LUCENE_30, "content", analyzer).parse(data);
			
			//IndexReader indexReader = IndexReader.open(indexDir);

			IndexSearcher indexSearcher = new IndexSearcher(indexDir);
			QueryParser queryParser = new QueryParser(Version.LUCENE_30, "content", 
					new StandardAnalyzer(Version.LUCENE_30, 
							new File( ServletContextInfo.getContextPath()+ stopWordsDirectory + "stopwords.txt")));
			//queryParser.setDefaultOperator(QueryParser.AND_OPERATOR);
			Query query = queryParser.parse(data);
			
			//Get the top hits
			TopScoreDocCollector collector = TopScoreDocCollector.create(HIT_COUNT, true);
			//Search dictionary index
			indexSearcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			
			//System.out.println("Hits retrieved:"+ hits.length);

			//Parse through the top hits (number of hits specified by HIT_COUNT above) and 
			//collect the frequency of the terms in a Map
			Map<String,Double> termFreqMap = new HashMap<String, Double>();
			double value;
			for(int i=0;i<hits.length;++i) {
				TermPositionVector v = (TermPositionVector)indexSearcher.getIndexReader()
						.getTermFreqVector(hits[i].doc, "content");
						//.getTermFreqVector(hits[i].doc, "id_content");
				String[] terms = v.getTerms();
				int[] freq = v.getTermFrequencies();
				double[] tfidf = new double[v.getTerms().length];
				double termTotal = 0.0;
				int docTotal = indexSearcher.getIndexReader().numDocs();
				for (int t = 0; t < terms.length; t++) {
					termTotal += freq[t];
				}
				for (int j = 0; j < terms.length; ++j) {
					tfidf[j] = (double) (freq[j] / termTotal) * (1 + Math.log(docTotal / (1 + 
							//(indexSearcher.getIndexReader().docFreq(new Term("id_contents", terms[j]))))));
							(indexSearcher.getIndexReader().docFreq(new Term("content", terms[j]))))));

					if(!termFreqMap.containsKey(terms[j])){//if the map does not already contain the phenotype
						termFreqMap.put(terms[j],tfidf[j]);
					}else{ //else add to the existing value
						value = termFreqMap.get(terms[j]).doubleValue() > tfidf[j] ? 
								termFreqMap.get(terms[j]).doubleValue() : tfidf[j];
								//value = ((Double)termFreqMap.get(terms[j])).doubleValue() + tfidf[j];
								termFreqMap.put(terms[j], value);
					}
				}
			}
			//Append the original query term with the top (specified by MAX_DICTIONARY_TERMS) most frequent terms
			if(hits.length > 0){
				value = 0; //reusing variable as an index now
				//System.out.println("Sorted Map......");
				Map<String,String> sortedMap =  SortMap.sortByComparator(termFreqMap);
				//Include the top 10 matches from the dictionary definition
				for (Map.Entry entry : sortedMap.entrySet()) {
					dictionaryDataBuilder.append(" ")
					.append((((String)entry.getKey()).replaceAll("\\t"," ")).replaceAll("\\s+"," "));
					if(value++ > MAX_DICTIONARY_TERMS) //get the top 10 terms
						break;
					//System.out.println("Key : " + entry.getKey() 
					//+ " Value : " + entry.getValue());
				}
			}
			// close searcher, no need to access the documents any more. 
			indexSearcher.close();
		}catch (CorruptIndexException ce) {
			ce.printStackTrace();
		}catch (IOException io) {
			io.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println(" Expand word with dictionary .."+ dictionaryDataBuilder.toString());
		return dictionaryDataBuilder.toString();
	}

}
