import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.*;

public class HttpGetMethod {
	private String url;
	private String ncbi_sid;
	private String webEnv;
	private String responseBody;
	//private Cookie[] responseCookies;
	
	//constructor
	public HttpGetMethod(String url) {
		this.url = url;
		this.ncbi_sid = null;
		this.webEnv = null;
	}
	
	public HttpGetMethod(String url, String ncbi_sid, String webEnv) {
		this.url = url;
		this.ncbi_sid = ncbi_sid;
		this.webEnv = webEnv;
	}
	
	//set URL
	public void setURL(String url) {
		this.url = url;
	}
	
	//set Cookie for next http
	public void setCookie(String ncbi_sid, String webEnv) {
		this.ncbi_sid = ncbi_sid;
		this.webEnv = webEnv;
	}
	
	public void setCookie(String[] cookie){
		this.ncbi_sid = cookie[0];
		this.webEnv = cookie[1];
	}
	
	public String getNcbi_sid(){
		return this.ncbi_sid;
	}
	
	public String getWebEnv(){
		return this.webEnv;
	}

	public String getResponseBodyString() {
		return responseBody;
	}
	
	public void initResponseBody(){
		this.responseBody = null;
	}
	/*
	public Cookie[] getCookie(){
		return this.responseCookies;
	}
	*/
	//send HTTP protocol (GET)
	//execute method, then get respond body and cookie
	public void executeGetMethod(int opt) {
		//opt == 1, get Studies from WebSite, opt == 2, get Variables from WebSite
		HttpClient client = new HttpClient();	
		GetMethod method = new GetMethod(this.url);
		
		//get variables from website  set queryParameters
		if(opt == 2){
			NameValuePair[] params = {new NameValuePair("term", "2[s_discriminator] AND 1[Is Top-Level Study]"),
					new NameValuePair("report","SVariables")
			};
			method.setQueryString(params);
		}
		
		//try 3 times?
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
				new DefaultHttpMethodRetryHandler(3, false));
		//cookie policy
		method.getParams().setParameter("http.protocol.cookie-policy",CookiePolicy.BROWSER_COMPATIBILITY);
		
		//set method Header
		//method.setRequestHeader("Cookie", headerValue);
		
		//execute Method
		try{
			int statusCode = client.executeMethod(method);	
			
			if(statusCode != HttpStatus.SC_OK){
				System.err.println("Method failed:" + method.getStatusLine());
			}
			

			//Read Response Head
		//	Header[] tem = method.getResponseHeaders();
			//0805 cookie
			//responseCookies = client.getState().getCookies();
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
		      if(nLen != 0){
		    	  setCookie(sHead[0],sHead[1]);
		      }
		      
		      // Read the response body.
		      initResponseBody();
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
		     //responseBody = method.getResponseBodyAsString();

		}catch (HttpException e) {
		      System.err.println("Fatal protocol violation: " + e.getMessage());
		      System.out.println(url);
		      e.printStackTrace();
		      System.exit(1);
		    } catch (IOException e) {
		      System.err.println("Fatal transport error: " + e.getMessage());
		      e.printStackTrace();
		      System.exit(1);
		    } finally {
		      // Release the connection.
		      method.releaseConnection();
		    }  
	}
}
