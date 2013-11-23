package edu.isi.pfindr.learn.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.isi.pfindr.learn.db.DatabaseQueryInterface;
import edu.isi.pfindr.learn.model.DomainKnowledge;
import edu.isi.pfindr.learn.model.Model;
import edu.isi.pfindr.learn.model.ModelFactory;
import edu.isi.pfindr.learn.model.ShinglesDatabaseDictionaryModel;
import edu.isi.pfindr.learn.model.UMLSDictionary;
import edu.isi.pfindr.learn.util.CleanDataUtil;
import edu.isi.pfindr.learn.util.ReadProperties;
import edu.isi.pfindr.listeners.ServletContextInfo;

/*
 * Creates a Lucene index of all the dbGaP variables.
 * 
 */
public class Indexer {

	/** Creates a new instance of Indexer */
	public Indexer() {}

	private IndexWriter indexWriter = null;
	private static Logger logger = Logger.getLogger("AppLogging"); 
	private static final String LUCENE_ESCAPE_CHARS = "[\\\\+\\-\\!\\(\\)\\:\\^\\[\\]\\{\\}\\~\\*\\?]";
	private static final Pattern LUCENE_PATTERN = Pattern.compile(LUCENE_ESCAPE_CHARS);
	private static final String REPLACEMENT_STRING_ESCAPE = "\\\\$0";

