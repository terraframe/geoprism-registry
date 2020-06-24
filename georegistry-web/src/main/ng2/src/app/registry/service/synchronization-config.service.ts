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

import { EventService } from '../../shared/service/event.service'

import { PageResult } from '../../shared/model/core'
import { SynchronizationConfig, OrgSyncInfo } from '../model/registry'

declare var acp: any;

@Injectable()
export class SynchronizationConfigService {

	constructor(private http: HttpClient, private eventService: EventService) { }

	getPage(pageNumber: number, pageSize: number): Promise<PageResult<SynchronizationConfig>> {

		let params: HttpParams = new HttpParams();
		params = params.set('pageNumber', pageNumber.toString());
		params = params.set('pageSize', pageSize.toString());

		this.eventService.start();

		return this.http
			.get<PageResult<SynchronizationConfig>>(acp + '/synchronization-config/get-all', { params: params })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	get(oid: string): Promise<SynchronizationConfig> {

		let params: HttpParams = new HttpParams();
		params = params.set('oid', oid);

		this.eventService.start();

		return this.http
			.get<SynchronizationConfig>(acp + '/synchronization-config/get', { params: params })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	apply(config: SynchronizationConfig): Promise<SynchronizationConfig> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<SynchronizationConfig>(acp + '/synchronization-config/apply', JSON.stringify({ config: config }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	edit(oid: string): Promise<{ config: SynchronizationConfig, orgs: OrgSyncInfo[] }> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		let params = {};

		if (oid != null) {
			params = { oid: oid };
		}

		this.eventService.start();

		return this.http
			.post<{ config: SynchronizationConfig, orgs: OrgSyncInfo[] }>(acp + '/synchronization-config/edit', JSON.stringify(params), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	remove(oid: string): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<any>(acp + '/synchronization-config/remove', JSON.stringify({ oid: oid }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

	unlock(oid: string): Promise<void> {

		let headers = new HttpHeaders({
			'Content-Type': 'application/json'
		});

		this.eventService.start();

		return this.http
			.post<any>(acp + '/synchronization-config/unlock', JSON.stringify({ oid: oid }), { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}



}
