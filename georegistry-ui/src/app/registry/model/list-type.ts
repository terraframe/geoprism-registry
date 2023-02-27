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

import { LocalizedValue } from "@core/model/core";
import { Layer } from "@registry/service/layer-data-source";
import { PageResult } from "@shared/model/core";
import { LazyLoadEvent } from "primeng/api";
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

    geometryType?: string;

}

export class ListVersionMetadata {

    oid?: string;
    listMetadata?: VersionMetadata;
    geospatialMetadata?: VersionMetadata;

}

export class ListColumn {

    name?: string;
    label: string;
    type?: string;
    value?: any
    colspan: number;
    rowspan: number;
    columns: ListColumn[];

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
    attributes: ListColumn[];

    isGeometryEditable: boolean;
    geometryType?: string;
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
    label?: string;
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
    hierarchies: { label: string, code: string, parents: { label: string, code: string, selected?: boolean }[] }[];
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

    constructor(oid: string, dataSourceType: string, legendLabel: string, rendered: boolean, color: string, forDate?: string, versionNumber?: number) {
        this.oid = oid;
        this.dataSourceType = dataSourceType;
        this.legendLabel = legendLabel;
        this.rendered = rendered;
        this.color = color;
        this.forDate = forDate;
        this.versionNumber = versionNumber;
    }

    oid: string;
    dataSourceType: string;
    legendLabel: string;
    rendered: boolean;
    color: string;
    forDate?: string;
    versionNumber?: number;

}

export class ListVersion {

    oid: string;
    forDate: string;
    versionNumber: number;
    layers?: Layer[];

}

export class ContextList {

    oid: string;
    label: string;
    versions: ListVersion[];
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
    edit: boolean;

    // Attributes required for the geo object properties panel
    type?: GeoObjectType;
    typeCode?: string;
    code?: string;
    forDate?: string;
    uid?: string;

    // Attributes required for the list row properties panel
    displayLabel?: LocalizedValue;
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

export class ListData {

    event: LazyLoadEvent;
    oid: string;

}
