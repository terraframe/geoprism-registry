import { Injectable } from '@angular/core';
import { Headers, Http, RequestOptions, Response, URLSearchParams } from '@angular/http';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';
import { Observable } from 'rxjs/Observable';

import { ChangeRequest, AbstractAction } from '../data/crtable/crtable';
import { EventService } from '../event/event.service';
import { GeoObject } from '../model/registry';

declare var acp: any;

@Injectable()
export class ChangeRequestService {

    constructor( private http: Http, private eventService: EventService ) { }

	  getAllActions( requestId: string ): Promise<AbstractAction[]> {
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
                return response.json();
            } )
	}
	

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

    applyAction( action: any ): Promise<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/changerequest/applyAction', JSON.stringify( { action: action } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response;
            } )
	}
	
	applyActionStatusProperties( action: any ): Promise<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/changerequest/applyActionStatusProperties', JSON.stringify( { action: action } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response;
            } )
	}
	
	
    lockAction( actionId: string ): Promise<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/changerequest/lockAction', JSON.stringify( { actionId: actionId } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response;
            } )
    }

    unlockAction( actionId: string ): Promise<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/changerequest/unlockAction', JSON.stringify( { actionId: actionId } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response;
            } )
    }

    getAllRequests(filter: string): Promise<ChangeRequest[]> {
		let params: URLSearchParams = new URLSearchParams();

		params.set('filter', filter );

        return this.http.get( acp + '/changerequest/get-all-requests', { params: params } )
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
				// return [{"approvalStatus":"PENDING","actionType":"net.geoprism.registry.action.geoobject.UpdateGeoObjectAction","geoObjectJson":{"type":"Feature","properties":{"displayLabel":{"localeValues":[{"locale":"defaultLocale","value":"Saen Monorom 1 TEST"},{"locale":"km_KH","value":"សែនមនោរម្យ១"}],"localizedValue":"Saen Monorom 1"},"uid":"37cdd6af-2df1-4b74-83bf-2b3bcd9694c3","sequence":null,"code":"22020312","lastUpdateDate":1553745600000,"type":"Village","createDate":1553745600000,"status":["CGR:Status-Active"]}},"maintainerNotes":"","createActionDate":"5/16/19 2:21:34 PM","geoObjectType":{"code":"Village","description":{"localeValues":[{"locale":"defaultLocale","value":""},{"locale":"km_KH","value":""}],"localizedValue":""},"attributes":[{"isDefault":true,"code":"displayLabel","unique":false,"description":{"localeValues":[],"localizedValue":"Label of the location"},"label":{"localeValues":[],"localizedValue":"Display Label"},"type":"local","required":true},{"isDefault":true,"code":"uid","unique":false,"description":{"localeValues":[],"localizedValue":"The internal globally unique identifier ID"},"label":{"localeValues":[],"localizedValue":"UID"},"type":"character","required":true},{"isDefault":true,"code":"sequence","unique":false,"description":{"localeValues":[],"localizedValue":"The sequence number of the GeoObject that is incremented when the object is updated"},"label":{"localeValues":[],"localizedValue":"Sequence"},"type":"integer","required":false},{"isDefault":true,"code":"code","unique":true,"description":{"localeValues":[{"locale":"defaultLocale","value":"Human readable unique identified"},{"locale":"km_KH","value":""}],"localizedValue":"Human readable unique identified"},"label":{"localeValues":[{"locale":"defaultLocale","value":"Code"},{"locale":"km_KH","value":""}],"localizedValue":"Code"},"type":"character","required":true},{"isDefault":true,"code":"lastUpdateDate","unique":false,"description":{"localeValues":[],"localizedValue":"The date the object was updated"},"label":{"localeValues":[],"localizedValue":"Date Last Updated"},"type":"date","required":false},{"isDefault":true,"code":"type","unique":false,"description":{"localeValues":[],"localizedValue":"The type of the GeoObject"},"label":{"localeValues":[],"localizedValue":"Type"},"type":"character","required":false},{"isDefault":true,"code":"createDate","unique":false,"description":{"localeValues":[],"localizedValue":"The date the object was created"},"label":{"localeValues":[],"localizedValue":"Date Created"},"type":"date","required":false},{"isDefault":true,"code":"status","rootTerm":{"code":"CGR:Status-Root","children":[{"code":"CGR:Status-New","children":[],"description":{"localeValues":[],"localizedValue":"A newly created GeoObject that has not been submitted for approval."},"label":{"localeValues":[],"localizedValue":"New"}},{"code":"CGR:Status-Active","children":[],"description":{"localeValues":[],"localizedValue":"The GeoObject is a part of the master list."},"label":{"localeValues":[],"localizedValue":"Active"}},{"code":"CGR:Status-Pending","children":[],"description":{"localeValues":[],"localizedValue":"Edits to the GeoObject are pending approval"},"label":{"localeValues":[],"localizedValue":"Pending"}},{"code":"CGR:Status-Inactive","children":[],"description":{"localeValues":[],"localizedValue":"The object is not considered a source of truth"},"label":{"localeValues":[],"localizedValue":"Inactive"}}],"description":{"localeValues":[],"localizedValue":"The status of a GeoObject changes during the governance lifecycle."},"label":{"localeValues":[],"localizedValue":"GeoObject Status"}},"unique":false,"description":{"localeValues":[{"locale":"defaultLocale","value":"The status of the GeoObject"},{"locale":"km_KH","value":""}],"localizedValue":"The status of the GeoObject"},"label":{"localeValues":[{"locale":"defaultLocale","value":"Status"},{"locale":"km_KH","value":""}],"localizedValue":"Status"},"type":"term","required":true}],"label":{"localeValues":[{"locale":"defaultLocale","value":"Village"},{"locale":"km_KH","value":"Village"}],"localizedValue":"Village"},"isLeaf":"false","geometryType":"POINT"},"oid":"f30ec8e1-0aab-4b4a-8df0-57f7b80005d1","contributorNotes":"TEST","statusLabel":"PENDING","actionLabel":"Update GeoObject"}]

				return response.json() as ChangeRequest
            } );

    }

	confirmChangeRequest( requestId: string ): Promise<ChangeRequest> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http.post( acp + '/changerequest/confirm-change-request', JSON.stringify( { requestId: requestId } ), { headers: headers } )
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

    rejectAllActions( requestId: string, actions:any ): Promise<AbstractAction[]> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http.post( acp + '/changerequest/reject-all-actions', JSON.stringify( { requestId: requestId, actions: actions } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json()
            } );
    }

    approveAllActions( requestId: string, actions:any ): Promise<AbstractAction[]> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http.post( acp + '/changerequest/approve-all-actions', JSON.stringify( { requestId: requestId, actions: actions } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() 
            } );

    }

    submitChangeRequest( actions: string ): Promise<GeoObject> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        let params: URLSearchParams = new URLSearchParams();
        params.set( 'actions', actions )

        this.eventService.start();

        return this.http.post( acp + '/cgr/submitChangeRequest', {actions: actions}, { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as GeoObject
            } );

    }

}
