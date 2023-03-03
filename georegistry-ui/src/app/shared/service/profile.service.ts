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

import { Profile } from "@shared/model/profile";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

@Injectable()
export class ProfileService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient) { }

    get(): Promise<Profile> {
        return this.http
            .get<Profile>(environment.apiUrl + "/api/registryaccount/get")
            .toPromise();
    }

    apply(profile: Profile): Promise<Profile> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http
            .post<Profile>(environment.apiUrl + "/api/registryaccount/apply", JSON.stringify({ account: profile }), { headers: headers })
            .toPromise();
    }

    unlock(oid: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http
            .post<void>(environment.apiUrl + "/api/registryaccount/unlock", JSON.stringify({ oid: oid }), { headers: headers })
            .toPromise();
    }

    setLocale(locale: string): Promise<any> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http
            .post<any>(environment.apiUrl + "/api/localization/set-locale", JSON.stringify({ locale: locale }), { headers: headers })
            .toPromise();
    }

    getRolesForUser(userOID: string): Promise<any> {
        let params: HttpParams = new HttpParams();
        params = params.set("userOID", userOID);

        return this.http
            .get<Profile>(environment.apiUrl + "/api/registryaccount/getRolesForUser", { params: params })
            .toPromise();
    }

}
