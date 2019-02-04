import { Injectable } from '@angular/core';
import { Headers, Http, Response, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { ImportConfiguration, GeoObjectSynonym, Location } from '../data/importer/io';
import { EventService } from '../event/event.service';

declare var acp: string;

@Injectable()
export class IOService {

    constructor( private http: Http, private eventService: EventService ) { }

    importSpreadsheet( configuration: ImportConfiguration ): Promise<Response> {
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



    createGeoObjectSynonym( entityId: string, label: string ): Promise<GeoObjectSynonym> {
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
                return response.json() as GeoObjectSynonym;
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
}
