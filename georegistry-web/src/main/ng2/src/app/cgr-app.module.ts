import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule} from '@angular/forms';
import { HttpModule, XHRBackend, RequestOptions, Http} from '@angular/http';
import { ModalModule } from 'ngx-bootstrap/modal';
import { TreeModule } from 'angular-tree-component';
import { ContextMenuModule } from 'ngx-contextmenu';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { CookieService } from 'ngx-cookie-service';
import { ButtonsModule } from 'ngx-bootstrap/buttons';
import { FileUploadModule } from 'ng2-file-upload/ng2-file-upload';

import './rxjs-extensions';

import { FillPipe } from './core/fill.pipe';
import { Safe } from './core/safe.html.pipe';
import { ErrorModalComponent } from './core/modals/error-modal.component';
import { ConfirmModalComponent } from './core/modals/confirm-modal.component';
import { CgrAppRoutingModule, routedComponents } from './cgr-app-routing.module';

import { CgrAppComponent } from './cgr-app.component';
import { HierarchyComponent } from './data/hierarchy/hierarchy.component';
import { LocalizationManagerComponent } from './data/localization-manager/localization-manager.component';
import { CgrHeaderComponent } from './header.component';
import { CreateModalComponent } from './data/hierarchy/modals/create-modal.component';
import { CreateChildModalComponent } from './data/hierarchy/modals/create-child-modal.component';
import { CreateGeoObjTypeModalComponent } from './data/hierarchy/modals/create-geoobjtype-modal.component';
import { ManageAttributesModalComponent } from './data/hierarchy/modals/manage-attributes-modal.component';
import { DefineAttributeModalContentComponent } from './data/hierarchy/modals/define-attribute-modal-content.component';
import { EditAttributeModalContentComponent } from './data/hierarchy/modals/edit-attribute-modal-content.component';
import { ShapefileModalComponent } from './data/importer/modals/shapefile-modal.component';
import { SpreadsheetModalComponent } from './data/importer/modals/spreadsheet-modal.component';
import { LoadingBarComponent } from './loading-bar/loading-bar.component';
import { NewLocaleModalComponent } from './data/localization-manager/new-locale-modal.component';
import { TermOptionInputComponent } from './data/hierarchy/form-inputs/term-option-input.component';
import { AttributeInputComponent } from './data/hierarchy/form-inputs/attribute-input.component';


import { GeoObjectTypePipe } from './data/hierarchy/pipes/geoobjecttype.pipe';

import { LocalizeComponent } from './core/localize/localize.component';
import { LocalizePipe } from './core/localize/localize.pipe';
import { LocalizationService } from './core/service/localization.service';

//import { UploadModalComponent } from './map/upload-modal/upload-modal.component';
import { HierarchyService } from './service/hierarchy.service';
import { LocalizationManagerService } from './service/localization-manager.service';
import { ShapefileService } from './service/shapefile.service';
import { ExcelService } from './service/excel.service';
import { EventService } from './event/event.service';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { GeoObjectAttributeCodeValidator } from './factory/form-validation.factory';


import './rxjs-extensions';

@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    CgrAppRoutingModule,
    ReactiveFormsModule,
    FileUploadModule,
    ModalModule.forRoot(),
    TreeModule.forRoot(),
    ContextMenuModule.forRoot(),
    BsDropdownModule.forRoot(),
    ButtonsModule.forRoot(),
    BrowserAnimationsModule
  ],
  declarations: [
    CgrAppComponent,
    HierarchyComponent,
    LocalizationManagerComponent,
    FillPipe,
    Safe,
    CgrHeaderComponent,
    CreateModalComponent,
    CreateChildModalComponent,
    CreateGeoObjTypeModalComponent,
    ConfirmModalComponent,
    ManageAttributesModalComponent,
    DefineAttributeModalContentComponent,
    ErrorModalComponent, 
    ShapefileModalComponent,
    SpreadsheetModalComponent,
    LoadingBarComponent,
    GeoObjectTypePipe,
    GeoObjectAttributeCodeValidator,
    NewLocaleModalComponent,
    LocalizeComponent,
    LocalizePipe,
    EditAttributeModalContentComponent,
    TermOptionInputComponent,
    AttributeInputComponent,
    
    // Routing components
    routedComponents
  ],
  providers: [
    HierarchyService,
    LocalizationManagerService,
    ShapefileService,
    ExcelService,
    EventService,
    Safe,
    CookieService,
    LocalizationService
  ],
  bootstrap: [CgrAppComponent],
  entryComponents: [
      ErrorModalComponent, 
      CreateChildModalComponent, 
      CreateGeoObjTypeModalComponent, 
      ManageAttributesModalComponent, 
      DefineAttributeModalContentComponent,
      EditAttributeModalContentComponent,
      CreateModalComponent, 
      ConfirmModalComponent, 
      LoadingBarComponent,
      ShapefileModalComponent, 
      SpreadsheetModalComponent,
      NewLocaleModalComponent,
      TermOptionInputComponent,
      AttributeInputComponent
  ]        
})
export class CgrAppModule { }
