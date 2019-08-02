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

import { EventService, BasicService } from '../../core/service/core.service';
import { EventHttpService } from '../../core/service/event-http.service';

import { Account, User, PageResult, UserInvite } from './account';

declare var acp: any;

@Injectable()
export class AccountService extends BasicService {
  
  constructor(service: EventService, private ehttp: EventHttpService, private http: Http) {
    super(service); 
  }
  
  page(p:number): Promise<PageResult> {
    let params: URLSearchParams = new URLSearchParams();
    params.set('number', p.toString());
    
    return this.ehttp
      .get(acp + '/account/page', {search: params})
      .toPromise()
      .then(response => {
        return response.json() as PageResult;
      })
      .catch(this.handleError.bind(this));
  }
  
  edit(oid:string): Promise<Account> {

    let headers = new Headers({
      'Content-Type': 'application/json'
    });  
  
    return this.ehttp
      .post(acp + '/account/edit', JSON.stringify({oid:oid}), {headers: headers})
      .toPromise()
      .then((response: any) => {
        return response.json() as Account;
      })
      .catch(this.handleError.bind(this));      
  }
  
  newInstance(): Promise<Account> {
    
    let headers = new Headers({
      'Content-Type': 'application/json'
    });  
    
    return this.ehttp
    .post(acp + '/account/newInstance', JSON.stringify({}), {headers: headers})
    .toPromise()
    .then((response: any) => {
      return response.json() as Account;
    })
    .catch(this.handleError.bind(this));      
  }
  
  newInvite(): Promise<Account> {
    
    let headers = new Headers({
      'Content-Type': 'application/json'
    });  
    
    return this.ehttp
    .post(acp + '/account/newInvite', JSON.stringify({}), {headers: headers})
    .toPromise()
    .then((response: any) => {
      return response.json() as Account;
    })
    .catch(this.handleError.bind(this));      
  }
  
  remove(oid:string): Promise<Response> {
    
    let headers = new Headers({
      'Content-Type': 'application/json'
    });  
    
    return this.ehttp
    .post(acp + '/account/remove', JSON.stringify({oid:oid}), {headers: headers})
    .toPromise()
    .catch(this.handleError.bind(this));      
  }
  
  apply(user:User, roleIds:string[]): Promise<User> {
    
    let headers = new Headers({
      'Content-Type': 'application/json'
    });  
    
    return this.ehttp
    .post(acp + '/account/apply', JSON.stringify({account:user, roleIds:roleIds}), {headers: headers})
    .toPromise()
    .then((response: any) => {
      return response.json() as User;
    })
    .catch(this.handleError.bind(this));      
  }
  
  unlock(oid:string): Promise<Response> {
    
    let headers = new Headers({
      'Content-Type': 'application/json'
    });  
    
    return this.ehttp
    .post(acp + '/account/unlock', JSON.stringify({oid:oid}), {headers: headers})
    .toPromise()
    .catch(this.handleError.bind(this));      
  }
  
  inviteUser(invite:UserInvite, roleIds:string[]): Promise<Response>
  {
    let headers = new Headers({
      'Content-Type': 'application/json'
    });  
    
    console.log("Submitting to inviteUser : ", JSON.stringify({invite: invite, roleIds: roleIds}));
    
    return this.ehttp
    .post(acp + '/account/inviteUser', JSON.stringify({invite: invite, roleIds: roleIds}), {headers: headers})
    .toPromise()
    .catch(this.handleError.bind(this));
  }
  
  inviteComplete(user:User, token:string): Promise<Response>
  {
    let headers = new Headers({
      'Content-Type': 'application/json'
    });  
    
    return this.ehttp
    .post(acp + '/account/inviteComplete', JSON.stringify({user: user, token: token}), {headers: headers})
    .toPromise()
    .catch(this.handleError.bind(this));
  }
  
}
