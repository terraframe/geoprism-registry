import { Input, Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef, ViewEncapsulation } from '@angular/core';
import { Headers, Http, RequestOptions, Response, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Router } from '@angular/router';

import { EventService } from '../../../../event/event.service';

import { ChangeRequestService } from '../../../../service/change-request.service';
import { ActionTableComponent } from '../../action-table.component';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ErrorModalComponent } from '../../../../core/modals/error-modal.component';

import { GeoObject, GeoObjectType } from '../../../../model/registry';

import { RegistryService } from '../../../../service/registry.service';

import { AbstractAction } from '../../crtable';

declare var acp: any;

@Component({
  
  selector: 'crtable-detail-create-geo-object',
  templateUrl: './detail.component.html',
  styleUrls: ['./crtable-detail-create-geo-object.css'],
  encapsulation: ViewEncapsulation.None
})
export class CreateUpdateGeoObjectDetailComponent {

  @Input() action: any;
  
  preGeoObject: GeoObject = null;
  
  postGeoObject: GeoObject = null;
  
  geoObjectType : GeoObjectType = null;
  
  @ViewChild("attributeEditor") attributeEditor;
  
  @Input() crtable: ActionTableComponent;
  
  private bsModalRef: BsModalRef;

  constructor(private router: Router, private eventService: EventService, private http: Http, private changeRequestService: ChangeRequestService, private modalService: BsModalService, private registryService: RegistryService) { 
	  
  }
  
  ngOnInit(): void {
    this.onSelect(this.action);
  }
  
  applyAction()
  {
    var action = JSON.parse(JSON.stringify(this.action));
    action.geoObjectJson = this.attributeEditor.getGeoObject();
  
    this.changeRequestService.applyAction(action).then( response => {
          this.crtable.refresh()
      } ).catch(( err: Response ) => {
          this.error( err.json() );
      } );
  }
  
  onSelect(action: AbstractAction)
  {
    this.action = action;
    
    this.postGeoObject = this.action.geoObjectJson;
    this.geoObjectType = this.action.geoObjectType;
    
    
    // There are multiple ways we could show a diff of an object.
    //
    // This line will show a diff only when a person is typing so as to show the
    // change they are creating.
    //
    // The method below (getGeoObjectByCode) will compare what is in the database
    // at that time with the change request. This will only track state compared to
    // what is currently in the database which isn't necessarily the original change.
    // 
    // A third option which is NOT implemented yet would store the state of a geoobject
    // (original and target) with the change request so as to manage state at time of 
    // the change request submission.
    //
    // Display diff when a user is changing a value
    // this.preGeoObject = JSON.parse(JSON.stringify(this.postGeoObject));
    
    // Display diff of what's in the database
    if(this.action.actionType === "net.geoprism.registry.action.geoobject.UpdateGeoObjectAction"
       && typeof this.postGeoObject.properties.createDate !== 'undefined') {
        this.registryService.getGeoObjectByCode(this.postGeoObject.properties.code, this.geoObjectType.code)
            .then(geoObject => {
                this.preGeoObject = geoObject;
            
            }).catch((err: Response) => {
                this.error(err.json());
            });
    }
  }
  
  unlockAction()
  {
    this.changeRequestService.unlockAction(this.action.oid).then( response => {
          this.crtable.refresh();
      } ).catch(( err: Response ) => {
          this.error( err.json() );
      } );
  }
  
  public error( err: any ): void {
      // Handle error
      if ( err !== null ) {
        // TODO: add error modal
          this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
          this.bsModalRef.content.message = ( err.localizedMessage || err.message );
      }

  }

}
