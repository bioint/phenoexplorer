package edu.isi.pfindr.helper;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility class for servlet processing
 * 
 * @author Serban Voinea
 * 
 */

public class Utils {

	public static final String STUDIES_TABLE = "dbgap_studies_2013_06_03_leslie_modified_07_07";//"dbgap_studies_2013_06_03";
	public static final String VARIABLES_TABLE = "dbgap_variables_2013_06_03_detail";
	public static final String VARIABLES_DICT_EXPANDED_TABLE = "dbgap_all_variable_expanded_stem_token_bi";
	public static final String STUDY_COLUMN = "study";
	public static final String CATEGORY_COLUMN = "category";
	public static final String VARSET_COLUMN = "varset";
	public static final String VARIABLE_COLUMN = "variable";
	public static final String DESCRIPTION_COLUMN = "description";
	public static final String VISIT_COLUMN = "visit";
	public static final String DEFINER_COLUMN = "definer";
	public static final String SCORE_COLUMN = "score";
	public static final String dbGaP = "dbGaP";
	public static final String USER_LABEL = "user_label";
	public static final String USER = "user";
	public static final String TIMESTAMP = "timestamp";
	public static final String PHENOTYPES_COLUMNS[] = {STUDY_COLUMN, CATEGORY_COLUMN, VARSET_COLUMN, VARIABLE_COLUMN, DESCRIPTION_COLUMN, VISIT_COLUMN, DEFINER_COLUMN, SCORE_COLUMN};
	public static final String DOWNLOAD_COLUMNS[] = {CATEGORY_COLUMN, STUDY_COLUMN, VARSET_COLUMN, VARIABLE_COLUMN, DESCRIPTION_COLUMN, VISIT_COLUMN, DEFINER_COLUMN, SCORE_COLUMN, dbGaP};
	public static final String FLAT_COLUMNS[] = {CATEGORY_COLUMN, STUDY_COLUMN, VARSET_COLUMN, VARIABLE_COLUMN, DESCRIPTION_COLUMN, VISIT_COLUMN, DEFINER_COLUMN, SCORE_COLUMN};
	public static final String MARKED_COLUMNS[] = {CATEGORY_COLUMN, STUDY_COLUMN, VARSET_COLUMN, VARIABLE_COLUMN, DESCRIPTION_COLUMN, VISIT_COLUMN, DEFINER_COLUMN, SCORE_COLUMN, USER_LABEL, USER, TIMESTAMP, dbGaP};
	public static final int MARKED_COLUMNS_TYPES[] = {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.DOUBLE, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP, Types.VARCHAR};
	
