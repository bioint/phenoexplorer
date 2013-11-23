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
 * File for Query Tool Page
 * 
 * @author Serban Voinea
 * 
-->

<html>
<head>
<meta content="text/html;charset=ISO-8859-1" http-equiv="Content-Type">
<title>PhenoExplorer</title>
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
		initPFINDR('<%=session.getAttribute("user")%>', <%=session.getAttribute("query") != null%>);
	});
</script>

</head>
<body>
	<%@ include file="Top.jsp"%>
	<div>
		<a id="home" class="link-style banner-text" href="/pfindr/query?action=homepage">About</a>&nbsp;&nbsp; 
		<a id="savedQueries" class="link-style banner-text" href="/pfindr/query?action=getQueries">Bookmarks</a>&nbsp;&nbsp; 
		<a id="historyQueries" class="link-style banner-text" href="/pfindr/query?action=history">History</a>&nbsp;&nbsp; 
	</div>
<div id="study_wrapper_div" style="width:100%;">
  	<table id="study_wrapper_table"  style="width:100%;">
  		<tr>
  			<td valign="top">
			  	<div id="datatable_study_div" class="predicate_div ui-widget-content">
			  		<h3 class="ui-widget-header query-header">Studies<img id="new_study_img" border="0px" src="resources/images/info_small.png" alt="Info"/>
			  		</h3>
			  		<div id="new_study_div" class="predicate_wrapper predicate_scroll predicate_shell predicate_study ">
			  		</div>
			  	</div>
  			</td>
  		</tr>
  	</table>
</div>

<br/>
<br/>
<fieldset class="fieldset">
  <legend class="ui-widget-header"><a id="studyDetailsA" href="javascript:toggleStudyDetails()" >-</a>&nbsp;Filter Studies
  	<img id="filter_studies_img" border="0px" src="resources/images/info_small.png" alt="Info"/>
  </legend>
  <div id="studyDetailsDiv" style="display:none;">
  	<table cellspacing="20">
  		<tr>
  			<td valign="top">
			  	<table>
			  		<tr>
			  			<th class="labelcell">Race/Ethnicity</th>
			  		</tr>
			  		<tr>
			  			<td>
						    <select id="studyRace" multiple="multiple" size="12" class="select-style">
								<option value="">Any</option>
							</select>
			  			</td>
			  		</tr>
			  	</table>
  			</td>
  			<td valign="top">
			  	<table>
			  		<tr>
			  			<th class="labelcell">Sex</th>
			  		</tr>
			  		<tr>
			  			<td align="center">
						    <select id="studyGender" multiple="multiple" size="12" class="select-style">
								<option value="">Any</option>
							</select>
			  			</td>
			  		</tr>
			  		<tr>
			  			<td>
			  				<table>
			  					<tr>
			  						<th class="labelcell">Age</th>
			  					</tr>
			  					<tr>
			  						<td  class="select-style">
			  							<table>
			  								<tr>
									  			<td class="labelcell" align="right">
									  				<label >From:</label>
									  			</td>
						  						<td>
						  							<input id="ageFrom" size="2" type="text" />
						  						</td>
						  						<td class="labelcell">
						  							<label>To:</label>
						  						</td>
						  						<td>
						  							<input id="ageTo" size="2" type="text" />
						  						</td>
			  								</tr>
			  							</table>
			  						</td>
			  					</tr>
			  				</table>
			  			</td>
			  		</tr>
			  		<tr>
			  			<td>
			  				<table>
			  					<tr>
			  						<th class="labelcell">#Subjects</th>
			  					</tr>
			  					<tr>
			  						<td  class="select-style" align="center">
			  							<table>
			  								<tr>
									  			<td class="labelcell" align="right">
									  				<label >Min:</label>
									  			</td>
						  						<td>
						  							<input id="participantsFrom" size="2" type="text" />
						  						</td>
						  						<!-- 
						  						<td class="labelcell">
						  							<label>To:</label>
						  						</td>
						  						<td>
						  							<input id="participantsTo" size="2" type="text" />
						  						</td>
						  						-->
			  								</tr>
			  							</table>
			  						</td>
			  					</tr>
			  				</table>
			  			</td>
			  		</tr>
			  	</table>
  			</td>
  			<td  valign="top">
			  	<table>
			  		<tr>
			  			<th class="labelcell">Platform</th>
			  		</tr>
			  		<tr>
			  			<td>
						    <select id="studyPlatform" multiple="multiple" size="12" class="select-style">
								<option value="">Any</option>
							</select>
			  			</td>
			  		</tr>
			  	</table>
  			</td>
  			<td  valign="top">
			  	<table>
			  		<tr>
			  			<th class="labelcell">Study Design</th>
			  		</tr>
			  		<tr>
			  			<td>
						    <select id="studyType" multiple="multiple" size="12" class="select-style">
								<option value="">Any</option>
							</select>
			  			</td>
			  		</tr>
			  	</table>
  			</td>
  			<td  valign="top">
			  	<table>
			  		<tr>
			  			<th class="labelcell">Genetic Data Type</th>
			  		</tr>
			  		<tr>
			  			<td>
						    <select id="geneticType" multiple="multiple" size="12" class="select-style">
								<option value="">Any</option>
							</select>
			  			</td>
			  		</tr>
			  	</table>
  			</td>
  		</tr>
  	</table>
  	<br/>
  	<input type="button" value="Apply Filters" onclick="applyStudiesFilter()" />
  	<input type="button" value="Clear Filters" onclick="clearAll()" />
  </div>
  <br/>
