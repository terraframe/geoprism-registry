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

import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";
import { AttributeType } from "@registry/model/registry";
import { GenericTableService } from "@shared/model/generic-table";
import { OrganizationGroup, PageResult } from "@shared/model/core";

import { environment } from 'src/environments/environment';
import { firstValueFrom } from "rxjs";
import { ObjectClass } from "@registry/model/object-class";

export abstract class ObjectClassService<T extends ObjectClass> {

    http: HttpClient;

    eventService: EventService;

    // eslint-disable-next-line no-useless-constructor
    constructor(http: HttpClient, eventService: EventService) {
        this.http = http;
        this.eventService = eventService;
    }

    abstract getController(): string;

    getByOrganization(): Promise<OrganizationGroup<T>[]> {
        let params: HttpParams = new HttpParams();

        this.eventService.start();

        return firstValueFrom(this.http.get<OrganizationGroup<T>[]>(environment.apiUrl + this.getController() + "/get-by-org", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    getAll(): Promise<T[]> {
        let params: HttpParams = new HttpParams();

        this.eventService.start();

        return firstValueFrom(this.http.get<T[]>(environment.apiUrl + this.getController() + "/get-all", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            })))
    }

    get(oid: string): Promise<T> {
        let params: HttpParams = new HttpParams();
        params = params.append("oid", oid);

        this.eventService.start();

        return firstValueFrom(this.http.get<T>(environment.apiUrl + this.getController() + "/get", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
        );
    }

    apply(type: T): Promise<T> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<T>(environment.apiUrl + this.getController() + "/apply", JSON.stringify(type), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    remove(type: T): Promise<T> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<T>(environment.apiUrl + this.getController() + "/remove", JSON.stringify({ oid: type.oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    edit(oid: string): Promise<T> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<T>(environment.apiUrl + this.getController() + "/edit", JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    unlock(oid: string): Promise<T> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<T>(environment.apiUrl + this.getController() + "/unlock", JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    addAttributeType(typeCode: string, attribute: AttributeType): Promise<AttributeType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<AttributeType>(environment.apiUrl + this.getController() + "/add-attribute", JSON.stringify({ typeCode: typeCode, attributeType: attribute }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    updateAttributeType(typeCode: string, attribute: AttributeType): Promise<AttributeType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<AttributeType>(environment.apiUrl + this.getController() + "/update-attribute", JSON.stringify({ typeCode: typeCode, attributeType: attribute }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }

    deleteAttributeType(typeCode: string, attributeName: string): Promise<boolean> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return firstValueFrom(this.http
            .post<boolean>(environment.apiUrl + this.getController() + "/remove-attribute", JSON.stringify({ typeCode: typeCode, attributeName: attributeName }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            })));
    }
}
