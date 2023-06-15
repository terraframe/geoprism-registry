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
import { LabeledPropertyGraphType, LabeledPropertyGraphTypeEntry, LabeledPropertyGraphTypeVersion } from "@registry/model/labeled-property-graph-type";
import { Observable } from "rxjs";

import { environment } from 'src/environments/environment';

@Injectable()
export class LabeledPropertyGraphTypeService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    entries(oid: string): Promise<LabeledPropertyGraphType> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        this.eventService.start();

        return this.http.get<LabeledPropertyGraphType>(environment.apiUrl + "/api/labeled-property-graph-type/entries", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    versions(oid: string): Promise<LabeledPropertyGraphTypeVersion[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        this.eventService.start();

        return this.http.get<LabeledPropertyGraphTypeVersion[]>(environment.apiUrl + "/api/labeled-property-graph-type/versions", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getVersion(oid: string): Promise<LabeledPropertyGraphTypeVersion> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        this.eventService.start();

        return this.http.get<LabeledPropertyGraphTypeVersion>(environment.apiUrl + "/api/labeled-property-graph-type/version", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    apply(list: LabeledPropertyGraphType): Promise<LabeledPropertyGraphType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<LabeledPropertyGraphType>(environment.apiUrl + "/api/labeled-property-graph-type/apply", JSON.stringify({ list: list }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    createEntries(oid: string): Promise<LabeledPropertyGraphType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<LabeledPropertyGraphType>(environment.apiUrl + "/api/labeled-property-graph-type/create-entries", JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    remove(list: { label: string, oid: string }): Promise<LabeledPropertyGraphType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<LabeledPropertyGraphType>(environment.apiUrl + "/api/labeled-property-graph-type/remove", JSON.stringify({ oid: list.oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    createVersion(entry: LabeledPropertyGraphTypeEntry): Promise<LabeledPropertyGraphTypeVersion> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<LabeledPropertyGraphTypeVersion>(environment.apiUrl + "/api/labeled-property-graph-type/create-version", JSON.stringify({ oid: entry.oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    removeVersion(list: LabeledPropertyGraphTypeVersion): Promise<LabeledPropertyGraphType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<LabeledPropertyGraphType>(environment.apiUrl + "/api/labeled-property-graph-type/remove-version", JSON.stringify({ oid: list.oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }


    publish(oid: string): Observable<{ jobOid: string }> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http.post<{ jobOid: string }>(environment.apiUrl + "/api/labeled-property-graph-type/publish", JSON.stringify({ oid: oid }), { headers: headers });
    }

    getAll(): Promise<{ label: string, oid: string }[]> {
        let params: HttpParams = new HttpParams();

        return this.http
            .get<{ label: string, oid: string }[]>(environment.apiUrl + "/api/labeled-property-graph-type/get-all", { params: params })
            .toPromise();
    }

    data(oid: string): Promise<any> {
        let params: HttpParams = new HttpParams();
        params = params.append("oid", oid)

        return this.http
            .get<any>(environment.apiUrl + "/api/labeled-property-graph-type/data", { params: params })
            .toPromise();
    }


}
