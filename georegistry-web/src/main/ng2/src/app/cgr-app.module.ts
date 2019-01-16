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

import './rxjs-extensions';

import { FillPipe } from './core/fill.pipe';
import { Safe } from './core/safe.html.pipe';
import { CgrAppRoutingModule, routedComponents } from './cgr-app-routing.module';

import { CgrAppComponent } from './cgr-app.component';
import { HierarchyComponent } from './data/hierarchy/hierarchy.component';
import { CgrHeaderComponent } from './header.component';
import { ErrorModalComponent } from './data/hierarchy/modals/error-modal.component';
import { CreateModalComponent } from './data/hierarchy/modals/create-modal.component';
import { CreateChildModalComponent } from './data/hierarchy/modals/create-child-modal.component';
import { CreateGeoObjTypeModalComponent } from './data/hierarchy/modals/create-geoobjtype-modal.component';
import { ConfirmModalComponent } from './data/hierarchy/modals/confirm-modal.component';
import { ManageAttributesModalComponent } from './data/hierarchy/modals/manage-attributes-modal.component';
import { DefineAttributeModalContentComponent } from './data/hierarchy/modals/define-attribute-modal-content.component';
import { LoadingBarComponent } from './loading-bar/loading-bar.component';

import { GeoObjectTypePipe } from './data/hierarchy/pipes/geoobjecttype.pipe';

//import { UploadModalComponent } from './map/upload-modal/upload-modal.component';
import { HierarchyService } from './service/hierarchy.service';
import { GeoObjTypeModalService } from './service/geo-obj-type-modal.service';
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
    LoadingBarComponent,
    GeoObjectTypePipe,
    GeoObjectAttributeCodeValidator,
    
    // Routing components
    routedComponents
  ],
  providers: [
    HierarchyService,
    GeoObjTypeModalService,
    EventService,
    Safe,
    CookieService
  ],
  bootstrap: [CgrAppComponent],
  entryComponents: [
      ErrorModalComponent, 
      CreateChildModalComponent, 
      CreateGeoObjTypeModalComponent, 
      ManageAttributesModalComponent, 
      DefineAttributeModalContentComponent,
      CreateModalComponent, 
      ConfirmModalComponent, 
      LoadingBarComponent]        
})
export class CgrAppModule { }
