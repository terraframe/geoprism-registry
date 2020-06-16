///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { FileUploadModule } from 'ng2-file-upload';
import { NgxPaginationModule } from 'ngx-pagination';

import { SystemLogoService } from './service/system-logo.service';
import { EmailService } from './service/email.service';
import { AccountService } from './service/account.service';
import { SettingsService } from './service/settings.service';
import { LocalizationManagerService } from './service/localization-manager.service';


import { AccountsComponent } from './component/account/accounts.component';
import { RoleManagementComponent } from './component/account/role-management.component';
import { AccountInviteComponent } from './component/account/account-invite.component';
import { AccountInviteCompleteComponent } from './component/account/account-invite-complete.component';
import { AccountComponent } from './component/account/account.component';
import { SystemLogoComponent } from './component/logo/system-logo.component';
import { SystemLogosComponent } from './component/logo/system-logos.component';
import { EmailComponent } from './component/email/email.component';
import { SettingsComponent } from './component/settings.component';
import { OrganizationModalComponent } from './component/organization/organization-modal.component';
import { NewLocaleModalComponent } from './component/localization-manager/new-locale-modal.component'


import { AdminRoutingModule } from './admin-routing.module';

import { SharedModule } from '../shared/shared.module';

import '../rxjs-extensions';

@NgModule( {
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        FileUploadModule,
        NgxPaginationModule,
        SharedModule,
        AdminRoutingModule
    ],
    declarations: [
        // Global components
        SystemLogoComponent,
        SystemLogosComponent,
        AccountsComponent,
        AccountInviteCompleteComponent,
        AccountInviteComponent,
        AccountComponent,
        SystemLogoComponent,
        SystemLogosComponent,
        EmailComponent,
        SettingsComponent,
        OrganizationModalComponent,
        NewLocaleModalComponent,
        RoleManagementComponent
    ],
    exports: [
        SystemLogoComponent,
        SystemLogosComponent
    ],
    providers: [
        SystemLogoService,
        EmailService,
        AccountService,
        SettingsService,
        LocalizationManagerService
    ],
    entryComponents: [
        AccountInviteComponent,
        AccountComponent,
        OrganizationModalComponent,
        NewLocaleModalComponent,
        RoleManagementComponent
    ]
} )
export class AdminModule { }
