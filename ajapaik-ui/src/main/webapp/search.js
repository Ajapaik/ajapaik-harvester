"use strict"

var ImageSearch = function(o){
    
    var defaults = {
		'destination'	: '#search-results',
		'action'	: '#action1',
		'searchForm'	: '#search-form',
		'url'		: 'req url goes here',
		'results'	: 100			    // how many results to return
    };
    
    this.opts	    = $.extend(defaults,o);
    this.$dest	    = $(this.opts.destination);
    this.$form	    = $(this.opts.searchForm);
    this.selected   = [];			    // array of currently selected id-s
    this.term	    = "";			    // search term
    this.offset	    = 0;			    // results offset
    this.loaded	    = false;			    // if all results are diplayed and there is no more coming from server.
    this.loading    = 0;			    // request status.
    
    this.id = 1;
    this.serviceUrl = 'http://localhost:8080/ajapaik-service/AjapaikService.json';
    
    this.init();
}

ImageSearch.prototype.init = function(){
    var self = this;
    
    this.$dest.find('li.item').each(function(){
	if($(this).hasClass('selected')){
	    self.selectItem(this);
	}
    });
    
    this.bindHandlers();
    
}

ImageSearch.prototype.bindHandlers = function(){
    var self = this;
    
    $(this.opts.action).on('click',function(e){
	e.preventDefault();
	alert(self.selected);
    });
    
    this.$dest.on('click','li.item',function(e){
	if($(this).hasClass('selected')){
	    self.unselectItem(this);
	    $(this).removeClass('selected');
	}else{
	    self.selectItem(this);
	    $(this).addClass('selected');
	}
    });
    /**
     *	search action
     */
    this.$form.find('input[type="submit"]').on('click',function(e){
	e.preventDefault();
	self.term = self.$form.find('input[name="s"]').val();
	
	if(self.term.length > 0){
	    self.loadData();
	}
    });
    
    /**
     *	Load more data on page scroll
     */
    $(document).scroll(function(e){
	var scrollPos = ($(document).scrollTop() + $(window).height()) / $(document).height()
	if(scrollPos > 0.8 && self.term.length > 0 && !self.loaded){
	    self.loadData();
	}
    });
    
    
}

ImageSearch.prototype.selectItem = function(item){
    var id = $(item).attr('data-id');
    this.selected.push(id);
}

ImageSearch.prototype.unselectItem = function(item){
    var id = $(item).attr('data-id');
    var len = this.selected.length;
    while(len--){
	if(this.selected[len] === id){
	    this.selected.splice(len,1);
	    return true;
	}
    }
    return false;
}

ImageSearch.prototype.parseResult = function(data){
    var html = '';
    
    if($.isArray(data.items)){
	if(data.items.length > 0){	
	    for(var i=0; i<data.items.length; i++){
		html += '<li class="item" data-id="'+data.items[i].id+'"><img src="'+data.items[i].img+'" /></li>';
	    }
	    this.offset = this.offset + i;
	    this.$dest.find('ul.items').append(html);
	}else{
	    this.loaded = true;
	}
    } 
    this.hideLoader();
}

ImageSearch.prototype.showLoader = function(){
    var w = this.$dest.find('ul.items').width();
    var html = '<li class="loader" style="display: none; width: '+w+'px;"><img src="ajax-loader.gif" /></li>';
    var $loader = $(html);
    this.$dest.find('ul.items').append($loader);
    
    this.loading = 1;
    $loader.show();
}
ImageSearch.prototype.hideLoader = function(){
    this.loading = 0;
    this.$dest.find('li.loader').hide();
}

ImageSearch.prototype.loadData = function(){
    var self = this;
    var params = {
		    'term' : self.term,
		    'offset' : self.offset,
		    'results' : self.opts.results
		    };
    
    self.showLoader();
    
//    $.ajax({
//	url: self.opts.url,
//	data: params,
//	dataType: 'json',
//	success : function(data){
//	    self.parseResult
//	}
//    });
    
    /** just for testing purpose **/
    
    var data = {
	'items':
		[	
		{'id':2,'img':'nopreview.jpg'},
		{'id':3,'img':'nopreview.jpg'},
		{'id':4,'img':'nopreview.jpg'},
		{'id':5,'img':'nopreview.jpg'},
		{'id':6,'img':'nopreview.jpg'},
		{'id':7,'img':'nopreview.jpg'},
		{'id':8,'img':'nopreview.jpg'},
		{'id':9,'img':'nopreview.jpg'},
		{'id':10,'img':'nopreview.jpg'},
		{'id':11,'img':'nopreview.jpg'},
		{'id':12,'img':'nopreview.jpg'},
		{'id':13,'img':'nopreview.jpg'},
		{'id':14,'img':'nopreview.jpg'},
		{'id':15,'img':'nopreview.jpg'},
		{'id':16,'img':'nopreview.jpg'},
		{'id':17,'img':'nopreview.jpg'}
	    ]
    };
    
    var is = {
    	"name":"MuIS",
    	"address":"http://www.muis.ee/OAIService/OAIService",
    	"useSet":null,
    	"mapper":"ee.ajapaik.harvester.MuisHarvestTask",
    	"lastHarvestTime":null,
    	"running":null,
    	"schedule": {
    		"monday":true,
    		"tuesday":false,
    		"wednesday":false,
    		"thursday":false,
    		"friday":false,
    		"saturday":false,
    		"sunday":false,
    		"updateTime":"18:53",
    		"active":true},
    	"email":"muis@muis.ee",
    	"homepageUrl":"http://www.muis.ee"
    }

    self.request("updateInfoSystem", [ is ], function(res) {
    	
    });
    
    self.parseResult(data);
}

ImageSearch.prototype.request = function(method, params, callback) {
	var self = this,
	    res = null,
	    url = this.serviceUrl,
	    requestParams = {
			"method" : method,
			"params" : params,
			"id"     : self.id++
	    };
	    
	$.ajax({
		type : 'POST',
		url : url,
		dataType : "json",
		contentType : 'application/json',
		data : JSON.stringify(requestParams),
		success : function(msg) {
			// If msg has result, then it's data structure will be according to API
			if(msg.result != null) {
				console.log("Result: " + msg.result);
				res = {
				    "success":msg.result,
				    "error":null
				}
				
			// If has no result, it must have error
			} else if(typeof msg.error != 'undefined'){
				console.log("Error: " + msg.error.message);
				if(msg.error.code == 4) {
					return null;
				}
				res = {
				    "success":"",
				    "error":msg.error
				}			}
			callback(res);
		},
		/**
		 *  1. no connection to serviceUrl
		 *  2. incorrect data structure in request
		 *  3. service server fail for some reason (overload)
		 */
		error: function(xhr, ajaxOptions, thrownError){
			res = {
				"success":"",
				"error":{
					"message": xhr.statusText,
					"code": xhr.status
				}}
			
			callback(res);
		}
	});
	return null;
}

$(document).ready(function(){
   
   var search = new ImageSearch({
       'results': '#search-results'
   });
   
});