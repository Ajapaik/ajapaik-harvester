var self = this;

var result = {};
var loaded = false;
var pageSize = 60;
var offset = 0;
var selection = [];

var gridSize = 90;

var scrollTo = 0;

var detailView = true;

var url = ""; //"http://ajapaik.ee:8080/ajapaik-ui/";

$(document).ready(function() {

	// Fill search form
	parseHash(location.hash);

	// Search
	$("#search-button").on("click", function(e) {
		search();
	});
	
	$("#search-form input").on("keypress", function(e) {
		
		if(e.keyCode == 13) {
			if(e.currentTarget.id == "task-input") {
				scheduleTask();
			} else {
				search();		
			}
		}
	});

	// Tab navication
	$(".nav-tabs li").on("click", function(e) {
		$(".nav-tabs li").removeClass("active");

		var target = $(e.currentTarget);
		target.addClass("active");
		if (target[0].id === "search") {
			$("#search-form").show();
			$("#result-view").show();
			
			$("#set-form").hide();
			$("#set-view").hide();
            $("#harvester-view").hide();
			
			buildGrid($("#result-view"));
		} else if(target[0].id === "set") {
			$("#search-form").hide();
			$("#result-view").hide();
			$("#harvester-view").hide();

			$("#set-form").show();

			self.parseSelection();
		} else if (target[0].id === "harvester") {
            $("#search-form").hide();
            $("#result-view").hide();
            $("#set-form").hide();
            $("#set-view").hide();

            $("#harvester-view").show();
            showFailedSets()
            loadInfosystems()
		}
	});

	// Select all
	$("#select-all").on("click", function(e) {
		var items = $('.item');
		
		for (var i = 0; i < items.length; i++) {
			var value = $(items[i]).data("id");
			
			if($.inArray(value, selection) == -1) {
				$(items[i]).addClass("selected");
				
				selection.push(value);
			}
		}

		updateSelection();
	});

	// Select none
	$("#select-none").on("click", function(e) {
		selectNone();
	});

	$("#send").on("click", function(e) {
		e.preventDefault();
		
		if(self.selection.length > 0) {
			
			$("#city-input").val("");
			$("#modal .dropdown-menu").html("");
			
			$("#lat").val("");
			$("#lon").val("");
			
			self.request("listCities", [], function(result) {
				_.each(result, function(element) {
					var li = $("<li><a>" + element.name + "</a></li>");
					li.data(element);
					
					li.on("click", function(e) {
						var target = $(e.currentTarget);
						var data = target.data();
						
						$("#city-input").val(data.name);
						$("#lat").val(data.lat);
						$("#lon").val(data.lon);
					});
					
					$("#modal .dropdown-menu").append(li);
				});
			
				showModal(function() {
					$("#modal #loader").fadeOut();
					$("#modal .btn-primary").removeAttr('disabled');
				}, function() {
					var name = $("#city-input").val();
					if(name != "") {
						$("#modal .btn-primary").attr('disabled', 'disabled');
						$("#loader").fadeIn();
						
						var data = null;
						_.each(result, function(element) {
							if(element.name == name) {
								data = element;
							}
						});
						
						if(data != null) {
							self.request("postImages", [data.id, self.selection], function(result) {
								selectNone();
								
								$('#modal').modal("hide");
							});
						} else {
							var city = {
									name: name, 
									lat: $("#lat").val(), 
									lon: $("#lon").val(),
							};
							
							self.request("createCity", [city], function(result) {
								self.request("postImages", [result.id, self.selection], function(result) {
									selectNone();
									
									$('#modal').modal("hide");
								});
							});
						}
					}
				});
			});
		}
	});
	
	$("#download").on("click", function(e) {
		e.preventDefault();
		
		if(self.selection.length > 0) {
			var win = window.open('../ajapaik-service/csv/?ids=' + self.selection, '_blank');
			win.focus();
		}
	});
	
	$("#add-task").on("click", function(e) {
		scheduleTask();
	});

	$("#sync").on("click", function(e) {
		self.request("scheduleProposal", []);
	});
	
	$("#show-tasks").on("click", function(e) {
		$("#task-select .dropdown-menu").html("");
		
		self.request("getTasks", [], function(result) {
			_.each(result, function (task, i) {
				drawTask(task);
			});
		});
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

function parseHash(hash) {
	hash = hash.replace("#", "");
	
	if(hash != "") {
		var keyValues = hash.split("&");
		
		for (var i = 0; i < keyValues.length; i++) {
			var keyValue = keyValues[i].split("=");
			
			$("#" + keyValue[0]).val(keyValue[1]);
		}
		
		search();
	}
}

function search() {
	
	$("#backdrop").fadeIn();

	self.loaded = false;
	
	var hash = null;
	_.each($("#search-form input"), function(item) {
		var value = $(item).val();
		
		if(item.type == "text" && item.id != "task-input" && value != "") {
			if(hash == null) {
				hash = item.id + "=" + $(item).val();
			} else {
				hash += "&" + item.id + "=" + $(item).val();
			}
		}
	});

	if(hash != null) {
		location.hash = hash;
	}
	
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
		"collectionTypes" :  [getValue("PHOTOS"), getValue("PAINTINGS"), getValue("GRAPHIC_ART"), getValue("PERSONA")],
		"institutionTypes" : [getValue("MUSEUM"), getValue("LIBRARY"), getValue("ARCHIVE"), getValue("ETERA"), getValue("DSPACE")],
		
		"pageSize" : 200
	}

	$("#result-view").fadeOut();
	
	self.request("search", [ search ], function(result) {
		
		self.result = result;
		self.offset = result.firstRecordViews != null ? result.firstRecordViews.length : 0;
		
		$("#search span").text(result.ids != null ? result.ids.length: "0");
		
		$("#result-view").html("");
		
		self.parseResult(result.firstRecordViews, true);
	});
}

function selectNone() {
	_.each($("#result-view").children(), function (task, i) {
		_.each(selection, function (s, i) {
			if($(task).data("id") == s) {
				$(task).removeClass("selected");
			}
		});
	});
	
	selection = [];
	
	$("#set-view").fadeOut(function(e) {
		$("#set-view").html("");	
	});
	
	updateSelection();
}

function scheduleTask() {
	var value = $("#task-input").val();
	
	if(value != "") {
		self.request("scheduleTask", [ value ]);
		
		$("#task-input").val("");
	}
}

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
			
			tooltipData[record.id] = {"img": record.imageUrl, "desc":record.description,"title":record.title, "number":record.identifyingNumber};
			
			var img = $("<img height='" + self.gridSize + "' src='" + record.imageUrl + "'>");
			
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
			
			item._tooltip({'container': $("#result-view"), 'data':tooltipData}); // apply tooltip
			
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
			if (typeof msg.result != 'undefined' && typeof callback != 'undefined') {
				callback(msg.result);
			} else if (typeof msg.error != 'undefined') {
				alert("Error: " + msg.error.message);
			}
		}
	});
}

