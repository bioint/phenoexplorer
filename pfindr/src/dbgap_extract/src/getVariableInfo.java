import java.util.List;


public class getVariableInfo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		dbConnect dbConn = new dbConnect();
		dbConn.connectDB();
		
		//get all variable Id & url, who has same variable name as other's
		String sql = " SELECT v.variable_id, v.variable_url "
				+ "FROM dbgap_variables_2014_08_05 AS v, var_name AS vn, addStudy AS ad "
				+ "where v.variable_name = vn.variable_name AND v.variable_id != ad.variable_id "
				+ "order by v.variable_name";		
		/*String sql = "select variable_id, variable_name, variable_url "
				+ "from dbgap_variables_2014_08_05 "
				+ "where study_id not in("
				+ "select dbgap_study_id "
				+ "from dbgap_studies_2014_08_05"
				+ ") "
				+ "order by variable_name";*/
		/*String sql = "select variable_id, variable_url "
				+ "from dbgap_variables_2014_08_05 as v, "
				+ "("
				+ "SELECT url_suffix "
				+ "FROM dbgap_variables_2014_08_05 "
				+ "GROUP BY url_suffix "
				+ "HAVING count(*) > 1"
				+ ") as vn "
				+ "WHERE v.url_suffix = vn.url_suffix "
				+ "order by variable_name";
		*/
		List<List<String>> rstList = dbConn.getSelectedVarUrl(sql);
		
		//insert value
		String url = "";
		HttpGetMethod httpGet = new HttpGetMethod(url);
		//htmlParse html = new htmlParse(null,"http://www.ncbi.nlm.nih.gov",dbConn);

		int nLen = rstList.size();
		System.out.println(nLen);
		for(int nIndex = 0;  nIndex < nLen; nIndex ++){
			String varId = rstList.get(nIndex).get(0);
			String nInclud = "0";//0 include, 1 not inlucde, 2 out of date
			url = rstList.get(nIndex).get(1);
			//System.out.println(url);
			httpGet.setURL(url);
			httpGet.executeGetMethod(1);			
			String getRespond = httpGet.getResponseBodyString();
			//System.out.println(" \n-----------\n" + getRespond + "\n--------\n");
			if(getRespond.contains("cannot be found in this study") == true){
				nInclud = "1";
			}
			else if(getRespond.contains("This version of the study has been superseded") == true){
				nInclud = "2";
			}
			sql = "INSERT INTO detectInclud(varId, include) "
					+ "VALUES('" + varId + "' , " + nInclud + ")";
			dbConn.insertData(sql);
		}
		dbConn.disconnectDB();
	
		/*
		String url = "";
		HttpGetMethod httpGet = new HttpGetMethod(url);
		for(int i = 189908; i <= 190269; i ++){
			url = "http://www.ncbi.nlm.nih.gov/projects/gap/cgi-bin/variable.cgi?study_id=phs000090.v2.p1&phv=" + Integer.toString(i);
			httpGet.setURL(url);
			httpGet.executeGetMethod(1);			
			String getRespond = httpGet.getResponseBodyString();
			if(getRespond.contains("cannot be found in this study") == false){
				System.out.println(url);
			}
		}
		*/
		
		
	}

}
