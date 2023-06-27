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
import { CookieService } from "ngx-cookie-service";
import { User } from "@shared/model/user";
import { RoleBuilder, RegistryRole, RegistryRoleType } from "@shared/model/core";

@Injectable()
export class AuthService {

    private user: User = {
        loggedIn: false,
        userName: "",
        roles: [],
        roleDisplayLabels: [],
        version: "0",
        installedLocales: []
    };

    constructor(private service: CookieService) {
        let cookie = service.get("user");

        if (this.service.check("user") && cookie != null && cookie.length > 0) {
            let cookieData: string = this.service.get("user");
            let cookieDataJSON: any = JSON.parse(cookieData);

            this.buildFromCookieJson(cookieDataJSON);
        }
    }

    buildFromCookieJson(cookieDataJSON: any) {
        this.user.userName = cookieDataJSON.userName;
        this.buildRolesFromCookie(cookieDataJSON);
        this.user.loggedIn = cookieDataJSON.loggedIn;
        this.user.roleDisplayLabels = cookieDataJSON.roleDisplayLabels;
        this.user.version = cookieDataJSON.version.replaceAll("+", " ");
        // this.user.installedLocales = cookieDataJSON.installedLocales;
    }

    buildRolesFromCookie(cookieDataJSON: any) {
        this.user.roles = [];
        let roles: string[] = cookieDataJSON.roles;

        for (let i = 0; i < roles.length; ++i) {
            let role: RegistryRole = RoleBuilder.buildFromRoleName(roles[i]);

            if (role != null) {
                this.user.roles.push(role);
            }
        }
    }

    isLoggedIn(): boolean {
        return this.user.loggedIn;
    }

    afterLogIn(logInResponse: any): void {
        localStorage.clear();

        this.buildFromCookieJson(JSON.parse(this.service.get("user")));

    }

    afterLogOut(): void {
        this.user = null;
        sessionStorage.removeItem("locales");
        localStorage.clear();
    }


    removeUser(): void {
        this.user = {
            loggedIn: false,
            userName: "",
            roles: [],
            roleDisplayLabels: [],
            version: "0",
            installedLocales: []
        };
    }

    // Legacy Accessors:
    isPublic(): boolean {
        return this.user.roles.length === 0;
    }

    isAdmin(): boolean {
        return this.isSRA() || this.isRA();
    }

    isMaintainer(): boolean {
        return this.isSRA() || this.isRM();
    }

    isContributer(): boolean {
        return this.isSRA() || this.isRC(false);
    }

    isContributerOnly(): boolean {
        return this.isRC(true);
    }

    // Used to exactly identify a role. I.e. if we say we need RC, SRA doesn't count.
    hasExactRole(roleType: RegistryRoleType) {
        for (let i = 0; i < this.user.roles.length; ++i) {
            let role: RegistryRole = this.user.roles[i];

            if (role.type === roleType) {
                return true;
            }
        }

        return false;
    }

    isSRA(): boolean {
        for (let i = 0; i < this.user.roles.length; ++i) {
            let role: RegistryRole = this.user.roles[i];

            if (role.type === RegistryRoleType.SRA) {
                return true;
            }
        }

        return false;
    }

    isRA(): boolean {
        if (this.isSRA()) {
            return true;
        }

        for (let i = 0; i < this.user.roles.length; ++i) {
            let role: RegistryRole = this.user.roles[i];

            if (role.type === RegistryRoleType.RA) {
                return true;
            } else if (role.roleName.indexOf("commongeoregistry.RegistryAdministrator") !== -1 || role.roleName.indexOf("cgr.RegistryAdministrator") !== -1) {
                // Legacy support
                return true;
            }
        }
    }

    isRM(): boolean {
        if (this.isSRA()) {
            return true;
        }
        for (let i = 0; i < this.user.roles.length; ++i) {
            let role: RegistryRole = this.user.roles[i];

            if (role.type === RegistryRoleType.RM) {
                return true;
            } else if (role.roleName.indexOf("commongeoregistry.RegistryMaintainer") !== -1 || role.roleName.indexOf("cgr.RegistryMaintainer") !== -1) {
                // Legacy support
                return true;
            }
        }

        return false;
    }

