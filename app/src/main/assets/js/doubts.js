var doubtsHolder = $("#doubtsHolder");
var doubtSample = $("#doubtSampleParent").children(".doubt");
var doubtPage = $("#doubtPage");
var doubtPageAnswers = $("#doubtPageAnswers");
var doubtPageFollowers = $("#doubtPageFollowers");
var doubtAnsSample = $("#doubtAnsSample").children(".doubtAns");
var doubtFollowerSample = $("#doubtFollowersSample").children(".doubtFollower");
var doubtAnsReplySample = $("#doubtAnsReplySample").children(".doubtAnsReply");
var fewSecondsAgo = "few seconds ago";
var loadingFontSize = 20;
var scrollTimeoutObj;
const scrollOffsetHeight = 100;
var doubtsFetchInProgress = false;

function prependDoubt(doubt) {
	if ($("#doubt_" + doubt.id).length != 0) {
		return;
	}
	var doubtHTML = doubtSample.clone(true);
	doubtHTML.data({
		doubtId : doubt.id,
		index : 0
	});
	populateDoubt(doubtHTML, doubt);
	doubtsHolder.prepend(doubtHTML);
	doubtsHolder.find(".doubt").each(function(index,d){
		$(this).data("index",index);
	});
	MathJax.Hub.Queue([ "Typeset", MathJax.Hub ]);

}
function resetValues(fn){
	stopScrollTimer();
	scrollTimeoutObj=null;
	doubtsFetchInProgress = false;
	var fn = fn ? fn : onDoubtsScroll;
	$(window).off("scroll.doubtsList").on("scroll.doubtsList",fn);
}
function removeLoading(){
	doubtsHolder.find(".loadingStyles").remove();
}
function appendDoubt(index, doubt) {
	var doubtHTML = doubtSample.clone(true);
	doubtHTML.data({
		doubtId : doubt.id,
		index : index
	});
	doubtHTML = populateDoubt(doubtHTML, doubt);
	doubtsHolder.append(doubtHTML);
	MathJax.Hub.Queue([ "Typeset", MathJax.Hub ]);
}
function updateDoubt(index, doubt) {
	var doubtHTML = doubtsHolder.find(".doubt").get(index);
	if(doubtHTML){
		doubtHTML = $(doubtHTML);
		if(doubtHTML.data("doubtId") == doubt.id){
			populateDoubt(doubtHTML, doubt);
			MathJax.Hub.Queue([ "Typeset", MathJax.Hub ]);
		}
	}
}

