import { Injectable } from "@angular/core";
import { HttpHeaders, HttpClient, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { ChangeRequest } from "@registry/model/crtable";
import { EventService } from "@shared/service";
import { PageResult } from "@shared/model/core";

import { ImportConfiguration } from "@registry/model/io";

declare let acp: any;

@Injectable()
export class ChangeRequestService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    implementDecisions(request: ChangeRequest): Promise<ChangeRequest> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ChangeRequest>(acp + "/changerequest/implement-decisions", JSON.stringify({ request: request }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    update(request: ChangeRequest): Promise<ChangeRequest> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ChangeRequest>(acp + "/changerequest/update", JSON.stringify({ request: request }), { headers: headers })
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

    getAllRequests(pageSize: number, pageNumber: number, filter: string, sort: any[], oid:string): Promise<PageResult<ChangeRequest>> {
        let params: HttpParams = new HttpParams();
        params = params.set("pageSize", pageSize.toString());
        params = params.set("pageNumber", pageNumber.toString());
        params = params.set("filter", filter);
        params = params.set("sort", JSON.stringify(sort));

        if (oid != null) {
            params = params.set("oid", oid);
        }

        this.eventService.start();

        return this.http.get<PageResult<ChangeRequest>>(acp + "/changerequest/get-all-requests", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    rejectChangeRequest(request: ChangeRequest): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http.post<void>(acp + "/changerequest/reject", JSON.stringify({ request: request }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

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

    deleteFile(crOid: string, fileId: string): Promise<ImportConfiguration> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ImportConfiguration>(acp + "/changerequest/delete-file-cr", JSON.stringify({ crOid: crOid, vfOid: fileId }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

}
