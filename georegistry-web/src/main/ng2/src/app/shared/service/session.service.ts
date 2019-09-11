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

import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { EventService } from './event.service';

import { AuthService } from './auth.service';
import { User } from '../model/user';

declare var acp: any;

@Injectable()
export class SessionService {

    constructor( private service: EventService, private http: HttpClient, private authService: AuthService ) { }

    login( username: string, password: string ): Promise<User> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.service.start();

        return this.http
            .post<User>( acp + '/session/login', JSON.stringify( { username: username, password: password } ), { headers: headers } )
            .finally(() => {
                this.service.complete();
            } )
            .toPromise()
            .then(( user: User ) => {
                this.authService.setUser( user );

                return user;
            } )
    }

    logout(): Promise<void> {

        let headers = new HttpHeaders( {
            'Content-Type': 'application/json'
        } );

        this.service.start();


        return this.http
            .post<void>( acp + '/session/logout', { headers: headers } )
            .finally(() => {
                this.service.complete();
            } )
            .toPromise()
            .then(( response: any ) => {
                this.authService.setUser( null );

                return response;
            } )
    }
}
