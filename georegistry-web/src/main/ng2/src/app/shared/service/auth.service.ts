import { Injectable } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { User } from '../model/user';
import { Locale } from '../../admin/model/localization-manager';

@Injectable()
export class AuthService {
  private user:User = {
    loggedIn:false,
    userName:'',
    roles:[],
    roleDisplayLabels:[],
    version:"0",
    installedLocales: []
  };

    constructor( private service: CookieService ) {
        let cookie = service.get( 'user' );

        if ( this.service.check( "user" ) && cookie != null && cookie.length > 0 ) {
            let cookieData: string = this.service.get( "user" )
            let cookieDataJSON: any = JSON.parse( JSON.parse( cookieData ) );
            
            this.user.userName = cookieDataJSON.userName;
            this.user.roles = cookieDataJSON.roles;
            this.user.loggedIn = cookieDataJSON.loggedIn;
            this.user.roleDisplayLabels = cookieDataJSON.roleDisplayLabels;
            this.user.version = cookieDataJSON.version;
            this.user.installedLocales = cookieDataJSON.installedLocales;
        }
    }
    
    isLoggedIn():boolean {
      return this.user.loggedIn;
    }
    
    setUser(user:User):void {
      this.user = user;    
    }

    removeUser(): void {
      this.user = {
        loggedIn:false,
        userName:'',
        roles:[],
        roleDisplayLabels:[],
        version:"0",
        installedLocales: []
      };
    }

    isAdmin(): boolean {
        return this.user.roles.indexOf( "geoprism.admin.Administrator" ) !== -1 || this.user.roles.indexOf( "commongeoregistry.RegistryAdministrator" ) !== -1;
    }

    isMaintainer(): boolean {
        return this.user.roles.indexOf( "commongeoregistry.RegistryMaintainer" ) !== -1;
    }
    
    isContributer(): boolean {
        return this.user.roles.indexOf( "commongeoregistry.RegistryContributor" ) !== -1;
    }
  
  getUsername(): string {
    return this.user.userName;
  }
  
  getRoles(): string {
    let str = "";
    for (let i = 0; i < this.user.roles.length; ++i)
    {
      str = str + this.user.roles[i];
      
      if (i < this.user.roles.length-1)
      {
        str = str + ",";
      }
    }
  
    return str;
  }
  
  getRoleDisplayLabels(): string {
    let str = "";
    for (let i = 0; i < this.user.roles.length; ++i)
    {
      str = str + this.user.roleDisplayLabels[i];
      
      if (i < this.user.roleDisplayLabels.length-1)
      {
        str = str + ",";
      }
    }
  
    return str;
  }
  
  getVersion(): string {
    return this.user.version;
  }

  getLocales(): any[] {
    return this.user.installedLocales;
  }
}
