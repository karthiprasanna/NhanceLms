function callMathJax() {
	MathJax.Hub.Queue([ "Typeset", MathJax.Hub ]);
}
var mathJaxParseTimeout;
function appendQus(position, content, options, rightAnswer, userAnswer,
		attemptResult, qId) {
	var holder = $("#questionsHolder");
	var tr = holder.find(".quesRow").get(position);
	if (!tr) {
		tr = $(holder.find(".quesRow").get(0)).clone();
		holder.append(tr);
	}
	tr = $(tr).removeClass("nonner");
	
	//making a check for Numeric by validating 'options' to null
	var userAnswerText,rightAnswerText;
	if(options.length>0){
		console.log(options.length);
		userAnswerText=getAnswerText(userAnswer);
		rightAnswerText=getAnswerText(rightAnswer);
	}else{
		userAnswerText=userAnswer;
		rightAnswerText=rightAnswer;
	}
	
	tr.find(".yourAnsVal").removeClass("correct").removeClass("incorrect")
			.addClass(attemptResult).text(userAnswerText);
	tr.find(".corAnsVal").text(rightAnswerText);
	var ques = tr.find(".question");
	ques.find(".question_body").html(htmlDecode(content));
	ques.find(".qus_no").text(position);

	if (options.length > 0) {
		drawQuesOptions(ques, options);
	}

	createViewSolutionButton(ques, qId, position);

	if (mathJaxParseTimeout) {
		clearTimeout(mathJaxParseTimeout);
	}
	mathJaxParseTimeout = setTimeout(function() {
		MathJax.Hub.Queue([ "Typeset", MathJax.Hub ]);
	}, 100);
}
function createViewSolutionButton(ques, qId, qusNo) {
	ques
			.append("<tr><td colspan='3' style='text-align:right;'><span class='viewSolution' onClick=\"showQuestionPage('"
					+ qId + "'," + qusNo + ")\">View Solution</span></td></tr>");
}

function showQuestionPage(qId, qusNo) {
	questionJSInterface.openSingleQuestionPopup(qId, qusNo);
}

var getAnswerText = function(ans) {
	if (ans === "-") {
		return ans;
	}
	try {
		var finalAns = "";
		var newAnsList = ans.split(",");
		for ( var i = 0, l = newAnsList.length; i < l; i++) {
			finalAns += optionsAlphabets[(parseInt(newAnsList[i]) - 1)];
		}
		return finalAns;
	} catch (err) {
		return ans;
	}
};
function removeAllQus() {
	var holder = $("#questionsHolder");
	var trs = holder.find(".quesRow");
	for ( var i = 1; i < trs.length; i++) {
		$(trs.get(i)).remove();
	}
}
function delQuestionData(position) {
	var holder = $("#questionsHolder");
	var tr = holder.find("tr").get(position);
	if (position == 0) {
		$(tr).addClass("nonner");
	} else {
		$(tr).remove();
	}
}

function htmlDecode(str) {
	return $("#htmlDecode").html(str).text();
}
function drawQuesOptions(ques, options) {
	try {
		var ntrs = ques.find(".quesOpt");
		var i = 0;
		for (i = 0; i < options.length; i++) {
			var ntr = ntrs.get(i);
			if (!ntr) {
				ntr = document.createElement("tr");
				ques.append(ntr);
			}
			ntr = $(ntr);
			ntr.addClass("quesOpt").html("");
			ntr.append("<td></td>");
			// ntr.append("<td class=''></td>");
			var option = "<td class='option'>";
			option += "<p class='option_value'>" + optionsAlphabets[i]
					+ htmlDecode(options[i]) + "</p></td>";
			ntr.append(option);
		}
		for (; i < ntrs.length; i++) {
			$(ntrs.get(i)).remove();
		}
		if (ntr.find("img").get(0)) {
			ntr.find(".opt_no").addClass("vertAlignTop");
		}
	} catch (err) {
		console.error(err);
	}
}
function addQuestionData(qId, content, options, position, solution,
		isShowSolution) {
	var ques = $("#question");
	ques.find("#question_body").html(htmlDecode(content));
	ques.find("#qus_no").text(position);
	drawQuesOptions(ques, options);
	showSolution(qId, solution, isShowSolution);
	MathJax.Hub.Queue([ "Typeset", MathJax.Hub ], document
			.getElementById("question"));
}

function showSolution(qId, solution, isShowSolution) {

	if (!isShowSolution) {
		$("#questionSolutionDiv").css({
			"display" : "none"
		});
		return;
	}

	var solutionText = "No Solution found.";
	if (solution && solution != '') {
		solutionText = solution;
	} else {
		questionJSInterface.fetchSolution(qId);
	}
	$("#questionSolution").html(htmlDecode(solutionText));
}

function showQuestionSolution(solutionText) {
	if (solutionText) {
		$("#questionSolution").html(htmlDecode(solutionText));
	}
}

// var optionsAlphabets = [ "A. ", "B. ", "C. ", "D. ", "E. ", "F. ", "G. ",
// "H. ", "I. ", "J. ", "K. ", "L. " ];
var optionsAlphabets = [ "(a)", "(b)", "(c)", "(d)", "(e)", "(f)", "(g)" ];
function appendAssignmentQuestionData(content, options, position) {
	var holder = $("#assignment_table");
	var tr = $(document.createElement("tr"))
			.html(
					"<tr class='assignment_ques'><td class='assignment_ques_num'>"
							+ (position + 1)
							+ "</td><td class='assignment_ques_sec'><div class='assignment_ques_body'>"
							+ "</div><div class='assignment_ques_opts'></div></td></tr>");

	tr.find(".assignment_ques_sec").data("questionPos", position);
	tr.find(".assignment_ques_body").html(htmlDecode(content));
	var optsHolder = $(document.createElement("div"));
	for ( var i = 0; i < options.length; i++) {
		var optionText = options[i];
		optsHolder.append("<div class='assignment_ques_opt'>"
				+ optionsAlphabets[i] + optionText + "</div>");
	}
	tr.find(".assignment_ques_opts").html(optsHolder.children());

	holder.children("tbody").append(tr);

	if (position == -1) {
		tr.find(".assignment_ques_num").hide();
		tr.toggleClass("assignment_single_ques assignment_ques");
	}

	if (mathJaxParseTimeout) {
		clearTimeout(mathJaxParseTimeout);
	}
	mathJaxParseTimeout = setTimeout(function() {
		MathJax.Hub.Queue([ "Typeset", MathJax.Hub ]);
	}, 100);
}
$(document).on("click", ".assignment_ques", function() {
	var quesPos = $(this).find(".assignment_ques_sec").data("questionPos");
	assignment_question_list_jsinterface.onClickQuestion(quesPos);
});
