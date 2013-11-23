PhenoExplorer 
=============

PhenoExplorer is a tool that supports cross-study data discovery among NHLBI genomic studies.

Despite the vast amounts of genomic data (e.g., GWAS, sequencing, etc.) available in repositories such as dbGaP, lack of standardization makes identification of relevant datasets challenging for biomedical researchers interested in specific phenotypic measures. PFINDR provides a web-based query interface that allows researchers to identify studies and phenotype variables of interest from dbGaP data repository. It uses an ensemble of semantic similarity metrics for matching phenotypic variables, using machine learning and information retrieval techniques.

This project is funded by NHLBI's pFINDR program: Phenotype Finder IN Data Resources under grant number 1UH2HL108780.

##Project setup

Phenoexplorer is a web based tool.To build phenoexplorer, you can clone the repository, and load it into a web project, like an eclipse dynamic web project. 

####Include third-party libraries

You need the following jars in the classpath:

- JSON.jar
- commons-codec-1.3.jar
- commons-collections-3.2.1.jar
- commons-dbcp-1.4.jar
- commons-fileupload-1.2.1.jar
- commons-httpclient-3.0.jar
- commons-io-2.1.jar
- commons-lang-2.4.jar
- commons-lang3-3.1.jar
- commons-logging-1.0.4.jar
- crowd-integration-client-2.1.0.jar
- javax.servlet.jar
- libraries.txt
- libstemmer.jar
- log4j-1.2.15.jar
- lucene-analyzers-2.9.3.jar
- lucene-core-3.0.1.jar
- mail.jar
- mallet-2.0.7.jar
- mallet-deps-2.0.5.jar
- postgresql-9.1-901.jdbc4.jar
- recaptcha4j-0.0.7.jar

These files should be included in the classpath. (You can place them in folder WebContent/WEB-INF/lib/)

####Data Files

The following files are being used by the program and should be present in the data directory:

Under data/, we have:
* domain_knowledge.txt		
* stopwords.txt
* model.properties
* db_connection.properties

And under the data/shingle/umls/ folder, we have:
* dbgap_all_variables_index	: The Information Retrieval Index (see the section below, on how to create the index)
* trained_MaxEnt_classifier.obj	: The trained MaxEntropy classifier being used by the system. The file is a binary object. It's built by applying MaxEntropy classification algorithm on training data (variables manually mapped to phenotypes)
* dbgap_all_variables_to_expand.txt : used as input to ExpandWithDictionaryUsingFile.java program. See section on "Precomputed dictionary expansion for the dbGaP variables"


####Set up back-end database


Phenoexplorer connects with the database and accesses the following 5 tables:

#####dbGaP studies and variables information

* Table: **dbgap_studies** : studies from dbGaP along with the meta-data information
* Table: **dbgap_variables** : variables from dbGaP
(MySQL dumps are provided for these tables)

#####UMLS (Unified Medical Language System) data for expanding the phenotype with definitions with related medical terms

umls_source_definition
umls_concept_name

We are using version 2012AB of the UMLS Dictionary. 
The tables we are using from the release are: MRCONSO, MRDEF 

Not all columns in the tables above are useful. So, we create our own tables with more user friendly names containing only the information used by our system:

* Table: **umls_source_definition**


```
CREATE TABLE umls_source_definition(
  concept_id character(12),
  source_abbr character(15),
  definition text,
  atom_id character(9)
);

CREATE INDEX def_source_idx
  ON umls_source_definition
  USING btree
  (source_abbr);

CREATE INDEX def_concept_idx
  ON umls_source_definition
  USING btree
  (concept_id );

CREATE INDEX def_atom_idx
  ON umls_source_definition
  USING btree
  (atom_id );
```

* Table: **umls_concept_name**

```
CREATE TABLE umls_concept_name(
concept_id char(12),
concept_name text,
atom_id char(9));

Index: concept_idx
CREATE INDEX concept_idx
  ON umls_concept_name
  USING btree
  (concept_id );
```

Extract information from the UMLS tables, and load them to your local database tables

```
INSERT INTO umls_source_definition AS
SELECT c.CUI, c.SAB, d.DEF, c.AUI
FROM 2012ab.MRCONSO c, 2012ab.MRDEF d
WHERE c.AUI= d.AUI AND c.LAT='ENG'
ORDER BY c.AUI;
```
OR 

