import { Injectable } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';

@Injectable()
export class AuthService {
    private user: any = {
        username: '',
        roles: []
    };

    constructor( private service: CookieService ) {
        let cookie = service.get( 'user' );

        if ( this.service.check( "user" ) ) {
            let cookieData: string = this.service.get( "user" )
            let cookieDataJSON: any = JSON.parse( JSON.parse( cookieData ) );
            
            this.user.username = cookieDataJSON.userName;
            this.user.roles = cookieDataJSON.roles;            
        }
    }

    removeUser(): void {
        this.user = {
            username: '',
            roles: []
        };
    }

    isAdmin(): boolean {
        return this.user.roles.indexOf( "geoprism.admin.Administrator" ) !== -1 || this.user.roles.indexOf( "commongeoregistry.RegistryAdministrator" ) !== -1;
    }

    isMaintainer(): boolean {
        return this.user.roles.indexOf( "commongeoregistry.RegistryMaintainer" ) !== -1;
    }
}