</fieldset>
<p class="info">PhenoExplorer currently only provides information about the 60,000 phenotype variables in 7 CARe consortium studies about cardiovascular diseases. Searches for variables in other studies will therefore not return any results yet.</p>
<p class="info">We are actively working on expanding the system.</p>
<div id="layout_div" style="display:none;">
  	<table id="new_query_div" cellspacing="10">
  		<tr>
  			<td valign="top">
				<!-- CATEGORY DIV -->
 				<div id="new_category_div" class="predicate_div ui-widget-content">
					<h3 id="category_header" class="ui-widget-header query-header">
						<input class="float_left" type="checkbox" />Categories<img id="new_category_img" border="0px" src="resources/images/info_small.png" alt="Info"/>
						<input style="float:right" value="" title="Search Category" type="text" id="category_input" name="category_input" />
					</h3>
					<div class="predicate_wrapper predicate_scroll predicate_shell predicate_category">
						<table id="new_category_table" width="100%" >
							<tr>
								<td></td>
							</tr>
						</table>
					</div>
				</div> <!-- END CATEGORY DIV -->
  			</td>
			<td valign="top" id="search_column" >
				<table cellpadding="0">
					<tbody>
						<tr>
							<td valign="top">
								<!-- KEYWORD DIV -->
								<div id="keyword_div" class="predicate_div ui-widget-content">
									<h3 class="ui-widget-header query-header">Pattern Search<img id="keyword_img" border="0px" src="resources/images/info_small.png" alt="Info"/>
									</h3>
									<input type="radio" name="keywordSelect" value="description"
										onclick="applyKeyword()" checked="checked" />description 
									<input type="radio" name="keywordSelect" value="variable"
									onclick="applyKeyword()" />variable
									<input type="radio" name="keywordSelect" value="visit"
									onclick="applyKeyword()" />visit
									<div class="predicate_wrapper">
										<table cellspacing="0" cellpadding="0">
											<tbody>
												<tr>
													<td><input class="query_input" value="" title="Search"
														size="41" type="text" id="query_input" name="query_input"
														maxlength="2048" />
													</td>
													<td>
														<input type="checkbox" id="query_regex" />Regular expression
													</td>
												</tr>
												<tr>
													<td><span id="query_span">&nbsp;</span>
													</td>
												</tr>
											</tbody>
										</table>
									</div>
								</div> <!-- END KEYWORD DIV -->
							</td>
						</tr>
						<tr>
							<td valign="bottom">
								<!-- SCORE DIV -->
								<div id="score_div" class="predicate_div ui-widget-content">
									<h3 id="score_header" class="ui-widget-header query-header">
									<label id="score_label">Score Range: 0.5 - 1</label><img id="score_img" border="0px" src="resources/images/info_small.png" alt="Info"/>
									</h3>
									<div class="predicate_wrapper">
										<table class="score">
											<tbody>
												<tr>
													<td>
														<div id="slider-range" class="score_slider"></div></td>
												</tr>
											</tbody>
										</table>
									</div>
								</div> <!-- END SCORE DIV -->
							</td>
						</tr>
					</tbody>
				</table>
			</td>
  		</tr>
  	</table>
  	<br/>
  	<table>
		<tr>
			<td>
				<input type="button" name="submitQuery" value="Submit" onclick="applySubmit()" />
			</td>
		</tr>
  	</table>
	<br />
	<br />
	<br />
	<!-- QUERY RESULTS DIV -->
	<div id="query_results" class="query_results">

					<!-- NAVIGATION DIV -->
					<div id="pf_tree_navigation">

						<!-- QUERY TOP TABLE -->
						<table>
							<tbody>
								<tr>
									<td>
										<!-- GLOBAL MENU DIV -->
										<div class="topnav global_menu">
											<table>
												<tbody>
													<tr>
														<td class="actions"><span>Actions<img alt="arrow" src="resources/images/arrow_down.gif" /></span>
														</td>
													</tr>
												</tbody>
											</table>
											<ul id="GlobalMenu" class="subnav">
												<li class="item" id="bookmarkQuery" onmousedown="bookmarkQuery();">Bookmark Query...</li>
												<li class="item" id="clearAll" onmousedown="clearAll();">Clear</li>
												<li class="item expandCollapseAll" id="collapseAll" onmousedown="collapseAll();">Collapse All</li>
												<li class="item" id="editTemplate" onmousedown="editTemplate();">Edit template...</li>
												<li class="item expandCollapseAll" id="expandAll" onmousedown="expandAll();">Expand All</li>
												<li class="item" id="exportQuery" onmousedown="exportQuery();">Export Query...</li>
												<li class="item" id="importQuery" onmousedown="importQuery();">Import Query...</li>
												<li class="item saveSelectedResults" id="markSelectedResultsAsCorrect" onmousedown="saveSelectedResults('markAsCorrect');" style="display: none;">Mark selected row(s) as correct...</li>
												<li class="item saveSelectedResults" id="markSelectedResultsAsInCorrect" onmousedown="saveSelectedResults('markAsIncorrect');" style="display: none;">Mark selected row(s) as incorrect...</li>
												<li class="item" id="newTemplate" onmousedown="newTemplate();">New template...</li>
												<li class="item saveSelectedResults" id="saveSelectedResults" onmousedown="saveSelectedResults('saveSelectedResults');" style="display: none;">Download selected result(s)...</li>
												<li class="item" id="savePhenotypes" onmousedown="savePhenotypes();">Download all results...</li>
											</ul>
										</div> <!-- END GLOBAL MENU DIV --></td>

									<td>
										<!-- PAGE DIV -->
										<div class="page_navigation">
											<span id="ViewResults">&nbsp;</span> <img id="pagePrevious"
												src="resources/images/back.jpg" alt="Previous"
												class="margin" onclick="setPreviousPage();"> <label
												id="resultsRange"></label> <img id="pageNext"
												src="resources/images/forward.jpg" alt="Next" class="margin"
												onclick="setNextPage();"> <span id="totalResults"></span>
											<label id="perPageWith">&nbsp;with </label><select id="previewLimit" name="previewLimit"
												onchange="updatePreviewLimit()">
												<option value="10">10</option>
												<option value="25">25</option>
												<option value="50">50</option>
												<option value="100">100</option>
												<option value="500">500</option>
												<option value="1000">1000</option>
												<option value="5000">5000</option>
											</select><label id="perPage">&nbsp;per page organized by </label><select id="selectTemplate"
												name="selectTemplate" onchange="selectTemplate()">
												<option value="default">default</option>
											</select>
										</div> <!-- END PAGE DIV --></td>

								</tr>
							</tbody>
						</table>
						<!-- END QUERY TOP TABLE -->

						<!-- RESIZABLE DIV -->
						<div id="pf_tree_div" class="ui-widget-content query_results">
							<h3 class="ui-widget-header query-header">Resulting Mappings</h3>

							<!-- TREE DIV -->
							<div class="tree_view">

								<ul id="navigation">
									<li></li>
								</ul>
							</div>
							<div id="dt_example" style="display: none;">
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

					</div>
					<!-- END NAVIGATION DIV -->

				</div> <!-- END QUERY RESULTS DIV --> <!-- FLY OVER DIV -->
				<div id="TipBox"
					style="display: none; position: absolute; font-size: 12px; font-weight: bold; font-family: verdana; border: #72B0E6 solid 1px; padding: 15px; color: #1A80DB; background-color: #FFFFFF; max-width:500px;">
				</div> <!-- END FLY OVER DIV --> 
				<!-- GET HELP DIV -->
				<div id="get_help_div">
					<ul id="get_help_ul">
						<li><b>Studies/Categories.</b>
						Select one or more studies/categories of interest. The logical disjunction operator applies. Once the mouse is over the Studies/Categories area, it highlights it and populates it with the available studies/categories to be selected. Each time the user selects some studies/categories of interest, the system automatically filters the categories/studies dimension and updates the resulting mappings. Once the mouse leaves the Studies/Categories area, its list is updated to the selected studies/categories.
						</li>
						<li><b>Keywords.</b>
						Check the domain (description or variable) where the keyword applies. During a text typing, up to five hints might be display. Once a hint is selected, or the Enter key is pressed inside the text area, or the Go button is clicked, the system automatically filters the categories and studies dimensions and updates the resulting mappings with entries having the keyword text in the specified domain.
						</li>
						<li><b>Score Range.</b>
						Use the slider to select a range of scores of interest and click the Apply button. The system automatically filters the categories and studies dimensions and updates the resulting mappings.
						</li>
					</ul>
				</div> <!-- END GET HELP DIV --> 
				<!-- NEW TEMPLATE DIV -->
				<div id="new_template_div">
					Template name: <input type="text" id="template_name" />
					<p />
					<ul id="new_template_ul" class="tree_border">
						<li></li>
					</ul>
				</div> <!-- END NEW TEMPLATE DIV --> 
				<!-- EDIT TEMPLATE DIV -->
				<div id="edit_template_div">
					Available templates: <select id="selectEditTemplate"
						name="selectEditTemplate" onchange="selectEditTemplate()">
						<option value="default">default</option>
					</select>
					<p />
					<ul id="edit_template_ul" class="tree_border">
						<li></li>
					</ul>
				</div> <!-- END EDIT TEMPLATE DIV -->
				<div id="import_query_div">
					<form id="import_query_id" action="/pfindr/query?predicate=import" enctype="multipart/form-data" method="post">
						<br /><br />
						<input type="file" name="queryfile" size="40" >
					</form>
				</div> <!-- END IMPORT QUERY DIV -->
</div>
<!-- END LAYOUT DIV -->
</body>
</html>