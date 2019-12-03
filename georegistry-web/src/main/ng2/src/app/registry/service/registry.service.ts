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
import { HttpHeaders, HttpClient, HttpResponse, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { GeoObject, GeoObjectType, Attribute, Term, MasterList, MasterListVersion, ParentTreeNode, ChildTreeNode, ValueOverTime } from '../model/registry';
import { HierarchyNode, HierarchyType } from '../model/hierarchy';
import { Progress } from '../../shared/model/progress';
import { EventService } from '../../shared/service/event.service';
import { templateJitUrl } from '@angular/compiler';

declare var acp: any;


@Injectable()
export class RegistryService {

    constructor( private http: HttpClient, private eventService: EventService ) { }

    init(): Promise<{ types: GeoObjectType[], hierarchies: HierarchyType[], locales: string[] }> {
        return this.http.get<{ types: GeoObjectType[], hierarchies: HierarchyType[], locales: string[] }>( acp + '/cgr/init' )
            .toPromise();
    }

    // param types: array of GeoObjectType codes. If empty array then all GeoObjectType objects are returned.
    getGeoObjectTypes( types: any ): Promise<GeoObjectType[]> {
        let params: HttpParams = new HttpParams();

        params = params.set( 'types', JSON.stringify( types ) );

        return this.http
            .get<GeoObjectType[]>( acp + '/cgr/geoobjecttype/get-all', { params: params } )
            .toPromise();
    }

    getParentGeoObjects( childId: string, childTypeCode: string, parentTypes: any, recursive: boolean ): Promise<ParentTreeNode> {
        let params: HttpParams = new HttpParams();

        params = params.set( 'childId', childId )
        params = params.set( 'childTypeCode', childTypeCode )
        params = params.set( 'parentTypes', JSON.stringify( parentTypes ) )
        params = params.set( 'recursive', JSON.stringify( recursive ) );

        return this.http
            .get<ParentTreeNode>( acp + '/cgr/geoobject/get-parent-geoobjects', { params: params } )
            .toPromise()
    }

    getChildGeoObjects( parentId: string, parentTypeCode: string, childrenTypes: any, recursive: boolean ): Promise<ChildTreeNode> {
        let params: HttpParams = new HttpParams();

        params = params.set( 'parentId', parentId )
        params = params.set( 'parentTypeCode', parentTypeCode )
        params = params.set( 'childrenTypes', JSON.stringify( childrenTypes ) )
        params = params.set( 'recursive', JSON.stringify( recursive ) );

        return this.http
            .get<ChildTreeNode>( acp + '/cgr/geoobject/getchildren', { params: params } )
            .toPromise();
    }

    newGeoObjectInstance( typeCode: string ): Promise<any> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<any>( acp + '/cgr/geoobject/newGeoObjectInstance', JSON.stringify( { 'typeCode': typeCode } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    createGeoObjectType( gtJSON: string ): Promise<GeoObjectType> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<GeoObjectType>( acp + '/cgr/geoobjecttype/create', JSON.stringify( { 'gtJSON': gtJSON } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    updateGeoObjectType( gtJSON: GeoObjectType ): Promise<GeoObjectType> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<GeoObjectType>( acp + '/cgr/geoobjecttype/update', JSON.stringify( { "gtJSON": gtJSON } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    deleteGeoObjectType( code: string ): Promise<void> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( acp + '/cgr/geoobjecttype/delete', JSON.stringify( { code: code } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
    }

    addAttributeType( geoObjTypeId: string, attribute: Attribute ): Promise<Attribute> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<Attribute>( acp + '/cgr/geoobjecttype/addattribute', JSON.stringify( { 'geoObjTypeId': geoObjTypeId, 'attributeType': attribute } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    updateAttributeType( geoObjTypeId: string, attribute: Attribute ): Promise<Attribute> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();


        return this.http
            .post<Attribute>( acp + '/cgr/geoobjecttype/updateattribute', JSON.stringify( { 'geoObjTypeId': geoObjTypeId, 'attributeType': attribute } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    deleteAttributeType( geoObjTypeId: string, attributeName: string ): Promise<boolean> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();


        return this.http
            .post<boolean>( acp + '/cgr/geoobjecttype/deleteattribute', JSON.stringify( { 'geoObjTypeId': geoObjTypeId, 'attributeName': attributeName } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    addAttributeTermTypeOption( parentTermCode: string, term: Term ): Promise<Term> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();


        return this.http
            .post<Term>( acp + '/cgr/geoobjecttype/addterm', JSON.stringify( { 'parentTermCode': parentTermCode, 'termJSON': term } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    updateAttributeTermTypeOption( termJSON: Term ): Promise<Term> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();


        return this.http
            .post<Term>( acp + '/cgr/geoobjecttype/updateterm', JSON.stringify( { 'termJSON': termJSON } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    deleteAttributeTermTypeOption( termCode: string ): Promise<Attribute> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();


        return this.http
            .post<Attribute>( acp + '/cgr/geoobjecttype/deleteterm', JSON.stringify( { 'termCode': termCode } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    getGeoObject( id: string, typeCode: string ): Promise<GeoObject> {
        let params: HttpParams = new HttpParams();

        params = params.set( 'id', id )
        params = params.set( 'typeCode', typeCode );

        return this.http
            .get<GeoObject>( acp + '/cgr/geoobject/get', { params: params } )
            .toPromise();
    }

    getGeoObjectBounds( code: string, typeCode: string ): Promise<number[]> {
        let params: HttpParams = new HttpParams();

        params = params.set( 'code', code )
        params = params.set( 'typeCode', typeCode );

        return this.http
            .get<number[]>( acp + '/cgr/geoobject/get-bounds', { params: params } )
            .toPromise();
    }

    getGeoObjectByCode( code: string, typeCode: string ): Promise<GeoObject> {
        let params: HttpParams = new HttpParams();

        params = params.set( 'code', code )
        params = params.set( 'typeCode', typeCode );

        return this.http
            .get<GeoObject>( acp + '/cgr/geoobject/get-code', { params: params } )
            .toPromise();
    }

    getHierarchiesForGeoObject( code: string, typeCode: string ): Promise<any> {
        let params: HttpParams = new HttpParams();
        params = params.set( 'code', code );
        params = params.set( 'typeCode', typeCode );

        this.eventService.start();

        return this.http
            .get<any>( acp + '/cgr/geoobject/get-hierarchies', { params: params } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
    }

    getGeoObjectSuggestions( text: string, type: string, parent: string, hierarchy: string ): Promise<GeoObject> {

        let headers = new HttpHeaders( {
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
            .post<GeoObject>( acp + '/cgr/geoobject/suggestions', JSON.stringify( params ), { headers: headers } )
            .toPromise();
    }

    getGeoObjectSuggestionsTypeAhead( text: string, type: string ): Promise<GeoObject> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        let params = {
            text: text,
            type: type,
        } as any;

        return this.http
            .post<GeoObject>( acp + '/cgr/geoobject/suggestions', JSON.stringify( params ), { headers: headers } )
            .toPromise();
    }

    getMasterLists(): Promise<{ locales: string[], lists: { label: string, oid: string, createDate: string, lastUpdateDate: string }[] }> {
        let params: HttpParams = new HttpParams();

        return this.http
            .get<{ locales: string[], lists: { label: string, oid: string, createDate: string, lastUpdateDate: string }[] }>( acp + '/master-list/list-all', { params: params } )
            .toPromise();
    }

    getMasterListHistory( oid: string ): Promise<MasterList> {
        let params: HttpParams = new HttpParams();
        params = params.set( 'oid', oid );

        return this.http
            .get<MasterList>( acp + '/master-list/versions', { params: params } )
            .toPromise();
    }

    getMasterListVersion( oid: string ): Promise<MasterListVersion> {
        let params: HttpParams = new HttpParams();
        params = params.set( 'oid', oid );

        return this.http
            .get<MasterListVersion>( acp + '/master-list/version', { params: params } )
            .toPromise();
    }

    getAttributeVersions( geoObjectCode: string, geoObjectTypeCode: string, attributeName: string ): Promise<ValueOverTime[]> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );
        
        let params: HttpParams = new HttpParams();
        params = params.set( 'geoObjectCode', geoObjectCode );
        params = params.set( 'geoObjectTypeCode', geoObjectTypeCode );
        params = params.set( 'attributeName', attributeName );
        
        return this.http
            .get<ValueOverTime[]>( acp + '/cgr/geoobject/getAttributeVersions', { params: params } )
            .toPromise();
    }
    
    setAttributeVersions( geoObjectCode: string, geoObjectTypeCode: string, attributeName: string, collection: ValueOverTime[] ): Promise<Response> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );
        
        let params = {
            geoObjectCode: geoObjectCode,
            geoObjectTypeCode: geoObjectTypeCode,
            attributeName: attributeName,
            collection: collection
            
        } as any;

        this.eventService.start();

        return this.http
            .post<Response>( acp + '/cgr/geoobject/setAttributeVersions', JSON.stringify( params ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    createMasterList( list: MasterList ): Promise<MasterList> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<MasterList>( acp + '/master-list/create', JSON.stringify( { list: list } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    createMasterListVersion( oid: string, forDate: string ): Promise<MasterListVersion> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<MasterListVersion>( acp + '/master-list/create-version', JSON.stringify( { oid: oid, forDate: forDate } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    deleteMasterList( oid: string ): Promise<void> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( acp + '/master-list/remove', JSON.stringify( { oid: oid } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
    }

    deleteMasterListVersion( oid: string ): Promise<void> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( acp + '/master-list/remove-version', JSON.stringify( { oid: oid } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
    }

    publishMasterList( oid: string ): Observable<MasterListVersion> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        return this.http.post<MasterListVersion>( acp + '/master-list/publish', JSON.stringify( { oid: oid } ), { headers: headers } );
    }

    getMasterList( oid: string ): Promise<MasterList> {
        let params: HttpParams = new HttpParams();
        params = params.set( 'oid', oid );

        return this.http
            .get<MasterList>( acp + '/master-list/get', { params: params } )
            .toPromise();
    }

    /*
     * Not really part of the RegistryService
     */
    applyGeoObjectEdit( parentTreeNode: ParentTreeNode, geoObject: GeoObject, isNew: boolean, masterListId: string ): Promise<void> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( acp + '/geoobject-editor/apply', JSON.stringify( { parentTreeNode: parentTreeNode, geoObject: geoObject, isNew: isNew, masterListId: masterListId } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    data( oid: string, pageNumber: number, pageSize: number, filter: { attribute: string, value: string }[], sort: { attribute: string, order: string } ): Promise<any> {
        let headers = new HttpHeaders( {
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
            .post<any>( acp + '/master-list/data', JSON.stringify( params ), { headers: headers } )
            .toPromise();
    }

    values( oid: string, value: string, attributeName: string, valueAttribute: string, filter: { attribute: string, value: string }[] ): Promise<{ label: string, value: string }[]> {
        let headers = new HttpHeaders( {
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
            .post<{ label: string, value: string }[]>( acp + '/master-list/values', JSON.stringify( params ), { headers: headers } )
            .toPromise();
    }

    progress( oid: string ): Promise<Progress> {
        let params: HttpParams = new HttpParams();
        params = params.set( 'oid', oid );

        return this.http
            .get<Progress>( acp + '/master-list/progress', { params: params } )
            .toPromise();
    }


}
