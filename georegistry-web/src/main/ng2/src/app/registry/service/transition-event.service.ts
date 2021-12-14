import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";
import { PageResult } from "@shared/model/core";

import { HistoricalRow, TransitionEvent } from "@registry/model/transition-event";

import { GeoRegistryConfiguration } from "@core/model/registry"; declare let registry: GeoRegistryConfiguration;

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

        return this.http.get<PageResult<TransitionEvent>>(registry.contextPath + "/transition-event/page", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getDetails(oid: string): Promise<TransitionEvent> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        this.eventService.start();

        return this.http.get<TransitionEvent>(registry.contextPath + "/transition-event/get-details", { params: params })
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
            .post<TransitionEvent>(registry.contextPath + "/transition-event/apply", JSON.stringify({ event: event }), { headers: headers })
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
            .post<TransitionEvent>(registry.contextPath + "/transition-event/delete", JSON.stringify({ eventId: event.oid }), { headers: headers })
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

        return this.http.get<PageResult<HistoricalRow>>(registry.contextPath + "/transition-event/historical-report", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

}
