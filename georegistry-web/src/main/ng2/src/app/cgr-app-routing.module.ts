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
import { MasterListManagerComponent } from './data/master-list/master-list-manager.component';
import { MasterListComponent } from './data/master-list/master-list.component';
import { DataPageComponent } from './data/data-page/data-page.component';
import { ChangeRequestPageComponent } from './data/change-request-page/change-request-page.component';

import { LoginComponent } from './core/login/login.component'
import { ForgotPasswordComponent } from './forgotpassword/forgotpassword.component'
import { ForgotPasswordCompleteComponent } from './forgotpassword-complete/forgotpassword-complete.component'
import { AuthGuard } from './core/auth/auth.guard';

import { AdminGuard, MaintainerGuard, ContributerGuard } from './core/auth/admin.guard';

import { PendingChangesGuard } from "./core/pending-changes-guard";

// TODO : Can't dynamically load the Admin Module due to a chunking error
import { AccountsComponent } from './admin/account/accounts.component';
import { AccountInviteComponent } from './admin/account/account-invite.component';
import { AccountInviteCompleteComponent } from './admin/account/account-invite-complete.component';
import { AccountComponent, AccountResolver } from './admin/account/account.component';
import { SystemLogoComponent } from './admin/logo/system-logo.component';
import { SystemLogosComponent } from './admin/logo/system-logos.component';
import { EmailComponent } from './admin/email/email.component';
import { HubComponent } from './hub/hub.component';
import { SystemInfoComponent } from './admin/system/system-info.component';


const routes: Routes = [
    {
      path: '',
      canActivate: [ AuthGuard ],
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
      canActivate: [ AuthGuard ],
      data: { title: 'login.header' }    
    },
    {
      path: 'menu/:value',
      component: HubComponent,
      canActivate: [ AuthGuard ],
      data: { title: 'login.header' }    
    },
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
          path: 'data',
          component: DataPageComponent,
          canActivate: [ContributerGuard]
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
        path: 'change-request',
        component: SubmitChangeRequestComponent,
        canActivate: [ContributerGuard]
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
      canActivate: [ AuthGuard ],
      redirectTo: 'admin/accounts',
      pathMatch: 'full'
    },
    {
      path: 'admin/logos',
      canActivate: [ AuthGuard ],
      component: SystemLogosComponent,
      data: { title: 'System_Configuration' }                
    },
    {
      path: 'admin/logo/:oid',
      canActivate: [ AuthGuard ],
      component: SystemLogoComponent,
      data: { title: 'System_Configuration' }            
      
    },
    {
      path: 'admin/email',
      canActivate: [ AuthGuard ],
      component: EmailComponent,
      data: { title: 'System_Configuration' }            
      
    },
  	{
      path: 'admin/accounts',
      canActivate: [ AuthGuard ],
      component: AccountsComponent,    
      data: { title: 'useraccounts.title' }                
    },
    {
      path: 'admin/account/:oid',
      component: AccountComponent,
      resolve: {
        account: AccountResolver
      },        
      canActivate: [ AuthGuard ],
      data: { title: 'account.title' }
    },
    {
      path: 'admin/invite',
      component: AccountInviteComponent,
      data: { title: 'account.title' }
    },
    {
      path: 'admin/invite-complete/:token',
      component: AccountInviteCompleteComponent,
      data: { title: 'account.title' }
    },
    {
      path: 'admin/system-info',
      component: SystemInfoComponent,
      data: { }
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

export const routedComponents: any = [LoginComponent, HubComponent, ForgotPasswordComponent, ForgotPasswordCompleteComponent, HierarchyComponent, ShapefileComponent, SpreadsheetComponent, DataExportComponent, MasterListComponent];
