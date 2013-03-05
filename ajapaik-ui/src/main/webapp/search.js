"use strict"

var ImageSearch = function(o) {

	var defaults = {
		'destination' : '#search-results',
		'csv' : '#csv',
		'harvest' : '#harvest',
		'index' : '#index',
		'selectAll' : "#selectAll",
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
    
    // set results top margin since header is fixed position.
    var headerH = $(this.opts.header).height();    
    this.$dest.css('margin-top',headerH+"px");
    
	this.bindHandlers();
}

ImageSearch.prototype.bindHandlers = function() {
	var self = this;

	$(this.opts.csv).on('click', function(e) {
		e.preventDefault();
		alert(self.selected);
	});
	
	$(this.opts.harvest).on('click', function(e) {
		e.preventDefault();
		
		var date = new Date();

		var is = {
			"name" : "MuIS",
			"schedule" : {
				"monday" : date.getDay() == 1,
				"tuesday" : date.getDay() == 2,
				"wednesday" : date.getDay() == 3,
				"thursday" : date.getDay() == 4,
				"friday" : date.getDay() == 5,
				"saturday" : date.getDay() == 6,
				"sunday" : date.getDay() == 0,
				"updateTime" : date.getHours() + ":" + (date.getMinutes() + 1),
				"active" : true
			}
		}
		
		self.request("updateInfoSystem", [ is ], function(result) {
			// no result
		});
	});
	
	$(this.opts.index).on('click', function(e) {
		e.preventDefault();
		
		self.request("index", [], function(result) {
			// no result
		});
	});
	

	$(this.opts.selectAll).on('click', function(e) {
		e.preventDefault();
		
		self.$dest.find('li.item').each(function() {
			if (!$(this).hasClass('selected')) {
				self.selectItem(this);
				$(this).addClass('selected');
			}
		});
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
				
				if(result.ids.length > 0) {
					self.loadData();
					
					self.$dest.fadeIn();
				} else {
					self.$dest.hide();
					
					alert('No results');
				}
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