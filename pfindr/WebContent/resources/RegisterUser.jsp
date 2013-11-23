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
 * File for Registering New User Page
 * 
 * @author Serban Voinea
 * 
-->

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Register User</title>
<style type="text/css">
      body {
        font-family: Helvetica, sans-serif;
        color: #000000;
        font-size: 12px;
        border: none;
        background-color: transparent;
      }
</style>
<script type="text/javascript" src="https://www.google.com/recaptcha/api/js/recaptcha_ajax.js"></script>
<link rel="stylesheet" type="text/css" href="resources/pfindr.css">
<script type="text/javascript" src="resources/jquery.js"></script>
<script type="text/javascript" src="resources/pfindr.js"></script>
<script type="text/javascript">
$(document).ready(function() {
	showRecaptcha();
	});
</script>
</head>
<body>
	<%@ include file="TopNewUser.jsp"%>

	<h2>Register New User</h2>

	<div id="registerForm">
		<p>Fields marked with <b><sup>*</sup></b> are mandatory.</p>
		<br/>
		<table>
		<tr>
		<td align="right">First Name:<sup>*</sup></td>
		<td><input type="text" id="firstName" name="firstName" /></td> 
		</tr>
		<tr>
		<td align="right">Last Name:<sup>*</sup></td>
		<td><input type="text" id="lastName" name="lastName" /></td>
		</tr>
		<tr>
		<td align="right">Institution:</td>
		<td><input type="text" id="institution" name="institution" /></td>
		</tr>
		<tr>
		<td align="right">Email:<sup>*</sup></td>
		<td><input type="text" id="email" name="email" /></td> 
		</tr>
		</table>
	</div>
	<div id="captchadiv"></div>
	<p id="registerP" >
	<input type="button" value="Register" onclick="submitRecaptcha()"/>
	<br/>
	<br/>
	After clicking the <b>Register</b> button, an email will be sent to you with the credentials to Login.
	<br/>
	Your email address will not be used for any other purpose.
	</p>
	<div id="success" style="display:none;">
	<p class="success">
	The account was successfully created.
	</p>
	<br/>
	An email was sent to you with the credentials to <a href="/pfindr">Login</a>.
	</div>
	<div id="errorDiv" style="display:none;" >
	<p class="error" id="errorP"></p>
	<br/>
	<br/>
	</div>

</body>
</html>