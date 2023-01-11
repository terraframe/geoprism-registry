import { Injectable } from "@angular/core";
import { HttpHeaders, HttpClient } from "@angular/common/http";

// import 'rxjs/add/operator/toPromise';
import { finalize } from "rxjs/operators";

import { EventService } from "./event.service";

import { AuthService } from "./auth.service";
import { User } from "@shared/model/user";

import { environment } from 'src/environments/environment';

@Injectable()
export class SessionService {

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: EventService, private http: HttpClient, private authService: AuthService) { }

    login(username: string, password: string): Promise<any> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.service.start();

        return this.http
            .post<User>(environment.apiUrl + "/api/cgrsession/login", JSON.stringify({ username: username, password: password }), { headers: headers })
            .pipe(finalize(() => {
                this.service.complete();
            }))
            .toPromise()
            .then((logInResponse: any) => {
                this.authService.afterLogIn(logInResponse);

                return logInResponse;
            });
    }

    logout(): Promise<void> {
        let headers = new HttpHeaders({
            "Content-Type": "application/json"
        });

        this.service.start();

        return this.http
            .post<void>(environment.apiUrl + "/session/logout", { headers: headers })
            .pipe(finalize(() => {
                this.service.complete();
            }))
            .toPromise()
            .then((response: any) => {
                this.authService.afterLogOut();

                return response;
            });
    }

}