    isOrganizationRA(orgCode: string): boolean {
        if (this.isSRA()) {
            return true;
        }

        for (let i = 0; i < this.user.roles.length; ++i) {
            let role: RegistryRole = this.user.roles[i];

            if (role.orgCode === orgCode && role.type === RegistryRoleType.RA) {
                return true;
            }
        }

        return false; // this.isSRA();
    }

    isGeoObjectTypeRM(orgCode: string, gotCode: string): boolean {
        if (this.isSRA()) {
            return true;
        }

        for (let i = 0; i < this.user.roles.length; ++i) {
            let role: RegistryRole = this.user.roles[i];

            if (role.type === RegistryRoleType.RM && role.orgCode === orgCode && role.geoObjectTypeCode === gotCode) {
                return true;
            }
        }

        return this.isOrganizationRA(orgCode);
    }

    isGeoObjectTypeOrSuperRM(got: { organizationCode: string, superTypeCode?: string, code: string }, allowRoleSuper: boolean = true): boolean {
        if (this.isGeoObjectTypeRM(got.organizationCode, got.code)) {
            return true;
        } else if (got.superTypeCode != null) {
            return this.isGeoObjectTypeRM(got.organizationCode, got.superTypeCode);
        }
    }

    isGeoObjectTypeRC(orgCode: string, gotCode: string): boolean {
        if (this.isSRA()) {
            return true;
        }

        for (let i = 0; i < this.user.roles.length; ++i) {
            let role: RegistryRole = this.user.roles[i];

            if (role.type === RegistryRoleType.RC && role.orgCode === orgCode && role.geoObjectTypeCode === gotCode) {
                return true;
            }
        }

        return this.isGeoObjectTypeRM(orgCode, gotCode);
    }

    isGeoObjectTypeOrSuperRC(got: { organizationCode: string, superTypeCode?: string, code: string }, allowRoleSuper: boolean = true): boolean {
        if (this.isGeoObjectTypeRC(got.organizationCode, got.code)) {
            return true;
        } else if (got.superTypeCode != null) {
            return this.isGeoObjectTypeRC(got.organizationCode, got.superTypeCode);
        }
    }

    isRC(isRCOnly: boolean): boolean {
        if (this.isSRA() && !isRCOnly) {
            return true;
        }

        for (let i = 0; i < this.user.roles.length; ++i) {
            let role: RegistryRole = this.user.roles[i];

            if (role.type === RegistryRoleType.RC) {
                return true;
            } else if (role.roleName.indexOf("commongeoregistry.RegistryContributor") !== -1 || role.roleName.indexOf("cgr.RegistryContributor") !== -1) {
                // Legacy support
                return true;
            }
        }

        return false;
    }

    // Returns all organization codes that the current user participates in.
    // If the user is an SRA then this method will return an empty string array.
    getMyOrganizations(): string[] {
        let orgCodes: string[] = [];

        for (let i = 0; i < this.user.roles.length; ++i) {
            let role: RegistryRole = this.user.roles[i];

            if (role.type === RegistryRoleType.SRA || role.type === RegistryRoleType.RC || role.type === RegistryRoleType.RM || role.type === RegistryRoleType.RA) {
                orgCodes.push(role.orgCode);
            }
        }

        return orgCodes;
    }

    __getRoleFromRoleName(roleName: string): string {
        let nameArr = roleName.split(".");

        return nameArr[nameArr.length - 1];
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
        for (let i = 0; i < this.user.roleDisplayLabels.length; ++i) {
            let displayLabel = this.user.roleDisplayLabels[i];

            if (displayLabel === "Administrator") {
                continue;
                // It's OK to hardcode to a display label here because the end user can't change it anyway.
                // Is it ideal? No. But sometimes it's better to get software out quicker than to spend forever
                // on something that nobody will ever see.
            }

            str = str + displayLabel;

            if (i < this.user.roleDisplayLabels.length - 1) {
                str = str + ",";
            }
        }

        return str;
    }

    getVersion(): string {
        return this.user.version;
    }

    clear(): void {
        this.user = {
            loggedIn: false,
            userName: "",
            roles: [],
            roleDisplayLabels: [],
            version: "0",
            installedLocales: []
        };
    }
}
