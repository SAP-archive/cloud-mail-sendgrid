$(document).ready(init);

var infoPanel = new InfoPanel("info_panel");
var warningPanel = new InfoPanel("warning_panel");
var dangerPanel = new InfoPanel("danger_panel");
var actions = new Actions();

function init() {
	var objects = new Objects();
	objects.initButtons();
	objects.initDropDowns();
	actions.templates.getTemplates();
}

function Objects() {
	function initButtons() {
		$("button").each(function(index, button) {
			if (typeof ButtonActions[button.id]) {
				$(button).on("click", ButtonActions[button.id]);
			};		
		});
	}

	function initDropDowns() {
		$('.dropdown').each(function(index, dd) {
			if (typeof ButtonActions[dd.id]) {
				$(dd).on('click', 'ul>li>a', function() {
					actions.templates.getTemplate(this.getAttribute("data-template-id"));
				});
			}
		});
	}

	return {
		initButtons : initButtons,
		initDropDowns : initDropDowns
	}
}

var ButtonActions = {
	b_HTTP_SEND : function() {
		actions.clearPanels();
		var formVars = actions.getFormVars();
		formVars.action = "send";
		$.ajax({
			url: '/http',
			data: formVars,
			type: 'POST'
		})
		.done(function(data) {
			infoPanel.write(data);
		})
		.fail(function(err) {
			console.error(err.statusCode + " " + err.statusText);
			dangerPanel.write(err);
		})
		.always(function() {
			warningPanel.write("Action completed.");
		});
	}
}

function InfoPanel(id) {
	var domDiv = document.querySelector('#' + id);
	
	function write(data) {
		domDiv.innerHTML = data;
	}

	function append(data) {
		domDiv.innerHTML = domDiv.innerHTML + data;
	}

	function clear() {
		domDiv.innerHTML = "";
	}

	function writeln(data) {
		append(data + "<br>");
	}

	return {
		write : write,
		append : append,
		clear : clear,
		writeln : writeln
	}
}

function Actions() {
	function getFormVars() {
		var returnObj = {};
		var formElements = document.forms[0].elements, i = 0;
		for (; i < formElements.length; i++) { 
			returnObj[formElements[i].id] = formElements[i].value; 
		}

		return returnObj; 
	}

	function clearPanels() {
		infoPanel.clear();
		warningPanel.clear();
		dangerPanel.clear();
	}

	var templates = new function() {
		function getTemplates() {
			actions.clearPanels();
			$.ajax({
				url: '/templates',
				/*data: formVars,*/
				type: 'GET'
			})
			.done(function(data) {
				//infoPanel.write(data);
				populateDropdown(JSON.parse(data));
			})
			.fail(function(err) {
				console.error(err.statusCode + " " + err.statusText);
				dangerPanel.write(err);
			})
			.always(function() {
				warningPanel.write("Action completed.");
			});
		}

		function getTemplate(templateId) {
			if (templateId == null) {
				enableEditing();
				return;
			}
			actions.clearPanels();
			$.ajax({
				url: '/templates',
				data: {"templateId" : templateId},
				type: 'GET'
			})
			.done(function(data) {
				//infoPanel.write(data);
				//populateDropdown(JSON.parse(data));
				disableEditing(JSON.parse(data));
				//console.log(data);
			})
			.fail(function(err) {
				console.error(err.statusCode + " " + err.statusText);
				dangerPanel.write(err);
			})
			.always(function() {
				warningPanel.write("Action completed.");
			});
		}

		function populateDropdown(data) {
			var dropDownList = document.querySelector("#dropdownTemplatesDiv ul");
			if (!data.templates)
				return;
			data.templates.forEach(function(elem) {
				var li = document.createElement("li");
				li.innerHTML = "<a data-template-id = '" + elem.id + "' href='#'>" + elem.name + "</a>";
				dropDownList.appendChild(li);
			});
		}

		function disableEditing(templateData) {
			document.querySelector("#templateLabel").innerHTML = templateData.name;
			document.querySelector("#templateId").value = templateData.id;
			document.querySelector("#mailtext").value = templateData.versions[0].plain_content;
			document.querySelector("#mailtext").disabled = true;
		}

		function enableEditing() {
			document.querySelector("#templateLabel").innerHTML = "No Template";
			document.querySelector("#templateId").value = "";
			document.querySelector("#mailtext").value = "Mail Text";
			document.querySelector("#mailtext").disabled = false;
		}

		return {
			getTemplates : getTemplates,
			getTemplate : getTemplate
		}
	}

	return {
		getFormVars : getFormVars,
		clearPanels : clearPanels,
		templates : templates
	}
}

