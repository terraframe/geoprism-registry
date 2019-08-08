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

import { EventService, BasicService } from '../service/core.service';
import { EventHttpService } from '../service/event-http.service';

import { AuthService } from './auth.service';
import { User } from './user';

declare var acp: any;

@Injectable()
export class SessionService extends BasicService {
  
  constructor(service: EventService, private ehttp: EventHttpService, private http: Http, private authService:AuthService) {
    super(service); 
  }
  
  login(username:string, password:string): Promise<User> {

    let headers = new Headers({
      'Content-Type': 'application/json'
    });  
  
    return this.ehttp
      .post(acp + '/session/login', JSON.stringify({username:username, password:password}), {headers: headers})
      .toPromise()
      .then((response: any) => {
        let user = response.json() as User;
        this.authService.setUser(user);
        
        return user;
      })
      .catch(this.handleError.bind(this));      
  }  
  
  logout(): Promise<Response> {
    
    let headers = new Headers({
      'Content-Type': 'application/json'
    });  
    
    return this.ehttp
    .post(acp + '/session/logout', {headers: headers})
    .toPromise()
    .then((response: any) => {
      this.authService.setUser(null);
      
      return response;
    })
    .catch(this.handleError.bind(this));      
  }  
}
