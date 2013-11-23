<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
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
 * File for Password Reset
 * 
 * @author Serban Voinea
 * 
-->

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Reset Password</title>
<link rel="stylesheet" type="text/css" href="resources/pfindr.css">
</head>
<body>
	<%@ include file="Top.jsp"%>
	<div>
		<a id="home" class="link-style banner-text" href="/pfindr/query?action=homepage">About</a>&nbsp;&nbsp; 
		<a id="queryTool" class="link-style banner-text" href="/pfindr/query">Query Tool</a> &nbsp;&nbsp;
		<%
			if (isDefiner) {
		%>
		<a id="managePhenotypes" class="link-style banner-text" href="/pfindr/define">Phenotype Mapping</a> &nbsp;&nbsp;
		<%
			}
		%>
	</div>
	<h2>Password Reset</h2>
	<br />
	<%
		String role = request.getParameter("role").trim();
		String tempPassword = (String) request.getAttribute("tempPassword");
	%>

	<div>
		The password for "<%=role%>" has been reset.
		<ul>

			<li>New temporary password: "<%=tempPassword%>"</li>
			<li>User must change password on next login.</li>
		</ul>
		<a href="/pfindr/role">Continue</a>
	</div>
</body>
</html>