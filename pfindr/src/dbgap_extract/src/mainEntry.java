import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.SAXParseException;



//main entry for the whole project
public class mainEntry {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
/*
		//html 
		//first page, use get method, from the respond head, we can get cookie for next 24 pages 
		String url = "http://www.ncbi.nlm.nih.gov/gap?db=gap&term=1[Is%20Top-Level%20Study]";
		HttpGetMethod httpGet = new HttpGetMethod(url);
		
		httpGet.executeGetMethod();
		String getRespond = httpGet.getResponseBodyString();
		
		//parse html, and insert data into database
		htmlParse html = new htmlParse(getRespond, "http://www.ncbi.nlm.nih.gov");
		html.parseTable();
		//get page size and result count
		html.getPageInfo();
		int nPageSize = html.getPageSize();
		int nRstCount = html.getResultCount();
		
		//next 24 pages
		//get cookie
		String ncbi_sid = httpGet.getNcbi_sid();
		String webEnv = httpGet.getWebEnv();
		HttpPostMethod httpPost = new HttpPostMethod("http://www.ncbi.nlm.nih.gov/gap", ncbi_sid, webEnv, 1, 2, nPageSize, nRstCount);
		int nTotalPage = 0;
		if(nRstCount%nPageSize == 0){
			nTotalPage = nRstCount/nPageSize;
		}
		else{
			nTotalPage = nRstCount/nPageSize + 1;
		}
		
		for(int nCurPage = 2; nCurPage <= nTotalPage; nCurPage ++){
			httpPost.executePostMethod();
			//parse html, and insert data into database
			String postRespond = httpPost.getResponseBodyString();
			html.setContent(postRespond);
			html.parseTable();
		}*/


		//ftp
		ftpClientMethod ftpStp = new ftpClientMethod("ftp.ncbi.nlm.nih.gov", null);
		ftpStp.openFTPClient();
		//3 steps
		//step 1: get the file list of all studies
		ftpStp.setRelativePath("dbgap/studies");
		
		List<ftpFileList> studyList = ftpStp.retrieveFileList(); //get all study's name
		int nLen = studyList.size();
		//for each study
		for(int nIndex = 50; nIndex < nLen; nIndex ++){
			//step 2: get each study's latest version
			String sdyName = studyList.get(nIndex).getName();

			ftpStp.setRelativePath(sdyName);
			List<ftpFileList> sdyVersion = ftpStp.retrieveFileList();
			
			//the sdyVersion may include one analysis file or version files, or both
			//remove the file named "analyses"		
			//get latest version name
			String sdyLastVerName = ftpStp.getLatestFileName(sdyVersion);
			System.out.println("the " + nIndex + "th study: " + sdyName + ", Version: " + sdyLastVerName);
						
			//step 3: get and download .xml from lastest update file
			if(sdyLastVerName != null){
				//if this study has study version file
				//each study has its own directory
				String mainDownloadPath = "/Users/Cici/Desktop/Study/" + sdyName + "_" + sdyLastVerName;
				//download GapExchange here, and parse it
				String exchangePath = mainDownloadPath + "/GapExchange";
				//create empty directory
				File exchangeFile = new File(exchangePath);
				exchangeFile.mkdirs();
				
				//download and parse GapExchange files
				String gapExchangeFileName = "GapExchange_"+sdyLastVerName+".xml";
				ftpStp.setRelativePath(sdyLastVerName);
				ftpStp.downloadFile(exchangePath, gapExchangeFileName);
				try {
					ftpStp.parseGapExchange(exchangePath, gapExchangeFileName,true,true, true);
				} catch (SAXParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			
				//get data_dict files for each study
				//first, is there any pheno_variable_summaries directory
				boolean hasSum = ftpStp.setRelativePath("pheno_variable_summaries");
				if(hasSum){
					//if has pheno_variable_summaries, then get file list from this directory
					List<ftpFileList> varFile = ftpStp.retrieveFileList();
					int nVarFileNum = varFile.size();
					System.out.println("File Num: " + nVarFileNum);
	
					//downlad data_dict here	
					if(nVarFileNum != 0){
						//has pheno_variable_summaries directory, but inside has no file
						String datadictPath = mainDownloadPath + "/Data_Dict";
						//create directory
						File datadictFile = new File(datadictPath);
						datadictFile.mkdir();
						
						//parse each data_dict 
						for(int i = 0; i < nVarFileNum; i ++){
							String dataDictFileName = varFile.get(i).getName();						
							if(dataDictFileName.contains("data_dict")){							
								//download
								ftpStp.downloadFile(datadictPath, dataDictFileName);
								//parse
								ftpStp.parseDataDict(datadictPath, dataDictFileName,true,true, true, new HashMap<String, String>());
							}
						}	
					}
					//if this version doesn't have file - "pheno_variable_summaries", that is, change not success
					//the program won't enter this file, in that situation, need not back to version file
					ftpStp.backToParentDirct(); //back to version file
				}
				//if has latest version, then enter sucess, so  now should back to version list
				ftpStp.backToParentDirct();//back to version List
			}
			ftpStp.backToParentDirct();//back to study List
		}
		ftpStp.releaseFTPClient();
		/**/
	}

}
