import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Date;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.apache.commons.net.ftp.*;

public class ftpClientMethod {

	private String domain;
	private String userName;
	private String password;
	private String relativePath;// the path  relates to last directory
	private FTPClient ftpClient; 
	private dbConnect dbConn;
	
	
	public ftpClientMethod(String domain, dbConnect dbConn){
		this.domain = domain;
		this.userName = "anonymous";
		this.password = "";
		//create ftpClient
		ftpClient = new FTPClient();	
		
		this.dbConn = dbConn;
	}
	
	public ftpClientMethod(String domain, String userName, String password, dbConnect dbConn){
		this.domain = domain;
		this.userName = userName;
		this.password = password;
		
		ftpClient = new FTPClient();
		
		this.dbConn = dbConn;
	}
	
	//change working directory, if success, return true, and set the relativePath 
	public boolean setRelativePath(String relativePath){
		boolean changeSuc = false;
		try {
			changeSuc = ftpClient.changeWorkingDirectory(relativePath);
			if(changeSuc){
				this.relativePath = relativePath;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return changeSuc;
	}
	
	public String getRelaticePath(){
		return this.relativePath;
	}
	
	public void setDomain(String domain){
		this.domain = domain;
	}
	
	public void setUser(String userName, String password){
		this.userName = userName;
		this.password = password;		
	}
	
	public void openFTPClient(){
		if(!ftpClient.isConnected()) {
			try {
				ftpClient.connect(domain);
				ftpClient.login(userName, password);
				
				int replyCode = ftpClient.getReplyCode();
				//FTPReply stores a set of constants for FTP reply codes
				if(!FTPReply.isPositiveCompletion(replyCode)){
						ftpClient.disconnect();				
				}
				ftpClient.enterLocalPassiveMode();
			} catch (Exception e) {
				// TODO: handle exception
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public void releaseFTPClient(){
		if(ftpClient.isConnected()){
			try {
				ftpClient.disconnect();
			} catch (Exception e) {
				// TODO: handle exception
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public List<ftpFileList> retrieveFileList(){
		List<ftpFileList> listFile = new ArrayList<ftpFileList>();
/*		int replyCode = ftpClient.getReplyCode();
		//FTPReply stores a set of constants for FTP reply codes
		if(!FTPReply.isPositiveCompletion(replyCode)){
			try{
				ftpClient.disconnect();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		ftpClient.enterLocalPassiveMode();*/

		try {
			//change directory to study
			//ftpClient.changeWorkingDirectory(this.relativePath);
			//get file list from current directory
			FTPFile[] ftpFilesList = ftpClient.listFiles(); 
			//get all files information
			for(FTPFile file : ftpFilesList){
				String name = file.getName();
				Date fileDate = file.getTimestamp().getTime();
				
				ftpFileList tempFile = new ftpFileList(name, fileDate, relativePath);
				listFile.add(tempFile);		
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return listFile;
	}
	
	//download file
	public void downloadFile(String filePath, String fileName){
		File downloadFile = new File(filePath + '/' + fileName);
		//if this file exits, should read it, compare the update date
		//if not exits, download, and should write the update date inside
		OutputStream os = null;
		try {
			os = new FileOutputStream(downloadFile);
			boolean fileExits = ftpClient.retrieveFile(fileName, os);
			if(fileExits == false){
				//determine file whether or not in the ftp, if not, delete the empty download file
				downloadFile.delete();
			}
			/*if(downloadFile.exists()){
				FileWriter fileWriter = new FileWriter(downloadFile, true);
			}*/
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}catch(IOException e){
			System.err.println(e.getMessage());
			e.printStackTrace();
		}finally{
			if(os != null){
				try {
					os.close();
				} catch (IOException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
	
	//get the latest update file name
	public String getLatestFileName(List<ftpFileList> listFile){
		int nLen = listFile.size();
		//get files start with "phs"
		List<ftpFileList> tmpFileList = new ArrayList<ftpFileList>();
		for(int i = 0; i < nLen; i ++){
			String temString = listFile.get(i).getName();
			if(temString.startsWith("phs") && temString.contains(".")){
				//set vNum and pNum here
				//the file name looks like(phs****.v*.p*.xml)
				int end = temString.length();//get the string end position
				int firstDot = temString.indexOf(".");//get the first dot position
				int secondDot = temString.indexOf(".", firstDot+1);//get the second dot position
				
				int nVNum = Integer.valueOf(temString.substring(firstDot + 2, secondDot));
				int nPNum = Integer.valueOf(temString.substring(secondDot + 2, end));
				
				listFile.get(i).setVersionNum(nVNum, nPNum);				
				tmpFileList.add(listFile.get(i));
			}
		}
		
		nLen = tmpFileList.size();
		//compare version number
		String fileName = null;
		if(nLen != 0){
			ftpFileList maxFile = tmpFileList.get(0);
			fileName = maxFile.getName();
			for(int i = 1; i < nLen; i ++){
				ftpFileList tmpFile = tmpFileList.get(i);
				if(tmpFile.getVNum() > maxFile.getVNum()){
					//the number after "v" is bigger
					fileName = tmpFile.getName();
					maxFile = tmpFile;
				}
				else if(tmpFile.getVNum() == maxFile.getVNum()){
					//if the number after "v" are same, compare the number after p, get the bigger one's file Name
					if(tmpFile.getPNum() > maxFile.getPNum()){
						fileName = tmpFile.getName();
						maxFile = tmpFile;
					}
				}
			}
		}
		
		/*
		//compare update time method
		Date maxDate = null;
		String fileName = null;
		if(nLen != 0){
			maxDate = listFile.get(0).getLastedUpdateTime();
			fileName = listFile.get(0).getName();
			
			
			for(int i = 1; i < nLen; i ++){
				Date temDate = listFile.get(i).getLastedUpdateTime();
				
				//the date is closer to now, means the file is more recent
				if(temDate.compareTo(maxDate) >= 0){
					maxDate = temDate;
					fileName = listFile.get(i).getName();
				}
			}
		}*/
		return fileName;
	}
	
	//parse GapExchange file
	public void parseGapExchange(String filePath, String fileName, boolean bLoadDB, boolean bDiseases, boolean bKeepOrgTable) throws SAXParseException{
		try{
			File file = new File(filePath + '/' + fileName);
			if(file.exists()){
				//if file exists, then parse this file, if not, do nothing
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				
				//make sure UTF-8
				InputStream is = new FileInputStream(file);
				Reader reader  = new InputStreamReader(is, "UTF-8");
				InputSource source = new InputSource(reader);
				Document doc = builder.parse(source);
				
				//dbConnect dbConn = new dbConnect();
				//dbConn.connectDB();
				
				String orgStudyTableName = dbConn.getOrgStudyTableName();
				String diseaseTableName = dbConn.getDiseaseTableName();
				String  studyTableName = dbConn.getStudyTableName();
				
				//get tag is "Study" node list
				NodeList nodeList = doc.getElementsByTagName("Study");
				int nLen = nodeList.getLength();
				for(int nIndex = 0; nIndex < nLen; nIndex ++){
					Node stdyNode = nodeList.item(nIndex);
					//get studyID first
					String styID = stdyNode.getAttributes().getNamedItem("accession").getNodeValue();
					
					if(stdyNode.hasChildNodes()){
						NodeList childNodeList = stdyNode.getChildNodes();	
						int nChildCount = childNodeList.getLength();
						
						//get tag is "Configuration" node List
						for(int nChildIndex = 0; nChildIndex < nChildCount; nChildIndex ++){	
							Node stdyChild = childNodeList.item(nChildIndex);
							String childNodeName = stdyChild.getNodeName();
							if(childNodeName.equals("Configuration") && stdyChild.hasChildNodes()){
								NodeList confChildList = stdyChild.getChildNodes();
								int nConfChildCount = confChildList.getLength();
								for(int nConfIndex = 0; nConfIndex < nConfChildCount; nConfIndex ++){
									Node confChildNode = confChildList.item(nConfIndex);
									String confChildName = confChildNode.getNodeName();
									if(confChildName.equals("Description")){
										//get description		
										if(confChildNode.hasChildNodes()){
											NodeList descList = confChildNode.getChildNodes();
											int nDescCount = descList.getLength();
											String description = "";
											for(int i = 0; i < nDescCount; i += 2){
												//i+=2 cause the even is not the correct one
												Node descCData = descList.item(i);
												if(descCData instanceof CharacterData){
													CharacterData cData = (CharacterData) descCData;
													String cDataString = cData.getData();
													description += cDataString;										
												}
												//remove html tag
												//htmlParse html = new htmlParse(description);
												//description = html.parseDescription();
												
												description = description.replaceAll("'", "''");
												if(bLoadDB){
													//7.8
													//update
													String sql = "update " + studyTableName
															+ " set description = '" + description + "' "
															+ "where dbgap_study_id = '" + styID + "'";
													dbConn.insertData(sql);
													//update orgStudyTableName
													if(bKeepOrgTable){
														sql = "update " + orgStudyTableName
																+ " set Description = '" + description + "' "
																		+ "where Study_Accession_ID = '" + styID + "'";
														dbConn.insertData(sql);
													}												
												}
											}
										}
									}
									else if(confChildName.equals("Diseases")){
										//get Diseases
										if(confChildNode.hasChildNodes()){
											NodeList diseasNodeList = confChildNode.getChildNodes();
											int nDiseasCount = diseasNodeList.getLength();
											String diseaseSet = null;
											for(int i = 0; i < nDiseasCount; i ++){
												Node diseaseNode = diseasNodeList.item(i);
												if(diseaseNode.hasAttributes()){
													
													// for each disease, get vocab_source and vocab_term
													String vocabulary = diseaseNode.getAttributes().getNamedItem("vocab_source").getNodeValue();
													vocabulary = vocabulary.replaceAll("'", "''");
													String disease = diseaseNode.getAttributes().getNamedItem("vocab_term").getNodeValue();
													disease = disease.replaceAll("'", "''");
													if(diseaseSet == null){
														diseaseSet = disease;
													}
													else{
														diseaseSet = diseaseSet + ";" + disease;
													}
													
													if(bLoadDB){
														//update study table, diseases
														String sql = "update " + studyTableName
																+ " set diseases = '" + diseaseSet + "' "
																+ "where dbgap_study_id = '" + styID + "'";
														dbConn.insertData(sql);
														
														if(bDiseases){
															//if need the individual table Diseases 
															//insert into table Diseases
															sql = "insert into " + diseaseTableName +"(dbGaP_Study_ID, Vocabulary, Disease) "
																	+ "values('" + styID +"','" + vocabulary + "','" + disease +"')";
															dbConn.insertData(sql);
														}
														
													}
												}
											}
										}
									}
									//7.8
									else if(confChildName.equals("StudyURLs")){
										//get study url
										if(bKeepOrgTable){//if keep the original table, update data 
											if(confChildNode.hasChildNodes()){
												NodeList sdyURLList = confChildNode.getChildNodes();
												int nSdyUrlCount = sdyURLList.getLength();
												String url = "";
												for(int i = 0; i < nSdyUrlCount; i ++){
													Node sdyUrl = sdyURLList.item(i);
													if(sdyUrl.hasAttributes()){
														url = url + sdyUrl.getAttributes().getNamedItem("url").getNodeValue() +"|";
													}
												}
												url = url.replaceAll("'", "''");
												
												if(bLoadDB){
													//insert into table
													String sql = "update " + orgStudyTableName
															+ " set Orig_Study_URL = '" + url + "' "
																	+ "where Study_Accession_ID = '" + styID + "'";
													dbConn.insertData(sql);
												}
											}			
										}
									}
								}
							}
						}
					}
					//disconnect database
					//dbConn.disconnectDB();
				}
			}
			}catch(Exception e){			
				if(e.toString().contains("org.xml.sax.SAXParseException") == false){
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
			}
	}
	
	//parse data_dict.xml file
	public void parseDataDict(String filePath, String fileName, boolean bLoadDB, boolean bVarValue, boolean bKeepOrgTable,HashMap<String, String> varMap){
		try{
			File file = new File(filePath + '/' + fileName);
			if(file.exists()){
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				
				//make sure UTF-8
				InputStream is = new FileInputStream(file);
				Reader reader  = new InputStreamReader(is, "UTF-8");
				InputSource source = new InputSource(reader);
				Document doc = builder.parse(source);
				
				//get study id 
				String studyId = "";
				String participantSet = "";

				NodeList dataTable = doc.getElementsByTagName("data_table");
				Node dataTableNode = dataTable.item(0);
				if(dataTableNode.hasAttributes()){
					participantSet = "p" + dataTableNode.getAttributes().getNamedItem("participant_set").getNodeValue();
					studyId = dataTableNode.getAttributes().getNamedItem("study_id").getNodeValue() + "." + participantSet;
				}
				
				String orgVarTableName = dbConn.getOrgVarTableName();
				String varTableName = dbConn.getVariableTableName();
				String valueTableName = dbConn.getVarValueTableName();
				String studyTableName = dbConn.getStudyTableName();
				
				//select from study table, get study_name and study_url
				String sql = "select dbgap_study_name, study_url "
						+ "from " + studyTableName
						+ " where dbgap_study_id = '" + studyId +"'";
				String[] rstSet = dbConn.getSelectRst(sql);
				String studyName = rstSet[0];
				String studyUrl = rstSet[1];
				
				if(studyName != null){
					studyName = studyName.replaceAll("'", "''");
					//if the study name doesn't exist in the study table, we should not insert the variable into the variable table
					
					//get variables
					NodeList nodeList = doc.getElementsByTagName("variable");
					for(int i = 0; i < nodeList.getLength(); i ++){
						//get each variableId
						String varId = null;
						//get suffix
						String varSuffix = null;
						if(nodeList.item(i).hasAttributes()){
							varId = nodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
							//get suffix
							int nVarIdLen = varId.length();
							for(int j = 3; j < nVarIdLen; j ++){
								char c = varId.charAt(j);
								if(c != '0'){
									varSuffix = varId.substring(j, nVarIdLen-3);
									break;
								}
							}
							varId = varId +"."+participantSet;
						}
						varId = varId.replaceAll("'", "''");
						
						//whether the Hashmap has the key == varSuffix, if not, insert into, if so, compare
						boolean bIncludeKey = varMap.containsKey(varSuffix);
						boolean bAdd = true;//add the new variable, if it not include in the hashmap or it latest than previous one
						boolean bDel = false;//delete the previous 
						/*if(varSuffix.equals("98756")){
							System.out.println("------\nhere\n-------");
						}*/
						if(bIncludeKey == true){
							String keyValue = varMap.get(varSuffix);
							
							//Get keyValue's Version Number and Participant Number
							int keyValueFirstDot = keyValue.indexOf(".");//get the first dot position
							int keyValueSecondDot = keyValue.indexOf(".", keyValueFirstDot+1);//get the second dot position
							int keyValueEnd = keyValue.length();//get the string end position
							int nkeyValueVNum = Integer.valueOf(keyValue.substring(keyValueFirstDot + 2, keyValueSecondDot));
							int nkeyValuePNum = Integer.valueOf(keyValue.substring(keyValueSecondDot + 2, keyValueEnd));
							//Get varId's Version Number and Participant Number
							int varIdFirstDot = varId.indexOf(".");//get the first dot position
							int varIdSecondDot = varId.indexOf(".", varIdFirstDot+1);//get the second dot position
							int varIdEnd = varId.length();//get the string end position
							int nvarIdVNum = Integer.valueOf(varId.substring(varIdFirstDot + 2, varIdSecondDot));
							int nvarIdPNum = Integer.valueOf(varId.substring(varIdSecondDot + 2, varIdEnd));
							
							//compare Version Number
							if(nkeyValueVNum < nvarIdVNum){
								//if the previous variable not the latest, delete it from the variable table and variable value
								bDel = true;	
							}
							else if(nkeyValueVNum == nvarIdVNum){
								//compare the p number
								if(nkeyValuePNum < nvarIdPNum){
									bDel = true;	
								}
								else{
									bAdd = false;
								}
							}
							else{
								bAdd = false;
							}
						}
						if(bDel == true){
							//delete the original one
							String keyVarId = varMap.get(varSuffix);
							if(bLoadDB){
								//delete from the var_value table
								if(bVarValue){
									sql = "delete from " +  valueTableName
											+ " where Variable_ID = '" + keyVarId +"'";
									dbConn.deleteData(sql);
								}
								if(bKeepOrgTable){
									sql = "delete from " + orgVarTableName
											+" where Variable_ID = '" + keyVarId +"'";
									dbConn.deleteData(sql);
								}
								
								sql = "delete from " + varTableName
										+" where variable_id = '" + keyVarId +"'";
								dbConn.deleteData(sql);
							}
						}
						if(bAdd == true){
							varMap.put(varSuffix, varId);
							
							//add the new varialbe into the tables
							NodeList childNodeList = nodeList.item(i).getChildNodes();
							int nLen = childNodeList.getLength();
							
							//all attributes for a varibale
							String varName = null;
							String varDescription = null;
							String varType = null;
							String varUnits = null;
							double logicalMin = 0;
							double logicalMax = 0;
							String comment = null;												
							
							for(int index = 0; index < nLen; index ++){
								//get each child's tag name
								Node childNode = childNodeList.item(index);
								String childTagName = childNode.getNodeName();
			
								if(childTagName.equals("name")){
									if(childNode.hasChildNodes()){
										varName = childNode.getFirstChild().getNodeValue();
										varName = varName.replaceAll("'", "''");
									}
								}
								else if(childTagName.equals("description")){
									if(childNode.hasChildNodes()){
										varDescription = childNode.getFirstChild().getNodeValue();
										varDescription = varDescription.replaceAll("'", "''");
									}
								}
								else if(childTagName.equals("type")){
									if(childNode.hasChildNodes()){
										varType = childNode.getFirstChild().getNodeValue();
										varType = varType.replaceAll("'", "''");
									}
								}
								else if(childTagName.equals("unit")){
									if(childNode.hasChildNodes()){
										varUnits = childNode.getFirstChild().getNodeValue();
										varUnits = varUnits.replaceAll("'", "''");
									}
								}
								else if(childTagName.equals("logical_min")){
									if(childNode.hasChildNodes()){
										logicalMin = Double.parseDouble(childNode.getFirstChild().getNodeValue());
									}
								}
								else if(childTagName.equals("logical_max")){
									if(childNode.hasChildNodes()){
										logicalMax = Double.parseDouble(childNode.getFirstChild().getNodeValue());
									}
								}
								else if(childTagName.equals("value")){
									if(childNode.hasChildNodes() &&  bVarValue){
										
										String value = childNode.getFirstChild().getNodeValue();
										value = value.replaceAll("'", "''");
										
										String valueCode = value;
										if(childNode.getAttributes().getNamedItem("code") != null){
											//if has the attribute named code, then get the value, if not, equal to value
											valueCode = childNode.getAttributes().getNamedItem("code").getNodeValue();
											valueCode.replaceAll("'", "''");
										}
										
										if(bLoadDB){
											//insert into table
											String sqlVar = "insert into " + valueTableName +"(Variable_ID, Variable_Code, Variable_Code_Description)"
													+ " values('" + varId + "','" + valueCode + "','" + value +"')";
											dbConn.insertData(sqlVar);
										}
									}
								}
								else if(childTagName.equals("comment")){
									if(childNode.hasChildNodes()){
										comment = childNode.getFirstChild().getNodeValue();
										comment = comment.replaceAll("'", "''");
									}
								}
							}
							
							if(bLoadDB){							
								//variable_url variable_href
								String varUrl = "http://www.ncbi.nlm.nih.gov/projects/gap/cgi-bin/variable.cgi?study_id=" + studyId + "&phv=" + varSuffix;
								String varhref = "<a href=\"http://www.ncbi.nlm.nih.gov/projects/gap/cgi-bin/variable.cgi?study_id=" + studyId + "&phv=" + varSuffix+ "\">" + varName +"</a>";
								//Study_href
								String studyHref = "<a href= \"http://www.ncbi.nlm.nih.gov/projects/gap/cgi-bin/study.cgi?study_id=" + studyId + "\">" + studyName +"</a>";
								
								sql = "insert into " + varTableName +"(variable_id, variable_name, study_id, study_name, url_suffix, description, study_url, variable_url, study_href, variable_href) "
										+ "values('" + varId +"','" + varName + "','" + studyId + "','" + studyName + "', '" + varSuffix + "', '" 
										+ varDescription + "','" + studyUrl + "', '" + varUrl + "', '" + studyHref + "','" + varhref + "')";
								dbConn.insertData(sql);		
								
								/*test 
								//for test duplicate key
								sql = "insert into VAR_FILE(varId, fileName) values('" + varId + "','" + fileName +"')";
								dbConn.insertData(sql);
								*/							
								if(bKeepOrgTable){
									//insert all attribute to table 		
									sql = "insert into " + orgVarTableName + "(Variable_ID, Variable_Name, Variable_Description, Variable_Type, Varibale_Units, Logical_Minimum, Logical_Maximum,Variable_Comment)"
											+ " values('" + varId + "','" + varName + "','" + varDescription + "','" + varType + "','" + varUnits + "'," + logicalMin + "," + logicalMax + ",'" + comment + "')";
									dbConn.insertData( sql);								
								}
							}
							
						}
					}
					
					//finish parsing file, disconnect database
					//dbConn.disconnectDB();
				}
			}
			//after parsing, delete file
			//file.delete();
		}catch(Exception e){
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	public void backToRootDirct(){
		try {
			ftpClient.changeWorkingDirectory("//");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void backToParentDirct(){
		try {
			ftpClient.changeToParentDirectory();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
