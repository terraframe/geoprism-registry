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
(function() {
  function LocationController($scope, $rootScope, $routeParams, locationService, localizationService, widgetService) {
    var controller = this;

    controller.init = function() {
    	
      var oid = $routeParams.oid; 
      
	  $scope.children = [];
	  $scope.previous = [];	       
    	
      if(oid == null) {
    	  var connection = {
    			  elementId: '#innerFrameHtml',
    			  onSuccess: function(data) {
    				  $scope.previous.push(data.entity);
    				  
    				  controller.load(data);
    			  }
    	  };
    	  
    	  locationService.select(connection, "", "", "", "");
    	  
    	  console.log("LocationController.init");    	  
      }
      else {
    	  controller.open(oid);
      }
    }

    controller.load = function(data) {
      console.log("LocationController.load");

      $scope.children = data.children != null ? data.children.resultSet : [];
      $scope.layers = data.layers;

      $scope.entity = data.entity;
      $scope.universal = {
        value: data.universal,
        options: data.universals
      };

      $scope.hierarchy = {
        value: data.hierarchy,
        options: data.hierarchies
      };

      var config = {
        oid: data.entity.oid,
        universalId: data.universal,
        type: "LM",
        targetGeom: data.childType,
        contextGeom: data.geometryType,
        relationshipId: data.entityRelationship
      };

      var layers = [
        // {name:'context-multipolygon', config: {id: data.entity.oid,
        // type:"LM_CONTEXT"}},

        {
          name: 'target-multipolygon',
          config: config,
          bbox: data.bbox
        }
      ];

      $scope.$broadcast('sharedGeoData', layers);
    }

    controller.select = function(entity, event) {
      if (!$(event.target).hasClass('inner-action')) {
        $scope.$broadcast('cancelEditLocation', {
          id: entity.oid
        });

        var connection = {
          elementId: '#innerFrameHtml',
          onSuccess: function(data) {
            $scope.previous.push(entity);

            controller.load(data);
          }
        };

        locationService.select(connection, entity.oid, "", $scope.layers, $scope.hierarchy.value);
      }
    }

    controller.open = function(entityId, mdRelationshipId) {
      if (entityId && entityId.length > 0) {
        $scope.$broadcast('cancelEditLocation', {
          id: entityId
        });

        var connection = {
          elementId: '#innerFrameHtml',
          onSuccess: function(data) {
            $scope.previous = data.ancestors;

            controller.load(data);
          }
        };

        $scope.children = [];
        $scope.previous = [];
        locationService.open(connection, entityId, $scope.layers, mdRelationshipId);
      }
    }

    controller.back = function(index) {
      if (index !== ($scope.previous.length - 1)) {
        var connection = {
          elementId: '#innerFrameHtml',
          onSuccess: function(data) {
            $scope.previous.splice(index + 1);

            controller.load(data);
          }
        };

        var id = $scope.previous[index].oid;

        locationService.select(connection, id, "", $scope.layers, $scope.hierarchy.value);
      }
    }

    controller.setUniversal = function() {
      var connection = {
        elementId: '#innerFrameHtml',
        onSuccess: function(data) {
          $scope.children = data.children != null ? data.children.resultSet : [];
          $scope.layers = data.layers;

          var config = {
            oid: data.entity.oid,
            universalId: data.universal,
            type: "LM",
            targetGeom: data.childType,
            contextGeom: data.geometryType,
            relationshipId: data.entityRelationship
          };


          var layers = [{
            name: 'target-multipolygon',
            config: config,
            bbox: '[]'
          }];

          $scope.$broadcast('sharedGeoData', layers);
        }
      };

      locationService.select(connection, $scope.entity.oid, $scope.universal.value, $scope.layers, $scope.hierarchy.value);
    }

    controller.setHierarchy = function() {
      var connection = {
        elementId: '#innerFrameHtml',
        onSuccess: function(data) {
          $scope.previous.push(data.entity);

          controller.load(data);
        }
      };

      $scope.children = [];
      $scope.previous = [];

      locationService.select(connection, "", "", "", $scope.hierarchy.value);
    }

    controller.getGeoEntitySuggestions = function(request, response) {
      var limit = 20;

      if (request.term && request.term.length > 0) {

        var connection = {
          onSuccess: function(data) {
            var resultSet = data.resultSet;

            var results = [];

            $.each(resultSet, function(index, result) {
              var label = result.displayLabel;
              var oid = result.oid;

              results.push({
                'label': label,
                'value': label,
                'id': oid
              });
            });

            response(results);
          }
        };

        var text = request.term;

        locationService.getGeoEntitySuggestions(connection, text, limit);
      }
    }

    controller.edit = function(entity) {
      var connection = {
        elementId: '#innerFrameHtml',
        onSuccess: function(entity) {
          $scope.$emit('locationEdit', {
            universal: $scope.universal,
            parent: $scope.entity,
            entity: entity,
            hierarchy: $scope.hierarchy
          });
        }
      };
      console.log("edit")
      locationService.edit(connection, entity.oid);
    }

    controller.editGeometry = function(entity) {
      $scope.$broadcast('editLocation', {
        id: entity.oid
      });
    }

    controller.viewSynonyms = function(entity) {
      var connection = {
        elementId: '#innerFrameHtml',
        onSuccess: function(synonyms) {
          $scope.$emit('locationSynonymEdit', {
            universal: $scope.universal,
            parent: $scope.entity,
            entity: entity,
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
        label: localizationService.localize("layer.category", "ok", "Ok"),
        config: {
          class: 'btn btn-primary'
        },
        callback: function() {
          controller.performRemove(entity);
        }
      });
      buttons.push({
        label: localizationService.localize("dataset", "cancel", "Cancel"),
        config: {
          class: 'btn'
        }
      });

      widgetService.createDialog(title, message, buttons);
    }


    controller.performRemove = function(entity) {
      var connection = {
        elementId: '#innerFrameHtml',
        onSuccess: function(response) {
          var index = controller.findIndex(entity.oid);

          if (index != -1) {
            $scope.children.splice(index, 1);
          }

          controller.open($scope.previous[$scope.previous.length - 1].oid);
        }
      };

      locationService.remove(connection, entity.oid, $scope.layers);
    }

    controller.newInstance = function(_wkt) {
      $scope.$emit('locationEdit', {
        wkt: _wkt || '',
        universal: $scope.universal,
        parent: $scope.entity
      });
      $scope.$apply();
    }

    controller.findIndex = function(entityId) {
      for (var i = 0; i < $scope.children.length; i++) {
        if ($scope.children[i].oid == entityId) {
          return i;
        };
      }

      return -1;
    }


    controller.listItemHover = function(entity, event) {
      $scope.$broadcast('listHoverOver', entity);
    }

    controller.listItemHoverOff = function(entity, event) {
      $scope.$broadcast('listHoverOff', entity);
    }

    controller.scrollTo = function(entityId) {
        var child = null;
        for (var i = 0; i < $scope.children.length; i++) {
          if ($scope.children[i].oid == entityId) {
            child = $scope.children[i];
          };
        }

        widgetService.animate("#location-explorer", {
          scrollTop: this._selected.offset().top
        }, "slow");
      },


      $scope.$on('locationFocus', function(event, data) {
        controller.open(data.id, $scope.hierarchy.value);
      });

    $scope.$on('locationReloadCurrent', function(event) {
      controller.open($scope.previous[$scope.previous.length - 1].oid, $scope.hierarchy.value);
    });

    $scope.$on('hoverChange', function(event, data) {
      $scope.hoverId = data.id;
    });

    $scope.$on('locationEditNew', function(event, data) {
      $scope.$emit('locationEdit', {
        wkt: data.wkt || '',
        geojson: data.geojson,
        universal: $scope.universal,
        parent: $scope.entity,
        afterApply: data.afterApply,
        hierarchy: $scope.hierarchy
      });
      $scope.$apply();
    });

    $rootScope.$on('locationChange', function(event, data) {
      var id = (data.entity.oid !== undefined) ? data.entity.oid : data.entity.oid;

      var index = controller.findIndex(id);

      if (index !== -1) {
        $scope.children[index] = data.entity;
      } else {
        $scope.children.push(data.entity);
      }
    });

    $rootScope.$on('locationLock', function(event, data) {
      if (data.entityId != null) {
        var connection = {
          elementId: '#innerFrameHtml',
          onSuccess: function(entity) {
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
        elementId: '#innerFrameHtml',
        onSuccess: function(entity) {

          $scope.$emit('locationChange', {
            entity: entity
          });
        },
        onFailure: function(e) {
          $scope.errors.push(e.localizedMessage);
        }
      };

      $scope.errors = [];

      locationService.apply(connection, $scope.entity, $scope.parent.id, $scope.layers, $scope.hierarchy.value);
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
        oid: Mojo.Util.generateId(),
        type: "com.runwaysdk.system.gis.geo.Synonym"
      });
    }

    controller.removeSynonym = function(synonym) {
      var removeIndex = null;

      for (var i = 0; i < $scope.synonyms.length; ++i) {
        if ($scope.synonyms[i].oid === synonym.oid) {
          removeIndex = i;
          break;
        }
      }

      if (removeIndex !== null) {
        $scope.synonyms.splice(removeIndex, 1);
        controller.deletedSyns.push(synonym.oid);
      }
    }

    controller.load = function(data) {
      if (data.entity == null) {
        $scope.entity = {
          type: 'com.runwaysdk.system.gis.geo.GeoEntity',
          wkt: data.wkt,
          universal: data.universal.value
        };
      } else {
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
        elementId: '#innerFrameHtml',
        onSuccess: function(entity) {
          controller.clear();

          // $scope.$emit('locationCancel', {});
        },
        onFailure: function(e) {
          $scope.errors.push(e.localizedMessage);
        }
      };

      $scope.errors = [];

      var synIds = [];
      for (var i = 0; i < $scope.synonyms.length; ++i) {
        var id = $scope.synonyms[i].oid;

        if (id.length === 64) {
          synIds.push(id);
        }
      }

      locationService.cancelEditSynonyms(connection, synIds);
    }

    controller.apply = function() {
      var connection = {
        elementId: '#innerFrameHtml',
        onSuccess: function(entity) {

          controller.clear();

          // $scope.$emit('locationReloadCurrent');
        },
        onFailure: function(e) {
          $scope.errors.push(e.localizedMessage);
        }
      };

      $scope.errors = [];

      locationService.applySynonyms(connection, {
        "parent": $scope.entity.oid,
        "synonyms": $scope.synonyms,
        "deleted": controller.deletedSyns
      });
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
          universal : data.universal.value,
          newInstance: true
        };
        
        locationService.editNewGeoObject({
          elementId : '#innerFrameHtml',
          onSuccess : function(resp) {
        	resp.newGeoObject.geometry = data.geojson.geometry;
            if (data.geojson.geometry.type === "Polygon" && resp.geoObjectType.geometryType.toLowerCase() === "multipolygon")
            {
	          resp.newGeoObject.geometry.type = "MultiPolygon";
	          resp.newGeoObject.geometry.coordinates = [resp.newGeoObject.geometry.coordinates];
            }
        	
            $scope.preGeoObject = resp.newGeoObject;
            $scope.postGeoObject = JSON.parse(JSON.stringify(resp.newGeoObject));
            $scope.parentTreeNode = resp.parentTreeNode;
            controller.setGeoObjectType(resp.geoObjectType);
            $scope.show = true;
            console.log(resp);
          },
          onFailure : function(e){
            $scope.errors.push(e.localizedMessage);
          }
        }, data.universal.value, data.parent, data.hierarchy.value);
      }
      else { // Editing an existing GeoObject
        $scope.entity = data.entity;
        
        locationService.fetchGeoObjectFromGeoEntity({
          elementId : '#innerFrameHtml',
          onSuccess : function(resp) {
        	controller.setGeoObjectType(resp.geoObjectType);
        	$scope.preGeoObject = resp.geoObject;
            $scope.postGeoObject = JSON.parse(JSON.stringify(resp.geoObject));
        	
        	// Angular front-end uses the Javascript Date type. Our backend expects dates in epoch format.
	        for (var i = 0; i < $scope.geoObjectType.attributes.length; ++i)
	        {
	          var attr = $scope.geoObjectType.attributes[i];
	        	  
	          if (attr.type === "date" && resp.geoObject.properties[attr.code] != null)
	          {
	            $scope.preGeoObject.properties[attr.code] = new Date(resp.geoObject.properties[attr.code]);
	            $scope.postGeoObject.properties[attr.code] = new Date(resp.geoObject.properties[attr.code]);
	          }
	        }
        	
            $scope.parentTreeNode = resp.parentTreeNode;
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
    
    controller.isParentsInvalid = function() {
      if ($scope.parentTreeNode == null) { return false; }
      
      var parents = $scope.parentTreeNode.parents;
      if (parents == null || parents.length == 0) { return false; }
      
      // enforce that we have at least one valid parent
      for (var i = 0; i < parents.length; ++i)
      {
        var ptnParent = parents[i];
        
        if (ptnParent.geoObject != null && ptnParent.geoObject.properties.displayLabel.localizedValue != null && ptnParent.geoObject.properties.displayLabel.localizedValue.length > 0)
        {
          return false;
        }
      }
      
      return true;
    }
    
    controller.onDateChange = function(key, props) {
      console.log("Date set to", props[key]);
      console.log("typeof = ", typeof props[key]);
    }
    
    controller.getParentSearchFunction = function(ptn) {
      var ctrl = this;
      
      return function(req, resp){ctrl.getGeoObjectSuggestions(req,resp,ptn);}
    }
    
    controller.getGeoObjectSuggestions = function( request, response, ptn ) {
      var limit = 20;
      
      if(request.term && request.term.length > 0) {
        
        var connection = {
          onSuccess : function(data){
            var results = [];
            
            $.each(data, function( index, result ) {
              results.push({'label':result.name, 'value':result.name, 'id':result.code});
            });
            
            response( results );
          }
        };
      
        var text = request.term;
        
        locationService.getGeoObjectSuggestions(connection, text, ptn.geoObject.properties.type);
      }
    }
    
    controller.getParentSearchOpenFunction = function(ptn) {
      return function(code){
        if(code && code.length > 0) {
          
          locationService.getGeoObjectByCode({
            elementId : '#innerFrameHtml',
            onSuccess : function(geoObject) {
              ptn.geoObject = geoObject;
            },
            onFailure : function(e){
              $scope.errors.push(e.localizedMessage);
            }
          }, code, ptn.geoObject.properties.type);
        }
      }
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
      
      // Remove parents that the user has set the input field to blank.
      for (var i = $scope.parentTreeNode.parents.length - 1; i >= 0; --i)
      {
        var ptn = $scope.parentTreeNode.parents[i];
        
        if (ptn.geoObject == null || ptn.geoObject.properties.displayLabel.localizedValue == "")
        {
          $scope.parentTreeNode.parents.splice(i, 1);
        }
      }
      
      // Angular front-end uses the Javascript Date type. Our backend expects dates in epoch format.
      var submitGO = JSON.parse(JSON.stringify($scope.postGeoObject));
      for (var i = 0; i < $scope.geoObjectType.attributes.length; ++i)
      {
    	var attr = $scope.geoObjectType.attributes[i];
    	  
        if (attr.type === "date" && $scope.postGeoObject.properties[attr.code] != null)
        {
          submitGO.properties[attr.code] = $scope.postGeoObject.properties[attr.code].getTime();
        }
      }
          
      locationService.apply(connection, $scope.entity.newInstance, submitGO, $scope.parent.oid, $scope.layers, $scope.parentTreeNode, $scope.$parent.hierarchy.value);
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
        layers: '='
      },
      controller: LocationModalController,
      controllerAs: 'ctrl',
      link: function(scope, element, attrs, ctrl) {}
    }
  }

  function LocationSynonymModal() {
    return {
      restrict: 'E',
      replace: true,
      templateUrl: com.runwaysdk.__applicationContextPath + '/partial/data/browser/location-synonym-modal.jsp',
      scope: {
        layers: '='
      },
      controller: LocationSynonymModalController,
      controllerAs: 'ctrl',
      link: function(scope, element, attrs, ctrl) {}
    }
  }

  angular.module("location-management", ["location-service", "styled-inputs", "editable-map-webgl", "widget-service", "localization-service"]);
  angular.module("location-management")
    .controller('LocationController', LocationController)
    .directive('locationModal', LocationModal)
    .directive('locationSynonymModal', LocationSynonymModal)
})();