function showLoadDoubtsError(fontSize) {
	fontSize = fontSize || 26;
	doubtsHolder.find(".loadingStyles").remove();
	var doubts = doubtsHolder.children(".doubt").length;
	var errText = "<div  class='loadingStyles redTextColor' style='font-size:"+fontSize+";'>";
	errText += "<div class='sadSmiley'></div>"
	errText += "<div>There was some error in fetching doubts.</br> Please check your internet connection!</div></div>";
	if (doubts === 0) {
		doubtsHolder.html(errText);
	} else {
		doubtsHolder.append(errText);
	}

}
var clearContainer = function() {
	doubtsHolder.html("");
};
var showLoadingForDoubts = function(fontSize) {
	fontSize = fontSize || 26;
	doubtsHolder.html("<div  class='loadingStyles' style='font-size:"+fontSize+";'>Loading Doubts...</div>");
	loadingFontSize = fontSize;
};
var showZeroLevel = function(fontSize){
	fontSize = fontSize || 20;
	var errText = "<div  class='doubtsZeroLevel' style='font-size:"+fontSize+";'>";
	errText += "<div class='sadSmiley'></div>"
	errText += "<div>No Doubts found!</div></div>";
	doubtsHolder.html(errText);
};
var showLoadingForAnswers = function(fontSize, isAppend) {
	doubtPageFollowers.addClass("nonner");
	showLoadingForOthers(doubtPageAnswers,fontSize,isAppend,"Loading Answers");
};
var showLoadingForFollowers = function(fontSize, isAppend) {
	doubtPageAnswers.addClass("nonner");
	doubtPageFollowers.removeClass("nonner");
	showLoadingForOthers(doubtPageFollowers.find(".followersHolder"),fontSize,isAppend,"Loading Followers");
}
var showLoadingForOthers = function(holder, fontSize, isAppend, loadingText) {
	fontSize = fontSize || 26;
	if(isAppend){
	   if( !holder.find(".loadMore").get(0)){
		holder.append('<div  class="loadingStyles loadMore" style="font-size:'+fontSize+';">'+loadingText+'</div>')
			.removeClass("nonner");
	   }
	}else{
		holder.html('<div  class="loadingStyles" style="font-size:'+fontSize+';">'+loadingText+'</div>').removeClass("nonner");
	}
	loadingFontSize = fontSize;
};
var appendLoadingForDoubts = function() {
	if(!doubtsHolder.find(".loadMore").get(0)){
		doubtsHolder.append("<div  class='loadingStyles loadMore' style='font-size:"+loadingFontSize+";'>Loading Doubts...</div>");
	}
};
$(doubtsHolder).on("click", ".doubt", function() {
	var index = $(this).data("index");
	if(!isNaN(index)){
		doubtJSInterface.openDoubt(index);
	}
});
$(doubtPage).on("click", ".RTEImageDiv img", function() {
	var $this = $(this);
	var url = $this.prop("src");
	if(url){
		doubtJSInterface.openImageViewer(url,"");
	}
});
function fillDoubtsAnsSection(html){
	if(doubtPageAnswers.find(".answersHolder").length==0){
		var innerHTML = "<div class='answersText'><span>Answers</span><div class='addAnsButton'>Add Answer</div></div>";
		innerHTML += "<div class='answersHolder'></div>";
		doubtPageAnswers.html(innerHTML);
	}
	doubtPageAnswers.find(".answersHolder").append(html);
}
function showSingleDoubt(doubt) {
	var doubtHTML = doubtPage.children(".doubt");
	doubtPage.data("doubtId", doubt.id);
	populateDoubt(doubtHTML, doubt);
	MathJax.Hub.Queue([ "Typeset", MathJax.Hub ]);
}
var populateDoubt = function(doubtHTML, doubt) {
	var addedBy = doubt.user;

	doubtHTML.attr("id", "doubt_" + doubt.id);
	doubtHTML.find(".doubtUserImg").attr("src", addedBy.thumbnail);
	doubtHTML.find(".doubtBy").html(addedBy.firstName + " " + addedBy.lastName);
	var time = $.timeago(new Date(doubt.timeCreated));
	doubtHTML.find(".doubtTime").html(time);
	doubtHTML.find(".doubtTitle").html(doubt.name);
	doubtHTML.find(".doubtBody").html(doubt.content);
	var tagsHTML = "";
	var doubtTagsDiv = doubtHTML.find(".doubtTags");
	if(doubtTagsDiv.get(0)){
	var subjects = doubt.boardTree;
	for ( var k = 0, l = subjects.length; k < l; k++) {
		var sub = subjects[k];
		tagsHTML += "<span class='doubtTag doubtSubjectTag' data-brd-id="
				+ sub.id + ">" + sub.name + "</span>";
		var topics = sub.children;
		for ( var m = 0, n = topics.length; m < n; m++) {
			var topic = topics[m];
			tagsHTML += "<span class='doubtTag doubtTopicTag' data-brd-id="
					+ topic.id + ">" + topic.name + "</span>";
		}
	}
	doubtTagsDiv.html(tagsHTML);
	}
	doubtHTML.find(".doubtAnsCount").html(doubt.comments);
	doubtHTML.find(".doubtFollowerCount").html(doubt.followers);
	if(doubt.comments>0){
		doubtHTML.find(".doubtAnsCountDiv").addClass("active");
	}
	if(doubt.followers>0){
		doubtHTML.find(".doubtFollowerCountDiv").addClass("active");
	}
	if(doubt.followType == "FOLLOWING" || doubt.followType == "BOTH_WAYS"){
		doubtHTML.find(".followDoubtBtn")
			.removeClass("followDoubtBtn")
			.addClass("unfollowDoubtBtn")
			.text("Unfollow");
	}else if(doubt.followType != "NONE"){
		doubtHTML.find(".followDoubtBtn").remove();
	}
	doubtHTML.removeClass("nonner");
	return doubtHTML;
};
function stopScrollTimer(){
	if(scrollTimeoutObj){
		clearTimeout(scrollTimeoutObj);
	}
}
function setAllDoubtsLoaded(){
	stopScrollTimer();
	$(window).off("scroll.doubtsList");
}
function setFetchInProgress(val){
	doubtsFetchInProgress = val;
}
function onDoubtsScroll(){
	stopScrollTimer();
	scrollTimeoutObj = setTimeout(function(){
		if(doubtsHolder.children(".doubt").length<=0 || doubtsFetchInProgress){
			stopScrollTimer();
			return;
		}
		var height = doubtsHolder.height();
		var scrollHeight = doubtsHolder.scrollTop();
		if((height + scrollOffsetHeight) >= scrollHeight){
			doubtsFetchInProgress = true;
			appendLoadingForDoubts();
			doubtJSInterface.loadMoreDoubts();
		}
	},100);
};
function onDoubtAnswersScroll(){
	onDoubtComponentsScroll(doubtPageAnswers,"ANSWERS");
};
function onDoubtFollowersScroll(){
	onDoubtComponentsScroll(doubtPageFollowers,"FOLLOWERS");
};
function onDoubtComponentsScroll(holder,reqType){
	stopScrollTimer();
	scrollTimeoutObj = setTimeout(function(){
		if(doubtsFetchInProgress){
			stopScrollTimer();
			return;
		}
		var height = holder.height();
		var scrollHeight = holder.scrollTop();
		if((height + scrollOffsetHeight) >= scrollHeight){
			doubtsFetchInProgress = true;
			$(window).off("scroll.doubtsList");
			if(reqType==="FOLLOWERS"){
				doubtJSInterface.loadMoreFollowers();
			}else if(reqType==="ANSWERS"){
				doubtJSInterface.loadMoreAnswers();
			}			
			/*if(cbFn){
				try{ cbFn(); }catch(err){ console.error(err); }
			}*/
		}
	},100);
};

