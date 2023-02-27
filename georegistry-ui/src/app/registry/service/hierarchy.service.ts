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
import { finalize } from "rxjs/operators";

import { HierarchyType, HierarchyGroupedTypeView } from "@registry/model/hierarchy";
import { TreeEntity } from "@registry/model/registry";
import { EventService } from "@shared/service";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

@Injectable()
export class HierarchyService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    getHierarchyTypes(types: any): Promise<HierarchyType[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("types", JSON.stringify(types));

        return this.http
            .get<HierarchyType[]>(environment.apiUrl + "/api/hierarchytype/get-all", { params: params })
            .toPromise();
    }

    getHierarchyGroupedTypes(): Promise<HierarchyGroupedTypeView[]> {
        let params: HttpParams = new HttpParams();

        return this.http
            .get<HierarchyGroupedTypeView[]>(environment.apiUrl + "/api/hierarchytype/groupedTypes", { params: params })
            .toPromise();
    }

    addChildToHierarchy(hierarchyCode: string, parentGeoObjectTypeCode: string, childGeoObjectTypeCode: string): Promise<HierarchyType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<HierarchyType>(environment.apiUrl + "/api/hierarchytype/add", JSON.stringify({ hierarchyCode: hierarchyCode, parentGeoObjectTypeCode: parentGeoObjectTypeCode, childGeoObjectTypeCode: childGeoObjectTypeCode }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    insertBetweenTypes(hierarchyCode: string, parentGeoObjectTypeCode: string, middleGeoObjectTypeCode: string, youngestGeoObjectTypeCode: string): Promise<HierarchyType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<HierarchyType>(environment.apiUrl + "/api/hierarchytype/insertBetweenTypes", JSON.stringify({ hierarchyCode: hierarchyCode, parentGeoObjectTypeCode: parentGeoObjectTypeCode, middleGeoObjectTypeCode: middleGeoObjectTypeCode, youngestGeoObjectTypeCode: youngestGeoObjectTypeCode }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    removeFromHierarchy(hierarchyCode: string, parentGeoObjectTypeCode: string, childGeoObjectTypeCode: string): Promise<HierarchyType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<HierarchyType>(environment.apiUrl + "/api/hierarchytype/remove", JSON.stringify({ hierarchyCode: hierarchyCode, parentGeoObjectTypeCode: parentGeoObjectTypeCode, childGeoObjectTypeCode: childGeoObjectTypeCode }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    createHierarchyType(hierarchyType: HierarchyType): Promise<HierarchyType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<HierarchyType>(environment.apiUrl + "/api/hierarchytype/create", JSON.stringify({ hierarchyType: hierarchyType }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    updateHierarchyType(hierarchyType: HierarchyType): Promise<HierarchyType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<HierarchyType>(environment.apiUrl + "/api/hierarchytype/update", JSON.stringify({ hierarchyType: hierarchyType }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    deleteHierarchyType(code: string): Promise<TreeEntity> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<TreeEntity>(environment.apiUrl + "/api/hierarchytype/delete", { code: code }, { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    setInheritedHierarchy(hierarchyTypeCode: string, inheritedHierarchyTypeCode: string, geoObjectTypeCode: string): Promise<HierarchyType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<HierarchyType>(environment.apiUrl + "/api/hierarchytype/setInherited", JSON.stringify({ hierarchyTypeCode: hierarchyTypeCode, inheritedHierarchyTypeCode: inheritedHierarchyTypeCode, geoObjectTypeCode: geoObjectTypeCode }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    removeInheritedHierarchy(hierarchyTypeCode: string, geoObjectTypeCode: string): Promise<HierarchyType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<HierarchyType>(environment.apiUrl + "/api/hierarchytype/removeInherited", JSON.stringify({ hierarchyTypeCode: hierarchyTypeCode, geoObjectTypeCode: geoObjectTypeCode }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

}
