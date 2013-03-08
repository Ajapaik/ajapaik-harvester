/**
 *  "Tooltip" jQuery plugin for easy tooltip functionality.
 *  
 *  Author: Taivo Kuusik taivok@gmail.com
 *  date:   march 2013
 *  
 */
;(function($, doc, win) {
    
    "use strict";
    
    var name = 'tooltip';
    
    function Tooltip(selector, opts) {
	
	var self = this;
	
	this.version = "1.0.1";
	this.$el  = $(selector);
	
	// Plugin default values
	this.defaults = {
	    id		    : "tooltip",	// tooltip window id
	    container	    : 'body',		// parent container from which tooltips cannot overlap
	    content	    : false,		// tooltip content. can be any html
	    xOffset	    : 20,		// offset x from mouse cursor
	    yOffset	    : 10,		// offset y from mouse cursor
	    zindex	    : 9999,
	    speed	    : 100,		// fade in animation speed
	    timeout	    : 500,		// timeout before show tooltip
	    data	    : null
	}
	
	this.opts = $.extend(this.defaults, opts);
	
	this.timer = null;
	
	this.$tooltip = null;
	this.size = {
	    w:null,		// tooltip width
	    h:null,		// tooltip height
	    xmax: null,		// x max
	    ymax: null		// y max
	}
	
	this.init();
    }
    
    Tooltip.prototype.init = function(){

	this.createTooltip();
	this.bindHandlers();
    }
    
    Tooltip.prototype.createTooltip = function(){
	var self = this;
	
	if($("#"+this.opts.id).size() === 0){
	    var $tooltip = $('<div><div/>').html("<div id='" + this.opts.id + "'></div>").contents();
	    
	    $tooltip.css({
		'display':'none',
		'zindex':self.opts.zindex,
		'position':'absolute'
	    });
	    this.$tooltip = $tooltip;
	    $("body").append($tooltip);
	    return;
	}
	this.$tooltip = $("#"+this.opts.id);
    }
    
    Tooltip.prototype.bindHandlers = function(){
	var self = this;
	
	
	
	this.$el.on('mouseenter',function(e){
		
		var data = self.opts.data[$(this).attr('data-id')];
		
		
		var content = '<div class="tooltip-img"><img src="'+data.img+'" /></div>'+
					    '<div class="tooltip-desc">'+data.desc+'</div>';
	   
		
	   
		self.$tooltip.html(content);
		self.calcSize();

		var pos = self.calcPosition(e);


		self.$tooltip
			    .css("top",pos.y + "px")
			    .css("left",pos.x + "px");
			    
			    
		self.setTimer(function(){self.$tooltip.fadeIn(self.opts.speed);});
	});
	
	this.$el.on('mouseleave',function(e){
	    self.$tooltip.hide().html();
	    self.clearTimer();
	});
	
	
	this.$el.on('mousemove',function(e){
	    var pos = self.calcPosition(e);
	    self.$tooltip
			.css("top",pos.y + "px")
			.css("left",pos.x + "px");
	});
	
    }
    Tooltip.prototype.setTimer = function(callback){
	this.clearTimer();
	this.timer = win.setTimeout(callback,this.opts.timeout);
    }
    Tooltip.prototype.clearTimer = function(){
	if(this.timer){
	    win.clearTimeout(this.timer);
	    this.timer = null;
	}
    }
    Tooltip.prototype.calcSize = function(){
	if(this.$tooltip){
	    
	    var $c = $(this.opts.container);	    
	    var curY = $('body')[0].scrollTop + $(win).height();
	    
	    this.size.xmax = $c.offset().left + $c.width();
	    this.size.ymax = ($c.height() > curY) ? curY : $c.height();
	    
	    this.$tooltip.css({
		'visibility':'hidden',
		'display':'block'
	    });
	    
	    var w = this.$tooltip.find('img').width();
	    
	    this.$tooltip.width(w);
	    
	    this.size.w = w;
	    this.size.h = this.$tooltip.height();
	    
	    this.$tooltip.css({
		'visibility':'visible',
		'display':'none'
	    });
	    
	    return true;
	}
	return false;
    }
    Tooltip.prototype.calcPosition = function(e){
	
	
	var x = e.pageX + this.opts.xOffset;
	var y = e.pageY - this.opts.yOffset;
	
	
	if(x + this.size.w > this.size.xmax){
	    x = x - this.size.w - 2 * this.opts.xOffset;
	}
	
	if(y + this.size.h > this.size.ymax){
	    y = y - this.size.h;	    
	}
	
	return {"x":x,"y":y};
	
    }
    
    
    // initialises our jQuery plugin
    $.fn.tooltip = function(opts) {
	    new Tooltip(this, opts);
    };

})(jQuery, document, window);