function showAnswers(){
	showLoadingForAnswers(loadingFontSize);
	doubtJSInterface.loadAnswers();
}
// single doubt page
function populateAnswers(isSuccess, ansRes, start) {
	if (!isSuccess || ansRes.errorCode != '') {
		if(doubtPageAnswers.find(".doubtAns").length>0){
			doubtPageAnswers.append("There was some problem in fetching the answers.");
		}else{
			doubtPageAnswers.html("There was some problem in fetching the answers.");
		}
		return;
	}
	var answers = ansRes.result.list;
	var answersCount = ansRes.result.totalHits;
	var ansHolder = $(document.createElement("div"));
	doubtPage.find(".doubtPageAnsCount").html(answersCount);
	for (i = 0, l = answers.length; i < l; i++) {
		var ans = answers[i];
		var ansHTML = doubtAnsSample.clone(true);
		var addedBy = ans.user;
		ansHTML.attr("id", "doubtAns_" + ans.id).data("doubtAnsId", ans.id);
		ansHTML.find(".doubtAnsUserImg").attr("src", addedBy.thumbnail);
		ansHTML.find(".doubtAnsBy").html(
				addedBy.firstName + " " + addedBy.lastName);
		var time = $.timeago(new Date(ans.timeCreated));
		ansHTML.find(".doubtAnsTime").html(time);
		ansHTML.find(".doubtAnsBody").html(ans.content);
		ansHTML.find(".doubtAnsReplyCount").html(ans.comments);
		ansHTML.find(".doubtAnsLikeCount").html(ans.upVotes);
		if (ans.voted) {
			ansHTML.find(".doubtAnsLikeCountDiv").addClass(
					"doubtAnsLikeCountImgLiked");
		}
		ansHolder.append(ansHTML);
	}
	doubtPageAnswers.find(".loadMore").remove();
	fillDoubtsAnsSection(ansHolder.children());
	if(answersCount > (start+answers.length)){
		showLoadingForAnswers(loadingFontSize,true);
		resetValues(onDoubtAnswersScroll);
	}else{
		setAllDoubtsLoaded();
		doubtPageAnswers.find(".loadMore").remove();
	}
	if (answers.length === 0) {
		var html = "<div style='margin:7px 0;'><span style='color:#999999;'>No answers Added</span>";
		html += "<div class='addAnsButton'>Add Answer</div></div>";
		doubtPageAnswers.html(html);
	}
	MathJax.Hub.Queue([ "Typeset", MathJax.Hub ]);
}
function showFollowers(){
	showLoadingForFollowers(loadingFontSize);
	doubtJSInterface.loadFollowers(0);
}
$(document).on("click",".doubtAnsCountDiv",showAnswers)
	.on("click",".doubtFollowerCountDiv",showFollowers);
