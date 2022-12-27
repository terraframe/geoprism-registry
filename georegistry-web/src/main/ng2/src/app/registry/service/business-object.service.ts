import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";
import { BusinessObject, BusinessType } from "@registry/model/business-type";
import { GeoRegistryConfiguration } from "@core/model/core";

import { environment } from 'src/environments/environment';

@Injectable()
export class BusinessObjectService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    get(businessTypeCode: string, code: string): Promise<BusinessObject> {
        let params: HttpParams = new HttpParams();
        params = params.append("businessTypeCode", businessTypeCode);
        params = params.append("code", code);

        this.eventService.start();

        return this.http.get<BusinessObject>(environment.apiUrl + "/api/business-object/get", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getTypeAndObject(businessTypeCode: string, code: string): Promise<{type:BusinessType, object: BusinessObject}> {
        let params: HttpParams = new HttpParams();
        params = params.append("businessTypeCode", businessTypeCode);
        params = params.append("code", code);

        this.eventService.start();

        return this.http.get<{type:BusinessType, object: BusinessObject}>(environment.apiUrl + "/api/business-object/get-type-and-object", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

}
