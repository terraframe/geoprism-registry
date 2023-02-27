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

/* eslint-disable no-use-before-define */
/* eslint-disable no-unused-vars */

import { LocalizedValue } from "@core/model/core";

export interface MessageContainer {
    setMessage(message: string);
}

export class Organization {

    code: string;
    label: any;
    contactInfo: any;

}

export class ExternalSystem {

    oid?: string;
    id: string;
    type: string;
    organization: string;
    label: LocalizedValue;
    description: LocalizedValue;
    username?: string;
    password?: string;
    url?: string;
    system?: string;
    version?: string;
    oAuthServer?: OAuthServer;

}

export class OAuthServer {

    keyName?: string;
    label?: LocalizedValue;
    url?: string;
    authorizationLocation: string;
    tokenLocation: string;
    profileLocation: string;
    clientId: string;
    secretKey: string;
    serverType: string;

}

export class SystemCapabilities {

    supportedVersion: boolean;
    oauth: boolean;

}

export class PageResult<T> {

    count: number;
    pageNumber: number;
    pageSize: number;
    resultSet: T[];
    externalSystems?: ExternalSystem[];

}

export class RoleBuilder {

    static buildFromRoleName(roleName: string): RegistryRole {
        if (roleName === "cgr.SRA") {
            return new RegistryRole(RegistryRoleType.SRA, "", "", "cgr.SRA");
        }

        let roleSplit = roleName.split(".");

        let orgCode: string = roleSplit[2];

        if (roleSplit.length === 4) {
            return new RegistryRole(RegistryRoleType.RA, orgCode, "", roleName);
        } else if (roleSplit.length === 5) {
            let roleSuffix: string = roleSplit[4];

            let gotCode: string = roleSplit[3];

            if (roleSuffix === "RM") {
                return new RegistryRole(RegistryRoleType.RM, orgCode, gotCode, roleName);
            } else if (roleSuffix === "RC") {
                return new RegistryRole(RegistryRoleType.RC, orgCode, gotCode, roleName);
            } else if (roleSuffix === "AC") {
                return new RegistryRole(RegistryRoleType.AC, orgCode, gotCode, roleName);
            }
        } else {
            return null;
        }
    }

}

export class RegistryRole {

    type: RegistryRoleType;
    orgCode: string;
    geoObjectTypeCode: string;
    roleName: string;
    displayLabel: string;

    constructor(type: RegistryRoleType, orgCode: string, geoObjectTypeCode: string, roleName: string) {
        this.type = type;
        this.orgCode = orgCode;
        this.geoObjectTypeCode = geoObjectTypeCode;
        this.roleName = roleName;
    }

}

export enum RegistryRoleType {
    SRA,
    RA,
    RM,
    RC,
    AC
}
