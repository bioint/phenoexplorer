public class getVariableFromDbGaPWebsite {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
/**/
		//url for variables
		dbConnect dbConn = new dbConnect();
		dbConn.connectDB();
		
		String  url = "http://www.ncbi.nlm.nih.gov/gap/?term=2[s_discriminator]%20AND%201[Is%20Top-Level%20Study]&report=SVariables";		
		HttpGetMethod httpGet = new HttpGetMethod(url);
		
		httpGet.executeGetMethod(2);
		String getRespond = httpGet.getResponseBodyString();
		//System.out.print("get:\n" + getRespond + "\n ---- \n");
		
		//parse html, and update data for dbgap_variables(url_suffix, variable_url, variable_href)
		htmlParse html = new htmlParse(getRespond,"http://www.ncbi.nlm.nih.gov", dbConn);
		//html.parseVariableTable();
		//get page size and result count
		html.getPageInfo();
		int nPageSize = html.getPageSize();
		int nRstCount = html.getResultCount();
		int nMaxPageSize = html.getMaxPageSize();
		//String phid = html.getPhid();
		
		//get cookie
		String ncbi_sid = httpGet.getNcbi_sid();
		String webEnv = httpGet.getWebEnv();
		//change the pageSize to the maximum, then parse it
		HttpPostMethod httpPost = new HttpPostMethod("http://www.ncbi.nlm.nih.gov/gap", ncbi_sid, webEnv, 1, 1, nMaxPageSize, nPageSize, nRstCount);	
		//HttpPostMethod httpPost = new HttpPostMethod("http://www.ncbi.nlm.nih.gov/gap", postCookies, 1, 1, nMaxPageSize, nPageSize, nRstCount);			
		httpPost.executePostMethod(3);
		String postRespond = httpPost.getResponseBodyString();
		//System.out.println(postRespond);
		html.setContent(postRespond);
		html.parseVariableTable();
		
		//get the information about the page
		html.getPageInfo();
		nPageSize = html.getPageSize();
		nRstCount = html.getResultCount();
		httpPost.setPageInfo(1, 2, nPageSize, nPageSize, nRstCount);
			
		int nTotalPage = 0;
		if(nRstCount%nPageSize == 0){
			nTotalPage = nRstCount/nPageSize;
		}
		else{
			nTotalPage = nRstCount/nPageSize + 1;
		}
		
		for(int nCurPage = 2; nCurPage <= nTotalPage; nCurPage ++){
			httpPost.executePostMethod(2);
			
			//parse html, and update data for dbgap_variables
			postRespond = httpPost.getResponseBodyString();
			//System.out.println(postRespond);
			html.setContent(postRespond);
			html.parseVariableTable();
		}	
		
		dbConn.disconnectDB();
	/**/
	/*	
		//get all variables name list
		dbConnect dbConn = new dbConnect();
		dbConn.connectDB();
		
		String varTableName = dbConn.getVariableTableName();
		String sql = "select variable_id"
				+ " from " + varTableName;
				//+" order by variable_id";
		List<String> varNameList = dbConn.getVarName(sql);
		
		String url = "";
		HttpGetMethod httpGet = new HttpGetMethod(url);
		htmlParse html = new htmlParse(null,"http://www.ncbi.nlm.nih.gov",dbConn);
		int nLen = varNameList.size();
		for(int nIndex = 0; nIndex < nLen; nIndex ++){
			String varName = varNameList.get(nIndex);
			url = "http://www.ncbi.nlm.nih.gov/gap/?term=2[s_discriminator]%20AND%20"+varName+"&report=SVariables";
			httpGet.setURL(url);
			httpGet.executeGetMethod(2);
			
			String getRespond = httpGet.getResponseBodyString();
			if(getRespond != null){
				html.setContent(getRespond);
				html.parseVariableTable();
			}
		}
		
		dbConn.disconnectDB();
		*/
	}
}
