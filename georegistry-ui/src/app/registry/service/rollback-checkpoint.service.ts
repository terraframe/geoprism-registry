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
import { RollbackCheckpoint } from "@registry/model/rollback-checkpoint";
import { PageResult } from "@shared/model/core";

import { environment } from 'src/environments/environment';
import { firstValueFrom } from "rxjs";

@Injectable()
export class RollbackCheckpointService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    getPage(pageNumber: number = 1, pageSize: number = 20): Promise<PageResult<RollbackCheckpoint>> {
        let params: HttpParams = new HttpParams();
        params = params.append('pageNumber', pageNumber);
        params = params.append('pageSize', pageSize);

        this.eventService.start();

        return firstValueFrom(this.http.get<PageResult<RollbackCheckpoint>>(environment.apiUrl + "/api/rollback/get-page", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            })))
    }

    rollback(oid: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<void>(environment.apiUrl + "/api/rollback/rollback", JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

}
