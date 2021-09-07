import { Injectable } from "@angular/core";
import { HttpHeaders, HttpClient } from "@angular/common/http";

import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";

import { Organization } from "@shared/model/core";

declare var acp: any;

@Injectable()
export class OrganizationService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    getOrganizations(): Promise<Organization[]> {
        this.eventService.start();

        return this.http
            .get<Organization[]>(acp + "/cgr/organizations/get-all")
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    updateOrganization(json: any): Promise<Organization> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<Organization>(acp + "/cgr/orgainization/update", JSON.stringify({ json: json }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    newOrganization(json: any): Promise<any> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<any>(acp + "/cgr/orgainization/create", JSON.stringify({ json: json }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    removeOrganization(code: any): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<any>(acp + "/cgr/orgainization/delete", JSON.stringify({ code: code }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

}
