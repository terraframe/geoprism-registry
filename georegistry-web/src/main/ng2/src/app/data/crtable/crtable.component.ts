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

declare var acp: any;

@Component({
  
  selector: 'crtable',
  templateUrl: './crtable.component.html',
  styleUrls: ['./crtable.css']
})
export class ChangeRequestTableComponent {

  rows: Observable<any[]>;
  
  selected: any = [];
  
  action: any = {};

  columns = [
    { name: 'Action', prop: 'actionLabel' },
    { name: 'Create Date', prop: 'createActionDate' },
    { name: 'Approval Status', prop: 'approvalStatus' }
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
    const req = new XMLHttpRequest();
    req.open('GET', `/georegistry/changerequest/getAllActions`);

    req.onload = () => {
      cb(JSON.parse(req.response));
    };

    req.send();
  }
  
  onSelect(selected: any)
  {
    this.action = selected.selected[0];
  }
   
}
