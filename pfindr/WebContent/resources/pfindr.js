/*
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
 */

/*
 * Dynamic Updates for Query Tool Page
 * 
 * @author Serban Voinea
 * 
 */

var debug = false; //true;//false;//
var userid = null;
var oTable = null;
var queryListTable = null;
var studyListTable = null;

var scrollMap = new Object();
var dbgapVariables = new Object();

var PhenotypesColumns = ['study', 'category', 'varset', 'variable', 'description', 'visit', 'definer', 'score'];
var range = new Object();
var groupByTemplates = { category: [['category'], ['study'], ['varset', 'variable', 'description', 'visit'], ['definer', 'score']],
		study: [['study'], ['category'], ['varset', 'variable', 'description', 'visit'], ['definer', 'score']],
		flat: [['category'], ['study'], ['varset'], ['variable'], ['description'], ['visit'], ['definer'], ['score']]};

var displayGroupByTemplates = {
		'category': 'Category Hierarchy',
		'study' : 'Study Hierarchy',
		'flat': 'Table Structure'
};

var query_categories = new Array();
var groupByDefault = groupByTemplates['flat'];
var editGroupByTemplates;
var sortArray;
var PAGE_PREVIEW;
var PREVIEW_LIMIT;
var LAST_PREVIEW_LIMIT;
var WINDOW_TAB = 0;

var phenotypesInstances;
var query_predicate = new Object();
var filters = ['category', 'study'];
var newTemplateDialog;
var getHelpDialog;
var editTemplateDialog;
var importQueryeDialog;
var groupByView;
var tipBox;

var MAX_RETRIES = 10;
var AJAX_TIMEOUT = 300000;

var score1 = 0.5;
var score2 = 1;

var clickedSubmit = false;
var fromQueryDescription = false;
var fromQueryAttribute = false;
var fromCheckBox = false;
var bookmarkedStudies = null;
var filteredStudiesData = null;

var urlLinks = {
		study: {
			ARIC: 'http://www.cscc.unc.edu/aric/',
			CARDIA: 'http://www.cardia.dopm.uab.edu/',
			CFS: 'http://dceweb1.case.edu/serc/collab/project_family.shtml',
			CHS: 'http://www.chs-nhlbi.org/',
			EAGLE: 'https://www.pagestudy.org/index.php/studies/57-eagle',
			FHS: 'http://www.framinghamheartstudy.org/',
			JHS: 'http://jhs.jsums.edu/jhsinfo/',
			MEC: 'http://www.crch.org/multiethniccohort/',
			MESA: 'http://www.mesa-nhlbi.org/',
			SHHS: 'http://www.jhucct.com/shhs/',
			WHI: 'http://www.nhlbi.nih.gov/whi/'
		}
};

var tipHovers = {
		study: {
			ARIC: 'Atherosclerosis Risk in Communities',
			CARDIA: 'Coronary Artery Risk Development in Young Adults',
			CFS: 'Cleveland Family Study',
			CHS: 'Cardiovascular Health Study',
			EAGLE: 'Epidemiologic Architecture of Genes Linked to Environment',
			FHS: 'Framingham Heart Study',
			JHS: 'Jackson Heart Study',
			MEC: 'Multiethnic Cohort Study',
			MESA: 'Multi-Ethnic Study of Atherosclerosis',
			SHHS: 'Sleep Heart Health Study',
			WHI: 'Women’s Health Initiative'
		}
};

var instructionsHovers = {
		new_study_img: 'Select one or more studies of interest. The logical disjunction operator applies. ' +
					'When the mouse enters the "Studies" area, it shows the available studies of the current page. ' +
					'When the mouse leaves the "Studies" area, it shows only the selected studies of the current page. ' +
					'You can select/deselect all the studies of the current page by clicking the header checkbox.',
		new_category_img: 'Select one or more categories of interest. The logical disjunction operator applies. ' +
					'When the mouse enters the "Categories" area, it shows the available categories of the current page. ' +
					'When the mouse leaves the "Categories" area, it shows only the selected categories of the current page. ' +
					'You can select/deselect all the categories of the current page by clicking the header checkbox.',
		keyword_img: 'Select the domain (description, variable or visit) where the pattern applies. ' +
					'Type the pattern (a substring or a regular expression) to be matched. ' +
					'Up to five hints might be display. Select a hint or continue to type your pattern.' +
					'Click the "Submit" button. The system automatically filters the categories and studies dimensions and ' +
					'updates the resulting mappings with entries having the matched pattern of the specified domain.',
		score_img: 'Move the slider to select a range of scores of interest. Click the "Submit" button. ' +
					'The system automatically filters the categories and studies dimensions and ' +
					'updates the resulting mappings.',
		filter_studies_img: 'Click the "+/-" link to show/hide the studies filters. Multiple selections can be done for ' +
					'the "Race", "Gender", "Platform" and "Study Type". For the "Age" and "Participants" you might enter a ' +
					'range of values. Click the "Apply Filters" button. The "Studies" table is updated and will contain ' +
					'only the filtered rows. To clear the selections, click the "Clear Filters" button.'
					
};

/**
 * Handle an error from the AJAX request
 * retry the request in case of timeout
 * maximum retries: 10
 * each retry is performed after an exponential delay
 * 
 * @param jqXHR
 * 	the jQuery XMLHttpRequest
 * @param textStatus
 * 	the string describing the type of error
 * @param errorThrown
 * 	the textual portion of the HTTP status
 * @param retryCallback
 * 	the AJAX request to be retried
 * @param url
 * 	the request url
 * @param obj
 * 	the parameters (in a dictionary form) for the POST request
 * @param successCallback
 * 	the success callback function
 * @param param
 * 	the parameters for the success callback function
 * @param count
 * 	the number of retries already performed
 */
function handleError(jqXHR, textStatus, errorThrown, retryCallback, url, obj, successCallback, param, count) {
	var retry = false;

	switch(jqXHR.status) {
	case 0:		// client timeout
	case 408:	// server timeout
	case 503:	// Service Unavailable
	case 504:	// Gateway Timeout
		retry = (count <= MAX_RETRIES);
	}

	if (!retry) {
		var msg = '';
		var errCode = null;
		var err = jqXHR.status;
		if (err != null) {
			errCode = parseInt(err);
		}
		if (errCode == 500) {
			msg += 'Time: ' + new Date() + '\n\n';
			msg += 'User: ' + userid + '\n\n';
			msg += 'The server encountered an internal error and was unable to complete your request.\n';
			msg += 'Please contact the server administrator and inform of the time the error occurred, and anything you might have done that may have caused the error.\n';
			msg += 'More information about this error may be available in the server error log.\n';
		} else {
			if (err != null) {
				msg += 'Status: ' + err + '\n';
			}
			err = jqXHR.responseText;
			if (err != null) {
				var index = err.indexOf('<pre>');
				if (index != -1) {
					err = err.substring(index + '<pre>'.length, err.indexOf('</pre>'));
				}
				msg += 'ResponseText: ' + err + '\n';
			}
			err = jqXHR.getResponseHeader('X-Error-Description');
			if (err != null) {
				msg += 'X-Error-Description: ' + decodeURIComponent(err) + '\n';
			}
			if (textStatus != null) {
				msg += 'TextStatus: ' + textStatus + '\n';
			}
			if (errorThrown != null) {
				msg += 'ErrorThrown: ' + errorThrown + '\n';
			}
			msg += 'URL: ' + url + '\n';
		}
		alert(msg);
		$('*', $('#query_div')).css('cursor', 'default');
		$('body').css('cursor', 'default');
		$('#navigation').css('display', '');
	} else {
		var delay = Math.round(Math.ceil((0.75 + Math.random() * 0.5) * Math.pow(10, count) * 0.00001));
		setTimeout(function(){retryCallback(url, obj, successCallback, param, count+1);}, delay);
	}
}

/**
 * Functions to send AJAX requests
 * 
 * @param url
 * 	the request url
 * @param obj
 * 	the parameters (in a dictionary form) for the POST request
 * @param successCallback
 * 	the success callback function
 * @param param
 * 	the parameters for the success callback function
 * @param count
 * 	the number of retries already performed
 */
var PFINDR = {
		post: function(url, obj, successCallback, param, count) {
			$('*', $('#query_div')).css('cursor', 'wait');
			$('body').css('cursor', 'wait');
			if (obj == null || obj.predicate != 'keyword' && obj.predicate != 'selectFilters') {
				$('#navigation').css('display', 'none');
			}
			$.ajax({
				url: url,
				headers: {'User-agent': 'PFINDR/1.0'},
				type: 'POST',
				data: obj,
				timeout: AJAX_TIMEOUT,
				async: true,
				accepts: {text: 'application/json'},
				dataType: 'json',
				success: function(data, textStatus, jqXHR) {
					$('*', $('#query_div')).css('cursor', 'default');
					$('body').css('cursor', 'default');
					$('#navigation').css('display', '');
					if (data['idle'] != null) {
						window.location = '/pfindr/resources/idle.html';
					} else {
						successCallback(data, textStatus, jqXHR, param);
					}
				},
				error: function(jqXHR, textStatus, errorThrown) {
					$('*', $('#query_div')).css('cursor', 'default');
					$('body').css('cursor', 'default');
					$('#navigation').css('display', '');
					handleError(jqXHR, textStatus, errorThrown, PFINDR.post, url, obj, successCallback, param, count);
				}
			});
		},
		get: function(url, successCallback, param, count) {
			PFINDR.fetch(url, null, successCallback, param, count);
		},
		fetch: function(url, obj, successCallback, param, count) {
			$.ajax({
				url: url,
				headers: {'User-agent': 'PFINDR/1.0'},
				timeout: AJAX_TIMEOUT,
				async: true,
				accepts: {text: 'application/json'},
				dataType: 'json',
				success: function(data, textStatus, jqXHR) {
					$('*', $('#query_div')).css('cursor', 'default');
					$('body').css('cursor', 'default');
					$('#navigation').css('display', '');
					successCallback(data, textStatus, jqXHR, param);
				},
				error: function(jqXHR, textStatus, errorThrown) {
					$('*', $('#query_div')).css('cursor', 'default');
					$('body').css('cursor', 'default');
					$('#navigation').css('display', '');
					handleError(jqXHR, textStatus, errorThrown, PFINDR.fetch, url, obj, successCallback, param, count);
				}
			});
		}
};

/**
 * Method "contains" for the Array object
 * returns true if an element is in the array, and false otherwise
 */
Array.prototype.contains = function (elem) {
	for (i in this) {
		if (this[i] == elem) return true;
	}
	return false;
};

/**
 * Set the available templates to a select list
 * 
 * @param id
 * 	the id of the select element
 */
function setTemplates(id) {
	var select = $('#' + id);
	select.html('');
	var options = new Array();
	$.each(groupByTemplates, function(key, value) {
		options.push(key);
	});
	options.sort(compareTemplateOptions);
	$.each(options, function(i, value) {
		var option = $('<option>');
		option.text(displayGroupByTemplates[value] != null ? displayGroupByTemplates[value] : value);
		option.attr('value', value);
		select.append(option);
	});
}

/**
 * Return the URL for the node
 * 
 * @param index
 * 	the column index
 * @param value
 * 	the column value
 * @return the url
 */
function getNodeUrl(index, value) {
	var ret = '';
	var col = PhenotypesColumns[index];
	if (urlLinks[col] != null && urlLinks[col][value] != null) {
		ret = urlLinks[col][value];
	} 
	return ret;
}

/**
 * Apply the modifications of the last edited template and save the templates
 */
function saveGroupBy() {
	var template = $('#selectEditTemplate').val();
	applyGroupBy(template);
	groupByTemplates = editGroupByTemplates;
	selectTemplate();
}

/**
 * Apply the modifications of the template
 * 
 * @param template
 * 	the template that was modified
 */
function applyGroupBy(template) {
	editGroupByTemplates[template] = new Array();
	$.each(editTemplateDialog.find('.empty'), function(i, label) {
		var nodeArray = new Array();
		var node = $(label).next();
		while (node.is('LABEL')) {
			nodeArray.push(node.html());
			node = node.next();
		}
		if (nodeArray.length > 0) {
			editGroupByTemplates[template].push(nodeArray);
		}
	});
}

/**
 * Save the new template
 */
function saveNewGroupBy() {
	var name = $('#template_name').val().replace(/^\s*/, '').replace(/\s*$/, '');
	if (name == '') {
		alert('Please provide a name for the template.');
	} else {
		var answer = true;
		if (groupByTemplates[name] != null) {
			answer = confirm ('A template with the name"' + name + '" already exists. Do you want to replace it?');
		}
		if (answer) {
			groupByTemplates[name] = new Array();
			$.each(newTemplateDialog.find('.empty'), function(i, label) {
				var nodeArray = new Array();
				var node = $(label).next();
				while (node.is('LABEL')) {
					nodeArray.push(node.html());
					node = node.next();
				}
				if (nodeArray.length > 0) {
					groupByTemplates[name].push(nodeArray);
				}
			});
			setTemplates('selectTemplate');
			$('#selectTemplate').val($('#template_name').val());
			$('#template_name').val('');
			selectTemplate();
			newTemplateDialog.dialog('close');
		}
	}
}

/**
 * append values to a node of the groupBy tree
 * 
 * @param ul
 * 	the node to append to
 * @param nodeValues
 * 	the values to be appended
 * @return the appended node
 */
function appendNode(ul, nodeValues) {
	var li = $('<li>');
	li.addClass('tree droppable_node');
	ul.append(li);
	var label = $('<label>');
	label.addClass('empty');
	label.html('&nbsp;');
	li.append(label);
	$.each(nodeValues, function(i, value) {
		var label = $('<label>');
		label.addClass('draggable_node droppable_node node_margin');
		label.html(value);
		li.append(label);
	});
	return li;
}

/**
 * append a tree to the groupBy node
 * 
 * @param ul
 * 	the node to append to
 * @param level
 * 	the level of the tree
 * @param groups
 * 	the nodes to be appended
 */
function appendTree(ul, level, groups) {
	if (level > 0) {
		var nodeValues = new Array();
		if (groups.length > 0) {
			nodeValues = groups.shift();
		}
		var li = appendNode(ul, nodeValues);
		var ulObject = $('<ul>');
		li.append(ulObject);
		appendTree(ulObject, level-1, groups);
	}
}

/**
 * get the index in the PhenotypesColumns array, based on the column name
 * 
 * @param col
 * 	the column name
 * @return the column index
 */
function getColumnIndex(col) {
	var index = -1;
	$.each(PhenotypesColumns, function(i, value) {
		if (col == value) {
			index = i;
			return false;
		}
	});
	return index;
}

/**
 * initialize a drop-down list with the range values
 * 
 * @param filter
 * 	the predicate name
 * @param id
 * 	the id of the drop-down list
 * @param values
 * 	the range values
 */
function initNewDropDownList(id, available) {
	var values = available['categories'];
	var flyover = available['flyover'];
	var table = $('#'+id);
	table.html('');
	var tbody = $('<tbody>');
	table.append(tbody);
	$.each(values, function(i, value) {
		var tr = $('<tr>');
		tbody.append(tr);
		var td = $('<td>');
		td.attr({'valign': 'top',
			'height': '10px'});
		tr.append(td);
		var input = $('<input>');
		input.attr('type', 'checkbox');
		input.click(function(event) {selectUnselectCategories('new_category_div', input);});
		td.append(input);
		var label = $('<label>');
		label.html(value[0]);
		td.append(label);
		if (flyover[value[0]] != null) {
			label.hover(
					function(event) {DisplayTipBox(event, flyover[value[0]]);}, 
					function(){HideTipBox();});
		}
	});
	$('#new_category_div').unbind('mouseenter mouseleave');
	$('#new_category_div').hover(
			function(event) {
				$('tbody tr', $('#new_category_div')).css('display', '');
				if (scrollMap['new_category_div'] != null) {
					$('.predicate_shell', $('#new_category_div')).scrollTop(scrollMap['new_category_div']);
				}
			}, 
			function(){
				scrollMap['new_category_div'] = $('.predicate_shell', $('#new_category_div')).scrollTop();
				displaySelectedCategories();
			});
	if (query_predicate.category != null) {
		$.each($('label', $('tbody', $('#new_category_table'))), function(i, label) {
			if (query_predicate.category.contains($(label).html())) {
				$(label).prev().attr('checked', 'checked');
			}
		});
	}
	displaySelectedCategories();
}

/**
 * initialize a drop-down list with the range values
 * 
 * @param filter
 * 	the predicate name
 * @param id
 * 	the id of the drop-down list
 * @param values
 * 	the range values
 */
function initDropDownList(filter, id, values) {
	var definer = null;
	var table = $('#'+id);
	table.html('');
	$.each(values, function(i, value) {
		var tr = $('<tr>');
		table.append(tr);
		var td = $('<td>');
		tr.append(td);
		var arr = value.split('<br/>');
		if (arr.length > 1) {
			if (definer != arr[0]) {
				var b = $('<b>');
				td.append(b);
				b.html(arr[0]);
				definer = arr[0];
				tr = $('<tr>');
				table.append(tr);
				td = $('<td>');
				tr.append(td);
				td.css('padding-left', '10px');
			} else {
				td.css('padding-left', '10px');
			}
		}
		var input = $('<input>');
		input.attr('type', 'checkbox');
		input.attr({tag: filter,
			tagval: value});
		input.click(function(event) {applyFilter(event, $(this));});
		td.append(input);
		var label = $('<label>');
		if (urlLinks[filter] != null) {
			$.each(urlLinks[filter], function(key, val) {
				if (key == value) {
					var a = $('<a>');
					label.append(a);
					a.attr({	target: '_newtab2' + ++WINDOW_TAB,
						href: val });
					a.css('color', 'blue');
					a.html(value);
					return false;
				}
			});
		}
		if (label.children().length == 0) {
			label.html(definer == null ? value : value.split('<br/>')[1]);
		}
		if (tipHovers[filter] != null && tipHovers[filter][value] != null) {
			label.hover(
					function(event) {DisplayTipBox(event, tipHovers[filter][value]);}, 
					function(){HideTipBox();}
			); 
		}
		td.append(label);
		if (query_predicate[filter] != null && 
				(query_predicate[filter].contains(value) || 
						definer != null && query_predicate[filter].contains(arr[1]))) {
			input.attr('checked', 'checked');
		}
	});
}

/**
 * initialize the Query list
 */
function initQueryList() {
	queryListTable = $('#example').dataTable( {
		'bFilter': false,
		'bInfo': false,
		'bPaginate': false,
		'bSort': false
	} );
	new FixedHeader( queryListTable );
}

/**
 * initialize the Explore Query list
 */
