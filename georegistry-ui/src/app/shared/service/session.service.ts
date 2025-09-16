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
import { HttpHeaders, HttpClient } from "@angular/common/http";

// import 'rxjs/add/operator/toPromise';
import { finalize } from "rxjs/operators";

import { EventService } from "./event.service";

import { User } from "@shared/model/user";

import { environment } from 'src/environments/environment';

@Injectable()
export class SessionService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: EventService, private http: HttpClient) { }

    login(username: string, password: string): Promise<any> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.service.start();

        return this.http
            .post<User>(environment.apiUrl + "/api/session/login", JSON.stringify({ username: username, password: password }), { headers: headers })
            .pipe(finalize(() => {
                this.service.complete();
            }))
            .toPromise()
            .then((logInResponse: any) => {
                this.service.onLogin();

                return logInResponse;
            });
    }

    logout(): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.service.start();

        return this.http
            .get<void>(environment.apiUrl + "/api/session/logout", { headers: headers })
            .pipe(finalize(() => {
                this.service.complete();
            }))
            .toPromise()
            .then((response: any) => {
                this.service.onLogout();

                return response;
            });
    }

}
