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

export class SourceAuthority {
    oid?: string;
    code: string;
    label: LocalizedValue;
    description: LocalizedValue;
    authorityType?: string;
}

export class DataSource {
    oid?: string;
    code: string;
    label: LocalizedValue;
    description: LocalizedValue;
    uri?: string;
    governanceLevel?: "AUTHORITATIVE" | "OFFICIAL" | "COMMUNITY" | "RESEARCH" | "DERIVED" | "EXPERIMENTAL" | "AD_HOC";
    metadataProfile?: "DCAT" | "GEO_DCAT" | "ISO19115" | "STAC" | "SENSOR_ML" | "FHIR" | "DATA_CITE" | "AD_HOC" | "CUSTOM" | "NONE" ;
    authority: string;
}