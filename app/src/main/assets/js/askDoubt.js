var askDoubt = new function(){
	var parDiv;
	var strings = {
		"COURSE" : "Subject / Course",
		"TOPIC" : "Topics"
	}
	this.init = function(){
		parDiv = $("#doubtAskPage");
		parDiv.on("change",".selectSubject",fetchTopics);
	}
	this.showBoards = function(success,resp,type){
		var selectTag = parDiv.find(".selectSubject");
		if(type == "TOPIC"){
			selectTag = parDiv.find(".selectTopic");
		}
		var innerHtml = "<option selected value=''>"+strings[type]+"</option>";
		if(success && resp && resp.result && resp.result.list.length>0){
			var list = resp.result.list;
			for(var i=0;i<list.length;i++){
				var brd = list[i];
				innerHtml += "<option value='"+brd.id+"'>"+brd.name+"</option>";	
			}	
		}
		selectTag.html(innerHtml).val(undefined).data("brdType",type);
		selectTag.prop("disabled",false);
	};
	var fetchTopics = function(){
		var selectTag = parDiv.find(".selectSubject");
		var parentId = selectTag.val();
		if(parentId){
			parDiv.find(".selectTopic")
				.html("<option selected value=''>Fetching Topics..</option>")
				.prop("disabled",true);
			doubtJSInterface.fetchTopics("TOPIC",parentId);
		};	
	};
	this.getDoubtParams = function(){
		var params = {
		};
		params.name = $.trim(parDiv.find(".askDoubtTitleInput").val());
		if(!params.name){
			doubtJSInterface.showMessage("Please provide the title for your doubt! be precise.");
			parDiv.find(".askDoubtTitleInput").focus();
			return;	
		}
		var rteHolder = parDiv.find(".RTEHolder")
		if(vRTE.isRTEEmpty(rteHolder)){
			doubtJSInterface.showMessage("Now describe your doubt briefly in 'Body' section, you can also take a picture and add there!");
			rteHolder.find(".RTEArea").focus();
			return;	
		}
		params.content = vRTE.getRTEContent(rteHolder);
		var subjectTag = parDiv.find(".selectSubject").val();
		var topicTags = parDiv.find(".selectTopic").val();
		if(!subjectTag || !topicTags){
			doubtJSInterface.showMessage("Please select atleast one subject / course and topic from the 'Tag' section!");
			parDiv.find(".selectTopic").focus();
			return;	
		};
		params.brdIds=[];
		params.brdIds.push(subjectTag);
		params.brdIds.push(topicTags);
		//$.extend(params.brdIds,topicTags)
		params.scope = "PUBLIC";
		doubtJSInterface.setDoubtJson(JSON.stringify(params));
	};
};
$(function(){
	askDoubt.init();
});
var showBoards = askDoubt.showBoards;
var getDoubtJson = askDoubt.getDoubtParams;
