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

import {
    GeoObject, GeoObjectType, AttributeType, Term, ParentTreeNode,
    ChildTreeNode, GeoObjectOverTime, HierarchyOverTime, ScheduledJob
} from "@registry/model/registry";

import { HierarchyType } from "@registry/model/hierarchy";
import { Progress } from "@shared/model/progress";

import { Organization, PageResult } from "@shared/model/core";
import { EventService } from "@shared/service";

import { environment } from 'src/environments/environment';
import { LocaleView } from "@core/model/core";

export interface AttributeTypeService {
    addAttributeType(geoObjTypeId: string, attribute: AttributeType): Promise<AttributeType>;

    updateAttributeType(geoObjTypeId: string, attribute: AttributeType): Promise<AttributeType>;

    deleteAttributeType(geoObjTypeId: string, attributeName: string): Promise<boolean>;
}

@Injectable()
export class RegistryService implements AttributeTypeService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    init(): Promise<{ types: GeoObjectType[], hierarchies: HierarchyType[], organizations: Organization[], locales: LocaleView[] }> {
        return this.http.get<{ types: GeoObjectType[], hierarchies: HierarchyType[], organizations: Organization[], locales: LocaleView[] }>(environment.apiUrl + "/api/cgr/init")
            .toPromise();
    }

    // param types: array of GeoObjectType codes. If empty array then all GeoObjectType objects are returned.
    getGeoObjectTypes(types: string[], hierarchies: string[]): Promise<GeoObjectType[]> {
        let params: HttpParams = new HttpParams();

        if (types != null) {
            params = params.set("types", JSON.stringify(types));
        }

        if (hierarchies != null) {
            params = params.set("hierarchies", JSON.stringify(hierarchies));
        }

        return this.http
            .get<GeoObjectType[]>(environment.apiUrl + "/api/geoobjecttype/get-all", { params: params })
            .toPromise();
    }

    getParentGeoObjects(childCode: string, childTypeCode: string, parentTypes: any, recursive: boolean, date: string): Promise<ParentTreeNode> {
        let params: HttpParams = new HttpParams();

        params = params.set("childCode", childCode);
        params = params.set("childTypeCode", childTypeCode);
        params = params.set("parentTypes", JSON.stringify(parentTypes));
        params = params.set("recursive", JSON.stringify(recursive));

        if (date != null) {
            params = params.set("date", date);
        }

        return this.http
            .get<ParentTreeNode>(environment.apiUrl + "/api/geoobject/get-parent-geoobjects", { params: params })
            .toPromise();
    }

    getChildGeoObjects(parentCode: string, parentTypeCode: string, childrenTypes: any, recursive: boolean): Promise<ChildTreeNode> {
        let params: HttpParams = new HttpParams();

        params = params.set("parentCode", parentCode);
        params = params.set("parentTypeCode", parentTypeCode);
        params = params.set("childrenTypes", JSON.stringify(childrenTypes));
        params = params.set("recursive", JSON.stringify(recursive));

        return this.http
            .get<ChildTreeNode>(environment.apiUrl + "/api/geoobject/getchildren", { params: params })
            .toPromise();
    }

    doesGeoObjectExistAtRange(startDate: string, endDate: string, typeCode: string, code: string): Promise<{ exists: boolean, invalid: boolean }> {
        let params: HttpParams = new HttpParams();

        params = params.set("startDate", startDate);
        params = params.set("endDate", endDate);
        params = params.set("typeCode", typeCode);
        params = params.set("code", code);

        return this.http
            .get<{ exists: boolean, invalid: boolean }>(environment.apiUrl + "/api/geoobject/exists-at-range", { params: params })
            .toPromise();
    }

    newGeoObjectInstance(typeCode: string): Promise<any> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<any>(environment.apiUrl + "/api/geoobject/newGeoObjectInstance", JSON.stringify({ typeCode: typeCode }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    createGeoObjectType(gtJSON: GeoObjectType): Promise<GeoObjectType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<GeoObjectType>(environment.apiUrl + "/api/geoobjecttype/create", JSON.stringify({ gtJSON: gtJSON }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    updateGeoObjectType(gtJSON: GeoObjectType): Promise<GeoObjectType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<GeoObjectType>(environment.apiUrl + "/api/geoobjecttype/update", JSON.stringify({ gtJSON: gtJSON }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    deleteGeoObjectType(code: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<void>(environment.apiUrl + "/api/geoobjecttype/delete", JSON.stringify({ code: code }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    addAttributeType(geoObjTypeCode: string, attribute: AttributeType): Promise<AttributeType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<AttributeType>(environment.apiUrl + "/api/geoobjecttype/addattribute", JSON.stringify({ geoObjTypeCode: geoObjTypeCode, attributeType: attribute }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    updateAttributeType(geoObjTypeCode: string, attribute: AttributeType): Promise<AttributeType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<AttributeType>(environment.apiUrl + "/api/geoobjecttype/updateattribute", JSON.stringify({ geoObjTypeCode: geoObjTypeCode, attributeType: attribute }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    deleteAttributeType(geoObjTypeId: string, attributeName: string): Promise<boolean> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<boolean>(environment.apiUrl + "/api/geoobjecttype/deleteattribute", JSON.stringify({ geoObjTypeId: geoObjTypeId, attributeName: attributeName }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    addAttributeTermTypeOption(parentTermCode: string, term: Term): Promise<Term> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<Term>(environment.apiUrl + "/api/geoobjecttype/addterm", JSON.stringify({ parentTermCode: parentTermCode, termJSON: term }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    updateAttributeTermTypeOption(parentTermCode: string, termJSON: Term): Promise<Term> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<Term>(environment.apiUrl + "/api/geoobjecttype/updateterm", JSON.stringify({ parentTermCode: parentTermCode, termJSON: termJSON }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    deleteAttributeTermTypeOption(parentTermCode: string, termCode: string): Promise<AttributeType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<AttributeType>(environment.apiUrl + "/api/geoobjecttype/deleteterm", JSON.stringify({ parentTermCode: parentTermCode, termCode: termCode }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getGeoObject(id: string, typeCode: string, showOverlay: boolean = true): Promise<GeoObject> {
        let params: HttpParams = new HttpParams();

        params = params.set("id", id);
        params = params.set("typeCode", typeCode);

        if (showOverlay) {
            this.eventService.start();
        }

        return this.http
            .get<GeoObject>(environment.apiUrl + "/api/geoobject/get", { params: params })
            .pipe(finalize(() => {
                if (showOverlay) {
                    this.eventService.complete();
                }
            }))
            .toPromise();
    }

    getGeoObjectBounds(code: string, typeCode: string): Promise<number[]> {
        let params: HttpParams = new HttpParams();

        params = params.set("code", code);
        params = params.set("typeCode", typeCode);

        return this.http
            .get<number[]>(environment.apiUrl + "/api/geoobject/get-bounds", { params: params })
            .toPromise();
    }

    getGeoObjectBoundsAtDate(code: string, typeCode: string, date: string): Promise<number[]> {
        let params: HttpParams = new HttpParams();

        params = params.set("code", code);
        params = params.set("typeCode", typeCode);

        if (date != null) {
            params = params.set("date", date);
        }

        return this.http
            .get<number[]>(environment.apiUrl + "/api/geoobject-time/get-bounds", { params: params })
            .toPromise();
    }

    getGeoObjectByCode(code: string, typeCode: string): Promise<GeoObject> {
        let params: HttpParams = new HttpParams();

        params = params.set("code", code);
        params = params.set("typeCode", typeCode);

        return this.http
            .get<GeoObject>(environment.apiUrl + "/api/geoobject/get-code", { params: params })
            .toPromise();
    }

    getHierarchiesForGeoObject(code: string, typeCode: string, showOverlay: boolean = true): Promise<HierarchyOverTime[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("code", code);
        params = params.set("typeCode", typeCode);

        if (showOverlay) {
            this.eventService.start();
        }

        return this.http
            .get<HierarchyOverTime[]>(environment.apiUrl + "/api/geoobject/get-hierarchies-over-time", { params: params })
            .pipe(finalize(() => {
                if (showOverlay) {
                    this.eventService.complete();
                }
            }))
            .toPromise();
    }

    getGeoObjectSuggestions(text: string, type: string, parent: string, parentTypeCode: string, hierarchy: string, startDate: string, endDate: string): Promise<{ id: string, code: string, name: string, typeCode: string, uid: string }[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("text", text);
        params = params.set("type", type);

        if (parent != null && hierarchy != null) {
            params = params.set("parent", parent);
            params = params.set("hierarchy", hierarchy);
        }

        if (parentTypeCode != null) {
            params = params.set("parentTypeCode", parentTypeCode);
        }

        if (startDate != null && endDate != null) {
            params = params.set("startDate", startDate);
            params = params.set("endDate", endDate);
        }

        return this.http
            .get<{ id: string, code: string, name: string, typeCode: string, uid: string }[]>(environment.apiUrl + "/api/geoobject/suggestions", { params: params })
            .toPromise();
    }

    getGeoObjectSuggestionsTypeAhead(text: string, type: string): Promise<{ id: string, code: string, name: string, typeCode: string, uid: string }[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("text", text);
        params = params.set("type", type);

        return this.http
            .get<{ id: string, code: string, name: string, typeCode: string, uid: string }[]>(environment.apiUrl + "/api/geoobject/suggestions", { params: params })
            .toPromise();
    }

    getScheduledJobs(pageSize: number, pageNumber: number, sortAttr: string, isAscending: boolean): Promise<PageResult<any>> {
        let params: HttpParams = new HttpParams();
        params = params.set("pageSize", pageSize.toString());
        params = params.set("pageNumber", pageNumber.toString());
        params = params.set("sortAttr", sortAttr);
        params = params.set("isAscending", isAscending.toString());

        return this.http
            .get<PageResult<any>>(environment.apiUrl + "/api/etl/get-active", { params: params })
            .toPromise();
    }

    getCompletedScheduledJobs(pageSize: number, pageNumber: number, sortAttr: string, isAscending: boolean): Promise<PageResult<any>> {
        let params: HttpParams = new HttpParams();
        params = params.set("pageSize", pageSize.toString());
        params = params.set("pageNumber", pageNumber.toString());
        params = params.set("sortAttr", sortAttr);
        params = params.set("isAscending", isAscending.toString());

        return this.http
            .get<PageResult<any>>(environment.apiUrl + "/api/etl/get-completed", { params: params })
            .toPromise();
    }

    getScheduledJob(historyId: string, pageSize: number, pageNumber: number, onlyUnresolved: boolean): Promise<ScheduledJob> {
        let params: HttpParams = new HttpParams();
        params = params.set("historyId", historyId);
        params = params.set("pageSize", pageSize.toString());
        params = params.set("pageNumber", pageNumber.toString());
        params = params.set("onlyUnresolved", onlyUnresolved.toString());

        return this.http
            .get<ScheduledJob>(environment.apiUrl + "/api/etl/get-import-details", { params: params })
            .toPromise();
    }

    getExportDetails(historyId: string, pageSize: number, pageNumber: number): Promise<ScheduledJob> {
        let params: HttpParams = new HttpParams();
        params = params.set("historyId", historyId);
        params = params.set("pageSize", pageSize.toString());
        params = params.set("pageNumber", pageNumber.toString());

        return this.http
            .get<ScheduledJob>(environment.apiUrl + "/api/etl/get-export-details", { params: params })
            .toPromise();
    }

    resolveScheduledJob(historyId: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<void>(environment.apiUrl + "/api/etl/import-resolve", JSON.stringify({ historyId: historyId }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    submitValidationResolve(config: any): Promise<any> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<any>(environment.apiUrl + "/api/etl/validation-resolve", JSON.stringify({ config: config }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    submitErrorResolve(config: any): Promise<any> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<any>(environment.apiUrl + "/api/etl/error-resolve", JSON.stringify({ config: config }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getLocales(): Promise<LocaleView[]> {
        let params: HttpParams = new HttpParams();

        return this.http
            .get<LocaleView[]>(environment.apiUrl + "/api/localization/get-locales", { params: params })
            .toPromise();
    }

    getGeoObjectOverTime(geoObjectCode: string, geoObjectTypeCode: string): Promise<GeoObjectOverTime> {
        let params: HttpParams = new HttpParams();
        params = params.set("code", geoObjectCode);
        params = params.set("typeCode", geoObjectTypeCode);

        return this.http
            .get<GeoObjectOverTime>(environment.apiUrl + "/api/geoobject-time/get-code", { params: params })
            .toPromise();
    }

    newGeoObjectOverTime(typeCode: string, showOverlay: boolean = true): Promise<any> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        if (showOverlay) {
            this.eventService.start();
        }

        return this.http
            .post<any>(environment.apiUrl + "/api/geoobject-time/newGeoObjectInstance", JSON.stringify({ typeCode: typeCode }), { headers: headers })
            .pipe(finalize(() => {
                if (showOverlay) {
                    this.eventService.complete();
                }
            }))
            .toPromise();
    }

    /*
     * Not really part of the RegistryService
     */
    applyGeoObjectEdit(geoObjectCode: string, geoObjectTypeCode: string, actions: string, masterListId: string, notes: string, showOverlay: boolean = true): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        // TODO
        // Custom attributes of Date type need to be encoded to date/time. The Date picker requires this format to be yyyy-mm-dd.
        // This conversion allows the date picker to work while ensuring the server recieves the correct format.
        // for(const prop in geoObject.attributes) {
        //	let attr = geoObject.attributes[prop];
        //	if(attr.type === "date"){
        //		attr.values.forEach( val => {
        //			val.value = new Date(val.value).getTime().toString();
        //		})
        //	}
        // }

        let params = { geoObjectCode: geoObjectCode, geoObjectTypeCode: geoObjectTypeCode, actions: actions };

        if (masterListId != null) {
            params["masterListId"] = masterListId;
        }
        if (notes != null) {
            params["notes"] = notes;
        }

        if (showOverlay) {
            this.eventService.start();
        }

        return this.http
            .post<void>(environment.apiUrl + "/api/geoobject-editor/update-geo-object", JSON.stringify(params), { headers: headers })
            .pipe(finalize(() => {
                if (showOverlay) {
                    this.eventService.complete();
                }
            }))
            .toPromise();
    }

    /*
    * Not really part of the RegistryService
    */
    applyGeoObjectCreate(parentTreeNode: HierarchyOverTime[], geoObject: GeoObjectOverTime, isNew: boolean, masterListId: string, notes: string, showOverlay: boolean = true): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        // Custom attributes of Date type need to be encoded to date/time. The Date picker requires this format to be yyyy-mm-dd.
        // This conversion allows the date picker to work while ensuring the server recieves the correct format.
        for (const prop in geoObject.attributes) {
            let attr = geoObject.attributes[prop];
            if (attr.type === "date") {
                attr.values.forEach(val => {
                    val.value = new Date(val.value).getTime().toString();
                });
            }
        }

        let params = { geoObject: geoObject, isNew: isNew, masterListId: masterListId };

        if (parentTreeNode != null) {
            params["parentTreeNode"] = parentTreeNode;
        }
        if (notes != null) {
            params["notes"] = notes;
        }

        if (showOverlay) {
            this.eventService.start();
        }

        return this.http
            .post<void>(environment.apiUrl + "/api/geoobject-editor/create-geo-object", JSON.stringify(params), { headers: headers })
            .pipe(finalize(() => {
                if (showOverlay) {
                    this.eventService.complete();
                }
            }))
            .toPromise();
    }

    progress(oid: string): Promise<Progress> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        return this.http
            .get<Progress>(environment.apiUrl + "/master-list/progress", { params: params })
            .toPromise();
    }

    getDatasetBounds(oid: string): Promise<number[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        return this.http
            .get<number[]>(environment.apiUrl + "/master-list/bounds", { params: params })
            .toPromise();
    }

    getOrganizations(): Promise<Organization[]> {
        this.eventService.start();

        return this.http
            .get<Organization[]>(environment.apiUrl + "/api/organization/get-all")
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

}
