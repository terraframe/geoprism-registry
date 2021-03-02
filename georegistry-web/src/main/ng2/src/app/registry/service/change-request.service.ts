import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';
import { finalize } from 'rxjs/operators';

import { ChangeRequest, AbstractAction } from '@registry/model/crtable';
import { EventService } from '@shared/service';
import { GeoObject } from '@registry/model/registry';

declare var acp: any;

@Injectable()
export class ChangeRequestService {

    constructor( private http: HttpClient, private eventService: EventService ) { }

	  getAllActions( requestId: string ): Promise<AbstractAction[]> {
        let params: HttpParams = new HttpParams();

        if ( requestId != null ) {
            params = params.set( 'requestId', requestId );
		}
		
        this.eventService.start();

        return this.http
            .get<AbstractAction[]>( acp + '/changerequest/getAllActions', { params: params } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
	}
	

//    fetchData( cb: any, requestId: string ): Promise<HttpResponse> {
//        let params: HttpParams = new HttpParams();
//
//        if ( requestId != null ) {
//            params = params.set( 'requestId', requestId );
//        }
//
//        this.eventService.start();
//
//        return this.http
//            .get( acp + '/changerequest/getAllActions', { params: params } )
//            .finally(() => {
//                this.eventService.complete();
//            } )
//            .toPromise()
//            .then( response => {
//                cb( response.json() );
//
//                return response;
//            } )
//    }

    applyAction( action: any ): Promise<void> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( acp + '/changerequest/applyAction', JSON.stringify( { action: action } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
	}
	
	applyActionStatusProperties( action: any ): Promise<void> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( acp + '/changerequest/applyActionStatusProperties', JSON.stringify( { action: action } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
	}
	
	
    lockAction( actionId: string ): Promise<void> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( acp + '/changerequest/lockAction', JSON.stringify( { actionId: actionId } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    unlockAction( actionId: string ): Promise<void> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( acp + '/changerequest/unlockAction', JSON.stringify( { actionId: actionId } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    getAllRequests(filter: string): Promise<ChangeRequest[]> {
		let params: HttpParams = new HttpParams();

		params = params.set('filter', filter );

        return this.http.get<ChangeRequest[]>( acp + '/changerequest/get-all-requests', { params: params } )
            .toPromise();
    }

    getRequestDetails( requestId: string ): Promise<ChangeRequest> {
        let params: HttpParams = new HttpParams();
        params = params.set( 'requestId', requestId );

        this.eventService.start();

        return this.http.get<ChangeRequest>( acp + '/changerequest/get-request-details', { params: params } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

	confirmChangeRequest( requestId: string ): Promise<ChangeRequest> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http.post<ChangeRequest>( acp + '/changerequest/confirm-change-request', JSON.stringify( { requestId: requestId } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
	}
	
    execute( requestId: string ): Promise<ChangeRequest> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http.post<ChangeRequest>( acp + '/changerequest/execute-actions', JSON.stringify( { requestId: requestId } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    delete( requestId: string ): Promise<ChangeRequest> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http.post<ChangeRequest>( acp + '/changerequest/delete', JSON.stringify( { requestId: requestId } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    rejectAllActions( requestId: string, actions:any ): Promise<AbstractAction[]> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http.post<AbstractAction[]>( acp + '/changerequest/reject-all-actions', JSON.stringify( { requestId: requestId, actions: actions } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    approveAllActions( requestId: string, actions:any ): Promise<AbstractAction[]> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http.post<AbstractAction[]>( acp + '/changerequest/approve-all-actions', JSON.stringify( { requestId: requestId, actions: actions } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    submitChangeRequest( actions: string ): Promise<GeoObject> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        let params: HttpParams = new HttpParams();
        params = params.set( 'actions', actions )

        this.eventService.start();

        return this.http.post<GeoObject>( acp + '/cgr/submitChangeRequest', {actions: actions}, { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

}
