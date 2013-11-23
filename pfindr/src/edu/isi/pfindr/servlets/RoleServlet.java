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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.pfindr.helper.Utils;

/**
 * Utility class for servlet processing
 * 
 * @author Serban Voinea
 * 
 */

/**
 * Servlet implementation class Role
 */
@WebServlet(description = "Manage roles", urlPatterns = { "/role" })
public class RoleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static private Logger logger = Logger.getLogger("AppLogging");       

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RoleServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		super.init(config);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String action = request.getParameter("action");
		if (action != null) {
			if (action.equals("newuser")) {
				GregorianCalendar ashg = new GregorianCalendar(2012, 10, 7, 10, 0, 0);
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				System.out.println("ashg: " + dateFormat.format(ashg.getTime()));
				GregorianCalendar now = new GregorianCalendar();
				RequestDispatcher dispatcher = request.getRequestDispatcher("/resources/RegisterUserUnavailable.html");
				if (now.after(ashg)) {
					dispatcher = request.getRequestDispatcher("/resources/RegisterUser.jsp");
				}
				dispatcher.forward(request, response);
			} else {
				doPost(request, response);
			}
		} else {
			doPost(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String action = request.getParameter("action");
		if (action != null && action.equals("register")) {
			if (request.getSession().getAttribute("recaptcha") == null) {
				RequestDispatcher dispatcher = request.getRequestDispatcher("/resources/RegisterUser.jsp");
				dispatcher.forward(request, response);
				return;
			}
			JSONObject res = registerUser(request);
			try {
				if (res.getBoolean("success")) {
					request.getSession().removeAttribute("recaptcha");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String text = res.toString();

			PrintWriter out = response.getWriter();
			out.print(text);
			return;
		} else if (action != null && action.equals("verify")) {
			String remoteAddr = request.getRemoteAddr();
			//String privateKey = "6Ld5FtgSAAAAAPFVq7QNHh1MP-nGosa1C5fnQY1_";	// phenoexplorer.org 
			
			ServletContext context = getServletContext();
			String privateKey = context.getInitParameter("recaptchaPrivateKey");
			
			ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
			reCaptcha.setPrivateKey(privateKey);
			String challenge = request.getParameter("recaptcha_challenge_field");
			String uresponse = request.getParameter("recaptcha_response_field");
			ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);
			logger.info("remoteAddr: " + remoteAddr + ", uresponse: " + uresponse);
			JSONObject obj = new JSONObject();
			try {
				boolean isValid = reCaptchaResponse.isValid();
				obj.put("success", isValid);
				if (isValid) {
					request.getSession().setAttribute("recaptcha", true);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String text = obj.toString();
			PrintWriter out = response.getWriter();
			out.print(text);
			return;
		}
		try {
			HttpSession session = request.getSession(false);
			Connection conn = (Connection) session.getAttribute("conn");
			String username = (String) session.getAttribute("user");
			action = request.getParameter("action");
			if (action == null) {
				// list all roles
				String sqlQuery = Utils.getRolesSQL();
				PreparedStatement stmt = conn.prepareStatement(sqlQuery);
				JSONArray rows = Utils.executeSQL(stmt);
				stmt.close();
				request.setAttribute("roles", rows);
				RequestDispatcher dispatcher = request.getRequestDispatcher("/resources/Role.jsp");
				dispatcher.forward(request, response);
			} else if (action.equals("change")) {
				// change password
				String oldpassword = request.getParameter("oldpassword");
				if (oldpassword == null) {
					// just go to the Change Password page
					RequestDispatcher dispatcher = request.getRequestDispatcher("/resources/PasswordChange.jsp");
					dispatcher.forward(request, response);
				} else {
					// commit password change
					oldpassword = oldpassword.trim();
					boolean valid = oldpassword.length() != 0;
					String newpassword1 = request.getParameter("newpassword1").trim();
					String newpassword2 = request.getParameter("newpassword2").trim();
					if (valid) {
						String digestPassword = Utils.digest(oldpassword);
						String sqlQuery = Utils.getUserRoleSQL();
						PreparedStatement stmt = conn.prepareStatement(sqlQuery);
						stmt.setString(1, username);
						JSONArray row = Utils.executeSQL(stmt).getJSONArray(0);
						String dbPassword = row.getString(0);
						valid = dbPassword.equals(digestPassword);
						stmt.close();
					}
					if (valid) {
						valid = newpassword1.length() != 0 && newpassword2.length() != 0 && newpassword1.equals(newpassword2) && !oldpassword.equals(newpassword1);
					}
					if (!valid) {
						request.setAttribute("error", true);
						RequestDispatcher dispatcher = request.getRequestDispatcher("/resources/PasswordChange.jsp");
						dispatcher.forward(request, response);
					} else {
						String newpassword = Utils.digest(newpassword1);
						String sqlQuery = Utils.updatePasswordSQL();
						PreparedStatement stmt = conn.prepareStatement(sqlQuery);
						stmt.setString(1, newpassword);
						stmt.setBoolean(2, false);
						stmt.setString(3, username);
						stmt.executeUpdate();
						stmt.close();
						request.setAttribute("success", true);
						session.setAttribute("mustchange", false);
						logger.info("User \"" + username + "\" has changed the password.");
						RequestDispatcher dispatcher = request.getRequestDispatcher("/resources/PasswordChange.jsp");
						dispatcher.forward(request, response);
					}
				}
			} else if (action.equals("admin")) {
				String role = request.getParameter("role");
				String sqlQuery = Utils.getUserRoleSQL();
				PreparedStatement stmt = conn.prepareStatement(sqlQuery);
				stmt.setString(1, role);
				JSONArray row = Utils.executeSQL(stmt).getJSONArray(0);
				boolean isadmin = row.getBoolean(2);
				stmt.close();
				sqlQuery = Utils.updateAdminSQL("isadmin");
				stmt = conn.prepareStatement(sqlQuery);
				stmt.setBoolean(1, !isadmin);
				stmt.setString(2, role);
				stmt.executeUpdate();
				stmt.close();
				logger.info((isadmin ? "Reset " : "Set ") + "admin role for user \"" + role + "\".");
				if (role.equals(username) && isadmin) {
					// the user has removed himself from the admin role
					session.setAttribute("isadmin", !isadmin);
					response.sendRedirect("/pfindr/query");
				} else {
					response.sendRedirect("/pfindr/role");
				}
			} else if (action.equals("definer")) {
				String role = request.getParameter("role");
				String sqlQuery = Utils.getUserRoleSQL();
				PreparedStatement stmt = conn.prepareStatement(sqlQuery);
				stmt.setString(1, role);
				JSONArray row = Utils.executeSQL(stmt).getJSONArray(0);
				boolean isdefiner = row.getBoolean(3);
				stmt.close();
				sqlQuery = Utils.updateAdminSQL("isdefiner");
				stmt = conn.prepareStatement(sqlQuery);
				stmt.setBoolean(1, !isdefiner);
				stmt.setString(2, role);
				stmt.executeUpdate();
				stmt.close();
				logger.info((isdefiner ? "Reset " : "Set ") + "definer role for user \"" + role + "\".");
				response.sendRedirect("/pfindr/role");
			} else if (action.equals("create")) {
				String role = request.getParameter("role").trim();
				if (role.length() != 0) {
					String sqlQuery = Utils.createUserSQL();
					PreparedStatement stmt = conn.prepareStatement(sqlQuery);
					stmt.setString(1, role);
					stmt.setBoolean(2, true);
					stmt.setBoolean(3, false);
					stmt.setBoolean(4, true);
					stmt.executeUpdate();
					stmt.close();
					logger.info("Created user \"" + role + "\".");
				}
				response.sendRedirect("/pfindr/role");
			} else if (action.equals("delete")) {
				String role = request.getParameter("role");
				String sqlQuery = Utils.deleteUserSQL();
				PreparedStatement stmt = conn.prepareStatement(sqlQuery);
				stmt.setString(1, role);
				stmt.executeUpdate();
				stmt.close();
				logger.info("Deleted user \"" + role + "\".");
				response.sendRedirect("/pfindr/role");
			} else if (action.equals("reset")) {
				String role = request.getParameter("role").trim();
				logger.info("Reset password for user \"" + role + "\".");
				String tempPassword = Utils.pwgen();
				request.setAttribute("tempPassword", tempPassword);
				String digestPassword = Utils.digest(tempPassword);
				String sqlQuery = Utils.updatePasswordSQL();
				PreparedStatement stmt = conn.prepareStatement(sqlQuery);
				stmt.setString(1, digestPassword);
				stmt.setBoolean(2, true);
				stmt.setString(3, role);
				stmt.executeUpdate();
				stmt.close();
				RequestDispatcher dispatcher = request.getRequestDispatcher("/resources/PasswordReset.jsp");
				dispatcher.forward(request, response);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Register a new user
	 * 
	 * @param request
	 *            the servlet request
	 * @return the operation status
	 */
	private JSONObject registerUser(HttpServletRequest request) {
		String error = null;
		try {
			String firstName = request.getParameter("firstName");
			String lastName = request.getParameter("lastName");
			String userid = request.getParameter("userid");
			String email = request.getParameter("email");
			String institution = request.getParameter("institution");
			
			ServletContext context = getServletContext();
			String driverclass = context.getInitParameter("driverclass"); 
			String dburl=context.getInitParameter("dbUrl");
		    String dbusername=context.getInitParameter("dbUserName");
		    String dbpassword=context.getInitParameter("dbPassword");
		    Class.forName(driverclass);
		    Connection conn = DriverManager.getConnection(dburl, dbusername, dbpassword);
			
			// create new user
			String sqlQuery = Utils.createUserSQL(firstName, lastName, email, institution);
			PreparedStatement stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, userid);
			stmt.setBoolean(2, true);
			stmt.setBoolean(3, false);
			stmt.setString(4, firstName);
			stmt.setString(5, lastName);
			stmt.setString(6, email);
			stmt.setString(7, institution);
			stmt.setBoolean(8, true);
			stmt.executeUpdate();
			stmt.close();
			logger.info("Registered user \"" + userid + "\".");
			
			// generate temporary password for the new registered user
			logger.info("Generate temporary password for the new registered user \"" + userid + "\".");
			String tempPassword = Utils.pwgen();
			request.setAttribute("tempPassword", tempPassword);
			String digestPassword = Utils.digest(tempPassword);
			sqlQuery = Utils.updatePasswordSQL();
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, digestPassword);
			stmt.setBoolean(2, true);
			stmt.setString(3, userid);
			stmt.executeUpdate();
			stmt.close();
			conn.close();
			
			// send emails for he new registered user
			String text = "The account for user \"" + userid + "\" was created.\n\n";
			text += "\t* Temporary password: \"" + tempPassword + "\".\n";
			text += "\t* User must change password on next login.\n\n\n\n";
			text += "Do not reply to this message.  This is an automated message generated by the system, which does not receive email messages.";
			InternetAddress	addresses[] = new InternetAddress[1];
			addresses[0] = new InternetAddress(email);
			boolean res = Utils.sendMessage(addresses, "Account Created", text);
			if (res) {
				text = "The account for the following user was created:\n\n";
				text += "\t* First Name: " + firstName + "\n";
				text += "\t* Last Name: " + lastName + "\n";
				text += "\t* Institution: " + institution + "\n";
				text += "\t* Email: " + email + "\n";
				text += "\t* UserId: " + userid + "\n\n\n\n\n";
				text += "Do not reply to this message.  This is an automated message generated by the system, which does not receive email messages.";
				addresses = new InternetAddress[3];
				addresses[0] = new InternetAddress("serban@isi.edu");
				addresses[1] = new InternetAddress("ambite@isi.edu");
				addresses[2] = new InternetAddress("sharma@isi.edu");
				Utils.sendMessage(addresses, "Account Created", text);
				//Utils.sendMessage(email, "Account Created", text);
			} else {
				error = "Error in creating the account. Please check the email address.";
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = "Error in creating the account. Please use a different email address.";
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject obj = new JSONObject();
		try {
			if (error == null) {
				obj.put("success", true);
			} else {
				obj.put("success", false);
				obj.put("error", error);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}

}
