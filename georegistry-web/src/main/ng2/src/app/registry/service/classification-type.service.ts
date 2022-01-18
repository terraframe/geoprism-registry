import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders } from "@angular/common/http";
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

    apply(type: ClassificationType): Promise<ClassificationType> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ClassificationType>(registry.contextPath + "/classification-type/apply", JSON.stringify({ classificationType: type }), { headers: headers })
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
            .post<ClassificationType>(registry.contextPath + "/classification-type/remove", JSON.stringify({ oid: type.oid }), { headers: headers })
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

        return this.http.post<PageResult<ClassificationType>>(registry.contextPath + "/classification-type/page", JSON.stringify(params), { headers: headers })
            .toPromise();
    }

}