function initExplorerQueryList() {
	queryListTable = $('#example').dataTable( {
		'bFilter': false,
		'bInfo': false,
		'bPaginate': false,
		'bSort': false
	} );
	new FixedHeader( queryListTable );
}

/**
 * initialize done after the page is loaded
 */
function initPFINDR(user, fromLink) {
	userid = user;
	fromQueryAttribute = fromLink;
	var obj = new Object();
	obj.predicate = 'study_metadata';
	var url = window.location.href;
	var param = {'initQuery': true};
	PFINDR.post(url, obj, initStudyFilters, param, 0);
}

/**
 * initialize the query page
 */
function initQuery() {
	$( '#slider-range' ).slider({
		range: true,
		min: 0,
		max: 1,
		step: 0.01,
		values: [ 0.5, 1 ],
		slide: function( event, ui ) {
			$( '#score_label' ).html( 'Score Range: ' + ui.values[ 0 ] + ' - ' + ui.values[ 1 ] );
			score1 = '' + ui.values[ 0 ];
			score2 = '' + ui.values[ 1 ];
		}
	});
	
	// green background for the slider
	$('.ui-widget-header', $( '#slider-range' )).css('background', '#ECFFB3 url(images/ui-bg_gloss-wave_35_f6a828_500x100.png) 50% 50% repeat-x');
	
	var help = $('#pfindrHelp');
	help.attr({	target: '_newtab2' + ++WINDOW_TAB });
	$('div.topnav ul.subnav li.item').click(function(event) {event.preventDefault();});
	$('div.topnav ul.subnav li.item').mouseup(function(event) {event.preventDefault();});
	$('div.topnav ul.subnav li.item').hover(
			function() { 
				$(this).addClass('highlighted');
			}, 
			function(){	
				$(this).removeClass('highlighted');
			}
	); 
	$('div.topnav span').click(function() {
		var height = $(this).height();
		var top = ($(this).position().top + height) + 'px';
		$('ul.subnav').css('top', top);
		var left = $(this).position().left + $(this).width() - 180;
		if (left < 0) {
			left = 0;
		}
		left += 'px';
		$('ul.subnav').css('left', left);
		if (oTable != null) {
			$('.FixedHeader_Cloned').css('display', 'none');
		}

		//Following events are applied to the subnav itself (moving subnav up and down)
		$(this).parent().parent().parent().parent().parent().find('ul.subnav').slideDown('fast').show(); //Drop down the subnav on click

		$(this).parent().parent().parent().parent().parent().hover(function() {
		}, function(){	
			$(this).parent().parent().parent().parent().parent().find('ul.subnav').slideUp('slow'); //When the mouse hovers out of the subnav, move it back up
		});

		//Following events are applied to the trigger (Hover events for the trigger)
	}).hover(function() { 
		$(this).addClass('subhover'); //On hover over, add class 'subhover'
	}, function(){	//On Hover Out
		$(this).removeClass('subhover'); //On hover out, remove class 'subhover'
	});
	tipBox = $('#TipBox');
	groupByView = groupByDefault;
	$('#previewLimit').val('100');
	sortArray = getSortIndexArray(groupByView);
	range.category = new Object();
	range.study = new Object();
	PREVIEW_LIMIT = parseInt($('#previewLimit').val());
	LAST_PREVIEW_LIMIT = PREVIEW_LIMIT;
	PAGE_PREVIEW = 0;

	$('#query_input').val('');
	$('#query_input').keyup(function(event) {getHints();});
	var treeHeight = Math.ceil($(window).height() / 2) + 'px';
	var treeWidth = Math.ceil($(window).width() / 2) + 'px';
	$('#pf_tree_div').css('height', treeHeight);
	var categoryHeight = Math.ceil($(window).height() / 4) + 'px';
	var predicateHeight = Math.ceil($(window).height() / 2) + 'px';
	var predicateWidth = Math.ceil($(window).height() / 4) + 'px';
	$('#new_category_div').css('height', categoryHeight);
	$('#new_category_div').find(':checkbox').click(function(event) {checkUncheckAll('new_category_div', $(this)); });
	$('#datatable_study_div').resizable('disable');
	$('#new_category_div').resizable('disable');
	$('#keyword_div').resizable('disable');
	$('#datatable_study_div').css('width', '100%');
	$('#score_div').css('width', $('#keyword_div').width() / 2);
	$('#new_category_div').css('width', Math.ceil($(window).width() * 25 / 100));
	$('#pf_tree_div').resizable({alsoResize: '.tree_view'});

	// adjust the wrappers height
	var index = predicateHeight.indexOf('px');
	var columnHeight = parseInt(predicateHeight.substring(0, index));
	var delta = Math.floor(columnHeight - $('#datatable_study_div').find('h3').height());
	$('.predicate_study').css('max-height', delta+'px');
	$('#studyDetailsDiv').css('display', '');
	
	index = categoryHeight.indexOf('px');
	columnHeight = parseInt(categoryHeight.substring(0, index));
	delta = Math.floor(columnHeight - $('#new_category_div').find('h3').height());
	$('.predicate_category').css('height', delta+'px');

	delta = Math.floor($(window).height() / 2 - $('#pf_tree_div').find('h3').height());
	$('.tree_view').css('height', delta+'px');

	// initialize categories hints area
	$('#category_input').keyup(function(event) {scrollToCategoryHints();});
	$('#category_header').height($('#category_input').height() + 8);
	
	// initialize keywords hints area
	initSpan();

	// initialize the new template pop-up window
	newTemplateDialog = $('#new_template_div');
	newTemplateDialog.dialog({
		autoOpen: false,
		title: 'New Template',
		buttons: {
			'Cancel': function() {
				$(this).dialog('close');
			},
			'Save': function() {
				saveNewGroupBy();
			}
		},
		position: 'top',
		draggable: true,
		height: ($(window).height() < 400 ? $(window).height() : 400),
		modal: false,
		resizable: true,
		width: ($(window).width() < 500 ? $(window).width() : 500)
	});

	// initialize the edit template pop-up window
	editTemplateDialog = $('#edit_template_div');
	editTemplateDialog.dialog({
		autoOpen: false,
		title: 'Edit Template',
		buttons: {
			'Cancel': function() {
				$(this).dialog('close');
			},
			'Save': function() {
				saveGroupBy();
				$(this).dialog('close');
			}
		},
		position: 'top',
		draggable: true,
		height: ($(window).height() < 400 ? $(window).height() : 400),
		modal: false,
		resizable: true,
		width: ($(window).width() < 500 ? $(window).width() : 500)
	});
	// initialize the edit template pop-up window
	importQueryeDialog = $('#import_query_div');
	importQueryeDialog.dialog({
		autoOpen: false,
		title: 'Import Query',
		buttons: {
			'Cancel': function() {
				$(this).dialog('close');
			},
			'Import': function() {
				$('#import_query_id').submit();
				$(this).dialog('close');
			}
		},
		position: 'top',
		draggable: true,
		height: ($(window).height() < 200 ? $(window).height() : 200),
		modal: false,
		resizable: true,
		width: ($(window).width() < 500 ? $(window).width() : 500)
	});
	// initialize the get Help pop-up window
	getHelpDialog = $('#get_help_div');
	getHelpDialog.dialog({
		autoOpen: false,
		title: 'Help Info',
		position: ['left', 'top'],
		draggable: true,
		height: 500,
		modal: false,
		resizable: true,
		width: 600
	});

	setTemplates('selectTemplate');
	$('#selectTemplate').val(fromQueryAttribute ? 'study' : 'flat');
	$.each(instructionsHovers, function(key, value) {
		$('#' + key).hover(
				function(event) {DisplayTipBox(event, value);}, 
				function(){HideTipBox();}
		); 
	});
	
	selectTemplate();
}

/**
 * initialize a drop-down list with hints
 * 
 * @param data
 * 	the data returned from the server
 * @param textStatus
 * 	the string describing the status
 * @param jqXHR
 * 	the jQuery XMLHttpRequest
 * @param param
 * 	the parameters to be used by the callback success function
 */
function getHintSuccess(data, textStatus, jqXHR, param) {
	var ul = $('#query_ul');
	$.each(data, function(i, val) {
		var li = getChild(ul, i+1);
		li.css('display', '');
		li.html(val[0]);
	});
	for (var i=data.length; i < 5; i++) {
		var li = getChild(ul, i+1);
		li.css('display', 'none');
	}
	$('#query_span').click();
	if (data.length == 0) {
		$('#query_input').css('border-bottom-width', '1px');
		$('.hint').css('display', 'none');
	}
}

/**
 * Check/uncheck all the studies or categories
 * 
 * @param id
 * 	the id of the studies or categories
 * @param input
 * 	the check box for check/uncheck all
 */
function checkUncheckAll(id, input) {
	var checked = input.attr('checked');
	if (checked == 'checked') {
		$('#' + id).find(':checkbox').attr('checked', 'checked');
		//applyAllFilters(input);
	} else {
		$('#' + id).find(':checkbox').removeAttr('checked');
		//removeAllFilters(input);
	}
}

/**
 * initialize a drop-down list for query hints
 */
function initSpan() {	
	var span = $('#query_span');
	var ul = $('<ul>');
	span.html('');
	span.append(ul);
	ul.attr('id', 'query_ul');
	var top = Math.ceil($('#query_input').offset().top + $('#query_input').height());
	var left = $('#query_input').offset().left;
	var li = $('<li>');
	li.addClass('keyitem');
	li.mouseup(function(event) {event.preventDefault();});
	li.mousedown(function(event) {event.preventDefault(); selectToken($(this));});
	ul.append(li);
	li = $('<li>');
	li.addClass('keyitem');
	li.mouseup(function(event) {event.preventDefault();});
	li.mousedown(function(event) {event.preventDefault(); selectToken($(this));});
	ul.append(li);
	li = $('<li>');
	li.addClass('keyitem');
	li.mouseup(function(event) {event.preventDefault();});
	li.mousedown(function(event) {event.preventDefault(); selectToken($(this));});
	ul.append(li);
	li = $('<li>');
	li.addClass('keyitem');
	li.mouseup(function(event) {event.preventDefault();});
	li.mousedown(function(event) {event.preventDefault(); selectToken($(this));});
	ul.append(li);
	li = $('<li>');
	li.addClass('keyitem');
	li.mouseup(function(event) {event.preventDefault();});
	li.mousedown(function(event) {event.preventDefault(); selectToken($(this));});
	ul.append(li);

	$('li.keyitem').click(function(event) {event.preventDefault();});
	$('li.keyitem').mouseup(function(event) {event.preventDefault(); selectToken($(this));});
	$('li.keyitem').hover(
			function() { 
				$(this).addClass('highlighted');
			}, 
			function(){	
				$(this).removeClass('highlighted');
			}
	); 
	$('#query_span').click(function() {
		$('#query_input').css('border-bottom-width', '0px');
		$('.hint').show();
	});
	ul.addClass('hint');
}

/**
 * get hints
 * 
 * @param li
 * 	the selected hint
 */
function selectToken(li) {
	$('#query_input').val(li.html());
	query();
}

/**
 * send an AJAX request for getting hints for keyword(s)
 */
function getHints() {
	var val = $('#query_input').val();
	if ($('#query_regex').attr('checked') != 'checked') {
		val = escapeRegExp(val);
		var len = val.length;
		if (len > 1 && val[len-1] == '\\' && val[len-2] != '\\') {
			return;
		}
	} else {
		try {
			new RegExp(val);
		} catch (e) {
			return;
		}
	}
	var ret = new Array();
	var obj = new Object();
	obj.predicate = 'keyword';
	obj.value = val;
	obj.column = $('input:radio[name=keywordSelect]:checked').val();
	var url = window.location.href;
	PFINDR.post(url, obj, getHintSuccess, null, 0);
}

/**
 * Compares two strings lexicographically, ignoring case differences.
 * 
 * @param str1
 * 	the left string
 * @param str2
 * 	the right string
 * @return
 * 	-1 if str1 < str2
 * 	0 if str1 = str2
 * 	1 if str1 > str2
 */
function compareIgnoreCase(str1, str2) {
	var val1 = str1.toLowerCase();
	var val2 = str2.toLowerCase();
	if (val1 == val2) {
		return 0;
	} else if (val1 < val2) {
		return -1;
	} else {
		return 1;
	}
}

/**
 * Compares two template text options lexicographically, ignoring case differences.
 * 
 * @param str1
 * 	the left option
 * @param str2
 * 	the right option
 * @return
 * 	-1 if left option text < right option text
 * 	0 if left option text = right option text
 * 	1 if left option text > right option text
 */
function compareTemplateOptions(str1, str2) {
	return compareIgnoreCase(displayGroupByTemplates[str1] != null ? displayGroupByTemplates[str1] : str1, 
			displayGroupByTemplates[str2] != null ? displayGroupByTemplates[str2] : str2);
}

/**
 * Compares two object names lexicographically, ignoring case differences.
 * 
 * @param node1
 * 	the left node
 * @param node2
 * 	the right node
 * @return
 * 	-1 if node1 < node2
 * 	0 if node1 = node2
 * 	1 if node1 > node2
 */
function compareNode(node1, node2) {
	var str1 = '';
	var str2 = '';
	$.each(node1.name, function(i, val) {
		str1 += val;
		str2 += node2.name[i];
	});
	return compareIgnoreCase(str1, str2);
}

/**
 * gets the child of a DOM object
 * 
 * @param item
 * 	the DOM object
 * @param index
 * 	the child number
 * @return the child
 */
function getChild(item, index) {
	return item.children(':nth-child(' + index + ')');
}

/**
 * builds the tree for the phenotypes mapping
 * 
 * @param totalRows
 * 	the total number of rows found for a predicate
 */
function getPhenotypesMapping(totalRows) {
	var offset = PAGE_PREVIEW * PREVIEW_LIMIT;
	while (offset > totalRows) {
		offset -= PREVIEW_LIMIT;
		--PAGE_PREVIEW;
	}
	var mapping = getPhenotypesDict(groupByView, totalRows);
	var roots = new Array();
	getPhenotypesTree(mapping, roots);
	var ul = $('#navigation');
	ul.html('');
	getPhenotypesDOM(ul, roots);
	$('#navigation').treeview({
		persist: 'location',
		collapsed: true,
		unique: false
	});
	expandTree($('#navigation'), 0, 1);
	var previewRows = PREVIEW_LIMIT;
	if (offset + previewRows > totalRows) {
		previewRows = totalRows - offset;
	}
	showNavigation(previewRows, totalRows, offset);
}

/**
 * builds the DOM tree for the query results
 * 
 * @param ul
 * 	the parent node
 * @param roots
 * 	the children nodes
 */
function getPhenotypesDOM(ul, roots) {
	$.each(roots, function(i, obj) {
		var li = appendItem(ul, obj);
		if (obj.values.length > 0) {
			var ulObject = $('<ul>');
			li.append(ulObject);
			getPhenotypesDOM(ulObject, obj.values);
		}
	});
}

/**
 * converts a dictionary to a tree structure
 * 
 * @param mapping
 * 	the dictionary
 * @param roots
 * 	the collection of the tree nodes
 */
function getPhenotypesTree(mapping, roots) {
	if (hasProperties(mapping)) {
		$.each(mapping, function(key, values) {
			var obj = new Object();
			obj.name = values.name;
			obj.tags = values.tags;
			obj.url = values.url;
			obj.values = new Array();
			getPhenotypesTree(values.value, obj.values);
			//obj.values.sort(compareNode);
			roots.push(obj);
		});
		//roots.sort(compareNode);
	}
}

/**
 * get the index array to be used in the tree groups 
 * 
 * @param groupBy
 * 	the predicate names to be grouped
 * @return the index array 
 */
function getSortIndexArray(groupBy) {
	// get the indexes
	var indexArray = new Array();
	$.each(groupBy, function(i, values) {
		var itemArray = new Array();
		$.each(values, function(j, value) {
			var index = getColumnIndex(value);
			if (index != -1) {
				itemArray.push(index);
			}
		});
		indexArray = indexArray.concat(itemArray);
	});
	return indexArray;
}

/**
 * get the dictionary for the phenotypes instances
 * 
 * @param groupBy
 * 	the predicate names to be grouped
 * @param totalRows
 * 	the total number of phenotypes for the given predicate
 * @return the dictionary 
 */
function getPhenotypesDict(groupBy, totalRows) {
	// get the indexes
	var indexArray = new Array();
	$.each(groupBy, function(i, values) {
		var itemArray = new Array();
		$.each(values, function(j, value) {
			var obj = new Object();
			obj.index = getColumnIndex(value);
			obj.value = value;
			itemArray.push(obj);
		});
		indexArray.push(itemArray);
	});

	var mapping = new Object();
	$.each(phenotypesInstances, function(i, row) {
		var root = mapping;
		$.each(indexArray, function(j, values) {
			var col;
			if (values.length == 1) {
				col = getColumnValue(values[0], row);
			} else {
				// concat group by
				col = getColumnConcatValue(values, row);
			}
			if (root[col] == null) {
				root[col] = new Object();
				var props = getNodeProperties(values, row);
				root[col].tags = props.tags;
				root[col].url = props.url;
				root[col].name = props.name;
				root[col].value = new Object();
			}
			root = root[col].value;
		});
	});
	return mapping;
}

/**
 * checks if the DOM object has at least one property
 * 
 * @param root
 * 	the DOM object
 * @return true if the object has at least one property and false otherwise
 */
function hasProperties(root) {
	ret = false;
	$.each(root, function(key, value) {
		ret = true;
		return false;
	});
	return ret;
}

/**
 * get the properties of the node
 * 
 * @param values
 * 	the values of the node
 * @param row
 * 	the phenotype row to get a value
 * @return the properties of the row
 */
function getNodeProperties(values, row) {
	var ret = new Object();
	ret.tags = new Array();
	ret.name = new Array();
	ret.url = new Array();
	$.each(values, function(i, obj) {
		var index = obj.index;
		var col = new Object();
		if (index == -1) {
			// value not in the row
			ret.name.push(obj.value);
			ret.url.push('');
			ret.tags.push('');
		} else {
			ret.name.push(row[index]);
			ret.url.push(getNodeUrl(index, row[index]));
			ret.tags.push(PhenotypesColumns[index]);
		}
	});
	return ret;
}

/**
 * concatenates a group of values
 * 
 * @param values
 * 	the values to be concatenated
 * @param row
 * 	the phenotype row to get a value
 * @return the string with the concatenated values
 */
function getColumnConcatValue(values, row) {
	var concatArray = new Array();
	$.each(values, function(i, obj) {
		var value = getColumnValue(obj, row);
		if (value != '') {
			concatArray.push(value);
		}
	});
	var col = concatArray.join(' | ');
	return col;
}

/**
 * get a phenotype value
 * 
 * @param obj
 * 	the DOM object that contains the predicate name and values
 * @param row
 * 	the phenotype row to get a value
 * @return the value
 */
