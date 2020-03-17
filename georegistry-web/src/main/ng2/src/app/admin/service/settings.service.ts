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
import { HttpHeaders, HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';

import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { EventService } from '../../shared/service/event.service'
import { MessageContainer } from '../../shared/model/core';

import { Account, User, PageResult, UserInvite } from '../model/account';
import { Settings, Organization } from '../model/settings'

declare var acp: any;

@Injectable()
export class SettingsService {

    constructor( private http: HttpClient, private eventService: EventService ) { }

    getOrganizations(): Promise<Organization[]> {

        this.eventService.start();
    
        return this.http
            .get<Organization[]>(acp + '/cgr/organizations/get-all')
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    editOrganization( oid: string ): Promise<Account> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<Account>( acp + '/account/edit', JSON.stringify( { oid: oid } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    removeOrganization( oid: string ): Promise<Account> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<Account>( acp + '/account/edit', JSON.stringify( { oid: oid } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }

    newOrganization(json: any ): Promise<any> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.eventService.start();

        return this.http
            .post<any>( acp + '/cgr/orgainization/create', JSON.stringify( { json: json } ), { headers: headers } )
            .finally(() => {
                this.eventService.complete();
            } )
            .toPromise();
    }


}
