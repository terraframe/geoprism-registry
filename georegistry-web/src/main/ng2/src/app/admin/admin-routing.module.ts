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

import { NgModule, Injectable, Inject } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { EmailComponent } from './email/email.component';
import { SystemLogoComponent } from './logo/system-logo.component';
import { SystemLogosComponent } from './logo/system-logos.component';
import { AccountsComponent } from './account/accounts.component';
import { AccountComponent, AccountResolver } from './account/account.component';

import { AccountInviteComponent } from './account/account-invite.component';
import { AccountInviteCompleteComponent } from './account/account-invite-complete.component';

declare var acp: any;

const routes: Routes = [
  {
    path: '',
    component: AccountsComponent,   
    data: { title: 'useraccounts.title' }            
  },	
  {
    path: 'logos',
    component: SystemLogosComponent,
    data: { title: 'System_Configuration' }                
  },
  {
    path: 'logo/:oid',
    component: SystemLogoComponent,
    data: { title: 'System_Configuration' }            
    
  },
  {
    path: 'email',
    component: EmailComponent,
    data: { title: 'System_Configuration' }            
    
  },
  {
    path: 'accounts',
    component: AccountsComponent,    
    data: { title: 'useraccounts.title' }                
  },
//  {
//    path: 'geotree',
//    component: GeoTreeComponent,
//    data: { title: 'geoEntity.title' }                
//  },
//  {
//    path: 'universaltree',
//    component: UniversalTreeComponent,
//    data: { title: 'universal.title' }                
//  },
//  {
//    path: 'classifiertree',
//    component: ClassifierTreeComponent,
//    data: { title: 'Term_Ontology_Admin' }                
//  },
//  {
//    path: 'browser',
//    component: BrowserComponent,
//    data: { title: 'Data_Browser' }                
//  },
  {
    path: 'account/:oid',
    component: AccountComponent,
    resolve: {
      account: AccountResolver
    },        
    data: { title: 'account.title' }
  },
  {
    path: 'invite',
    component: AccountInviteComponent,    
    data: { }
  },
  {
    path: 'invite-complete',
    component: AccountInviteCompleteComponent,    
    data: { }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
  providers: [{provide: LocationStrategy, useClass: HashLocationStrategy}, AccountResolver]
})
export class AdminRoutingModule { }

export const routedComponents = [SystemLogosComponent, SystemLogoComponent, EmailComponent, AccountsComponent, AccountComponent, AccountInviteComponent, AccountInviteCompleteComponent];
