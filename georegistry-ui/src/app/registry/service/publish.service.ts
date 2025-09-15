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

import { environment } from 'src/environments/environment';
import { firstValueFrom } from "rxjs";
import { Commit, Publish, PublishEvents } from "@registry/model/publish";

@Injectable()
export class PublishService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    toPublishEvent(p: Publish): PublishEvents {
        return {
            uid: p.uid,
            label: p.label,
            date: p.date,
            startDate: p.startDate,
            endDate: p.endDate,
            typeCodes: p.types.filter(t => t.type === 'GEO_OBJECT').map(t => t.code),
            businessTypeCodes: p.types.filter(t => t.type === 'BUSINESS').map(t => t.code),
            dagCodes: p.types.filter(t => t.type === 'DAG').map(t => t.code),
            undirectedCodes: p.types.filter(t => t.type === 'UNDIRECTED').map(t => t.code),
            hierarchyCodes: p.types.filter(t => t.type === 'HIERARCHY').map(t => t.code),
            businessEdgeCodes: p.types.filter(t => t.type === 'BUSINESS_EDGE').map(t => t.code)
        }
    }

    toPublish(p: PublishEvents): Publish {
        let types = [];
        types = types.concat(p.businessEdgeCodes.map(code => { return { type: 'BUSINESS_EDGE', code } }))
        types = types.concat(p.hierarchyCodes.map(code => { return { type: 'HIERARCHY', code } }))
        types = types.concat(p.undirectedCodes.map(code => { return { type: 'UNDIRECTED', code } }))
        types = types.concat(p.dagCodes.map(code => { return { type: 'DAG', code } }))
        types = types.concat(p.businessTypeCodes.map(code => { return { type: 'BUSINESS', code } }))
        types = types.concat(p.typeCodes.map(code => { return { type: 'GEO_OBJECT', code } }))

        return {
            uid: p.uid,
            label: p.label,
            date: p.date,
            startDate: p.startDate,
            endDate: p.endDate,
            types: types,
            exclusions: []
        }
    }

    getAll(): Promise<PublishEvents[]> {
        let params: HttpParams = new HttpParams();

        this.eventService.start();

        return firstValueFrom(this.http.get<Publish[]>(environment.apiUrl + "/api/publish/get-all", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))).then(array => {
                return array.map(this.toPublishEvent);
            });
    }

    get(uid: string): Promise<PublishEvents> {
        let params: HttpParams = new HttpParams();
        params = params.append('uid', uid);

        this.eventService.start();

        return firstValueFrom(this.http.get<Publish>(environment.apiUrl + "/api/publish/get", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))).then(this.toPublishEvent);
    }



    create(publish: PublishEvents): Promise<PublishEvents> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<Publish>(environment.apiUrl + "/api/publish/create", JSON.stringify(this.toPublish(publish)), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))).then(this.toPublishEvent);
    }

    remove(publish: PublishEvents): Promise<PublishEvents> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<PublishEvents>(environment.apiUrl + "/api/publish/remove", JSON.stringify({ uid: publish.uid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }


    createNewVersion(uid: string): Promise<Commit> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<Commit>(environment.apiUrl + "/api/publish/create-new-version", JSON.stringify({ uid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    getCommits(uid: string): Promise<Commit[]> {
        let params: HttpParams = new HttpParams();
        params = params.append("publishId", uid);

        this.eventService.start();

        return firstValueFrom(this.http.get<Commit[]>(environment.apiUrl + "/api/commit/get-all", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }


}
