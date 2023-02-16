import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";

import { GeoRegistryConfiguration } from "@core/model/core";
import { Classification, ClassificationNode } from "@registry/model/classification-type";
import { PageResult } from "@shared/model/core";

import { environment } from 'src/environments/environment';

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
            .post<Classification>(environment.apiUrl + "/api/classification/apply", JSON.stringify(params), { headers: headers })
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
            .post<void>(environment.apiUrl + "/api/classification/remove", JSON.stringify(params), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    move(classificationCode: string, code: string, parentCode: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        const params = {
            classificationCode: classificationCode,
            code: code,
            parentCode: parentCode
        };

        this.eventService.start();

        return this.http
            .post<void>(environment.apiUrl + "/api/classification/move", JSON.stringify(params), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getChildren(classificationCode: string, code: string, pageNumber: number, pageSize: number): Promise<PageResult<Classification>> {
        let params: HttpParams = new HttpParams();
        params = params.set("classificationCode", classificationCode);
        params = params.set("pageNumber", pageNumber.toString());
        params = params.set("pageSize", pageSize.toString());

        if (code != null) {
            params = params.set("code", code);
        }

        return this.http.get<PageResult<Classification>>(environment.apiUrl + "/api/classification/get-children", { params: params })
            .toPromise();
    }

    getAncestorTree(classificationCode: string, rootCode: string, code: string, pageSize: number): Promise<ClassificationNode> {
        let params: HttpParams = new HttpParams();
        params = params.set("classificationCode", classificationCode);
        params = params.set("code", code);
        params = params.set("pageSize", pageSize.toString());

        if (rootCode != null) {
            params = params.set("rootCode", rootCode);
        }

        return this.http.get<ClassificationNode>(environment.apiUrl + "/api/classification/get-ancestor-tree", { params: params })
            .toPromise();
    }

    search(classificationCode: string, rootCode: string, text: string): Promise<Classification[]> {
        let params: HttpParams = new HttpParams();
        params = params.set("classificationCode", classificationCode);

        if (rootCode != null) {
            params = params.set("rootCode", rootCode);
        }

        if (text != null) {
            params = params.set("text", text);
        }

        return this.http.get<Classification[]>(environment.apiUrl + "/api/classification/search", { params: params })
            .toPromise();
    }

    get(classificationCode: string, code: string): Promise<Classification> {
        let params: HttpParams = new HttpParams();
        params = params.set("classificationCode", classificationCode);
        params = params.set("code", code);

        return this.http.get<Classification>(environment.apiUrl + "/api/classification/get", { params: params })
            .toPromise();
    }

}