package edu.isi.pfindr.servlets;

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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.core.BaseConnection;

import edu.isi.pfindr.helper.Utils;
import edu.isi.pfindr.listeners.SessionActivationListener;


/**
 * Servlet implementation class Login
 * 
 * @author Serban Voinea
 */

@WebServlet(description = "Login Servlet", urlPatterns = { "/login" })
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static private Logger logger = Logger.getLogger("AppLogging");       
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LoginServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String digestPassword = Utils.digest(password);
		boolean authenticate = false;
		Connection conn = null;
		String dbPassword = null;
		boolean mustchange = false;
		boolean isadmin = false;
		boolean isdefiner = false;

		if (username != null && username.length() > 0 && password != null && password.length() > 0) {
			//InitialContext cxt;
			try {
				ServletContext context = getServletContext();
				String driverclass = context.getInitParameter("driverclass"); 
				String dburl=context.getInitParameter("dbUrl");
			    String dbusername=context.getInitParameter("dbUserName");
			    String dbpassword=context.getInitParameter("dbPassword");
				
				/*cxt = new InitialContext();
				DataSource ds = (DataSource) cxt.lookup( "java:/comp/env/jdbc/pfindr" );
				conn = ds.getConnection();
				*/
			    Class.forName(driverclass);
				conn = DriverManager.getConnection(dburl, dbusername, dbpassword);
				
				System.out.println("DriverManager \"" + conn.getClass().getName() + "\" from library \"" + 
						conn.getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + 
						"\" is instance of BaseConnection: "+ (conn instanceof BaseConnection));
				String sqlQuery = Utils.getUserRoleSQL();
				PreparedStatement stmt = conn.prepareStatement(sqlQuery);
				stmt.setString(1, username);
				JSONArray res = Utils.executeSQL(stmt);
				stmt.close();
				if (res.length() > 0) {
					JSONArray row = res.getJSONArray(0);
					dbPassword = row.getString(0);
					mustchange = row.getBoolean(1);
					isadmin = row.getBoolean(2);
					isdefiner = row.getBoolean(3);
					authenticate = dbPassword.equals(digestPassword);
				}
			} /*catch (NamingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/ catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		if (authenticate) {
			HttpSession session = request.getSession(true);
			if (session.isNew() == false) {
				session.invalidate();
				session = request.getSession(true);
			}  
			session.setAttribute("user", username);
			session.setAttribute("isadmin", isadmin);
			session.setAttribute("isdefiner", isdefiner);
			session.setAttribute("mustchange", mustchange);
			session.setAttribute("conn", conn);
			session.setAttribute("history", new ArrayList<JSONObject>());
			session.setAttribute("listener", new SessionActivationListener());
			logger.info("User \"" + username + "\" logged in.");

			// 100 minutes idle time
			session.setMaxInactiveInterval(1000 * 60);
			if (mustchange) {
				RequestDispatcher dispatcher = request.getRequestDispatcher("/resources/PasswordChange.jsp");
				dispatcher.forward(request, response);
			} else {
				//response.sendRedirect("/pfindr/query?action=homepage");
				response.sendRedirect("/pfindr/explore");
			}
		} else {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			String error = "Login failure";
			if (username != null && username.length() > 0) {
				error += " for user \"" + username + "\".";
			} else {
				error += ". No username provided.";
			}
			logger.error(error);
			RequestDispatcher dispatcher = request.getRequestDispatcher("/resources/error.html");
			dispatcher.forward(request, response);
		}
	}

}
