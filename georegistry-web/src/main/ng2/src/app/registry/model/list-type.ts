import { LocalizedValue } from "@shared/model/core";
import { GeoObjectType } from "./registry";

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

    topicCategories?: string;
    placeKeywords?: string;
    updateFrequency?: string;
    lineage?: string;
    languages?: string;
    scaleResolution?: string;
    spatialRepresentation?: string;
    referenceSystem?: string;
    reportSpecification?: string;
    distributionFormat?: string;
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
    superTypeCode?: string;
    typeLabel?: string;
    displayLabel: LocalizedValue;
    description: LocalizedValue;
    subtypes?: { label: string, code: string }[];
    subtypeHierarchies?: any[];
    hierarchies: { label: string, code: string, parents: { label: string, code: string }[] }[];

    listMetadata: ListMetadata;
    geospatialMetadata: ListMetadata;

    // Attributes for the subtypes
    validOn?: string;
    publishingStartDate?: string;
    frequency?: string;
    intervalJson?: { startDate: string, endDate: string }[]

    entries?: ListTypeEntry[];
}

export class ListTypeEntry {
    displayLabel: string;
    oid: string;
    typeCode: string;
    orgCode: string;
    listType: string;
    forDate: string;
    wokring: ListTypeVersion;
    versions?: ListTypeVersion[];
    showAll?: boolean;
}

export class VersionMetadata {
    master: boolean;
    visibility: string;
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

    topicCategories?: string;
    placeKeywords?: string;
    updateFrequency?: string;
    lineage?: string;
    languages?: string;
    scaleResolution?: string;
    spatialRepresentation?: string;
    referenceSystem?: string;
    reportSpecification?: string;
    distributionFormat?: string;
}

export class ListVersionMetadata {
    oid?: string;
    listMetadata?: VersionMetadata;
    geospatialMetadata?: VersionMetadata;
}

export class ListTypeVersion extends ListVersionMetadata {
    displayLabel: string;
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
    working: boolean;
    isMember?: boolean;
    versionNumber: number;
    subtypes?: { label: string, code: string }[];
    collapsed?: boolean;
}

export class ContextLayer {
    oid: string;
    forDate: string;
    versionNumber: string;
    active?: boolean;
    enabled?: boolean;
    color?: string;
    label?: string;
}

export class ContextList {
    oid: string;
    label: string;
    versions: ContextLayer[];
    open?: boolean;
}

export class LayerRecord {
    recordType: string;
    type?: GeoObjectType;
    code?: string;
    forDate?: string;
    attributes?: any[];
    data?: Object;
}