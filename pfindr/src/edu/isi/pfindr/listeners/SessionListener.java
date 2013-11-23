package edu.isi.pfindr.listeners;

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

import java.sql.Connection;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

import edu.isi.pfindr.helper.Utils;

/**
 * Application Lifecycle Listener implementation class SessionListener
 *
 * @author Serban Voinea
 * 
 */
@WebListener
public class SessionListener implements HttpSessionListener {

	/**
	 * Default constructor. 
	 */
	public SessionListener() {
		// TODO Auto-generated constructor stub
		Logger.getLogger("AppLogging").info("Creating SessionListener...");
	}

	/**
	 * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
	 */
	public void sessionCreated(HttpSessionEvent arg0) {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpSessionListener#sessionDestroyed(HttpSessionEvent)
	 */
	public void sessionDestroyed(HttpSessionEvent arg0) {
		// TODO Auto-generated method stub
		HttpSession session = arg0.getSession();
		String tempTableName = (String) session.getAttribute("newPhenotypeTableName");
		Connection conn = (Connection) session.getAttribute("conn");
		if (tempTableName != null && conn != null) {
			Utils.dropTable(conn, tempTableName);
		}

		Logger.getLogger("AppLogging").info("Session " + session.getId() + " is being destroyed...");
	}

}
