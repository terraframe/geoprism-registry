import { NgModule, Injectable, Inject } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { HierarchyComponent } from './registry/component/hierarchy/hierarchy.component';
import { LocalizationManagerComponent } from './registry/component/localization-manager/localization-manager.component';
import { ShapefileComponent } from './registry/component/importer/shapefile.component';
import { SpreadsheetComponent } from './registry/component/importer/spreadsheet.component';
import { DataExportComponent } from './registry/component/data-export/data-export.component';
import { SubmitChangeRequestComponent } from './registry/component/submit-change-request/submit-change-request.component';
import { MasterListManagerComponent } from './registry/component/master-list/master-list-manager.component';
import { MasterListComponent } from './registry/component/master-list/master-list.component';
import { DataPageComponent } from './registry/component/data-page/data-page.component';
import { ChangeRequestPageComponent } from './registry/component/change-request-page/change-request-page.component';

import { LoginComponent } from './core/component/login/login.component'
import { HubComponent } from './core/component/hub/hub.component';
import { ForgotPasswordComponent } from './core/component/forgotpassword/forgotpassword.component'
import { ForgotPasswordCompleteComponent } from './core/component/forgotpassword-complete/forgotpassword-complete.component'

import { AuthGuard, AdminGuard, MaintainerGuard, ContributerGuard } from './shared/service/guard.service';
import { PendingChangesGuard } from "./shared/service/pending-changes-guard";

const routes: Routes = [
    {
        path: '',
        redirectTo: '/menu',
        pathMatch: 'full'
    },
    {
        path: 'login',
        component: LoginComponent,
        data: { title: 'login.title' }
    },
    {
        path: 'menu',
        component: HubComponent,
        canActivate: [AuthGuard],
        data: { title: 'login.header' }
    },
    {
        path: 'menu/:value',
        component: HubComponent,
        canActivate: [AuthGuard],
        data: { title: 'login.header' }
    },
    {
        path: 'forgotpassword',
        component: ForgotPasswordComponent,
        data: { title: 'useraccounts.title' }
    },
    {
        path: 'forgotpassword-complete/:token',
        component: ForgotPasswordCompleteComponent
    },
    {
        path: 'admin',
        loadChildren: "./admin/admin.module#AdminModule"
    },
    {
        path: 'registry',
        loadChildren: "./registry/registry.module#RegistryModule"
    }
];

@NgModule( {
    imports: [RouterModule.forRoot( routes )],
    exports: [RouterModule],
    providers: [
        { provide: LocationStrategy, useClass: HashLocationStrategy },
        AdminGuard,
        MaintainerGuard,
        ContributerGuard
    ]
} )
export class CgrAppRoutingModule { }

export const routedComponents: any = [LoginComponent, HubComponent, ForgotPasswordComponent, ForgotPasswordCompleteComponent];
