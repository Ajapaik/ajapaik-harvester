"use strict"

var ImageSearch = function(o) {

	var defaults = {
		'destination' : '#search-results',
		'action' : '#action1',
		'searchForm' : '#search-form',
		'url' : 'req url goes here',
		'resultSize' : 60
	};

	this.opts = $.extend(defaults, o);
	this.$dest = $(this.opts.destination);
	this.$form = $(this.opts.searchForm);
	this.selected = []; // array of currently selected id-s
	this.term = ""; // search term
	this.offset = 0; // results offset
	this.loaded = false; // if all results are diplayed and there is no more
							// coming from server.
	this.loading = 0; // request status.

	this.id = 1;
	this.serviceUrl = 'http://localhost:8080/ajapaik-service/';

	this.result = null;
	
	this.init();
}

ImageSearch.prototype.init = function() {
	var self = this;

	this.$dest.find('li.item').each(function() {
		if ($(this).hasClass('selected')) {
			self.selectItem(this);
		}
	});

	this.bindHandlers();
}

ImageSearch.prototype.bindHandlers = function() {
	var self = this;

	$(this.opts.action).on('click', function(e) {
		e.preventDefault();
		alert(self.selected);
	});

	this.$dest.on('click', 'li.item', function(e) {
		if ($(this).hasClass('selected')) {
			self.unselectItem(this);
			$(this).removeClass('selected');
		} else {
			self.selectItem(this);
			$(this).addClass('selected');
		}
	});
	/**
	 * search action
	 */
	this.$form.find('input[type="submit"]').on('click', function(e) {
		e.preventDefault();
		
		self.term = self.$form.find('input[name="s"]').val();

		if (self.term.length > 0) {
			
			self.$dest.find('ul.items').html('');
			self.offset = 0;
			
			var search = {
				"fullSearch" : {
					"value" : self.term
				},
				"pageSize" : self.opts.resultSize,
				"digital" : true
			}

			self.request("search", [ search ], function(result) {
				self.result = result;
				
				self.loadData();
			});
		}
	});

	/**
	 * Load more data on page scroll
	 */
	$(document).scroll(function(e) {
		var scrollPos = ($(document).scrollTop() + $(window).height()) / $(document).height()
		if (scrollPos > 0.8 && self.term.length > 0 && !self.loaded) {
			self.loadData();
		}
	});
}

ImageSearch.prototype.selectItem = function(item) {
	var id = $(item).attr('data-id');
	this.selected.push(id);
}

ImageSearch.prototype.unselectItem = function(item) {
	var id = $(item).attr('data-id');
	var len = this.selected.length;
	while (len--) {
		if (this.selected[len] === id) {
			this.selected.splice(len, 1);
			return true;
		}
	}
	return false;
}

ImageSearch.prototype.parseResult = function(data) {
	var html = '';

	if ($.isArray(data)) {
		if (data.length > 0) {
			for ( var i = 0; i < data.length; i++) {
				html += '<li class="item" data-id="' + data[i].id
						+ '"><img src="' + this.serviceUrl + "images/" + data[i].cachedThumbnailUrl + '" title="' + data[i].description + '" /></li>';
			}
			this.offset = this.offset + i;
			this.$dest.find('ul.items').append(html);
		} else {
			this.loaded = true;
		}
	}
	this.hideLoader();
}

ImageSearch.prototype.showLoader = function() {
	var w = this.$dest.find('ul.items').width();
	var html = '<li class="loader" style="display: none; width: ' + w
			+ 'px;"><img src="ajax-loader.gif" /></li>';
	var $loader = $(html);
	this.$dest.find('ul.items').append($loader);

	this.loading = 1;
	$loader.show();
}
ImageSearch.prototype.hideLoader = function() {
	this.loading = 0;
	this.$dest.find('li.loader').hide();
}

ImageSearch.prototype.loadData = function() {
	var self = this;

	self.showLoader();

	var data;
	if(self.offset == 0) {
		self.parseResult(self.result.firstRecordViews);
	} else {
		self.request("getRecords", [ self.result.ids.slice(self.offset, self.offset + self.opts.resultSize) ], function(result) {
			self.parseResult(result);
		});
	}
}

ImageSearch.prototype.request = function(method, params, callback) {
	var self = this, res = null, url = this.serviceUrl + 'AjapaikService.json', requestParams = {
		"method" : method,
		"params" : params,
		"id" : self.id++
	};

	$.ajax({
		type : 'POST',
		url : url,
		dataType : "json",
		contentType : 'application/json',
		data : JSON.stringify(requestParams),
		success : function(msg) {
			// If msg has result, then it's data structure will be according to
			// API
			if (msg.result != null) {
				console.log("Result: " + msg.result);

				callback(msg.result);
				// If has no result, it must have error
			} else if (typeof msg.error != 'undefined') {
				console.log("Error: " + msg.error.message);
			}
			
		},
		
		/**
		 * 1. no connection to serviceUrl 2. incorrect data structure in request
		 * 3. service server fail for some reason (overload)
		 */
//		error : function(xhr, ajaxOptions, thrownError) {
//			res = {
//				"success" : "",
//				"error" : {
//					"message" : xhr.statusText,
//					"code" : xhr.status
//				}
//			}
//
//			callback(res);
//		}
	});
	return null;
}

$(document).ready(function() {

	var search = new ImageSearch({
		'results' : '#search-results'
	});

});

//var is = {
//	"name" : "MuIS",
//	"address" : "http://www.muis.ee/OAIService/OAIService",
//	"useSet" : null,
//	"mapper" : "ee.ajapaik.harvester.MuisHarvestTask",
//	"lastHarvestTime" : null,
//	"running" : null,
//	"schedule" : {
//		"monday" : true,
//		"tuesday" : false,
//		"wednesday" : false,
//		"thursday" : false,
//		"friday" : false,
//		"saturday" : false,
//		"sunday" : false,
//		"updateTime" : "18:53",
//		"active" : true
//	},
//	"email" : "muis@muis.ee",
//	"homepageUrl" : "http://www.muis.ee"
//}