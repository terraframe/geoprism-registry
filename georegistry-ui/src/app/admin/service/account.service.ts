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

// import 'rxjs/add/operator/toPromise';
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";

import { Account, User, UserInvite } from "@admin/model/account";

import { PageResult } from "@shared/model/core";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

@Injectable()
export class AccountService {

    constructor(private http: HttpClient, private eventService: EventService) { }

    page(pageNumber: number, pageSize: number): Promise<PageResult<User>> {
        let params: HttpParams = new HttpParams();
        params = params.set("pageNumber", pageNumber.toString());
        params = params.set("pageSize", pageSize.toString());

        this.eventService.start();

        return this.http
            .get<PageResult<User>>(environment.apiUrl + "/api/registryaccount/page", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getSRAs(pageNumber: number, pageSize: number): Promise<PageResult<User>> {
        let params: HttpParams = new HttpParams();
        params = params.set("pageNumber", pageNumber.toString());
        params = params.set("pageSize", pageSize.toString());

        this.eventService.start();

        return this.http
            .get<PageResult<User>>(environment.apiUrl + "/api/registryaccount/get-sras", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    edit(oid: string): Promise<Account> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<Account>(environment.apiUrl + "/api/registryaccount/edit", JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    newInstance(organizationCodes: string[]): Promise<Account> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<Account>(environment.apiUrl + "/api/registryaccount/newInstance", JSON.stringify({ organizationCodes: organizationCodes }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    newInvite(organizationCodes: string[]): Promise<Account> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<Account>(environment.apiUrl + "/api/registryaccount/newInvite", JSON.stringify({ organizationCodes: organizationCodes }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    remove(oid: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<void>(environment.apiUrl + "/api/registryaccount/remove", JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    apply(user: User, roleNames: string[]): Promise<Account> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<Account>(environment.apiUrl + "/api/registryaccount/apply", JSON.stringify({ account: user, roleNames: roleNames }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    inviteUser(invite: UserInvite, roleIds: string[]): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        //        console.log( "Submitting to inviteUser : ", JSON.stringify( { invite: invite, roleIds: roleIds } ) );

        return this.http
            .post<void>(environment.apiUrl + "/api/invite-user/initiate", JSON.stringify({ invite: invite, roleIds: roleIds }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    inviteComplete(user: User, token: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<void>(environment.apiUrl + "/api/invite-user/complete", JSON.stringify({ user: user, token: token }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getRolesForUser(userOID: string): Promise<any> {
        let params: HttpParams = new HttpParams();
        params = params.set("userOID", userOID);

        this.eventService.start();

        return this.http
            .get<any>(environment.apiUrl + "/cgr/account/get-roles-for-user", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

}
