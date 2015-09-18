import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.*;

public class HttpPostMethod {
	private String url;
	private String ncbi_sid;
	private String webEnv;
	//private String phid;
	private int nCurPage;
	private int ncPage;
	private int nPageSize;
	private int nLastPageSize;
	private int nRstCount;
	private String responseBody;
	
	public HttpPostMethod(String url, String ncbi_sid, String webEnv, int ncPage, int nCurPage, int nPageSize, int nLastPageSize, int nRstCount){
		this.url = url;
		this.ncbi_sid = ncbi_sid;
		this.webEnv = webEnv;
		this.nCurPage = nCurPage;
		this.ncPage = ncPage;
		this.nPageSize = nPageSize;
		this.nLastPageSize = nLastPageSize;
		this.nRstCount = nRstCount;
	}
	/*
	public HttpPostMethod(String url, String ncbi_sid, String webEnv, String phid, int ncPage, int nCurPage, int nPageSize, int nLastPageSize, int nRstCount){
		this.url = url;
		this.ncbi_sid = ncbi_sid;
		this.webEnv = webEnv;
		this.phid = phid;
		this.nCurPage = nCurPage;
		this.ncPage = ncPage;
		this.nPageSize = nPageSize;
		this.nLastPageSize = nLastPageSize;
		this.nRstCount = nRstCount;
	}
	*/
	//set URL
	public void setURL(String url) {
		this.url = url;
	}
	
	//change page
	public void changePage(){
		this.nCurPage += 1;
		this.ncPage += 1;

	}
	
	//set Cookie for next http
	public void setCookie(String ncbi_sid, String webEnv) {
		this.ncbi_sid = ncbi_sid;
		this.webEnv = webEnv;
	}
	/*
	public void setCookie(Cookie[] postCookies){
		this.postCookie = postCookies;
	}
	*/
	public void setCookie(String[] cookie){
		this.ncbi_sid = cookie[0];
		this.webEnv = cookie[1];
	}
	
	public void getCookie(String[] cookie, String ncbi_sid, String webEnv) {
		cookie[0] = ncbi_sid;
		cookie[1] = webEnv;
	}

	public String getResponseBodyString() {
		return responseBody;
	}
	
	public void initalResponseBody(){
		this.responseBody = null;
	}
	
	//set lastPageSize
	public void setLastPageSize(int nLastPageSize){
		this.nLastPageSize = nLastPageSize;
	}
	
	//set the page information
	public void setPageInfo(int ncPage, int nCurPage, int nPageSize, int nLastPageSize, int nRstCount){
		this.nCurPage = nCurPage;
		this.ncPage = ncPage;
		this.nPageSize = nPageSize;
		this.nLastPageSize = nLastPageSize;
		this.nRstCount = nRstCount;
	}
	
