import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";
import { BusinessType, BusinessTypeByOrg } from "@registry/model/business-type";
import { AttributeType } from "@registry/model/registry";
import { GeoRegistryConfiguration } from "@core/model/registry";
import { GenericTableService } from "@shared/model/generic-table";
import { PageResult } from "@shared/model/core";

declare let registry: GeoRegistryConfiguration;

@Injectable()
export class BusinessTypeService implements GenericTableService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    getByOrganization(): Promise<BusinessTypeByOrg[]> {
        let params: HttpParams = new HttpParams();

        this.eventService.start();

        return this.http.get<BusinessTypeByOrg[]>(registry.contextPath + "/api/business-type/get-by-org", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getAll(): Promise<BusinessType[]> {
        let params: HttpParams = new HttpParams();

        this.eventService.start();

        return this.http.get<BusinessType[]>(registry.contextPath + "/api/business-type/get-all", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    get(oid: string): Promise<BusinessType> {
        let params: HttpParams = new HttpParams();
        params = params.append("oid", oid);

        this.eventService.start();

        return this.http.get<BusinessType>(registry.contextPath + "/api/business-type/get", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    apply(type: BusinessType): Promise<BusinessType> {
        const data = new FormData();
        data.append("type", JSON.stringify(type));

        this.eventService.start();

        return this.http
            .post<BusinessType>(registry.contextPath + "/api/business-type/apply", data)
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    remove(type: BusinessType): Promise<BusinessType> {
        const data = new FormData();
        data.append("oid", type.oid);

        this.eventService.start();

        return this.http
            .post<BusinessType>(registry.contextPath + "/api/business-type/remove", data)
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    edit(oid: string): Promise<BusinessType> {
        const data = new FormData();
        data.append("oid", oid);

        this.eventService.start();

        return this.http
            .post<BusinessType>(registry.contextPath + "/api/business-type/edit", data)
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    unlock(oid: string): Promise<BusinessType> {
        const data = new FormData();
        data.append("oid", oid);

        this.eventService.start();

        return this.http
            .post<BusinessType>(registry.contextPath + "/api/business-type/unlock", data)
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    addAttributeType(typeCode: string, attribute: AttributeType): Promise<AttributeType> {
        const data = new FormData();
        data.append("typeCode", typeCode);
        data.append("attributeType", JSON.stringify(attribute));

        this.eventService.start();

        return this.http
            .post<AttributeType>(registry.contextPath + "/api/business-type/add-attribute", data)
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    updateAttributeType(typeCode: string, attribute: AttributeType): Promise<AttributeType> {
        const data = new FormData();
        data.append("typeCode", typeCode);
        data.append("attributeType", JSON.stringify(attribute));

        this.eventService.start();

        return this.http
            .post<AttributeType>(registry.contextPath + "/api/business-type/update-attribute", data)
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    deleteAttributeType(typeCode: string, attributeName: string): Promise<boolean> {
        const data = new FormData();
        data.append("typeCode", typeCode);
        data.append("attributeName", attributeName);

        this.eventService.start();

        return this.http
            .post<boolean>(registry.contextPath + "/api/business-type/remove-attribute", data)
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
            .get<PageResult<Object>>(registry.contextPath + "/api/business-type/data", { params: params })
            .toPromise();
    }

}
