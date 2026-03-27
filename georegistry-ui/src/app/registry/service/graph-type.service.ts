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
import { GraphType, ImportHistory } from "@registry/model/registry";

import { environment } from 'src/environments/environment';
import { firstValueFrom } from "rxjs";

@Injectable()
export class GraphTypeService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    controller(typeCode: string): string {
        if (typeCode === 'DirectedAcyclicGraphType') {
            return '/api/directed-graph-type'
        }

        return '/api/undirected-graph-type'
    }

    getAll(codes: string[] = null): Promise<GraphType[]> {
        let params: HttpParams = new HttpParams();

        if (codes?.length) {
            params = new HttpParams({ fromObject: { codes } });
        }

        this.eventService.start();

        return firstValueFrom(this.http.get<GraphType[]>(environment.apiUrl + "/api/graph/get", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            })))
    }

    getAllForType(typeCode: string): Promise<GraphType[]> {
        let params: HttpParams = new HttpParams();


        this.eventService.start();

        return firstValueFrom(this.http.get<GraphType[]>(environment.apiUrl + this.controller(typeCode) + "/get-all", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            })))
    }


    get(typeCode: string, code: string): Promise<GraphType> {
        let params: HttpParams = new HttpParams();
        params = params.append("code", code);

        this.eventService.start();

        return firstValueFrom(this.http.get<GraphType>(environment.apiUrl + this.controller(typeCode) + "/get", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
        );
    }

    apply(typeCode: string, type: GraphType): Promise<GraphType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<GraphType>(environment.apiUrl + this.controller(typeCode) + "/apply", JSON.stringify(type), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    remove(typeCode: string, type: GraphType): Promise<GraphType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<GraphType>(environment.apiUrl + this.controller(typeCode) + "/remove", JSON.stringify({ code: type.code }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    getImportHistory(typeCode: string, code: string): Promise<ImportHistory[]> {
        let params: HttpParams = new HttpParams();
        params = params.append("code", code);

        this.eventService.start();

        return firstValueFrom(this.http.get<ImportHistory[]>(environment.apiUrl + this.controller(typeCode) + "/get-import-history", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            })))
    }

}
