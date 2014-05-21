var self = this;

var result = {};
var loaded = false;
var pageSize = 60;
var offset = 0;
var selection = [];

var gridSize = 90;

var scrollTo = 0;

var detailView = false;

$(document).ready(function() {

	// Search
	$("form").on("submit", function(e) {
		
		e.preventDefault();
		
		$("#backdrop").fadeIn();

		self.loaded = false;
		
		var search = {
			"fullSearch" : {
				"value" : $("#fullSearch").val(),
			},
			"id" : {
				"value" : $("#id").val(),
				"type" : "OR"
			},
			"what" : {
				"value" : $("#what").val(),
			},
			"description" : {
				"value" : $("#description").val(),
			},
			"who" : {
				"value" : $("#who").val(),
			},
			"from" : {
				"value" : $("#from").val(),
			},
			"number" : {
				"value" : $("#number").val(),
			},
			
			"luceneQuery" : $("#luceneQuery").val() == "" ? null : $("#luceneQuery").val(),
			"institutionTypes" : [getValue("MUSEUM"), getValue("LIBRARY"), getValue("ARCHIVE")],
			
			"pageSize" : 200,
			"digital" : true
		}

		$("#result-view").fadeOut();
		
		self.request("search", [ search ], function(result) {
			
			self.result = result;
			self.offset = result.firstRecordViews != null ? result.firstRecordViews.length : 0;
			
			$("#search span").text(result.ids != null ? result.ids.length: "0");
			
			$("#result-view").html("");
			
			self.parseResult(result.firstRecordViews, true);
		});
	});

	// Tab navication
	$(".nav-tabs li").on("click", function(e) {
		$(".nav-tabs li").removeClass("active");

		var target = $(e.currentTarget);
		target.addClass("active");

		if (target[0].id == "search") {
			$("#search-form").show();
			$("#result-view").show();
			
			$("#set-form").hide();
			$("#set-view").hide();
		} else {
			$("#search-form").hide();
			$("#result-view").hide();
			
			$("#set-form").show();

			self.parseSelection();
		}
	});

	// Select all
	$("#select-all").on("click", function(e) {
		var items = $('.item');
		
		items.addClass("selected");

//		self.loaded = false;
		
//		$("#result-view").fadeOut(function() {
//			$("#result-view").html("");
//		});

		for (var i = 0; i < items.length; i++) {
			var value = $(items[i]).data("id");
			
			if($.inArray(value, selection) == -1) {
				selection.push(value);
			}
		}

		updateSelection();
	});

	// Select none
	$("#select-none").on("click", function(e) {
		selection = [];
		
		$("#set-view").fadeOut(function(e) {
			$("#set-view").html("");	
		});
		
		updateSelection();
	});

	$("#download").on("click", function(e) {
		e.preventDefault();
		
		if(self.selection.length > 0) {
			var win = window.open('../ajapaik-service/csv/?ids=' + self.selection, '_blank');
			win.focus();
		}
	});
	
	// Thumb size
	self.thumbs("#sm", 90);
	self.thumbs("#md", 120);
	self.thumbs("#lg", 200);
	
	$("#thumbnails").on("click", function(e) {
		detailView = false;
		
		parseSelection();
	});

	$("#list").on("click", function(e) {
		detailView = true;
		
		parseSelection();
	});
});

$(document).scroll(function(e) {
	var vp = viewport();
	
	if ($("#result-view").height() <= (vp.h + $(window).scrollTop()) && self.loaded) {
		
		var ids = self.result.ids.slice(self.offset, self.offset + self.pageSize);
		
		if(ids.length > 0) {
			self.loaded = false;
			
			$("#backdrop").fadeIn();
			
			self.request("getRecords", [ ids ], function(result) {
				
				self.scrollTo = $(window).scrollTop();
				
				self.parseResult(result, false);
				
				self.offset += self.pageSize;
			});
		}
	}
});

var keys = [ 37, 38, 39, 40 ];

function preventDefault(e) {
	e = e || window.event;
	if (e.preventDefault)
		e.preventDefault();
	e.returnValue = false;
}

function keydown(e) {
	for (var i = keys.length; i--;) {
		if (e.keyCode === keys[i]) {
			preventDefault(e);
			return;
		}
	}
}

function wheel(e) {
	preventDefault(e);
}

function disableScroll() {
	if (window.addEventListener) {
		window.addEventListener('DOMMouseScroll', wheel, false);
	}
	
	window.onmousewheel = document.onmousewheel = wheel;
	document.onkeydown = keydown;
}

function enableScroll() {
	if (window.removeEventListener) {
		window.removeEventListener('DOMMouseScroll', wheel, false);
	}
	
	window.onmousewheel = document.onmousewheel = document.onkeydown = null;
}

function viewport() {
	var viewPortWidth;
	var viewPortHeight;

	// the more standards compliant browsers (mozilla/netscape/opera/IE7) use window.innerWidth and window.innerHeight
	if (typeof window.innerWidth != 'undefined') {
		viewPortWidth = window.innerWidth,
		viewPortHeight = window.innerHeight
	}

	// IE6 in standards compliant mode (i.e. with a valid doctype as the first line in the document)
	else if (typeof document.documentElement != 'undefined'
		&& typeof document.documentElement.clientWidth !=
		'undefined' && document.documentElement.clientWidth != 0) {
		viewPortWidth = document.documentElement.clientWidth,
		viewPortHeight = document.documentElement.clientHeight
	}

	// older versions of IE
	else {
		viewPortWidth = document.getElementsByTagName('body')[0].clientWidth,
		viewPortHeight = document.getElementsByTagName('body')[0].clientHeight
	}

	this.viewport.height = viewPortHeight;
	this.viewport.width = viewPortWidth;
	
	return {w:viewPortWidth, h:viewPortHeight};
}

