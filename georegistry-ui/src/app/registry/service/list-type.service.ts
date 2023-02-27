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
import { FilterMetadata } from "primeng/api";

import { EventService } from "@shared/service";
import { CurationJob, CurationProblem, LayerRecord, ListOrgGroup, ListType, ListTypeByType, ListTypeEntry, ListTypeVersion, ListVersion, ListVersionMetadata } from "@registry/model/list-type";
import { Observable } from "rxjs";

import { GeoRegistryConfiguration } from "@core/model/core";
import { PageResult } from "@shared/model/core";
import { GenericTableService } from "@shared/model/generic-table";
import { ScheduledJob } from "@registry/model/registry";
import { environment } from 'src/environments/environment';

@Injectable()
export class ListTypeService implements GenericTableService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    listForType(typeCode: string): Promise<ListTypeByType> {
        let params: HttpParams = new HttpParams();
        params = params.set("typeCode", typeCode);

        this.eventService.start();

        return this.http.get<ListTypeByType>(environment.apiUrl + "/api/list-type/list-for-type", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    entries(oid: string): Promise<ListType> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        this.eventService.start();

        return this.http.get<ListType>(environment.apiUrl + "/api/list-type/entries", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    versions(oid: string): Promise<ListTypeVersion[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        this.eventService.start();

        return this.http.get<ListTypeVersion[]>(environment.apiUrl + "/api/list-type/versions", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getVersion(oid: string): Promise<ListTypeVersion> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        this.eventService.start();

        return this.http.get<ListTypeVersion>(environment.apiUrl + "/api/list-type/version", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    fetchVersionsAsListVersion(oids: string[]): Promise<ListVersion[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("oids", oids.join(","));

        // this.eventService.start();

        return this.http.get<ListVersion[]>(environment.apiUrl + "/api/list-type/fetchVersionsAsListVersion", { params: params })
            .pipe(finalize(() => {
                // this.eventService.complete();
            }))
            .toPromise();
    }

    apply(list: ListType): Promise<ListType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ListType>(environment.apiUrl + "/api/list-type/apply", JSON.stringify({ list: list }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    createEntries(oid: string): Promise<ListType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ListType>(environment.apiUrl + "/api/list-type/create-entries", JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    remove(list: ListType): Promise<ListType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ListType>(environment.apiUrl + "/api/list-type/remove", JSON.stringify({ oid: list.oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    createVersion(entry: ListTypeEntry, metadata: ListVersionMetadata): Promise<ListTypeVersion> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ListTypeVersion>(environment.apiUrl + "/api/list-type/create-version", JSON.stringify({ oid: entry.oid, metadata: metadata }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    applyVersion(metadata: ListVersionMetadata): Promise<ListTypeVersion> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ListTypeVersion>(environment.apiUrl + "/api/list-type/apply-version", JSON.stringify({ oid: metadata.oid, metadata: metadata }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    removeVersion(list: ListTypeVersion): Promise<ListType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ListType>(environment.apiUrl + "/api/list-type/remove-version", JSON.stringify({ oid: list.oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    page(criteria: Object, pageConfig: any): Promise<PageResult<Object>> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", pageConfig.oid);
        params = params.set("showInvalid", pageConfig.showInvalid);
        params = params.set("criteria", JSON.stringify(criteria));

        return this.http.get<PageResult<Object>>(environment.apiUrl + "/api/list-type/data", { params: params })
            .toPromise();
    }

    // data(oid: string, pageNumber: number, pageSize: number, filter: { attribute: string, value: string }[], sort: { attribute: string, order: string }): Promise<any> {
    //     let headers = new HttpHeaders({
    //         "Content-Type": "application/json"
    //     });

    //     let params = {
    //         oid: oid,
    //         sort: sort
    //     } as any;

    //     if (pageNumber != null) {
    //         params.pageNumber = pageNumber;
    //     }

    //     if (pageSize != null) {
    //         params.pageSize = pageSize;
    //     }

    //     if (filter.length > 0) {
    //         params.filter = filter;
    //     }

    //     return this.http
    //         .post<any>(environment.apiUrl + "/api/list-type/data", JSON.stringify(params), { headers: headers })
    //         .toPromise();
    // }

    record(oid: string, uid: string, showOverlay: boolean = true): Promise<LayerRecord> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);
        params = params.set("uid", uid);

        if (showOverlay) {
            this.eventService.start();
        }

        return this.http
            .get<LayerRecord>(environment.apiUrl + "/api/list-type/record", { params: params })
            .pipe(finalize(() => {
                if (showOverlay) {
                    this.eventService.complete();
                }
            }))
            .toPromise();
    }

    values(oid: string, value: string, attributeName: string, filters: { [s: string]: FilterMetadata }): Promise<string[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);
        params = params.set("attributeName", attributeName);

        if (filters != null) {
            params = params.set("criteria", JSON.stringify({ filters: filters }));
        }

        if (value != null && value.length > 0) {
            params = params.set("value", value);
        }

        return this.http
            .get<string[]>(environment.apiUrl + "/api/list-type/values", { params: params })
            .toPromise();
    }

    publishList(oid: string): Observable<{ jobOid: string }> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http.post<{ jobOid: string }>(environment.apiUrl + "/api/list-type/publish", JSON.stringify({ oid: oid }), { headers: headers });
    }

    getAllLists(): Promise<{ label: string, oid: string }[]> {
        let params: HttpParams = new HttpParams();

        return this.http
            .get<{ label: string, oid: string }[]>(environment.apiUrl + "/api/list-type/list-all", { params: params })
            .toPromise();
    }

    getPublicVersions(oid: string): Promise<ListTypeVersion[]> {
        let params: HttpParams = new HttpParams();
        params = params.append("oid", oid);

        return this.http
            .get<ListTypeVersion[]>(environment.apiUrl + "/api/list-type/get-public-versions", { params: params })
            .toPromise();
    }

    getGeospatialVersions(startDate: string, endDate: string): Promise<ListOrgGroup[]> {
        let params: HttpParams = new HttpParams();

        if (startDate != null && startDate.length > 0) {
            params = params.append("startDate", startDate);
        }

        if (endDate != null && endDate.length > 0) {
            params = params.append("endDate", endDate);
        }

        return this.http
            .get<ListOrgGroup[]>(environment.apiUrl + "/api/list-type/get-geospatial-versions", { params: params })
            .toPromise();
    }

    getBounds(oid: string, uid?: string): Promise<number[]> {
        let params: HttpParams = new HttpParams();
        params = params.append("oid", oid);

        if (uid != null) {
            params = params.append("uid", uid);
        }

        return this.http
            .get<number[]>(environment.apiUrl + "/api/list-type/bounds", { params: params })
            .toPromise();
    }

    getCurationInfo(version: ListTypeVersion, onlyUnresolved: boolean, pageNumber: number, pageSize: number): Promise<CurationJob> {
        let params: HttpParams = new HttpParams();
        params = params.set("historyId", version.curation.curationId);
        params = params.set("onlyUnresolved", onlyUnresolved.toString());
        params = params.set("pageSize", pageSize.toString());
        params = params.set("pageNumber", pageNumber.toString());

        return this.http.get<CurationJob>(environment.apiUrl + "/api/curation/details", { params: params })
            .toPromise();
    }

    getCurationPage(version: ListTypeVersion, onlyUnresolved: boolean, pageNumber: number, pageSize: number): Promise<PageResult<any>> {
        let params: HttpParams = new HttpParams();
        params = params.set("historyId", version.curation.curationId);
        params = params.set("onlyUnresolved", onlyUnresolved.toString());
        params = params.set("pageSize", pageSize.toString());
        params = params.set("pageNumber", pageNumber.toString());

        this.eventService.start();

        return this.http.get<PageResult<any>>(environment.apiUrl + "/api/curation/page", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    createCurationJob(version: ListTypeVersion): Promise<CurationJob> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<CurationJob>(environment.apiUrl + "/api/curation/curate", JSON.stringify({ listTypeVersionId: version.oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    submitErrorResolve(config: any): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<void>(environment.apiUrl + "/api/curation/problem-resolve", JSON.stringify({ config: config }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    setResolution(problem: CurationProblem, resolution: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        const params: any = { problemId: problem.id };

        if (resolution != null) {
            params.resolution = resolution;
        }

        this.eventService.start();

        return this.http
            .post<void>(environment.apiUrl + "/api/curation/set-resolution", JSON.stringify(params), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getJob(historyOid: string): Promise<ScheduledJob> {
        let params: HttpParams = new HttpParams();
        params = params.append("historyOid", historyOid);

        return this.http
            .get<ScheduledJob>(environment.apiUrl + "/api/list-type/get-publish-job", { params: params })
            .toPromise();
    }

}
