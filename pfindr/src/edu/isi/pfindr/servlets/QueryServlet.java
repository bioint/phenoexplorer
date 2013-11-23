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
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.isi.pfindr.helper.Utils;

/**
 * Servlet implementation class Query
 * 
 * @author Serban Voinea
 */

@WebServlet(description = "Query Tool Servlet", urlPatterns = { "/query" })
public class QueryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static private Logger logger = Logger.getLogger(QueryServlet.class);       

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public QueryServlet() {
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
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
		String action = request.getParameter("action");
		boolean mustchange = (Boolean) session.getAttribute("mustchange");
		if (mustchange) {
			RequestDispatcher dispatcher = request.getRequestDispatcher("/resources/PasswordChange.jsp");
			dispatcher.forward(request, response);
		} else if (action != null && action.equals("history")) {
			ArrayList<JSONObject> history = (ArrayList<JSONObject>) session.getAttribute("history");
			ArrayList<String> rows = new ArrayList<String>();
			for (int i=0; i < history.size(); i++) {
				rows.add(Utils.decodeQueryHTML(history.get(i)));
			}
			request.setAttribute("history", rows);
			RequestDispatcher dispatcher = request.getRequestDispatcher("/resources/History.jsp");
			dispatcher.forward(request, response);
		} else if (action != null && action.equals("getQueries")) {
			getBookmarks(request, (Connection) session.getAttribute("conn"));
			RequestDispatcher dispatcher = request.getRequestDispatcher("/resources/Bookmarks.jsp");
			dispatcher.forward(request, response);
		} else if (action != null && action.equals("getBookmark")) {
			getBookmarks(request, (Connection) session.getAttribute("conn"));
			// set the bookmark query in the session
			setBookmarkQuery(request, (Connection) session.getAttribute("conn"));
			response.sendRedirect("/pfindr/query");
		} else if (action != null && action.equals("getHistory")) {
			// set the history query in the session
			int bookmark = Integer.parseInt(request.getParameter("qid"));
			session.setAttribute("query", ((ArrayList<JSONObject>) session.getAttribute("history")).get(bookmark));
			response.sendRedirect("/pfindr/query");
		} else if (action != null && action.equals("homepage")) {
			// go to the HOME Page
			RequestDispatcher dispatcher = request.getRequestDispatcher("/resources/PhenoExplorer.jsp");
			dispatcher.forward(request, response);
		} else {
			RequestDispatcher dispatcher = request.getRequestDispatcher("/resources/Query.jsp");
			dispatcher.forward(request, response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
		Connection conn = (Connection) session.getAttribute("conn");
		response.setContentType("application/json");
		String action = request.getParameter("predicate");
		String text = "";

		if (action.equals("apply_filter_study_metadata")) {
			JSONObject res = applyFilterStudyMetadata(request, conn);
			text = res.toString();
		}
		else if (action.equals("study_metadata")) {
			JSONObject res = studyMetadata(request, conn);
			text = res.toString();
		}
		else if (action.equals("bookmark")) {
			JSONObject res = bookmarkQuery(request, conn);
			text = res.toString();
		} else if (action.equals("save")) {
			// save the query results to a file
			text = save(request, conn);
			//response.setContentType ("application/xml");
			response.setContentType ("text/plain");
			response.setHeader ("Content-Disposition", "attachment; filename=\"Results.xls\"");
			response.setContentLength(text.length());
		} else if (action.equals("export")) {
			// export the query to a file
			text = export(request, conn);
			response.setContentType ("text/plain");
			response.setHeader ("Content-Disposition", "attachment; filename=\"Query.txt\"");
			response.setContentLength(text.length());
		} else if (action.equals("import")) {
			// export the query to a file
			importQuery(request, conn);
			response.sendRedirect("/pfindr/query");
			return;
		} else if (action.equals("saveSelectedResults") || 
				action.equals("markAsCorrect") || 
				action.equals("markAsIncorrect")) {
			// save the query results to a file
			text = saveSelectedResults(request, conn);
			if (request.getParameter("timestamp") != null || action.equals("saveSelectedResults")) {
				response.setContentType ("text/plain");
				response.setHeader ("Content-Disposition", "attachment; filename=\"SelectedResults.xls\"");
				response.setContentLength(text.length());
			}
		} else if (action.equals("keyword")) {
			// get hints for a keyword
			JSONArray rows = keywords(request, conn);
			text = rows.toString();
		} else if (action.equals("select")) {
			// get query results
			try {
				JSONObject obj = new JSONObject();
				JSONObject queryDescription = (JSONObject) session.getAttribute("query");
				if (queryDescription == null) {
					String sql = request.getParameter("sql");
					JSONObject json = new JSONObject(sql);
					request.setAttribute("sql", json);
					obj = select(request, conn);
				} else {
					session.removeAttribute("query");
					obj.put("queryDescription", queryDescription);
				}
				text = obj.toString();
				//logger.info(text);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (action.equals("selectFilters")) {
			// get query results
			try {
				JSONObject obj = new JSONObject();
				String sql = request.getParameter("sql");
				JSONObject json = new JSONObject(sql);
				request.setAttribute("sql", json);
				obj = selectFilters(request, conn);
				text = obj.toString();
				//logger.info(text);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (action.equals("flat")) {
			// get query results
			JSONObject res = dataTables(request, conn);
			text = res.toString();
		}
		else {
			logger.error("Invalid action: \"" + action + "\".");
		}
		PrintWriter out = response.getWriter();
		out.print(text);
	}

	/**
	 * get the array of hints for a given keyword
	 * 
	 * @param request
	 *            the servlet request
	 * @param conn
	 *            the DB connection
	 * @return the array with hints
	 */
	private JSONArray keywords(HttpServletRequest request, Connection conn) {
		JSONArray rows = new JSONArray();
		try {
			String value = request.getParameter("value");
			if (value.trim().length() != 0) {
				String column = request.getParameter("column");
				String sqlQuery = Utils.getKeywordSQL(column);
				logger.info(sqlQuery + ", [" + value + "]");
				PreparedStatement stmt = conn.prepareStatement(sqlQuery);
				stmt.setString(1, value);
				rows = Utils.executeSQL(stmt);
				stmt.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rows;
	}

	/**
	 * get the query results containing:
	 * 	- the phenotypes
	 * 	- the total number of phenotypes
	 * 	- the range values (available and restricted) for the studies and the categories
	 * 
	 * @param request
	 *            the servlet request
	 * @param conn
	 *            the DB connection
	 * @return the query results
	 */
	@SuppressWarnings("unchecked")
	private JSONObject select(HttpServletRequest request, Connection conn) {
		JSONObject res = new JSONObject();
		try {
			// log the query
			String studyMetadata = request.getParameter("study_details");
			JSONObject jsonStudy = null;
			if (studyMetadata != null) {
				jsonStudy = new JSONObject(studyMetadata);
			}
			JSONObject jsonLog = (JSONObject) request.getAttribute("sql");
			String keys[] = {"where"};
			jsonLog = new JSONObject(jsonLog, keys);
			jsonLog.put("template", request.getParameter("selectedTemplate"));
			Utils.logQuery(jsonLog);
			
			// get the phenotypes
			JSONObject json = (JSONObject) request.getAttribute("sql");
			JSONObject history = new JSONObject(request.getParameter("sql"));
			history.put("template", request.getParameter("selectedTemplate"));
			//logger.info("History: " + history);
			Utils.updateHistory((ArrayList<JSONObject>) request.getSession().getAttribute("history"), history);
			String sqlQuery = Utils.getPhenotypesSQL(json);
			logger.info(sqlQuery);
			PreparedStatement stmt = conn.prepareStatement(sqlQuery);
			Utils.loadValues(stmt, json, "", false);
			String names[] = {"limit", "offset", "where"};
			logger.info((new JSONObject(json, names).toString()));
			JSONArray rows = Utils.executeSQL(stmt);
			stmt.close();
			res.put("phenotypes", rows);
			
			sqlQuery = "select upper(variable), url from dbgap where upper(variable) in (select upper(variable) from (" + sqlQuery + ") TTTT)";
			//logger.info(sqlQuery);
			stmt = conn.prepareStatement(sqlQuery);
			Utils.loadValues(stmt, json, "", false);
			rows = Utils.executeSQL(stmt);
			stmt.close();
			res.put("dbgap", rows);
			//logger.info(rows.toString());

			// get the phenotypes count
			sqlQuery = Utils.getPhenotypesCount(json);
			logger.info(sqlQuery);
			stmt = conn.prepareStatement(sqlQuery);
			Utils.loadValues(stmt, json, "", true);
			rows = Utils.executeSQL(stmt);
			stmt.close();
			res.put("count", rows.getJSONArray(0).getInt(0));

			JSONObject range = new JSONObject();
			res.put("range", range);
			JSONArray columns = new JSONArray(request.getParameter("columns"));
			boolean hasPredicate = false;
			if (json.has(Utils.WHERE)) {
				JSONObject wherePredicate = json.getJSONObject(Utils.WHERE);
				if (wherePredicate.length() > 1) {
					hasPredicate = true;
				}
			}
			for (int i=0; i < columns.length(); i++) {
				String column = columns.getString(i);
				JSONObject values = new JSONObject();
				range.put(column, values);

				// set available values
				sqlQuery = Utils.getPhenotypesRange(json, column, column, jsonStudy);
				logger.info(sqlQuery);
				stmt = conn.prepareStatement(sqlQuery);
				int position = 1;
				if (!column.equals("study") || hasPredicate) {
					position = Utils.loadValues(stmt, json, column, true);
				}
					
				if (column.equals("study")) {
					Utils.loadStudiesMetadataValues(stmt, jsonStudy, position);
				}
				rows = Utils.executeSQL(stmt);
				stmt.close();
				
				JSONObject flyOver = new JSONObject();
				JSONObject available = new JSONObject();
				if (column.equals("study")) {
					for (int j=0; j < rows.length(); j++) {
						JSONArray row = rows.getJSONArray(j);
						if (!row.isNull(12)) {
							flyOver.put(row.getString(13), row.getString(12));
						}
						row.remove(13);
						row.remove(12);
					}
					available.put("studies", rows);
					available.put("flyover", flyOver);
					values.put("available", available);
				} else if (column.equals("category")) {
					for (int j=0; j < rows.length(); j++) {
						JSONArray row = rows.getJSONArray(j);
						if (!row.isNull(1)) {
							flyOver.put(row.getString(0), row.getString(1));
						}
						row.remove(1);
					}
					available.put("categories", rows);
					available.put("flyover", flyOver);
					values.put("available", available);
				} else {
					values.put("available", rows);
				}
				
				/*
				// set restricted values
				if (json.has(Utils.WHERE)) {
					sqlQuery = Utils.getPhenotypesRange(json, column, "");
					logger.info(sqlQuery);
					stmt = conn.prepareStatement(sqlQuery);
					Utils.loadValues(stmt, json, "", true);
					rows = Utils.executeSQL(stmt);
					stmt.close();
				}
				values.put("restricted", rows);
				*/
			}
			//System.out.println(res);
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
	 * get the query results containing:
	 * 	- the phenotypes
	 * 	- the total number of phenotypes
	 * 	- the range values (available and restricted) for the studies and the categories
	 * 
	 * @param request
	 *            the servlet request
	 * @param conn
	 *            the DB connection
	 * @return the query results
	 */
	private JSONObject selectFilters(HttpServletRequest request, Connection conn) {
		JSONObject res = new JSONObject();
		try {
			// get the phenotypes
			JSONObject json = (JSONObject) request.getAttribute("sql");
			String sqlQuery = Utils.getPhenotypesSQL(json);
			PreparedStatement stmt = null;
			JSONArray rows = null;
			JSONObject range = new JSONObject();
			res.put("range", range);
			JSONArray columns = new JSONArray(request.getParameter("columns"));
			for (int i=0; i < columns.length(); i++) {
				String column = columns.getString(i);
				JSONObject values = new JSONObject();
				range.put(column, values);

				// set available values
				sqlQuery = Utils.getPhenotypesRange(json, column, column, null);
				logger.info(sqlQuery);
				stmt = conn.prepareStatement(sqlQuery);
				Utils.loadValues(stmt, json, column, true);
				rows = Utils.executeSQL(stmt);
				stmt.close();
				values.put("available", rows);

				// set restricted values
				if (json.has(Utils.WHERE)) {
					sqlQuery = Utils.getPhenotypesRange(json, column, "", null);
					logger.info(sqlQuery);
					stmt = conn.prepareStatement(sqlQuery);
					Utils.loadValues(stmt, json, "", true);
					rows = Utils.executeSQL(stmt);
					stmt.close();
				}
				values.put("restricted", rows);
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

	/**
	 * get the XML string of the results of an SQL Query 
	 * 
	 * @param request
	 *            the servlet request
	 * @param conn
	 *            the DB connection
	 * @return the XML string to be saved in a file
	 */
	@SuppressWarnings("unused")
	private String saveXML(HttpServletRequest request, Connection conn) {
		String sql = request.getParameter("sql");
		JSONObject json;
		String phenotypes = "";
		try {
			// get the query results
			json = new JSONObject(sql);
			String sqlQuery = Utils.getPhenotypesSQL(json);
			logger.info(sqlQuery);
			PreparedStatement stmt = conn.prepareStatement(sqlQuery);
			Utils.loadValues(stmt, json, "", false);
			JSONArray group = new JSONArray(request.getParameter("template"));
			JSONArray rows = Utils.executeSQL(stmt);
			stmt.close();

			// create the document builder
			DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element root = doc.createElement("Phenotypes");
			doc.appendChild(root);

			// append the query results to the document
			JSONObject res = Utils.toJSONObject(group, rows);
			Utils.toXML(doc, root, res);

			// transform the document to an XML string
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			phenotypes = sw.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
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
	private String save(HttpServletRequest request, Connection conn) {
		String sql = request.getParameter("sql");
		JSONObject json;
		String phenotypes = "";
		try {
			// get the query results
			json = new JSONObject(sql);
			json.remove(Utils.LIMIT);
			json.remove(Utils.OFFSET);
			String sqlQuery = Utils.getPhenotypesSQL(json);
			logger.info(sqlQuery);
			PreparedStatement stmt = conn.prepareStatement(sqlQuery);
			Utils.loadValues(stmt, json, "", false);
			String names[] = {"where"};
			logger.info((new JSONObject(json, names).toString()));
			JSONArray group = new JSONArray(request.getParameter("template"));
			JSONArray rows = Utils.executeSQL(stmt);
			stmt.close();
			phenotypes = Utils.toText(group, rows);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return phenotypes;
	}

	/**
	 * export the Query URL
	 * 
	 * @param request
	 *            the servlet request
	 * @param conn
	 *            the DB connection
	 * @return the Query URL to be saved in a file
	 */
	private String export(HttpServletRequest request, Connection conn) {
		String sql = request.getParameter("sql");
		JSONObject json;
		String query = "";
		try {
			// get the query results
			json = new JSONObject(sql);
			json.put("template", request.getParameter("template"));
			logger.info("Query URL: " + json.toString());
			query = json.toString() + Utils.decodeQuery(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return query;
	}

	/**
	 * import the Query URL
	 * 
	 * @param request
	 *            the servlet request
	 * @param conn
	 *            the DB connection
	 */
	@SuppressWarnings("unchecked")
	private void importQuery(HttpServletRequest request, Connection conn) {
		FileItemFactory factory = new DiskFileItemFactory(Integer.MAX_VALUE, null);
		ServletFileUpload upload = new ServletFileUpload(factory);
		HttpSession session = request.getSession();
		try {
			List<FileItem> items = (List<FileItem>) upload.parseRequest(request);
			FileItem item = items.get(0);
			String sql;
			sql = item.getString();
			logger.info("Imported Query: " + sql);
			StringTokenizer tokenizer = new StringTokenizer(sql, "\n\r");
			sql = tokenizer.nextToken();
			JSONObject json = new JSONObject(sql);
			session.setAttribute("query", json);
			logger.info("JSON: " + json.toString());
		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * get the Excel string of the mappings to be saved
	 * 
	 * @param request
	 *            the servlet request
	 * @param conn
	 *            the DB connection
	 * @return the CSV string to be saved in a file
	 */
	private String saveSelectedResults(HttpServletRequest request, Connection conn) {
		String rows = request.getParameter("rows");
		String action = request.getParameter("predicate");
		String phenotypes = "";
		try {
			// get the query results
			JSONArray json = new JSONArray(rows);
			if (action.equals("markAsCorrect") || action.equals("markAsIncorrect")) {
				// extend the rows with the user_label, user and timestamp info
				String user_label = action.equals("markAsCorrect") ? "CORRECT" : "INCORRECT";
				String markedTimestamp = request.getParameter("timestamp");
				String user = (String) request.getSession(false).getAttribute("user");
				for (int i=0; i < json.length(); i++) {
					JSONArray row = json.getJSONArray(i);
					int index = row.length() - 1;
					String dbGaP = row.getString(index);
					row.put(index++, user_label);
					row.put(index++, user);
					row.put(index++, dbGaP);
				}
				if (markedTimestamp == null) {
					Timestamp timestamp = new Timestamp((new Date()).getTime());
					for (int i=0; i < json.length(); i++) {
						JSONArray row = json.getJSONArray(i);
						int index = row.length() - 1;
						String dbGaP = row.getString(index);
						row.put(index++, timestamp);
						row.put(index++, dbGaP);
					}
					// save into the DB
					conn.setAutoCommit(false);
					String sqlQuery = Utils.getSQLsaveSelectedResults();
					logger.info(sqlQuery);
					PreparedStatement stmt = conn.prepareStatement(sqlQuery);
					int rowCount = Utils.executeUpdate(stmt, json);
					conn.commit();
					logger.info("Inserted " + rowCount + " rows.");
					stmt.close();
					conn.setAutoCommit(true);
					// convert the timestamp to a date format
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					markedTimestamp = df.format(timestamp);
					JSONObject data = new JSONObject();
					data.putOpt("markedTime", markedTimestamp);
					phenotypes = data.toString();
				} else {
					for (int i=0; i < json.length(); i++) {
						JSONArray row = json.getJSONArray(i);
						int index = row.length() - 1;
						String dbGaP = row.getString(index);
						row.put(index++, markedTimestamp);
						row.put(index++, dbGaP);
					}
					phenotypes = Utils.toText(json, Utils.MARKED_COLUMNS);
				}
			} else {
				phenotypes = Utils.toText(json, Utils.DOWNLOAD_COLUMNS);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				conn.rollback();
				conn.setAutoCommit(true);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return phenotypes;
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
			json.put("template", request.getParameter("template"));
			String sqlQuery = "INSERT INTO bookmarks(name, description, userid) VALUES(?, ?, ?)";
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
	 * bookmark a query
	 * 
	 * @param request
	 *            the servlet request
	 * @param conn
	 *            the DB connection
	 * @return the bookmark
	 */
	private JSONObject studyMetadata(HttpServletRequest request, Connection conn) throws ServletException {
		JSONObject res = new JSONObject();
		try {
			String sqlQuery = "select distinct sex from " + Utils.STUDIES_TABLE + " order by sex";
			PreparedStatement stmt = conn.prepareStatement(sqlQuery);
			JSONArray rows = Utils.executeSQL(stmt);
			//System.out.println(rows);
			JSONArray gender = new JSONArray();
			for (int i=0; i < rows.length(); i++) {
				JSONArray row = rows.getJSONArray(i);
				if (!row.isNull(0)) {
					gender.put(row.getString(0));
				}
			}
			stmt.close();
			res.put("gender", gender);
			
			sqlQuery = "select distinct race from " + Utils.STUDIES_TABLE + " order by race";
			stmt = conn.prepareStatement(sqlQuery);
			rows = Utils.executeSQL(stmt);
			HashSet<String> raceSet = new HashSet<String>();
			for (int i=0; i < rows.length(); i++) {
				JSONArray row = rows.getJSONArray(i);
				if (!row.isNull(0)) {
					String values = row.getString(0);
					StringTokenizer tokenizer = new StringTokenizer(values, ",");
					while (tokenizer.hasMoreTokens()) {
						raceSet.add(tokenizer.nextToken().trim());
					}
				}
			}
			Object raceValues[] = raceSet.toArray();
			Utils.sortArray(raceValues);
			JSONArray races = new JSONArray();
			for (int i=0; i < raceValues.length; i++) {
				races.put(raceValues[i]);
			}
			stmt.close();
			res.put("races", races);
			
			sqlQuery = "select distinct platform from " + Utils.STUDIES_TABLE + " order by platform";
			stmt = conn.prepareStatement(sqlQuery);
			rows = Utils.executeSQL(stmt);
			HashSet<String> platformSet = new HashSet<String>();
			for (int i=0; i < rows.length(); i++) {
				JSONArray row = rows.getJSONArray(i);
				if (!row.isNull(0)) {
					String values = row.getString(0);
					StringTokenizer tokenizer = new StringTokenizer(values, "|");
					while (tokenizer.hasMoreTokens()) {
						platformSet.add(tokenizer.nextToken().trim());
					}
				}
			}
			Object platformValues[] = platformSet.toArray();
			Utils.sortArray(platformValues);
			JSONArray platform = new JSONArray();
			for (int i=0; i < platformValues.length; i++) {
				platform.put(platformValues[i]);
			}
			stmt.close();
			res.put("platform", platform);
			
			sqlQuery = "select distinct study_type from " + Utils.STUDIES_TABLE + " order by study_type";
			stmt = conn.prepareStatement(sqlQuery);
			rows = Utils.executeSQL(stmt);
			HashSet<String> study_typeSet = new HashSet<String>();
			for (int i=0; i < rows.length(); i++) {
				JSONArray row = rows.getJSONArray(i);
				if (!row.isNull(0)) {
					String values = row.getString(0);
					StringTokenizer tokenizer = new StringTokenizer(values, ",");
					while (tokenizer.hasMoreTokens()) {
						study_typeSet.add(tokenizer.nextToken().trim());
					}
				}
			}
			Object study_typeValues[] = study_typeSet.toArray();
			Utils.sortArray(study_typeValues);
			JSONArray study_type = new JSONArray();
			for (int i=0; i < study_typeValues.length; i++) {
				study_type.put(study_typeValues[i]);
			}
			stmt.close();
			res.put("study_type", study_type);
			
			sqlQuery = "select distinct genetic_type from " + Utils.STUDIES_TABLE + " order by genetic_type";
			stmt = conn.prepareStatement(sqlQuery);
			rows = Utils.executeSQL(stmt);
			HashSet<String> genetic_typeSet = new HashSet<String>();
			for (int i=0; i < rows.length(); i++) {
				JSONArray row = rows.getJSONArray(i);
				if (!row.isNull(0)) {
					String values = row.getString(0);
					StringTokenizer tokenizer = new StringTokenizer(values, ",");
					while (tokenizer.hasMoreTokens()) {
						genetic_typeSet.add(tokenizer.nextToken().trim());
					}
				}
			}
			Object genetic_typeValues[] = genetic_typeSet.toArray();
			Utils.sortArray(genetic_typeValues);
			JSONArray genetic_type = new JSONArray();
			for (int i=0; i < genetic_typeValues.length; i++) {
				genetic_type.put(genetic_typeValues[i]);
			}
			stmt.close();
			res.put("genetic_type", genetic_type);
			
			sqlQuery = "select distinct(unnest (a.da)) b from (SELECT diseases, regexp_split_to_array(diseases, ';') as da FROM " + Utils.STUDIES_TABLE + ") a order by b";
			stmt = conn.prepareStatement(sqlQuery);
			rows = Utils.executeSQL(stmt);
			HashSet<String> diseasesSet = new HashSet<String>();
			for (int i=0; i < rows.length(); i++) {
				JSONArray row = rows.getJSONArray(i);
				if (!row.isNull(0)) {
					String values = row.getString(0);
					diseasesSet.add(values.trim());
				}
			}
			Object diseasesValues[] = diseasesSet.toArray();
			Utils.sortArray(diseasesValues);
			JSONArray diseases = new JSONArray();
			for (int i=0; i < diseasesValues.length; i++) {
				diseases.put(diseasesValues[i]);
			}
			stmt.close();
			res.put("diseases", diseases);
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
	 * bookmark a query
	 * 
	 * @param request
	 *            the servlet request
	 * @param conn
	 *            the DB connection
	 * @return the bookmark
	 */
	private JSONObject applyFilterStudyMetadata(HttpServletRequest request, Connection conn) throws ServletException {
		JSONObject res = new JSONObject();
		JSONObject json = null;
		try {
			String sql = request.getParameter("sql");
			if (sql != null) {
				//System.out.println(sql);
				json = new JSONObject(request.getParameter("sql"));
			}
			String sqlQuery = Utils.getStudieMetadataSQL(json);
			PreparedStatement stmt = conn.prepareStatement(sqlQuery);
			Utils.loadStudiesMetadataValues(stmt, json, 1);
			JSONArray rows = Utils.executeSQL(stmt);
			JSONObject flyOver = new JSONObject();
			//System.out.println("Number of returned rows: " + rows.length());
			for (int i=0; i < rows.length(); i++) {
				JSONArray row = rows.getJSONArray(i);
				//System.out.println("(" + i + "), row length: " + row.length());
				if (!row.isNull(12)) {
					flyOver.put(row.getString(13), row.getString(12));
				}
				row.remove(13);
				row.remove(12);
			}
			stmt.close();
			res.put("studies", rows);
			res.put("flyover", flyOver);
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
			String sqlQuery = "SELECT name, description, created from bookmarks where userid = ?";
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
				item.put("description", Utils.decodeQueryHTML(new JSONObject(row.getString(1))));
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
	 * select mappings in a flat format
	 * 
	 * @param request
	 *            the servlet request
	 * @param conn
	 *            the DB connection
	 * @return the selected rows in a table format
	 */
	private JSONObject dataTables(HttpServletRequest request, Connection conn) {
		JSONObject res = new JSONObject();

		try{
			int offset = Integer.parseInt(request.getParameter("iDisplayStart"));
			int limit = Integer.parseInt(request.getParameter("iDisplayLength"));
			int sEcho = Integer.parseInt(request.getParameter("sEcho"));
			int iSortCol = Integer.parseInt(request.getParameter("iSortCol_0"));
			String sSortDir = request.getParameter("sSortDir_0");
			String sql = request.getParameter("sql");
			JSONObject json = new JSONObject(sql);
			if (limit != -1) {
				json.put("offset", offset);
				json.put("limit", limit);
			} else {
				json.remove("offset");
				json.remove("limit");
			}
			json.put("iSortCol", iSortCol);
			json.put("sSortDir", sSortDir);
			request.setAttribute("sql", json);
			JSONObject obj = select(request, conn);
			JSONObject dbgapVariables = new JSONObject();
			JSONArray dbgapArray = obj.getJSONArray("dbgap");
			for (int i=0; i < dbgapArray.length(); i++) {
				JSONArray row = dbgapArray.getJSONArray(i);
				dbgapVariables.put(row.getString(0).toUpperCase(), row.getString(1));
			}
			JSONArray phenotypesArray = obj.getJSONArray("phenotypes");
			for (int i=0; i < phenotypesArray.length(); i++) {
				JSONArray row = phenotypesArray.getJSONArray(i);
				String key = row.getString(3).toUpperCase();
				if (dbgapVariables.has(key)) {
					String a = "<a style=\"color:blue;\" target=\"_newtab2\" href=\"http://www.ncbi.nlm.nih.gov/projects/gap/cgi-bin/variable.cgi?study_id=" + 
					dbgapVariables.get(key) + "\">" + row.getString(3) + "</a>";
					row.put(3, a);
				}
			}
			res.put("aaData", obj.getJSONArray("phenotypes"));
			res.put("iTotalRecords", obj.getInt("count"));
			res.put("iTotalDisplayRecords", obj.getInt("count"));
			res.put("sEcho", sEcho);
			if (sEcho == 1) {
				res.put("range", obj.getJSONObject("range"));
			}
		} 	 catch (JSONException e) {
			e.printStackTrace();
		}

		return res;
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
			String sqlQuery = "SELECT description FROM bookmarks WHERE userid = ? AND name = ?";
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

}

