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
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";
import { BusinessObject, BusinessType } from "@registry/model/business-type";
import { GeoRegistryConfiguration } from "@core/model/core";

import { environment } from 'src/environments/environment';

@Injectable()
export class BusinessObjectService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    get(businessTypeCode: string, code: string): Promise<BusinessObject> {
        let params: HttpParams = new HttpParams();
        params = params.append("businessTypeCode", businessTypeCode);
        params = params.append("code", code);

        this.eventService.start();

        return this.http.get<BusinessObject>(environment.apiUrl + "/api/business-object/get", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getTypeAndObject(businessTypeCode: string, code: string): Promise<{type:BusinessType, object: BusinessObject}> {
        let params: HttpParams = new HttpParams();
        params = params.append("businessTypeCode", businessTypeCode);
        params = params.append("code", code);

        this.eventService.start();

        return this.http.get<{type:BusinessType, object: BusinessObject}>(environment.apiUrl + "/api/business-object/get-type-and-object", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

}
