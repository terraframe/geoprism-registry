import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";

import { GeoRegistryConfiguration } from "@core/model/core";
import { PageResult } from "@shared/model/core";
import { GenericTableService } from "@shared/model/generic-table";
import { ClassificationType } from "@registry/model/classification-type";

import { environment } from 'src/environments/environment';

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
            .post<ClassificationType>(environment.apiUrl + "/classification-type/apply", JSON.stringify({ classificationType: classificationType }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    remove(type: ClassificationType): Promise<ClassificationType> {
        const data = new FormData();
        data.append("oid", type.oid);

        this.eventService.start();

        return this.http
            .post<ClassificationType>(environment.apiUrl + "/classification-type/remove", data)
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    page(criteria: Object): Promise<PageResult<ClassificationType>> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        let params = {
            criteria: criteria
        } as any;

        return this.http.post<PageResult<ClassificationType>>(environment.apiUrl + "/classification-type/page", JSON.stringify(params), { headers: headers })
            .toPromise();
    }

    get(classificationCode: string): Promise<ClassificationType> {
        let params: HttpParams = new HttpParams();
        params = params.set("classificationCode", classificationCode);

        return this.http.get<ClassificationType>(environment.apiUrl + "/classification-type/get", { params: params })
            .toPromise();
    }

}
