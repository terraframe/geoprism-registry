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
import { LocalizedValue } from "@core/model/core";

export class HierarchyType {
    code: string;
    description: LocalizedValue;
    label: LocalizedValue;
    rootGeoObjectTypes: HierarchyNode[];
    organizationCode: string;
    progress?: string;
    acknowledgement?: string;
    disclaimer?: string;
    contact?: string;
    phoneNumber?: string;
    email?: string;
    accessConstraints?: string;
    useConstraints?: string;
}

export class Hierarchy {
    id: string;
    label: string;
}

export class HierarchyNode {
    geoObjectType: string;
    children: HierarchyNode[];
    label: string;
    inheritedHierarchyCode: string;
}

export class HierarchyGroupedTypeView {
    code: string;
    label: string;
    orgCode: string;
    types: any[];
}

export class TypeGroupedHierachyView {
    code: string;
    label: string;
    orgCode: string;
    super?: {code: string, label: string, orgCode: string, isAbstract: boolean};
    permissions: [string];
    hierarchies: any[];
}
