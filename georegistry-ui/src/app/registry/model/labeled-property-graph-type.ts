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

export class LabeledPropertyGraphTypeVersion {

    oid?: string;
    displayLabel: string;
    entry: string;
    graphType: string;
    forDate: string;
    createDate: string;
    publishDate: string;

    locales?: string[];
    refreshProgress?: any;
    working: boolean;
    isMember?: boolean;
    versionNumber: number;
    collapsed?: boolean;
    period?: {
        type: string,
        value: any
    };
    label?: string;
}

export class LabeledPropertyGraphTypeEntry {

    displayLabel: string;
    oid: string;
    graphType: string;
    forDate: string;
    period?: {
        type: string,
        value: any
    };

    versions?: LabeledPropertyGraphTypeVersion[];
    showAll?: boolean;

}


export class LabeledPropertyGraphType {

    oid?: string;
    code: string;
    displayLabel: LocalizedValue;
    description: LocalizedValue;
    hierarchy: string;
    strategyType?: string;
    strategyConfiguration?: any;
    graphType: string;
    graphTypes?: string;
    geoObjectTypeCodes?: string;

    // Attributes for the subtypes
    validOn?: string;
    publishingStartDate?: string;
    frequency?: string;
    intervalJson?: { startDate: string, endDate: string, readonly?: string, oid?: string }[]

    entries?: LabeledPropertyGraphTypeEntry[];
}

