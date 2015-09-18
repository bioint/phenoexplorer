import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

//write config file for database
public class writeConfigFile {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Properties prop = new Properties();
		OutputStream output = null;
		InputStream is = null;
		try {
			output = new FileOutputStream("config/config.properties");
			
			//Properties consProp = new Properties();
			is = new FileInputStream("static/config.properties");
			
			//load file			
			prop.load(is);				
			
			//set table's name
			Date date = new Date();
			DateFormat datefmt = new SimpleDateFormat("_yyyy_MM_dd");
			String suffix = datefmt.format(date);
			
			prop.setProperty("studyTableName", "dbgap_studies" + suffix);
			prop.setProperty("variableTableName", "dbgap_variables" + suffix);
			prop.setProperty("diseaseTableName", "Diseases" + suffix);
			prop.setProperty("varValueTableName", "Variable_Value" + suffix);
			prop.setProperty("orginalStudyTableName", "dbgap_studies_org" + suffix);
			prop.setProperty("orginalVariableTableName", "dbgap_variables_org" + suffix);
			
			//save properties to project root folder
			prop.store(output, null);
			
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			if(output != null){
				try {
					output.close();
				} catch (IOException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
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
	}
}
