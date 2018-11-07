import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule} from '@angular/forms';
import { HttpModule, XHRBackend, RequestOptions, Http} from '@angular/http';

import { FileUploadModule } from 'ng2-file-upload/ng2-file-upload';
import { ModalModule } from 'ngx-bootstrap/modal';

import { FillPipe } from './core/fill.pipe';
import { Safe } from './core/safe.html.pipe';
import { CgrAppRoutingModule, routedComponents } from './cgr-app-routing.module';

import { CgrAppComponent } from './cgr-app.component';

//import { UploadModalComponent } from './map/upload-modal/upload-modal.component';
import { HierarchyService } from './service/hierarchy.service';

import { TreeModule } from 'angular-tree-component';

import { CoreModule } from '@terraframe/core/core.module';

import './rxjs-extensions';

@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,    
    CgrAppRoutingModule,
    CoreModule,
    ReactiveFormsModule,
    FileUploadModule,
    ModalModule.forRoot()    
  ],
  declarations: [
    CgrAppComponent,
    FillPipe,
    Safe,
    
    // Routing components
    routedComponents
  ],
  providers: [
    HierarchyService,
    Safe
  ],
  bootstrap: [CgrAppComponent],
  entryComponents: []        
})
export class CgrAppModule { }
