import { Injectable } from '@angular/core';
import { Headers, Http, Response, URLSearchParams } from '@angular/http';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { ShapefileConfiguration, Location } from '../data/importer/shapefile';
import { EventService } from '../event/event.service';

declare var acp: string;

@Injectable()
export class ShapefileService {

    constructor( private http: Http, private eventService: EventService ) { }

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

    importShapefile( configuration: ShapefileConfiguration ): Promise<Response> {
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
    }

    cancelImport( configuration: ShapefileConfiguration ): Promise<Response> {
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
}
