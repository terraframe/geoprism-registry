import { NgModule, Injectable, Inject } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { HierarchyComponent } from './data/hierarchy/hierarchy.component';
import { LocalizationManagerComponent } from './data/localization-manager/localization-manager.component';


const routes: Routes = [
  {
	path: 'hierarchies',
	component: HierarchyComponent
  },
  {
	path: '',
	redirectTo: '/hierarchies',
	pathMatch: 'full'
  },
  {
  path: 'localization-manager',
  component: LocalizationManagerComponent
  }
//  {
//    path: 'map/:id/:simple',
//    component: MapComponent,
//  },
//  {
//    path: 'map/:id/:simple/:props',
//    component: MapComponent,
//  },
//  {
//	  path: 'map/:id',
//	  component: MapComponent,
//  },
//  {
//    path: 'maps',
//    component: MapsComponent
//  },
//  {
//	path: 'legend/:layerId',
//	component: MapsComponent
//  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
  providers: [{provide: LocationStrategy, useClass: HashLocationStrategy}]
})
export class CgrAppRoutingModule { }

export const routedComponents:any = [];
