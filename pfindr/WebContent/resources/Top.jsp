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
 * File for Top Page
 * 
 * @author Serban Voinea
 * 
-->

<%
	String user = (String) session.getAttribute("user");
	boolean isAdmin = (Boolean) session.getAttribute("isadmin");
	boolean isDefiner = (Boolean) session.getAttribute("isdefiner");
%>
<!-- LOGO DIV -->
<table class="topmargin">
	<tr>
		<td class="logo">
			<div>
				<img alt="NHLBI logo" title="NHLBI" src="images/HL-logo.jpg"
					height="75" /> <img alt="USC logo" title="USC"
					src="images/usc-primaryshieldwordmark.png" height="75" /> <img
					alt="UNC logo" title="UNC" src="images/UNC blue_spot542.jpg" width="215"
					height="75" />
			</div>
		</td>
		<td class="authninfo">
			<table>
				<tr>
					<td>
						<a id="welcome" class="link-style banner-text" href="#">Welcome <%=user%>!</a>
					</td>
				</tr>
				<tr>
					<td>
						<a id="changePassword" class="link-style banner-text" href="/pfindr/role?action=change&amp;role=<%=user%>">Change Password</a>
					</td>
				</tr>
				<tr>
					<td>
						<a id="pfindrLogout" class="link-style banner-text" href="/pfindr/logout">Logout</a>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
<!-- END LOGO DIV -->

<h1>PhenoExplorer</h1>
