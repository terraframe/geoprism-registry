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

import { GeoObject, GeoObjectType, Attribute, Term } from '../model/registry';
import { HierarchyNode } from '../data/hierarchy/hierarchy';
import { EventService } from '../event/event.service';

declare var acp: any;


//import { EventService, BasicService } from '../core/service/core.service';
//import { EventHttpService } from '../../core/service/event-http.service';

declare var acp: any;


@Injectable()
export class RegistryService {

    baseUrl: string = acp + '/cgr/manage';

    constructor( private http: Http, private eventService: EventService ) { }

    getGeoObjectTypes( types: any ): Promise<GeoObjectType[]> {
        let params: URLSearchParams = new URLSearchParams();
    
        if(types.length < 1){
          params.set( 'types', JSON.stringify(types) );
        }
        
        return this.http
            .get( acp + '/cgr/geoobjecttype/get-all', {params: params} )
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

     updateGeoObjectType( gtJSON: GeoObjectType ): Promise<GeoObjectType> {
    	let headers = new Headers( {
    		'Content-Type': 'application/json'
    	} );

    	this.eventService.start();

    	return this.http
    	.post( acp + '/cgr/geoobjecttype/update', JSON.stringify( { "gtJSON": gtJSON } ), { headers: headers } )
    	.finally(() => {
    		this.eventService.complete();
    	} )
    	.toPromise()
            .then( response => {
                return response.json() as GeoObjectType;
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

    updateAttributeType(geoObjTypeId: string, attribute: Attribute): Promise<Attribute> {

	   let headers = new Headers( {
           'Content-Type': 'application/json'
       } );

       this.eventService.start();


       return this.http
           .post( acp + '/cgr/geoobjecttype/updateattribute', JSON.stringify( { 'geoObjTypeId': geoObjTypeId, 'attributeType' : attribute } ), { headers: headers } )
           .finally(() => {
               this.eventService.complete();
           } )
           .toPromise()
           .then( response => {
               return response.json() as Attribute;
           } )
    }

    deleteAttributeType(geoObjTypeId: string, attributeName: string): Promise<boolean> {

	   let headers = new Headers( {
           'Content-Type': 'application/json'
       } );

       this.eventService.start();


       return this.http
           .post( acp + '/cgr/geoobjecttype/deleteattribute', JSON.stringify( { 'geoObjTypeId': geoObjTypeId, 'attributeName' : attributeName } ), { headers: headers } )
           .finally(() => {
               this.eventService.complete();
           } )
           .toPromise()
           .then( response => {
               return response.json();
           } )
    }

    addAttributeTermTypeOption(parentTermCode: string, term: Term): Promise<Term> {

	   let headers = new Headers( {
           'Content-Type': 'application/json'
       } );

       this.eventService.start();


       return this.http
           .post( acp + '/cgr/geoobjecttype/addterm', JSON.stringify( { 'parentTermCode': parentTermCode, 'termJSON' : term } ), { headers: headers } )
           .finally(() => {
               this.eventService.complete();
           } )
           .toPromise()
           .then( response => {
               return response.json() as Term;
           } )
    }

    updateAttributeTermTypeOption(termJSON: Term): Promise<Term> {

	   let headers = new Headers( {
           'Content-Type': 'application/json'
       } );

       this.eventService.start();


       return this.http
           .post( acp + '/cgr/geoobjecttype/updateterm', JSON.stringify( { 'termJSON': termJSON } ), { headers: headers } )
           .finally(() => {
               this.eventService.complete();
           } )
           .toPromise()
           .then( response => {
               return response.json() as Term;
           } )
    }

    deleteAttributeTermTypeOption(termCode: string): Promise<Attribute> {

	   let headers = new Headers( {
           'Content-Type': 'application/json'
       } );

       this.eventService.start();


       return this.http
           .post( acp + '/cgr/geoobjecttype/deleteterm', JSON.stringify( { 'termCode': termCode } ), { headers: headers } )
           .finally(() => {
               this.eventService.complete();
           } )
           .toPromise()
           .then( response => {
               return response.json();
           } )
    }

    getGeoObject( id: string, typeCode: string ): Promise<GeoObject> {
        let params: URLSearchParams = new URLSearchParams();
    
          params.set( 'id', id )
          params.set( 'typeCode', typeCode );
        
        return this.http
            .get( acp + '/cgr/geoobject/get', {params: params} )
            .toPromise()
            .then( response => {
                return response.json() as GeoObject;
            } )
    }

    getGeoObjectByCode( code: string, typeCode: string ): Promise<GeoObject> {
        let params: URLSearchParams = new URLSearchParams();
    
          params.set( 'code', code )
          params.set( 'typeCode', typeCode );
        
        return this.http
            .get( acp + '/cgr/geoobject/get-code', {params: params} )
            .toPromise()
            .then( response => {
                return response.json() as GeoObject;
            } )
    }

    getGeoObjectSuggestions( text: string, type: string, parent: string, hierarchy: string ): Promise<GeoObject> {
        let params: URLSearchParams = new URLSearchParams();
    
          params.set( 'text', text )
          params.set( 'type', type );
          params.set( 'parent', parent );
          params.set( 'hierarchy', hierarchy );
        
        return this.http
            .get( acp + '/cgr/geoobject/suggestions', {params: params} )
            .toPromise()
            .then( response => {
                return response.json() as GeoObject;
            } )
    }
}