	public static final String DEFINER_MAPPINGS_COLUMNS[] = {SCORE_COLUMN, VARIABLE_COLUMN, VARSET_COLUMN, STUDY_COLUMN, CATEGORY_COLUMN, DEFINER_COLUMN};
	public static final int DEFINER_MAPPINGS_COLUMNS_TYPES[] = {Types.DOUBLE, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
	public static final String TEMPORARY_COLUMNS[] = { "variable_match_score", "more", "description", "variable", "study"};
	public static final String TEMPORARY_JOIN_COLUMNS[] = { "T1.variable_match_score", "", "T1.description", "T1.variable", "T1.study", "T2.diseases"};
	public static final int TEMPORARY_COLUMNS_TYPES[] = {Types.DOUBLE, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.DOUBLE};
	
	public static final String CATEGORY_COLUMNS[] = {CATEGORY_COLUMN, DEFINER_COLUMN, DESCRIPTION_COLUMN};
	public static final int CATEGORY_COLUMNS_TYPES[] = {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
	
	public static final int PHENOTYPES_COLUMNS_TYPES[] = {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.DOUBLE};
	public static final String CARE_VARIABLES = "variables";
	public static final String CARE_CATEGORIES = "categories";
	public static final String CARE_MAPPINGS = "mappings";
	public static final String CATEGORY_DEFINER_COLUMN = CARE_CATEGORIES + "." + DEFINER_COLUMN;
	public static final String PHENOTYPES_COLUMNS_TABLES[] = {CARE_MAPPINGS, CARE_MAPPINGS, CARE_MAPPINGS, CARE_MAPPINGS, CARE_VARIABLES, CARE_VARIABLES, CARE_MAPPINGS, CARE_MAPPINGS};
	public static final String PHENOTYPES_USING_COLUMNS[] = {STUDY_COLUMN, VARSET_COLUMN, VARIABLE_COLUMN};
	public static final String PHENOTYPES_JOIN_TABLES[] = {CARE_MAPPINGS,  CARE_VARIABLES};
	public static final String CATEGORY_USING_COLUMNS[] = {CATEGORY_COLUMN};
	public static final String CATEGORY_JOIN_TABLES[] = {CARE_CATEGORIES};
	public static final String ORDER_BY = "orderBy";
	public static final String LIMIT = "limit";
	public static final String OFFSET = "offset";
	public static final String WHERE = "where";
	public static final String OP = "op";
	public static final String VALUES = "values";
	public static final String KEYWORDS = "keywords";
	public static final String COLUMNS = "columns";
	public static final String COLUMN = "column";
	public static final String GROUP_BY = "groupBy";
	public static final String GROUP_BY_DEFAULT[][]= {{CATEGORY_COLUMN}, {STUDY_COLUMN}, {VARSET_COLUMN, VARIABLE_COLUMN, DESCRIPTION_COLUMN, VISIT_COLUMN}, {DEFINER_COLUMN, SCORE_COLUMN}};
	public static final int KEYWORDS_LIMIT = 5;
	public static final String REGEX_CHARACTERS = "\\.[{()*+?|^$";

	@SuppressWarnings("serial")
	public final static HashMap<String,String[]> columnMap = new HashMap<String,String[]>() {	{
		put(STUDY_COLUMN, new String[]{STUDY_COLUMN, STUDY_COLUMN, CARE_VARIABLES});
		//put(CATEGORY_COLUMN, new String[]{CATEGORY_DEFINER_COLUMN + " || '<br/>' || " + CATEGORY_COLUMN, CATEGORY_DEFINER_COLUMN + ", " + CATEGORY_COLUMN, CARE_CATEGORIES});
		put(CATEGORY_COLUMN, new String[]{CATEGORY_COLUMN, CATEGORY_COLUMN, CARE_CATEGORIES});
	}};

	static private Logger logger = Logger.getLogger("AppLogging");       

	/**
	 * Append the WHERE clause in the SQL query
	 * 
	 * @param select
	 *            the array containing the SQL query "tokens"
	 * @param json
	 *            the dictionary of the WHERE clause
	 * @param excludeColumn
	 *            column to be excluded from the WHERE clause
	 */
	public static void appendWherePredicate(ArrayList<String> select, JSONObject json, String excludeColumn) {
		try {
			if (json.has(WHERE)) {
				boolean first = true;
				JSONObject wherePredicate = json.getJSONObject(WHERE);
				String names[] = JSONObject.getNames(wherePredicate);

				// build AND between columns
				ArrayList<String> sqlPredicate = new ArrayList<String>();
				for (int i=0; i<names.length; i++) {
					if (names[i].equals(excludeColumn)) {
						continue;
					}
					if (first) {
						select.add("WHERE");
						first = false;
					}
					// build OR between column values
					ArrayList<String> columnPredicate = new ArrayList<String>();
					columnPredicate.add("(");
					JSONObject obj = wherePredicate.getJSONObject(names[i]);
					String op = obj.getString(OP);
					JSONArray values = obj.getJSONArray(VALUES);
					ArrayList<String> predicates = new ArrayList<String>();
					if (names[i].equals("the category")) {
						for (int j=0; j < values.length(); j++) {
							ArrayList<String> predicate = new ArrayList<String>();
							JSONObject categoryPredicate = values.getJSONObject(j);
							JSONArray predicateNames = categoryPredicate.names();
							ArrayList<String> categoryPredicates = new ArrayList<String>();
							for (int k=0; k < predicateNames.length(); k++) {
								predicate.add(predicateNames.getString(k));
								predicate.add("=");
								predicate.add("?");
								categoryPredicates.add(join(predicate, " "));
							}
							predicates.add("(" + join(categoryPredicates, "AND") + ")");
						}
						columnPredicate.add(join(predicates, " OR "));
					}
					else if (op.equals("Between")) {
						ArrayList<String> predicate = new ArrayList<String>();
						predicate.add(getColumnQualifierName(names[i]));
						predicate.add(">=");
						predicate.add("?");
						predicates.add(join(predicate, " "));
						predicate = new ArrayList<String>();
						predicate.add(getColumnQualifierName(names[i]));
						predicate.add("<=");
						predicate.add("?");
						predicates.add(join(predicate, " "));
						columnPredicate.add(join(predicates, " AND "));
					} else {
						for (int j=0; j < values.length(); j++) {
							ArrayList<String> predicate = new ArrayList<String>();
							predicate.add(getColumnQualifierName(names[i]));
							predicate.add(op);
							predicate.add("?");
							predicates.add(join(predicate, " "));
						}
						columnPredicate.add(join(predicates, " OR "));
					}
					columnPredicate.add(")");
					sqlPredicate.add(join(columnPredicate, " "));
				}
				select.add(join(sqlPredicate, " AND "));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Convert a tree structure to an XML format
	 * 
	 * @param doc
	 *            the Document for generating the XML
	 * @param root
	 *            the Element to which will be added new nodes
	 * @param obj
	 *            the object with SQL results
	 */
	public static void toXML(Document doc, Element root, JSONObject obj) {
		try {
			if (obj.length() > 0) {
				JSONArray names = obj.names();
				ArrayList<String> keys = new ArrayList<String>();
				for (int i=0; i < names.length(); i++) {
					keys.add(names.getString(i));
				}
				Collections.sort(keys, (new Utils()).new StringComparator());
				for (String key : keys) {
					JSONObject value = obj.getJSONObject(key);
					String tag = value.getString("tag");
					Element child = doc.createElement(tag);
					child.setAttribute("value", key);
					root.appendChild(child);
					JSONObject children = value.getJSONObject("values");
					toXML(doc, child, children);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Gets the full name of a column
	 * 
	 * @param column
	 *            the column name
	 * @return the full name of the column (TABLE.COLUMN)
	 */
	public static String getColumnQualifierName(String column) {
		int index = getColumnIndex(column);
		StringBuffer buffer = (new StringBuffer(PHENOTYPES_COLUMNS_TABLES[index])).append(".").append(column);
		return buffer.toString();
	}

	/**
	 * Gets the index of a column in the returned results
	 * 
	 * @param column
	 *            the column name
	 * @return the index of a column in the returned results
	 */
	public static int getColumnIndex(String column) {
		int index = -1;
		for (int i=0; i < PHENOTYPES_COLUMNS.length; i++) {
			if (PHENOTYPES_COLUMNS[i].equals(column)) {
				index = i;
				break;
			}
		}
		return index;
	}

	/**
	 * Join the elements of the array
	 * 
	 * @param strings
	 *            the array of elements
	 * @param delimiter
	 *            the delimiter
	 * @return the join string of the array elements
	 */
	public static String join(String strings[], String delimiter){
		if(strings==null || delimiter == null) {
			return "";
		}

		StringBuffer buf = new StringBuffer();
		boolean first = true;

		for (String value : strings) {
			if (first) {
				first = false;
			} else {
				buf.append(delimiter);
			}
			buf.append(value);
		}

		return buf.toString();
	}

	/**
	 * Join the elements of the list
	 * 
	 * @param strings
	 *            the list of elements
	 * @param delimiter
	 *            the delimiter
	 * @return the join string of the list elements
	 */
	public static String join(List<String> strings, String delimiter){
		if(strings==null || delimiter == null) {
			return "";
		}

		StringBuffer buf = new StringBuffer();
		boolean first = true;

		for (String value : strings) {
			if (first) {
				first = false;
			} else {
				buf.append(delimiter);
			}
			buf.append(value);
		}

		return buf.toString();
	}

	/**
	 * Get the Phenotypes SQL query for the PreparedStatement
	 * 
	 * @param json
	 *            the dictionary for the SQL query
	 * @return the string for the PreparedStatement
	 */
	public static String getPhenotypesSQL(JSONObject json) {
		boolean hasStudyCategoryTemplate = !json.has("iSortCol");

		if (hasStudyCategoryTemplate && json.has(ORDER_BY)) {
			try {
				hasStudyCategoryTemplate = false;
				JSONArray values = json.getJSONArray(ORDER_BY);
				if (values.length() > 1 && 
						values.getString(0).equals(STUDY_COLUMN) &&
						values.getString(1).equals(CATEGORY_COLUMN)) {
					// we must return the number of variables in the study and category		
					hasStudyCategoryTemplate = true;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (hasStudyCategoryTemplate) {
			return getStudiesCategoriesPhenotypesSQL(json);
		} else {
			return getTemplatePhenotypesSQL(json);
		}
	}

	/**
	 * Get the Phenotypes SQL query for the PreparedStatement
	 * 
	 * @param json
	 *            the dictionary for the SQL query
	 * @return the string for the PreparedStatement
	 */
	public static String getStudieMetadataSQL(JSONObject json) {
		/*
		String sql = "SELECT '<input type=\"checkbox\"/>', " + 
			"'<a target=\"_newtab2\" class=\"flyover\" ' || CASE WHEN website is not null THEN  'style=\"color:blue;\" href=\"http://' || website || '\"' ELSE '' END || '>'  || study_id || '</a>', " +
			"race, participants, sex, min_age, max_age, study_type, genetic_type, platform, " + 
			"CASE WHEN study is not null THEN study || '<br/>' END || " + 
			"CASE WHEN description is not null THEN description END, study_id FROM studies WHERE study_id is not null";
		*/
		//*
		String sql = "SELECT '<input type=\"checkbox\"/>', " + 
		"'<a target=\"_newtab2\" class=\"flyover\" ' || 'style=\"color:blue;\" id=\"' || dbgap_study_id || '\" href=\"' || study_url || '\"' || '>'  || dbgap_study_name || '</a>', " +
		"'0', race, participants, sex, min_age, max_age, study_type, genetic_type, replace(platform, '|', ' | '), replace(diseases, ';', ' ; '), " +
		"CASE WHEN dbgap_study_name is not null THEN dbgap_study_name || '<br/>' END || " + 
		"CASE WHEN description is not null THEN description END, dbgap_study_name FROM " + STUDIES_TABLE + " WHERE dbgap_study_name is not null";
		System.out.println("Metadata query:\n" + sql);
		//*/
		if (json != null) {
			try {
				sql += " AND ";
				ArrayList<String> where = new ArrayList<String>();
				if (json.has("studies")) {
					JSONArray values = json.getJSONArray("studies");
					ArrayList<String> studies = new ArrayList<String>();
					for (int i=0; i < values.length(); i++) {
						studies.add("dbgap_study_id = '" + values.getString(i) + "'");
					}
					where.add("(" + join(studies, " OR ") + ")");
				}
				if (json.has("races")) {
					JSONArray values = json.getJSONArray("races");
					ArrayList<String> races = new ArrayList<String>();
					races.add("race is null");
					for (int i=0; i < values.length(); i++) {
						races.add("position(? in race) > 0");
					}
					where.add("(" + join(races, " OR ") + ")");
				}
				if (json.has("genders")) {
					JSONArray values = json.getJSONArray("genders");
					ArrayList<String> genders = new ArrayList<String>();
					genders.add("sex is null");
					for (int i=0; i < values.length(); i++) {
						genders.add("sex = ?");
					}
					where.add("(" + join(genders, " OR ") + ")");
				}
				if (json.has("platforms")) {
					JSONArray values = json.getJSONArray("platforms");
					ArrayList<String> platforms = new ArrayList<String>();
					platforms.add("platform is null");
					for (int i=0; i < values.length(); i++) {
						platforms.add("position(? in platform) > 0");
					}
					where.add("(" + join(platforms, " OR ") + ")");
				}
				if (json.has("study_type")) {
					JSONArray values = json.getJSONArray("study_type");
					ArrayList<String> study_type = new ArrayList<String>();
					for (int i=0; i < values.length(); i++) {
						study_type.add("position(? in study_type) > 0");
					}
					where.add("(" + join(study_type, " OR ") + ")");
				}
				if (json.has("genetic_type")) {
					JSONArray values = json.getJSONArray("genetic_type");
					ArrayList<String> genetic_type = new ArrayList<String>();
					genetic_type.add("genetic_type is null");
					for (int i=0; i < values.length(); i++) {
						genetic_type.add("position(? in genetic_type) > 0");
					}
					where.add("(" + join(genetic_type, " OR ") + ")");
				}
				if (json.has("diseases")) {
					JSONArray values = json.getJSONArray("diseases");
					ArrayList<String> diseases = new ArrayList<String>();
					diseases.add("diseases is null");
					for (int i=0; i < values.length(); i++) {
						diseases.add("position(? in diseases) > 0");
					}
					where.add("(" + join(diseases, " OR ") + ")");
				}
				if (json.has("age")) {
					JSONObject age = json.getJSONObject("age");
					if (age.has("min")) {
						where.add("(min_age >= ?)");
					}
					if (age.has("max")) {
						where.add("(max_age <= ?)");
					}
				}
				if (json.has("participants")) {
					JSONObject age = json.getJSONObject("participants");
					if (age.has("min")) {
						where.add("(participants >= ?)");
					}
					if (age.has("max")) {
						where.add("(participants <= ?)");
					}
				}
				sql += join(where, " AND ");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//sql += " ORDER BY study_id";
		sql += " ORDER BY dbgap_study_name";
		
		return sql;
	}

	/**
	 * Loads the values in a PreparedStatement
	 * 
	 * @param stmt
	 *            the PreparedStatement
	 * @param json
	 *            the dictionary with the values for the SQL query
	 * @param exclude
	 *            if true, do not load the values for the offset and limit
	 */
	public static void loadStudiesMetadataValues(PreparedStatement stmt, JSONObject json, int position) {
		if (json == null) {
			return;
		}
		try {
			if (json.has("races")) {
				JSONArray values = json.getJSONArray("races");
				for (int i=0; i < values.length(); i++) {
					stmt.setString(position++, values.getString(i));
				}
			}
			if (json.has("genders")) {
				JSONArray values = json.getJSONArray("genders");
				for (int i=0; i < values.length(); i++) {
					stmt.setString(position++, values.getString(i));
				}
			}
			if (json.has("platforms")) {
				JSONArray values = json.getJSONArray("platforms");
				for (int i=0; i < values.length(); i++) {
					stmt.setString(position++, values.getString(i));
				}
			}
			if (json.has("study_type")) {
				JSONArray values = json.getJSONArray("study_type");
				for (int i=0; i < values.length(); i++) {
					stmt.setString(position++, values.getString(i));
				}
			}
			if (json.has("genetic_type")) {
				JSONArray values = json.getJSONArray("genetic_type");
				for (int i=0; i < values.length(); i++) {
					stmt.setString(position++, values.getString(i));
				}
			}
			if (json.has("diseases")) {
				JSONArray values = json.getJSONArray("diseases");
				for (int i=0; i < values.length(); i++) {
					stmt.setString(position++, values.getString(i));
				}
			}
			if (json.has("age")) {
				JSONObject age = json.getJSONObject("age");
				if (age.has("min")) {
					stmt.setInt(position++, age.getInt("min"));
				}
				if (age.has("max")) {
					stmt.setInt(position++, age.getInt("max"));
				}
			}
			if (json.has("participants")) {
				JSONObject age = json.getJSONObject("participants");
				if (age.has("min")) {
					stmt.setInt(position++, age.getInt("min"));
				}
				if (age.has("max")) {
					stmt.setInt(position++, age.getInt("max"));
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Get the Phenotypes SQL query for the PreparedStatement
	 * 
	 * @param json
	 *            the dictionary for the SQL query
	 * @return the string for the PreparedStatement
	 */
	public static String getStudiesCategoriesPhenotypesSQL(JSONObject json) {
		try {
			// generate the select statement
			ArrayList<String> select = new ArrayList<String>();
			// with T as (select study, category, varset, variable, description, visit, definer, score from mappings join variables using (study, varset, variable))
			//select TT2.study || '(' || TT1.b || ')' , TT2.category || '(' || TT1.a || ')' , TT2.varset, TT2.variable, TT2.description, TT2.visit, TT2.definer, TT2.score  from (select study, T2.b b, category, T1.a a from (select study, category, count(distinct variable) as a from T group by study, category) as T1 JOIN (select study, count(distinct variable) as b from T group by study) as T2 USING (study)) TT1 JOIN (select * from T order by study, category, varset, variable, description, visit, definer, score) TT2 using (study, category) limit 10;

			select.add("WITH T AS ( SELECT");
			select.add("study, category, varset, variable, description, visit, definer, score");
			select.add("FROM");
			select.add("mappings");
			select.add("JOIN");
			select.add("variables");
			select.add("USING ( study, varset, variable )");

			// append WHERE predicate
			appendWherePredicate(select, json, "");

			// END WITH
			select.add(")");


			select.add("SELECT");
			select.add("TT2.study || ' (' || TT1.b || ')' as study, TT2.category || ' (' || TT1.a || ')' as category, TT2.varset, TT2.variable, TT2.description, TT2.visit, TT2.definer, TT2.score");
			select.add("FROM");
			select.add("(");
			select.add("SELECT");
			select.add("study, T2.b b, category, T1.a a");
			select.add("FROM");
			select.add("(");
			select.add("SELECT");
			select.add("study, category, count(distinct variable) AS a");
			select.add("FROM");
			select.add("T");
			select.add("GROUP BY");
			select.add("study, category");
			select.add(")");
			select.add("AS T1");
			select.add("JOIN");
			select.add("(");
			select.add("SELECT");
			select.add("study, count(distinct variable) AS b ");
			select.add("FROM");
			select.add("T");
			select.add("GROUP BY");
			select.add("study");
			select.add(")");
			select.add("AS T2 ");
			select.add("USING");
			select.add("(");
			select.add("study");
			select.add(")");
			select.add(")");
			select.add("TT1");
			select.add("JOIN");
			select.add("(");
			select.add("SELECT");
			select.add("*");
			select.add("FROM");
			select.add("T");
			if (json.has(ORDER_BY)) {
				// append ORDER BY clause
				select.add("ORDER BY");
				JSONArray values = json.getJSONArray(ORDER_BY);
				ArrayList<String> orderBy = new ArrayList<String>();
				int iSortCol = -1;
				String sSortDir = null;
				if (json.has("iSortCol")) {
					iSortCol = json.getInt("iSortCol");
					sSortDir = json.getString("sSortDir");
					orderBy.add(FLAT_COLUMNS[iSortCol] + " " + sSortDir);
				}
				for (int i=0; i < values.length(); i++) {
					if (i == iSortCol) {
						continue;
					}
					String column = values.getString(i);
					if (column.equals(SCORE_COLUMN)) {
						column += " DESC";
					}
					orderBy.add(column);
				}
				select.add(join(orderBy, ", "));
			}
			select.add(")");
			select.add("TT2");
			select.add("USING");
			select.add("(");
			select.add("study, category");
			select.add(")");

			// for data tables (template flat)
			if (json.has("iSortCol")) {
				// append ORDER BY clause
				select.add("ORDER BY");
				//JSONArray values = json.getJSONArray(ORDER_BY);
				ArrayList<String> orderBy = new ArrayList<String>();
				int iSortCol = json.getInt("iSortCol");
				String sSortDir = json.getString("sSortDir");
				orderBy.add(FLAT_COLUMNS[iSortCol] + " " + sSortDir);
				for (int i=0; i < FLAT_COLUMNS.length; i++) {
					if (i == iSortCol) {
						continue;
					}
					String column = FLAT_COLUMNS[i];
					if (column.equals(SCORE_COLUMN)) {
						column += " DESC";
					}
					orderBy.add(column);
				}
				select.add(join(orderBy, ", "));
			}
			if (json.has(LIMIT)) {
				// append LIMIT clause
				select.add("LIMIT");
				select.add("?");
			}
			if (json.has(OFFSET)) {
				// append OFFSET clause
				select.add("OFFSET");
				select.add("?");
			}
			return join(select, " ");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get the Phenotypes SQL query for the PreparedStatement
	 * 
	 * @param json
	 *            the dictionary for the SQL query
	 * @return the string for the PreparedStatement
	 */
	public static String getTemplatePhenotypesSQL(JSONObject json) {
		try {
			// generate the select statement
			ArrayList<String> select = new ArrayList<String>();
			select.add("SELECT");

			// append the columns
			ArrayList<String> columns = new ArrayList<String>();
			for (String column : json.has("iSortCol") ? FLAT_COLUMNS : PHENOTYPES_COLUMNS) {
				columns.add(getColumnQualifierName(column));
			}
			if (json.has("iSortCol")) {
				columns.add("'<input type=\"checkbox\"/>'");
			}
			select.add(join(columns, ", "));

			// append JOIN tables
			select.add("FROM");
			select.add(join(PHENOTYPES_JOIN_TABLES, " JOIN "));

			// append JOIN variables
			select.add("USING");
			select.add("(");
			select.add(join(PHENOTYPES_USING_COLUMNS, ", "));
			select.add(")");

			// append WHERE predicate
			appendWherePredicate(select, json, "");

			if (json.has(ORDER_BY)) {
				// append ORDER BY clause
				select.add("ORDER BY");
				JSONArray values = json.getJSONArray(ORDER_BY);
				ArrayList<String> orderBy = new ArrayList<String>();
				int iSortCol = -1;
				String sSortDir = null;
				if (json.has("iSortCol")) {
					iSortCol = json.getInt("iSortCol");
					sSortDir = json.getString("sSortDir");
					orderBy.add(FLAT_COLUMNS[iSortCol] + " " + sSortDir);
					if (!FLAT_COLUMNS[iSortCol].equals(SCORE_COLUMN)) {
						orderBy.add(SCORE_COLUMN + " " + "DESC");
					}
				}
				for (int i=0; i < values.length(); i++) {
					if (i == iSortCol || FLAT_COLUMNS[i].equals(SCORE_COLUMN)) {
						continue;
					}
					String column = values.getString(i);
					if (column.equals(SCORE_COLUMN)) {
						column += " DESC";
					}
					orderBy.add(column);
				}
				select.add(join(orderBy, ", "));
			}
			if (json.has(LIMIT)) {
				// append LIMIT clause
				select.add("LIMIT");
				select.add("?");
			}
			if (json.has(OFFSET)) {
				// append OFFSET clause
				select.add("OFFSET");
				select.add("?");
			}
			return join(select, " ");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get the SQL query for the count of the Phenotypes
	 * 
	 * @param json
	 *            the dictionary for the SQL query
	 * @return the string for the PreparedStatement
	 */
	public static String getPhenotypesCount(JSONObject json) {
		// generate the select statement
		ArrayList<String> select = new ArrayList<String>();
		select.add("SELECT");
		select.add("count(*)");
		select.add("FROM");
		select.add(join(PHENOTYPES_JOIN_TABLES, " JOIN "));

		// append JOIN variables
		select.add("USING");
		select.add("(");
		select.add(join(PHENOTYPES_USING_COLUMNS, ", "));
		select.add(")");

		// append WHERE predicate
		appendWherePredicate(select, json, "");

		return join(select, " ");
	}

	/**
	 * Get the SQL query for the range of the studies or categories
	 * 
	 * @param json
	 *            the dictionary for the SQL query
	 * @param column
	 *            the column for which we get the range
	 * @param excludeColumn
	 *            the column to be excluded from the WHERE predicate
	 * @return the string for the PreparedStatement
	 */
	public static String getPhenotypesRange(JSONObject json, String column, String excludeColumn, JSONObject jsonStudy) {
		// generate the select statement
		ArrayList<String> select = new ArrayList<String>();
		select.add("SELECT distinct ");
		select.add(columnMap.get(column)[0]);
		select.add("FROM");
		select.add(join(PHENOTYPES_JOIN_TABLES, " JOIN "));

		// append JOIN variables
		select.add("USING");
		select.add("(");
		select.add(join(PHENOTYPES_USING_COLUMNS, ", "));
		select.add(")");

		/*
		if (column.equals(CATEGORY_COLUMN)) {
			select.add("JOIN");
			select.add(columnMap.get(column)[2]);
			select.add("USING");
			select.add("(");
			select.add(CATEGORY_COLUMN);
			select.add(")");
		}
		*/

		// append WHERE predicate
		appendWherePredicate(select, json, excludeColumn);
		String sqlQuery = join(select, " ");
		boolean hasPredicate = false;
		if (json.has(WHERE)) {
			try {
				JSONObject wherePredicate = json.getJSONObject(WHERE);
				if (wherePredicate.length() > 1) {
					hasPredicate = true;
				}
				if (wherePredicate.has(column)) {
					JSONObject obj = wherePredicate.getJSONObject(column);
					JSONArray values = obj.getJSONArray(VALUES);
					select = new ArrayList<String>();
					for (int i=0; i < values.length(); i++) {
						select.add((column.equals("study") ? "study_id" : column)  + " = '" + values.getString(i) + "'");
					}
					String unionQuery = " union (select distinct ";
					if (column.equals("study")) {
						unionQuery += "study_id from studies ";
					} else {
						unionQuery += column + " from mappings ";
					}
					String whereSQL = unionQuery + " where " + join(select, " OR ") + ")";
					sqlQuery += whereSQL;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (column.equals("study")) {
			String metadataQuery = getStudieMetadataSQL(jsonStudy);
			if (!hasPredicate) {
				sqlQuery = metadataQuery;
			} else {
				sqlQuery = "SELECT T2.* FROM (" + sqlQuery + ") T1 JOIN (" + metadataQuery + ") T2 on T1.study = T2.study_id ";
				sqlQuery += " ORDER BY T2.study_id";
			}
		} else if (column.equals("category")) {
			sqlQuery = "select category, description from categories where category in (" + sqlQuery + ") ORDER BY category";
		} else {
			sqlQuery += " ORDER BY " + column;
		}
		/*
		select.add("GROUP BY");
		select.add(columnMap.get(column)[1]);
		select.add("ORDER BY");
		select.add(columnMap.get(column)[1]);
		*/
		//sqlQuery += " ORDER BY " + column;

		//return join(select, " ");
		return sqlQuery;

	}

	/**
	 * Get the SQL query for fetching up to 5 hints for keyword(s)
	 * 
	 * @return the string for the PreparedStatement
	 */
	public static String getKeywordSQL(String column) {
		// generate the select statement
		ArrayList<String> select = new ArrayList<String>();
		select.add("SELECT DISTINCT");
		select.add(column);
		select.add("FROM");
		select.add(CARE_VARIABLES);
		select.add("WHERE");
		select.add(column);
		select.add("~*");
		select.add("?");
		select.add("ORDER BY");
		select.add(column);
		select.add("LIMIT");
		select.add("" + KEYWORDS_LIMIT);
		return join(select, " ");
	}

	/**
	 * Get the SQL query for fetching up the users
	 * 
	 * @return the string for the PreparedStatement
	 */
	public static String getRolesSQL() {
		// generate the select statement
		ArrayList<String> select = new ArrayList<String>();
		select.add("SELECT");
		select.add("userid,");
		select.add("isadmin,");
		select.add("isdefiner");
		select.add("FROM");
		select.add("roles");
		select.add("ORDER BY");
		select.add("userid");
		return join(select, " ");
	}

	/**
	 * Get the SQL query for fetching up the roles of an user
	 * 
	 * @return the string for the PreparedStatement
	 */
	public static String getUserRoleSQL() {
		// generate the select statement
		ArrayList<String> select = new ArrayList<String>();
		select.add("SELECT");
		select.add("password,");
		select.add("mustchange,");
		select.add("isadmin,");
		select.add("isdefiner");
		select.add("FROM");
		select.add("roles");
		select.add("WHERE");
		select.add("userid");
		select.add("=");
		select.add("?");
		return join(select, " ");
	}

	/**
	 * Get the SQL query for updating the roles of an user
	 * 
	 * @return the string for the PreparedStatement
	 */
	public static String updatePasswordSQL() {
		// generate the select statement
		ArrayList<String> update = new ArrayList<String>();
		update.add("UPDATE");
		update.add("roles");
		update.add("SET");
		update.add("password");
		update.add("=");
		update.add("?,");
		update.add("mustchange");
		update.add("=");
		update.add("?");
		update.add("WHERE");
		update.add("userid");
		update.add("=");
		update.add("?");
		return join(update, " ");
	}

	/**
	 * Get the SQL query for updating the admin role of an user
	 * 
	 * @return the string for the PreparedStatement
	 */
	public static String updateAdminSQL(String role) {
		// generate the select statement
		ArrayList<String> update = new ArrayList<String>();
		update.add("UPDATE");
		update.add("roles");
		update.add("SET");
		update.add(role);
		update.add("=");
		update.add("?");
		update.add("WHERE");
		update.add("userid");
		update.add("=");
		update.add("?");
		return join(update, " ");
	}

	/**
	 * Get the SQL query for adding a new user
	 * 
	 * @return the string for the PreparedStatement
	 */
	public static String createUserSQL() {
		// generate the select statement
		ArrayList<String> insert = new ArrayList<String>();
		insert.add("INSERT");
		insert.add("INTO");
		insert.add("roles(");
		insert.add("userid,");
		insert.add("mustchange,");
		insert.add("isadmin,");
		insert.add("isdefiner)");
		insert.add("VALUES(");
		insert.add("?,");
		insert.add("?,");
		insert.add("?,");
		insert.add("?)");
		return join(insert, " ");
	}

	/**
	 * Get the SQL query for adding a new user
	 * 
	 * @return the string for the PreparedStatement
	 */
	public static String createUserSQL(
			String firstName, 
			String lastName,
			String email,
			String institution) {
		// generate the select statement
		ArrayList<String> insert = new ArrayList<String>();
		insert.add("INSERT");
		insert.add("INTO");
		insert.add("roles(");
		insert.add("userid,");
		insert.add("mustchange,");
		insert.add("isadmin,");
		insert.add("firstName,");
		insert.add("lastName,");
		insert.add("email,");
		insert.add("institution,");
		insert.add("isdefiner)");
		insert.add("VALUES(");
		insert.add("?,");
		insert.add("?,");
		insert.add("?,");
		insert.add("?,");
		insert.add("?,");
		insert.add("?,");
		insert.add("?,");
		insert.add("?)");
		return join(insert, " ");
	}

	/**
	 * Get the SQL query for deleting an user
	 * 
	 * @return the string for the PreparedStatement
	 */
	public static String deleteUserSQL() {
		// generate the select statement
		ArrayList<String> deleteStmt = new ArrayList<String>();
		deleteStmt.add("DELETE");
		deleteStmt.add("FROM");
		deleteStmt.add("roles");
		deleteStmt.add("WHERE");
		deleteStmt.add("userid");
		deleteStmt.add("=");
		deleteStmt.add("?");
		return join(deleteStmt, " ");
	}

	/**
	 * Loads the values in a PreparedStatement
	 * 
	 * @param stmt
	 *            the PreparedStatement
	 * @param json
	 *            the dictionary with the values for the SQL query
	 * @param exclude
	 *            if true, do not load the values for the offset and limit
	 */
	public static int loadValues(PreparedStatement stmt, JSONObject json, String excludeColumn, boolean exclude) {
		int position = 1;
		try {
			if (json.has(WHERE)) {
				JSONObject wherePredicate = json.getJSONObject(WHERE);
				String names[] = JSONObject.getNames(wherePredicate);

				// build AND between columns
				for (int i=0; i<names.length; i++) {
					if (names[i].equals(excludeColumn)) {
						continue;
					}
					// build OR between column values
					JSONObject obj = wherePredicate.getJSONObject(names[i]);
					int index = getColumnIndex(names[i]);
					JSONArray values = obj.getJSONArray(VALUES);
					if (names[i].equals("the category")) {
						for (int j=0; j < values.length(); j++) {
							JSONObject categoryPredicate = values.getJSONObject(j);
							JSONArray predicateNames = categoryPredicate.names();
							for (int k=0; k < predicateNames.length(); k++) {
								stmt.setString(position++, categoryPredicate.getString(predicateNames.getString(k)));
							}
						}
					} else {
						for (int j=0; j < values.length(); j++) {
							String val = values.getString(j);
							if (obj.getString(OP) != null && obj.getString(OP).equals("~*")) {
								/*
								for (char c : REGEX_CHARACTERS.toCharArray()) {
									String oldChar = "" + c;
									String newChar = "\\" + c;
									val = val.replace(oldChar, newChar);
								}
								*/
							}
							if (PHENOTYPES_COLUMNS_TYPES[index] == Types.DOUBLE) {
								stmt.setDouble(position++, Double.valueOf(val));
							} else {
								stmt.setString(position++, val);
							}
						}
					}
				}
			}
			if (!exclude) {
				if (json.has(LIMIT)) {
					// append LIMIT clause
					stmt.setLong(position++, json.getLong(LIMIT));
				}
				if (json.has(OFFSET)) {
					// append OFFSET clause
					stmt.setLong(position++, json.getLong(OFFSET));
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return position;
	}

	/**
	 * Execute a PreparedStatement
	 * 
	 * @param stmt
	 *            the PreparedStatement
	 * @return the SQL query result as an JSONArray
	 */
	public static JSONArray executeSQL(PreparedStatement stmt) {
		JSONArray res = new JSONArray();
		try {
			ResultSet rs = stmt.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
			int columnCount = meta.getColumnCount();
			int columnTypes[] = new int[columnCount];
			for (int i=0; i < columnCount; i++) {
				columnTypes[i] = meta.getColumnType(i+1);
			}
			while (rs.next()) {
				try {
					JSONArray row = new JSONArray();
					for (int i=0; i < columnCount; i++) {
						//System.out.println("type: "+columnTypes[i]);
						if (columnTypes[i] == Types.DOUBLE) {
							row.put(rs.getDouble(i+1));
						} else if (columnTypes[i] == Types.BIGINT) {
							int intValue = rs.getInt(i+1);
							//row.put(rs.getInt(i+1));
							row.put(rs.wasNull() ? null : intValue);
							//if (rs.wasNull() ? null : rs.wasNull()) {
								//System.out.println("NULL Integer: " + a);
							//}
						} else if (columnTypes[i] == Types.VARCHAR) {
							row.put(rs.getString(i+1));
						} else if (columnTypes[i] == Types.BIT) {
							row.put(rs.getBoolean(i+1));
						} else if (columnTypes[i] == Types.TIMESTAMP) {
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							row.put(df.format(rs.getTimestamp(i+1)));
						} else if (columnTypes[i] == Types.OTHER) {
							row.put(rs.getString(i+1));
						} else if (columnTypes[i] == Types.NUMERIC) {
							row.put(rs.getBigDecimal(i+1));
						} else {
							logger.info("Unhandled column type: " + columnTypes[i]);
						}
					}
					res.put(row);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Execute an INSERT PreparedStatement
	 * 
	 * @param stmt
	 *            the PreparedStatement
	 * @param json
	 *            the array of rows to be inserted
	 * @param user
	 *            the user that defines the marked mappings
	 * @return the row count of the inserted statements
	 */
	public static int executeUpdate(PreparedStatement stmt, JSONArray json) {
		int res = 0;
		try {
			for (int i=0; i < json.length(); i++) {
				JSONArray row = json.getJSONArray(i);
				for (int j=0; j < MARKED_COLUMNS.length; j++) {
					switch (MARKED_COLUMNS_TYPES[j]) {
					case Types.VARCHAR:
						stmt.setString(j+1, row.getString(j));
						break;
					case Types.DOUBLE:
						stmt.setDouble(j+1, row.getDouble(j));
						break;
					case Types.TIMESTAMP:
						stmt.setTimestamp(j+1, (Timestamp) row.get(j));
						break;
					}
				}
				res += stmt.executeUpdate();
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
	 * Execute an INSERT PreparedStatement
	 * 
	 * @param stmt
	 *            the PreparedStatement
	 * @param json
	 *            the array of rows to be inserted
	 * @param user
	 *            the user that defines the marked mappings
	 * @return the row count of the inserted statements
	 */
	public static int executeMappingsUpdate(PreparedStatement stmt, JSONArray json) {
		int res = 0;
		try {
			for (int i=0; i < json.length(); i++) {
				JSONArray row = json.getJSONArray(i);
				for (int j=0; j < DEFINER_MAPPINGS_COLUMNS.length; j++) {
					switch (DEFINER_MAPPINGS_COLUMNS_TYPES[j]) {
					case Types.VARCHAR:
						stmt.setString(j+1, row.getString(j));
						break;
					case Types.DOUBLE:
						stmt.setDouble(j+1, row.getDouble(j));
						break;
					}
				}
				res += stmt.executeUpdate();
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
	 * Execute an INSERT PreparedStatement
	 * 
	 * @param stmt
	 *            the PreparedStatement
	 * @param json
	 *            the array of rows to be inserted
	 * @param user
	 *            the user that defines the marked mappings
	 * @return the row count of the inserted statements
	 */
	public static int executeShoppingCartUpdate(PreparedStatement stmt, JSONArray json) {
		int res = 0;
		try {
			for (int i=0; i < json.length(); i++) {
				JSONArray row = json.getJSONArray(i);
				for (int j=0; j < TEMPORARY_COLUMNS.length; j++) {
					switch (TEMPORARY_COLUMNS_TYPES[j]) {
					case Types.VARCHAR:
						stmt.setString(j+1, row.getString(j));
						break;
					case Types.DOUBLE:
						stmt.setDouble(j+1, row.getDouble(j));
						break;
					}
				}
				res += stmt.executeUpdate();
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
	 * Execute an INSERT PreparedStatement
	 * 
	 * @param stmt
	 *            the PreparedStatement
	 * @param json
	 *            the array of rows to be inserted
	 * @param user
	 *            the user that defines the marked mappings
	 * @return the row count of the inserted statements
	 */
	public static int executeCategoryUpdate(PreparedStatement stmt, JSONArray row) {
		int res = 0;
		try {
			for (int i=0; i < CATEGORY_COLUMNS.length; i++) {
				switch (CATEGORY_COLUMNS_TYPES[i]) {
				case Types.VARCHAR:
					stmt.setString(i+1, row.getString(i));
					break;
				case Types.DOUBLE:
					stmt.setDouble(i+1, row.getDouble(i));
					break;
				}
			}
			res = stmt.executeUpdate();
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
	 * Get the SQL insert statement for adding marked mappings
	 * 
	 * @return the string for the PreparedStatement
	 */
	public static String getSQLsaveSelectedResults() {
		String sql = "INSERT INTO selected_results VALUES (";
		ArrayList<String> values = new ArrayList<String>();
		for (int i=0; i < MARKED_COLUMNS.length; i++) {
			values.add("?");
		}
		sql += join(values, ", ") + ")";
		return sql;
	}

	/**
	 * Get the SQL insert statement for adding marked mappings
	 * 
	 * @return the string for the PreparedStatement
	 */
	public static String getSQLsaveSelectedMappings() {
		String sql = "INSERT INTO mappings (";
		ArrayList<String> values = new ArrayList<String>();
		for (int i=0; i < DEFINER_MAPPINGS_COLUMNS.length; i++) {
			values.add(DEFINER_MAPPINGS_COLUMNS[i]);
		}
		sql += join(values, ", ") + ") VALUES (";
		values = new ArrayList<String>();
		for (int i=0; i < DEFINER_MAPPINGS_COLUMNS.length; i++) {
			values.add("?");
		}
		sql += join(values, ", ") + ")";
		return sql;
	}

	/**
	 * Get the SQL insert statement for adding marked mappings
	 * 
	 * @return the string for the PreparedStatement
	 */
	public static String getSQLaddSelectedMappings(String table) {
		String sql = "INSERT INTO " + table + " (";
		ArrayList<String> values = new ArrayList<String>();
		for (int i=0; i < TEMPORARY_COLUMNS.length; i++) {
			values.add(TEMPORARY_COLUMNS[i]);
		}
		sql += join(values, ", ") + ") VALUES (";
		values = new ArrayList<String>();
		for (int i=0; i < TEMPORARY_COLUMNS.length; i++) {
			values.add("?");
		}
		sql += join(values, ", ") + ")";
		return sql;
	}

	/**
	 * Get the SQL insert statement for adding marked mappings
	 * 
	 * @return the string for the PreparedStatement
	 */
	public static String getSQLshoppingCart(String table) {
		String sql = "SELECT ";
		ArrayList<String> values = new ArrayList<String>();
		for (int i=0; i < TEMPORARY_COLUMNS.length; i++) {
			values.add(TEMPORARY_COLUMNS[i]);
		}
		values.add("'<input type=\"checkbox\"/>'");
		sql += join(values, ", ") + " FROM " + table;
		return sql;
	}

	/**
	 * Get the SQL insert statement for adding marked mappings
	 * 
	 * @return the string for the PreparedStatement
	 */
	public static String getSQLsaveCategory() {
		String sql = "INSERT INTO categories (";
		ArrayList<String> values = new ArrayList<String>();
		for (int i=0; i < CATEGORY_COLUMNS.length; i++) {
			values.add(CATEGORY_COLUMNS[i]);
		}
		sql += join(values, ", ") + ") VALUES (";
		values = new ArrayList<String>();
		for (int i=0; i < CATEGORY_COLUMNS.length; i++) {
			values.add("?");
		}
		sql += join(values, ", ") + ")";
		return sql;
	}
	
	public static void dropTable(Connection conn, String name) {
		String sqlQuery = "drop table " + name;
		try {
			PreparedStatement stmt = conn.prepareStatement(sqlQuery);
			logger.info(sqlQuery);
			stmt.execute();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void deleteTable(Connection conn, String name) {
		String sqlQuery = "delete from " + name;
		try {
			PreparedStatement stmt = conn.prepareStatement(sqlQuery);
			logger.info(sqlQuery);
			stmt.execute();
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Convert a "table" to a tree structure suitable for writing it in an XML format
	 * 
	 * @param group
	 *            the list of columns that define the tree structure
	 * @param rows
	 *            the table containing the SQL results
	 * @return the tree structure as a JSONObject
	 */
	public static JSONObject toJSONObject(JSONArray group, JSONArray rows) {
		JSONObject phenotypes = null;
		try {
			phenotypes = new JSONObject();
			for (int i=0; i < rows.length(); i++) {
				JSONArray row = rows.getJSONArray(i);
				JSONObject phenotypeLevel = phenotypes;
				for (int j=0; j< group.length(); j++) {
					JSONArray level = group.getJSONArray(j);
					ArrayList<String> names = new ArrayList<String>();
					for (int k=0; k < level.length(); k++) {
						names.add(level.getString(k));
					}
					String tag = join(names, "_");
					ArrayList<String> values = new ArrayList<String>();
					// the key is generated from the column values
					for (String column: names) {
						int index = getColumnIndex(column);
						values.add(row.getString(index).toString());
					}
					String value = join(values, " | ");
					if (!phenotypeLevel.has(value)) {
						JSONObject obj = new JSONObject();
						obj.put("tag", tag);
						obj.put("values", new JSONObject());
						phenotypeLevel.put(value, obj);
					}
					phenotypeLevel = phenotypeLevel.getJSONObject(value).getJSONObject("values");
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return phenotypes;
	}

	/**
	 * Convert a "table" to a text format suitable for reading with Excel 
	 * 
	 * @param group
	 *            the list of columns that define the tree structure
	 * @param rows
	 *            the table containing the SQL results
	 * @return the string equivalent in Excel format
	 */
	public static String toText(JSONArray group, JSONArray rows) {
		String phenotypes = "";
		ArrayList<String> tr = new ArrayList<String>();
		try {
			// build the table header
			ArrayList<String> names = new ArrayList<String>();
			for (int i=0; i < group.length(); i++) {
				JSONArray level = group.getJSONArray(i);
				for (int j=0; j < level.length(); j++) {
					names.add(level.getString(j));
				}
			}
			String header = join(names, "\t");
			tr.add(header);

			for (int i=0; i < rows.length(); i++) {
				JSONArray row = rows.getJSONArray(i);
				ArrayList<String> values = new ArrayList<String>();
				for (String column: names) {
					int index = getColumnIndex(column);
					values.add(row.optString(index).toString());
				}
				String value = join(values, "\t");
				tr.add(value);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		phenotypes = join(tr, "\n");
		return phenotypes;
	}

	/**
	 * Convert a "table" to a text format suitable for reading with Excel 
	 * 
	 * @param group
	 *            the list of columns that define the tree structure
	 * @param rows
	 *            the table containing the SQL results
	 * @return the string equivalent in Excel format
	 */
	public static String toText(String columns[], JSONArray rows) {
		String phenotypes = "";
		ArrayList<String> tr = new ArrayList<String>();
		try {
			// build the table header
			ArrayList<String> names = new ArrayList<String>();
			for (int i=0; i < columns.length; i++) {
				names.add(columns[i]);
			}
			String header = join(names, "\t");
			tr.add(header);

			for (int i=0; i < rows.length(); i++) {
				JSONArray row = rows.getJSONArray(i);
				ArrayList<String> values = new ArrayList<String>();
				for (int j=0; j < columns.length; j++) {
					values.add(row.isNull(j) ? "" : row.getString(j).toString());
				}
				String value = join(values, "\t");
				tr.add(value);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		phenotypes = join(tr, "\n");
		return phenotypes;
	}

	/**
	 * Convert an array to a text format suitable for reading with Excel 
	 * 
	 * @param rows
	 *            the table containing the SQL results
	 * @return the string equivalent in Excel format
	 */
	public static String toText(JSONArray rows, String columns[]) {
		String phenotypes = "";
		ArrayList<String> tr = new ArrayList<String>();
		try {
			// build the table header
			ArrayList<String> names = new ArrayList<String>();
			for (int i=0; i < columns.length; i++) {
				names.add(columns[i]);
			}
			String header = join(names, "\t");
			tr.add(header);

			for (int i=0; i < rows.length(); i++) {
				JSONArray row = rows.getJSONArray(i);
				ArrayList<String> values = new ArrayList<String>();
				for (int j=0; j < row.length(); j++) {
					values.add(row.getString(j));
				}
				String value = join(values, "\t");
				tr.add(value);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		phenotypes = join(tr, "\n");
		return phenotypes;
	}

	/**
	 * Generate a random password
	 * 
	 * @return the random password
	 */
	public static String pwgen() {
		String psw = null;
		try {
			Process proc = Runtime.getRuntime().exec("/usr/bin/pwgen -c --num-passwords=1");
			int exitVal = proc.waitFor();
			if (exitVal == 0) {
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				psw = stdInput.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.info("Temporary password \"" + psw + "\".");

		return psw;
	}

	/**
	 * Convert a digest value to a hexa string
	 * 
	 * @param cksum
	 *            the byte array of the digest value
	 * @return the hexa string of the digest value
	 */
	public static String hexChecksum(byte[] cksum) {
		String result = "";
		for (int i=0; i < cksum.length; i++) {
			result +=
				Integer.toString( ( cksum[i] & 0xff ) + 0x100, 16).substring( 1 );
		}
		return result;
	}

	/**
	 * Encrypt an password
	 * 
	 * @param password
	 *            the password to be encrypted
	 * @return the encrypted password
	 */
	public static String digest(String password) {
		String hash = null;
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(password.getBytes("UTF-8"));
			byte raw[] = messageDigest.digest();
			hash = hexChecksum(raw);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hash;
	}

	/**
	 * Decode a query predicate in a readable format
	 * 
	 * @param json
	 *            the Query "URL"
	 * @return the query description
	 */
	public static String decodeQuery(JSONObject json) {
		StringBuffer queryDescription = new StringBuffer("\n\nQuery Description\n");
		try {
			queryDescription.append("Template: \"").append(json.get("template")).append("\"\n");
			if (json.has("where")) {
				JSONObject where = json.getJSONObject("where");
				if (where.has("study")) {
					JSONObject study = where.getJSONObject("study");
					JSONArray values = study.getJSONArray("values");
					queryDescription.append("Study: ");
					ArrayList<String> arr = new ArrayList<String>();
					for (int i=0; i < values.length(); i++) {
						arr.add("\"" + values.getString(i) + "\"");
					}
					queryDescription.append(join(arr, " OR ")).append("\n");
				}
				if (where.has("category")) {
					JSONObject category = where.getJSONObject("category");
					JSONArray values = category.getJSONArray("values");
					queryDescription.append("Category: ");
					ArrayList<String> arr = new ArrayList<String>();
					for (int i=0; i < values.length(); i++) {
						arr.add("\"" + values.getString(i) + "\"");
					}
					queryDescription.append(join(arr, " OR ")).append("\n");
				}
				if (where.has("score")) {
					JSONObject score = where.getJSONObject("score");
					JSONArray values = score.getJSONArray("values");
					queryDescription.append("Score between: [").append(values.getDouble(0)).append(", ").append(values.getDouble(1)).append("]\n");
				}
				if (where.has("description")) {
					JSONObject description = where.getJSONObject("description");
					JSONArray values = description.getJSONArray("values");
					queryDescription.append("Description like: \"").append(values.getString(0)).append("\"\n");
				}
				if (where.has("variable")) {
					JSONObject variable = where.getJSONObject("variable");
					JSONArray values = variable.getJSONArray("values");
					queryDescription.append("Variable like: \"").append(values.getString(0)).append("\"\n");
				}
			}
			if (json.has("offset")) {
				queryDescription.append("Offset: ").append(json.getInt("offset")).append("\n");
			}
			if (json.has("limit")) {
				queryDescription.append("Limit: \"").append(json.getInt("limit")).append("\n");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return queryDescription.toString();
	}

	/**
	 * Log a query
	 * 
	 * @param json
	 *            the Query "URL"
	 */
	public static void logQuery(JSONObject json) {
		try {
			//Logger.getLogger("AppLogging").info("Entry:\n"+json.toString());
			if (json.has("where")) {
				JSONObject where = json.getJSONObject("where");
				boolean selectedScore = where.has("score") ? true : false;
				if (selectedScore || where.has("study") || where.has("category") ||
						where.has("description") || where.has("variable") || where.has("visit")) {
					Logger.getLogger("AppLogging").info(json.toString());
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Decode a query predicate in HTML format
	 * 
	 * @param json
	 *            the Query "URL"
	 * @return the query description
	 */
	public static String decodeQueryHTML(JSONObject json) {
		StringBuffer queryDescription = new StringBuffer("");
		try {
			queryDescription.append("<div>Template: \"").append(json.get("template")).append("\"</div>\n");
			if (json.has("where")) {
				JSONObject where = json.getJSONObject("where");
				if (where.has("study")) {
					JSONObject study = where.getJSONObject("study");
					JSONArray values = study.getJSONArray("values");
					queryDescription.append("<div>Study: ");
					ArrayList<String> arr = new ArrayList<String>();
					for (int i=0; i < values.length(); i++) {
						arr.add("\"" + values.getString(i) + "\"");
					}
					queryDescription.append(join(arr, " OR ")).append("</div>\n");
				}
				if (where.has("category")) {
					JSONObject category = where.getJSONObject("category");
					JSONArray values = category.getJSONArray("values");
					queryDescription.append("<div>Category: ");
					ArrayList<String> arr = new ArrayList<String>();
					for (int i=0; i < values.length(); i++) {
						arr.add("\"" + values.getString(i) + "\"");
					}
					queryDescription.append(join(arr, " OR ")).append("</div>\n");
				}
				if (where.has("score")) {
					JSONObject score = where.getJSONObject("score");
					JSONArray values = score.getJSONArray("values");
					queryDescription.append("<div>Score between: [").append(values.getDouble(0)).append(", ").append(values.getDouble(1)).append("]</div>\n");
				}
				if (where.has("description")) {
					JSONObject description = where.getJSONObject("description");
					JSONArray values = description.getJSONArray("values");
					queryDescription.append("<div>Description like: \"").append(values.getString(0)).append("\"</div>\n");
				}
				if (where.has("variable")) {
					JSONObject variable = where.getJSONObject("variable");
					JSONArray values = variable.getJSONArray("values");
					queryDescription.append("<div>Variable like: \"").append(values.getString(0)).append("\"</div>\n");
				}
			}
			if (json.has("offset")) {
				queryDescription.append("<div>Offset: \"").append(json.getInt("offset")).append("\"</div>\n");
			}
			if (json.has("limit")) {
				queryDescription.append("<div>Limit: \"").append(json.getInt("limit")).append("\"</div>\n");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return queryDescription.toString();
	}

	/**
	 * Decode a query predicate in HTML format
	 * 
	 * @param json
	 *            the Query "URL"
	 * @return the query description
	 */
	public static String decodeExploreQueryHTML(JSONObject json) {
		StringBuffer queryDescription = new StringBuffer("");
		try {
			if (json.has("races")) {
				JSONArray values = json.getJSONArray("races");
				queryDescription.append("<div>Race/Ethnicity: ");
				ArrayList<String> arr = new ArrayList<String>();
				for (int i=0; i < values.length(); i++) {
					arr.add("\"" + values.getString(i) + "\"");
				}
				queryDescription.append(join(arr, " OR ")).append("</div>\n");
			}
			if (json.has("genders")) {
				JSONArray values = json.getJSONArray("genders");
				queryDescription.append("<div>Sex: ");
				ArrayList<String> arr = new ArrayList<String>();
				for (int i=0; i < values.length(); i++) {
					arr.add("\"" + values.getString(i) + "\"");
				}
				queryDescription.append(join(arr, " OR ")).append("</div>\n");
			}
			if (json.has("study_type")) {
				JSONArray values = json.getJSONArray("study_type");
				queryDescription.append("<div>Study Design: ");
				ArrayList<String> arr = new ArrayList<String>();
				for (int i=0; i < values.length(); i++) {
					arr.add("\"" + values.getString(i) + "\"");
				}
				queryDescription.append(join(arr, " OR ")).append("</div>\n");
			}
			if (json.has("genetic_type")) {
				JSONArray values = json.getJSONArray("genetic_type");
				queryDescription.append("<div>Genetic Data Type: ");
				ArrayList<String> arr = new ArrayList<String>();
				for (int i=0; i < values.length(); i++) {
					arr.add("\"" + values.getString(i) + "\"");
				}
				queryDescription.append(join(arr, " OR ")).append("</div>\n");
			}
			if (json.has("diseases")) {
				JSONArray values = json.getJSONArray("diseases");
				queryDescription.append("<div>Diseases: ");
				ArrayList<String> arr = new ArrayList<String>();
				for (int i=0; i < values.length(); i++) {
					arr.add("\"" + values.getString(i) + "\"");
				}
				queryDescription.append(join(arr, " OR ")).append("</div>\n");
			}
			if (json.has("platforms")) {
				JSONArray values = json.getJSONArray("platforms");
				queryDescription.append("<div>Platform: ");
				ArrayList<String> arr = new ArrayList<String>();
				for (int i=0; i < values.length(); i++) {
					arr.add("\"" + values.getString(i) + "\"");
				}
				queryDescription.append(join(arr, " OR ")).append("</div>\n");
			}
			if (json.has("ageFrom") || json.has("ageTo")) {
				queryDescription.append("<div>Age ");
				if (json.has("ageFrom")) {
					String value = json.getString("ageFrom");
					queryDescription.append("from " + value + " ");
				}
				if (json.has("ageTo")) {
					String value = json.getString("ageTo");
					queryDescription.append("to " + value);
				}
				queryDescription.append("</div>\n");
			}
			if (json.has("participants")) {
				String value = json.getString("participants");
				queryDescription.append("<div>#Subjects: " + value + "</div>\n");
			}
			if (json.has("description")) {
				String description = json.getString("description");
				queryDescription.append("<div>Description like: \"").append(description).append("\"</div>\n");
			}
			if (json.has("score")) {
				String value = json.getString("score");
				queryDescription.append("<div>Score: ").append(value).append("</div>\n");
			}
			if (json.has("keyword")) {
				String description = json.getString("keyword");
				queryDescription.append("<div>Description like: \"").append(description).append("\"</div>\n");
			}
			if (json.has("studies")) {
				JSONArray values = json.getJSONArray("studies");
				queryDescription.append("<div>Study: ");
				ArrayList<String> arr = new ArrayList<String>();
				for (int i=0; i < values.length(); i++) {
					arr.add("\"" + values.getString(i) + "\"");
				}
				queryDescription.append(join(arr, " OR ")).append("</div>\n");
			}
			if (json.has("offset")) {
				queryDescription.append("<div>Offset: \"").append(json.getInt("offset")).append("\"</div>\n");
			}
			if (json.has("limit")) {
				queryDescription.append("<div>Limit: \"").append(json.getInt("limit")).append("\"</div>\n");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return queryDescription.toString();
	}

	/**
	 * Compare 2 JSON objects
	 * 
	 * @param json1
	 *            the first object
	 * @param json2
	 *            the second object
	 * @return true if the objects are equal and false otherwise
	 */
	public static boolean compareJSONObjects(JSONObject json1, JSONObject json2) {
		boolean res = false;

		try {
			// check the number of keys
			String keys[] = JSONObject.getNames(json1);
			res = keys.length == JSONObject.getNames(json2).length;
			if (res) {
				// check the keys
				for (int i=0; i < keys.length && res; i++) {
					res = json2.has(keys[i]);
				}
			}
			if (res) {
				// check the objects
				for (int i=0; i < keys.length && res; i++) {
					res = compareObjects(json1.get(keys[i]), json2.get(keys[i]));
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Compare 2 objects
	 * 
	 * @param obj1
	 *            the first object
	 * @param obj2
	 *            the second object
	 * @return true if the objects are equal and false otherwise
	 */
	public static boolean compareObjects(Object obj1, Object obj2) {
		boolean res = false;
		if (obj1 instanceof JSONObject) {
			res = compareJSONObjects((JSONObject) obj1, (JSONObject) obj2);
		} else if (obj1 instanceof JSONArray) {
			res = compareJSONArrays((JSONArray) obj1, (JSONArray) obj2);
		} else {
			res = obj1.equals(obj2);
		}

		return res;
	}

	/**
	 * Compare 2 JSON arrays
	 * 
	 * @param json1
	 *            the first array
	 * @param json2
	 *            the second array
	 * @return true if the arrays are equal and false otherwise
	 */
	public static boolean compareJSONArrays(JSONArray json1, JSONArray json2) {
		// check array length
		boolean res = json1.length() == json2.length();
		if (res) {
			try {
				// convert the arrays to JSON objects and compare these objects
				res = compareJSONObjects(json1.toJSONObject(json1), json2.toJSONObject(json2));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return res;
	}

	/**
	 * Send an email
	 * 
	 * @param to
	 *            the email receiver
	 * @param subject
	 *            the email
	 * @param text
	 *            the email body
	 * @return the operation status
	 */
	public static boolean sendMessage(InternetAddress	addresses[], String subject, String text) {
		boolean ret = false;
		try {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			Session session = (Session) envCtx.lookup("mail/isi");
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("Pfindr@isi.edu"));
			msg.setRecipients(Message.RecipientType.TO, addresses);
			msg.setSubject(subject);
			msg.setSentDate(new Date());
			msg.setText(text);
			Transport.send(msg);
			ret = true;
		} catch (NamingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * Update the history
	 * 
	 * @param history
	 *            the array of saved queries
	 * @param json
	 *            the latest query
	 */
	public static void updateHistory(ArrayList<JSONObject> history, JSONObject json) {
		// check if the query already exists in the history
		boolean res = false;
		int index = -1;
		for (int i=0; i < history.size() && !res; i++) {
			res = compareJSONObjects(history.get(i), json);
			if (res) {
				index = i;
			}
		}

		if (!res) {
			// new query - insert it in the first position
			history.add(0, json);
		} else if (index != 0){
			// query already exists - move it in the first position marking such as LRU
			history.add(0, history.remove(index));
		}
	}
	
	public static String getOrderByClause(HttpServletRequest request, String table[]) {
		String orderBy = "";
		
		if (request.getParameter("iSortingCols") != null) {
			int iSortingCols = Integer.parseInt(request.getParameter("iSortingCols"));
			ArrayList<String> orderByColumns = new ArrayList<String>();
			for (int i=0; i < iSortingCols; i++) {
				int index = Integer.parseInt(request.getParameter("iSortCol_" + i));
				orderByColumns.add(table[index] + " " + request.getParameter("sSortDir_" + i));
			}
			orderBy = "order by " + join(orderByColumns, ", ");
		}
		return orderBy;
	}
	
	public static void sortArray(Object[] values) {
		Arrays.sort(values, 0, values.length, (new Utils()).new ObjectComparator());
	}

	/**
	 * Join the elements of the list
	 * 
	 * @param strings
	 *            the list of elements
	 * @param delimiter
	 *            the delimiter
	 * @return the join string of the list elements
	 */
	public static String joinStudies(String[] strings, String delimiter){
		if(strings==null || delimiter == null) {
			return "";
		}

		StringBuffer buf = new StringBuffer();
		boolean first = true;

		for (String value : strings) {
			if (first) {
				first = false;
			} else {
				buf.append(delimiter);
			}
			buf.append("study ~ '" + value + "'");
		}

		return buf.toString();
	}

	/**
	 * Class for sorting strings using by compare with ignore case
	 * 
	 */
	class StringComparator implements Comparator<String> {
		public int compare (String o1, String o2) {
			return o1.compareToIgnoreCase(o2);
		}

		public boolean equals(Object obj) {
			return (this.toString().compareToIgnoreCase(obj.toString()) == 0);
		}
	}
	
	/**
	 * Class for sorting strings using by compare with ignore case
	 * 
	 */
	class ObjectComparator implements Comparator<Object> {
		public int compare (Object o1, Object o2) {
			return o1.toString().compareToIgnoreCase(o2.toString());
		}

		public boolean equals(Object obj) {
			return (this.toString().compareToIgnoreCase(obj.toString()) == 0);
		}
	}
	

}
