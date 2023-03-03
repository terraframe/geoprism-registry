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

export class BusinessType implements AttributedType {

    oid?: string;
    code: string;
    organization: string;
    organizationLabel?: string;
    displayLabel: LocalizedValue;
    description: LocalizedValue;
    attributes?: Array<AttributeType>;
    labelAttribute?: string;

}

export class BusinessTypeByOrg {

    oid: string;
    code: string;
    label: string;
    types: BusinessType[];
    write: boolean;

}

export class BusinessObject {

    code: string;
    label: string;
    data: {
        [key: string]: string | number;
    }

}
