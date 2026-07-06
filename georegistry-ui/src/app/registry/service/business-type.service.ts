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

import { EventService } from "@shared/service";
import { GenericTableService } from "@shared/model/generic-table";
import { PageResult } from "@shared/model/core";

import { environment } from 'src/environments/environment';
import { finalize, firstValueFrom } from "rxjs";
import { ObjectClassService } from "./object-class.service";
import { BusinessEdgeType, BusinessType } from "@registry/model/object-class";

@Injectable({ providedIn: 'root' })
export class BusinessTypeService extends ObjectClassService<BusinessType> implements GenericTableService {

    // eslint-disable-next-line no-useless-constructor
    constructor(public http: HttpClient, public eventService: EventService) {
        super(http, eventService);
    }

    getController(): string {
        return "/api/business-type";
    }

    getEdges(): Promise<BusinessEdgeType[]> {
        let params: HttpParams = new HttpParams();

        this.eventService.start();

        return firstValueFrom(this.http.get<BusinessEdgeType[]>(environment.apiUrl + "/api/business-type/get-edges", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            })))
    }

    page(criteria: Object, pageConfig: any): Promise<PageResult<Object>> {
        let params: HttpParams = new HttpParams();
        params = params.set("criteria", JSON.stringify(criteria));
        params = params.set("typeCode", pageConfig.typeCode);

        return firstValueFrom(this.http
            .get<PageResult<Object>>(environment.apiUrl + "/api/business-type/data", { params: params }));
    }

}
