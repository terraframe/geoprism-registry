import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";
import * as mapboxgl from "mapbox-gl";

import { LocationInformation } from "@registry/model/location-manager";
import { EventService } from "@shared/service";
import { GeoObject } from "@registry/model/registry";

import { GeoRegistryConfiguration } from "@core/model/registry";
declare let registry: GeoRegistryConfiguration;

@Injectable()
export class MapService {

    constructor(private http: HttpClient, private eventService: EventService) {
        (mapboxgl as any).accessToken = registry.mapboxAccessToken;
    }

    roots(typeCode: string, hierarchyCode: string, date: string): Promise<LocationInformation> {
        let params: HttpParams = new HttpParams();

        if (typeCode != null) {
            params = params.set("typeCode", typeCode);
        }

        if (hierarchyCode != null) {
            params = params.set("hierarchyCode", hierarchyCode);
        }

        if (date != null) {
            params = params.set("date", date);
        }

        this.eventService.start();

        return this.http
            .get<LocationInformation>(registry.contextPath + "/registrylocation/roots", { params: params })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    search(text: string, date: string, showOverlay: boolean = true): Promise<{ type: string, features: GeoObject[] }> {
        let params: HttpParams = new HttpParams();
        params = params.set("text", text);

        if (date != null) {
            params = params.set("date", date);
        }

        if (showOverlay) {
            this.eventService.start();
        }

        return this.http
            .get<{ type: string, features: GeoObject[] }>(registry.contextPath + "/registrylocation/search", { params: params })
            .pipe(finalize(() => {
                if (showOverlay) {
                    this.eventService.complete();
                }
            }))
            .toPromise();
    }

}