function getColumnValue(obj, row) {
	var index = obj.index;
	var col;
	if (index == -1) {
		// value not in the row
		col = obj.value;
	} else {
		col = row[index];
	}
	return col;
}

/**
 * execute a query
 * hide the keyword(s) hints
 */
function query() {
	$('#query_input').css('border-bottom-width', '1px');
	$('.hint').css('display', 'none');
}

/**
 * appends an item to a node
 * 
 * @param ul
 * 	the node
 * @param obj
 * 	the DOM object to be appended
 * @return the appended item
 */
function appendItem(ul, obj) {
	var li = $('<li>');
	li.addClass('tree');
	ul.append(li);
	var label = $('<label>');
	li.append(label);
	var first = true;
	$.each(obj.name, function(i, val) {
		if (val != '') {
			if (!first) {
				label.append(' | ');
			} else {
				first = false;
			}
			var span = $('<span>');
			span.addClass('node_description');
			label.append(span);
			if (obj.tags[i] == 'variable' && dbgapVariables[val.toUpperCase()] != null) {
				var a = $('<a>');
				a.css('color', 'blue');
				a.attr({target: '_newtab2',
					href: 'http://www.ncbi.nlm.nih.gov/projects/gap/cgi-bin/variable.cgi?study_id=' + dbgapVariables[val.toUpperCase()]});
				a.html(val);
				span.append(a);
			} else {
				span.append(val);
				span.click(function(event) {DisplayTipBox(event, obj.tags[i]);});
				span.mouseout(function(event) {HideTipBox();});
			}
		}
	});
	return li;
}

/**
 * renders the navigation widget
 * 
 * @param previewRows
 * 	the number of records to be displayed in the view
 * @param totalRows
 * 	the total number of records that matches a predicate
 * @param offset
 * 	the offset from which the records are selected
 */
function showNavigation(previewRows, totalRows, offset) {
	$('#perPage').css('display', '');
	$('#perPageWith').css('display', '');
	$('#previewLimit').css('display', '');
	var span = $('#ViewResults');
	span.html('');
	if (previewRows == 0) {
		span.html('No matches');
		$('#pagePrevious').css('display', 'none');
		$('#resultsRange').html('');
		$('#pageNext').css('display', 'none');
		$('#totalResults').html('');
	} else if (previewRows != -1 && previewRows < totalRows) {
		var minRow = offset + 1;
		var maxRow = minRow + PREVIEW_LIMIT - 1;
		if (maxRow > totalRows) {
			maxRow = totalRows;
		}
		span.html('Showing ');
		getSelectRange(totalRows);
		if (minRow > PREVIEW_LIMIT) {
			$('#pagePrevious').css('display', '');
		} else {
			$('#pagePrevious').css('display', 'none');
		}
		if (maxRow < totalRows) {
			$('#pageNext').css('display', '');
		} else {
			$('#pageNext').css('display', 'none');
		}
		$('#totalResults').html('of ' + totalRows + ' matches');
	} else {
		$('#pagePrevious').css('display', 'none');
		$('#pageNext').css('display', 'none');
		span.html('Showing all ' + totalRows + ' matches');
		$('#resultsRange').html('');
		$('#totalResults').html('');
	}
}

/**
 * disable the navigation after rendering a data table
 * 
 */
function disableNavigation() {
	$('#ViewResults').html('');
	$('#pagePrevious').css('display', 'none');
	$('#pageNext').css('display', 'none');
	$('#resultsRange').html('');
	$('#totalResults').html('');
	$('#perPageWith').css('display', 'none');
	$('#perPage').css('display', 'none');
	$('#previewLimit').css('display', 'none');
}

/**
 * sets the options for selecting a range of records 
 * 
 * @param totalRows
 * 	the total number of records that matches a predicate
 */
function getSelectRange(totalRows) {
	var select = $('#resultsRange').find('select');
	if (select.get(0) == null) {
		$('#resultsRange').html('');
		select = $('<select>');
		select.attr('id', 'showRange');
		select.change(function(event) {accessNextPage();});
		$('#resultsRange').append(select);
	}
	select.html('');
	var totalPages = Math.ceil(totalRows / PREVIEW_LIMIT);
	var minPage = PAGE_PREVIEW - 9;
	var maxPage = PAGE_PREVIEW + 11;
	if (minPage < 0) {
		minPage = 0;
		maxPage = 20;
		if (maxPage > totalPages) {
			maxPage = totalPages;
		}
	} else if (maxPage > totalPages) {
		maxPage = totalPages;
		minPage = maxPage - 20;
		if (minPage < 0) {
			minPage = 0;
		}
	}
	var options = select.children().length;
	var entries = maxPage - minPage;
	if (minPage > 0) {
		entries++;
	}
	if (maxPage < totalPages) {
		entries++;
	}
	if (options < entries) {
		var delta = entries - options;
		for (var i=0; i < delta; i++) {
			select.append($('<option>'));
		}
	}
	var index = 0;
	for (var i=0; i < entries; i++) {
		var option = getChild(select, i+1);
		option.css('display', '');
		if (i == 0 && minPage > 0) {
			option.text('1 to ' + PREVIEW_LIMIT);
			option.attr('value', '0');
		} else if (i == (entries - 1) && maxPage < totalPages) {
			option.text('' + ((totalPages - 1) * PREVIEW_LIMIT  + 1) + ' to ' + totalRows);
			option.attr('value', '' + (totalPages - 1));
		} else {
			var maxVal = (minPage + index + 1) * PREVIEW_LIMIT;
			if (maxVal > totalRows) {
				maxVal = totalRows;
			}
			option.text('' + ((minPage + index) * PREVIEW_LIMIT + 1) + ' to ' + maxVal);
			option.attr('value', '' + (minPage + index));
			index++;
		}
	}
	select.val('' + PAGE_PREVIEW);
}

/**
 * apply a new range
 */
function accessNextPage() {
	PAGE_PREVIEW = parseInt($('#showRange').val());
	applyPredicate();
}

/**
 * apply a new page limit
 */
function updatePreviewLimit() {
	LAST_PREVIEW_LIMIT = PREVIEW_LIMIT;
	PREVIEW_LIMIT = parseInt($('#previewLimit').val());
	// update the page counter
	if (PREVIEW_LIMIT != -1) {
		PAGE_PREVIEW = Math.floor((LAST_PREVIEW_LIMIT * PAGE_PREVIEW / PREVIEW_LIMIT));
	} else {
		PAGE_PREVIEW = 0;
	}
	applyPredicate();
}

/**
 * get the the previous results
 */
function setPreviousPage() {
	PAGE_PREVIEW--;
	applyPredicate();
}

/**
 * get the next results
 */
function setNextPage() {
	PAGE_PREVIEW++;
	applyPredicate();
}

/**
 * Expand the entire tree
 */
function expandAll() {
	$('#navigation').find('div.hitarea.tree-hitarea.expandable-hitarea').click();
	$('#GlobalMenu').slideUp('slow');
	if (oTable != null) {
		$('.FixedHeader_Cloned').css('display', '');
	}
}

/**
 * Collapse the entire tree
 */
function collapseAll() {
	$('#navigation>li>div.hitarea.tree-hitarea.collapsable-hitarea').click();
	$('#GlobalMenu').slideUp('slow');
	if (oTable != null) {
		$('.FixedHeader_Cloned').css('display', '');
	}
}

/**
 * apply an item selected from the drop-down list
 * 
 * @param event
 * 	the event that triggered the call
 * @param input
 * 	the checkbox that was clicked
 */
function applyFilter(event, input) {
	var td = input.parent();
	var label = getChild(td, 2);
	var predicate = input.attr('tagval');
	var selected = input.attr('checked') == 'checked';
	var isCtrl = event.ctrlKey;
	var tag = input.attr('tag');
	if (!selected) {
		// remove the value from the filter
		var values = query_predicate[tag];
		var index = -1;
		$.each(values, function(i, value) {
			if (value == predicate) {
				index = i;
				return false;
			}
		});
		values.splice(index, 1);
		if (values.length == 0) {
			delete query_predicate[tag];
		}
	} else {
		if (query_predicate[tag] == null) {
			query_predicate[tag] = new Array();
		}
		query_predicate[tag].push(predicate);
	}

	applyPredicate();
}

/**
 * apply all the selected items
 * 
 * @param input
 * 	the checkbox that selects all the items
 */
function applyAllFilters(input) {
	var tag = input.attr('tag');
	var values = range[tag].available;
	if (query_predicate[tag] == null) {
		query_predicate[tag] = new Array();
	}
	$.each(values, function(i, value) {
		if (!query_predicate[tag].contains(value)) {
			query_predicate[tag].push(value);
		}
	});

	applyPredicate();
}

/**
 * apply all the deselected items 
 * 
 * @param input
 * 	the checkbox that deselects all the items
 */
function removeAllFilters(input) {
	var tag = input.attr('tag');
	if (query_predicate[tag] != null) {
		delete query_predicate[tag];
	}
	applyPredicate();
}

/**
 * apply a filter predicate and get updated selection for studies/predicates
 */
function applyFilterPredicate() {
	var sql = getPhenotypesSQL();
	var obj = new Object();
	obj.sql = objectToString(sql);
	obj.predicate = 'selectFilters';
	obj.columns = arrayToString(filters);
	var url = window.location.href;
	PFINDR.post(url, obj, postApplyFilterPredicate, null, 0);
}

/**
 * apply a predicate and get the new phenotypes intances
 */
function applyPredicate() {
	clickedSubmit = true;
	if (oTable != null) {
		oTable.fnDestroy(true);
		$('.FixedHeader_Cloned').remove();
		$('#example').remove();
		FixedHeader.destroy();
		oTable = null;
		var demo = $('#demo');
		demo.html('');
		var table = $('<table>');
		demo.append(table);
		table.attr({'cellpadding': '0',
			'cellspacing': '0',
			'border': '0',
			'id': 'example'});
		table.addClass('display');
		var thead = $('<thead>');
		table.append(thead);
		thead.append($('<tr>'));
		var tfoot = $('<tfoot>');
		table.append(tfoot);
		tfoot.append($('<tr>'));
		var tbody = $('<tbody>');
		table.append(tbody);
	}
	if (!fromQueryDescription) {
		initQueryPredicate();
	}
	var sql = getPhenotypesSQL();
	var obj = new Object();
	obj.sql = objectToString(sql);
	obj.predicate = 'select';
	obj.columns = arrayToString(filters);
	obj.template = arrayToString(groupByView);
	obj.selectedTemplate = $('#selectTemplate').val();
	var studyMetadataFilter = getStudiesMetadataFilter();
	if (studyMetadataFilter != null) {
		obj.study_details = studyMetadataFilter;
	}
	var url = window.location.href;
	if (groupByView.length < 8) {
		$('.tree_view').css('display', '');
		$('#dt_example').css('display', 'none');
		PFINDR.post(url, obj, postApplyPredicate, null, 0);
	} else {
		$('.tree_view').css('display', 'none');
		$('#dt_example').css('display', '');
		var thead = $($('thead', $('#demo'))[0]);
		var tfoot = $($('tfoot', $('#demo'))[0]);
		var tbody = $($('tbody', $('#demo'))[0]);
		var trHead = $($('tr', thead)[0]);
		var trFoot = $($('tr', tfoot)[0]);
		trHead.html('');
		trFoot.html('');
		tbody.html('');
		$.each(groupByView, function (i, col) {
			var thHead = $('<th>');
			var thFoot = $('<th>');
			thHead.html(col[0]);
			thFoot.html(col[0]);
			trHead.append(thHead);
			trFoot.append(thFoot);
		});
		var th = $('<th>');
		var checkBox = $('<input>');
		checkBox.attr('type', 'checkbox');
		checkBox.click(function(event) {markUnmarkAll('example', $(this)); });
		th.append(checkBox);
		trHead.append(th);
		trFoot.append($('<th>'));
		thead.css('background-color', '#F0FFFF');
		obj.predicate = 'flat';
		$('#container').css('width', '100%');
		$('#container').css('margin-top', '0px');
		$('#demo').css('margin-top', '0px');
		oTable = $('#example').dataTable( {
			'aLengthMenu': [
			                [10, 25, 50, 100, 500, 1000, 5000, -1],
			                [10, 25, 50, 100, 500, 1000, 5000, 'All']
			                ],
            'iDisplayLength': 500,
            'sDom': '<"top"ilp<"clear">>rt<"bottom"ilp<"clear">>',
            'bServerSide': true,
            'bProcessing': true,
            'sPaginationType': 'full_numbers',
            'bFilter': false,
            'sServerMethod': 'POST',
            'aoColumns': [null,
                          null,
                          null,
                          null,
                          null,
                          null,
                          null,
                          null,
                          { "bSortable": false }],
			'fnServerData': function ( sSource, aoData, fnCallback ) {
				/* Add some extra data to the sender */
				$('.FixedHeader_Cloned').css('display', 'none');
				$('thead', $('#example')).find(':checkbox').removeAttr('checked');
				$('tbody', $('#demo')).css('display', 'none');
				$.ajax({
					'dataType': 'json',
					'type': 'POST',
					'url': sSource,
					'data': aoData,
					'success': function (json) {
						if (json['range'] != null) {
							setNewRange(json['range']);
							disableNavigation();
						}
						$('tbody', $('#demo')).css('display', '');
						fnCallback( json );
					}
					});
			},
			'fnDrawCallback': function( oSettings ) {
				$.each($('tbody', $('#example')).find(':checkbox'), function(i, checkbox) {
					$(checkbox).parent().attr('align', 'center');
					$(checkbox).click(function(event) {showHideSelectedResults(); });
				});
				showHideSelectedResults();
				if ($('.FixedHeader_Cloned').get(0) == null) {
					new FixedHeader( oTable );
				}
				$('.FixedHeader_Cloned').css('display', '');
			},
			'fnServerParams': function ( aoData ) {
				$.each(obj, function(key, val) {
					aoData.push( { 'name': key, 'value': val } );
				});
			},
			'sAjaxSource': url
		} );
	}
}

function initQueryPredicate() {
	query_predicate = new Object();
	var studies = getSelectedStudies();
	if (studies.length > 0) {
		query_predicate.study = studies;
	}
	var categories = getSelectedCategories();
	if (categories.length > 0) {
		query_predicate.category = categories;
	}
}

/**
 * callback after getting the new filters values
 * 
 * @param data
 * 	the data returned from the server
 * @param textStatus
 * 	the string describing the status
 * @param jqXHR
 * 	the jQuery XMLHttpRequest
 * @param param
 * 	the parameters to be used by the callback success function
 */
function postApplyFilterPredicate(data, textStatus, jqXHR, param) {
	var rangeData = data['range'];
	clickedSubmit = true;
	setRange(rangeData);
}

/**
 * callback after getting the new phenotypes intances
 * 
 * @param data
 * 	the data returned from the server
 * @param textStatus
 * 	the string describing the status
 * @param jqXHR
 * 	the jQuery XMLHttpRequest
 * @param param
 * 	the parameters to be used by the callback success function
 */
function postApplyPredicate(data, textStatus, jqXHR, param) {
	var queryDescription = data['queryDescription'];
	if (queryDescription != null) {
		clearPredicate();
		var wherePredicate = queryDescription['where'];
		if (wherePredicate != null) {
			var score = wherePredicate['score'];
			if (score != null) {
				var values = score['values'];
				if (values[0] != 0.5 || values[1] != 1) {
					score1 = values[0];
					score2 = values[1];
					$( '#score_label' ).html( 'Score Range: ' + score1 + ' - ' + score2);
					$('#slider-range').slider('values', values);
				}
			} else {
				score1 = 0;
				score2 = 1;
				$( '#score_label' ).html( 'Score Range: 0 - 1');
				$('#slider-range').slider('values', [0, 1]);
			}
			var description = wherePredicate['description'];
			if (description != null) {
				$('#query_input').val(description.values[0]);
				$('input:radio[name=keywordSelect][value=description]').attr('checked', true);
			}
			var variablePredicate = wherePredicate['variable'];
			if (variablePredicate != null) {
				$('#query_input').val(variablePredicate.values[0]);
				$('input:radio[name=keywordSelect][value=variable]').attr('checked', true);
			}
			var study = wherePredicate['study'];
			if (study != null) {
				query_predicate.study = study.values;
			}
			var category = wherePredicate['category'];
			if (category != null) {
				query_predicate['category'] = category.values;
			}
		} else {
			score1 = 0;
			score2 = 1;
			$( '#score_label' ).html( 'Score Range: 0 - 1');
			$('#slider-range').slider('values', [0, 1]);
		}
		var limit = queryDescription['limit'];
		if (limit != null) {
			PREVIEW_LIMIT = parseInt(limit);
			$('#previewLimit').val(limit);
		}
		var offset = queryDescription['offset'];
		if (offset != null) {
			PAGE_PREVIEW = Math.floor(parseInt(offset) / PREVIEW_LIMIT);
		}
		var template = queryDescription['template'];
		if (template != null) {
			$('#selectTemplate').val(template);
			groupByView = groupByTemplates[template];
			sortArray = getSortIndexArray(groupByView);
			if ($('#selectTemplate').val() == 'flat') {
				$('.expandCollapseAll').css('display', 'none');
				$('.saveSelectedResults').css({'display': '',
					'color': 'grey'});
			}
			else {
				$('.saveSelectedResults').css('display', 'none');
				$('.expandCollapseAll').css('display', '');
			}
		}
		fromQueryDescription = true;
		applyPredicate();
	} else {
		phenotypesInstances = data['phenotypes'];
		dbgapVariables = new Object();
		$.each(data['dbgap'], function(i, row) {
			dbgapVariables[row[0]] = row[1];
		});
		var totalRows = data['count'];
		var rangeData = data['range'];
		//setRange(rangeData);
		setNewRange(rangeData);
		fromQueryDescription = false;
		getPhenotypesMapping(totalRows);
	}
}

/**
 * Set the values for the studies and categories dimension
 * 
 * @param rangeData
 * 	the JSONObject with the values
 */
function setNewRange(rangeData) {
	$.each(rangeData, function(filter, values) {
		if (filter == 'category') {
			initNewDropDownList('new_category_table', rangeData[filter].available);
		} else if (filter == 'study') {
			setStudiesTable(rangeData[filter].available.studies, rangeData[filter].available.flyover);
		}
	});
}

/**
 * Set the values for the studies and categories dimension
 * 
 * @param rangeData
 * 	the JSONObject with the values
 */
