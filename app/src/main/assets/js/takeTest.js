var optionsAlphabets = [ "A", "B", "C", "D", "E", "F", "G", "H", "I" ];
var qType = "SCQ";
var activeOptClass = "quesOptActive";
function htmlDecode(str) {
	return $("#htmlDecode").html(str).text();
}
function drawQuesOptions(options) {
	try {
		for ( var i = 0; i < options.length; i++) {
			var option = '<tr class="quesOpt quesOptSelectable quesOpt'
					+ (i + 1) + '" data-index="' + (i + 1)
					+ '"><td><div class="quesOptNum">' + optionsAlphabets[i]
					+ '</div></td><td>' + htmlDecode(options[i]) + '</td></tr>';
			$("#quesOpts tbody").append(option);
		}
	} catch (err) {
		console.error(err);
	}
}
function addQuestionData(content, options, questionType, answerGiven) {
	$("#quesBody").html(htmlDecode(content));
	qType = questionType;
	if (questionType === "NUMERIC") {
		$("#numericAnsBlock").show();
		console.log("Answet " + answerGiven);
		if (answerGiven) {
			$("#numericDummyAnsBox").show();
			$("#numericDummyAnsBox").html(answerGiven);
		} else {
			$("#numericAnsTextBox").show();
		}
	} else {
		$("#quesOpts").show();
		drawQuesOptions(options);
		if (answerGiven) {
			console.log("removing class");
			$("#quesBody").addClass("answered");
			$(".quesOptSelectable").removeClass("quesOptSelectable");
			var ansList = answerGiven.split("#");
			for ( var k = 0; k < ansList.length; k++) {
				$(".quesOpt" + ansList[k]).addClass(activeOptClass);
			}
		}
	}
	MathJax.Hub.Queue([ "Typeset", MathJax.Hub ]);
}
$(document).on("click", "#numericDummyAnsBox", function() {
	TakeTestJSInterface.makeToastToResetAnswer();
});
$(document).on("click", ".quesOpt", function() {
	if ($("#quesBody").hasClass("answered")) {
		TakeTestJSInterface.makeToastToResetAnswer();
	}
});
//$(document).tap(function() {
//	$(".quesOpt").addClass(activeOptClass);
//});
$(document).on("click", ".quesOptSelectable", function() {
	var $this = $(this);
	var ans = "";
	if (qType === "MCQ") {
		$this.toggleClass(activeOptClass);
		var opts = $(".quesOptActive");
		var l = opts.length;
		for ( var k = 0; k < l; k++) {
			var opt = opts.eq(k);
			ans += opt.data("index") + ((k === (l - 1)) ? "" : "#");
		}
	} else if (qType === "SCQ") {
		$(".quesOpt").removeClass(activeOptClass);
		$this.addClass(activeOptClass);
		ans = $this.data("index");
	}
	TakeTestJSInterface.onAnswerAdded(ans);
});
$(document).on("keyup", "#numericAnsTextBox", function() {
	TakeTestJSInterface.onAnswerAdded($(this).val());
});
var resetAnswer = function(questionType) {
	$("#quesBody").removeClass("answered")
	if (questionType === "NUMERIC") {
		$("#numericDummyAnsBox").hide();
		$("#numericAnsTextBox").show().val("");
	} else {
		$(".quesOpt").addClass("quesOptSelectable").removeClass(activeOptClass);
	}
};