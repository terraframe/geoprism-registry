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
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { Application } from '@shared/model/application';
import { SessionService } from '@shared/service';
import { HubService } from '@core/service/hub.service';

import { ErrorHandler, ErrorModalComponent } from '@shared/component';

import { LoginHeaderComponent } from './login-header.component';

declare var acp: any;

@Component( {
    selector: 'login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
} )
export class LoginComponent implements OnInit {
    context: string;
    username: string = '';
    password: string = '';
    
    oauthServers: any[] = null;
    viewOauthServers: boolean = false;

    /*
     * Reference to the modal current showing
    */
    private bsModalRef: BsModalRef;

    ngOnInit(): void {
      this.hService.oauthGetPublic(null).then(oauthServers => {
        
        if (oauthServers && oauthServers.length > 0)
        {
          this.oauthServers = oauthServers;
        }
        
      }).catch((err: HttpErrorResponse) => {
        this.error(err);
      });
    }

    constructor( private service: SessionService, private hService: HubService, private modalService: BsModalService, private router: Router ) {
        this.context = acp as string;
    }
    
    onClickDhis2(url: any): void {
      if (url == null)
      {
        if (this.oauthServers.length == 1)
        {
          window.location.href = this.oauthServers[0].url;
        }
        else
        {
          this.viewOauthServers = !this.viewOauthServers;
        }
      }
      else
      {
        window.location.href = url;
      }
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
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    open( application: Application ): void {
        window.location.href = this.context + '/' + application.url;
    }

    public error( err: HttpErrorResponse ): void {
      this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }


}
