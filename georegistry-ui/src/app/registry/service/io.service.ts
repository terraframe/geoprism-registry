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
import { finalize } from "rxjs/operators";

import { ImportConfiguration, Synonym, Location, Term } from "@registry/model/io";
import { EventService } from "@shared/service";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

@Injectable()
export class IOService {

    constructor(private http: HttpClient, private eventService: EventService) { }

    beginImport(configuration: ImportConfiguration): Promise<ImportConfiguration> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ImportConfiguration>(environment.apiUrl + "/api/etl/import", JSON.stringify({ config: configuration }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    cancelImport(configuration: ImportConfiguration): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<void>(environment.apiUrl + "/api/etl/cancel-import", JSON.stringify({ config: configuration }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    listGeoObjectTypes(includeAbstractTypes: boolean): Promise<{ label: string, code: string, orgCode: string, superTypeCode?: string }[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("includeAbstractTypes", includeAbstractTypes.toString());

        return this.http
            .get<{ label: string, code: string, orgCode: string }[]>(environment.apiUrl + "/api/geoobjecttype/list-types", { params: params })
            .toPromise();
    }

    getTypeAncestors(code: string, hierarchyCode: string, includeInheritedTypes: boolean, includeChild: boolean = false): Promise<Location[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("code", code);
        params = params.set("hierarchyCode", hierarchyCode);
        params = params.set("includeInheritedTypes", includeInheritedTypes.toString());
        params = params.set("includeChild", includeChild.toString());

        return this.http
            .get<Location[]>(environment.apiUrl + "/api/geoobjecttype/get-ancestors", { params: params })
            .toPromise();
    }

    getHierarchiesForType(code: string, includeTypes: boolean): Promise<{ label: string, code: string, parents: { label: string, code: string }[] }[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("code", code);
        params = params.set("includeTypes", includeTypes.toString());

        this.eventService.start();

        return this.http
            .get<{ label: string, code: string, parents: { label: string, code: string }[] }[]>(environment.apiUrl + "/api/geoobjecttype/get-hierarchies", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getHierarchiesForSubtypes(code: string, includeTypes: boolean): Promise<{ label: string, code: string, parents: { label: string, code: string }[] }[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("code", code);
        params = params.set("includeTypes", includeTypes.toString());

        this.eventService.start();

        return this.http
            .get<{ label: string, code: string, parents: { label: string, code: string }[] }[]>(environment.apiUrl + "/api/geoobjecttype/get-subtype-hierarchies", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getGeoObjectSuggestions(text: string, type: string, parent: string, hierarchy: string): Promise<any> {
        let params: HttpParams = new HttpParams();
        params = params.set("text", text);
        params = params.set("type", type);

        if (parent != null && hierarchy != null) {
            params = params.set("parent", parent);
            params = params.set("hierarchy", hierarchy);
        }

        return this.http
            .get<any>(environment.apiUrl + "/api/geoobject/suggestions", { params: params })
            .toPromise();
    }

    createGeoObjectSynonym(entityId: string, label: string): Promise<Synonym> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<Synonym>(environment.apiUrl + "/geo-synonym/create-geo-entity-synonym", JSON.stringify({ entityId: entityId, label: label }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    deleteGeoObjectSynonym(synonymId: string, oid: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<void>(environment.apiUrl + "/geo-synonym/create-geo-entity-synonym", JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getTermSuggestions(importType: string, typeCode: string, attributeCode: string, text: string, limit: number): Promise<{ text: string, data: any }[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("importType", importType);
        params = params.set("typeCode", typeCode);
        params = params.set("attributeCode", attributeCode);
        params = params.set("text", text);
        params = params.set("limit", limit);

        return this.http
            .get<{ text: string, data: any }[]>(environment.apiUrl + "/api/term/getClassifierSuggestions", { params: params })
            .toPromise();
    }

    createTermSynonym(classifierId: string, label: string): Promise<Synonym> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        let data = JSON.stringify({ classifierId: classifierId, label: label });

        return this.http
            .post<Synonym>(environment.apiUrl + "/uploader/createClassifierSynonym", data, { headers: headers })
            .toPromise();
    }

    deleteTermSynonym(synonymId: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        let data = JSON.stringify({ synonymId: synonymId });

        return this.http
            .post<void>(environment.apiUrl + "/uploader/deleteClassifierSynonym", data, { headers: headers })
            .toPromise();
    }

    createTerm(label: string, code: string, parentTermCode: string): Promise<Term> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        let params = { parentTermCode: parentTermCode, termJSON: { label: label, code: code } };

        return this.http
            .post<Term>(environment.apiUrl + "/api/geoobjecttype/addterm", JSON.stringify(params), { headers: headers })
            .toPromise();
    }

    removeTerm(parentTermCode: string, termCode: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http
            .post<void>(environment.apiUrl + "/api/geoobjecttype/deleteterm", JSON.stringify({ parentTermCode: parentTermCode, termCode: termCode }), { headers: headers })
            .toPromise();
    }

}