```
SELECT c.CUI, c.SAB, d.DEF, c.AUI
FROM 2012ab.MRCONSO c, 2012ab.MRDEF d
WHERE c.AUI= d.AUI AND c.LAT='ENG'
ORDER BY c.AUI; -- save to definition.csv
```

```
INSERT INTO umls_concept_name AS
SELECT DISTINCT CUI, STR, AUI
FROM 2012ab.MRCONSO
WHERE LAT = 'ENG'
order by AUI;
```
OR

```
SELECT DISTINCT CUI, STR, AUI
FROM 2012ab.MRCONSO
WHERE LAT = 'ENG'
order by AUI; -- save to resultset.csv
```

If you choose to save them to files, you can load them using COPY

```
COPY umls_source_definition FROM '/../definition.csv' WITH csv;
COPY 185287
COPY umls_concept_name FROM '/../resultset.csv' WITH csv;
COPY 7637095
```

#####Precomputed UMLS (Unified Medical Language System) expansions for dbGaP variables 
(variable expanded with UMLS definition, with other processing applied : stop word removal, bi-gram construction, and stemming)

For faster matching of the variables, we precompute dictionary expansion for all the dbGaP variables, and store it in a database table.

* Table: **dbgap_all_variable_expanded_stem_token_bi**

```
CREATE TABLE dbgap_all_variable_expanded_stem_token_bi(
  variable_desc_org text,
  variable_desc_expanded text
);

CREATE INDEX dbgap_all_expan_stem_desc_idx
  ON dbgap_all_variable_expanded_stem_token_bi
  USING btree (variable_desc_org );
```

Due to the UMLS licensing agreement we cannot share this data, and you will need to build it yourself, using the program mentioned below:

Run the java program as a standalone : ExpandWithDictionaryUsingFile.java under edu.isi.pfindr.learn.util
Pass the  following command-line arguments to the program:
1. Absolute path to the folder where the files are located
2. Filename of the file with the dbGaP variables to be expanded
Note that the base/absolute path will also be used as a path, relative to which other files, like, the classification model, stop words, domain knowledge etc. will be searched for, so, organize the data keeping that in mind

The file mentioned as argument 2) is used as input to this program, place it in the folder specified in the argument 1)
The file is attached here: 

An output file with the same name as mentioned in 1) + _expanded.txt will be created. This file contains the expansions from the UMLS dictionary. Load this data into the table:
dbgap_all_variable_expanded_stem_token_bi


#####Database Connection Configuration

Before connecting to any of the tables above, you will need to specify the database connection details. When running Phenoexplorer as a web project, we use the database connection details from the web context. Modify the web.xml file inside WebContent/WEB-INF/, by adding the following context parameters, and entering the corresponding parameter details:
```
<context-param>
  <param-name>dbUrl</param-name>
  <param-value>put URL including db name</param-value>
</context-param>
<context-param>
  <param-name>dbUserName</param-name>
  <param-value></param-value>
</context-param>
<context-param>
  <param-name>dbPassword</param-name>
  <param-value></param-value>
</context-param>
<context-param>
  <param-name>driverclass</param-name>
  <param-value></param-value>
</context-param>
<context-param>
  <param-name>recaptchaPrivateKey</param-name>
  <param-value></param-value>
</context-param>
```
To prevent abuse from "bots" (automated programs usually written to generate spam), phenoexplorer.org uses google's reCAPTCHA service for registering new users. If you will be using the same, you will need to generate a key for the server where you will install the service.

For creating the UMLS expansions precomputed table (running ExpandWithDictionaryUsingFile.java), and the IR index (running Indexer.java), which are run as standalone programs, web context is not loaded, but instead, properties are read from db_connection.properties file.


####Create the Information Retrieval Index

The IR index is created with all the dbGaP variables, expanded with UMLS definitions, in a multi-field index. To create the IR index, run the program:
Indexer under edu.isi.pfindr.learn.search. Again, you need to pass the base path in the command line. The input file name and output index directory name are specified in the model.properties file as: dbgap.variable.index.file, variable.index.dir respectively. Their location is relative to the base path you pass, in the command line argument.

Both information retrieval and machine learning techniques are used to compute the final similarity between phenotypes. The classification model is precomputed and provided as a binary object as mentioned above, the IR index needs to be created using the program provided. Once you have set-up the database tables, modified the properties as per your settings and created the IR index, you are ready to go. You can create a WAR of the web project and drop it into a webapps directory of the server and restart the server.
