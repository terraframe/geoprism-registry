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

import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { Headers, Http, RequestOptions, Response, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Router } from '@angular/router';

import { EventService } from '../../event/event.service';

import { ChangeRequestService } from '../../service/change-request.service';

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';

declare var acp: any;

@Component({
  
  selector: 'crtable',
  templateUrl: './crtable.component.html',
  styleUrls: ['./crtable.css']
})
export class ChangeRequestTableComponent {

  private bsModalRef: BsModalRef;

  rows: Observable<any[]>;
  
  selected: any = [];
  
  action: any = {};
  
  loading: boolean = false;

  columns = [
    { name: 'Action', prop: 'actionLabel', sortable: false },
    { name: 'Create Date', prop: 'createActionDate', sortable: false, width: 195 },
    { name: 'Approval Status', prop: 'approvalStatus', sortable: false }
  ];

  constructor(private router: Router, private eventService: EventService, private http: Http, private changeRequestService: ChangeRequestService, private modalService: BsModalService) { 
	  this.rows = Observable.create((subscriber: any) => {
      this.fetch((data: any) => {
        subscriber.next(data.splice(0, 30));
        subscriber.complete();
      });
    });
  }
  
  refresh() {
    this.rows = Observable.create((subscriber: any) => {
      this.fetch((data: any) => {
        subscriber.next(data.splice(0, 30));
        subscriber.complete();
      });
    });
    
    this.selected = [];
    this.action = {};
  }

  fetch(cb: any) {
    this.loading = true;
  
    this.changeRequestService.fetchData(cb)
      .then( () => {
        // Do nothing
      }).catch(( err: any ) => {
        console.log(err);
        this.error( err.json() );
      });
  }
  
  onSelect(selected: any)
  {
    this.action = selected.selected[0];
  }
  
  public error( err: any ): void {
    // Handle error
    if ( err !== null ) {
        let bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
        bsModalRef.content.message = ( err.localizedMessage || err.message );
    }
  }
   
}
