import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";

import { GeoRegistryConfiguration } from "@core/model/registry";
import { Classification } from "@registry/model/classification-type";

declare let registry: GeoRegistryConfiguration;

@Injectable()
export class ClassificationService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    apply(classificationCode: string, parentCode: string, classification: Classification, isNew: boolean): Promise<Classification> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        const params = {
            classificationCode: classificationCode,
            classification: classification,
            isNew: isNew
        };

        if (parentCode != null) {
            params["parentCode"] = parentCode;
        }

        this.eventService.start();

        return this.http
            .post<Classification>(registry.contextPath + "/classification/apply", JSON.stringify(params), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    remove(classificationCode: string, code: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        const params = {
            classificationCode: classificationCode,
            code: code
        };

        this.eventService.start();

        return this.http
            .post<void>(registry.contextPath + "/classification/remove", JSON.stringify(params), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getChildren(classificationCode: string, code: string): Promise<Classification[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("classificationCode", classificationCode);

        if (code != null) {
            params = params.set("code", code);
        }

        return this.http.get<Classification[]>(registry.contextPath + "/classification/get-children", { params: params })
            .toPromise();
    }

}
