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
        return firstValueFrom(this.http.post<void>(environment.apiUrl + "/session/logout", {}));
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
