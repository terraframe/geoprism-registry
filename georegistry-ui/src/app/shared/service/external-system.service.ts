import { Injectable } from "@angular/core";
import { HttpHeaders, HttpClient, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";
import { EventService } from "@shared/service";
import { ExternalSystem, PageResult, SystemCapabilities } from "@shared/model/core";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

@Injectable()
export class ExternalSystemService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient, private eventService: EventService) { }

    getExternalSystems(pageNumber: number, pageSize: number): Promise<PageResult<ExternalSystem>> {
        let params: HttpParams = new HttpParams();
        params = params.set("pageNumber", pageNumber.toString());
        params = params.set("pageSize", pageSize.toString());

        this.eventService.start();

        return this.http
            .get<PageResult<ExternalSystem>>(environment.apiUrl + "/api/external-system/get-all", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getExternalSystem(oid: string): Promise<ExternalSystem> {
        let params: HttpParams = new HttpParams();
        params = params.set("oid", oid);

        this.eventService.start();

        return this.http
            .get<ExternalSystem>(environment.apiUrl + "/api/external-system/get", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    applyExternalSystem(system: ExternalSystem): Promise<ExternalSystem> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<ExternalSystem>(environment.apiUrl + "/api/external-system/apply", JSON.stringify({ system: system }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    getSystemCapabilities(system: ExternalSystem): Promise<SystemCapabilities> {
        let params: HttpParams = new HttpParams();
        params = params.set("system", JSON.stringify(system));

        this.eventService.start();

        return this.http
            .get<SystemCapabilities>(environment.apiUrl + "/api/external-system/system-capabilities", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    removeExternalSystem(oid: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<any>(environment.apiUrl + "/api/external-system/remove", JSON.stringify({ oid: oid }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

}
