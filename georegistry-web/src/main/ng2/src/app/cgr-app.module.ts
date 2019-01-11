import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule} from '@angular/forms';
import { HttpModule, XHRBackend, RequestOptions, Http} from '@angular/http';
import { ModalModule } from 'ngx-bootstrap/modal';
import { TreeModule } from 'angular-tree-component';
import { ContextMenuModule } from 'ngx-contextmenu';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { CookieService } from 'ngx-cookie-service';
import { FileUploadModule } from 'ng2-file-upload/ng2-file-upload';

import './rxjs-extensions';

import { FillPipe } from './core/fill.pipe';
import { Safe } from './core/safe.html.pipe';
import { ErrorModalComponent } from './core/modals/error-modal.component';
import { ConfirmModalComponent } from './core/modals/confirm-modal.component';
import { CgrAppRoutingModule, routedComponents } from './cgr-app-routing.module';

import { CgrAppComponent } from './cgr-app.component';
import { CgrHeaderComponent } from './header.component';
import { CreateModalComponent } from './data/hierarchy/modals/create-modal.component';
import { CreateChildModalComponent } from './data/hierarchy/modals/create-child-modal.component';
import { CreateGeoObjTypeModalComponent } from './data/hierarchy/modals/create-geoobjtype-modal.component';
import { ShapefileModalComponent } from './data/importer/modals/shapefile-modal.component';
import { SpreadsheetModalComponent } from './data/importer/modals/spreadsheet-modal.component';
import { LoadingBarComponent } from './loading-bar/loading-bar.component';

import { GeoObjectTypePipe } from './data/hierarchy/pipes/geoobjecttype.pipe';

//import { UploadModalComponent } from './map/upload-modal/upload-modal.component';
import { HierarchyService } from './service/hierarchy.service';
import { ShapefileService } from './service/shapefile.service';

import { ExcelService } from './service/excel.service';
import { EventService } from './event/event.service';

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
    BsDropdownModule.forRoot()
  ],
  declarations: [
    CgrAppComponent,
    FillPipe,
    Safe,
    CgrHeaderComponent,
    CreateModalComponent,
    CreateChildModalComponent,
    CreateGeoObjTypeModalComponent,
    ConfirmModalComponent,
    ErrorModalComponent,
    ShapefileModalComponent,
    SpreadsheetModalComponent,
    LoadingBarComponent,
    GeoObjectTypePipe,
    
    // Routing components
    routedComponents
  ],
  providers: [
    HierarchyService,
    ShapefileService,
    ExcelService,
    EventService,
    Safe,
    CookieService
  ],
  bootstrap: [CgrAppComponent],
  entryComponents: [ErrorModalComponent, CreateChildModalComponent, CreateGeoObjTypeModalComponent, CreateModalComponent, ConfirmModalComponent, LoadingBarComponent, ShapefileModalComponent, SpreadsheetModalComponent]        
})
export class CgrAppModule { }
