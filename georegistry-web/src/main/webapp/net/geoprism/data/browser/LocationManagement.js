/*
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
(function(){
  function LocationController($scope, $rootScope, locationService, localizationService, widgetService) {
    var controller = this;
    
    controller.init = function() {
      var connection = {
        elementId : '#innerFrameHtml',
        onSuccess : function(data) {
          $scope.previous.push(data.entity);
          
          controller.load(data);
        }      
      };      
      
      $scope.children = [];
      $scope.previous = [];
      
      locationService.select(connection, "", "", "" );
      
      console.log("LocationController.init");
    }
    
    controller.load = function(data) {
      console.log("LocationController.load");
      
	      $scope.children = data.children != null ? data.children.resultSet : [];
	      $scope.layers = data.layers;
	      
	      $scope.entity = data.entity;
	      $scope.universal = {
	        value : data.universal,
	        options : data.universals
	      };
	      
	      var config = {
	        oid: data.entity.oid, 
	        universalId: data.universal,
	    	type:"LM",
  	    	targetGeom:data.childType,
  	    	contextGeom:data.geometryType	    		
	      };
	      
	      var layers = [
	//        {name:'context-multipolygon', config: {id: data.entity.oid, type:"LM_CONTEXT"}},
	    	  
	        {name:'target-multipolygon', config:config, bbox:data.bbox}
	      ];
	      
	      $scope.$broadcast('sharedGeoData', layers);
    }
    
    controller.select = function(entity, event) {
      if(!$(event.target).hasClass('inner-action')) {
        $scope.$broadcast('cancelEditLocation', {
          id : entity.oid
        });
        
        var connection = {
          elementId : '#innerFrameHtml',      
          onSuccess : function(data) {
            $scope.previous.push(entity);          
              
            controller.load(data);
          }
        };    
        
        locationService.select(connection, entity.oid, "", $scope.layers );        
      }
    }
    
    controller.open = function(entityId) {
      if(entityId && entityId.length > 0) {
        $scope.$broadcast('cancelEditLocation', {
          id : entityId
        });
        
        var connection = {
          elementId : '#innerFrameHtml',
          onSuccess : function(data) {
            $scope.previous = data.ancestors;
                  
            controller.load(data);
          }      
        };      
               
        $scope.children = [];
        $scope.previous = [];
        locationService.open(connection, entityId, $scope.layers);
      }
    }
    
    controller.back = function(index) {
      if(index !== ($scope.previous.length - 1)) {
        var connection = {
          elementId : '#innerFrameHtml',            
          onSuccess : function(data) {            
            $scope.previous.splice(index + 1);
            
            controller.load(data);
          }
        };    
                
        var id = $scope.previous[index].oid;
                
        locationService.select(connection, id, "", $scope.layers);        
      }
    }
    
    controller.setUniversal = function() {
      var connection = {
        elementId : '#innerFrameHtml',
        onSuccess : function(data) {          
          $scope.children = data.children != null ? data.children.resultSet : [];
          $scope.layers = data.layers;
          
	      var config = {
  	        oid: data.entity.oid, 
  	        universalId: data.universal,
  	    	type:"LM",
  	    	targetGeom:data.childType,
  	    	contextGeom:data.geometryType	    		
 	      };

      
          var layers = [
            {name:'target-multipolygon', config: config, bbox:'[]'}
          ];
      
          $scope.$broadcast('sharedGeoData', layers);
        }
      };
      
      locationService.select(connection, $scope.entity.oid, $scope.universal.value, $scope.layers);      
    }
    
    controller.getGeoEntitySuggestions = function( request, response ) {
      var limit = 20;
      
      if(request.term && request.term.length > 0) {
        
        var connection = {
          onSuccess : function(data){
            var resultSet = data.resultSet;
            
            var results = [];
            
            $.each(resultSet, function( index, result ) {
              var label = result.displayLabel;
              var id = result.id;
              
              results.push({'label':label, 'value':label, 'id':id});
            });
            
            response( results );
          }
        };
      
        var text = request.term;
        
        locationService.getGeoEntitySuggestions(connection, text, limit);
      }
    }
    
    controller.edit = function(entity) {
      var connection = {
        elementId : '#innerFrameHtml',
        onSuccess : function(entity) {
          $scope.$emit('locationEdit', {
            universal : $scope.universal,
            parent : $scope.entity,
            entity : entity
          });
        }      
      };      
      console.log("edit")
      locationService.edit(connection, entity.oid);
    }
    
    controller.editGeometry = function(entity) {
      $scope.$broadcast('editLocation', {
        id : entity.oid
      });
    }
    
    controller.viewSynonyms = function(entity) {
      var connection = {
          elementId : '#innerFrameHtml',
          onSuccess : function(synonyms) {
            $scope.$emit('locationSynonymEdit', {
              universal : $scope.universal,
              parent : $scope.entity,
              entity : entity,
              synonyms: synonyms
            });
          }      
        };      
        
      locationService.viewSynonyms(connection, entity.oid);
    }
    
    controller.remove = function(entity) {
      var title = localizationService.localize("location.management", "removeOptionTitle", "Delete location");

      var message = localizationService.localize("location.management", "removeConfirm", "Are you sure you want to delete the location [{0}]?");
      message = message.replace('{0}', entity.displayLabel);
              
      var buttons = [];
      buttons.push({
        label : localizationService.localize("layer.category", "ok", "Ok"),
        config : {class:'btn btn-primary'},
        callback : function(){
          controller.performRemove(entity);
        }
      });
      buttons.push({
        label : localizationService.localize("dataset", "cancel", "Cancel"),
        config : {class:'btn'}
      });
              
      widgetService.createDialog(title, message, buttons);    
    }
    
    
    controller.performRemove = function(entity) {
      var connection = {
        elementId : '#innerFrameHtml',
        onSuccess : function(response) {
          var index = controller.findIndex(entity.oid);
          
          if(index != -1){
            $scope.children.splice(index, 1);
          }          
          
          controller.open($scope.previous[$scope.previous.length-1].oid);
        }
      };
      
      locationService.remove(connection, entity.oid, $scope.layers);
    }    
    
    controller.newInstance = function(_wkt) {
      $scope.$emit('locationEdit', {
        wkt : _wkt || '',
        universal : $scope.universal,
        parent : $scope.entity
      });
      $scope.$apply();
    }
    
    controller.findIndex = function(entityId) {
      for(var i = 0; i < $scope.children.length; i++) {
        if($scope.children[i].oid == entityId) {
          return i;
        };
      }
      
      return -1;
    }
    
    
    controller.listItemHover = function(entity, event){
      $scope.$broadcast('listHoverOver', entity);
    }
    
    controller.listItemHoverOff = function(entity, event){
      $scope.$broadcast('listHoverOff', entity);
    }
    
    controller.scrollTo = function(entityId) {
      var child = null;
      for(var i = 0; i < $scope.children.length; i++) {
        if($scope.children[i].oid == entityId) {
          child = $scope.children[i];
        };
      }
      
      widgetService.animate("#location-explorer", {scrollTop: this._selected.offset().top}, "slow");
    },
    
    
    $scope.$on('locationFocus', function(event, data){
        controller.open(data.id);
    });
    
    $scope.$on('locationReloadCurrent', function(event){
      controller.open($scope.previous[$scope.previous.length-1].oid);
    });
    
    $scope.$on('hoverChange', function(event, data){
      $scope.hoverId = data.id;
    });
    
    $scope.$on('locationEditNew', function(event, data){
      $scope.$emit('locationEdit', {
        wkt : data.wkt || '',
        universal : $scope.universal,
        parent : $scope.entity,
        afterApply: data.afterApply
      });
      $scope.$apply();
    });
    
    $rootScope.$on('locationChange', function(event, data) {
      var id = (data.entity.oid !== undefined) ? data.entity.oid : data.entity.oid;
      
      var index = controller.findIndex(id);
      
      if(index !== -1) {
        $scope.children[index] = data.entity;
      }
      else {
        $scope.children.push(data.entity);
      }
    });    

    $rootScope.$on('locationLock', function(event, data) {
      if(data.entityId != null) {
        var connection = {
          elementId : '#innerFrameHtml',
          onSuccess : function(entity) {
            entity.wkt = data.wkt;
            
            $scope.parent = $scope.entity;
            $scope.entity = entity;
            
            controller.apply();
          }      
        };      
        
        locationService.edit(connection, data.entityId);        
      }
    });
    
    controller.apply = function() {
        var connection = {
          elementId : '#innerFrameHtml',
          onSuccess : function(entity) {
            
            $scope.$emit('locationChange', {
              entity : entity  
            });
          },
          onFailure : function(e){
            $scope.errors.push(e.localizedMessage);
          }                
        };
                                
        $scope.errors = [];
            
        locationService.apply(connection, $scope.entity, $scope.parent.id, $scope.layers);        
    }
    
    controller.init();
  }
  
  function LocationSynonymModalController($scope, $rootScope, locationService) {
    var locationController = controller;
    var controller = this;
        
    controller.init = function() {
      $scope.show = false;
      controller.deletedSyns = [];
    }
    
    controller.newSynonym = function() {
      $scope.synonyms.push({
        displayLabel: "",
        id: Mojo.Util.generateId(),
        type: "com.runwaysdk.system.gis.geo.Synonym"
      });
    }
    
    controller.removeSynonym = function(synonym) {
      var removeIndex = null;
      
      for (var i = 0; i < $scope.synonyms.length; ++i)
      {
        if ($scope.synonyms[i].id === synonym.id)
        {
          removeIndex = i;
          break;
        }
      }
      
      if (removeIndex !== null)
      {
        $scope.synonyms.splice(removeIndex, 1);
        controller.deletedSyns.push(synonym.id);
      }
    }
    
    controller.load = function(data) {
      if(data.entity == null) {
        $scope.entity = {
          type : 'com.runwaysdk.system.gis.geo.GeoEntity',
          wkt : data.wkt,
          universal : data.universal.value
        };        
      }
      else {
        $scope.entity = data.entity;
      }
      
      $scope.synonyms = data.synonyms;
      $scope.universals = data.universal.options;
      $scope.parent = data.parent;
      $scope.show = true;
    }
        
    controller.clear = function() { 
      $scope.entity = undefined;
      $scope.parent = undefined;
      $scope.show = false;
      $scope.synonyms = [];
      controller.deletedSyns = [];
    }
    
    controller.cancel = function() {
      var connection = {
        elementId : '#innerFrameHtml',
        onSuccess : function(entity) {
          controller.clear();
          
//          $scope.$emit('locationCancel', {});
        },
        onFailure : function(e){
          $scope.errors.push(e.localizedMessage);
        }                
      };
      
      $scope.errors = [];
      
      var synIds = [];
      for (var i = 0; i < $scope.synonyms.length; ++i)
      {
        var id = $scope.synonyms[i].id;
        
        if (id.length === 64)
        {
          synIds.push(id);
        }
      }
      
      locationService.cancelEditSynonyms(connection, synIds);                      
    }
    
    controller.apply = function() {
      var connection = {
        elementId : '#innerFrameHtml',
        onSuccess : function(entity) {
          
          controller.clear();
          
//          $scope.$emit('locationReloadCurrent');
        },
        onFailure : function(e){
          $scope.errors.push(e.localizedMessage);
        }                
      };
      
      $scope.errors = [];
      
      locationService.applySynonyms(connection, { "parent" : $scope.entity.oid, "synonyms" : $scope.synonyms, "deleted" : controller.deletedSyns });        
    }
      
    $rootScope.$on('locationSynonymEdit', function(event, data) {
      controller.load(data);
    });
       
    controller.init();
  }
  
  function LocationModalController($scope, $rootScope, locationService) {
    var locationController = controller;
    var controller = this;
        
    controller.init = function() {
      $scope.show = false;
    }
        
    controller.load = function(data) {
      if(data.entity == null) { // Used when creating a new GeoObject
        $scope.entity = {
          type : 'com.runwaysdk.system.gis.geo.GeoEntity',
          wkt : data.wkt,
          universal : data.universal.value
        };
        
        locationService.editNewGeoObject({
          elementId : '#innerFrameHtml',
          onSuccess : function(resp) {
            $scope.preGeoObject = resp.newGeoObject;
            $scope.postGeoObject = JSON.parse(JSON.stringify(resp.newGeoObject));
            controller.setGeoObjectType(resp.geoObjectType);
            $scope.show = true;
            console.log(resp);
          },
          onFailure : function(e){
            $scope.errors.push(e.localizedMessage);
          }
        }, data.universal.value);
      }
      else { // Editing an existing GeoObject
        $scope.entity = data.entity;
        
        locationService.fetchGeoObjectFromGeoEntity({
          elementId : '#innerFrameHtml',
          onSuccess : function(resp) {
            $scope.preGeoObject = resp.geoObject;
            $scope.postGeoObject = JSON.parse(JSON.stringify(resp.geoObject));
            $scope.parentTreeNode = resp.parentTreeNode;
            controller.setGeoObjectType(resp.geoObjectType);
            $scope.show = true;
            console.log(resp);
          },
          onFailure : function(e){
            $scope.errors.push(e.localizedMessage);
          }                
        }, data.entity.oid);
      }
      
      $scope.tabIndex = 0;
      $scope.errors = [];
      $scope.universals = data.universal.options;
      $scope.parent = data.parent;
      $scope.show = false;
    }
    
    controller.setTabIndex = function(index)
    {
      $scope.tabIndex = index;
    }
    
    controller.getGeoObjectTypeTermAttributeOptions = function(termAttributeCode) {
      for (var i=0; i < $scope.geoObjectType.attributes.length; i++) {
        var attr = $scope.geoObjectType.attributes[i];

        if (attr.type === "term" && attr.code === termAttributeCode){
          var attrOpts = attr.rootTerm.children;
  
          if(attrOpts.length > 0){
            return attrOpts;
          }
        }
      }

      return null;
    }
        
    controller.clear = function() { 
      $scope.entity = undefined;
      $scope.parent = undefined;
      $scope.show = false;
    }
    
    controller.setGeoObjectType = function(got)
    {
      var filter = ["uid", "sequence", "type", "lastUpdateDate", "createDate"];
      
      // https://stackoverflow.com/questions/9882284/looping-through-array-and-removing-items-without-breaking-for-loop
      for (var i = got.attributes.length - 1; i >= 0; --i)
      {
        var attr = got.attributes[i];
        
        if (filter.indexOf(attr.code) !== -1)
        {
          got.attributes.splice(i, 1);
        }
      }
      
      $scope.geoObjectType = got;
    }
    
    controller.cancel = function() {
      if($scope.entity.oid !== undefined) {
        var connection = {
          elementId : '#innerFrameHtml',
          onSuccess : function(entity) {
            controller.clear();
            
            $scope.$emit('locationCancel', {});
          },
          onFailure : function(e){
            $scope.errors.push(e.localizedMessage);
          }                
        };
                                        
        $scope.errors = [];
                    
        locationService.unlock(connection, $scope.entity.oid);                      
      }
      else {
        controller.clear();
        
        $scope.$emit('locationCancel', {});        
      }
    }
    
    controller.apply = function() {
      var connection = {
        elementId : '#innerFrameHtml',
        onSuccess : function(entity) {
          
          if (controller.afterApply != null)
          {
            controller.afterApply();
          }
          
          controller.clear();
          
          $scope.$emit('locationChange', {
            entity : entity  
          });
          $scope.$emit('locationReloadCurrent');
        },
        onFailure : function(e){
          $scope.errors.push(e.localizedMessage);
        }                
      };
                              
      $scope.errors = [];
          
      locationService.apply(connection, $scope.entity, $scope.parent.oid, $scope.layers);
    }
      
    $rootScope.$on('locationEdit', function(event, data) {
      controller.afterApply = data.afterApply;
      controller.load(data);
    });
       
    controller.init();
  }
    
  function LocationModal() {
    return {
      restrict: 'E',
      replace: true,
      templateUrl: com.runwaysdk.__applicationContextPath + '/partial/data/browser/location-modal.jsp',
      scope: {
        layers : '='
      },
      controller : LocationModalController,
      controllerAs : 'ctrl',      
      link: function (scope, element, attrs, ctrl) {
      }
    }   
  }
  
  function LocationSynonymModal() {
    return {
      restrict: 'E',
      replace: true,
      templateUrl: com.runwaysdk.__applicationContextPath + '/partial/data/browser/location-synonym-modal.jsp',
      scope: {
        layers : '='
      },
      controller : LocationSynonymModalController,
      controllerAs : 'ctrl',      
      link: function (scope, element, attrs, ctrl) {
      }
    }
  }
  
  angular.module("location-management", ["location-service", "styled-inputs", "editable-map-webgl", "widget-service", "localization-service"]);
  angular.module("location-management")
   .controller('LocationController', LocationController)
   .directive('locationModal', LocationModal)
   .directive('locationSynonymModal', LocationSynonymModal)
})();
