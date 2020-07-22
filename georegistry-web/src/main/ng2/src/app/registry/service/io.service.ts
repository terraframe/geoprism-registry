import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpResponse, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { ImportConfiguration, Synonym, Location, Term } from '../model/io';
import { EventService } from '../../shared/service/event.service';

declare var acp: string;

@Injectable()
export class IOService {

    constructor( private http: HttpClient, private eventService: EventService ) { }

    importSpreadsheet( configuration: ImportConfiguration ): Promise<ImportConfiguration> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<ImportConfiguration>( acp + '/etl/import', JSON.stringify( { json: configuration } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    cancelImport( configuration: ImportConfiguration ): Promise<void> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( acp + '/etl/cancel-import', JSON.stringify( { configuration: configuration } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise()
    }

    importShapefile( configuration: ImportConfiguration ): Promise<ImportConfiguration> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<ImportConfiguration>( acp + '/etl/import', JSON.stringify( { json: configuration } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise()
    }

    listGeoObjectTypes( includeLeafTypes: boolean ): Promise<{ label: string, code: string, orgCode: string }[]> {
        let params: HttpParams = new HttpParams();
        params = params.set( 'includeLeafTypes', includeLeafTypes.toString() );

        return this.http
            .get<{ label: string, code: string, orgCode: string }[]>( acp + '/cgr/geoobjecttype/list-types', { params: params } )
            .toPromise();
    }

    getTypeAncestors( code: string, hierarchyCode: string ): Promise<Location[]> {
        let params: HttpParams = new HttpParams();
        params = params.set( 'code', code );
        params = params.set( 'hierarchyCode', hierarchyCode );

        return this.http
            .get<Location[]>( acp + '/cgr/geoobjecttype/get-ancestors', { params: params } )
            .toPromise();
    }

    getHierarchiesForType( code: string, includeTypes: boolean ): Promise<{ label: string, code: string, parents: { label: string, code: string }[] }[]> {
        let params: HttpParams = new HttpParams();
        params = params.set( 'code', code );
        params = params.set( 'includeTypes', includeTypes.toString() );

        this.eventService.start();

        return this.http
            .get<{ label: string, code: string, parents: { label: string, code: string }[] }[]>( acp + '/cgr/geoobjecttype/get-hierarchies', { params: params } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    getGeoObjectSuggestions( text: string, type: string, parent: string, hierarchy: string ): Promise<any> {
        
        let headers = new HttpHeaders( {
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
            .post<any>( acp + '/cgr/geoobject/suggestions', JSON.stringify( params ), { headers: headers } )
            .toPromise();
    }

    createGeoObjectSynonym( entityId: string, label: string ): Promise<Synonym> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<Synonym>( acp + '/geo-synonym/createGeoEntitySynonym', JSON.stringify( { entityId: entityId, label: label } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    deleteGeoObjectSynonym( synonymId: string, vOid: string ): Promise<void> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( acp + '/geo-synonym/deleteGeoEntitySynonym', JSON.stringify( { synonymId: synonymId, vOid: vOid } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise()
    }

    getTermSuggestions( mdAttributeId: string, text: string, limit: string ): Promise<{ text: string, data: any }[]> {

        let params: HttpParams = new HttpParams();
        params = params.set( 'mdAttributeId', mdAttributeId );
        params = params.set( 'text', text );
        params = params.set( 'limit', limit );

        return this.http
            .get<{ text: string, data: any }[]>( acp + '/uploader/getClassifierSuggestions', { params: params } )
            .toPromise()
    }

    createTermSynonym( classifierId: string, label: string ): Promise<Synonym> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        let data = JSON.stringify( { classifierId: classifierId, label: label } );

        return this.http
            .post<Synonym>( acp + '/uploader/createClassifierSynonym', data, { headers: headers } )
            .toPromise();
    }

    deleteTermSynonym( synonymId: string ): Promise<void> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        let data = JSON.stringify( { synonymId: synonymId } );

        return this.http
            .post<void>( acp + '/uploader/deleteClassifierSynonym', data, { headers: headers } )
            .toPromise()
    }

    createTerm( label: string, code: string, parentTermCode: string ): Promise<Term> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        let params = { parentTermCode: parentTermCode, termJSON: { label: label, code: code } };

        return this.http
            .post<Term>( acp + '/cgr/geoobjecttype/addterm', JSON.stringify( params ), { headers: headers } )
            .toPromise();
    }

    removeTerm( parentTermCode: string, termCode: string ): Promise<void> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        return this.http
            .post<void>( acp + '/cgr/geoobjecttype/deleteterm', JSON.stringify( { 'parentTermCode': parentTermCode, 'termCode': termCode } ), { headers: headers } )
            .toPromise()
    }




}
