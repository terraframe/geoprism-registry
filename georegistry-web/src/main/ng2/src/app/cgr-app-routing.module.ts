import { NgModule, Injectable, Inject } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { HierarchyComponent } from './data/hierarchy/hierarchy.component';
import { LocalizationManagerComponent } from './data/localization-manager/localization-manager.component';
import { ShapefileComponent } from './data/importer/shapefile.component';
import { SpreadsheetComponent } from './data/importer/spreadsheet.component';
import { DataExportComponent } from './data/data-export/data-export.component';
import { SubmitChangeRequestComponent } from './data/submit-change-request/submit-change-request.component';
import { RegistryViewerComponent } from './data/crtable/registry-viewer.component';
import { MasterListManagerComponent } from './data/master-list/master-list-manager.component';
import { MasterListComponent } from './data/master-list/master-list.component';

import { AdminGuard, MaintainerGuard, ContributerGuard } from './core/auth/admin.guard';


const routes: Routes = [
    {
        path: 'hierarchies',
        component: HierarchyComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: 'lists',
        redirectTo: '/master-lists',
        canActivate: [ContributerGuard]
    },
    {
        path: 'requests',
        redirectTo: '/change-request',
        canActivate: [MaintainerGuard]
    },
    {
        path: 'uploads',
        redirectTo: '/shapefile',
        canActivate: [MaintainerGuard]
    },
    {
        path: 'shapefile',
        component: ShapefileComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: 'spreadsheet',
        component: SpreadsheetComponent,
        canActivate: [MaintainerGuard]
    },
    {
        path: 'export',
        component: DataExportComponent
    },
    {
        path: 'localization-manager',
        component: LocalizationManagerComponent,
        canActivate: [AdminGuard]
    },
    {
        path: 'crtable',
        component: RegistryViewerComponent,
        canActivate: [MaintainerGuard]
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
        path: 'change-request',
        component: SubmitChangeRequestComponent,
        canActivate: [ContributerGuard]
    },
    {
        path: '',
        redirectTo: '/',
        pathMatch: 'full'
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

export const routedComponents: any = [HierarchyComponent, ShapefileComponent, SpreadsheetComponent, DataExportComponent, RegistryViewerComponent, MasterListComponent];
