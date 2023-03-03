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
import { BusinessType, BusinessTypeByOrg } from "@registry/model/business-type";
import { AttributeType } from "@registry/model/registry";
import { GeoRegistryConfiguration } from "@core/model/core";
import { GenericTableService } from "@shared/model/generic-table";
import { PageResult } from "@shared/model/core";

import { environment } from 'src/environments/environment';

@Injectable()
export class BusinessTypeService implements GenericTableService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    getByOrganization(): Promise<BusinessTypeByOrg[]> {
        let params: HttpParams = new HttpParams();

        this.eventService.start();

        return this.http.get<BusinessTypeByOrg[]>(environment.apiUrl + "/api/business-type/get-by-org", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getAll(): Promise<BusinessType[]> {
        let params: HttpParams = new HttpParams();

        this.eventService.start();

        return this.http.get<BusinessType[]>(environment.apiUrl + "/api/business-type/get-all", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    get(oid: string): Promise<BusinessType> {
        let params: HttpParams = new HttpParams();
        params = params.append("oid", oid);

        this.eventService.start();

        return this.http.get<BusinessType>(environment.apiUrl + "/api/business-type/get", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    apply(type: BusinessType): Promise<BusinessType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<BusinessType>(environment.apiUrl + "/api/business-type/apply", JSON.stringify({ type: type }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    remove(type: BusinessType): Promise<BusinessType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<BusinessType>(environment.apiUrl + "/api/business-type/remove", JSON.stringify({ oid: type.oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    edit(oid: string): Promise<BusinessType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<BusinessType>(environment.apiUrl + "/api/business-type/edit", JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    unlock(oid: string): Promise<BusinessType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<BusinessType>(environment.apiUrl + "/api/business-type/unlock", JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    addAttributeType(typeCode: string, attribute: AttributeType): Promise<AttributeType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<AttributeType>(environment.apiUrl + "/api/business-type/add-attribute", JSON.stringify({ typeCode: typeCode, attributeType: attribute }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    updateAttributeType(typeCode: string, attribute: AttributeType): Promise<AttributeType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<AttributeType>(environment.apiUrl + "/api/business-type/update-attribute", JSON.stringify({ typeCode: typeCode, attributeType: attribute }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    deleteAttributeType(typeCode: string, attributeName: string): Promise<boolean> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<boolean>(environment.apiUrl + "/api/business-type/remove-attribute", JSON.stringify({ typeCode: typeCode, attributeName: attributeName }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    page(criteria: Object, pageConfig: any): Promise<PageResult<Object>> {
        let params: HttpParams = new HttpParams();
        params = params.set("criteria", JSON.stringify(criteria));
        params = params.set("typeCode", pageConfig.typeCode);

        return this.http
            .get<PageResult<Object>>(environment.apiUrl + "/api/business-type/data", { params: params })
            .toPromise();
    }

}
