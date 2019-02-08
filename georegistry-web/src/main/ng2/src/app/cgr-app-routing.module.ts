import { NgModule, Injectable, Inject } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { HierarchyComponent } from './data/hierarchy/hierarchy.component';
import { LocalizationManagerComponent } from './data/localization-manager/localization-manager.component';
import { ShapefileComponent } from './data/importer/shapefile.component';
import { SpreadsheetComponent } from './data/importer/spreadsheet.component';
import { ChangeRequestTableComponent } from './data/crtable/crtable.component';
import { DataExportComponent } from './data/data-export/data-export.component';

import { AdminGuard, MaintainerGuard } from './core/auth/admin.guard';


const routes: Routes = [
    {
        path: 'hierarchies',
        component: HierarchyComponent,
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
        component: ChangeRequestTableComponent,
        canActivate: [AdminGuard]
    },
    {
        path: '',
        redirectTo: '/hierarchies',
        pathMatch: 'full'
    }
];

@NgModule( {
    imports: [RouterModule.forRoot( routes )],
    exports: [RouterModule],
    providers: [
        { provide: LocationStrategy, useClass: HashLocationStrategy },
        AdminGuard,
        MaintainerGuard
    ]
} )
export class CgrAppRoutingModule { }

export const routedComponents: any = [HierarchyComponent, ShapefileComponent, SpreadsheetComponent, DataExportComponent];
