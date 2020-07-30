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
import { HttpHeaders, HttpClient, HttpResponse, HttpParams } from '@angular/common/http';

// import 'rxjs/add/operator/toPromise';

import { EventService } from './event.service';

import { Profile } from '@shared/model/profile';

declare var acp: any;

@Injectable()
export class ProfileService {

    constructor( service: EventService, private http: HttpClient ) { }

    get(): Promise<Profile> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        return this.http
            .post<Profile>( acp + '/registryaccount/get', { headers: headers } )
            .toPromise();
    }


    apply( profile: Profile ): Promise<Profile> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        return this.http
            .post<Profile>( acp + '/registryaccount/apply', JSON.stringify( { account: profile } ), { headers: headers } )
            .toPromise();
    }

    unlock( oid: string ): Promise<void> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        return this.http
            .post<void>( acp + '/registryaccount/unlock', JSON.stringify( { oid: oid } ), { headers: headers } )
            .toPromise()
    }

    getRolesForUser( userOID: string ): Promise<any> {
        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );


        return this.http
            .post<Profile>( acp + '/registryaccount/getRolesForUser', {userOID: userOID}, { headers: headers } )
            .toPromise();

    }
}
