package edu.isi.pfindr.learn.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import edu.isi.pfindr.listeners.ServletContextInfo;


/*
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * * ReadProperties.java 
 * 
 * Reads the properties file, for configurations, and connections
 * 
 * @author sharma@isi.edu 
 * 
 */
public class ReadProperties {
	private static Properties properties;  
	private static Logger logger = Logger.getLogger("AppLogging"); 

	static String pathToProperties = "data/model.properties";
	public static Properties readProperties(){
		properties = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(ServletContextInfo.getContextPath()+pathToProperties);
			properties.load(fis);
		} catch (IOException io) {
			logger.error("Problem reading properties file ..");
			io.printStackTrace();
		} catch (Exception e){
			logger.error("Problem reading properties file ..");
			e.printStackTrace();
		}finally{
			try{
				if(fis != null)
					fis.close();
			}catch (IOException io) {
				logger.error("Problem closing properties file ..");
				io.printStackTrace();
			} catch (Exception e){
				logger.error("Problem closing properties file ..");
				e.printStackTrace();
			}
		}
		return properties;
	}

	public static Properties readDatabaseProperties(){
		properties = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(ServletContextInfo.getContextPath()+"data/db_connection.properties");
			properties.load(fis);
		} catch (IOException io) {
			logger.error("Problem reading properties file ..");
			io.printStackTrace();
		} catch (Exception e){
			logger.error("Problem reading properties file ..");
			e.printStackTrace();
		}finally{
			try{
				if(fis != null)
					fis.close();
			}catch (IOException io) {
				logger.error("Problem closing properties file ..");
				io.printStackTrace();
			} catch (Exception e){
				logger.error("Problem closing properties file ..");
				e.printStackTrace();
			}
		}
		return properties;
	}
}
