import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";
import { ContextList, CurationJob, CurationProblem, LayerRecord, ListType, ListTypeByType, ListTypeEntry, ListTypeVersion, ListVersionMetadata } from "@registry/model/list-type";
import { Observable } from "rxjs";

import { GeoRegistryConfiguration } from "@core/model/registry"; import { PageResult } from "@shared/model/core";
declare let registry: GeoRegistryConfiguration;

@Injectable()
export class ListTypeService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    listForType(typeCode: string): Promise<ListTypeByType> {
        let params: HttpParams = new HttpParams();
        params = params.set("typeCode", typeCode);

        this.eventService.start();

        return this.http.get<ListTypeByType>(registry.contextPath + "/list-type/list-for-type", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    entries(oid: string): Promise<ListType> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        this.eventService.start();

        return this.http.get<ListType>(registry.contextPath + "/list-type/entries", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    versions(oid: string): Promise<ListTypeVersion[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        this.eventService.start();

        return this.http.get<ListTypeVersion[]>(registry.contextPath + "/list-type/versions", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getVersion(oid: string): Promise<ListTypeVersion> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        this.eventService.start();

        return this.http.get<ListTypeVersion>(registry.contextPath + "/list-type/version", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }


    apply(list: ListType): Promise<ListType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ListType>(registry.contextPath + "/list-type/apply", JSON.stringify({ list: list }), { headers: headers })
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
            .post<ListType>(registry.contextPath + "/list-type/remove", JSON.stringify({ oid: list.oid }), { headers: headers })
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
            .post<ListTypeVersion>(registry.contextPath + "/list-type/create-version", JSON.stringify({ oid: entry.oid, metadata: metadata }), { headers: headers })
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
            .post<ListTypeVersion>(registry.contextPath + "/list-type/apply-version", JSON.stringify({ oid: metadata.oid, metadata: metadata }), { headers: headers })
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
            .post<ListType>(registry.contextPath + "/list-type/remove-version", JSON.stringify({ oid: list.oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    data(oid: string, pageNumber: number, pageSize: number, filter: { attribute: string, value: string }[], sort: { attribute: string, order: string }): Promise<any> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        let params = {
            oid: oid,
            sort: sort
        } as any;

        if (pageNumber != null) {
            params.pageNumber = pageNumber;
        }

        if (pageSize != null) {
            params.pageSize = pageSize;
        }

        if (filter.length > 0) {
            params.filter = filter;
        }

        return this.http
            .post<any>(registry.contextPath + "/list-type/data", JSON.stringify(params), { headers: headers })
            .toPromise();
    }

    record(oid: string, uid: string): Promise<LayerRecord> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        let params = {
            oid: oid,
            uid: uid
        };

        this.eventService.start();


        return this.http
            .post<LayerRecord>(registry.contextPath + "/list-type/record", JSON.stringify(params), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }


    values(oid: string, value: string, attributeName: string, valueAttribute: string, filter: { attribute: string, value: string }[]): Promise<{ label: string, value: string }[]> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        let params = {
            oid: oid,
            attributeName: attributeName,
            valueAttribute: valueAttribute
        } as any;

        if (filter.length > 0) {
            params.filter = filter;
        }

        if (value != null && value.length > 0) {
            params.value = value;
        }

        return this.http
            .post<{ label: string, value: string }[]>(registry.contextPath + "/list-type/values", JSON.stringify(params), { headers: headers })
            .toPromise();
    }

    publishList(oid: string): Observable<string> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http.post<string>(registry.contextPath + "/list-type/publish", JSON.stringify({ oid: oid }), { headers: headers });
    }

    getAllLists(): Promise<{ label: string, oid: string }[]> {
        let params: HttpParams = new HttpParams();

        return this.http
            .get<{ label: string, oid: string }[]>(registry.contextPath + "/list-type/list-all", { params: params })
            .toPromise();
    }

    getPublicVersions(oid: string): Promise<ListTypeVersion[]> {
        let params: HttpParams = new HttpParams();
        params = params.append("oid", oid);

        return this.http
            .get<ListTypeVersion[]>(registry.contextPath + "/list-type/get-public-versions", { params: params })
            .toPromise();
    }

    getGeospatialVersions(startDate: string, endDate: string): Promise<ContextList[]> {
        let params: HttpParams = new HttpParams();

        if (startDate != null && startDate.length > 0) {
            params = params.append("startDate", startDate);
        }

        if (endDate != null && endDate.length > 0) {
            params = params.append("endDate", endDate);
        }

        return this.http
            .get<ContextList[]>(registry.contextPath + "/list-type/get-geospatial-versions", { params: params })
            .toPromise();
    }

    getBounds(oid: string, uid?: string): Promise<number[]> {
        let params: HttpParams = new HttpParams();
        params = params.append("oid", oid);

        if (uid != null) {
            params = params.append("uid", uid);
        }

        return this.http
            .get<number[]>(registry.contextPath + "/list-type/bounds", { params: params })
            .toPromise();
    }

    getCurationInfo(version: ListTypeVersion, onlyUnresolved: boolean, pageNumber: number, pageSize: number): Promise<CurationJob> {

        let params: HttpParams = new HttpParams();
        params = params.set("historyId", version.curation.curationId);
        params = params.set("onlyUnresolved", onlyUnresolved.toString());
        params = params.set("pageSize", pageSize.toString());
        params = params.set("pageNumber", pageNumber.toString());


        return this.http.get<CurationJob>(registry.contextPath + "/curation/details", { params: params })
            .toPromise();
    }


    getCurationPage(version: ListTypeVersion, onlyUnresolved: boolean, pageNumber: number, pageSize: number): Promise<PageResult<any>> {

        let params: HttpParams = new HttpParams();
        params = params.set("historyId", version.curation.curationId);
        params = params.set("onlyUnresolved", onlyUnresolved.toString());
        params = params.set("pageSize", pageSize.toString());
        params = params.set("pageNumber", pageNumber.toString());

        this.eventService.start();


        return this.http.get<PageResult<any>>(registry.contextPath + "/curation/page", { params: params })
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
            .post<CurationJob>(registry.contextPath + "/curation/curate", JSON.stringify({ listTypeVersionId: version.oid }), { headers: headers })
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
            .post<void>(registry.contextPath + "/curation/problem-resolve", JSON.stringify({ config: config }), { headers: headers })
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
            .post<void>(registry.contextPath + "/curation/set-resolution", JSON.stringify(params), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

}