	public NameValuePair[] setStudiesRequestBody(){
		NameValuePair[] param = { new NameValuePair("term","1[s_discriminator] AND 1[Is Top-Level Study]"),	
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Entrez_PageController.PreviousPageName","results"),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.sPageSize", Integer.toString(nPageSize)),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.FileFormat","SStudies"),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.LastPresentation","SStudies"),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.Presentation","SStudies"),	
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.PageSize",Integer.toString(nPageSize)),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.LastPageSize",Integer.toString(nLastPageSize)),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.Format",""),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.LastFormat",""),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_Pager.cPage",Integer.toString(ncPage)),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_Pager.CurrPage",Integer.toString(nCurPage)),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_ResultsController.ResultCount",Integer.toString(nRstCount)),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_ResultsController.RunLastQuery",""),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_Pager.cPage",Integer.toString(ncPage)),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.sPageSize2",Integer.toString(nPageSize)),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_Filters.CurrFilter","all"),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_Filters.LastFilter","all"),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_MultiItemSupl.RelatedDataLinks.rdDatabase","rddbto"),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_MultiItemSupl.RelatedDataLinks.DbName","gap"),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.HistoryDisplay.Cmd","PageChanged"),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.Db","gap"),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.LastDb","gap"),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.Term","1[s_discriminator] AND 1[Is Top-Level Study]"),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.LastTabCmd",""),	
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.LastQueryKey","1"),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.IdsFromResult",""),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.LastIdsFromResult",""),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.LinkName",""),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.LinkReadableName",""),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.LinkSrcDb",""),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.Cmd","PageChanged"),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.TabCmd",""),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.QueryKey",""),
				new NameValuePair("p$a","EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_Pager.Page"),
				new NameValuePair("p$l","EntrezSystem2"),
				new NameValuePair("p$st","gap")				
		};
		return param;
	}
	
	public NameValuePair[] setVariablesRequestBody(String cmd){
		NameValuePair[] param = { new NameValuePair("term","2[s_discriminator] AND 1[Is Top-Level Study]"),	
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Entrez_PageController.PreviousPageName","results"),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.sPageSize", Integer.toString(nPageSize)),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.FileFormat","svariables"),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.LastPresentation","svariables"),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.Presentation","svariables"),	
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.PageSize",Integer.toString(nPageSize)),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.LastPageSize",Integer.toString(nLastPageSize)),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.Format",""),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.LastFormat",""),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_Pager.cPage",Integer.toString(ncPage)),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_Pager.CurrPage",Integer.toString(nCurPage)),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_ResultsController.ResultCount",Integer.toString(nRstCount)),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_ResultsController.RunLastQuery",""),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_Pager.cPage",Integer.toString(ncPage)),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Gap_DisplayBar.sPageSize2",Integer.toString(nPageSize)),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_Filters.CurrFilter","all"),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_Filters.LastFilter","all"),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_MultiItemSupl.RelatedDataLinks.rdDatabase","rddbto"),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_MultiItemSupl.RelatedDataLinks.DbName","gap"),
				new NameValuePair("EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.HistoryDisplay.Cmd",cmd),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.Db","gap"),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.LastDb","gap"),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.Term","1[Is Top-Level Study] AND 2[s_discriminator]"),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.LastTabCmd",""),	
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.LastQueryKey","2"),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.IdsFromResult",""),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.LastIdsFromResult",""),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.LinkName",""),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.LinkReadableName",""),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.LinkSrcDb",""),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.Cmd",cmd),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.TabCmd",""),
				new NameValuePair("EntrezSystem2.PEntrez.DbConnector.QueryKey",""),
				new NameValuePair("p$a","EntrezSystem2.PEntrez.Gap.Gap3_ResultsPanel.Entrez_Pager.Page"),
				new NameValuePair("p$l","EntrezSystem2"),
				new NameValuePair("p$st","gap")				
		};
		return param;
	}
	
	//send HTTP protocol (POST)
	//inside will get cookie, and set cookie, change page
	public void executePostMethod(int opt){
		//opt = 1, for study; opt = 2, for variable; opt == 3, for display change	
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(url);
		//cookie policy
		method.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);
		//set Request Head
	    //this.ncbi_sid = "396D1D193E02CEB1_0047SID";
		//this.webEnv = "1fm0X5WgqyOcoTBeE_yMfN7E8FpGaQPtXH8ZfsTeeUIepBp_L9_h8nepFjTxySz4ZpKenc_0tKnOA2i-xyN1fyxxqXaLNmPnsyLQOQ%40396D1D193E02CEB1_0047SID";
		String cookie = this.ncbi_sid + "; " + this.webEnv;
		method.setRequestHeader("Cookie", cookie);
		//method.setRequestHeader("Cookie", "ncbi_sid=CE8E395E3C5B40E1_0188SID; WebEnv=1ts74ouCKb7B6UjRTJ23Tn-i7bhStkHUS2LCJzrcXfnba5ukLOPNzBC6YLQT9_P6cZN0ZSQMwWmDDR8u3KYIa2t2xW-m6fIDGevkE%40CE8E395E3C5B40E1_0188SID");			
		//set Request Body	
		NameValuePair[] param = null;
		if(opt == 1){
			param = setStudiesRequestBody();
		}
		else if(opt == 2){
			param = setVariablesRequestBody("PageChanged");
		}
		else if(opt == 3){
			param = setVariablesRequestBody("displaychanged");
		}
		
		method.setRequestBody(param);
		try{
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
		        System.err.println("Method failed: " + method.getStatusLine());
		      }

				//Read Response Head

				Header[] head = method.getResponseHeaders("Set-Cookie");
				int nLen = head.length;		    
				String[] sHead = new String[nLen];
			      
			     //get cookie value
			      for(int i = 0; i < nLen; i++){
			    	  sHead[i] = head[i].toString();
			    	  String[] temp = sHead[i].split(":|;");   	  
			    	  sHead[i] = temp[1].substring(1);//ignore the space
			      }	
			      
			      //set cookie value to the variable
			      setCookie(sHead[0], sHead[1]);
			      changePage();
			      
			      // Read the response body.
			      initalResponseBody();
			    //cover inputstream to string				   
			      InputStream is = method.getResponseBodyAsStream();				    
			      StringBuilder sBuilder = new StringBuilder();				    
			      BufferedReader bReader =new BufferedReader(new InputStreamReader(is, "UTF-8"));				    
			      String line = bReader.readLine();				   
			      while(line != null){				   
			    	  sBuilder.append(line);				    
			    	  sBuilder.append("\n");				    
			    	  line = bReader.readLine();				   
			      }				    
			      responseBody = sBuilder.toString();		
			     // System.out.print(responseBody);
			      //this.responseBody = method.getResponseBodyAsString();	
		}catch (HttpException e) {
		      System.err.println("Fatal protocol violation: " + e.getMessage());
		      e.printStackTrace();
		      System.exit(1);
		}
		catch (IOException e) {
		      System.err.println("Fatal transport error: " + e.getMessage());
		      e.printStackTrace();
		      System.exit(1);
		    } finally {
		      // Release the connection.
		      method.releaseConnection();
		    }  
	}
}
