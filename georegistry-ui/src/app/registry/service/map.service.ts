///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { finalize } from "rxjs/operators";
import * as mapboxgl from "mapbox-gl";

import { LocationInformation } from "@registry/model/location-manager";
import { EventService } from "@shared/service";
import { GeoObject } from "@registry/model/registry";

import { environment } from 'src/environments/environment';
import { ConfigurationService } from "@core/service/configuration.service";
import { firstValueFrom } from "rxjs";

@Injectable()
export class MapService {

    constructor(private configuration: ConfigurationService, private http: HttpClient, private eventService: EventService) {
        (mapboxgl as any).accessToken = configuration.getMapboxAccessToken();
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
            .get<LocationInformation>(environment.apiUrl + "/api/registrylocation/roots", { params: params })
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
            .get<{ type: string, features: GeoObject[] }>(environment.apiUrl + "/api/registrylocation/search", { params: params })
            .pipe(finalize(() => {
                if (showOverlay) {
                    this.eventService.complete();
                }
            }))
            .toPromise();
    }

    labels(text: string, date: string, showOverlay: boolean = true): Promise<{ label: string, name: string, code: string }> {
        let params: HttpParams = new HttpParams();
        params = params.set("text", text);

        if (date != null) {
            params = params.set("date", date);
        }

        if (showOverlay) {
            this.eventService.start();
        }

        return firstValueFrom(this.http
            .get<{ label: string, name: string, code: string }>(environment.apiUrl + "/api/registrylocation/labels", { params: params })
            .pipe(finalize(() => {
                if (showOverlay) {
                    this.eventService.complete();
                }
            })));
    }

}
