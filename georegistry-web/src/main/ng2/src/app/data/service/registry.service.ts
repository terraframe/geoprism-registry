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
import { HierarchyNode, HierarchyType } from '../model/hierarchy';
import { Progress } from '../../shared/model/progress';
import { EventService } from '../../shared/service/event.service';

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

    newGeoObjectInstance( typeCode: string ): Promise<any> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/cgr/geoobject/newGeoObjectInstance', JSON.stringify( { 'typeCode': typeCode } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as any;
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

    getGeoObjectBounds( code: string, typeCode: string ): Promise<number[]> {
        let params: URLSearchParams = new URLSearchParams();

        params.set( 'code', code )
        params.set( 'typeCode', typeCode );

        return this.http
            .get( acp + '/cgr/geoobject/get-bounds', { params: params } )
            .toPromise()
            .then( response => {
                return response.json() as number[];
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

    getHierarchiesForGeoObject( code: string, typeCode: string ): Promise<any> {
        let params: URLSearchParams = new URLSearchParams();
        params.set( 'code', code );
        params.set( 'typeCode', typeCode );

        this.eventService.start();

        return this.http
            .get( acp + '/cgr/geoobject/get-hierarchies', { params: params } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as any;
            } )
    }

    getGeoObjectSuggestions( text: string, type: string, parent: string, hierarchy: string ): Promise<GeoObject> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        let params = {
            text: text,
            type: type,
        } as any;

        if ( parent != null && hierarchy != null ) {
            params.parent = parent;
            params.hierarchy = hierarchy;
        }

        return this.http
            .post( acp + '/cgr/geoobject/suggestions', JSON.stringify( params ), { headers: headers } )
            .toPromise()
            .then( response => {
                return response.json();
            } );

    }

    getGeoObjectSuggestionsTypeAhead( text: string, type: string ): Promise<GeoObject> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        let params = {
            text: text,
            type: type,
        } as any;

        return this.http
            .post( acp + '/cgr/geoobject/suggestions', JSON.stringify( params ), { headers: headers } )
            .toPromise()
            .then( response => {
                return response.json();
            } );
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
    applyGeoObjectEdit( parentTreeNode: ParentTreeNode, geoObject: GeoObject, isNew: boolean, masterListId: string ): Promise<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/geoobject-editor/apply', JSON.stringify( { parentTreeNode: parentTreeNode, geoObject: geoObject, isNew: isNew, masterListId: masterListId } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response;
            } )
    }

    data( oid: string, pageNumber: number, pageSize: number, filter: { attribute: string, value: string }[], sort: { attribute: string, order: string } ): Promise<any> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        let params = {
            oid: oid,
            sort: sort
        } as any;

        if ( pageNumber != null ) {
            params.pageNumber = pageNumber;
        }

        if ( pageSize != null ) {
            params.pageSize = pageSize;
        }

        if ( filter.length > 0 ) {
            params.filter = filter;
        }

        return this.http
            .post( acp + '/master-list/data', JSON.stringify( params ), { headers: headers } )
            .toPromise()
            .then( response => {
                return response.json() as any;
            } )
    }

    values( oid: string, value: string, attributeName: string, valueAttribute: string, filter: { attribute: string, value: string }[] ): Promise<{ label: string, value: string }[]> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        let params = {
            oid: oid,
            attributeName: attributeName,
            valueAttribute: valueAttribute
        } as any;

        if ( filter.length > 0 ) {
            params.filter = filter;
        }

        if ( value != null && value.length > 0 ) {
            params.value = value;
        }


        return this.http
            .post( acp + '/master-list/values', JSON.stringify( params ), { headers: headers } )
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