function showModal(close, save) {
	$('#modal').on("hidden.bs.modal", function(event) {
		$('#modal').unbind("hidden.bs.modal");
		$('#modal .btn-primary').unbind("click");
		$('#modal .btn-danger').unbind("click");
		
		close(event);
	}).find(".btn-primary").on("click", function(event) {
		save(event);
	});
	
	return $('#modal').modal({
		"backdrop": "static",
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
					var description = $("<div class='col-sm-8'></div>");
					
					description.append("<p><b><a href='" + record.urlToRecord + "' target='_blank'>" + record.title + "</a></b></p>");
					description.append("<p>" + record.identifyingNumber + "</p>");
					description.append("<p>" + record.providerName.replace("NLIB", "Digar") + "</p>");
					description.append("<p>" + record.description.replace("<", "").replace(">", "") + "</p>");
					
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
					
					var item = $("<div class='col-sm-4'></div>");
					
					item.append(img);
					
					recordContainer.append(item);
					recordContainer.append(description);
					
					$("#set-view").append(recordContainer);
					
				} else {
					recordContainer = $("<div class='item'></div>");
					
					// Image
					var img = $("<img height='90' src='" + record.imageUrl + "'>");
					
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
				
				recordContainer.data("id", record.id);
				
				recordContainer.on("click", function(e) {
					var target = e.target;
					var id = $(this).data("id");
					if(target.nodeName != "A" && confirm("Eemalda valikust?")) {
						
						// Unselect in result-view
						_.each($("#result-view").children(), function (task, i) {
							if($(task).data("id") == id) {
								$(task).removeClass("selected");
							}
						});
						
						// Remove from selection
						var index = selection.indexOf(id);
						selection.splice(index, 1);
						
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

function drawTask(task) {
	var li = null;
	if(task.finished != null) {
		li = $("<li><a><span class='glyphicon glyphicon-remove'></span> <span class='badge'>" + task.taskIds.length + "</span> Ülesanne " + task.id + "</a></li>");
	} else {
		li = $("<li class='disabled'><a><img width='14' src='ajax-loader.gif' style='margin-top: -5px;'>  <span class='badge'>" + task.taskIds.length + "</span> Ülesanne " + task.id + "</a></li>");
	}
	
	li.data(id, task.id);
	
	li.on("click", function(e) {
		if(!$(this).hasClass("disabled") && !$(e.target).hasClass("glyphicon-remove")) {
			$("#result-view").html("");
			
			if(task.taskIds.length > 0) {
				self.loaded = false;
				
				$("#backdrop").fadeIn();
				
				self.request("getRecords", [ task.taskIds ], function(result) {
					self.result = result;
					
					$("#search span").text(result.length);
					
					self.parseResult(result, true);
				});
			}
		}
	});
	
	li.find(".glyphicon-remove").on("click", function(e) {
		if(!li.hasClass("disabled")) {
			self.request("removeTask", [ task.id ]);
		}
	});
	
	$("#task-select .dropdown-menu").append(li);
}