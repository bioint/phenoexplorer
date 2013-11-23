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
<title>Explore Phenotypes</title>
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
		initExplorer('<%=session.getAttribute("user")%>');
	});
</script>

</head>
<body>
	<%@ include file="Top.jsp"%>
	<div>
		<a id="home" class="link-style banner-text" href="/pfindr/query?action=homepage">About</a>&nbsp;&nbsp; 
		<a id="savedQueries" class="link-style banner-text" href="/pfindr/explore?action=getQueries">Bookmarks</a>&nbsp;&nbsp; 
	</div>
	<div id="createPhenotypeDiv">
<div id="columns" class="info">
	<div class="left column">
		<p class="nospacing">PhenoExplorer contains information about 305 public dbGaP studies. 
			Of these, 280 provide data about the 136,962 phenotype variables in them. 
			We are actively expanding and improving the system.
		</p>
		<br/>
		<p class="nospacing">You may search by metadata characteristics, phenotype variable description, or both.
		</p>
		<br/>
		<p class="nospacing">To identify studies of interest, do the following:</p>
		<ul class="nospacing">
			<li>To limit the search to studies with given metadata characteristics (e.g., particular sex of subjects), 
				select the constraints. You can select multiple constraints in a box to get their disjunction.
			</li>
			<li>To search for studies containing specific phenotype variables, enter in the text box a word or phrase 
				that describes the phenotype.
			</li>
			<li>Click 'Submit'.</li>
		</ul>
	</div>
	<div class="right column">
			<p class="nospacing">PhenoExplorer will present a list of studies that satisfy any given constrains and contain 
				relevant phenotype variables. It will show the number of relevant variables in each study. 
				If no variable constraint was provides, all variables will be displayed.
			</p>
			<br/>
			<p class="nospacing">You may then,</p>
			<ul class="nospacing">
				<li>Select studies by clicking their check boxes.
					<br/>The relevant phenotype variables in the selected studies will be displayed. 
					You may constrain the accuracy of the phenotype matching procedure from the pop-up menu.
				</li>
				<li>Finally, you may click on the "More" button next to a variable to begin a new search for studies 
					containing variables related to that one.
				</li>
			</ul>
			<br/>
			<p class="nospacing">If you have any questions or comments, please contact us at <a class="mailto banner-text" href="mailto:info@phenoexplorer.org">info@phenoexplorer.org</a>.
			</p>
	</div>
</div>
<div class="clear"></div>
<h5><i>This version of PhenoExplorer searches dbGaP as of 06/03/2013, and uses term definitions from UMLS version 2012.AB.</i></h5>
<h4>Restrict search to studies with:</h4>
  <div id="studyDetailsDiv">
  	<table cellspacing="20">
  		<tr>
  			<td valign="top">
			  	<table>
			  		<tr>
			  			<th class="labelcell">Race/Ethnicity</th>
			  		</tr>
			  		<tr>
			  			<td>
						    <select id="studyRace" multiple="multiple" size="10" class="select-style">
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
						    <select id="studyGender" multiple="multiple" size="10" class="select-style">
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
			  			<th class="labelcell">Study Design</th>
			  		</tr>
			  		<tr>
			  			<td>
						    <select id="studyType" multiple="multiple" size="10" class="select-style">
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
						    <select id="geneticType" multiple="multiple" size="10" class="select-style">
								<option value="">Any</option>
							</select>
			  			</td>
			  		</tr>
			  	</table>
  			</td>
  		</tr>
  	</table>
	<table cellspacing="20">
  		<tr>
  			<td  valign="top">
			  	<table>
			  		<tr>
			  			<th class="labelcell">Disease</th>
			  		</tr>
			  		<tr>
			  			<td>
						    <select id="diseases" multiple="multiple" size="10" class="select-style">
								<option value="">Any</option>
							</select>
			  			</td>
			  		</tr>
			  	</table>
  			</td>
  			<td  valign="top">
			  	<table>
			  		<tr>
			  			<th class="labelcell">Platform</th>
			  		</tr>
			  		<tr id="platformSelect">
			  			<td>
						    <select id="studyPlatform" multiple="multiple" size="10" class="select-style">
								<option value="">Any</option>
							</select>
			  			</td>
			  		</tr>
			  	</table>
  			</td>
  		</tr>
  	</table>
  </div>
