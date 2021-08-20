import { Injectable } from "@angular/core";
import { HttpHeaders, HttpClient } from "@angular/common/http";

// import 'rxjs/add/operator/toPromise';

import { Profile } from "@shared/model/profile";

declare let acp: any;

@Injectable()
export class ProfileService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private http: HttpClient) { }

    get(): Promise<Profile> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http
            .post<Profile>(acp + "/registryaccount/get", { headers: headers })
            .toPromise();
    }


    apply(profile: Profile): Promise<Profile> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http
            .post<Profile>(acp + "/registryaccount/apply", JSON.stringify({ account: profile }), { headers: headers })
            .toPromise();
    }

    unlock(oid: string): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http
            .post<void>(acp + "/registryaccount/unlock", JSON.stringify({ oid: oid }), { headers: headers })
            .toPromise()
    }

    setLocale(locale: string): Promise<any> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        return this.http
            .post<any>(acp + "/localization/set-locale", JSON.stringify({ locale: locale }), { headers: headers })
            .toPromise();
    }

    getRolesForUser(userOID: string): Promise<any> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });


        return this.http
            .post<Profile>(acp + "/registryaccount/getRolesForUser", { userOID: userOID }, { headers: headers })
            .toPromise();
    }

}
