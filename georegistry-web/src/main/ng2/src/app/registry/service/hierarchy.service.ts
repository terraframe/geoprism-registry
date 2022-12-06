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
// import 'rxjs/add/operator/toPromise';
import { finalize } from "rxjs/operators";

import { HierarchyType, HierarchyGroupedTypeView } from "@registry/model/hierarchy";
import { TreeEntity } from "@registry/model/registry";
import { EventService } from "@shared/service";

import { GeoRegistryConfiguration } from "@core/model/registry"; declare let registry: GeoRegistryConfiguration;

@Injectable()
export class HierarchyService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    getHierarchyTypes(types: any): Promise<HierarchyType[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("types", JSON.stringify(types));

        return this.http
            .get<HierarchyType[]>(registry.contextPath + "/cgr/hierarchytype/get-all", { params: params })
            .toPromise();
    }

    getHierarchyGroupedTypes(): Promise<HierarchyGroupedTypeView[]> {
        let params: HttpParams = new HttpParams();

        return this.http
            .get<HierarchyGroupedTypeView[]>(registry.contextPath + "/api/hierarchytype/groupedTypes", { params: params })
            .toPromise();
    }

    addChildToHierarchy(hierarchyCode: string, parentGeoObjectTypeCode: string, childGeoObjectTypeCode: string): Promise<HierarchyType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<HierarchyType>(registry.contextPath + "/cgr/hierarchytype/add", JSON.stringify({ hierarchyCode: hierarchyCode, parentGeoObjectTypeCode: parentGeoObjectTypeCode, childGeoObjectTypeCode: childGeoObjectTypeCode }), { headers: headers })
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
            .post<HierarchyType>(registry.contextPath + "/api/hierarchytype/insertBetweenTypes", JSON.stringify({ hierarchyCode: hierarchyCode, parentGeoObjectTypeCode: parentGeoObjectTypeCode, middleGeoObjectTypeCode: middleGeoObjectTypeCode, youngestGeoObjectTypeCode: youngestGeoObjectTypeCode }), { headers: headers })
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
            .post<HierarchyType>(registry.contextPath + "/cgr/hierarchytype/remove", JSON.stringify({ hierarchyCode: hierarchyCode, parentGeoObjectTypeCode: parentGeoObjectTypeCode, childGeoObjectTypeCode: childGeoObjectTypeCode }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    createHierarchyType(htJSON: string): Promise<HierarchyType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<HierarchyType>(registry.contextPath + "/cgr/hierarchytype/create", JSON.stringify({ htJSON: htJSON }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    updateHierarchyType(htJSON: string): Promise<HierarchyType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<HierarchyType>(registry.contextPath + "/cgr/hierarchytype/update", JSON.stringify({ htJSON: htJSON }), { headers: headers })
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
            .post<TreeEntity>(registry.contextPath + "/cgr/hierarchytype/delete", { code: code }, { headers: headers })
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
            .post<HierarchyType>(registry.contextPath + "/api/hierarchytype/setInherited", JSON.stringify({ hierarchyTypeCode: hierarchyTypeCode, inheritedHierarchyTypeCode: inheritedHierarchyTypeCode, geoObjectTypeCode: geoObjectTypeCode }), { headers: headers })
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
            .post<HierarchyType>(registry.contextPath + "/api/hierarchytype/removeInherited", JSON.stringify({ hierarchyTypeCode: hierarchyTypeCode, geoObjectTypeCode: geoObjectTypeCode }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

}
