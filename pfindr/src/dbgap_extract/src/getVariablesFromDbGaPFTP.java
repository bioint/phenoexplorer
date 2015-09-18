import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.SAXParseException;

public class getVariablesFromDbGaPFTP {

	public static void main(String[] args){
		// TODO Auto-generated method stub
		//get parameter from args
	
		boolean bDownload = false;
		boolean bParse = false;
		boolean bLoadDB = false;
		
		boolean bKeepOrgTable = false;
		boolean bDiseases = false;
		boolean bVarValue = false;
	
		/*test 0805*/
		/*boolean bDownload = true;
		boolean bParse = true;
		boolean bLoadDB = true;
		boolean bDiseases = true;
		boolean bVarValue = true;
		*/
		
		int nArgsLen = args.length;
		for(int i = 0; i < nArgsLen; i ++){
			String arg = args[i];
			if(arg.equals("-d")){
				bDownload = true;
			}
			else if(arg.equals("-p")){
				bParse = true;
			}
			else if(arg.equals("-l")){
				bLoadDB = true;
			}
			else if(arg.equals("-k")){
				bKeepOrgTable = true;
			}
			else if(arg.equals("-s")){
				bDiseases = true;
			}
			else if(arg.equals("-v")){
				bVarValue = true;
			}
		}
		//connect to database
		dbConnect dbConn = new dbConnect();
		dbConn.connectDB();
		
		//global hash Map for variable 
		HashMap<String, String> gVarMap = new HashMap<String,String>();
		
		//ftp
		ftpClientMethod ftpStp = new ftpClientMethod("ftp.ncbi.nlm.nih.gov", dbConn);
		ftpStp.openFTPClient();
		//3 steps
		//step 1: get the file list of all studies
		ftpStp.setRelativePath("dbgap/studies");
		
		List<ftpFileList> studyList = ftpStp.retrieveFileList(); //get all study's name
		int nLen = studyList.size();
		//for each study
		for(int nIndex = 0; nIndex < nLen; nIndex ++){
			//step 2: get each study's latest version
			String sdyName = studyList.get(nIndex).getName();

			ftpStp.setRelativePath(sdyName);
			List<ftpFileList> sdyVersion = ftpStp.retrieveFileList();
			
			//the sdyVersion may include one analysis file or version files, or both
			//remove the file named "analyses"		
			//get latest version name
			String sdyLastVerName = ftpStp.getLatestFileName(sdyVersion);
			//System.out.println("the " + nIndex + "th study: " + sdyName + ", Version: " + sdyLastVerName);
						
			//step 3: get and download .xml from lastest update file
			if(sdyLastVerName != null){
				//if this study has study version file
				//each study has its own directory
				String mainDownloadPath = "Study/" + sdyName + "_" + sdyLastVerName;
				//download GapExchange here, and parse it
				String exchangePath = mainDownloadPath + "/GapExchange";
				//create empty directory
				File exchangeFile = new File(exchangePath);
				exchangeFile.mkdirs();
				
				//download and parse GapExchange files
				String gapExchangeFileName = "GapExchange_"+sdyLastVerName+".xml";
				ftpStp.setRelativePath(sdyLastVerName);
				if(bDownload){
					ftpStp.downloadFile(exchangePath, gapExchangeFileName);
					if(bParse){
						try {
							ftpStp.parseGapExchange(exchangePath, gapExchangeFileName, bLoadDB, bDiseases, bKeepOrgTable);
						} catch (SAXParseException e) {
							// TODO Auto-generated catch block
						}
					}
				}
				
			
				//get data_dict files for each study
				//first, is there any pheno_variable_summaries directory
				boolean hasSum = ftpStp.setRelativePath("pheno_variable_summaries");
				if(hasSum){
					//if has pheno_variable_summaries, then get file list from this directory
					List<ftpFileList> varFile = ftpStp.retrieveFileList();
					int nVarFileNum = varFile.size();
					//System.out.println("File Num: " + nVarFileNum);
	
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
								if(bDownload){
									//download
									ftpStp.downloadFile(datadictPath, dataDictFileName);
									if(bParse){
										//parse
										ftpStp.parseDataDict(datadictPath, dataDictFileName,bLoadDB,bVarValue,bKeepOrgTable,gVarMap);
									}
								}
							}
						}	
					}
					//if this version doesn't have file - "pheno_variable_summaries", that is, change not success
					//the program won't enter this file, in that situation, need not back to version file
					ftpStp.backToParentDirct(); //back to version file
				}
				//if has latest version, then enter success, so  now should back to version list
				ftpStp.backToParentDirct();//back to version List
			}
			ftpStp.backToParentDirct();//back to study List
		}
		ftpStp.releaseFTPClient();
		
		if (gVarMap.isEmpty() == false) {
			//clear hash Map
			gVarMap.clear();
		}
		dbConn.disconnectDB();
	}

}
