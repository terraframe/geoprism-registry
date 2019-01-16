import { Injectable } from '@angular/core';
import { Headers, Http, Response, URLSearchParams } from '@angular/http';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { ShapefileConfiguration } from '../data/importer/shapefile';
import { EventService } from '../event/event.service';

declare var acp: string;

@Injectable()
export class ExcelService {

    constructor( private http: Http, private eventService: EventService ) { }

    importSpreadsheet( configuration: ShapefileConfiguration ): Promise<Response> {
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
}
