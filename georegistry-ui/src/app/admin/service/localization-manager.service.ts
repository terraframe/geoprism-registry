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
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { finalize } from "rxjs/operators";

import { EventService } from "@shared/service";

import { AllLocaleInfo } from "@admin/model/localization-manager";

import { environment } from 'src/environments/environment';
import { LocaleView } from "@core/model/core";

@Injectable()
export class LocalizationManagerService {

    constructor(private http: HttpClient, private eventService: EventService) { }

    getNewLocaleInfo(): Promise<AllLocaleInfo> {
        this.eventService.start();

        return this.http
            .get<AllLocaleInfo>(environment.apiUrl + "/api/localization/getNewLocaleInformation")
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
            .post<LocaleView>(environment.apiUrl + "/api/localization/editLocale", JSON.stringify({ json: locale }), { headers: headers })
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
            .post<LocaleView>(environment.apiUrl + "/api/localization/installLocale", JSON.stringify({ json: locale }), { headers: headers })
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
            .post<{ locale: string }>(environment.apiUrl + "/api/localization/uninstallLocale", JSON.stringify({ json: locale }), { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

    installFile(formData: FormData): Promise<void> {
        let headers = new HttpHeaders();

        this.eventService.start();

        return this.http.post<void>(environment.apiUrl + "/api/localization/importSpreadsheet", formData, { headers: headers })
            .pipe(finalize(() => {
                this.eventService.complete();
            }))
            .toPromise();
    }

}
