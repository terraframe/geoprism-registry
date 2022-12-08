import { Injectable } from "@angular/core";
import { HttpHeaders, HttpClient } from "@angular/common/http";

// import 'rxjs/add/operator/toPromise';

import { Profile } from "@shared/model/profile";

import { GeoRegistryConfiguration } from "@core/model/registry"; declare let registry: GeoRegistryConfiguration;

@Injectable()
export class ProfileService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient) { }

    get(): Promise<Profile> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http
            .post<Profile>(registry.contextPath + "/api/registryaccount/get", { headers: headers })
            .toPromise();
    }


    apply(profile: Profile): Promise<Profile> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http
            .post<Profile>(registry.contextPath + "/api/registryaccount/apply", JSON.stringify({ account: profile }), { headers: headers })
            .toPromise();
    }

    unlock(oid: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http
            .post<void>(registry.contextPath + "/api/registryaccount/unlock", JSON.stringify({ oid: oid }), { headers: headers })
            .toPromise()
    }

    setLocale(locale: string): Promise<any> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http
            .post<any>(registry.contextPath + "/localization/set-locale", JSON.stringify({ locale: locale }), { headers: headers })
            .toPromise();
    }

    getRolesForUser(userOID: string): Promise<any> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });


        return this.http
            .post<Profile>(registry.contextPath + "/api/registryaccount/getRolesForUser", { userOID: userOID }, { headers: headers })
            .toPromise();
    }

}
