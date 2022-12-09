///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <ehttp://www.gnu.org/licenses/>.
///

import { Injectable } from "@angular/core";
import { HttpHeaders, HttpClient, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";

import { GeoRegistryConfiguration } from "@core/model/registry";import { PageResult } from "@shared/model/core";
 declare let registry: GeoRegistryConfiguration;

@Injectable()
export class TaskService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    getMyTasks(pageNum: number, pageSize: number, whereStatus: string): Promise<PageResult<any>> {
        let params: HttpParams = new HttpParams();

        params = params.set("orderBy", "createDate");
        params = params.set("pageNum", pageNum.toString());
        params = params.set("pageSize", pageSize.toString());
        params = params.set("whereStatus", whereStatus);

        return this.http
            .get<PageResult<any>>(registry.contextPath + "/api/tasks/get", { params: params })
            .toPromise();
    }

    completeTask(taskId: string): Promise<Response> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<any>(registry.contextPath + "/api/tasks/complete", JSON.stringify({ id: taskId }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    setTaskStatus(taskId: string, status: string): Promise<Response> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<any>(registry.contextPath + "/api/tasks/setTaskStatus", JSON.stringify({ id: taskId, status: status }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

}