function setRange(rangeData) {
	$.each(rangeData, function(filter, values) {
		if (clickedSubmit && query_predicate[filter] != null && !fromQueryDescription) {
			var found = false;
			$.each(query_predicate[filter], function(i, value) {
				found = false;
				$.each(values.restricted, function(j, arrayValues) {
					if (arrayValues.contains(value)) {
						found = true;
						return false;
					}
				});
				if (!found) {
					values.restricted.push(new Array(value));
				}
				found = false;
				$.each(values.available, function(j, arrayValues) {
					if (arrayValues.contains(value)) {
						found = true;
						return false;
					}
				});
				if (!found) {
					values.available.push(new Array(value));
				}
			});
		}
		range[filter].available = new Array();
		$.each(values['available'], function(i, value) {
			range[filter].available.push(value[0]);
		});
		updateQueryPredicate(filter);
		initDropDownList(filter, filter + '_table', range[filter].available);
		range[filter].restricted = new Array();
		$.each(values['restricted'], function(i, value) {
			range[filter].restricted.push(value[0]);
		});
		initDropDownList(filter, 'predicate_' + filter + '_table', range[filter].restricted);
	});
	clickedSubmit = false;
}

/**
 * Update a predicate based on the keyword(s) constraint
 */
function updateQueryPredicate(filter) {
	var values = query_predicate[filter];
	if (values != null) {
		var done = false;
		var rangeArray = range[filter].available; 
		if (rangeArray.length > 0 && rangeArray[0].split('<br/>').length > 1) {
			if (values.length > 0 && values[0].split('<br/>').length <= 1) {
				rangeArray = new Array();
				$.each(range[filter].available, function(i, value) {
					rangeArray.push(value.split('<br/>')[1]);
				});
			}
		}
		while (!done) {
			done = true;
			$.each(values, function(i, value) {
				if (!rangeArray.contains(value)) {
					values.splice(i, 1);
					if (values.length == 0) {
						delete query_predicate[filter];
					} else {
						done = false;
					}
					return false;
				}
			});
		}
	}
}

/**
 * Save the phenotypes to a file
 */
function savePhenotypes() {
	$('#GlobalMenu').slideUp('slow');
	if (oTable != null) {
		$('.FixedHeader_Cloned').css('display', '');
	}
	var sql = getPhenotypesSQL();
	var url = '/pfindr/query';

	var form = $('<form>').appendTo('body');
	form.attr({	action: url,
		method: 'post'});
	var input = $('<input>');
	input.attr({type: 'hidden',
		name: 'predicate'});
	input.val('save');
	form.append(input);

	input = $('<input>');
	input.attr({type: 'hidden',
		name: 'sql'});
	input.val(objectToString(sql));
	form.append(input);

	input = $('<input>');
	input.attr({type: 'hidden',
		name: 'template'});
	input.val(arrayToString(groupByView));
	form.append(input);

	form.submit().remove();
}

/**
 * Save the selected mappings
 * 
 * @param value
 * 	the type of saving: 'markAsCorrect', 'markAsIncorrect' or 'saveSelectedResults'
 */
function saveSelectedResults(value) {
	$('#GlobalMenu').slideUp('slow');
	if (oTable != null) {
		$('.FixedHeader_Cloned').css('display', '');
	}
	var rows = getSelectedRows();
	if (rows.length > 0) {
		// remove the dbGaP link
		var variable_index = 3;
		$.each(rows, function(i, row) {
			var a = $(row[variable_index]);
			if (a.html() != null) {
				row[variable_index] = a.html();
				row.push(a.attr('href'));
			} else {
				row.push('');
			}
		});

		if (value == 'saveSelectedResults') {
			saveSelectedResultsIntoFile(value, rows, null);
		} else {
			var url = '/pfindr/query';
			var obj = new Object();
			obj.predicate = value;
			obj.rows = valueToString(rows);
			var param = new Object();
			param.rows = rows;
			param.mark = value;
			PFINDR.post(url, obj, getMarkSuccess, param, 0);
		}
	}
}

/**
 * save the marked rows into a file
 * 
 * @param data
 * 	the data returned from the server
 * @param textStatus
 * 	the string describing the status
 * @param jqXHR
 * 	the jQuery XMLHttpRequest
 * @param param
 * 	the parameters to be used by the callback success function
 */
function getMarkSuccess(data, textStatus, jqXHR, param) {
	var value = param.mark;
	var rows = param.rows;
	var markedTime = data.markedTime;
	var answer = confirm('The system administrator has been informed about the selected row(s) marked as ' +
			((value == 'markAsCorrect') ? 'correct' : 'incorrect') + '. Do you want to download them?');
	if (answer == true) {
		saveSelectedResultsIntoFile(value, rows, markedTime);
	}
}

/**
 * Save the selected mappings into a file
 * 
 * @param value
 * 	the type of saving: 'markAsCorrect', 'markAsIncorrect' or 'saveSelectedResults'
 * @param rows
 * 	the rows to be saved
 * @param markedTime
 * 	the timestamp the rows were save into the database table
 */
function saveSelectedResultsIntoFile(value, rows, markedTime) {
	var url = '/pfindr/query';
	
	var form = $('<form>').appendTo('body');
	form.attr({	action: url,
		method: 'post'});
	var input = $('<input>');
	input.attr({type: 'hidden',
		name: 'predicate'});
	input.val(value);
	form.append(input);

	input = $('<input>');
	input.attr({type: 'hidden',
		name: 'rows'});
	input.val(valueToString(rows));
	form.append(input);
	
	if (markedTime != null) {
		input = $('<input>');
		input.attr({type: 'hidden',
			name: 'timestamp'});
		input.val(markedTime);
		form.append(input);
	}

	form.submit().remove();
}

/**
 * Display a flyover message
 * 
 * @param e
 * 	the event that triggered the flyover
 * @param content
 * 	the content to be displayed
 */
function DisplayTipBox(e, content) {
	tipBox.html(content);
	var dx = tipBox.width() + 30;
	var delta = dx + 10;
	dx = (e.clientX + delta > $(window).width()) ? $(window).width() - e.clientX - delta : 0;
	tipBox.css('left', String(parseInt(e.pageX + dx) + 'px'));
	tipBox.css('top', String(parseInt(e.pageY - tipBox.height() - 50) + 'px'));
	tipBox.css('display', 'block');
}

/**
 * Hide a flyover message
 */
function HideTipBox() {
	tipBox.css('display', 'none');
}

/**
 * Create a new template
 */
function newTemplate() {
	var ul = $('#new_template_ul');
	ul.html('');
	var groupBy = new Array();
	groupBy = groupBy.concat(groupByView);
	appendTree(ul, PhenotypesColumns.length, groupBy);
	newTemplateDialog.treeview({
		persist: 'location',
		unique: false
	});
	newTemplateDialog.find('.draggable_node').draggable({
		revert: 'invalid',
		helper: 'clone',
		zIndex: -1,
		drag: function(event, ui) {$(this).addClass('highlighted');},
		stop: function(event, ui) {$(this).removeClass('highlighted');}}
	);
	newTemplateDialog.find('.droppable_node').droppable({
		greedy: true,
		hoverClass: 'drophover',
		drop: function( event, ui ) {
			ui.draggable.css('left', '');
			ui.draggable.css('top', '');
			if ($(this).is('LABEL')) {
				ui.draggable.insertAfter($(this));
			} else {
				var label = $(this).find('.empty')[0];
				ui.draggable.insertAfter(label);
			}
		}
	});
	newTemplateDialog.dialog('open');
	$('#new_template_ul').find('div.hitarea.tree-hitarea.expandable-hitarea').css('cursor', 'default');
	$('#new_template_ul').find('div.hitarea.tree-hitarea.expandable-hitarea').unbind('click');
	$('#GlobalMenu').slideUp('slow');
	if (oTable != null) {
		$('.FixedHeader_Cloned').css('display', '');
	}
}

/**
 * Populate the tree for a new template
 * 
 * @param template
 * 	the template to be populated
 */
function setTemplateTree(template) {
	var ul = $('#edit_template_ul');
	ul.html('');
	var groupBy = new Array();
	groupBy = groupBy.concat(groupByTemplates[template]);
	appendTree(ul, PhenotypesColumns.length, groupBy);
	editTemplateDialog.treeview({
		persist: 'location',
		unique: false
	});
	editTemplateDialog.find('.draggable_node').draggable({
		revert: 'invalid',
		helper: 'clone',
		zIndex: -1,
		drag: function(event, ui) {$(this).addClass('highlighted');},
		stop: function(event, ui) {$(this).removeClass('highlighted');}}
	);
	editTemplateDialog.find('.droppable_node').droppable({
		greedy: true,
		hoverClass: 'drophover',
		drop: function( event, ui ) {
			ui.draggable.css('left', '');
			ui.draggable.css('top', '');
			if ($(this).is('LABEL')) {
				ui.draggable.insertAfter($(this));
			} else {
				var label = $(this).find('.empty')[0];
				ui.draggable.insertAfter(label);
			}
		}
	});
	$('#edit_template_ul').find('div.hitarea.tree-hitarea.expandable-hitarea').css('cursor', 'default');
	$('#edit_template_ul').find('div.hitarea.tree-hitarea.expandable-hitarea').unbind('click');
}

/**
 * Edit an existing template
 */
function editTemplate() {
	setTemplateTree($('#selectTemplate').val());
	setTemplates('selectEditTemplate');
	$('#selectEditTemplate').attr('previous', $('#selectTemplate').val());
	$('#selectEditTemplate').val($('#selectTemplate').val());
	editGroupByTemplates = groupByTemplates;
	editTemplateDialog.dialog('open');
	$('#GlobalMenu').slideUp('slow');
	if (oTable != null) {
		$('.FixedHeader_Cloned').css('display', '');
	}
}

/**
 * Render the mapping matches using the new selected template
 */
function selectTemplate() {
	groupByView = groupByTemplates[$('#selectTemplate').val()];
	sortArray = getSortIndexArray(groupByView);
	if ($('#selectTemplate').val() == 'flat') {
		$('.expandCollapseAll').css('display', 'none');
		$('.saveSelectedResults').css({'display': '',
			'color': 'grey'});
	}
	else {
		$('.saveSelectedResults').css('display', 'none');
		$('.expandCollapseAll').css('display', '');
	}
	applyPredicate();
}

/**
 * Render the render the tree of the selected template
 */
function selectEditTemplate() {
	var template = $('#selectEditTemplate').attr('previous');
	applyGroupBy(template);
	setTemplateTree($('#selectEditTemplate').val());
	$('#selectEditTemplate').attr('previous', $('#selectEditTemplate').val());
}

/**
 * Get the WHERE clause for an SQL query
 * 
 * @return the JSON object representing the WHERE predicate
 */
function getWhereClause() {
	var obj = new Object();

	// studies and categories predicates
	$.each(query_predicate, function(predicate, values) {
		if (obj.where == null) {
			obj.where = new Object();
		}
		obj.where[predicate] = new Object();
		obj.where[predicate].op = '=';
		obj.where[predicate].values = new Array();
		$.each(values, function(i, value) {
			var arr = value.split('<br/>');
			obj.where[predicate].values.push(arr.length == 1 ? value : arr[1]);
		});
	});

	// keyword(s) predicate
	var pattern = $('#query_input').val();
	if (pattern.replace(/^\s*/, '').replace(/\s*$/, '') != '') {
		if (obj.where == null) {
			obj.where = new Object();
		}
		if ($('#query_regex').attr('checked') != 'checked') {
			pattern = escapeRegExp(pattern);
		}
		pattern = pattern.replace(/\\/g, "\\\\");
		var keyword = $('input:radio[name=keywordSelect]:checked').val();
		obj.where[keyword] = new Object();
		obj.where[keyword].op = '~*';
		obj.where[keyword].values = new Array();
		obj.where[keyword].values.push(pattern);
	}

	// score predicate
	var hasScore = score1 != 0 || score2 != 1;
	if (hasScore) {
		if (obj.where == null) {
			obj.where = new Object();
		}
		obj.where.score = new Object();
		obj.where.score.op = 'Between';
		obj.where.score.values = new Array();
		obj.where.score.values.push('' + score1);
		obj.where.score.values.push('' + score2);
	}
	return obj;
}

/**
 * Method "escape" for a string
 * escape all the regular expression characters 
 * 
 * @param text
 * 	the string to be escaped
 * @return the escaped string
 */
function escapeRegExp(text) {
	return text.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&");
}

/**
 * Method "escape" for a string
 * escape all the " characters 
 * 
 * @param text
 * 	the string to be escaped
 * @return the escaped string
 */