function populateFollowers(isSuccess, resp, start) {
	if (!isSuccess || resp.errorCode != '') {
		if(doubtPageFollowers.find(".doubtAns").length>0){
			doubtPageFollowers.append("There was some problem in fetching the followers.");
		}else{
			doubtPageFollowers.html("There was some problem in fetching the followers.");
		}
		return;
	}
	var followers = resp.result.list;
	var followersCount = resp.result.totalHits;
	var followersHolder = doubtPageFollowers.find(".followersHolder");
	if(start == 0){
		followersHolder.html("");
	}
	doubtPage.find(".doubtFollowerCount").html(followersCount);
	for (i = 0, l = followers.length; i < l; i++) {
		var follower = followers[i];
		var flwHTML = doubtFollowerSample.clone(true);
		var addedBy = follower;
		flwHTML.find(".doubtFlwUserImg").attr("src", addedBy.thumbnail);
		var nameHTML = "<span class='boldy alignTop'>" + addedBy.firstName + " </span><span class='alignTop'>" + addedBy.lastName +"</span>";
		flwHTML.find(".doubtFlwBy").html(nameHTML);
		var profileHTML = getProfileText(addedBy);
		flwHTML.find(".doubtFlwByProfileDiv").html(profileHTML);
		followersHolder.append(flwHTML);
	}
	doubtPageFollowers.find(".loadMore").remove();
	if(followersCount > (start+followers.length)){
		showLoadingForFollowers(loadingFontSize,true);
		resetValues(onDoubtFollowersScroll);
	}else{
		setAllDoubtsLoaded();
		doubtPageFollowers.find(".loadMore").remove();
	}
	if (follower.length === 0) {
		var html = "<div style='margin:7px 0;'><span style='color:#999999;'>No followers</span>";
		doubtPageFollowers.html(html);
	}
}
$(document).on("click", ".doubtAnsReplyCountDiv", function() {
	var $this = $(this), className = "doubtAnsReplyCountDivActive";
	var doubtAns = $this.closest(".doubtAns");
	var repliesSec = doubtAns.find(".doubtAnsRepliesSec");
	if ($this.hasClass(className)) {
		$this.removeClass(className);
		repliesSec.addClass("nonner");
	} else {
		$this.addClass(className);
		repliesSec.removeClass("nonner");
		doubtJSInterface.getAnswerReplies(doubtAns.data("doubtAnsId"));
	}
	onDelete(doubtAns.find("textarea"));
});
var populateAnswerReplies = function(doubtAnsId, isSuccess, response) {
	var doubtAns = $("#doubtAns_" + doubtAnsId);
	var repliesHolder = doubtAns.find(".doubtAnsRepliesHolder");
	if (!isSuccess || response.errorCode != '') {
		repliesHolder.html("There was some problem in fetching the replies.");
		return;
	}
	var replies = response.result.list;
	var repliesDummyHolder = $(document.createElement("div"));
	for (i = 0, l = replies.length; i < l; i++) {
		var reply = replies[i];
		var replyHTML = doubtAnsReplySample.clone(true);
		var addedBy = reply.user;
		replyHTML.find(".doubtAnsReplyUserImg").attr("src", addedBy.thumbnail);
		replyHTML.find(".doubtAnsReplyBy").html(
				addedBy.firstName + " " + addedBy.lastName);
		var time = $.timeago(new Date(reply.timeCreated));
		replyHTML.find(".doubtAnsReplyTime").html(time);
		replyHTML.find(".doubtAnsReplyBody").html(reply.content);
		repliesDummyHolder.append(replyHTML);
	}
	repliesHolder.html(repliesDummyHolder.children());
	if (replies.length === 0) {
		repliesHolder
				.html("<div style='margin:7px 0;color:#999999'>No replies Added</div>");
	}
	MathJax.Hub.Queue([ "Typeset", MathJax.Hub ]);
};
var increaseCount = function(countEl) {
	var count = parseInt(countEl.text());
	if (!isNaN(count))
		countEl.text(++count);
}
var decreaseCount = function(countEl) {
	var count = parseInt(countEl.text());
	if (!isNaN(count))
		--count;
	if (count >= 0)
		countEl.text(count);
}

