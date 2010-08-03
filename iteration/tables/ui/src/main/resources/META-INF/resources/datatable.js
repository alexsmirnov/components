(function ($, richfaces) {
    
        richfaces.ui = richfaces.ui || {};
        
        richfaces.ui.DataTable =  function(id, options) {
            this.id = id;
            this.options = options;
            $super.constructor.call(this,id);
        	$p.attachToDom.call(this, id);
        	
        };
        
    	var $super = richfaces.BaseComponent.extend(richfaces.BaseComponent, richfaces.ui.DataTable);
        var $p = richfaces.BaseComponent.extend(richfaces.BaseComponent,richfaces.ui.DataTable, {});
    	var $super = richfaces.ui.DataTable.$super;
    	
    	$.extend(richfaces.ui.DataTable, {
    		SORTING: "rich:sorting",
    		FILTERING: "rich:filtering",
    		SUBTABLE_SELECTOR:".rich-subtable"
    	});
    	
    	$.extend(richfaces.ui.DataTable.prototype, ( function () {

    		var invoke = function(event, attributes) {
        		richfaces.ajax(this.id, event, {"parameters" : attributes});
        	};
        	
        	var createParameters = function(type, id, arg1, arg2) {
        		var parameters = {}; 
        		var key = this.id + type;
        		parameters[key] = ((id || "") + ":" + (arg1 || "") + ":" + (arg2 || true));
        		
        		var eventOptions = this.options.ajaxEventOption;
        		for (key in eventOptions) {
    				if(!parameters[key]) {
    					parameters[key] = eventOptions[key];
    				}
    			}
        		return parameters;
        	};
        	
        	        	
           	return {
           		
           		name : "RichFaces.ui.DataTable",
            	
            	sort: function(columnId, direction, isClear) {
           			invoke.call(this,null,createParameters.call(this,richfaces.ui.DataTable.SORTING, columnId, direction, isClear));
            	},
           	
            	filter: function(columnId, filterValue, isClear) {
            		invoke.call(this,null,createParameters.call(this,richfaces.ui.DataTable.FILTERING, columnId, filterValue, isClear));
            	},
            	
            	expandAllSubTables: function() {
           			this.invokeOnSubTables('expand');
            	}, 
            	
            	collapseAllSubTables: function() {
            		this.invokeOnSubTables('collapse');
            	},
            	
            	switchSubTable: function(id) {
            		this.getSubTable(id).toggle();
            	}, 
            	
            	getSubTable: function(id) {
            		return richfaces.$(id);
            	}, 
            	
            	invokeOnSubTables: function(funcName) {
            		var elements = $(document.getElementById(this.id)).children(richfaces.ui.DataTable.SUBTABLE_SELECTOR);
            		var invokeOnComponent = this.invokeOnComponent;
            		elements.each(
            				function(){
            					if(this.richfaces && this.richfaces.component) {
            						var component = this.richfaces.component;
            						if(component instanceof RichFaces.ui.SubTable) {
                	        			invokeOnComponent(component, funcName);
                					}
            					}
            				}
            		);
            	}, 
            	
            	invokeOnSubTable: function(id, funcName) {
            		var subtable = this.getSubTable(id);
            		this.invokeOnComponent(subtable, funcName);
            	}, 
            	
            	invokeOnComponent: function(component, funcName) {
            		if(component) {
            			var func = component[funcName];
            			if(typeof func == 'function') {
            				func.call(component);
            			}
            		}
            	}
            }
           	
        })());

})(jQuery, window.RichFaces);

