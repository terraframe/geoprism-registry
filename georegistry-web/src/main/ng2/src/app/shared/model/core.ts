/* eslint-disable no-use-before-define */
/* eslint-disable no-unused-vars */
import { LocalizationService } from "@shared/service/localization.service";

export interface MessageContainer {
    setMessage(message: string);
}

export class LocaleValue {

    locale: string;
    value: string;

    constructor(locale: string, value: string) {
        this.locale = locale;
        this.value = value;
    }

}

export class LocaleView {

    constructor(lService: LocalizationService) {
        this.label = lService.create();
        this.toString = "";
        this.tag = "";
        this.isDefaultLocale = false;
        this.language = { label: "", code: "" };
        this.country = { label: "", code: "" };
        this.variant = { label: "", code: "" };
    }

    label: LocalizedValue;
    toString: string;
    tag: string;
    isDefaultLocale: boolean;
    language: { label: string, code: string };
    country: { label: string, code: string };
    variant: { label: string, code: string };

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

export class LocalizedValue {

    localizedValue: string;
    localeValues: LocaleValue[];

    constructor(localizedValue: string, localeValues: LocaleValue[]) {
        this.localizedValue = localizedValue;
        this.localeValues = localeValues;
    }

    public getValue(localeToString: string): string {
        let len = this.localeValues.length;

        for (let i = 0; i < len; ++i) {
            let lv = this.localeValues[i];

            if (lv.locale === localeToString) {
                return lv.value;
            }
        }

        return this.localizedValue;
    }

    /*
     * Populates lv1 with all values contained in lv2
     */
    public static populate(lv1: LocalizedValue, lv2: LocalizedValue) {
        if (lv1 == null || lv2 == null) {
            return;
        }

        lv1.localizedValue = lv2.localizedValue;

        if (lv2.localeValues != null) {
            lv2.localeValues.forEach(lv2lv => {
                if (lv1.localeValues) {
                    let found = false;

                    lv1.localeValues.forEach(lv1lv => {
                        if (!found && lv1lv.locale === lv2lv.locale) {
                            lv1lv.value = lv2lv.value;
                            found = true;
                        }
                    });

                    if (!found) {
                        lv1.localeValues.push(JSON.parse(JSON.stringify(lv2lv)));
                    }
                } else {
                    lv1.localeValues = lv2.localeValues;
                }
            });
        }
    }

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
