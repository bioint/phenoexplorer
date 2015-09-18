#!/bin/bash

(
flock 200

WEEK="week_$(expr $(date +%W) % 4)"
SUBJECT="PhenoExplorer Update (${WEEK})"
EMAIL="ambite@isi.edu serban@isi.edu tallis@isi.edu"
today=`date +%Y_%m_%d`
dbGaP_date=`date +%x`
iso_dbGaP_date=`date +%F`
pfindr_home=/pfindr/users/pfindr
dest_data_dir=/usr/etc/pfindr
LOGFILE=${pfindr_home}/update_dbgap/pfindr.log
MESSAGES_FILE=${pfindr_home}/update_dbgap/messages.log

MAILMESSAGE="${pfindr_home}/update_dbgap/mailMessage.txt"
echo -n "" > ${MAILMESSAGE}

echo "Start dbGaP update: $(date)"
echo "Start dbGaP update: $(date)" >> $LOGFILE

sendMail() {
	echo "Finished dbGaP update: $(date)"
	echo $1 >> ${MAILMESSAGE}
	echo "" >> ${MAILMESSAGE}
	cat ${MESSAGES_FILE} >> ${MAILMESSAGE}
	/bin/mail -s "${SUBJECT}" "${EMAIL}" < ${MAILMESSAGE}
	echo "Finished dbGaP update: $(date)" >> $LOGFILE
}

# Step 1

cd ${pfindr_home}/DataRetrieved
if [ $? != 0 ]
  then
    sendMail "Can not change directory to DataRetrieved."
    exit 1
  fi

java -jar writeConfig.jar
if [ $? != 0 ]
  then
    sendMail "Can not generate the configuration file."
    exit 1
  fi

java -jar createTables.jar
if [ $? != 0 ]
  then
    sendMail "Can not create the database tables."
    exit 1
  fi

java -jar getStudiesFromDbGaPWebsite.jar -k
if [ $? != 0 ]
  then
    sendMail "Can not get the studies from the dbGaP."
    exit 1
  fi

java -jar getVariablesFromDbGaPFTP.jar -d -p -l -k -s -v
if [ $? != 0 ]
  then
    sendMail "Can not get the variables from the dbGaP."
    exit 1
  fi

rm -rf Study
if [ $? != 0 ]
  then
    sendMail "Can not remove the Study directory."
    exit 1
  fi

# Step 2

data_dir=${pfindr_home}/data

rm -fr ${data_dir}/shingle/umls/*
if [ $? != 0 ]
  then
    sendMail "Can not cleanup the umls directory."
    exit 1
  fi

java -jar storeDataIntoFile.jar ${data_dir}/shingle/umls varDescs.txt ${data_dir}/shingle/umls dbgap_indexer.txt
if [ $? != 0 ]
  then
    sendMail "Can not store data into file."
    exit 1
  fi

cmd_dir=${pfindr_home}/pfindrcmds

${cmd_dir}/bin/expandvars.sh
if [ $? != 0 ]
  then
    sendMail "Can not expand variables."
    exit 1
  fi

${cmd_dir}/bin/indexer.sh
if [ $? != 0 ]
  then
    sendMail "Can not index variables."
    exit 1
  fi

java -jar storeFileIntoDB.jar dbgap_all_variable_expanded_${today} ${data_dir}/shingle/umls varDescs_expanded.txt
if [ $? != 0 ]
  then
    sendMail "Can not store file into table."
    exit 1
  fi

cp -r ${data_dir}/shingle/umls/dbgap_all_variables_index ${dest_data_dir}/data/shingle/umls/
if [ $? != 0 ]
  then
    sendMail "Can not copy the index directory."
    exit 1
  fi

SQL_FILE=${pfindr_home}/update_dbgap/pfindr_${WEEK}.sql
echo "BEGIN;" > ${SQL_FILE}

echo "\\set ON_ERROR_STOP on" >> ${SQL_FILE}

echo "CREATE TABLE dbgap_page_studies_${today} (" >> ${SQL_FILE}
echo "dbgap_study_id text NOT NULL," >> ${SQL_FILE}
echo "dbgap_study_name text," >> ${SQL_FILE}
echo "participants bigint," >> ${SQL_FILE}
echo "study_type text," >> ${SQL_FILE}
echo "platform text," >> ${SQL_FILE}
echo "genetic_type text," >> ${SQL_FILE}
echo "race text," >> ${SQL_FILE}
echo "sex text," >> ${SQL_FILE}
echo "min_age bigint," >> ${SQL_FILE}
echo "max_age bigint," >> ${SQL_FILE}
echo "description text," >> ${SQL_FILE}
echo "diseases text," >> ${SQL_FILE}
echo "study_url text" >> ${SQL_FILE}
echo ");" >> ${SQL_FILE}

echo "ALTER TABLE ONLY dbgap_page_studies_${today}" >> ${SQL_FILE}
echo "ADD CONSTRAINT dbgap_page_studies_${today}_pkey PRIMARY KEY (dbgap_study_id);" >> ${SQL_FILE}
echo "CREATE INDEX dbgap_page_studies_${today}_dbgap_study_name_idx ON dbgap_page_studies_${today} USING btree (dbgap_study_name);" >> ${SQL_FILE}

echo "insert into dbgap_page_studies_${today} select * from dbgap_studies_${today} where split_part(dbgap_study_id,'.',1) in (" >> ${SQL_FILE}
echo "'phs000200'," >> ${SQL_FILE}
echo "'phs000220'," >> ${SQL_FILE}
echo "'phs000280'," >> ${SQL_FILE}
echo "'phs000285'," >> ${SQL_FILE}
echo "'phs000388'," >> ${SQL_FILE}
echo "'phs000555'," >> ${SQL_FILE}
echo "'phs000559'," >> ${SQL_FILE}
echo "'phs000580'" >> ${SQL_FILE}
echo ");" >> ${SQL_FILE}

echo "CREATE TABLE dbgap_page_variables_${today} (" >> ${SQL_FILE}
echo "variable_id text NOT NULL," >> ${SQL_FILE}
echo "variable_name text," >> ${SQL_FILE}
echo "study_id text," >> ${SQL_FILE}
echo "study_name text," >> ${SQL_FILE}
echo "url_suffix text," >> ${SQL_FILE}
echo "description text," >> ${SQL_FILE}
echo "study_url text," >> ${SQL_FILE}
echo "variable_url text," >> ${SQL_FILE}
echo "study_href text," >> ${SQL_FILE}
echo "variable_href text" >> ${SQL_FILE}
echo ");" >> ${SQL_FILE}

echo "ALTER TABLE ONLY dbgap_page_variables_${today}" >> ${SQL_FILE}
echo "ADD CONSTRAINT dbgap_page_variables_${today}_pkey PRIMARY KEY (variable_id);" >> ${SQL_FILE}
echo "CREATE INDEX dbgap_page_variables_${today}_description_idx ON dbgap_page_variables_${today} USING btree (description);" >> ${SQL_FILE}
echo "CREATE INDEX dbgap_page_variables_${today}_study_id_idx ON dbgap_page_variables_${today} USING btree (study_id);" >> ${SQL_FILE}
echo "CREATE INDEX dbgap_page_variables_${today}_variable_name_idx ON dbgap_page_variables_${today} USING btree (variable_name);" >> ${SQL_FILE}

echo "insert into dbgap_page_variables_${today} select * from dbgap_variables_${today} where split_part(study_id,'.',1) in (" >> ${SQL_FILE}
echo "'phs000200'," >> ${SQL_FILE}
echo "'phs000220'," >> ${SQL_FILE}
echo "'phs000280'," >> ${SQL_FILE}
echo "'phs000285'," >> ${SQL_FILE}
echo "'phs000388'," >> ${SQL_FILE}
echo "'phs000555'," >> ${SQL_FILE}
echo "'phs000559'," >> ${SQL_FILE}
echo "'phs000580'" >> ${SQL_FILE}
echo ");" >> ${SQL_FILE}

echo "DROP TABLE IF EXISTS dbgap_studies_${WEEK};" >> ${SQL_FILE}
echo "DROP TABLE IF EXISTS dbgap_variables_${WEEK};" >> ${SQL_FILE}
echo "DROP TABLE IF EXISTS dbgap_all_variable_expanded_${WEEK};" >> ${SQL_FILE}
echo "DROP TABLE IF EXISTS Diseases_${WEEK};" >> ${SQL_FILE}
echo "DROP TABLE IF EXISTS Variable_Value_${WEEK};" >> ${SQL_FILE}
echo "DROP TABLE IF EXISTS dbgap_studies_org_${WEEK};" >> ${SQL_FILE}
echo "DROP TABLE IF EXISTS dbgap_variables_org_${WEEK};" >> ${SQL_FILE}
echo "DROP TABLE IF EXISTS dbgap_page_studies_${WEEK};" >> ${SQL_FILE}
echo "DROP TABLE IF EXISTS dbgap_page_variables_${WEEK};" >> ${SQL_FILE}

echo "ALTER TABLE dbgap_studies_previous RENAME TO dbgap_studies_${WEEK};" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_variables_previous RENAME TO dbgap_variables_${WEEK};" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_all_variable_expanded_previous RENAME TO dbgap_all_variable_expanded_${WEEK};" >> ${SQL_FILE}
echo "ALTER TABLE Diseases_previous RENAME TO Diseases_${WEEK};" >> ${SQL_FILE}
echo "ALTER TABLE Variable_Value_previous RENAME TO Variable_Value_${WEEK};" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_studies_org_previous RENAME TO dbgap_studies_org_${WEEK};" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_variables_org_previous RENAME TO dbgap_variables_org_${WEEK};" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_page_studies_previous RENAME TO dbgap_page_studies_${WEEK};" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_page_variables_previous RENAME TO dbgap_page_variables_${WEEK};" >> ${SQL_FILE}

echo "ALTER TABLE dbgap_studies RENAME TO dbgap_studies_previous;" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_variables RENAME TO dbgap_variables_previous;" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_all_variable_expanded RENAME TO dbgap_all_variable_expanded_previous;" >> ${SQL_FILE}
echo "ALTER TABLE Diseases RENAME TO Diseases_previous;" >> ${SQL_FILE}
echo "ALTER TABLE Variable_Value RENAME TO Variable_Value_previous;" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_studies_org RENAME TO dbgap_studies_org_previous;" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_variables_org RENAME TO dbgap_variables_org_previous;" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_page_studies RENAME TO dbgap_page_studies_previous;" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_page_variables RENAME TO dbgap_page_variables_previous;" >> ${SQL_FILE}

echo "ALTER TABLE dbgap_studies_${today} RENAME TO dbgap_studies;" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_variables_${today} RENAME TO dbgap_variables;" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_all_variable_expanded_${today} RENAME TO dbgap_all_variable_expanded;" >> ${SQL_FILE}
echo "ALTER TABLE Diseases_${today} RENAME TO Diseases;" >> ${SQL_FILE}
echo "ALTER TABLE Variable_Value_${today} RENAME TO Variable_Value;" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_studies_org_${today} RENAME TO dbgap_studies_org;" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_variables_org_${today} RENAME TO dbgap_variables_org;" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_page_studies_${today} RENAME TO dbgap_page_studies;" >> ${SQL_FILE}
echo "ALTER TABLE dbgap_page_variables_${today} RENAME TO dbgap_page_variables;" >> ${SQL_FILE}

echo "COMMIT;" >> ${SQL_FILE}

psql < ${SQL_FILE}
if [ $? != 0 ]
  then
    sendMail "Can not rename the tables."
    exit 1
  fi

TEMP_SQL_FILE=${pfindr_home}/update_dbgap/temp.sql

echo "date=${dbGaP_date}" > ${dest_data_dir}/dbGaP.properties
echo "iso_date=${iso_dbGaP_date}" >> ${dest_data_dir}/dbGaP.properties

echo "select count(*) from dbgap_studies;" > ${TEMP_SQL_FILE}
STUDIES_COUNT=$(psql -t < ${TEMP_SQL_FILE} | tr -d ' \n')
echo "studies=${STUDIES_COUNT}" >> ${dest_data_dir}/dbGaP.properties

echo "select count(*) from dbgap_variables;" > ${TEMP_SQL_FILE}
VARIABLES_COUNT=$(psql -t < ${TEMP_SQL_FILE} | tr -d ' \n')
echo "variables=${VARIABLES_COUNT}" >> ${dest_data_dir}/dbGaP.properties

echo "select count(*) from dbgap_page_studies;" > ${TEMP_SQL_FILE}
PAGE_STUDIES_COUNT=$(psql -t < ${TEMP_SQL_FILE} | tr -d ' \n')
echo "page_studies=${PAGE_STUDIES_COUNT}" >> ${dest_data_dir}/dbGaP.properties

echo "select count(*) from dbgap_page_variables;" > ${TEMP_SQL_FILE}
PAGE_VARIABLES_COUNT=$(psql -t < ${TEMP_SQL_FILE} | tr -d ' \n')
echo "page_variables=${PAGE_VARIABLES_COUNT}" >> ${dest_data_dir}/dbGaP.properties

echo "select count(distinct study_id) from dbgap_variables;" > ${TEMP_SQL_FILE}
COUNT=$(psql -t < ${TEMP_SQL_FILE} | tr -d ' \n')
echo "studies_with_variables=${COUNT}" >> ${dest_data_dir}/dbGaP.properties

echo "select count(distinct study_id) from dbgap_page_variables;" > ${TEMP_SQL_FILE}
COUNT=$(psql -t < ${TEMP_SQL_FILE} | tr -d ' \n')
echo "page_studies_with_variables=${COUNT}" >> ${dest_data_dir}/dbGaP.properties

echo "delete from dbgap_studies where dbgap_study_id not in (select study_id from dbgap_variables);" > ${TEMP_SQL_FILE}
psql < ${TEMP_SQL_FILE}
if [ $? != 0 ]
  then
    sendMail "Can not delete studies w/o variables."
    exit 1
  fi

echo "delete from dbgap_page_studies where dbgap_study_id not in (select study_id from dbgap_page_variables);" > ${TEMP_SQL_FILE}
psql < ${TEMP_SQL_FILE}
if [ $? != 0 ]
  then
    sendMail "Can not delete studies w/o variables."
    exit 1
  fi

rm -f ${pfindr_home}/update_dbgap/output/*

DBGAP_STUDIES=${pfindr_home}/update_dbgap/output/dbGaP_studies.txt
DBGAP_STUDIES_DIFF=${pfindr_home}/update_dbgap/output/dbGaP_studies_diff.txt
DBGAP_VARIABLES_DIFF=${pfindr_home}/update_dbgap/output/dbGaP_variables_diff.txt

PAGE_STUDIES=${pfindr_home}/update_dbgap/output/page_studies.txt
PAGE_STUDIES_DIFF=${pfindr_home}/update_dbgap/output/page_studies_diff.txt
PAGE_VARIABLES_DIFF=${pfindr_home}/update_dbgap/output/page_variables_diff.txt

rm -f ${pfindr_home}/update_dbgap/output/*

echo "copy (select A.dbgap_study_id Study_ID, A.dbgap_study_name Study_Name, CASE WHEN B.variables is NULL THEN 0 ELSE B.variables END  NumVariables from dbgap_studies A full join (select study_id, count(variable_id) variables from dbgap_variables group by study_id) B on A.dbgap_study_id = B.study_id order by B.study_id, A.dbgap_study_id) to stdout with csv delimiter E'\t' header;" > ${TEMP_SQL_FILE}
psql < ${TEMP_SQL_FILE} > ${DBGAP_STUDIES}
 
echo "copy (SELECT A.dbgap_study_id previous_study, A.dbgap_study_name previous_name, B.dbgap_study_id current_study, B.dbgap_study_name current_name FROM dbgap_studies_previous A FULL JOIN dbgap_studies B ON (split_part(A.dbgap_study_id, '.', 1) = split_part(B.dbgap_study_id, '.', 1)) WHERE A.dbgap_study_id != B.dbgap_study_id OR A.dbgap_study_id IS NULL OR B.dbgap_study_id IS NULL ORDER BY A.dbgap_study_id, B.dbgap_study_id) to stdout with csv delimiter E'\t' header;" > ${TEMP_SQL_FILE}
psql < ${TEMP_SQL_FILE} > ${DBGAP_STUDIES_DIFF}

echo "copy (SELECT A.study_id previous_study, A.variable_id previous_variable, B.study_id current_study, B.variable_id current_variable FROM dbgap_variables_previous A FULL JOIN dbgap_variables B ON (split_part(A.study_id, '.', 1) = split_part(B.study_id, '.', 1) AND split_part(A.variable_id, '.', 1) = split_part(B.variable_id, '.', 1)) WHERE A.study_id != B.study_id OR A.variable_id != B.variable_id OR A.study_id IS NULL OR A.variable_id IS NULL OR B.study_id IS NULL OR B.variable_id IS NULL ORDER BY A.study_id, A.variable_id, B.study_id, B.variable_id) to stdout with csv delimiter E'\t' header;" > ${TEMP_SQL_FILE}
psql < ${TEMP_SQL_FILE} > ${DBGAP_VARIABLES_DIFF}

echo "copy (select A.dbgap_study_id Study_ID, A.dbgap_study_name Study_Name, CASE WHEN B.variables is NULL THEN 0 ELSE B.variables END  NumVariables from dbgap_page_studies A full join (select study_id, count(variable_id) variables from dbgap_page_variables group by study_id) B on A.dbgap_study_id = B.study_id order by B.study_id, A.dbgap_study_id) to stdout with csv delimiter E'\t' header;" > ${TEMP_SQL_FILE}
psql < ${TEMP_SQL_FILE} > ${PAGE_STUDIES}
 
echo "copy (SELECT A.dbgap_study_id previous_study, A.dbgap_study_name previous_name, B.dbgap_study_id current_study, B.dbgap_study_name current_name FROM dbgap_page_studies_previous A FULL JOIN dbgap_page_studies B ON (split_part(A.dbgap_study_id, '.', 1) = split_part(B.dbgap_study_id, '.', 1)) WHERE A.dbgap_study_id != B.dbgap_study_id OR A.dbgap_study_id IS NULL OR B.dbgap_study_id IS NULL ORDER BY A.dbgap_study_id, B.dbgap_study_id) to stdout with csv delimiter E'\t' header;" > ${TEMP_SQL_FILE}
psql < ${TEMP_SQL_FILE} > ${PAGE_STUDIES_DIFF}

echo "copy (SELECT A.study_id previous_study, A.variable_id previous_variable, B.study_id current_study, B.variable_id current_variable FROM dbgap_page_variables_previous A FULL JOIN dbgap_page_variables B ON (split_part(A.study_id, '.', 1) = split_part(B.study_id, '.', 1) AND split_part(A.variable_id, '.', 1) = split_part(B.variable_id, '.', 1)) WHERE A.study_id != B.study_id OR A.variable_id != B.variable_id OR A.study_id IS NULL OR A.variable_id IS NULL OR B.study_id IS NULL OR B.variable_id IS NULL ORDER BY A.study_id, A.variable_id, B.study_id, B.variable_id) to stdout with csv delimiter E'\t' header;" > ${TEMP_SQL_FILE}
psql < ${TEMP_SQL_FILE} > ${PAGE_VARIABLES_DIFF}

rm -f ${pfindr_home}/update_dbgap.zip
cd ${pfindr_home}/update_dbgap/output
zip ${pfindr_home}/update_dbgap.zip * >/dev/null

${pfindr_home}/update_dbgap/pfindrMail.py --pfindr_home ${pfindr_home} --dbgap_studies ${STUDIES_COUNT} --dbgap_variables ${VARIABLES_COUNT} --dbgap_page_studies ${PAGE_STUDIES_COUNT} --dbgap_page_variables ${PAGE_VARIABLES_COUNT}

)  200</var/lock/pfindr.lock