	public IndexWriter getIndexWriter(String path, StandardAnalyzer analyzer) throws IOException {
		Directory dir = FSDirectory.open(new File(path));
		if (indexWriter == null) {
			indexWriter = new IndexWriter(dir, analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
		}
		return indexWriter;
	}    

	public void closeIndexWriter() throws IOException {
		if (indexWriter != null) {
			indexWriter.close();
		}
	}

	public void indexDict(String word, String originalDefinition, String definition, 
			String definitionStemmed,String definitionExpanded, String definitionExpandedStemmed,
			String path, StandardAnalyzer analyzer) throws Exception {

		logger.info("Indexing dictionary : " + word );
		logger.info("Indexing dictionary original content : " + originalDefinition );
		logger.info("Indexing dictionary content : " + definition );
		logger.info("Indexing dictionary stemmed content : " + definitionStemmed );
		logger.info("Indexing dictionary expanded content : " + definitionExpanded );
		logger.info("Indexing dictionary expanded stemmed content : " + definitionExpandedStemmed );

		IndexWriter index = getIndexWriter(path, analyzer);
		Document doc = new Document();
		doc.add(new Field("id", word, Field.Store.YES, Field.Index.NO));
		doc.add(new Field("orgContent", originalDefinition, Field.Store.YES, Field.Index.NO));

		Field fieldUnStemmed = new Field("content", definition, Field.Store.YES, Field.Index.ANALYZED);
		Field fieldStemmed = new Field("contentStemmed", definitionStemmed, Field.Store.YES, Field.Index.ANALYZED);
		Field fieldExpanded = new Field("contentExpanded", definitionExpanded, Field.Store.YES, Field.Index.ANALYZED);
		Field fieldExpandedStemmed = new Field("contentExpandedStemmed", definitionExpandedStemmed, Field.Store.YES, Field.Index.ANALYZED);

		fieldUnStemmed.setBoost(2f);
		fieldStemmed.setBoost(1.5f);

		doc.add(fieldUnStemmed);
		doc.add(fieldStemmed);
		doc.add(fieldExpanded);
		doc.add(fieldExpandedStemmed);

		index.addDocument(doc);
	}

	public static void main(String[] args){   
		
		String basePath = "";
		//Get the absolute path from the command-line
		if (args.length > 0) {
			try {
				basePath = args[0];
				ServletContextInfo.setContextPath(basePath);
			} catch (Exception e) {
				System.err.println("Base Path not correctly specified!!");
				System.exit(1);
			}
		}

		Properties properties = ReadProperties.readProperties();
		String variableFilename = ServletContextInfo.getContextPath() + (String)properties.get("dbgap.variable.index.file");
		String pathToIndexDir = ServletContextInfo.getContextPath() + (String)properties.get("variable.index.dir");
		String stopWords = ServletContextInfo.getContextPath() + (String)properties.get("stopwords.file.path");

		Map<String, String> domainMap = DomainKnowledge.loadDomainKnowledge(properties);

		Connection conn = null;
		try {
			CleanDataUtil.loadStopwordFile(stopWords);
			Model model =  ModelFactory.createModel();
			//Set database connection, if model is defined to be a database model
			conn = DatabaseQueryInterface.getDatabaseConnection(null);
			if(model.getModelName().equals("shingle-db")){
				((ShinglesDatabaseDictionaryModel)model).setDataBaseConnection(conn);
			}

			StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);

			Indexer di = new Indexer();
			BufferedReader br;
			String thisLine;
			String definitionEscapeSpecialCharacters;
			String definitionEscapeSpecialCharactersStemmed;
			String definitionEscapeSpecialCharactersExpanded;
			String definitionEscapeSpecialCharactersExpandedStemmed;
			String description;

			//Parse through the input file and add the terms (id, data to search) to the index
			//IndexWriter index = di.getIndexWriter(false, indexPath);
			br = new BufferedReader(new FileReader(variableFilename));
			int i = 0;
			while ((thisLine = br.readLine()) != null) {
				System.out.println(i++);
				thisLine = thisLine.trim();
				if (thisLine.equals(""))
					continue;
				String[] fields = thisLine.split("\\t");
				//System.out.println("DomainMap size:"+ domainMap.size());
				description = fields[2];
				//description = fields[4];

				definitionEscapeSpecialCharacters = LUCENE_PATTERN.matcher(
						CleanDataUtil.preProcessWordsSpecialCharacters(
								DomainKnowledge.expandVariableWithDomainKnowledge(
										domainMap,CleanDataUtil.preProcessWordsSpecialCharacters(description.trim().toLowerCase()))))
										.replaceAll(REPLACEMENT_STRING_ESCAPE).toLowerCase();

				definitionEscapeSpecialCharactersStemmed = LUCENE_PATTERN.matcher(
						CleanDataUtil.preProcessWordsSpecialCharacters(
								CleanDataUtil.preprocessStemAndTokenize(description)))
								.replaceAll(REPLACEMENT_STRING_ESCAPE).toLowerCase();

				definitionEscapeSpecialCharactersExpanded = LUCENE_PATTERN.matcher(
						CleanDataUtil.preprocessRemoveStopWords(
								UMLSDictionary.preprocessDictionaryFrequentWords(
										CleanDataUtil.preProcessWordsSpecialCharacters(
												model.expandWithDictionaryForString(
														CleanDataUtil.preProcessWordsSpecialCharacters(
																CleanDataUtil.preprocessRemoveStopWords(description)))))))
																.replaceAll(REPLACEMENT_STRING_ESCAPE).toLowerCase();

				definitionEscapeSpecialCharactersExpandedStemmed = LUCENE_PATTERN.matcher(
						CleanDataUtil.preprocessStemAndTokenize(definitionEscapeSpecialCharactersExpanded))
						.replaceAll(REPLACEMENT_STRING_ESCAPE).toLowerCase();

				di.indexDict(
						fields[0] + "\t"+ fields[1] +"\t"+ fields[2] + "\t"+ fields[3], //study, class, dataset, variable as the id
						description,
						definitionEscapeSpecialCharacters.trim(),
						definitionEscapeSpecialCharactersStemmed.trim(),
						definitionEscapeSpecialCharactersExpanded.trim(),
						definitionEscapeSpecialCharactersExpandedStemmed.trim(),
						pathToIndexDir,analyzer );
			}
			di.closeIndexWriter();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