$(document).on("click", ".doubtAnsLikeCountDiv", function() {
	var $this = $(this), className = "doubtAnsLikeCountImgLiked";
	var doubtAns = $this.closest(".doubtAns");
	var imgEl = $(this).find(".doubtAnsLikeCountImg")
	if (!$this.hasClass(className)) {
		$this.addClass(className);
		increaseCount($this.find(".doubtAnsLikeCount"));
		doubtJSInterface.likeAnswer(doubtAns.data("doubtAnsId"));
	}
});
var checkLikeAnswerResp = function(doubtAnsId, isSuccess, response) {
	var doubtAns = $("#doubtAns_" + doubtAnsId);
	if (!isSuccess || response.errorCode != "") {
		alert("There was some error. Please try again.");
		decreaseCount(doubtAns.find(".doubtAnsLikeCount"));
		doubtAns.find(".doubtAnsLikeCountDiv").removeClass(
				"doubtAnsLikeCountImgLiked");
	}
};
$(document).on("click",".doubtAnsReplySubmitButton",function() {
	var $this = $(this), disableClassName = "doubtAnsReplyPostSecDisabled";
	var doubtAnsReplyPostSec = $this.closest(".doubtAnsReplyPostSec");
	var doubtAns = $this.closest(".doubtAns");
	var content = doubtAns.find(".doubtAnsReplyPostArea").val();
	if (content && !doubtAnsReplyPostSec.hasClass(disableClassName)) {
		doubtAnsReplyPostSec.addClass(disableClassName);
		var postArea = doubtAnsReplyPostSec.find(".doubtAnsReplyPostArea");
		postArea.css("display", "none");
		$this.text("Submitting");
		doubtJSInterface.postAnswerReply(doubtPage.data("doubtId"), doubtAns.data("doubtAnsId"),filterText(content));
	}else{
	alert("Please enter text in the reply box.");
	}
});
var checkPostAnswerReplyResp = function(doubtAnsId, isSuccess, response,
		content, name, thumbnail) {
	var doubtAns = $("#doubtAns_" + doubtAnsId);
	if (!isSuccess || response.errorCode != "") {
		alert("There was some error. Please try again.");
	} else {
		var replyHTML = doubtAnsReplySample.clone(true);
		replyHTML.find(".doubtAnsReplyUserImg").attr("src", thumbnail);
		replyHTML.find(".doubtAnsReplyBy").html(name);
		replyHTML.find(".doubtAnsReplyTime").html(fewSecondsAgo);
		replyHTML.find(".doubtAnsReplyBody").html(content);
		var doubtAnsReplyPostSec = doubtAns.find(".doubtAnsReplyPostSec");
		doubtAnsReplyPostSec.removeClass("doubtAnsReplyPostSecDisabled")
		doubtAnsReplyPostSec.find(".doubtAnsReplySubmitButton").text("Submit");
		doubtAnsReplyPostSec.find(".doubtAnsReplyPostArea").css("display","block");
		var repliesHolder = doubtAns.find(".doubtAnsRepliesHolder");
		if (repliesHolder.children(".doubtAnsReply").length > 0) {
			repliesHolder.prepend(replyHTML);
		} else {
			repliesHolder.html(replyHTML);
		}
		var textarea = doubtAns.find(".doubtAnsReplyPostArea");
		textarea.val("");
		onDelete(textarea)
		increaseCount(doubtAns.find(".doubtAnsReplyCount"));
		alert("Reply Posted Successfully.");
	}
};
$(document).on("click", ".addAnsButton", function() {
	doubtJSInterface.onClickAddAnswer(doubtPage.data("doubtId"));
});
$(document).on("click", ".followDoubtBtn", function() {
	var $this = $(this);
	$this.text("Unfollow").addClass("unfollowDoubtBtn").removeClass("followDoubtBtn");
	var followCountDiv = $(".doubtFollowerCountDiv");
	var countDiv = followCountDiv.find(".doubtFollowerCount"); 
	increaseCount(countDiv);
	var count = countDiv.text();
	if(parseInt(count)>0 && !followCountDiv.hasClass("active")){
		followCountDiv.addClass("active");
	}
	doubtJSInterface.followDoubt(doubtPage.data("doubtId"),true,count);
});
$(document).on("click", ".unfollowDoubtBtn", function() {
	var $this = $(this);
	$this.text("Follow").addClass("followDoubtBtn").removeClass("unfollowDoubtBtn");
	var followCountDiv = $(".doubtFollowerCountDiv");
	var countDiv = followCountDiv.find(".doubtFollowerCount"); 
	decreaseCount(countDiv);
	var count = countDiv.text();
	if(parseInt(count)<=0 && !followCountDiv.hasClass("active")){
		followCountDiv.removeClass("active");
	}
	doubtJSInterface.followDoubt(doubtPage.data("doubtId"),false,count);
});
var followDoubtFailed = function(type){
	var followCountDiv = $(".doubtFollowerCountDiv");
	var countDiv = followCountDiv.find(".doubtFollowerCount"); 
	if(type){
		decreaseCount(countDiv);
	}else{
		increaseCount(countDiv);
	}
	var count = countDiv.text();
	if(parseInt(count)<=0){
		followCountDiv.removeClass("active");
	}else{
		followCountDiv.addClass("active");
	}
}
var checkPostAnswerResp = function(isSuccess, response, name, thumbnail) {
	if (!isSuccess || response.errorCode != "") {
		alert("There was some error. Please try again.");
	} else {
		var ansHTML = doubtAnsSample.clone(true);
		var ansId = response.result.id;
		ansHTML.attr("id", "doubtAns_" + ansId).data("doubtAnsId", ansId);
		ansHTML.find(".doubtAnsUserImg").attr("src", thumbnail);
		ansHTML.find(".doubtAnsBy").html(name);
		ansHTML.find(".doubtAnsTime").html(fewSecondsAgo);
		ansHTML.find(".doubtAnsBody").html(response.result.content);
		if (doubtPageAnswers.children(".doubtAns").length > 0) {
			doubtPageAnswers.find(".answersHolder").prepend(ansHTML);
		} else {
			fillDoubtsAnsSection(ansHTML);
		}
		var ansCountDiv = doubtPage.find(".doubtAnsCountDiv");
		var ansCount = doubtPage.find(".doubtPageAnsCount");
		increaseCount(ansCount);
		var count = ansCount.text();
		if(parseInt(count)>0 && !ansCountDiv.hasClass("active")){
			ansCountDiv.addClass("active");
		}
		count += "";
		doubtJSInterface.updateAnswerCount(count);
	}
};
function filterText(input) {
	var output;
	output = input.replace("'", "&apos;", "g");
	output = output.replace("\"", "&quot;", "g");
	output = output.replace("`", "&acute;", "g");
	output = output.replace(">", "&gt;", "g");
	output = output.replace("<", "&lt;", "g");
	output = output.replace(/\n/g, "<br>");
	return output;
}
/*
 * function transformResponse(input){ var output; output =
 * input.replace("&apos;", "'", "g"); output = output.replace("\"", "&quot;",
 * "g"); output = output.replace("`", "&acute;", "g"); output =
 * output.replace(">", "&gt;", "g"); output = output.replace("<", "&lt;", "g");
 * output = output.replace(/\n/g, "<br>"); return output; }
 */
