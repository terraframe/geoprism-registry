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

import { Application } from '@shared/model/application';

import { GeoRegistryConfiguration } from "@core/model/registry"; declare let registry: GeoRegistryConfiguration;

@Injectable()
export class HubService {

	constructor(private http: HttpClient) { }

	applications(): Promise<Application[]> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		return this.http
			.post<Application[]>(registry.contextPath + '/menu/applications', { headers: headers })
			.toPromise();
	}
	
	oauthGetPublic(id: string): Promise<any[]> {
    let params: HttpParams = new HttpParams();

    if (id)
    {
      params = params.set('id', id)
    }

    return this.http
      .get<any[]>(registry.contextPath + '/cgr/oauth/get-public', { params: params })
      .toPromise();
  }
}
