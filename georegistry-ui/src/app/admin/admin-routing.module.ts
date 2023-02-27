///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { NgModule, Injectable, Inject } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';

import { AuthGuard, AdminGuard, MaintainerGuard, ContributerGuard } from '../shared/service/guard.service';
import { PendingChangesGuard } from "../shared/service/pending-changes-guard";

import { AccountsComponent } from './component/account/accounts.component';
import { SettingsComponent } from './component/settings.component';
import { AccountInviteComponent } from './component/account/account-invite.component';
import { AccountInviteCompleteComponent } from './component/account/account-invite-complete.component';
import { AccountComponent } from './component/account/account.component';
import { SystemLogoComponent } from './component/logo/system-logo.component';
import { SystemLogosComponent } from './component/logo/system-logos.component';
import { EmailComponent } from './component/email/email.component';


const routes: Routes = [
    {
        path: 'logos',
        canActivate: [AuthGuard],
        component: SystemLogosComponent,
        data: { title: 'System_Configuration' }
    },
    {
        path: 'logo/:oid',
        canActivate: [AuthGuard],
        component: SystemLogoComponent,
        data: { title: 'System_Configuration' }

    },
    {
        path: 'email',
        canActivate: [AuthGuard],
        component: EmailComponent,
        data: { title: 'System_Configuration' }

    },
    {
        path: '',
        canActivate: [AuthGuard],
        component: AccountsComponent,
        data: { title: 'useraccounts.title' }
    },   
    {
        path: 'settings',
        canActivate: [AuthGuard],
        component: SettingsComponent,
        data: { title: 'settings.title' }
    }, 
    {
        path: 'accounts',
        canActivate: [AuthGuard],
        component: AccountsComponent,
        data: { title: 'useraccounts.title' }
    },
    {
        path: 'account/:oid',
        component: AccountComponent,
        //   resolve: {
        //     account: AccountResolver
        //   },        
        canActivate: [AuthGuard],
        data: { title: 'account.title' }
    },
    {
        path: 'invite',
        component: AccountInviteComponent,
        data: { title: 'account.title' }
    },
    {
        path: 'invite-complete/:token',
        component: AccountInviteCompleteComponent,
        data: { title: 'account.title' }
    }
];

@NgModule( {
    imports: [RouterModule.forChild( routes )],
    exports: [RouterModule],
    providers: [
        { provide: LocationStrategy, useClass: HashLocationStrategy },
    ]
} )
export class AdminRoutingModule { }
