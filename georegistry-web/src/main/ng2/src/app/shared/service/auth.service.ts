import { Injectable } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { User } from '../model/user';
import { Locale } from '../../admin/model/localization-manager';
import { RoleBuilder, RegistryRole, RegistryRoleType } from '../model/core';

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
      
      this.buildFromCookieJson(cookieDataJSON);
    }
  }
  
  buildFromCookieJson(cookieDataJSON: any)
  {
    this.user.userName = cookieDataJSON.userName;
    this.buildRolesFromCookie(cookieDataJSON);
    this.user.loggedIn = cookieDataJSON.loggedIn;
    this.user.roleDisplayLabels = cookieDataJSON.roleDisplayLabels;
    this.user.version = cookieDataJSON.version;
    this.user.installedLocales = cookieDataJSON.installedLocales;
  }
  
  buildRolesFromCookie(cookieDataJSON: any)
  {
    this.user.roles = [];
    let roles: string[] = cookieDataJSON.roles;
  
    for (let i = 0; i < roles.length; ++i)
    {
      let role: RegistryRole = RoleBuilder.buildFromRoleName(roles[i]);
    
      if (role != null)
      {
        this.user.roles.push(role);
      }
    }
  }
  
  isLoggedIn():boolean {
    return this.user.loggedIn;
  }
  
  setUser(cookieDataJSON: any):void {
    this.buildFromCookieJson(cookieDataJSON);    
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
  
  // Legacy Accessors:
  isAdmin(): boolean {
    return this.isSRA() || this.isRA();
  }
  
  isMaintainer(): boolean {
    return this.isRM();
  }
  
  isContributer(): boolean {
    return this.isRC();
  }
  

  isSRA(): boolean {
    for (let i = 0; i < this.user.roles.length; ++i)
    {
      let role: RegistryRole = this.user.roles[i];
      
      if (role.type === RegistryRoleType.SRA) {
        return true;
      }
    }
    
    return false;
  }

  isRA(): boolean {
    for (let i = 0; i < this.user.roles.length; ++i)
    {
      let role: RegistryRole = this.user.roles[i];
      
      if (role.type === RegistryRoleType.RA) {
        return true;
      }
      else if (role.roleName.indexOf('commongeoregistry.RegistryAdministrator') !== -1
               || role.roleName.indexOf( "cgr.RegistryAdministrator" ) !== -1) // Legacy support
      {
        return true;
      }
    }
    
    return false;
  }
  
  isRM(): boolean {
    for (let i = 0; i < this.user.roles.length; ++i)
    {
      let role: RegistryRole = this.user.roles[i];
      
      if (role.type === RegistryRoleType.RM) {
        return true;
      }
      else if (role.roleName.indexOf('commongeoregistry.RegistryMaintainer') !== -1
               || role.roleName.indexOf( "cgr.RegistryMaintainer" ) !== -1) // Legacy support
      {
        return true;
      }
    }
    
    return false;
  }
  
  isRC(): boolean {
    for (let i = 0; i < this.user.roles.length; ++i)
    {
      let role: RegistryRole = this.user.roles[i];
      
      if (role.type === RegistryRoleType.RC) {
        return true;
      }
      else if (role.roleName.indexOf('commongeoregistry.RegistryContributor') !== -1
               || role.roleName.indexOf( "cgr.RegistryContributor" ) !== -1) // Legacy support
      {
        return true;
      }
    }
    
    return false;
  }
  
  // Returns all organization codes that the current user participates in.
  // If the user is an SRA then this method will return an empty string array.
  getMyOrganizations(): string[] {
    let orgCodes: string[] = [];
  
    for (let i = 0; i < this.user.roles.length; ++i)
    {
      let role: RegistryRole = this.user.roles[i];
      
      if (role.type === RegistryRoleType.RC
          || role.type === RegistryRoleType.RM
          || role.type === RegistryRoleType.RA) {
        orgCodes.push(role.orgCode);
      }
    }
    
    return orgCodes;
  }
  
  getUsername(): string {
    return this.user.userName;
  }

  getRoles(): any {
    return this.user.roles;
  }

  getRoleDisplayLabelsArray(): any {
    return this.user.roleDisplayLabels;
  }
  
  getRoleDisplayLabels(): string {
    let str = "";
    for (let i = 0; i < this.user.roleDisplayLabels.length; ++i)
    {
      let displayLabel = this.user.roleDisplayLabels[i]; 
    
      if (displayLabel === "Administrator")
      {
        continue;
        // It's OK to hardcode to a display label here because the end user can't change it anyway.
        // Is it ideal? No. But sometimes it's better to get software out quicker than to spend forever
        // on something that nobody will ever see.
      }
    
      str = str + displayLabel;
      
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
