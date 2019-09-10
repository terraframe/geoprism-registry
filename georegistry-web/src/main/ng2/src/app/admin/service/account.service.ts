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
import { Headers, Http, Response, URLSearchParams } from '@angular/http';

import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { EventService } from '../../shared/service/event.service'
import { MessageContainer } from '../../shared/model/core';

import { Account, User, PageResult, UserInvite } from '../model/account';

declare var acp: any;

@Injectable()
export class AccountService {

    constructor( private http: Http, private eventService: EventService ) { }

    page( p: number ): Promise<PageResult> {
        let params: URLSearchParams = new URLSearchParams();
        params.set( 'number', p.toString() );

        this.eventService.start();

        return this.http
            .get( acp + '/account/page', { search: params } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then( response => {
                return response.json() as PageResult;
            } );
    }

    edit( oid: string ): Promise<Account> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/account/edit', JSON.stringify( { oid: oid } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then(( response: any ) => {
                return response.json() as Account;
            } );
    }

    newInstance(): Promise<Account> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/account/newInstance', JSON.stringify( {} ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then(( response: any ) => {
                return response.json() as Account;
            } );
    }

    newUserInstance(): Promise<User> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/account/newUserInstance', JSON.stringify( {} ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then(( response: any ) => {
                return response.json() as User;
            } );
    }

    newInvite(): Promise<Account> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/account/newInvite', JSON.stringify( {} ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then(( response: any ) => {
                return response.json() as Account;
            } );
    }

    remove( oid: string ): Promise<Response> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/account/remove', JSON.stringify( { oid: oid } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    apply( user: User, roleIds: string[] ): Promise<User> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/account/apply', JSON.stringify( { account: user, roleIds: roleIds } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise()
            .then(( response: any ) => {
                return response.json() as User;
            } );
    }

    unlock( oid: string ): Promise<Response> {

        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/account/unlock', JSON.stringify( { oid: oid } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    inviteUser( invite: UserInvite, roleIds: string[] ): Promise<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        console.log( "Submitting to inviteUser : ", JSON.stringify( { invite: invite, roleIds: roleIds } ) );

        return this.http
            .post( acp + '/account/inviteUser', JSON.stringify( { invite: invite, roleIds: roleIds } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    inviteComplete( user: User, token: string ): Promise<Response> {
        let headers = new Headers( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post( acp + '/account/inviteComplete', JSON.stringify( { user: user, token: token } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

     error( resp: Response, container: MessageContainer ): void {
         this.eventService.error(resp, container);
    }



}
