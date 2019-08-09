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

import { Account } from './account';

import { EventService } from '../../core/service/core.service';
import { AccountService } from './account.service';


export class AccountResolver implements Resolve<Account> {
  constructor(@Inject(AccountService) private accountService:AccountService, @Inject(EventService) private eventService: EventService) {}
  
  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):Promise<Account> {
	let oid = route.params['oid'];
	
	if(oid === 'NEW') {
      return this.accountService.newInstance().catch((error:any) => {
        this.eventService.onError(error); 
        
        return Promise.reject(error);
      });    				
	}
	else {	
      return this.accountService.edit(oid).catch((error:any) => {
        this.eventService.onError(error); 
        
        return Promise.reject(error);
      });    		
	}
  }
}

@Component({  
  selector: 'account',
  templateUrl: './account.component.html',
  styles: ['.modal-form .check-block .chk-area { margin: 10px 0px 0 0;}']
})
export class AccountComponent implements OnInit {
  account:Account;
  message: string = null;
  
  constructor(
    private service:AccountService,
    private route:ActivatedRoute,
    private location:Location) {
  }

  ngOnInit(): void {
    this.account = this.route.snapshot.data['account'];
  }
  
  cancel(): void {
	if(this.account.user.newInstance === true) {
      this.location.back();		
	}
	else {
      this.service.unlock(this.account.user.oid).then(response => {
        this.location.back();
      })
      .catch(( err: Response ) => {
        this.error( err.json() );
      } );
	}
  } 
  
  onSubmit(): void {
    let roleIds:string[] = [];
  
    for(let i = 0; i < this.account.groups.length; i++) {
      let group = this.account.groups[i];
      
      roleIds.push(group.assigned);
//      for(let j = 0; j < group.roles.length; j++) {
//        let role = group.roles[j];
//        
//        if(role.assigned) {
//          roleIds.push(role.roleId);
//        }      
//      }    
    }
    
    if(!this.account.changePassword && !this.account.user.newInstance) {
      delete this.account.user.password;
    }
    
    this.service.apply(this.account.user, roleIds).then(response => {
      this.location.back();
    })
    .catch(( err: Response ) => {
      this.error( err.json() );
    } );
  }
  
  error( err: any ): void {
    // Handle error
    if ( err !== null ) {
      this.message = ( err.localizedMessage || err.message );
    }
  }
}