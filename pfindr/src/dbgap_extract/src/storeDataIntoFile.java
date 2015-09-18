import java.util.List;

import org.jsoup.helper.StringUtil;


public class storeDataIntoFile {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filePath1 = args[0];
		String fileName1 = args[1];
		String filePath2 = args[2];
		String fileName2 = args[3];
		
		dbConnect dbConn = new dbConnect();
		dbConn.connectDB();
		
		String varTableName = dbConn.getVariableTableName();
		
		//variable expansion
		String sql = "SELECT distinct description "
				+ "FROM " + varTableName;
		List<String> lVarDescList = dbConn.getVarDesc(sql);
		String fileContent = StringUtil.join(lVarDescList, "\n");
		
		fileFunc ff = new fileFunc();
		ff.setFileContent(fileContent);
		//ff.writeFile("dbData", "varDescs.txt");
		ff.writeFile(filePath1, fileName1);
		lVarDescList.clear();
		
		//variable index
		sql = "SELECT variable_id, variable_name, study_id, description, url_suffix, study_name "
				+ "FROM " + varTableName;
		List<String> lVarInfo = dbConn.getVarInfo(sql);
		fileContent = StringUtil.join(lVarInfo, "\n");
		//ff.setFileName();
		ff.setFileContent(fileContent);
		//ff.writeFile("dbData", "dbgap_indexer.txt");
		ff.writeFile(filePath2, fileName2);
		lVarInfo.clear();
		
		dbConn.disconnectDB();
	}

}
