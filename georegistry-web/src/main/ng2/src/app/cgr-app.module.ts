import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { HttpClientModule, HTTP_INTERCEPTORS } from "@angular/common/http";
import { TreeModule } from "@circlon/angular-tree-component";
import { ContextMenuModule } from "ngx-contextmenu";
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

import "./rxjs-extensions";

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
        ContextMenuModule.forRoot(),
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
        PasswordStrengthBarComponent,
        HubService
    ],
    exports: [
        CgrAppComponent
    ],
    bootstrap: [CgrAppComponent],
    entryComponents: [
    ]
})
export class CgrAppModule { }
