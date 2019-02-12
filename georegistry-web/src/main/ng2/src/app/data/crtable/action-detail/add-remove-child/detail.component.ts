///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Input, Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { Headers, Http, RequestOptions, Response, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Router } from '@angular/router';

import { EventService } from '../../../../event/event.service';

import { ChangeRequestService } from '../../../../service/change-request.service';
import { ChangeRequestTableComponent } from '../../crtable.component';

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
  
  @Input() crtable: ChangeRequestTableComponent;
  
  private bsModalRef: BsModalRef;

  constructor(private router: Router, private eventService: EventService, private http: Http, private changeRequestService: ChangeRequestService, private modalService: BsModalService) { 

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
