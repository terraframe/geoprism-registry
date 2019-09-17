import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { TreeModule } from 'angular-tree-component';
import { ContextMenuModule } from 'ngx-contextmenu';
import { ModalModule } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { ButtonsModule } from 'ngx-bootstrap/buttons';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { CookieService } from 'ngx-cookie-service';
import { FileUploadModule } from 'ng2-file-upload/ng2-file-upload';
import { NgxPaginationModule } from 'ngx-pagination';
import { ProgressbarModule } from 'ngx-bootstrap/progressbar';
import { CollapseModule } from 'ngx-bootstrap/collapse';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { PasswordStrengthBarModule } from 'ng2-password-strength-bar';
import { CustomFormsModule } from 'ng2-validation'

import { CgrAppComponent } from './cgr-app.component';
import { CgrAppRoutingModule, routedComponents } from './cgr-app-routing.module';

import { LoginComponent } from './core/component/login/login.component';
import { LoginHeaderComponent } from './core/component/login/login-header.component';
import { HubComponent } from './core/component/hub/hub.component';
import { ForgotPasswordComponent } from './core/component/forgotpassword/forgotpassword.component';
import { ForgotPasswordCompleteComponent } from './core/component/forgotpassword-complete/forgotpassword-complete.component';

import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpErrorInterceptor } from './core/service/http-error.interceptor';

import { ForgotPasswordService } from './core/service/forgotpassword.service';
import { ForgotPasswordCompleteService } from './core/service/forgotpassword-complete.service';
import { HubService } from './core/service/hub.service';


import { SharedModule } from './shared/shared.module';
import { AdminModule } from './admin/admin.module';
import { RegistryModule } from './registry/registry.module';

import './rxjs-extensions';

@NgModule( {
    imports: [
        BrowserModule,
        FormsModule,
        HttpClientModule,
        CgrAppRoutingModule,
        ReactiveFormsModule,
        FileUploadModule,
        ModalModule.forRoot(),
        TreeModule.forRoot(),
        ContextMenuModule.forRoot(),
        BsDropdownModule.forRoot(),
        ButtonsModule.forRoot(),
        TypeaheadModule.forRoot(),
        ProgressbarModule.forRoot(),
        CollapseModule.forRoot(),
        NgxPaginationModule,
        BrowserAnimationsModule,
        PasswordStrengthBarModule,
        CustomFormsModule,
        SharedModule.forRoot(),
        AdminModule,
        RegistryModule
    ],
    declarations: [
        CgrAppComponent,
        LoginComponent,
        LoginHeaderComponent,
        HubComponent,
        ForgotPasswordComponent,
        ForgotPasswordCompleteComponent,

        // Routing components
        routedComponents
    ],
    providers: [
        {
            provide: HTTP_INTERCEPTORS,
            useClass: HttpErrorInterceptor,
            multi: true
        },
        ForgotPasswordService,
        ForgotPasswordCompleteService,
        HubService,
    ],
    exports: [
        CgrAppComponent,
    ],
    bootstrap: [CgrAppComponent],
    entryComponents: [
    ]
} )
export class CgrAppModule { }
