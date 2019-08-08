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

import { Component, EventEmitter, Input, OnInit, OnChanges, Output, Inject, ViewChild } from '@angular/core';
import { ActivatedRoute, Params, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Location } from '@angular/common';

import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/switchMap';

import { Account, User } from './account';

import { EventService } from '../../core/service/core.service';
import { AccountService } from './account.service';

declare let acp: string;

@Component({
  selector: 'account-invite-complete',
  templateUrl: './account-invite-complete.component.html',
  styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}']
})
export class AccountInviteCompleteComponent implements OnInit {
  user:User;
  private sub: any;
  token: string; 
  
  constructor(
    private service:AccountService,
    private eventService:EventService,
    private route:ActivatedRoute,
    private location:Location) {
  }

  ngOnInit(): void {
    this.service.newUserInstance().catch((error:any) => {
      this.eventService.onError(error); 
    
      return Promise.reject(error);
    }).then((user:User) => {
      this.user = user;
    });
    this.sub = this.route.params.subscribe(params => {
       this.token = params['token'];
    });
  }
  
  cancel(): void {
    window.location.href = acp;
  } 
  
  onSubmit(): void {
    this.service.inviteComplete(this.user, this.token).then(response => {
      window.location.href = acp;
    });
  }  
}