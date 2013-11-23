<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="org.json.JSONArray,org.json.JSONException"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.net.URLEncoder"%>
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
 * File for Displaying the Saved Queries Page
 * 
 * @author Serban Voinea
 * 
-->

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Bookmarks</title>
		<style type="text/css" title="currentStyle">
			@import "resources/css/demo_page.css";
			@import "resources/css/demo_table.css";
			.FixedHeader_Cloned th { background-color: #F0FFFF;}
		</style>
<link rel="stylesheet" type="text/css" href="resources/jquery-ui.css">
<link rel="stylesheet" href="resources/jquery.treeview.css">
<link rel="stylesheet" type="text/css" href="resources/pfindr.css">
<script type="text/javascript" src="resources/jquery.js"></script>
<script type="text/javascript" src="resources/jquery-ui.js"></script>
<script type="text/javascript" src="resources/jquery.treeview.js"></script>
<script type="text/javascript" language="javascript" src="resources/jquery.dataTables.js"></script>
<script type="text/javascript" src="resources/FixedHeader.js"></script>
<script type="text/javascript" src="resources/pfindr.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		initQueryList();
	});
</script>
</head>
<body>
	<%@ include file="Top.jsp"%>
	<div>
		<a id="home" class="link-style banner-text" href="/pfindr/query?action=homepage">About</a>&nbsp;&nbsp; 
		<a id="queryTool" class="link-style banner-text" href="/pfindr/query">Query Tool</a> &nbsp;&nbsp;
	</div>

	<div id="dt_example">
		<div id="container">
			<div id="demo">
				<table cellpadding="0" cellspacing="0" border="0" class="display"
					id="example">
					<thead>
						<tr>
							<th>Name</th>
							<th>Description</th>
							<th>Created</th>
						</tr>
					</thead>
					<tfoot>
						<tr>
							<th></th>
							<th></th>
							<th></th>
						</tr>
					</tfoot>
					<tbody>
					<% ArrayList<HashMap<String,String>> bookmarks = (ArrayList<HashMap<String,String>>) request.getAttribute("bookmarks");
						for (int i=0; i < bookmarks.size(); i++) { 
							HashMap<String,String> item = bookmarks.get(i); %>
						<tr>
							<% String name =  item.get("name");
							   String encodedName = URLEncoder.encode(name, "UTF-8");
							%>
							<td align="center"><a href="/pfindr/query?action=getBookmark&amp;qid=<%=encodedName%>"><%=name%></a></td>
							<td><%=item.get("description")%></td>
							<td align="center"><%=item.get("created")%></td>
						</tr>
					<% } %>
					</tbody>
				</table>
			</div>
		</div>
	</div>

</body>
</html>