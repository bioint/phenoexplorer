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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
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

import edu.isi.pfindr.helper.Utils;
import edu.isi.pfindr.learn.test.TestNewPhenotype;

/**
 * Utility class for servlet processing
 * 
 * @author Serban Voinea
 * 
 */

/**
 * Servlet implementation class ExplorerServlet
 */
@WebServlet(description = "Servlet for defining new categories", urlPatterns = { "/define" })
public class ExplorerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static private Logger logger = Logger.getLogger("AppLogging");       
	public static final ArrayList<String> TEMPORARY_MAPPINGS_COLUMNS = new ArrayList<String>(Arrays.asList("variable_match_score", "variable", "varset", "study"));

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ExplorerServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
		String action = request.getParameter("action");
		if (action != null && action.equals("getBookmark")) {
			getBookmarks(request, (Connection) session.getAttribute("conn"));
			// set the bookmark query in the session
			setBookmarkQuery(request, (Connection) session.getAttribute("conn"));
			response.sendRedirect("/pfindr/explore");
		} else if (action != null && action.equals("getQueries")) {
			getBookmarks(request, (Connection) session.getAttribute("conn"));
			RequestDispatcher dispatcher = request.getRequestDispatcher("/resources/ExplorerBookmarks.jsp");
			dispatcher.forward(request, response);
		} else {
			RequestDispatcher dispatcher = request.getRequestDispatcher("/resources/Explorer.jsp");
			dispatcher.forward(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		Connection conn = (Connection) session.getAttribute("conn");
		String action = request.getParameter("action");
		if (action.equals("getBookmarkedStudies")) {
			try {
				JSONObject res = new JSONObject();
				JSONObject json = new JSONObject();
				JSONArray studies = new JSONArray(request.getParameter("studies"));
				json.put("studies", studies);
				String sqlQuery = Utils.getStudieMetadataSQL(json);
				System.out.println(sqlQuery);
				PreparedStatement stmt = conn.prepareStatement(sqlQuery);
				JSONArray rows = Utils.executeSQL(stmt);
				JSONObject flyOver = new JSONObject();
				for (int i=0; i < rows.length(); i++) {
					JSONArray row = rows.getJSONArray(i);
					if (!row.isNull(11)) {
						flyOver.put(row.getString(12), row.getString(11));
					}
					row.remove(12);
					row.remove(11);
				}
				stmt.close();
				res.put("studies", rows);
				res.put("flyover", flyOver);
				String text = res.toString();
				PrintWriter out = response.getWriter();
				out.print(text);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (action.equals("getQueryFilter")) {
			try {
				JSONObject obj = new JSONObject();
				JSONObject queryDescription = (JSONObject) session.getAttribute("query");
				if (queryDescription != null) {
					session.removeAttribute("query");
					obj.put("queryDescription", queryDescription);
				}
				String text = obj.toString();
				PrintWriter out = response.getWriter();
				out.print(text);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (action.equals("create")) {
			//String phenotype = request.getParameter("phenotype");
			String phenotype = "phenotype";
			String description = request.getParameter("description");
			String studies = request.getParameter("studies");
			Logger.getLogger(ExplorerServlet.class).info("Selected studies: " + studies);
			double score = Double.parseDouble(request.getParameter("score"));
			if (description.trim().length() == 0) {
				score = 0;
			}
			Logger.getLogger(ExplorerServlet.class).info("Create phenotype \"" + phenotype + "\" with description: \"" + description + "\", score: " + score);
			String text = "";
			JSONObject res = createPhenotype(request, conn);
			text = res.toString();
			Logger.getLogger(ExplorerServlet.class).info("The result was sent: " + text.length() + " bytes.");
			PrintWriter out = response.getWriter();
			out.print(text);
		} else if (action.equals("bookmark")) {
			String text = "";
			JSONObject res = bookmarkQuery(request, conn);			
			text = res.toString();
			PrintWriter out = response.getWriter();
			out.print(text);
		} else if (action.equals("saveStudies")) {
			// save the query results to a file
			String text = saveStudies(request, conn);
			response.setContentType ("text/plain");
			response.setHeader ("Content-Disposition", "attachment; filename=\"Studies.xls\"");
			response.setContentLength(text.length());
			PrintWriter out = response.getWriter();
			out.print(text);
		} else if (action.equals("saveVariables")) {
			// save the query results to a file
			String text = saveVariables(request, conn);
			response.setContentType ("text/plain");
			response.setHeader ("Content-Disposition", "attachment; filename=\"Variables.xls\"");
			response.setContentLength(text.length());
			PrintWriter out = response.getWriter();
			out.print(text);
		} else if (action.equals("getVariables")) {
			String text = "";
			JSONObject res = getVariables(request, conn);
			text = res.toString();
			PrintWriter out = response.getWriter();
			out.print(text);
		} else if (action.equals("addVariables")) {
			String text = "";
			JSONObject res = addVariables(request, conn);
			text = res.toString();
			PrintWriter out = response.getWriter();
			out.print(text);
		} else if (action.equals("addMappings")) {
			String text = "";
			JSONObject res = addMappings(request, conn);
			text = res.toString();
			PrintWriter out = response.getWriter();
			out.print(text);
		} else if (action.equals("clearShoppingCart")) {
			String text = "";
			JSONObject res = clearShoppingCart(request, conn);
			text = res.toString();
			PrintWriter out = response.getWriter();
			out.print(text);
		} else if (action.equals("review")) {
			String text = "";
			JSONObject res = reviewMappings(request, conn);
			text = res.toString();
			PrintWriter out = response.getWriter();
			out.print(text);
		} else if (action.equals("export")) {
			String text = "";
			text = exportMappings(request, conn);
			response.setContentType ("text/plain");
			response.setHeader ("Content-Disposition", "attachment; filename=\"ResultsMappings.xls\"");
			response.setContentLength(text.length());
			PrintWriter out = response.getWriter();
			out.print(text);
		} else {
			Logger.getLogger(ExplorerServlet.class).info("Unknown action: " + action);
		}
	}

	private JSONObject getVariables(HttpServletRequest request, Connection conn) {
		JSONArray rows = new JSONArray();
		JSONObject res = new JSONObject();
		try {
			String category = request.getParameter("category");
			String sqlQuery = "select variable, score from mappings where category = ? ";
			PreparedStatement stmt;
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, category);
			rows = Utils.executeSQL(stmt);
			stmt.close();
			res.put("variables", rows);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	private JSONObject createPhenotype(HttpServletRequest request, Connection conn) {
		JSONArray rows = new JSONArray();
		JSONObject res = new JSONObject();
		try {
			double score = Double.parseDouble(request.getParameter("score"));
			if(request.getParameter("description").trim().length() == 0) {
				score = 0;
			}
			int offset = Integer.parseInt(request.getParameter("iDisplayStart"));
			int limit = Integer.parseInt(request.getParameter("iDisplayLength"));
			int sEcho = Integer.parseInt(request.getParameter("sEcho"));
			String path = getServletContext().getRealPath("/index.html");
			int index = path.lastIndexOf(File.separator) + 1;
			path = path.substring(0, index);
			String tempTableName = null;
			HttpSession session = request.getSession(false);
			boolean newTemporaryTable = false;
			boolean buttonClicked = Boolean.parseBoolean(request.getParameter("buttonClicked")) &&
			(//session.getAttribute("newPhenotypeName") == null || 
					session.getAttribute("newPhenotypeDescription") == null ||
					session.getAttribute("studies") == null ||
					!request.getParameter("description").equalsIgnoreCase((String) session.getAttribute("newPhenotypeDescription")) ||
					!request.getParameter("studies").equalsIgnoreCase((String) session.getAttribute("studies")));
			if (buttonClicked && sEcho == 1) {
				tempTableName = (String) session.getAttribute("newPhenotypeTableName");
				if (tempTableName != null) {
					System.out.println("Dropping table " + tempTableName);
					Utils.dropTable(conn, tempTableName);
					session.removeAttribute("newPhenotypeTableName");
					session.removeAttribute("newPhenotypeTableCount");
					//session.removeAttribute("newPhenotypeName");
					session.removeAttribute("newPhenotypeDescription");
					session.removeAttribute("studies");
				}
				//System.out.println("getInnermostDelegate Connection: " + ((DelegatingConnection) conn).getInnermostDelegate());
				//System.out.println("getDelegate: " + ((DelegatingConnection) conn).getDelegate());
				//Connection innerConn = ((DelegatingConnection) conn).getInnermostDelegate();
				Connection innerConn = conn;
				//System.out.println("conn: " + conn);
				//System.out.println("innerConn: " + innerConn);
				JSONArray studies = new JSONArray(request.getParameter("studies"));
				String studiesArray[] = new String[studies.length()];
				for (int i=0; i < studies.length(); i++) {
					studiesArray[i] = studies.getString(i);
					//System.out.println("Study to send: " + studiesArray[i]);
				}
				tempTableName = new TestNewPhenotype().testNewPhenotype(innerConn, 
						session.getId(), 
						//request.getParameter("phenotype"), 
						"phenotype", 
						request.getParameter("description"), studiesArray);
				Logger.getLogger(ExplorerServlet.class).info("Temporary table: " + tempTableName);
				newTemporaryTable = true;
				if (tempTableName != null) {
					session.setAttribute("newPhenotypeTableName", tempTableName);
					//session.setAttribute("newPhenotypeName", request.getParameter("phenotype"));
					session.setAttribute("newPhenotypeDescription", request.getParameter("description"));
					session.setAttribute("studies", request.getParameter("studies"));
					session.setAttribute("studyFilter", "");
				}
			} else {
				tempTableName = (String) session.getAttribute("newPhenotypeTableName");
			}
			int count = 0;
			if (tempTableName != null) {
				String sqlQuery = null;
				String studyFilter = "";
				if (!newTemporaryTable) {
					JSONArray studies = new JSONArray(request.getParameter("studies"));
					String studiesArray[] = new String[studies.length()];
					for (int i=0; i < studies.length(); i++) {
						studiesArray[i] = studies.getString(i);
					}
					studyFilter = Utils.joinStudies(studiesArray, " OR ");
					//System.out.println("Study filter: " + studyFilter);
					session.setAttribute("studyFilter", studyFilter);
				} else {
					studyFilter = (String) session.getAttribute("studyFilter");
				}
				sqlQuery = "select count(*) from " + tempTableName + " where variable_match_score >= ?";
				if (studyFilter.length() > 0) {
					sqlQuery += " AND (" + studyFilter + ")";
				}
				Logger.getLogger(ExplorerServlet.class).info(sqlQuery);
				PreparedStatement stmt = conn.prepareStatement(sqlQuery);
				stmt.setDouble(1, score);
				rows = Utils.executeSQL(stmt);
				if (sEcho == 1) {
					count = (rows.getJSONArray(0)).getInt(0);
					session.setAttribute("newPhenotypeTableCount", count);
				} else {
					count = (Integer) session.getAttribute("newPhenotypeTableCount");
				}
				stmt.close();
				if (sEcho == 1) {
					sqlQuery = "select variable_match_score, '<a href=\"javascript:moreLikeThis(\\'' || description || '\\')\"><img alt=\"More...\" title=\"More like this\" src=\"resources/images/more.jpg\" /></a>', description, variable, study, '<input type=\"checkbox\"/>' from " + tempTableName + " where variable_match_score >= ? ";
					if (studyFilter.length() > 0) {
						sqlQuery += " AND (" + studyFilter + ") ";
					}
					sqlQuery += " order by variable_match_score desc offset ? limit ?";
				} else {
					sqlQuery = "select variable_match_score, '<a href=\"javascript:moreLikeThis(\\'' || description || '\\')\"><img alt=\"More...\" title=\"More like this\" src=\"resources/images/more.jpg\" /></a>', description, variable, study, '<input type=\"checkbox\"/>' from " + tempTableName + " where variable_match_score >= ? ";
					if (studyFilter.length() > 0) {
						sqlQuery += " AND (" + studyFilter + ") ";
					}
					sqlQuery += Utils.getOrderByClause(request, Utils.TEMPORARY_COLUMNS) + ((limit != -1) ? " offset ? limit ?" : "");
				}
				Logger.getLogger(ExplorerServlet.class).info(sqlQuery);
				stmt = conn.prepareStatement(sqlQuery);
				stmt.setDouble(1, score);
				if (limit != -1) {
					stmt.setInt(2, offset);
					stmt.setInt(3, limit);
				}
				rows = Utils.executeSQL(stmt);
				stmt.close();
			} else {
				rows = new JSONArray();
			}
			res.put("aaData", rows);
			res.put("iTotalRecords", count);
			res.put("iTotalDisplayRecords", count);
			res.put("sEcho", sEcho);
			System.out.println("echo: " + sEcho);
			if (tempTableName != null) {
				String sqlQuery = "select study, count(distinct variable) a from " + tempTableName  + " where variable_match_score >= ?" + " group by (study) order by a desc";
				System.out.println("sqlQuery: " + sqlQuery);
				PreparedStatement stmt = conn.prepareStatement(sqlQuery);
				stmt.setDouble(1, score);
				rows = Utils.executeSQL(stmt);
				res.put("studies", rows);
				String studiesQuery = "select regexp_replace(study, E'(.*)study_id=([^\">]*)(.*)', E'\\\\2') dbgap_study_id, count(distinct variable) matching_variables from " + tempTableName  + " where variable_match_score >= " + score + " group by (study)";
				session.setAttribute("studiesQuery", studiesQuery);
				String variablesQuery = "select description, regexp_replace(variable, E'([^>]*)>([^<]*)(.*)', E'\\\\2') variable_name, regexp_replace(variable, E'(.*)href=([^>]*)>(.*)', E'\\\\2') variable_url, regexp_replace(study, E'([^>]*)>([^<]*)(.*)', E'\\\\2') study_name, regexp_replace(study, E'(.*)href=([^>]*)>(.*)', E'\\\\2') study_url from " + tempTableName  + " where variable_match_score >= " + score + " order by variable_match_score desc";
				session.setAttribute("variablesQuery", variablesQuery);
			} else {
				res.put("studies", new JSONArray());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	private JSONObject addVariables(HttpServletRequest request, Connection conn) {
		JSONObject res = new JSONObject();
		try {
			HttpSession session = request.getSession(false);
			String category = request.getParameter("category");
			double score1 = Double.parseDouble(request.getParameter("score1"));
			double score2 = Double.parseDouble(request.getParameter("score2"));
			int offset = Integer.parseInt(request.getParameter("iDisplayStart"));
			int limit = Integer.parseInt(request.getParameter("iDisplayLength"));
			int sEcho = Integer.parseInt(request.getParameter("sEcho"));
			String tempTableName = (String) session.getAttribute("newPhenotypeTableName");
			String sqlQuery = "select count(*) from " + tempTableName + " where variable_match_score >= ? and variable_match_score <= ? and category = ?";
			Logger.getLogger(ExplorerServlet.class).info(sqlQuery);
			PreparedStatement stmt = conn.prepareStatement(sqlQuery);
			stmt.setDouble(1, score1);
			stmt.setDouble(2, score2);
			stmt.setString(3, category);
			JSONArray rows = Utils.executeSQL(stmt);
			int count = (rows.getJSONArray(0)).getInt(0);
			stmt.close();
			//sqlQuery = "select variable_match_score, description, variable, varset, study, visit, category, definer, category_match_score, '<input type=\"checkbox\"/>' from " + tempTableName + " where variable_match_score >= ? and variable_match_score <= ? and category = ? " + Utils.getOrderByClause(request, Utils.TEMPORARY_COLUMNS) + " offset ? limit ?";
			if (sEcho ==  1) {
				sqlQuery = "select variable_match_score, description, variable, varset, study, visit, category, definer, category_match_score, '<input type=\"checkbox\"/>' from " + tempTableName + " where variable_match_score >= ? and variable_match_score <= ? and category = ? " + ((limit != -1) ? " offset ? limit ?" : "");
			} else {
				sqlQuery = "select variable_match_score, description, variable, varset, study, visit, category, definer, category_match_score, '<input type=\"checkbox\"/>' from " + tempTableName + " where variable_match_score >= ? and variable_match_score <= ? and category = ? " + Utils.getOrderByClause(request, Utils.TEMPORARY_COLUMNS)  + ((limit != -1) ? " offset ? limit ?" : "");
			}
			Logger.getLogger(ExplorerServlet.class).info(sqlQuery);
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setDouble(1, score1);
			stmt.setDouble(2, score2);
			stmt.setString(3, category);
			if (limit != -1) {
				stmt.setInt(4, offset);
				stmt.setInt(5, limit);
			}
			rows = Utils.executeSQL(stmt);
			stmt.close();
			res.put("aaData", rows);
			res.put("iTotalRecords", count);
			res.put("iTotalDisplayRecords", count);
			res.put("sEcho", sEcho);
			//JSONArray arr = new JSONArray(request.getParameter("variables"));
			logger.info("Added the variable(s): to the category \"" + category + "\"");
			//res.put("status", "The variables were added successfully.");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	private JSONObject addMappings(HttpServletRequest request, Connection conn) {
		JSONObject res = new JSONObject();
		try {
			HttpSession session = request.getSession(false);
			String user = (String) session.getAttribute("user");
			String shoppingCart = (String) session.getAttribute("shoppingCart");
			String sqlQuery = null;
			PreparedStatement stmt = null;
			if (shoppingCart == null) {
				try {
					shoppingCart = user + "_shoppingCart";
					sqlQuery = "create table " + shoppingCart + " ( like " + 
					session.getAttribute("newPhenotypeTableName") + " )";
					Logger.getLogger(ExplorerServlet.class).info(sqlQuery);
					stmt = conn.prepareStatement(sqlQuery);
					stmt.execute();
					stmt.close();
				} catch (SQLException e) {
					if (e.getErrorCode() != 0) {
						e.printStackTrace();
					}
				}
				session.setAttribute("shoppingCart", shoppingCart);
			}
			String rows = request.getParameter("rows");
			JSONArray json = new JSONArray(rows);
			//logger.info("After: " + json.toString());
			conn.setAutoCommit(false);
			sqlQuery = Utils.getSQLaddSelectedMappings(shoppingCart);
			Logger.getLogger(ExplorerServlet.class).info(sqlQuery);
			stmt = conn.prepareStatement(sqlQuery);
			int rowCount = Utils.executeShoppingCartUpdate(stmt, json);
			conn.commit();
			Logger.getLogger(ExplorerServlet.class).info("Inserted " + rowCount + " rows.");
			stmt.close();
			conn.setAutoCommit(true);
			res.put("status", "The mappings were added successfully.");
			if (request.getParameter("fromCategory") != null) {
				res.put("fromCategory", true);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	private JSONObject clearShoppingCart(HttpServletRequest request, Connection conn) {
		JSONObject res = new JSONObject();
		try {
			HttpSession session = request.getSession(false);
			String user = (String) session.getAttribute("user");
			String shoppingCart = (String) session.getAttribute("shoppingCart");
			Utils.deleteTable(conn, shoppingCart);
			res.put("status", "The shopping cart was cleared successfully.");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	private JSONObject reviewMappings(HttpServletRequest request, Connection conn) {
		JSONObject res = new JSONObject();
		try {
			HttpSession session = request.getSession(false);
			String user = (String) session.getAttribute("user");
			String shoppingCart = (String) session.getAttribute("shoppingCart");
			String sqlQuery = null;
			PreparedStatement stmt = null;
			if (shoppingCart == null) {
				try {
					shoppingCart = user + "_shoppingCart";
					sqlQuery = "create table " + shoppingCart + " ( like " + 
					session.getAttribute("newPhenotypeTableName") + " )";
					Logger.getLogger(ExplorerServlet.class).info(sqlQuery);
					stmt = conn.prepareStatement(sqlQuery);
					stmt.execute();
					stmt.close();
				} catch (SQLException e) {
					if (e.getErrorCode() != 0) {
						e.printStackTrace();
					}
				}
				session.setAttribute("shoppingCart", shoppingCart);
			}
			int sEcho = Integer.parseInt(request.getParameter("sEcho"));
			int offset = Integer.parseInt(request.getParameter("iDisplayStart"));
			int limit = Integer.parseInt(request.getParameter("iDisplayLength"));
			sqlQuery = "select count(*) from " + shoppingCart ;
			Logger.getLogger(ExplorerServlet.class).info(sqlQuery);
			stmt = conn.prepareStatement(sqlQuery);
			JSONArray rows = Utils.executeSQL(stmt);
			int count = (rows.getJSONArray(0)).getInt(0);
			stmt.close();

			if (sEcho == 1) {
				sqlQuery = Utils.getSQLshoppingCart(shoppingCart) + ((limit != -1) ? " offset ? limit ?" : "");
			} else {
				sqlQuery = Utils.getSQLshoppingCart(shoppingCart) + " " + Utils.getOrderByClause(request, Utils.TEMPORARY_COLUMNS) + ((limit != -1) ? " offset ? limit ?" : "");
			}
			Logger.getLogger(ExplorerServlet.class).info(sqlQuery);
			stmt = conn.prepareStatement(sqlQuery);
			if (limit != -1) {
				stmt.setInt(1, offset);
				stmt.setInt(2, limit);
			}
			rows = Utils.executeSQL(stmt);
			stmt.close();
			res.put("aaData", rows);
			res.put("iTotalRecords", count);
			res.put("iTotalDisplayRecords", count);
			res.put("sEcho", sEcho);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	private String exportMappings(HttpServletRequest request, Connection conn) {
		String phenotypes = "";
		String rows = request.getParameter("rows");
		JSONArray json = null;
		try {
			json = new JSONArray(rows);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		phenotypes = Utils.toText(Utils.TEMPORARY_COLUMNS, json);
		return phenotypes;
	}

	private JSONObject testSaveMappings(HttpServletRequest request, Connection conn) {
		JSONObject res = new JSONObject();
		try {
			HttpSession session = request.getSession(false);
			String rows = request.getParameter("rows");
			JSONArray json = new JSONArray(rows);
			String category = request.getParameter("category");
			String description = request.getParameter("description");
			String user = (String) session.getAttribute("user");
			//logger.info("Before: " + json.toString());
			for (int i=0; i < json.length(); i++) {
				JSONArray row = json.getJSONArray(i);
				for (int j=Utils.TEMPORARY_COLUMNS.length-1; j>=0; j--) {
					if (!TEMPORARY_MAPPINGS_COLUMNS.contains(Utils.TEMPORARY_COLUMNS[j])) {
						row.remove(j);
					}
				}
				row.put(category);
				row.put(user);
			}
			//logger.info("After: " + json.toString());
			conn.setAutoCommit(false);
			String sqlQuery = Utils.getSQLsaveSelectedMappings();
			Logger.getLogger(ExplorerServlet.class).info(sqlQuery);
			PreparedStatement stmt = conn.prepareStatement(sqlQuery);
			int rowCount = Utils.executeMappingsUpdate(stmt, json);
			conn.commit();
			Logger.getLogger(ExplorerServlet.class).info("Inserted " + rowCount + " rows.");
			stmt.close();
			conn.setAutoCommit(true);
			sqlQuery = Utils.getSQLsaveCategory();
			Logger.getLogger(ExplorerServlet.class).info(sqlQuery);
			stmt = conn.prepareStatement(sqlQuery);
			json = new JSONArray();
			json.put(category);
			json.put(user);
			json.put(description);
			rowCount = Utils.executeCategoryUpdate(stmt, json);
			Logger.getLogger(ExplorerServlet.class).info("Inserted " + rowCount + " rows.");
			Logger.getLogger(ExplorerServlet.class).info("Saved the mappings(s) to the category \"" + category + "\"");
			res.put("status", "The mappings were saved successfully.");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * bookmark a query
	 * 
	 * @param request
	 *            the servlet request
	 * @param conn
	 *            the DB connection
	 * @return the bookmark
	 */
	private JSONObject bookmarkQuery(HttpServletRequest request, Connection conn) throws ServletException {
		JSONObject res = new JSONObject();
		HttpSession session = request.getSession(false);
		try {
			String bookmark = request.getParameter("bookmark");
			JSONObject json = new JSONObject(request.getParameter("sql"));
			String sqlQuery = "INSERT INTO dbgap_bookmarks(name, description, userid) VALUES(?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, bookmark);
			stmt.setString(2, json.toString());
			stmt.setString(3, (String) session.getAttribute("user"));
			stmt.executeUpdate();
			stmt.close();
			res.put("bookmark", bookmark);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ServletException(e.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return res;

	}

	/**
	 * get the bookmarks from the DB
	 * 
	 * @param request
	 *            the servlet request
	 * @param conn
	 *            the DB connection
	 */
	private void getBookmarks(HttpServletRequest request, Connection conn) {
		HttpSession session = request.getSession(false);
		try {
			String sqlQuery = "SELECT name, description, created from dbgap_bookmarks where userid = ?";
			PreparedStatement stmt;
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, (String) session.getAttribute("user"));
			JSONArray rows = Utils.executeSQL(stmt);
			stmt.close();
			ArrayList<HashMap<String,String>> bookmarks = new ArrayList<HashMap<String,String>>();
			for (int i=0; i < rows.length(); i++) {
				JSONArray row = rows.getJSONArray(i);
				HashMap<String,String> item = new HashMap<String,String>();
				item.put("name", row.getString(0));
				item.put("description", Utils.decodeExploreQueryHTML(new JSONObject(row.getString(1))));
				item.put("created", row.getString(2));
				bookmarks.add(item);
			}
			request.setAttribute("bookmarks", bookmarks);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * set the query from a bookmark
	 * 
	 * @param request
	 *            the servlet request
	 * @param conn
	 *            the DB connection
	 */
	private void setBookmarkQuery(HttpServletRequest request, Connection conn) throws ServletException {
		HttpSession session = request.getSession(false);
		try {
			String bookmark = request.getParameter("qid");
			String sqlQuery = "SELECT description FROM dbgap_bookmarks WHERE userid = ? AND name = ?";
			PreparedStatement stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, (String) session.getAttribute("user"));
			stmt.setString(2, bookmark);
			JSONArray rows = Utils.executeSQL(stmt);
			stmt.close();
			String description = ((JSONArray) rows.get(0)).getString(0);
			logger.info("Description " + description);
			JSONObject json = new JSONObject(((JSONArray) rows.get(0)).getString(0));
			session.setAttribute("query", json);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ServletException(e.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * get the Excel string of the results of an SQL Query 
	 * 
	 * @param request
	 *            the servlet request
	 * @param conn
	 *            the DB connection
	 * @return the CSV string to be saved in a file
	 */
	private String saveStudies(HttpServletRequest request, Connection conn) {
		String studiesQuery = (String) request.getSession(false).getAttribute("studiesQuery");
		String columns[] = { "dbgap_study_name",
				//"short_name",
				"dbgap_study_id",
				"matching_variables",
				"study_url",
				"race",
				"sex",
				"participants",
				"min_age",
				"max_age",
				//"sample_size",
				"study_type",
				"genetic_type",
				"diseases",
				"platform",
				"description"
		};
		String selectColumns[] = { Utils.STUDIES_TABLE + ".dbgap_study_name",
				//Utils.STUDIES_TABLE + ".short_name",
				Utils.STUDIES_TABLE + ".dbgap_study_id",
				"T.matching_variables",
				Utils.STUDIES_TABLE + ".study_url",
				Utils.STUDIES_TABLE + ".race",
				Utils.STUDIES_TABLE + ".sex",
				Utils.STUDIES_TABLE + ".participants",
				Utils.STUDIES_TABLE + ".min_age",
				Utils.STUDIES_TABLE + ".max_age",
				//Utils.STUDIES_TABLE + ".sample_size",
				Utils.STUDIES_TABLE + ".study_type",
				Utils.STUDIES_TABLE + ".genetic_type",
				Utils.STUDIES_TABLE + ".diseases",
				Utils.STUDIES_TABLE + ".platform",
				Utils.STUDIES_TABLE + ".description "
		};
		String sqlQuery = "select " + Utils.join(selectColumns, ", ") + " from " + Utils.STUDIES_TABLE + " join (" + studiesQuery + ") T on " + Utils.STUDIES_TABLE + ".dbgap_study_id = T.dbgap_study_id order by T.matching_variables desc";
		System.out.println("SqlQuery:"+ sqlQuery);
		
		String phenotypes = "";
		try {
			// get the query results
			logger.info(sqlQuery);
			PreparedStatement stmt = conn.prepareStatement(sqlQuery);
			JSONArray rows = Utils.executeSQL(stmt);
			stmt.close();
			phenotypes = Utils.toText(columns, rows);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return phenotypes;
	}

	/**
	 * get the Excel string of the results of an SQL Query 
	 * 
	 * @param request
	 *            the servlet request
	 * @param conn
	 *            the DB connection
	 * @return the CSV string to be saved in a file
	 */
	private String saveVariables(HttpServletRequest request, Connection conn) {
		String sqlQuery = (String) request.getSession(false).getAttribute("variablesQuery");
		String columns[] = { "description",
				"variable_name",
				"variable_url",
				"study_name",
				"study_url"
		};
		String phenotypes = "";
		try {
			// get the query results
			logger.info(sqlQuery);
			PreparedStatement stmt = conn.prepareStatement(sqlQuery);
			JSONArray rows = Utils.executeSQL(stmt);
			stmt.close();
			phenotypes = Utils.toText(columns, rows);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return phenotypes;
	}

}
