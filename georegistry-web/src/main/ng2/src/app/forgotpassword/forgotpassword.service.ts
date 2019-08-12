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

import { EventService } from '../event/event.service'

declare var acp: any;

@Injectable()
export class ForgotPasswordService {
  
  constructor(private http: Http, private eventService: EventService) {}
  
  submit(username:string): Promise<Response> {
    let headers = new Headers({
      'Content-Type': 'application/json'
    });  
    
    this.eventService.start();
  
    return this.http
      .post(acp + '/forgotpassword/initiate', JSON.stringify({username:username}), {headers: headers})
      .finally(() => {
        this.eventService.complete();
      } )
      .toPromise()
      .then((response: any) => {
        return response.json();
      });
  }  
}
