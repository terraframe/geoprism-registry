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

import { NgModule } from "@angular/core";
import { LocationStrategy, HashLocationStrategy } from "@angular/common";
import { Routes, RouterModule } from "@angular/router";

import { LoginComponent } from "./core/component/login/login.component";
import { HubComponent } from "./core/component/hub/hub.component";
import { ForgotPasswordComponent } from "./core/component/forgotpassword/forgotpassword.component";
import { ForgotPasswordCompleteComponent } from "./core/component/forgotpassword-complete/forgotpassword-complete.component";

import { AuthGuard, AdminGuard, MaintainerGuard, ContributerGuard } from "./shared/service/guard.service";

const routes: Routes = [
    {
        path: "",
        redirectTo: "/menu",
        pathMatch: "full"
    },
    {
        path: "login",
        component: LoginComponent,
        data: { title: "login.title" }
    },
    {
        path: "login/:errorMsg",
        component: LoginComponent,
        data: { title: "login.title" }
    },
    {
        path: "menu",
        component: HubComponent,
        canActivate: [AuthGuard],
        data: { title: "login.header" }
    },
    {
        path: "menu/:value",
        component: HubComponent,
        canActivate: [AuthGuard],
        data: { title: "login.header" }
    },
    {
        path: "forgotpassword",
        component: ForgotPasswordComponent,
        data: { title: "useraccounts.title" }
    },
    {
        path: "forgotpassword-complete/:token",
        component: ForgotPasswordCompleteComponent
    },
    {
        path: "admin",
        loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule)
    },
    {
        path: "registry",
        loadChildren: () => import('./registry/registry.module').then(m => m.RegistryModule)
    },
    {
        path: "**",
        redirectTo: "/menu"
    },

];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule],
    providers: [
        { provide: LocationStrategy, useClass: HashLocationStrategy },
        AdminGuard,
        MaintainerGuard,
        ContributerGuard
    ]
})
export class CgrAppRoutingModule { }

export const routedComponents: any = [LoginComponent, HubComponent, ForgotPasswordComponent, ForgotPasswordCompleteComponent];
