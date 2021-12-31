import { Injectable } from "@angular/core";
import { HttpHeaders, HttpClient, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { ImportConfiguration, Synonym, Location, Term } from "@registry/model/io";
import { EventService } from "@shared/service";

import { GeoRegistryConfiguration } from "@core/model/registry"; declare let registry: GeoRegistryConfiguration;

@Injectable()
export class IOService {

    constructor(private http: HttpClient, private eventService: EventService) { }

    importSpreadsheet(configuration: ImportConfiguration): Promise<ImportConfiguration> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ImportConfiguration>(registry.contextPath + "/etl/import", JSON.stringify({ json: configuration }), { headers: headers })
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
            .post<void>(registry.contextPath + "/etl/cancel-import", JSON.stringify({ configuration: configuration }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    importShapefile(configuration: ImportConfiguration): Promise<ImportConfiguration> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ImportConfiguration>(registry.contextPath + "/etl/import", JSON.stringify({ json: configuration }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    listGeoObjectTypes(includeAbstractTypes: boolean): Promise<{ label: string, code: string, orgCode: string, superTypeCode?: string }[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("includeAbstractTypes", includeAbstractTypes.toString());

        return this.http
            .get<{ label: string, code: string, orgCode: string }[]>(registry.contextPath + "/cgr/geoobjecttype/list-types", { params: params })
            .toPromise();
    }

    getTypeAncestors(code: string, hierarchyCode: string, includeInheritedTypes: boolean): Promise<Location[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("code", code);
        params = params.set("hierarchyCode", hierarchyCode);
        params = params.set("includeInheritedTypes", includeInheritedTypes.toString());

        return this.http
            .get<Location[]>(registry.contextPath + "/cgr/geoobjecttype/get-ancestors", { params: params })
            .toPromise();
    }

    getHierarchiesForType(code: string, includeTypes: boolean): Promise<{ label: string, code: string, parents: { label: string, code: string }[] }[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("code", code);
        params = params.set("includeTypes", includeTypes.toString());

        this.eventService.start();

        return this.http
            .get<{ label: string, code: string, parents: { label: string, code: string }[] }[]>(registry.contextPath + "/cgr/geoobjecttype/get-hierarchies", { params: params })
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
            .get<{ label: string, code: string, parents: { label: string, code: string }[] }[]>(registry.contextPath + "/cgr/geoobjecttype/get-subtype-hierarchies", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getGeoObjectSuggestions(text: string, type: string, parent: string, hierarchy: string): Promise<any> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        let params = {
            text: text,
            type: type
        } as any;

        if (parent != null && hierarchy != null) {
            params.parent = parent;
            params.hierarchy = parent;
        }

        return this.http
            .post<any>(registry.contextPath + "/cgr/geoobject/suggestions", JSON.stringify(params), { headers: headers })
            .toPromise();
    }

    createGeoObjectSynonym(entityId: string, label: string): Promise<Synonym> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<Synonym>(registry.contextPath + "/geo-synonym/createGeoEntitySynonym", JSON.stringify({ entityId: entityId, label: label }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    deleteGeoObjectSynonym(synonymId: string, vOid: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<void>(registry.contextPath + "/geo-synonym/deleteGeoEntitySynonym", JSON.stringify({ synonymId: synonymId, vOid: vOid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getTermSuggestions(mdAttributeId: string, text: string, limit: string): Promise<{ text: string, data: any }[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("mdAttributeId", mdAttributeId);
        params = params.set("text", text);
        params = params.set("limit", limit);

        return this.http
            .get<{ text: string, data: any }[]>(registry.contextPath + "/uploader/getClassifierSuggestions", { params: params })
            .toPromise();
    }

    createTermSynonym(classifierId: string, label: string): Promise<Synonym> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        let data = JSON.stringify({ classifierId: classifierId, label: label });

        return this.http
            .post<Synonym>(registry.contextPath + "/uploader/createClassifierSynonym", data, { headers: headers })
            .toPromise();
    }

    deleteTermSynonym(synonymId: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        let data = JSON.stringify({ synonymId: synonymId });

        return this.http
            .post<void>(registry.contextPath + "/uploader/deleteClassifierSynonym", data, { headers: headers })
            .toPromise();
    }

    createTerm(label: string, code: string, parentTermCode: string): Promise<Term> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        let params = { parentTermCode: parentTermCode, termJSON: { label: label, code: code } };

        return this.http
            .post<Term>(registry.contextPath + "/cgr/geoobjecttype/addterm", JSON.stringify(params), { headers: headers })
            .toPromise();
    }

    removeTerm(parentTermCode: string, termCode: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http
            .post<void>(registry.contextPath + "/cgr/geoobjecttype/deleteterm", JSON.stringify({ parentTermCode: parentTermCode, termCode: termCode }), { headers: headers })
            .toPromise();
    }

}
