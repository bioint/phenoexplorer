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
 * Servlet implementation class DefinerServlet
 */
@WebServlet(description = "Servlet for defining new categories", urlPatterns = { "/define" })
public class DefinerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static private Logger logger = Logger.getLogger("AppLogging");       
	public static final ArrayList<String> TEMPORARY_MAPPINGS_COLUMNS = new ArrayList<String>(Arrays.asList("variable_match_score", "variable", "varset", "study"));

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DefinerServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		RequestDispatcher dispatcher = request.getRequestDispatcher("/resources/Definer.jsp");
		dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		Connection conn = (Connection) session.getAttribute("conn");
		String action = request.getParameter("action");
		if (action.equals("create")) {
			String phenotype = request.getParameter("phenotype");
			String description = request.getParameter("description");
			double score1 = Double.parseDouble(request.getParameter("score1"));
			double score2 = Double.parseDouble(request.getParameter("score2"));
			Logger.getLogger(DefinerServlet.class).info("Create phenotype \"" + phenotype + "\" with description: \"" + description + "\", score: [" + score1 + ", " + score2 + "]");
			String text = "";
			JSONObject res = createPhenotype(request, conn);
			text = res.toString();
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
			Logger.getLogger(DefinerServlet.class).info("Unknown action: " + action);
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
			double score1 = Double.parseDouble(request.getParameter("score1"));
			double score2 = Double.parseDouble(request.getParameter("score2"));
			JSONObject params = new JSONObject();
			params.put("connection", conn);
			params.put("name", request.getParameter("phenotype"));
			params.put("description", request.getParameter("description"));
			params.put("minScore", score1);
			params.put("maxScore", score2);
			int offset = Integer.parseInt(request.getParameter("iDisplayStart"));
			int limit = Integer.parseInt(request.getParameter("iDisplayLength"));
			int sEcho = Integer.parseInt(request.getParameter("sEcho"));
			String path = getServletContext().getRealPath("/index.html");
			int index = path.lastIndexOf(File.separator) + 1;
			path = path.substring(0, index);
			String tempTableName = null;
			HttpSession session = request.getSession(false);
			if (conn != null) {
				boolean buttonClicked = Boolean.parseBoolean(request.getParameter("buttonClicked")) &&
				(session.getAttribute("newPhenotypeName") == null || 
						session.getAttribute("newPhenotypeDescription") == null ||
						!request.getParameter("phenotype").equalsIgnoreCase((String) session.getAttribute("newPhenotypeName")) ||
						!request.getParameter("description").equalsIgnoreCase((String) session.getAttribute("newPhenotypeDescription")));
				if (buttonClicked && sEcho == 1) {
					tempTableName = (String) session.getAttribute("newPhenotypeTableName");
					if (tempTableName != null) {
						Utils.dropTable(conn, tempTableName);
						session.removeAttribute("newPhenotypeTableName");
						session.removeAttribute("newPhenotypeTableCount");
						session.removeAttribute("newPhenotypeName");
						session.removeAttribute("newPhenotypeDescription");
					}
					//System.out.println("getInnermostDelegate Connection: " + ((DelegatingConnection) conn).getInnermostDelegate());
					//System.out.println("getDelegate: " + ((DelegatingConnection) conn).getDelegate());
					//Connection innerConn = ((DelegatingConnection) conn).getInnermostDelegate();
					Connection innerConn = conn;
					//System.out.println("conn: " + conn);
					//System.out.println("innerConn: " + innerConn);
					tempTableName = new TestNewPhenotype().testNewPhenotype(innerConn, 
							session.getId(), 
							request.getParameter("phenotype"), 
							request.getParameter("description"), new String[0]);
					Logger.getLogger(DefinerServlet.class).info("Temporary table: " + tempTableName);
					if (tempTableName != null) {
						session.setAttribute("newPhenotypeTableName", tempTableName);
						session.setAttribute("newPhenotypeName", request.getParameter("phenotype"));
						session.setAttribute("newPhenotypeDescription", request.getParameter("description"));
					}
				} else {
					tempTableName = (String) session.getAttribute("newPhenotypeTableName");
				}
			} else {
				tempTableName = "";
			}
			int count = 0;
			if (tempTableName != null) {
				String sqlQuery = null;
				if (tempTableName.length() > 0) {
					sqlQuery = "select count(*) from " + tempTableName + " where variable_match_score >= ? and variable_match_score <= ?";
				} else {
					sqlQuery = "select count(*) from (select category, score, variable, description, score, study, varset, visit, definer from mappings join variables using (study, varset, variable) where score >= ? and score <= ?) T";
				}
				Logger.getLogger(DefinerServlet.class).info(sqlQuery);
				PreparedStatement stmt = conn.prepareStatement(sqlQuery);
				stmt.setDouble(1, score1);
				stmt.setDouble(2, score2);
				rows = Utils.executeSQL(stmt);
				if (sEcho == 1) {
					count = (rows.getJSONArray(0)).getInt(0);
					session.setAttribute("newPhenotypeTableCount", count);
				} else {
					count = (Integer) session.getAttribute("newPhenotypeTableCount");
				}
				stmt.close();
				if (tempTableName.length() > 0) {
					if (sEcho == 1) {
						sqlQuery = "select variable_match_score, description, variable, varset, study, visit, category, definer, category_match_score, '<input type=\"checkbox\"/>' from " + tempTableName + " where variable_match_score >= ? and variable_match_score <= ? offset ? limit ?";
					} else {
						sqlQuery = "select variable_match_score, description, variable, varset, study, visit, category, definer, category_match_score, '<input type=\"checkbox\"/>' from " + tempTableName + " where variable_match_score >= ? and variable_match_score <= ? " + Utils.getOrderByClause(request, Utils.TEMPORARY_COLUMNS) + ((limit != -1) ? " offset ? limit ?" : "");
					}
				} else {
					if (sEcho == 1) {
						sqlQuery = "select score category_match_score, description, variable, varset, study, visit, category, definer, score variable_match_score, '<input type=\"checkbox\"/>' from mappings join variables using (study, varset, variable) where score >= ? and score <= ? " + "" +
						"order by variable_match_score desc, category, description, variable, varset, study, visit, definer, variable_match_score" + 
						" offset ? limit ?";
					} else {
						sqlQuery = "select score variable_match_score, description, variable, varset, study, visit, category, definer, score variable_match_score, '<input type=\"checkbox\"/>' from mappings join variables using (study, varset, variable) where score >= ? and score <= ? " + Utils.getOrderByClause(request, Utils.TEMPORARY_COLUMNS) + " offset ? limit ?";
					}
				}
				Logger.getLogger(DefinerServlet.class).info(sqlQuery);
				stmt = conn.prepareStatement(sqlQuery);
				stmt.setDouble(1, score1);
				stmt.setDouble(2, score2);
				if (limit != -1) {
					stmt.setInt(3, offset);
					stmt.setInt(4, limit);
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
			Logger.getLogger(DefinerServlet.class).info(sqlQuery);
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
			Logger.getLogger(DefinerServlet.class).info(sqlQuery);
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
					Logger.getLogger(DefinerServlet.class).info(sqlQuery);
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
			Logger.getLogger(DefinerServlet.class).info(sqlQuery);
			stmt = conn.prepareStatement(sqlQuery);
			int rowCount = Utils.executeShoppingCartUpdate(stmt, json);
			conn.commit();
			Logger.getLogger(DefinerServlet.class).info("Inserted " + rowCount + " rows.");
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
					Logger.getLogger(DefinerServlet.class).info(sqlQuery);
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
			Logger.getLogger(DefinerServlet.class).info(sqlQuery);
			stmt = conn.prepareStatement(sqlQuery);
			JSONArray rows = Utils.executeSQL(stmt);
			int count = (rows.getJSONArray(0)).getInt(0);
			stmt.close();

			if (sEcho == 1) {
				sqlQuery = Utils.getSQLshoppingCart(shoppingCart) + ((limit != -1) ? " offset ? limit ?" : "");
			} else {
				sqlQuery = Utils.getSQLshoppingCart(shoppingCart) + " " + Utils.getOrderByClause(request, Utils.TEMPORARY_COLUMNS) + ((limit != -1) ? " offset ? limit ?" : "");
			}
			Logger.getLogger(DefinerServlet.class).info(sqlQuery);
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
			Logger.getLogger(DefinerServlet.class).info(sqlQuery);
			PreparedStatement stmt = conn.prepareStatement(sqlQuery);
			int rowCount = Utils.executeMappingsUpdate(stmt, json);
			conn.commit();
			Logger.getLogger(DefinerServlet.class).info("Inserted " + rowCount + " rows.");
			stmt.close();
			conn.setAutoCommit(true);
			sqlQuery = Utils.getSQLsaveCategory();
			Logger.getLogger(DefinerServlet.class).info(sqlQuery);
			stmt = conn.prepareStatement(sqlQuery);
			json = new JSONArray();
			json.put(category);
			json.put(user);
			json.put(description);
			rowCount = Utils.executeCategoryUpdate(stmt, json);
			Logger.getLogger(DefinerServlet.class).info("Inserted " + rowCount + " rows.");
			Logger.getLogger(DefinerServlet.class).info("Saved the mappings(s) to the category \"" + category + "\"");
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

}
