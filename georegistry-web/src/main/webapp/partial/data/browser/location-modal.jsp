<%--

    Copyright (c) 2015 TerraFrame, Inc. All rights reserved.

    This file is part of Runway SDK(tm).

    Runway SDK(tm) is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Runway SDK(tm) is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/tlds/geoprism.tld" prefix="gdb"%>

<div>
  <div ng-if="show">
    <div class="modal-backdrop fade in"></div>
    <div id="modal-div" style="display: block;" class="modal fade in" role="dialog" aria-hidden="false" data-backdrop="static" data-keyboard="false">
    <dl>      
      <form class="modal-form" name="ctrl.form">    
        <div class="modal-dialog">
          <div class="modal-content" show-on-ready>
            <div class="heading">
              <h1 ng-show="!entity.oid"><gdb:localize key="location.management.newTooltip"/></h1>
              <h1 ng-show="entity.oid"><gdb:localize key="location.management.editTooltip"/></h1>
            </div>
            <fieldset>
              <div class="row-holder" ng-show="errors.length > 0 && show">
                <div class="label-holder">
                </div>
                <div class="holder">
                  <div class="alert alertbox" ng-repeat="error in errors track by $index">
                    <p class="error-message">{{error}}</p>
                  </div>
                </div>
              </div>
              <div class="row-holder">
                <div class="label-holder">
                  <label><gdb:localize key="location.management.label"/></label>
                </div>    
                <div class="holder">
                  <span class="text">
                    <input type="text" ng-model="entity.displayLabel" name="label" required="required" placeholder="<gdb:localize key="location.management.labelPlaceholder"/>">
                  </span>
                </div>
              </div>
              <div class="row-holder">
                <div class="label-holder">
                  <label><gdb:localize key="location.management.geoId"/></label>
                </div>    
                <div class="holder">
                  <span class="text">
                    <input type="text" ng-model="entity.geoId" name="geoId" placeholder="<gdb:localize key="location.management.geoIdPlaceholder"/>" required="required">
                  </span>
                </div>
              </div>
              <div class="row-holder">
                <div class="label-holder">
                  <label><gdb:localize key="location.management.status"/></label>
                </div>    
                <div class="holder">
                  <div class="select-box">
                    <select class="method-select" ng-model="entity.geoObject.properties.status.code" required="required">
                      <option value="CGR:Status-New">New</option> <!-- TODO : Localization -->
                      <option ng-show="entity.oid" value="CGR:Status-Active">Active</option>
                      <option ng-show="entity.oid" value="CGR:Status-Pending">Pending</option>
                      <option ng-show="entity.oid" value="CGR:Status-Inactive">Inactive</option>
                    </select>
                  </div>
                </div>
              </div>
              <div class="row-holder">
                <div class="label-holder">
                  <label><gdb:localize key="location.management.universal"/></label>
                </div>    
                <div class="holder">
                  <label ng-show="entity.oid" style="margin-top:12px;">{{entity.geoObject.properties.type}}</label>
                  <div ng-show="!entity.oid" class="select-box">
                    <select class="method-select" ng-model="entity.universal" ng-options="opt.oid as opt.displayLabel for opt in universals" required="required">
                      <option value=""></option>
                    </select>
                  </div>
                </div>
              </div>
              <div class="row-holder" fire-on-ready>
                <div class="label-holder">
                </div>  
                <div class="holder">
                  <div class="button-holder">
                    <input type="button" value="<gdb:localize key="dataset.cancel"/>" class="btn btn-default" ng-click="ctrl.cancel()" />              
                    <input type="button" value="<gdb:localize key="dataset.submit"/>" class="btn btn-primary" ng-click="ctrl.apply()" ng-disabled="ctrl.form.$invalid" />
                  </div>
                </div>
              </div>
            </fieldset>
          </div>
        </div>
      </form> 
    </dl>   
    </div>
  </div>
</div>