$(document).on("focus",".doubtAnsReplyPostArea",function() {
	$(this).closest(".doubtAnsReplyPostSec").find(".doubtAnsReplySubmitButtonDiv").show();
});
/*
 * $(document).on("blur",".doubtAnsReplyPostArea",function(){
 * $(this).closest(".doubtAnsReplyPostSec").find(".doubtAnsReplySubmitButtonDiv").css("visibility","hidden");
 * });
 */

var timeoutObj;
new function() {	
	$(document).on('keyup cut paste', ".autogrow", function(e) {
		var $this = $(this);
		var replyStore = $this.siblings(".replyStore");
		replyStore.html(filterText($this.val()));
		$this.css({
			"height" : (replyStore.height() + 36)
		});
	});
	$(document).on('cut delete', ".autogrow", function(e) {
		onDelete($(this));
	});
}(jQuery);
function onDelete($this) {
	if (timeoutObj)
		clearTimeout(timeoutObj);
	timeoutObj = setTimeout(function() {
		$this.height(0);
		if (timeoutObj)
			clearTimeout(timeoutObj);
		timeoutObj = setTimeout(function() {
			var replyStore = $this.siblings(".replyStore");
			replyStore.html(filterText($this.val()));			
			var h = (replyStore.height() + 36);
			$this.height(h);
		}, 10);
	}, 300);
};
function getProfileText(user){
	var profile = "";
	switch(user.profile){
		case "TEACHER" : profile = "Teacher";
				 break;
		case "STUDENT" : profile = "Student";
				 break;
		case "MANAGER" : profile = "Manager";
				 break;
	};
	return profile;
}
