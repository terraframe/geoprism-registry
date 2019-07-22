import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule} from '@angular/forms';
import { HttpModule, XHRBackend, RequestOptions, Http} from '@angular/http';
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
import { NgxDatatableModule } from '@swimlane/ngx-datatable';


import './rxjs-extensions';

import { FillPipe } from './core/fill.pipe';
import { Safe } from './core/safe.html.pipe';
import { ErrorModalComponent } from './core/modals/error-modal.component';
import { SuccessModalComponent } from './core/modals/success-modal.component';
import { ConfirmModalComponent } from './core/modals/confirm-modal.component';
import { CgrAppRoutingModule, routedComponents } from './cgr-app-routing.module';
import { ErrorMessageComponent } from './core/message/error-message.component';
import { BooleanFieldComponent } from './core/form-fields/boolean-field/boolean-field.component';
import { ValidationComponent } from './core/form-fields/base/validation.component';
import { LocalizedInputComponent } from './core/form-fields/localized-input/localized-input.component';
import { LocalizedTextComponent } from './core/form-fields/localized-text/localized-text.component';

import { CgrAppComponent } from './cgr-app.component';
import { HierarchyComponent } from './data/hierarchy/hierarchy.component';
import { LocalizationManagerComponent } from './data/localization-manager/localization-manager.component';
import { CgrHeaderComponent } from './header.component';
import { CreateHierarchyTypeModalComponent } from './data/hierarchy/modals/create-hierarchy-type-modal.component';
import { AddChildToHierarchyModalComponent } from './data/hierarchy/modals/add-child-to-hierarchy-modal.component';
import { CreateGeoObjTypeModalComponent } from './data/hierarchy/modals/create-geoobjtype-modal.component';
import { ManageAttributesModalComponent } from './data/hierarchy/geoobjecttype-management/manage-attributes-modal.component';
import { DefineAttributeModalContentComponent } from './data/hierarchy/geoobjecttype-management/define-attribute-modal-content.component';
import { EditAttributeModalContentComponent } from './data/hierarchy/geoobjecttype-management/edit-attribute-modal-content.component';
import { ShapefileModalComponent } from './data/importer/modals/shapefile-modal.component';
import { AttributesPageComponent } from './data/importer/modals/attributes-page.component';
import { LocationPageComponent } from './data/importer/modals/location-page.component';
import { LocationProblemPageComponent } from './data/importer/modals/location-problem-page.component';
import { LocationProblemComponent } from './data/importer/modals/location-problem.component';
import { TermProblemPageComponent } from './data/importer/modals/term-problem-page.component';
import { TermProblemComponent } from './data/importer/modals/term-problem.component';
import { SpreadsheetModalComponent } from './data/importer/modals/spreadsheet-modal.component';
import { DataPageComponent } from './data/data-page/data-page.component';
import { LoadingBarComponent } from './loading-bar/loading-bar.component';
import { NewLocaleModalComponent } from './data/localization-manager/new-locale-modal.component';
import { TermOptionWidgetComponent } from './data/hierarchy/geoobjecttype-management/term-option-widget.component';
import { AttributeInputComponent } from './data/hierarchy/geoobjecttype-management/attribute-input.component';
import { EditTermOptionInputComponent } from './data/hierarchy/geoobjecttype-management/edit-term-option-input.component';
import { ManageTermOptionsComponent } from './data/hierarchy/geoobjecttype-management/manage-term-options.component';
import { GeoObjectTypeInputComponent } from './data/hierarchy/geoobjecttype-management/geoobjecttype-input.component';
import { ManageGeoObjectTypeModalComponent } from './data/hierarchy/modals/manage-geoobjecttype-modal.component';
import { MasterListManagerComponent } from './data/master-list/master-list-manager.component';
import { PublishModalComponent } from './data/master-list/publish-modal.component';
import { ExportFormatModalComponent } from './data/master-list/export-format-modal.component';

import { RequestTableComponent } from './data/crtable/request-table.component';
import { CreateUpdateGeoObjectDetailComponent } from './data/crtable/action-detail/create-update-geo-object/detail.component';
import { AddRemoveChildDetailComponent } from './data/crtable/action-detail/add-remove-child/detail.component';
import { GeoObjectSharedAttributeEditorComponent } from './data/geoobject-shared-attribute-editor/geoobject-shared-attribute-editor.component';
import { SubmitChangeRequestComponent } from './data/submit-change-request/submit-change-request.component';
import { ChangeRequestPageComponent } from './data/change-request-page/change-request-page.component';
import { GeoObjectEditorComponent } from './data/geoobject-editor/geoobject-editor.component';
import { GeoObjectEditorMapComponent } from './data/geoobject-editor-map/geoobject-editor-map.component';
import { ProgressBarComponent } from './progress-bar/progress-bar.component';
import { CascadingGeoSelector } from './data/cascading-geo-selector/cascading-geo-selector';
import { ActionDetailModalComponent } from './data/crtable/action-detail/action-detail-modal.component';


import { GeoObjectTypePipe } from './data/hierarchy/pipes/geoobjecttype.pipe';
import { GeoObjectAttributeExcludesPipe } from './data/geoobject-shared-attribute-editor/geoobject-attribute-excludes.pipe';
import { ToEpochDateTimePipe } from './data/submit-change-request/to-epoch-date-time.pipe';
import { DatePipe } from '@angular/common';

