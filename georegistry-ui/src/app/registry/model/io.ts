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

/* eslint-disable padded-blocks */
import { GeoObjectType } from "./registry";

export class ImportSheet {
    name: string;
    attributes: {
        boolean: string[];
        date: string[];
        numeric: string[];
        text: string[];
    }
}

export class Location {
    label: string;
    code: string;
    target: string;
    matchStrategy: string;
}

export class Term {
    code: string;
    label: string;
}

export class TermProblem {
    label: string;
    parentCode: string;
    importType: string;
    typeCode: string;
    attributeCode: string;
    attributeLabel: string;
    action: any;
    resolved: boolean;
}

export class LocationProblem {
    label: string;
    type: string;
    typeLabel: string;
    parent: string;
    context: { label: string, type: string }[];
    action: any;
    resolved: boolean;
}

export class Exclusion {
    code: string;
    value: string;
}

export class Synonym {
    label: string;
    synonymId: string;
    vOid?: string;
}

export class ImportConfiguration {
    type: GeoObjectType;
    sheet: ImportSheet;
    directory: string;
    filename: string;
    hierarchy: string;
    postalCode: boolean;
    hasPostalCode: boolean;
    locations: Location[];
    formatType: string;
    objectType: string;
    locationProblems: LocationProblem[];
    termProblems: TermProblem[];
    exclusions: Exclusion[];
    hierarchies: { code: string, label: string }[];
    startDate: string;
    endDate: string;
    isExternal: string;
    externalSystemId: string;
    externalSystem?: any;
    revealGeometryColumn?: any;
    onValidChange: any;
    externalIdAttributeTarget: string;
}

