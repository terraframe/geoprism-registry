import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";
import { ContextList, LayerRecord, ListType, ListTypeByType, ListTypeEntry, ListTypeVersion, ListVersionMetadata } from "@registry/model/list-type";
import { Observable } from "rxjs";

declare let acp: any;

@Injectable()
export class ListTypeService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    listForType(typeCode: string): Promise<ListTypeByType> {
        let params: HttpParams = new HttpParams();
        params = params.set("typeCode", typeCode);

        this.eventService.start();

        return this.http.get<ListTypeByType>(acp + "/list-type/list-for-type", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    entries(oid: string): Promise<ListType> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        this.eventService.start();

        return this.http.get<ListType>(acp + "/list-type/entries", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    versions(oid: string): Promise<ListTypeVersion[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        this.eventService.start();

        return this.http.get<ListTypeVersion[]>(acp + "/list-type/versions", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getVersion(oid: string): Promise<ListTypeVersion> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        this.eventService.start();

        return this.http.get<ListTypeVersion>(acp + "/list-type/version", { params: params })
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
            .post<ListType>(acp + "/list-type/apply", JSON.stringify({ list: list }), { headers: headers })
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
            .post<ListType>(acp + "/list-type/remove", JSON.stringify({ oid: list.oid }), { headers: headers })
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
            .post<ListTypeVersion>(acp + "/list-type/create-version", JSON.stringify({ oid: entry.oid, metadata: metadata }), { headers: headers })
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
            .post<ListTypeVersion>(acp + "/list-type/apply-version", JSON.stringify({ oid: metadata.oid, metadata: metadata }), { headers: headers })
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
            .post<ListType>(acp + "/list-type/remove-version", JSON.stringify({ oid: list.oid }), { headers: headers })
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
            .post<any>(acp + "/list-type/data", JSON.stringify(params), { headers: headers })
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
            .post<LayerRecord>(acp + "/list-type/record", JSON.stringify(params), { headers: headers })
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
            .post<{ label: string, value: string }[]>(acp + "/list-type/values", JSON.stringify(params), { headers: headers })
            .toPromise();
    }

    publishList(oid: string): Observable<string> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http.post<string>(acp + "/list-type/publish", JSON.stringify({ oid: oid }), { headers: headers });
    }

    getAllLists(): Promise<{ label: string, oid: string }[]> {
        let params: HttpParams = new HttpParams();

        return this.http
            .get<{ label: string, oid: string }[]>(acp + "/list-type/list-all", { params: params })
            .toPromise();
    }

    getPublicVersions(oid: string): Promise<ListTypeVersion[]> {
        let params: HttpParams = new HttpParams();
        params = params.append("oid", oid);

        return this.http
            .get<ListTypeVersion[]>(acp + "/list-type/get-public-versions", { params: params })
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
            .get<ContextList[]>(acp + "/list-type/get-geospatial-versions", { params: params })
            .toPromise();
    }

    getBounds(oid: string): Promise<number[]> {
        let params: HttpParams = new HttpParams();
        params = params.append("oid", oid);

        return this.http
            .get<number[]>(acp + "/list-type/bounds", { params: params })
            .toPromise();
    }

}
