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
import { HttpHeaders, HttpClient, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";
import { EventService } from "@shared/service";
import { ExternalSystem, PageResult, SystemCapabilities } from "@shared/model/core";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

@Injectable()
export class ExternalSystemService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    getExternalSystems(pageNumber: number, pageSize: number): Promise<PageResult<ExternalSystem>> {
        let params: HttpParams = new HttpParams();
        params = params.set("pageNumber", pageNumber.toString());
        params = params.set("pageSize", pageSize.toString());

        this.eventService.start();

        return this.http
            .get<PageResult<ExternalSystem>>(environment.apiUrl + "/api/external-system/get-all", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }
    
    getAllRead(): Promise<ExternalSystem[]> {
        let params: HttpParams = new HttpParams();

        this.eventService.start();

        return this.http
            .get<ExternalSystem[]>(environment.apiUrl + "/api/external-system/get-all-read", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getExternalSystem(oid: string): Promise<ExternalSystem> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        this.eventService.start();

        return this.http
            .get<ExternalSystem>(environment.apiUrl + "/api/external-system/get", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    applyExternalSystem(system: ExternalSystem): Promise<ExternalSystem> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ExternalSystem>(environment.apiUrl + "/api/external-system/apply", JSON.stringify({ system: system }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getSystemCapabilities(system: ExternalSystem): Promise<SystemCapabilities> {
        let params: HttpParams = new HttpParams();
        params = params.set("system", JSON.stringify(system));

        this.eventService.start();

        return this.http
            .get<SystemCapabilities>(environment.apiUrl + "/api/external-system/system-capabilities", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    removeExternalSystem(oid: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<any>(environment.apiUrl + "/api/external-system/remove", JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

}
