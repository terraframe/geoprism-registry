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

import { GeoRegistryConfiguration } from "@core/model/core";
import { Classification, ClassificationNode } from "@registry/model/classification-type";
import { PageResult } from "@shared/model/core";

import { environment } from 'src/environments/environment';

@Injectable()
export class ClassificationService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    apply(classificationCode: string, parentCode: string, classification: Classification, isNew: boolean): Promise<Classification> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        const params = {
            classificationCode: classificationCode,
            classification: classification,
            isNew: isNew
        };

        if (parentCode != null) {
            params["parentCode"] = parentCode;
        }

        this.eventService.start();

        return this.http
            .post<Classification>(environment.apiUrl + "/api/classification/apply", JSON.stringify(params), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    remove(classificationCode: string, code: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        const params = {
            classificationCode: classificationCode,
            code: code
        };

        this.eventService.start();

        return this.http
            .post<void>(environment.apiUrl + "/api/classification/remove", JSON.stringify(params), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    move(classificationCode: string, code: string, parentCode: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        const params = {
            classificationCode: classificationCode,
            code: code,
            parentCode: parentCode
        };

        this.eventService.start();

        return this.http
            .post<void>(environment.apiUrl + "/api/classification/move", JSON.stringify(params), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getChildren(classificationCode: string, code: string, pageNumber: number, pageSize: number): Promise<PageResult<Classification>> {
        let params: HttpParams = new HttpParams();
        params = params.set("classificationCode", classificationCode);
        params = params.set("pageNumber", pageNumber.toString());
        params = params.set("pageSize", pageSize.toString());

        if (code != null) {
            params = params.set("code", code);
        }

        return this.http.get<PageResult<Classification>>(environment.apiUrl + "/api/classification/get-children", { params: params })
            .toPromise();
    }

    getAncestorTree(classificationCode: string, rootCode: string, code: string, pageSize: number): Promise<ClassificationNode> {
        let params: HttpParams = new HttpParams();
        params = params.set("classificationCode", classificationCode);
        params = params.set("code", code);
        params = params.set("pageSize", pageSize.toString());

        if (rootCode != null) {
            params = params.set("rootCode", rootCode);
        }

        return this.http.get<ClassificationNode>(environment.apiUrl + "/api/classification/get-ancestor-tree", { params: params })
            .toPromise();
    }

    search(classificationCode: string, rootCode: string, text: string): Promise<Classification[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("classificationCode", classificationCode);

        if (rootCode != null) {
            params = params.set("rootCode", rootCode);
        }

        if (text != null) {
            params = params.set("text", text);
        }

        return this.http.get<Classification[]>(environment.apiUrl + "/api/classification/search", { params: params })
            .toPromise();
    }

    get(classificationCode: string, code: string): Promise<Classification> {
        let params: HttpParams = new HttpParams();
        params = params.set("classificationCode", classificationCode);
        params = params.set("code", code);

        return this.http.get<Classification>(environment.apiUrl + "/api/classification/get", { params: params })
            .toPromise();
    }

}
