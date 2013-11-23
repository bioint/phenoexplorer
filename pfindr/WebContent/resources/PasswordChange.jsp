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
 * File for Password Change
 * 
 * @author Serban Voinea
 * 
-->

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Password Change</title>
<link rel="stylesheet" type="text/css" href="resources/pfindr.css">
</head>
<body>
	<%@ include file="Top.jsp"%>
	<div>
		<a id="home" class="link-style banner-text" href="/pfindr/query?action=homepage">About</a>&nbsp;&nbsp; 
		<a id="managePhenotypes" class="link-style banner-text" href="/pfindr/explore">Phenotype Explorer</a> &nbsp;&nbsp;
	</div>
	<h2>Change Password</h2>
	<%
		Object isError = request.getAttribute("error");
		Object isSuccess = request.getAttribute("success");
		boolean mustchange = (Boolean) session.getAttribute("mustchange");
		if (isSuccess != null) {
	%>
	<p class="success">
		<b>The password was successfully changed.</b>
	</p>
	<a id="continue" href="/pfindr/explore">Continue </a>
	<%
		} else {
			if (isError != null) {
	%>
	<p class="error">
		<b>Incorrect password(s).</b>
	</p>
	<%
		}
	%>
	<div>
		<fieldset>
			<legend>
				User
				<%=user%></legend>
			<form enctype="application/x-www-form-urlencoded"
				action="/pfindr/role" method="post">
				<input type="hidden" name="role" value="<%=user%>" /> <input
					type="hidden" name="action" value="change" />
				<table>
					<tr>
						<td>Old Password:</td>
						<td><input type="password" name="oldpassword" /></td>
					</tr>
					<tr>
						<td>New Password:</td>
						<td><input type="password" name="newpassword1" /></td>
					</tr>
					<tr>
						<td>New Password (confirm):</td>
						<td><input type="password" name="newpassword2" /></td>
					</tr>
					<tr>
						<td><input type="submit" value="Change Password" /></td>
					</tr>
				</table>
			</form>
			<%
				if (!mustchange) {
			%>
				<form enctype="application/x-www-form-urlencoded"
					action="/pfindr/explore" method="get">
					<input type="submit" value="Cancel" />
				</form>
			<%
				}
			%>
		</fieldset>
	</div>
	<%
		}
	%>
</body>
</html>