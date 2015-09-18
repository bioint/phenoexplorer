
public class createTables {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//connect to Database
		dbConnect dbConn = new dbConnect();
		dbConn.connectDB();
		
		//Get tables' Name
		String varTableName = dbConn.getVariableTableName();
		String orgVarTableName = dbConn.getOrgVarTableName();
		String StudyTableName = dbConn.getStudyTableName();
		String orgStudyTableName = dbConn.getOrgStudyTableName();
		String diseaseTableName = dbConn.getDiseaseTableName();
		String varValueTableName = dbConn.getVarValueTableName();
		
		//execute sql
		//create table dbgap_studies_org
		String sql = "CREATE TABLE " + orgStudyTableName
				+ "(Study_Accession_ID varchar(20) NOT NULL,"
				+ "Study_URL text,"
				+ "Study_Name text,"
				+ "Embargo_Release text,"
				+ "Participants integer,"
				+ "Type_Of_Study text,"
				+ "Platform text,"
				+ "Description text,"
				+ "Orig_Study_URL text,"
				+ "PRIMARY KEY(Study_Accession_ID)"
				+ ")";
		dbConn.createTable(sql);
		
		//create table dbgap_variables_org
		sql = "CREATE TABLE " + orgVarTableName
				+ "(Variable_ID varchar(100),"
				+ "Variable_Name text,"
				+ "Variable_Description text,"
				+ "Variable_Type text,"
				+ "Varibale_Units text,"
				+ "Logical_Minimum numeric,"
				+ "Logical_Maximum numeric,"
				+ "Variable_Comment text,"
				+ "PRIMARY KEY(Variable_ID)"
				+ ")";
		dbConn.createTable(sql);
		
		//create table Variable_Value
		sql = "CREATE TABLE " + varValueTableName
				+ "(Variable_ID varchar(100),"
				+ "Variable_Code varchar(200),"
				+ "Variable_Code_Description text,"
				+ "PRIMARY KEY(Variable_ID, Variable_Code)"
				+ ")";
		dbConn.createTable(sql);
		
		//create table Diseases
		sql = "CREATE TABLE " + diseaseTableName
				+ "(dbGaP_Study_ID varchar(20),"
				+ "Vocabulary varchar(200),"
				+ "Disease varchar(200),"
				+ "PRIMARY KEY(dbGaP_Study_ID, Vocabulary, Disease)"
				+ ")";
		dbConn.createTable(sql);
		
		//create table dbgap_studies
		sql = "CREATE TABLE " + StudyTableName
				+ "(dbgap_study_id TEXT NOT NULL,"
				+ "dbgap_study_name TEXT,"
				+ "participants BIGINT,"
				+ "study_type TEXT,"
				+ "platform TEXT,"
				+ "genetic_type TEXT,"
				+ "race TEXT,"
				+ "sex TEXT,"
				+ "min_age BIGINT,"
				+ "max_age BIGINT,"
				+ "description TEXT,"
				+ "diseases TEXT,"
				+ "study_url TEXT,"
				+ "PRIMARY KEY(dbgap_study_id)"
				+ ")";
		dbConn.createTable(sql);
		sql = "CREATE INDEX " + StudyTableName + "_dbgap_study_name_idx ON " + StudyTableName + " USING btree (dbgap_study_name)";
		dbConn.createTable(sql);
		
		//create tabel dbgap_variables
		sql = "CREATE TABLE " + varTableName
				+ "(variable_id TEXT NOT NULL,"
				+ "variable_name TEXT,"
				+ "study_id TEXT,"
				+ "study_name TEXT,"
				+ "url_suffix	TEXT,"
				+ "description TEXT,"
				+ "study_url TEXT,"
				+ "variable_url TEXT,"
				+ "study_href TEXT,"
				+ "variable_href TEXT,"
				+ "PRIMARY KEY(variable_id)"
				+ ")";
		dbConn.createTable(sql);
		sql = "CREATE INDEX " + varTableName + "_description_idx ON " + varTableName + " USING btree (description)";
		dbConn.createTable(sql);
		sql = "CREATE INDEX " + varTableName + "_variable_name_idx ON " + varTableName + " USING btree (variable_name)";
		dbConn.createTable(sql);
		sql = "CREATE INDEX " + varTableName + "_study_id_idx ON " + varTableName + " USING btree (study_id)";
		dbConn.createTable(sql);
		//disconnect database
		dbConn.disconnectDB();
	}

}
