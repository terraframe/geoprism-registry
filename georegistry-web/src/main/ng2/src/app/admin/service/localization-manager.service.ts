///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <ehttp://www.gnu.org/licenses/>.
///

import { Injectable } from "@angular/core";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";

import { LocaleView } from "@shared/model/core";

import { AllLocaleInfo } from "@admin/model/localization-manager";

import { GeoRegistryConfiguration } from "@core/model/registry";
declare let registry: GeoRegistryConfiguration;

@Injectable()
export class LocalizationManagerService {

    constructor(private http: HttpClient, private eventService: EventService) { }

    getNewLocaleInfo(): Promise<AllLocaleInfo> {
        this.eventService.start();

        return this.http
            .get<AllLocaleInfo>(registry.contextPath + "/api/localization/getNewLocaleInformation")
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    editLocale(locale: LocaleView): Promise<LocaleView> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<LocaleView>(registry.contextPath + "/api/localization/editLocale", JSON.stringify({ json: locale }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    installLocale(locale: LocaleView): Promise<LocaleView> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<LocaleView>(registry.contextPath + "/api/localization/installLocale", JSON.stringify({ json: locale }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    uninstallLocale(locale: LocaleView) {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.eventService.start();

        return this.http
            .post<{ locale: string }>(registry.contextPath + "/api/localization/uninstallLocale", JSON.stringify({ json: locale }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    installFile(formData: FormData): Promise<void> {
        let headers = new HttpHeaders();

        this.eventService.start();

        return this.http.post<void>(registry.contextPath + "/api/localization/importSpreadsheet", formData, { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

}
