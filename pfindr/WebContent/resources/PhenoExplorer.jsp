<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>PhenoExplorer</title>
<link rel="stylesheet" type="text/css" href="resources/pfindr.css">
</head>
<body>
	<%@ include file="Top.jsp"%>
	<%
		if (isAdmin) {
	%>
	<a id="manageRoles" class="link-style banner-text" href="/pfindr/role">Manage Users</a> &nbsp;&nbsp;
	<%
		}
	%>
	<a style="display:none;" id="queryTool" class="link-style banner-text" href="/pfindr/query">Query Tool</a> &nbsp;&nbsp;
	<%
		if (isDefiner) {
	%>
	<a style="display:none;" id="managePhenotypes" class="link-style banner-text" href="/pfindr/define">Phenotype Mapping</a> &nbsp;&nbsp;
	<a id="explorePhenotypes" class="link-style banner-text" href="/pfindr/explore">Phenotype Explorer</a> &nbsp;&nbsp;
	<%
		}
	%>
	<a style="display:none;" id="pfindrHelp" class="link-style banner-text" target="_newtab2" href="resources/help/help.html">Tutorial</a> &nbsp;&nbsp; 
	<div id="main" class='home-text'>
		<span class="heading">&nbsp;&nbsp;</span>
		<div class="tabs-horizontal-line"></div>
		<p><b>PhenoExplorer</b> is an effort to help biomedical researchers search dbGaP for studies that contain phenotype variables 
			of interest to them. The importation of data from multiple studies ends up creating a repository with many different 
			representations of semantically related variables. PhenoExplorer improves the utility of the repository and supports 
			reuse of its data by identifying phenotypes that are equivalent or related and simplifying the search for variables of 
			interest.
		</p>
	</div>
	<ul class='noindent'>
		<li>
			<div class='h1'>The Challenge</div>
			<ul>
				<li>Multiple variables related to any given phenotype, resulting from different definitions and multiple measurements or subsets of data</li>
				<li>Researchers today need to tediously look at documentation associated with each study in the repository, documentation that is increasing in size as more data is added</li>
				<li>The process is time-consuming and may still miss relevant variables</li>
				<li>Every researcher who wants to compare the same datasets often needs to start from scratch, since there is no record of previous phenotype comparison results</li>
			</ul>
		</li>
	</ul>
	<ul class='noindent'>
		<li>
			<div class='h1'>Phenotype Mapping</div>
			<ul>
				<li>An automatic method for matching phenotypes with variables</li>
				<li>Text in variable descriptions is used for matching</li>
				<li>The text augmented with the UMLS dictionary, so matching of related phenotypes is possible even if they share no words</li>
				<li>Transitive inference allows matching of more distantly related words, boosting recall</li>
				<li>Phenotype pairs are classified using the MaxEnt learning algorithm</li>
			</ul>
		</li>
	</ul>
	<ul class='noindent'>
		<li>
			<div class='h1'>Query Tool</div>
			<ul>
				<li>Quickly obtains the information needed to assess whether a specific study will be useful for the hypothesis of interest</li>
				<li>Can be restricted to studies that have combinations of phenotype and genetic information of interest</li>
				<li>More easily expands research questions beyond the most basic main-effects to more complex analyses such as gene-by-environment interactions and multivariate tests incorporating multiple phenotypes</li>
			</ul>
		</li>
	</ul>
	<ul class='noindent'>
		<li>
			<div class='h1'>Query Tool Features</div>
			<ul>
				<li>Filter results for a particular study or match score range</li>
				<li>Search by entering your own phenotype description</li>
				<li>Bookmark interesting queries for the future</li>
				<li>Export studies and phenotypes, and share them with others</li>
			</ul>
		</li>
	</ul>
			
			
</body>
</html>