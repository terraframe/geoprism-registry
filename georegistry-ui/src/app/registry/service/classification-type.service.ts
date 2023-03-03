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
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";

import { PageResult } from "@shared/model/core";
import { GenericTableService } from "@shared/model/generic-table";
import { ClassificationType } from "@registry/model/classification-type";

import { environment } from 'src/environments/environment';

@Injectable()
export class ClassificationTypeService implements GenericTableService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    apply(classificationType: ClassificationType): Promise<ClassificationType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ClassificationType>(environment.apiUrl + "/api/classification-type/apply", JSON.stringify({ classificationType: classificationType }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    remove(type: ClassificationType): Promise<ClassificationType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ClassificationType>(environment.apiUrl + "/api/classification-type/remove", JSON.stringify({ oid: type.oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    page(criteria: Object): Promise<PageResult<ClassificationType>> {
        let params: HttpParams = new HttpParams();
        params = params.set("criteria", JSON.stringify(criteria));
        return this.http.get<PageResult<ClassificationType>>(environment.apiUrl + "/api/classification-type/page", { params: params })
            .toPromise();
    }

    get(classificationCode: string): Promise<ClassificationType> {
        let params: HttpParams = new HttpParams();
        params = params.set("classificationCode", classificationCode);

        return this.http.get<ClassificationType>(environment.apiUrl + "/api/classification-type/get", { params: params })
            .toPromise();
    }

}
