import { LocalizedValue } from "@shared/model/core";

export class ListTypeByType {
    orgCode: string;
    orgLabel: string;
    typeCode: string;
    typeLabel: string;
    write: boolean;
    lists: ListType[];
}

export class ListMetadata {
    label: LocalizedValue;
    description: LocalizedValue;
    process: LocalizedValue;
    progress: LocalizedValue;
    accessConstraints: LocalizedValue;
    useConstraints: LocalizedValue;
    acknowledgements: LocalizedValue;
    disclaimer: LocalizedValue;
    collectionDate: string;
    originator: string;
    contactName: string;
    organization: string;
    telephoneNumber: string;
    email: string;
}

export class ListType {
    oid?: string;
    code: string;
    organization: string;
    listType: string;
    write?: boolean;
    read?: boolean;
    exploratory?: boolean;
    typeCode: string;
    typeLabel?: string;
    displayLabel: LocalizedValue;
    description: LocalizedValue;
    subtypes?: { label: string, code: string }[];
    subtypeHierarchies?: any[];
    hierarchies: { label: string, code: string, parents: { label: string, code: string }[] }[];

    // List metadata fields
    listMetadata: ListMetadata;

    // Attributes for the subtypes
    validOn?: string;
}

export class ListTypeEntry {
    displayLabel: string;
    oid: string;
    typeCode: string;
    orgCode: string;
    listType: string;
    forDate: string;
}


export class ListTypeVersion {
    displayLabel: string;
    oid: string;
    typeCode: string;
    orgCode: string;
    listEntry: string;
    listType: string;
    forDate: string;
    createDate: string;
    publishDate: string;
    attributes: any[];
    isGeometryEditable: boolean;
    locales?: string[];
    shapefile?: boolean;
    isAbstract?: boolean;
    superTypeCode?: string;
    refreshProgress?: any;
    subtypes?: { label: string, code: string }[];
}
