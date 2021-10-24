import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";
import { BusinessType, BusinessTypeByOrg } from "@registry/model/business-type";
import { AttributeType } from "@registry/model/registry";


declare let acp: any;

@Injectable()
export class BusinessTypeService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    getByOrganization(): Promise<BusinessTypeByOrg[]> {
        let params: HttpParams = new HttpParams();

        this.eventService.start();

        return this.http.get<BusinessTypeByOrg[]>(acp + "/business-type/get-by-org", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getAll(): Promise<BusinessType[]> {
        let params: HttpParams = new HttpParams();

        this.eventService.start();

        return this.http.get<BusinessType[]>(acp + "/business-type/get-all", { params: params })
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
            .post<BusinessType>(acp + "/business-type/apply", JSON.stringify({ type: type }), { headers: headers })
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
            .post<BusinessType>(acp + "/business-type/remove", JSON.stringify({ oid: type.oid }), { headers: headers })
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
            .post<BusinessType>(acp + "/business-type/edit", JSON.stringify({ oid: oid }), { headers: headers })
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
            .post<BusinessType>(acp + "/business-type/unlock", JSON.stringify({ oid: oid }), { headers: headers })
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
            .post<AttributeType>(acp + "/business-type/add-attribute", JSON.stringify({ typeCode: typeCode, attributeType: attribute }), { headers: headers })
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
            .post<AttributeType>(acp + "/business-type/update-attribute", JSON.stringify({ typeCode: typeCode, attributeType: attribute }), { headers: headers })
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
            .post<boolean>(acp + "/business-type/remove-attribute", JSON.stringify({ typeCode: typeCode, attributeName: attributeName }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }
}
