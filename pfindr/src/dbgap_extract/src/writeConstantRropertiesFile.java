import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;


public class writeConstantRropertiesFile {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Properties prop = new Properties();
		OutputStream output = null;
		try {
			output = new FileOutputStream("static/config.properties");
			
			//set value 
			prop.setProperty("driverclass", "com.mysql.jdbc.Driver");
			prop.setProperty("dburl", "jdbc:mysql://127.0.0.1:3306/dataRetrieve");
			prop.setProperty("dbusername", "root");
			prop.setProperty("dbpassword", "");
			
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
		}
	}

}
