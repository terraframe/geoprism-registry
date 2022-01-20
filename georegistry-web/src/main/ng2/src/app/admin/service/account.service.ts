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
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service'

import { Account, User, UserInvite } from '@admin/model/account';

import { PageResult } from '@shared/model/core'

import { GeoRegistryConfiguration } from "@core/model/registry"; declare let registry: GeoRegistryConfiguration;

@Injectable()
export class AccountService {

    constructor( private http: HttpClient, private eventService: EventService ) { }

    page( pageNumber: number, pageSize: number ): Promise<PageResult<User>> {
        let params: HttpParams = new HttpParams();
        params = params.set( 'pageNumber', pageNumber.toString() );
        params = params.set( 'pageSize', pageSize.toString() );

        this.eventService.start();

        return this.http
            .get<PageResult<User>>( registry.contextPath + '/registryaccount/page', { params: params } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    getSRAs( pageNumber: number, pageSize: number ): Promise<PageResult<User>> {
        let params: HttpParams = new HttpParams();
        params = params.set( 'pageNumber', pageNumber.toString() );
        params = params.set( 'pageSize', pageSize.toString() );

        this.eventService.start();

        return this.http
            .get<PageResult<User>>( registry.contextPath + '/registryaccount/get-sras', { params: params } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    edit( oid: string ): Promise<Account> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<Account>( registry.contextPath + '/registryaccount/edit', JSON.stringify( { oid: oid } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    newInstance(organizationCodes: string[]): Promise<Account> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<Account>( registry.contextPath + '/registryaccount/newInstance', JSON.stringify( {"organizationCodes": organizationCodes} ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    newUserInstance(): Promise<User> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<User>( registry.contextPath + '/registryaccount/newUserInstance', JSON.stringify( {} ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    newInvite(organizationCodes: string[]): Promise<Account> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<Account>( registry.contextPath + '/registryaccount/newInvite', JSON.stringify( {"organizationCodes": organizationCodes} ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    remove( oid: string ): Promise<void> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( registry.contextPath + '/registryaccount/remove', JSON.stringify( { oid: oid } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    apply( user: User, roleNames: string[] ): Promise<Account> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<Account>( registry.contextPath + '/registryaccount/apply', JSON.stringify( { account: user, roleNames: roleNames } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    unlock( oid: string ): Promise<void> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( registry.contextPath + '/registryaccount/unlock', JSON.stringify( { oid: oid } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    inviteUser( invite: UserInvite, roleIds: string[] ): Promise<void> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

//        console.log( "Submitting to inviteUser : ", JSON.stringify( { invite: invite, roleIds: roleIds } ) );

        return this.http
            .post<void>( registry.contextPath + '/registryaccount/inviteUser', JSON.stringify( { invite: invite, roleIds: roleIds } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    inviteComplete( user: User, token: string ): Promise<void> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<void>( registry.contextPath + '/registryaccount/inviteComplete', JSON.stringify( { user: user, token: token } ), { headers: headers } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

    getRolesForUser( userOID: string ): Promise<any> {
        let params: HttpParams = new HttpParams();
        params = params.set( 'userOID', userOID );

        this.eventService.start();

        return this.http
            .get<any>( registry.contextPath + '/cgr/account/get-roles-for-user', { params: params } )
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
            .toPromise();
    }

}