function parseResult(result, scroll) {
	if (result != null && result.length > 0) {

		disableScroll();
		
		var tooltipData = {};
		var loadedImages = 0;
		for (var i = 0; i < result.length; i++) {
			var record = result[i];
			
			tooltipData[record.id] = {"img":record.imageUrl,"desc":record.description,"title":record.title, "number":record.identifyingNumber};
			
			var img = $("<img height='" + self.gridSize + "' src='http://ajapaik.ee:8080/ajapaik-service/images/" + record.cachedThumbnailUrl + "'>");
			
			img.load(function(e) {
				loadedImages++;
				
				if(loadedImages == result.length) {
					$("#result-view").fadeIn();
					
					self.buildGrid($("#result-view"));
					
					self.loaded = true;
					
					enableScroll();
					
					if(scroll) {
						window.scroll(0, 0);
					} else {
						window.scroll(0, self.scrollTo);
					}
					
					$("#backdrop").fadeOut();
				}
			});
			
			var item = $("<div data-id='" + record.id + "' class='item'></div>");
			item.on('click', function(e) {
				var target = $(this);
				var id = target.data("id");
				
				if(target.hasClass("selected")) {
					target.removeClass("selected");
					
					selection.splice(selection.indexOf(id), 1);
				} else {
					target.addClass("selected");
					
					if($.inArray(id, selection) == -1) {
						selection.push(id);
					}
				}

				updateSelection();
			});
			
			item.append(img);
			
			item.tooltip({'container': $("#result-view"), 'data':tooltipData}); // apply tooltip
			
			$("#result-view").append(item);
		}
		
	} else {
		$("#backdrop").fadeOut();
		
		alert("No result");
	}
}

function updateSelection() {
	$("#set span").text(selection.length);
}

function thumbs(target, height) {
	$(target).on("click", function(e) {
		
		self.gridSize = height;
		
		var items = $(".item img");

		for (var i = 0; i < items.length; i++) {
			items[i].height = height;
		}

		self.buildGrid($("#result-view"));
	});
}

function request(method, params, callback) {
	var self = this, res = null, url = '../ajapaik-service/AjapaikService.json', requestParams = {
		"method" : method,
		"params" : params,
		"id" : 0,
	};

	$.ajax({
		type : 'POST',
		url : url,
		dataType : "json",
		contentType : 'application/json',
		data : JSON.stringify(requestParams),
		success : function(msg) {
			if (msg.result != null) {
				callback(msg.result);
			} else if (typeof msg.error != 'undefined') {
				alert("Error: " + msg.error.message);
			}
		}
	});
}

function buildGrid(target) {
	target.rowGrid({
		itemSelector : ".item",
		minMargin : 10,
		maxMargin : 25,
		firstItemClass : "first-item"
	});
}

function getValue(name) {
	return $("#" + name).prop("checked") ? name : null;
}

function parseSelection() {
	
	$("#set-view").html("");
	
	if(self.selection.length > 0) {
		self.request("getRecords", [selection], function(result) {
			
			$("#backdrop").fadeIn();
			
			var loadedImages = 0;
			for (var i = 0; i < result.length; i++) {
				var record = result[i];
				
				// Container
				var recordContainer = {};
				
				if(detailView) {
					recordContainer = $("<div class='record-container col-sm-12'></div>");
					
					// Description
					var description = $("<div class='col-sm-9'></div>");
					
					description.append("<p><b>" + record.title + "</b></p>");
					description.append("<p>" + record.identifyingNumber + "</p>");
					description.append("<p>" + record.description + "</p>");
					
					// Image
					var img = $("<img src='" + record.imageUrl + "'>");
					
					img.data(description);
					
					img.load(function(e) {
						loadedImages++;
						
						$(this).data().append("<p>" + this.width + "x" + this.height + "</p>");
						
						this.height = 200;
						
						if(loadedImages == result.length) {
							$("#set-view").fadeIn();
							$("#backdrop").fadeOut();
						}
					});
					
					var item = $("<div class='col-sm-3'></div>");
					
					item.append(img);
					
					recordContainer.append(item);
					recordContainer.append(description);
					
					$("#set-view").append(recordContainer);
					
				} else {
					recordContainer = $("<div class='item'></div>");
					
					// Image
					var img = $("<img height='90' src='http://ajapaik.ee:8080/ajapaik-service/images/" + record.cachedThumbnailUrl + "'>");
					
					img.data(description);
					
					img.load(function(e) {
						loadedImages++;
						
						if(loadedImages == result.length) {
							$("#set-view").fadeIn();
							$("#backdrop").fadeOut();
							
							buildGrid($("#set-view"));
						}
					});
					
					recordContainer.append(img);
					
					$("#set-view").append(recordContainer);
				}
				
				recordContainer.data(record.id);
				
				recordContainer.on("click", function(e) {
					if(confirm("Eemalda valikust?")) {
						
						// Remove from selection
						selection.splice(selection.indexOf($(this).data()), 1);
						
						var container = $(this);
						container.fadeOut(function() {
							container.remove();
							
							buildGrid($("#set-view"));
						});
						
						updateSelection();
					}
				});
			}
		});
	}
}
