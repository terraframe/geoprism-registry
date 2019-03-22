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
			        <h1 ng-show="!entity.oid"><gdb:localize key="location.management.newTooltip"></gdb:localize></h1>
			        <h1 ng-show="entity.oid"><gdb:localize key="location.management.editTooltip"></gdb:localize></h1>
			      </div>
			      <div ng-if="parentTreeNode" class="row-holder">
			        <div class="label-holder"></div>
			        <div class="holder">
			          <div class="btn-group geobject-editor-tab-holder">
			            <label class="btn full-width-radio-button" ng-click="ctrl.setTabIndex(0);">
			              <gdb:localize key="geoobject.editor.tabZero"></gdb:localize>
			            </label>
			            <label class="btn full-width-radio-button" ng-click="ctrl.setTabIndex(1);">
			              <gdb:localize key="geoobject.editor.tabOne"></gdb:localize>
			            </label>
			          </div>
			        </div>
			      </div>
			      <div name="geoobject-shared-attribute-editor" ng-if="tabIndex === 0">
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
	              <div class="holder">
	                <span class="text"></span>
		                <div class="panel" style="box-shadow: none;">
		                  <div class="panel-body">
		                      <ul class="list-group">
		                          <li class="list-group-item" style="text-align:left;" ng-repeat="attr in geoObjectType.attributes">
		                              <h5>{{attr.label.localizedValue}}</h5>
		
		
		                              <div ng-if="attr.type === 'character'">
		                                  <input ng-if="postGeoObject" type="text"
		                                      ng-model="postGeoObject.properties[attr.code]" id="mod-{{attr.code}}" name="mod-{{attr.code}}"
		                                      disabled="attr.code === 'code' && !allowCodeEdit"> 
		
		                                      <p class="warning-text" ng-if="this.preGeoObject.properties[attr.code] && this.postGeoObject.properties[attr.code] !== this.preGeoObject.properties[attr.code]">
		                                          <localize key="change.request.changed.value.prefix"></localize> {{this.preGeoObject.properties[attr.code]}}
		                                      </p>
		                              </div>
		                              
		                              <div ng-if="attr.type === 'local'">
		                                <ul class="list-group">
		                                  <li class="list-group-item" ng-repeat="localeValue in postGeoObject.properties[attr.code].localeValues track by $index">
		                                    <h5>{{localeValue.locale}}</h5>
		                                    <input ng-if="postGeoObject" type="text" ng-model="localeValue.value" name="mod-{{attr.code}}-{{localeValue.locale}}"> 
		
		                                    <p class="warning-text" ng-if="localeValue.value !== preGeoObject.properties[attr.code].localeValues[i].value">
		                                      <localize key="change.request.changed.value.prefix"></localize> {{preGeoObject.properties[attr.code].localeValues[i].value}}
		                                    </p>
		                                  </li>
		                                </ul>
		                              </div>
		
		                              <div ng-if="attr.type === 'date'">
		                                  <input type="date" ng-model="postGeoObject.properties[attr.code] | date:'yyyy-MM-dd'"
		                                      ngModelChange="postGeoObject.properties[attr.code] = $event" id="mod-{{attr.code}}" name="mod-{{attr.code}}"> 
		                                      
		                                  <p class="warning-text" ng-if="preGeoObject.properties[attr.code] && postGeoObject.properties[attr.code] !== preGeoObject.properties[attr.code]">
		                                          <localize key="change.request.changed.value.prefix"></localize> {{preGeoObject.properties[attr.code]}}
		                                      </p>
		                              </div>
		
		                              <div ng-if="attr.type === 'boolean'">
		                                  <label>
		                                      <input type="radio" [checked]="postGeoObject.properties[attr.code] === true"
		                                          ng-model="postGeoObject.properties[attr.code]"
		                                          value="true" id="mod-{{attr.code}}" name="mod-{{attr.code}}">
		                                      <localize key="change.request.boolean.option.true"></localize>
		                                  </label>
		
		                                  <label>
		                                      <input type="radio" checked="{{postGeoObject.properties[attr.code] === false}}"
		                                          ng-model="postGeoObject.properties[attr.code]"
		                                          value="false" id="mod-{{attr.code}}" name="mod-{{attr.code}}">
		                                      <localize key="change.request.boolean.option.false"></localize>
		                                  </label>
		
		                                  <p class="warning-text" ng-if="preGeoObject.properties[attr.code] && postGeoObject.properties[attr.code] !== preGeoObject.properties[attr.code]">
		                                    <localize key="change.request.changed.value.prefix"></localize> {{preGeoObject.properties[attr.code]}}
		                                  </p>
		                              </div>
		
		                              <div ng-if="attr.type === 'float'">
		                                  <input type="number" ng-model="postGeoObject.properties[attr.code]"
		                                      id="mod-{{attr.code}}" name="mod-{{attr.code}}">
		
		                                  <p class="warning-text" ng-if="preGeoObject.properties[attr.code] && postGeoObject.properties[attr.code] !== preGeoObject.properties[attr.code]">
		                                          <localize key="change.request.changed.value.prefix"></localize> {{preGeoObject.properties[attr.code]}}
		                                      </p>
		                              </div>
		
		                              <div ng-if="attr.type === 'integer'">
		                                  <input type="number" ng-model="postGeoObject.properties[attr.code]"
		                                      id="mod-{{attr.code}}" name="mod-{{attr.code}}">
		
		                                  <p class="warning-text" ng-if="preGeoObject.properties[attr.code] && postGeoObject.properties[attr.code] !== preGeoObject.properties[attr.code]">
		                                    <localize key="change.request.changed.value.prefix"></localize> {{preGeoObject.properties[attr.code]}}
		                                  </p>
		                              </div>
		
		                              <div ng-if="attr.type === 'term'">
		                                  <select id="mod-{{attr.code}}" name="modifiedTermOptionsSelect" class="select-area" style="float:none;"
		                                      change="onSelectPropertyOption($event)" ng-model="postGeoObject.properties[attr.code][0]">
		                                      <option ng-repeat="option in ctrl.getGeoObjectTypeTermAttributeOptions(attr.code)" value="{{option.code}}" >{{option.label.localizedValue}}</option>
		                                  </select>
		
		                                  <!-- <p class="warning-text" ng-if="preGeoObject.properties[attr.code] && postGeoObject.properties[attr.code] !== preGeoObject.properties[attr.code]">
		                                          <localize key="change.request.changed.value.prefix"></localize> {{preGeoObject.properties[attr.code]}}
		                                      </p> -->
		                              </div>
		                          </li>
		                      </ul>
		                  </div>
		                </div>
		              </div>
	            </fieldset>
	          </div>
	          <div name="parents-tab" ng-if="tabIndex === 1 && parentTreeNode">
			        <div ng-repeat="ptn in parentTreeNode.parents">
			          <div class="label-holder">
			            <label>{{ptn.hierarchyType}}</label>
			          </div>
			  
			          <div class="holder">
			             <input type="text" ng-model="ptn.geoObject.properties.displayLabel.localizedValue" autocomplete="off" callback-auto-complete=""
			             source="ctrl.getParentSearchFunction(ptn)" setter="ctrl.getParentSearchOpenFunction(ptn)" class="ng-isolate-scope ui-autocomplete-input ui-autocomplete-loading">
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
          </div>
        </div>
      </form>
    </dl>
    </div>
  </div>
</div>
