var optionsAlphabets = [ "A", "B", "C", "D", "E", "F", "G", "H", "I" ];
function htmlDecode(str) {
	return $("#htmlDecode").html(str).text();
}
function drawQuesOptions(options) {
	var holder =  $("#quesOpts tbody");
	try {
		for ( var i = 0; i < options.length; i++) {
			var option = '<tr class="quesOpt'
					+ (i + 1) + '" data-index="' + (i + 1)
					+ '"><td><div class="quesOptNum">' + intToAlphabet(i+1)
					+ '</div></td><td>' + htmlDecode(options[i]) + '</td></tr>';
			holder.append(option);
		}
	} catch (err) {
		console.error(err);
	}
}
function drawQuestion(questionType,content,options,qNo,answerGiven,correctAnswer,attemptStatus,isCorrect,showAnsGiven){
	var quesBody = "<span class='qNo'>Q"+qNo+".</span>"+htmlDecode(content);
	$("#quesBody").html(quesBody);
	var ansGivenHtml="",correctAnsHtml="";
	if (questionType != "NUMERIC") {
		$("#quesOpts").show();
		drawQuesOptions(options);
		if(questionType == "SCQ"){
			if(answerGiven && answerGiven!='null'){
				ansGivenHtml = intToAlphabet(answerGiven);
			}
			correctAnsHtml = intToAlphabet(correctAnswer);
		}else if(questionType == "MCQ"){
			if(answerGiven && answerGiven!='null'){
				var ansGivenArr = answerGiven.split("#");
				ansGivenArr = arrToAlphabets(ansGivenArr);
				ansGivenHtml = ansGivenArr.join(",");
			}
			var correctAnsArr = correctAnswer.split("#");
			correctAnsArr = arrToAlphabets(correctAnsArr);
                        correctAnsHtml = correctAnsArr.join(",");
		}
	}else{
		correctAnsHtml = correctAnswer;
		if(answerGiven && answerGiven!='null'){
			ansGivenHtml = answerGiven;
		}
	}
	if(!showAnsGiven || showAnsGiven=="false"){
		$(".yourAnswer").hide();			
	}
	else if(ansGivenHtml && (attemptStatus == "SAVED" || attemptStatus == "null")){
		var yourAnswerTag = $(".yourAnswer");
		yourAnswerTag.find(".value").text(ansGivenHtml);
		if(isCorrect || isCorrect=="true"){
			yourAnswerTag.addClass("correct");
			yourAnswerTag.data("type","correct");
		}else{
			yourAnswerTag.addClass("incorrect");
			yourAnswerTag.data("type","incorrect");
		}
	}else{
		$(".yourAnswer").text("Not Attempted");
	}
	$(".correctAnswer").find(".value").text(correctAnsHtml);
	MathJax.Hub.Queue([ "Typeset", MathJax.Hub ]);
};
var intToAlphabet = function(str){
	var i = parseInt(str);
	if(!isNaN(i)&&i>0){
		return optionsAlphabets[(i-1)];
	}else{
		return "";
	}
}
var arrToAlphabets = function(arr){
	for(var i=0;i<arr.length;i++){
		arr[i] = intToAlphabet(arr[i]);
	}
	return arr;
};
var showSolution = function(solution,solutionText){
	try{
		if(!solution){
			alert("No solution available");
			return;
		}
		var elem = document.getElementById("questionSolutionDiv");
		$(elem).removeClass("nonner");
		try{
			solutionText = htmlDecode(solutionText);
		}catch(err){
			console.error(err);
		}
		$("#questionSolution").html(solutionText);
		if(solution&&solution.attachmentsInfo&&solution.attachmentsInfo.length>0){				
			var attachmentInfo=solution.attachmentsInfo[0];
			var attachmentEntity=attachmentInfo.entity;
			var info=attachmentInfo.info;
			if(attachmentEntity.type==="VIDEO"){
				var solnAttachmentDiv=$(document.createElement("div"));
				solnAttachmentDiv.addClass("quesSoln");
				console.log(info.thumbnailURL);
				solnAttachmentDiv.data("jsonStr",'{type:"VIDEO",id:"'+attachmentEntity.id+'",linkUrl:"'+info.url+'",thumbnail:"'+info.thumbnailURL+'",linkType:"'+info.linkType+'"}');
				solnAttachmentDiv.html('<div class="vidThumbHold"><div class="vidThumbImg"><img src="'+info.thumbnailURL+'" alt="Image not available">\n\
		            </div><div class="vidPlayIcon"></div><div class="vidThumbDuration">'+getDurationStr(info.duration)+'</div></div><div class="videoTitle">'+info.name+'</div>');			
		        $("#questionSolution").append(solnAttachmentDiv);						
			}			
		}
		MathJax.Hub.Queue([ "Typeset", MathJax.Hub,elem],function(){
			setTimeout(function(){
				quesJSInterface.scrollTo("BOTTOM");
				var solPos=$("#questionSolutionDiv").offset().top;
				$(window).scrollTop(solPos+20);
			},500);
		});
	}catch(er){
		console.error(er);
	}
};
var hideSolution = function(solution){
	$("#questionSolution").html("");
	$("#questionSolutionDiv").addClass("nonner");
	quesJSInterface.scrollTo("TOP");
};
$(document).on("click",".yourAnswer",function(){
	var $this = $(this);
	var msg = "";
	switch($this.data("type")){
		case "correct" : msg = "You attempted the question correctly!";
				break;
		case "incorrect" : msg = "You attempted the question incorrectly!";
				break;
		default : msg = "You have not attempted the question!";
				break;
	}
	quesJSInterface.showMessage(msg);
});
$(document).on("click",".correctAnswer",function(){
	var msg = "This is the correct answer to the question.";
	quesJSInterface.showMessage(msg);
});
$(document).on("click",".quesSoln",function(){
	var jsonStr=$(this).data("jsonStr");
	quesJSInterface.openAttachment(jsonStr);
});
function getDurationStr(millis) {
    var secs = millis / 1000;
    var timeStr='';
    if (secs != 0 && secs != undefined) {
        var hr = parseInt(Math.floor(secs / 3600));
        var min = parseInt(Math.floor((secs - (hr * 3600)) / 60));
        var sec = parseInt(secs - (hr * 3600) - (min * 60));
        if (hr > 0) {
            if(hr<10){
                timeStr+="0" + hr;
            }else{
                timeStr+=hr;
            }
            timeStr+=":";
        }
        
        if (min < 10) {
            timeStr+= "0" + min;
        }else{
            timeStr+= min;        
        }
        timeStr+= ":";
        
        
        if (sec < 10) {
            timeStr+= "0" + sec;
        }else{
            timeStr+= sec;        
        }
    }
    return timeStr;
}
