import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";

import { GeoRegistryConfiguration } from "@core/model/registry";
import { PageResult } from "@shared/model/core";
import { GenericTableService } from "@shared/model/generic-table";
import { ClassificationType } from "@registry/model/classification-type";

declare let registry: GeoRegistryConfiguration;

@Injectable()
export class ClassificationTypeService implements GenericTableService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    apply(classificationType: ClassificationType): Promise<ClassificationType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ClassificationType>(registry.contextPath + "/api/classification-type/apply", JSON.stringify({ classificationType: classificationType }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    remove(type: ClassificationType): Promise<ClassificationType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ClassificationType>(registry.contextPath + "/api/classification-type/remove", JSON.stringify({ oid: type.oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    page(criteria: Object): Promise<PageResult<ClassificationType>> {
        let params: HttpParams = new HttpParams();
        params = params.set("criteria", JSON.stringify(criteria));

        return this.http.get<PageResult<ClassificationType>>(registry.contextPath + "/api/classification-type/page", { params: params })
            .toPromise();
    }

    get(classificationCode: string): Promise<ClassificationType> {
        let params: HttpParams = new HttpParams();
        params = params.set("classificationCode", classificationCode);

        return this.http.get<ClassificationType>(registry.contextPath + "/api/classification-type/get", { params: params })
            .toPromise();
    }

}
