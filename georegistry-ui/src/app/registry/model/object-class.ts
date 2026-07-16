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
import { AttributedType, AttributeType } from "./registry";

export class ObjectClass {
    oid?: string;
    type: "business-type" | "concept-class";
    code: string;
    organization: string;
    organizationLabel?: string;
    displayLabel: LocalizedValue;
    description: LocalizedValue;
    attributes?: Array<AttributeType>;
}

export class ConceptClass extends ObjectClass implements AttributedType {

}

export class BusinessType extends ObjectClass implements AttributedType {
    labelAttribute?: string;
}

export class BusinessEdgeType {
    oid?: string;
    code: string;
    organizationCode: string;
    label: LocalizedValue;
    description: LocalizedValue;
    parentTypeCode: string;
    childTypeCode: string;
    direction?: string;
}

export class ObjectOverTime {
    type: {
        typeCode: string;
        typeClass: string;
    }
    label?: string;
    code: string;
    properties: {
        [key: string]: {
            type: string;
            changeOverTime: boolean;
            value?: string | number;
            values?: {
                startDate: string;
                endDate: string;
                oid: string;
                value: string | number;
            }[]
        }
    }

}

export class ObjectAtTime {
    label?: string;
    code: string;
    data: {
        [key: string]: string | number;
    }

}

