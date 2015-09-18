import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class dbConnect {
	
	private String driver;
	private String databaseUrl;
	private String userName;
	private String password;
	private Connection conn;
	
	//four tables
	private String studyTableName;
	private String variableTableName;
	private String diseaseTableName;
	private String varValueTableName;
	private String orginalStudyTableName;
	private String orginalVariableTableName;
	
	public dbConnect(){
		/*
		driver = "com.mysql.jdbc.Driver";
		databaseUrl = "jdbc:mysql://127.0.0.1:3306/dataRetrieve";
		userName = "root";
		password = "";
		*/
		//get value from configuration file
		Properties prop = new Properties();
		InputStream is = null;
		try {
			is = new FileInputStream("config/config.properties");
			//load file
			prop.load(is);
			
			//get value
			this.driver = prop.getProperty("driverclass");
			this.databaseUrl = prop.getProperty("dburl");
			this.userName = prop.getProperty("dbusername");
			this.password = prop.getProperty("dbpassword");
			
			//get table Name
			this.studyTableName = prop.getProperty("studyTableName");
			this.variableTableName = prop.getProperty("variableTableName");
			this.varValueTableName = prop.getProperty("varValueTableName");
			this.diseaseTableName = prop.getProperty("diseaseTableName");
			this.orginalStudyTableName = prop.getProperty("orginalStudyTableName");
			this.orginalVariableTableName = prop.getProperty("orginalVariableTableName");
			
		} catch (IOException e) {
			// TODO: handle exception
			System.err.println(e.getMessage());
			e.printStackTrace();
		}finally{
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		
		conn = null;
	}
	
	//connect database
	public void connectDB(){
		if(conn == null){
			try {
				Class.forName(driver);
				conn = DriverManager.getConnection(databaseUrl, userName, password);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				System.err.println(e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}catch (Exception e) {
				// TODO: handle exception
				System.err.println(e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	public void disconnectDB(){
		//disconnect database
		try {
			if(!conn.isClosed()){
				conn.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	//insert data into table
	public void insertData(String sql){
		try{
			//Class.forName(driver);
			//conn = DriverManager.getConnection(databaseUrl, userName, password);
			if(!conn.isClosed()){
				Statement stmt = conn.createStatement();
				stmt.execute(sql);
			}
		}catch(Exception e){
			if(false == e.getMessage().contains("Duplicate entry") && false == e.getMessage().contains("duplicate key")){
				System.err.println(e.getMessage());
				e.printStackTrace();
				System.out.println(sql);
				System.exit(1);
			}
		}
	}
	//create table
	public void createTable(String sql){
		try{
			//Class.forName(driver);
			//conn = DriverManager.getConnection(databaseUrl, userName, password);
			if(!conn.isClosed()){
				Statement stmt = conn.createStatement();
				stmt.execute(sql);
			}
		}catch(Exception e){
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.out.println(sql);
			System.exit(1);
		}
	}
	
	//delete data
	public void deleteData(String sql){
		try{
			if(!conn.isClosed()){
				Statement stmt = conn.createStatement();
				stmt.execute(sql);
			}
		}catch(Exception e){
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.out.println(sql);
			System.exit(1);
		}
	}
	
	//get result 
	public String[] getSelectRst(String sql){
		//cause use primary key to get result, so only two
		String[] resultSet = new String[2];
		try {
			if(!conn.isClosed()){
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while(rs.next()){
					resultSet[0] = rs.getString("dbgap_study_name");
					resultSet[1] = rs.getString("study_url");
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		return resultSet;
	}
	
	//get all variables' name
	public List<String> getVarName(String sql){
		List<String> varNameList = new ArrayList<String>();
		try{
			//Class.forName(driver);
			//conn = DriverManager.getConnection(databaseUrl, userName, password);
			if(!conn.isClosed()){
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while(rs.next()){
					String varName = rs.getString("variable_id");
					varNameList.add(varName);
				}
			}
		}catch(Exception e){
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		return varNameList;
	}
	
	//get selected variables' url
	public List<List<String>> getSelectedVarUrl(String sql){
		List<List<String>> llRst = new ArrayList<List<String>>();
		try{
			//Class.forName(driver);
			//conn = DriverManager.getConnection(databaseUrl, userName, password);
			if(!conn.isClosed()){
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while(rs.next()){
					String varId = rs.getString("variable_id");
					String varUrl = rs.getString("variable_url");
					List<String> rstList = new ArrayList<String>();
					rstList.add(varId);
					rstList.add(varUrl);
					llRst.add(rstList);
				}
			}
		}catch(Exception e){
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		return llRst;
	}
	
	//get all variables' description 
	public List<String> getVarDesc(String sql){
		List<String> lRst = new ArrayList<String>();
		try {
			if(!conn.isClosed()){
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while(rs.next()){
					String varDesc = rs.getString("description").replace("\n", " ").replace("\t", " ");
					lRst.add(varDesc);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		return lRst;
	}
	
	//get all variables' 
	public List<String> getVarInfo(String sql){
		List<String> lRst = new ArrayList<String>();
		try {
			if(!conn.isClosed()){
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while(rs.next()){
					String varId = rs.getString("variable_id");					
					String varName = rs.getString("variable_name");
					String varDesc = rs.getString("description").replace("\n", " ").replace("\t", " ");
					String styId = rs.getString("study_id");
					String styName = rs.getString("study_name");
					String urlSuffix = styId + "&phv=" + rs.getString("url_suffix");
					String varInfo = varId + "\t" + varName + "\t" + varDesc + "\t" + urlSuffix + "\t" + styId + "\t" + styName;
					lRst.add(varInfo);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		return lRst;
	}
	
	public String getStudyTableName(){
		return this.studyTableName;
	}
	
	public String getVariableTableName(){
		return this.variableTableName;
	}
	
	public String getVarValueTableName(){
		return this.varValueTableName;
	}
	
	public String getDiseaseTableName(){
		return this.diseaseTableName;
	}
	
	public String getOrgStudyTableName(){
		return this.orginalStudyTableName;
	}
	
	public String getOrgVarTableName(){
		return this.orginalVariableTableName;
	}
}
