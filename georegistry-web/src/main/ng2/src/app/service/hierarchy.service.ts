///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <ehttp://www.gnu.org/licenses/>.
///

import { Injectable } from '@angular/core';
import { Headers, Http, Response, URLSearchParams } from '@angular/http';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { Observable } from 'rxjs/Observable';

import { TreeEntity, HierarchyType, Hierarchy, HierarchyNode, GeoObject, GeoObjectType, Attribute, Term } from '../data/hierarchy/hierarchy';
import { EventService } from '../event/event.service';

declare var acp: any;


//import { EventService, BasicService } from '../core/service/core.service';
//import { EventHttpService } from '../../core/service/event-http.service';

import { TreeNode } from 'angular-tree-component';

declare var acp: any;

//@Injectable()
//export class HierarchyService extends BasicService {
//  
//  constructor(service: EventService, private ehttp: EventHttpService, private http: Http) {
//    super(service); 
//  }
//  
//  
//}

@Injectable()
export class HierarchyService {

    baseUrl: string = acp + '/cgr/hierarchies';

    constructor( private http: Http, private eventService: EventService ) { }

    getHierarchyTypes( types: any ): Promise<HierarchyType[]> {
        let params: URLSearchParams = new URLSearchParams();
        params.set( 'types', JSON.stringify(types) );
        
        return this.http
            .get( acp + '/cgr/hierarchytype/get-all', {params: params})
            .toPromise()
            .then( response => {
                return response.json() as HierarchyType[];
            } )
    }
    
    getGeoObjectTypes( types: any ): Promise<GeoObjectType[]> {
        let params: URLSearchParams = new URLSearchParams();
    
        if(types.length < 1){
          params.set( 'types', JSON.stringify(types) );
        }
        
        return this.http
            .get( acp + '/cgr/geoobjecttype/get-all' )
            .toPromise()
            .then( response => {
                return response.json() as GeoObjectType[];
            } )
    }
    
    getChildGeoObjects( parentUid: string, childrenTypes: any, recursive: boolean ): Promise<HierarchyNode> {
        let params: URLSearchParams = new URLSearchParams();
    
          params.set( 'parentUid', parentUid )
          params.set( 'childrenTypes', JSON.stringify(childrenTypes) )
          params.set( 'recursive', JSON.stringify(recursive) );
        
        return this.http
            .get( acp + '/cgr/geoobject/getchildren', {params: params} )
            .toPromise()
            .then( response => {
                return response.json() as HierarchyNode;
            } )
    }

    addChildToHierarchy( hierarchyCode: string, parentGeoObjectTypeCode: string, childGeoObjectTypeCode: string ): Promise<HierarchyType> {

	   let headers = new Headers( {
           'Content-Type': 'application/json'
       } );

       this.eventService.start();

       return this.http
           .post( acp + '/cgr/hierarchytype/add', JSON.stringify( { hierarchyCode : hierarchyCode, parentGeoObjectTypeCode : parentGeoObjectTypeCode, childGeoObjectTypeCode : childGeoObjectTypeCode } ), { headers: headers } )
           .finally(() => {
               this.eventService.complete();
           } )
           .toPromise()
           .then( response => {
               return response.json() as HierarchyType;
           } )
    }
    
    removeFromHierarchy( hierarchyCode: string, parentGeoObjectTypeCode: string, childGeoObjectTypeCode: string ): Promise<HierarchyType> {

 	   let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/cgr/hierarchytype/remove', JSON.stringify( { hierarchyCode : hierarchyCode, parentGeoObjectTypeCode : parentGeoObjectTypeCode, childGeoObjectTypeCode : childGeoObjectTypeCode } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as HierarchyType;
            } )
     }
    
    createHierarchyType( htJSON: string): Promise<HierarchyType> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        });
        
        this.eventService.start();

        return this.http
            .post( acp + '/cgr/hierarchytype/create', JSON.stringify({ 'htJSON': htJSON }), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as HierarchyType;
            } )
    }
    
    createGeoObjectType( gtJSON: string): Promise<GeoObjectType> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        });
        
        this.eventService.start();

        return this.http
            .post( acp + '/cgr/geoobjecttype/create', JSON.stringify({ 'gtJSON': gtJSON }), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as GeoObjectType;
            } )
    }

    deleteHierarchyType( code: string ): Promise<TreeEntity> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );
        
        this.eventService.start();

        return this.http
            .post( acp + '/cgr/hierarchytype/delete', { 'code': code }, { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as TreeEntity;
            } )
    }
    
    deleteGeoObjectType( code: string ): Promise<Response> {
    	let headers = new Headers( {
    		'Content-Type': 'application/json'
    	} );

    	this.eventService.start();

    	return this.http
    	.post( acp + '/cgr/geoobjecttype/delete', JSON.stringify( { code: code } ), { headers: headers } )
    	.finally(() => {
    		this.eventService.complete();
    	} )
    	.toPromise()
      }

    addAttributeType(geoObjTypeId: string, attribute: Attribute): Promise<Attribute> {

	   let headers = new Headers( {
           'Content-Type': 'application/json'
       } );

       this.eventService.start();


       return this.http
           .post( acp + '/cgr/geoobjecttype/addattribute', JSON.stringify( { 'geoObjTypeId': geoObjTypeId, 'attributeType' : attribute } ), { headers: headers } )
           .finally(() => {
               this.eventService.complete();
           } )
           .toPromise()
           .then( response => {
               return response.json() as Attribute;
           } )
    }

    deleteAttributeType(geoObjTypeId: string, attribute: Attribute): Promise<boolean> {

	   let headers = new Headers( {
           'Content-Type': 'application/json'
       } );

       this.eventService.start();


       return this.http
           .post( acp + '/cgr/geoobjecttype/deleteattribute', JSON.stringify( { 'geoObjTypeId': geoObjTypeId, 'attributeType' : attribute } ), { headers: headers } )
           .finally(() => {
               this.eventService.complete();
           } )
           .toPromise()
           .then( response => {
               return response.json();
           } )
    }

    getTerms(): Promise<Term[]> {
        
        return this.http
            .get( acp + '/cgr/terms/get-all' )
            .toPromise()
            .then( response => {
                return response.json() as Term[];
            } )
    }

    search(terms: Observable<string>) {
	  return terms.debounceTime(400)
	    .distinctUntilChanged()
	    .switchMap(term => this.searchEntries(term));
    }
    
    searchEntries(term:string) {

      let params: URLSearchParams = new URLSearchParams();
      params.set( 'term', term );

	  return this.http
	      .get(this.baseUrl, { search: params } )
	      .map(res => res.json());
	}

}
