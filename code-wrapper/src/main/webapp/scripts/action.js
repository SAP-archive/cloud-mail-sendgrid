$(document).ready(init);

var infoPanel = new InfoPanel("info_panel");
var warningPanel = new InfoPanel("warning_panel");
var dangerPanel = new InfoPanel("danger_panel");
var actions = new Actions();

function init() {
	var objects = new Objects();
	objects.initButtons();
}

function Objects() {
	function initButtons() {
		$("button").each(function(index, button) {
			if (typeof ButtonActions[button.id]) {
				$(button).on("click", ButtonActions[button.id]);
			}
			
		});
	}

	return {
		initButtons : initButtons
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

	return {
		getFormVars : getFormVars,
		clearPanels : clearPanels
	}
}

