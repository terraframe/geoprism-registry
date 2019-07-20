import { Input, Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { Headers, Http, RequestOptions, Response, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Router } from '@angular/router';

import { ChangeRequestService } from '../../../../service/change-request.service';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ErrorModalComponent } from '../../../../core/modals/error-modal.component';

import { AddChildAction } from '../../crtable';

declare var acp: any;

@Component({
  
  selector: 'crtable-detail-add-remove-child',
  templateUrl: './detail.component.html',
  styleUrls: []
})
export class AddRemoveChildDetailComponent {

  @Input() action: AddChildAction;
  
  private bsModalRef: BsModalRef;

  constructor(private router: Router, private changeRequestService: ChangeRequestService, private modalService: BsModalService) { 

  }
  
  applyAction()
  {
    this.changeRequestService.applyAction(this.action).then( response => {
		this.unlockAction();
      } ).catch(( err: Response ) => {
          this.error( err.json() );
      } );
  }
  
  onSelect(action: AddChildAction)
  {
    this.action = action;
  }
  
  unlockAction()
  {
    this.changeRequestService.unlockAction(this.action.oid).then( response => {
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