function escapeDoubleQuotes(text) {
	return text.replace(/"/g, '\\"');
}

/**
 * Gets an object representing the SQL query to be executed
 * 
 * @return the JSON object representing the predicate
 */
function getPhenotypesSQL() {
	var obj = getWhereClause();
	if (PREVIEW_LIMIT != -1) {
		obj.offset = PAGE_PREVIEW * PREVIEW_LIMIT;
		obj.limit = PREVIEW_LIMIT;
	}
	obj.orderBy = new Array();
	$.each(sortArray, function(i, index) {
		obj.orderBy.push(PhenotypesColumns[index]);
	});
	return obj;
}

/**
 * Converts a value to a JSON string representation
 * 
 * @param val
 * 	the value to converted
 * @return the JSON string representation
 */
function valueToString(val) {
	if ($.isArray(val)) {
		return arrayToString(val);
	} else if ($.isPlainObject(val)) {
		return objectToString(val);
	} else if ($.isNumeric(val)) {
		return val;
	} else if ($.isFunction(val)) {
		return '"Function"';
	} else if($.isWindow(val)) {
		return '"Window"';
	} else if ($.isXMLDoc(val)) {
		return '"XMLDoc"';
	} else {
		var valType = $.type(val);
		if (valType == 'string') {
			return '"' + escapeDoubleQuotes(val) + '"';
		} else if (valType == 'object') {
			return '"Object"';
		} else {
			return '"' + valType + '"';
		}
	}
}

/**
 * Converts an object to a JSON string representation
 * 
 * @param obj
 * 	the object to converted
 * @return the JSON string representation
 */
function objectToString(obj) {
	var s = '{';
	var first = true;
	$.each(obj, function(key, val) {
		if (!first) {
			s += ',';
		}
		first = false;
		s += '"' + key + '":' + valueToString(val);
	});
	s += '}';
	return s;
}

/**
 * Converts an array to a JSON string representation
 * 
 * @param obj
 * 	the array to converted
 * @return the JSON string representation
 */
function arrayToString(obj) {
	var s = '[';
	var first = true;
	$.each(obj, function(i, val) {
		if (!first) {
			s += ',';
		}
		first = false;
		s += valueToString(val);
	});
	s += ']';
	return s;
}

/**
 * Load the category tree
 * 
 * @param categoriesRows
 * 	the array of rows to be displayed
 *  each row has the format "definer<br/>category<br/>subcategory"
 * @param id
 * 	the DOM id to which the tree will be built
 *  values will be availableCategories or restrictedCategories
 */
function loadCategories(categoriesRows, id) {
	var categoriesDict = getCategoriesDict(categoriesRows);
	var ul = $('#' + id);
	ul.html('');
	getCategoriesDOM(ul, categoriesDict, new Array());
	$('#' + id).treeview({
		persist: 'location',
		collapsed: true,
		unique: false
	});
}

/**
 * Initialize the dictionary used in building the category tree
 * 
 * @param rows
 * 	the array of rows to be displayed
 * each row has the format "definer<br/>category<br/>subcategory"
 * 
 * @return the dictionary 
 */
function getCategoriesDict(rows) {
	var categoriesDict = new Object();
	$.each(rows, function(index, value) {
		var crtDict = categoriesDict;
		var values = value.split('<br/>');
		var lastValue = null;
		$.each(values, function(i, val) {
			if (val == lastValue) {
				return true;
			}
			if (crtDict[val] == null) {
				crtDict[val] = new Object();
			}
			crtDict = crtDict[val];
			lastValue = val;
		});
	});
	return categoriesDict;
}

/**
 * Build the DOM category tree
 * 
 * @param ul
 * 	the node to append to
 * @param roots
 * 	the values to be appended
 * @param prefix
 * 	the array containing the node predicate
 */
function getCategoriesDOM(ul, roots, prefix) {
	$.each(roots, function(key, obj) {
		prefix.push(key);
		var li = appendCategoryItem(ul, key, prefix);
		prefix.pop();
		if (hasProperties(obj)) {
			var ulObject = $('<ul>');
			li.append(ulObject);
			prefix.push(key);
			getCategoriesDOM(ulObject, obj, prefix);
			prefix.pop();
		}
	});
}

/**
 * Append an item in the category tree
 * 
 * @param ul
 * 	the node to append to
 * @param val
 * 	the item label
 * @param prefix
 * 	the array containing the node predicate
 */
function appendCategoryItem(ul, val, prefix) {
	var li = $('<li>');
	li.addClass('tree');
	ul.append(li);
	var input = $('<input>');
	input.attr('type', 'checkbox');
	input.attr('predicate', prefix.join('<br/>'));
	input.click(function(event) {applyCategoryFilter(event, $(this));});
	li.append(input);
	var label = $('<label>');
	li.append(label);
	label.html(val);
	return li;
}

/**
 * Apply when an item was clicked
 * 
 * @param event
 * 	the click event
 * @param input
 * 	the check box
 */
function applyCategoryFilter(event, input) {
	var checked = input.attr('checked');
	removeChildrenPredicates(input.attr('predicate'));
	if (checked == 'checked') {
		query_categories.push(input.attr('predicate'));
		// check all its direct and indirect children
		$(':checkbox', input.parent()).attr('checked', 'checked');

		// check the parent if all its direct and indirect children are checked
		var li = input.parent().parent().parent();
		if (li.is('li')) {
			var ul = $(li.children('ul')[0]);
			var all = true;
			$.each($(':checkbox', ul), function(i, checkbox) {
				all = all && ($(checkbox).attr('checked') == 'checked');
				if (!all) {
					return false;
				}
			});
			if (all) {
				$(li.children(':checkbox')[0]).attr('checked', 'checked');
				applyCategoryFilter(event, $(li.children(':checkbox')[0]));
			}
		}
	} else {
		removeCategoryPredicate(input.attr('predicate'));
		// uncheck all its direct and indirect children
		$(':checkbox', input.parent()).removeAttr('checked');
	}

	// in case of uncheck, uncheck also all its direct and indirect parents
	if (checked != 'checked') {
		var li = input.parent();
		while (li.parent().parent().is('li')) {
			li = li.parent().parent();
			removeCategoryPredicate($(li.children(':checked')[0]).attr('predicate'));
			li.children(':checked').removeAttr('checked');
			// if any subcategory is selected, added in the query predicate
			$.each($(':checkbox', li), function(i, inp) {
				if ($(inp).attr('checked') == 'checked') {
					var inpPredicate = $(inp).attr('predicate');
					if (!query_categories.contains(inpPredicate)) {
						query_categories.push($(inp).attr('predicate'));
					}
				}
			});
		}
	}
	var predicates = new Array();
	getCategoryPredicate($('#availableCategories'), predicates);
	var predicate = buildJSONCategoryPredicate(predicates);
	query_predicate.category = new Array();
	query_predicate.category.push(predicate);

	// in getWhereClause (1557) for categories
	// obj.where.category = values;

	applyPredicate();
}

/**
 * Load the predicate array for the category
 * 
 * @param ul
 * 	the node for which we are collecting the category predicate
 * @param predicate
 * 	the array that will contain the category predicate
 * each row has the format "definer<br/>category<br/>subcategory"
 */
function getCategoryPredicate(ul, predicate) {
	$.each(ul.children('li'), function (i, li) {
		var input = $($(li).children(':checkbox')[0]);
		if (input.attr('checked') == 'checked') {
			predicate.push(input.attr('predicate'));
		} else {
			getCategoryPredicate($($(li).children('ul')[0]), predicate);
		}
	});
}

/**
 * Get the JSONObject for the category predicate 
 * 
 * @param predicates
 * 	the array that will contain the category predicate
 * each row has the format "definer<br/>category<br/>subcategory"
 * 
 * @return the dictionary 
 */
function buildJSONCategoryPredicate(predicates) {
	var categoryPredicate = new Object();
	categoryPredicate.op = '=';
	categoryPredicate.values = new Array();
	$.each(predicates, function(index, predicate) {
		var items = predicate.split('<br/>');
		var crtPredicate = new Object();
		$.each(items, function(i, val) {
			switch (i) {
			case 0:
				crtPredicate.categorydefiner = val;
				break;
			case 1:
				crtPredicate.category = val;
				break;
			case 2:
				crtPredicate.subcategory = val;
				break;
			}
		});
		categoryPredicate.values.push(crtPredicate);
	});
	return categoryPredicate;
}

/**
 * Expand/collapse the tree of the category predicate 
 * 
 */
function expandCollapse() {
	if ($('#expandCollapse').val() == '+') {
		$('#categoryNavigation').find('div.hitarea.tree-hitarea.expandable-hitarea').click();
		$('#expandCollapse').val('-');
	} else {
		$('#categoryNavigation>li>div.hitarea.tree-hitarea.collapsable-hitarea').click();
		$('#expandCollapse').val('+');
	}
}

/**
 * Remove a category predicate from the query
 * 
 * @param predicate
 * 	the predicate to be removed
 */
function removeCategoryPredicate(predicate) {
	$.each(query_categories, function(i, val) {
		if (val == predicate) {
			query_categories.splice(i, 1);
			return false;
		}
	});
}

/**
 * Remove all subcategory predicates from the query
 * This happens when a category was checked and all its subcategories predicates are not any more necessary
 * @param predicate
 * 	the category predicate that was checked
 */
function removeChildrenPredicates(predicate) {
	var indexArray = new Array();
	// the subcategories will have a pattern containing the <br/> suffix
	var pattern = predicate + '<br/>';
	// collect the array indexes
	$.each(query_categories, function(i, val) {
		if (val.indexOf(pattern) == 0) {
			indexArray.push(i);
		}
	});
	// remove the predicates
	while (indexArray.length > 0) {
		var index = indexArray.pop();
		query_categories.splice(index, 1);
	}
}

/**
 * Check if a predicate already exits direct or indirect (through its parent) in the query
 * @param predicate
 * 	the (sub)category predicate
 */
function hasCategoryPredicate(predicate) {
	var arr = predicate.split('<br/>');
	var ret = false;
	var predicate_array = new Array();
	$.each(arr, function(i, val) {
		predicate_array.push(val);
		var pred = predicate_array.join('<br/>');
		if (query_categories.contains(pred)) {
			ret = true;
			return false;
		}
	});
	return ret;
}

/**
 * Activity after getting a response from applyPredicate()
 * It will check the restrictedCategories that are part of the query
 * 
 * @param ul
 * 	the list with the nodes that might be checked
 */
function checkSeletedCategories(ul) {
	// check selected values
	$.each(ul.children('li'), function (i, li) {
		var input = $($(li).children(':checkbox')[0]);
		var predicate = input.attr('predicate');
		if (hasCategoryPredicate(predicate)) {
			$(':checkbox', $(li)).attr('checked', 'checked');
		} else {
			var ulNode = $('ul', $(li));
			if (ulNode.length > 0) {
				checkSeletedCategories($(ulNode[0]));
			}
		}
	});
}

/**
 * Activity after getting a response from applyPredicate()
 * Initialize the restrictedCategories
 */
function initSeletedCategories() {
	var categoriesDict = getCategoriesDict(query_categories.length > 0 ? query_categories : categoriesRows);
	var ul = $('#restrictedCategories');
	ul.html('');
	getCategoriesDOM(ul, categoriesDict, new Array());
	$('#restrictedCategories').treeview({
		persist: 'location',
		collapsed: (query_categories.length > 0 ? false : true),
		unique: false
	});

	// check selected values
	checkSeletedCategories(ul);
}

//context to be called
function testCategories() {
	// AJAX returns the categories in the array availableCategoriesRows and restrictedCategoriesRows
	loadCategories(availableCategoriesRows, 'availableCategories');
	loadCategories(restrictedCategoriesRows, 'restrictedCategories');
	$('#availableCategories').removeClass('predicate');
	// avoid having duplicates of the class
	$('#restrictedCategories').removeClass('predicate');
	$('#restrictedCategories').addClass('predicate');
}

/**
 * Apply after a keyword radio button was clicked
 * It will reload the resulting mappings if necessary
 */
function applyKeyword() {
	if ($('#query_input').val().replace(/^\s*/, '').replace(/\s*$/, '') != '') {
		//applyPredicate();
	}
}

/**
 * Reset all predicates
 */
function clearPredicate() {
	$('#query_input').val('');
	score1 = 0.5;
	score2 = 1;
	$( '#score_label' ).html( 'Score Range: 0.5 - 1' );
	$('#slider-range').slider('values', [0.5, 1]);
	$('#new_study_div').find(':checkbox').removeAttr('checked');
	$('#new_category_div').find(':checkbox').removeAttr('checked');
	query_predicate = new Object();
	scrollMap = new Object();
	PAGE_PREVIEW = 0;
	PREVIEW_LIMIT = LAST_PREVIEW_LIMIT = 100;
	$('#previewLimit').val('100');
	$('#GlobalMenu').slideUp('slow');
	if (oTable != null) {
		$('.FixedHeader_Cloned').css('display', '');
	}
}

/**
 * Apply the initial predicate
 */
function clearAll() {
	clearPredicate();
	clearStudiesFilter();
	applyPredicate();
}

/**
 * Expand the tree up to an level
 */
function expandTree(node, level, maxlevel) {
	if (level < maxlevel) {
		$.each(node.children(), function (i, li) {
			var div = $(li).find('>div.hitarea.tree-hitarea.expandable-hitarea');
			div.click();
			var ul = $(li).find('>ul');
			expandTree(ul, level+1, maxlevel);
		});
	}
}

/**
 * Check/uncheck all the mappings
 * 
 * @param id
 * 	the id of the studies or categories
 * @param input
 * 	the check box for check/uncheck all
 */
function markUnmarkAll(id, input) {
	var checked = input.attr('checked');
	if (checked == 'checked') {
		$('#' + id).find(':checkbox').attr('checked', 'checked');
	} else {
		$('#' + id).find(':checkbox').removeAttr('checked');
	}
	showHideSelectedResults();
}

/**
 * Show/Hide selected results command
 * 
 */
function showHideSelectedResults() {
	var count = $('tbody', $('#example')).find('input:checked').length;
	if (count == 0) {
		$('.saveSelectedResults').css('color', 'grey');
	} else {
		$('.saveSelectedResults').css('color', 'black');
	}
}

/**
 * Get the selected rows
 * 
 * @return the selected rows
 */
function getSelectedRows() {
	$('#GlobalMenu').slideUp('slow');
	if (oTable != null) {
		$('.FixedHeader_Cloned').css('display', '');
	}
	var selectedRows = new Array();
	$.each($('input:checked', $('tbody', '#example')), function(i, elem) {
		// Get the position of the current data from the node
		var aPos = oTable.fnGetPosition(elem.parentNode);
		// Get a copy of the data array for this row
		var aData = oTable.fnGetData( aPos[0] ).slice(0);
		aData.pop();
		selectedRows.push(aData);
	});
	return selectedRows;
}

/**
 * Bookmark a query
 * 
 */
function bookmarkQuery() {
	$('#GlobalMenu').slideUp('slow');
	var queryName;
	while (true) {
		queryName = prompt("Please enter the bookmark name:");
		if (queryName == null) {
			// cancel
			return;
		}
		queryName = queryName.replace(/^\s*/, "").replace(/\s*$/, "");
		if (queryName.length > 0) {
			break;
		} else {
			alert("Bookmark name can not be empty.");
		}
	}
	var sql = getPhenotypesSQL();
	var obj = new Object();
	obj.bookmark = queryName;
	obj.predicate = 'bookmark';
	obj.sql = objectToString(sql);
	obj.template = $('#selectTemplate').val();
	var url = '/pfindr/query';
	PFINDR.post(url, obj, postBookmarkQuery, null, 0);
}

/**
 * Export a query
 * 
 */
function exportQuery() {
	$('#GlobalMenu').slideUp('slow');
	if (oTable != null) {
		$('.FixedHeader_Cloned').css('display', '');
	}
	var sql = getPhenotypesSQL();
	var url = '/pfindr/query';

	var form = $('<form>').appendTo('body');
	form.attr({	action: url,
		method: 'post'});
	var input = $('<input>');
	input.attr({type: 'hidden',
		name: 'predicate'});
	input.val('export');
	form.append(input);

	input = $('<input>');
	input.attr({type: 'hidden',
		name: 'sql'});
	input.val(objectToString(sql));
	form.append(input);

	input = $('<input>');
	input.attr({type: 'hidden',
		name: 'template'});
	input.val($('#selectTemplate').val());
	form.append(input);

	form.submit().remove();
}

/**
 * Notify the success of bookmarking a query
 * 
 * @param data
 * 	the data returned from the server
 * @param textStatus
 * 	the string describing the status
 * @param jqXHR
 * 	the jQuery XMLHttpRequest
 * @param param
 * 	the parameters to be used by the callback success function
 */
function postBookmarkQuery(data, textStatus, jqXHR, param) {
	var queryNumber = data['bookmark'];
	alert('Query "' + queryNumber + '" was successfully bookmarked.');
}

/**
 * Import a query
 * 
 */
function importQuery() {
	importQueryeDialog.dialog('open');
	$('#GlobalMenu').slideUp('slow');
	if (oTable != null) {
		$('.FixedHeader_Cloned').css('display', '');
	}
}

/**
 * Get Help
 * 
 */
function getHelp() {
	getHelpDialog.dialog('open');
}

/**
 * Submit a query
 * 
 */
function applySubmit() {
	$('#query_input').css('border-bottom-width', '1px');
	$('.hint').css('display', 'none');
	clickedSubmit = true;
	applyPredicate();
}

var cTable = null;
//var createView = ['match score to<br/>new phenotype', 'description', 'variable', 'varset', 'study', 'visit', 'category', 'definer', 'match score to<br/>previous phenotype'];
var createView = ['match score to<br/>new phenotype', '&nbsp;&nbsp;&nbsp;&nbsp;', 'description', 'variable', 'study'];


/**
 * initialize done after the definer page is loaded
 * @param user
 * 	the user who is logged in
 */
function initDefiner(user) {
	userid = user;
	$('#checkUncheckAllVariables').click(function(event) {checkUncheckAllVariables($(this)); });
	$( '#slider-range' ).slider({
		range: true,
		min: 0,
		max: 1,
		step: 0.01,
		values: [ 0, 1 ],
		slide: function( event, ui ) {
			$( '#score_label' ).html( 'Score Range: ' + ui.values[ 0 ] + ' - ' + ui.values[ 1 ] );
			score1 = '' + ui.values[ 0 ];
			score2 = '' + ui.values[ 1 ];
		}
	});
	// green background for the slider
	$('.ui-widget-header', $( '#slider-range' )).css('background', '#ECFFB3 url(images/ui-bg_gloss-wave_35_f6a828_500x100.png) 50% 50% repeat-x');
	$('#score_div').css('width', $('#phenotype').width() * 1.5);
	score1 = 0;
}

/**
 * initialize done after the definer page is loaded
 * @param user
 * 	the user who is logged in
 */
function initExplorer(user) {
	userid = user;
	$('#checkUncheckAllVariables').click(function(event) {checkUncheckAllVariables($(this)); });
	// green background for the slider
	$('.ui-widget-header', $( '#slider-range' )).css('background', '#ECFFB3 url(images/ui-bg_gloss-wave_35_f6a828_500x100.png) 50% 50% repeat-x');
	$('#score_div').css('width', $('#description').width() / 2);
	score1 = 0.5;
	score2 = 1;
	var obj = new Object();
	obj.predicate = 'study_metadata';
	var url = window.location.href;
	var index = url.lastIndexOf('/');
	url = url.substring(0, index+1) + 'query';
	var param = {'initQuery': false};
	PFINDR.post(url, obj, initStudyFilters, param, 0);
}

/**
 * create a phenotype
 * @param buttonClicked
 * 	"true" if the user has clicked the submit button; "false" otherwise
 */
function createPhenotype(buttonClicked, withStudies) {
	
	if ($('#phenotype').val().replace(/^\s*/, '').replace(/\s*$/, '') == '') {
		alert('Please provide a name for the phenotype.');
		return;
	}
	if ($('#description').val().replace(/^\s*/, '').replace(/\s*$/, '') == '') {
		alert('Please provide a description for the phenotype.');
		return;
	}
	var obj = new Object();
	obj.action = 'create';
	obj.phenotype = $('#phenotype').val();
	obj.description = $('#description').val();
	obj.score1 = score1;
	obj.score2 = score2;
	obj.buttonClicked = buttonClicked;
	if (withStudies == true) {
		obj.studies = arrayToString(getSelectedStudies());
	}
	var url = window.location.href;
	$('#define_div').css('display', '');
	$('#saveMappings').css('display', '');
	$('#addMappingsButton').css('display', '');
	$('#addMappingsButton').attr('disabled', 'disabled');
	$('#reviewMappingsButton').css('display', 'none');
	$('#exportMappingsButton').css('display', 'none');
	$('#clearShoppingCartButton').css('display', 'none');
	$('#cancelReviewMappingsButton').css('display', 'none');
	$('#resultingPhenotypesH3').html('Resulting Phenotypes');
	if (cTable != null) {
		cTable.fnDestroy(true);
		$('.FixedHeader_Cloned').remove();
		$('#example').remove();
		FixedHeader.destroy();
		cTable = null;
		var demo = $('#demo');
		demo.html('');
		var table = $('<table>');
		demo.append(table);
		table.attr({'cellpadding': '0',
			'cellspacing': '0',
			'border': '0',
			'id': 'example'});
		table.addClass('display');
		var thead = $('<thead>');
		table.append(thead);
		thead.append($('<tr>'));
		var tfoot = $('<tfoot>');
		table.append(tfoot);
		tfoot.append($('<tr>'));
		var tbody = $('<tbody>');
		table.append(tbody);
	}
	
	var thead = $($('thead', $('#demo'))[0]);
	var tfoot = $($('tfoot', $('#demo'))[0]);
	var tbody = $($('tbody', $('#demo'))[0]);
	var trHead = $($('tr', thead)[0]);
	var trFoot = $($('tr', tfoot)[0]);
	trHead.html('');
	trFoot.html('');
	tbody.html('');
	$.each(createView, function (i, col) {
		var thHead = $('<th>');
		var thFoot = $('<th>');
		thHead.html(col);
		thFoot.html(col);
		trHead.append(thHead);
		trFoot.append(thFoot);
	});
	var th = $('<th>');
	var checkBox = $('<input>');
	checkBox.attr('type', 'checkbox');
	checkBox.click(function(event) {checkUncheckAllRows('example', $(this), ['addMappingsButton']); });
	th.append(checkBox);
	trHead.append(th);
	trFoot.append($('<th>'));
	thead.css('background-color', '#F0FFFF');
	$('#container').css('width', '100%');
	$('#container').css('margin-top', '0px');
	$('#demo').css('margin-top', '0px');
	cTable = $('#example').dataTable( {
		'aLengthMenu': [
		                [10, 25, 50, 100, 500, 1000, 5000, -1],
		                [10, 25, 50, 100, 500, 1000, 5000, 'All']
		                ],
        'iDisplayLength': 100,
        'sDom': '<"top"ilp<"clear">>rt<"bottom"ilp<"clear">>',
        'bServerSide': true,
        'bProcessing': true,
        'sPaginationType': 'full_numbers',
        'bFilter': false,
        'sServerMethod': 'POST',
        'aaSorting': [[0, 'desc']],
        'aoColumns': [null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      { "bSortable": false }],
		'fnServerData': function ( sSource, aoData, fnCallback ) {
			/* Add some extra data to the sender */
			$('.FixedHeader_Cloned').css('display', 'none');
			$('thead', $('#example')).find(':checkbox').removeAttr('checked');
			$('tbody', $('#demo')).css('display', 'none');
			$.ajax({
				'dataType': 'json',
				'type': 'POST',
				'url': sSource,
				'data': aoData,
				'success': function (json) {
					$('tbody', $('#demo')).css('display', '');
					if (withStudies == true) {
						$.each($('input:checked', $('tbody', '#new_study_table')), function(j, elem) {
							// Get the position of the current data from the node
							var aPos = studyListTable.fnGetPosition(elem.parentNode);
							var aData = studyListTable.fnGetData( aPos[0] )[1];
							var a = $(aData);
							if (!json.studies.contains(a.html())) {
								$(elem).click();
								$(elem).parent().parent().hide();
							}
						});
					}
					fnCallback( json );
				}
				});
		},
		'fnDrawCallback': function( oSettings ) {
			$.each($('tbody', $('#example')).find(':checkbox'), function(i, checkbox) {
				$(checkbox).parent().attr('align', 'center');
				$(checkbox).click(function(event) {showHideSelectedMappings(); });
			});
			if ($('.FixedHeader_Cloned').get(0) == null) {
				new FixedHeader( cTable );
				$('#reviewMappingsButton').css('display', '');
			}
			$('.FixedHeader_Cloned').css('display', '');
			$.each($('tbody', $('#example')).find('tr'), function(i, tr) {
				getChild($(tr), 7).addClass('link');
				getChild($(tr), 7).click(function(event) {displayCategoryMappings($(this).html()); });
			});
			showHideSelectedMappings();
		},
		'fnServerParams': function ( aoData ) {
			$.each(obj, function(key, val) {
				aoData.push( { 'name': key, 'value': val } );
			});
		},
		'sAjaxSource': url
	} );
}

/**
 * Check/uncheck all the variables
 * 
 * @param input
 * 	the check box for check/uncheck all
 */
function checkUncheckAllVariables(input) {
	var checked = input.attr('checked');
	if (checked == 'checked') {
		$('#category_variables_tbody').find(':checkbox').attr('checked', 'checked');
	} else {
		$('#category_variables_tbody').find(':checkbox').removeAttr('checked');
	}
}

/**
 * Update the Check/uncheck of all the variables
 * 
 * @param input
 * 	the check box for check/uncheck all
 */
function updateCheckUncheckAllVariables(input) {
	if ($(':checked', $('#category_variables_tbody')).length == $(':checkbox', $('#category_variables_tbody')).length) {
		$('#checkUncheckAllVariables').attr('checked', 'checked');
	} else {
		$('#checkUncheckAllVariables').removeAttr('checked');
	}
}

/**
 * Check/uncheck all the mappings
 * 
 * @param id
 * 	the id of the studies or categories
 * @param input
 * 	the check box for check/uncheck all
 * @param buttonIds
 * 	the array of buttons to enabled/disabled
 */
function checkUncheckAllRows(id, input, buttonIds) {
	var checked = input.attr('checked');
	if ($('tbody', '#' + id).find(':checkbox').length > 0) {
		if (checked == 'checked') {
			$('#' + id).find(':checkbox').attr('checked', 'checked');
			$.each(buttonIds, function(i, buttonId) {
				$('#' + buttonId).removeAttr('disabled');
			});
		} else {
			$('#' + id).find(':checkbox').removeAttr('checked');
			$.each(buttonIds, function(i, buttonId) {
				$('#' + buttonId).attr('disabled', 'disabled');
			});
		}
	}
}

/**
 * Add the selected mappings to the shopping cart
 * 
 * @param fromCategory
 * 	the category the mappings belong to
 *    if null, then the mappings belong to any category
 */
function addSelectedMappings(fromCategory) {
	$('.FixedHeader_Cloned').css('display', '');
	var rows = getSelectedMappings();
	if (rows.length > 0) {
		var url = window.location.href;
		var obj = new Object();
		obj.action = 'addMappings';
		if (fromCategory != null) {
			obj.fromCategory = fromCategory;
		}
		obj.rows = valueToString(rows);
		PFINDR.post(url, obj, postAddSelectedMappings, null, 0);
	}
}

/**
 * Get the selected mappings
 * 
 * @return the selected rows from the data table
 */
function getSelectedMappings() {
	$('.FixedHeader_Cloned').css('display', '');
	var selectedRows = new Array();
	$.each($('input:checked', $('tbody', '#example')), function(i, elem) {
		// Get the position of the current data from the node
		var aPos = cTable.fnGetPosition(elem.parentNode);
		// Get a copy of the data array for this row
		var aData = cTable.fnGetData( aPos[0] ).slice(0);
		aData.pop();
		selectedRows.push(aData);
	});
	return selectedRows;
}

/**
* callback after adding variables to a phenotype
 * 
 * @param data
 * 	the data returned from the server
 * @param textStatus
 * 	the string describing the status
 * @param jqXHR
 * 	the jQuery XMLHttpRequest
 * @param param
 * 	the parameters to be used by the callback success function
 */
function postAddSelectedMappings(data, textStatus, jqXHR, param) {
	var message = data['status'];
	alert(message);
	if (data['fromCategory'] != null) {
		cancelSelectMappings();
	}
}

/**
 * Show/Hide selected results for the "Add" mapping command
 * 
 */
function showHideSelectedMappings() {
	var count = $('tbody', $('#example')).find('input:checked').length;
	if (count == 0) {
		$('#addMappingsButton').attr('disabled', 'disabled');
	} else {
		$('#addMappingsButton').removeAttr('disabled');
	}
}

/**
 * Show/Hide selected results for the "Add" mapping command of a category
 * 
 */
function showHideCategoriesSelectedMappings() {
	var count = $('tbody', $('#example')).find('input:checked').length;
	if (count == 0) {
		$('#topAddCategoryMappingsButton').attr('disabled', 'disabled');
		$('#bottomAddCategoryMappingsButton').attr('disabled', 'disabled');
	} else {
		$('#topAddCategoryMappingsButton').removeAttr('disabled');
		$('#bottomAddCategoryMappingsButton').removeAttr('disabled');
	}
}

/**
 * Show/Hide selected results for the reviewed mappings
 * 
 */
function showHideReviewMappings() {
	var count = $('tbody', $('#example')).find('input:checked').length;
	if (count == 0) {
		$('#exportMappingsButton').attr('disabled', 'disabled');
	} else {
		$('#exportMappingsButton').removeAttr('disabled');
	}
	if ($('tbody', $('#example')).find('td').length == 1) {
		$('#clearShoppingCartButton').attr('disabled', 'disabled');
	} else {
		$('#clearShoppingCartButton').removeAttr('disabled');
	}
}

/**
 * display the mappings of a category
 * @param category
 * 	the category the mappings belong to
 */
function displayCategoryMappings(category) {
	$('#createPhenotypeDiv').css('display', 'none');
	$('#categoryMappingsH2').html('Phenotype: ' + category + '<br/>Match Score Range: [' + score1 + ', ' + score2 + ']');
	$('#categoryMappingsDiv').css('display', '');
	var obj = new Object();
	obj.action = 'addVariables';
	obj.category = category;
	obj.score1 = score1;
	obj.score2 = score2;
	var url = window.location.href;
	$('#saveMappings').css('display', 'none');
	$('#topCategoryMappings').css('display', '');
	$('#bottomCategoryMappings').css('display', '');
	$('#resultingPhenotypesH3').html('Phenotype: "' + category + '"');
	if (cTable != null) {
		cTable.fnDestroy(true);
		$('.FixedHeader_Cloned').remove();
		$('#example').remove();
		FixedHeader.destroy();
		cTable = null;
		var demo = $('#demo');
		demo.html('');
		var table = $('<table>');
		demo.append(table);
		table.attr({'cellpadding': '0',
			'cellspacing': '0',
			'border': '0',
			'id': 'example'});
		table.addClass('display');
		var thead = $('<thead>');
		table.append(thead);
		thead.append($('<tr>'));
		var tfoot = $('<tfoot>');
		table.append(tfoot);
		tfoot.append($('<tr>'));
		var tbody = $('<tbody>');
		table.append(tbody);
	}
	
	var thead = $($('thead', $('#demo'))[0]);
	var tfoot = $($('tfoot', $('#demo'))[0]);
	var tbody = $($('tbody', $('#demo'))[0]);
	var trHead = $($('tr', thead)[0]);
	var trFoot = $($('tr', tfoot)[0]);
	trHead.html('');
	trFoot.html('');
	tbody.html('');
	$.each(createView, function (i, col) {
		var thHead = $('<th>');
		var thFoot = $('<th>');
		thHead.html(col);
		thFoot.html(col);
		trHead.append(thHead);
		trFoot.append(thFoot);
	});
	var th = $('<th>');
	var checkBox = $('<input>');
	checkBox.attr('type', 'checkbox');
	checkBox.click(function(event) {checkUncheckAllRows('example', $(this), ['topAddCategoryMappingsButton', 'bottomAddCategoryMappingsButton']);});
	th.append(checkBox);
	trHead.append(th);
	trFoot.append($('<th>'));
	thead.css('background-color', '#F0FFFF');
	$('#container').css('width', '100%');
	$('#container').css('margin-top', '0px');
	$('#demo').css('margin-top', '0px');
	cTable = $('#example').dataTable( {
		'aLengthMenu': [
		                [10, 25, 50, 100, 500, 1000, 5000, -1],
		                [10, 25, 50, 100, 500, 1000, 5000, 'All']
		                ],
        'iDisplayLength': 100,
        'sDom': '<"top"ilp<"clear">>rt<"bottom"ilp<"clear">>',
        'bServerSide': true,
        'bProcessing': true,
        'sPaginationType': 'full_numbers',
        'bFilter': false,
        'sServerMethod': 'POST',
        'aaSorting': [[0, 'desc']],
        'aoColumns': [null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      { "bSortable": false }],
		'fnServerData': function ( sSource, aoData, fnCallback ) {
			/* Add some extra data to the sender */
			$('.FixedHeader_Cloned').css('display', 'none');
			$('thead', $('#example')).find(':checkbox').removeAttr('checked');
			$('tbody', $('#demo')).css('display', 'none');
			$.ajax({
				'dataType': 'json',
				'type': 'POST',
				'url': sSource,
				'data': aoData,
				'success': function (json) {
					$('tbody', $('#demo')).css('display', '');
					fnCallback( json );
				}
				});
		},
		'fnDrawCallback': function( oSettings ) {
			$.each($('tbody', $('#example')).find(':checkbox'), function(i, checkbox) {
				$(checkbox).parent().attr('align', 'center');
				$(checkbox).click(function(event) {showHideCategoriesSelectedMappings(); });
			});
			if ($('.FixedHeader_Cloned').get(0) == null) {
				new FixedHeader( cTable );
			}
			$('.FixedHeader_Cloned').css('display', '');
			showHideCategoriesSelectedMappings();
		},
		'fnServerParams': function ( aoData ) {
			$.each(obj, function(key, val) {
				aoData.push( { 'name': key, 'value': val } );
			});
		},
		'sAjaxSource': url
	} );
}

/**
 * function called when the "Cancel" button was clicked
 * it returns to the previous view
 */
function cancelSelectMappings() {
	$('#createPhenotypeDiv').css('display', '');
	$('#categoryMappingsDiv').css('display', 'none');
	$('#bottomCategoryMappings').css('display', 'none');
	createPhenotype(false);
}

/**
 * display the selected mappings for review
 */
function reviewSelectedMappings() {
	var obj = new Object();
	obj.action = 'review';
	var url = window.location.href;
	$('#saveMappings').css('display', '');
	$('#addMappingsButton').css('display', 'none');
	$('#exportMappingsButton').css('display', '');
	$('#clearShoppingCartButton').css('display', '');
	$('#cancelReviewMappingsButton').css('display', '');
	$('#reviewMappingsButton').css('display', 'none');
	$('#resultingPhenotypesH3').html('Resulting Phenotypes');
	if (cTable != null) {
		cTable.fnDestroy(true);
		$('.FixedHeader_Cloned').remove();
		$('#example').remove();
		FixedHeader.destroy();
		cTable = null;
		var demo = $('#demo');
		demo.html('');
		var table = $('<table>');
		demo.append(table);
		table.attr({'cellpadding': '0',
			'cellspacing': '0',
			'border': '0',
			'id': 'example'});
		table.addClass('display');
		var thead = $('<thead>');
		table.append(thead);
		thead.append($('<tr>'));
		var tfoot = $('<tfoot>');
		table.append(tfoot);
		tfoot.append($('<tr>'));
		var tbody = $('<tbody>');
		table.append(tbody);
	}
	
	var thead = $($('thead', $('#demo'))[0]);
	var tfoot = $($('tfoot', $('#demo'))[0]);
	var tbody = $($('tbody', $('#demo'))[0]);
	var trHead = $($('tr', thead)[0]);
	var trFoot = $($('tr', tfoot)[0]);
	trHead.html('');
	trFoot.html('');
	tbody.html('');
	$.each(createView, function (i, col) {
		var thHead = $('<th>');
		var thFoot = $('<th>');
		thHead.html(col);
		thFoot.html(col);
		trHead.append(thHead);
		trFoot.append(thFoot);
	});
	var th = $('<th>');
	var checkBox = $('<input>');
	checkBox.attr('type', 'checkbox');
	checkBox.click(function(event) {checkUncheckAllRows('example', $(this), ['exportMappingsButton']); });
	th.append(checkBox);
	trHead.append(th);
	trFoot.append($('<th>'));
	thead.css('background-color', '#F0FFFF');
	$('#container').css('width', '100%');
	$('#container').css('margin-top', '0px');
	$('#demo').css('margin-top', '0px');
	cTable = $('#example').dataTable( {
		'aLengthMenu': [
		                [10, 25, 50, 100, 500, 1000, 5000, -1],
		                [10, 25, 50, 100, 500, 1000, 5000, 'All']
		                ],
        'iDisplayLength': 100,
        'sDom': '<"top"ilp<"clear">>rt<"bottom"ilp<"clear">>',
        'bServerSide': true,
        'bProcessing': true,
        'sPaginationType': 'full_numbers',
        'bFilter': false,
        'sServerMethod': 'POST',
        'aaSorting': [[0, 'desc']],
        'aoColumns': [null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      { "bSortable": false }],
		'fnServerData': function ( sSource, aoData, fnCallback ) {
			/* Add some extra data to the sender */
			$('.FixedHeader_Cloned').css('display', 'none');
			$('thead', $('#example')).find(':checkbox').removeAttr('checked');
			$('tbody', $('#demo')).css('display', 'none');
			$.ajax({
				'dataType': 'json',
				'type': 'POST',
				'url': sSource,
				'data': aoData,
				'success': function (json) {
					$('tbody', $('#demo')).css('display', '');
					fnCallback( json );
				}
				});
		},
		'fnDrawCallback': function( oSettings ) {
			$.each($('tbody', $('#example')).find(':checkbox'), function(i, checkbox) {
				$(checkbox).parent().attr('align', 'center');
				$(checkbox).click(function(event) {showHideReviewMappings(); });
			});
			if ($('.FixedHeader_Cloned').get(0) == null) {
				new FixedHeader( cTable );
			}
			$('.FixedHeader_Cloned').css('display', '');
			showHideReviewMappings();
		},
		'fnServerParams': function ( aoData ) {
			$.each(obj, function(key, val) {
				aoData.push( { 'name': key, 'value': val } );
			});
		},
		'sAjaxSource': url
	} );
}

/**
 * Export the selected mappings
 * 
 */
function exportSelectedMappings() {
	var rows = getShoppingCartRows();
	exportSelectedResultsIntoFile(rows);
}

/**
 * Get the selected rows from the shopping cart
 * 
 * @return the selected rows
 */
function getShoppingCartRows() {
	var selectedRows = new Array();
	$.each($('input:checked', $('tbody', '#example')), function(i, elem) {
		// Get the position of the current data from the node
		var aPos = cTable.fnGetPosition(elem.parentNode);
		// Get a copy of the data array for this row
		var aData = cTable.fnGetData( aPos[0] ).slice(0);
		aData.pop();
		selectedRows.push(aData);
	});
	return selectedRows;
}

/**
 * Save the selected mappings from the shopping cart into a file
 * 
 * @param rows
 * 	the rows to be saved
 */
function exportSelectedResultsIntoFile(rows) {
	var url = '/pfindr/define';

	var form = $('<form>').appendTo('body');
	form.attr({	action: url,
		method: 'post'});
	var input = $('<input>');
	input.attr({type: 'hidden',
		name: 'action'});
	input.val('export');
	form.append(input);

	input = $('<input>');
	input.attr({type: 'hidden',
		name: 'rows'});
	input.val(valueToString(rows));
	form.append(input);
	
	form.submit().remove();
}

/**
 * Clear the shopping cart
 * 
 */
function clearShoppingCart() {
	$('.FixedHeader_Cloned').css('display', '');
	var url = window.location.href;
	var obj = new Object();
	obj.action = 'clearShoppingCart';
	PFINDR.post(url, obj, postClearShoppingCart, null, 0);
}

/**
 * callback after adding variables to a phenotype
 * 
 * @param data
 * 	the data returned from the server
 * @param textStatus
 * 	the string describing the status
 * @param jqXHR
 * 	the jQuery XMLHttpRequest
 * @param param
 * 	the parameters to be used by the callback success function
 */
function postClearShoppingCart(data, textStatus, jqXHR, param) {
	var message = data['status'];
	alert(message);
	$('#clearShoppingCartButton').attr('disabled', 'disabled');
	cancelSelectMappings();
}

/**
 * Register a new user
 * 
 */
function registerNewUser() {
	var firstName = $('#firstName').val().replace(/^\s*/, '').replace(/\s*$/, '');
	var lastName = $('#lastName').val().replace(/^\s*/, '').replace(/\s*$/, '');
	var userid = $('#email').val().replace(/^\s*/, '').replace(/\s*$/, '');
	var email = $('#email').val().replace(/^\s*/, '').replace(/\s*$/, '');
	var institution = $('#institution').val().replace(/^\s*/, '').replace(/\s*$/, '');
	var url = '/pfindr/role';
	var obj = new Object();
	obj.firstName = firstName;
	obj.lastName = lastName;
	obj.userid = userid;
	obj.email = email;
	obj.institution = institution;
	obj.action = 'register';
	PFINDR.post(url, obj, getRegisterSuccess, null, 0);
	
}

/**
* callback after registering a new user
 * 
 * @param data
 * 	the data returned from the server
 * @param textStatus
 * 	the string describing the status
 * @param jqXHR
 * 	the jQuery XMLHttpRequest
 * @param param
 * 	the parameters to be used by the callback success function
 */
function getRegisterSuccess(data, textStatus, jqXHR, param) {
	var success = data['success'];
	if (success) {
		$('#errorDiv').css('display', 'none');
		$('#registerForm').css('display', 'none');
		$('#captchadiv').css('display', 'none');
		$('#registerP').css('display', 'none');
		$('#success').css('display', '');
	} else {
		$('#errorDiv').css('display', '');
		$('#errorP').html(data['error']);
	}
}

/**
 * Display reCAPTCHA
 * 
 */
function showRecaptcha() {
	  //var public_key = '6LfBLtcSAAAAADK86i0u4xF2ISWu-YZUaurpREHD';	// aspc.isi.edu
	  //var public_key = '6Le8LdcSAAAAAGTYw19JscLoGxxzHLIBcDWUfXjF';	// uracil.isi.edu
	  //var public_key = '6Lcrzt8SAAAAAM2v-LJ7XplHS8m5AmBRpGoiRcAA';	// dna.isi.edu
	  var public_key = '6Ld5FtgSAAAAAEaR31QdyIuVGHd9uyHq6g3sG-BM';	// phenoexplorer.org
	  if (window.location.hostname == 'aspc.isi.edu') {
		  public_key = '6LfBLtcSAAAAADK86i0u4xF2ISWu-YZUaurpREHD';
	  }
	  Recaptcha.create(public_key, 'captchadiv', {
	      tabindex: 1,
	      theme: 'red',
	      callback: Recaptcha.focus_response_field
	  });
}

/**
 * Verify reCAPTCHA
 * 
 */
function submitRecaptcha() {
	if ($('#firstName').val().replace(/^\s*/, '').replace(/\s*$/, '') == '') {
		$('#errorDiv').css('display', '');
		$('#errorP').html('The "First Name" field can not be empty.');
		return;
	}
	if ($('#lastName').val().replace(/^\s*/, '').replace(/\s*$/, '') == '') {
		$('#errorDiv').css('display', '');
		$('#errorP').html('The "Last Name" field can not be empty.');
		return;
	}
	if ($('#email').val().replace(/^\s*/, '').replace(/\s*$/, '') == '') {
		$('#errorDiv').css('display', '');
		$('#errorP').html('The "Email" field can not be empty.');
		return;
	}
	
	var url = '/pfindr/role';
	var obj = new Object();
	obj.recaptcha_challenge_field = Recaptcha.get_challenge();
	obj.recaptcha_response_field = Recaptcha.get_response();
	obj.action = 'verify';
	PFINDR.post(url, obj, postSubmitRecaptcha, null, 0);
}

/**
* callback after submitting reCAPTCHA
 * 
 * @param data
 * 	the data returned from the server
 * @param textStatus
 * 	the string describing the status
 * @param jqXHR
 * 	the jQuery XMLHttpRequest
 * @param param
 * 	the parameters to be used by the callback success function
 */
function postSubmitRecaptcha(data, textStatus, jqXHR, param) {
	if (!data['success']) {
		$('#errorDiv').css('display', '');
		$('#errorP').html('Incorrect reCAPTCHA words. Try again.');
		$('#captchadiv').html('');
		showRecaptcha();
	} else {
		registerNewUser();
	}
}

/**
 * toggle the studies filters
 * 
 */
function toggleStudyDetails() {
	var a = $('#studyDetailsA');
	var sign = a.html();
	if (sign == '+') {
		a.html('-');
		$('#studyDetailsDiv').css('display', '');
	} else {
		a.html('+');
		$('#studyDetailsDiv').css('display', 'none');
	}
}

/**
 * initialize the studies filters
 * 
 * @param data
 * 	the data returned from the server
 * @param textStatus
 * 	the string describing the status
 * @param jqXHR
 * 	the jQuery XMLHttpRequest
 * @param param
 * 	the parameters to be used by the callback success function
 */
function initStudyFilters(data, textStatus, jqXHR, param) {
	var select = $('#studyRace');
	select.html('');
	var races = data['races'];
	if (races.length < 12) {
		//select.attr('size', races.length);
	}
	$.each(races, function(i, value) {
		var option = $('<option>');
		option.text(value);
		option.attr('value', value);
		select.append(option);
	});

	select = $('#studyGender');
	select.html('');
	var gender = data['gender'];
	if (gender.length < 12) {
		select.attr('size', gender.length);
	}
	$.each(gender, function(i, value) {
		var option = $('<option>');
		option.text(value);
		option.attr('value', value);
		select.append(option);
	});
	select = $('#studyPlatform');
	select.html('');
	var platform = data['platform'];
	if (platform.length < 12) {
		//select.attr('size', platform.length);
	}
	$.each(platform, function(i, value) {
		var option = $('<option>');
		option.text(value);
		option.attr('value', value);
		select.append(option);
	});
	select = $('#studyType');
	select.html('');
	var study_type = data['study_type'];
	if (study_type.length < 12) {
		//select.attr('size', study_type.length);
	}
	$.each(study_type, function(i, value) {
		var option = $('<option>');
		option.text(value);
		option.attr('value', value);
		select.append(option);
	});
	select = $('#geneticType');
	select.html('');
	var genetic_type = data['genetic_type'];
	if (genetic_type.length < 12) {
		//select.attr('size', genetic_type.length);
	}
	$.each(genetic_type, function(i, value) {
		var option = $('<option>');
		option.text(value);
		option.attr('value', value);
		select.append(option);
	});
	select = $('#diseases');
	select.html('');
	var diseases = data['diseases'];
	$.each(diseases, function(i, value) {
		var option = $('<option>');
		option.text(value);
		option.attr('value', value);
		select.append(option);
	});
	$('#layout_div').css('display', '');
	if (param['initQuery'] == true) {
		initQuery();
	} else {
		var predicateHeight = Math.ceil($(window).height() / 2) + 'px';
		// adjust the wrappers height
		var index = predicateHeight.indexOf('px');
		var columnHeight = parseInt(predicateHeight.substring(0, index));
		var delta = Math.floor(columnHeight - $('#datatable_study_div').find('h3').height());
		$('.predicate_study').css('max-height', delta+'px');

		tipBox = $('#TipBox');
		applyPhenotypeFilter();
		//$('#new_study_img').hover(
		//		function(event) {DisplayTipBox(event, instructionsHovers.new_study_img);}, 
		//		function(){HideTipBox();}
		//); 
		//applyStudiesFilter(true);
	}
}

function applyPhenotypeFilter() {
	var obj = new Object();
	obj.action = 'getQueryFilter';
	var url = window.location.href;
	PFINDR.post(url, obj, postApplyPhenotypeFilter, null, 0);
}

function postApplyPhenotypeFilter(data, textStatus, jqXHR, param) {
	var obj = data['queryDescription'];
	if (obj != null) {
		if (obj.races != null) {
			$('#studyRace').val(obj.races);
		}
		if (obj.genders != null) {
			$('#genders').val(obj.genders);
		}
		if (obj.study_type != null) {
			$('#studyType').val(obj.study_type);
		}
		if (obj.genetic_type != null) {
			$('#geneticType').val(obj.genetic_type);
		}
		if (obj.diseases != null) {
			$('#diseases').val(obj.diseases);
		}
		if (obj.platforms != null) {
			$('#studyPlatform').val(obj.platforms);
		}
		if (obj.ageFrom != null) {
			$('#ageFrom').val(obj.ageFrom);
		}
		if (obj.ageTo != null) {
			$('#ageTo').val(obj.ageTo);
		}
		if (obj.participants != null) {
			$('#participantsFrom').val(obj.participants);
		}
		$('#description').val(obj.description);
		score1 = obj.score;
		$( '#scorePhenotype' ).val(score1);
		if (obj.studies != null) {
			$('#saveStudies').hide();
			$('#bookmarkPhenotype').hide();
			$('#saveVariables').hide();
			$('#spinnerwrapper').show();
			$('#ajaxSpinnerImage').show();
			bookmarkedStudies = obj.studies;
			var reqObj = {};
			var url = window.location.href;
			reqObj.action = 'getBookmarkedStudies';
			reqObj.studies = valueToString(obj.studies);
			PFINDR.post(url, reqObj, postGetBokkmarkedStudies, null, 0);
		} else {
			getFilteredStudies();
		}
	}
}

function postGetBokkmarkedStudies(data, textStatus, jqXHR, param) {
	var params = {};
	params.sql = false;
	postGetFilteredStudies(data, textStatus, jqXHR, params);
}

/**
 * get the studies filter
 * 
 */
function getStudiesMetadataFilter() {
	var sql = null;
	var selectedRaces = $('#studyRace').val();
	if (selectedRaces != null) {
		sql = new Object();
		sql.races = selectedRaces;
	}
	var selectedGenders = $('#studyGender').val();
	if (selectedGenders != null) {
		if (sql == null) {
			sql = new Object();
		}
		sql.genders = selectedGenders;
	}
	var selectedPlatforms = $('#studyPlatform').val();
	if (selectedPlatforms != null) {
		if (sql == null) {
			sql = new Object();
		}
		sql.platforms = selectedPlatforms;
	}
	var selectedStudyTypes = $('#studyType').val();
	if (selectedStudyTypes != null) {
		if (sql == null) {
			sql = new Object();
		}
		sql.study_type = selectedStudyTypes;
	}
	var selectedGeneticTypes = $('#geneticType').val();
	if (selectedGeneticTypes != null) {
		if (sql == null) {
			sql = new Object();
		}
		sql.genetic_type = selectedGeneticTypes;
	}
	var selectedDiseases = $('#diseases').val();
	if (selectedDiseases != null) {
		if (sql == null) {
			sql = new Object();
		}
		sql.diseases = selectedDiseases;
	}
	var ageFrom = $('#ageFrom').val().replace(/^\s*/, '').replace(/\s*$/, '');
	var ageTo = $('#ageTo').val().replace(/^\s*/, '').replace(/\s*$/, '');
	if (ageFrom != '' || ageTo != '') {
		if (sql == null) {
			sql = new Object();
		}
		sql.age = new Object();
		if (ageFrom != '') {
			sql.age.min = ageFrom;
		}
		if (ageTo != '') {
			sql.age.max = ageTo;
		}
	}
	var participantsFrom = $('#participantsFrom').val().replace(/^\s*/, '').replace(/\s*$/, '');
	var participantsTo = '';
	if (participantsFrom != '' || participantsTo != '') {
		if (sql == null) {
			sql = new Object();
		}
		sql.participants = new Object();
		if (participantsFrom != '') {
			sql.participants.min = participantsFrom;
		}
		if (participantsTo != '') {
			sql.participants.max = participantsTo;
		}
	}
	if (sql != null) {
		sql = objectToString(sql);
	}
	return sql;
}

/**
 * Apply the studies filter
 * 
 */
function applyStudiesFilter(changeURL) {
	var obj = new Object();
	obj.predicate = 'apply_filter_study_metadata';
	var sql = getStudiesMetadataFilter();
	if (sql != null) {
		obj.sql = sql;
	}
	var url = window.location.href;
	if (changeURL == true) {
		var index = url.lastIndexOf('/');
		url = url.substring(0, index+1) + 'query';
	}
	PFINDR.post(url, obj, postApplyStudiesFilter, null, 0);
	//toggleStudyDetails();
}

/**
 * initialize the studies table
 * 
 * @param data
 * 	the data returned from the server
 * @param textStatus
 * 	the string describing the status
 * @param jqXHR
 * 	the jQuery XMLHttpRequest
 * @param param
 * 	the parameters to be used by the callback success function
 */
function postApplyStudiesFilter(data, textStatus, jqXHR, param) {
	setStudiesTable(data['studies'], data['flyover']);
}

/**
 * Populate the studies table
 * 
 * @param studies
 * 	the studies to be populated
 * @param flyover
 * 	the flyover text
 */
function setStudiesTableOld(studies, flyover) {
	var studyMetadataDiv = $('#new_study_div');
	if (studyListTable != null) {
		studyListTable.fnDestroy(true);
		$('#new_study_table').remove();
		studyListTable = null;
		studyMetadataDiv.html('');
	}
	var table = $('<table>');
	studyMetadataDiv.append(table);
	table.attr({'cellpadding': '0',
		'cellspacing': '0',
		'border': '0',
		'id': 'new_study_table',
		'width': '100%'});
	table.addClass('display');
	var thead = $('<thead>');
	table.append(thead);
	thead.css('background-color', '#F0FFFF');
	var tr = $('<tr>');
	thead.append(tr);
	var th = $('<th>');
	tr.append(th);
	th.attr({'rowspan': '2'});
	var input = $('<input>');
	input.attr({'type': 'checkbox'});
	input.click(function(event) {checkUncheckAll('new_study_div', input); });
	th.append(input);
	th = $('<th>');
	tr.append(th);
	th.attr({'rowspan': '2'});
	th.html('Name');
	th = $('<th>');
	tr.append(th);
	th.attr({'rowspan': '2'});
	th.html('Race/Ethnicity');
	th = $('<th>');
	tr.append(th);
	th.attr({'rowspan': '2'});
	th.html('#Subjects');
	th = $('<th>');
	tr.append(th);
	th.attr({'rowspan': '2'});
	th.html('Sex');
	th = $('<th>');
	tr.append(th);
	th.attr({'colspan': '2'});
	th.html('Age');
	th = $('<th>');
	tr.append(th);
	th.attr({'rowspan': '2'});
	th.html('Study Design');
	th = $('<th>');
	tr.append(th);
	th.attr({'rowspan': '2'});
	th.html('Genetic Data Type');
	th = $('<th>');
	tr.append(th);
	th.attr({'rowspan': '2'});
	th.html('Platform');
	tr = $('<tr>');
	thead.append(tr);
	th = $('<th>');
	tr.append(th);
	th.html('Min');
	th = $('<th>');
	tr.append(th);
	th.html('Max');
	var tbody = $('<tbody>');
	table.append(tbody);

	studyListTable = $('#new_study_table').dataTable( {
		'bFilter': false,
		//'bInfo': false,
		//'bPaginate': false,
		//'bSort': false,
		'aLengthMenu': [
		                [10, 25, 50, 100, -1],
		                [10, 25, 50, 100, 'All']
		                ],
		'iDisplayLength': 10,
		'sPaginationType': 'full_numbers',
		//'sDom': '<"top"li<"clear">pf<"clear">>t',
		//'sDom': '<"top"lipf>t',
		'sDom': '<"top"lpi>t',
        'aoColumns': [{ "bSortable": false },
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null],
		'fnDrawCallback': function( oSettings ) {
			input.removeAttr('checked');
			var all = $('tbody :checkbox', $('#new_study_table')).length;
			var filters = $('tbody :checked', $('#new_study_table')).length;
			if (all !=0 && all == filters) {
				input.attr('checked', 'checked');
			}
			$('#datatable_study_div').hover(
					function(event) {$('tbody tr', $('#new_study_table')).css('display', '');}, 
					function(){displaySelectedStudies();});
			$('.flyover', $('#new_study_table')).hover(
					function(event) {DisplayTipBox(event, flyover[$(this).html()]);}, 
					function(){HideTipBox();});
			$('tbody :checkbox', $('#new_study_table')).click(function(event) {selectUnselectStudies('new_study_table', $(this));});
			if (query_predicate.study != null) {
				$.each($('a', $('tbody', $('#new_study_table'))), function(i, a) {
					if (query_predicate.study.contains($(a).html())) {
						var tr = $(a).parent().parent();
						getChild(getChild(tr, 1), 1).attr('checked', 'checked');
					}
				});
			}
			displaySelectedStudies();
		},
		'aaData': studies
	} );
}

/**
 * Reset all the studies filters
 */
function clearStudiesFilter() {
	$('option:selected', $('#studyDetailsDiv')).removeAttr('selected');
	$('#ageFrom').val('');
	$('#ageTo').val('');
	$('#participantsFrom').val('');
}

/**
 * Display the selected studies
 */
function displaySelectedStudies() {
	var arr = $('input:checked', $('tbody', $('#new_study_table')));
	if (arr.length > 0) {
		$('input:not(:checked)', $('tbody', $('#new_study_table'))).parent().parent().css('display', 'none');
	}
}

/**
 * Display the selected categories
 */
function displaySelectedCategories() {
	var arr = $('input:checked', $('tbody', $('#new_category_table')));
	if (arr.length > 0) {
		$('input:not(:checked)', $('tbody', $('#new_category_table'))).parent().parent().css('display', 'none');
	}
}

/**
 * Get the selected studies
 * 
 * @return the selected rows from the data table
 */
function getSelectedStudies() {
	var selectedRows = new Array();
	if (studyListTable != null) {
		var rows = studyListTable.fnGetData().length;
		var oSettings = studyListTable.fnSettings();
		var length = oSettings._iDisplayLength;
		var offset = oSettings._iDisplayStart;
		var pages = Math.floor(rows / length);
		if (rows % length != 0) {
			pages++;
		}
		if (pages < 1) {
			pages = 1;
		}
		studyListTable.fnPageChange('first');
		for (var i=0; i < pages; i++) {
			$.each($('input:checked', $('tbody', '#new_study_table')), function(j, elem) {
				// Get the position of the current data from the node
				var aPos = studyListTable.fnGetPosition(elem.parentNode);
				var aData = studyListTable.fnGetData( aPos[0] )[1];
				var a = $(aData);
				//selectedRows.push(a.html());
				selectedRows.push(a.attr('id'));
			});
			if (i < pages - 1) {
				studyListTable.fnPageChange('next');
			}
		}
		studyListTable.fnPageChange('first');
		while(oSettings._iDisplayStart < offset) {
			studyListTable.fnPageChange('next');
		}
	}
	return selectedRows;
}

/**
 * Get the selected mappings
 * 
 * @return the selected rows from the data table
 */
function getSelectedCategories() {
	var selectedRows = new Array();
	$.each($('input:checked', $('tbody', '#new_category_table')), function(i, elem) {
		var label = $(elem).next();
		selectedRows.push(label.html());
	});
	return selectedRows;
}

/**
 * Check/uncheck all the studies
 * 
 * @param id
 * 	the id of the studies
 * @param input
 * 	the check box for check/uncheck all
 */
function selectUnselectStudies(id, input) {
	var selected = input.attr('checked') == 'checked';
	if (!selected) {
		$('thead :checkbox', $('#'+id)).removeAttr('checked');
	} else {
		var all = $('tbody :checkbox', $('#'+id)).length;
		var filters = $('tbody :checked', $('#'+id)).length;
		if (all == filters) {
			$('thead :checkbox', $('#'+id)).attr('checked', 'checked');
		}
	}
}

/**
 * Check/uncheck all the categories
 * 
 * @param id
 * 	the id of the categories
 * @param input
 * 	the check box for check/uncheck all
 */
function selectUnselectCategories(id, input) {
	var selected = input.attr('checked') == 'checked';
	if (!selected) {
		$('h3 :checkbox', $('#'+id)).removeAttr('checked');
	} else {
		var all = $('tbody :checkbox', $('#'+id)).length;
		var filters = $('tbody :checked', $('#'+id)).length;
		if (all == filters) {
			$('h3 :checkbox', $('#'+id)).attr('checked', 'checked');
		}
	}
}

/**
 * Scroll in the categories hints in the drop down list 
 * 
 */
function scrollToCategoryHints() {
	var val = $('#category_input').val().toUpperCase().replace(/^\s*/, "").replace(/\s*$/, "");
	if (val.length == 0) {
		$('.predicate_shell', $('#new_category_div')).scrollTop(0);
	} else {
		var count = 0;
		var found = false;
		$.each($('label', $('#new_category_table')), function(i, label) {
			if ($(label).html().toUpperCase().indexOf(val) != -1) {
				var height = Math.floor($('#new_category_table').height() / $('label', $('#new_category_table')).length * count);
				$('.predicate_shell', $('#new_category_div')).scrollTop(height);
				found = true;
				return false;
			} else {
				count++;
			}
		});
		
		if (!found) {
			$('.predicate_shell', $('#new_category_div')).scrollTop(0);
		}
	}
}

/**
 * create a phenotype
 * @param buttonClicked
 * 	"true" if the user has clicked the submit button; "false" otherwise
 */
function createStudiesPhenotypes(studies) {
	
	// get the list of selected studies
	var obj = new Object();
	obj.action = 'create';
	obj.phenotype = $('#phenotype').val();
	obj.description = $('#description').val();
	obj.score = score1;
	obj.buttonClicked = !fromCheckBox;
	obj.studies = arrayToString(studies);
	$('#define_div').hide();
	if (!fromCheckBox) {
		$('#study_wrapper_div').hide();
	}
	var url = window.location.href;
	$('#addMappingsButton').css('display', '');
	$('#addMappingsButton').attr('disabled', 'disabled');
	$('#reviewMappingsButton').css('display', 'none');
	$('#exportMappingsButton').css('display', 'none');
	$('#clearShoppingCartButton').css('display', 'none');
	$('#cancelReviewMappingsButton').css('display', 'none');
	$('#resultingPhenotypesH3').html('Phenotypes');
	if (cTable != null) {
		cTable.fnDestroy(true);
		$('#example').remove();
		cTable = null;
		var demo = $('#demo');
		demo.html('');
		var table = $('<table>');
		demo.append(table);
		table.attr({'cellpadding': '0',
			'cellspacing': '0',
			'border': '0',
			'id': 'example'});
		table.addClass('display');
		var thead = $('<thead>');
		table.append(thead);
		thead.append($('<tr>'));
		var tfoot = $('<tfoot>');
		table.append(tfoot);
		tfoot.append($('<tr>'));
		var tbody = $('<tbody>');
		table.append(tbody);
	}
	
	var thead = $($('thead', $('#demo'))[0]);
	var tfoot = $($('tfoot', $('#demo'))[0]);
	var tbody = $($('tbody', $('#demo'))[0]);
	var trHead = $($('tr', thead)[0]);
	var trFoot = $($('tr', tfoot)[0]);
	trHead.html('');
	trFoot.html('');
	tbody.html('');
	$.each(createView, function (i, col) {
		var thHead = $('<th>');
		var thFoot = $('<th>');
		thHead.html(col);
		thFoot.html(col);
		trHead.append(thHead);
		trFoot.append(thFoot);
		if (i==0) {
			thHead.addClass('match_score');
			thFoot.addClass('match_score');
		} else if (i==1) {
			thHead.attr('width', '1%');
			thFoot.attr('width', '1%');
		}
	});
	var th = $('<th>');
	var checkBox = $('<input>');
	checkBox.attr('type', 'checkbox');
	checkBox.click(function(event) {checkUncheckAllRows('example', $(this), ['addMappingsButton']); });
	th.append(checkBox);
	trHead.append(th);
	trFoot.append($('<th>'));
	thead.css('background-color', '#F0FFFF');
	$('#container').css('width', '100%');
	$('#container').css('margin-top', '0px');
	$('#demo').css('margin-top', '0px');
	cTable = $('#example').dataTable( {
		'aLengthMenu': [
		                [10, 25, 50, 100, 500, 1000, 5000, -1],
		                [10, 25, 50, 100, 500, 1000, 5000, 'All']
		                ],
        'iDisplayLength': 100,
        'sDom': '<"top"ilp<"clear">>rt<"bottom"ilp<"clear">>',
        'bServerSide': true,
        'bProcessing': true,
        'sPaginationType': 'full_numbers',
        'bFilter': false,
        'sServerMethod': 'POST',
        'aaSorting': [[0, 'desc']],
        'aoColumns': [null,
                      { "bSortable": false },
                      null,
                      null,
                      null,
                      { "bSortable": false }],
		'fnServerData': function ( sSource, aoData, fnCallback ) {
			/* Add some extra data to the sender */
			$('thead', $('#example')).find(':checkbox').removeAttr('checked');
			$('tbody', $('#demo')).css('display', 'none');
			$.ajax({
				'dataType': 'json',
				'type': 'POST',
				'url': sSource,
				'data': aoData,
				'timeout': AJAX_TIMEOUT,
				'error': function(jqXHR, textStatus, errorThrown) {
					$('#ajaxSpinnerImage').hide();
					handleError(jqXHR, textStatus, errorThrown, null, url, null, null, null, MAX_RETRIES);
				},
				'success': function (json) {
					$('.query-header').css('background', '#024A68');
					$('.query-header').css('border', '1px solid #024A68');
					$('tbody', $('#demo')).css('display', '');
					$('#study_wrapper_div').show();
					$('#define_div').css('display', '');
					if (!fromCheckBox) {
						var dataDict = {};
						var tempStudies = [];
						var dictStudies = {};
						var dictStudiesIds = {};
						$.each(json.studies, function(i, row) {
							var study = $(row[0]).html();
							dictStudies[study] = row[1];
							var index1 = row[0].indexOf('?study_id=') + '?study_id='.length;
							if (row[0][index1] == '"') {
								index1++;
							}
							var index2 = row[0].indexOf('>', index1);
							if (row[0][index2-1] == '"') {
								index2--;
							}
							var val = row[0].substring(index1, index2);
							dictStudiesIds[study] = val;
						});
						$.each(json.studies, function(i, row) {
							tempStudies.push($(row[0]).html());
						});
						$.each(filteredStudiesData.studies, function(i, row) {
							var a = $(row[1]);
							if (tempStudies.contains(a.html())) {
								var tr = [];
								$.each(row, function(j, td) {
									if (j == 2) {
										tr.push(dictStudies[a.html()]);
									} else {
										tr.push(td);
									}
								});
								dataDict[a.html()] = tr;
							}
						});
						$.each(tempStudies, function(i, study) {
							if (dataDict[study] == null) {
								var studyId = dictStudiesIds[study];
								$.each(filteredStudiesData.studies, function(k, row) {
									var a = $(row[1]);
									var index1 = row[1].indexOf('?study_id=') + '?study_id='.length;
									var index2 = row[1].indexOf('"', index1);
									var crtStudyId = row[1].substring(index1, index2);
									if (studyId == crtStudyId) {
										var tr = [];
										$.each(row, function(j, td) {
											if (j == 2) {
												tr.push(dictStudies[study]);
											} else {
												tr.push(td);
											}
										});
										dataDict[study] = tr;
									}
								});
							}
						});
						filteredStudiesData.studies = [];
						$.each(tempStudies, function(i, study) {
							if (dataDict[study] != null) {
								filteredStudiesData.studies.push(dataDict[study]);
							}
						});
						setStudiesTable(filteredStudiesData.studies, filteredStudiesData['flyover']);
					}
					fromCheckBox = false;
					if (filteredStudiesData.studies.length > 0) {
						$('#saveStudies').show();
						$('#bookmarkPhenotype').show();
						$('#saveVariables').show();
					}
					fnCallback( json );
					$.each($('tr', $('tbody', $('#demo'))), function(i, tr) {
						var td = $(tr).children()[0];
						$(td).addClass('match_score');
						td = $(tr).children()[1];
						$(td).attr('width', '1%');
					});
					$('.match_score').hide();
					if (debug == true) {
						$('#show_score_div').show();
						if ($('#showHideMatchScores').val() == 'Hide Scores') {
							$('.match_score').show();
						}
					}
					$('#ajaxSpinnerImage').hide();
				}
				});
		},
		'fnDrawCallback': function( oSettings ) {
			$.each($('tbody', $('#example')).find(':checkbox'), function(i, checkbox) {
				$(checkbox).parent().attr('align', 'center');
				$(checkbox).click(function(event) {showHideSelectedMappings(); });
			});
			showHideSelectedMappings();
		},
		'fnServerParams': function ( aoData ) {
			$.each(obj, function(key, val) {
				aoData.push( { 'name': key, 'value': val } );
			});
		},
		'sAjaxSource': url
	} );
}

/**
 * Apply the studies filter
 * 
 */
function getFilteredStudies() {
	//if ($('#description').val().replace(/^\s*/, '').replace(/\s*$/, '') == '') {
	//	alert('Please provide a description for the phenotype.');
	//	return;
	//}
	$('#saveStudies').hide();
	$('#bookmarkPhenotype').hide();
	$('#saveVariables').hide();
	$('#spinnerwrapper').show();
	$('#ajaxSpinnerImage').show();
	var ret = bookmarkedStudies;
	if (ret == null) {
		ret = getSelectedStudies();
	}
	if (ret.length > 0) {
		createStudiesPhenotypes(ret);
	} else {
		var obj = new Object();
		obj.predicate = 'apply_filter_study_metadata';
		var param = {};
		var sql = getStudiesMetadataFilter();
		if (sql != null) {
			obj.sql = sql;
			param.sql = true;
		} else {
			param.sql = false;
		}
		var url = window.location.href;
		var index = url.lastIndexOf('/');
		url = url.substring(0, index+1) + 'query';
		PFINDR.post(url, obj, postGetFilteredStudies, param, 0);
	}
}

function postGetFilteredStudies(data, textStatus, jqXHR, param) {
	filteredStudiesData = data;
	var temp = [];
	if (param.sql == true) {
		$.each(filteredStudiesData.studies, function(i, row) {
			var a = $(row[1]);
			temp.push(a.attr('id'));
		});
		if (temp.length == 0) {
			setStudiesTable(filteredStudiesData.studies, filteredStudiesData['flyover']);
			$('#ajaxSpinnerImage').hide();
			$('#study_wrapper_div').show();
			return;
		}
	} else if (bookmarkedStudies != null) {
		temp = bookmarkedStudies;
		bookmarkedStudies = null;
	}
	createStudiesPhenotypes(temp);
}

/**
 * Apply the initial predicate
 */
function clearAllPhenotypes() {
	$('input:checked', $('tbody', '#new_study_table')).removeAttr('checked');
	clearStudiesFilter();
	$('#showHideMatchScores').val('Show Scores');
	$('#show_score_div').hide();
	$('#study_wrapper_div').hide();
	$('#define_div').hide();
	$('#saveMappings').hide();
	$('#bottomCategoryMappings').hide();
	$('#bookmarkPhenotype').hide();
	$('#saveStudies').hide();
	$('#saveVariables').hide();
	score1 = 0.1;
	$( '#scorePhenotype' ).val(score1);
	$('#description').val('');
}

/**
 * Populate the studies table
 * 
 * @param studies
 * 	the studies to be populated
 * @param flyover
 * 	the flyover text
 */
function setStudiesTable(studies, flyover) {
	var studyMetadataDiv = $('#new_study_div');
	if (studyListTable != null) {
		studyListTable.fnDestroy(true);
		$('#new_study_table').remove();
		studyListTable = null;
		studyMetadataDiv.html('');
	}
	var table = $('<table>');
	studyMetadataDiv.append(table);
	table.attr({'cellpadding': '0',
		'cellspacing': '0',
		'border': '0',
		'id': 'new_study_table',
		'width': '100%'});
	table.addClass('display');
	table.css('font', '80%/1.45em "Lucida Grande", Verdana, Arial, Helvetica, sans-serif');
	var thead = $('<thead>');
	table.append(thead);
	thead.css('background-color', '#F0FFFF');
	var tr = $('<tr>');
	thead.append(tr);
	var th = $('<th>');
	th.css('width', '1%');
	tr.append(th);
	th.attr({'rowspan': '2'});
	var input = $('<input>');
	input.attr({'type': 'checkbox'});
	input.click(function(event) {checkUncheckAll('new_study_div', input); });
	th.append(input);
	th = $('<th>');
	tr.append(th);
	th.attr({'rowspan': '2'});
	th.html('Name');
	th = $('<th>');
	tr.append(th);
	th.attr({'rowspan': '2'});
	th.html('Matching Variables');
	th = $('<th>');
	tr.append(th);
	th.attr({'rowspan': '2'});
	th.html('Race/Ethnicity');
	th = $('<th>');
	tr.append(th);
	th.attr({'rowspan': '2'});
	th.html('#Subjects');
	th = $('<th>');
	tr.append(th);
	th.attr({'rowspan': '2'});
	th.html('Sex');
	th = $('<th>');
	tr.append(th);
	th.attr({'colspan': '2'});
	th.html('Age');
	th = $('<th>');
	tr.append(th);
	th.attr({'rowspan': '2'});
	th.html('Study Design');
	th = $('<th>');
	tr.append(th);
	th.attr({'rowspan': '2'});
	th.html('Genetic Data Type');
	th = $('<th>');
	tr.append(th);
	th.attr({'rowspan': '2'});
	th.html('Platform');
	th = $('<th>');
	tr.append(th);
	th.attr({'rowspan': '2'});
	//th.css('min-width', '50%');
	th.html('Diseases');
	tr = $('<tr>');
	thead.append(tr);
	th = $('<th>');
	tr.append(th);
	th.html('Min');
	th = $('<th>');
	tr.append(th);
	th.html('Max');
	var tbody = $('<tbody>');
	table.append(tbody);

	studyListTable = $('#new_study_table').dataTable( {
		'bFilter': false,
		//'bInfo': false,
		//'bPaginate': false,
		//'bSort': false,
		'aLengthMenu': [
		                [10, 25, 50, 100, -1],
		                [10, 25, 50, 100, 'All']
		                ],
		'iDisplayLength': -1,
		'sPaginationType': 'full_numbers',
		//'sDom': '<"top"li<"clear">pf<"clear">>t',
		//'sDom': '<"top"lipf>t',
		'sDom': '<"top"lpi>t',
        'aoColumns': [{ "bSortable": false },
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null],
		'fnDrawCallback': function( oSettings ) {
			input.removeAttr('checked');
			var all = $('tbody :checkbox', $('#new_study_table')).length;
			var filters = $('tbody :checked', $('#new_study_table')).length;
			if (all !=0 && all == filters) {
				input.attr('checked', 'checked');
			}
			$('.flyover', $('#new_study_table')).hover(
					function(event) {DisplayTipBox(event, flyover[$(this).html()]);}, 
					function(){HideTipBox();});
			$('tbody :checkbox', $('#new_study_table')).click(function(event) {applyStudyFilter(event);});
		},
		'aaData': studies
	} );
}

function applyStudyFilter(event) {
	if (fromCheckBox) {
		return;
	}
	fromCheckBox = true;
	getFilteredStudies();
}

/**
 * Bookmark a query
 * 
 */
function bookmarkPhenotype() {
	var queryName;
	while (true) {
		queryName = prompt("Please enter the bookmark name:");
		if (queryName == null ) {
			// cancel
			return;
		}
		queryName = queryName.replace(/^\s*/, "").replace(/\s*$/, "");
		if (queryName.length > 0) {
			break;
		} else {
			alert("Bookmark name can not be empty.");
		}
	}
	var sql = getExplorePhenotypesSQL();
	var selStudies = getSelectedStudies();
	if (selStudies.length > 0) {
		sql.studies = selStudies;
	}
	sql.description = $('#description').val();
	sql.score = score1;
	var obj = {};
	obj.sql = valueToString(sql);
	obj.bookmark = queryName;
	obj.action = 'bookmark';
	var url = '/pfindr/explore';
	PFINDR.post(url, obj, postBookmarkPhenotype, null, 0);
}

function getExplorePhenotypesSQL() {
	var obj = {};
	var selectedRaces = $('#studyRace').val();
	if (selectedRaces != null) {
		obj.races = selectedRaces;
	}
	var selectedGenders = $('#studyGender').val();
	if (selectedGenders != null) {
		obj.genders = selectedGenders;
	}
	var selectedPlatforms = $('#studyPlatform').val();
	if (selectedPlatforms != null) {
		obj.platforms = selectedPlatforms;
	}
	var selectedStudyTypes = $('#studyType').val();
	if (selectedStudyTypes != null) {
		obj.study_type = selectedStudyTypes;
	}
	var selectedGeneticTypes = $('#geneticType').val();
	if (selectedGeneticTypes != null) {
		obj.genetic_type = selectedGeneticTypes;
	}
	var selectedDiseases = $('#diseases').val();
	if (selectedDiseases != null) {
		obj.diseases = selectedDiseases;
	}
	var ageFrom = $('#ageFrom').val().replace(/^\s*/, '').replace(/\s*$/, '');
	var ageTo = $('#ageTo').val().replace(/^\s*/, '').replace(/\s*$/, '');
	if (ageFrom != '' || ageTo != '') {
		if (ageFrom != '') {
			obj.ageFrom = ageFrom;
		}
		if (ageTo != '') {
			obj.ageTo = ageTo;
		}
	}
	var participantsFrom = $('#participantsFrom').val().replace(/^\s*/, '').replace(/\s*$/, '');
	if (participantsFrom != '') {
		if (participantsFrom != '') {
			obj.participants = participantsFrom;
		}
	}
	return obj;
}

/**
 * Notify the success of bookmarking a query
 * 
 * @param data
 * 	the data returned from the server
 * @param textStatus
 * 	the string describing the status
 * @param jqXHR
 * 	the jQuery XMLHttpRequest
 * @param param
 * 	the parameters to be used by the callback success function
 */
function postBookmarkPhenotype(data, textStatus, jqXHR, param) {
	var queryNumber = data['bookmark'];
	alert('Query "' + queryNumber + '" was successfully bookmarked.');
}

function saveStudies() {
	var url = '/pfindr/explore';

	var form = $('<form>').appendTo('body');
	form.attr({	action: url,
		method: 'post'});
	var input = $('<input>');
	input.attr({type: 'hidden',
		name: 'action'});
	input.val('saveStudies');
	form.append(input);

	form.submit().remove();
}

function saveVariables() {
	var url = '/pfindr/explore';

	var form = $('<form>').appendTo('body');
	form.attr({	action: url,
		method: 'post'});
	var input = $('<input>');
	input.attr({type: 'hidden',
		name: 'action'});
	input.val('saveVariables');
	form.append(input);

	form.submit().remove();
}

function setScore() {
	score1 = $('#scorePhenotype').val();
}

function togglePlatform() {
	if ($('#platformLabel').html()[0] == '+') {
		$('#platformLabel').html('- Platform');
		$('#platformSelect').css('visibility', 'visible');
	} else {
		$('#platformLabel').html('+ Platform');
		$('#platformSelect').css('visibility', 'hidden');
	}
}

function toggleMatchScores() {
	if ($('#showHideMatchScores').val() == 'Show Scores') {
		$('.match_score').show();
		$('#showHideMatchScores').val('Hide Scores');
	} else {
		$('.match_score').hide();
		$('#showHideMatchScores').val('Show Scores');
	}
}

function submitQuery() {
	$('input:checked', $('tbody', '#new_study_table')).removeAttr('checked');
	$('#showHideMatchScores').val('Show Scores');
	$('#show_score_div').hide();
	$('#study_wrapper_div').hide();
	$('#define_div').hide();
	$('#saveMappings').hide();
	$('#bottomCategoryMappings').hide();
	$('#bookmarkPhenotype').hide();
	$('#saveStudies').hide();
	$('#saveVariables').hide();
	getFilteredStudies();
}

function moreLikeThis(value) {
	$('#description').val(value);
	getFilteredStudies();
}

