import { NgModule, Injectable, Inject } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { HierarchyComponent } from './component/hierarchy/hierarchy.component';
import { LocalizationManagerComponent } from './component/localization-manager/localization-manager.component';
import { ShapefileComponent } from './component/importer/shapefile.component';
import { SpreadsheetComponent } from './component/importer/spreadsheet.component';
import { DataExportComponent } from './component/data-export/data-export.component';
import { SubmitChangeRequestComponent } from './component/submit-change-request/submit-change-request.component';
import { MasterListManagerComponent } from './component/master-list/master-list-manager.component';
import { MasterListComponent } from './component/master-list/master-list.component';
import { MasterListHistoryComponent } from './component/master-list/master-list-history.component';
import { DataPageComponent } from './component/data-page/data-page.component';
import { ChangeRequestPageComponent } from './component/change-request-page/change-request-page.component';

import { AuthGuard, AdminGuard, MaintainerGuard, ContributerGuard } from '../shared/service/guard.service';
import { PendingChangesGuard } from "../shared/service/pending-changes-guard";

const routes: Routes = [
    {
        path: '',
        component: HierarchyComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: 'hierarchies',
        component: HierarchyComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: 'data',
        component: DataPageComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: 'change-requests',
        component: ChangeRequestPageComponent,
        canActivate: [ContributerGuard]
    },
    {
        path: 'localization-manager',
        component: LocalizationManagerComponent,
        canActivate: [AdminGuard]
    },
    {
        path: 'master-lists',
        component: MasterListManagerComponent,
        canActivate: [ContributerGuard]
    },
    {
        path: 'master-list/:oid',
        component: MasterListComponent,
        canActivate: [ContributerGuard]
    },
    {
        path: 'master-list-history/:oid',
        component: MasterListHistoryComponent,
        canActivate: [ContributerGuard]
    },
    {
        path: 'change-request',
        component: SubmitChangeRequestComponent,
        canActivate: [ContributerGuard]
    }
];

@NgModule( {
    imports: [RouterModule.forChild( routes )],
    exports: [RouterModule],
    providers: [
        { provide: LocationStrategy, useClass: HashLocationStrategy },
    ]
} )
export class RegistryRoutingModule { }