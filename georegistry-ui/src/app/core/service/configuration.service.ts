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

import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { GeoRegistryConfiguration, LocaleView } from "@core/model/core";
import { firstValueFrom } from "rxjs";
import { environment } from "src/environments/environment";

@Injectable()
export class ConfigurationService {

    configuration: GeoRegistryConfiguration;

    constructor(private http: HttpClient) {
    }

    load(): Promise<GeoRegistryConfiguration> {
        return firstValueFrom(this.http.get<GeoRegistryConfiguration>(environment.apiUrl + "/api/cgr/configuration")).then(configuration => {
            this.configuration = configuration;

            return this.configuration;
        });
    }

    logout(): Promise<void> {
        return firstValueFrom(this.http.get<void>(environment.apiUrl + "/api/session/logout", {}));
    }


    getConfiguration(): GeoRegistryConfiguration {
        return this.configuration;
    }

    isEnableBusinessData(): boolean {
        return this.getConfiguration().enableBusinessData;
    }

    isGraphVisualizerEnabled(): boolean {
        return this.getConfiguration().graphVisualizerEnabled;
    }

    isSearchEnabled(): boolean {
        return this.getConfiguration().searchEnabled;
    }

    getLocale(): string {
        return this.getConfiguration().locale;
    }

    getLocales(): LocaleView[] {
        return this.getConfiguration().locales;
    }

    getMapboxAccessToken(): string {
        return this.getConfiguration().mapboxAccessToken;
    }

    getAnalyticsToken(): string {
        return this.getConfiguration().googleanalyticstoken;
    }

    getContextPath(): string {
        return this.getConfiguration().contextPath + "/";
    }

    getDefaultMapBounds(): [[number]] {
        return this.getConfiguration().defaultMapBounds;
    }

    getCustomFont(): string {
        return this.getConfiguration().customFont;
    }

}
