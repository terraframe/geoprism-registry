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
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { HierarchyType } from '../data/hierarchy/hierarchy';
import { TreeEntity } from '../model/registry';
import { EventService } from '../event/event.service';

declare var acp: any;

@Injectable()
export class HierarchyService {

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

}
