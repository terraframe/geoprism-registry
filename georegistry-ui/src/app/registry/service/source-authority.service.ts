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
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";
import { SourceAuthority } from "@registry/model/source";

import { environment } from 'src/environments/environment';
import { firstValueFrom } from "rxjs";

@Injectable({ providedIn: 'root' })
export class SourceAuthorityService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    getAll(): Promise<SourceAuthority[]> {
        let params: HttpParams = new HttpParams();

        this.eventService.start();

        return firstValueFrom(this.http.get<SourceAuthority[]>(environment.apiUrl + "/api/source-authority/get-all", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            })))
    }

    search(text: string): Promise<SourceAuthority[]> {
        let params: HttpParams = new HttpParams();
        params = params.append('text', text);

        return firstValueFrom(this.http.get<SourceAuthority[]>(environment.apiUrl + "/api/source-authority/search", { params: params }))
    }

    get(code: string): Promise<SourceAuthority> {
        let params: HttpParams = new HttpParams();
        params = params.append("code", code);

        this.eventService.start();

        return firstValueFrom(this.http.get<SourceAuthority>(environment.apiUrl + "/api/source-authority/get", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    apply(source: SourceAuthority): Promise<SourceAuthority> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<SourceAuthority>(environment.apiUrl + "/api/source-authority/apply", JSON.stringify(source), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    remove(source: SourceAuthority): Promise<SourceAuthority> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<SourceAuthority>(environment.apiUrl + "/api/source-authority/remove", JSON.stringify({ code: source.code }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }


}
