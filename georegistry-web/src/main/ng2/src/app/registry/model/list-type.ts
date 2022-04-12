import { LocalizedValue, PageResult } from "@shared/model/core";
import { GeoObject, GeoObjectType } from "./registry";

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
    curation?: any;
    period?: {
        type: string,
        value: any
    };

}

export class ListTypeEntry {

    displayLabel: string;
    oid: string;
    typeCode: string;
    orgCode: string;
    listType: string;
    forDate: string;
    period?: {
        type: string,
        value: any
    };

    working: ListTypeVersion;
    versions?: ListTypeVersion[];
    showAll?: boolean;

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
    typePrivate?: boolean;
    displayLabel: LocalizedValue;
    description: LocalizedValue;
    subtypes?: { label: string, code: string }[];
    subtypeHierarchies?: any[];
    hierarchies: { label: string, code: string, parents: { label: string, code: string }[] }[];
    includeLatLong?: boolean;

    listMetadata: ListMetadata;
    geospatialMetadata: ListMetadata;

    // Attributes for the subtypes
    validOn?: string;
    publishingStartDate?: string;
    frequency?: string;
    intervalJson?: { startDate: string, endDate: string, readonly?: string, oid?: string }[]

    entries?: ListTypeEntry[];
    filter?: {
        attribute: string,
        operation: string,
        value: any,
        id: string
    }[];

}

export class ListTypeByType {

    orgCode: string;
    orgLabel: string;
    typeCode: string;
    typeLabel: string;
    geometryType: string;
    write: boolean;
    private: boolean;
    lists: ListType[];

}

export class ContextLayer {

    oid: string;
    forDate: string;
    versionNumber: number;
    rendered?: boolean;
    showOnLegend?: boolean;
    color?: string;
    label?: string;

}

export class ContextList {

    oid: string;
    label: string;
    versions: ContextLayer[];
    open?: boolean;

}

export class ListTypeGroup {

    typeCode: string;
    typeLabel: LocalizedValue;
    lists: ContextList[];

}

export class ListOrgGroup {

    orgCode: string;
    orgLabel: LocalizedValue;
    types: ListTypeGroup[];

}

export class LayerRecord {

    recordType: string;

    // Attributes required for the geo object properties panel
    type?: GeoObjectType;
    code?: string;
    forDate?: string;

    // Attributes required for the list row properties panel
    typeLabel?: LocalizedValue;
    version?: string;
    attributes?: any[];
    data?: any;

    geoObject?: GeoObject;
    bbox?: any;

}

export class CurationProblem {

    resolution: string;
    historyId: string;
    type: string;
    id: string;
    typeCode?: string;
    goCode?: string;
    goUid?: string;
    selected?: boolean;

}

export class CurationJob {

    status: string;
    lastRun: string;
    lastRunBy: string;
    historyId: string;
    jobId: string;
    workTotal: number;
    workProgress: number;
    exception?: {
        type: string,
        message: string
    };

    page?: PageResult<CurationProblem>

}
