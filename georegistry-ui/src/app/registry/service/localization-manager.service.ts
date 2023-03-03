///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
// import 'rxjs/add/operator/toPromise';

import { EventService } from "@shared/service";

import { AllLocaleInfo } from "@registry/model/localization-manager";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

@Injectable()
export class LocalizationManagerService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    getNewLocaleInfo(): Promise<AllLocaleInfo> {
        return this.http
            .get<AllLocaleInfo>(environment.apiUrl + "/api/localization/getNewLocaleInformation")
            .toPromise();
    }

    installLocale(language: string, country: string, variant: string): Promise<void> {
        let params: HttpParams = new HttpParams();

        if (language != null) {
            params = params.set("language", language);
        }

        if (country != null) {
            params = params.set("country", country);
        }

        if (variant != null) {
            params = params.set("variant", variant);
        }

        return this.http
            .get<void>(environment.apiUrl + "/api/localization/installLocale", { params: params })
            .toPromise();
    }

}
