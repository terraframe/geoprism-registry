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
/// License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
///
import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { SessionService } from '../auth/session.service';
import { LoginHeaderComponent } from './login-header.component';
import { HubService } from '../../hub/hub.service';
import { Application } from '../../hub/application';

declare var acp: any;

@Component( {
    selector: 'login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
} )
export class LoginComponent {
    context: string;
    username: string = '';
    password: string = '';

    constructor( private service: SessionService, private hService: HubService, private router: Router ) {
        this.context = acp as string;
    }

    onSubmit(): void {
        this.service.login( this.username, this.password ).then( response => {

            this.hService.applications().then( applications => {
                if ( applications.length == 1 ) {
                    this.open( applications[0] );
                }
                else {
                    this.router.navigate( ['/menu/true'] );
                }
            } );

        } );
    }

    open( application: Application ): void {
        window.location.href = this.context + '/' + application.url;
    }

}
