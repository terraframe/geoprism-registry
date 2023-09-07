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

import { Organization, OrganizationNode, PageResult } from "@shared/model/core";

import { environment } from 'src/environments/environment';

@Injectable()
export class OrganizationService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    getOrganizations(): Promise<Organization[]> {
        this.eventService.start();

        return this.http
            .get<Organization[]>(environment.apiUrl + "/api/organization/get-all")
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
            .post<Organization>(environment.apiUrl + "/api/organization/update", JSON.stringify(json), { headers: headers })
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
            .post<any>(environment.apiUrl + "/api/organization/create", JSON.stringify(json), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    removeOrganization(code: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<any>(environment.apiUrl + "/api/organization/delete", code, { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    move(code: string, parentCode: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        const params = {
            code: code,
            parentCode: parentCode
        };

        this.eventService.start();

        return this.http
            .post<void>(environment.apiUrl + "/api/organization/move", JSON.stringify(params), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    removeParent(code: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        const params = {
            code: code
        };

        this.eventService.start();

        return this.http
            .post<void>(environment.apiUrl + "/api/organization/remove-parent", JSON.stringify(params), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }



    getChildren(code: string, pageNumber: number, pageSize: number): Promise<PageResult<Organization>> {
        let params: HttpParams = new HttpParams();
        params = params.set("pageNumber", pageNumber.toString());
        params = params.set("pageSize", pageSize.toString());

        if (code != null) {
            params = params.set("code", code);
        }

        return this.http.get<PageResult<Organization>>(environment.apiUrl + "/api/organization/get-children", { params: params })
            .toPromise();
    }

    getAncestorTree(rootCode: string, code: string, pageSize: number): Promise<OrganizationNode> {
        let params: HttpParams = new HttpParams();
        params = params.set("code", code);
        params = params.set("pageSize", pageSize.toString());

        if (rootCode != null) {
            params = params.set("rootCode", rootCode);
        }

        return this.http.get<OrganizationNode>(environment.apiUrl + "/api/organization/get-ancestor-tree", { params: params })
            .toPromise();
    }
}
