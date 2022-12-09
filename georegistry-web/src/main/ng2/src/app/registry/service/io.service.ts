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
            .post<ImportConfiguration>(registry.contextPath + "/api/etl/import", JSON.stringify({ config: configuration }), { headers: headers })
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
            .post<void>(registry.contextPath + "/api/etl/cancel-import", JSON.stringify({ config: configuration }), { headers: headers })
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
            .post<ImportConfiguration>(registry.contextPath + "/api/etl/import", JSON.stringify({ config: configuration }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    listGeoObjectTypes(includeAbstractTypes: boolean): Promise<{ label: string, code: string, orgCode: string, superTypeCode?: string }[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("includeAbstractTypes", includeAbstractTypes.toString());

        return this.http
            .get<{ label: string, code: string, orgCode: string }[]>(registry.contextPath + "/api/geoobjecttype/list-types", { params: params })
            .toPromise();
    }

    getTypeAncestors(code: string, hierarchyCode: string, includeInheritedTypes: boolean, includeChild: boolean = false): Promise<Location[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("code", code);
        params = params.set("hierarchyCode", hierarchyCode);
        params = params.set("includeInheritedTypes", includeInheritedTypes.toString());
        params = params.set("includeChild", includeChild.toString());

        return this.http
            .get<Location[]>(registry.contextPath + "/api/geoobjecttype/get-ancestors", { params: params })
            .toPromise();
    }

    getHierarchiesForType(code: string, includeTypes: boolean): Promise<{ label: string, code: string, parents: { label: string, code: string }[] }[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("code", code);
        params = params.set("includeTypes", includeTypes.toString());

        this.eventService.start();

        return this.http
            .get<{ label: string, code: string, parents: { label: string, code: string }[] }[]>(registry.contextPath + "/api/geoobjecttype/get-hierarchies", { params: params })
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
            .get<{ label: string, code: string, parents: { label: string, code: string }[] }[]>(registry.contextPath + "/api/geoobjecttype/get-subtype-hierarchies", { params: params })
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
            .get<any>(registry.contextPath + "/api/geoobject/suggestions", { params: params })
            .toPromise();
    }

    createGeoObjectSynonym(entityId: string, label: string): Promise<Synonym> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<Synonym>(registry.contextPath + "/geo-synonym/create-geo-entity-synonym", JSON.stringify({ entityId: entityId, label: label }), { headers: headers })
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
            .post<void>(registry.contextPath + "/geo-synonym/create-geo-entity-synonym", JSON.stringify({ oid: oid }), { headers: headers })
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
            .post<Term>(registry.contextPath + "/api/geoobjecttype/addterm", JSON.stringify(params), { headers: headers })
            .toPromise();
    }

    removeTerm(parentTermCode: string, termCode: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http
            .post<void>(registry.contextPath + "/api/geoobjecttype/deleteterm", JSON.stringify({ parentTermCode: parentTermCode, termCode: termCode }), { headers: headers })
            .toPromise();
    }

}
