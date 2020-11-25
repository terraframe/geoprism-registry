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
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler } from '@shared/component';
import { HubService } from '@core/service/hub.service';

declare var acp:string;

@Component({
  selector: 'login-header',
  templateUrl: './login-header.component.html',
  styleUrls: []
})
export class LoginHeaderComponent implements OnInit {
  context:string;
  
  oauthServer: any = null;

  constructor(private hubService: HubService) {
    this.context = acp;
  }
  
  ngOnInit(): void {
    this.hubService.oauthGetPublic(null).then(oauthServers => {
      
      console.log("Retrieved oauth servers", oauthServers);
      if (oauthServers && oauthServers.length > 0)
      {
        this.oauthServer = oauthServers[0];
      }
      
    }).catch((err: HttpErrorResponse) => {
      this.error(err);
    });
  }
  
  public error(err: HttpErrorResponse): void {
    let msg = ErrorHandler.getMessageFromError(err);
    console.log(msg, err);
  }
}