import { LocalizeComponent } from './core/localize/localize.component';
import { LocalizePipe } from './core/localize/localize.pipe';
import { LocalizationService } from './core/service/localization.service';
import { ModalStepIndicatorComponent } from './core/modals/modal-step-indicator.component';

import { RegistryService } from './service/registry.service';
import { HierarchyService } from './service/hierarchy.service';
import { LocalizationManagerService } from './service/localization-manager.service';
import { ChangeRequestService } from './service/change-request.service';
import { ProgressService } from './service/progress.service';
import { IOService } from './service/io.service';
import { EventService } from './event/event.service';
import { AuthService } from './core/auth/auth.service';
import { GeoObjectTypeManagementService } from './service/geoobjecttype-management.service'
import { PendingChangesGuard } from './core/pending-changes-guard';

import { GeoObjectAttributeCodeValidator } from './factory/form-validation.factory';

import { OnlyNumber } from './core/number-only.directive';

import './rxjs-extensions';
import { ModalStepIndicatorService } from './core/service/modal-step-indicator.service';

@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    CgrAppRoutingModule,
    NgxDatatableModule,
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
    BrowserAnimationsModule
  ],
  declarations: [
    CgrAppComponent,
    HierarchyComponent,
    LocalizationManagerComponent,
    RequestTableComponent,
    CreateUpdateGeoObjectDetailComponent,
    AddRemoveChildDetailComponent,
    FillPipe,
    Safe,
    CgrHeaderComponent,
    CreateHierarchyTypeModalComponent,
    AddChildToHierarchyModalComponent,
    CreateGeoObjTypeModalComponent,
    ConfirmModalComponent,
    ManageAttributesModalComponent,
    DefineAttributeModalContentComponent,
	  ErrorModalComponent, 
	  SuccessModalComponent,
    ShapefileModalComponent,
    AttributesPageComponent,
    LocationPageComponent,
    LocationProblemPageComponent,
    LocationProblemComponent,
    TermProblemPageComponent,
    TermProblemComponent,
    SpreadsheetModalComponent,
    LoadingBarComponent,
    GeoObjectTypePipe,
    GeoObjectAttributeCodeValidator,
    NewLocaleModalComponent,
    LocalizeComponent,
    LocalizePipe,
    EditAttributeModalContentComponent,
    TermOptionWidgetComponent,
    AttributeInputComponent,
    EditTermOptionInputComponent,
    ManageGeoObjectTypeModalComponent,
    GeoObjectTypeInputComponent,
    ModalStepIndicatorComponent,
    ManageTermOptionsComponent,
    OnlyNumber,
    ErrorMessageComponent,
    BooleanFieldComponent,
    ValidationComponent,
    LocalizedInputComponent,    
    LocalizedTextComponent,    
    GeoObjectSharedAttributeEditorComponent,
    SubmitChangeRequestComponent,
    GeoObjectEditorComponent,
    GeoObjectAttributeExcludesPipe,
    ToEpochDateTimePipe,
    ProgressBarComponent,
	GeoObjectEditorMapComponent,
	DataPageComponent,
	ChangeRequestPageComponent,
	CascadingGeoSelector,
	ActionDetailModalComponent,
    
    // Master List screens
    MasterListManagerComponent,
    PublishModalComponent,
    ExportFormatModalComponent,
    
    // Routing components
    routedComponents
  ],
  providers: [
    HierarchyService,
    LocalizationManagerService,
    ChangeRequestService,
    IOService,
    EventService,
    Safe,
    AuthService,
    CookieService,
    LocalizationService,
    ModalStepIndicatorService,
    GeoObjectTypeManagementService,
    RegistryService,
    ProgressService,
    DatePipe,
    ToEpochDateTimePipe,
    PendingChangesGuard
  ],
  bootstrap: [CgrAppComponent],
  entryComponents: [
	  ErrorModalComponent, 
	  SuccessModalComponent,
      AddChildToHierarchyModalComponent, 
      CreateGeoObjTypeModalComponent, 
      ManageAttributesModalComponent, 
      DefineAttributeModalContentComponent,
      EditAttributeModalContentComponent,
      CreateHierarchyTypeModalComponent, 
      ConfirmModalComponent, 
      LoadingBarComponent,
      ShapefileModalComponent, 
      SpreadsheetModalComponent,
      NewLocaleModalComponent,
      TermOptionWidgetComponent,
      AttributeInputComponent,
      EditTermOptionInputComponent,
      ManageGeoObjectTypeModalComponent,
      GeoObjectTypeInputComponent,
      ModalStepIndicatorComponent,
      ManageTermOptionsComponent,
      ErrorMessageComponent,
      GeoObjectSharedAttributeEditorComponent,
      SubmitChangeRequestComponent,
      GeoObjectEditorComponent,
      PublishModalComponent,
	  ExportFormatModalComponent,
	  DataPageComponent,
	  ChangeRequestPageComponent,
	  ActionDetailModalComponent
  ]        
})
export class CgrAppModule { }
