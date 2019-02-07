import { NgModule, Injectable, Inject } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { HierarchyComponent } from './data/hierarchy/hierarchy.component';
import { LocalizationManagerComponent } from './data/localization-manager/localization-manager.component';
import { ShapefileComponent } from './data/importer/shapefile.component';
import { SpreadsheetComponent } from './data/importer/spreadsheet.component';

import { AdminGuard } from './core/auth/admin.guard';


const routes: Routes = [
    {
        path: 'hierarchies',
        component: HierarchyComponent
    },
    {
        path: 'shapefile',
        component: ShapefileComponent
    },
    {
        path: 'spreadsheet',
        component: SpreadsheetComponent
    },
    {
        path: '',
        redirectTo: '/hierarchies',
        pathMatch: 'full'
    },
    {
        path: 'localization-manager',
        component: LocalizationManagerComponent,
        canActivate: [ AdminGuard ],          
    }
];

@NgModule( {
    imports: [RouterModule.forRoot( routes )],
    exports: [RouterModule],
    providers: [{ provide: LocationStrategy, useClass: HashLocationStrategy }]
} )
export class CgrAppRoutingModule { }

export const routedComponents: any = [HierarchyComponent, ShapefileComponent, SpreadsheetComponent];
