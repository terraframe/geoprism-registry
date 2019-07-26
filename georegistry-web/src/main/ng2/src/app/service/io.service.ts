import { Injectable } from '@angular/core';
import { Headers, Http, Response, URLSearchParams, RequestOptions, ResponseContentType } from '@angular/http';
import { Observable } from 'rxjs';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { ImportConfiguration, Synonym, Location, Term } from '../data/importer/io';
import { EventService } from '../event/event.service';

declare var acp: string;

@Injectable()
export class IOService {

    constructor( private http: Http, private eventService: EventService ) { }

    importSpreadsheet( configuration: ImportConfiguration ): Promise<ImportConfiguration> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/excel/import-spreadsheet', JSON.stringify( { configuration: configuration } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as ImportConfiguration;
            } )
    }

    cancelSpreadsheetImport( configuration: ImportConfiguration ): Promise<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/excel/cancel-import', JSON.stringify( { configuration: configuration } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
    }

    importShapefile( configuration: ImportConfiguration ): Promise<ImportConfiguration> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/shapefile/import-shapefile', JSON.stringify( { configuration: configuration } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as ImportConfiguration;
            } )
    }

    cancelShapefileImport( configuration: ImportConfiguration ): Promise<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/shapefile/cancel-import', JSON.stringify( { configuration: configuration } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
    }

    listGeoObjectTypes( includeLeafTypes: boolean ): Promise<{ label: string, code: string }[]> {
        let params: URLSearchParams = new URLSearchParams();
        params.set( 'includeLeafTypes', includeLeafTypes.toString() );

        return this.http
            .get( acp + '/cgr/geoobjecttype/list-types', { params: params } )
            .toPromise()
            .then( response => {
                return response.json() as { label: string, code: string }[];
            } )
    }

    getTypeAncestors( code: string, hierarchyCode: string ): Promise<Location[]> {
        let params: URLSearchParams = new URLSearchParams();
        params.set( 'code', code );
        params.set( 'hierarchyCode', hierarchyCode );

        return this.http
            .get( acp + '/cgr/geoobjecttype/get-ancestors', { params: params } )
            .toPromise()
            .then( response => {
                return response.json() as Location[];
            } )
    }

    getHierarchiesForType( code: string, includeTypes: boolean ): Promise<{ label: string, code: string, parents: { label: string, code: string }[] }[]> {
        let params: URLSearchParams = new URLSearchParams();
        params.set( 'code', code );
        params.set( 'includeTypes', includeTypes.toString() );

        this.eventService.start();

        return this.http
            .get( acp + '/cgr/geoobjecttype/get-hierarchies', { params: params } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as { label: string, code: string, parents: { label: string, code: string }[] }[];
            } )
    }

    getGeoObjectSuggestions( text: string, type: string, parent: string, hierarchy: string ): Promise<any> {
        
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        let params = {
            text: text,
            type: type,
        } as any;

        if ( parent != null && hierarchy != null ) {
            params.parent = parent;
            params.hierarchy = parent;
        }

        return this.http
            .post( acp + '/cgr/geoobject/suggestions', JSON.stringify( params ), { headers: headers } )
            .toPromise()
            .then( response => {
                return response.json();
            } );
    }

    createGeoObjectSynonym( entityId: string, label: string ): Promise<Synonym> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/uploader/createGeoEntitySynonym', JSON.stringify( { entityId: entityId, label: label } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as Synonym;
            } )
    }

    deleteGeoObjectSynonym( synonymId: string ): Promise<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/uploader/deleteGeoEntitySynonym', JSON.stringify( { synonymId: synonymId } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
    }

    getTermSuggestions( mdAttributeId: string, text: string, limit: string ): Promise<Array<{ text: string, data: any }>> {

        let params: URLSearchParams = new URLSearchParams();
        params.set( 'mdAttributeId', mdAttributeId );
        params.set( 'text', text );
        params.set( 'limit', limit );

        return this.http
            .get( acp + '/uploader/getClassifierSuggestions', { search: params } )
            .toPromise()
            .then(( response: any ) => {
                return response.json() as Array<{ text: string, data: any }>;
            } )
    }

    createTermSynonym( classifierId: string, label: string ): Promise<Synonym> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        let data = JSON.stringify( { classifierId: classifierId, label: label } );

        return this.http
            .post( acp + '/uploader/createClassifierSynonym', data, { headers: headers } )
            .toPromise()
            .then(( response: any ) => {
                return response.json() as Synonym;
            } )
    }

    deleteTermSynonym( synonymId: string ): Promise<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        let data = JSON.stringify( { synonymId: synonymId } );

        return this.http
            .post( acp + '/uploader/deleteClassifierSynonym', data, { headers: headers } )
            .toPromise()
    }

    createTerm( label: string, code: string, parentTermCode: string ): Promise<Term> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        let params = { parentTermCode: parentTermCode, termJSON: { label: label, code: code } };

        return this.http
            .post( acp + '/cgr/geoobjecttype/addterm', JSON.stringify( params ), { headers: headers } )
            .toPromise()
            .then(( response: any ) => {
                return response.json() as Term;
            } )
    }

    removeTerm( termCode: string ): Promise<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        return this.http
            .post( acp + '/cgr/geoobjecttype/deleteterm', JSON.stringify( { termCode: termCode } ), { headers: headers } )
            .toPromise()
    }




}
