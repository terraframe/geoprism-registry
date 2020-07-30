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
/// License along with Runway SDK(tm).  If not, see <ehttp://www.gnu.org/licenses/>.
///

import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';

import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service/event.service'

import { Organization, PageResult } from '@shared/model/core'

declare var acp: any;

@Injectable()
export class OrganizationService {

	constructor(private http: HttpClient, private eventService: EventService) { }

	getOrganizations(): Promise<Organization[]> {

    this.eventService.start();

    return this.http
      .get<Organization[]>(acp + '/cgr/organizations/get-all')
      .pipe(finalize(() => {
        this.eventService.complete();
      }))
      .toPromise();
  }

  updateOrganization(json: any): Promise<Organization> {

    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    this.eventService.start();

    return this.http
      .post<Organization>(acp + '/cgr/orgainization/update', JSON.stringify({ json: json }), { headers: headers })
      .pipe(finalize(() => {
        this.eventService.complete();
      }))
      .toPromise();
  }

  newOrganization(json: any): Promise<any> {

    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    this.eventService.start();

    return this.http
      .post<any>(acp + '/cgr/orgainization/create', JSON.stringify({ json: json }), { headers: headers })
      .pipe(finalize(() => {
        this.eventService.complete();
      }))
      .toPromise();
  }

  removeOrganization(code: any): Promise<void> {

    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    this.eventService.start();

    return this.http
      .post<any>(acp + '/cgr/orgainization/delete', JSON.stringify({ code: code }), { headers: headers })
      .pipe(finalize(() => {
        this.eventService.complete();
      }))
      .toPromise();
  }
}
