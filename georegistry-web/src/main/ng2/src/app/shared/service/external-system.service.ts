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

import { EventService } from '@shared/service'

import { ExternalSystem, PageResult } from '@shared/model/core'

declare var acp: any;

@Injectable()
export class ExternalSystemService {

	constructor(private http: HttpClient, private eventService: EventService) { }

	getExternalSystems(pageNumber: number, pageSize:number): Promise<PageResult<ExternalSystem>> {

        let params: HttpParams = new HttpParams();
        params = params.set( 'pageNumber', pageNumber.toString() );
        params = params.set( 'pageSize', pageSize.toString() );

		this.eventService.start();

		return this.http
			.get<PageResult<ExternalSystem>>(acp + '/external-system/get-all', {params:params })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	getExternalSystem(oid:string): Promise<ExternalSystem> {

        let params: HttpParams = new HttpParams();
        params = params.set( 'oid', oid );

		this.eventService.start();

		return this.http
			.get<ExternalSystem>(acp + '/external-system/get', {params:params })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	applyExternalSystem(system: ExternalSystem): Promise<ExternalSystem> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<ExternalSystem>(acp + '/external-system/apply', JSON.stringify({ system: system }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}
	
	removeExternalSystem(oid: string): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<any>(acp + '/external-system/remove', JSON.stringify({ oid: oid }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	

}
