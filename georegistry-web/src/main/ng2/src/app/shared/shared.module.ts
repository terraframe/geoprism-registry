import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
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

import { ErrorModalComponent } from './component/modals/error-modal.component';
import { SuccessModalComponent } from './component/modals/success-modal.component';
import { ConfirmModalComponent } from './component/modals/confirm-modal.component';
import { ErrorMessageComponent } from './component/message/error-message.component';
import { MessageComponent } from './component/message/message.component';
import { ValidationComponent } from './component/form-fields/base/validation.component';
import { BooleanFieldComponent } from './component/form-fields/boolean-field/boolean-field.component';
import { LoadingBarComponent } from './component/loading-bar/loading-bar.component';

import { AdminGuard, MaintainerGuard, ContributerGuard, AuthGuard } from './service/guard.service';
import { PendingChangesGuard } from './service/pending-changes-guard';

import { AuthService } from './service/auth.service';
import { ProfileService } from './service/profile.service';
import { LocalizationService } from './service/localization.service';
import { ProgressService } from './service/progress.service';
import { EventService } from './service/event.service';
import { ModalStepIndicatorService } from './service/modal-step-indicator.service';
import { SessionService } from './service/session.service';
import { ForgotPasswordService } from './service/forgotpassword.service';
import { ForgotPasswordCompleteService } from './service/forgotpassword-complete.service';
import { HubService } from './service/hub.service';

import { LocalizePipe } from './pipe/localize.pipe';
import { PhonePipe } from './pipe/phone.pipe';

import { OnlyNumber } from './directive/number-only.directive';

import { CgrHeaderComponent } from './component/header/header.component';
import { ProgressBarComponent } from './component/progress-bar/progress-bar.component';
import { LocalizeComponent } from './component/localize/localize.component';
import { ModalStepIndicatorComponent } from './component/modals/modal-step-indicator.component';
import { ProfileComponent } from './component/profile/profile.component';

import '../rxjs-extensions';

@NgModule( {
    imports: [
        CommonModule,
        RouterModule,
        FormsModule,
        ReactiveFormsModule,                
        FileUploadModule,
        ModalModule,
        TreeModule,
        ContextMenuModule,
        BsDropdownModule,
        ButtonsModule,
        TypeaheadModule,
        ProgressbarModule,
        CollapseModule,
        NgxPaginationModule,
        BrowserAnimationsModule,
        PasswordStrengthBarModule,
        CustomFormsModule
    ],
    declarations: [
        LocalizeComponent,
        BooleanFieldComponent,
        LoadingBarComponent,
        ErrorMessageComponent,
        MessageComponent,
        ConfirmModalComponent,
        ErrorModalComponent,
        SuccessModalComponent,
        OnlyNumber,
        ValidationComponent,
        ProgressBarComponent,
        ModalStepIndicatorComponent,
        CgrHeaderComponent,
        ProfileComponent,        
        LocalizePipe,
        PhonePipe
    ],
    providers: [
        CookieService,
        AuthService,
        SessionService,
        ProfileService,
        ForgotPasswordService,
        ForgotPasswordCompleteService,
        HubService,
        LocalizationService,
        ModalStepIndicatorService,
        EventService,
        ProgressService,
        AdminGuard,
        MaintainerGuard,
        ContributerGuard,
        AuthGuard,
        PendingChangesGuard
    ],
    exports: [
        LocalizeComponent,
        BooleanFieldComponent,
        LoadingBarComponent,
        ErrorMessageComponent,
        MessageComponent,
        ConfirmModalComponent,
        ErrorModalComponent,
        SuccessModalComponent,
        OnlyNumber,
        ValidationComponent,
        ProgressBarComponent,
        ModalStepIndicatorComponent,
        CgrHeaderComponent,
        ProfileComponent,
        LocalizePipe,
        PhonePipe,
        CommonModule,
        FormsModule, 
        ReactiveFormsModule
    ],
    entryComponents: [
        ErrorModalComponent,
        SuccessModalComponent,
        ConfirmModalComponent,
        ModalStepIndicatorComponent,
        ErrorMessageComponent,
        LoadingBarComponent,
        ProfileComponent
    ]
} )
export class SharedModule { }
