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

  function LocationService(runwayService) {
    var service = {};
    
    service.select = function(connection, oid, universalId, existingLayers, config) {
      var req = {
        method: 'POST',
        url: com.runwaysdk.__applicationContextPath + '/location/select',
        data : {
          oid : oid,
          universalId : universalId,
          existingLayers : existingLayers
        }
      }      
      
      runwayService.execute(req, connection);      
    }
    
    service.open = function(connection, id, existingLayers) {
      var req = {
        method: 'POST',
        url: com.runwaysdk.__applicationContextPath + '/location/open',
        data : {
          oid : id,
          existingLayers : existingLayers            
        }
      }      
      
      runwayService.execute(req, connection);      
    }
    
    service.getGeoEntitySuggestions = function(connection, text, limit) {
      var req = {
        method: 'POST',
        url: com.runwaysdk.__applicationContextPath + '/location/suggestions',
        data : {
          text : text,
          limit : limit
        }
      }      
              
      runwayService.execute(req, connection);      
    }
    
    service.apply = function(connection, entity, parentOid, existingLayers) {
      var req = {
        method: 'POST',
        url: com.runwaysdk.__applicationContextPath + '/registrylocation/apply',
        data : {
          entity : entity,
          parentOid : parentOid,          
          existingLayers : existingLayers
        }
      }      
      
      runwayService.execute(req, connection);      
    }
    
    service.applySynonyms = function(connection, data) {
      var req = {
        method: 'POST',
        url: com.runwaysdk.__applicationContextPath + '/location/applyEditSynonyms',
        data : {
          synonyms : data
        }
      }      
      
      runwayService.execute(req, connection);      
    }
    
    service.openEditingSession = function(connection, config) {
      var req = {
        method: 'POST',
        url: com.runwaysdk.__applicationContextPath + '/location/openEditingSession',
        data : {
          config : config
        }
      }
      
      runwayService.execute(req, connection);
    }
    
    service.cancelEditSynonyms = function(connection, synonyms) {
      var req = {
        method: 'POST',
        url: com.runwaysdk.__applicationContextPath + '/location/cancelEditSynonyms',
        data : {
          synonyms : synonyms
        }
      }
      
      runwayService.execute(req, connection);
    }
    
    service.cancelEditingSession = function(connection, config) {
      var req = {
        method: 'POST',
        url: com.runwaysdk.__applicationContextPath + '/location/cancelEditingSession',
        data : {
          config : config
        }
      }
      
      runwayService.execute(req, connection);
    }
    
    service.applyGeometries = function(connection, featureCollection) {
      var req = {
        method: 'POST',
        url: com.runwaysdk.__applicationContextPath + '/location/applyGeometries',
        data : {
          featureCollection : featureCollection
        }
      }
      
      runwayService.execute(req, connection);
    }
      
    service.edit = function(connection, entityId) {
      var req = {
        method: 'POST',
        url: com.runwaysdk.__applicationContextPath + '/registrylocation/edit',
        data : {
          entityId : entityId
        }
      }      
      
      runwayService.execute(req, connection);      
    }
    
    service.viewSynonyms = function(connection, entityId) {
      var req = {
        method: 'POST',
        url: com.runwaysdk.__applicationContextPath + '/location/viewSynonyms',
        data : {
          entityId : entityId
        }
      }      
      
      runwayService.execute(req, connection);      
    }
    
    service.unlock = function(connection, entityId) {
      var req = {
        method: 'POST',
        url: com.runwaysdk.__applicationContextPath + '/location/unlock',
        data : {
          entityId : entityId
        }
      }      
      
      runwayService.execute(req, connection);      
    }
    
    service.remove = function(connection, entityId, existingLayers) {
      var req = {
          method: 'POST',
          url: com.runwaysdk.__applicationContextPath + '/location/remove',
          data : {
            entityId : entityId,
            existingLayers : existingLayers            
          }
      }      
      
      runwayService.execute(req, connection);      
    }
    
    return service;  
  }
  
  angular.module("location-service", ["runway-service"]);
  angular.module("location-service")
    .factory('locationService', LocationService)
})();
