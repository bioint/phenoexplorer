
public class getStudiesFromDbGaPWebsite {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		boolean bKeepOrgTable = false;
		int nArgsLen = args.length;
		for(int i = 0; i < nArgsLen; i ++){
			String arg = args[i];
			if(arg.equals("-k")){
				bKeepOrgTable = true;
			}
		}
		
		//connect to Database
		dbConnect dbConn = new dbConnect();
		dbConn.connectDB();
		
		//html 
		//first page, use get method, from the respond head, we can get cookie for rest pages 	
		String url = "http://www.ncbi.nlm.nih.gov/gap?db=gap&term=1[Is%20Top-Level%20Study]";
		HttpGetMethod httpGet = new HttpGetMethod(url);
		
		httpGet.executeGetMethod(1);
		String getRespond = httpGet.getResponseBodyString();
		
		if(getRespond != null){
			//parse html, and insert data into database
			htmlParse html = new htmlParse(getRespond, "http://www.ncbi.nlm.nih.gov", dbConn);
			html.parseStudyTable(bKeepOrgTable);
			//get page size and result count
			html.getPageInfo();
			int nPageSize = html.getPageSize();
			int nRstCount = html.getResultCount();
			
			//get cookie
			String ncbi_sid = httpGet.getNcbi_sid();
			String webEnv = httpGet.getWebEnv();
			HttpPostMethod httpPost = new HttpPostMethod("http://www.ncbi.nlm.nih.gov/gap", ncbi_sid, webEnv, 1, 2, nPageSize,nPageSize, nRstCount);
			int nTotalPage = 0;
			if(nRstCount%nPageSize == 0){
				nTotalPage = nRstCount/nPageSize;
			}
			else{
				nTotalPage = nRstCount/nPageSize + 1;
			}
			
			for(int nCurPage = 2; nCurPage <= nTotalPage; nCurPage ++){
				httpPost.executePostMethod(1);
				//parse html, and insert data into database
				String postRespond = httpPost.getResponseBodyString();
				if(postRespond != null){
					html.setContent(postRespond);
					html.parseStudyTable(bKeepOrgTable);
				}
			}
		}
		
		dbConn.disconnectDB();
	}
}
