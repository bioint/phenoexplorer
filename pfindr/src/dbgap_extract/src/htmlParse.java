import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class htmlParse {
	
	private String parseContent;
	private String url; //default url
	private int nPageSize;
	private int nResultCount;
	private String phid;
	private String sid;
	private dbConnect dbConn;
	
	public htmlParse(String ParseContent, dbConnect dbConn){
		this.parseContent = ParseContent;
		this.dbConn = dbConn;
		this.url = null;
		nPageSize = 0;
		nResultCount = 0;
	}
	
	public htmlParse(String parseContent, String  url, dbConnect dbConn){
		this.parseContent = parseContent;
		this.url = url;
		this.dbConn = dbConn;
		nPageSize = 0;
		nResultCount = 0;
		phid = "";
	}
	
	public void setContent(String parseContent) {
		this.parseContent = parseContent;
	}
	
	public void setURL(String url){
		this.url = url;
	}
	
	public void getPageInfo(){
		Document doc = Jsoup.parse(parseContent);
		Elements metaElms = doc.getElementsByTag("meta");	
		for(Element p : metaElms){
			String attName = p.attr("name");
			if(attName.equals("ncbi_pagesize")){
				this.nPageSize = Integer.valueOf(p.attr("content"));
			}
			else if(attName.equals("ncbi_resultcount")){
				this.nResultCount = Integer.valueOf(p.attr("content"));
			}
			
			else if(attName.equals("ncbi_phid")){
				this.phid = "ncbi_prevPHID=" + p.attr("content");
			}
			/*
			else if(attName.equals("ncbi_sessionid")){
				this.sid = "ncbi_sid=" + p.attr("content");
			}
			*/
		}
	}
	
	public int getMaxPageSize(){
		int nMaxPageSize = 0;
		Document doc = Jsoup.parse(parseContent);
		Element ele = doc.getElementsByClass("column_list").get(0);
		for(Element p : ele.select("ul > li")){
			nMaxPageSize = Integer.valueOf(p.select("label").text());
		}
		return nMaxPageSize;
	}
	
	public int getPageSize(){
		return nPageSize;
	}
	
	public int getResultCount(){		
		return nResultCount;
	}
	
	public String getPhid(){
		return phid;
	}
	
	public String getSid(){
		return sid;
	}
	
	public void parseStudyTable(boolean bKeepOrgTable){
		Document doc = Jsoup.parse(parseContent);

		//dbConnect dbConn = new dbConnect();
		//dbConn.connectDB();
		String orgStudyTableName = dbConn.getOrgStudyTableName();
		String studyTableName = dbConn.getStudyTableName();
		
		Element tableHeaader = doc.getElementsByTag("table").get(0);

		for(Element ele : tableHeaader.select("tbody > tr")){
			if(!ele.select("td").toString().equals("")){
				//get study ID & URL & NAME
				//the html gives the relative url
				String stdName = ele.select("td").get(0).select("a").get(1).text();
				stdName = stdName.replaceAll("'", "''");				
				String stdURL = url + ele.select("td").get(0).select("a").get(0).attr("href");
				String stdID = ele.select("td").get(0).select("a").get(0).text();
				if(stdID.startsWith("EGAS") == false || stdID.startsWith("EGAS") == true){
					
					//get Embargo Release
					String rls = "";
					Element rlsParent = ele.select("td").get(1);
	
					int nRlsChildCount = rlsParent.childNodeSize();
					for(int nIndex = 0; nIndex < nRlsChildCount; nIndex++){
						//get each child Node
						Node child = rlsParent.childNode(nIndex);
						String childTagName = child.nodeName();
						if(childTagName.equals("br") && child.nextSibling() != null){
							//if there is a <br> tag means there has a line
							nIndex += 1;
							while(true){
								Node temChild = rlsParent.childNode(nIndex);
								String value = null;
								int nChildNodeSize = temChild.childNodeSize();
								if(nChildNodeSize > 0){
									//for special case <span>
									value = temChild.childNode(0).toString();
									for(int i = 1; i < nChildNodeSize; i ++){
										value += temChild.childNode(i).toString();
									}
								}
								else{
									value = temChild.toString();
								}
								rls += value;
								child = rlsParent.childNode(++nIndex);
								if(child.nodeName().equals("br")){
									nIndex --;
									break;
								}
							}						
						}
						if(!rls.equals("")){
							rls += "|";
						}
					}
					if(!rls.equals("")){
						rls = rls.substring(0, rls.length() - 2);//remove the last "|"
						rls = rls.replaceAll("'", "''");
					}
					//get Participate
					Integer nParticipate = Integer.valueOf(ele.select("td").get(3).text());
					//get Study Type
					String stdType = ele.select("td").get(4).text();
					//stdType = stdType.replaceAll(",", "|");
					stdType = stdType.replaceAll("'", "\'");
					
					//get platform
					String platforms = ele.select("td").get(6).select("div").html();
					String platform = platforms.replaceAll("<br />", "|").replaceAll("\n", "");
					while(platform.contains("<")){
						//this string has html tag, we should remove the inside content
						int nStart = platform.indexOf('<');
						int nEnd = platform.indexOf('>');
						//if the tag is the last one, remove it directly
						if(nEnd + 1 <= platform.length() - 1){
							platform = platform.substring(0, nStart - 1) + platform.substring(nEnd + 1, platform.length() - 1);
						}
						else{
							platform = platform.substring(0, nStart - 1);
						}
					}
					
					platform = platform.replaceAll("'", "''");
					if(platform.endsWith("|")){
						platform = platform.substring(0,platform.length() - 1);
					}
					
					//
					String sql = "insert into " + studyTableName + "(dbgap_study_id, dbgap_study_name, participants, study_type, platform, genetic_type, race, sex, min_age, max_age, description, diseases, study_url)"
							+ " values('" + stdID + "','" + stdName + "'," + nParticipate + ",'" + stdType + "','" + platform +"', null, null, null, null, null, null, null, '" + stdURL +"')";
					dbConn.insertData(sql);
					
					if(bKeepOrgTable){
						//insert into database
						 sql = "insert into "+orgStudyTableName +"(Study_Accession_ID, Study_URL, Study_Name, Embargo_Release, Participants, Type_Of_Study, Platform)"
								+ " values('" + stdID + "','" + stdURL + "','" + stdName + "','" + rls + "'," + nParticipate + ",'" + stdType + "','" + platform +"')";
						dbConn.insertData(sql);
					}
				}
				
				
			}
		}		
		//dbConn.disconnectDB();
	}
	
	public void parseVariableTable(){
		Document doc = Jsoup.parse(parseContent);

		//dbConnect dbConn = new dbConnect();
		//dbConn.connectDB();
		
		String varTableName = dbConn.getVariableTableName();

		//if(doc.getElementsByTag("table").size() != 0){
		Element tableHeaader = doc.getElementsByTag("table").get(0);

		for(Element ele : tableHeaader.select("tbody > tr")){
			if(!ele.select("td").toString().equals("")){
				String varId = ele.select("td").get(3).select("a").get(0).text();
				String varUrl = url + ele.select("td").get(3).select("a").attr("href");
				varUrl = varUrl.replaceAll("'", "''");
				String varHref = ele.select("td").get(3).html();
				varHref = varHref.replaceAll("'", "''");
				
				//update url
				String sql = "update " + varTableName 
						+ " set variable_url = '" + varUrl + "' "
								+ "where variable_id = '" + varId +"'";
				dbConn.insertData(sql);
				
				//update href
				sql = "update " + varTableName 
						+ " set variable_href = '" + varHref + "' "
						+ "where variable_id = '" + varId +"'";
				dbConn.insertData(sql);
			}
		}
	//	}
		//disconnect database
		//dbConn.disconnectDB();
	}
	
	public String parseDescription(){
		Document doc = Jsoup.parse(parseContent);
		String description = "";
		Elements pElm = doc.select("p");
		//before <p> may have text!!!!!
		
		for(Element p : pElm){
			description = description +  "  " + p.text() + "\n";
		}
		description = description.replaceAll("'", "&#39;");
		return description;
	}
}
