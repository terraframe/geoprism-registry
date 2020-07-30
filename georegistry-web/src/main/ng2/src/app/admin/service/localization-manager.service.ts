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
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { finalize } from 'rxjs/operators';

import { EventService } from '@shared/service';

import { AllLocaleInfo } from '@admin/model/localization-manager';

declare var acp: any;

@Injectable()
export class LocalizationManagerService {


	constructor(private http: HttpClient, private eventService: EventService) { }


	getNewLocaleInfo(): Promise<AllLocaleInfo> {
		return this.http
			.get<AllLocaleInfo>(acp + '/localization/getNewLocaleInformation')
			.toPromise();
	}

	installLocale(language: string, country: string, variant: string): Promise<{ locale: string }> {
		let params: HttpParams = new HttpParams();

		if (language != null) {
			params = params.set('language', language);
		}

		if (country != null) {
			params = params.set('country', country);
		}

		if (variant != null) {
			params = params.set('variant', variant);
		}

		return this.http
			.get<{ locale: string }>(acp + '/localization/installLocale', { params: params })
			.toPromise();
	}

	installFile(formData: FormData): Promise<void> {
		let headers = new HttpHeaders();

		this.eventService.start();

		return this.http.post<void>(acp + "/localization/importSpreadsheet", formData, { headers: headers })
			.pipe(finalize(() => {
				this.eventService.complete();
			}))
			.toPromise();
	}

}
