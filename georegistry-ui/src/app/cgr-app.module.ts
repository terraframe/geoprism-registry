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

import { APP_INITIALIZER, NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { HttpClientModule, HTTP_INTERCEPTORS } from "@angular/common/http";
import { TreeModule } from "@circlon/angular-tree-component";
import { BsDropdownModule } from "ngx-bootstrap/dropdown";
import { ButtonsModule } from "ngx-bootstrap/buttons";
import { TypeaheadModule } from "ngx-bootstrap/typeahead";
import { FileUploadModule } from "ng2-file-upload";
import { NgxPaginationModule } from "ngx-pagination";
import { ProgressbarModule } from "ngx-bootstrap/progressbar";
import { CollapseModule } from "ngx-bootstrap/collapse";
import { TabsModule } from "ngx-bootstrap/tabs";
import { BsDatepickerModule } from "ngx-bootstrap/datepicker";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";

import { CgrAppComponent } from "./cgr-app.component";
import { CgrAppRoutingModule, routedComponents } from "./cgr-app-routing.module";

import { LoginComponent } from "@core/component/login/login.component";
import { LoginHeaderComponent } from "@core/component/login/login-header.component";
import { HubComponent } from "@core/component/hub/hub.component";
import { ForgotPasswordComponent } from "@core/component/forgotpassword/forgotpassword.component";
import { ForgotPasswordCompleteComponent } from "@core/component/forgotpassword-complete/forgotpassword-complete.component";

import { HttpErrorInterceptor } from "@core/service/http-error.interceptor";

import { ForgotPasswordService } from "@core/service/forgotpassword.service";
import { PasswordStrengthBarComponent } from "@shared/component/password-strength-bar/password-strength-bar.component";
import { HubService } from "@core/service/hub.service";

import { SharedModule } from "@shared/shared.module";
import { ConfigurationService } from "@core/service/configuration.service";
import { APP_BASE_HREF, PlatformLocation } from "@angular/common";
import { PhetsarathFontComponent } from "@core/component/phetsarath-font/phetsarath-font.component";

@NgModule({
    imports: [
        BrowserModule,
        FormsModule,
        HttpClientModule,
        CgrAppRoutingModule,
        ReactiveFormsModule,
        FileUploadModule,
        //        ModalModule.forRoot(),
        TreeModule,
        BsDropdownModule.forRoot(),
        ButtonsModule.forRoot(),
        TypeaheadModule.forRoot(),
        ProgressbarModule.forRoot(),
        CollapseModule.forRoot(),
        TabsModule.forRoot(),
        NgxPaginationModule,
        BrowserAnimationsModule,
        SharedModule.forRoot(),
        BsDatepickerModule.forRoot()
    ],
    declarations: [
        CgrAppComponent,
        LoginComponent,
        LoginHeaderComponent,
        HubComponent,
        ForgotPasswordComponent,
        ForgotPasswordCompleteComponent,
        PhetsarathFontComponent,

        // Routing components
        routedComponents
    ],
    providers: [
        {
            provide: APP_BASE_HREF,
            useFactory: (s: PlatformLocation) => s.getBaseHrefFromDOM(),
            deps: [PlatformLocation]
        },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: HttpErrorInterceptor,
            multi: true
        },
        ForgotPasswordService,
        PasswordStrengthBarComponent,
        HubService,
        ConfigurationService,
        {
            'provide': APP_INITIALIZER,
            'useFactory': (service: ConfigurationService) => {
                // Do initing of services that is required before app loads
                // NOTE: this factory needs to return a function (that then returns a promise)
                return () => service.load()  // + any other services...
            },
            'deps': [ConfigurationService, HttpClientModule],
            'multi': true,
        },
    ],
    exports: [
        PhetsarathFontComponent,
        CgrAppComponent
    ],
    bootstrap: [CgrAppComponent],
    entryComponents: [
    ]
})
export class CgrAppModule { }
