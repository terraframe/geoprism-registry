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

import { environment } from 'src/environments/environment';
import { firstValueFrom } from "rxjs";
import { BusinessEdgeType } from "@registry/model/business-type";

@Injectable({ providedIn: 'root' })
export class BusinessEdgeTypeService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    controller(): string {
        return '/api/business-edge-type'
    }


    getAll(): Promise<BusinessEdgeType[]> {
        let params: HttpParams = new HttpParams();


        this.eventService.start();

        return firstValueFrom(this.http.get<BusinessEdgeType[]>(environment.apiUrl + this.controller() + "/get-all", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            })))
    }


    get(code: string): Promise<BusinessEdgeType> {
        let params: HttpParams = new HttpParams();
        params = params.append("code", code);

        this.eventService.start();

        return firstValueFrom(this.http.get<BusinessEdgeType>(environment.apiUrl + this.controller() + "/get", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
        );
    }

    apply(type: BusinessEdgeType): Promise<BusinessEdgeType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<BusinessEdgeType>(environment.apiUrl + this.controller() + "/apply", JSON.stringify(type), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    remove(type: BusinessEdgeType): Promise<BusinessEdgeType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<BusinessEdgeType>(environment.apiUrl + this.controller() + "/remove", JSON.stringify({ code: type.code }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }
}
