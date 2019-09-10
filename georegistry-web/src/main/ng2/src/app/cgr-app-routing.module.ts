import { NgModule, Injectable, Inject } from '@angular/core';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { Routes, RouterModule, Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { HierarchyComponent } from './data/component/hierarchy/hierarchy.component';
import { LocalizationManagerComponent } from './data/component/localization-manager/localization-manager.component';
import { ShapefileComponent } from './data/component/importer/shapefile.component';
import { SpreadsheetComponent } from './data/component/importer/spreadsheet.component';
import { DataExportComponent } from './data/component/data-export/data-export.component';
import { SubmitChangeRequestComponent } from './data/component/submit-change-request/submit-change-request.component';
import { MasterListManagerComponent } from './data/component/master-list/master-list-manager.component';
import { MasterListComponent } from './data/component/master-list/master-list.component';
import { DataPageComponent } from './data/component/data-page/data-page.component';
import { ChangeRequestPageComponent } from './data/component/change-request-page/change-request-page.component';

import { LoginComponent } from './core/component/login/login.component'
import { HubComponent } from './core/component/hub/hub.component';
import { ForgotPasswordComponent } from './core/component/forgotpassword/forgotpassword.component'
import { ForgotPasswordCompleteComponent } from './core/component/forgotpassword-complete/forgotpassword-complete.component'

import { AuthGuard, AdminGuard, MaintainerGuard, ContributerGuard } from './shared/service/guard.service';
import { PendingChangesGuard } from "./shared/service/pending-changes-guard";

// TODO : Can't dynamically load the Admin Module due to a chunking error
import { AccountsComponent } from './admin/component/account/accounts.component';
import { AccountInviteComponent } from './admin/component/account/account-invite.component';
import { AccountInviteCompleteComponent } from './admin/component/account/account-invite-complete.component';
import { AccountComponent} from './admin/component/account/account.component';
import { SystemLogoComponent } from './admin/component/logo/system-logo.component';
import { SystemLogosComponent } from './admin/component/logo/system-logos.component';
import { EmailComponent } from './admin/component/email/email.component';


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
    //   resolve: {
    //     account: AccountResolver
    //   },        
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
