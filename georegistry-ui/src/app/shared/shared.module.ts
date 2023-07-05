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

import { NgModule, ModuleWithProviders } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterModule } from "@angular/router";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { TreeModule } from "@circlon/angular-tree-component";
import { ModalModule } from "ngx-bootstrap/modal";
import { BsDropdownModule } from "ngx-bootstrap/dropdown";
import { ButtonsModule } from "ngx-bootstrap/buttons";
import { TypeaheadModule } from "ngx-bootstrap/typeahead";
import { CookieService } from "ngx-cookie-service";
import { FileUploadModule } from "ng2-file-upload";
import { NgxPaginationModule } from "ngx-pagination";
import { ProgressbarModule } from "ngx-bootstrap/progressbar";
import { CollapseModule } from "ngx-bootstrap/collapse";
import { BsDatepickerModule } from "ngx-bootstrap/datepicker";

import { TableModule } from "primeng/table";
import { SliderModule } from "primeng/slider";
import { DropdownModule } from "primeng/dropdown";
import { MultiSelectModule } from "primeng/multiselect";
import { ButtonModule } from "primeng/button";
import { AutoCompleteModule } from "primeng/autocomplete";

import { ErrorModalComponent } from "./component/modals/error-modal.component";
import { SuccessModalComponent } from "./component/modals/success-modal.component";
import { ConfirmModalComponent } from "./component/modals/confirm-modal.component";
import { GenericModalComponent } from "./component/modals/generic-modal.component";
import { ErrorMessageComponent } from "./component/message/error-message.component";
import { MessageComponent } from "./component/message/message.component";
import { BooleanFieldComponent } from "./component/form-fields/boolean-field/boolean-field.component";
import { LoadingBarComponent } from "./component/loading-bar/loading-bar.component";
import { PasswordStrengthBarComponent } from "./component/password-strength-bar/password-strength-bar.component";

import { AdminGuard, MaintainerGuard, ContributerGuard, AuthGuard } from "./service/guard.service";
import { PendingChangesGuard } from "./service/pending-changes-guard";

import { AuthService } from "./service/auth.service";
import { ProfileService } from "./service/profile.service";
import { OrganizationService } from "./service/organization.service";
import { ExternalSystemService } from "./service/external-system.service";
import { ProgressService } from "./service/progress.service";
import { EventService } from "./service/event.service";
import { ModalStepIndicatorService } from "./service/modal-step-indicator.service";
import { SessionService } from "./service/session.service";

import { LocalizePipe } from "./pipe/localize.pipe";
import { PhonePipe } from "./pipe/phone.pipe";

import { MustMatchDirective } from "./directive/must-match.directive";

import { CgrHeaderComponent } from "./component/header/header.component";
import { ProgressBarComponent } from "./component/progress-bar/progress-bar.component";
import { LocalizeComponent } from "./component/localize/localize.component";
import { ConvertKeyLabel } from "./component/localize/convert-key-label.component";
import { ModalStepIndicatorComponent } from "./component/modals/modal-step-indicator.component";
import { ProfileComponent } from "./component/profile/profile.component";
import { GenericTableComponent } from "./component/generic-table/generic-table.component";
import { DateTextComponent } from "./component/date-text/date-text.component";
import { DateService } from "./service/date.service";
import { DateFieldComponent } from "./component/form-fields/date-field/date-field.component";
import { LocalizationService } from "./service";

@NgModule({
    imports: [
        CommonModule,
        RouterModule,
        FormsModule,
        ReactiveFormsModule,
        FileUploadModule,
        ModalModule.forRoot(),
        TreeModule,
        BsDropdownModule,
        ButtonsModule,
        TypeaheadModule,
        ProgressbarModule,
        CollapseModule,
        NgxPaginationModule,
        BsDatepickerModule.forRoot(),
        TableModule,
        SliderModule,
        DropdownModule,
        MultiSelectModule,
        ButtonModule,
        AutoCompleteModule
    ],
    declarations: [
        LocalizeComponent,
        ConvertKeyLabel,
        BooleanFieldComponent,
        LoadingBarComponent,
        ErrorMessageComponent,
        MessageComponent,
        ConfirmModalComponent,
        GenericModalComponent,
        ErrorModalComponent,
        SuccessModalComponent,
        PasswordStrengthBarComponent,
        MustMatchDirective,
        ProgressBarComponent,
        ModalStepIndicatorComponent,
        CgrHeaderComponent,
        ProfileComponent,
        LocalizePipe,
        PhonePipe,
        GenericTableComponent,
        DateFieldComponent,
        DateTextComponent
    ],
    exports: [
        LocalizeComponent,
        ConvertKeyLabel,
        BooleanFieldComponent,
        LoadingBarComponent,
        ErrorMessageComponent,
        MessageComponent,
        ConfirmModalComponent,
        GenericModalComponent,
        ErrorModalComponent,
        SuccessModalComponent,
        PasswordStrengthBarComponent,
        MustMatchDirective,
        ProgressBarComponent,
        ModalStepIndicatorComponent,
        CgrHeaderComponent,
        ProfileComponent,
        LocalizePipe,
        PhonePipe,
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        GenericTableComponent,
        DateFieldComponent,
        DateTextComponent
    ],
    entryComponents: [
        ErrorModalComponent,
        SuccessModalComponent,
        ConfirmModalComponent,
        GenericModalComponent,
        ModalStepIndicatorComponent,
        ErrorMessageComponent,
        LoadingBarComponent,
        ProfileComponent
    ]
})
export class SharedModule {

    static forRoot(): ModuleWithProviders<SharedModule> {
        return {
            ngModule: SharedModule,
            providers: [
                LocalizationService,
                CookieService,
                AuthService,
                SessionService,
                ProfileService,
                OrganizationService,
                ExternalSystemService,
                ModalStepIndicatorService,
                EventService,
                ProgressService,
                AdminGuard,
                MaintainerGuard,
                ContributerGuard,
                AuthGuard,
                PendingChangesGuard,
                DateService
            ]
        };
    }

}
