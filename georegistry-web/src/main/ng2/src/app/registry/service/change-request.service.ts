import { Injectable } from "@angular/core";
import { HttpHeaders, HttpClient, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { ChangeRequest, UpdateAttributeAction, CreateGeoObjectAction } from "@registry/model/crtable";
import { EventService } from "@shared/service";
import { GeoObject } from "@registry/model/registry";
import { PageResult } from "@shared/model/core";

import { ImportConfiguration } from "@registry/model/io";

declare var acp: any;

@Injectable()
export class ChangeRequestService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    implementDecisions(requestId: string): Promise<ChangeRequest> {

        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ChangeRequest>(acp + "/changerequest/implement-decisions", JSON.stringify({ requestId: requestId }), { headers: headers })
            .pipe(finalize(() => {

                this.eventService.complete();

            }))
            .toPromise();

    }

    setActionStatus(actionOid: String, status: String): Promise<void> {

        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<void>(acp + "/changerequest/set-action-status", JSON.stringify({ actionOid: actionOid, status: status }), { headers: headers })
            .pipe(finalize(() => {

                this.eventService.complete();

            }))
            .toPromise();

    }

    getAllRequests(pageSize: number, pageNumber: number, filter: string): Promise<PageResult<ChangeRequest>> {

        let params: HttpParams = new HttpParams();
        params = params.set("pageSize", pageSize.toString());
        params = params.set("pageNumber", pageNumber.toString());
        params = params.set("filter", filter);

        this.eventService.start();

        return this.http.get<PageResult<ChangeRequest>>(acp + "/changerequest/get-all-requests", { params: params })
            .pipe(finalize(() => {

                this.eventService.complete();

            }))
            .toPromise();

    }

    /*
    getRequestDetails(requestId: string): Promise<ChangeRequest> {

        let params: HttpParams = new HttpParams();
        params = params.set("pageSize", pageSize.toString());
        params = params.set("pageNumber", pageNumber.toString());
        params = params.set("filter", filter);

        this.eventService.start();

        return this.http.get<PageResult<ChangeRequest>>(acp + "/changerequest/get-all-requests", { params: params })
            .pipe(finalize(() => {

                this.eventService.complete();

            }))
            .toPromise();

    }
    */

    delete(requestId: string): Promise<string> {

        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http.post<string>(acp + "/changerequest/delete", JSON.stringify({ requestId: requestId }), { headers: headers })
            .pipe(finalize(() => {

                this.eventService.complete();

            }))
            .toPromise();

    }

    deleteFile(actionId: string, fileId: string): Promise<ImportConfiguration> {

        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ImportConfiguration>(acp + "/changerequest/delete-file-action", JSON.stringify({ actionOid: actionId, vfOid: fileId }), { headers: headers })
            .pipe(finalize(() => {

                this.eventService.complete();

            }))
            .toPromise();

    }

}
