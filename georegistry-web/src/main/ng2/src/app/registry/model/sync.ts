
export interface AttributeConfigInfo {
    cgrAttr: CGRAttrInfo
    attributeMappingStrategies: string;
    dhis2Attrs: Dhis2Attr[];
    terms?: Term[];
}

export interface DHIS2AttributeMapping {
    attributeMappingStrategy: string;
    info?: AttributeConfigInfo;
    cgrAttrName: string;
    dhis2AttrName: string;
    dhis2Id?: string; // This is a front-end only, derived attribute
    externalId: string;
    terms?: {};
    isOrgUnitGroup?: boolean;
}

export interface CGRAttrInfo {
    name: string;
    label: string;
    type: string;
    typeLabel: string;
}

export interface Dhis2Attr {
    name: string;
    code: string;
    dhis2Id: string;
    options: Option[];
}

export interface Term {
    label: string;
    code: string;
}

export interface Option {
    code: string;
    name: string;
    id: string;
}

export interface SyncLevel {
    geoObjectType: string;
    type: string;
    level: number;
    mappings: DHIS2AttributeMapping[];
    orgUnitGroupId: string;
}
