<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="org.json.JSONArray,org.json.JSONException"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<!-- 
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
 *
-->

<!-- 
 * File for Manage Users Page
 * 
 * @author Serban Voinea
 * 
-->

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Manage roles</title>
<link rel="stylesheet" type="text/css" href="resources/pfindr.css">
</head>
<body>
	<%@ include file="Top.jsp"%>
	<div>
		<a id="home" class="link-style banner-text" href="/pfindr/query?action=homepage">About</a>&nbsp;&nbsp; 
		<a id="managePhenotypes" class="link-style banner-text" href="/pfindr/explore">Phenotype Explorer</a> &nbsp;&nbsp;
	</div>

	<h2>Create an User</h2>

	<form enctype="application/x-www-form-urlencoded" action="/pfindr/role"
		method="POST">
		<input type="text" name="role" /> <input type="hidden" name="action"
			value="create" /> <input type="submit" value="Create" />
	</form>

	<h2>Existing Users</h2>

	<%
		JSONArray rows = (JSONArray) request.getAttribute("roles");
		try {
			for (int i = 0; i < rows.length(); i++) {
				JSONArray row = rows.getJSONArray(i);
				String role = row.getString(0);
				boolean roleAdmin = row.getBoolean(1);
				boolean roleDefiner = row.getBoolean(2);
				String admin = (roleAdmin ? "Reset" : "Set") + " admin role";
				String definer = (roleDefiner ? "Reset" : "Set") + " definer role";
	%>
	<fieldset>
		<legend><%=role%></legend>
		<table>
			<tr>
				<td>
					<form enctype="application/x-www-form-urlencoded"
						action="/pfindr/role?action=delete&role=<%=role%>" method="POST">
						<input type="submit" value="Delete role" />
					</form>
				</td>
				<td>
					<form enctype="application/x-www-form-urlencoded"
						action="/pfindr/role?action=reset&role=<%=role%>" method="POST">
						<div>
							<input type="submit" value="Reset password" />
						</div>

					</form>
				</td>
				<td>
					<form enctype="application/x-www-form-urlencoded"
						action="/pfindr/role?action=admin&role=<%=role%>" method="POST">
						<div>
							<input type="submit" value="<%=admin%>" />
						</div>

					</form>
				</td>
				<td>
					<form enctype="application/x-www-form-urlencoded"
						action="/pfindr/role?action=definer&role=<%=role%>" method="POST">
						<div>
							<input type="submit" value="<%=definer%>" />
						</div>

					</form>
				</td>
			</tr>
		</table>
	</fieldset>
	<%
		}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	%>

</body>
</html>