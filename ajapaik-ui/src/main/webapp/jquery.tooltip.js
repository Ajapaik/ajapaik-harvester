/**
 * "Tooltip" jQuery plugin for easy tooltip functionality.
 * 
 * Author: Taivo Kuusik taivok@gmail.com date: march 2013
 * 
 */
;
(function($, doc, win) {

	"use strict";

	var name = 'tooltip';

	function Tooltip(selector, opts) {

		var self = this;

		this.version = "1.0.1";
		this.$el = $(selector);

		// Plugin default values
		this.defaults = {
			id : "tooltip", // tooltip window id
			container : 'body', // parent container from which tooltips cannot
								// overlap
			content : false, // tooltip content. can be any html
			xOffset : 20, // offset x from mouse cursor
			yOffset : 10, // offset y from mouse cursor
			zindex : 9999,
			speed : 100, // fade in animation speed
			timeout : 500, // timeout before show tooltip
			data : null,
			tolerance : 180
		}

		this.opts = $.extend(this.defaults, opts);

		this.timer = null;

		this.$tooltip = null;
		this.size = {
			w : 400, // tooltip width
			h : 200, // tooltip height
			max : {
				x : null,
				y : null
			},
			min : {
				x : null,
				y : null
			},
			img : {
				w : null,
				h : null
			}
		}
		this.pos = {};
		this.img = null;

		this.init();
	}

	Tooltip.prototype.init = function() {

		this.createTooltip();
		this.bindHandlers();
	}

	Tooltip.prototype.createTooltip = function() {
		var self = this;

		if ($("#" + this.opts.id).size() === 0) {
			var $tooltip = $('<div><div/>').html(
					"<div id='" + this.opts.id + "'></div>").contents();

			$tooltip.css({
				'display' : 'none',
				'z-index' : self.opts.zindex,
				'position' : 'absolute'
			});
			this.$tooltip = $tooltip;
			$("body").append($tooltip);
			return;
		}
		this.$tooltip = $("#" + this.opts.id);
	}

	Tooltip.prototype.getImageSize = function(img) {
		if (!img)
			return false;

		this.size.img.w = img.width;
		this.size.img.h = img.height;
		return this.size.img;
	}
	/**
	 * cross browser
	 */
	Tooltip.prototype.scroll = function(doc) {
		doc = doc || document;

		// doc.body.scrollTop is IE quirkmode only
		return Math.max(doc.documentElement.scrollTop, doc.body.scrollTop);
	}
	/**
	 * sets max and minimum coordinates
	 */
	Tooltip.prototype.setRange = function(e) {
		var scroll = this.scroll();
		var $c = $(this.opts.container);
		var curY = scroll + $(win).height();

		this.size.max.x = $c.offset().left + $c.width();
		this.size.max.y = ($c.height() > curY) ? curY : $c.height();

		this.size.min.x = $c.offset().left;
		this.size.min.y = ($c.height() > scroll) ? scroll : $c.height();

	}
	Tooltip.prototype.bindHandlers = function() {
		var self = this;

		this.$el.on('mouseenter', function(e) {
			var id = $(this).attr('data-id');
			var data = self.opts.data[id];

			self.img = new Image();

			self.setRange(e);

			$(self.img).bind('load', function() {
				self.getImageSize(self.img); // after image is loaded we can get image size
				
				var $image = self.$tooltip.find('img');
				self.$tooltip.find('.spinner').remove(); // remove spinner
				$image.show();

				
				var vp = self.viewport();
				var ratio = self.size.img.h / self.size.img.w;
				
				if(self.size.img.h > vp.h) {
					self.size.img.h = vp.h - 80;
					self.size.img.w = self.size.img.h / ratio; 
				}
				
				if(self.size.img.w > vp.w / 2) {
					self.size.img.w = (vp.w / 2);
					self.size.img.h = self.size.img.w / ratio; 
				}
				
				var imageW = self.size.img.w;
				
				$image.width(imageW);
				self.setTooltipSize(imageW); // set tooltip size according to image width

				var pos = self.calcPosition(e);
				
				self.$tooltip.css("top", pos.y + "px").css("left",
						pos.x + "px");

				if (!self.timer) {
					self.$tooltip.show();
				}
			});
			self.img.src = data.img;

			var content = '<div class="tooltip-content">'
					+ '<div class="tooltip-img loading">'
					+ '<div class="spinner" style="width: ' + self.size.w
					+ 'px; height:100px;" ><img src="ajax-loader.gif"/></div>'
					+ '<img class="image" src="' + data.img
					+ '" style="display: none;"/>' + '</div>'
					+ '<div class="tooltip-title">' + data.title + '</div>'
					+ '<div class="tooltip-number">' + data.number + '</div>'
					+ '</div>';

			self.$tooltip.html(content);

			// at first we set tooltip size according to spinner size
			self.setTooltipSize(self.size.w);

			var pos = self.calcPosition(e);
			self.$tooltip.css("top", pos.y + "px").css("left", pos.x + "px");

			self.setTimer(function() {
				self.$tooltip.fadeIn(self.opts.speed);
			});
		});

		this.$el.on('mouseleave', function(e) {
			self.$tooltip.css('display', 'none');
			self.$tooltip.html();
			self.clearTimer();
			if (self.img) {
				$(self.img).unbind('load');
				self.img = null;
			}

		});

		this.$el.on('mousemove', function(e) {
			var pos = self.calcPosition(e);
			self.$tooltip.css("top", pos.y + "px").css("left", pos.x + "px");
		});

	}
	Tooltip.prototype.setTimer = function(callback) {
		var self = this;
		this.clearTimer();
		this.timer = win.setTimeout(function() {
			self.timer = null;
			callback();
		}, this.opts.timeout);
	}
	Tooltip.prototype.clearTimer = function() {
		if (this.timer) {
			win.clearTimeout(this.timer);
			this.timer = null;
		}
	}

	/**
	 * calculates maximum image width
	 * 
	 * @pos - current mouse position
	 */
	Tooltip.prototype.imageWidth = function(pos) {

		var w = this.size.img.w;
		var h = this.size.img.h;
		var tolerance = this.opts.tolerance;

		var mW = this.size.max.x - pos.x - tolerance;
		var mH = this.size.max.y - this.size.min.y - tolerance;

		if (pos.x - this.size.min.x > mW)
			mW = pos.x - this.size.min.x;

		var ratio = w / h;

		if (w + tolerance > mW) {
			w = mW;
		}
		if (h + tolerance > mH) {
			h = mH;
			w = h * ratio;
		}

		return w;
	}
	/**
	 * calculates tooltip box size according to current image width
	 */
	Tooltip.prototype.setTooltipSize = function(imgWidth) {
		if (this.$tooltip) {

			this.size.w = imgWidth;

			this.$tooltip.css({
				'visibility' : 'hidden',
				'display' : 'block'
			});

			this.$tooltip.width(this.size.w);
			this.size.h = this.$tooltip.height();

			this.$tooltip.css({
				'display' : 'none',
				'visibility' : 'visible'
			});

			return true;
		}
		return false;
	}

	Tooltip.prototype.getMousePosition = function(e) {
		if (!e) {
			return this.pos;
		}
		var x = e.pageX + this.opts.xOffset;
		var y = e.pageY - this.opts.yOffset;

		this.pos = {
			x : x,
			y : y
		};
		return this.pos;
	}
	/**
	 * calculates tooltip x and y coordianates
	 */
	Tooltip.prototype.calcPosition = function(e) {

		var img_size = this.size.img;
		var pos = this.getMousePosition(e);
		var vp = this.viewport();
		var t = $(window).scrollTop();
		
		pos.y = t + Math.max(((vp.h - img_size.h) / 2) - 10, 10);
		
		if((pos.x + img_size.w + 20) > vp.w) {
			pos.x = pos.x - img_size.w - 50; 
		}
		
		return pos;
	}
	
	Tooltip.prototype.viewport = function() {
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
	
	// initialises our jQuery plugin
	$.fn._tooltip = function(opts) {
		new Tooltip(this, opts);
	};

})(jQuery, document, window);