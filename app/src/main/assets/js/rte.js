var rtetemp;
function getAndroidVersion(ua) {
    var ua = ua || navigator.userAgent; 
    var match = ua.match(/Android\s([0-9\.]*)/);
    return match ? match[1] : false;
};
var checkContentEditable = function(div){
	var contentEditableSupport = "contentEditable" in document.documentElement && div.get(0).contentEditable != null;
	var androidVersion = parseInt(getAndroidVersion(),10);
	contentEditableSupport = contentEditableSupport && androidVersion>=3;
	return contentEditableSupport;
};
var vRTE = new (function($) {
	var textFormatClasses = ".RTEBold,.RTEItalic,.RTEInsertUnorderedList,.RTEInsertOrderedList,.RTEUnderline,.RTESuperscript,.RTESubscript";
	var rangeFocused;
	this.rteAreaForFE;
	var contentEditableSupport = "contentEditable" in document.documentElement;
	this.init = function(rteHolder, params) {
		contentEditableSupport = checkContentEditable(rteHolder);
		if(!contentEditableSupport){
			rteHolder.html("<textarea class='RTEArea textArea'></textarea>");	
			return;
		};
		rteHolder
			.off()
			.on('mousedown', ".RTEToolBar", cancelEvent)
			.on('click', textFormatClasses, formatText)
			.on("click",".RTELink,.RTEImage,.RTEVideo,.RTEFormulaEditor",insertRTETempDiv)
			.on("click", ".RTEAddCode",addCodeSnippet)
			// formula editor
			.on("click", ".RTEFormulaEditor", initFE).on("click",
					".RTEArea", RTEAreaClick).on("focus", ".RTEArea",
					RTEAreaFocus).on("blur", ".RTEArea", RTEAreaBlur).on(
					"click", ".removeRTELatex", removeRTELatexClick).on(
					"keydown", ".RTEArea", RTEAreaKeydown).on("keyup",
					".RTEArea", RTEAreaKeyup)

			// video and image addition
			.on("paste", ".RTEUrlInput", rteHolder, RTEUrlInputPaste).on(
					"keyup", ".RTEUrlInput", rteHolder, RTEUrlInputKeyup)
			.on("click", ".RTEUrlSubmit", rteHolder, RTEUrlSubmitClick).on(
					"click", ".RTEUrlCancel", rteHolder, RTEUrlCancelClick)

			// image adding
			.on("click", ".RTEImage", rteHolder, RTEImageClick)

			// video adding
			.on("click", ".RTEVideo", rteHolder, RTEVideoClick)
	};
	var formatText = function() {
		var command = $(this).data("formatType");
		var d = document;
		d.execCommand(command, false, null);
		textFormatCommandStatus($(this).closest(".RTEHolder"));
	};
	var insertRTETempDiv = function() {
		var rteArea = $(this).closest(".RTEHolder").find(".RTEArea");
		insertRTETempDivHere(rteArea);
	}
	var insertRTETempDivHere = function(rteArea){
		rteArea.find(".RTETempDiv").remove();
		try {
			var range = window.getSelection().getRangeAt(0);
			rangeFocused = range;
			// document.execCommand("insertHTML",false,"<div
			// class='RTETempDiv'></div>");
			rangeFocused.insertNode(makeHTMLTag("div", {
				"class" : "RTETempDiv"
			}).get(0));
		} catch (err) {
			console.log("err::  " + err)
		}
		if (rteArea.find(".RTETempDiv").length == 0) {
			rteArea.append("<div class='RTETempDiv'></div>");
		}
	};
	this.insertRTETempDiv = function(rteHolder){
		insertRTETempDivHere(rteHolder.find(".RTEArea"));	
	};
	var addCodeSnippet = function() {
		var rteArea = $(this).closest(".RTEHolder").find(".RTEArea");
		rteArea.find(".RTETempDiv").remove();
		try {
			var range = window.getSelection().getRangeAt(0);
			rangeFocused = range;
			var node = makeHTMLTag("div", {
				"class" : "RTECodeDiv"
			}).html("<pre class='brush: java;'>hi</pre>").get(0);
			rangeFocused.insertNode(node);
		} catch (err) {
			console.log("err::  " + err);
		}
	};
	var initFE = function() {
		var rteHolder = $(this).closest(".RTEHolder");
		vRTE.rteAreaForFE = rteHolder.children(".RTEArea");
		var feWrapperHolder = $("body").children("#FEWrapperHolder")
				.removeClass("nonner");
	};
	var RTEAreaClick = function() {
		var anchorNode = $(window.getSelection().anchorNode);
		var rteLatex = anchorNode.closest(".RTELatex");
		if (rteLatex.length > 0) {
			selectTextNode(rteLatex);
		}
		textFormatCommandStatus($(this).closest(".RTEHolder"));
	};
	var selectTextNode = function(rteLatex) {
		var r = rteLatex.get(0).nextSibling;
		if (!r) {
			r = document.createTextNode("");
			$(r).insertAfter(rteLatex);
		}
		var range = document.createRange();
		range.setStart(r, 0);
		range.setEnd(r, 0);
		var sel = window.getSelection();
		sel.removeAllRanges();
		sel.addRange(range);
	};
	var RTEAreaFocus = function() {
		$(this).closest(".RTEHolder").addClass("RTEActive");
		if ($(this).hasClass("RTEHasPH")) {
			$(this).removeClass("RTEHasPH").html("");
		}
	};
	var RTEAreaBlur = function() {
		$(this).closest(".RTEHolder").removeClass("RTEActive");
	};
	var RTEAreaKeydown = function(e) {
		var RTEArea = $(this);
		// var sel = window.getSelection();
		// var anchorNode = sel.anchorNode;
		// var rteLatex;
		// if (((e.which && e.which == 8) || (e.keyCode && e.keyCode == 8))) {
		// console.log($.extend({}, window.getSelection()))
		// if (anchorNode.nodeType !== 3) {
		// rteLatex = anchorNode.childNodes[sel.anchorOffset - 1];
		// } else if (anchorNode.nodeType === 3 && sel.anchorOffset === 0) {
		// rteLatex = anchorNode.previousSibling;
		// }
		// } else if (((e.which && e.which == 46) || (e.keyCode && e.keyCode ==
		// 46))) {
		// console.log($.extend({}, window.getSelection()))
		// if (anchorNode.nodeType === 3 && sel.anchorOffset ===
		// anchorNode.textContent.length) {
		// rteLatex = anchorNode.nextSibling;
		// }
		// }
		// if (rteLatex && $(rteLatex).hasClass("RTELatex")) {
		// e.preventDefault();
		// // selectTextNode($(rteLatex));
		//
		// var r = rteLatex.nextSibling;
		// if (!r) {
		// r = document.createTextNode("");
		// $(r).insertAfter($(rteLatex));
		// }
		// $(rteLatex).remove();
		// var range = document.createRange();
		// range.setStart(r, 0);
		// range.setEnd(r, 0);
		// var sel = window.getSelection();
		// sel.removeAllRanges();
		// sel.addRange(range);
		// }
		// return;

		if (((e.which && e.which == 8) || (e.keyCode && e.keyCode == 8))) {
			var posResults = getCurrentElAndRange();
			var currentEl = posResults.currentEl;
			var rteLatexAsSibling = posResults.currentEl.prev(".RTELatex,img");
			var rteLatex;

			// case when current el is inside mathjax
			if (posResults.rteLatexAsParent.length > 0) {
				rteLatex = posResults.rteLatexAsParent;
			} else if (rteLatexAsSibling.length > 0
					&& posResults.range.endOffset === 0) {
				// case when current el is a text node
				rteLatex = rteLatexAsSibling;
			} else if (currentEl.is(RTEArea)) {
				var offset = posResults.range.endOffset;
				rteLatex = $(currentEl.get(0).childNodes[offset - 1]);
			}
			RTEAreaDelHelper(rteLatex, e);
		} else if (((e.which && e.which == 46) || (e.keyCode && e.keyCode == 46))) {
			var posResults = getCurrentElAndRange();
			var currentEl = posResults.currentEl;
			var rteLatexAsSibling = currentEl.next(".RTELatex,img");
			var rteLatex;
			if (posResults.rteLatexAsParent.length > 0) {
				rteLatex = posResults.rteLatexAsParent;
			} else if (rteLatexAsSibling.length > 0
					&& currentEl.get(0).nodeType === 3
					&& posResults.range.endOffset === currentEl.text().length) {
				rteLatex = rteLatexAsSibling;
			}
			RTEAreaDelHelper(rteLatex, e);
		} else {
			var delClass = "deleteRTELatexEqn"
			RTEArea.find("." + delClass).removeClass(delClass);
		}
	};
	var textFormatCommandStatus = function(rteHolder) {
		var toolBar = rteHolder.children(".RTEToolBar");
		var activeRTEOptionClass = "activeRTEOption";
		var b = toolBar.children(".RTEBold");
		var i = toolBar.children(".RTEItalic");
		var u = toolBar.children(".RTEUnderline");
		var ul = toolBar.children(".RTEInsertUnorderedList");
		var ol = toolBar.children(".RTEInsertOrderedList");
		var sup = toolBar.children(".RTESuperscript");
		var sub = toolBar.children(".RTESubscript");

		if (document.queryCommandState("bold")) {
			b.addClass(activeRTEOptionClass);
		} else {
			b.removeClass(activeRTEOptionClass);
		}

		if (document.queryCommandState("italic")) {
			i.addClass(activeRTEOptionClass);
		} else {
			i.removeClass(activeRTEOptionClass);
		}

		if (document.queryCommandState("underline")) {
			u.addClass(activeRTEOptionClass);
		} else {
			u.removeClass(activeRTEOptionClass);
		}

		if (document.queryCommandState("insertunorderedlist")) {
			ul.addClass(activeRTEOptionClass);
		} else {
			ul.removeClass(activeRTEOptionClass);
		}

		if (document.queryCommandState("insertOrderedList")) {
			ol.addClass(activeRTEOptionClass);
		} else {
			ol.removeClass(activeRTEOptionClass);
		}

		if (document.queryCommandState("superscript")) {
			sup.addClass(activeRTEOptionClass);
		} else {
			sup.removeClass(activeRTEOptionClass);
		}

		if (document.queryCommandState("subscript")) {
			sub.addClass(activeRTEOptionClass);
		} else {
			sub.removeClass(activeRTEOptionClass);
		}
	};
	var getCurrentElAndRange = function() {
		var range = window.getSelection().getRangeAt(0);
		var currentEl = $(range.startContainer);
		var rteLatexAsParent = currentEl.closest(".RTELatex");
		return {
			range : range,
			currentEl : currentEl,
			rteLatexAsParent : rteLatexAsParent
		};
	};
	var RTEAreaDelHelper = function(rteLatex, e) {
		var delClass = 'deleteRTELatexEqn';
		if (rteLatex && rteLatex.hasClass("RTELatex")) {
			e.preventDefault();
			if (rteLatex.hasClass(delClass)) {
				rteLatex.remove();
			} else {
				rteLatex.addClass(delClass);
			}
		}
	};
	var RTEAreaKeyup = function(e) {
		// SyntaxHighlighter.all();
		textFormatCommandStatus($(this).parent());
	};
	var RTEDelBS = function(latexNode) {
		if (latexNode.hasClass("RTELatexRemovable"))
			latexNode.remove();
		else
			latexNode.addClass("RTELatexRemovable");
	};
	var getRTEContent = function(rteHolder) {
		if(!contentEditableSupport){
			var rteArea = rteHolder.children(".RTEArea");
			return $.trim(rteArea.val());
		}
		var rteArea = rteHolder.children(".RTEArea").clone(true);
		rteArea.find(".MathJax_Preview,.MathJax_Display").remove();
		rteArea.find("script").each(
				function() {
					var $this = $(this);
					if ($this.attr("id").indexOf("MathJax-Element-") != -1) {
						var latex = "\\[" + $this.text() + "\\]";
						if ($this.parent().hasClass("RTELatex")) {
							$this.parent().html(latex);
						} else {
							$(
									"<div class='RTELatex' contenteditable=false>"
											+ latex + "</div>").insertAfter(
									$this);
							$this.remove();
						}
					}
				});
		// rteArea.find(".RTEEqn,.RTELatex").each(function(){
		// var
		// latexDiv=$(this),tex=latexDiv.children("script[type='math/tex']");
		// if(tex.length>0){
		// latexDiv.html(tex.text());
		// }
		// else{
		// latexDiv.html(latexDiv.data("latexEqn"));
		// latexDiv.removeClass().addClass("RTEEqn");
		// }
		// });

		// var urlStore={},i=0;
		// rteArea.find(".vUrl,.vMailto").each(function(){
		// var $this=$(this);
		// urlStore["vUrl"+i]={urltxt:$this.text().trim(),urlsrc:$this.attr("src"),urlhref:$this.attr("href")};
		// $this.attr("vurlcount","vUrl"+i).text("").removeAttr("href").removeAttr("src");
		// i++;
		// });

		rteArea.find(".RTETempDiv").remove();
		// var rtehtml=filterJS(rteArea.html().trim());
		// urlify
		// var newDiv=makeHTMLTag("div").html(urlify(rtehtml));
		// newDiv.find(".vUrl,.vMailto").each(function(){
		// var $this=$(this),vurlcount=$this.attr("vurlcount"),d={};
		// if(vurlcount&&urlStore[vurlcount])d=urlStore[vurlcount];
		// var href=d.urlhref,src=d.urlsrc,txt=d.urltxt;
		// if(href)$this.attr("href",href)
		// if(txt)$this.text(txt);
		// if(src)$this.attr("src",src);
		// $this.removeAttr("vurlcount");
		// });
		return rteArea.html().trim();
	};
	this.getRTEContent = getRTEContent;
	var putRTEContent = function(rteArea, htmlContent) {
		rteArea.html(htmlContent);
		var cbfn = function() {
			rteArea.find(".MathJax_Display").attr("contenteditable", false);
		}
		loadMJEqns(rteArea.get(0), cbfn);
	};
	this.putRTEContent = putRTEContent;
	// fe
	var removeRTELatexClick = function() {
		$(this).closest(".RTELatex").remove();
	};
	// video and image addition
	var RTEUrlInputPaste = function() {
		var $this = $(this);
		setTimeout(function() {
			checkRTEUrl($this);
		}, 10)
	};
	var RTEUrlInputKeyup = function(e) {
		if (((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13))) {
			checkRTEUrl($(this));
		}
	};
	var RTEUrlSubmitClick = function() {
		checkRTEUrl($(this).closest(".RTEUrlBox").find(".RTEUrlInput"));
	};
	var checkRTEUrl = function($this) {
		var url = $this.val(), target = $this.data("urlTarget"), rteHolder = $this
				.closest(".RTEHolder");
		if (url.length > 0 && checkUrl(url)) {
			if (url.indexOf("http") < 0)
				url = "http://" + url;
			var domain = url.substring(0, url
					.indexOf("/", url.lastIndexOf(".")));
			if (target == "VIDEO")
				insertVideoFinal(url, domain, rteHolder);
			else if (target == "IMAGE")
				insertImageInRTE(rteHolder, url, true);
		} else
			showError("Please enter a valid url");
	};
	var setUrlBox = function($this, urlTarget) {
		var urlBox = $this.closest(".RTEHolder").find(".RTEUrlBox");
		urlBox.removeClass("nonner");
		urlBox.find(".RTEUrlInput").val("").data("urlTarget", urlTarget)
				.focus();
		urlBox.find(".RTEUrlSubmit").removeClass("nonner");
		urlBox.find(".RTEUrlLoading").addClass("nonner");
	};
	var getRTETempDiv = function(rteHolder, dataClass) {
		var temp = rteHolder.find(".RTETempDiv");
		temp.removeClass().addClass(dataClass);
		rteHolder.find(".RTEUrlBox").addClass("nonner");
		return temp;
	};
	var RTEUrlCancelClick = function() {
		$(this).closest(".RTEUrlBox").addClass("nonner");
	};
	// image addition
	var RTEImageClick = function() {
		setUrlBox($(this), "IMAGE");
	};
	var insertImageInRTE = function(rteHolder, imgSrc, addTag) {
		var imgHMTL;
		if (addTag)
			imgHMTL = "<img src='" + imgSrc + "' class='vUrl'/>";
		else {
			var el = makeHTMLTag("div");
			el.html(imgSrc);
			el.find("img").addClass("vUrl");
			imgHMTL = el.html();
		}
		getRTETempDiv(rteHolder, "RTEImageDiv").html(imgHMTL);
	};
	this.insertImageInRTE = insertImageInRTE;
	// video addition
	var RTEVideoClick = function() {
		setUrlBox($(this), "VIDEO");
	};
	var insertVideoFinal = function(url, domain, rteHolder) {
		$
				.get(
						"/widgets/fetchVideoInfo",
						{
							url : url
						},
						function(data) {
							if (data.result && data.result.title != ""
									&& data.errorMessage == "") {
								var d = data.result;
								getRTETempDiv(rteHolder, "RTEVideoDiv")
										.html(
												"<div style='float:left;margin-right:10px;' contenteditable=false>\n\
                    <a class='videoThumbWrapper playRTEVideo'>\n\
                <img src='"
														+ d.image
														+ "' alt='Image not available' class='vUrl'/>"
														+ getDuration(d.duration)
														+ "</a></div>\n\
                <div><a class='boldy vUrl RTEVideoTitle' target='_blank' href='"
														+ d.url
														+ "'>\n\
                "
														+ d.title
														+ "</a><a href='"
														+ domain
														+ "' target='_blank' class='blocky vUrl'>"
														+ domain
														+ "</a>\n\
                <div class='margTop'>"
														+ d.description
														+ "</div></div>");
							} else
								showError("There was some error. Please check the url and try again.");
						});
	};
	var isRTEEmpty = function(holder) {
		var area = $(holder).find(".RTEArea");
		var text = "";
		if(contentEditableSupport){
			text = $.trim(area.text());
		}else{
			text = $.trim(area.val());
		}
		if (text.length <= 0 && !area.find("img").get(0)) {
			return true;
		}
		return false;
	};
	this.isRTEEmpty = isRTEEmpty;

	// utility functions
	var cancelEvent = function(e) {
		e = e || window.event;
		if (e.preventDefault && e.stopPropagation) {
			e.preventDefault();
			e.stopPropagation()
		}
		return false;
	};
	var makeHTMLTag = function(tag, attrs) {
		var el = document.createElement(tag);
		if (attrs) {
			for ( var k in attrs)
				el.setAttribute(k, attrs[k]);
		}
		return $(el);
	};
})(jQuery);

// for tablet
$(document).on("click", ".RTEAddDiagram", function() {
	doubtJSInterface.addImageClick();
});
function appendUploadedImgToRTE(imgHtml) {
	var rteHolder = $(".RTEHolder");
	vRTE.insertRTETempDiv(rteHolder);
	vRTE.insertImageInRTE(rteHolder, imgHtml, false);
};
function postDoubtFromJS(title) {
	var rteHolder=$(".RTEHolder");
	var rteContent = vRTE.getRTEContent(rteHolder);
	var isRTEEmpty=vRTE.isRTEEmpty(rteHolder);
	doubtJSInterface.postDoubt(title, rteContent ,isRTEEmpty);
}
