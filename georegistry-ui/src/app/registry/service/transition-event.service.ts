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

import { HistoricalRow, TransitionEvent } from "@registry/model/transition-event";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

@Injectable()
export class TransitionEventService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    getPage(pageSize: number, pageNumber: number, attrConditions: any): Promise<PageResult<TransitionEvent>> {
        let params: HttpParams = new HttpParams();
        params = params.set("pageSize", pageSize.toString());
        params = params.set("pageNumber", pageNumber.toString());
        params = params.set("attrConditions", JSON.stringify(attrConditions));

        this.eventService.start();

        return this.http.get<PageResult<TransitionEvent>>(environment.apiUrl + "/api/transition-event/page", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getDetails(oid: string): Promise<TransitionEvent> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        this.eventService.start();

        return this.http.get<TransitionEvent>(environment.apiUrl + "/api/transition-event/get-details", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    apply(event: TransitionEvent): Promise<TransitionEvent> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<TransitionEvent>(environment.apiUrl + "/api/transition-event/apply", JSON.stringify({ event: event }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    delete(event: TransitionEvent): Promise<TransitionEvent> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<TransitionEvent>(environment.apiUrl + "/api/transition-event/delete", JSON.stringify({ eventId: event.oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getHistoricalReport(typeCode: string, startDate: string, endDate: string, pageSize: number, pageNumber: number): Promise<PageResult<HistoricalRow>> {
        let params: HttpParams = new HttpParams();
        params = params.set("typeCode", typeCode.toString());
        params = params.set("startDate", startDate.toString());
        params = params.set("endDate", endDate.toString());
        params = params.set("pageSize", pageSize.toString());
        params = params.set("pageNumber", pageNumber.toString());

        this.eventService.start();

        return this.http.get<PageResult<HistoricalRow>>(environment.apiUrl + "/api/transition-event/historical-report", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

}
