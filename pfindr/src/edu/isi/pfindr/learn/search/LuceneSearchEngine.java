package edu.isi.pfindr.learn.search;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.standard.ParseException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.isi.pfindr.learn.util.CleanDataUtil;
import edu.isi.pfindr.learn.util.ReadProperties;
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
 * * LuceneSearchEngine.java 
 * 
 * Search the Lucene Index and return results
 * 
 * @author sharma@isi.edu 
 * 
 */
public class LuceneSearchEngine {

	private static final String LUCENE_ESCAPE_CHARS = "[\\\\+\\-\\!\\(\\)\\:\\^\\[\\]\\{\\}\\~\\*\\?]";
	private static final Pattern LUCENE_PATTERN = Pattern.compile(LUCENE_ESCAPE_CHARS);
	private static final String REPLACEMENT_STRING_ESCAPE = "\\\\$0";

	private static Properties properties = ReadProperties.readProperties();
	private static String pathToIndexDir = ServletContextInfo.getContextPath() + 
			(String)properties.get("variable.index.dir");
	private static IndexSearcher indexSearcher = null;
	
	public static IndexSearcher getIndexSearcher(){
		try{
			// RAMDirectory idx = new RAMDirectory(); //TODO
			if(indexSearcher == null)
				indexSearcher = new IndexSearcher(FSDirectory.open(new File(pathToIndexDir)));
		}catch (ParseException pe) {
			pe.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return indexSearcher;
	}

	public static void closeIndexSearcher() throws IOException {
		if (indexSearcher != null) {
			indexSearcher.close();
		}
	}

	public static Map<String, Double> search(String queryString, 
			String descriptionExpandedNotStemmed, String descriptionExpandedStemmed) {

		Map<String,Double> searchResultMap = new HashMap<String,Double>();
		//Escape special characters in Lucene
		String originalDefinitionEscaped =  LUCENE_PATTERN.matcher(queryString).replaceAll(REPLACEMENT_STRING_ESCAPE).toLowerCase();
		descriptionExpandedNotStemmed =  LUCENE_PATTERN.matcher(descriptionExpandedNotStemmed).replaceAll(REPLACEMENT_STRING_ESCAPE).toLowerCase();
		descriptionExpandedStemmed =  LUCENE_PATTERN.matcher(descriptionExpandedStemmed).replaceAll(REPLACEMENT_STRING_ESCAPE).toLowerCase();
				
		try{
			String originalDefinitionStemmedQuery = CleanDataUtil.preprocessStemAndTokenize(queryString.toLowerCase()).trim();
			StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
			Query query;

			//Get the top hits
			TopScoreDocCollector collector = TopScoreDocCollector.create(100000, true);
			indexSearcher = getIndexSearcher();

			BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
			//+(+contents:hello +contents:world) +priority:high
			//"jakarta apache"^4 "Apache Lucene"
			
			////////////
			if(!originalDefinitionStemmedQuery.equals("")){
				originalDefinitionStemmedQuery = LUCENE_PATTERN.matcher(originalDefinitionStemmedQuery)
						.replaceAll(REPLACEMENT_STRING_ESCAPE);
				String[] fields = new String[] { "content", "contentStemmed","contentExpanded","contentExpandedStemmed" };
				String[] queries = new String[] {
					"\""+originalDefinitionEscaped.trim().toLowerCase()+"\"^8 "+ 
							originalDefinitionEscaped.trim().toLowerCase(),
					originalDefinitionStemmedQuery +"^3",
					descriptionExpandedNotStemmed,
					descriptionExpandedStemmed};
				query = MultiFieldQueryParser.parse(Version.LUCENE_30, queries, fields, analyzer);
				
			}else{
				QueryParser queryParser = new QueryParser(Version.LUCENE_30, "content", analyzer);
					
				query = queryParser.parse("\""+originalDefinitionEscaped.trim().toLowerCase()+"\"^8 "+ 
						originalDefinitionEscaped.trim().toLowerCase());
			}
			////////
			
			indexSearcher.search(query, collector);
			ScoreDoc[] scoreDocs = collector.topDocs().scoreDocs;
			int hitCount = collector.getTotalHits();
			if (hitCount > 0){
				//System.out.println("Hits for \"" +  queryString + "\" were found by:");
				// Iterate over the Documents in the Hits object
				ScoreDoc scoreDoc;
				for (int i = 0; i < hitCount; i++) {

					scoreDoc = scoreDocs[i];
					//System.out.println("docId: " + scoreDoc.doc + "\t" + "docScore: " + scoreDoc.score);
					Document doc = indexSearcher.doc(scoreDoc.doc);
					//System.out.println("  " + (i + 1) + ". " + doc.get("id"));
					//System.out.println("Content: " + doc.get("orgContent"));   
					if(!searchResultMap.containsKey((String)doc.get("orgContent")))
						searchResultMap.put(((String)doc.get("orgContent")), new Double(scoreDoc.score));
				}
			}
			analyzer = null;
		}catch (org.apache.lucene.queryParser.ParseException pe) {
			// TODO Auto-generated catch block
			pe.printStackTrace();
		}catch(IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		}finally {
			/*try{
				closeIndexSearcher();
			}catch(IOException ioe){
				ioe.printStackTrace();
			}*/
		}
		return searchResultMap;
	}
}

