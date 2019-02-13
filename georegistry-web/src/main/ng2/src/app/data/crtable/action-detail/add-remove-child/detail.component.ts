import { Input, Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { Headers, Http, RequestOptions, Response, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Router } from '@angular/router';

import { ChangeRequestService } from '../../../../service/change-request.service';
import { ActionTableComponent } from '../../action-table.component';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ErrorModalComponent } from '../../../../core/modals/error-modal.component';

declare var acp: any;

@Component({
  
  selector: 'crtable-detail-add-remove-child',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.css']
})
export class AddRemoveChildDetailComponent {

  @Input() action: any;
  
  @Input() crtable: ActionTableComponent;
  
  private bsModalRef: BsModalRef;

  constructor(private router: Router, private changeRequestService: ChangeRequestService, private modalService: BsModalService) { 

  }
  
  acceptAction()
  {
    this.changeRequestService.acceptAction(this.action).then( response => {
          this.action.approvalStatus = "REJECTED";
          this.crtable.refresh()
      } ).catch(( err: Response ) => {
          this.error( err.json() );
      } );
  }
  
  rejectAction()
  {
    this.changeRequestService.rejectAction(this.action).then( response => {
          this.action.approvalStatus = "REJECTED";
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
