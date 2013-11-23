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
 * File for defining phenotypes
 * 
 * @author Serban Voinea
 * 
-->

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Define Phenotypes</title>
		<style type="text/css" title="currentStyle">
			@import "resources/css/demo_page.css";
			@import "resources/css/demo_table.css";
			.FixedHeader_Cloned th { background-color: #F0FFFF;}
		</style>
<link rel="stylesheet" type="text/css" href="resources/jquery-ui.css">
<link rel="stylesheet" type="text/css" href="resources/pfindr.css">
<script type="text/javascript" src="resources/jquery.js"></script>
<script type="text/javascript" src="resources/jquery-ui.js"></script>
<script type="text/javascript" language="javascript" src="resources/jquery.dataTables.js"></script>
<script type="text/javascript" src="resources/FixedHeader.js"></script>
<script type="text/javascript" src="resources/pfindr.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		initDefiner('<%=session.getAttribute("user")%>');
	});
</script>

</head>
<body>
	<%@ include file="Top.jsp"%>
	<div>
		<a id="home" class="link-style banner-text" href="/pfindr/query?action=homepage">About</a>&nbsp;&nbsp; 
		<a id="queryTool" class="link-style banner-text" href="/pfindr/query">Query Tool</a> &nbsp;&nbsp;
	</div>
	<div id="createPhenotypeDiv">
	<h2>Create a Phenotype</h2>
<p class="info">PhenoExplorer currently only provides information about the 60,000 phenotype variables in 7 CARe consortium studies about cardiovascular diseases. Searches for variables in other studies will therefore not return any results yet.</p>
<p class="info">We are actively working on expanding the system.</p>
	<fieldset>
		<legend>Phenotype</legend>
		<table>
			<tbody><tr>
			<td>
				<label>Name:</label>
				<input type="text" id="phenotype" name="phenotype" /> 
			</td>
			<td>
				<label>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Description:</label>
				<input type="text" name="description" id="description" size="64" maxlength="2048" /> 
			</td>
			<td>
				<div id="score_div" class="predicate_div ui-widget-content">
					<h3 id="score_header" class="ui-widget-header query-header">
					<label id="score_label">Score Range: 0 - 1</label>
					</h3>
					<div id="slider-range" class="score_slider"></div>
				</div> 
			</td>
			<td>
				<input type="button" name="createPhenotype" value="Submit" onclick="createPhenotype(true)" />
			</td>
			</tr></tbody>
		</table>
	</fieldset>
		<br />
		<br />
		<br />
	</div>
	<div id="categoryMappingsDiv" style="display: none;" >
		<h2 id="categoryMappingsH2" >Phenotype</h2>
		<br />
		<br />
		<br />
		<div id="topCategoryMappings"  style="display: none;" >
			<input type="button" id="topCancelCategoryMappingsButton" name="topCancelCategoryMappingsButton" value="Back" onclick="cancelSelectMappings()" />
			<input type="button" id="topAddCategoryMappingsButton" name="topAddCategoryMappingsButton" value="Add" onclick="addSelectedMappings(true)" />
		</div>
	</div>
	<div id="saveMappings"  style="display: none;" >
		<input type="button" id="addMappingsButton" name="addMappingsButton" value="Add" onclick="addSelectedMappings()" />
		<input type="button" id="reviewMappingsButton" name="reviewMappingsButton" value="Review" onclick="reviewSelectedMappings()" style="display: none;" />
		<input type="button" id="cancelReviewMappingsButton" name="cancelReviewMappingsButton" value="Back" onclick="cancelSelectMappings()" style="display: none;" />
		<input type="button" id="exportMappingsButton" name="exportMappingsButton" value="Export" onclick="exportSelectedMappings()" style="display: none;" />
		<input type="button" id="clearShoppingCartButton" name="clearShoppingCartButton" value="Clear" onclick="clearShoppingCart()" style="display: none;" />
	</div>
	<!-- RESIZABLE DIV -->
	<div id="define_div" class="ui-widget-content query_results" style="display: none;">
		<h3 id="resultingPhenotypesH3" class="ui-widget-header query-header">Resulting Phenotypes</h3>

		<!-- TREE DIV -->
		<div id="dt_example">
				<div id="container">
					<div id="demo">
						<table cellpadding="0" cellspacing="0" border="0" class="display"
							id="example">
							<thead>
								<tr>
									<th></th>
								</tr>
							</thead>
							<tfoot>
								<tr>
									<th></th>
								</tr>
							</tfoot>
							<tbody>
								<tr>
									<td></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
</div>
<!-- END TREE DIV -->

	</div>
	<!-- END RESIZABLE DIV -->
	<div id="bottomCategoryMappings"  style="display: none;" >
		<input type="button" id="bottomCancelCategoryMappingsButton" name="bottomCancelCategoryMappingsButton" value="Back" onclick="cancelSelectMappings()" />
		<input type="button" id="bottomAddCategoryMappingsButton" name="bottomAddCategoryMappingsButton" value="Add" onclick="addSelectedMappings(true)" />
	</div>

</body>
</html>
