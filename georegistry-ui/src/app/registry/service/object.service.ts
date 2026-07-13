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

import { HttpClient, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";
import { ObjectAtTime, ObjectClass, ObjectOverTime } from "@registry/model/object-class";

import { environment } from 'src/environments/environment';
import { firstValueFrom } from "rxjs";
import { GenericTableService } from "@shared/model/generic-table";
import { PageResult } from "@shared/model/core";

export abstract class ObjectService implements GenericTableService {


    // eslint-disable-next-line no-useless-constructor
    constructor(protected http: HttpClient, protected eventService: EventService) { }

    abstract controller();

    get(typeCode: string, code: string): Promise<ObjectOverTime> {
        let params: HttpParams = new HttpParams();
        params = params.append("typeCode", typeCode);
        params = params.append("code", code);

        this.eventService.start();

        return firstValueFrom(this.http.get<ObjectOverTime>(environment.apiUrl + this.controller() + "/get", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    getTypeAndObject(typeCode: string, code: string): Promise<{ type: ObjectClass, object: ObjectOverTime }> {
        let params: HttpParams = new HttpParams();
        params = params.append("typeCode", typeCode);
        params = params.append("code", code);

        this.eventService.start();

        return firstValueFrom(this.http.get<{ type: ObjectClass, object: ObjectOverTime }>(environment.apiUrl + this.controller() + "/get-type-and-object", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    page(criteria: Object, pageConfig: any): Promise<PageResult<Object>> {
        let params: HttpParams = new HttpParams();
        params = params.set("criteria", JSON.stringify(criteria));
        params = params.set("typeCode", pageConfig.typeCode);

        return firstValueFrom(this.http
            .get<PageResult<Object>>(environment.apiUrl + this.controller() + "/data", { params: params }));
    }

}