<div class="note">
<b>Please note: </b>
<ol>
<li>Many dbGaP studies do not provide some or all of the information listed above. To avoid significant omissions, PhenoExplorer 
does not interpret the absence of data as a negative result. So, for example, a search restricted to studies of a particular 
ethnic group will also be applied to studies where dbGaP contains no entry for the ethnic group investigated.
</li>
<li>Absence of information about diseases studied is particularly common. Searching for studies of a specific disease will 
therefore return also all studies where the disease studied is not recorded in dbGaP.
</li>
</ol>
</div>
<table>
  <tbody>
    <tr>
      <td><h4>with phenotypes like:</h4></td>
      <td><input type="text" name="description" id="description" size="64" maxlength="2048" /></td>
    </tr>
  </tbody>
</table>
<table>
  <tbody>
    <tr>
      <td><h4>and closeness of match to study variables&nbsp;</h4></td>
		<td>
			<select id="scorePhenotype" name="scorePhenotype" onchange="setScore()">
				<option value="0.9">Strict</option>
				<option value="0.5">Moderate</option>
				<option value="0.1" selected="selected">Relaxed</option>
			</select>		
		</td>
    </tr>
</table>
<div>If you're unsure, submit the query, view the results, then change the setting and resubmit.</div>
<br/>
<div id="spinnerwrapper" style="display:none;">
	<div id="ajaxSpinnerContainer">
		<img src="resources/ajax-loader.gif" id="ajaxSpinnerImage"
			title="Processing..." alt="Spinner" />
	</div>
</div>
<table>
  <tbody>
    <tr>
      <td><input type="button" name="createPhenotype" value="Submit" onclick="submitQuery()" /></td>
      <td><input type="button" name="clearPhenotype" value="Clear" onclick="clearAllPhenotypes()" /></td>
      <td><input type="button" id="bookmarkPhenotype" name="bookmarkPhenotype" value="Bookmark Query" onclick="bookmarkPhenotype()" style="display: none"/></td>
      <td><input type="button" id="saveStudies" name="saveStudies" value="Save Studies" onclick="saveStudies()" style="display: none"/></td>
      <td><input type="button" id="saveVariables"name="saveVariables" value="Save Variables" onclick="saveVariables()" style="display: none"/></td>
    </tr>
  </tbody>
</table>
<div id="study_wrapper_div" style="width:100%;display:none;">
  	<table id="study_wrapper_table"  style="width:100%;">
  		<tr>
  			<td valign="top">
			  	<div id="datatable_study_div" class="predicate_div ui-widget-content">
			  		<h3 class="ui-widget-header query-header">Studies
			  		</h3>
			  		<div id="new_study_div" class="predicate_wrapper predicate_scroll predicate_shell predicate_study ">
			  		</div>
			  	</div>
  			</td>
  		</tr>
  	</table>
</div>
</div>
<br/><br/>
<div id="show_score_div" style="display:none;">
	<input type="button" id="showHideMatchScores" name="showHideMatchScores" value="Show Scores" onclick="toggleMatchScores()" />
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
		<h3 id="resultingPhenotypesH3" class="ui-widget-header query-header">Phenotypes</h3>

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
				<div id="TipBox"
					style="display: none; position: absolute; font-size: 12px; font-weight: bold; font-family: verdana; border: #72B0E6 solid 1px; padding: 15px; color: #1A80DB; background-color: #FFFFFF; max-width:1000px;">
				</div> <!-- END FLY OVER DIV --> 

</body>
</html>
