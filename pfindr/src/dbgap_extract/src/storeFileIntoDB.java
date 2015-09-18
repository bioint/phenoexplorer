import java.util.List;


public class storeFileIntoDB {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//Date date = new Date();
		//DateFormat datefmt = new SimpleDateFormat("_yyyy_MM_dd");
		//String suffix = datefmt.format(date);
		
		//String varExpTableName = args[0] + suffix;
		String varExpTableName = args[0];
		String filePath = args[1];
		String fileName = args[2];
		
		dbConnect dbConn = new dbConnect();
		dbConn.connectDB();
		//create table
		String sql = "CREATE TABLE " + varExpTableName + "( "
				+ "variable_desc_org text, "
				+ "variable_desc_expanded text"
				+ ")";
		dbConn.createTable(sql);
		sql = "CREATE INDEX " + varExpTableName + "_variable_desc_org_idx ON " + varExpTableName + " USING btree (variable_desc_org)";
		dbConn.createTable(sql);
		
		//Insert Value
		fileFunc ff = new fileFunc();
		List<List<String>> lVarDescExpansion = ff.readVarDescFile(filePath,fileName);
		int nLen = lVarDescExpansion.size();
		for(int i = 0; i < nLen; i ++){
			List<String> l1Var = lVarDescExpansion.get(i);
			String varDescription = l1Var.get(0);
			String varExpansion = "";
			if(l1Var.size() > 1){
				varExpansion = l1Var.get(1);
			} else {
				continue;
			}
			varDescription = varDescription.replaceAll("'", "''");
			varExpansion = varExpansion.replaceAll("'", "''");
			sql = "INSERT INTO " + varExpTableName + "(variable_desc_org, variable_desc_expanded) "
					+ "VALUES('" + varDescription + "','" + varExpansion + "')";
			dbConn.insertData(sql);
		}
		
		dbConn.disconnectDB();
		
	}

}
