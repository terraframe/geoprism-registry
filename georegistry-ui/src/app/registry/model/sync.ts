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


export interface AttributeConfigInfoStrategy {
    type: string;
    label: string;
    dhis2Attrs: Dhis2Attr[];
    terms?: Term[]; // Terms here refer to the CGR terms which can be potentially mapped to DHIS2 options or orgUnitGroups
}

// The objects returned from the 'synchronization-config/get-custom-attr' endpoint
export interface AttributeConfigInfo {
    cgrAttr: CGRAttrInfo
    attributeMappingStrategies: AttributeConfigInfoStrategy[];
}

export interface DHIS2AttributeMapping {
    attributeMappingStrategy: string;
    info?: AttributeConfigInfo;
    cgrAttrName: string;
    dhis2AttrName: string;
    dhis2Id?: string; // This is a front-end only, derived attribute
    externalId: string;
    terms?: {};
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
