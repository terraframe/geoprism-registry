import { Injectable } from '@angular/core';
import { Headers, Http, Response, URLSearchParams } from '@angular/http';
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

    listGeoObjectTypes(): Promise<{ label: string, code: string }[]> {
        return this.http
            .get( acp + '/cgr/list-geo-object-types' )
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

    getGeoObjectSuggestions( text: string, type: string, parent: string, hierarchy: string ): Promise<any> {

        let params: URLSearchParams = new URLSearchParams();
        params.set( 'text', text );
        params.set( 'type', type );
        params.set( 'parent', parent );
        params.set( 'hierarchy', hierarchy );

        return this.http
            .get( acp + '/cgr/geoobject/suggestions', { search: params } )
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

    createTerm( label: string, parentOid: string, validate: boolean ): Promise<Term> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        let option = { label: label, parentOid: parentOid, validate: validate };

        return this.http
            .post( acp + '/category/create', JSON.stringify( { option: option } ), { headers: headers } )
            .toPromise()
            .then(( response: any ) => {
                return response.json() as Term;
            } )
    }

    removeTerm( oid: string ): Promise<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        return this.http
            .post( acp + '/category/remove', JSON.stringify( { oid: oid } ), { headers: headers } )
            .toPromise()
    }




}
