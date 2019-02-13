import { Injectable } from '@angular/core';
import { Headers, Http, RequestOptions, Response, URLSearchParams } from '@angular/http';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';
import { Observable } from 'rxjs/Observable';

import { ChangeRequest } from '../data/crtable/crtable';
import { EventService } from '../event/event.service';

declare var acp: any;

@Injectable()
export class ChangeRequestService {

    constructor( private http: Http, private eventService: EventService ) { }

    fetchData( cb: any, requestId: string ): Promise<Response> {
        let params: URLSearchParams = new URLSearchParams();

        if ( requestId != null ) {
            params.set( 'requestId', requestId );
        }

        this.eventService.start();

        return this.http
            .get( acp + '/changerequest/getAllActions', { params: params } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                cb( response.json() );

                return response;
            } )
    }

    acceptAction( action: any ): Promise<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/changerequest/acceptAction', JSON.stringify( { action: action } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response;
            } )
    }

    rejectAction( action: any ): Promise<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/changerequest/rejectAction', JSON.stringify( { action: action } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response;
            } )
    }

    getAllRequests(): Promise<ChangeRequest[]> {
        return this.http.get( acp + '/changerequest/get-all-requests' )
            .toPromise()
            .then( response => {
                return response.json() as ChangeRequest[]
            } );

    }

    getRequestDetails( requestId: string ): Promise<ChangeRequest> {
        let params: URLSearchParams = new URLSearchParams();
        params.set( 'requestId', requestId );

        this.eventService.start();

        return this.http.get( acp + '/changerequest/get-request-details', { params: params } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as ChangeRequest
            } );

    }

    execute( requestId: string ): Promise<ChangeRequest> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http.post( acp + '/changerequest/execute-actions', JSON.stringify( { requestId: requestId } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as ChangeRequest
            } );

    }

    rejectAllActions( requestId: string ): Promise<ChangeRequest> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http.post( acp + '/changerequest/reject-all-actions', JSON.stringify( { requestId: requestId } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as ChangeRequest
            } );
    }

    approveAllActions( requestId: string ): Promise<ChangeRequest> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http.post( acp + '/changerequest/approve-all-actions', JSON.stringify( { requestId: requestId } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as ChangeRequest
            } );

    }
}
