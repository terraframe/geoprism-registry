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
import { Observable } from 'rxjs';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { GeoObject, GeoObjectType, Attribute, Term, MasterList, ParentTreeNode, ChildTreeNode } from '../model/registry';
import { Progress } from '../progress-bar/progress';
import { HierarchyNode, HierarchyType } from '../data/hierarchy/hierarchy';
import { EventService } from '../event/event.service';

declare var acp: any;


@Injectable()
export class RegistryService {

    constructor( private http: Http, private eventService: EventService ) { }

    init(): Promise<{ types: GeoObjectType[], hierarchies: HierarchyType[], locales: string[] }> {
        return this.http.get( acp + '/cgr/init' )
            .toPromise()
            .then( response => {
                return response.json() as { types: GeoObjectType[], hierarchies: HierarchyType[], locales: string[] };
            } )
    }

    getGeoObjectTypes( types: any ): Promise<GeoObjectType[]> {
        let params: URLSearchParams = new URLSearchParams();

        params.set( 'types', JSON.stringify( types ) );

        return this.http
            .get( acp + '/cgr/geoobjecttype/get-all', { params: params } )
            .toPromise()
            .then( response => {
                return response.json() as GeoObjectType[];
            } )
    }

    getParentGeoObjects( childId: string, childTypeCode: string, parentTypes: any, recursive: boolean ): Promise<ParentTreeNode> {
        let params: URLSearchParams = new URLSearchParams();

        params.set( 'childId', childId )
        params.set( 'childTypeCode', childTypeCode )
        params.set( 'parentTypes', JSON.stringify( parentTypes ) )
        params.set( 'recursive', JSON.stringify( recursive ) );

        return this.http
            .get( acp + '/cgr/geoobject/get-parent-geoobjects', { params: params } )
            .toPromise()
            .then( response => {
                return response.json() as ParentTreeNode;
            } )
    }

    getChildGeoObjects( parentId: string, parentTypeCode: string, childrenTypes: any, recursive: boolean ): Promise<ChildTreeNode> {
        let params: URLSearchParams = new URLSearchParams();

        params.set( 'parentId', parentId )
        params.set( 'parentTypeCode', parentTypeCode )
        params.set( 'childrenTypes', JSON.stringify( childrenTypes ) )
        params.set( 'recursive', JSON.stringify( recursive ) );

        return this.http
            .get( acp + '/cgr/geoobject/getchildren', { params: params } )
            .toPromise()
            .then( response => {
                return response.json() as ChildTreeNode;
            } )
    }

    createGeoObjectType( gtJSON: string ): Promise<GeoObjectType> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/cgr/geoobjecttype/create', JSON.stringify( { 'gtJSON': gtJSON } ), { headers: headers } )
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

    addAttributeType( geoObjTypeId: string, attribute: Attribute ): Promise<Attribute> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/cgr/geoobjecttype/addattribute', JSON.stringify( { 'geoObjTypeId': geoObjTypeId, 'attributeType': attribute } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as Attribute;
            } )
    }

    updateAttributeType( geoObjTypeId: string, attribute: Attribute ): Promise<Attribute> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();


        return this.http
            .post( acp + '/cgr/geoobjecttype/updateattribute', JSON.stringify( { 'geoObjTypeId': geoObjTypeId, 'attributeType': attribute } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as Attribute;
            } )
    }

    deleteAttributeType( geoObjTypeId: string, attributeName: string ): Promise<boolean> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();


        return this.http
            .post( acp + '/cgr/geoobjecttype/deleteattribute', JSON.stringify( { 'geoObjTypeId': geoObjTypeId, 'attributeName': attributeName } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json();
            } )
    }

    addAttributeTermTypeOption( parentTermCode: string, term: Term ): Promise<Term> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();


        return this.http
            .post( acp + '/cgr/geoobjecttype/addterm', JSON.stringify( { 'parentTermCode': parentTermCode, 'termJSON': term } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as Term;
            } )
    }

    updateAttributeTermTypeOption( termJSON: Term ): Promise<Term> {

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

    deleteAttributeTermTypeOption( termCode: string ): Promise<Attribute> {

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
            .get( acp + '/cgr/geoobject/get', { params: params } )
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
            .get( acp + '/cgr/geoobject/get-code', { params: params } )
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
            .get( acp + '/cgr/geoobject/suggestions', { params: params } )
            .toPromise()
            .then( response => {
                return response.json() as GeoObject;
            } )
    }

    getGeoObjectSuggestionsTypeAhead( text: string, type: string ): Promise<GeoObject> {
        let params: URLSearchParams = new URLSearchParams();

        params.set( 'text', text );
        params.set( 'type', type );

        return this.http
            .get( acp + '/cgr/geoobject/suggestions', { params: params } )
            .toPromise()
            .then( response => {
                return response.json() as GeoObject;
            } )
    }

    getMasterLists(): Promise<{ locales: string[], lists: { label: string, oid: string, createDate: string, lastUpdateDate: string }[] }> {
        let params: URLSearchParams = new URLSearchParams();

        return this.http
            .get( acp + '/master-list/list-all', { params: params } )
            .toPromise()
            .then( response => {
                return response.json() as { locales: string[], lists: { label: string, oid: string, createDate: string, lastUpdateDate: string }[] };
            } )
    }

    createMasterList( list: MasterList ): Promise<MasterList> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/master-list/create', JSON.stringify( { list: list } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as MasterList;
            } )
    }

    deleteMasterList( oid: string ): Promise<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/master-list/remove', JSON.stringify( { oid: oid } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
    }

    publishMasterList( oid: string ): Observable<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        return this.http.post( acp + '/master-list/publish', JSON.stringify( { oid: oid } ), { headers: headers } );
    }

    getMasterList( oid: string ): Promise<MasterList> {
        let params: URLSearchParams = new URLSearchParams();
        params.set( 'oid', oid );

        return this.http
            .get( acp + '/master-list/get', { params: params } )
            .toPromise()
            .then( response => {
                return response.json() as MasterList;
            } )
    }
    
    /*
     * Not really part of the RegistryService
     */
    applyGeoObjectEdit( parentTreeNode: ParentTreeNode, geoObject: GeoObject, masterListId: string ): Promise<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/geoobject-editor/apply', JSON.stringify( { parentTreeNode: parentTreeNode, geoObject: geoObject, masterListId: masterListId } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response;
            } )
    }

    data( oid: string, pageNumber: number, pageSize: number, filter: string ): Promise<any> {
        let params: URLSearchParams = new URLSearchParams();
        params.set( 'oid', oid );

        if ( pageNumber != null ) {
            params.set( 'pageNumber', pageNumber.toString() );
        }

        if ( pageSize != null ) {
            params.set( 'pageSize', pageSize.toString() );
        }

        if ( filter != null ) {
            params.set( 'filter', filter );
        }

        return this.http
            .get( acp + '/master-list/data', { params: params } )
            .toPromise()
            .then( response => {
                return response.json() as any;
            } )
    }

    progress( oid: string ): Promise<Progress> {
        let params: URLSearchParams = new URLSearchParams();
        params.set( 'oid', oid );

        return this.http
            .get( acp + '/master-list/progress', { params: params } )
            .toPromise()
            .then( response => {
                return response.json() as Progress;
            } )
    }


}
